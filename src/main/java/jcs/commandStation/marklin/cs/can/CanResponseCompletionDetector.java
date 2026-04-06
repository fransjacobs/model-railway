/*
 * Copyright 2026 frans.
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
package jcs.commandStation.marklin.cs.can;

import java.util.HashMap;
import java.util.Map;

/**
 * Determines when a CAN command response is complete based on Märklin protocol rules
 *
 * Based on the CAN CS2 Protocol specification: - Most commands have single response - CONFIG_DATA can have multiple responses - Some commands use data streaming
 */
public class CanResponseCompletionDetector {

  /**
   * Predefined response expectations for known commands
   */
  private static final Map<Integer, ResponseExpectation> RESPONSE_PATTERNS = new HashMap<>();

  static {
    // System commands (0x00-0x00)
    RESPONSE_PATTERNS.put(CanMessage.SYSTEM_COMMAND, single());

    // Management commands (0x01-0x0A)
    RESPONSE_PATTERNS.put(CanMessage.SYSTEM_COMMAND, single());
    //RESPONSE_PATTERNS.put(CanMessage.SYSTEM_HALT, single());
    //RESPONSE_PATTERNS.put(CanMessage.SYSTEM_STOP, single());
    RESPONSE_PATTERNS.put(CanMessage.REQUEST_CONFIG_DATA, multiPacket()); // Can have long responses
    RESPONSE_PATTERNS.put(CanMessage.PING_REQ, single());
    RESPONSE_PATTERNS.put(CanMessage.PING_RESP, single());

    // Accessory commands (0x0B-0x0D)
    RESPONSE_PATTERNS.put(CanMessage.ACCESSORY_SWITCHING, single());
    RESPONSE_PATTERNS.put(CanMessage.ACCESSORY_CONFIG, single());

    // Feedback commands (0x10-0x12)
    RESPONSE_PATTERNS.put(CanMessage.S88_EVENT_RESPONSE, single());
    RESPONSE_PATTERNS.put(CanMessage.S88_EVENT_QUERY, single());

    // Software/Config (0x18-0x1C)
    RESPONSE_PATTERNS.put(CanMessage.BOOTLOADER_CAN, streaming());
    RESPONSE_PATTERNS.put(CanMessage.REQUEST_CONFIG_DATA_RESP, streaming());
    RESPONSE_PATTERNS.put(CanMessage.STATUS_CONFIG, multiPacket());

    // GUI commands (0x20-0x22)
    //RESPONSE_PATTERNS.put(CanMessage.GUI_COMMAND, single());
    // Automation (0x30+)
    //RESPONSE_PATTERNS.put(CanMessage.AUTOMATION, single());
  }

  /**
   * Response expectation strategies
   */
  private enum ResponseType {
    SINGLE, // One response packet
    MULTI_PACKET, // Multiple packets, use protocol indicators
    STREAMING      // Data stream, use sequence numbers in hash field
  }

  private static class ResponseExpectation {

    final ResponseType type;
    final int expectedPackets; // -1 for variable

    ResponseExpectation(ResponseType type, int expectedPackets) {
      this.type = type;
      this.expectedPackets = expectedPackets;
    }
  }

  private static ResponseExpectation single() {
    return new ResponseExpectation(ResponseType.SINGLE, 1);
  }

  private static ResponseExpectation multiPacket() {
    return new ResponseExpectation(ResponseType.MULTI_PACKET, -1);
  }

  private static ResponseExpectation streaming() {
    return new ResponseExpectation(ResponseType.STREAMING, -1);
  }

  /**
   * State tracker for a specific response sequence
   */
  public static class ResponseState {

    private final CanMessage requestMessage;
    private final ResponseExpectation expectation;

    private int packetsReceived = 0;
    private int totalExpectedPackets = -1; // -1 until determined from response
    private Integer lastSequenceNumber = null;

    public ResponseState(CanMessage requestMessage) {
      this.requestMessage = requestMessage;
      this.expectation = getExpectation(requestMessage.getCommand());
    }

    /**
     * Add a response packet and determine if response is complete
     *
     * @param response The received response packet
     * @param bytesAvailableInStream Bytes still in TCP stream (unreliable, used as hint only)
     * @return true if response is complete
     */
    public boolean addResponse(CanMessage response, int bytesAvailableInStream) {
      packetsReceived++;

      switch (expectation.type) {
        case SINGLE:
          return handleSingleResponse(response);

        case MULTI_PACKET:
          return handleMultiPacketResponse(response, bytesAvailableInStream);

        case STREAMING:
          return handleStreamingResponse(response);

        default:
          return true; // Unknown, assume complete
      }
    }

    private boolean handleSingleResponse(CanMessage response) {
      // Single response always complete after first packet
      return packetsReceived >= 1;
    }

    private boolean handleMultiPacketResponse(CanMessage response, int bytesAvailableInStream) {
      // For multi-packet responses, check if protocol indicates total count

      // First packet analysis
      if (packetsReceived == 1) {
        totalExpectedPackets = determineExpectedPackets(response);
      }

      // If we know the total, check if we have them all
      if (totalExpectedPackets > 0) {
        return packetsReceived >= totalExpectedPackets;
      }

      // Fallback: use stream bytes as hint (unreliable!)
      // Assume each packet is 13 bytes
      int estimatedRemainingPackets = bytesAvailableInStream / 13;

      // If no more bytes AND we have at least one packet, assume complete
      if (bytesAvailableInStream == 0 && packetsReceived > 0) {
        return true;
      }

      // Otherwise, keep waiting (will timeout if needed)
      return false;
    }

    private boolean handleStreamingResponse(CanMessage response) {
      // For streaming responses, hash field contains sequence number
      // According to spec: bits 7-9 are masked, giving range 0-8192

      int sequenceNumber = extractSequenceNumber(response.getHash());

      if (lastSequenceNumber != null) {
        // Check if sequence wraps to 0 (end of stream)
        if (sequenceNumber == 0 && lastSequenceNumber > 0) {
          return true; // Stream complete
        }

        // Check for duplicate (retransmission)
        if (sequenceNumber <= lastSequenceNumber) {
          // Potential end or error
          return true;
        }
      }

      lastSequenceNumber = sequenceNumber;

      // Check DLC for end marker
      if (response.getDlc() == 0) {
        return true; // Empty data = end of stream
      }

      return false;
    }

    /**
     * Determine total expected packets from protocol-specific data
     */
    private int determineExpectedPackets(CanMessage firstResponse) {
      int command = requestMessage.getCommand();

      // CONFIG_DATA special handling
      if (command == CanMessage.REQUEST_CONFIG_DATA) {
        // First byte often contains total packet count
        if (firstResponse.getDlc() > 0) {
          byte[] data = firstResponse.getData();
          // Check if first byte looks like a count (1-255)
          int possibleCount = data[0] & 0xFF;
          if (possibleCount > 0 && possibleCount < 100) {
            return possibleCount;
          }
        }
      }

      // STATUS_CONFIG handling
      if (command == CanMessage.STATUS_CONFIG) {
        // Status typically has fixed response structure
        // Adjust based on actual observation
        return 3; // Example: assume 3 packets
      }

      return -1; // Unknown, rely on stream bytes
    }

    /**
     * Extract sequence number from hash field According to spec: bits 7-9 are masked out, bits 13-15 are 0
     */
    private int extractSequenceNumber(byte[] hash) {
      int h = CanMessage.toInt(hash);
      // Mask bits 7-9 (0b0000011110000000 = 0x0380)
      // Mask bits 13-15 (0b1110000000000000 = 0xE000)
      int mask = ~(0x0380 | 0xE000);
      return h & mask;
    }
  }

  /**
   * Get the response expectation for a command
   */
  private static ResponseExpectation getExpectation(int command) {
    ResponseExpectation exp = RESPONSE_PATTERNS.get(command);
    if (exp != null) {
      return exp;
    }

    // Default based on command range
    if (command >= 0x00 && command <= 0x0A) {
      return single(); // Most management commands
    } else if (command >= 0x18 && command <= 0x1C) {
      return multiPacket(); // Config/update commands
    } else {
      return single(); // Conservative default
    }
  }

  /**
   * Create a new response state tracker for a request
   */
  public static ResponseState createResponseState(CanMessage request) {
    return new ResponseState(request);
  }

  /**
   * Quick check: does this command expect multiple packets?
   */
  public static boolean expectsMultiplePackets(int command) {
    ResponseExpectation exp = getExpectation(command);
    return exp.type != ResponseType.SINGLE;
  }
}

/**
 * Integration example with PendingRequest
 */
class PendingRequestWithDetector {

  private final CanMessage txMessage;
  private final CanResponseCompletionDetector.ResponseState responseState;

  public PendingRequestWithDetector(CanMessage txMessage) {
    this.txMessage = txMessage;
    this.responseState = CanResponseCompletionDetector.createResponseState(txMessage);
  }

  public boolean addResponse(CanMessage rx, int bytesAvailable) {
    txMessage.addResponse(rx);
    return responseState.addResponse(rx, bytesAvailable);
  }
}
