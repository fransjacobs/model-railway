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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.CanMessageFactory;
import lan.wervel.jcs.controller.cs2.events.CanMessageEvent;
import org.pmw.tinylog.Logger;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;

/**
 *
 * @author Frans Jacobs
 */
public class CS2Connection implements AutoCloseable {

    private static final int CS2_TX_PORT = 15730;
    private static final int CS2_RX_PORT = 15731;
    private static final String BROADCAST_ADDRESS = "255.255.255.255";

    private boolean listening;
    private DatagramSocket txSocket;

    private final List<CanMessageListener> canListeners;
    private InetAddress cs2Address;

    public CS2Connection() {
        canListeners = new ArrayList<>();
        try {
            this.txSocket = new DatagramSocket(CS2_RX_PORT);
        } catch (SocketException ex) {
            Logger.error(ex);
        }
    }

    private void startListening() {
        if (!listening) {
            listening = true;
        }

        Logger.debug("Starting CS2 listener thread on port " + CS2_TX_PORT + "...");
        try {
            //Listen on the CS2 tx messages. Listen on all interfaces
            DatagramSocket rxSocket = new DatagramSocket(CS2_TX_PORT, InetAddress.getByName("0.0.0.0"));
            rxSocket.setBroadcast(true);

            //Set a timeout so the thread can end when needed.
            rxSocket.setSoTimeout(5000);

            final Thread thread = new Thread(() -> {
                while (listening) {
                    try {
                        final DatagramPacket datagramPacket = new DatagramPacket(new byte[CanMessage.MESSAGE_SIZE], CanMessage.MESSAGE_SIZE);

                        //Blocking, but can throw timeout...
                        rxSocket.receive(datagramPacket);

                        InetAddress cs2 = InetAddress.getByName(datagramPacket.getAddress().getHostAddress());
                        Logger.trace("Received reply from " + cs2.getHostName());

                        final CanMessage msg = new CanMessage(datagramPacket.getData());
                        Logger.trace("Received: " + msg);

                        for (CanMessageListener cl : this.canListeners) {
                            //cl.onCanEvent(new CanMessageEvent(msg, cs2));
                        }
                    } catch (SocketTimeoutException ste) {
                        if (listening) {
                            Logger.trace("Timeout waiting for a CAN message.");
                        }
                    } catch (final IOException e) {
                        Logger.error(e);
                        break;
                    }
                }
                rxSocket.close();
                Logger.debug("CS2 Listener Thread finished.");
            });

            thread.start();

        } catch (UnknownHostException ex) {
            Logger.error(ex);
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }

    public boolean isListening() {
        return this.listening;
    }

    @SuppressWarnings("SleepWhileInLoop")
    private void discoverCS2() {
        //Broadcast the "PING" command
        try {
            CanMessage discoveryRequest = CanMessageFactory.getPingRequest();
            InetAddress address = InetAddress.getByName(BROADCAST_ADDRESS);
            DatagramPacket discoveryPacket = new DatagramPacket(discoveryRequest.getBytes(), CanMessage.MESSAGE_SIZE, address, CS2_RX_PORT);
            //Broadcast on the network     
            txSocket.setBroadcast(true);

            //CS2EventListener hostlistener = new CS2EventListener(this);
            //this.addCANListener(hostlistener);

            txSocket.send(discoveryPacket);
            Logger.trace("Send: " + discoveryRequest + " to " + BROADCAST_ADDRESS);

            long now = System.currentTimeMillis();
            //arbritriary time out of 15 s
            long timeout = now + 15000L;

            while (timeout > now && this.cs2Address == null) {
                try {
                    Thread.sleep(100L);
                    now = System.currentTimeMillis();
                } catch (InterruptedException ex) {
                    Logger.error(ex);
                }
            }

            if (this.cs2Address == null) {
                Logger.error("CS2 not found!");
            } else {
                Logger.debug("CS2 host: " + this.cs2Address.getHostName());
            }

            txSocket.setBroadcast(false);
            //this.removeCANListener(hostlistener);
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }

    void setCS2Host(InetAddress cs2Address) {
        this.cs2Address = cs2Address;
    }

    boolean connect() {
        //start the listener
        startListening();
        //Set discovery;
        discoverCS2();
        boolean connected = this.cs2Address != null;
        Logger.debug((connected ? "" : "NOT ") + "Connected" + (connected ? " to CS2 on " + this.cs2Address.getHostName() : ""));

        return connected;
    }

    void disConnect() {
        this.listening = false;
        this.cs2Address = null;
        Logger.debug("Disconnected");
    }

    public void send(CanMessage can) {
        DatagramPacket datagramPacket = new DatagramPacket(can.getBytes(), CanMessage.MESSAGE_SIZE, cs2Address, CS2_RX_PORT);

        try {
            Logger.trace("Sending: " + can.toString());
            this.txSocket.send(datagramPacket);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    @Override
    public void close() {
        this.listening = false;
        this.txSocket.close();
    }

    public void addCANListener(CanMessageListener canListener) {
        this.canListeners.add(canListener);
    }

    public void removeCANListener(CanMessageListener canListener) {
        this.canListeners.remove(canListener);
    }
    
    private void onMessage(CanMessageEvent canEvent) {
        Logger.debug(canEvent);
    }
    

//    private class CS2EventListener extends DiscoveryResponseListener {
//
//        private final CS2Connection cs2Connection;
//
//        CS2EventListener(CS2Connection cs2Connection) {
//            this.cs2Connection = cs2Connection;
//        }
//
//        @Override
//        public void setHost(InetAddress cs2Address) {
//            this.cs2Connection.setCS2Host(cs2Address);
//        }
//
//        @Override
//        public void onResponse() {
//        }
//
//    }
    private class CS2MessageListener implements CanMessageListener {

        private final CS2Connection cs2Connection;

        CS2MessageListener(CS2Connection cs2Connection) {
            this.cs2Connection = cs2Connection;
        }

        @Override
        public void onCanMessage(CanMessageEvent canEvent) {
            cs2Connection.onMessage(canEvent);
        }

    }

}
