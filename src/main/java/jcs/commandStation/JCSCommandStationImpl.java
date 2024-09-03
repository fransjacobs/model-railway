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
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.DisconnectionEvent;
import jcs.commandStation.events.DisconnectionEventListener;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.MeasurementEventListener;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.Protocol;
import jcs.entities.FunctionBean;
import jcs.entities.InfoBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * The JCSCommandStation is the layer between the UI, engines and Command stations
 */
public class JCSCommandStationImpl implements JCSCommandStation {

  private DecoderController decoderController;
  private Map<String, AccessoryController> accessoryControllers;
  private Map<String, FeedbackController> feedbackControllers;

  private final List<SensorEventListener> anonymousSensorListeners;
  //private final Map<String, SensorEventListener> sensorEventListeners;

  private final List<AccessoryEventListener> accessoryEventListeners;
  private final List<LocomotiveFunctionEventListener> LocomotiveFunctionEventListeners;

  private final List<LocomotiveDirectionEventListener> locomotiveDirectionEventListeners;
  private final List<LocomotiveSpeedEventListener> locomotiveSpeedEventListeners;

  private final List<MeasurementEventListener> measurementEventListeners;

  private final Set<Protocol> supportedProtocols;
  private CommandStationBean commandStation;

  public JCSCommandStationImpl() {
    this("true".equalsIgnoreCase(System.getProperty("skip.controller.autoconnect", "true")));
  }

  private JCSCommandStationImpl(boolean autoConnectController) {
    accessoryControllers = new HashMap<>();
    feedbackControllers = new HashMap<>();

    anonymousSensorListeners = new LinkedList<>();
    //sensorEventListeners = new HashMap<>();
    accessoryEventListeners = new LinkedList<>();
    LocomotiveFunctionEventListeners = new LinkedList<>();
    locomotiveDirectionEventListeners = new LinkedList<>();
    locomotiveSpeedEventListeners = new LinkedList<>();
    measurementEventListeners = new LinkedList<>();
    supportedProtocols = new HashSet<>();

    try {
      if (autoConnectController && decoderController != null && decoderController.getCommandStationBean() != null || accessoryControllers.isEmpty() || feedbackControllers.isEmpty()) {
        connect();
        Logger.trace(decoderController != null ? "Aquired " + decoderController.getClass().getSimpleName() : "Could not aquire a Command Station! " + (decoderController.isConnected() ? "Connected" : "NOT Connected"));
      } else {
        Logger.trace("Auto Connect disabled");
      }
    } catch (Exception e) {
      Logger.warn("Can't connect with default Command Station!");
    }
  }

  @Override
  public final boolean connect() {
    //TODO revice the connect, to nices code and preventing duplicat instantiations...
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

    if (decoderControllerConnected && !allreadyConnected) {
      decoderController.addDisconnectionEventListener(new DisconnectionListener(this));

      decoderController.addLocomotiveFunctionEventListener(new LocomotiveFunctionChangeEventListener(this));
      decoderController.addLocomotiveDirectionEventListener(new LocomotiveDirectionChangeEventListener(this));
      decoderController.addLocomotiveSpeedEventListener(new LocomotiveSpeedChangeEventListener(this));

      supportedProtocols.addAll(decoderController.getCommandStationBean().getSupportedProtocols());

      if (this.decoderController.isSupportTrackMeasurements()) {
        //Start the measurements background task
        long measureInterval = Long.parseLong(System.getProperty("track.measurements.interval", "5"));
        measureInterval = measureInterval * 1000;

        if (measureInterval > 0) {
          TrackMeasurementTask measurementTask = new TrackMeasurementTask(this);
          Timer timer = new Timer("Timer");
          timer.schedule(measurementTask, 0, measureInterval);
          Logger.debug("Started Track measurements with an interval of " + measureInterval + "s");
        } else {
          Logger.debug("Skipping Track measurements");
        }
      } else {
        Logger.debug("Track measurements are not supported");
      }
    }

    if (accessoryCntrConnected > 0 && !allreadyConnected) {
      for (AccessoryController ac : accessoryControllers.values()) {
        if (ac.isConnected()) {
          ac.addAccessoryEventListener(new AccessoryChangeEventListener(this));
          ac.addDisconnectionEventListener(new DisconnectionListener(this));
        }
      }
    }

    if (feedbackCntrConnected > 0 && !allreadyConnected) {
      for (FeedbackController fc : feedbackControllers.values()) {
        if (fc.isConnected()) {
          fc.addSensorEventListener(new SensorChangeEventListener(this));
          fc.addDisconnectionEventListener(new DisconnectionListener(this));
        }
      }
    }

    //TODO implement get the day end i.e. the current state of all Objects on track
    return decoderControllerConnected;
  }

  @Override
  public CommandStationBean getCommandStationBean() {
    if (this.decoderController != null) {
      return this.decoderController.getCommandStationBean();
    } else {
      return null;
    }
  }

  @Override
  public boolean isConnected() {
    if (decoderController != null) {
      return decoderController.isConnected();
    } else {
      return false;
    }
  }

  @Override
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
    this.decoderController = null;
    this.accessoryControllers.clear();
    this.feedbackControllers.clear();
  }

  @Override
  public Image getLocomotiveImage(String imageName
  ) {
    Image image = null;

    if (decoderController != null) {
      image = decoderController.getLocomotiveImage(imageName);
      if (image != null) {
        storeImage(image, imageName, true);
      }
    }
    return image;
  }

  @Override
  public Image getLocomotiveFunctionImage(String imageName
  ) {
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

  @Override
  public InfoBean getCommandStationInfo() {
    if (this.decoderController != null && this.decoderController.getDevice() != null) {
      return this.decoderController.getCommandStationInfo();
    } else {
      return null;
    }
  }

  //@Override
  public String getCommandStationName() {
    if (decoderController != null) {
      if (decoderController.getDevice() != null) {
        return decoderController.getDevice().getName();
      } else {
        return decoderController.getCommandStationBean().getDescription();
      }
    } else {
      return null;
    }
  }

  //@Override
  public String getCommandStationSerialNumber() {
    if (decoderController != null && decoderController.getDevice() != null) {
      return decoderController.getDevice().getSerial();
    } else {
      return null;
    }
  }

  //@Override
  public String getCommandStationArticleNumber() {
    if (decoderController != null && decoderController.getDevice() != null) {
      return decoderController.getDevice().getArticleNumber();
    } else {
      return null;
    }
  }

  @Override
  public void switchPower(boolean on) {
    //Logger.trace("Switch Power " + (on ? "On" : "Off"));
    if (decoderController != null) {
      decoderController.power(on);
    }
  }

  @Override
  public boolean isPowerOn() {
    boolean power = false;
    if (decoderController != null) {
      power = decoderController.isPower();
    }
    return power;
  }

  @Override
  public void changeLocomotiveDirection(Direction newDirection, LocomotiveBean locomotive) {
    Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.getName() + " id: " + locomotive.getId());

    int address;
    if (supportedProtocols.size() == 1) {
      address = locomotive.getAddress();
    } else {
      address = locomotive.getUid().intValue();
    }
    if (decoderController != null) {
      //Set the velocity to zero before changing the direction
      //Run this in a worker thread...

      decoderController.changeVelocity(address, 0, locomotive.getDirection());
      decoderController.changeDirection(address, newDirection);
    }
  }

  @Override
  public void changeLocomotiveSpeed(Integer newVelocity, LocomotiveBean locomotive) {
    Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());
    int address;
    if (supportedProtocols.size() == 1) {
      address = locomotive.getAddress();
    } else {
      address = locomotive.getUid().intValue();
    }
    if (decoderController != null) {
      decoderController.changeVelocity(address, newVelocity, locomotive.getDirection());
    }
  }

  @Override
  public void changeLocomotiveFunction(Boolean newValue, Integer functionNumber, LocomotiveBean locomotive) {
    Logger.trace("Changing Function " + functionNumber + " to " + (newValue ? "on" : "off") + " on " + locomotive.getName());
    int address;
    if (this.supportedProtocols.size() == 1) {
      address = locomotive.getAddress();
    } else {
      address = locomotive.getUid().intValue();
    }
    if (decoderController != null) {
      decoderController.changeFunctionValue(address, functionNumber, newValue);
    }
  }

  @Override
  public void switchAccessory(AccessoryBean accessory, AccessoryValue value) {
    Integer address = accessory.getAddress();
    Integer switchTime = accessory.getSwitchTime();
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
        //val = AccessoryValue.cs3Get(state - 2);
        val = AccessoryValue.get(state - 2);
      }
    }

    Logger.trace("Change accessory with address: " + address + ", " + accessory.getName() + " to " + val.getValue());

    for (AccessoryController ac : accessoryControllers.values()) {
      ac.switchAccessory(address, val, switchTime);
    }
  }

  @Override
  public void addSensorEventListener(SensorEventListener listener) {
    this.anonymousSensorListeners.add(listener);
  }

  @Override
  public void removeSensorEventListener(SensorEventListener listener) {
    this.anonymousSensorListeners.remove(listener);
  }

  @Override
  public void addAccessoryEventListener(AccessoryEventListener listener) {
    this.accessoryEventListeners.add(listener);
  }

  @Override
  public void removeAccessoryEventListener(AccessoryEventListener listener) {
    this.accessoryEventListeners.remove(listener);
  }

  @Override
  public void addLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    this.LocomotiveFunctionEventListeners.add(listener);
  }

  @Override
  public void removeLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    this.LocomotiveFunctionEventListeners.remove(listener);
  }

  @Override
  public void addLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    this.locomotiveDirectionEventListeners.add(listener);
  }

  @Override
  public void removeLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    this.locomotiveDirectionEventListeners.remove(listener);
  }

  @Override
  public void addLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    this.locomotiveSpeedEventListeners.add(listener);
    //this.decoderController.addLocomotiveSpeedEventListener(listener);

  }

//  private void getLocomotiveSpeedEventListeners() {
//    this.decoderController.
//  }
  @Override
  public void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    this.locomotiveSpeedEventListeners.remove(listener);
    //this.decoderController.addLocomotiveSpeedEventListener(listener);
  }

  @Override
  public void addDisconnectionEventListener(DisconnectionEventListener listener) {
    if (this.decoderController != null) {
      this.decoderController.addDisconnectionEventListener(listener);
    }
    for (AccessoryController ac : this.accessoryControllers.values()) {
      if (ac != this.decoderController) {
        ac.addDisconnectionEventListener(listener);
      }
    }

    for (FeedbackController fc : this.feedbackControllers.values()) {
      if (fc != this.decoderController) {
        fc.addDisconnectionEventListener(listener);
      }
    }
  }

  @Override
  public void addPowerEventListener(PowerEventListener listener) {
    if (this.decoderController != null) {
      this.decoderController.addPowerEventListener(listener);
    }
  }

  @Override
  public void removePowerEventListener(PowerEventListener listener) {
    if (this.decoderController != null) {
      this.decoderController.removePowerEventListener(listener);
    }
  }

  @Override
  public void addMeasurementEventListener(MeasurementEventListener listener) {
    this.measurementEventListeners.add(listener);
  }

  @Override
  public void removeMeasurementListener(MeasurementEventListener listener) {
    this.measurementEventListeners.remove(listener);
  }

  @Override
  public DecoderController getDecoderController() {
    return decoderController;
  }

  @Override
  public List<AccessoryController> getAccessoryControllers() {
    return accessoryControllers.values().stream().collect(Collectors.toList());
  }

  @Override
  public List<FeedbackController> getFeedbackControllers() {
    return feedbackControllers.values().stream().collect(Collectors.toList());
  }

  private class TrackMeasurementTask extends TimerTask {

    private final JCSCommandStationImpl Controller;
    private boolean debuglog = false;

    TrackMeasurementTask(JCSCommandStationImpl Controller) {
      this.Controller = Controller;
      this.debuglog = System.getProperty("debug.measurements", "false").equalsIgnoreCase("true");
    }

    @Override
    public void run() {
      if (this.Controller != null && this.Controller.decoderController != null) {
        Map<Integer, ChannelBean> measurements = this.Controller.decoderController.getTrackMeasurements();
        for (ChannelBean ch : measurements.values()) {
          if (ch.isChanged()) {
            MeasurementEvent me = new MeasurementEvent(ch);
            if (debuglog) {
              Logger.trace("Changed Channel " + ch.getNumber() + ", " + ch.getName() + ": " + ch.getHumanValue() + " " + ch.getUnit());
            }
            for (MeasurementEventListener mel : this.Controller.measurementEventListeners) {
              mel.onMeasurement(me);
            }
          }
        }
      }
    }
  }

  private class SensorChangeEventListener implements SensorEventListener {

    private final JCSCommandStationImpl commandStation;

    SensorChangeEventListener(JCSCommandStationImpl commandStation) {
      this.commandStation = commandStation;
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      SensorBean sb = event.getSensorBean();
      SensorBean dbsb = PersistenceFactory.getService().getSensor(sb.getDeviceId(), sb.getContactId());

      if (dbsb != null) {
        sb.setId(dbsb.getId());
        sb.setName(dbsb.getName());
        PersistenceFactory.getService().persist(sb);
      }

      //Avoid concurrent modification exceptions
      List<SensorEventListener> snapshot = new ArrayList<>(commandStation.anonymousSensorListeners);

      for (SensorEventListener sl : snapshot) {
        if (sl != null) {
          sl.onSensorChange(event);
        }
      }
    }
  }

  private class AccessoryChangeEventListener implements AccessoryEventListener {

    private final JCSCommandStationImpl trackService;

    AccessoryChangeEventListener(JCSCommandStationImpl trackService) {
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

    private final JCSCommandStationImpl trackService;

    LocomotiveFunctionChangeEventListener(JCSCommandStationImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onFunctionChange(LocomotiveFunctionEvent functionEvent) {
      FunctionBean fb = functionEvent.getFunctionBean();

      FunctionBean dbfb = null;
      if ("marklin.cs".equals(trackService.getDecoderController().getCommandStationBean().getId())) {
        dbfb = PersistenceFactory.getService().getLocomotiveFunction(fb.getLocomotiveId(), fb.getNumber());
      } else {
        Integer address = fb.getLocomotiveId().intValue();
        LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(address, DecoderType.get(fb.getDecoderTypeString()), fb.getCommandStationId());
        if (dblb != null) {
          dbfb = PersistenceFactory.getService().getLocomotiveFunction(dblb.getId(), fb.getNumber());
        }
      }

//      if (dbfb == null) {
//        //try via loc address and decoder type
//        Integer address = fb.getLocomotiveId().intValue();
//        LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(address, DecoderType.get(fb.getDecoderTypeString()), fb.getCommandStationId());
//        if (dblb != null) {
//          dbfb = PersistenceFactory.getService().getLocomotiveFunction(dblb.getId(), fb.getNumber());
//        }
//      }
      if (dbfb != null) {
        if (!Objects.equals(dbfb.getValue(), fb.getValue())) {
          dbfb.setValue(fb.getValue());
          if (!dbfb.isMomentary()) {
            PersistenceFactory.getService().persist(dbfb);
            functionEvent.setFunctionBean(dbfb);
          }
          for (LocomotiveFunctionEventListener fl : trackService.LocomotiveFunctionEventListeners) {
            fl.onFunctionChange(functionEvent);
          }
        }
      }
    }
  }

  private class LocomotiveDirectionChangeEventListener implements LocomotiveDirectionEventListener {

    private final JCSCommandStationImpl trackService;

    LocomotiveDirectionChangeEventListener(JCSCommandStationImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onDirectionChange(LocomotiveDirectionEvent directionEvent) {
      LocomotiveBean lb = directionEvent.getLocomotiveBean();
      if (lb != null) {
        LocomotiveBean dblb = null;
        //For marklin use the ID 
        if ("marklin.cs".equals(lb.getCommandStationId())) {
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

    private final JCSCommandStationImpl trackService;

    LocomotiveSpeedChangeEventListener(JCSCommandStationImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onSpeedChange(LocomotiveSpeedEvent speedEvent) {
      LocomotiveBean lb = speedEvent.getLocomotiveBean();
      if (lb != null) {
        LocomotiveBean dblb;
        //For marklin use the ID 
        if ("marklin.cs".equals(lb.getCommandStationId())) {
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
          Logger.trace("No loc found for " + lb.toLogString());
        }
      }
    }
  }

  private class DisconnectionListener implements DisconnectionEventListener {

    private final JCSCommandStationImpl jcsCommandStationImpl;

    DisconnectionListener(JCSCommandStationImpl jcsCommandStationImpl) {
      this.jcsCommandStationImpl = jcsCommandStationImpl;
    }

    @Override
    public void onDisconnect(DisconnectionEvent event) {
      Logger.trace(event.getSource() + " is Disconnected!");
      jcsCommandStationImpl.disconnect();
    }
  }

}
