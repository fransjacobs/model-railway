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
import java.nio.charset.StandardCharsets;
import java.net.SocketTimeoutException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.commandStation.esu.ecos.EcosMessageFactory;
import jcs.commandStation.events.ConnectionEvent;
import org.tinylog.Logger;

/**
 *
 * TCP Connection to the ESU ECoS
 */
class EcosTCPConnection implements EcosConnection {

  private final InetAddress ecosAddress;
  private volatile Socket clientSocket;
  private Writer writer;

  // Carries completed REPLY messages from the receiver thread to sendMessage().
  private final TransferQueue<String> replyQueue;

  // Carries completed EVENT messages to the application event consumer.
  private final BlockingQueue<EcosMessage> eventQueue;

  private volatile ClientMessageReceiver messageReceiver;
  private static final boolean DEBUG = Boolean.getBoolean("message.debug");
  private static final long TIMEOUT_MS = Long.getLong("ecos.reply.timeout.ms", 1500L);

  private static final long HEARTBEAT_INTERVAL_MS = Long.getLong("ecos.heartbeat.interval.ms", 5000L);

  private static final int MAX_MISSED_HEARTBEATS = Integer.getInteger("ecos.heartbeat.max.missed", 2);

  private volatile ScheduledFuture<?> heartbeatTask;
  private volatile int missedHeartbeats;
  private final AtomicBoolean disconnectNotified = new AtomicBoolean(false);
  private final Consumer<EcosConnection> disconnectCallback;

  private final ScheduledExecutorService heartbeatExecutor
          = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "ECoS-HEARTBEAT");
            thread.setDaemon(true);
            return thread;
          });

  EcosTCPConnection(InetAddress address, Consumer<EcosConnection> disconnectCallback) {
    ecosAddress = address;
    this.disconnectCallback = disconnectCallback;
    replyQueue = new LinkedTransferQueue<>();
    eventQueue = new LinkedBlockingQueue<>();
    checkConnection();
  }

  private synchronized void startHeartbeat() {
    if (heartbeatTask != null && !heartbeatTask.isCancelled() && !heartbeatTask.isDone()) {
      return;
    }

    heartbeatTask = heartbeatExecutor.scheduleWithFixedDelay(
            this::heartbeat,
            HEARTBEAT_INTERVAL_MS,
            HEARTBEAT_INTERVAL_MS,
            TimeUnit.MILLISECONDS
    );
  }

  private void checkConnection() {
    Socket socket = null;
    try {
      if (clientSocket == null || !clientSocket.isConnected()
              || (messageReceiver != null && !messageReceiver.isRunning())) {

        socket = new Socket();
        socket.connect(new InetSocketAddress(ecosAddress, DEFAULT_NETWORK_PORT), DEFAULT_CONNECT_TIMEOUT_MS);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(1000); // Bounded read during partial-frame assembly; idle timeouts are ignored.

        clientSocket = socket;
        writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));

        messageReceiver = new ClientMessageReceiver(clientSocket);
        messageReceiver.start();

        disconnectNotified.set(false);
        missedHeartbeats = 0;
        startHeartbeat();

      }
    } catch (IOException ex) {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException closeEx) {
          Logger.trace("Could not close failed socket: {}", closeEx.getMessage());
        }
      }

      clientSocket = null;
      writer = null;
      messageReceiver = null;
      Logger.error("Can't (re)connect with ESU Ecos " + ecosAddress.getHostAddress() + ". Cause: " + ex.getMessage());
      //Logger.trace(ex);
    }
  }

  private void disconnect() {
    if (heartbeatTask != null) {
      heartbeatTask.cancel(true);
      heartbeatTask = null;
    }

    ClientMessageReceiver receiver = messageReceiver;
    if (receiver != null) {
      receiver.quit();
    }
    // Closing the socket before joining releases any blocked readLine().
    Socket socket = clientSocket;
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException ex) {
        Logger.trace("Error closing ECoS socket: {}", ex.getMessage());
      }
    }
    if (receiver != null && receiver != Thread.currentThread()) {
      try {
        receiver.join(TIMEOUT_MS * 4);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
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

    clientSocket = null;
    messageReceiver = null;
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
      replyQueue.clear();
      Logger.trace("TX: {}",message);

      writeCommand(writer, message);

      if (DEBUG) {
        Logger.trace("TX: " + message.getMessage());
      }

      long start = System.currentTimeMillis();

      // If no reply arrives in time, reply is null and we log it and move on.
      String reply = null;
      long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(TIMEOUT_MS);
      while (reply == null) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
          break;
        }
        String candidate = replyQueue.poll(remaining, TimeUnit.NANOSECONDS);
        if (candidate == null) {
          break;
        }
        // A timed-out previous request may reply late. Never accept its
        // response as confirmation of the current command.
        if (isReplyFor(candidate, message.getMessage())) {
          reply = candidate;
        } else {
          Logger.warn("Discarding stale ECoS reply: {}", candidate.strip());
        }
      }
      if (reply == null) {
        Logger.warn("No matching reply received for [{}] within {} ms", message.getMessage().trim(), TIMEOUT_MS);
      } else {
        message.addResponse(reply);
       
        Logger.trace("RX: {}",reply);
        
        if (DEBUG) {
          Logger.trace("Reply in {} ms, complete={}",
                  System.currentTimeMillis() - start, message.isResponseComplete());
        }
      }
    } catch (IOException ex) {
      Logger.error("I/O error sending message: {}", ex.getMessage());
      markDisconnected("I/O error sending message: " + ex.getMessage());

      //notifyDisconnect();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      Logger.error("Interrupted while waiting for reply: {}", ex.getMessage());
      markDisconnected("Interrupted while waiting for reply: " + ex.getMessage());

      //notifyDisconnect();
    }

   
    
    return message;
  }

  static void writeCommand(Writer writer, EcosMessage message) throws IOException {
    writer.write(message.getMessage());
    writer.write('\n'); // ECoS requires LF-terminated commands.
    writer.flush();
  }

  /**
   * Match the echoed request, allowing for whitespace differences that ECoS can introduce in the REPLY header.
   */
  static boolean isReplyFor(String reply, String command) {
    if (reply == null || command == null || !reply.startsWith(EcosMessage.REPLY)) {
      return false;
    }
    int end = reply.indexOf('>');
    if (end < 0) {
      return false;
    }
    String echoed = reply.substring(EcosMessage.REPLY.length(), end)
            .replaceAll("\\s+", "");
    return echoed.equals(command.strip().replaceAll("\\s+", ""));
  }

  private void markDisconnected(String reason) {
    if (!disconnectNotified.compareAndSet(false, true)) {
      return;
    }

    Logger.warn("ECoS connection lost: {}", reason);

    ClientMessageReceiver receiver = messageReceiver;
    if (receiver != null) {
      String host = ecosAddress.getHostName();
      receiver.notifyDisconnect(new ConnectionEvent(host, false, false));
      receiver.quit();
    }

    disconnect();

    if (disconnectCallback != null) {
      disconnectCallback.accept(this);
    }
  }

  @Override
  public void close() {
    disconnect();
    heartbeatExecutor.shutdownNow();
  }

  @Override
  public boolean isConnected() {
    Socket socket = clientSocket;
    return socket != null
            && socket.isConnected()
            && !socket.isClosed()
            && !socket.isInputShutdown()
            && !socket.isOutputShutdown()
            && messageReceiver != null
            && messageReceiver.isRunning()
            && !disconnectNotified.get();
  }

  @Override
  public boolean isVirtual() {
    return false;
  }

  @Override
  public BlockingQueue<EcosMessage> getEventQueue() {
    return eventQueue;
  }

  private void heartbeat() {
    if (!isConnected()) {
      markDisconnected("Heartbeat found connection not connected");
      return;
    }

    try {
      EcosMessage heartbeat = EcosMessageFactory.getPowerStatus();
      EcosMessage reply = sendMessage(heartbeat);

      if (reply == null || !reply.isResponseComplete()) {
        int missed = ++missedHeartbeats;
        Logger.warn("ECoS heartbeat missed {}/{}", missed, MAX_MISSED_HEARTBEATS);

        if (missed >= MAX_MISSED_HEARTBEATS) {
          markDisconnected("ECoS heartbeat failed");
        }
      } else {
        missedHeartbeats = 0;
      }

    } catch (Exception ex) {
      int missed = ++missedHeartbeats;
      Logger.warn("ECoS heartbeat error {}/{}: {}", missed, MAX_MISSED_HEARTBEATS, ex.getMessage());

      if (missed >= MAX_MISSED_HEARTBEATS) {
        markDisconnected("ECoS heartbeat exception: " + ex.getMessage());
      }
    }
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
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
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
              markDisconnected("ECoS stream reached EOF");
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
        } catch (SocketTimeoutException timeout) {
          // Normal idle read; the heartbeat probes liveness independently.
        } catch (SocketException se) {
          if (running) {
            Logger.error("Socket error: {}", se.getMessage());
            markDisconnected("Socket error: " + se.getMessage());
          }
        } catch (IOException ex) {
          if (running) {
//            Logger.error("I/O error in receiver: " + ex.getMessage());
//            Logger.trace(ex);
            Logger.warn("Stream closed unexpectedly — disconnecting.");
            markDisconnected("Stream closed unexpectedly");
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

        String rx;
        try {
          rx = reader.readLine();
        } catch (SocketTimeoutException timeout) {
          continue;
        }
        if (rx == null) {
          markDisconnected("ECoS stream closed mid-message");
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
