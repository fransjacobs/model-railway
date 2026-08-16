package jcs.commandStation.loconet;

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
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import static jcs.commandStation.loconet.Opcodes.BYTE_MASK;
import static jcs.commandStation.loconet.Opcodes.DATA_MASK;
import jcs.entities.AccessoryBean;
import jcs.entities.SensorBean;
import org.tinylog.Logger;

public class LoconetMessageParser implements Opcodes {

  private static final String COMMAND_STATION_ID = "intellibox2";

  private final boolean validateChecksum;

  public LoconetMessageParser() {
    this(true);
  }

  public LoconetMessageParser(boolean validateChecksum) {
    this.validateChecksum = validateChecksum;
  }

  private int getMessageLength(int opcode) {
    return Opcodes.lengthFromOpcode(opcode);
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

  /**
   * Reads one complete LocoNet frame from the input stream.
   *
   * Returns null on inter-byte timeout or incomplete frame. Throws IOException for real stream/port errors.
   *
   * @param input
   * @return
   * @throws java.io.IOException
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

  private static Integer calculateDeviceId(int address) {
    int deviceId = (address + 1) / 16 + 1;
    return deviceId;
  }

  private static int calculateContactId(int address) {
    int module = (address + 1) / 16 + 1;
    int mport = address + 1 - (module - 1) * 16;
    return mport;
  }

  private static int calculateContactId(int module, int port) {
    module = module - 1;
    int contactId = module * 16;
    return contactId + port;
  }

  /**
   * Parses a raw 4-byte LocoNet sensor message.
   *
   * @param opcode expected 0xB2
   * @param in1 address low byte (a6..a0)
   * @param in2 address high nibble + flags (X, I, L, a10..a7)
   * @param chk checksum byte
   * @return the decoded sensor event
   * @throws IllegalArgumentException if opcode or checksum is invalid
   */
  public static SensorBean parseSensorEvent(LoconetMessage message) {
    if (!message.isChecksumValid()) {
      throw new IllegalArgumentException(String.format("Checksum mismatch for message {}", message.toString()));
    }
    if (!message.isExpectedsOpcode(OPC_INPUT_REP)) {
      throw new IllegalArgumentException(String.format("Not a sensor message, opcode={}", message.getHexOpcode()));
    }

    int in1 = message.getByte(1);
    int in2 = message.getByte(2);

    int addrLow = in1 & 0x7F;   // a6..a0
    int addrHigh = in2 & 0x0F;  // a10..a7
    int rawAddress = (addrHigh << 7) | addrLow; // 11-bit pair address

    boolean x = (in2 & 0x40) != 0;
    boolean i = (in2 & 0x20) != 0; // selects sub-address within the pair
    boolean value = (in2 & 0x10) != 0; // L bit

    // I bit distinguishes the two sensors sharing this raw pair address
    int address = (rawAddress << 1) | (i ? 1 : 0);

    Integer id = address + 1;
    Integer deviceId = calculateDeviceId(address);
    Integer contactId = calculateContactId(address);
    return new SensorBean(id, deviceId, contactId, 0, (value ? 1 : 0), (value ? 0 : 1), COMMAND_STATION_ID, 0);
  }

  /**
   * Parses a raw 4-byte LocoNet accessory message.
   *
   * @param opcode expected 0xB2
   * @param sw1 address low byte (a6..a0)
   * @param sw2 address high nibble + flags (0, dir, on, a10..a7)
   * @param chk checksum byte
   * @return the decoded Accessory event
   * @throws IllegalArgumentException if opcode or checksum is invalid
   */
  public static AccessoryBean parseSwitchEvent(LoconetMessage message) {
    if (!message.isChecksumValid()) {
      throw new IllegalArgumentException(String.format("Checksum mismatch for message {}", message.toString()));
    }
    if (!message.isExpectedsOpcode(OPC_SW_REQ)) {
      throw new IllegalArgumentException(String.format("Not a sensor message, opcode={}", message.getHexOpcode()));
    }

    int sw1 = message.getByte(1);
    int sw2 = message.getByte(2);

    int addrLow = sw1 & 0x7F;   // a6..a0
    int addrHigh = sw2 & 0x0F;  // a10..a7
    int zeroBasedAddress = (addrHigh << 7) | addrLow;
    int displayAddress = zeroBasedAddress + 1;

    boolean green = (sw2 & 0x20) != 0; // DIR: 1=closed/green, 0=thrown/red
    boolean outputOn = (sw2 & 0x10) != 0; // ON: 1=coil/output active, 0=off

    String id = Integer.toString(displayAddress);

    Integer address2 = null;
    String name = null;
    String type = null;
    int state = green ? 1 : 0;
    Integer states = null;
    Integer switchTime = null;
    String protocol = null;

    AccessoryBean ab = new AccessoryBean(id, displayAddress, address2, name, type, state, states, switchTime, protocol, COMMAND_STATION_ID);
    ab.setOn(outputOn);
    return ab;
  }

}
