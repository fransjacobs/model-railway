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

import java.util.Arrays;
import java.util.Optional;

/**
 * LocoNet protocol constants and opcode definitions.<br>
 * LocoNet® is a registered trademark of Digitrax, Inc.
 */
public interface Loconet {

  int BYTE_MASK = 0xFF;
  int DATA_MASK = 0x7F;

  enum MessageLengthKind {
    FIXED_2,
    FIXED_4,
    FIXED_6,
    VARIABLE
  }

  enum ReplyKind {
    NONE,
    LONG_ACK,
    SLOT_READ,
    SLOT_READ_OR_LONG_ACK,
    MAYBE_LONG_ACK
  }

  enum Opcode {
    // 2-byte messages
    OPC_BUSY(0x81, "MASTER busy / NOP", ReplyKind.NONE),
    OPC_GPOFF(0x82, "Global power OFF request", ReplyKind.NONE),
    OPC_GPON(0x83, "Global power ON request", ReplyKind.NONE),
    OPC_IDLE(0x85, "Force idle / broadcast emergency stop", ReplyKind.NONE),
    // 4-byte messages
    OPC_LOCO_SPD(0xA0, "Set slot speed", ReplyKind.NONE),
    OPC_LOCO_DIRF(0xA1, "Set slot direction and F0-F4", ReplyKind.NONE),
    OPC_LOCO_SND(0xA2, "Set slot sound functions F5-F8", ReplyKind.NONE),
    OPC_SW_REQ(0xB0, "Request switch function", ReplyKind.MAYBE_LONG_ACK),
    OPC_SW_REP(0xB1, "Turnout sensor state report", ReplyKind.NONE),
    OPC_INPUT_REP(0xB2, "General sensor input report", ReplyKind.NONE),
    OPC_LONG_ACK(0xB4, "Long acknowledge", ReplyKind.NONE),
    OPC_SLOT_STAT1(0xB5, "Write slot STAT1", ReplyKind.NONE),
    OPC_CONSIST_FUNC(0xB6, "Set consist function bits", ReplyKind.NONE),
    OPC_UNLINK_SLOTS(0xB8, "Unlink slots", ReplyKind.SLOT_READ_OR_LONG_ACK),
    OPC_LINK_SLOTS(0xB9, "Link slots", ReplyKind.SLOT_READ_OR_LONG_ACK),
    OPC_MOVE_SLOTS(0xBA, "Move slots", ReplyKind.SLOT_READ_OR_LONG_ACK),
    OPC_RQ_SL_DATA(0xBB, "Request slot data", ReplyKind.SLOT_READ),
    OPC_SW_STATE(0xBC, "Request switch state", ReplyKind.LONG_ACK),
    OPC_SW_ACK(0xBD, "Request switch with acknowledge", ReplyKind.LONG_ACK),
    OPC_LOCO_ADR(0xBF, "Request locomotive address", ReplyKind.SLOT_READ_OR_LONG_ACK),
    // Variable-length messages
    OPC_PEER_XFER(0xE5, "Peer transfer", ReplyKind.NONE),
    OPC_SL_RD_DATA(0xE7, "Slot data read", ReplyKind.NONE),
    OPC_IMM_PACKET(0xED, "Immediate packet", ReplyKind.LONG_ACK),
    OPC_WR_SL_DATA(0xEF, "Write slot data", ReplyKind.LONG_ACK);

    private final int value;
    private final String description;
    private final ReplyKind replyKind;

    Opcode(int value, String description, ReplyKind replyKind) {
      this.value = value;
      this.description = description;
      this.replyKind = replyKind;
    }

    public int value() {
      return value;
    }

    public byte byteValue() {
      return (byte) value;
    }

    public String description() {
      return description;
    }

    public ReplyKind replyKind() {
      return replyKind;
    }

    public MessageLengthKind lengthKind() {
      return lengthKindFromOpcode(value);
    }

    public int fixedLength() {
      return fixedLengthFromOpcode(value);
    }

    public static Optional<Opcode> from(int value) {
      int normalized = value & BYTE_MASK;

      return Arrays.stream(values())
              .filter(opcode -> opcode.value == normalized)
              .findFirst();
    }

    public static boolean isKnown(int value) {
      return from(value).isPresent();
    }
  }

  static boolean isOpcodeByte(int value) {
    return (value & 0x80) != 0;
  }

  static boolean isDataByte(int value) {
    return (value & 0x80) == 0;
  }

  static MessageLengthKind lengthKindFromOpcode(int opcode) {
    int value = opcode & BYTE_MASK;

    if (!isOpcodeByte(value)) {
      throw new IllegalArgumentException("Not a LocoNet opcode byte: " + toHex(value));
    }

    int lengthBits = value & 0x60;

    return switch (lengthBits) {
      case 0x00 ->
        MessageLengthKind.FIXED_2;
      case 0x20 ->
        MessageLengthKind.FIXED_4;
      case 0x40 ->
        MessageLengthKind.FIXED_6;
      case 0x60 ->
        MessageLengthKind.VARIABLE;
      default ->
        throw new IllegalStateException("Unexpected length bits: " + toHex(lengthBits));
    };
  }

  static int fixedLengthFromOpcode(int opcode) {
    return switch (lengthKindFromOpcode(opcode)) {
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

  static void require7Bit(String name, int value) {
    if ((value & ~DATA_MASK) != 0) {
      throw new IllegalArgumentException(name + " must be a 7-bit value: " + value);
    }
  }

  static String toHex(int value) {
    return String.format("0x%02X", value & BYTE_MASK);
  }
}

//  public static final int OPC_BUSY = 0x81;
//  public static final int OPC_GPOFF = 0x82;
//  public static final int OPC_GPON = 0x83;
//  public static final int OPC_IDLE = 0x85;
//
//  public static final int OPC_LOCO_SPD = 0xA0;
//  public static final int OPC_LOCO_DIRF = 0xA1;
//  public static final int OPC_LOCO_SND = 0xA2;
//  public static final int OPC_LOCO_F9F12 = 0xA3; //!
//  public static final int OPC_SW_REQ = 0xB0;
//  public static final int OPC_SW_REP = 0xB1;
//  public static final int OPC_INPUT_REP = 0xB2;
//  public static final int OPC_LONG_ACK = 0xB4;
//  public static final int OPC_SLOT_STAT1 = 0xB5;
//  public static final int OPC_CONSIST_FUNC = 0xB6;
//  public static final int OPC_UNLINK_SLOTS = 0xB8;
//  public static final int OPC_LINK_SLOTS = 0xB9;
//  public static final int OPC_MOVE_SLOTS = 0xBA;
//  public static final int OPC_RQ_SL_DATA = 0xBB;
//  public static final int OPC_SW_STATE = 0xBC;
//  public static final int OPC_SW_ACK = 0xBD;
//  public static final int OPC_LOCO_ADR = 0xBF;
//
//  public static final int OPC_MULTI_SENSE = 0xD0; //!
//  public static final int OPC_D4 = 0xD4; //!
//
//  // n byte message opcodes:
//  public static final int OPC_MULTI_SENSE_LONG = 0XE0; // !
//  public static final int OPC_E4 = 0xE4; // !
//  public static final int OPC_PEER_XFER = 0xE5;
//  public static final int OPC_SL_RD_DATA = 0xE7;
//  public static final int OPC_IMM_PACKET = 0xED;
//  public static final int OPC_WR_SL_DATA = 0xEF;

