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
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import jcs.JCS;
import jcs.controller.events.SensorEvent;
import jcs.entities.SensorBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import jcs.entities.AccessoryBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;
import jcs.controller.events.AccessoryEvent;
import jcs.controller.events.LocomotiveDirectionEvent;
import jcs.controller.events.LocomotiveFunctionEvent;
import jcs.controller.events.LocomotiveSpeedEvent;
import jcs.persistence.PersistenceFactory;
import jcs.controller.events.AccessoryEventListener;
import jcs.controller.events.PowerEventListener;
import jcs.controller.events.SensorEventListener;
import jcs.controller.events.LocomotiveFunctionEventListener;
import jcs.controller.events.LocomotiveDirectionEventListener;
import jcs.controller.events.LocomotiveSpeedEventListener;
import jcs.controller.cs.MarklinCentralStation;

/**
 * The Controller Implementation is the implementation of the Controller Interface. Its purpose is to serve as an abstraction layer for Controllers so that in the future more Controllers can be
 * implemented
 */
public class ControllerImpl implements Controller {

  private MarklinCentralStation vendorController;

  private final List<SensorEventListener> sensorEventListeners;
  private final List<AccessoryEventListener> accessoryEventListeners;
  private final List<LocomotiveFunctionEventListener> LocomotiveFunctionEventListeners;

  private final List<LocomotiveDirectionEventListener> locomotiveDirectionEventListeners;
  private final List<LocomotiveSpeedEventListener> locomotiveSpeedEventListeners;

  public ControllerImpl() {
    this("true".equalsIgnoreCase(System.getProperty("controller.autoconnect", "true")));
  }

  private ControllerImpl(boolean autoConnectController) {
    sensorEventListeners = new LinkedList<>();
    accessoryEventListeners = new LinkedList<>();
    LocomotiveFunctionEventListeners = new LinkedList<>();
    locomotiveDirectionEventListeners = new LinkedList<>();
    locomotiveSpeedEventListeners = new LinkedList<>();

    if (autoConnectController) {
      connect();
      Logger.trace(vendorController != null ? "Aquired " + vendorController.getClass().getSimpleName() : "Could not aquire a Vendor Controller Service! " + (vendorController.isConnected() ? "Connected" : "NOT Connected"));
    }
  }

  @Override
  public final boolean connect() {
    if (vendorController != null && vendorController.isConnected()) {
      return vendorController.isConnected();
    }
    JCS.logProgress("Connecting to Vendor Controller");
    String controllerImplClassName = System.getProperty("vendorController");

    if (vendorController == null) {
      try {
        //TODO make the interface more abstract..
        this.vendorController = (MarklinCentralStation) Class.forName(controllerImplClassName).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
        Logger.error("Can't instantiate a '" + controllerImplClassName + "' " + ex.getMessage());
      }
    }

    if (vendorController != null && vendorController.connect()) {
      this.vendorController.addSensorEventListener(new SensorChangeEventListener(this));
      this.vendorController.addAccessoryEventListener(new AccessoryChangeEventListener(this));
      this.vendorController.addLocomotiveFunctionEventListener(new LocomotiveFunctionChangeEventListener(this));
      this.vendorController.addLocomotiveDirectionEventListener(new LocomotiveDirectionChangeEventListener(this));
      this.vendorController.addLocomotiveSpeedEventListener(new LocomotiveSpeedChangeEventListener(this));
    }

    //TODO implement get the day end i.e. the current stata of all Objects on track
    JCS.logProgress("Obtaining the last state of all items...");

    return this.vendorController != null && this.vendorController.isConnected();
  }

  @Override
  public boolean isConnected() {
    return this.vendorController.isConnected();
  }

  @Override
  public void disconnect() {
    this.vendorController.disconnect();
    this.vendorController = null;
  }

  public Image getLocomotiveImage(String imageName) {
    Image image = vendorController.getLocomotiveImage(imageName);
    if (image != null) {
      storeImage(image, imageName);
    }
    return image;
  }

  private void storeImage(Image image, String imageName) {
    Path path = Paths.get(System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache" + File.separator + vendorController.getDevice().getSerialNumber());
    File imageFile = new File(path + File.separator + imageName + ".png");

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
  public String getControllerName() {
    if (this.vendorController != null && this.vendorController.getDevice() != null) {
      return this.vendorController.getDevice().getDeviceName();
    } else {
      return null;
    }
  }

  @Override
  public String getControllerSerialNumber() {
    if (this.vendorController != null && this.vendorController.getDevice() != null) {
      return this.vendorController.getDevice().getSerialNumber();
    } else {
      return null;
    }
  }

  @Override
  public String getControllerArticleNumber() {
    if (this.vendorController != null && this.vendorController.getDevice() != null) {
      return this.vendorController.getDevice().getArticleNumber();
    } else {
      return null;
    }
  }

  @Override
  public void addPowerEventListener(PowerEventListener listener) {
    if (this.vendorController != null) {
      this.vendorController.addPowerEventListener(listener);
    }
  }

  @Override
  public void switchPower(boolean on) {
    Logger.trace("Switch Power " + (on ? "On" : "Off"));
    if (this.vendorController != null) {
      this.vendorController.power(on);
    }
  }

  @Override
  public boolean isPowerOn() {
    boolean power = false;
    if (this.vendorController != null) {
      power = vendorController.isPower();
    }

    return power;
  }

  @Override
  public void synchronizeLocomotivesWithController(PropertyChangeListener progressListener) {
    List<LocomotiveBean> fromController = this.vendorController.getLocomotives();

    if (progressListener != null) {
      PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", null, "Controller reports " + fromController.size() + " Locomotives");
      progressListener.propertyChange(pce);
    }

    for (LocomotiveBean loco : fromController) {
      Long id = loco.getId();
      LocomotiveBean dbLoco = PersistenceFactory.getService().getLocomotive(id);

      if (dbLoco != null && loco.getAddress().equals(dbLoco.getAddress()) && loco.getDecoderType().equals(dbLoco.getDecoderType())) {
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
        PersistenceFactory.getService().persist(loco);

        //Also cache the locomotive Image
        if (progressListener != null) {
          PropertyChangeEvent pce = new PropertyChangeEvent(this, "synchProcess", null, "Getting Icon for " + loco.getName());
          progressListener.propertyChange(pce);
        }
        getLocomotiveImage(loco.getIcon());

      } catch (Exception e) {
        Logger.error(e);
      }
    }

    //Also cache the function Icons
    this.vendorController.cacheAllFunctionIcons(progressListener);
  }

  @Override
  public void synchronizeTurnoutsWithController() {
    List<AccessoryBean> csTurnouts = this.vendorController.getSwitches();

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
    List<AccessoryBean> csSignals = this.vendorController.getSignals();

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
    Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.toLogString());
    Integer address = locomotive.getAddress();
    DecoderType decoderType = locomotive.getDecoderType();

    vendorController.changeVelocity(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), 0);
    vendorController.changeDirection(address, decoderType, newDirection);
  }

  @Override
  public void changeLocomotiveSpeed(Integer newVelocity, LocomotiveBean locomotive) {
    Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());
    vendorController.changeVelocity(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), newVelocity);
  }

  @Override
  public void changeLocomotiveFunction(Boolean newValue, Integer functionNumber, LocomotiveBean locomotive) {
    Logger.trace("Changing Function " + functionNumber + " to " + (newValue ? "on" : "off") + " on " + locomotive.getName());
    vendorController.changeFunctionValue(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), functionNumber, newValue);
  }

  @Override
  public void switchAccessory(AccessoryValue value, AccessoryBean accessory) {
    int address = accessory.getAddress();
    AccessoryValue val = value;
    if (accessory.isSignal() && accessory.getStates() > 2) {
      if (accessory.getPosition() > 1) {
        address = address + 1;
        val = AccessoryValue.cs3Get(accessory.getPosition() - 2);
      }
    }

    Logger.trace("Change accessory with address: " + address + ", " + accessory.getName() + " to " + val.getValue());
    vendorController.switchAccessory(address, val);
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
      LocomotiveBean lb = functionEvent.getLocomotiveBean();
      LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(lb.getId());

      if (dblb != null) {
        FunctionBean fb = lb.getFunctionBean(functionEvent.getUpdatedFunctionNumber());
        if (fb != null) {
          dblb.setFunctionValue(fb.getNumber(), fb.getValue());
          PersistenceFactory.getService().persist(dblb);
          functionEvent.setLocomotiveBean(dblb);
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
