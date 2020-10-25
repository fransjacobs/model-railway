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

/**
 *
 * @author Frans Jacobs
 */
public class CanFeedbackEvent {

    private boolean feedbackEvent;

    private int deviceId;
    private int contactId;
    private boolean previousValue;
    private boolean value;
    private int durationMillis;

    public CanFeedbackEvent(CanMessage feedbackEventMessage) {
        parseMessage(feedbackEventMessage);
    }

    private void parseMessage(CanMessage feedbackEventMessage) {
        int cmd = feedbackEventMessage.getCommand();
        feedbackEvent = feedbackEventMessage.isResponseMessage() && MarklinCan.S88_EVENT == (cmd - 1);

        if (feedbackEvent) {
            int[] data = feedbackEventMessage.getData();
            int[] deviceIdentifier = new int[]{data[0], data[1]};
            this.deviceId = CanMessage.toInt(deviceIdentifier);
            int[] portId = new int[]{data[2], data[3]};
            contactId = CanMessage.toInt(portId);
            int oldValue = data[4];
            previousValue = oldValue == 1;
            int newValue = data[5];
            value = newValue == 1;
            int[] time = new int[]{data[6], data[7]};
            durationMillis = CanMessage.toInt(time) * 10;
        }
    }


    //00 23 CB 12 08 00 00 00 01 00 00 00 00
    //00 23 CB 12 08 00 00 00 02 00 00 00 00
    //00 23 CB 12 08 00 00 00 03 00 00 00 00
    //00 23 CB 12 08 00 00 00 04 00 00 00 00
    //00 23 CB 12 08 00 00 00 05 00 00 00 00
    //00 23 CB 12 08 00 00 00 06 00 00 00 00
    //00 23 CB 12 08 00 00 00 07 00 00 00 00
    //00 23 CB 12 08 00 00 00 08 00 00 00 00
    //00 23 CB 12 08 00 00 00 09 00 00 00 00
    //00 23 CB 12 08 00 00 00 0A 00 00 00 00
    //00 23 CB 12 08 00 00 00 0B 00 00 00 00
    //00 23 CB 12 08 00 00 00 0C 00 00 00 00
    //00 23 CB 12 08 00 00 00 0D 00 00 00 00
    //00 23 CB 12 08 00 00 00 0E 01 01 00 00
    //00 23 CB 12 08 00 00 00 0F 00 00 00 00
    //
    //00 23 CB 12 08 00 00 00 10 00 00 00 00
    //00 23 CB 12 08 00 00 00 11 01 01 00 00
    //00 23 CB 12 08 00 00 00 12 01 01 00 00
    //00 23 CB 12 08 00 00 00 13 01 01 00 00
    //00 23 CB 12 08 00 00 00 14 00 00 00 00
    //00 23 CB 12 08 00 00 00 15 01 01 00 00
    //00 23 CB 12 08 00 00 00 16 01 01 00 00
    //00 23 CB 12 08 00 00 00 17 00 00 00 00
    //00 23 CB 12 08 00 00 00 18 00 00 00 00
    //00 23 CB 12 08 00 00 00 19 01 01 00 00
    //00 23 CB 12 08 00 00 00 1[...]
    public boolean isFeedbackEvent() {
        return feedbackEvent;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getContactId() {
        return contactId;
    }

    public boolean isPreviousValue() {
        return previousValue;
    }

    public boolean isValue() {
        return value;
    }

    public int getDurationMillis() {
        return durationMillis;
    }

    @Override
    public String toString() {
        return "FeedbackEventInfo{" + "feedbackEvent=" + feedbackEvent + ", deviceId=" + deviceId + ", contactId=" + contactId + ", value=" + value + ", duration=" + durationMillis + '}';
    }

}
