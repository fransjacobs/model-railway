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
package jcs.controller.cs3.can;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import jcs.util.ByteUtil;
import static jcs.util.ByteUtil.toInt;

/**
 * CS 2 CAN message.
 */
public class CanMessage implements MarklinCan, Serializable {

  public static final int MESSAGE_SIZE = 13;

  public static final int HASH_SIZE = 2;
  public static final int DATA_SIZE = 8;

  private static final int PRIO_IDX = 0;
  private static final int CMD_IDX = 1;
  private static final int SUB_CMD_IDX = 9;
  private static final int HASH_IDX = 2;
  private static final int DLC_IDX = 4;
  private static final int DATA_IDX = 5;

  private final int[] message;

  private final List<CanMessage> responses;

  private boolean expectResponse;

  public CanMessage() {
    this.message = getEmptyMessage();
    this.responses = new LinkedList<>();
  }

  public CanMessage(byte[] message) {
    this.message = getEmptyMessage();
    this.responses = new ArrayList<>();
    for (int i = 0; i < this.message.length; i++) {
      this.message[i] = message[i] & 0xFF;
    }
  }

  public CanMessage(int[] message) {
    this.message = getEmptyMessage();
    this.responses = new ArrayList<>();
    System.arraycopy(message, 0, this.message, 0, message.length);
  }

  public static CanMessage get(int[] message) {
    return new CanMessage(message);
  }

  public CanMessage(int priority, int command, int[] hash, int dlc, int[] data) {
    this(priority, command, hash, dlc, data, true);
  }

  public CanMessage(int priority, int command, int[] hash, int dlc, int[] data, boolean expectResponse) {
    this.message = getEmptyMessage();
    this.responses = new ArrayList<>();
    this.expectResponse = expectResponse;
    this.setPriority(priority);
    this.setCommand(command);
    this.setDlc(dlc);
    this.setData(data);

    if (hash == null) {
      setHash(MarklinCan.MAGIC_HASH);
    } else {
      this.setHash(hash);
    }
  }

  public int getLength() {
    return MESSAGE_SIZE;
  }

  public int[] getMessage() {
    return this.message;
  }

  public void addResponse(CanMessage reply) {
    if (reply != null) {
      this.responses.add(reply);
      //Logger.trace("Resp# " + this.responses.size() + " last Added " + reply);
    }
  }

  public List<CanMessage> getResponses() {
    return this.responses;
  }

  public CanMessage getResponse(int idx) {
    if (idx < this.responses.size()) {
      return this.responses.get(idx);
    } else {
      return new CanMessage();
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

  public void setMessage(int[] message) {
    System.arraycopy(message, 0, this.message, 0, message.length);
  }

  public final void setPriority(int priority) {
    this.message[PRIO_IDX] = priority;
  }

  public int getPriority() {
    return this.message[PRIO_IDX];
  }

  public final void setCommand(int command) {
    this.message[CMD_IDX] = command;
  }

  public int getCommand() {
    return this.message[CMD_IDX];
  }

  public int getSubCommand() {
    return this.message[SUB_CMD_IDX];
  }

  public int[] getHash() {
    int[] hash = new int[HASH_SIZE];
    System.arraycopy(message, HASH_IDX, hash, 0, HASH_SIZE);
    return hash;
  }

  public final void setHash(int[] hash) {
    System.arraycopy(hash, 0, this.message, HASH_IDX, hash.length);
  }

  public int getDlc() {
    return this.message[DLC_IDX];
  }

  public final void setDlc(int dlc) {
    this.message[DLC_IDX] = dlc;
  }

  public int[] getData() {
    int[] data = new int[DATA_SIZE];
    System.arraycopy(message, DATA_IDX, data, 0, DATA_SIZE);
    return data;
  }

  public byte[] getDataBytes() {
    int[] data = this.getData();
    byte[] db = new byte[data.length];
    for (int i = 0; i < data.length; i++) {
      byte b = (byte) (data[i] & 0xFF);
      db[i] = b;
    }
    return db;
  }

  public final void setData(int[] data) {
    System.arraycopy(data, 0, this.message, DATA_IDX, data.length);
  }

  public boolean isResponseMessage() {
    int command = getCommand();
    command = command & 0x01;
    return command == 1;
  }

  public boolean isResponseFor(CanMessage other) {
    //Check what is the is the response message
    int cmd = this.getCommand();
    int ocmd = other.getCommand();

    if ((cmd & 0x01) == 1) {
      //this is the response so other must be the message
      return (cmd - 1) == ocmd;
    } else if ((ocmd & 0x01) == 1) {
      //the other is a response
      return (ocmd - 1) == cmd;
    } else if (cmd == 0x31 && ocmd == 0x3a) {
      return true;
    } else {
      //no responses
      return false;
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

  public boolean expectsResponse() {
    return this.expectResponse;
  }

  public boolean isStatusDataConfigMessage() {
    return getCommand() == STATUS_CONFIG | getCommand() == STATUS_CONFIG + 1;
  }

  public boolean isEvent() {
    return !(getPriority() == PRIO_IGNORE
            | getCommand() == BOOTLOADER_CAN_SERVICE
            | getCommand() == REQ_PING
            | getCommand() == SW_STATUS_REQ
            | getCommand() == SW_STATUS_RESP
            | getCommand() == STATUS_CONFIG
            | (getCommand() == SYSTEM_COMMAND && getSubCommand() == SYSTEM_SUB_MFX_SEEK));
  }

  public boolean isSofwareStatusRequest() {
    return (getCommand() == SW_STATUS_REQ | getCommand() == SW_STATUS_RESP || getCommand() == STATUS_CONFIG);
  }

  public boolean expectsAcknowledge() {
    int cmd = this.getCommand();

    return switch (cmd) {
      case MarklinCan.STATUS_CONFIG ->
        true;
      default ->
        false;
    };
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
    int[] data = getData();

    return ((data[0] & 0xFF) << 24)
            | ((data[1] & 0xFF) << 16)
            | ((data[2] & 0xFF) << 8)
            | (data[3] & 0xFF);
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

  public boolean isExpectResponse() {
    return expectResponse;
  }

  public void setExpectResponse(boolean expectResponse) {
    this.expectResponse = expectResponse;
  }

  public boolean isResponseComplete() {
    int cmd = getCommand();
    boolean hasResp = !responses.isEmpty();
    switch (cmd) {
      case STATUS_CONFIG -> {
        if (!responses.isEmpty()) {
          int idx = this.responses.size() - 1;
          CanMessage m = this.responses.get(idx);
          return m.getDlc() == DLC_6;
        } else {
          return false;
        }
      }
      case REQUEST_CONFIG_DATA -> {
        if (!responses.isEmpty()) {
          int idx = this.responses.size() - 1;
          CanMessage m = this.responses.get(idx);
          return m.getDlc() == DLC_6;
        } else {
          return false;
        }
      }
      default -> {
        if (expectResponse) {
          return !this.responses.isEmpty();
        } else {
          return true;
        }
      }
    }
    //case CONFIG_DATA_STREAM:
    //    //TODO check
    //    return this.responses.size() > 0;
    //case CON_60128_DATA_STREAM:
    //    //TODO check
    //    return this.responses.size() > 0;
  }

  public String print() {
    StringBuilder sb = new StringBuilder();
    sb.append(getMessageName());
    sb.append(", RB: ");
    sb.append((this.isResponseMessage() ? "1" : "0"));
    sb.append(": ");
    sb.append(toString());
    return sb.toString();
  }

  //TODO: NEEDS FIXING
  public String getMessageName() {
    int cmd = getCommand();
    switch (cmd) {
      case MarklinCan.SYSTEM_COMMAND:
        int subcmd = this.getSubCommand();
        switch (subcmd) {
          case MarklinCan.STOP_SUB_CMD:
            int dlc = this.getDlc();
            if (dlc == MarklinCan.DLC_4) {
              return "Query System";
            } else {
              return "Stop";
            }
          case MarklinCan.GO_SUB_CMD:
            return "Go";
          case MarklinCan.HALT_SUB_CMD:
            return "Halt";
          case MarklinCan.LOC_STOP_SUB_CMD:
            return "loc emergency stop";
          case MarklinCan.SYSTEM_SUB_LOC_CYCLE_STOP:
            return "loc cycle stop";
          case MarklinCan.SYSTEM_SUB_LOC_PROTOCOL:
            return "loc data protocol";
          case MarklinCan.SYSTEM_SUB_SWITCH_TIME_ACCESSORY:
            return "Accessory switch time";
          case MarklinCan.SYSTEM_SUB_MFX_FAST_READ:
            return "MFX Fast read";
          case MarklinCan.SYSTEM_SUB_UNLOCK_TRACK_PROTOCOL:
            return "Unlock Track Protocol";
          case MarklinCan.SYSTEM_SUB_MFX_REG_CNT:
            return "MFX Reg count";
          case MarklinCan.OVERLOAD_SUB_CMD:
            return "System overload";
          case MarklinCan.SYSTEM_SUB_SYSTEM_ID:
            return "System ID";
          case MarklinCan.SYSTEM_SUB_MFX_SEEK:
            return "MFX Seek";
          case MarklinCan.SYSTEM_SUB_SYSTEM_RESET:
            return "System Reset";
          default:
            return "Unknown " + cmd + ", " + subcmd;
        }
      case MarklinCan.LOC_DISCOVERY_COMMAND:
        return "Loc Discovery";
      case MarklinCan.MFX_BIND_COMMAND:
        return "Loc Discovery";
      case MarklinCan.MFX_VERIFY_COMMAND:
        return "MFX Verify";
      case MarklinCan.LOC_VELOCITY:
        return "Loc Velocity";
      case MarklinCan.LOC_DIRECTION:
        return "Loc Direction";
      case MarklinCan.LOC_FUNCTION:
        return "Loc Function";
      case MarklinCan.READ_CONFIG:
        return "Read Config";
      case MarklinCan.WRITE_CONFIG:
        return "Write Config";
      case MarklinCan.ACCESSORY_SWITCHING:
        return "Switch Accessory";
      case MarklinCan.ACCESSORY_CONFIG:
        return "Accessory Config";
      case MarklinCan.S88_EVENT:
        return "S88 Event";
      case MarklinCan.S88_EVENT_RESPONSE:
        return "S88 Event Response";
      case MarklinCan.SX1_EVENT:
        return "SX1 Event";
      case MarklinCan.SW_STATUS_REQ:
        return "Member Ping";
      case MarklinCan.REQ_PING:
        return "Member Ping response";
      case MarklinCan.UPDATE_OFFER:
        return "Update offer";
      case MarklinCan.READ_CONFIG_DATA:
        return "Read Config data";
      case MarklinCan.BOOTLOADER_CAN_SERVICE:
        return "CAN Bootloader";
      case MarklinCan.BOOTLOADER_TRACK_SERVICE:
        return "Track Bootloader";
      case MarklinCan.STATUS_CONFIG:
        return "Status Config";
      case MarklinCan.REQUEST_CONFIG_DATA:
        return "Config data request";
      case MarklinCan.CONFIG_DATA_STREAM:
        return "Config data stream";
      case MarklinCan.CON_60128_DATA_STREAM:
        return "60128 data stream";

      default:
        return "Unknown: " + cmd;
    }
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

// Can probaly all cleaned up
//
//    public static final int generateHashInt(int[] gfpUid) {
//        if (gfpUid.length == 4) {
//            int[] h = new int[2];
//            int[] l = new int[2];
//            System.arraycopy(gfpUid, 0, h, 0, 2);
//            System.arraycopy(gfpUid, 2, l, 0, 2);
//
//            int hint = ByteUtil.toInt(h);
//            int lint = ByteUtil.toInt(l);
//
//            //Xor
//            int xor = hint ^ lint;
//
//            int msb = xor >> 16;
//            int lsb = xor & 0xffff;
//            int hash = msb ^ lsb;
//            hash = (((hash << 3) & 0xFF00) | 0x0300) | (hash & 0x7F);
//            return hash;
//        } else {
//            int uid = ByteUtil.toInt(gfpUid);
//            int msb = uid >> 16;
//            int lsb = uid & 0xffff;
//            int hash = msb ^ lsb;
//            hash = (((hash << 3) & 0xFF00) | 0x0300) | (hash & 0x7F);
//            return hash;
//        }
//
//    }
//
//    public static int[] to2ByteArray(int value) {
//        int[] bts = new int[]{
//            (value >> 8) & 0xFF,
//            value & 0XFF};
//
//        return bts;
//    }
//    public final int generateHashInt() {
//        int uid;
//        if (this.deviceUidNumber > 0) {
//            uid = this.deviceUidNumber;
//        } else {
//            uid = this.getUidInt();
//        }
//        return generateHashInt(uid);
//    }
//    public final int[] generateHash() {
//        int gh = generateHashInt();
//        int[] hash = to2ByteArray(gh);
//        return hash;
//    }
//    public final int getHashInt() {
//        int[] h = this.getHash();
//
//        return ((h[0] & 0xFF) << 8)
//                | ((h[1] & 0xFF));
//    }
//    public static int toInt(int[] value) {
//        int val;
//        switch (value.length) {
//            case 2:
//                val = ((value[0] & 0xFF) << 8) | (value[1] & 0xFF);
//                break;
//            case 4:
//                val = ((value[0] & 0xFF) << 24)
//                        | ((value[1] & 0xFF) << 16)
//                        | ((value[2] & 0xFF) << 8)
//                        | (value[3] & 0xFF);
//                break;
//            default:
//                val = 0;
//                break;
//        }
//        return val;
//    }
//    public static int[] to4ByteArray(int value) {
//        int[] bts = new int[]{
//            (value >> 24) & 0xFF,
//            (value >> 16) & 0xFF,
//            (value >> 8) & 0xFF,
//            value & 0XFF};
//
//        return bts;
//    }
//    private static String toHexString(int b) {
//        String h = Integer.toHexString((b & 0xff));
//        if (h.length() == 1) {
//            h = "0" + h;
//        }
//        return h;
//    }
//
//
//The CS2 response in my case with: "00 01 CB 13 05 43 53 9A 40 01 00 00 00".
//Notice the second byte should be 01 as a response and the first 4 data bytes hold the UID in my case "43 53 9A 40".
//I store the UID as an int so in my case: 1129552448.
//I calculate the hash as follows:
//int msb = uid >> 16;
//int lsb = uid & 0xffff;
//int hash = msb ^ lsb;
//hash = (((hash << 3) & 0xFF00) | 0x0300) | (hash & 0x7F);   
//
//  public static void main(String[] a) {
//
//    int[] gfp = new int[]{0x63, 0x73, 0x45, 0x8c};
//    int uid = ByteUtil.toInt(gfp);
//    int[] gfpHash = new int[]{0x03, 0x26};
//
//    int[] gui = new int[]{0x63, 0x73, 0x45, 0x8d};
//    int[] guiHash = new int[]{0x37, 0x7e};
//
//    int highword = uid >> 16;
//    int lowword = uid & 0xFFFF;
//
//    System.out.println("gfp hash: " + ByteUtil.toInt(gfpHash) + " " + ByteUtil.toHexString(gfpHash));
//    //System.out.println("highword: " + highword + " lowword: " + lowword);
//
//    int hash = highword ^ lowword;
//    hash = hash & 0xFFFF;
//    hash = hash & 0x1f7f;
//    hash = hash >> 3;
//
//    hash = hash & 0x1f7f;
//    hash = hash | 0x0300;
//
//    System.out.println("S hash: " + hash + " " + ByteUtil.toHexString(ByteUtil.to2ByteArray(hash)));
//
//    hash = highword ^ lowword;
//    hash = hash & 0xFFFF;
//
//    System.out.println("^hash: " + hash + " " + ByteUtil.toHexString(ByteUtil.to2ByteArray(hash)));
//
//    int[] hh = ByteUtil.to2ByteArray(hash);
//    System.out.println("hh: " + hash + " " + ByteUtil.toHexString(hh));
//
//    //int msb = hh[0];
//    //int lsb = hh[1];
//    int msb = hash & 0xff00;
//    int lsb = hash & 0x00ff;
//
//    System.out.println("msb: " + ByteUtil.toHexString(msb) + " lsb: " + ByteUtil.toHexString(lsb));
//
//    msb = msb & 0x1f;
//    msb = msb >> 3;
//    msb = msb | 0x03;  //msb is ok
//
//    lsb = lsb & 0x7f;
//    lsb = lsb >> 1;
//    //lsb = lsb | 0x00;
//    lsb = lsb & 0x7f;
//
//    System.out.println("a msb: " + ByteUtil.toHexString(msb) + " lsb: " + ByteUtil.toHexString(lsb));
//
//    hash = hash & 0x1f7f;
//    hash = hash >> 3;
//    hash = hash & 0x1fff;
//    hash = hash | 0x0300; //msb is now ok lsb not
//
//    //int[] hh = ByteUtil.to2ByteArray(hash);
//    //System.out.println("hh: " + hash + " " + ByteUtil.toHexString(hh));
//    hash = hash & 0x1f7f;
//    hash = hash | 0x0300;
//  }
