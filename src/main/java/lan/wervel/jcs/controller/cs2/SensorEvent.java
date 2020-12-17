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

import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.MarklinCan;
import org.pmw.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class SensorEvent {

    private boolean newValue;
    private boolean oldValue;
    private int contactId;
    private int deviceId;
    private int millis;

    public SensorEvent(CanMessage message) {
        parseMessage(message);
    }

    private void parseMessage(CanMessage message) {
        CanMessage resp;

        if (!message.isResponseMessage()) {
            resp = message.getResponse();
        } else {
            resp = message;
        }

        if (resp.isResponseMessage() && MarklinCan.S88_EVENT_RESPONSE == resp.getCommand()) {
            int[] data = resp.getData();

            int[] did = new int[2];
            int[] cid = new int[2];
            int[] time = new int[2];

            System.arraycopy(data, 0, did, 0, did.length);
            System.arraycopy(data, 2, cid, 0, cid.length);
            int ov = data[4];
            int nv = data[5];
            System.arraycopy(data, 6, time, 0, time.length);

            deviceId = CanMessage.toInt(did);
            contactId = CanMessage.toInt(cid);
            oldValue = ov == 1;
            newValue = nv == 1;
            millis = CanMessage.toInt(time) * 10;
        } else {
            Logger.warn("Can't parse message, not a Sensor Response! "+resp);
        }
    }

    public boolean isNewValue() {
        return newValue;
    }

    public boolean isOldValue() {
        return oldValue;
    }

    public int getContactId() {
        return contactId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getMillis() {
        return millis;
    }

    @Override
    public String toString() {
        return "FeedbackEventStatus{" + "newValue=" + newValue + ", oldValue=" + oldValue + ", contactId=" + contactId + ", deviceId=" + deviceId + ", millis=" + millis + '}';
    }
}
