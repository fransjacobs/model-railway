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
import jcs.util.ByteUtil;

/**
 * Loconet Message.<br>
 * Inspired on the work from Thomas Kurz in 2007
 */
public final class LoconetMessage implements Opcodes {

  protected int[] message = new int[0];

  public LoconetMessage() {
  }

  /**
   * 2 Byte Message Constructor
   */
  public LoconetMessage(int opcode) {
    this.message = new int[]{opcode, 0};
    calculateChecksum();
    String chk = checkMessage();
    if (chk != null) {
      throw new IllegalArgumentException(chk);
    }
  }

  /**
   * 4 Byte Message Constructor
   */
  public LoconetMessage(int opcode, int param1, int param2) {
    setMsg4Byte(opcode, param1, param2);
  }

  private void setMsg4Byte(int opcode, int param1, int param2) {
    this.message = new int[]{opcode, param1, param2, 0};
    calculateChecksum();
    String chk = checkMessage();
    if (chk != null) {
      throw new IllegalArgumentException(chk);
    }
  }

  /**
   * 6 Byte Message Constructor
   */
  public LoconetMessage(int opcode, int param1, int param2, int param3, int param4) {
    this.message = new int[]{opcode, param1, param2, param3, param4, 0};
    calculateChecksum();
    String chk = checkMessage();
    if (chk != null) {
      throw new IllegalArgumentException(chk);
    }
  }

  /**
   * N Bytes length Message Constructor
   */
  public LoconetMessage(int opcode, int... aParams) {
    int iLength = aParams.length + 3;  // 3 for opcode/length/checkbyte
    this.message = new int[iLength];
    this.message[0] = opcode;
    this.message[1] = iLength;
    System.arraycopy(aParams, 0, this.message, 2, aParams.length);
    calculateChecksum();
    String chk = checkMessage();
    if (chk != null) {
      throw new IllegalArgumentException(chk);
    }
  }

  public int[] getMessage() {
    return Arrays.copyOf(message, message.length);
  }

  public byte[] getMessageBytes() {
    byte[] msg = new byte[message.length];
    for (int i = 0; i < message.length; i++) {
      msg[i] = (byte) message[i];
    }
    return msg;
  }

  public int getLength() {
    return message.length;
  }

  public int getLengthByOpcode() {
    switch (getOpcode() & 0x60) {
      case 0x00:
        return 2;
      case 0x20:
        return 4;
      case 0x40:
        return 6;
      default:
      case 0x60:
        return message[1];
    }
  }

  protected String checkMessage() {
    if (message.length < 2) {
      return "Message too short";
    }
    if (!getMsb(getOpcode())) {
      return "First byte is not an opcode";
    }
    for (int i = 1; i < message.length; i++) {
      if (getMsb(message[i])) {
        return "Data bytes after opcode may not have bit 7 set";
      }
    }
    if (getLengthByOpcode() != getLength()) {
      return "Length as specified by opcode differes with actual byte count";
    }
    if (!checkChecksum()) {
      return "Invalid checksum";
    }
    return null;
  }

  protected static boolean getMsb(int value, int index) {
    return getMsb(value >> (index * 8));
  }

  protected static boolean getMsb(int value) {
    return (value & 0x80) == 0x80;
  }

  protected static byte getLs7b(int value, int index) {
    return getLs7b(value >> (index * 8));
  }

  protected static byte getLs7b(int value) {
    return (byte) (value & 0x7F);
  }

  protected static int changeBit(int number, int index, boolean value) {
    if (value) {
      return number | (1 << index);
    } else {
      return number & (~(1 << index));
    }
  }

  protected static boolean isBitSet(int number, int index) {
    return (number & (1 << index)) != 0;
  }

  public void setMessage(int[] message, int length) {
    this.message = message;
    if (message.length != length) {
      throw new IllegalArgumentException("Data array length (" + message.length + ") is not equal to " + length + "!");
    }
  }

  public int getOpcode() {
    return message[0];
  }

  public int getDataByte(int pos) {
    if (pos == 0) {
      return -1;
    }
    if (pos < (message.length - 1)) {
      return message[pos];
    } else {
      return -1;
    }
  }

  public int getNumOfData() {
    return message.length - 2;
  }

  public void setMsgOpcSwReq(int address, boolean dirFlag, boolean pwrOnFlag) {
    int dataBytes1 = address & 0x7F;
    int dataBytes2 = (address & 0x0780) >> 7;

    if (dirFlag) {
      dataBytes2 |= 0x20;
    }
    if (pwrOnFlag) {
      dataBytes2 |= 0x10;
    }

    setMsg4Byte(OPC_SW_REQ, dataBytes1, dataBytes2);
  }

  public void setMsgOpcSwRep(int address, boolean dirFlag, boolean pwrOnFlag) {
    int dataBytes1 = address & 0x7F;
    int dataBytes2 = (address & 0x0780) >> 7;

    if (dirFlag) {
      dataBytes2 |= 0x20;
    }
    if (pwrOnFlag) {
      dataBytes2 |= 0x10;
    }

    setMsg4Byte(OPC_SW_REP, dataBytes1, dataBytes2);
  }

  public void setMsgOpcInpRep(int address, boolean state) {
    int dataBytes1 = address & 0x7F;
    int dataBytes2 = (address & 0x0780) >> 7;

    if (state) {
      dataBytes2 |= 0x10;
    }

    setMsg4Byte(OPC_INPUT_REP, dataBytes1, dataBytes2);
  }

  public int calculateChecksumValue() {
    int checksum = 0xFF;
    for (int i = 0; i < message.length - 1; i++) {
      checksum ^= message[i];
    }
    return checksum;
  }

  public void calculateChecksum() {
    message[message.length - 1] = calculateChecksumValue();
  }

  public boolean checkChecksum() {
    return message[message.length - 1] == calculateChecksumValue();
  }

  public String getHexString() {
    String retString = "";
    for (int i = 0; i < message.length; i++) {
      retString += " " + getByteHex(message[i]);
    }

    return retString;
  }

  @Override
  public String toString() {
    return ByteUtil.toHexString(message);
  }

  public static String getByteHex(int b) {
    return String.format("%02X", b & 0xFF);
  }

  public static String toString(byte[] data) {
    return new String(data);
  }

  public static String toString(byte[] data, int length) {
    byte[] stringData = new byte[length];
    System.arraycopy(data, 0, stringData, 0, stringData.length);
    return new String(stringData);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof LoconetMessage otherMessage)) {
      return false;
    }
    return Arrays.equals(this.message, otherMessage.message);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(message);
  }

  public boolean sameMessage(LoconetMessage other) {
    return other != null && Arrays.equals(this.message, other.message);
  }
}
