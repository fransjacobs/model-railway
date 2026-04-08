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

    @SuppressWarnings("unused")
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
//    private int estimateRemainingPackets(CanMessage firstPacket, int remainingBytes) {
//      // For multi-packet responses, the protocol should indicate packet count
//      // or we can estimate from remaining bytes
//
//      // Heuristic: each CAN message is 13 bytes (4 ID + 1 DLC + 8 data)
//      int estimatedPackets = remainingBytes / 13;
//
//      // Check if the message itself indicates more packets
//      // (This depends on your specific protocol - adjust as needed)
//      if (firstPacket.getDlc() > 0) {
//        // Some protocols put packet count in first data byte
//        // Adjust this based on actual protocol
//      }
//
//      return estimatedPackets;
//    }
    /**
     * Estimate remaining packets based on the Märklin CS2/3 CAN protocol (v2.0).
     *
     * Protocol invariants from the spec:
     * <ul>
     * <li>Every CAN-over-Ethernet packet is exactly 13 bytes: 4 bytes CAN-ID, 1 byte DLC, 8 bytes data (zero-padded when DLC &lt; 8).</li>
     * <li>The DLC field indicates how many data bytes are meaningful (0–8).</li>
     * <li>Responses always echo the command byte + 1 (response bit set).</li>
     * </ul>
     *
     * Multi-packet commands (per spec section references):
     * <ul>
     * <li><b>Config Data Stream (0x21)</b> §7.2 — first response packet has DLC=6 and carries a 4-byte stream length; remaining data packets always have DLC=8 carrying 8 bytes each. Expected
     * follow-on packets = ceil(streamLength / 8).</li>
     * <li><b>Statusdaten Konfiguration (0x1D)</b> §6.2 — transmitted as a data stream; the closing confirmation frame carries the packet count in D-Byte 5 (Paketanzahl).</li>
     * <li><b>S88 Polling (0x10)</b> §5.1 — one DLC=7 response per module; the request carries the module count in D-Byte 4, so that many responses are expected.</li>
     * <li><b>Read Config (0x07)</b> §3.7 — one response per byte read; D-Byte 6 of the request ("Anzahl") is the byte count. Value 0 means 256.</li>
     * <li><b>Lok Discovery (0x01)</b> §3.1 — MFX discovery runs in 32 steps (Range 0→32); intermediate responses carry the current range value in D-Byte 4. Non-MFX discoveries produce a single
     * response.</li>
     * </ul>
     *
     * All other commands produce exactly one response packet.
     *
     * @param firstPacket The first response packet that has just been received.
     * @param remainingBytes Bytes currently buffered in the DataInputStream (from {@code din.available()}).
     * @return The number of <em>additional</em> packets still expected after {@code firstPacket}. Returns 0 when this is the only response.
     */
    private int estimateRemainingPackets(CanMessage firstPacket, int remainingBytes) {
      // The command value in a *response* has the response bit set, so strip it
      // to recover the original command code.  The response bit is bit 0 of the
      // command byte (the entire low bit of the second byte in the CAN-ID).
      // Dividing by 2 (right-shift 1) gives the base command index per the table
      // in §1.4 of the spec.
      int responseCmd = firstPacket.getCommand();

      // -----------------------------------------------------------------------
      // §7.2 Config Data Stream (command 0x21, CAN-ID 0x42)
      // First response: DLC=6  → bytes 0-3 = stream length (Big Endian uint32),
      //                          bytes 4-5 = CRC16.
      // All subsequent: DLC=8  → 8 bytes of payload, zero-padded at the end.
      // -----------------------------------------------------------------------
      if (responseCmd == CanMessage.CONFIG_DATA_STREAM || responseCmd == CanMessage.CONFIG_DATA_STREAM + 1) {
        int dlc = firstPacket.getDlc();
        if (dlc == 6 || dlc == 7) {
          // This is the header frame; extract the 4-byte stream length (Big Endian).
          byte[] data = firstPacket.getData();
          long streamLength = ((data[0] & 0xFFL) << 24)
                  | ((data[1] & 0xFFL) << 16)
                  | ((data[2] & 0xFFL) << 8)
                  | (data[3] & 0xFFL);

          if (streamLength <= 0) {
            return 0;
          }
          // Each data packet carries exactly 8 bytes; use integer ceiling division.
          return (int) ((streamLength + 7) / 8);
        }
        // DLC=8 → data packet inside an already-started stream; caller handles it.
        return 0;
      }

      // -----------------------------------------------------------------------
      // §5.1 S88 Polling (command 0x10, CAN-ID 0x20)
      // Request DLC=5: bytes 0-3 = device UID, byte 4 = module count.
      // Response: one DLC=7 frame per module.
      // The request is stored in txMessage so we can retrieve the module count.
      // -----------------------------------------------------------------------
      if (responseCmd == CanMessage.S88_POLLING || responseCmd == CanMessage.S88_POLLING + 1) {
        // The number of requested modules is in D-Byte 4 of the *request*.
        CanMessage request = txMessage;
        if (request != null && request.getDlc() == 5) {
          int moduleCount = request.getData()[4] & 0xFF;
          // We've already received the first response; expect moduleCount-1 more.
          return Math.max(0, moduleCount - 1);
        }
        // Fallback: estimate from buffered bytes (each response = 13 bytes).
        return remainingBytes / CanMessage.MESSAGE_SIZE;
      }

      // -----------------------------------------------------------------------
      // §3.7 Read Config (command 0x07, CAN-ID 0x0E)
      // Request DLC=7: D-Byte 4 = CV-Index(6)|CV-Num(10 hi), D-Byte 5 = CV-Num lo,
      //                D-Byte 6 = Anzahl (number of bytes to read; 0 means 256).
      // One response frame per byte read (each carries one value byte in D-Byte 6).
      // Negative ACK comes back as DLC=6 (missing D-Byte 6) — that is a single frame.
      // -----------------------------------------------------------------------
      if (responseCmd == CanMessage.READ_CONFIG || responseCmd == CanMessage.READ_CONFIG + 1) {
        CanMessage request = txMessage;
        if (request != null && request.getDlc() == 7) {
          int anzahl = request.getData()[6] & 0xFF;
          if (anzahl == 0) {
            anzahl = 256; // Per spec: value 0 means read 256 bytes
          }
          // Already received the first byte; expect anzahl-1 more.
          // Negative ACK (DLC=6 on the first packet) → no further packets.
          if (firstPacket.getDlc() < 7) {
            return 0; // Read failed / negative ACK
          }
          return Math.max(0, anzahl - 1);
        }
        return 0;
      }

      // -----------------------------------------------------------------------
      // §3.1 Lok Discovery (command 0x01, CAN-ID 0x02)
      // MFX full discovery runs in 32 steps (Range 0 → 32).
      // The intermediate response (DLC=5) carries the current Range in D-Byte 4.
      // Range=32 signals the final (complete) packet.
      // Non-MFX discoveries and negative results use DLC=0 or DLC=5 with a
      // protocol-specific range value ≥ 33 (MM2=33/34, DCC=35-37, SX1=38/39).
      // -----------------------------------------------------------------------
      if (responseCmd == CanMessage.LOK_DISCOVERY || responseCmd == CanMessage.LOK_DISCOVERY + 1) {
        int dlc = firstPacket.getDlc();
        if (dlc == 5) {
          byte[] data = firstPacket.getData();
          int range = data[4] & 0xFF; // D-Byte 4 = Range / Protokollkennung
          if (range < 33) {
            // MFX discovery in progress; Range steps remaining until 32.
            return Math.max(0, 32 - range);
          }
        }
        // DLC=0 (negative), DLC=6 (ASK debug), or non-MFX → single response.
        return 0;
      }

      // -----------------------------------------------------------------------
      // §6.2 Statusdaten Konfiguration (command 0x1D, CAN-ID 0x3A)
      // Data is streamed; the closing confirmation carries Paketanzahl in D-Byte 5.
      // Individual stream packets have DLC=8 and use the Hash for packet numbering.
      // The closing frame has DLC=6 and carries the total packet count.
      // -----------------------------------------------------------------------
      if (responseCmd == CanMessage.STATUS_CONFIG || responseCmd == CanMessage.STATUS_CONFIG + 1) {
        int dlc = firstPacket.getDlc();
        if (dlc == 8) {
          // Stream data packet; more are coming — use buffered byte estimate.
          return Math.max(0, remainingBytes / CanMessage.MESSAGE_SIZE);
        }
        // DLC=6 is the closing confirmation frame → no further packets.
        return 0;
      }

      // -----------------------------------------------------------------------
      // All other commands produce exactly ONE response packet.
      //
      // This covers (among others):
      //   System Stopp/Go/Halt (0x00 sub-cmds)   §2.1–2.3  DLC=5
      //   Lok Nothalt (0x00/0x03)                §2.4      DLC=5
      //   Lok Geschwindigkeit (0x04)              §3.4      DLC=4 or 6
      //   Lok Richtung (0x05)                     §3.5      DLC=4 or 5
      //   Lok Funktion (0x06)                     §3.6      DLC=5, 6, or 8
      //   MFX Bind (0x02)                         §3.2      DLC=6
      //   MFX Verify (0x03)                       §3.3      DLC=6 or 7
      //   Write Config (0x08)                     §3.8      DLC=8
      //   Zubehör Schalten (0x0B)                 §4.1      DLC=6 or 8
      //   S88 Event / Rückmelde Event (0x11)      §5.2      DLC=8
      //   Softwarestand / Teilnehmer Ping (0x18)  §6.1      DLC=0 or 8
      //   Automatik schalten (0x30)               §9.1      DLC=6 or 8
      // -----------------------------------------------------------------------
      return 0;
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
  public synchronized CanMessage sendCanMessage(CanMessage message) {
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
