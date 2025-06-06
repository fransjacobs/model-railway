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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

/**
 * The JCSCommandStation is the layer between the UI, engines and Command stations
 */
public class JCSCommandStation {

  private DecoderController decoderController;
  private Map<String, AccessoryController> accessoryControllers;
  private Map<String, FeedbackController> feedbackControllers;

  private final List<SensorEventListener> sensorListeners;

  private final List<AccessoryEventListener> accessoryEventListeners;
  private final List<LocomotiveFunctionEventListener> locomotiveFunctionEventListeners;

  private final List<LocomotiveDirectionEventListener> locomotiveDirectionEventListeners;
  private final List<LocomotiveSpeedEventListener> locomotiveSpeedEventListeners;

  private final Set<Protocol> supportedProtocols;
  private CommandStationBean commandStation;

  private final ExecutorService executor;

  private static final String AWT_THREAD = "AWT-EventQueue-0";

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

    sensorListeners = new LinkedList<>();
    accessoryEventListeners = new LinkedList<>();
    locomotiveFunctionEventListeners = new LinkedList<>();
    locomotiveDirectionEventListeners = new LinkedList<>();
    locomotiveSpeedEventListeners = new LinkedList<>();
    supportedProtocols = new HashSet<>();

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
    if (decoderController != null) {
      con = decoderController.isConnected();
    } else {
      Logger.trace("Can't connect as there is no DecoderController configured !");
    }

    while (!con && now < timemax) {
      try {
        wait(500);
      } catch (InterruptedException ex) {
        Logger.trace(ex);
      }
      now = System.currentTimeMillis();
      con = decoderController.isConnected();
    }

    if (con) {
      Logger.trace("Connected to " + decoderController.getCommandStationBean().getDescription() + " in " + (now - start) + " ms");
    } else {
      Logger.trace("Timeout connecting...");
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

    Logger.trace("Connected Controllers:  Decoder: " + (decoderControllerConnected ? "Yes" : "No") + " Accessory: " + accessoryCntrConnected + " Feedback: " + feedbackCntrConnected);

    if (decoderControllerConnected && !allreadyConnected && decoderController != null) {
      decoderController.addConnectionEventListener(new ConnectionListener(this));

      decoderController.addLocomotiveFunctionEventListener(new LocomotiveFunctionChangeEventListener(this));
      decoderController.addLocomotiveDirectionEventListener(new LocomotiveDirectionChangeEventListener(this));
      decoderController.addLocomotiveSpeedEventListener(new LocomotiveSpeedChangeEventListener(this));

      supportedProtocols.addAll(decoderController.getCommandStationBean().getSupportedProtocols());
    }

    if (accessoryCntrConnected > 0 && !allreadyConnected) {
      for (AccessoryController ac : accessoryControllers.values()) {
        if (ac.isConnected()) {
          ac.addAccessoryEventListener(new AccessoryChangeEventListener(this));
          ac.addConnectionEventListener(new ConnectionListener(this));
        }
      }
    }

    if (feedbackCntrConnected > 0 && !allreadyConnected) {
      for (FeedbackController fc : feedbackControllers.values()) {
        if (fc.isConnected()) {
          fc.addSensorEventListener(new SensorChangeEventListener(this));
          fc.addConnectionEventListener(new ConnectionListener(this));
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
    Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.getName() + " id: " + locomotive.getId() + " velocity: "+ locomotive.getVelocity());

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
    } else {
      executor.execute(() -> decoderController.changeFunctionValue(address, functionNumber, newValue));
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

  public void addSensorEventListener(SensorEventListener listener) {
    sensorListeners.add(listener);
  }

  public void removeSensorEventListener(SensorEventListener listener) {
    sensorListeners.remove(listener);
  }

  public void addAccessoryEventListener(AccessoryEventListener listener) {
    accessoryEventListeners.add(listener);
  }

  public void removeAccessoryEventListener(AccessoryEventListener listener) {
    accessoryEventListeners.remove(listener);
  }

  public void addLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    locomotiveFunctionEventListeners.add(listener);
  }

  public void removeLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    locomotiveFunctionEventListeners.remove(listener);
  }

  public void addLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    locomotiveDirectionEventListeners.add(listener);
  }

  public void removeLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    this.locomotiveDirectionEventListeners.remove(listener);
  }

  public void addLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    locomotiveSpeedEventListeners.add(listener);
  }

  public void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    locomotiveSpeedEventListeners.remove(listener);
  }

  public void addDisconnectionEventListener(ConnectionEventListener listener) {
    if (decoderController != null) {
      decoderController.addConnectionEventListener(listener);
    }
    for (AccessoryController ac : accessoryControllers.values()) {
      if (ac != decoderController) {
        ac.addConnectionEventListener(listener);
      }
    }

    for (FeedbackController fc : feedbackControllers.values()) {
      if (fc != decoderController) {
        fc.addConnectionEventListener(listener);
      }
    }
  }

  public void addPowerEventListener(PowerEventListener listener) {
    if (decoderController != null) {
      decoderController.addPowerEventListener(listener);
    }
  }

  public void removePowerEventListener(PowerEventListener listener) {
    if (decoderController != null) {
      decoderController.removePowerEventListener(listener);
    }
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

  private class SensorChangeEventListener implements SensorEventListener {

    private final JCSCommandStation commandStation;

    SensorChangeEventListener(JCSCommandStation commandStation) {
      this.commandStation = commandStation;
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      SensorBean sb = event.getSensorBean();
      boolean newValue = event.isActive();
      //SensorBean dbsb = PersistenceFactory.getService().getSensor(sb.getDeviceId(), sb.getContactId());
      SensorBean dbsb = PersistenceFactory.getService().getSensor(event.getSensorId());

      if (dbsb == null) {
        //Try using the deviceId and contactId
        dbsb = PersistenceFactory.getService().getSensor(sb.getDeviceId(), sb.getContactId());
      }

      if (dbsb != null) {
        if (sb.getId() == null) {
          sb.setId(dbsb.getId());
        }
        sb.setName(dbsb.getName());
        sb.setActive(newValue);
        PersistenceFactory.getService().persist(sb);
      }

      //Avoid concurrent modification exceptions
      List<SensorEventListener> snapshot = new ArrayList<>(commandStation.sensorListeners);

      for (SensorEventListener sl : snapshot) {
        if (sl != null) {
          sl.onSensorChange(event);
        }
      }
    }
  }

  private class AccessoryChangeEventListener implements AccessoryEventListener {

    private final JCSCommandStation trackService;

    AccessoryChangeEventListener(JCSCommandStation trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onAccessoryChange(AccessoryEvent event) {
      AccessoryBean ab = event.getAccessoryBean();

      int address = ab.getAddress();
      String commandStationId = ab.getCommandStationId();

      AccessoryBean dbab = PersistenceFactory.getService().getAccessoryByAddressAndCommandStationId(address, commandStationId);
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
        //set all properties
        ab.setId(dbab.getId());
        ab.setDecoder(dbab.getDecoder());
        ab.setDecType(dbab.getDecType());
        ab.setName(dbab.getName());
        ab.setType(dbab.getType());
        ab.setGroup(dbab.getGroup());
        ab.setIcon(dbab.getIcon());
        ab.setIconFile(dbab.getIconFile());
        ab.setStates(dbab.getStates());
        ab.setCommandStationId(dbab.getCommandStationId());
        //might be set by the event
        if (ab.getSwitchTime() == null) {
          ab.setSwitchTime(dbab.getSwitchTime());
        }

        PersistenceFactory.getService().persist(ab);

        for (AccessoryEventListener al : this.trackService.accessoryEventListeners) {
          al.onAccessoryChange(event);
        }
      }
    }
  }

  private class LocomotiveFunctionChangeEventListener implements LocomotiveFunctionEventListener {

    private final JCSCommandStation trackService;

    LocomotiveFunctionChangeEventListener(JCSCommandStation trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onFunctionChange(LocomotiveFunctionEvent functionEvent) {
      FunctionBean fb = functionEvent.getFunctionBean();

      FunctionBean dbfb = null;
      String commandStationId = trackService.getDecoderController().getCommandStationBean().getId();

      if ("marklin.cs".equals(commandStationId) || "esu-ecos".equals(commandStationId)) {
        dbfb = PersistenceFactory.getService().getLocomotiveFunction(fb.getLocomotiveId(), fb.getNumber());
      } else {
        Integer address = fb.getLocomotiveId().intValue();

        LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(address, DecoderType.get(fb.getDecoderTypeString()), fb.getCommandStationId());
        if (dblb != null) {
          dbfb = PersistenceFactory.getService().getLocomotiveFunction(dblb.getId(), fb.getNumber());
        }
      }

      if (dbfb != null) {
        if (!Objects.equals(dbfb.getValue(), fb.getValue())) {
          dbfb.setValue(fb.getValue());
          if (!dbfb.isMomentary()) {
            PersistenceFactory.getService().persist(dbfb);
            functionEvent.setFunctionBean(dbfb);
          }
          for (LocomotiveFunctionEventListener fl : trackService.locomotiveFunctionEventListeners) {
            fl.onFunctionChange(functionEvent);
          }
        }
      }
    }
  }

  private class LocomotiveDirectionChangeEventListener implements LocomotiveDirectionEventListener {

    private final JCSCommandStation trackService;

    LocomotiveDirectionChangeEventListener(JCSCommandStation trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onDirectionChange(LocomotiveDirectionEvent directionEvent) {
      LocomotiveBean lb = directionEvent.getLocomotiveBean();
      if (lb != null) {
        LocomotiveBean dblb = null;
        //For marklin and Ecos use the ID 
        if ("marklin.cs".equals(lb.getCommandStationId()) || "esu-ecos".equals(lb.getCommandStationId())) {
          dblb = PersistenceFactory.getService().getLocomotive(lb.getId());
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

        if (dblb != null) {
          if (!Objects.equals(dblb.getRichtung(), lb.getRichtung())) {
            Integer richtung = lb.getRichtung();
            dblb.setRichtung(richtung);
            PersistenceFactory.getService().persist(dblb);

            Logger.trace(dblb.getId() + ", " + dblb.getName() + ": " + dblb.getDirection().getDirection());

            directionEvent.setLocomotiveBean(dblb);

            for (LocomotiveDirectionEventListener dl : this.trackService.locomotiveDirectionEventListeners) {
              dl.onDirectionChange(directionEvent);
            }
          }
        } else {
          Logger.trace("No loc found for " + lb.toLogString());
        }
      }
    }
  }

  private class LocomotiveSpeedChangeEventListener implements LocomotiveSpeedEventListener {

    private final JCSCommandStation trackService;

    LocomotiveSpeedChangeEventListener(JCSCommandStation trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onSpeedChange(LocomotiveSpeedEvent speedEvent) {
      LocomotiveBean lb = speedEvent.getLocomotiveBean();
      if (lb != null) {
        LocomotiveBean dblb;
        //For marklin and Ecos use the ID 
        if ("marklin.cs".equals(lb.getCommandStationId()) || "esu-ecos".equals(lb.getCommandStationId())) {
          dblb = PersistenceFactory.getService().getLocomotive(lb.getId());
        } else {
          Integer address;
          if (lb.getAddress() != null) {
            address = lb.getAddress();
          } else {
            address = lb.getId().intValue();
          }
          dblb = PersistenceFactory.getService().getLocomotive(address, lb.getDecoderType(), lb.getCommandStationId());
        }

        if (dblb != null) {
          Integer velocity = lb.getVelocity();
          dblb.setVelocity(velocity);
          PersistenceFactory.getService().persist(dblb);

          speedEvent.setLocomotiveBean(dblb);
          for (LocomotiveSpeedEventListener dl : trackService.locomotiveSpeedEventListeners) {
            if (dl != null) {
              dl.onSpeedChange(speedEvent);
            }
          }
        } else {
          if ("marklin.cs".equals(lb.getCommandStationId()) || "esu-ecos".equals(lb.getCommandStationId())) {
            Logger.trace("No loc with id " + lb.getId() + ", " + lb.getCommandStationId());
          } else {
            Logger.trace("No loc found for " + lb.toLogString());
          }
        }
      }
    }
  }

  private class ConnectionListener implements ConnectionEventListener {

    private final JCSCommandStation jcsCommandStation;

    ConnectionListener(JCSCommandStation jcsCommandStation) {
      this.jcsCommandStation = jcsCommandStation;
    }

    @Override
    public void onConnectionChange(ConnectionEvent event) {
      if (event.isConnected()) {
        Logger.trace(event.getSource() + " has re-connected!");
      } else {
        Logger.trace(event.getSource() + " is Disconnected!");
        //jcsCommandStationImpl.disconnect();
      }
    }
  }

}
