/*
 * Copyright (C) 2020 frans.
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
package jcs.controller.cs2.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.controller.cs2.can.CanMessage;
import jcs.controller.cs2.events.CanMessageEvent;
import jcs.controller.cs2.events.CanMessageListener;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class UDPListener extends Thread {

    private static UDPListener instance;

    private boolean canOpenSocket = false;
    private DatagramSocket socket;
    private boolean running = false;

    private static final int MAX_ERRORS = 15;
    static final int TIMEOUT_MILLIS = 15000;

    private final List<CanMessageListener> canMessageListeners;
    private final ExecutorService executor;

    private UDPListener() {
        this.canOpenSocket = openUDPSocket();
        canMessageListeners = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
    }

    static UDPListener getInstance() {
        if (instance == null) {
            instance = new UDPListener();
            if (instance.canOpenSocket) {
                instance.start();
            }
        }
        return instance;
    }

    private boolean openUDPSocket() {
        try {
            InetAddress addr = InetAddress.getByName("0.0.0.0");
            socket = new DatagramSocket(Connection.CS2_TX_PORT, addr);
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MILLIS);
            return true;
        } catch (SocketException | UnknownHostException ex) {
            Logger.error("Could not create UDP socket on port " + Connection.CS2_TX_PORT);
            return false;
        }
    }

    @Override
    public void run() {
        int errorCount = 0;

        running = canOpenSocket;
        if (running) {
            Logger.trace("UDPListener is listening on port " + Connection.CS2_TX_PORT);
        } else {
            Logger.error("Can't Start UDPListener on port " + Connection.CS2_TX_PORT);
        }
        while (running) {
            DatagramPacket rxPacket = new DatagramPacket(new byte[CanMessage.MESSAGE_SIZE], CanMessage.MESSAGE_SIZE);

            InetAddress sourceAddress;
            try {
                socket.receive(rxPacket);
                sourceAddress = InetAddress.getByName(rxPacket.getAddress().getHostAddress());
            } catch (IOException ioe) {
                errorCount++;
                Logger.warn("ErrorCount: " + errorCount + " Cause: " + ioe.getMessage(), ioe);
                if (errorCount >= MAX_ERRORS) {
                    Logger.error("Quiting due to too much errors!");
                    return;
                }
                continue;
            }

            CanMessage received = new CanMessage(rxPacket.getData());
            
            Logger.trace("Received: "+received);
            CanMessageEvent cme = new CanMessageEvent(received, sourceAddress);
            executor.execute(() -> notifyCanMessageListeners(cme));
            errorCount = 0;
        }
        this.socket.close();
        Logger.trace("UDPListener stopped.");
    }

    boolean canRun() {
        return canOpenSocket;
    }

    boolean isRunning() {
        return this.running;
    }

    void stopListening() {
        this.running = false;
        this.canMessageListeners.clear();
    }

    public void addCanMessageListener(CanMessageListener listener) {
        this.canMessageListeners.add(listener);
    }

    public void removeCanMessageListener(CanMessageListener listener) {
        this.canMessageListeners.remove(listener);
    }

    private void notifyCanMessageListeners(final CanMessageEvent event) {
        for (CanMessageListener listener : this.canMessageListeners) {
            listener.onCanMessage(event);
        }
    }
}
