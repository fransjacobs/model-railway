/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.controller;

import java.awt.Image;
import java.util.List;
import jcs.controller.cs3.events.SensorMessageListener;
import jcs.entities.AccessoryBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;
import jcs.entities.enums.DecoderType;

public interface MarklinController {

    boolean connect();

    boolean isConnected();

    void disconnect();

    String getName();

    String getSerialNumber();

    String getArticleNumber();

    boolean isPower();

    boolean power(boolean on);

    //void toggleDirection(int address, DecoderType protocol, boolean function);
    //void toggleDirection(int address, DecoderType protocol);
    void changeDirection(int address, DecoderType protocol, Direction direction);

    void setSpeed(int address, DecoderType protocol, int speed);

    void setFunction(int address, DecoderType protocol, int functionNumber, boolean flag);

    void switchAccessory(int address, AccessoryValue value);

    //void addControllerEventListener(ControllerEventListener listener);
    //void removeControllerEventListener(ControllerEventListener listener);
    //void notifyAllControllerEventListeners();
    //StatusDataConfigParser getControllerInfo();
    //void addCanMessageListener(CanMessageListener listener);
    //void removeCanMessageListener(CanMessageListener listener);
    void addSensorMessageListener(SensorMessageListener listener);

    void removeSensorMessageListener(SensorMessageListener listener);

    //void addHeartbeatListener(HeartbeatListener listener);
    //void removeHeartbeatListener(HeartbeatListener listener);
    //void removeAllHeartbeatListeners();
    List<LocomotiveBean> getLocomotives();

    void cacheAllFunctionIcons();

    Image getLocomotiveImage(String icon);

    List<AccessoryBean> getAccessories();

    //List<SensorMessageEvent> querySensors(int sensorCount);
    //List<AccessoryStatus> getAccessoryStatuses();
}
