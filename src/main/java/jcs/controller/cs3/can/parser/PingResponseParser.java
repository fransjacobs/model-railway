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
package jcs.controller.cs3.can.parser;

import java.io.Serializable;
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
