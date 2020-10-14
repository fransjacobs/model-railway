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
import lan.wervel.jcs.controller.ControllerInfo;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.CanMessageFactory;
import lan.wervel.jcs.controller.cs2.events.CanMessageEvent;
import lan.wervel.jcs.controller.cs2.events.CanMessageListener;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;

/**
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
class TCPConnection implements Connection {

    private final InetAddress cs2Address;
    private final List<CanMessageListener> listeners;
    private final ExecutorService executor;

    TCPConnection(InetAddress cs2Address) {
        this.cs2Address = cs2Address;
        listeners = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public CanMessage sendCanMessage(CanMessage message) {
        CanMessage response = null;
        try {
            try (Socket socket = new Socket(cs2Address, Connection.CS2_RX_PORT);
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    DataInputStream din = new DataInputStream(socket.getInputStream());) {

                if (socket.isConnected()) {
                    socket.setSoTimeout(5000);
                    Logger.trace("Send " + message);

                    //Send message
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
                    }

                    CanMessage reply = new CanMessage(data);

                    //what kind of respnse is expected?
                    

                    message.addResponse(reply);

                    Logger.trace("Reply: " + reply + ", isResponse " + reply.isResponseMessage() + (reply.getNumberOfMeasurementValues() < 0 ? "" : ", Measurement values " + reply.getNumberOfMeasurementValues()));

                    int expectedPackets = reply.getNumberOfMeasurementValues();
                    if (expectedPackets > 0) {
                        Logger.trace("Expected packets " + expectedPackets + " Expected bytes " + ((expectedPackets) * 13));

                        data = new byte[0];
                        int expBytes = (expectedPackets + 1) * CanMessage.MESSAGE_SIZE;
                        while (data.length < expBytes) {
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
                        }

                        for (int i = 0; i < data.length; i = i + CanMessage.MESSAGE_SIZE) {
                            byte[] chunck = new byte[CanMessage.MESSAGE_SIZE];
                            System.arraycopy(data, i, chunck, 0, chunck.length);
                            CanMessage extra = new CanMessage(chunck);
                            message.addResponse(extra);
                            if (message.hasValidResponse()) {
                                Logger.trace("Package # " + message.getResponses().size() + " is the request response: " + extra);
                            }
                        }
                    }

                    InetAddress replyAddress = socket.getInetAddress();
                    CanMessageEvent event = new CanMessageEvent(response, replyAddress);

                    executor.execute(() -> fireMessageListeners(event));
                } else {
                    Logger.warn("Not connected!");
                }
            }
        } catch (SocketTimeoutException ste) {
            Logger.debug("No reply on message " + message + " Error: " + ste.getMessage());
        } catch (IOException e) {
            Logger.error(e);
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
    }

    public static void main(String[] a) {
        Configurator.defaultConfig().level(org.pmw.tinylog.Level.TRACE).activate();
        try {
            InetAddress cs2Address = InetAddress.getByName("192.168.1.127");
            TCPConnection con = new TCPConnection(cs2Address);

            int[] cs2Uid = new int[]{0x43, 0x53, 0x9a, 0x40};

//      con.sendCanMessage(CanMessageFactory.go(null));
//      waitAsec();
//      waitAsec();
//      con.sendCanMessage(CanMessageFactory.stop(null));
            CanMessage req = CanMessageFactory.statusConfig(cs2Uid);

            con.sendCanMessage(req);

            ControllerInfo info = new ControllerInfo(req);

            Logger.info(info);

            waitAsec();
            waitAsec();

            con.close();

        } catch (Exception ex) {
            Logger.error(ex);
        }
    }

    private static void waitAsec() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ex) {
            Logger.error(ex);
        }
    }

}
