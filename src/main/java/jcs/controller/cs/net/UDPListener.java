/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.controller.cs.net;

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
import jcs.controller.cs.can.CanMessage;
import jcs.controller.cs.events.CanMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
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
            socket = new DatagramSocket(CSConnection.CS_TX_PORT, addr);
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MILLIS);
            return true;
        } catch (SocketException | UnknownHostException ex) {
            Logger.error("Could not create UDP socket on port " + CSConnection.CS_TX_PORT);
            return false;
        }
    }

    @Override
    public void run() {
        int errorCount = 0;

        running = canOpenSocket;
        if (running) {
            Logger.trace("UDPListener is listening on port " + CSConnection.CS_TX_PORT);
        } else {
            Logger.error("Can't Start UDPListener on port " + CSConnection.CS_TX_PORT);
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

            Logger.trace("Received: " + received);
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
