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
import java.beans.PropertyChangeListener;
import java.util.List;
import jcs.controller.cs3.devices.GFP;
import jcs.controller.cs3.devices.LinkSxx;
import jcs.controller.cs3.events.PowerEventListener;
import jcs.controller.cs3.events.SensorMessageListener;
import jcs.entities.AccessoryBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;
import jcs.entities.enums.DecoderType;
import jcs.controller.cs3.events.AccessoryMessageEventListener;
import jcs.controller.cs3.events.DirectionMessageEventListener;
import jcs.controller.cs3.events.FunctionMessageEventListener;
import jcs.controller.cs3.events.VelocityMessageEventListener;

public interface MarklinController {

    boolean connect();

    boolean isConnected();

    void disconnect();

    String getName();

    String getSerialNumber();

    String getArticleNumber();

    boolean isPower();

    boolean power(boolean on);

    void changeDirection(int address, DecoderType protocol, Direction direction);

    void changeVelocity(int address, DecoderType protocol, int speed);

    void changeFunctionValue(int address, DecoderType protocol, int functionNumber, boolean flag);

    void switchAccessory(int address, AccessoryValue value);

    void addPowerEventListener(PowerEventListener listener);

    void removePowerEventListener(PowerEventListener listener);

    void addSensorMessageListener(SensorMessageListener listener);

    void removeSensorMessageListener(SensorMessageListener listener);

    void addAccessoryEventListener(AccessoryMessageEventListener listener);

    void removeAccessoryEventListener(AccessoryMessageEventListener listener);

    void addFunctionMessageEventListener(FunctionMessageEventListener listener);

    void removeFunctionMessageEventListener(FunctionMessageEventListener listener);

    void addDirectionMessageEventListener(DirectionMessageEventListener listener);

    void removeDirectionMessageEventListener(DirectionMessageEventListener listener);

    void addVelocityMessageEventListener(VelocityMessageEventListener listener);

    void removeVelocityMessageEventListener(VelocityMessageEventListener listener);

    List<LocomotiveBean> getLocomotives();

    void cacheAllFunctionIcons(PropertyChangeListener progressListener);

    Image getLocomotiveImage(String icon);

    //List<AccessoryBean> getAccessories();
    List<AccessoryBean> getSwitches();

    List<AccessoryBean> getSignals();

    GFP getGFP();

    LinkSxx getLinkSxx();

}
