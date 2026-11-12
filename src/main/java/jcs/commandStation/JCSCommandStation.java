/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.commandStation;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.MeasurementEventListener;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.Protocol;
import jcs.entities.FunctionBean;
import jcs.commandStation.entities.InfoBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.automation.RailController;
import jcs.commandStation.lifecycle.ConnectionLifecycleManager;
import jcs.commandStation.lifecycle.ConnectionState;
import jcs.commandStation.lifecycle.ConnectionStateListener;
import jcs.commandStation.marklin.cs.MarklinCentralStationImpl;
import jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl;
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.commandStation.events.LocomotiveEvent;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.PowerEvent;
import jcs.entities.AccessoryBean.SignalValue;

/**
 * The JCSCommandStation is the layer between the UI, engines and Command stations
 */
public class JCSCommandStation {

  private volatile DecoderController decoderController;
  private Map<String, AccessoryController> accessoryControllers;
  private Map<String, FeedbackController> feedbackControllers;

  private final List<ConnectionEventListener> connectionEventListeners;
  private final List<PowerEventListener> powerEventListeners;

  private final List<AllSensorEventsListener> allSensorEventsListeners;
  private final Map<Integer, List<SensorEventListener>> sensorListeners;

  private final Map<String, List<AccessoryEventListener>> accessoryEventListeners;

  private final Map<Long, List<LocomotiveFunctionEventListener>> locomotiveFunctionEventListeners;
  private final Map<Long, List<LocomotiveDirectionEventListener>> locomotiveDirectionEventListeners;
  private final Map<Long, List<LocomotiveSpeedEventListener>> locomotiveSpeedEventListeners;

  private final List<MeasurementEventListener> measurementEventListeners;

  private final Set<Protocol> supportedProtocols;
  private CommandStationBean commandStation;

  private ExecutorService executor;
  // One lifecycle manager for the default command station (not per controller role).
  private volatile ConnectionLifecycleManager connectionLifecycleManager;
  private final List<ConnectionStateListener> connectionStateListeners
          = new java.util.concurrent.CopyOnWriteArrayList<>();
  private volatile boolean shuttingDown;
  private volatile boolean cleaningUpControllers;

  private final BlockingQueue<SensorEvent> sensorEventQueue;
  private final BlockingQueue<AccessoryEvent> accessoryEventQueue;
  private final BlockingQueue<LocomotiveEvent> locomotiveEventQueue;

  private EventHandlerThread<SensorEvent> sensorEventHandlerThread;
  private EventHandlerThread<AccessoryEvent> accessoryEventHandlerThread;
  private EventHandlerThread<LocomotiveEvent> locomotiveEventHandlerThread;

  private MeasurementEventHandler measurementEventHandler;

  private final AtomicBoolean powerEventRunning = new AtomicBoolean(false);

  private ThreadGroup threadGroup;

  private final Object lock = new Object();

  private boolean signalsRestored = false;
  // Track power is cached while the transport is healthy. Querying the driver
  // after TCP failure can incorrectly report OFF and lose the operator's setting.
  private volatile boolean lastKnownPowerOn;
  private volatile boolean powerWasOnBeforeLoss;
  private volatile boolean connectedSession;
  private volatile boolean recoveryStopPending;

  /**
   * Wrapper around the "real" CommandStation implementation.<br>
   * Operations to commandStations should not be performed in the EventDispatch thread.<br>
   * Operations to commandStations are performed in a worker thread to avoid blocking the EventDispatch thread.<br>
   */
  public JCSCommandStation() {
    this("true".equalsIgnoreCase(System.getProperty("skip.controller.autoconnect", "true")));
  }

  private JCSCommandStation(boolean flag) {
    threadGroup = new ThreadGroup("JCS-CS");
    executor = Executors.newCachedThreadPool(runnable -> new Thread(threadGroup, runnable, "JCS-WORKER"));

    accessoryControllers = new HashMap<>();
    feedbackControllers = new HashMap<>();
    connectionEventListeners = new LinkedList<>();
    powerEventListeners = new LinkedList<>();

    allSensorEventsListeners = new LinkedList<>();
    sensorListeners = new HashMap<>();
    accessoryEventListeners = new HashMap<>();
    measurementEventListeners = new LinkedList<>();

    locomotiveFunctionEventListeners = new HashMap<>();
    locomotiveDirectionEventListeners = new HashMap<>();
    locomotiveSpeedEventListeners = new HashMap<>();
    supportedProtocols = new HashSet<>();
    locomotiveEventQueue = new LinkedBlockingQueue<>();
    accessoryEventQueue = new LinkedBlockingQueue<>();

    sensorEventQueue = new LinkedBlockingQueue<>();

//    sensorEventHandlerThread = new EventHandlerThread<>(threadGroup, "SENSOR-EVENT-HANDLER", sensorEventQueue, this::handleSensorEvent);
//    accessoryEventHandlerThread = new EventHandlerThread<>(threadGroup, "ACCESSORY-EVENT-HANDLER", accessoryEventQueue, this::handleAccessoryEvent);
//    locomotiveEventHandlerThread = new EventHandlerThread<>(threadGroup, "LOCOMOTIVE-EVENT-HANDLER", locomotiveEventQueue, this::handleLocomotiveEvent);
    try {
      if (decoderController != null && (decoderController.getCommandStationBean() != null || !accessoryControllers.isEmpty() || !feedbackControllers.isEmpty())) {
        if (flag) {
          connect();
        }
        if (decoderController != null) {
          Logger.trace("Acquired {}, {}.", decoderController.getClass().getSimpleName(), (decoderController.isConnected() ? "Connected" : "NOT Connected"));
        } else {
          Logger.trace("Could not aquire a Command Station! NOT Connected.");
        }
      }

      if (flag) {
        connectInBackground();
      }
    } catch (Exception e) {
      Logger.warn("Can't connect with default Command Station!");
    }
  }

  public void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  void wakeUp() {
    synchronized (lock) {
      lock.notify();
    }
  }

  /**
   * Connect asynchronously. The default Märklin station uses the shared lifecycle manager. Other drivers continue to use the existing path until migrated.
   */
  private static boolean supportsManagedLifecycle(CommandStationBean bean) {
    if (bean == null || bean.getClassName() == null) {
      return false;
    }
    return MarklinCentralStationImpl.class.getName().equals(bean.getClassName())
            || EsuEcosCommandStationImpl.class.getName().equals(bean.getClassName());
  }

  private boolean lifecycleReady(DecoderController controller) {
    ConnectionLifecycleManager manager = connectionLifecycleManager;
    if (controller instanceof MarklinCentralStationImpl
            || controller instanceof EsuEcosCommandStationImpl) {
      return manager == null || manager.getState() == ConnectionState.CONNECTED;
    }
    return true;
  }

  public final void connectInBackground() {
    if (shuttingDown) {
      return;
    }
    CommandStationBean bean = getCommandStationBean();
    if (supportsManagedLifecycle(bean)) {
      getOrCreateLifecycleManager(bean).start();
      return;
    }

    // Existing behavior for command-station implementations not yet migrated.
    if (executor == null || executor.isShutdown()) {
      executor = Executors.newCachedThreadPool(runnable -> new Thread(threadGroup, runnable, "JCS-WORKER"));
    }
    long start = System.currentTimeMillis();
    executor.execute(() -> {
      try {
        if (connect()) {
          long end = System.currentTimeMillis();
          Logger.trace("Connected to " + decoderController.getCommandStationBean().getDescription()
                  + " in " + (end - start) + " ms");
          if (!isVirtual()) {
            ConnectionEvent ce = new ConnectionEvent(commandStation.getDescription(), true, isVirtual());
            for (ConnectionEventListener cel : connectionEventListeners) {
              cel.onConnectionChange(ce);
            }
          }
        } else {
          if (!isVirtual()) {
            ConnectionEvent ce = new ConnectionEvent(commandStation.getDescription(), false, isVirtual());
            for (ConnectionEventListener cel : connectionEventListeners) {
              cel.onConnectionChange(ce);
            }
          }
        }

        // Existing track-power policy for non-migrated drivers.
        switchPower(true);
        boolean p = isPowerOn();
        PowerEvent pe = new PowerEvent(p);
        for (PowerEventListener pl : powerEventListeners) {
          pl.onPowerChange(pe);
        }
      } catch (Exception e) {
        Logger.error(e.getMessage());
      }
    });
  }

  private synchronized ConnectionLifecycleManager getOrCreateLifecycleManager(CommandStationBean bean) {
    if (connectionLifecycleManager != null) {
      return connectionLifecycleManager;
    }
    final ConnectionLifecycleManager manager = new ConnectionLifecycleManager(
            bean.getId(),
            () -> connect() && isConnected(),
            this::disconnectControllers);

    manager.addConnectionStateListener(event -> {
      for (ConnectionStateListener listener : connectionStateListeners) {
        try {
          listener.onConnectionStateChanged(event);
        } catch (RuntimeException ex) {
          Logger.warn("Connection state listener failed: " + ex.getMessage());
        }
      }
      ConnectionState state = event.state();
      if (state == ConnectionState.RECONNECTING || state == ConnectionState.DISCONNECTED) {
        // Capture the last *confirmed* power value, not isPowerOn() on a
        // broken TCP connection. Only a previously connected session needs
        // emergency recovery; an initial failed connect does not.
        if (connectedSession && !recoveryStopPending) {
          powerWasOnBeforeLoss = lastKnownPowerOn;
          recoveryStopPending = true;
          Logger.warn("Connection lost: will stop all locomotives on reconnect; previous track power={}",
                  powerWasOnBeforeLoss);
        }
        connectedSession = false;
        // Preserve dispatchers and railway state when the CS vanishes.
        if (recoveryStopPending) {
          RailController.getInstance().suspendForConnectionLoss();
        }
      } else if (state == ConnectionState.CONNECTED) {
        connectedSession = true;
      }
      if (state == ConnectionState.CONNECTED
              || state == ConnectionState.RECONNECTING
              || state == ConnectionState.DISCONNECTED) {
        notifyConnectionListeners(new ConnectionEvent(
                bean.getDescription(), state == ConnectionState.CONNECTED, isVirtual()));
      }
    });
    connectionLifecycleManager = manager;
    return manager;
  }

  public void addConnectionStateListener(ConnectionStateListener listener) {
    connectionStateListeners.add(java.util.Objects.requireNonNull(listener));
  }

  public void removeConnectionStateListener(ConnectionStateListener listener) {
    connectionStateListeners.remove(listener);
  }

  public ConnectionState getConnectionState() {
    ConnectionLifecycleManager manager = connectionLifecycleManager;
    if (manager != null) {
      return manager.getState();
    }
    return isConnected() ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED;
  }

  public final synchronized boolean connect() {
    boolean decoderControllerConnected = false;
    boolean alreadyConnected = false;

    if (executor.isShutdown()) {
      Logger.trace("Restarting executers...");
      executor = Executors.newCachedThreadPool(runnable -> new Thread(threadGroup, runnable, "JCS-WORKER"));
    }

    //Check if already connected to avoid duplication....
    if (commandStation != null && decoderController != null) {
      decoderControllerConnected = decoderController.isConnected();
      alreadyConnected = true;
      Logger.trace(decoderController.getClass().getName() + " already connected...");
    } else {
      commandStation = PersistenceFactory.getService().getDefaultCommandStation();
    }

    int accessoryCntrConnected = 0;
    int feedbackCntrConnected = 0;

    if (commandStation == null) {
      Logger.error("No Default Command Station found!");
      return false;
    }

    if (decoderController == null && commandStation != null) {
      decoderController = ControllerFactory.getDecoderController(commandStation, false);
    }

    if (decoderController instanceof MarklinCentralStationImpl marklin) {
      // Do not tear down TCP resources on its receiver thread; the manager owns cleanup.
      ConnectionLifecycleManager manager = connectionLifecycleManager;
      marklin.setLifecycleManaged(manager != null
              && manager.getState() != ConnectionState.DISCONNECTED
              && manager.getState() != ConnectionState.STOPPING);
    }

    if (decoderController instanceof EsuEcosCommandStationImpl ecos) {
      // Must be set BEFORE transport opens: the first command on a recovered
      // session must be STOP, before any subscriptions or roster reads.
      ecos.setRecoveryMode(recoveryStopPending);
    }

    if (decoderController == null) {
      Logger.error("No DecoderController configured!");
      return false;
    } else {
      supportedProtocols.addAll(decoderController.getCommandStationBean().getSupportedProtocols());
    }

    if (accessoryControllers.isEmpty()) {
      List<AccessoryController> acl = ControllerFactory.getAccessoryControllers();
      for (AccessoryController ac : acl) {
        accessoryControllers.put(ac.getCommandStationBean().getId(), ac);
      }
    }

    //TODO: a warning log in the main screen
    if (accessoryControllers.isEmpty()) {
      Logger.warn("No Accessory Controllers configured!");
    }

    if (feedbackControllers.isEmpty()) {
      List<FeedbackController> fcl = ControllerFactory.getFeedbackControllers();
      for (FeedbackController fc : fcl) {
        feedbackControllers.put(fc.getCommandStationBean().getId(), fc);
      }
    }

    //TODO: a warning log in the main screen
    if (feedbackControllers.isEmpty()) {
      Logger.warn("No Feedback Controllers configured!");
    }

    // The decoder, accessory and feedback roles can share the same physical
    // controller (e.g. a single Märklin CS). An unsuccessful connect() must not
    // be repeated for each role during the same lifecycle attempt.
    Map<GenericController, Boolean> connectionAttempts = new java.util.IdentityHashMap<>();
    decoderControllerConnected = connectControllerOnce(decoderController, connectionAttempts);

    //Connect the Accessories controllers if needed
    if (!accessoryControllers.isEmpty() && !alreadyConnected) {
      for (AccessoryController ac : accessoryControllers.values()) {
        try {
          if (connectControllerOnce(ac, connectionAttempts)) {
            accessoryCntrConnected++;
          }
        } catch (Exception e) {
          Logger.warn("Can't connect to accessory controller "
                  + ac.getCommandStationBean().getDescription() + ": " + e.getMessage());
        }
      }
    }

    //Connect the Feedback Controllers controllers if needed
    if (!feedbackControllers.isEmpty() && !alreadyConnected) {
      for (FeedbackController fc : feedbackControllers.values()) {
        try {
          if (connectControllerOnce(fc, connectionAttempts)) {
            fc.addAllSensorEventsListener(new AllSensorEventsHandler(this));
            // A single physical station can implement decoder, feedback and
            // accessory roles. Its decoder listener is installed below.
            if (fc != decoderController && fc.getConnectionEventListeners().isEmpty()) {
              fc.addConnectionEventListener(new ControllerConnectionListener(this, null));
            }
            feedbackCntrConnected++;
          }
        } catch (Exception e) {
          Logger.warn("Can't connect to feedback controller "
                  + fc.getCommandStationBean().getDescription() + ": " + e.getMessage());
        }
      }
    }

    if (decoderController != null && decoderController.isConnected()) {
      startLocomotiveEventHandler();
    }

    if (accessoryCntrConnected > 0) {
      startAccessoryEventHandler();
    }

    if (feedbackCntrConnected > 0) {
      startSensorEventHandler();
    }

    Logger.debug("Connected Controllers:  Decoder: " + (decoderControllerConnected ? "Yes" : "No") + " Accessory: " + accessoryCntrConnected + " Feedback: " + feedbackCntrConnected);

    if (decoderControllerConnected && !alreadyConnected && decoderController != null) {
      decoderController.addConnectionEventListener(new ControllerConnectionListener(this, decoderController));

      decoderController.addPowerEventListener(new ControllerPowerListener(this));

      decoderController.addLocomotiveFunctionEventListener(new LocomotiveFunctionChangeEventListener(this));
      decoderController.addLocomotiveDirectionEventListener(new LocomotiveDirectionChangeEventListener(this));
      decoderController.addLocomotiveSpeedEventListener(new LocomotiveSpeedChangeEventListener(this));

      measurementEventHandler = new MeasurementEventHandler(this);
      decoderController.addMeasurementEventListener(measurementEventHandler);
    }

    if (accessoryCntrConnected > 0 && !alreadyConnected) {
      for (AccessoryController ac : accessoryControllers.values()) {
        if (ac.isConnected()) {
          ac.addAccessoryEventListener(new AccessoryChangeEventListener(this));

          if (ac != decoderController && ac.getConnectionEventListeners().isEmpty()) {
            ac.addConnectionEventListener(new ControllerConnectionListener(this, null));
          }
        }
      }
    }

    if (!decoderControllerConnected) {
      return false;
    }

    // A TCP reconnect is NOT an ordinary startup. Never announce CONNECTED
    // before zero-speed commands have been issued to the station.
    boolean wasRecovering = recoveryStopPending;
    if (wasRecovering) {
      if (!stopTrainsAfterReconnect(decoderController, powerWasOnBeforeLoss)) {
        Logger.warn("Reconnect safety stop did not complete; retrying connection");
        return false;
      }
      recoveryStopPending = false;
    }

    // Managed Märklin connections do not use connectInBackground()'s legacy
    // switchPower(true). Initial startup must request GO here, before the
    // lifecycle manager publishes CONNECTED. Never do this on recovery.
    if (!wasRecovering && decoderController instanceof MarklinCentralStationImpl) {
      Logger.info("Initial connection: requesting track power ON");
      if (!decoderController.power(true) || !decoderController.isPower()) {
        Logger.warn("Initial connection established, but track power could not be switched on");
      }
    }

    boolean power = isPowerOn();
    lastKnownPowerOn = power;
    PowerEvent pe = new PowerEvent(power);
    for (PowerEventListener pl : powerEventListeners) {
      pl.onPowerChange(pe);
    }

    // Restoring saved signal aspects can set a proceed signal. Do not restore
    // them automatically after an unexpected loss of sensor/position tracking.
    if (!wasRecovering && !signalsRestored && power) {
      restoreSignalValues();
    } else if (wasRecovering) {
      Logger.warn("Signal restoration deferred after reconnect; verify locomotive positions and routes");
    }

    return true;
  }

  /**
   * Fail-safe recovery for ALL DecoderController implementations. The command station must support power(boolean) and changeVelocity(...). Never write a nonzero speed during reconnection.
   *
   * STOP first removes track energy. Sending zero while stopped gives drivers which accept speed changes in STOP mode a chance to clear their cached speeds before GO. Re-send zero after GO, because
   * not all drivers do.
   *
   * Returns false if the requested power-on state could not be confirmed.
   */
  private boolean stopTrainsAfterReconnect(DecoderController controller, boolean restorePower) {
    Logger.warn("Reconnected after connection loss: stopping locomotives; previous track power={}",
            restorePower);
    try {
      // Immediately remove track energy before HTTP/CAN locomotive enumeration.
      // Reading the locomotive roster can take seconds; postponing STOP until
      // afterwards could let an engine resume during that interval.
      // The ECoS driver already sent STOP as the first command on the
      // reconnected TCP session. Its recovery method verifies STOP again
      // before touching locomotive speeds. Do not send a third STOP here.
      // Märklin and other drivers still require this immediate command.
      if (!(controller instanceof EsuEcosCommandStationImpl)) {
        controller.power(false);
      }

      // An active locomotive may not be assigned to a layout block. Include
      // all station locomotives, not only JCS's "on track" selection.
      Map<Integer, LocomotiveBean> byAddress = new java.util.LinkedHashMap<>();
      List<LocomotiveBean> known = controller.getLocomotives();
      if (known != null) {
        for (LocomotiveBean locomotive : known) {
          if (locomotive != null) {
            byAddress.put(resolveAddress(locomotive), locomotive);
          }
        }
      }
      List<LocomotiveBean> onTrack = PersistenceFactory.getService().getOnTrackLocomotives();
      if (onTrack != null) {
        for (LocomotiveBean locomotive : onTrack) {
          if (locomotive != null
                  && (!(controller instanceof EsuEcosCommandStationImpl)
                  || "esu-ecos".equals(locomotive.getCommandStationId()))) {
            byAddress.put(resolveAddress(locomotive), locomotive);
          }
        }
      }
      if (controller instanceof EsuEcosCommandStationImpl ecos) {
        // Different from Märklin: NEVER restore GO after unexpected loss.
        // Every locomotive speed is checked against a fresh ECoS reply.
        Logger.warn("ECoS recovery: previous power was {}, leaving track power OFF",
                restorePower ? "ON" : "OFF");
        return ecos.stopLocomotivesForRecovery(new java.util.ArrayList<>(byAddress.keySet()));
      }
      if (controller instanceof MarklinCentralStationImpl marklin) {
        // Raw CAN zero-speed commands must also be sent with track power OFF.
        // MarklinCentralStationImpl.changeVelocity() deliberately gates normal
        // commands on power, so that API cannot implement STOP -> zero -> GO.
        return marklin.stopLocomotivesForRecovery(
                new java.util.ArrayList<>(byAddress.keySet()), restorePower);
      }
      return safelyRestoreAfterReconnect(controller, restorePower,
              () -> new java.util.ArrayList<>(byAddress.values()), this::resolveAddress);
    } catch (RuntimeException ex) {
      Logger.error("Unable to enumerate locomotives for reconnect safety: {}", ex.getMessage());
      // Never enable power after a failed recovery.
      try {
        controller.power(false);
      } catch (RuntimeException stopEx) {
        Logger.error("Emergency recovery STOP failed: {}", stopEx.getMessage());
      }
      return false;
    }
  }

  /**
   * Package-visible for regression tests. This order is deliberate: STOP -> zero speeds -> optional GO -> zero speeds. No work is submitted to the disposable JCS-WORKER executor.
   */
  static boolean safelyRestoreAfterReconnect(
          DecoderController controller,
          boolean restorePower,
          java.util.function.Supplier<List<LocomotiveBean>> locomotivesSupplier,
          java.util.function.ToIntFunction<LocomotiveBean> addressResolver) {
    try {
      controller.power(false);
      if (controller.isPower()) {
        Logger.warn("STOP could not be confirmed; refusing to restore track power");
        return false;
      }
      List<LocomotiveBean> locomotives = locomotivesSupplier.get();
      if (locomotives == null) {
        Logger.warn("Could not enumerate locomotives for recovery stop");
        return false;
      }
      sendZeroVelocityForRecovery(controller, locomotives, addressResolver);

      if (restorePower) {
        if (!controller.power(true) || !controller.isPower()) {
          Logger.warn("Could not restore track power; keeping reconnect recovery pending");
          return false;
        }
        sendZeroVelocityForRecovery(controller, locomotives, addressResolver);
      } else if (controller.isPower()) {
        Logger.warn("Command station reports track power ON after requested STOP");
        return false;
      }
      Logger.warn("Reconnect zero-speed commands dispatched for {} locomotives", locomotives.size());
      return true;
    } catch (RuntimeException ex) {
      Logger.error("Reconnect safety stop failed: {}", ex.getMessage());
      return false;
    }
  }

  /**
   * Synchronous, no executor: called by the lifecycle connection worker.
   */
  static void sendZeroVelocityForRecovery(
          DecoderController controller,
          List<LocomotiveBean> locomotives,
          java.util.function.ToIntFunction<LocomotiveBean> addressResolver) {
    for (LocomotiveBean locomotive : locomotives) {
      if (locomotive == null) {
        continue;
      }
      controller.changeVelocity(addressResolver.applyAsInt(locomotive),
              0, locomotive.getDirection());
    }
  }

  /**
   * Connect a physical controller at most once per outer connect() invocation. Identity semantics are intentional: one controller object may implement DecoderController, AccessoryController and
   * FeedbackController.
   *
   * Failures are cached too, so a failed TCP connect is not retried for each role. The ConnectionLifecycleManager owns subsequent retry attempts.
   */
  static boolean connectControllerOnce(GenericController controller,
          Map<GenericController, Boolean> attempts) {
    Boolean priorResult = attempts.get(controller);
    if (priorResult != null) {
      return priorResult;
    }
    boolean connected = controller.isConnected() || controller.connect();
    attempts.put(controller, connected);
    return connected;
  }

  private synchronized void startLocomotiveEventHandler() {
    if (locomotiveEventHandlerThread == null || locomotiveEventHandlerThread.getState() == Thread.State.TERMINATED) {
      locomotiveEventHandlerThread = new EventHandlerThread<>(threadGroup, "LOCOMOTIVE-EVENT-HANDLER", locomotiveEventQueue, this::handleLocomotiveEvent);
    }

    if (locomotiveEventHandlerThread.getState() == Thread.State.NEW) {
      locomotiveEventHandlerThread.start();
    }
  }

  private synchronized void startAccessoryEventHandler() {
    if (accessoryEventHandlerThread == null || accessoryEventHandlerThread.getState() == Thread.State.TERMINATED) {
      accessoryEventHandlerThread = new EventHandlerThread<>(threadGroup, "ACCESSORY-EVENT-HANDLER", accessoryEventQueue, this::handleAccessoryEvent);
    }

    if (accessoryEventHandlerThread.getState() == Thread.State.NEW) {
      accessoryEventHandlerThread.start();
    }
  }

  private synchronized void startSensorEventHandler() {
    if (sensorEventHandlerThread == null || sensorEventHandlerThread.getState() == Thread.State.TERMINATED) {
      sensorEventHandlerThread = new EventHandlerThread<>(threadGroup, "SENSOR-EVENT-HANDLER", sensorEventQueue, this::handleSensorEvent);
    }

    if (sensorEventHandlerThread.getState() == Thread.State.NEW) {
      sensorEventHandlerThread.start();
    }
  }

  public CommandStationBean getCommandStationBean() {
    if (decoderController != null) {
      return decoderController.getCommandStationBean();
    } else {
      return PersistenceFactory.getService().getDefaultCommandStation();
    }
  }

  public boolean isConnected() {
    if (decoderController != null) {
      return decoderController.isConnected();
    } else {
      return false;
    }
  }

  private void stopEventHandler(EventHandlerThread<?> thread) {
    if (thread == null) {
      return;
    }

    thread.quit();

    try {
      thread.join(1000);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Intentional user disconnect: cancels retries but permits a later reconnect.
   */
  public void disconnect() {
    connectedSession = false;
    recoveryStopPending = false;
    ConnectionLifecycleManager manager = connectionLifecycleManager;
    if (manager != null) {
      manager.stop();
    } else {
      disconnectControllers();
    }
  }

  /**
   * Terminal application shutdown, preserving the legacy no-extra-STOP API.
   */
  public void shutdown() {
    shutdown(false);
  }

  /**
   * Shutdown with an optional synchronous track STOP. Invoked before the lifecycle manager closes the TCP session. A failed confirmation is logged; no GO is ever attempted.
   */
  public void shutdown(boolean stopTrackPower) {
    shuttingDown = true; // Reject new queued user commands before STOP.
    connectedSession = false;
    recoveryStopPending = false;
    try {
      if (stopTrackPower) {
        DecoderController controller = decoderController;
        if (controller == null || !controller.isConnected()) {
          Logger.warn("Shutdown STOP not confirmed: command station unavailable");
        } else {
          boolean stopped;
          if (controller instanceof EsuEcosCommandStationImpl ecos) {
            stopped = ecos.stopTrackPowerForShutdown();
          } else {
            controller.power(false);
            stopped = controller.isConnected() && !controller.isPower();
          }
          if (stopped) {
            Logger.info("Shutdown track STOP confirmed before disconnect");
          } else {
            Logger.warn("Shutdown track STOP NOT confirmed; disconnecting anyway");
          }
        }
      }
    } catch (RuntimeException ex) {
      Logger.error("Shutdown STOP failed: {}", ex.getMessage());
    } finally {
      ConnectionLifecycleManager manager = connectionLifecycleManager;
      if (manager != null) {
        manager.close();
      } else {
        disconnectControllers();
      }
    }
  }

  /**
   * Called by the manager's worker; must not call manager.stop() recursively.
   */
  private synchronized void disconnectControllers() {
    if (cleaningUpControllers) {
      return;
    }
    cleaningUpControllers = true;
    try {
      Logger.debug("Disconnecting controllers...");
      for (FeedbackController fc : feedbackControllers.values()) {
        if (fc != decoderController) {
          fc.disconnect();
        }
      }
      for (AccessoryController ac : accessoryControllers.values()) {
        if (ac != decoderController) {
          ac.disconnect();
        }
      }

      stopEventHandler(locomotiveEventHandlerThread);
      stopEventHandler(accessoryEventHandlerThread);
      stopEventHandler(sensorEventHandlerThread);

      locomotiveEventHandlerThread = null;
      accessoryEventHandlerThread = null;
      sensorEventHandlerThread = null;

      if (decoderController != null) {
        if (measurementEventHandler != null) {
          decoderController.removeMeasurementEventListener(measurementEventHandler);
        }
        measurementEventHandler = null;

        decoderController.disconnect();
      }

      accessoryControllers.clear();
      feedbackControllers.clear();
      decoderController = null;
      commandStation = null;
      ControllerFactory.reset();

      // All controller callbacks have finished; no further work should be accepted.
      if (executor != null) {
        executor.shutdown();
      }
    } finally {
      cleaningUpControllers = false;
    }
  }

  private void notifyConnectionListeners(final ConnectionEvent connectionEvent) {
    if (!shuttingDown && !isVirtual()) {
      if (!connectionEventListeners.isEmpty()) {
        if (executor == null || executor.isShutdown()) {
          return;
        }
        try {
          executor.execute(() -> {
            for (ConnectionEventListener cel : connectionEventListeners) {
              cel.onConnectionChange(connectionEvent);
            }
          });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
          // Connection notification raced with intentional shutdown.
        }
      }
    }
  }

  private void notifyPowerListeners(final PowerEvent powerEvent) {
    try {

      if (powerEventRunning.compareAndSet(false, true)) {
        try {
          if (!powerEventListeners.isEmpty()) {
            if (executor == null || executor.isShutdown()) {
              return;
            }
            executor.execute(() -> {
              for (PowerEventListener pel : powerEventListeners) {
                pel.onPowerChange(powerEvent);
              }
            });
          }
        } finally {
          powerEventRunning.set(false);
        }
      }
    } catch (java.util.concurrent.RejectedExecutionException ignored) {
      // A callback raced with executor shutdown; normal during disconnect.
    }
  }

  public void setVirtual(boolean flag) {
    Logger.info("Switch Virtual Mode " + (flag ? "On" : "Off"));
    CommandStationBean bean = getCommandStationBean();
    if (bean == null) {
      Logger.warn("No command station configured for virtual mode");
      return;
    }

    ConnectionLifecycleManager manager = connectionLifecycleManager;
    if (manager != null && supportsManagedLifecycle(bean)) {
      // The mode change replaces the transport, so suspend retries first.
      manager.stop();
      bean.setVirtual(flag);
      PersistenceFactory.getService().persist(bean);
      manager.start();
    } else {
      bean.setVirtual(flag);
      PersistenceFactory.getService().persist(bean);
      if (decoderController != null) {
        decoderController.setVirtual(flag);
      }
    }
  }

  public boolean isVirtual() {
    if (decoderController != null) {
      return decoderController.isVirtual();
    } else {
      return false;
    }
  }

  public boolean isSupportVNC() {
    //TODO: Make a property in the CommandStationBean for this
    return commandStation != null && ("marklin.cs".equals(commandStation.getId()) || "esu-ecos".equals(commandStation.getId()));
  }

  public boolean isSupportTrackMeasurements() {
    return commandStation != null && decoderController != null && decoderController.isSupportTrackMeasurements();
  }

  public Image getLocomotiveImage(String imageName) {
    Image image = null;

    if (decoderController != null) {
      image = decoderController.getLocomotiveImage(imageName);
      if (image != null) {
        storeImage(image, imageName, true);
      }
    }
    return image;
  }

  public Image getLocomotiveFunctionImage(String imageName) {
    Image image = null;
    if (decoderController != null) {
      image = decoderController.getLocomotiveFunctionImage(imageName);
      if (image != null) {
        storeImage(image, imageName, false);
      }
    }
    return image;
  }

  private void storeImage(Image image, String imageName, boolean locomotive) {
    Path path;
    String csp = null;
    if (decoderController != null) {
      csp = this.decoderController.getCommandStationBean().getLastUsedSerial();
      if (csp == null) {
        csp = this.decoderController.getCommandStationBean().getId();
      }
    }

    String basePath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + csp;

    if (locomotive) {
      path = Paths.get(basePath);
    } else {
      path = Paths.get(basePath + File.separator + "functions");
    }

    File imageFile = new File(path + File.separator + imageName.toLowerCase() + ".png");

    try {
      if (!Files.exists(path)) {
        Files.createDirectories(path);
        Logger.trace("Created new directory " + path);
      }
      ImageIO.write((BufferedImage) image, "png", imageFile);
    } catch (IOException ex) {
      Logger.error("Can't store image " + imageFile + "! ", ex.getMessage());
    }
    Logger.trace("Stored image " + imageName + ".png in the cache");
  }

  //Restore the signal values, as most new decoders do not remember the last state
  void restoreSignalValues() {
    List<AccessoryBean> signals = PersistenceFactory.getService().getSignals();

    for (AccessoryBean signal : signals) {
      switchAccessory(signal, signal.getSignalValue());
    }

    this.signalsRestored = true;
    Logger.debug("Restored the signalValue For " + signals.size() + " signals...");
  }

  public InfoBean getCommandStationInfo() {
    if (decoderController != null) {
      return decoderController.getCommandStationInfo();
    } else {
      return null;
    }
  }

  public String getCommandStationName() {
    if (decoderController != null && decoderController.getCommandStationInfo() != null) {
      return decoderController.getCommandStationInfo().getProductName();
    } else if (decoderController != null && decoderController.getCommandStationBean() != null) {
      return decoderController.getCommandStationBean().getDescription();
    } else {
      return null;
    }
  }

  public String getCommandStationSerialNumber() {
    if (decoderController != null && decoderController.getCommandStationInfo() != null) {
      return decoderController.getCommandStationInfo().getSerialNumber();
    } else {
      return null;
    }
  }

  public String getCommandStationArticleNumber() {
    if (decoderController != null && decoderController.getCommandStationInfo() != null) {
      return decoderController.getCommandStationInfo().getArticleNumber();
    } else {
      return null;
    }
  }

  public void switchPower(boolean on) {
    DecoderController controller = decoderController;
    // No power changes while the old worker has terminated or recovery is
    // in progress. In particular, never queue a stale GO for after recovery.
    if (shuttingDown || controller == null || !controller.isConnected()
            || recoveryStopPending || !lifecycleReady(controller)) {
      Logger.debug("Ignoring track power request: command station not ready");
      return;
    }
    Runnable task = () -> {
      if (controller != decoderController || !controller.isConnected()
              || recoveryStopPending || shuttingDown
              || !lifecycleReady(controller)) {
        return;
      }
      controller.power(on);
      lastKnownPowerOn = controller.isPower();
    };
    if (!SwingUtilities.isEventDispatchThread()) {
      task.run();
    } else {
      ExecutorService pool = executor;
      if (pool == null || pool.isShutdown()) {
        Logger.debug("Ignoring track power request: worker is stopped");
        return;
      }
      try {
        pool.execute(task);
      } catch (java.util.concurrent.RejectedExecutionException ignored) {
        // A connection loss can shut down the worker between the check and execute().
        Logger.debug("Track power request cancelled by disconnect");
      }
    }
  }

  public boolean isPowerOn() {
    boolean power = false;
    if (decoderController != null) {
      power = decoderController.isPower();
    }
    return power;
  }

  private int resolveAddress(LocomotiveBean locomotive) {
    int address;

    if ("marklin.cs".equals(locomotive.getCommandStationId()) || "esu-ecos".equals(locomotive.getCommandStationId())) {
      address = locomotive.getId().intValue();
    } else {
      //TODO: check this probably not needed anymore
      if (supportedProtocols.size() == 1) {
        address = locomotive.getAddress();
      } else {
        if (locomotive.getUid() != null) {
          address = locomotive.getUid().intValue();
        } else {
          address = locomotive.getId().intValue();
        }
      }
    }
    return address;
  }

  public void changeLocomotiveDirection(Direction newDirection, LocomotiveBean locomotive) {
    DecoderController controller = decoderController;
    if (controller == null || !controller.isConnected() || shuttingDown
            || recoveryStopPending || !lifecycleReady(controller)) {
      Logger.debug("Ignoring locomotive direction while command station is unavailable");
      return;
    }
    int address = resolveAddress(locomotive);
    Runnable task = () -> {
      if (shuttingDown || recoveryStopPending || controller != decoderController
              || !controller.isConnected() || !lifecycleReady(controller)) {
        return;
      }
      if (!(controller instanceof MarklinCentralStationImpl)) {
        controller.changeVelocity(address, 0, locomotive.getDirection());
      }
      controller.changeDirection(address, newDirection);
    };
    if (!SwingUtilities.isEventDispatchThread()) {
      task.run();
    } else if (executor != null && !executor.isShutdown()) {
      try {
        executor.execute(task);
      } catch (java.util.concurrent.RejectedExecutionException ignored) {
        Logger.debug("Locomotive direction cancelled by disconnect");
      }
    }
  }

  public void changeLocomotiveSpeed(Integer newVelocity, LocomotiveBean locomotive) {
    DecoderController controller = decoderController;
    if (controller == null || !controller.isConnected() || shuttingDown
            || recoveryStopPending || !lifecycleReady(controller)) {
      Logger.debug("Ignoring locomotive speed request while command station is unavailable");
      return;
    }
    Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());
    int address = resolveAddress(locomotive);
    Runnable task = () -> {
      if (!shuttingDown && !recoveryStopPending && controller == decoderController
              && controller.isConnected()
              && lifecycleReady(controller)) {
        controller.changeVelocity(address, newVelocity, locomotive.getDirection());
      }
    };
    if (!SwingUtilities.isEventDispatchThread()) {
      task.run();
    } else {
      ExecutorService pool = executor;
      if (pool == null || pool.isShutdown()) {
        return;
      }
      try {
        pool.execute(task);
      } catch (java.util.concurrent.RejectedExecutionException ignored) {
        Logger.debug("Locomotive speed request cancelled by disconnect");
      }
    }
  }

  public void haltAllOnTrackLocomotives() {
    List<LocomotiveBean> locomotivesOnTrack = PersistenceFactory.getService().getOnTrackLocomotives();

    for (LocomotiveBean loco : locomotivesOnTrack) {
      changeLocomotiveSpeed(0, loco);
    }

  }

  public void changeLocomotiveFunction(Boolean newValue, Integer functionNumber, LocomotiveBean locomotive) {
    DecoderController controller = decoderController;
    if (controller == null || !controller.isConnected() || shuttingDown
            || recoveryStopPending || !lifecycleReady(controller)) {
      Logger.debug("Ignoring locomotive function while command station is unavailable");
      return;
    }
    int address = resolveAddress(locomotive);
    Runnable task = () -> {
      if (!shuttingDown && !recoveryStopPending && controller == decoderController
              && controller.isConnected() && lifecycleReady(controller)) {
        controller.changeFunctionValue(address, functionNumber, newValue);
      }
    };
    if (!SwingUtilities.isEventDispatchThread()) {
      task.run();
    } else if (executor != null && !executor.isShutdown()) {
      try {
        executor.execute(task);
      } catch (java.util.concurrent.RejectedExecutionException ignored) {
        Logger.debug("Locomotive function cancelled by disconnect");
      }
    }
  }

  public void switchAccessory(AccessoryBean accessory, SignalValue sValue) {
    //Convert the signal value into a Accessory Value
    AccessoryValue aValue;
    switch (sValue) {
      case SignalValue.Hp0 ->
        aValue = AccessoryValue.RED;
      case SignalValue.Hp1 ->
        aValue = AccessoryValue.GREEN;
      case SignalValue.Hp0Sh1 ->
        aValue = AccessoryValue.WHITE;
      case SignalValue.Hp2 ->
        aValue = AccessoryValue.YELLOW;
      default ->
        aValue = AccessoryValue.OFF;
    }
    switchAccessory(accessory, aValue);
  }

  public void switchAccessory(AccessoryBean accessory, AccessoryValue value) {
    Integer address = accessory.getAddress();
    Integer switchTime = accessory.getSwitchTime();
    AccessoryBean.Protocol protocol = accessory.getProtocol();
    if (protocol == null) {
      protocol = AccessoryBean.Protocol.DCC;
    }

    Logger.trace("Changing accessory with address: {}, {} Name: {} to {}", address, protocol, accessory.getName(), value.getValue());
    changeAccessory(address, protocol.getValue(), value, switchTime);
  }

  private void changeAccessory(final Integer address, final String protocol, final AccessoryValue value, final Integer switchTime) {
    DecoderController controller = decoderController;
    if (controller == null || !controller.isConnected() || shuttingDown
            || recoveryStopPending || !lifecycleReady(controller)) {
      Logger.debug("Ignoring accessory switch while command station is unavailable");
      return;
    }
    Runnable task = () -> {
      if (shuttingDown || recoveryStopPending || controller != decoderController
              || !controller.isConnected() || !lifecycleReady(controller)) {
        return;
      }
      for (AccessoryController accessoryController : new ArrayList<>(accessoryControllers.values())) {
        if (accessoryController.isConnected()) {
          accessoryController.switchAccessory(address, protocol, value, switchTime);
        }
      }
    };
    if (!SwingUtilities.isEventDispatchThread()) {
      task.run();
    } else if (executor != null && !executor.isShutdown()) {
      try {
        executor.execute(task);
      } catch (java.util.concurrent.RejectedExecutionException ignored) {
        Logger.debug("Accessory command cancelled by disconnect");
      }
    }
  }

  public void addAllSensorEventsListener(AllSensorEventsListener listener) {
    allSensorEventsListeners.add(listener);
  }

  public void removeAllSensorEventsListener(AllSensorEventsListener listener) {
    allSensorEventsListeners.remove(listener);
  }

  public void addSensorEventListener(Integer sensorId, SensorEventListener listener) {
    if (sensorId == null) {
      Logger.warn("SensorId can't be null for listener " + listener.getSensorId());
    }
    if (sensorListeners.containsKey(sensorId)) {
      List<SensorEventListener> sameSensorListeners = sensorListeners.get(listener.getSensorId());
      sameSensorListeners.add(listener);
    } else {
      List<SensorEventListener> sameSensorListeners = new ArrayList<>();
      sameSensorListeners.add(listener);
      sensorListeners.put(sensorId, sameSensorListeners);
    }
  }

  public void removeSensorEventListener(Integer sensorId, SensorEventListener listener) {
    if (sensorListeners.containsKey(sensorId)) {
      List<SensorEventListener> sameSensorListeners = sensorListeners.get(sensorId);
      sameSensorListeners.remove(listener);
    }
  }

  public void addAccessoryEventListener(String accessoryId, AccessoryEventListener listener) {
    if (accessoryEventListeners.containsKey(accessoryId)) {
      List<AccessoryEventListener> sameAccessoryIdListeners = accessoryEventListeners.get(accessoryId);
      sameAccessoryIdListeners.add(listener);

      accessoryEventListeners.put(accessoryId, sameAccessoryIdListeners);
    } else {
      List<AccessoryEventListener> sameAccessoryIdListeners = new ArrayList<>();
      sameAccessoryIdListeners.add(listener);
      accessoryEventListeners.put(accessoryId, sameAccessoryIdListeners);
    }
  }

  public void removeAccessoryEventListener(String accessoryId, AccessoryEventListener listener) {
    if (accessoryEventListeners.containsKey(accessoryId)) {
      List<AccessoryEventListener> sameAccessoryListener = accessoryEventListeners.get(accessoryId);
      sameAccessoryListener.remove(listener);
    }
  }

  public void addLocomotiveFunctionEventListener(Long locomotiveId, LocomotiveFunctionEventListener listener) {
    if (locomotiveFunctionEventListeners.containsKey(locomotiveId)) {
      List<LocomotiveFunctionEventListener> sameFunctionEventListeners = locomotiveFunctionEventListeners.get(locomotiveId);
      sameFunctionEventListeners.add((LocomotiveFunctionEventListener) listener);
    } else {
      List<LocomotiveFunctionEventListener> sameFunctionEventListeners = new ArrayList<>();
      sameFunctionEventListeners.add((LocomotiveFunctionEventListener) listener);
      locomotiveFunctionEventListeners.put(locomotiveId, sameFunctionEventListeners);
    }
  }

  public void removeLocomotiveFunctionEventListener(Long locomotiveId, LocomotiveFunctionEventListener listener) {
    if (locomotiveFunctionEventListeners.containsKey(locomotiveId)) {
      List<LocomotiveFunctionEventListener> sameFunctionEventListeners = locomotiveFunctionEventListeners.get(locomotiveId);
      sameFunctionEventListeners.remove(listener);
    }
  }

  public void addLocomotiveDirectionEventListener(Long locomotiveId, LocomotiveDirectionEventListener listener) {
    if (locomotiveDirectionEventListeners.containsKey(locomotiveId)) {
      List<LocomotiveDirectionEventListener> sameDirectionEventListeners = locomotiveDirectionEventListeners.get(locomotiveId);
      sameDirectionEventListeners.add((LocomotiveDirectionEventListener) listener);
      Logger.trace("There are now " + sameDirectionEventListeners.size());
    } else {
      List<LocomotiveDirectionEventListener> sameDirectionEventListeners = new ArrayList<>();
      sameDirectionEventListeners.add((LocomotiveDirectionEventListener) listener);
      locomotiveDirectionEventListeners.put(locomotiveId, sameDirectionEventListeners);
      Logger.trace("There is now " + sameDirectionEventListeners.size());
    }
  }

  public void removeLocomotiveDirectionEventListener(Long locomotiveId, LocomotiveDirectionEventListener listener) {
    if (locomotiveDirectionEventListeners.containsKey(locomotiveId)) {
      List<LocomotiveDirectionEventListener> sameDirectionEventListeners = locomotiveDirectionEventListeners.get(locomotiveId);
      sameDirectionEventListeners.remove(listener);
      Logger.trace("remaining " + sameDirectionEventListeners.size());
    }
  }

  public void addLocomotiveSpeedEventListener(Long locomotiveId, LocomotiveSpeedEventListener listener) {
    if (locomotiveSpeedEventListeners.containsKey(locomotiveId)) {
      List<LocomotiveSpeedEventListener> sameSpeedEventListeners = locomotiveSpeedEventListeners.get(locomotiveId);
      sameSpeedEventListeners.add((LocomotiveSpeedEventListener) listener);
    } else {
      List<LocomotiveSpeedEventListener> sameSpeedEventListeners = new ArrayList<>();
      sameSpeedEventListeners.add((LocomotiveSpeedEventListener) listener);
      locomotiveSpeedEventListeners.put(locomotiveId, sameSpeedEventListeners);
    }
  }

  public void removeLocomotiveSpeedEventListener(Long locomotiveId, LocomotiveSpeedEventListener listener) {
    if (locomotiveSpeedEventListeners.containsKey(locomotiveId)) {
      List<LocomotiveSpeedEventListener> sameSpeedEventListeners = locomotiveSpeedEventListeners.get(locomotiveId);
      sameSpeedEventListeners.remove(listener);
    }
  }

  public void addConnectionEventListener(ConnectionEventListener listener) {
    this.connectionEventListeners.add(listener);
  }

  public void removeConnectionEventListener(ConnectionEventListener listener) {
    this.connectionEventListeners.remove(listener);
  }

  public void addPowerEventListener(PowerEventListener listener) {
    this.powerEventListeners.add(listener);
  }

  public void removePowerEventListener(PowerEventListener listener) {
    this.powerEventListeners.remove(listener);
  }

  public void addMeasurementEventListener(MeasurementEventListener listener) {
    this.measurementEventListeners.add(listener);
  }

  public void removeMeasurementListener(MeasurementEventListener listener) {
    this.measurementEventListeners.remove(listener);
  }

  public DecoderController getDecoderController() {
    return decoderController;
  }

  public void refreshLocomotives() {
    if (decoderController != null) {
      decoderController.refreshLocomotives();
    }
  }

  public List<AccessoryController> getAccessoryControllers() {
    return new ArrayList<>(accessoryControllers.values());
  }

  public List<FeedbackController> getFeedbackControllers() {
    return new ArrayList<>(feedbackControllers.values());
  }

  public SensorBean getSensorStatus(SensorBean sensorBean) {
    for (FeedbackController fbc : feedbackControllers.values()) {
      SensorBean sb = fbc.getSensorStatus(sensorBean);
      if (sb != null) {
        SensorEvent se = new SensorEvent(sb);
        sensorEventQueue.offer(se);
        sensorBean.setActive(sb.isActive());
      }
    }
    return sensorBean;
  }

  private class MeasurementEventHandler implements MeasurementEventListener {

    private final JCSCommandStation commandStation;

    MeasurementEventHandler(JCSCommandStation commandStation) {
      this.commandStation = commandStation;
    }

    @Override
    public void onMeasurement(final MeasurementEvent event) {
      for (MeasurementEventListener mel : commandStation.measurementEventListeners) {
        mel.onMeasurement(event);
      }
    }
  }

  private class AllSensorEventsHandler implements AllSensorEventsListener {

    private final JCSCommandStation commandStation;

    AllSensorEventsHandler(JCSCommandStation commandStation) {
      this.commandStation = commandStation;
    }

    @Override
    public void onSensorChange(SensorEvent sensorEvent) {
      Logger.trace("Enqueued SensorEvent ID: " + sensorEvent.getSensorId() + " Active: " + sensorEvent.isActive());
      commandStation.sensorEventQueue.offer(sensorEvent);
    }
  }

  private void handleSensorEvent_old(SensorEvent event) {
    SensorBean sb = event.getSensorBean();
    boolean newValue = event.isActive();
    SensorBean dbsb = null;

    //TODO: Add CommandStationID!
    if (dbsb == null && event.getSensorId() != null) {
      dbsb = PersistenceFactory.getService().getSensor(event.getSensorId());
    }

    if (sb.getDeviceId() != null && sb.getContactId() != null) {
      dbsb = PersistenceFactory.getService().getSensor(sb.getDeviceId(), sb.getContactId());
    }

    //SensorBean dbsb = PersistenceFactory.getService().getSensor(event.getSensorId());
    if (dbsb == null) {
      //Try using the deviceId and contactId and command station...
      dbsb = PersistenceFactory.getService().getSensor(sb.getDeviceId(), sb.getContactId());
    }

    if (dbsb != null) {
      //if (sb.getId() == null) {
      sb.setId(dbsb.getId());
      //}

      sb.setName(dbsb.getName());
      //make sure the previous vale is set by setting the current and the new one
      sb.setActive(dbsb.isActive());
      //Sensor bean will set the prev value to indicate a (real) sensor change
      sb.setActive(newValue);

      PersistenceFactory.getService().persist(sb);
    }

    if (sb.getId() != null && sensorListeners.containsKey(sb.getId())) {
      //Avoid concurrent modification exceptions
      List<SensorEventListener> snapshot = new ArrayList<>(sensorListeners.get(sb.getId()));

      for (SensorEventListener sl : snapshot) {
        if (sl != null) {
          sl.onSensorChange(event);
        }
      }
    }

    //For generic sensor listeners which are interested in every event...
    if (!allSensorEventsListeners.isEmpty()) {
      List<AllSensorEventsListener> snapshot = new ArrayList<>(allSensorEventsListeners);
      for (AllSensorEventsListener sl : snapshot) {
        if (sl != null) {
          sl.onSensorChange(event);
        }
      }
    }
  }

  private void handleSensorEvent(SensorEvent event) {
    SensorBean incoming = event.getSensorBean();
    boolean newValue = event.isActive();

    SensorBean sensor = null;

    if (incoming.getDeviceId() != null && incoming.getContactId() != null) {

      sensor = PersistenceFactory.getService().getSensor(incoming.getDeviceId(), incoming.getContactId());
    }

    if (sensor == null && incoming.getId() != null) {
      sensor = PersistenceFactory.getService().getSensor(incoming.getId());
    }

    if (sensor == null) {
      Logger.warn("Could not resolve sensor event: id={}, device={}, contact={}, commandStation={}",
              incoming.getId(),
              incoming.getDeviceId(),
              incoming.getContactId(),
              incoming.getCommandStationId());
      return;
    }

    // Preserve the previous DB state first.
    sensor.setActive(sensor.isActive());

    // Apply received state.
    sensor.setActive(newValue);

    sensor.setLastUpdatedMillis(System.currentTimeMillis());

    PersistenceFactory.getService().persist(sensor);

    Integer sensorId = sensor.getId();

    if (sensorListeners.containsKey(sensorId)) {
      SensorEvent resolvedEvent = new SensorEvent(sensor);

      List<SensorEventListener> snapshot = new ArrayList<>(sensorListeners.get(sensorId));

      for (SensorEventListener listener : snapshot) {
        if (listener != null) {
          listener.onSensorChange(resolvedEvent);
        }
      }
    }

    if (!allSensorEventsListeners.isEmpty()) {
      SensorEvent resolvedEvent = new SensorEvent(sensor);

      List<AllSensorEventsListener> snapshot = new ArrayList<>(allSensorEventsListeners);

      for (AllSensorEventsListener listener : snapshot) {
        if (listener != null) {
          listener.onSensorChange(resolvedEvent);
        }
      }
    }
  }

  private void handleAccessoryEvent(AccessoryEvent event) {
    AccessoryBean ab = event.getAccessoryBean();
    int address = ab.getAddress();
    String commandStationId = ab.getCommandStationId();
    AccessoryBean dbab = PersistenceFactory.getService().getAccessory(event.getId());

    if (dbab == null) {
      dbab = PersistenceFactory.getService().getAccessoryByAddressAndCommandStationId(address, commandStationId);
    }

    if (dbab == null) {
      dbab = PersistenceFactory.getService().getAccessoryByAddress2AndCommandStationId(address, commandStationId);
    }

    if (dbab != null) {
      dbab.setState(ab.getState());
      PersistenceFactory.getService().persist(dbab);

      if (accessoryEventListeners.containsKey(dbab.getId())) {
        List<AccessoryEventListener> snapshot = new ArrayList<>(accessoryEventListeners.get(dbab.getId()));
        //Logger.trace("Obtaining listener for accessory " + dbab.getId() + " which has " + snapshot.size() + " listeners to set to value " + event.getValue());

        for (AccessoryEventListener al : snapshot) {
          al.onAccessoryChange(new AccessoryEvent(dbab));
        }
      }
    }
  }

  private void handleLocomotiveEvent(LocomotiveEvent event) {
    LocomotiveBean lb = event.getLocomotiveBean();
    LocomotiveBean dblb = null;
    //Some command station use the ID and some the address

    if (lb.getCommandStationId() == null) {
      lb.setCommandStationBean(PersistenceFactory.getService().getDefaultCommandStation());
      Logger.trace("Setting default commandStation {} in Locomotive {}, {}", lb.getCommandStationId(), lb.getId(), lb.getName());
    }

    if (lb.getId() != null
            && ("marklin.cs".equals(lb.getCommandStationId())
            || "esu-ecos".equals(lb.getCommandStationId())
            || "intellibox2".equals(lb.getCommandStationId()))) {
      dblb = PersistenceFactory.getService().getLocomotiveById(lb.getId(), lb.getCommandStationId());
    }

    if (dblb == null) {
      Integer address = lb.getAddress();
      if (address == null) {
        if (lb.getId() != null) {
          address = lb.getId().intValue();
        } else if (lb.getUid() != null) {
          address = lb.getUid().intValue();
        }
      }
      if (address == null) {
        Logger.error("Can't search for a locomotive without address!");
        return;
      }
      LocomotiveBean.DecoderType decoder = lb.getDecoderType();

      if (decoder != null) {
        dblb = PersistenceFactory.getService().getLocomotive(address, decoder, lb.getCommandStationId());
        if (dblb != null) {
          Logger.trace("Found Locomotive id: {}, {}  via address {} and decoder {}", dblb.getId(), dblb.getName(), dblb.getAddress(), dblb.getDecoderType());
        }
      }

      if (dblb == null) {
        //Last attempt to look for a locomotive 
        Set<Protocol> protocols = PersistenceFactory.getService().getDefaultCommandStation().getSupportedProtocols();
        for (Protocol protocol : protocols) {
          decoder = DecoderType.get(protocol.getProtocol());
          dblb = PersistenceFactory.getService().getLocomotive(address, decoder, lb.getCommandStationId());
          if (dblb != null) {
            Logger.trace("Found Locomotive id: {}, {}  via address {} and decoder {} for Protocol {}", dblb.getId(), dblb.getName(), dblb.getAddress(), dblb.getDecoderType(), protocol);
            break;
          }
        }
      }
    }

    if (dblb == null) {
      Logger.error("Can't find Locomotive with Id: {} or Uid {} or Address {} and Decoder {} LocCS {} in Database.", lb.getId(), lb.getUid(), lb.getAddress(), lb.getDecoderType(), lb.getCommandStationId());
      return;
    }

    switch (event) {
      case LocomotiveSpeedEvent speedEvent -> {
        Integer newVelocity = speedEvent.getVelocity();
        dblb.setVelocity(newVelocity);
        PersistenceFactory.getService().persist(dblb);
        speedEvent.setLocomotiveBean(dblb);

        if (locomotiveSpeedEventListeners.containsKey(dblb.getId())) {
          List<LocomotiveSpeedEventListener> velocityListeners = new ArrayList<>(locomotiveSpeedEventListeners.get(dblb.getId()));
          Logger.trace("Firing " + velocityListeners.size() + " LocomotiveSpeedEventListener(s)");

          for (LocomotiveSpeedEventListener lvel : velocityListeners) {
            lvel.onSpeedChange(speedEvent);
          }
        }
      }
      case LocomotiveDirectionEvent directionEvent -> {
        Direction newDirection = directionEvent.getNewDirection();
        dblb.setDirection(newDirection);
        PersistenceFactory.getService().persist(dblb);
        directionEvent.setLocomotiveBean(dblb);

        if (locomotiveDirectionEventListeners.containsKey(dblb.getId())) {
          List<LocomotiveDirectionEventListener> directionListeners = new ArrayList<>(locomotiveDirectionEventListeners.get(dblb.getId()));
          Logger.trace("Firing " + directionListeners.size() + " LocomotiveDirectionEventListener(s)");

          for (LocomotiveDirectionEventListener ldel : directionListeners) {
            ldel.onDirectionChange(directionEvent);
          }
        }
      }
      case LocomotiveFunctionEvent functionEvent -> {
        Integer newValue = functionEvent.isOn() ? 1 : 0;

        FunctionBean dbfb = dblb.getFunctionBean(functionEvent.getNumber());
        dbfb.setValue(newValue);

        Logger.trace("Function " + dbfb.getNumber() + " value " + dbfb.getValue() + " -> " + (dbfb.isOn() ? "On" : "Off"));

        PersistenceFactory.getService().persist(dbfb);
        functionEvent.setLocomotiveBean(dblb);
        functionEvent.setFunctionBean(dbfb);

        if (locomotiveFunctionEventListeners.containsKey(dblb.getId())) {
          List<LocomotiveFunctionEventListener> functionListeners = new ArrayList<>(locomotiveFunctionEventListeners.get(dblb.getId()));
          for (LocomotiveFunctionEventListener fl : functionListeners) {
            fl.onFunctionChange(functionEvent);
          }
        }
      }
      default -> {
        Logger.warn("Unknown Event: " + event.getClass().getName());
      }
    }
  }

  private class AccessoryChangeEventListener implements AccessoryEventListener {

    private final JCSCommandStation jcsCommandStation;

    AccessoryChangeEventListener(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    @Override
    public void onAccessoryChange(AccessoryEvent event) {
      jcsCommandStation.accessoryEventQueue.offer(event);
    }
  }

  private class LocomotiveFunctionChangeEventListener implements LocomotiveFunctionEventListener {

    private final JCSCommandStation jcsCommandStation;

    LocomotiveFunctionChangeEventListener(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    @Override
    public void onFunctionChange(LocomotiveFunctionEvent functionEvent) {
      if (functionEvent != null) {
        jcsCommandStation.locomotiveEventQueue.offer(functionEvent);
      }
    }
  }

  private class LocomotiveDirectionChangeEventListener implements LocomotiveDirectionEventListener {

    private final JCSCommandStation jcsCommandStation;

    LocomotiveDirectionChangeEventListener(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    @Override
    public void onDirectionChange(LocomotiveDirectionEvent directionEvent) {
      Logger.trace(directionEvent.getId() + ": " + directionEvent.getNewDirection().getDirection());
      jcsCommandStation.locomotiveEventQueue.offer(directionEvent);
    }
  }

  private class LocomotiveSpeedChangeEventListener implements LocomotiveSpeedEventListener {

    private final JCSCommandStation jcsCommandStation;

    LocomotiveSpeedChangeEventListener(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    @Override
    public void onSpeedChange(LocomotiveSpeedEvent speedEvent) {
      jcsCommandStation.locomotiveEventQueue.offer(speedEvent);
    }
  }

  private class ControllerPowerListener implements PowerEventListener {

    private final JCSCommandStation jcsCommandStation;

    ControllerPowerListener(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    @Override
    public void onPowerChange(PowerEvent powerEvent) {
      if (this.jcsCommandStation.isConnected()
              && !this.jcsCommandStation.recoveryStopPending) {
        this.jcsCommandStation.lastKnownPowerOn = powerEvent.isPower();
      }
      this.jcsCommandStation.notifyPowerListeners(powerEvent);
    }
  }

  private class ControllerConnectionListener implements ConnectionEventListener {

    final JCSCommandStation jcsCommandStation;
    private final Object registeredController;

    ControllerConnectionListener(JCSCommandStation jcsCommandStation) {
      this(jcsCommandStation, null);
    }

    ControllerConnectionListener(JCSCommandStation jcsCommandStation, Object controller) {
      this.jcsCommandStation = jcsCommandStation;
      this.registeredController = controller;
    }

    @Override
    public void onConnectionChange(ConnectionEvent event) {
      // Ignore late events from a previous connection generation.
      if (registeredController != null && registeredController != decoderController) {
        return;
      }
      if (event.isConnected()) {
        Logger.trace(event.getSource() + " has re-connected!");
      } else {
        Logger.trace(event.getSource() + " is Disconnected!");
      }
      if (shuttingDown || cleaningUpControllers) {
        return;
      }
      ConnectionLifecycleManager manager = connectionLifecycleManager;
      if (manager != null && event.getSource() != null
              && (decoderController instanceof MarklinCentralStationImpl
              || decoderController instanceof EsuEcosCommandStationImpl)) {
        if (!event.isConnected()) {
          manager.connectionLost(null);
        }
        // The lifecycle manager is the sole source of UI connection events.
      } else {
        jcsCommandStation.notifyConnectionListeners(event);
      }
    }
  }

  /**
   * Universal handler Thread<br>
   * Handles events via a Queue of:<br>
   * - Sensors<br>
   * - Accessories<br>
   * - Locomotives<br>
   *
   * @param <T> the Class which contains the handler method
   */
  class EventHandlerThread<T> extends Thread {

    private final BlockingQueue<T> queue;
    private final Consumer<T> handler;
    private volatile boolean running = false;

    EventHandlerThread(ThreadGroup group, String name, BlockingQueue<T> queue, Consumer<T> handler) {
      super(group, name);
      this.queue = queue;
      this.handler = handler;
    }

    boolean isRunning() {
      return running;
    }

    @SuppressWarnings("unused")
    void quit() {
      running = false;
      interrupt();
    }

    @Override
    public void run() {
      running = true;
      Logger.trace(getName() + " Started...");

      while (isRunning()) {
        try {
          T event = queue.poll(100, TimeUnit.MILLISECONDS);
          if (event != null) {
            handler.accept(event);
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          // quit() interrupts poll() during normal shutdown.
          if (running) {
            Logger.warn(getName() + " interrupted unexpectedly");
          }
          break;
        } catch (Exception e) {
          Logger.error("Error in " + getName() + ". Cause: " + e.getMessage());
        }
      }

      Logger.trace(getName() + " stopped.");
    }
  }

}
