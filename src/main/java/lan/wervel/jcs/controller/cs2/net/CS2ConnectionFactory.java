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

import java.net.InetAddress;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.MarklinCan;
import lan.wervel.jcs.controller.cs2.events.CanMessageEvent;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;
import org.pmw.tinylog.Logger;

/**
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
public class CS2ConnectionFactory {

    private static CS2ConnectionFactory instance;

    private Connection cs2Connection;
    private UDPListener udpListener;
    private InetAddress cs2Host;
    private int cs2Uid;

    //private static final byte[] PING = new byte[]{0x00, 0x31, 0x47, 0x11, 0x08, 0x4f, 0x59, 0x10, (byte) 0xdf, 0x01, 0x04, (byte) 0xee, (byte) 0xee};
    //private static final String BROADCAST_ADDRESS = "255.255.255.255";
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
            passivePing();

            if (cs2Host != null) {
                Logger.debug("CS2/3 ip: " + cs2Host.getHostName() + " CS2/3 UID: " + this.cs2Uid);
                cs2Connection = new TCPConnection(this.cs2Host);
            } else {
                Logger.warn("CS2/3 host not found!");
            }
        }
        return this.cs2Connection;
    }

    public static Connection getConnection() {
        return getInstance().getConnectionImpl();
    }

    public static int getCS2Uid() {
        return getInstance().cs2Uid;
    }

    private void passivePing() {
        udpListener = UDPListener.getInstance();

        udpListener.addCanMessageListener(new CanMessageListener() {
            @Override
            public void onCanMessage(CanMessageEvent canEvent) {
                parseMessage(canEvent);
            }
        });

        Long now = System.currentTimeMillis();
        Long timeout = now + UDPListener.TIMEOUT_MILLIS;
        while (now < timeout && cs2Host == null && cs2Uid == 0) {
            now = System.currentTimeMillis();
        }
        if (now > timeout) {
            Logger.error("Timeout while waiting for a CS2/3 to respond.");
        }
    }

    private void parseMessage(CanMessageEvent canEvent) {
        CanMessage msg = canEvent.getCanMessage();

        switch (msg.getCommand()) {
            case MarklinCan.REQ_PING:
                if (this.cs2Host == null) {
                    this.cs2Host = canEvent.getSourceAddress();
                }
                if (this.cs2Uid == 0) {
                    this.cs2Uid = msg.getUidInt();
                }
                break;
            default:
            // Ignore Message...
            //Logger.trace(msg);
        }
    }

//    private void activePing() {
//        try {
//            Logger.debug("Try to discover a CS2/3....");
//            InetAddress localAddress = InetAddress.getByName("0.0.0.0");
//            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
//
//            try (DatagramSocket requestSocket = new DatagramSocket()) {
//                DatagramPacket requestPacket = new DatagramPacket(PING, PING.length, broadcastAddress, Connection.CS2_RX_PORT);
//                requestSocket.send(requestPacket);
//            }
//
//            try (DatagramSocket responseSocket = new DatagramSocket(Connection.CS2_TX_PORT, localAddress)) {
//                responseSocket.setSoTimeout(10000);
//                DatagramPacket responsePacket = new DatagramPacket(new byte[CanMessage.MESSAGE_SIZE], CanMessage.MESSAGE_SIZE, localAddress, Connection.CS2_TX_PORT);
//
//                Logger.trace("Listen on " + localAddress.getHostAddress() + " port " + Connection.CS2_TX_PORT);
//
//                responseSocket.receive(responsePacket);
//
//                this.cs2Host = InetAddress.getByName(responsePacket.getAddress().getHostAddress());
//                Logger.trace("Received reply from " + this.cs2Host.getHostAddress());
//
//                CanMessage response = new CanMessage(responsePacket.getData());
//                Logger.trace("Received: " + response);
//            }
//        } catch (SocketTimeoutException ste) {
//            Logger.debug("No reply. " + ste.getMessage());
//        } catch (IOException ex) {
//            Logger.error(ex);
//        }
//    }
//    public static void main(String[] a) {
//        Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();
//
//        Connection c = CS2ConnectionFactory.getConnection();
//    }
    public void addCanMessageListener(CanMessageListener listener) {
        this.udpListener.addCanMessageListener(listener);
    }

    public void removeCanMessageListener(CanMessageListener listener) {
        this.udpListener.removeCanMessageListener(listener);
    }

}
