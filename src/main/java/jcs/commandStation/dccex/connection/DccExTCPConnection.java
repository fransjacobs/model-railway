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
package jcs.commandStation.dccex.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import jcs.commandStation.dccex.DccExConnection;
import jcs.commandStation.dccex.DccExMessage;
import jcs.commandStation.dccex.DccExMessageFactory;
import jcs.commandStation.events.DisconnectionEvent;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
class DccExTCPConnection implements DccExConnection {

  private final InetAddress dccExAddress;
  private Socket clientSocket;
  private Writer writer;
  private ResponseCallback responseCallback;

  private ClientMessageReceiver messageReceiver;

  private boolean debug = false;
  private static final long TIMEOUT = 3000L;

  DccExTCPConnection(InetAddress csAddress) {
    dccExAddress = csAddress;
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    checkConnection();
  }

  private void checkConnection() {
    try {
      if (clientSocket == null || !clientSocket.isConnected()) {
        clientSocket = new Socket(dccExAddress, DEFAULT_NETWORK_PORT);
        clientSocket.setKeepAlive(true);
        clientSocket.setTcpNoDelay(true);
        writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        messageReceiver = new ClientMessageReceiver(clientSocket);
        messageReceiver.setDaemon(true);
        messageReceiver.start();
      }
    } catch (IOException ex) {
      this.clientSocket = null;
      Logger.error("Can't (re)connect with DCC-EX " + dccExAddress.getHostAddress() + ". Cause: " + ex.getMessage());
      Logger.trace(ex);
    }
  }

  private void disconnect() {
    this.messageReceiver.quit();
    //wait until the messageReceiver has shut down
    pause(50);

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
  public void setMessageListener(DccExMessageListener messageListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setMessageListener(messageListener);
    }
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.trace(e.getMessage());
    }
  }

  @Override
  public synchronized String sendMessage(String message) {
    String response = message;
    String rxOpcode = DccExMessageFactory.getResponseOpcodeFor(message);
    if (rxOpcode != null) {
      this.responseCallback = new ResponseCallback(message);
    }

    try {
      writer.write(message);
      writer.flush();
      if (debug) {
        Logger.trace("TX:" + message);
      }

    } catch (IOException ex) {
      Logger.error(ex);
      String msg = "Host " + dccExAddress.getHostName();
      DisconnectionEvent de = new DisconnectionEvent(msg);

      messageReceiver.messageListener.onDisconnect(de);
      messageReceiver.quit();
    }
    if (responseCallback != null) {
      long now = System.currentTimeMillis();
      long start = now;
      long timeout = now + TIMEOUT;

      //Wait for the response
      boolean responseComplete = responseCallback.isResponseComplete();
      while (!responseComplete && now < timeout) {
        pause(10);
        responseComplete = responseCallback.isResponseComplete();
        now = System.currentTimeMillis();
      }

      response = responseCallback.getResponse();
      if (debug) {
        if (responseComplete) {
          Logger.trace("Got Response in " + (now - start) + " ms");
        } else {
          Logger.trace("No Response for " + message + " in " + (now - start) + " ms");
        }
      }
    }

    responseCallback = null;
    return response;
  }

  @Override
  public void close() throws Exception {
    disconnect();
  }

  @Override
  public boolean isConnected() {
    return this.messageReceiver != null && this.messageReceiver.isRunning();
  }

  private class ClientMessageReceiver extends Thread {

    private boolean quit = true;
    private BufferedReader reader;
    private DccExMessageListener messageListener;

    public ClientMessageReceiver(Socket socket) {
      try {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }

    synchronized void quit() {
      this.quit = true;
    }

    synchronized boolean isRunning() {
      return !this.quit;
    }

    void setMessageListener(DccExMessageListener messageListener) {
      this.messageListener = messageListener;
    }

    @Override
    public void run() {
      this.quit = false;
      this.setName("DCC-EX-RX");

      Logger.trace("Started listening on port " + clientSocket.getLocalPort() + " ...");

      while (isRunning()) {
        try {
          String message = reader.readLine();

          if (responseCallback != null && responseCallback.isSubscribedfor(message)) {
            //a "synchroneous" response
            responseCallback.setResponse(message);
          } else {
            //a "asynchroneous" response
            DccExMessage msg = new DccExMessage(message);
            this.messageListener.onMessage(msg);
          }
        } catch (SocketException se) {
          Logger.error(se.getMessage());
          String msg = "Host " + dccExAddress.getHostName();
          DisconnectionEvent de = new DisconnectionEvent(msg);
          this.messageListener.onDisconnect(de);
          quit();
        } catch (IOException ioe) {
          Logger.error(ioe);
        }
      }

      Logger.debug("Stop receiving");
      try {
        reader.close();
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }
  }

  private class ResponseCallback {

    private final String tx;
    private final String rxOpcode;
    private String rx;

    ResponseCallback(final String tx) {
      this.tx = tx;
      this.rxOpcode = DccExMessageFactory.getResponseOpcodeFor(tx);
      Logger.trace("Expected response opcode: " + this.rxOpcode);
    }

    boolean isSubscribedfor(final String response) {
      String rsp = response.replaceAll("\n", "").replaceAll("\r", "");
      String opcode = rsp.substring(1, 2);
      return opcode.equals(rxOpcode);
    }

    void setResponse(String response) {
      this.rx = response.replaceAll("\n", "").replaceAll("\r", "");
    }

    String getResponse() {
      if (this.rx != null) {
        return this.rx;
      } else {
        return tx;
      }
    }

    boolean isResponseComplete() {
      return rx != null && !rx.isBlank() && rx.startsWith("<") && rx.endsWith(">");
    }
  }

}
