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
package jcs.controller.cs2.events;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import jcs.controller.cs2.can.CanMessage;

public class CanMessageEvent implements Serializable {

    private final CanMessage canMessage;
    private final InetAddress sourceAddress;
    private final Date eventDate;

    public CanMessageEvent(CanMessage canMessage, InetAddress sourceAddress) {
        this(canMessage, sourceAddress, new Date());
    }

    public CanMessageEvent(CanMessage canMessage, InetAddress sourceAddress, Date eventDate) {
        this.canMessage = canMessage;
        this.sourceAddress = sourceAddress;
        this.eventDate = eventDate;
    }

    public CanMessage getCanMessage() {
        return canMessage;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    public Date getEventDate() {
        return eventDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CanMessageEvent{message: ").append(canMessage);
        if (sourceAddress != null) {
            sb.append(", from: ").append(sourceAddress.getHostAddress());
        }
        sb.append('}');
        return sb.toString();
    }

}
