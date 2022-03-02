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
import java.util.List;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.MarklinCan;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class SystemStatusParser implements Serializable {

    private boolean power;
    private int[] gfpUid;

    public SystemStatusParser(CanMessage message) {
        parseMessage(message);
    }

    //There might be more than 1 responses.
    //when there are more use the one which contains a valid gfp uid
    private void parseMessage(CanMessage message) {
        if (message != null) {
            CanMessage resp = null;
            List<CanMessage> respList = message.getResponses();
            if (respList.isEmpty()) {
                Logger.warn("No response for: " + message);
                gfpUid = message.getDeviceUidFromMessage();
                int status = message.getData()[4];
                power = status == 1;
            } else {
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
                    gfpUid = resp.getDeviceUidFromMessage();
                    int status = data[4];
                    power = status == 1;
                }
            }
        } else {
            power = false;
            gfpUid = new int[]{0, 0, 0, 0};
        }
    }

    @Override
    public String toString() {
        return "SystemStatus{" + " power: " + (power ? "On" : "Off") + " GFP UID: " + ByteUtil.toHexString(gfpUid) + " }";
    }

    public boolean isPower() {
        return power;
    }

    public int[] getGfpUid() {
        return gfpUid;
    }
}
