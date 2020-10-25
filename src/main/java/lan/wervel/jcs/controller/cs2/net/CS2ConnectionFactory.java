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
package lan.wervel.jcs.controller.cs2.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.CanMessageFactory;
import lan.wervel.jcs.controller.cs2.can.MarklinCan;
import org.pmw.tinylog.Logger;

/**
 * Try to connect with a CS2/3. A "ping" is send to the broadcast address like
 * the mobile app does. The CS2/3 will response reveals the IP address.
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
public class CS2ConnectionFactory {

    private static CS2ConnectionFactory instance;

    private Connection cs2Connection;
    private InetAddress cs2Host;

    private static final String BROADCAST_ADDRESS = "255.255.255.255";

    private CS2ConnectionFactory() {
    }

    public static CS2ConnectionFactory getInstance() {
        if (instance == null) {
            instance = new CS2ConnectionFactory();
        }
        return instance;
    }

    Connection getConnectionImpl() {
        if (cs2Connection == null) {
            Logger.trace("Try to discover a CS2/3...");
            sendMobileAppUDPReply();

            if (cs2Host != null) {
                Logger.debug("CS2/3 ip: " + cs2Host.getHostName());
                cs2Connection = new TCPConnection(cs2Host);
            } else {
                Logger.warn("CS2/3 host not found!");
            }
        }
        return this.cs2Connection;
    }

    public static Connection getConnection() {
        return getInstance().getConnectionImpl();
    }

    private void sendMobileAppUDPReply() {
        try {
            InetAddress localAddress = InetAddress.getByName("0.0.0.0");
            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);

            CanMessage ping = CanMessageFactory.getPingRequest();

            try (DatagramSocket requestSocket = new DatagramSocket()) {
                DatagramPacket requestPacket = new DatagramPacket(ping.getBytes(), ping.getLength(), broadcastAddress, Connection.CS2_RX_PORT);
                requestSocket.send(requestPacket);
            }

            try (DatagramSocket responseSocket = new DatagramSocket(Connection.CS2_TX_PORT, localAddress)) {
                responseSocket.setSoTimeout(15000);
                DatagramPacket responsePacket = new DatagramPacket(new byte[CanMessage.MESSAGE_SIZE], CanMessage.MESSAGE_SIZE, localAddress, Connection.CS2_TX_PORT);
                responseSocket.receive(responsePacket);

                InetAddress replyHost = InetAddress.getByName(responsePacket.getAddress().getHostAddress());
                CanMessage response = new CanMessage(responsePacket.getData());

                Logger.trace("Received: " + response + " from: " + replyHost.getHostAddress());

                if (response.getCommand() == MarklinCan.SW_STATUS_REQ) {
                    if (this.cs2Host == null) {
                        this.cs2Host = replyHost;
                    }
                } else {
                    Logger.debug("Received wrong command: " + response.getCommand() + " != " + MarklinCan.SW_STATUS_REQ + "...");
                }
            }
        } catch (SocketTimeoutException ste) {
            Logger.debug("No reply. " + ste.getMessage());
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }

    public String getDeviceIp() {
        if (this.cs2Host != null) {
            return this.cs2Host.getHostAddress();
        } else {
            return "Unknown";
        }
    }

}
