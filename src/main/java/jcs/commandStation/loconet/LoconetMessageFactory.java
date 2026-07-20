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
import java.util.List;

/**
 * Factory for creating common Loconet messages.
 */
public final class LoconetMessageFactory {

  private LoconetMessageFactory() {
  }

  public static LoconetMessage powerOn() {
    return fixed(Loconet.Opcode.OPC_GPON);
  }

  public static LoconetMessage powerOff() {
    return fixed(Loconet.Opcode.OPC_GPOFF);
  }

  public static LoconetMessage emergencyStopIdle() {
    return fixed(Loconet.Opcode.OPC_IDLE);
  }

  public static LoconetMessage busy() {
    return fixed(Loconet.Opcode.OPC_BUSY);
  }

  public static LoconetMessage requestLocoAddress(int locomotiveAddress) {
    validateLocomotiveAddress(locomotiveAddress);

    int adrHigh = (locomotiveAddress >> 7) & Loconet.DATA_MASK;
    int adrLow = locomotiveAddress & Loconet.DATA_MASK;

    return fixed(Loconet.Opcode.OPC_LOCO_ADR, adrHigh, adrLow);
  }

  public static LoconetMessage requestSlotData(int slot) {
    Loconet.require7Bit("slot", slot);
    return fixed(Loconet.Opcode.OPC_RQ_SL_DATA, slot, 0x00);
  }

  public static LoconetMessage activateSlot(int slot) {
    Loconet.require7Bit("slot", slot);

    // NULL move: SRC == DEST. Used to mark slot IN_USE.
    return fixed(Loconet.Opcode.OPC_MOVE_SLOTS, slot, slot);
  }

  public static LoconetMessage dispatchPut(int slot) {
    Loconet.require7Bit("slot", slot);

    // Move source slot to destination slot 0.
    return fixed(Loconet.Opcode.OPC_MOVE_SLOTS, slot, 0x00);
  }

  public static LoconetMessage dispatchGet() {
    // Source slot 0 means dispatch get.
    return fixed(Loconet.Opcode.OPC_MOVE_SLOTS, 0x00, 0x00);
  }

  public static LoconetMessage changeLocomotiveSpeedBySlot(int slot, int speed) {
    Loconet.require7Bit("slot", slot);
    validateSpeed(speed);

    return fixed(Loconet.Opcode.OPC_LOCO_SPD, slot, speed);
  }

  /**
   * Creates the first message in the speed-change workflow.
   *
   * A real speed change by locomotive address is a multi-step operation: 1. Request locomotive address. 2. Parse slot from OPC_SL_RD_DATA reply. 3. Send OPC_LOCO_SPD using that slot.
   */
  public static List<LoconetMessage> beginChangeLocomotiveSpeed(int locomotiveAddress) {
    return List.of(requestLocoAddress(locomotiveAddress));
  }

  /**
   * Creates the follow-up speed message after the command station has replied with OPC_SL_RD_DATA and the slot has been extracted.
   */
  public static LoconetMessage finishChangeLocomotiveSpeed(int slotFromReply, int speed) {
    return changeLocomotiveSpeedBySlot(slotFromReply, speed);
  }

  public static LoconetMessage setDirectionAndFunctions(
          int slot,
          boolean forward,
          boolean f0,
          boolean f1,
          boolean f2,
          boolean f3,
          boolean f4
  ) {
    Loconet.require7Bit("slot", slot);

    int dirf = 0;

    if (forward) {
      dirf |= 0x20;
    }
    if (f0) {
      dirf |= 0x10;
    }
    if (f4) {
      dirf |= 0x08;
    }
    if (f3) {
      dirf |= 0x04;
    }
    if (f2) {
      dirf |= 0x02;
    }
    if (f1) {
      dirf |= 0x01;
    }

    return fixed(Loconet.Opcode.OPC_LOCO_DIRF, slot, dirf);
  }

  public static LoconetMessage setSoundFunctions(
          int slot,
          boolean f5,
          boolean f6,
          boolean f7,
          boolean f8
  ) {
    Loconet.require7Bit("slot", slot);

    int snd = 0;

    if (f8) {
      snd |= 0x08;
    }
    if (f7) {
      snd |= 0x04;
    }
    if (f6) {
      snd |= 0x02;
    }
    if (f5) {
      snd |= 0x01;
    }

    return fixed(Loconet.Opcode.OPC_LOCO_SND, slot, snd);
  }

  public static LoconetMessage switchRequest(int turnoutAddress, boolean closedGreen, boolean outputOn) {
    return switchMessage(Loconet.Opcode.OPC_SW_REQ, turnoutAddress, closedGreen, outputOn);
  }

  public static LoconetMessage switchRequestWithAck(int turnoutAddress, boolean closedGreen, boolean outputOn) {
    return switchMessage(Loconet.Opcode.OPC_SW_ACK, turnoutAddress, closedGreen, outputOn);
  }

  public static LoconetMessage requestSwitchState(int turnoutAddress) {
    int[] sw = encodeSwitch(turnoutAddress, false, false);
    return fixed(Loconet.Opcode.OPC_SW_STATE, sw[0], sw[1]);
  }

  public static LoconetMessage writeSlotData(
          int slot,
          int stat1,
          int addressLow,
          int speed,
          int dirf,
          int track,
          int stat2,
          int addressHigh,
          int sound,
          int id1,
          int id2
  ) {
    int[] payload = {
      0x0E,
      slot,
      stat1,
      addressLow,
      speed,
      dirf,
      track,
      stat2,
      addressHigh,
      sound,
      id1,
      id2
    };

    for (int value : payload) {
      Loconet.require7Bit("variable payload byte", value);
    }

    return variable(Loconet.Opcode.OPC_WR_SL_DATA, payload);
  }

  public static LoconetMessage immediatePacket(int repetitions, int... packetBytes) {
    if (packetBytes == null || packetBytes.length < 1 || packetBytes.length > 5) {
      throw new IllegalArgumentException("Immediate packet must contain 1 to 5 DCC packet bytes");
    }

    int dhi = 0x20;
    int[] im = new int[5];

    for (int i = 0; i < packetBytes.length; i++) {
      int value = packetBytes[i] & Loconet.BYTE_MASK;

      if ((value & 0x80) != 0) {
        dhi |= 1 << i;
      }

      im[i] = value & Loconet.DATA_MASK;
    }

    int reps = ((packetBytes.length & 0x07) << 4) | (repetitions & 0x07);

    return variable(
            Loconet.Opcode.OPC_IMM_PACKET,
            0x0B,
            0x7F,
            reps,
            dhi,
            im[0],
            im[1],
            im[2],
            im[3],
            im[4]
    );
  }

  private static LoconetMessage switchMessage(
          Loconet.Opcode opcode,
          int turnoutAddress,
          boolean closedGreen,
          boolean outputOn
  ) {
    int[] sw = encodeSwitch(turnoutAddress, closedGreen, outputOn);
    return fixed(opcode, sw[0], sw[1]);
  }

  /**
   * Encodes Loconet switch address and control bits.
   *
   * Assumes public turnout addresses are 1-based.
   */
  private static int[] encodeSwitch(int turnoutAddress, boolean closedGreen, boolean outputOn) {
    if (turnoutAddress < 1 || turnoutAddress > 2040) {
      throw new IllegalArgumentException("turnoutAddress must be in range 1..2040: " + turnoutAddress);
    }

    int address = turnoutAddress - 1;

    int sw1 = address & 0x7F;

    int sw2 = (address >> 7) & 0x0F;
    if (closedGreen) {
      sw2 |= 0x20;
    }
    if (outputOn) {
      sw2 |= 0x10;
    }

    return new int[]{sw1, sw2};
  }

  private static LoconetMessage fixed(Loconet.Opcode opcode, int... args) {
    int expectedLength = opcode.fixedLength();

    if (expectedLength < 0) {
      throw new IllegalArgumentException(opcode.name() + " is not a fixed-length opcode");
    }

    if (args.length != expectedLength - 2) {
      throw new IllegalArgumentException(
              opcode.name() + " expects " + (expectedLength - 2)
              + " args but got " + args.length
      );
    }

    byte[] bytes = new byte[expectedLength];
    bytes[0] = opcode.byteValue();

    for (int i = 0; i < args.length; i++) {
      Loconet.require7Bit("arg" + (i + 1), args[i]);
      bytes[i + 1] = (byte) args[i];
    }

    bytes[bytes.length - 1]
            = LoconetMessage.calculateChecksum(Arrays.copyOf(bytes, bytes.length - 1));

    return new LoconetMessage(opcode, bytes);
  }

  private static LoconetMessage variable(Loconet.Opcode opcode, int... payloadIncludingCount) {
    if (opcode.lengthKind() != Loconet.MessageLengthKind.VARIABLE) {
      throw new IllegalArgumentException(opcode.name() + " is not a variable-length opcode");
    }

    if (payloadIncludingCount.length < 1) {
      throw new IllegalArgumentException("Variable message requires a count byte");
    }

    int count = payloadIncludingCount[0];

    if (count != payloadIncludingCount.length + 2) {
      throw new IllegalArgumentException(
              "Variable message count byte must equal total message length. Count="
              + count + ", actual=" + (payloadIncludingCount.length + 2)
      );
    }

    byte[] bytes = new byte[count];
    bytes[0] = opcode.byteValue();

    for (int i = 0; i < payloadIncludingCount.length; i++) {
      Loconet.require7Bit("payload byte " + i, payloadIncludingCount[i]);
      bytes[i + 1] = (byte) payloadIncludingCount[i];
    }

    bytes[bytes.length - 1]
            = LoconetMessage.calculateChecksum(Arrays.copyOf(bytes, bytes.length - 1));

    return new LoconetMessage(opcode, bytes);
  }

  private static void validateLocomotiveAddress(int locomotiveAddress) {
    if (locomotiveAddress < 0 || locomotiveAddress > 9999) {
      throw new IllegalArgumentException(
              "locomotiveAddress must be in range 0..9999: " + locomotiveAddress
      );
    }
  }

  private static void validateSpeed(int speed) {
    if (speed < 0 || speed > 127) {
      throw new IllegalArgumentException("speed must be in range 0..127: " + speed);
    }
  }
}
