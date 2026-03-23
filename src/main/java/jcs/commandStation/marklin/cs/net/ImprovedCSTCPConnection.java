/*
 * Copyright 2026 Frans Jacobs.
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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.marklin.cs.can.CanMessage;
import org.tinylog.Logger;

/**
 * Improved TCP Connection with the Central Station
 *
 * Key improvements:<br>
 * - Proper multi-packet response handling<br>
 * - Multiple concurrent requests support<br>
 * - Better timeout handling with CountDownLatch<br>
 * - Cleaner separation of concerns
 */
class ImprovedCSTCPConnection implements CSConnection {

  private final InetAddress centralStationAddress;

  private Socket clientSocket;
  private DataOutputStream dos;
  private final Object writeLock = new Object();

  private ClientMessageReceiver messageReceiver;
  private final List<ConnectionEventListener> disconnectionEventListeners;

  private static final long SHORT_TIMEOUT = 1000L;
  private static final long LONG_TIMEOUT = 5000L;

  private final boolean debug;
  private final BlockingQueue<CanMessage> eventQueue;

  // Map to track pending requests by their unique ID
  private final Map<Integer, PendingRequest> pendingRequests;
  private final AtomicInteger requestIdGenerator;

  ImprovedCSTCPConnection(InetAddress csAddress) {
    centralStationAddress = csAddress;
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    eventQueue = new LinkedBlockingQueue<>();
    pendingRequests = new ConcurrentHashMap<>();
    requestIdGenerator = new AtomicInteger(0);
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
  public BlockingQueue<CanMessage> getEventQueue() {
    return this.eventQueue;
  }

  @Override
  public void addDisconnectionEventListener(ConnectionEventListener listener) {
    this.disconnectionEventListeners.add(listener);
  }

  /**
   * Improved request/response tracking with proper multi-packet support
   */
  private static class PendingRequest {

    private final int requestId;
    private final CanMessage txMessage;
    private final CountDownLatch responseLatch;
    private final long timeoutMs;
    private final long createdAt;

    // Track expected response packets
    private volatile int expectedPackets = -1; // -1 = unknown, 0 = done, >0 = packets remaining
    private volatile boolean completed = false;

    PendingRequest(int requestId, CanMessage txMessage, long timeoutMs) {
      this.requestId = requestId;
      this.txMessage = txMessage;
      this.timeoutMs = timeoutMs;
      this.createdAt = System.currentTimeMillis();
      this.responseLatch = new CountDownLatch(1);
    }

    /**
     * Check if this request matches the incoming response command
     */
    boolean matchesResponse(int responseCommand) {
      int txCmd = txMessage.getCommand();

      // Special case for CONFIG_DATA requests
      if (CanMessage.REQUEST_CONFIG_DATA == txCmd) {
        return txCmd == (responseCommand - 1) || txCmd == (responseCommand - 2);
      } else {
        return txCmd == (responseCommand - 1);
      }
    }

    /**
     * Add a response packet
     *
     * @param rx The response message
     * @param remainingBytes Bytes still available in the stream (for multi-packet detection)
     * @return true if response is now complete
     */
    boolean addResponse(CanMessage rx, int remainingBytes) {
      txMessage.addResponse(rx);

      // Determine if more packets are expected
      // This is a heuristic - adjust based on your protocol specifics
      if (expectedPackets == -1) {
        // First packet - try to determine total expected
        expectedPackets = estimateRemainingPackets(rx, remainingBytes);
      } else if (expectedPackets > 0) {
        expectedPackets--;
      }

      // Check if we're done
      if (txMessage.isResponseComplete() && expectedPackets == 0) {
        markComplete();
        return true;
      }

      return false;
    }

    /**
     * Estimate remaining packets based on protocol knowledge This is protocol-specific - adjust for your needs
     */
    private int estimateRemainingPackets(CanMessage firstPacket, int remainingBytes) {
      // For multi-packet responses, the protocol should indicate packet count
      // or we can estimate from remaining bytes

      // Heuristic: each CAN message is 13 bytes (4 ID + 1 DLC + 8 data)
      int estimatedPackets = remainingBytes / 13;

      // Check if the message itself indicates more packets
      // (This depends on your specific protocol - adjust as needed)
      if (firstPacket.getDlc() > 0) {
        // Some protocols put packet count in first data byte
        // Adjust this based on actual protocol
      }

      return estimatedPackets;
    }

    void markComplete() {
      if (!completed) {
        completed = true;
        responseLatch.countDown();
      }
    }

    boolean isComplete() {
      return completed;
    }

    boolean await() throws InterruptedException {
      return responseLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    boolean isTimedOut() {
      return System.currentTimeMillis() - createdAt > timeoutMs;
    }

    CanMessage getMessage() {
      return txMessage;
    }
  }

  @Override
  public CanMessage sendCanMessage(CanMessage message) {
    if (message == null) {
      Logger.warn("Message is NULL?");
      return null;
    }

    PendingRequest request = null;
    int requestId = -1;

    try {
      // Determine timeout
      long timeout;
      if (message.expectsLongResponse()) {
        timeout = LONG_TIMEOUT;
      } else if (message.expectsResponse()) {
        timeout = SHORT_TIMEOUT;
      } else {
        timeout = 0;
      }

      // Register pending request if expecting response
      if (timeout > 0) {
        requestId = requestIdGenerator.incrementAndGet();
        request = new PendingRequest(requestId, message, timeout);
        pendingRequests.put(requestId, request);
      }

      // Send message (only lock the write operation, not the whole method)
      synchronized (writeLock) {
        try {
          byte[] bytes = message.getMessage();
          dos.write(bytes);
          dos.flush();
        } catch (IOException ex) {
          Logger.error("Failed to send message: " + ex.getMessage());
          if (request != null) {
            request.markComplete(); // Release waiting thread
          }
          return message;
        }
      }

      // Log transmission (except frequent ping responses)
      if (CanMessage.PING_RESP != message.getCommand() && debug) {
        Logger.trace("TX: " + message);
      }

      // Wait for response if needed
      if (request != null) {
        waitForResponse(request);
      }

      // Log response
      Logger.trace("#TX: " + message + (message.isResponseMessage() ? " response msg" : ""));
      if (!message.isResponseMessage()) {
        if (message.getResponses().size() > 1) {
          List<CanMessage> responses = message.getResponses();
          for (int i = 0; i < responses.size(); i++) {
            Logger.trace("#RX " + i + ": " + message.getResponse(i));
          }
        } else if (message.getResponse() != null) {
          Logger.trace("#RX: " + message.getResponse());
        }
      }

    } finally {
      // Clean up pending request
      if (requestId >= 0) {
        pendingRequests.remove(requestId);
      }
    }

    return message;
  }

  private void waitForResponse(PendingRequest request) {
    long start = System.currentTimeMillis();

    try {
      // Wait with timeout using CountDownLatch (more efficient than sleep polling)
      boolean completed = request.await();

      long elapsed = System.currentTimeMillis() - start;

      if (debug) {
        if (completed) {
          Logger.trace("Got Response in " + elapsed + " ms");
        } else {
          Logger.trace("No Response for " + request.getMessage() + " in " + elapsed + " ms (timeout)");
        }
      }

    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      Logger.warn("Interrupted while waiting for response");
    }
  }

  @Override
  public void close() throws Exception {
    if (messageReceiver != null) {
      messageReceiver.quit();
      messageReceiver.join();
    }

    // Cancel all pending requests
    for (PendingRequest request : pendingRequests.values()) {
      request.markComplete();
    }
    pendingRequests.clear();

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

  @Override
  public InetAddress getControllerAddress() {
    return centralStationAddress;
  }

  @Override
  public boolean isConnected() {
    return messageReceiver != null && messageReceiver.isRunning();
  }

  /**
   * Improved message receiver with better response routing
   */
  private class ClientMessageReceiver extends Thread {

    private volatile boolean running = false;
    private DataInputStream din;

    public ClientMessageReceiver(Socket socket) {
      super("CS-CAN-RX");
      try {
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
        din = new DataInputStream(bis);
      } catch (IOException ex) {
        Logger.error("Failed to create input stream", ex);
      }
    }

    void quit() {
      running = false;
      interrupt(); // Wake up from blocking read
    }

    boolean isRunning() {
      return running;
    }

    @Override
    public void run() {
      running = true;
      Logger.trace("Started listening on port " + clientSocket.getLocalPort() + "...");

      while (running) {
        try {
          CanMessage rx = readCanMessage();

          if (rx != null) {
            routeMessage(rx);
          }

        } catch (SocketException se) {
          if (running) { // Only notify if not intentionally closed
            handleDisconnection();
          }
          quit();

        } catch (IOException ex) {
          if (running) {
            Logger.error("Error reading CAN message", ex);
          }
        }
      }

      Logger.debug("Stop receiving");
      cleanup();
    }

    /**
     * Read a single CAN message from the stream
     */
    private CanMessage readCanMessage() throws IOException {
      // Read CAN message header (5 bytes)
      int prio = din.readUnsignedByte();
      int cmd = din.readUnsignedByte();
      int hash = din.readUnsignedShort();
      int dlc = din.readUnsignedByte();

      // Read data bytes (always 8 bytes, even if DLC < 8)
      byte[] data = new byte[CanMessage.DATA_SIZE];
      din.readFully(data);

      return new CanMessage(prio, cmd, hash, dlc, data);
    }

    /**
     * Route message to either a pending request or the event queue
     */
    private void routeMessage(CanMessage rx) {
      int cmd = rx.getCommand();
      boolean routed = false;

      // Try to match with pending requests
      for (PendingRequest request : pendingRequests.values()) {
        if (request.matchesResponse(cmd)) {
          // This is a response to a pending request
          try {
            int remainingBytes = din.available();
            boolean complete = request.addResponse(rx, remainingBytes);

            if (debug) {
              Logger.trace("RX (response): " + rx + " remaining=" + remainingBytes + " complete=" + complete);
            }

            routed = true;
            break; // Only route to first matching request

          } catch (IOException ex) {
            Logger.error("Error checking available bytes", ex);
            request.markComplete(); // Mark as complete on error
          }
        }
      }

      // If not routed to a pending request, it's an unsolicited event
      if (!routed) {
        eventQueue.offer(rx);

        if (debug) {
          Logger.trace("RX (event): " + rx + " QueueSize: " + eventQueue.size());
        }
      }
    }

    private void handleDisconnection() {
      String msg = "Host " + centralStationAddress.getHostName();
      ConnectionEvent de = new ConnectionEvent(msg, false, false);
      for (ConnectionEventListener listener : disconnectionEventListeners) {
        listener.onConnectionChange(de);
      }
    }

    private void cleanup() {
      try {
        if (din != null) {
          din.close();
        }
      } catch (IOException ex) {
        Logger.error("Error closing input stream", ex);
      }
    }
  }
}
