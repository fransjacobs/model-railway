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

import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.LocomotiveBean.Direction;

/**
 * Factory for creating common Opcodes messages.
 */
public final class LoconetMessageFactory {

  private LoconetMessageFactory() {
  }

  public static LoconetMessage powerOn() {
    return new LoconetMessage(LoconetMessage.OPC_GPON);
  }

  public static LoconetMessage powerOff() {
    return new LoconetMessage(LoconetMessage.OPC_GPOFF);
  }

  public static LoconetMessage emergencyStopIdle() {
    return new LoconetMessage(LoconetMessage.OPC_IDLE);
  }

  public static LoconetMessage busy() {
    return new LoconetMessage(LoconetMessage.OPC_BUSY);
  }

  public static LoconetMessage switchAccessory(int address, AccessoryValue value, boolean on) {
    if (address < 1 || address > 2048) {
      throw new IllegalArgumentException("Accessory address must be in range 1..2048: " + address);
    }
    int zeroBasedAddress = address - 1;

    int sw1 = zeroBasedAddress & 0x7F;        // A6..A0
    int sw2 = (zeroBasedAddress >> 7) & 0x0F; // A10..A7

    if (value == AccessoryValue.GREEN) {
      sw2 |= 0x20; // DIR: 1 = closed / green
    }

    if (on) {
      sw2 |= 0x10; // ON: 1 = output active
    }
    return new LoconetMessage(LoconetMessage.OPC_SW_REQ, sw1, sw2);
  }

  public static LoconetMessage longAcknowlegde(int lopc, int ack1) {
    return new LoconetMessage(LoconetMessage.OPC_LONG_ACK, lopc, ack1);
  }

  //OPC_LOCO_ADR 0xBF ;REQ loco ADR ; <0xBF>,<0>,<ADR>,<CHK> REQ loco ADR
  public static LoconetMessage requestLocoAddress(int locomotiveAddress) {
    validateLocomotiveAddress(locomotiveAddress);

    int adrHigh = (locomotiveAddress >> 7) & Opcodes.DATA_MASK;
    int adrLow = locomotiveAddress & Opcodes.DATA_MASK;

    return new LoconetMessage(LoconetMessage.OPC_LOCO_ADDR, adrHigh, adrLow);
  }

  public static LoconetMessage requestSlotData(int slot) {
    int slt = slot & 0x7F;
    return new LoconetMessage(LoconetMessage.OPC_RQ_SL_DATA, slt, 0x00);
  }

  public static LoconetMessage setDirectionAndFunctions(int slot, Direction direction, boolean f0, boolean f1, boolean f2, boolean f3, boolean f4) {
    int slt = slot & 0x7F;
    int dirf = 0;
    if (Direction.FORWARDS == direction) {
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
    return new LoconetMessage(LoconetMessage.OPC_LOCO_DIRF, slot, dirf);
  }

  //0x00=SPEED 0 ,STOP
  //0x01=SPEED 0 EMERGENCY stop
  //0x02-0x7F increasing SPEED,0x7F=MAX speed
  public static LoconetMessage changeLocomotiveSpeed(int slot, int speed) {
    int slt = slot & 0x7F;
    int spd = speed & 0x7F;
    validateSpeed(spd);

    return new LoconetMessage(LoconetMessage.OPC_LOCO_SPD, slt, spd);
  }

  public static LoconetMessage setFunctions(int slot, boolean f5, boolean f6, boolean f7, boolean f8) {
    int slt = slot & 0x7F;
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

    return new LoconetMessage(LoconetMessage.OPC_LOCO_SND, slt, snd);
  }

  public static LoconetMessage activateSlot(int slot, boolean active) {
    int src = slot & 0x7F;

    int dst;
    if (active) {
      dst = slot & 0x7F;
    } else {
      dst = 0;
    }

    return new LoconetMessage(LoconetMessage.OPC_MOVE_SLOTS, src, dst);
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

  public static LoconetMessage requestSerialNumber() {
    int arg1 = 0x01;
    int arg2 = 0x49;
    int arg3 = 0x42;
    int arg4 = 0x07;
    int arg5 = 0x00;
    int arg6 = 0x00;
    int arg7 = 0x00;
    int arg8 = 0x00;
    int arg9 = 0x00;
    int arg10 = 0x00;
    int arg11 = 0x00;
    int arg12 = 0x00;

    return new LoconetMessage(LoconetMessage.OPC_IMM_PACKET, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);
  }

  public static LoconetMessage requestSoftwareVersion() {
    int arg1 = 0x01;
    int arg2 = 0x49;
    int arg3 = 0x42;
    int arg4 = 0x06;
    int arg5 = 0x00;
    int arg6 = 0x00;
    int arg7 = 0x00;
    int arg8 = 0x00;
    int arg9 = 0x00;
    int arg10 = 0x00;
    int arg11 = 0x00;
    int arg12 = 0x00;

    return new LoconetMessage(LoconetMessage.OPC_IMM_PACKET, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);
  }

  public static LoconetMessage requestS88ReportAddress() {
    int arg1 = 0x01;
    int arg2 = 0x05;
    int arg3 = 0x00;
    int arg4 = 0x21;
    int arg5 = 0x00;
    int arg6 = 0x6e;
    int arg7 = 0x19;
    int arg8 = 0x49;
    int arg9 = 0x00;
    int arg10 = 0x00;
    int arg11 = 0x00;
    int arg12 = 0x00;

    return new LoconetMessage(LoconetMessage.OPC_IMM_PACKET, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);
  }

  public static LoconetMessage startLNCVProgramming(int article, int module) {
    int articleLow = article & 0xFF;
    int articleHigh = (article >> 8) & 0xFF;

    int moduleLow = module & 0xFF;
    int moduleHigh = (module >> 8) & 0xFF;

    int commandData = 0x80;

    int pxct = 0;

    if ((articleLow & 0x80) != 0) {
      pxct |= 0x01;
    }
    if ((articleHigh & 0x80) != 0) {
      pxct |= 0x02;
    }

    // LNCV low/high are both zero here.
    if ((moduleLow & 0x80) != 0) {
      pxct |= 0x10;
    }
    if ((moduleHigh & 0x80) != 0) {
      pxct |= 0x20;
    }
    if ((commandData & 0x80) != 0) {
      pxct |= 0x40;
    }

    int arg1 = 0x01;
    int arg2 = 0x05;
    int arg3 = 0x00;
    int arg4 = 0x21;
    int arg5 = pxct;
    int arg6 = articleLow & 0x7F;
    int arg7 = articleHigh & 0x7F;
    int arg8 = 0x00; // LNCV low
    int arg9 = 0x00; // LNCV high
    int arg10 = moduleLow & 0x7F;
    int arg11 = moduleHigh & 0x7F;
    int arg12 = commandData & 0x7F;

    return new LoconetMessage(
            LoconetMessage.OPC_IMM_PACKET,
            arg1, arg2, arg3, arg4,
            arg5, arg6, arg7, arg8,
            arg9, arg10, arg11, arg12);
  }

  public static LoconetMessage readLNCV(int article, int lncv) {
    int articleLow = article & 0xFF;
    int articleHigh = (article >> 8) & 0xFF;

    int lncvLow = lncv & 0xFF;
    int lncvHigh = (lncv >> 8) & 0xFF;

    int pxct = 0;

    // PXCT carries bit 7 of the seven following data bytes.
    if ((articleLow & 0x80) != 0) {
      pxct |= 0x01;
    }
    if ((articleHigh & 0x80) != 0) {
      pxct |= 0x02;
    }
    if ((lncvLow & 0x80) != 0) {
      pxct |= 0x04;
    }
    if ((lncvHigh & 0x80) != 0) {
      pxct |= 0x08;
    }

    int arg1 = 0x01;
    int arg2 = 0x05;
    int arg3 = 0x00;
    int arg4 = 0x21; // LNCV read
    int arg5 = pxct;
    int arg6 = articleLow & 0x7F;
    int arg7 = articleHigh & 0x7F;
    int arg8 = lncvLow & 0x7F;
    int arg9 = lncvHigh & 0x7F;
    int arg10 = 0x00; // value low - unused for read
    int arg11 = 0x00; // value high - unused for read
    int arg12 = 0x00; // command data

    return new LoconetMessage(
            LoconetMessage.OPC_IMM_PACKET,
            arg1, arg2, arg3, arg4,
            arg5, arg6, arg7, arg8,
            arg9, arg10, arg11, arg12);
  }

  public static LoconetMessage endLNCVProgramming(int article, int module) {
    int articleLow = article & 0xFF;
    int articleHigh = (article >> 8) & 0xFF;

    int moduleLow = module & 0xFF;
    int moduleHigh = (module >> 8) & 0xFF;

    int commandData = 0x40;

    int pxct = 0;

    if ((articleLow & 0x80) != 0) {
      pxct |= 0x01;
    }
    if ((articleHigh & 0x80) != 0) {
      pxct |= 0x02;
    }

    // LNCV low/high are both zero.
    if ((moduleLow & 0x80) != 0) {
      pxct |= 0x10;
    }
    if ((moduleHigh & 0x80) != 0) {
      pxct |= 0x20;
    }
    if ((commandData & 0x80) != 0) {
      pxct |= 0x40;
    }

    int arg1 = 0x01;
    int arg2 = 0x05;
    int arg3 = 0x00;
    int arg4 = 0x21;
    int arg5 = pxct;
    int arg6 = articleLow & 0x7F;
    int arg7 = articleHigh & 0x7F;
    int arg8 = 0x00; // LNCV low
    int arg9 = 0x00; // LNCV high
    int arg10 = moduleLow & 0x7F;
    int arg11 = moduleHigh & 0x7F;
    int arg12 = commandData & 0x7F;

    return new LoconetMessage(
            LoconetMessage.OPC_PEER_XFER,
            arg1, arg2, arg3, arg4,
            arg5, arg6, arg7, arg8,
            arg9, arg10, arg11, arg12);
  }

  public static void main(String[] a) {
    System.out.println(requestS88ReportAddress());
  }

}
