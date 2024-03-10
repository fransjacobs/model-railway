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
import jcs.commandStation.marklin.cs.can.CanMessage;
import org.tinylog.Logger;
import jcs.commandStation.marklin.cs.events.CanPingListener;
import jcs.commandStation.marklin.cs.events.AccessoryListener;
import jcs.commandStation.marklin.cs.events.FeedbackListener;
import jcs.commandStation.marklin.cs.events.LocomotiveListener;
import jcs.commandStation.marklin.cs.events.SystemListener;

/**
 *
 * @author Frans Jacobs
 */
class TCPConnection implements CSConnection {

  private final InetAddress centralStationAddress;

  private Socket clientSocket;
  private DataOutputStream dos;

  private ClientMessageReceiver messageReceiver;

  private static final long SHORT_TIMEOUT = 1000L;
  private static final long LONG_TIMEOUT = 5000L;

  private boolean debug = false;

  TCPConnection(InetAddress csAddress) {
    centralStationAddress = csAddress;
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
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
    ResponseCallback callback = null;

    if (message != null) {
      if (message.expectsResponse() || message.expectsLongResponse()) {
        //Message is expecting response so lets register for response
        callback = new ResponseCallback(message);
        this.messageReceiver.registerResponseCallback(callback);
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
        this.messageReceiver.unRegisterResponseCallback();
      }
    }
    return message;
  }

  @Override
  public void setCanPingListener(CanPingListener pingListener) {
    if (messageReceiver != null) {
      this.messageReceiver.registerCanPingListener(pingListener);
    }
  }

  @Override
  public void setFeedbackListener(FeedbackListener feedbackListener) {
    if (messageReceiver != null) {
      this.messageReceiver.registerFeedbackListener(feedbackListener);
    }
  }

  @Override
  public void setSystemListener(SystemListener systemEventListener) {
    if (messageReceiver != null) {
      this.messageReceiver.registerSystemListener(systemEventListener);
    }
  }

  @Override
  public void setAccessoryListener(AccessoryListener accessoryEventListener) {
    if (messageReceiver != null) {
      this.messageReceiver.registerAccessoryListener(accessoryEventListener);
    }
  }

  @Override
  public void setLocomotiveListener(LocomotiveListener locomotiveListener) {
    if (messageReceiver != null) {
      this.messageReceiver.registerLocomotiveListener(locomotiveListener);
    }
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
    return this.messageReceiver != null && this.messageReceiver.isRunning();
  }

  private class ClientMessageReceiver extends Thread {

    private boolean quit = true;
    private DataInputStream din;

    private CanPingListener pingListener;
    private FeedbackListener feedbackListener;
    private SystemListener systemListener;
    private AccessoryListener accessoryListener;
    private LocomotiveListener locomotiveListener;

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
      this.callBack = null;
    }

    void registerCanPingListener(CanPingListener pingListener) {
      this.pingListener = pingListener;
    }

    void registerFeedbackListener(FeedbackListener feedbackListener) {
      this.feedbackListener = feedbackListener;
    }

    void registerSystemListener(SystemListener systemListener) {
      this.systemListener = systemListener;
    }

    void registerAccessoryListener(AccessoryListener accessoryListener) {
      this.accessoryListener = accessoryListener;
    }

    void registerLocomotiveListener(LocomotiveListener locomotiveListener) {
      this.locomotiveListener = locomotiveListener;
    }

    synchronized void quit() {
      this.quit = true;
    }

    synchronized boolean isRunning() {
      return !this.quit;
    }

    @Override
    public void run() {
      Thread.currentThread().setName("CAN-RX");

      this.quit = false;
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
          } else if (rx.isPingResponse() && pingListener != null) {
            this.pingListener.onCanPingResponseMessage(rx);
          } else if (rx.isPingRequest() && pingListener != null) {
            this.pingListener.onCanPingRequestMessage(rx);
          } else if (rx.isStatusConfigRequest() && pingListener != null) {
            this.pingListener.onCanStatusConfigRequestMessage(rx);
          } else if (rx.isSensorResponse() && feedbackListener != null) {
            this.feedbackListener.onFeedbackMessage(rx);
          } else if (rx.isSystemMessage() && systemListener != null) {
            this.systemListener.onSystemMessage(rx);
          } else if (rx.isAccessoryMessage() && accessoryListener != null) {
            this.accessoryListener.onAccessoryMessage(rx);
          } else if (rx.isLocomotiveMessage() && locomotiveListener != null) {
            this.locomotiveListener.onLocomotiveMessage(rx);
          } else {
            if (CanMessage.BOOTLOADER_CAN != 0x36) {
              //Do not log the bootloader message. it is not used in JCS. No idea what this message is for. 
              if (debug) {
                Logger.trace("#RX: " + rx);
              }
            }
          }
        } catch (SocketException se) {
          quit();
        } catch (IOException ioe) {
          Logger.error(ioe);
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

//  public static void main(String[] a) throws UnknownHostException {
//    boolean cs3 = true;
//
//    InetAddress inetAddr;
//    if (cs3) {
//      inetAddr = InetAddress.getByName("192.168.178.180");
//    } else {
//      inetAddr = InetAddress.getByName("192.168.178.86");
//    }
//
//    int uid;
//    if (cs3) {
//      uid = 1668498828;
//    } else {
//      uid = 1129552448;
//    }
//
//    TCPConnection c = new TCPConnection(inetAddr);
//
//    while (!c.messageReceiver.isRunning()) {
//
//    }
//
//    //CanMessage m = c.sendCanMessage(CanMessageFactory.getMembersPing());
//    CanMessage m = c.sendCanMessage(CanMessageFactory.querySystem(uid));
//
//    Logger.trace("TX: " + m);
//    for (CanMessage r : m.getResponses()) {
//      Logger.trace("RSP: " + r);
//    }
//
//    CanMessage m2 = c.sendCanMessage(CanMessageFactory.querySystem(uid));
//
//    Logger.trace("TX: " + m2);
//    for (CanMessage r : m2.getResponses()) {
//      Logger.trace("RSP: " + r);
//    }
//
//  }

}
