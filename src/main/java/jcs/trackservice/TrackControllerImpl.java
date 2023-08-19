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
package jcs.trackservice;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import jcs.JCS;
import jcs.controller.cs3.events.SensorMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.entities.JCSPropertyBean;
import jcs.entities.SensorBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import jcs.trackservice.events.AccessoryListener;
import jcs.controller.cs3.events.SensorMessageListener;
import jcs.entities.AccessoryBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.trackservice.events.SensorListener;
import org.tinylog.Logger;
import jcs.controller.MarklinController;
import jcs.controller.cs3.devices.LinkSxx;
import jcs.controller.cs3.events.AccessoryMessageEvent;
import jcs.controller.cs3.events.AccessoryMessageEventListener;
import jcs.controller.cs3.events.DirectionMessageEvent;
import jcs.controller.cs3.events.DirectionMessageEventListener;
import jcs.controller.cs3.events.FunctionMessageEvent;
import jcs.controller.cs3.events.PowerEventListener;
import jcs.controller.cs3.events.FunctionMessageEventListener;
import jcs.controller.cs3.events.VelocityMessageEvent;
import jcs.controller.cs3.events.VelocityMessageEventListener;
import jcs.persistence.PersistenceFactory;
import jcs.trackservice.events.DirectionListener;
import jcs.trackservice.events.FunctionListener;
import jcs.trackservice.events.VelocityListener;

public class TrackControllerImpl implements TrackController {

  private MarklinController controllerService;

  private final List<SensorListener> sensorListeners;
  private final List<AccessoryListener> accessoryListeners;
  private final List<FunctionListener> functionListeners;

  private final List<DirectionListener> directionListeners;
  private final List<VelocityListener> velocityListeners;

  private final Properties jcsProperties;

  public TrackControllerImpl() {
    this(true);
  }

  private TrackControllerImpl(boolean aquireControllerService) {
    jcsProperties = new Properties();

    sensorListeners = new LinkedList<>();
    accessoryListeners = new LinkedList<>();
    functionListeners = new LinkedList<>();
    directionListeners = new LinkedList<>();
    velocityListeners = new LinkedList<>();

    retrieveJCSProperties();

    if (aquireControllerService) {
      if (System.getProperty("trackServiceSkipControllerInit", "false").equals("true")) {
        Logger.info("Skipping controller initialization...");
      } else {
        connect();
        Logger.trace(controllerService != null ? "Aquired " + controllerService.getClass().getSimpleName() : "Could not aquire a Controller Service!");
      }
    }
  }

  private void retrieveJCSProperties() {
    JCS.logProgress("Obtain properties from Persistent store");
    List<JCSPropertyBean> props = PersistenceFactory.getService().getProperties();
    props.forEach(p -> {
      jcsProperties.setProperty(p.getKey(), p.getValue());
      System.setProperty(p.getKey(), p.getValue());
    });
  }

  @Override
  public final boolean connect() {
    JCS.logProgress("Connecting to Central Station");
    String controllerImpl = System.getProperty("CS3");

    if (controllerService == null) {
      try {
        this.controllerService = (MarklinController) Class.forName(controllerImpl).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
        Logger.error("Can't instantiate a '" + controllerImpl + "' " + ex.getMessage());
      }
    }

    if (controllerService != null) {
      this.controllerService.addSensorMessageListener(new SensorMessageEventListener(this));
      this.controllerService.addAccessoryEventListener(new AccessoryMessageListener(this));
      this.controllerService.addFunctionMessageEventListener(new FunctionMessageListener(this));
      this.controllerService.addDirectionMessageEventListener(new DirectionMessageListener(this));
      this.controllerService.addVelocityMessageEventListener(new VelocityMessageListener(this));
    }

    JCS.logProgress("Obtaining the last state of all items...");

    return this.controllerService != null && this.controllerService.isConnected();
  }

  @Override
  public boolean isConnected() {
    return this.controllerService.isConnected();
  }

  @Override
  public void disconnect() {
    this.controllerService.disconnect();
    this.controllerService = null;
  }

  public Image getLocomotiveImage(String imageName) {
    Image image = controllerService.getLocomotiveImage(imageName);
    if (image != null) {
      storeImage(image, imageName);
    }
    return image;
  }

  private void storeImage(Image image, String imageName) {
    String path = System.getProperty("user.home") + File.separator + "jcs" + File.separator + "cache";
    File cachePath = new File(path);
    if (cachePath.mkdir()) {
      Logger.trace("Created new directory " + cachePath);
    }
    try {
      ImageIO.write((BufferedImage) image, "png", new File(path + File.separator + imageName + ".png"));
    } catch (IOException ex) {
      Logger.error("Can't store image " + cachePath.getName() + "! ", ex.getMessage());
    }
    Logger.trace("Stored image " + imageName + ".png in the cache");
  }

  @Override
  public String getControllerName() {
    if (this.controllerService != null) {
      return this.controllerService.getDevice().getDeviceName();
    } else {
      return null;
    }
  }

  @Override
  public String getControllerSerialNumber() {
    if (this.controllerService != null) {
      return this.controllerService.getDevice().getSerialNumber();
    } else {
      return null;
    }
  }

  @Override
  public String getControllerArticleNumber() {
    if (this.controllerService != null) {
      return this.controllerService.getDevice().getArticleNumber();
    } else {
      return null;
    }
  }

//  @Override
//  public LinkSxx getLinkSxx() {
//    if (this.controllerService != null) {
//      return this.controllerService.getLinkSxx();
//    } else {
//      return null;
//    }
//  }

  @Override
  public void addPowerEventListener(PowerEventListener listener) {
    if (this.controllerService != null) {
      this.controllerService.addPowerEventListener(listener);
    }
  }

  @Override
  public void removePowerEventListener(PowerEventListener listener) {
    if (this.controllerService != null) {
      this.controllerService.removePowerEventListener(listener);
    }
  }

  @Override
  public void switchPower(boolean on) {
    Logger.trace("Switch Power " + (on ? "On" : "Off"));
    if (this.controllerService != null) {
      this.controllerService.power(on);
    }
  }

  @Override
  public boolean isPowerOn() {
    boolean power = false;
    if (this.controllerService != null) {
      power = controllerService.isPower();
    }

    return power;
  }

  @Override
  public void synchronizeLocomotivesWithController(PropertyChangeListener progressListener) {
    List<LocomotiveBean> fromController = this.controllerService.getLocomotives();

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
    this.controllerService.cacheAllFunctionIcons(progressListener);
  }

  @Override
  public void synchronizeTurnouts() {
    List<AccessoryBean> csTurnouts = this.controllerService.getSwitches();

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
  public void synchronizeSignals() {
    List<AccessoryBean> csSignals = this.controllerService.getSignals();

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
  public void updateGuiStatuses() {
    //updateAccessoryStatuses();
  }

  @Override
  public void changeDirection(Direction newDirection, LocomotiveBean locomotive) {
    Logger.debug("Changing direction to " + newDirection + " for: " + locomotive.toLogString());
    Integer address = locomotive.getAddress();
    DecoderType decoderType = locomotive.getDecoderType();

    //Issue a halt or stop for the loc
    controllerService.changeVelocity(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), 0);
    controllerService.changeDirection(address, decoderType, newDirection);
  }

  @Override
  public void changeVelocity(Integer newVelocity, LocomotiveBean locomotive) {
    Logger.trace("Changing velocity to " + newVelocity + " for " + locomotive.getName());
    controllerService.changeVelocity(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), newVelocity);
  }

  @Override
  public void changeFunction(Boolean newValue, Integer functionNumber, LocomotiveBean locomotive) {
    Logger.trace("Changing Function " + functionNumber + " to " + (newValue ? "on" : "off") + " on " + locomotive.getName());
    controllerService.changeFunctionValue(locomotive.getAddress(), DecoderType.get(locomotive.getDecoderTypeString()), functionNumber, newValue);
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
    controllerService.switchAccessory(address, val);
  }

  private void sendPingConfirmation() {

  }

  @Override
  public void addMessageListener(CanMessageListener listener) {
    if (this.controllerService != null) {
      //this.controllerService.addCanMessageListener(listener);
    }
  }

  @Override
  public void removeMessageListener(CanMessageListener listener) {
    if (this.controllerService != null) {
      //this.controllerService.removeCanMessageListener(listener);
    }
  }

  @Override
  public void addSensorListener(SensorListener listener) {
    this.sensorListeners.add(listener);
  }

  @Override
  public void removeSensorListener(SensorListener listener) {
    this.sensorListeners.remove(listener);
  }

  @Override
  public void addAccessoryListener(AccessoryListener listener) {
    this.accessoryListeners.add(listener);
  }

  @Override
  public void removeAccessoryListener(AccessoryListener listener) {
    this.accessoryListeners.remove(listener);
  }

  @Override
  public void addFunctionListener(FunctionListener listener) {
    this.functionListeners.add(listener);
  }

  @Override
  public void removeFunctionListener(FunctionListener listener) {
    this.functionListeners.remove(listener);
  }

  @Override
  public void addDirectionListener(DirectionListener listener) {
    this.directionListeners.add(listener);
  }

  @Override
  public void removeDirectionListener(DirectionListener listener) {
    this.directionListeners.remove(listener);
  }

  @Override
  public void addVelocityListener(VelocityListener listener) {
    this.velocityListeners.add(listener);
  }

  @Override
  public void removeVelocityListener(VelocityListener listener) {
    this.velocityListeners.remove(listener);
  }

  private class SensorMessageEventListener implements SensorMessageListener {

    private final TrackControllerImpl trackController;

    SensorMessageEventListener(TrackControllerImpl trackController) {
      this.trackController = trackController;
    }

    @Override
    public void onSensorMessage(SensorMessageEvent event) {
      SensorBean sb = event.getSensorBean();
      SensorBean dbsb = PersistenceFactory.getService().getSensor(sb.getDeviceId(), sb.getContactId());

      if (dbsb != null) {
        sb.setId(dbsb.getId());
        sb.setName(dbsb.getName());
        PersistenceFactory.getService().persist(sb);
      }

      for (SensorListener sl : trackController.sensorListeners) {
        sl.onChange(sb);
      }
    }
  }

  private class AccessoryMessageListener implements AccessoryMessageEventListener {

    private final TrackControllerImpl trackService;

    AccessoryMessageListener(TrackControllerImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onAccessoryMessage(AccessoryMessageEvent event) {
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

        for (AccessoryListener al : this.trackService.accessoryListeners) {
          al.onChange(event);
        }
      }
    }
  }

  private class FunctionMessageListener implements FunctionMessageEventListener {

    private final TrackControllerImpl trackService;

    FunctionMessageListener(TrackControllerImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onFunctionMessage(FunctionMessageEvent functionEvent) {
      LocomotiveBean lb = functionEvent.getLocomotiveBean();
      LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(lb.getId());

      if (dblb != null) {
        FunctionBean fb = lb.getFunctionBean(functionEvent.getUpdatedFunctionNumber());
        if (fb != null) {
          dblb.setFunctionValue(fb.getNumber(), fb.getValue());
          PersistenceFactory.getService().persist(dblb);
          functionEvent.setLocomotiveBean(dblb);
        }
        for (FunctionListener fl : trackService.functionListeners) {
          fl.onFunctionChange(functionEvent);
        }
      }
    }
  }

  private class DirectionMessageListener implements DirectionMessageEventListener {

    private final TrackControllerImpl trackService;

    DirectionMessageListener(TrackControllerImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onDirectionMessage(DirectionMessageEvent directionEvent) {
      LocomotiveBean lb = directionEvent.getLocomotiveBean();
      LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(lb.getId());

      if (dblb != null) {
        Integer richtung = lb.getRichtung();
        dblb.setRichtung(richtung);
        PersistenceFactory.getService().persist(dblb);
        directionEvent.setLocomotiveBean(dblb);

        for (DirectionListener dl : this.trackService.directionListeners) {
          dl.onDirectionChange(directionEvent);
        }
      }
    }

  }

  private class VelocityMessageListener implements VelocityMessageEventListener {

    private final TrackControllerImpl trackService;

    VelocityMessageListener(TrackControllerImpl trackService) {
      this.trackService = trackService;
    }

    @Override
    public void onVelocityMessage(VelocityMessageEvent velocityEvent) {
      LocomotiveBean lb = velocityEvent.getLocomotiveBean();
      if (lb != null && lb.getId() != null) {
        LocomotiveBean dblb = PersistenceFactory.getService().getLocomotive(lb.getId());

        if (dblb != null) {
          Integer velocity = lb.getVelocity();
          dblb.setVelocity(velocity);
          PersistenceFactory.getService().persist(dblb);

          velocityEvent.setLocomotiveBean(dblb);

          for (VelocityListener dl : this.trackService.velocityListeners) {
            dl.onVelocityChange(velocityEvent);
          }
        }
      }
    }
  }
}
