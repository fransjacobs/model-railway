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
import java.util.List;
import java.util.Map;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
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
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.entities.DeviceBean;
import jcs.entities.FeedbackModuleBean;
import jcs.entities.InfoBean;
import jcs.entities.LocomotiveBean;
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

  public VirtualCommandStationImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public VirtualCommandStationImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);

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
  public void fireSensorEventListeners(final SensorEvent sensorEvent) {
    for (SensorEventListener listener : sensorEventListeners) {
      listener.onSensorChange(sensorEvent);
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

}
