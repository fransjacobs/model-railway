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
package lan.wervel.jcs.controller;

import lan.wervel.jcs.controller.cs2.DeviceInfo;
import lan.wervel.jcs.controller.cs2.PowerStatus;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.Direction;
import lan.wervel.jcs.entities.enums.DecoderType;

public interface ControllerService {

    static final String SERVICE_TYPE = "ControllerService";

    PowerStatus powerOff();

    PowerStatus powerOn();

    boolean isPowerOn();

    boolean connect();

    boolean isConnected();

    void disconnect();

    String getName();

    void toggleDirection(int address, DecoderType protocol, boolean function);

    void toggleDirection(int address, DecoderType protocol);

    void setDirection(int address, DecoderType protocol, Direction direction);

    void setSpeedAndFunction(int address, DecoderType protocol, boolean function, int speed);

    void setSpeed(int address, DecoderType protocol, int speed);

    void setFunctions(int address, DecoderType protocol, boolean f1, boolean f2, boolean f3, boolean f4);

    void setFunction(int address, DecoderType protocol, int functionNumber, boolean value);

    void switchAccessoiry(int address, AccessoryValue value);

    int[] getFeedback(int moduleNumber);

    void addControllerEventListener(ControllerEventListener listener);

    void removeControllerEventListener(ControllerEventListener listener);

    void notifyAllControllerEventListeners();

    DeviceInfo getControllerInfo();

    void addCanMessageListener(CanMessageListener listener);

    void removeCanMessageListener(CanMessageListener listener);

}
