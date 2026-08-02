/*
 * Copyright 2026 fransjacobs.
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

import com.fazecast.jSerialComm.SerialPortTimeoutException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.tinylog.Logger;

public final class LoconetMessageFramer implements Opcodes {

  private final boolean validateChecksum;

  public LoconetMessageFramer() {
    this(true);
  }

  public LoconetMessageFramer(boolean validateChecksum) {
    this.validateChecksum = validateChecksum;
  }

  /**
   * Reads one complete LocoNet frame from the input stream.
   *
   * Returns null on inter-byte timeout or incomplete frame. Throws IOException for real stream/port errors.
   */
  public LoconetMessage readMessage(InputStream input) throws IOException {
    int opcode = readUntilOpcode(input);
    if (opcode < 0) {
      return null;
    }

    int length = getMessageLength(opcode);
    if (length == 0) {
      return null;
    }

    final int[] frame;

    if (length < 0) {
      // Variable length message. Byte 1 is total message length.
      int count = readByteOrTimeout(input);
      if (count < 0) {
        return null;
      }

      length = count & DATA_MASK;

      // Total length includes opcode and checksum.
      if (length < 3) {
        Logger.trace("Discarding variable LocoNet frame with invalid length: {}", length);
        return null;
      }

      frame = new int[length];
      frame[0] = opcode;
      frame[1] = count & BYTE_MASK;

      for (int i = 2; i < length; i++) {
        int value = readByteOrTimeout(input);
        if (value < 0) {
          Logger.trace("Discarding incomplete variable LocoNet frame: {}", Arrays.toString(frame));
          return null;
        }
        frame[i] = value & BYTE_MASK;
      }
    } else {
      frame = new int[length];
      frame[0] = opcode;

      for (int i = 1; i < length; i++) {
        int value = readByteOrTimeout(input);
        if (value < 0) {
          Logger.trace("Discarding incomplete fixed LocoNet frame: {}", Arrays.toString(frame));
          return null;
        }
        frame[i] = value & BYTE_MASK;
      }
    }

    if (validateChecksum && !isValidChecksum(frame)) {
      Logger.trace("Discarding LocoNet frame with invalid checksum: {}", Arrays.toString(frame));
      return null;
    }

    return LoconetMessage.fromReceived(frame);
  }

  private int readUntilOpcode(InputStream input) throws IOException {
    while (true) {
      int value = readByteOrTimeout(input);
      if (value < 0) {
        return -1;
      }

      value &= BYTE_MASK;

      if (Opcodes.isOpcodeByte(value)) {
        return value;
      }

      Logger.trace("Skipping non-opcode byte while resyncing: {}", Opcodes.toHex(value));
    }
  }

  private int readByteOrTimeout(InputStream input) throws IOException {
    try {
      return input.read();
    } catch (SerialPortTimeoutException timeout) {
      return -1;
    }
  }

  private int getMessageLength(int opcode) {
    return switch (Opcodes.lengthKindFromOpcode(opcode)) {
      case FIXED_2 ->
        2;
      case FIXED_4 ->
        4;
      case FIXED_6 ->
        6;
      case VARIABLE ->
        -1;
    };
  }

  /**
   * LocoNet checksum validation: XOR of all bytes in a valid message equals 0xFF.
   */
  static boolean isValidChecksum(int[] frame) {
    if (frame == null || frame.length < 2) {
      return false;
    }

    int xor = 0x00;
    for (int value : frame) {
      xor ^= (value & BYTE_MASK);
    }

    return (xor & BYTE_MASK) == 0xFF;
  }
}
