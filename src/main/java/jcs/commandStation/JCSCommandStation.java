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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
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

  private final Set<Protocol> supportedProtocols;
  private CommandStationBean commandStation;

  private final ExecutorService executor;

  private static final String AWT_THREAD = "AWT-EventQueue-0";

  private final TransferQueue<SensorEvent> sensorEventQueue;
  private final TransferQueue<AccessoryEvent> accessoryEventQueue;
  private final TransferQueue<LocomotiveEvent> locomotiveEventQueue;

  private SensorEventHandlerThread sensorEventHandlerThread;
  private AccessoryEventHandlerThread accessoryEventHandlerThread;
  private LocomotiveEventHandlerThread locomotiveEventHandlerThread;

  private boolean powerEventRunning;

  /**
   * Wrapper around the "real" CommandStation implementation.<br>
   * Operations to commandStations should not be performed in the EventDispatch thread.<br>
   * Operations to commandStations are performed in a worker thread to avoid blocking the EventDispatch thread.<br>
   */
  public JCSCommandStation() {
    this("true".equalsIgnoreCase(System.getProperty("skip.controller.autoconnect", "false")));
  }

  private JCSCommandStation(boolean autoConnectController) {
    executor = Executors.newCachedThreadPool();
    accessoryControllers = new HashMap<>();
    feedbackControllers = new HashMap<>();
    connectionEventListeners = new LinkedList<>();
    powerEventListeners = new LinkedList<>();

    allSensorEventsListeners = new LinkedList<>();
    sensorListeners = new HashMap<>();
    accessoryEventListeners = new HashMap<>();

    locomotiveFunctionEventListeners = new HashMap<>();
    locomotiveDirectionEventListeners = new HashMap<>();
    locomotiveSpeedEventListeners = new HashMap<>();
    supportedProtocols = new HashSet<>();
    locomotiveEventQueue = new LinkedTransferQueue<>();
    accessoryEventQueue = new LinkedTransferQueue<>();

    sensorEventQueue = new LinkedTransferQueue<>();
    sensorEventHandlerThread = new SensorEventHandlerThread(this);
    accessoryEventHandlerThread = new AccessoryEventHandlerThread(this);
    locomotiveEventHandlerThread = new LocomotiveEventHandlerThread(this);

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

  public final synchronized boolean connectInBackground() {
    long now = System.currentTimeMillis();
    long start = now;
    long timemax = now + 3000;

    executor.execute(() -> connect());

    boolean con = false;
    while (!con && now < timemax) {
      try {
        wait(500);
      } catch (InterruptedException ex) {
        Logger.trace(ex);
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
      this.decoderController.power(true);

    } else {
      Logger.trace("Timeout connecting...");

      if (!isVirtual()) {
        ConnectionEvent ce = new ConnectionEvent(commandStation.getDescription(), false);
        for (ConnectionEventListener cel : connectionEventListeners) {
          cel.onConnectionChange(ce);
        }
      }
    }

    return con;
  }

  public final boolean connect() {
    boolean decoderControllerConnected = false;
    boolean allreadyConnected = false;

    //Check if already connected to avoid duplication....
    if (commandStation != null && decoderController != null) {
      decoderControllerConnected = decoderController.isConnected();
      allreadyConnected = true;
      Logger.trace(decoderController.getClass().getName() + " allready connected...");
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
    if (!accessoryControllers.isEmpty() && !allreadyConnected) {
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
    if (!feedbackControllers.isEmpty() && !allreadyConnected) {
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

    if (decoderControllerConnected && !allreadyConnected && decoderController != null) {
      decoderController.addLocomotiveFunctionEventListener(new LocomotiveFunctionChangeEventListener(this));
      decoderController.addLocomotiveDirectionEventListener(new LocomotiveDirectionChangeEventListener(this));
      decoderController.addLocomotiveSpeedEventListener(new LocomotiveSpeedChangeEventListener(this));
    }

    if (accessoryCntrConnected > 0 && !allreadyConnected) {
      for (AccessoryController ac : accessoryControllers.values()) {
        if (ac.isConnected()) {
          ac.addAccessoryEventListener(new AccessoryChangeEventListener(this));

          if (ac.getConnectionEventListeners().isEmpty()) {
            ac.addConnectionEventListener(new ControllerConnectionListener(this));
          }
        }
      }
    }

    if (feedbackCntrConnected > 0 && !allreadyConnected) {
      for (FeedbackController fc : feedbackControllers.values()) {
        if (fc.isConnected()) {
          fc.addAllSensorEventsListener(new AllSensorEventsHandler(this));

          if (fc.getConnectionEventListeners().isEmpty()) {
            fc.addConnectionEventListener(new ControllerConnectionListener(this));
          }
        }
      }
    }

    //TODO implement get the day end i.e. the current state of all Objects on track
    return decoderControllerConnected;
  }

  public CommandStationBean getCommandStationBean() {
    if (decoderController != null) {
      return decoderController.getCommandStationBean();
    } else {
      Logger.trace("Using the default CommandStationBean...");
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
      this.executor.execute(() -> {
        for (ConnectionEventListener cel : connectionEventListeners) {
          cel.onConnectionChange(connectionEvent);
        }
      });
    }
  }

  private void notifyPowerListeners(final PowerEvent powerEvent) {
    if (!powerEventRunning) {
      try {
        powerEventRunning = true;
        this.executor.execute(() -> {
          Logger.trace("Signalling " + powerEventListeners.size() + " power " + (powerEvent.isPower() ? "On" : "Off"));
          for (PowerEventListener pel : powerEventListeners) {
            pel.onPowerChange(powerEvent);
          }
        });
      } finally {
        powerEventRunning = false;
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
    //Logger.trace("Switch Power " + (on ? "On" : "Off"));
    if (decoderController != null && !AWT_THREAD.equals(Thread.currentThread().getName())) {
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

  public void changeLocomotiveDirection(Direction newDirection, LocomotiveBean locomotive) {
    Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.getName() + " id: " + locomotive.getId() + " velocity: " + locomotive.getVelocity());

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

    if (decoderController != null && !AWT_THREAD.equals(Thread.currentThread().getName())) {
      decoderController.changeVelocity(address, 0, locomotive.getDirection());
      decoderController.changeDirection(address, newDirection);
    } else {
      executor.execute(() -> {
        decoderController.changeVelocity(address, 0, locomotive.getDirection());
        decoderController.changeDirection(address, newDirection);
      });
    }
  }

  public void changeLocomotiveSpeed(Integer newVelocity, LocomotiveBean locomotive) {
    Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());

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

    if (decoderController != null && !AWT_THREAD.equals(Thread.currentThread().getName())) {
      decoderController.changeVelocity(address, newVelocity, locomotive.getDirection());
    } else {
      executor.execute(() -> decoderController.changeVelocity(address, newVelocity, locomotive.getDirection()));
    }
  }

  public void changeLocomotiveFunction(Boolean newValue, Integer functionNumber, LocomotiveBean locomotive) {
    Logger.trace("Changing Function " + functionNumber + " to " + (newValue ? "on" : "off") + " on " + locomotive.getName());
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
    if (decoderController != null && !AWT_THREAD.equals(Thread.currentThread().getName())) {
      decoderController.changeFunctionValue(address, functionNumber, newValue);
    } else if (decoderController != null) {
      executor.execute(() -> decoderController.changeFunctionValue(address, functionNumber, newValue));
    } else {
      Logger.warn("Can't switch function decoderController is null!");
    }
  }

  public void switchAccessory(AccessoryBean accessory, AccessoryValue value) {
    String id = accessory.getId();
    Integer address = accessory.getAddress();
    Integer switchTime = accessory.getSwitchTime();
    AccessoryBean.Protocol protocol = accessory.getProtocol();
    if (protocol == null) {
      protocol = AccessoryBean.Protocol.DCC;
    }
    AccessoryValue val = value;
    Integer states = accessory.getStates();
    Integer state = accessory.getState();

    if (states == null) {
      states = 2;
    }
    if (state == null) {
      state = AccessoryValue.RED == val ? 0 : 1;
    }

    if (states > 2) {
      if (accessory.getState() > 1) {
        address = address + 1;
        val = AccessoryValue.get(state - 2);
      }
    }

    Logger.trace("Changing accessory with address: " + address + ", " + accessory.getName() + " to " + val.getValue());
    changeAccessory(address, protocol.getValue(), val, switchTime);
  }

  private void changeAccessory(final Integer address, final String protocol, final AccessoryValue value, final Integer switchTime) {
    if (!AWT_THREAD.equals(Thread.currentThread().getName())) {
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
      List<AccessoryEventListener> sameAccessoryListener = accessoryEventListeners.get(accessoryId);
      sameAccessoryListener.add(listener);
    } else {
      List<AccessoryEventListener> sameAccessoryListener = new ArrayList<>();
      sameAccessoryListener.add(listener);
      accessoryEventListeners.put(accessoryId, sameAccessoryListener);
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
    if (decoderController != null && decoderController.isSupportTrackMeasurements()) {
      decoderController.addMeasurementEventListener(listener);
    }
  }

  public void removeMeasurementListener(MeasurementEventListener listener) {
    if (decoderController != null && decoderController.isSupportTrackMeasurements()) {
      decoderController.removeMeasurementEventListener(listener);
    }
  }

  public DecoderController getDecoderController() {
    return decoderController;
  }

  public List<AccessoryController> getAccessoryControllers() {
    return accessoryControllers.values().stream().collect(Collectors.toList());
  }

  public List<FeedbackController> getFeedbackControllers() {
    return feedbackControllers.values().stream().collect(Collectors.toList());
  }

  public SensorBean getSensorStatus(SensorBean sensorBean) {
    for (FeedbackController fbc : feedbackControllers.values()) {
      SensorBean sb = fbc.getSensorStatus(sensorBean);
      SensorEvent se = new SensorEvent(sb);
      sensorEventQueue.offer(se);
      sensorBean.setActive(sb.isActive());
    }
    return sensorBean;
  }

  private class AllSensorEventsHandler implements AllSensorEventsListener {

    private final JCSCommandStation commandStation;

    AllSensorEventsHandler(JCSCommandStation commandStation) {
      this.commandStation = commandStation;
    }

    @Override
    public void onSensorChange(SensorEvent sensorEvent) {
      commandStation.sensorEventQueue.offer(sensorEvent);
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
      Logger.trace(directionEvent);
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
   * Handle Sensor Events, which are unsolicited messages from the CS.
   */
  private class SensorEventHandlerThread extends Thread {

    @SuppressWarnings("FieldMayBeFinal")
    private boolean stop = false;
    private boolean quit = true;

    private final JCSCommandStation jcsCommandStation;

    SensorEventHandlerThread(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    void quit() {
      this.quit = true;
    }

    boolean isRunning() {
      return !this.quit;
    }

    boolean isFinished() {
      return this.stop;
    }

    @Override
    public void run() {
      quit = false;
      Thread.currentThread().setName("SENSOR-EVENT-HANDLER");
      Logger.trace("Event Handler Started...");

      while (isRunning()) {
        try {
          try {
            SensorEvent event = jcsCommandStation.sensorEventQueue.take();
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
              sb.setActive(dbsb.isActive());
              sb.setActive(newValue);

              PersistenceFactory.getService().persist(sb);
            }

            if (this.jcsCommandStation.sensorListeners.containsKey(sb.getId())) {
              //Avoid concurrent modification exceptions
              List<SensorEventListener> snapshot = new ArrayList<>(this.jcsCommandStation.sensorListeners.get(sb.getId()));
              //Logger.trace("SensorEvent from Sensor " + event.getSensorId() + ": " + event.isActive() + " Firing " + snapshot.size() + " SensorListeners...");

              for (SensorEventListener sl : snapshot) {
                if (sl != null) {
                  sl.onSensorChange(event);
                }
              }
            }

            //For generic sensor listeners which are interested in every event...
            if (!jcsCommandStation.allSensorEventsListeners.isEmpty()) {
              List<AllSensorEventsListener> snapshot = new ArrayList<>(jcsCommandStation.allSensorEventsListeners);
              for (AllSensorEventsListener sl : snapshot) {
                if (sl != null) {
                  sl.onSensorChange(event);
                }
              }
            }

          } catch (InterruptedException ex) {
            Logger.error(ex);
          }
        } catch (Exception e) {
          Logger.error("Error in Handling Thread. Cause: " + e.getMessage());
        }
      }

      Logger.debug("Stop Event handling");
    }
  }

  /**
   * Handle Accessory Events, which could be unsolicited,<br>
   * but mostly the event will come after an accessory command.<br>
   * Handle in separate thread as most listeners has to do with the UI.
   */
  private class AccessoryEventHandlerThread extends Thread {

    @SuppressWarnings("FieldMayBeFinal")
    private boolean stop = false;
    private boolean quit = true;

    private final JCSCommandStation jcsCommandStation;

    AccessoryEventHandlerThread(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    void quit() {
      this.quit = true;
    }

    boolean isRunning() {
      return !this.quit;
    }

    boolean isFinished() {
      return this.stop;
    }

    @Override
    public void run() {
      quit = false;
      Thread.currentThread().setName("ACCESSORY-EVENT-HANDLER");
      Logger.trace("Event Handler Started...");

      while (isRunning()) {
        try {
          try {
            AccessoryEvent event = jcsCommandStation.accessoryEventQueue.take();
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
              //set all current properties
              ab.copyInto(dbab);
              //update the value
              ab.setAccessoryValue(newValue);
              PersistenceFactory.getService().persist(ab);
            }

            if (jcsCommandStation.accessoryEventListeners.containsKey(ab.getId())) {
              List<AccessoryEventListener> snapshot = new ArrayList<>(this.jcsCommandStation.accessoryEventListeners.get(ab.getId()));

              for (AccessoryEventListener al : snapshot) {
                al.onAccessoryChange(event);
              }
            }

          } catch (InterruptedException ex) {
            Logger.error(ex);
          }
        } catch (Exception e) {
          Logger.error("Error in Handling Thread. Cause: " + e.getMessage());
        }
      }

      Logger.debug("Stop Event handling");
    }
  }

  /**
   * Handle Locomotive Events, which could be unsolicited,<br>
   * but mostly the event will come after a used change something on the locomotive Cap or the auatoPilot.<br>
   * Handle in separate thread as most listeners has to do something with the UI.
   */
  private class LocomotiveEventHandlerThread extends Thread {

    @SuppressWarnings("FieldMayBeFinal")
    private boolean stop = false;
    private boolean quit = true;

    private final JCSCommandStation jcsCommandStation;

    LocomotiveEventHandlerThread(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    void quit() {
      this.quit = true;
    }

    boolean isRunning() {
      return !this.quit;
    }

    boolean isFinished() {
      return this.stop;
    }

    @Override
    public void run() {
      quit = false;
      Thread.currentThread().setName("LOCOMOTIVE-EVENT-HANDLER");
      Logger.trace("Event Handler Started...");

      while (isRunning()) {
        try {
          try {
            LocomotiveEvent event = jcsCommandStation.locomotiveEventQueue.take();
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

                if (jcsCommandStation.locomotiveSpeedEventListeners.containsKey(dblb.getId())) {
                  List<LocomotiveSpeedEventListener> velocityListeners = new ArrayList<>(jcsCommandStation.locomotiveSpeedEventListeners.get(dblb.getId()));
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

                if (jcsCommandStation.locomotiveDirectionEventListeners.containsKey(dblb.getId())) {
                  List<LocomotiveDirectionEventListener> directionListeners = new ArrayList<>(jcsCommandStation.locomotiveDirectionEventListeners.get(dblb.getId()));
                  Logger.trace("Firing " + directionListeners.size() + " LocomotiveDirectionEventListener(s)");

                  for (LocomotiveDirectionEventListener ldel : directionListeners) {
                    ldel.onDirectionChange(directionEvent);
                  }
                }
              }
              case LocomotiveFunctionEvent functionEvent -> {
                Integer newValue = functionEvent.isOn() ? 1 : 0;
                //FunctionBean fb = functionEvent.getFunctionBean();

                FunctionBean dbfb = dblb.getFunctionBean(functionEvent.getNumber());
                dbfb.setValue(newValue);

                Logger.trace("Function " + dbfb.getNumber() + " value " + dbfb.getValue() + " -> " + (dbfb.isOn() ? "On" : "Off"));

                //FunctionBean dbfb = PersistenceFactory.getService().getLocomotiveFunction(dblb, fb.getNumber());
                PersistenceFactory.getService().persist(dbfb);
                functionEvent.setLocomotiveBean(dblb);
                functionEvent.setFunctionBean(dbfb);

                if (jcsCommandStation.locomotiveFunctionEventListeners.containsKey(dblb.getId())) {
                  List<LocomotiveFunctionEventListener> functionListeners = new ArrayList<>(jcsCommandStation.locomotiveFunctionEventListeners.get(dblb.getId()));
                  for (LocomotiveFunctionEventListener fl : functionListeners) {
                    fl.onFunctionChange(functionEvent);
                  }
                }
              }
              default -> {
                Logger.warn("Unkown Event: " + event.getClass().getName());
              }
            }
          } catch (InterruptedException ex) {
            Logger.error(ex);
          }
        } catch (Exception e) {
          Logger.error("Error in Handling Thread. Cause: " + e.getMessage());
        }
      }

      Logger.debug("Stop Event handling");
    }
  }

}
