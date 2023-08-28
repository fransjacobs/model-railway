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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import jcs.util.ByteUtil;

/**
 * CS 2/3 CAN message.
 */
public class CanMessage implements MarklinCan, Serializable {

  private int priority;
  private int command;
  private short hash;
  private int dlc;
  private byte[] data;

  private final List<CanMessage> responses;

  public CanMessage(byte[] message) {
    this.data = new byte[DATA_SIZE];
    this.responses = new LinkedList<>();
    if (message != null && message.length == MESSAGE_SIZE) {
      this.priority = message[0];
      this.command = message[1];
      byte[] h = new byte[HASH_SIZE];
      h[0] = message[2];
      h[1] = message[3];
      this.hash = (short) toInt(h);
      this.dlc = message[4];
      System.arraycopy(message, 5, data, 0, DATA_SIZE);
    }
  }

  public CanMessage(int priority, int command, int hash, int dlc, byte[] data) {
    this(priority, command, to2Bytes(hash), dlc, data);
  }

  public CanMessage(int priority, int command, byte[] hash, int dlc, byte[] data) {
    this.responses = new LinkedList<>();
    this.priority = priority;
    this.command = command;

    if (hash == null) {
      this.hash = (short) toInt(MAGIC_HASH);
    } else {
      this.hash = (short) toInt(hash);
    }
    this.dlc = dlc;

    this.data = new byte[DATA_SIZE];

    if (data != null && data.length == DATA_SIZE) {
      this.data = data;
    } else if (data != null) {
      System.arraycopy(data, 0, this.data, 0, DATA_SIZE);
    }
  }

  public static final int toInt(byte[] value) {
    int val = -1;
    if (value != null) {
      val = switch (value.length) {
        case 2 ->
          ((value[0] & 0xFF) << 8) | (value[1] & 0xFF);
        case 4 ->
          ((value[0] & 0xFF) << 24)
          | ((value[1] & 0xFF) << 16)
          | ((value[2] & 0xFF) << 8)
          | (value[3] & 0xFF);
        default ->
          0;
      };
    }
    return val;
  }

  public static String toString(byte[] data) {
    return new String(data);
  }

  public static final byte[] to2Bytes(int value) {
    byte[] bts = new byte[]{
      (byte) ((value >> 8) & 0xFF),
      (byte) (value & 0XFF)};

    return bts;
  }

  public static final byte[] to4Bytes(int value) {
    byte[] bts = new byte[]{
      (byte) ((value >> 24) & 0xFF),
      (byte) ((value >> 16) & 0xFF),
      (byte) ((value >> 8) & 0xFF),
      (byte) (value & 0XFF)};

    return bts;
  }

  public int getLength() {
    return MESSAGE_SIZE;
  }

  public int getDataLength() {
    return DATA_SIZE;
  }

  public byte[] getMessage() {
    byte[] msg = new byte[MESSAGE_SIZE];

    msg[0] = (byte) this.priority;
    msg[1] = (byte) this.command;
    byte[] h = to2Bytes(this.hash);
    msg[2] = h[0];
    msg[3] = h[1];
    msg[4] = (byte) this.dlc;
    System.arraycopy(this.data, 0, msg, DATA_IDX, this.data.length);
    return msg;
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

  public int getPriority() {
    return this.priority;
  }

  public int getCommand() {
    return this.command;
  }

  public int getSubCommand() {
    return data[SUB_CMD_IDX];
  }

  public byte[] getHash() {
    return to2Bytes(this.hash);
  }

  public int getDlc() {
    return this.dlc;
  }

  public final byte getDataByte(int position) {
    return this.data[position];
  }

  public int getPackageNumber() {
    if (STATUS_CONFIG_RESP == this.command) {
      byte[] h = CanMessage.to2Bytes(this.hash);
      return h[1];
    } else {
      return 0;
    }
  }

  public byte[] getData() {
    return this.data;
  }

  public boolean isResponseMessage() {
    int cmd = this.command;
    cmd = cmd & 0x01;
    return cmd == 1;
  }

//  public boolean isAdministrativeMessage() {
//    int cmd = getCommand();
//    return cmd == DISCOVERY_COMMAND_ID
//            || cmd == MFX_BIND_COMMAND || cmd == MFX_VERIFY_COMMAND
//            || cmd == LOC_VELOCITY || cmd == LOC_VELOCITY_RESP
//            || cmd == LOC_DIRECTION || cmd == LOC_DIRECTION_RESP
//            || cmd == LOC_FUNCTION || cmd == LOC_FUNCTION_RESP
//            || cmd == LOC_DECODER_READ || cmd == LOC_DECODER_READ_RESP
//            || cmd == LOC_DECODER_WRITE || cmd == LOC_DECODER_WRITE_RESP;
//  }
//  public boolean isGuiInfoMessage() {
//    return this.command == REQUEST_CONFIG_DATA || this.command == REQUEST_CONFIG_DATA_RESP
//            || this.command == CONFIG_DATA_STREAM
//            || this.command == CON_60128_DATA_STREAM;
//  }
//  public boolean isUpdateMessage() {
//    return this.command == PING_REQ || this.command == PING_RESP
//            || this.command == UPDATE_OFFER || this.command == UPDATE_OFFER_RESP
//            || this.command == READ_CONFIG_DATA || this.command == READ_CONFIG_DATA_RESP
//            || this.command == BOOTLOADER_CAN || this.command == BOOTLOADER_LOC
//            || this.command == LOC_FUNCTION || this.command == LOC_FUNCTION_RESP
//            || this.command == STATUS_CONFIG || this.command == STATUS_CONFIG_RESP;
//  }
  public boolean expectsResponse() {
    return this.command == SYSTEM_COMMAND
            || this.command == MFX_BIND_COMMAND
            || this.command == MFX_VERIFY_COMMAND
            || this.command == LOC_VELOCITY
            || this.command == LOC_DIRECTION
            || this.command == LOC_FUNCTION
            || this.command == LOC_DECODER_READ
            || this.command == LOC_DECODER_WRITE
            || this.command == ACCESSORY_SWITCHING
            || this.command == S88_EVENT
            || this.command == PING_REQ
            || this.command == STATUS_CONFIG
            || this.command == REQUEST_CONFIG_DATA;
  }

  public boolean expectsLargeResponse() {
    return this.command == STATUS_CONFIG
            || this.command == REQUEST_CONFIG_DATA;
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

  public boolean isPingResponse() {
    return this.command == PING_RESP;
  }

  public boolean isPingRequest() {
    return this.command == PING_REQ;
  }

  public boolean isStatusConfigRequest() {
    return this.command == STATUS_CONFIG;
  }

  public boolean isSensorResponse() {
    return this.command == S88_EVENT_RESP;
  }

  public boolean isStatusDataConfigMessage() {
    return this.command == STATUS_CONFIG | this.command == STATUS_CONFIG + 1;
  }

  public boolean isSensorMessage() {
    return this.command == S88_EVENT || this.command == SX1_EVENT;
  }

  public boolean isAccessoryMessage() {
    return this.command == ACCESSORY_SWITCHING || this.command == ACCESSORY_SWITCHING_RESP;
  }

  public boolean isLocomotiveMessage() {
    return this.command == LOC_VELOCITY || this.command == LOC_VELOCITY_RESP
            || this.command == LOC_DIRECTION || this.command == LOC_DIRECTION_RESP
            || this.command == LOC_FUNCTION || this.command == LOC_FUNCTION_RESP;
  }

  public boolean isSystemMessage() {
    return this.command == SYSTEM_COMMAND || this.command == SYSTEM_COMMAND_RESP;
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
    byte[] uid = getDeviceUidFromMessage();
    int did = toInt(uid);
    return did > 0;
  }

  public byte[] getDeviceUidFromMessage() {
    byte[] uid = new byte[4]; //UID is 4 bytes long
    System.arraycopy(this.data, 0, uid, 0, uid.length);
    return uid;
  }

  public int getDeviceUidNumberFromMessage() {
    return toInt(getDeviceUidFromMessage());
  }

  public String responseString() {
    return this.responses.get(0).toString();
  }

  public boolean isResponseComplete() {
    if (!(this.expectsLargeResponse() || this.expectsResponse())) {
      return true;
    } else if (this.responses.isEmpty()) {
      return false;
    } else {
      if (this.expectsLargeResponse()) {
        //depending on the message
        switch (this.command) {
          case STATUS_CONFIG -> {
            //Should have at least 5 responses and the last response has dlc 6 (cs3) or dlc 5 (cs2) 
            CanMessage r = this.responses.get(this.responses.size() - 1);
            int rdlc = r.getDlc();
            return rdlc == DLC_5 || rdlc == DLC_6;
          }
          case REQUEST_CONFIG_DATA -> {
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
                } else {
                  return false;
                }
              } else {
                return false;
              }
            } else {
              return false;
            }
          }
          case PING_REQ -> {
            return this.responses.size() >= 2;
          }
          default -> {
            if (this.expectsResponse()) {
              CanMessage r0 = this.responses.get(0);
              return this.command == r0.getCommand() - 1;
            } else {
              return true;
            }
          }

        }
      }
      return false;
    }
  }

  @Override
  public String toString() {
    return ByteUtil.toHexString(this.getMessage());
  }

  public int getNumberOfMeasurementValues() {
    if (this.isResponseMessage()) {
      //int cmd = this.getCommand();
      if ((command & 0xFe) == STATUS_CONFIG) {
        //get the 1st data byte
        return this.data[0];
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
  public static byte[] calcHash(int[] uid) {
    int hash = calcHash(ByteUtil.toInt(uid));
    return to2Bytes(hash);
  }

  public static final int generateHashInt(int gfpUid) {
    int msb = gfpUid >> 16;
    int lsb = gfpUid & 0xffff;
    int hash = msb ^ lsb;
    hash = (((hash << 3) & 0xFF00) | 0x0300) | (hash & 0x7F);
    return hash;
  }

  public static final byte[] generateHash(int gfpUid) {
    int hash = generateHashInt(gfpUid);
    return to2Bytes(hash);
  }

  @Override
  public int hashCode() {
    int h = 5;
    h = 19 * h + this.priority;
    h = 19 * h + this.command;
    h = 19 * h + this.hash;
    h = 19 * h + this.dlc;
    h = 19 * h + Arrays.hashCode(this.data);
    return h;
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
    if (this.priority != other.priority) {
      return false;
    }
    if (this.command != other.command) {
      return false;
    }
    if (this.hash != other.hash) {
      return false;
    }
    if (this.dlc != other.dlc) {
      return false;
    }
    return Arrays.equals(this.data, other.data);
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
