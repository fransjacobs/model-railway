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

import java.util.ArrayList;
import java.util.List;
import jcs.entities.enums.AccessoryValue;

/**
 * Factory for creating CAN Messages
 */
public class CanMessageFactory implements MarklinCan {

  public static final int FUNCTION_0 = 0;
  public static final int FUNCTION_1 = 1;
  public static final int FUNCTION_2 = 2;
  public static final int FUNCTION_3 = 3;
  public static final int FUNCTION_4 = 4;

  private static List<byte[]> stringToData(String txt) {
    List<byte[]> dataList = new ArrayList<>();
    byte[] buf = txt.getBytes();
    for (int i = 0; i < buf.length; i = i + CanMessage.DATA_SIZE) {
      byte[] b = new byte[CanMessage.DATA_SIZE];
      if (i + b.length < buf.length) {
        System.arraycopy(buf, i, b, 0, b.length);
      } else {
        System.arraycopy(buf, i, b, 0, buf.length - i);
      }
      dataList.add(b);
    }
    return dataList;
  }

  /**
   * System Stop
   *
   * Track format processor stops operation on main and programming track.Electrical energy is no longer supplied.All speed levels/function values and settings are retained. As a special form,
   * attention must be paid to a general stop command, which affects all track format processors.
   *
   * @param go true GO, false STOP
   * @param gfpUid the uid of the GFP
   * @return CanMessage: 0x00 0x00 0x47 0x11 0x05 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00
   */
  public static CanMessage systemStopGo(boolean go, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      byte[] uid = CanMessage.to4Bytes(gfpUid);
      System.arraycopy(uid, 0, data, 0, uid.length);
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = CanMessage.MAGIC_HASH;
    }
    if (go) {
      data[SUBCMD_IDX] = GO_SUB_CMD;
    } else {
      data[SUBCMD_IDX] = STOP_SUB_CMD;
    }

    CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, hash, DLC_5, data);
    return cm;
  }

  /**
   * Query the Power Status of the System.Undocumented feature, by by sending query DLC you get an answer which contains the Power Status of the System, but also the GFP UID which is needed for
   * further processing...
   *
   * @param gfpUid UID of the GFP
   * @return CanMessage: [0x00 0x00 0x07 0x69 0x04 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00] in case the GFP UID is 0
   */
  public static CanMessage querySystem(int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      byte[] uid = CanMessage.to4Bytes(gfpUid);
      System.arraycopy(uid, 0, data, 0, uid.length);
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }

    data[SUBCMD_IDX] = STOP_SUB_CMD;

    CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, hash, DLC_4, data);
    return cm;
  }

  /**
   * Each device responds with the appropriate data. In this way, the configuration query of all participants that can be reached on the CAN bus is achieved. DLC = 0: Query of all participants on the
   * bus. DLC = 8: When responding, the UID is replaced by that of the responding device. Thus, the graphical user interface processor can determine which devices are connected. Version number is an
   * identifier of the software version.
   *
   * @return CanMessage: 0x00 0x30 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00
   */
  public static CanMessage getMembersPing() {
    byte[] data = new byte[CanMessage.DATA_SIZE];

    CanMessage m = new CanMessage(PRIO_1, PING_REQ, MAGIC_HASH, DLC_0, data);
    return m;
  }

  public static CanMessage getMemberPingResponse(int uid, int swVersion, int deviceId) {
    byte[] data = new byte[CanMessage.DATA_SIZE];

    byte[] deviceUID = CanMessage.to4Bytes(JCS_UID);
    System.arraycopy(deviceUID, 0, data, 0, deviceUID.length);
    byte[] swv = CanMessage.to2Bytes(swVersion);
    System.arraycopy(swv, 0, data, 4, swv.length);
    byte[] dev = CanMessage.to2Bytes(JCS_DEVICE_ID);
    System.arraycopy(dev, 0, data, 6, dev.length);

    CanMessage m = new CanMessage(PRIO_1, PING_RESP, CanMessage.generateHash(JCS_UID), DLC_8, data);
    return m;
  }

  public static List<CanMessage> getStatusDataConfigResponse(int serialNumber, int measurementValues, int configChannels, String articleNumber, String deviceName, int uid) {
    List<CanMessage> statusDataResp = new ArrayList<>();
    //First message
    byte[] hash = new byte[]{0x03, 0x01}; //Packet 1

    //First message contains the number of measument values, the number of config chanels and the the serial number
    byte[] data = new byte[CanMessage.DATA_SIZE];
    data[0] = (byte) (measurementValues & 0xff);
    data[1] = (byte) (configChannels & 0xff);

    byte[] ser = CanMessage.to2Bytes(serialNumber);
    System.arraycopy(ser, 0, data, 6, ser.length);

    CanMessage m1 = new CanMessage(PRIO_1, STATUS_CONFIG_RESP, hash, DLC_8, data);
    statusDataResp.add(m1);

    //The next message contain the article number in ASCII max lenght is 8 bytes
    byte[] hash2 = new byte[]{(byte) 0x03, (byte) 0x02}; //Packet 2

    int lenght = articleNumber.length();
    if (lenght > CanMessage.DATA_SIZE) {
      lenght = CanMessage.DATA_SIZE;
    }
    String an = articleNumber.substring(0, lenght);
    byte[] data2 = stringToData(an).get(0);

    CanMessage m2 = new CanMessage(PRIO_1, STATUS_CONFIG_RESP, hash2, DLC_8, data2);
    statusDataResp.add(m2);

    //Next messages contain the device name splitten into message of 8 byte data
    List<byte[]> deviceNameData = stringToData(deviceName);
    for (int i = 0; i < deviceNameData.size(); i++) {
      byte[] hashN = new byte[]{(byte) 0x03, (byte) (3 + i)}; //Packet n

      CanMessage mn = new CanMessage(PRIO_1, STATUS_CONFIG_RESP, hashN, DLC_8, deviceNameData.get(i));
      statusDataResp.add(mn);
    }

    //Last message contains the target UID and the number of packets
    byte[] hashLast = CanMessage.generateHash(JCS_UID);

    byte[] lastData = new byte[CanMessage.DATA_SIZE];
    byte[] uida = CanMessage.to4Bytes(uid);
    System.arraycopy(uida, 0, lastData, 0, uida.length);
    lastData[5] = (byte) statusDataResp.size();

    CanMessage last = new CanMessage(PRIO_1, STATUS_CONFIG_RESP, hashLast, DLC_6, lastData);
    statusDataResp.add(last);

    return statusDataResp;
  }

  public static CanMessage getMobileAppPingRequest() {
    byte[] data = new byte[CanMessage.DATA_SIZE];

    System.arraycopy(MOBILE_APP_UID, 0, data, 0, MOBILE_APP_UID.length);
    System.arraycopy(APP_VERSION, 0, data, 4, APP_VERSION.length);
    System.arraycopy(WIRELESS_DEVICE_ID, 0, data, 6, APP_VERSION.length);

    CanMessage m = new CanMessage(PRIO_1, PING_RESP, MOBILE_APP_HASH, DLC_8, data);
    return m;
  }

  /**
   *
   * @param channel the number of the channel to get the measurement value for
   * @param gfpUid the GFP UID
   * @return
   */
  public static CanMessage systemStatus(int channel, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      byte[] uid = CanMessage.to4Bytes(gfpUid);
      System.arraycopy(uid, 0, data, 0, uid.length);
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }
    data[SUBCMD_IDX] = SYSTEM_SUB_STATUS;

    data[5] = (byte) (channel & 0xff);

    CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, hash, DLC_6, data);
    return cm;
  }

  public static CanMessage switchAccessory(int address, AccessoryValue value, boolean on, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    //TODO support for DCC
    //localID = address - 1; // GUI-address is 1-based, protocol-address is 0-based
    //if (protocol == ProtocolDCC) { localID |= 0x3800; } else { localID |= 0x3000;}
    byte[] hash;
    if (gfpUid > 0) {
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }

    data[ACCESSORY_CAN_ADDRESS_IDX] = ACCESSORY_CAN_ADDRESS;
    data[ACCESSORY_ADDRESS_IDX] = (byte) (address - 1);
    data[ACCESSORY_VALUE_IDX] = (byte) (AccessoryValue.GREEN.equals(value) ? 1 : 0);
    data[ACCESSORY_ACTIVE_IDX] = (byte) (on ? 1 : 0);

    CanMessage cm = new CanMessage(PRIO_1, ACCESSORY_SWITCHING, hash, DLC_6, data);
    return cm;
  }

  /**
   * Request the Configuration Data of a Member.
   *
   * @param memberUid
   * @param channel
   * @return
   */
  public static CanMessage statusDataConfig(int memberUid, int channel) {
    byte[] data = new byte[CanMessage.MESSAGE_SIZE];
    byte[] hash;
    if (memberUid > 0) {
      byte[] uid = CanMessage.to4Bytes(memberUid);
      System.arraycopy(uid, 0, data, 0, uid.length);
      hash = CanMessage.generateHash(memberUid);
    } else {
      hash = MAGIC_HASH;
    }

    data[STATUS_CONFIG_INDEX] = (byte) (channel & 0xFF);
    CanMessage cm = new CanMessage(PRIO_1, STATUS_CONFIG, hash, DLC_5, data);
    return cm;
  }

  public static CanMessage queryFunction(int address, int functionNumber, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }

    byte[] locid = CanMessage.to4Bytes(address);
    System.arraycopy(locid, 0, data, 0, locid.length);
    data[4] = (byte) (functionNumber & 0xff);
    CanMessage cm = new CanMessage(PRIO_1, LOC_FUNCTION, hash, DLC_5, data);
    return cm;
  }

  public static CanMessage setFunction(int address, int functionNumber, int value, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }

    byte[] locid = CanMessage.to4Bytes(address);
    System.arraycopy(locid, 0, data, 0, locid.length);
    data[4] = (byte) (functionNumber & 0xff);
    data[5] = (byte) (value & 0xff);
    CanMessage cm = new CanMessage(PRIO_1, LOC_FUNCTION, hash, DLC_6, data);
    return cm;
  }

  public static CanMessage queryDirection(int address, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }

    byte[] locid = CanMessage.to4Bytes(address);
    System.arraycopy(locid, 0, data, 0, locid.length);

    CanMessage cm = new CanMessage(PRIO_1, LOC_DIRECTION, hash, DLC_4, data);
    return cm;
  }

  public static CanMessage setDirection(int address, int csdirection, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }

    byte[] locid = CanMessage.to4Bytes(address);
    System.arraycopy(locid, 0, data, 0, locid.length);
    data[LOC_DIRECTION_VALUE_IDX] = (byte) (csdirection & 0xff);

    CanMessage cm = new CanMessage(PRIO_1, LOC_DIRECTION, hash, DLC_5, data);
    return cm;
  }

  public static CanMessage querySpeed(int address, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }
    byte[] locid = CanMessage.to4Bytes(address);
    System.arraycopy(locid, 0, data, 0, locid.length);

    CanMessage cm = new CanMessage(PRIO_1, LOC_VELOCITY, hash, DLC_4, data);
    return cm;
  }

  public static CanMessage setLocSpeed(int address, int speed, int gfpUid) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }
    byte[] locid = CanMessage.to4Bytes(address);
    System.arraycopy(locid, 0, data, 0, locid.length);
    byte[] sb = CanMessage.to2Bytes(speed);
    System.arraycopy(sb, 0, data, 4, sb.length);

    CanMessage cm = new CanMessage(PRIO_1, LOC_VELOCITY, hash, DLC_6, data);
    return cm;
  }

  /**
   * Querying the description of the measured value data and the configuration data of a device.
   *
   * @param gfpUid
   * @param dataName
   * @return
   */
  public static CanMessage requestConfigData(int gfpUid, String dataName) {
    byte[] data = new byte[CanMessage.DATA_SIZE];
    byte[] hash;
    if (gfpUid > 0) {
      byte[] uid = CanMessage.to4Bytes(gfpUid);
      System.arraycopy(uid, 0, data, 0, uid.length);
      hash = CanMessage.generateHash(gfpUid);
    } else {
      hash = MAGIC_HASH;
    }

    byte[] type = stringToData(dataName).get(0);
    System.arraycopy(type, 0, data, 0, type.length);

    CanMessage cm = new CanMessage(PRIO_1, REQUEST_CONFIG_DATA, hash, DLC_8, data);
    return cm;
  }

  //Mainly for testing....
  public static void main(String[] a) {
    System.out.println("getMobAppPingReq:   " + getMobileAppPingRequest());
    System.out.println("getMembersPing:     " + getMembersPing());

    System.out.println("querySystem:        " + querySystem(1668498828));
    System.out.println("stop:               " + systemStopGo(false, 1668498828));
    System.out.println("go:                 " + systemStopGo(true, 1668498828));
    System.out.println();
    System.out.println("statusDataConfig:   " + statusDataConfig(1668498828, 0));
    System.out.println("systemStatus ch 1:  " + systemStatus(1, 1668498828));
    System.out.println("systemStatus ch 4:  " + systemStatus(4, 1668498828));

    System.out.println("switchAccessory 1g: " + switchAccessory(1, AccessoryValue.GREEN, true, 1668498828));
    System.out.println("switchAccessory 1g: " + switchAccessory(1, AccessoryValue.GREEN, false, 1668498828));

    System.out.println("switchAccessory 1g: " + switchAccessory(1, AccessoryValue.RED, true, 1668498828));

    System.out.println("requestConfigData:  " + requestConfigData(1668498828, "loks"));

    System.out.println("");
    List<CanMessage> msgs = getStatusDataConfigResponse(2374, 4, 2, "60226", "Central Station 3", 1668498828);
    for (int i = 0; i < msgs.size(); i++) {
      System.out.println((i == 0 ? "statusDataConfigRes: " : i + "                    ") + msgs.get(i));
    }

    System.out.println("");
    List<CanMessage> msgs1 = getStatusDataConfigResponse(JCS_SERIAL, 0, 0, "JCS", "Java Central Station", JCS_UID);
    for (int i = 0; i < msgs1.size(); i++) {
      System.out.println((i == 0 ? "statusDataConfigRes1: " : i + "                    ") + msgs1.get(i));
    }
  }
}
