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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import jcs.controller.cs.can.CanMessage;
import org.tinylog.Logger;
import jcs.controller.cs.events.CanPingListener;
import jcs.controller.cs.events.AccessoryListener;
import jcs.controller.cs.events.FeedbackListener;
import jcs.controller.cs.events.LocomotiveListener;
import jcs.controller.cs.events.SystemListener;

/**
 *
 * @author Frans Jacobs
 */
class TCPConnection implements CSConnection {

  private final InetAddress centralStationAddress;

  private Socket clientSocket;
  private DataOutputStream dos;

  private ClientMessageReceiver messageReceiver;

  private static final long SHORT_TIMEOUT = 500L;
  private static final long LONG_TIMEOUT = 4000L;

  TCPConnection(InetAddress csAddress) {
    centralStationAddress = csAddress;
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

  @Override
  public synchronized CanMessage sendCanMessage(CanMessage message) {

    if (message != null) {
      //set the message as a call-back for the reciever Thread
      this.messageReceiver.setCanMessage(message);

      try {
        byte[] bytes = message.getMessage();
        //Send the message
        dos.write(bytes);
        dos.flush();
      } catch (IOException ex) {
        Logger.error(ex);
      }
      Logger.trace("TX: " + message);
      long now = System.currentTimeMillis();
      long start = now;
      long timeout;
      if (message.expectsLargeResponse()) {
        timeout = now + LONG_TIMEOUT;
      } else if (message.expectsAcknowledge()) {
        timeout = now + SHORT_TIMEOUT;
      } else {
        timeout = now;
      }

      //Wait for the response 
      boolean responseComplete = !(message.expectsAcknowledge() || message.expectsLargeResponse());
      while ((now < timeout) && !responseComplete) {
        responseComplete = message.isResponseComplete();
        now = System.currentTimeMillis();
        //pause(5);
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
  public void setCanPingListener(CanPingListener pingListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setCanPingListener(pingListener);
    }
  }

  @Override
  public void setFeedbackListener(FeedbackListener feedbackListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setFeedbackListener(feedbackListener);
    }
  }

  @Override
  public void setSystemListener(SystemListener systemEventListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setSystemListener(systemEventListener);
    }
  }

  @Override
  public void setAccessoryListener(AccessoryListener accessoryEventListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setAccessoryListener(accessoryEventListener);
    }
  }

  @Override
  public void setLocomotiveListener(LocomotiveListener locomotiveListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setLocomotiveListener(locomotiveListener);
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

  private static void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public boolean isConnected() {
    return this.messageReceiver != null && this.messageReceiver.isRunning();

  }

  private class ClientMessageReceiver extends Thread {

    private boolean quit = false;
    private DataInputStream din;
    private CanMessage message;

    private CanPingListener pingListener;
    private FeedbackListener feedbackListener;
    private SystemListener systemListener;
    private AccessoryListener accessoryListener;
    private LocomotiveListener locomotiveListener;

    public ClientMessageReceiver(Socket socket) {
      try {
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        din = new DataInputStream(bis);
      } catch (IOException ex) {
        Logger.error(ex);
        this.quit = true;
      }
    }

    void setCanPingListener(CanPingListener pingListener) {
      this.pingListener = pingListener;
    }

    void setFeedbackListener(FeedbackListener feedbackListener) {
      this.feedbackListener = feedbackListener;
    }

    void setSystemListener(SystemListener systemListener) {
      this.systemListener = systemListener;
    }

    void setAccessoryListener(AccessoryListener accessoryListener) {
      this.accessoryListener = accessoryListener;
    }

    void setLocomotiveListener(LocomotiveListener locomotiveListener) {
      this.locomotiveListener = locomotiveListener;
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
      Thread.currentThread().setName("CAN-RX");
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
            //data[dataIdx] = din.readUnsignedByte();
            data[dataIdx] = din.readByte();
            dataIdx++;
          }
          CanMessage rx = new CanMessage(prio, cmd, hash, dlc, data);

          if (this.message != null && rx.isResponseFor(message)) {
            this.message.addResponse(rx);
            //Logger.trace("RX: " + rx + " Response count " + this.message.getResponses().size());
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
            Logger.trace("#RX: " + rx);
          }
        } catch (SocketException se) {
          quit();
        } catch (IOException ioe) {
          Logger.error(ioe);
        }
        //pause();
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
