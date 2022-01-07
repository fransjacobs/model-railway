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
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.MarklinCan;

/**
 *
 * @author Frans Jacobs
 */
public class PingResponse implements Serializable {

    private int[] swVersion;
    private int[] deviceId;
    private int[] senderDeviceUid;

    public PingResponse(CanMessage message) {
        parseMessage(message);
    }

    private void parseMessage(CanMessage response) {
        if (response.isResponseMessage() && MarklinCan.REQ_PING == response.getCommand()) {
            int[] data = response.getData();
            swVersion = new int[2];
            deviceId = new int[2];
            senderDeviceUid = new int[4];

            System.arraycopy(data, 0, senderDeviceUid, 0, senderDeviceUid.length);
            System.arraycopy(data, 4, swVersion, 0, swVersion.length);
            System.arraycopy(data, 6, deviceId, 0, deviceId.length);
        }
    }

    @Override
    public String toString() {
        return "PingResponse{" + "senderDeviceUid: " + CanMessage.toHexString(senderDeviceUid) + " SW version: " + CanMessage.toHexString(swVersion) + " deviceId: " + CanMessage.toHexString(deviceId) + '}';
    }

    public int[] getSwVersion() {
        return swVersion;
    }

    public int getSwVersionInt() {
        return CanMessage.toInt(swVersion);
    }

    public int[] getDeviceId() {
        return deviceId;
    }

    public int getDeviceIdint() {
        return CanMessage.toInt(deviceId);
    }

    public int[] getSenderDeviceUid() {
        return senderDeviceUid;
    }

    public int getSenderDeviceUidInt() {
        return CanMessage.toInt(senderDeviceUid);
    }

}
