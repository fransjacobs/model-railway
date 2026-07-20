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
package jcs.commandStation.loconet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Immutable Loconet message.
 */
public final class LoconetMessage {

  private final Loconet.Opcode opcode;
  private final byte[] message;
  private final List<Integer> reply;

  public LoconetMessage(Loconet.Opcode opcode, byte[] message) {
    this(opcode, message, List.of());
  }

  public LoconetMessage(Loconet.Opcode opcode, byte[] message, List<Integer> reply) {
    if (opcode == null) {
      throw new IllegalArgumentException("opcode may not be null");
    }
    if (message == null || message.length < 2) {
      throw new IllegalArgumentException("message must contain at least opcode and checksum");
    }
    if ((message[0] & Loconet.BYTE_MASK) != opcode.value()) {
      throw new IllegalArgumentException(
              "Message opcode byte " + Loconet.toHex(message[0])
              + " does not match opcode " + opcode.name()
      );
    }

    this.opcode = opcode;
    this.message = Arrays.copyOf(message, message.length);
    this.reply = new ArrayList<>(reply == null ? List.of() : reply);

    validateStructure();
  }

  public Loconet.Opcode getOpcode() {
    return opcode;
  }

  public byte[] getMessageBytes() {
    return Arrays.copyOf(message, message.length);
  }

  public int[] getMessageUnsigned() {
    int[] result = new int[message.length];

    for (int i = 0; i < message.length; i++) {
      result[i] = message[i] & Loconet.BYTE_MASK;
    }

    return result;
  }

  public List<Integer> getReply() {
    return Collections.unmodifiableList(reply);
  }

  public void setReply(List<Integer> newReply) {
    reply.clear();

    if (newReply != null) {
      reply.addAll(newReply);
    }
  }

  public int length() {
    return message.length;
  }

  public int calculateLength() {
    return calculateLength(message);
  }

  public static int calculateLength(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      throw new IllegalArgumentException("Message bytes are empty");
    }

    int opcode = bytes[0] & Loconet.BYTE_MASK;
    Loconet.MessageLengthKind kind = Loconet.lengthKindFromOpcode(opcode);

    return switch (kind) {
      case FIXED_2 ->
        2;
      case FIXED_4 ->
        4;
      case FIXED_6 ->
        6;
      case VARIABLE -> {
        if (bytes.length < 2) {
          throw new IllegalArgumentException("Variable-length message is missing count byte");
        }
        yield bytes[1] & Loconet.DATA_MASK;
      }
    };
  }

  /**
   * Calculates checksum for all bytes except the final checksum byte.
   *
   * Loconet checksum = one's complement of XOR of all previous bytes.
   */
  public static byte calculateChecksum(byte[] bytesWithoutChecksum) {
    int xor = 0;

    for (byte b : bytesWithoutChecksum) {
      xor ^= b & Loconet.BYTE_MASK;
    }

    return (byte) (xor ^ Loconet.BYTE_MASK);
  }

  /**
   * Calculates checksum for all values passed as unsigned byte values.
   */
  public static byte calculateChecksum(int... bytesWithoutChecksum) {
    int xor = 0;

    for (int value : bytesWithoutChecksum) {
      xor ^= value & Loconet.BYTE_MASK;
    }

    return (byte) (xor ^ Loconet.BYTE_MASK);
  }

  /**
   * Calculates the checksum expected for this message, excluding the current final checksum byte.
   */
  public byte calculateChecksum() {
    byte[] withoutChecksum = Arrays.copyOf(message, message.length - 1);
    return calculateChecksum(withoutChecksum);
  }

  /**
   * Valid Loconet messages XOR to 0xFF when all bytes, including checksum, are XOR'ed together.
   */
  public boolean hasValidChecksum() {
    int xor = 0;

    for (byte b : message) {
      xor ^= b & Loconet.BYTE_MASK;
    }

    return (xor & Loconet.BYTE_MASK) == Loconet.BYTE_MASK;
  }

  public boolean hasValidOpcode() {
    int value = message[0] & Loconet.BYTE_MASK;
    return Loconet.Opcode.isKnown(value);
  }

  public List<Integer> predictReplyLengths() {
    return predictReplyLengths(opcode);
  }

  public static List<Integer> predictReplyLengths(Loconet.Opcode opcode) {
    return switch (opcode.replyKind()) {
      case NONE ->
        List.of();
      case LONG_ACK ->
        List.of(4);
      case MAYBE_LONG_ACK ->
        List.of(0, 4);
      case SLOT_READ ->
        List.of(14);
      case SLOT_READ_OR_LONG_ACK ->
        List.of(14, 4);
    };
  }

  private void validateStructure() {
    int expectedLength = calculateLength();

    if (expectedLength != message.length) {
      throw new IllegalArgumentException(
              "Invalid Loconet message length. Expected "
              + expectedLength + " but got " + message.length
      );
    }

    if (!hasValidOpcode()) {
      throw new IllegalArgumentException(
              "Unknown Loconet opcode: " + Loconet.toHex(message[0])
      );
    }

    /*
         * Only the first byte may be an opcode byte with bit 7 set.
         * All remaining bytes, including checksum, must be 7-bit data bytes.
     */
    for (int i = 1; i < message.length; i++) {
      int value = message[i] & Loconet.BYTE_MASK;

      if (!Loconet.isDataByte(value)) {
        throw new IllegalArgumentException(
                "Invalid data byte at index " + i + ": " + Loconet.toHex(value)
                + ". Only the first byte may have bit 7 set."
        );
      }
    }

    if (!hasValidChecksum()) {
      throw new IllegalArgumentException("Invalid Loconet checksum: " + toHexString());
    }
  }

  public String toHexString() {
    StringBuilder sb = new StringBuilder();

    for (byte b : message) {
      if (!sb.isEmpty()) {
        sb.append(' ');
      }

      sb.append(Loconet.toHex(b));
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    return opcode.name() + " [" + toHexString() + "]";
  }
}
