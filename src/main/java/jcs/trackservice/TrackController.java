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

import java.beans.PropertyChangeListener;
import jcs.controller.cs3.devices.LinkSxx;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.controller.cs3.events.PowerEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;
import jcs.trackservice.events.AccessoryListener;
import jcs.trackservice.events.DirectionListener;
import jcs.trackservice.events.FunctionListener;
import jcs.trackservice.events.SensorListener;
import jcs.trackservice.events.VelocityListener;

/**
 * The Track repository contain all track item which are used on the Track This can be Locomotives, Turnouts, Signals, etc There For
 * future use the implementation of the Repository could be changed to an other storage provider
 *
 * @author frans
 */
public interface TrackController {

  void switchPower(boolean on);

  boolean isPowerOn();

  boolean connect();

  boolean isConnected();

  void disconnect();

  void addPowerEventListener(PowerEventListener listener);

  void removePowerEventListener(PowerEventListener listener);

  //Image getFunctionImage(String imageName);
  void changeDirection(Direction direction, LocomotiveBean locomotive);

  void changeVelocity(Integer speed, LocomotiveBean locomotive);

  void changeFunction(Boolean value, Integer functionNumber, LocomotiveBean locomotive);

  void switchAccessory(AccessoryValue value, AccessoryBean accessory);

  void addAccessoryListener(AccessoryListener listener);

  void removeAccessoryListener(AccessoryListener listener);

  void addSensorListener(SensorListener listener);

  void removeSensorListener(SensorListener listener);

  void addFunctionListener(FunctionListener listener);

  void removeFunctionListener(FunctionListener listener);

  void addDirectionListener(DirectionListener listener);

  void removeDirectionListener(DirectionListener listener);

  void addVelocityListener(VelocityListener listener);

  public void removeVelocityListener(VelocityListener listener);

  String getControllerName();

  String getControllerSerialNumber();

  String getControllerArticleNumber();

  LinkSxx getLinkSxx();

  void addMessageListener(CanMessageListener listener);

  void removeMessageListener(CanMessageListener listener);

  void synchronizeLocomotivesWithController(PropertyChangeListener progressListener);

  void synchronizeTurnouts();

  void synchronizeSignals();

  void updateGuiStatuses();
}
