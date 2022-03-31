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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.CanMessageFactory;
import jcs.controller.cs3.can.MarklinCan;
import org.tinylog.Logger;

/**
 * Try to connect with a CS 3. A "ping" is send to the broadcast address like
 * the mobile app does. The CS 3 will response reveals the IP address.
 *
 * @author Frans Jacobs
 */
public class CS3ConnectionFactory {

    private static CS3ConnectionFactory instance;

    private CS3Connection controllerConnection;
    private HTTPConnection httpConnection;
    private InetAddress controllerHost;

    private static final String BROADCAST_ADDRESS = "255.255.255.255";

    private CS3ConnectionFactory() {
    }

    public static CS3ConnectionFactory getInstance() {
        if (instance == null) {
            instance = new CS3ConnectionFactory();
        }
        return instance;
    }

    CS3Connection getConnectionImpl() {
        if (controllerConnection == null) {
            Logger.trace("Try to discover a Marklin CS3...");
            sendMobileAppPing();

            if (controllerHost != null) {
                Logger.trace("CS3 ip: " + controllerHost.getHostName());
                controllerConnection = new TCPConnection(controllerHost);
            } else {
                Logger.warn("Can't find a Marklin Controller host!");
            }
        }
        return this.controllerConnection;
    }

    public static CS3Connection getConnection() {
        return getInstance().getConnectionImpl();
    }

    HTTPConnection getHTTPConnectionImpl() {
        if (controllerConnection == null) {
            getConnectionImpl();
        }
        if (httpConnection == null) {
            httpConnection = new HTTPConnection(controllerHost);
        }
        return this.httpConnection;
    }

    public static HTTPConnection getHTTPConnection() {
        return getInstance().getHTTPConnectionImpl();
    }

    private void sendMobileAppPing() {
        try {
            InetAddress localAddress = InetAddress.getByName("0.0.0.0");
            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);

            CanMessage ping = CanMessageFactory.getMobileAppPingRequest();

            try (DatagramSocket requestSocket = new DatagramSocket()) {
                Logger.trace("Sending: " + ping);
                DatagramPacket requestPacket = new DatagramPacket(ping.getBytes(), ping.getLength(), broadcastAddress, CS3Connection.CS3_RX_PORT);
                requestSocket.send(requestPacket);
            }

            try (DatagramSocket responseSocket = new DatagramSocket(CS3Connection.CS3_TX_PORT, localAddress)) {
                responseSocket.setSoTimeout(3000);
                DatagramPacket responsePacket = new DatagramPacket(new byte[CanMessage.MESSAGE_SIZE], CanMessage.MESSAGE_SIZE, localAddress, CS3Connection.CS3_TX_PORT);
                responseSocket.receive(responsePacket);

                InetAddress replyHost = InetAddress.getByName(responsePacket.getAddress().getHostAddress());
                CanMessage response = new CanMessage(responsePacket.getData());

                Logger.trace("Received: " + response + " from: " + replyHost.getHostAddress());

                if (response.getCommand() == MarklinCan.SW_STATUS_REQ) {
                    if (this.controllerHost == null) {
                        this.controllerHost = replyHost;
                    }
                } else {
                    Logger.debug("Received wrong command: " + response.getCommand() + " != " + MarklinCan.SW_STATUS_REQ + "...");
                }
            }
        } catch (SocketTimeoutException ste) {
            Logger.trace("No reply. " + ste.getMessage());
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }

    String getControllerIpImpl() {
        if (this.controllerHost != null) {
            return this.controllerHost.getHostAddress();
        } else {
            return "Unknown";
        }
    }

    public static String getControllerIp() {
        return getInstance().getControllerIpImpl();
    }

}
