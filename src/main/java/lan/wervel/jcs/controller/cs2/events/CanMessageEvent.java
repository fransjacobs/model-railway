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
package lan.wervel.jcs.controller.cs2.events;

import java.io.Serializable;
import java.net.InetAddress;
import lan.wervel.jcs.controller.cs2.can.CanMessage;

public class CanMessageEvent implements Serializable {

    private final CanMessage message;
    private final InetAddress sourceAddress;

    public CanMessageEvent(CanMessage message, InetAddress sourceAddress) {
        this.message = message;
        this.sourceAddress = sourceAddress;
    }

    public CanMessage getCanMessage() {
        return message;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CanMessageEvent{message: ").append(message);
        if(sourceAddress != null) {
          sb.append(", from: ").append(sourceAddress.getHostAddress());
        }
        sb.append('}');
        return sb.toString();
    }

}
