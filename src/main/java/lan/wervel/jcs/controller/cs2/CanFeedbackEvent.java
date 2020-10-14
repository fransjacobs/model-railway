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
