/*
 * Copyright 2023 frans.
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
package jcs.controller.dccex;

import java.awt.Image;
import java.util.List;
import java.util.Map;
import jcs.controller.CommandStation;
import jcs.controller.events.AccessoryEventListener;
import jcs.controller.events.LocomotiveDirectionEventListener;
import jcs.controller.events.LocomotiveFunctionEventListener;
import jcs.controller.events.LocomotiveSpeedEventListener;
import jcs.controller.events.PowerEventListener;
import jcs.controller.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.Device;
import jcs.entities.LocomotiveBean;
import jcs.entities.MeasurementChannel;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;

/**
 *
 * @author frans
 */
public class DccExSerialImpl implements CommandStation {

  @Override
  public boolean connect() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isConnected() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void disconnect() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isPower() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean power(boolean on) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changeDirection(int locUid, Direction direction) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changeVelocity(int locUid, int speed) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchAccessory(int address, AccessoryValue value) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchAccessory(int address, AccessoryValue value, int switchTime) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addPowerEventListener(PowerEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removePowerEventListener(PowerEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addSensorEventListener(SensorEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeSensorEventListener(SensorEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addAccessoryEventListener(AccessoryEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeAccessoryEventListener(AccessoryEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
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
  public List<AccessoryBean> getSwitches() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<AccessoryBean> getSignals() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Device getDevice() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Device> getDevices() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void clearCaches() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Map<Integer, MeasurementChannel> getTrackMeasurements() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
