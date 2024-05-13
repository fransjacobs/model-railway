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
import java.util.List;
import java.util.Map;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.PowerEventListener;
import jcs.entities.ChannelBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;

public interface DecoderController extends GenericController {

  boolean isPower();

  boolean power(boolean on);

  void changeDirection(int locUid, Direction direction);

  void changeVelocity(int locUid, int speed, Direction direction);

  void changeFunctionValue(int locUid, int functionNumber, boolean flag);

  void addPowerEventListener(PowerEventListener listener);

  void removePowerEventListener(PowerEventListener listener);

  void addLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener);

  void removeLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener);

  void addLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener);

  void removeLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener);

  void addLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener);

  void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener);

  //List<LocomotiveSpeedEventListener> getLocomotiveSpeedEventListeners();

  List<LocomotiveBean> getLocomotives();

  Image getLocomotiveImage(String icon);

  Image getLocomotiveFunctionImage(String icon);

  boolean isSupportTrackMeasurements();

  Map<Integer, ChannelBean> getTrackMeasurements();
}
