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
package jcs.controller.cs3.can.parser;

import java.io.Serializable;
import java.util.Arrays;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.MarklinCan;
import jcs.util.ByteUtil;

/**
 *
 * @author Frans Jacobs
 */
public class PingResponseParser implements Serializable {

    private int[] swVersion;
    private int[] deviceId;
    private int[] senderDeviceUid;

    public PingResponseParser(CanMessage message) {
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
        return "Device { " + "senderDeviceUid: " + ByteUtil.toHexString(senderDeviceUid) + " SW version: " + ByteUtil.toHexString(swVersion) + " deviceId: " + ByteUtil.toHexString(deviceId) + " }";
    }

    public int[] getSwVersionBytes() {
        return swVersion;
    }

    public int getSwVersion() {
        return ByteUtil.toInt(swVersion);
    }

    public int[] getDeviceIdBytes() {
        return deviceId;
    }

    public int getDeviceId() {
        return ByteUtil.toInt(deviceId);
    }

    public int[] getSenderDeviceUidBytes() {
        return senderDeviceUid;
    }

    public int getSenderDeviceUid() {
        return ByteUtil.toInt(senderDeviceUid);
    }

}
