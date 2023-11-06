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
import jcs.commandStation.dccex.events.DccExMessageListener;
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

  @Override
  public void setMessageListener(DccExMessageListener messageListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setMessageListener(messageListener);
    }
  }

  @Override
  public void sendMessage(String message) {
    try {
      writer.write(message);
      writer.flush();
      if(debug) {
        Logger.trace("TX:" +message);
      }

    } catch (IOException ex) {
      Logger.error(ex);
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
      Thread.currentThread().setName("DCC-EX-RX");

      Logger.trace("Started listening on port " + clientSocket.getLocalPort() + " ...");

      while (isRunning()) {
        try {
          String message = reader.readLine();
          this.messageListener.onMessage(message);
        } catch (SocketException se) {
          Logger.error(se.getMessage());
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

}
