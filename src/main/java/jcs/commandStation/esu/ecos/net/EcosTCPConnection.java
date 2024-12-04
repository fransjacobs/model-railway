/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.esu.ecos.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.commandStation.events.DisconnectionEvent;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
class EcosTCPConnection implements EcosConnection {

  private final InetAddress ecosAddress;
  private Socket clientSocket;
  private Writer writer;

  private final TransferQueue<String> transferQueue;
  private final TransferQueue<EcosMessage> eventQueue;

  private ClientMessageReceiver messageReceiver;
  private boolean debug = false;
  private static final long TIMEOUT = 500L;

  EcosTCPConnection(InetAddress address) {
    ecosAddress = address;
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    transferQueue = new LinkedTransferQueue<>();
    eventQueue = new LinkedTransferQueue<>();
    checkConnection();
  }

  private void checkConnection() {
    try {
      if (clientSocket == null || !clientSocket.isConnected()) {
        clientSocket = new Socket(ecosAddress, DEFAULT_NETWORK_PORT);
        clientSocket.setKeepAlive(true);
        clientSocket.setTcpNoDelay(true);
        writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        messageReceiver = new ClientMessageReceiver(clientSocket);
        messageReceiver.setDaemon(true);
        messageReceiver.start();
      }
    } catch (IOException ex) {
      this.clientSocket = null;
      Logger.error("Can't (re)connect with ESU Ecos " + ecosAddress.getHostAddress() + ". Cause: " + ex.getMessage());
      Logger.trace(ex);
    }
  }

  private void disconnect() {
    this.messageReceiver.quit();

    //wait until the messageReceiver has shut down
    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + TIMEOUT;
    boolean finished = this.messageReceiver.isFinished();
    while (!finished && now < timeout) {
      finished = this.messageReceiver.isFinished();
      now = System.currentTimeMillis();
    }

    if (!finished) {
      Logger.warn("Message receiver thread not finished after " + (now - start) + " ms");
    }

    if (writer != null) {
      try {
        writer.close();
      } catch (IOException ex) {
        Logger.error("Can't close output. Cause: " + ex.getMessage());
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
  public void setMessageListener(EcosMessageListener messageListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setMessageListener(messageListener);
    }
  }

  @Override
  public synchronized EcosMessage sendMessage(EcosMessage message) {
    try {
      writer.write(message.getMessage());
      writer.flush();
      if (debug) {
        Logger.trace("TX:" + message.getMessage());
      }

      long now = System.currentTimeMillis();
      long start = now;
      String reply = this.transferQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);

      message.addResponse(reply);

      now = System.currentTimeMillis();
      if (debug) {
        //if (true) {
        if (message.isResponseComplete()) {
          Logger.trace("Reply in " + (now - start) + " ms");
        } else {
          Logger.trace("No Reply for " + message + " in " + (now - start) + " ms");
        }
      }
    } catch (IOException | InterruptedException ex) {
      Logger.error(ex);
      String msg = "Host " + ecosAddress.getHostName();
      DisconnectionEvent de = new DisconnectionEvent(msg);

      messageReceiver.messageListener.onDisconnect(de);
      messageReceiver.quit();
    }
    return message;
  }

  @Override
  public void close() {
    disconnect();
  }

  @Override
  public boolean isConnected() {
    return this.messageReceiver != null && this.messageReceiver.isRunning();
  }

  @Override
  public TransferQueue<EcosMessage> getEventQueue() {
    return this.eventQueue;
  }

  private class ClientMessageReceiver extends Thread {

    private boolean stop = false;
    private boolean quit = true;
    private BufferedReader reader;
    private EcosMessageListener messageListener;

    public ClientMessageReceiver(Socket socket) {
      try {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }

    void quit() {
      this.quit = true;
      //Shutdown the socket input otherwise the receving thread can't stop
      try {
        clientSocket.shutdownInput();
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }

    boolean isRunning() {
      return !this.quit;
    }

    boolean isFinished() {
      return this.stop;
    }

    void setMessageListener(EcosMessageListener messageListener) {
      this.messageListener = messageListener;
    }

    @Override
    public void run() {
      this.quit = false;
      this.setName("ESU-ECOS-RX");

      Logger.trace("Started listening on port " + clientSocket.getLocalPort() + " ...");

      while (isRunning()) {
        try {
          String rx = reader.readLine();
          Logger.trace("RX: " + rx);
          if (rx != null && rx.startsWith(EcosMessage.REPLY)) {
            StringBuilder sb = new StringBuilder();

            long now = System.currentTimeMillis();
            long start = now;
            long timeout = now + TIMEOUT;

            sb.append(rx);
            boolean complete = EcosMessage.isComplete(rx);

            while (!complete && now < timeout) {
              rx = reader.readLine();
              sb.append(rx);
              complete = EcosMessage.isComplete(sb.toString());
            }

            if (!complete) {
              Logger.trace("No reply " + sb.toString() + " in " + (now - start) + " ms");
            }
            transferQueue.transfer(sb.toString());
          } else {
            //Logger.trace("RX Event: " + rx);

            StringBuilder sb = new StringBuilder();

            long now = System.currentTimeMillis();
            long start = now;
            long timeout = now + TIMEOUT;

            sb.append(rx);
            boolean complete = EcosMessage.isComplete(rx);

            while (!complete && now < timeout) {
              rx = reader.readLine();
              sb.append(rx);
              complete = EcosMessage.isComplete(sb.toString());
            }

            if (!complete) {
              Logger.trace("Event has no END tag " + sb.toString() + " in " + (now - start) + " ms");
            } else {
              EcosMessage emsg = new EcosMessage(sb.toString());
              Logger.trace("Complete: " + emsg.isResponseComplete() + "\n" + emsg.getMessage() + "\n" + emsg.getResponse());

              eventQueue.put(emsg);

            }

          }
        } catch (SocketException se) {
          Logger.error(se.getMessage());
          String msg = "Host " + ecosAddress.getHostName();
          DisconnectionEvent de = new DisconnectionEvent(msg);
          this.messageListener.onDisconnect(de);
          quit();
        } catch (IOException | InterruptedException ex) {
          Logger.error(ex);
        }
      }

      Logger.debug("Stop receiving");
      try {
        reader.close();
      } catch (IOException ex) {
        Logger.error(ex);
      }
      stop = true;
    }
  }

  @Override
  public InetAddress getControllerAddress() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
