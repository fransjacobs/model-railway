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
package jcs.controller.cs;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;
import jcs.entities.enums.DecoderType;
import jcs.controller.events.AccessoryEventListener;
import jcs.controller.events.LocomotiveDirectionEventListener;
import jcs.controller.events.LocomotiveFunctionEventListener;
import jcs.controller.events.LocomotiveSpeedEventListener;
import jcs.controller.events.PowerEventListener;
import jcs.controller.events.SensorEventListener;

public interface MarklinCentralStation {

  boolean connect();

  boolean isConnected();

  void disconnect();

  boolean isPower();

  boolean power(boolean on);

  void changeDirection(int address, DecoderType protocol, Direction direction);

  void changeVelocity(int address, DecoderType protocol, int speed);

  void changeFunctionValue(int address, DecoderType protocol, int functionNumber, boolean flag);

  void switchAccessory(int address, AccessoryValue value);

  void addPowerEventListener(PowerEventListener listener);

  void removePowerEventListener(PowerEventListener listener);

  void addSensorEventListener(SensorEventListener listener);

  void removeSensorEventListener(SensorEventListener listener);

  void addAccessoryEventListener(AccessoryEventListener listener);

  void removeAccessoryEventListener(AccessoryEventListener listener);

  void addLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener);

  void removeLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener);

  void addLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener);

  void removeLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener);

  void addLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener);

  void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener);

  List<LocomotiveBean> getLocomotives();

  void cacheAllFunctionIcons(PropertyChangeListener progressListener);

  Image getLocomotiveImage(String icon);

  //List<AccessoryBean> getAccessories();
  List<AccessoryBean> getSwitches();

  List<AccessoryBean> getSignals();

  Device getDevice();

  List<Device> getDevices();

}
