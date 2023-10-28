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
package jcs.controller.dccex.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import jcs.controller.dccex.DccExConnection;
import jcs.controller.dccex.DccExMessage;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
class DccExTCPConnection implements DccExConnection {

  private final InetAddress dccExAddress;
  private Socket clientSocket;
  private Writer writer;
  private ClientMessageReceiver messageReceiver;

  private static final long SHORT_TIMEOUT = 1000L;

  private boolean debug = false;

  DccExTCPConnection(InetAddress csAddress) {
    dccExAddress = csAddress;
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    checkConnection();
  }

  private void checkConnection() {
    try {
      if (clientSocket == null || !clientSocket.isConnected()) {
        clientSocket = new Socket(dccExAddress, PORT);
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

  private class ResponseCallback {

    private final DccExMessage tx;

    ResponseCallback(final DccExMessage tx) {
      this.tx = tx;
    }

    public void addResponse(String rx) {
      this.tx.addResponse(rx);
    }

    public synchronized boolean isResponseReceived() {
      return tx.isResponseReceived();
    }
  }

  @Override
  public synchronized DccExMessage sendMessage(DccExMessage message) {
    ResponseCallback callback = new ResponseCallback(message);
    this.messageReceiver.setReplyCallback(callback);
    try {
      writer.write(message.getCommand());
      writer.flush();

    } catch (IOException ex) {
      Logger.error(ex);
    }
    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + SHORT_TIMEOUT;

    //Wait for the response, it looks like that my setup allways needs a little time before the full reply comes back
    //there is no guarantee that a response received directly after a command is sent ....
    pause(20);

    boolean responseComplete = callback.isResponseReceived();

    while (!responseComplete && now < timeout) {
      responseComplete = callback.isResponseReceived();
      now = System.currentTimeMillis();
    }

    if (debug) {
      if (responseComplete) {
        Logger.trace("Got Response in " + (now - start) + " ms");
      } else {
        Logger.trace("No Response for " + message + " in " + (now - start) + " ms");
      }
    }

    this.messageReceiver.releaseReplyCallback();
    return message;
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.warn(ex);
    }
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
    private ResponseCallback callBack;

    public ClientMessageReceiver(Socket socket) {
      try {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }

    void setReplyCallback(ResponseCallback callBack) {
      this.callBack = callBack;
    }

    void releaseReplyCallback() {
      this.callBack = null;
    }

    synchronized void quit() {
      this.quit = true;
    }

    synchronized boolean isRunning() {
      return !this.quit;
    }

    @Override
    public void run() {
      this.quit = false;
      Thread.currentThread().setName("DCC-EX-RX");

      Logger.trace("Started listening on port " + clientSocket.getLocalPort() + " ...");

      while (isRunning()) {
        try {
          String message = reader.readLine();
          if (this.callBack != null) {
            this.callBack.addResponse(message);
          } else {
            Logger.trace("->RX: " + message);
          }

        } catch (SocketException se) {
          Logger.error(se);
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

  
  //Only for initial testing ...
  public static void main(String[] a) throws UnknownHostException {
    InetAddress inetAddr = InetAddress.getByName("192.168.178.73");
    DccExTCPConnection c = new DccExTCPConnection(inetAddr);

    DccExMessage versionHarwareRequest = new DccExMessage("<s>");
    versionHarwareRequest = c.sendMessage(versionHarwareRequest);
    Logger.trace(versionHarwareRequest);

    DccExMessage currentRequest = new DccExMessage("<c>");
    currentRequest = c.sendMessage(currentRequest);
    Logger.trace(currentRequest);

    DccExMessage supportedCabs = new DccExMessage("<#>");
    supportedCabs = c.sendMessage(supportedCabs);
    Logger.trace(supportedCabs);

    DccExMessage trackConfig = new DccExMessage("<=>");
    trackConfig = c.sendMessage(trackConfig);
    Logger.trace(trackConfig);

  }

}
