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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.commandStation.events.ConnectionEvent;
import org.tinylog.Logger;

/**
 *
 * TCP Connection to the ESU ECoS
 */
class EcosTCPConnection implements EcosConnection {

  private final InetAddress ecosAddress;
  private Socket clientSocket;
  private Writer writer;

  // Carries completed REPLY messages from the receiver thread to sendMessage().
  private final TransferQueue<String> replyQueue;

  // Carries completed EVENT messages to the application event consumer.
  private final BlockingQueue<EcosMessage> eventQueue;

  private ClientMessageReceiver messageReceiver;
  private static final boolean DEBUG = Boolean.getBoolean("message.debug");
  private static final long TIMEOUT_MS = 500L;

  EcosTCPConnection(InetAddress address) {
    ecosAddress = address;
    replyQueue = new LinkedTransferQueue<>();
    eventQueue = new LinkedBlockingQueue<>();
    checkConnection();
  }

  private void checkConnection() {
    try {
      if (clientSocket == null || !clientSocket.isConnected()
              || (messageReceiver != null && !messageReceiver.isRunning())) {

        clientSocket = new Socket(ecosAddress, DEFAULT_NETWORK_PORT);
        clientSocket.setKeepAlive(true);
        clientSocket.setTcpNoDelay(true);
        writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        messageReceiver = new ClientMessageReceiver(clientSocket);
        messageReceiver.start();
      }
    } catch (IOException ex) {
      clientSocket = null;
      writer = null;
      messageReceiver = null;
      Logger.error("Can't (re)connect with ESU Ecos " + ecosAddress.getHostAddress() + ". Cause: " + ex.getMessage());
      //Logger.trace(ex);
    }
  }

  private void disconnect() {
    if (messageReceiver == null) {
      return;
    }
    messageReceiver.quit();

    try {
      messageReceiver.join(TIMEOUT_MS * 4);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      Logger.warn("Interrupted while waiting for receiver thread to stop.");
    }

    if (writer != null) {
      try {
        writer.close();
      } catch (IOException ex) {
        Logger.error("Can't close output. Cause: " + ex.getMessage());
        Logger.trace(ex);
      }
      writer = null;
    }

    if (clientSocket != null) {
      try {
        clientSocket.close();
      } catch (IOException ex) {
        Logger.error("Can't close socket. Cause: " + ex.getMessage());
        Logger.trace(ex);
      }
      clientSocket = null;
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
    if (writer == null || messageReceiver == null) {
      Logger.error("Cannot send message — not connected.");
      return message;
    }

    try {
      writer.write(message.getMessage());
      writer.flush();

      if (DEBUG) {
        Logger.trace("TX: " + message.getMessage());
      }

      long start = System.currentTimeMillis();

      // If no reply arrives in time, reply is null and we log it and move on.
      String reply = replyQueue.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);

      if (reply == null) {
        Logger.warn("No reply received for [" + message.getMessage().trim() + "] within " + TIMEOUT_MS + " ms");
      } else {
        message.addResponse(reply);
        if (DEBUG) {
          long elapsed = System.currentTimeMillis() - start;
          if (message.isResponseComplete()) {
            Logger.trace("Reply in " + elapsed + " ms");
          } else {
            Logger.trace("Incomplete reply for [" + message.getMessage().trim() + "] in " + elapsed + " ms");
          }
        }
      }
    } catch (IOException ex) {
      Logger.error("I/O error sending message: " + ex.getMessage());
      notifyDisconnect();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      Logger.error("Interrupted while waiting for reply: " + ex.getMessage());
      notifyDisconnect();
    }

    return message;
  }

  /**
   * Notifies the registered listener of a disconnection and shuts down the receiver.
   */
  private void notifyDisconnect() {
    if (messageReceiver != null) {
      String host = ecosAddress.getHostName();
      messageReceiver.notifyDisconnect(new ConnectionEvent(host, false, false));
      messageReceiver.quit();
    }
  }

  @Override
  public void close() {
    disconnect();
  }

  @Override
  public boolean isConnected() {
    return messageReceiver != null && messageReceiver.isRunning();
  }

  @Override
  public BlockingQueue<EcosMessage> getEventQueue() {
    return eventQueue;
  }

  /**
   * Listen to replies or events from the ECoS
   */
  private class ClientMessageReceiver extends Thread {

    private volatile boolean running = false;

    private BufferedReader reader;
    private EcosMessageListener messageListener;

    ClientMessageReceiver(Socket socket) {
      super("ECoS-RX");
      try {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } catch (IOException ex) {
        Logger.error("Failed to open socket input stream: " + ex.getMessage());
        Logger.trace(ex);
      }
    }

    void setMessageListener(EcosMessageListener messageListener) {
      this.messageListener = messageListener;
    }

    /**
     * Signals the run loop to stop and interrupts any blocking I/O.
     */
    synchronized void quit() {
      running = false;
      interrupt();
      try {
        if (clientSocket != null && !clientSocket.isClosed()) {
          clientSocket.shutdownInput();
        }
        if (reader != null) {
          reader.close();
        }
      } catch (IOException ex) {
        Logger.error("Error shutting down receiver input: " + ex.getMessage());
        Logger.trace(ex);
      }
    }

    boolean isRunning() {
      return running;
    }

    /**
     * Routes a disconnect event to the registered listener.
     */
    void notifyDisconnect(ConnectionEvent event) {
      if (messageListener != null) {
        messageListener.onDisconnect(event);
      }
    }

    @Override
    public void run() {
      running = true;
      Logger.trace("Started listening on port " + clientSocket.getLocalPort() + " ...");

      while (running) {
        try {
          String rx = reader.readLine();

          if (rx == null) {
            // null from readLine() means the stream was closed (EOF / shutdownInput).
            if (running) {
              Logger.warn("Stream closed unexpectedly — disconnecting.");
              notifyDisconnect(new ConnectionEvent(ecosAddress.getHostName(), false, false));
              quit();
            }
            break;
          }

          if (DEBUG) {
            Logger.trace("RX->" + rx);
          }

          if (rx.startsWith(EcosMessage.REPLY)) {
            // --- Synchronous reply to a command sent by this client ---
            String assembled = assembleMessage(rx);
            if (assembled != null) {
              boolean accepted = replyQueue.offer(assembled, TIMEOUT_MS, TimeUnit.MILLISECONDS);
              if (!accepted) {
                Logger.warn("Reply queue not consumed within timeout — discarding orphaned reply.");
              }
            }
          } else if (rx.startsWith(EcosMessage.EVENT)) {
            // --- Unsolicited event from the ECoS ---
            String assembled = assembleMessage(rx);
            if (assembled != null) {
              EcosMessage emsg = new EcosMessage(assembled);
              if (DEBUG) {
                Logger.trace("EVENT complete=" + emsg.isResponseComplete()
                        + (emsg.getMessage() != null ? " -> " + emsg.getMessage() : "")
                        + " -> " + emsg.getResponse());
              }

              boolean queued = eventQueue.offer(emsg, TIMEOUT_MS, TimeUnit.MILLISECONDS);
              if (!queued) {
                Logger.warn("Event queue full — dropping event: " + emsg.getResponse());
              }
            }
          } else {
            Logger.trace("Ignoring unrecognised line: " + rx);
          }
        } catch (SocketException se) {
          if (running) {
            Logger.error("Socket error: " + se.getMessage());
            notifyDisconnect(new ConnectionEvent(ecosAddress.getHostName(), false, false));
            quit();
          }
        } catch (IOException ex) {
          if (running) {
            Logger.error("I/O error in receiver: " + ex.getMessage());
            Logger.trace(ex);
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }

      closeReader();
      Logger.debug("Receiver stopped.");
    }

    /**
     * Reads lines from the socket until the message is complete (contains {@code <END}), reassembling them with newlines preserved as the protocol requires.
     *
     * @param firstLine the first line already read from the socket
     * @return the fully assembled message string, or null if timed out / running
     */
    private String assembleMessage(String firstLine) throws IOException, InterruptedException {
      StringBuilder sb = new StringBuilder();
      sb.append(firstLine).append("\n");

      boolean complete = EcosMessage.isComplete(firstLine);
      long deadline = System.currentTimeMillis() + TIMEOUT_MS;

      while (!complete && running) {
        long now = System.currentTimeMillis();
        if (now >= deadline) {
          Logger.warn("Timeout assembling message, partial: " + sb.toString().trim());
          return null;
        }

        String rx = reader.readLine();

        if (rx == null) {
          // Stream closed mid-message.
          return null;
        }

        if (DEBUG) {
          Logger.trace("RX: " + rx);
        }

        sb.append(rx).append("\n");
        complete = EcosMessage.isComplete(sb.toString());
      }

      if (!complete) {
        return null;  // running was set before message completed
      }

      return sb.toString();
    }

    private void closeReader() {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
          Logger.error("Error closing reader: " + ex.getMessage());
          Logger.trace(ex);
        }
      }
    }
  }

  @Override
  public InetAddress getControllerAddress() {
    return ecosAddress;
  }

}
