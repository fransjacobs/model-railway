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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.events.CanMessageEvent;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;
import org.pmw.tinylog.Logger;

/**
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
class TCPConnection implements Connection {

    private final InetAddress cs2Address;
    private final List<CanMessageListener> listeners;
    private final ExecutorService executor;

    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream din;

    private ByteArrayInputStream bais;

    TCPConnection(InetAddress cs2Address) {
        this.cs2Address = cs2Address;
        listeners = new ArrayList<>();
        executor = Executors.newCachedThreadPool();

        checkConnection();
    }

    private void checkConnection() {
        try {
            if (socket == null || !socket.isConnected()) {
                socket = new Socket(cs2Address, Connection.CS2_RX_PORT);
                socket.setSoTimeout(5000);
                dos = new DataOutputStream(socket.getOutputStream());
                din = new DataInputStream(socket.getInputStream());
            }
        } catch (IOException ex) {
            this.socket = null;
            Logger.error("Can't (re)connect with CS2/3 " + cs2Address.getHostAddress() + ". Cause: " + ex.getMessage());
            Logger.trace(ex);
        }
    }

    private boolean isDataAvailable() {
        boolean dataAvailable = false;
        try {
            if (din.available() > 0) {
                dataAvailable = true;
            }
        } catch (IOException e) {
            Logger.error("Error while checking for bytes available!");
            Logger.trace(e);
        }
        return dataAvailable;
    }

    private void disconnect() {
        if (din != null) {
            try {
                din.close();
            } catch (IOException ex) {
                Logger.error("Can't close input stream. Cause: " + ex.getMessage());
                Logger.trace(ex);
            }
        }
        if (dos != null) {
            try {
                dos.close();
            } catch (IOException ex) {
                Logger.error("Can't close output stream. Cause: " + ex.getMessage());
                Logger.trace(ex);
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.error("Can't close socket. Cause: " + ex.getMessage());
                Logger.trace(ex);
            }
        }
    }

    private CanMessage receiveCanMessage() throws IOException {
        byte[] buffer = new byte[CanMessage.MESSAGE_SIZE];

        int bytesRead = din.read(buffer);
        if (bytesRead < 0) {
            throw new IOException("Connection closed");
        }
        CanMessage cm = new CanMessage(buffer);

        return cm;
    }

    private List<CanMessage> receiveCanMessages() {
        List<CanMessage> messages = new ArrayList<>();
        while (isDataAvailable()) {
            try {
                CanMessage cm = receiveCanMessage();
                messages.add(cm);
            } catch (IOException e) {
                Logger.error(e);
            }
        }
        return messages;
    }

    private void sendMessage(byte[] data) {
        checkConnection();
        try {
            dos.write(data);
        } catch (IOException e) {
            Logger.error("Data send failed!");
        }
    }

    @Override
    public CanMessage sendCanMessage(CanMessage message) {
//        Logger.trace("TX: " + message);
        sendMessage(message.getBytes());

        //For what ever reason after sending the message a CS2/3 need some time to
        //process an prepare an response.
        //so wait at least 10 ms otherwise there is no valid response...
        if (message.expectsAcknowledge()) {
            //For some messages the wait is even longer...
            pause(85);
        } else if (message.isMemberPing()) {
            pause(20);
        } else {
            pause(10);
        }

        List<CanMessageEvent> events = new ArrayList<>();

        if (isDataAvailable()) {
            InetAddress replyAddress = socket.getInetAddress();
            List<CanMessage> responses = receiveCanMessages();
            for (CanMessage resp : responses) {
                if (resp.isResponseFor(message)) {
                    message.addResponse(resp);
//                    if (message.expectsAcknowledge()) {
//                        Logger.trace("RX: " + resp + " Ack? " + resp.isAcknowledgeFor(message));
//                    } else {
//                        Logger.trace("RX: " + resp);
//                    }
                } else {
                    CanMessageEvent cme = new CanMessageEvent(resp, replyAddress);
                    events.add(cme);
//                    Logger.trace("Event RX: " + resp);
                }
            }
        }
        if (!events.isEmpty()) {
//            Logger.trace("Received " + events.size() + " Event messages...");
            for (CanMessageEvent me : events) {
                executor.execute(() -> fireMessageListeners(me));
            }
        }

        return message;
    }

    private void fireMessageListeners(final CanMessageEvent event) {
        for (CanMessageListener listener : this.listeners) {
            listener.onCanMessage(event);
        }
    }

    @Override
    public void addCanMessageListener(CanMessageListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeCanMessageListener(CanMessageListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void close() throws Exception {
        this.executor.shutdown();
        disconnect();
    }

    @Override
    public InetAddress getCs2Address() {
        return cs2Address;
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.error(ex);
        }
    }

}
