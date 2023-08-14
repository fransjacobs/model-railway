/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.controller.cs.can;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import jcs.util.ByteUtil;
import static jcs.util.ByteUtil.toInt;

/**
 * CS 2/3 CAN message.
 */
public class CanMessage implements MarklinCan, Serializable {


  private final int[] message;
//TODO replace the 13 int array by the normal properties
//  private int priority;
//  private int command;
//  private short hash;
//  private int dlc;
//  private int[] data;

  private final List<CanMessage> responses;

  public CanMessage(byte[] message) {
    this.message = getEmptyMessage();
    this.responses = new ArrayList<>();
    for (int i = 0; i < this.message.length; i++) {
      this.message[i] = message[i] & 0xFF;
    }
  }

  public CanMessage(int priority, int command, int hash, int dlc, int[] data) {
    this(priority, command, ByteUtil.to2ByteArray(hash), dlc, data);
  }

  public CanMessage(int priority, int command, int[] hash, int dlc, int[] data) {
    this.message = getEmptyMessage();
    this.responses = new ArrayList<>();
    this.message[PRIO_IDX] = priority;
    this.message[CMD_IDX] = command;
    if (hash == null) {
      System.arraycopy(MarklinCan.MAGIC_HASH, 0, this.message, HASH_IDX, MarklinCan.MAGIC_HASH.length);
    } else {
      System.arraycopy(hash, 0, this.message, HASH_IDX, hash.length);
    }
    this.message[DLC_IDX] = dlc;
    System.arraycopy(data, 0, this.message, DATA_IDX, data.length);
  }

  public int getLength() {
    return MESSAGE_SIZE;
  }

  public int getDataLength() {
    return DATA_SIZE;
  }

  public int[] getMessage() {
    return this.message;
  }

  public void addResponse(CanMessage reply) {
    if (reply != null) {
      this.responses.add(reply);
    }
  }

  public List<CanMessage> getResponses() {
    return this.responses;
  }

  public CanMessage getResponse(int idx) {
    if (idx < this.responses.size()) {
      return this.responses.get(idx);
    } else {
      return null;
    }
  }

  public CanMessage getResponse() {
    //Figure out the best response for the message as there could be multiple responses...
    int txUid = this.getDeviceUidNumberFromMessage();
    for (int i = 0; i < responses.size(); i++) {
      CanMessage resp = responses.get(i);
      int rxUid = resp.getDeviceUidNumberFromMessage();
      if (txUid == rxUid) {
        //found a response with same uid as sent
        if (txUid == 0) {
          //lets continue
        } else {
          return resp;
        }
      } else {
        return resp;
      }
    }

    return this.getResponse(0);
  }

  public byte[] getBytes() {
    byte[] bytes = new byte[MESSAGE_SIZE];
    for (int i = 0; i < bytes.length; i++) {
      byte b = (byte) (this.message[i] & 0xFF);
      bytes[i] = b;
    }
    return bytes;
  }

  public int getPriority() {
    return this.message[PRIO_IDX];
  }

  public int getCommand() {
    return this.message[CMD_IDX];
  }

  public int getSubCommand() {
    return this.message[SUB_CMD_IDX];
  }

  public int[] getHash() {
    int[] h = new int[HASH_SIZE];
    System.arraycopy(message, HASH_IDX, h, 0, HASH_SIZE);
    return h;
  }

  public int getDlc() {
    return this.message[DLC_IDX];
  }

  public final int getDataByte(int position) {
    return this.message[DATA_IDX + position];
  }

  public int getPackageNumber() {
    return this.message[PACKAGE_IDX];

  }

  public int[] getData() {
    int[] dt = new int[DATA_SIZE];
    System.arraycopy(message, DATA_IDX, dt, 0, DATA_SIZE);
    return dt;
  }

  public byte[] getDataBytes() {
    int[] dt = this.getData();
    byte[] db = new byte[dt.length];
    for (int i = 0; i < dt.length; i++) {
      byte b = (byte) (dt[i] & 0xFF);
      db[i] = b;
    }
    return db;
  }

  public boolean isResponseMessage() {
    int cmd = getCommand();
    cmd = cmd & 0x01;
    return cmd == 1;
  }

  public boolean isAdministrativeMessage() {
    int cmd = getCommand();
    return cmd == DISCOVERY_COMMAND_ID
            || cmd == MFX_BIND_COMMAND || cmd == MFX_VERIFY_COMMAND
            || cmd == LOC_VELOCITY || cmd == LOC_VELOCITY_RESP
            || cmd == LOC_DIRECTION || cmd == LOC_DIRECTION_RESP
            || cmd == LOC_FUNCTION || cmd == LOC_FUNCTION_RESP
            || cmd == LOC_DECODER_READ || cmd == LOC_DECODER_READ_RESP
            || cmd == LOC_DECODER_WRITE || cmd == LOC_DECODER_WRITE_RESP;
  }

  public boolean isGuiInfoMessage() {
    int cmd = getCommand();
    return cmd == REQUEST_CONFIG_DATA || cmd == REQUEST_CONFIG_DATA_RESP
            || cmd == CONFIG_DATA_STREAM
            || cmd == CON_60128_DATA_STREAM;
  }

  public boolean isSensorMessage() {
    int cmd = getCommand();
    return cmd == S88_EVENT || cmd == SX1_EVENT;
  }

  public boolean isSwitchingMessage() {
    int cmd = getCommand();
    return cmd == ACCESSORY_SWITCHING || cmd == ACCESSORY_SWITCHING_RESP;
  }

  public boolean isUpdateMessage() {
    int cmd = getCommand();
    return cmd == PING_REQ || cmd == PING_RESP
            || cmd == UPDATE_OFFER || cmd == UPDATE_OFFER_RESP
            || cmd == READ_CONFIG_DATA || cmd == READ_CONFIG_DATA_RESP
            || cmd == BOOTLOADER_CAN || cmd == BOOTLOADER_LOC
            || cmd == LOC_FUNCTION || cmd == LOC_FUNCTION_RESP
            || cmd == STATUS_CONFIG || cmd == STATUS_CONFIG_RESP;
  }

  public boolean expectsAcknowledge() {
    int cmd = this.getCommand();

    return cmd == SYSTEM_COMMAND
            || cmd == MFX_BIND_COMMAND
            || cmd == MFX_VERIFY_COMMAND
            || cmd == LOC_VELOCITY
            || cmd == LOC_DIRECTION
            || cmd == LOC_FUNCTION
            || cmd == LOC_DECODER_READ
            || cmd == LOC_DECODER_WRITE
            || cmd == ACCESSORY_SWITCHING
            || cmd == S88_EVENT
            || cmd == PING_REQ
            || cmd == STATUS_CONFIG
            || cmd == REQUEST_CONFIG_DATA;
  }

  public boolean expectsLargeResponse() {
    int cmd = this.getCommand();
    return cmd == STATUS_CONFIG
            || cmd == REQUEST_CONFIG_DATA;
  }

  public boolean isResponseFor(CanMessage other) {
    //Check what is the is the response message
    int cmd = this.getCommand();
    int ocmd = other.getCommand();

    //normal response is the cmd + 1
    if (cmd - 1 == ocmd) {
      return true;
    } else if (cmd == CONFIG_DATA_STREAM && ocmd == REQUEST_CONFIG_DATA) {
      return true;
    } else {
      return cmd == PING_RESP && ocmd == STATUS_CONFIG;
    }
  }

  public boolean isAcknowledgeFor(CanMessage other) {
    if (isResponseFor(other)) {
      int[] base = new int[CanMessage.MESSAGE_SIZE];
      System.arraycopy(other.getMessage(), 0, base, 0, base.length);

      int[] ackm = new int[CanMessage.MESSAGE_SIZE];
      System.arraycopy(this.message, 0, ackm, 0, ackm.length);

      ackm[CMD_IDX] = base[CMD_IDX];
      return Objects.deepEquals(ackm, base);
    }
    return false;
  }

  public boolean isPingResponse() {
    return this.getCommand() == PING_RESP;
  }

  public boolean isPingRequest() {
    return this.getCommand() == PING_REQ;
  }

  public boolean isStatusConfigRequest() {
    return this.getCommand() == STATUS_CONFIG;
  }

  public boolean isSensorResponse() {
    return this.getCommand() == S88_EVENT_RESP;
  }

  public boolean isStatusDataConfigMessage() {
    return getCommand() == STATUS_CONFIG | getCommand() == STATUS_CONFIG + 1;
  }

  public boolean hasValidResponse() {
    if (this.responses == null || this.responses.isEmpty()) {
      return false;
    }
    //get the first response
    CanMessage response = responses.get(0);
    //compare the messsage
    int cmd = response.getCommand();
    //check the response bit
    cmd = cmd & 0x01;
    return cmd == 1;
  }

  public boolean isDeviceUidValid() {
    int[] uid = getDeviceUidFromMessage();
    int did = toInt(uid);
    return did > 0;
  }

  public int getUidInt() {
    int[] dt = getData();

    return ((dt[0] & 0xFF) << 24)
            | ((dt[1] & 0xFF) << 16)
            | ((dt[2] & 0xFF) << 8)
            | (dt[3] & 0xFF);
  }

  public int[] getDeviceUidFromMessage() {
    int[] uid = new int[4]; //UID is 4 bytes long
    System.arraycopy(this.message, DATA_IDX, uid, 0, uid.length);
    return uid;
  }

  public int getDeviceUidNumberFromMessage() {
    return toInt(getDeviceUidFromMessage());
  }

  public String responseString() {
    return this.responses.get(0).toString();
  }

  public boolean isResponseComplete() {
    int cmd = getCommand();

    if (!(this.expectsLargeResponse() || this.expectsAcknowledge())) {
      return true;
    } else if (this.responses.isEmpty()) {
      return false;
    } else {
      if (this.expectsLargeResponse()) {
        //depending on the message
        if (cmd == STATUS_CONFIG) {
          //Should have at least 5 responses and the last response has dlc 6 (cs3) or dlc 5 (cs2) 
          CanMessage r = this.responses.get(this.responses.size() - 1);
          int rdlc = r.getDlc();
          return rdlc == DLC_5 || rdlc == DLC_6;
        } else if (cmd == REQUEST_CONFIG_DATA) {
          if (!this.responses.isEmpty()) {
            //Get the ack
            CanMessage ackm = this.responses.get(0);
            boolean ack = REQUEST_CONFIG_DATA_RESP == ackm.getCommand();
            if (ack && this.responses.size() > 2) {
              CanMessage lenm = this.responses.get(1);
              if (CONFIG_DATA_STREAM == lenm.getCommand() && DLC_6 == lenm.getDlc()) {
                int dataLength = lenm.getDeviceUidNumberFromMessage();
                int rspMessages = dataLength / 8;
                rspMessages = rspMessages + 2;
                int respSize = this.responses.size();
                return respSize == rspMessages;
              }
              return false;
            } else {
              return false;
            }
          } else {
            return false;
          }
        }
      } else if (this.expectsAcknowledge()) {
        CanMessage r = this.responses.get(0);
        return cmd == r.getCommand() - 1;
      } else {
        return false;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return ByteUtil.toHexString(this.message);
  }

  public int getNumberOfMeasurementValues() {
    if (this.isResponseMessage()) {
      int command = this.getCommand();
      if ((command & 0xFe) == MarklinCan.STATUS_CONFIG) {
        //get the 1st data byte
        return this.getData()[0];
      }
    }
    return -1;
  }

  /**
   * The hash fulfills a double function: It primarily serves to resolve the collisions of the messages and to ensure that there are no collisions with the CS1 protocol. Secondarily, it can contain
   * the sequence number of a data transmission. No collisions with the CS1 protocol: In the CAN protocol of the CS1, the value 6 for the "com area of ​​the ID", these are the bits 7..9, i.e. the
   * highest bit in the lowest byte (0b0xxxxxxx) and the two bits above it (0bxxxxxx11), is not used.
   *
   * This bit combination is therefore used for differentiation in the hash. collision resolution: The hash is used to make the CAN messages collision-free with a high probability. This 16-bit value
   * is formed from the UID hash. Calculation: 16-bit high UID XOR 16-bit low of the UID. Then the bits are set according to the CS1 distinction.
   *
   * Each participant on the bus has to check the hash of received CAN messages to ensure that they are free of collisions. If your own hash is received, a new one must be chosen. This must not match
   * any other received. Sequence number of a data transfer: If the hash is used to identify the package number, these bits are hidden when the package number is calculated. i.e. With the 16-bit
   * number, bits 7 to 9 are hidden, the top 3 bits are 0. The range of values ​​is reduced accordingly to 8192.
   *
   * @param uid the uid to calculate the hash over
   * @return an integer representing the hash value
   */
  public static int calcHash(int uid) {
    int calc = (uid >> 16) ^ (uid & 0xFFFF);
    int hash = ((calc << 3) | 0x0300) & 0xFF00;
    hash |= (calc & 0x007F);
    return hash;
  }

  /**
   * Calculate the hash value
   *
   * @param uid the UID as an int array of 4 bytes
   * @return an int array of 2 bytes
   */
  public static int[] calcHash(int[] uid) {
    int hash = calcHash(ByteUtil.toInt(uid));
    return ByteUtil.to2ByteArray(hash);
  }

  /**
   * Create an empty data array of 8 bytes filled with zeros
   *
   * @return and Array of int[8] filled with 0 in each position
   */
  public static int[] getEmptyData() {
    int[] data = new int[CanMessage.DATA_SIZE];
    //Enshure it is filled with 0x00
    for (int i = 0; i < data.length; i++) {
      data[i] = 0;
    }
    return data;
  }

  public static final int generateHashInt(int gfpUid) {
    int msb = gfpUid >> 16;
    int lsb = gfpUid & 0xffff;
    int hash = msb ^ lsb;
    hash = (((hash << 3) & 0xFF00) | 0x0300) | (hash & 0x7F);
    return hash;
  }

  public static final int[] generateHash(int gfpUid) {
    int hash = generateHashInt(gfpUid);
    return ByteUtil.to2ByteArray(hash);
  }

  private static int[] getEmptyMessage() {
    int[] msg = new int[MESSAGE_SIZE];
    //Enshure it is filled with 0x00
    for (int i = 0; i < msg.length; i++) {
      msg[i] = 0;
    }
    return msg;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 47 * hash + Arrays.hashCode(this.message);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CanMessage other = (CanMessage) obj;
    return Arrays.equals(this.message, other.message);
  }
}

//  public String print() {
//    StringBuilder sb = new StringBuilder();
//    sb.append(getMessageName());
//    sb.append(", RB: ");
//    sb.append((this.isResponseMessage() ? "1" : "0"));
//    sb.append(": ");
//    sb.append(toString());
//    return sb.toString();
//  }
//  //TODO: NEEDS FIXING
//  public String getMessageName() {
//    int cmd = getCommand();
//    switch (cmd) {
//      case MarklinCan.SYSTEM_COMMAND:
//        int subcmd = this.getSubCommand();
//        switch (subcmd) {
//          case MarklinCan.STOP_SUB_CMD:
//            int dlc = this.getDlc();
//            if (dlc == MarklinCan.DLC_4) {
//              return "Query System";
//            } else {
//              return "Stop";
//            }
//          case MarklinCan.GO_SUB_CMD:
//            return "Go";
//          case MarklinCan.HALT_SUB_CMD:
//            return "Halt";
//          case MarklinCan.LOC_STOP_SUB_CMD:
//            return "loc emergency stop";
//          case MarklinCan.SYSTEM_SUB_LOC_CYCLE_STOP:
//            return "loc cycle stop";
//          case MarklinCan.SYSTEM_SUB_LOC_PROTOCOL:
//            return "loc data protocol";
//          case MarklinCan.SYSTEM_SUB_SWITCH_TIME_ACCESSORY:
//            return "Accessory switch time";
//          case MarklinCan.SYSTEM_SUB_MFX_FAST_READ:
//            return "MFX Fast read";
//          case MarklinCan.SYSTEM_SUB_UNLOCK_TRACK_PROTOCOL:
//            return "Unlock Track Protocol";
//          case MarklinCan.SYSTEM_SUB_MFX_REG_CNT:
//            return "MFX Reg count";
//          case MarklinCan.OVERLOAD_SUB_CMD:
//            return "System overload";
//          case MarklinCan.SYSTEM_SUB_SYSTEM_ID:
//            return "System ID";
//          case MarklinCan.SYSTEM_SUB_MFX_SEEK:
//            return "MFX Seek";
//          case MarklinCan.SYSTEM_SUB_SYSTEM_RESET:
//            return "System Reset";
//          default:
//            return "Unknown " + cmd + ", " + subcmd;
//        }
//      case MarklinCan.LOC_DISCOVERY_COMMAND:
//        return "Loc Discovery";
//      case MarklinCan.MFX_BIND_COMMAND:
//        return "Loc Discovery";
//      case MarklinCan.MFX_VERIFY_COMMAND:
//        return "MFX Verify";
//      case MarklinCan.LOC_VELOCITY:
//        return "Loc Velocity";
//      case MarklinCan.LOC_DIRECTION:
//        return "Loc Direction";
//      case MarklinCan.LOC_FUNCTION:
//        return "Loc Function";
//      case MarklinCan.READ_CONFIG:
//        return "Read Config";
//      case MarklinCan.WRITE_CONFIG:
//        return "Write Config";
//      case MarklinCan.ACCESSORY_SWITCHING:
//        return "Switch Accessory";
//      case MarklinCan.ACCESSORY_CONFIG:
//        return "Accessory Config";
//      case MarklinCan.S88_EVENT:
//        return "S88 Event";
//      case MarklinCan.S88_EVENT_RESPONSE:
//        return "S88 Event Response";
//      case MarklinCan.SX1_EVENT:
//        return "SX1 Event";
//      case MarklinCan.PING_REQ:
//        return "Member Ping";
//      case MarklinCan.REQ_PING:
//        return "Member Ping response";
//      case MarklinCan.UPDATE_OFFER:
//        return "Update offer";
//      case MarklinCan.READ_CONFIG_DATA:
//        return "Read Config data";
//      case MarklinCan.BOOTLOADER_CAN_SERVICE:
//        return "CAN Bootloader";
//      case MarklinCan.BOOTLOADER_TRACK_SERVICE:
//        return "Track Bootloader";
//      case MarklinCan.STATUS_CONFIG:
//        return "Status Config";
//      case MarklinCan.REQUEST_CONFIG_DATA:
//        return "Config data request";
//      case MarklinCan.CONFIG_DATA_STREAM:
//        return "Config data stream";
//      case MarklinCan.CON_60128_DATA_STREAM:
//        return "60128 data stream";
//
//      default:
//        return "Unknown: " + cmd;
//    }
//  }
