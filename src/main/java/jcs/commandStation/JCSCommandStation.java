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
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.commandStation.events.LocomotiveEvent;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.PowerEvent;

/**
 * The JCSCommandStation is the layer between the UI, engines and Command stations
 */
public class JCSCommandStation {

  private DecoderController decoderController;
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

  private final ExecutorService executor;

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

  /**
   * Wrapper around the "real" CommandStation implementation.<br>
   * Operations to commandStations should not be performed in the EventDispatch thread.<br>
   * Operations to commandStations are performed in a worker thread to avoid blocking the EventDispatch thread.<br>
   */
  public JCSCommandStation() {
    this("true".equalsIgnoreCase(System.getProperty("skip.controller.autoconnect", "false")));
  }

  private JCSCommandStation(boolean autoConnectController) {
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

    sensorEventHandlerThread = new EventHandlerThread<>(threadGroup, "SENSOR-EVENT-HANDLER", sensorEventQueue, this::handleSensorEvent);
    accessoryEventHandlerThread = new EventHandlerThread<>(threadGroup, "ACCESSORY-EVENT-HANDLER", accessoryEventQueue, this::handleAccessoryEvent);
    locomotiveEventHandlerThread = new EventHandlerThread<>(threadGroup, "LOCOMOTIVE-EVENT-HANDLER", locomotiveEventQueue, this::handleLocomotiveEvent);

    try {
      if (decoderController != null && (decoderController.getCommandStationBean() != null || !accessoryControllers.isEmpty() || !feedbackControllers.isEmpty()) && autoConnectController) {
        connect();
        Logger.trace(decoderController != null ? "Aquired " + decoderController.getClass().getSimpleName() : "Could not aquire a Command Station! " + (decoderController.isConnected() ? "Connected" : "NOT Connected"));
      } else {
        Logger.trace("Auto Connect disabled");
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

  public final boolean connectInBackground() {
    long now = System.currentTimeMillis();
    long start = now;
    long timemax = now + 3000;

    executor.execute(() -> {
      connect();
      wakeUp();
    });

    boolean con = false;

    while (!con && now < timemax) {
      try {
        synchronized (lock) {
          lock.wait(500);
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        Logger.trace(ex);
        break;
      }

      now = System.currentTimeMillis();
      if (decoderController != null) {
        con = decoderController.isConnected();
      } else {
        Logger.trace("Can't connect as there is no DecoderController configured !");
      }
    }

    if (con) {
      Logger.trace("Connected to " + decoderController.getCommandStationBean().getDescription() + " in " + (now - start) + " ms");
      //Switch the track power on
      //TODO: make this configurable via property
      boolean power = decoderController.power(true);
      Logger.trace("Power is " + (power ? "On" : "Off"));

      if (!isVirtual()) {
        ConnectionEvent ce = new ConnectionEvent(commandStation.getDescription(), true, isVirtual());
        for (ConnectionEventListener cel : connectionEventListeners) {
          cel.onConnectionChange(ce);
        }
      }

    } else {
      Logger.trace("Timeout connecting...");

      if (!isVirtual()) {
        ConnectionEvent ce = new ConnectionEvent(commandStation.getDescription(), false, isVirtual());
        for (ConnectionEventListener cel : connectionEventListeners) {
          cel.onConnectionChange(ce);
        }
      }
    }

    return con;
  }

  public final boolean connect() {
    boolean decoderControllerConnected = false;
    boolean alreadyConnected = false;

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

    if (decoderController != null && !decoderControllerConnected) {
      decoderControllerConnected = decoderController.isConnected();
      if (!decoderControllerConnected) {
        decoderControllerConnected = decoderController.connect();
      }
    }

    //Connect the Accessories controllers if needed
    if (!accessoryControllers.isEmpty() && !alreadyConnected) {
      for (AccessoryController ac : accessoryControllers.values()) {
        if (ac.isConnected()) {
          accessoryCntrConnected++;
        } else {
          try {
            if (ac.connect()) {
              accessoryCntrConnected++;
            }
          } catch (Exception e) {
            Logger.warn(" Can't connected to " + ac.getCommandStationBean().getDescription());
          }
        }
      }
    }

    //Connect the Feedback Controllers controllers if needed
    if (!feedbackControllers.isEmpty() && !alreadyConnected) {
      for (FeedbackController fc : feedbackControllers.values()) {
        if (fc.isConnected()) {
          feedbackCntrConnected++;
        } else {
          try {
            if (fc.connect()) {
              feedbackCntrConnected++;
            }
          } catch (Exception e) {
            Logger.warn(" Can't connected to " + fc.getCommandStationBean().getDescription());
          }
        }
      }
    }

    if (decoderController != null && decoderController.isConnected()) {
      decoderController.addConnectionEventListener(new ControllerConnectionListener(this));

      decoderController.addPowerEventListener(new ControllerPowerListener(this));

      if (!locomotiveEventHandlerThread.isRunning()) {
        locomotiveEventHandlerThread.start();
      }

      measurementEventHandler = new MeasurementEventHandler(this);
      decoderController.addMeasurementEventListener(measurementEventHandler);
    }

    if (accessoryCntrConnected > 0) {
      if (!accessoryEventHandlerThread.isRunning()) {
        accessoryEventHandlerThread.start();
      }
    }

    if (feedbackCntrConnected > 0) {
      if (!sensorEventHandlerThread.isRunning()) {
        sensorEventHandlerThread.start();
      }
    }

    Logger.debug("Connected Controllers:  Decoder: " + (decoderControllerConnected ? "Yes" : "No") + " Accessory: " + accessoryCntrConnected + " Feedback: " + feedbackCntrConnected);

    if (decoderControllerConnected && !alreadyConnected && decoderController != null) {
      decoderController.addLocomotiveFunctionEventListener(new LocomotiveFunctionChangeEventListener(this));
      decoderController.addLocomotiveDirectionEventListener(new LocomotiveDirectionChangeEventListener(this));
      decoderController.addLocomotiveSpeedEventListener(new LocomotiveSpeedChangeEventListener(this));
    }

    if (accessoryCntrConnected > 0 && !alreadyConnected) {
      for (AccessoryController ac : accessoryControllers.values()) {
        if (ac.isConnected()) {
          ac.addAccessoryEventListener(new AccessoryChangeEventListener(this));

          if (ac.getConnectionEventListeners().isEmpty()) {
            ac.addConnectionEventListener(new ControllerConnectionListener(this));
          }
        }
      }
    }

    if (feedbackCntrConnected > 0 && !alreadyConnected) {
      for (FeedbackController fc : feedbackControllers.values()) {
        if (fc.isConnected()) {
          fc.addAllSensorEventsListener(new AllSensorEventsHandler(this));

          if (fc.getConnectionEventListeners().isEmpty()) {
            fc.addConnectionEventListener(new ControllerConnectionListener(this));
          }
        }
      }
    }

    boolean power = this.isPowerOn();
    PowerEvent pe = new PowerEvent(power);
    for (PowerEventListener pl : powerEventListeners) {
      pl.onPowerChange(pe);
    }

    return decoderControllerConnected;
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

  public void disconnect() {
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

    if (decoderController != null) {
      if (measurementEventHandler != null) {
        decoderController.removeMeasurementEventListener(measurementEventHandler);
      }
      measurementEventHandler = null;

      decoderController.disconnect();
    }

    //Enable command station switching so
    decoderController = null;
    accessoryControllers.clear();
    feedbackControllers.clear();
    commandStation = null;
    ControllerFactory.reset();
  }

  private void notifyConnectionListeners(final ConnectionEvent connectionEvent) {
    if (!isVirtual()) {
      if (!connectionEventListeners.isEmpty()) {
        executor.execute(() -> {
          for (ConnectionEventListener cel : connectionEventListeners) {
            cel.onConnectionChange(connectionEvent);
          }
        });
      }
    }
  }

  private void notifyPowerListeners(final PowerEvent powerEvent) {
    if (powerEventRunning.compareAndSet(false, true)) {
      try {
        if (!powerEventListeners.isEmpty()) {
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
  }

  public void setVirtual(boolean flag) {
    Logger.info("Switch Virtual Mode " + (flag ? "On" : "Off"));
    commandStation.setVirtual(flag);
    PersistenceFactory.getService().persist(commandStation);

    decoderController.setVirtual(flag);
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
    if (decoderController != null && !SwingUtilities.isEventDispatchThread()) {
      decoderController.power(on);
    } else {
      executor.execute(() -> {
        if (decoderController != null) {
          decoderController.power(on);
        }
      });
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
    Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.getName() + " id: " + locomotive.getId() + " velocity: " + locomotive.getVelocity());
    int address = resolveAddress(locomotive);

    if (decoderController != null && !SwingUtilities.isEventDispatchThread()) {
      //Marklin CS does not need a zero velocity. ths is handle by the CS
      if (!"marklin.cs".equals(this.commandStation.getId())) {
        decoderController.changeVelocity(address, 0, locomotive.getDirection());
      }
      decoderController.changeDirection(address, newDirection);
    } else {
      executor.execute(() -> {
        if (!"marklin.cs".equals(this.commandStation.getId())) {
          decoderController.changeVelocity(address, 0, locomotive.getDirection());
        }
        decoderController.changeDirection(address, newDirection);
      });
    }
  }

  public void changeLocomotiveSpeed(Integer newVelocity, LocomotiveBean locomotive) {
    Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());
    int address = resolveAddress(locomotive);

    if (decoderController != null && !SwingUtilities.isEventDispatchThread()) {
      decoderController.changeVelocity(address, newVelocity, locomotive.getDirection());
    } else {
      executor.execute(() -> decoderController.changeVelocity(address, newVelocity, locomotive.getDirection()));
    }
  }

  public void changeLocomotiveFunction(Boolean newValue, Integer functionNumber, LocomotiveBean locomotive) {
    Logger.trace("Changing Function " + functionNumber + " to " + (newValue ? "on" : "off") + " on " + locomotive.getName());
    int address = resolveAddress(locomotive);

    if (decoderController != null && !SwingUtilities.isEventDispatchThread()) {
      decoderController.changeFunctionValue(address, functionNumber, newValue);
    } else if (decoderController != null) {
      executor.execute(() -> decoderController.changeFunctionValue(address, functionNumber, newValue));
    } else {
      Logger.warn("Can't switch function decoderController is null!");
    }
  }

  public void switchAccessory(AccessoryBean accessory, AccessoryValue value) {
    Integer address = accessory.getAddress();
    Integer switchTime = accessory.getSwitchTime();
    AccessoryBean.Protocol protocol = accessory.getProtocol();
    if (protocol == null) {
      protocol = AccessoryBean.Protocol.DCC;
    }

    Logger.trace("Changing accessory with address: " + address + ", " + accessory.getName() + " to " + value.getValue());
    changeAccessory(address, protocol.getValue(), value, switchTime);
  }

  private void changeAccessory(final Integer address, final String protocol, final AccessoryValue value, final Integer switchTime) {
    if (!SwingUtilities.isEventDispatchThread()) {
      for (AccessoryController ac : accessoryControllers.values()) {
        ac.switchAccessory(address, protocol, value, switchTime);
      }
    } else {
      executor.execute(() -> {
        for (AccessoryController ac : accessoryControllers.values()) {
          ac.switchAccessory(address, protocol, value, switchTime);
        }
      });
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

  public List<AccessoryController> getAccessoryControllers() {
    return new ArrayList<>(accessoryControllers.values());
  }

  public List<FeedbackController> getFeedbackControllers() {
    return new ArrayList<>(feedbackControllers.values());
  }

  public SensorBean getSensorStatus(SensorBean sensorBean) {
    for (FeedbackController fbc : feedbackControllers.values()) {
      SensorBean sb = fbc.getSensorStatus(sensorBean);
      SensorEvent se = new SensorEvent(sb);
      if (sb != null) {
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
      //if ("true".equals(System.getProperty("state.machine.stepTest", "false"))) {
      //  Logger.warn("Handle sensorevent inline...");
      //  handleSensorEvent(sensorEvent);
      //} else {
      Logger.trace("Enqueued SensorEvent ID: " + sensorEvent.getSensorId() + " Active: " + sensorEvent.isActive());
      commandStation.sensorEventQueue.offer(sensorEvent);
      //}
    }
  }

  private void handleSensorEvent(SensorEvent event) {
    SensorBean sb = event.getSensorBean();
    boolean newValue = event.isActive();
    SensorBean dbsb = PersistenceFactory.getService().getSensor(event.getSensorId());

    if (dbsb == null) {
      //Try using the deviceId and contactId and command station...
      dbsb = PersistenceFactory.getService().getSensor(sb.getDeviceId(), sb.getContactId());
    }

    if (dbsb != null) {
      if (sb.getId() == null) {
        sb.setId(dbsb.getId());
      }

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
//    else {
//      Logger.trace("There is Not an AllSensorsListener registered!");
//    }
  }

  private void handleAccessoryEvent(AccessoryEvent event) {
    AccessoryBean ab = event.getAccessoryBean();
    int address = ab.getAddress();
    String commandStationId = ab.getCommandStationId();

    AccessoryValue newValue = event.getValue();
    AccessoryBean dbab = PersistenceFactory.getService().getAccessory(event.getId());

    if (dbab == null) {
      //Try using the deviceId and contactId and command station...
      dbab = PersistenceFactory.getService().getAccessoryByAddressAndCommandStationId(address, commandStationId);
    }

    if (dbab == null) {
      //check if address is even, might be the second address of a signal
      if (address % 2 == 0) {
        address = address - 1;
        dbab = PersistenceFactory.getService().getAccessoryByAddressAndCommandStationId(address, commandStationId);
        if (dbab != null && dbab.isSignal() && dbab.getStates() > 2) {
          ab.setAddress(address);
          int p = ab.getState() + 2;
          ab.setState(p);
        } else {
          dbab = null;
        }
      }
    }

    if (dbab != null) {
      AccessoryValue previous = dbab.getAccessoryValue();
      boolean changed = newValue != previous;
      if (changed) {
        //set all current properties
        ab.copyInto(dbab);
        //update the value
        ab.setAccessoryValue(newValue);
        PersistenceFactory.getService().persist(ab);
      } else {
        Logger.trace("Value " + newValue + " for accessory " + dbab.getId() + " has NOT changed...");
      }
    }

    if (accessoryEventListeners.containsKey(ab.getId())) {
      List<AccessoryEventListener> snapshot = new ArrayList<>(accessoryEventListeners.get(ab.getId()));
      Logger.trace("Obtaining listener for accessory " + ab.getId() + " which has " + snapshot.size() + " listeners to set to value " + event.getValue());

      for (AccessoryEventListener al : snapshot) {
        Logger.trace("Listener source " + al.getClass().getName());
        al.onAccessoryChange(event);
      }
    }
  }

  private void handleLocomotiveEvent(LocomotiveEvent event) {
    LocomotiveBean lb = event.getLocomotiveBean();
    LocomotiveBean dblb = null;
    if ("marklin.cs".equals(lb.getCommandStationId()) || "esu-ecos".equals(lb.getCommandStationId())) {
      dblb = PersistenceFactory.getService().getLocomotiveById(lb.getId(), lb.getCommandStationId());
    } else {
      Integer address;
      if (lb.getAddress() != null) {
        address = lb.getAddress();
      } else {
        address = lb.getId().intValue();
      }
      if (lb.getDecoderType() != null) {
        dblb = PersistenceFactory.getService().getLocomotive(address, lb.getDecoderType(), lb.getCommandStationId());
      } else {
        //Try to match one...
        Set<Protocol> protocols = PersistenceFactory.getService().getDefaultCommandStation().getSupportedProtocols();
        for (Protocol protocol : protocols) {
          DecoderType decoder = DecoderType.get(protocol.getProtocol());
          dblb = PersistenceFactory.getService().getLocomotive(address, decoder, lb.getCommandStationId());
          if (dblb != null) {
            break;
          }
        }
      }
    }

    if (dblb == null) {
      if ("marklin.cs".equals(lb.getCommandStationId()) || "esu-ecos".equals(lb.getCommandStationId())) {
        Logger.error("No loc with id " + lb.getId() + ", " + lb.getCommandStationId() + " found in Database");
      } else {
        Logger.error("No loc found for " + lb.getId() + " / " + lb.getCommandStationId() + " found in Database");
      }
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
      jcsCommandStation.locomotiveEventQueue.offer(functionEvent);
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
      this.jcsCommandStation.notifyPowerListeners(powerEvent);
    }
  }

  private class ControllerConnectionListener implements ConnectionEventListener {

    final JCSCommandStation jcsCommandStation;

    ControllerConnectionListener(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    @Override
    public void onConnectionChange(ConnectionEvent event) {
      if (event.isConnected()) {
        Logger.trace(event.getSource() + " has re-connected!");
      } else {
        Logger.trace(event.getSource() + " is Disconnected!");
      }
      jcsCommandStation.notifyConnectionListeners(event);
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
          Logger.error(ex);
          break;
        } catch (Exception e) {
          Logger.error("Error in " + getName() + ". Cause: " + e.getMessage());
        }
      }

      Logger.trace(getName() + " stopped.");
    }
  }

}
