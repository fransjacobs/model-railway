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
package lan.wervel.jcs.controller.cs2;

import java.io.Serializable;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.MarklinCan;

/**
 *
 * @author Frans Jacobs
 */
public class PowerStatus implements Serializable {

    private boolean powerOn;
    private int[] deviceUid;
    private int deviceUidNumber;

    public PowerStatus(boolean powerOn, int[] deviceUid, int deviceUidNumber) {
        this.powerOn = powerOn;
        this.deviceUid = deviceUid;
        this.deviceUidNumber = deviceUidNumber;
    }

    public PowerStatus(CanMessage message) {
        parseMessage(message);
    }

    private void parseMessage(CanMessage message) {
        CanMessage response = message.getResponse();
        if (response.isResponseMessage() && MarklinCan.SYSTEM_COMMAND_RESPONSE == response.getCommand() && response.isDeviceUidValid()) {
            int[] data = response.getData();
            deviceUid = response.getDeviceUidFromMessage();
            deviceUidNumber = response.getDeviceUidNumberFromMessage();
            int status = data[4];
            powerOn = status == 1;
        }
    }

    @Override
    public String toString() {
        return "PowerStatus{" + "power=" + (powerOn ? "On" : "Off") + '}';
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public int[] getDeviceUid() {
        return deviceUid;
    }

    public int getDeviceUidNumber() {
        return deviceUidNumber;
    }

}
