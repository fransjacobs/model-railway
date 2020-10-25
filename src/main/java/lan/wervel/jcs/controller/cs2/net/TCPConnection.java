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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
    DataInputStream din;

    TCPConnection(InetAddress cs2Address) {
        this.cs2Address = cs2Address;
        listeners = new ArrayList<>();
        executor = Executors.newCachedThreadPool();

        connect();
    }

    private void connect() {
        try {
            socket = new Socket(cs2Address, Connection.CS2_RX_PORT);

            socket.setSoTimeout(5000);

            dos = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());

        } catch (IOException ex) {
            Logger.error("Can't connect whith CS2/3 " + cs2Address.getHostAddress() + ". Cause: " + ex.getMessage());
            Logger.trace(ex);
        }
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

    @Override
    public CanMessage sendCanMessage(CanMessage message) {
        try {
            //Logger.trace("Send " + message);
            dos.write(message.getBytes());

            //Receive first packet
            byte[] data = new byte[1];
            {
                int currByte = din.read();
                if (currByte < 0) {
                    throw new IOException("Connection closed");
                }
                data[0] = (byte) currByte;
            }

            List<CanMessage> responseMessages = new ArrayList<>();
            while (din.available() > 0) {
                byte[] tempHolder = new byte[1024];
                int bytesRead = din.read(tempHolder);
                if (bytesRead < 0) {
                    throw new IOException("Connection closed");
                }

                byte[] tempCopy = new byte[data.length + bytesRead];
                System.arraycopy(data, 0, tempCopy, 0, data.length);
                System.arraycopy(tempHolder, 0, tempCopy, data.length, bytesRead);

                data = new byte[tempCopy.length];
                System.arraycopy(tempCopy, 0, data, 0, data.length);
                //need to pause for 10 ms...(?) else not all messages are captured ...
                pause(10);
            }

            //Logger.trace("data len:" + data.length);
            int cmd = message.getCommand();
            for (int i = 0; i < data.length; i += CanMessage.MESSAGE_SIZE) {
                //Logger.trace("Index = " + i);
                byte[] m = new byte[CanMessage.MESSAGE_SIZE];

                if (data.length > i + m.length) {
                    System.arraycopy(data, i, m, 0, m.length);
                    CanMessage cm = new CanMessage(m);
                    if (cm.getCommand() == cmd + 1) {
                        message.addResponse(cm);
                        //Logger.trace("Got reply for: " + message + " -> " + cm);
                    } else {
                        responseMessages.add(cm);
                        //Logger.trace(cm);
                    }
                }
            }

            //Logger.trace("Received " + message.getResponses().size() + " responses");
            if (!responseMessages.isEmpty()) {
                //Logger.trace("received " + responseMessages.size());
                InetAddress replyAddress = socket.getInetAddress();

                for (CanMessage cm : responseMessages) {
                    CanMessageEvent event = new CanMessageEvent(cm, replyAddress);
                    executor.execute(() -> fireMessageListeners(event));
                }
            } else {
                //Logger.trace("No extra messages received.");
            }

        } catch (SocketTimeoutException ste) {
            Logger.debug("No reply on message " + message + " Error: " + ste.getMessage());
        } catch (IOException e) {
            Logger.error("Message " + message + " not send cause: " + e.getMessage());
            Logger.trace(e);
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
