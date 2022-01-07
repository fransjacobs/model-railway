/*
 * Copyright (C) 2020 Frans Jacobs <frans.jacobs@gmail.com>.
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
package jcs.controller.cs3.net;

import java.net.InetAddress;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.events.CanMessageListener;

/**
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
public interface Connection extends AutoCloseable {

    static final int MAX_ERRORS = 15;

    static final int CS2_TX_PORT = 15730;

    static final int CS2_RX_PORT = 15731;

    CanMessage sendCanMessage(CanMessage message);

    void addCanMessageListener(CanMessageListener listener);

    void removeCanMessageListener(CanMessageListener listener);

    InetAddress getCs2Address();

}
