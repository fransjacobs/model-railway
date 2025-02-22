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
package jcs.commandStation.marklin.cs.can;

/**
 * Constants for CS 2/3 CAN communication
 *
 * @see http://medienpdb.maerklin.de/digital22008/files/cs2CAN-Protokoll-2_0.pdf or on the Central Station 2: http://<central station 2>/can/index.html
 *
 */
interface MarklinCan {

  public static final String MARKLIN_COMMANDSTATION_ID = "marklin.cs";

  public static final int MESSAGE_SIZE = 13;

  public static final int HASH_SIZE = 2;
  public static final int DATA_SIZE = 8;

  public static final int SUB_CMD_IDX = 4;
  public static final int HASH_IDX = 2;
  public static final int PACKAGE_IDX = 3;

  public static final int DLC_IDX = 4;
  public static final int DATA_IDX = 5;

  public final static int JCS_UID = 0x1823;
  public final static int JCS_DEVICE_ID = 0x5850;

  public final static int JCS_SERIAL = 0xFE5A;

  public final static int PRIO_1 = 0x00;  // Priority 1: Stop / go / short message
  public final static int PRIO_2 = 0x01;  // Priority 2: feedback
  public final static int PRIO_3 = 0x02;  // Priority 3: Engine Stop
  public final static int PRIO_4 = 0x03;  // Priority 4: Engine/acessory command

  public final static int PRIO_IGNORE = 0x0c;  // Ignore messages which start with 0x0c

  public final static int DLC_0 = 0x00;
  public final static int DLC_4 = 0x04;
  public final static int DLC_5 = 0x05;
  public final static int DLC_6 = 0x06;
  public final static int DLC_7 = 0x07;
  public final static int DLC_8 = 0x08;

  public final static int SYSTEM_COMMAND = 0x00;
  public final static int SYSTEM_COMMAND_RESP = 0x01;

  public static final int SUBCMD_IDX = 4;

  public final static int DISCOVERY_COMMAND = 0x01;

  //Administrative commands
  public final static int DISCOVERY_COMMAND_ID = 0x02;

  public final static int MFX_BIND_COMMAND = 0x04;
  public final static int MFX_BIND_COMMAND_RESP = 0x05;

  public final static int MFX_VERIFY_COMMAND = 0x06;
  public final static int MFX_VERIFY_COMMAND_RESP = 0x07;

  public final static int LOC_VELOCITY = 0x08;
  public final static int LOC_VELOCITY_RESP = 0x09;

  public final static int LOC_DIRECTION = 0x0a;
  public final static int LOC_DIRECTION_RESP = 0x0b;

  public final static int LOC_FUNCTION = 0x0c;
  public final static int LOC_FUNCTION_RESP = 0x0d;

  public final static int LOC_DECODER_READ = 0x0e;
  public final static int LOC_DECODER_READ_RESP = 0x0f;

  public final static int LOC_DECODER_WRITE = 0x10;
  public final static int LOC_DECODER_WRITE_RESP = 0x11;

  public final static int LOC_DIRECTION_VALUE_IDX = 4;

  public static final int FUNCTION_OFF = 0x00;
  public static final int FUNCTION_ON = 0x01;

  //Switching Messages
  public final static int ACCESSORY_SWITCHING = 0x16;
  public final static int ACCESSORY_SWITCHING_RESP = 0x17;

  public final static int ACCESSORY_CAN_ADDRESS = 0x30;
  public final static int ACCESSORY_CAN_ADDRESS_IDX = 2;
  public final static int ACCESSORY_ADDRESS_IDX = 3;
  public final static int ACCESSORY_VALUE_IDX = 4;
  public final static int ACCESSORY_ACTIVE_IDX = 5;
  public final static int ACCESSORY_SWITCH_TIME_IDX = 6;

  public final static int ACCESSORY_CONFIG = 0x18;

  //Sensor messages
  public final static int S88_EVENT = 0x22;
  //public final static int S88_EVENT_RESP = 0x23;

  public final static int SX1_EVENT = 0x24;

  //public final static int S88_EVENT_QUERY_DLC = 0x04;
  public final static int S88_EVENT_RESPONSE = 0x23;

  public final static int PARAM_PIN_RESET = 0x00;
  public final static int PARAM_PIN_READ = 0x01;
  public final static int PARAM_PIN_WATCH_CHANGE = 0x02;
  public final static int PARAM_PIN_WATCH_ACTIVE = 0x03;
  public final static int PARAM_PIN_WATCH_DEACTIVE = 0x04;
  public final static int PARAM_PIN_COUNT_ON = 0x05;
  public final static int PARAM_PIN_TIME = 0x06;
  public final static int PARAM_PIN_COUNT_RESET = 0xFE;
  public final static int PARAM_BROADCAST_ON = 0xFF;

  //Update messages
  public final static int PING_REQ = 0x30;
  public final static int PING_RESP = 0x31;

  public final static int UPDATE_OFFER = 0x32;
  public final static int UPDATE_OFFER_RESP = 0x33;

  public final static int READ_CONFIG_DATA = 0x34;
  public final static int READ_CONFIG_DATA_RESP = 0x35;

  public final static int BOOTLOADER_CAN = 0x36;
  public final static int BOOTLOADER_LOC = 0x38;

  public final static int STATUS_CONFIG = 0x3a;
  public final static int STATUS_CONFIG_RESP = 0x3b;

  public final static int STATUS_CONFIG_INDEX = 4;
  public final static int STATUS_CONFIG_PACKET_COUNT = 5;

  //Gui Information Messages
  public final static int REQUEST_CONFIG_DATA = 0x40;
  public final static int REQUEST_CONFIG_DATA_RESP = 0x41;
  public final static int CONFIG_DATA_STREAM = 0x42;
  public final static int CON_60128_DATA_STREAM = 0x44;

  //System Sub commands
  public static final int STOP_SUB_CMD = 0x00;
  public static final int GO_SUB_CMD = 0x01;
  public static final int HALT_SUB_CMD = 0x02;
  public static final int LOC_STOP_SUB_CMD = 0x03;

  public static final int SYSTEM_SUB_LOC_CYCLE_STOP = 0x04;
  public static final int SYSTEM_SUB_LOC_PROTOCOL = 0x05;
  public static final int SYSTEM_SUB_SWITCH_TIME_ACCESSORY = 0x06;
  public static final int SYSTEM_SUB_MFX_FAST_READ = 0x07;
  public static final int SYSTEM_SUB_UNLOCK_TRACK_PROTOCOL = 0x08;
  public static final int SYSTEM_SUB_MFX_REG_CNT = 0x09;

  public static final int OVERLOAD_SUB_CMD = 0x0a;
  public static final int SYSTEM_SUB_STATUS = 0x0b;
  public static final int SYSTEM_SUB_SYSTEM_ID = 0x0c;
  public static final int SYSTEM_SUB_MFX_SEEK = 0x30;

  public static final int SYSTEM_SUB_SYSTEM_RESET = (byte) 0x80;

  //The UID of the phone app for disovery
  public static final byte[] MOBILE_APP_UID = new byte[]{(byte) 0x4f, (byte) 0x59, (byte) 0x10, (byte) 0xdf};
  //Wireless device
  public static final byte[] WIRELESS_DEVICE_ID = new byte[]{(byte) 0xee, (byte) 0xee};
  //Mobile app version 1.4.2
  public static final byte[] APP_VERSION = new byte[]{0x01, 0x04};
  //HASH send by mobile APP
  public static final byte[] MOBILE_APP_HASH = new byte[]{0x47, 0x11};
  //Magic hash which will result is getting device ids 
  public static final byte[] MAGIC_HASH = new byte[]{0x07, 0x69};

}
