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
package jcs.controller.cs3.events;

import java.util.Date;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.MarklinCan;
import jcs.entities.SensorBean;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class SensorMessageEvent {

    private SensorBean sensor;

    public SensorMessageEvent(CanMessage message, Date eventDate) {
        parseMessage(message, eventDate);
    }

    private void parseMessage(CanMessage message, Date eventDate) {
        CanMessage resp;
        if (!message.isResponseMessage()) {
            resp = message.getResponse();
        } else {
            resp = message;
        }

        if (resp.isResponseMessage() && MarklinCan.S88_EVENT_RESPONSE == resp.getCommand()) {
            int[] data = resp.getData();

            Integer deviceId = ByteUtil.toInt(new int[]{data[0], data[1]});
            Integer contactId = ByteUtil.toInt(new int[]{data[2], data[3]});

            Integer previousStatus = data[4];
            Integer status = data[5];

            Integer millis = ByteUtil.toInt(new int[]{data[6], data[7]}) * 10;

            this.sensor = new SensorBean(deviceId, contactId, status, previousStatus, millis, eventDate);
        } else {
            Logger.warn("Can't parse message, not a Sensor Response! " + resp);
        }
    }

    public SensorBean getSensorBean() {
        return sensor;
    }

}
