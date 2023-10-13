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
package jcs.controller;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import jcs.JCS;
import jcs.controller.cs.MarklinCentralStation;
import jcs.controller.events.AccessoryEvent;
import jcs.controller.events.AccessoryEventListener;
import jcs.controller.events.LocomotiveDirectionEvent;
import jcs.controller.events.LocomotiveDirectionEventListener;
import jcs.controller.events.LocomotiveFunctionEvent;
import jcs.controller.events.LocomotiveFunctionEventListener;
import jcs.controller.events.LocomotiveSpeedEvent;
import jcs.controller.events.LocomotiveSpeedEventListener;
import jcs.controller.events.MeasurementEvent;
import jcs.controller.events.MeasurementEventListener;
import jcs.controller.events.PowerEventListener;
import jcs.controller.events.SensorEvent;
import jcs.controller.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.MeasurementChannel;
import jcs.entities.SensorBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * The Command Station Controller HAL Implementation
 */
public class ControllerImpl implements CommandStation {

  private MarklinCentralStation centralStation;

  private final List<SensorEventListener> sensorEventListeners;
  private final List<AccessoryEventListener> accessoryEventListeners;
  private final List<LocomotiveFunctionEventListener> LocomotiveFunctionEventListeners;

  private final List<LocomotiveDirectionEventListener> locomotiveDirectionEventListeners;
  private final List<LocomotiveSpeedEventListener> locomotiveSpeedEventListeners;

  private final List<MeasurementEventListener> measurementEventListeners;

  public ControllerImpl() {
    this("true".equalsIgnoreCase(System.getProperty("skip.controller.autoconnect", "true")));
  }

  private ControllerImpl(boolean autoConnectController) {
    sensorEventListeners = new LinkedList<>();
    accessoryEventListeners = new LinkedList<>();
    LocomotiveFunctionEventListeners = new LinkedList<>();
    locomotiveDirectionEventListeners = new LinkedList<>();
    locomotiveSpeedEventListeners = new LinkedList<>();
    measurementEventListeners = new LinkedList<>();

    if (autoConnectController) {
      connect();
      Logger.trace(centralStation != null ? "Aquired " + centralStation.getClass().getSimpleName() : "Could not aquire a Vendor Controller Service! " + (centralStation.isConnected() ? "Connected" : "NOT Connected"));
    } else {
      Logger.trace("Auto Connect disabled");
    }
  }

  @Override
  public final boolean connect() {
    if (centralStation != null && centralStation.isConnected()) {
      return centralStation.isConnected();
    }
    String controllerImplClassName = System.getProperty("vendorController");
    JCS.logProgress("Connecting to: " + controllerImplClassName);

    if (centralStation == null) {
      try {
        //TODO make the interface more abstract..
        this.centralStation = (MarklinCentralStation) Class.forName(controllerImplClassName).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
        Logger.error("Can't instantiate a '" + controllerImplClassName + "' " + ex.getMessage());
      }
    }

    if (centralStation != null && centralStation.connect()) {
      this.centralStation.addSensorEventListener(new SensorChangeEventListener(this));
      this.centralStation.addAccessoryEventListener(new AccessoryChangeEventListener(this));
      this.centralStation.addLocomotiveFunctionEventListener(new LocomotiveFunctionChangeEventListener(this));
      this.centralStation.addLocomotiveDirectionEventListener(new LocomotiveDirectionChangeEventListener(this));
      this.centralStation.addLocomotiveSpeedEventListener(new LocomotiveSpeedChangeEventListener(this));
    }

    //TODO implement get the day end i.e. the current state of all Objects on track
    JCS.logProgress("Obtaining the last state of all items...");

    if (this.centralStation != null && this.centralStation.isConnected()) {
      //Start the measurments backgrount task
      TrackMeasusementTask measurementTask = new TrackMeasusementTask(this);
      Timer timer = new Timer("Timer");

      long delay = 5000L;
      timer.schedule(measurementTask, 0, delay);
    }

    return this.centralStation != null && this.centralStation.isConnected();
  }

  @Override
  public boolean isConnected() {
    return this.centralStation.isConnected();
  }

  @Override
  public void disconnect() {
    this.centralStation.disconnect();
    this.centralStation = null;
  }

  public Image getLocomotiveImage(String imageName) {
    Image image = centralStation.getLocomotiveImage(imageName);
    if (image != null) {
      storeImage(image, imageName, true);
    }
    return image;
  }

  public Image getLocomotiveFunctionImage(String imageName) {
    Image image = centralStation.getLocomotiveFunctionImage(imageName);
    if (image != null) {
      storeImage(image, imageName, false);
    }
    return image;
  }

  private void storeImage(Image image, String imageName, boolean locomotive) {
    Path path;
    String basePath = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + centralStation.getDevice().getSerialNumber();

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
  public String getControllerName() {
    if (this.centralStation != null && this.centralStation.getDevice() != null) {
      return this.centralStation.getDevice().getDeviceName();
    } else {
      return null;
    }
  }

  @Override
  public String getControllerSerialNumber() {
    if (this.centralStation != null && this.centralStation.getDevice() != null) {
      return this.centralStation.getDevice().getSerialNumber();
    } else {
      return null;
    }
  }

  @Override
  public String getControllerArticleNumber() {
    if (this.centralStation != null && this.centralStation.getDevice() != null) {
      return this.centralStation.getDevice().getArticleNumber();
    } else {
      return null;
    }
  }

  @Override
  public void switchPower(boolean on) {
    Logger.trace("Switch Power " + (on ? "On" : "Off"));
    if (this.centralStation != null) {
      this.centralStation.power(on);
    }
  }

  @Override
  public boolean isPowerOn() {
    boolean power = false;
    if (this.centralStation != null) {
      power = centralStation.isPower();
    }

    return power;
  }

  @Override
  public void synchronizeLocomotivesWithController(PropertyChangeListener progressListener) {
    List<LocomotiveBean> fromController = this.centralStation.getLocomotives();

    String importedFrom;
    if (this.centralStation.getDevice() != null) {
      importedFrom = this.centralStation.getDevice().isCS3() ? "CS3-" : "CS2-";
      importedFrom = importedFrom + this.centralStation.getDevice().getSerialNumber();
    } else {
      //There are some rare situations which occur mainly during lots of testing, where the device is not found ins 10 s or so...
      importedFrom = "CS2-xxxxxx";
    }

    Set<String> functionImageNames = new HashSet<>();
    //Map<String,String> functionImageNames =  new HashMap();

    //TODO show the progress...
    if (progressListener != null) {
      PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", null, "Controller reports " + fromController.size() + " Locomotives");
      progressListener.propertyChange(pce);
    }

    for (LocomotiveBean loco : fromController) {
      Long id = loco.getId();
      LocomotiveBean dbLoco = PersistenceFactory.getService().getLocomotive(id);

      if (dbLoco != null && loco.getId().equals(dbLoco.getId())) {
        Logger.trace("Loco id: " + loco.getId() + ", " + loco.getName() + " Addres: " + loco.getAddress() + " Decoder: " + loco.getDecoderTypeString() + " Exists");

        // Keep the name, commuter, show and lenght
        loco.setName(dbLoco.getName());
        loco.setCommuter(dbLoco.isCommuter());
        loco.setShow(dbLoco.isShow());
        loco.setLength(dbLoco.getLength());

        if (progressListener != null) {
          PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", dbLoco.getName(), "Updating " + loco.getId() + ", " + loco.getName());
          progressListener.propertyChange(pce);
        }
      } else {
        Logger.trace("New Loco, id:" + loco.getId() + ", " + loco.getName() + " Addres: " + loco.getAddress() + " Decoder: " + loco.getDecoderTypeString());

        if (progressListener != null) {
          PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", null, "Inserting " + loco.getId() + ", " + loco.getName());
          progressListener.propertyChange(pce);
        }
      }
      try {
        loco.setImported(importedFrom);
        PersistenceFactory.getService().persist(loco);

        //Also cache the locomotive Image
        if (progressListener != null) {
          PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", null, "Getting Icon for " + loco.getName());
          progressListener.propertyChange(pce);
        }
        getLocomotiveImage(loco.getIcon());

        //Function icons...
        Set<FunctionBean> functions = loco.getFunctions().values().stream().collect(Collectors.toSet());
        for (FunctionBean fb : functions) {

          String aIcon = fb.getActiveIcon();
          String iIcon = fb.getInActiveIcon();
          functionImageNames.add(aIcon);
          functionImageNames.add(iIcon);
        }
      } catch (Exception e) {
        Logger.error(e);
      }
    }

    //Now get all the function images in one batch
    Logger.trace("Trying to get " + functionImageNames.size() + " function images");
    for (String functionImage : functionImageNames) {
      boolean available = getLocomotiveFunctionImage(functionImage) != null;
      Logger.trace("Function Image " + functionImage + " is " + (available ? "available" : "NOT available"));
    }

    this.centralStation.clearCaches();
  }

  @Override
  public void synchronizeTurnoutsWithController() {
    List<AccessoryBean> csTurnouts = this.centralStation.getSwitches();

    for (AccessoryBean turnout : csTurnouts) {
      AccessoryBean dbTurnout = PersistenceFactory.getService().getAccessoryByAddress(turnout.getAddress());
      if (dbTurnout != null) {
        turnout.setId(dbTurnout.getId());
      }

      Logger.trace(turnout.toLogString());
      PersistenceFactory.getService().persist(turnout);
    }
  }

  @Override
  public void synchronizeSignalsWithController() {
    List<AccessoryBean> csSignals = this.centralStation.getSignals();

    for (AccessoryBean signal : csSignals) {
      AccessoryBean dbSignal = PersistenceFactory.getService().getAccessoryByAddress(signal.getAddress());
      if (dbSignal != null) {
        signal.setId(dbSignal.getId());
      }

      Logger.trace(signal.toLogString());
      PersistenceFactory.getService().persist(signal);
    }
  }

  @Override
  public void changeLocomotiveDirection(Direction newDirection, LocomotiveBean locomotive) {
    Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.getName());
    centralStation.changeVelocity(locomotive.getUid().intValue(), 0);
    centralStation.changeDirection(locomotive.getUid().intValue(), newDirection);
  }

  @Override
  public void changeLocomotiveSpeed(Integer newVelocity, LocomotiveBean locomotive) {
    Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());
    centralStation.changeVelocity(locomotive.getUid().intValue(), newVelocity);
  }

  @Override
  public void changeLocomotiveFunction(Boolean newValue, Integer functionNumber, LocomotiveBean locomotive) {
    Logger.trace("Changing Function " + functionNumber + " to " + (newValue ? "on" : "off") + " on " + locomotive.getName());
    centralStation.changeFunctionValue(locomotive.getUid().intValue(), functionNumber, newValue);
  }

  @Override
  public void switchAccessory(AccessoryValue value, AccessoryBean accessory) {
    int address = accessory.getAddress();
    int switchTime = accessory.getSwitchTime();
    AccessoryValue val = value;
    if (accessory.isSignal() && accessory.getStates() > 2) {
      if (accessory.getPosition() > 1) {
        address = address + 1;
        val = AccessoryValue.cs3Get(accessory.getPosition() - 2);
      }
    }

    Logger.trace("Change accessory with address: " + address + ", " + accessory.getName() + " to " + val.getValue());
    centralStation.switchAccessory(address, val, switchTime);
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
    if (this.centralStation != null) {
      this.centralStation.addPowerEventListener(listener);
    }
  }

  @Override
  public void removePowerEventListener(PowerEventListener listener) {
    if (this.centralStation != null) {
      this.centralStation.removePowerEventListener(listener);
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

  private class TrackMeasusementTask extends TimerTask {

    private final ControllerImpl Controller;

    TrackMeasusementTask(ControllerImpl Controller) {
      this.Controller = Controller;
    }

    @Override
    public void run() {

      Map<Integer, MeasurementChannel> measurements = this.Controller.centralStation.getTrackMeasurements();
      for (MeasurementChannel ch : measurements.values()) {
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

    private final ControllerImpl trackController;

    SensorChangeEventListener(ControllerImpl trackController) {
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

    private final ControllerImpl trackService;

    AccessoryChangeEventListener(ControllerImpl trackService) {
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
            int p = ab.getPosition() + 2;
            ab.setPosition(p);
          } else {
            dbab = null;
          }
        }
      }

      if (dbab != null) {
        //set all properties
        ab.setId(dbab.getId());
        ab.setDecoder(dbab.getDecoder());
        ab.setDecoderType(dbab.getDecoderType());
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

    private final ControllerImpl trackService;

    LocomotiveFunctionChangeEventListener(ControllerImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onFunctionChange(LocomotiveFunctionEvent functionEvent) {
      FunctionBean fb = functionEvent.getFunctionBean();
      FunctionBean dbfb = PersistenceFactory.getService().getLocomotiveFunction(fb.getLocomotiveId(), fb.getNumber());

      if (dbfb != null) {
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

  private class LocomotiveDirectionChangeEventListener implements LocomotiveDirectionEventListener {

    private final ControllerImpl trackService;

    LocomotiveDirectionChangeEventListener(ControllerImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onDirectionChange(LocomotiveDirectionEvent directionEvent) {
      LocomotiveBean lb = directionEvent.getLocomotiveBean();
      LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(lb.getId());

      if (dblb != null) {
        Integer richtung = lb.getRichtung();
        dblb.setRichtung(richtung);
        PersistenceFactory.getService().persist(dblb);
        directionEvent.setLocomotiveBean(dblb);

        for (LocomotiveDirectionEventListener dl : this.trackService.locomotiveDirectionEventListeners) {
          dl.onDirectionChange(directionEvent);
        }
      }
    }
  }

  private class LocomotiveSpeedChangeEventListener implements LocomotiveSpeedEventListener {

    private final ControllerImpl trackService;

    LocomotiveSpeedChangeEventListener(ControllerImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onSpeedChange(LocomotiveSpeedEvent speedEvent) {
      LocomotiveBean lb = speedEvent.getLocomotiveBean();
      if (lb != null && lb.getId() != null) {
        LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(lb.getId());

        if (dblb != null) {
          Integer velocity = lb.getVelocity();
          dblb.setVelocity(velocity);
          PersistenceFactory.getService().persist(dblb);

          speedEvent.setLocomotiveBean(dblb);
          for (LocomotiveSpeedEventListener dl : this.trackService.locomotiveSpeedEventListeners) {
            dl.onSpeedChange(speedEvent);
          }
        }
      }
    }
  }

}
