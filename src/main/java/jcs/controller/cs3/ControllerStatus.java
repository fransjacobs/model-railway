/*
 * Copyright (C) 2020 Frans Jacobs.
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
package jcs.controller.cs3;

import java.io.Serializable;
import java.util.List;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.MarklinCan;

/**
 *
 * @author Frans Jacobs
 */
public class ControllerStatus implements Serializable {

    private boolean connected = false;
    private boolean power;
    private int[] deviceUid;
    private int deviceUidNumber;

    public ControllerStatus(boolean connected, boolean power) {
        this(connected, power, new int[0], -1);
    }

    public ControllerStatus(boolean connected, boolean power, int[] deviceUid, int deviceUidNumber) {
        this.connected = connected;
        this.power = power;
        this.deviceUid = deviceUid;
        this.deviceUidNumber = deviceUidNumber;
    }

    public ControllerStatus(CanMessage message) {
        parseMessage(message);
    }

    private void parseMessage(CanMessage message) {
        if (message != null) {
            connected = true;
            CanMessage resp = null;
            List<CanMessage> respList = message.getResponses();
            for (CanMessage cm : respList) {
                if (cm.isResponseMessage() && cm.isDeviceUidValid()) {
                    resp = cm;
                }
            }
            if (resp == null) {
                resp = message;
            }

            if (MarklinCan.SYSTEM_COMMAND_RESPONSE == resp.getCommand() && resp.isDeviceUidValid()) {
                int[] data = resp.getData();
                deviceUid = resp.getDeviceUidFromMessage();
                deviceUidNumber = resp.getDeviceUidNumberFromMessage();
                int status = data[4];
                power = status == 1;
            }
        } else {
            power = false;
            deviceUid = new int[0];
            deviceUidNumber = -1;
        }
    }

    @Override
    public String toString() {
        return "ControllerStatus{" + "connected=" + (connected ? "Yes" : "No") + " power=" + (power ? "On" : "Off") + '}';
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isPower() {
        return power;
    }

    public int[] getDeviceUid() {
        return deviceUid;
    }

    public int getDeviceUidNumber() {
        return deviceUidNumber;
    }

}
