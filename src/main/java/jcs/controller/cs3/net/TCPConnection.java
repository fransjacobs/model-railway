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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.events.CanMessageEvent;
import jcs.controller.cs3.events.CanMessageListener;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
class TCPConnection implements CS3Connection {

    private final InetAddress cs3Address;

    private Socket socket;
    private ClientMessageReceiver messageReceiver;

    private DataOutputStream dos;

    TCPConnection(InetAddress csAddress) {
        cs3Address = csAddress;
        checkConnection();
    }

    private void checkConnection() {
        try {
            if (socket == null || !socket.isConnected()) {
                socket = new Socket(cs3Address, CS3Connection.CS3_RX_PORT);
                socket.setSoTimeout(5000);

                dos = new DataOutputStream(socket.getOutputStream());
                messageReceiver = new ClientMessageReceiver(socket);
                messageReceiver.start();
            }
        } catch (IOException ex) {
            this.socket = null;
            Logger.error("Can't (re)connect with CS3 " + cs3Address.getHostAddress() + ". Cause: " + ex.getMessage());
            Logger.trace(ex);
        }
    }

    private void disconnect() {
        this.messageReceiver.quit();
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

    @Override
    public CanMessage sendCanMessage(CanMessage message) {
        if (message != null) {
            //set the messaage as a call-back for the reciever Thread
            this.messageReceiver.setCanMessage(message);

            byte[] bytes = message.getBytes();
            try {
                //Send the message
                dos.write(bytes);
                dos.flush();
            } catch (IOException ex) {
                Logger.error(ex);
            }
            Logger.trace("TX: " + message);

            long now = System.currentTimeMillis();
            long start = now;
            long timeout = now + 1000L;
            //Wait for the response 
            boolean responseComplete = false;
            while ((now < timeout) && !responseComplete) {
                synchronized (message) {
                    responseComplete = message.isResponseComplete();
                }
                now = System.currentTimeMillis();
                pause(5);
            }

            if (responseComplete) {
                Logger.trace("Got Response in " + (now - start) + " ms");
            } else {
                Logger.trace("No Response for " + message + " in " + (now - start) + " ms");
            }
            //Remove the callback
            this.messageReceiver.setCanMessage(null);
        }
        return message;
    }

    @Override
    public void addCanMessageListener(CanMessageListener listener) {
        if (messageReceiver != null) {
            this.messageReceiver.addCanMessageListener(listener);
        }
    }

    @Override
    public void removeCanMessageListener(CanMessageListener listener) {
        if (messageReceiver != null) {
            this.messageReceiver.removeCanMessageListener(listener);
        }
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }

    @Override
    public InetAddress getControllerAddress() {
        return cs3Address;
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.error(ex);
        }
    }

    private class ClientMessageReceiver extends Thread {

        Socket serverSocket;
        private boolean quit = false;
        private DataInputStream din;

        private CanMessage message;
        private List<CanMessageListener> listeners;

        public ClientMessageReceiver(Socket serverSocket) {
            try {
                this.serverSocket = serverSocket;
                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                din = new DataInputStream(bis);
                listeners = new ArrayList<>();
            } catch (IOException ex) {
                Logger.error(ex);
                this.quit = true;
            }
        }

        void addCanMessageListener(CanMessageListener listener) {
            this.listeners.add(listener);
        }

        void removeCanMessageListener(CanMessageListener listener) {
            this.listeners.remove(listener);
        }

        synchronized void quit() {
            this.quit = true;
        }

        synchronized boolean isRunning() {
            return !this.quit;
        }

        private void pause() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.warn(ex);
            }
        }

        //call-back
        synchronized void setCanMessage(CanMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            while (isRunning()) {
                Thread.currentThread().setName("CAN-RX");
                Logger.trace("Started listening on port " + socket.getLocalPort() + "...");
                try {
                    int size = 0;
                    byte[] buffer = new byte[CanMessage.MESSAGE_SIZE];
                    while (size != -1) {
                        size = din.read(buffer);
                        if (size != -1) {
                            CanMessage resp = new CanMessage(buffer);
                            if (this.message != null && resp.isResponseFor(message)) {
                                synchronized (message) {
                                    this.message.addResponse(resp);
                                }
                                Logger.trace("RX: " + resp + " Response count " + this.message.getResponses().size());
                            } else {
                                if (resp.isEvent()) {
                                    CanMessageEvent cme = new CanMessageEvent(resp, cs3Address);
                                    Logger.trace((message == null ? "Idle" : "RX") + " Event: " + resp);
                                    for (CanMessageListener l : this.listeners) {
                                        l.onCanMessage(cme);
                                    }
                                }
                            }
                        }
                        buffer = new byte[CanMessage.MESSAGE_SIZE];
                    }
                } catch (SocketException se) {
                    quit();
                } catch (IOException ioe) {
                    Logger.error(ioe);
                }
                pause();
            }
            Logger.trace("Stop receiving");
            try {
                din.close();
            } catch (IOException ex) {
                Logger.error(ex);
            }
        }
    }
}
