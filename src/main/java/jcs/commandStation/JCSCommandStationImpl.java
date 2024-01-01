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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.Protocol;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.ChannelBean;
import jcs.entities.InfoBean;
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

  private final List<SensorEventListener> sensorEventListeners;
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

    sensorEventListeners = new LinkedList<>();
    accessoryEventListeners = new LinkedList<>();
    LocomotiveFunctionEventListeners = new LinkedList<>();
    locomotiveDirectionEventListeners = new LinkedList<>();
    locomotiveSpeedEventListeners = new LinkedList<>();
    measurementEventListeners = new LinkedList<>();
    supportedProtocols = new HashSet<>();

    if (autoConnectController && decoderController != null || accessoryControllers.isEmpty() || feedbackControllers.isEmpty()) {
      connect();
      Logger.trace(decoderController != null ? "Aquired " + decoderController.getClass().getSimpleName() : "Could not aquire a Command Station! " + (decoderController.isConnected() ? "Connected" : "NOT Connected"));
    } else {
      Logger.trace("Auto Connect disabled");
    }
  }

  @Override
  public final boolean connect() {
    boolean decoderConnected = false;
    int accessoryCntrConnected = 0;
    int feedbackCntrConnected = 0;

    commandStation = PersistenceFactory.getService().getDefaultCommandStation();

    if (commandStation == null) {
      Logger.error("No Default Command Station found!");
      return false;
    }

    if (decoderController == null) {
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

    if (decoderController != null) {
      decoderConnected = decoderController.isConnected();
      if (!decoderConnected) {
        decoderConnected = decoderController.connect();
      }
    }

    //Connect the Accessories controllers if needed
    if (!accessoryControllers.isEmpty()) {
      for (AccessoryController ac : accessoryControllers.values()) {
        if (ac.isConnected()) {
          accessoryCntrConnected++;
        } else {
          if (ac.connect()) {
            accessoryCntrConnected++;
          }
        }
      }
    }

    //Connect the Feedback Controllers controllers if needed
    if (!feedbackControllers.isEmpty()) {
      for (FeedbackController fc : feedbackControllers.values()) {
        if (fc.isConnected()) {
          feedbackCntrConnected++;
        } else {
          if (fc.connect()) {
            feedbackCntrConnected++;
          }
        }
      }
    }

    Logger.trace("Connected Controllers:  Decoder: " + (decoderConnected ? "Yes" : "No") + " Accessory: " + accessoryCntrConnected + " Feedback:" + feedbackCntrConnected);

    if (decoderConnected) {
      this.decoderController.addLocomotiveFunctionEventListener(new LocomotiveFunctionChangeEventListener(this));
      this.decoderController.addLocomotiveDirectionEventListener(new LocomotiveDirectionChangeEventListener(this));
      this.decoderController.addLocomotiveSpeedEventListener(new LocomotiveSpeedChangeEventListener(this));

      this.supportedProtocols.addAll(decoderController.getCommandStationBean().getSupportedProtocols());

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

      if (accessoryCntrConnected > 0) {
        for (AccessoryController ac : accessoryControllers.values()) {
          if (ac.isConnected()) {
            ac.addAccessoryEventListener(new AccessoryChangeEventListener(this));
          }
        }
      }

      if (feedbackCntrConnected > 0) {
        for (FeedbackController fc : feedbackControllers.values()) {
          if (fc.isConnected()) {
            fc.addSensorEventListener(new SensorChangeEventListener(this));
          }
        }
      }

      //TODO implement get the day end i.e. the current state of all Objects on track
    }
    return decoderConnected;
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
    if (decoderController != null) {
      decoderController.disconnect();
      decoderController = null;
    }

    for (AccessoryController ac : accessoryControllers.values()) {
      ac.disconnect();
    }

    for (FeedbackController fc : feedbackControllers.values()) {
      fc.disconnect();
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

      //    ImageIO.write((BufferedImage) imgg, "PNG", new File(test + File.separator + "FktIcon_a_ge_116" + ".png"));
    } catch (IOException ex) {
      Logger.error("Can't store image " + imageFile + "! ", ex.getMessage());
    }
    Logger.trace("Stored image " + imageName + ".png in the cache");
  }

  @Override
  public InfoBean getCommandStationInfo() {
    if (this.decoderController.getDevice() != null) {
      return this.decoderController.getCommandStationInfo();
    } else {
      return null;
    }
  }

  //@Override
  public String getCommandStationName() {
    if (this.decoderController != null) {
      if (this.decoderController.getDevice() != null) {
        return this.decoderController.getDevice().getName();
      } else {
        return decoderController.getCommandStationBean().getDescription();
      }
    } else {
      return null;
    }
  }

  //@Override
  public String getCommandStationSerialNumber() {
    if (this.decoderController != null && this.decoderController.getDevice() != null) {
      return this.decoderController.getDevice().getSerial();
    } else {
      return null;
    }
  }

  //@Override
  public String getCommandStationArticleNumber() {
    if (this.decoderController != null && this.decoderController.getDevice() != null) {
      return this.decoderController.getDevice().getArticleNumber();
    } else {
      return null;
    }
  }

  @Override
  public void switchPower(boolean on) {
    Logger.trace("Switch Power " + (on ? "On" : "Off"));
    if (this.decoderController != null) {
      this.decoderController.power(on);
    }
  }

  @Override
  public boolean isPowerOn() {
    boolean power = false;
    if (this.decoderController != null) {
      power = decoderController.isPower();
    }
    return power;
  }

  //@Override
//  public void synchronizeLocomotivesWithCommandStation(PropertyChangeListener progressListener) {
//    if (decoderController != null && this.decoderController.getCommandStationBean().isLocomotiveSynchronizationSupport()) {
//      List<LocomotiveBean> fromController = this.decoderController.getLocomotives();
//
//      String importedFrom;
//      if (this.decoderController.getDevice() != null) {
//        importedFrom = this.decoderController.getDevice().getArticleNumber();
//        importedFrom = importedFrom + this.decoderController.getDevice().getSerial();
//      } else {
//        //There are some rare situations which occur mainly during lots of testing, where the device is not found ins 10 s or so...
//        importedFrom = "CS2-xxxxxx";
//      }
//
//      Set<String> functionImageNames = new HashSet<>();
//      //Map<String,String> functionImageNames =  new HashMap();
//
//      //TODO show the progress...
//      if (progressListener != null) {
//        PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", null, "Controller reports " + fromController.size() + " Locomotives");
//        progressListener.propertyChange(pce);
//      }
//
//      for (LocomotiveBean loco : fromController) {
//        Long id = loco.getId();
//        LocomotiveBean dbLoco = PersistenceFactory.getService().getLocomotive(id);
//
//        if (dbLoco != null && loco.getId().equals(dbLoco.getId())) {
//          Logger.trace("Loco id: " + loco.getId() + ", " + loco.getName() + " Addres: " + loco.getAddress() + " Decoder: " + loco.getDecoderTypeString() + " Exists");
//
//          // Keep the name, commuter, show and lenght
//          loco.setName(dbLoco.getName());
//          loco.setCommuter(dbLoco.isCommuter());
//          loco.setShow(dbLoco.isShow());
//
//          if (progressListener != null) {
//            PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", dbLoco.getName(), "Updating " + loco.getId() + ", " + loco.getName());
//            progressListener.propertyChange(pce);
//          }
//        } else {
//          Logger.trace("New Loco, id:" + loco.getId() + ", " + loco.getName() + " Addres: " + loco.getAddress() + " Decoder: " + loco.getDecoderTypeString());
//
//          if (progressListener != null) {
//            PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", null, "Inserting " + loco.getId() + ", " + loco.getName());
//            progressListener.propertyChange(pce);
//          }
//        }
//        try {
//          loco.setImported(importedFrom);
//          PersistenceFactory.getService().persist(loco);
//
//          //Also cache the locomotive Image
//          if (progressListener != null) {
//            PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", null, "Getting Icon for " + loco.getName());
//            progressListener.propertyChange(pce);
//          }
//          getLocomotiveImage(loco.getIcon());
//
//          //Function icons...
//          Set<FunctionBean> functions = loco.getFunctions().values().stream().collect(Collectors.toSet());
//          for (FunctionBean fb : functions) {
//
//            String aIcon = fb.getActiveIcon();
//            String iIcon = fb.getInActiveIcon();
//            functionImageNames.add(aIcon);
//            functionImageNames.add(iIcon);
//          }
//        } catch (Exception e) {
//          Logger.error(e);
//        }
//      }
//
//      //Now get all the function images in one batch
//      Logger.trace("Trying to get " + functionImageNames.size() + " function images");
//      for (String functionImage : functionImageNames) {
//        boolean available = getLocomotiveFunctionImage(functionImage) != null;
//        Logger.trace("Function Image " + functionImage + " is " + (available ? "available" : "NOT available"));
//      }
//
//      //this.decoderController.clearCaches();
//    }
//  }

//  @Override
//  public void synchronizeTurnoutsWithCommandStation() {
//    List<AccessoryBean> turnouts = new LinkedList<>();
//
//    for (AccessoryController ac : this.accessoryControllers.values()) {
//      if (ac.getCommandStationBean().isAccessorySynchronizationSupport()) {
//        turnouts.addAll(ac.getSwitches());
//      }
//    }
//
//    for (AccessoryBean turnout : turnouts) {
//      AccessoryBean dbTurnout = PersistenceFactory.getService().getAccessoryByAddress(turnout.getAddress());
//      if (dbTurnout != null) {
//        turnout.setId(dbTurnout.getId());
//      }
//
//      Logger.trace(turnout.toLogString());
//      PersistenceFactory.getService().persist(turnout);
//    }
//  }

//  @Override
//  public void synchronizeSignalsWithCommandStation() {
//    List<AccessoryBean> signals = new LinkedList<>();
//
//    for (AccessoryController ac : this.accessoryControllers.values()) {
//      if (ac.getCommandStationBean().isAccessorySynchronizationSupport()) {
//        signals.addAll(ac.getSignals());
//      }
//    }
//
//    for (AccessoryBean signal : signals) {
//      AccessoryBean dbSignal = PersistenceFactory.getService().getAccessoryByAddress(signal.getAddress());
//      if (dbSignal != null) {
//        signal.setId(dbSignal.getId());
//      }
//
//      Logger.trace(signal.toLogString());
//      PersistenceFactory.getService().persist(signal);
//    }
//  }

  @Override
  public void changeLocomotiveDirection(Direction newDirection, LocomotiveBean locomotive) {
    Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.getName() + " id: " + locomotive.getId());

    int address;
    if (this.supportedProtocols.size() == 1) {
      address = locomotive.getAddress();
    } else {
      address = locomotive.getUid().intValue();
    }
    if (decoderController != null) {
      decoderController.changeVelocity(address, 0, locomotive.getDirection());
      decoderController.changeDirection(address, newDirection);
    }
  }

  @Override
  public void changeLocomotiveSpeed(Integer newVelocity, LocomotiveBean locomotive) {
    Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());
    int address;
    if (this.supportedProtocols.size() == 1) {
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
  public void switchAccessory(AccessoryValue value, AccessoryBean accessory) {
    int address = accessory.getAddress();
    int switchTime = accessory.getSwitchTime();
    AccessoryValue val = value;
    if (accessory.isSignal() && accessory.getStates() > 2) {
      if (accessory.getState() > 1) {
        address = address + 1;
        val = AccessoryValue.cs3Get(accessory.getState() - 2);
      }
    }

    Logger.trace("Change accessory with address: " + address + ", " + accessory.getName() + " to " + val.getValue());

    for (AccessoryController ac : this.accessoryControllers.values()) {
      ac.switchAccessory(address, val, switchTime);
    }
  }

  @Override
  public void addSensorEventListener(SensorEventListener listener) {
    this.sensorEventListeners.add(listener);
  }

  @Override
  public void removeSensorEventListener(SensorEventListener listener) {
    this.sensorEventListeners.remove(listener);
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
  }

  @Override
  public void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    this.locomotiveSpeedEventListeners.remove(listener);
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

  private class TrackMeasurementTask extends TimerTask {

    private final JCSCommandStationImpl Controller;

    TrackMeasurementTask(JCSCommandStationImpl Controller) {
      this.Controller = Controller;
    }

    @Override
    public void run() {
      Map<Integer, ChannelBean> measurements = this.Controller.decoderController.getTrackMeasurements();
      for (ChannelBean ch : measurements.values()) {
        if (ch.isChanged()) {
          MeasurementEvent me = new MeasurementEvent(ch);
          Logger.trace("Changed Channel " + ch.getNumber() + ", " + ch.getName() + ": " + ch.getHumanValue() + " " + ch.getUnit());
          for (MeasurementEventListener mel : this.Controller.measurementEventListeners) {
            mel.onMeasurement(me);
          }
        }
      }
    }
  }

  private class SensorChangeEventListener implements SensorEventListener {

    private final JCSCommandStationImpl trackController;

    SensorChangeEventListener(JCSCommandStationImpl trackController) {
      this.trackController = trackController;
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

      for (SensorEventListener sl : trackController.sensorEventListeners) {
        sl.onSensorChange(event);
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
      AccessoryBean dbab = PersistenceFactory.getService().getAccessoryByAddress(ab.getAddress());
      if (dbab == null) {
        //check if address is even, might be the second address of a signal
        if (address % 2 == 0) {
          address = address - 1;
          dbab = PersistenceFactory.getService().getAccessoryByAddress(address);
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
      FunctionBean dbfb = PersistenceFactory.getService().getLocomotiveFunction(fb.getLocomotiveId(), fb.getNumber());

      //if (dbfb == null) {
      //try via address and decoder type, assume DCC....
      //LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(fb.getLocomotiveId());
      //LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(fb.getAddress(), fb.getProtocol(), fb.getCommandStationId());
      //if (dblb != null) {
      //  dbfb = PersistenceFactory.getService().getLocomotiveFunction((long) dbfb.getId(), fb.getNumber());
      //}
      //}
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

      LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(lb.getId());
      if (dblb == null) {
        //try via address and decoder type
        dblb = PersistenceFactory.getService().getLocomotive(lb.getAddress(), lb.getDecoderType(), lb.getCommandStationId());
      }

      if (dblb != null) {
        if (!Objects.equals(dblb.getRichtung(), lb.getRichtung())) {
          Integer richtung = lb.getRichtung();
          dblb.setRichtung(richtung);
          PersistenceFactory.getService().persist(dblb);
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

  private class LocomotiveSpeedChangeEventListener implements LocomotiveSpeedEventListener {

    private final JCSCommandStationImpl trackService;

    LocomotiveSpeedChangeEventListener(JCSCommandStationImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onSpeedChange(LocomotiveSpeedEvent speedEvent) {
      LocomotiveBean lb = speedEvent.getLocomotiveBean();
      if (lb != null && lb.getId() != null) {

        LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(lb.getId());
        if (dblb == null) {
          //try via address and decoder type
          dblb = PersistenceFactory.getService().getLocomotive(lb.getAddress(), lb.getDecoderType(), lb.getCommandStationId());
        }

        if (dblb != null) {
          if (!Objects.equals(dblb.getVelocity(), lb.getVelocity())) {
            Integer velocity = lb.getVelocity();
            dblb.setVelocity(velocity);
            PersistenceFactory.getService().persist(dblb);

            speedEvent.setLocomotiveBean(dblb);
            for (LocomotiveSpeedEventListener dl : this.trackService.locomotiveSpeedEventListeners) {
              dl.onSpeedChange(speedEvent);
            }
          }
        } else {
          Logger.trace("No loc found for " + lb.toLogString());
        }
      }
    }
  }

}
