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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.events.CanMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
class UDPConnection implements CS3Connection {

    private final InetAddress cs2Address;

    private final List<CanMessageListener> listeners;
    private final ExecutorService executor;

    UDPConnection(InetAddress cs2Address) {
        this.cs2Address = cs2Address;
        listeners = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public CanMessage sendCanMessage(CanMessage message) {
        Logger.trace("Sending: " + message + " from: " + this.cs2Address.getHostAddress() + " port " + CS3Connection.CS3_RX_PORT);
        CanMessage response = null;
        try {
            InetAddress localAddress = InetAddress.getByName("0.0.0.0");

            try (DatagramSocket requestSocket = new DatagramSocket()) {
                DatagramPacket requestPacket = new DatagramPacket(message.getBytes(), message.getLength(), cs2Address, CS3Connection.CS3_RX_PORT);
                requestSocket.send(requestPacket);
            }

            try (DatagramSocket responseSocket = new DatagramSocket(CS3Connection.CS3_TX_PORT, localAddress)) {
                responseSocket.setSoTimeout(5000);
                DatagramPacket responsePacket = new DatagramPacket(new byte[CanMessage.MESSAGE_SIZE], CanMessage.MESSAGE_SIZE, localAddress, CS3Connection.CS3_TX_PORT);

                Logger.trace("Listen on " + localAddress.getHostAddress() + " port " + CS3Connection.CS3_TX_PORT);

                responseSocket.receive(responsePacket);

                InetAddress replyAddress = InetAddress.getByName(responsePacket.getAddress().getHostAddress());
                Logger.trace("Received reply from " + replyAddress.getHostName());

                response = new CanMessage(responsePacket.getData());
                Logger.trace("Received: " + response);

                CanMessageEvent event = new CanMessageEvent(response, replyAddress);
                executor.execute(() -> fireMessageListeners(event));
                //fireMessageListeners(event);
            }
        } catch (SocketTimeoutException ste) {
            Logger.debug("No reply. " + ste.getMessage());
        } catch (IOException ex) {
            Logger.error(ex);
        }

        return response;
    }

    @Override
    public void addCanMessageListener(CanMessageListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeCanMessageListener(CanMessageListener listener) {
        this.listeners.remove(listener);
    }

    private void fireMessageListeners(final CanMessageEvent event) {
        for (CanMessageListener listener : this.listeners) {
            listener.onCanMessage(event);
        }
    }

    @Override
    public void close() throws Exception {
        this.executor.shutdown();
    }

    @Override
    public InetAddress getControllerAddress() {
        return cs2Address;
    }

}
