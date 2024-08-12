/*
 * Copyright 2024 frans.
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
package jcs.commandStation.virtual;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.BlockBean;
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.entities.DeviceBean;
import jcs.entities.FeedbackModuleBean;
import jcs.entities.InfoBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.util.NetworkUtil;
import jcs.util.VersionInfo;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class VirtualCommandStationImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController {
  
  private DeviceBean mainDevice;
  private DeviceBean feedbackDevice;
  private InfoBean infoBean;
  
  private final ScheduledExecutorService scheduledExecutor;
  
  public VirtualCommandStationImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }
  
  public VirtualCommandStationImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    scheduledExecutor = new ScheduledThreadPoolExecutor(10);
    
    if (autoConnect) {
      autoConnect();
    }
  }
  
  private void autoConnect() {
    connect();
  }
  
  @Override
  public boolean connect() {
    this.connected = true;
    
    mainDevice = new DeviceBean();
    mainDevice.setArticleNumber("JCS Virtual CS");
    mainDevice.setVersion(VersionInfo.getVersion());
    
    mainDevice.setSerial("1");
    mainDevice.setIdentifier(this.commandStationBean.getId());
    mainDevice.setName(this.commandStationBean.getDescription());
    
    infoBean = new InfoBean();
    infoBean.setProductName(commandStationBean.getDescription());
    infoBean.setArticleNumber(commandStationBean.getShortName());
    infoBean.setHostname(this.getIp());
    
    power(true);
    
    return connected;
  }
  
  @Override
  public void disconnect() {
    this.connected = false;
    this.infoBean = null;
  }
  
  @Override
  public InfoBean getCommandStationInfo() {
    return this.infoBean;
  }
  
  @Override
  public DeviceBean getDevice() {
    return this.mainDevice;
  }
  
  @Override
  public List<DeviceBean> getDevices() {
    List<DeviceBean> devices = new ArrayList<>();
    devices.add(this.mainDevice);
    return devices;
  }
  
  @Override
  public String getIp() {
    return NetworkUtil.getIPv4HostAddress().getHostAddress();
  }
  
  @Override
  public boolean power(boolean on) {
    if (this.connected) {
      power = on;
      PowerEvent pe = new PowerEvent(this.power);
      notifyPowerEventListeners(pe);
      return power;
    } else {
      return false;
    }
  }
  
  @Override
  public void changeDirection(int locUid, LocomotiveBean.Direction direction) {
    if (this.power && this.connected) {
      Logger.debug("locUid " + locUid + " direction " + direction);
      LocomotiveDirectionEvent lde = new LocomotiveDirectionEvent(locUid, direction, commandStationBean.getId());
      notifyLocomotiveDirectionEventListeners(lde);
    }
  }
  
  @Override
  public void changeVelocity(int locUid, int speed, LocomotiveBean.Direction direction) {
    if (this.power && connected) {
      Logger.debug("locUid " + locUid + " speed " + speed);
      
      LocomotiveSpeedEvent lse = new LocomotiveSpeedEvent(locUid, speed, commandStationBean.getId());
      notifyLocomotiveSpeedEventListeners(lse);
    }

    //When a locomotive has a speed change (>0) check if Auto mode is on.
    //When in Auto mode try to simulate the first sensor the locomotive is suppose to hit.
    if (AutoPilot.getInstance().isAutoModeActive() && speed > 0) {
      simulateDriving(locUid, speed, direction);
    }
    
  }
  
  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
    if (this.power && connected) {
      Logger.debug("locUid " + locUid + " functionNumber " + functionNumber + " " + (flag ? "on" : "off"));
      LocomotiveFunctionEvent lfe = new LocomotiveFunctionEvent(locUid, functionNumber, flag, commandStationBean.getId());
      notifyLocomotiveFunctionEventListeners(lfe);
    }
  }
  
  @Override
  public List<LocomotiveBean> getLocomotives() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public Image getLocomotiveImage(String icon) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public boolean isSupportTrackMeasurements() {
    return false;
  }
  
  @Override
  public Map<Integer, ChannelBean> getTrackMeasurements() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public void switchAccessory(Integer address, AccessoryBean.AccessoryValue value) {
    switchAccessory(address, value, 200);
  }
  
  @Override
  public void switchAccessory(Integer address, AccessoryBean.AccessoryValue value, Integer switchTime) {
    AccessoryBean ab = new AccessoryBean();
    ab.setAddress(address);
    ab.setAccessoryValue(value);
    String id = address + "";
    if (id.length() == 1) {
      id = "00" + id;
    } else if (id.length() == 2) {
      id = "0" + id;
    }
    ab.setId(id);
    ab.setCommandStationId(commandStationBean.getId());
    
    AccessoryEvent ae = new AccessoryEvent(ab);
    notifyAccessoryEventListeners(ae);
  }
  
  @Override
  public List<AccessoryBean> getAccessories() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public DeviceBean getFeedbackDevice() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public List<FeedbackModuleBean> getFeedbackModules() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  private void notifyPowerEventListeners(final PowerEvent powerEvent) {
    this.power = powerEvent.isPower();
    executor.execute(() -> fireAllPowerEventListeners(powerEvent));
  }
  
  private void fireAllPowerEventListeners(final PowerEvent powerEvent) {
    this.power = powerEvent.isPower();
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }
  
  @Override
  public synchronized void fireSensorEventListeners(final SensorEvent sensorEvent) {
    for (SensorEventListener listener : sensorEventListeners) {
      if (listener != null) {
        listener.onSensorChange(sensorEvent);
      }
    }
  }

  //TODO: is a threaded varian needed?
  private void notifySensorEventListeners(final SensorEvent sensorEvent) {
    executor.execute(() -> fireSensorEventListeners(sensorEvent));
  }
  
  private void fireAllAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    for (AccessoryEventListener listener : this.accessoryEventListeners) {
      listener.onAccessoryChange(accessoryEvent);
    }
  }
  
  private void notifyAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    executor.execute(() -> fireAllAccessoryEventListeners(accessoryEvent));
  }
  
  private void fireAllFunctionEventListeners(final LocomotiveFunctionEvent functionEvent) {
    if (functionEvent.isValid()) {
      for (LocomotiveFunctionEventListener listener : this.locomotiveFunctionEventListeners) {
        listener.onFunctionChange(functionEvent);
      }
    }
  }
  
  private void notifyLocomotiveFunctionEventListeners(final LocomotiveFunctionEvent functionEvent) {
    executor.execute(() -> fireAllFunctionEventListeners(functionEvent));
  }
  
  private void fireAllDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    if (directionEvent.isValid()) {
      for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
        listener.onDirectionChange(directionEvent);
      }
    }
  }
  
  private void notifyLocomotiveDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    executor.execute(() -> fireAllDirectionEventListeners(directionEvent));
  }
  
  private void fireAllLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent speedEvent) {
    if (speedEvent.isValid()) {
      for (LocomotiveSpeedEventListener listener : this.locomotiveSpeedEventListeners) {
        listener.onSpeedChange(speedEvent);
      }
    }
  }
  
  private void notifyLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent locomotiveEvent) {
    executor.execute(() -> fireAllLocomotiveSpeedEventListeners(locomotiveEvent));
  }

  //Method for virtual driving
  private List<LocomotiveBean> getOnTrackLocomotives() {
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    //filter..
    List<BlockBean> occupiedBlocks = blocks.stream().filter(t -> t.getLocomotive() != null && t.getLocomotive().getId() != null).collect(Collectors.toList());

    //Logger.trace("There " + (occupiedBlocks.size() == 1 ? "is" : "are") + " " + occupiedBlocks.size() + " occupied block(s)");
    Set<LocomotiveBean> activeLocomotives = new HashSet<>();
    for (BlockBean occupiedBlock : occupiedBlocks) {
      LocomotiveBean dbl = PersistenceFactory.getService().getLocomotive(occupiedBlock.getLocomotiveId());
      if (dbl != null) {
        activeLocomotives.add(dbl);
      }
    }
    
    if (Logger.isDebugEnabled()) {
      Logger.trace("There are " + activeLocomotives.size() + " Locomotives on the track: ");
      for (LocomotiveBean loc : activeLocomotives) {
        Logger.trace(loc);
      }
    }
    return new ArrayList<>(activeLocomotives);
  }

  //Find the route the locomotive is doing....
  void simulateDriving(int locUid, int speed, LocomotiveBean.Direction direction) {
    //Check is the Dispatcher for the locomotive is running...
    Dispatcher dispatcher = AutoPilot.getInstance().getLocomotiveDispatcher(locUid);
    
    if (dispatcher.isLocomotiveAutomodeOn()) {
      Logger.trace("Try to simulate the next sensor of " + dispatcher.getName());
      
      String sensorId = dispatcher.getWaitingForSensorId();
      if (sensorId != null) {
        //Start a time which execute a worker thread which fires the sensor
        scheduledExecutor.schedule(() -> toggleSensor(sensorId), 5, TimeUnit.SECONDS);
      }
    }
    // All sensor become active.
    //But in real live they will be automatically toggled
    //as the loc passes the sensor...
    //TODO Fix the tiggel of the sensor to off also....
    
  }
  
  private void toggleSensor(String sensorId) {
    SensorBean sensor = PersistenceFactory.getService().getSensor(sensorId);
    sensor.toggle();
    sensor.setActive((sensor.getStatus() == 1));
    
    SensorEvent sensorEvent = new SensorEvent(sensor);
    Logger.trace("Fire Sensor " + sensorId);
    
    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
    for (FeedbackController fbc : acl) {
      fbc.fireSensorEventListeners(sensorEvent);
    }
  }
  
}
