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

import java.net.InetAddress;
import jcs.controller.cs2.can.CanMessage;
import jcs.controller.cs2.can.MarklinCan;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public abstract class DiscoveryResponseListener implements CanMessageListener {

    @Override
    public void onCanMessage(CanMessageEvent canEvent) {
        CanMessage can = canEvent.getCanMessage();
        int command = can.getCommand();

        if (command == MarklinCan.SW_STATUS_REQ) {
            Logger.trace("Response Command: " + Integer.toHexString(command));
            //got response
            setHost(canEvent.getSourceAddress());

            //onResponse();
        }
    }

    public abstract void setHost(InetAddress hostAddress);

}
