/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.loconet;

import java.awt.Image;
import java.util.List;
import java.util.concurrent.Executors;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.entities.Device;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;

/**
 * Uhlenbrock IntelliBox 2 implementation
 */
public class IntelliboxImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController, ConnectionEventListener {

  public IntelliboxImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public IntelliboxImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    this.executor = Executors.newCachedThreadPool();
  }

  @Override
  public boolean connect() {
    return false;
  }

  @Override
  public void disconnect() {

  }

  @Override
  public InfoBean getCommandStationInfo() {
    return null;
  }

  @Override
  public List<Device> getDevices() {
    return null;
  }

  @Override
  public String getIp() {
    return null;
  }

  @Override
  public boolean power(boolean on) {
    return false;
  }

  @Override
  public void changeDirection(int locUid, LocomotiveBean.Direction direction) {
  }

  @Override
  public void changeVelocity(int locUid, int speed, LocomotiveBean.Direction direction) {
  }

  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    return null;
  }

  @Override
  public Image getLocomotiveImage(String icon) {
    return null;
  }

  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    return null;
  }

  @Override
  public boolean isSupportTrackMeasurements() {
    return false;
  }

  @Override
  public void switchAccessory(Integer address, String protocol, AccessoryBean.AccessoryValue value, Integer switchTime) {
  }

  @Override
  public List<AccessoryBean> getAccessories() {
    return null;
  }

  @Override
  public void fireAllSensorEventsListeners(SensorEvent sensorEvent) {
  }

  @Override
  public List<FeedbackModule> getFeedbackModules() {
    return null;
  }

  @Override
  public SensorBean getSensorStatus(SensorBean sensorBean) {
    return null;
  }

  @Override
  public void simulateSensor(SensorEvent sensorEvent) {
  }

  @Override
  public void onConnectionChange(ConnectionEvent event) {
  }

}
