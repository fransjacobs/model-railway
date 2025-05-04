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
package jcs.commandStation.marklin.cs.net;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.marklin.cs.can.CanMessage;
import org.tinylog.Logger;
import jcs.commandStation.events.ConnectionEventListener;

/**
 *
 * @author Frans Jacobs
 */
class CSTCPConnection implements CSConnection {

  private final InetAddress centralStationAddress;

  private Socket clientSocket;
  private DataOutputStream dos;

  private ClientMessageReceiver messageReceiver;
  private final List<ConnectionEventListener> disconnectionEventListeners;

  private static final long SHORT_TIMEOUT = 1000L;
  private static final long LONG_TIMEOUT = 5000L;

  private boolean debug = false;

  private final TransferQueue<CanMessage> eventQueue;

  CSTCPConnection(InetAddress csAddress) {
    centralStationAddress = csAddress;
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    eventQueue = new LinkedTransferQueue<>();
    disconnectionEventListeners = new ArrayList<>();
    checkConnection();
  }

  private void checkConnection() {
    try {
      if (clientSocket == null || !clientSocket.isConnected()) {
        clientSocket = new Socket(centralStationAddress, CSConnection.CS_RX_PORT);
        clientSocket.setKeepAlive(true);
        clientSocket.setTcpNoDelay(true);
        dos = new DataOutputStream(clientSocket.getOutputStream());

        messageReceiver = new ClientMessageReceiver(clientSocket);
        messageReceiver.setDaemon(true);
        messageReceiver.start();
      }
    } catch (IOException ex) {
      this.clientSocket = null;
      Logger.error("Can't (re)connect with Central Station " + centralStationAddress.getHostAddress() + ". Cause: " + ex.getMessage());
      Logger.trace(ex);
    }
  }

  @Override
  public TransferQueue<CanMessage> getEventQueue() {
    return this.eventQueue;
  }

  @Override
  public void addDisconnectionEventListener(ConnectionEventListener listener) {
    this.disconnectionEventListeners.add(listener);
  }

  private void disconnect() {
    messageReceiver.quit();
    if (dos != null) {
      try {
        dos.close();
      } catch (IOException ex) {
        Logger.error("Can't close output stream. Cause: " + ex.getMessage());
        Logger.trace(ex);
      }
    }

    disconnectionEventListeners.clear();
    if (clientSocket != null) {
      try {
        clientSocket.close();
      } catch (IOException ex) {
        Logger.error("Can't close socket. Cause: " + ex.getMessage());
        Logger.trace(ex);
      }
    }
  }

  private class ResponseCallback {

    private final CanMessage tx;
    private boolean done = false;

    ResponseCallback(final CanMessage tx) {
      this.tx = tx;
    }

    public boolean isSubscribedfor(int command) {
      int txCmd = this.tx.getCommand();
      if (CanMessage.REQUEST_CONFIG_DATA == txCmd) {
        //Special case so valid are +1 and +2
        return txCmd == (command - 1) || txCmd == (command - 2);
      } else {
        return txCmd == (command - 1);
      }
    }

    public void addResponse(CanMessage rx, int moreAvailable) {
      this.tx.addResponse(rx);
      this.done = moreAvailable == 0;
    }

    public boolean isResponseComplete() {
      //Most of the messages will have just one response but there are some which have more
      return tx.isResponseComplete() && this.done;
    }
  }

  @Override
  public synchronized CanMessage sendCanMessage(CanMessage message) {
    if (message == null) {
      Logger.warn("Message is NULL?");
      return null;
    }

    ResponseCallback callback = null;

    if (message.expectsResponse() || message.expectsLongResponse()) {
      //Message is expecting response so lets register for response
      callback = new ResponseCallback(message);
      messageReceiver.registerResponseCallback(callback);
    }

    try {
      byte[] bytes = message.getMessage();
      //Send the message
      dos.write(bytes);
      dos.flush();
    } catch (IOException ex) {
      Logger.error(ex);
    }

    if (CanMessage.PING_RESP != message.getCommand()) {
      //Do not log the ping response as this message is send every 5 seconds or so as a response to the CS ping request.
      if (debug) {
        Logger.trace("TX: " + message);
      }
    }

    long now = System.currentTimeMillis();
    long start = now;
    long timeout;

    if (message.expectsLongResponse()) {
      timeout = now + LONG_TIMEOUT;
    } else if (message.expectsResponse()) {
      timeout = now + SHORT_TIMEOUT;
    } else {
      timeout = now;
    }

    if (callback != null) {
      //Wait for the response
      boolean responseComplete = callback.isResponseComplete();
      while (!responseComplete && now < timeout) {
        responseComplete = callback.isResponseComplete();
        now = System.currentTimeMillis();
      }

      if (debug) {
        if (responseComplete) {
          Logger.trace("Got Response in " + (now - start) + " ms");
        } else {
          Logger.trace("No Response for " + message + " in " + (now - start) + " ms");
        }
      }

      //Remove the callback
      messageReceiver.unRegisterResponseCallback();
    }

    //Capture messages for now to be able to develop the virtual mode
    Logger.trace("#TX: " + message + (message.isResponseMessage() ? " response msg" : ""));
    if (!message.isResponseMessage()) {
      if (message.getResponses().size() > 1) {
        List<CanMessage> responses = message.getResponses();
        for (int i = 0; i < responses.size(); i++) {
          Logger.trace("#RX " + i + ": " + message.getResponse(i));
        }
      } else {
        Logger.trace("#RX: " + message.getResponse());
      }
    }

    return message;
  }

  @Override
  public void close() throws Exception {
    disconnect();
  }

  @Override
  public InetAddress getControllerAddress() {
    return centralStationAddress;
  }

  @Override
  public boolean isConnected() {
    return messageReceiver != null && messageReceiver.isRunning();
  }

  private class ClientMessageReceiver extends Thread {

    private boolean quit = true;
    private DataInputStream din;

    private ResponseCallback callBack;

    public ClientMessageReceiver(Socket socket) {
      try {
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        din = new DataInputStream(bis);
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }

    void registerResponseCallback(ResponseCallback callBack) {
      this.callBack = callBack;
    }

    void unRegisterResponseCallback() {
      callBack = null;
    }

    synchronized void quit() {
      quit = true;
    }

    synchronized boolean isRunning() {
      return !quit;
    }

    @Override
    public void run() {
      Thread.currentThread().setName("CS-CAN-RX");

      quit = false;
      Logger.trace("Started listening on port " + clientSocket.getLocalPort() + "...");

      while (isRunning()) {
        try {
          int prio = din.readUnsignedByte();
          int cmd = din.readUnsignedByte();
          int hash = din.readUnsignedShort();
          int dlc = din.readUnsignedByte();
          //read the data
          int dataIdx = 0;
          byte[] data = new byte[CanMessage.DATA_SIZE];
          while (dataIdx < CanMessage.DATA_SIZE) {
            data[dataIdx] = din.readByte();
            dataIdx++;
          }
          CanMessage rx = new CanMessage(prio, cmd, hash, dlc, data);

          //Logger.trace("RX: "+rx +"; "+ din.available());
          if (this.callBack != null && this.callBack.isSubscribedfor(cmd)) {
            this.callBack.addResponse(rx, din.available());
          } else {
            eventQueue.offer(rx);
            //Logger.trace("Enqueued: " + rx + " QueueSize: " + eventQueue.size());
          }

        } catch (SocketException se) {
          if (!quit) {
            String msg = "Host " + centralStationAddress.getHostName();
            ConnectionEvent de = new ConnectionEvent(msg, false);
            for (ConnectionEventListener listener : disconnectionEventListeners) {
              listener.onConnectionChange(de);
            }
          }

          quit();
        } catch (IOException ex) {
          Logger.error(ex);
        }
      }

      Logger.debug("Stop receiving");
      try {
        din.close();
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }
  }
}
