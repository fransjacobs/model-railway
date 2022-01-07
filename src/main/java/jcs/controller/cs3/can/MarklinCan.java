/*
 * Copyright (C) 2020 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.controller.cs3.can;

/**
 * Constants for CS 2 CAN communication
 *
 * @see http://medienpdb.maerklin.de/digital22008/files/cs2CAN-Protokoll-2_0.pdf
 *
 */
public interface MarklinCan {

    public final static int JCS_UID = 0x1800;
    public final static int PRIO_1 = 0x00;  // Priority 1: Stop / go / short message
    public final static int PRIO_2 = 0x01;  // Priority 2: feedback
    public final static int PRIO_3 = 0x02;  // Priority 3: Engine Stop
    public final static int PRIO_4 = 0x03;  // Priority 4: Engine/acessory command

    public final static int SYSTEM_COMMAND = 0x00;
    public final static int SYSTEM_COMMAND_RESPONSE = 0x01;
    public static final int STOP_AND_GO_QUERY_DLC = 0x04;
    public static final int STOP_AND_GO_DLC = 0x05;

    public static final int STOP_SUBCMD = 0x00;
    public static final int GO_SUBCMD = 0x01;
    public static final int SUBCMD_IDX = 4;

    public final static int LOC_DISCOVERY_COMMAND = 0x01;

    public final static int MFX_BIND_COMMAND = 0x04;

    public final static int MFX_VERIFY_COMMAND = 0x06;

    public final static int LOC_SPEED = 0x08;
    public final static int LOC_SPEED_QUERY_DLC = 0x04;
    public final static int LOC_SPEED_SET_DLC = 0x06;

    public final static int LOC_DIRECTION = 0x0a;
    public final static int LOC_DIRECTION_RESPONSE = 0x0b;

    public static final int LOC_DIRECTION_QUERY_DLC = 0x04;
    public static final int LOC_DIRECTION_SET_DLC = 0x05;
    public final static int LOC_DIRECTION_VALUE_IDX = 4;

    public final static int LOC_FUNCTION = 0x0c;
    public static final int LOC_FUNCTION_QUERY_DLC = 0x05;
    public static final int LOC_FUNCTION_SET_DLC = 0x06;
    public static final int FUNCTION_OFF = 0x00;
    public static final int FUNCTION_ON = 0x01;

    public final static int READ_CONFIG = 0x0e;

    public final static int WRITE_CONFIG = 0x10;

    public final static int ACCESSORY_SWITCHING = 0x16;
    public final static int ACCESSORY_SWITCHING_DLC = 0x06;
    public final static int ACCESSORY_CAN_ADDRESS = 0x30;
    public final static int ACCESSORY_CAN_ADDRESS_IDX = 2;
    public final static int ACCESSORY_ADDRESS_IDX = 3;
    public final static int ACCESSORY_VALUE_IDX = 4;
    public final static int ACCESSORY_ACTIVE_IDX = 5;

    public final static int ACCESSORY_CONFIG = 0x18;

    //public final static int S88_POLLING = 0x20;

    public final static int S88_EVENT = 0x22;
    public final static int S88_EVENT_QUERY_DLC = 0x04;
    public final static int S88_EVENT_QUERY_PIN_DLC = 0x05;
    public final static int S88_EVENT_QUERY_BLOCK_DLC = 0x07;
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

    public final static int SX1_EVENT = 0x24;

    public final static int SW_STATUS_DLC = 0x00;
    public final static int SW_STATUS_REQ = 0x30;
    
    // after debugging using the Marklin phone app it appears that the command is 0x31...
    public final static int REQ_PING = 0x31;
    
    public static final int REQ_PING_DLC = 0x08;

    public final static int UPDATE_OFFER = 0x32;

    public final static int READ_CONFIG_DATA = 0x34;

    public final static int BOOTLOADER_CAN_SERVICE = 0x36;

    public final static int BOOTLOADER_TRACK_SERVICE = 0x38;

    public final static int STATUS_CONFIG = 0x3a;
    public final static int STATUS_CONFIG_DLC = 0x05;

    public final static int REQUEST_CONFIG_DATA = 0x40;
    public static final int REQUEST_CONFIG_DATA_DLC = 0x08;

    public final static int CONFIG_DATA_STREAM = 0x42;

    public final static int CON_60128_DATA_STREAM = 0x44;

    //System Sub commands
    //public static final int SYSTEM_SUB_STOP = 0x00;
    //public static final int SYSTEM_SUB_GO = 0x01;
    public static final int SYSTEM_SUB_HALT = 0x02;

    public static final int SYSTEM_SUB_LOC_EMERGENCY_STOP = 0x03;

    public static final int SYSTEM_SUB_LOC_CYCLE_STOP = 0x04;

    public static final int SYSTEM_SUB_LOC_PROTOCOL = 0x05;

    public static final int SYSTEM_SUB_SWITCH_TIME_ACCESSORY = 0x06;

    public static final int SYSTEM_SUB_MFX_FAST_READ = 0x07;

    public static final int SYSTEM_SUB_UNLOCK_TRACK_PROTOCOL = 0x08;

    public static final int SYSTEM_SUB_MFX_REG_CNT = 0x09;

    public static final int SYSTEM_SUB_SYS_OVERLOAD = 0x0a;

    public static final int SYSTEM_SUB_SYSTEM_ID = 0x0c;

    public static final int SYSTEM_SUB_MFX_SEEK = 0x30;

    public static final int SYSTEM_SUB_SYSTEM_RESET = (byte) 0x80;

    public static final int SYSTEM_STOP_AND_GO_DLC = 0x05;

    public static final int SYSTEM_RAIL_UNLOCK_DLC = 0x06;

    //The UID of the phone app for disovery
    public static final int[] MOBILE_APP_UID = new int[]{0x4f, 0x59, 0x10, 0xdf};
    //Wireless device
    public static final int[] WIRELESS_DEVICE_ID = new int[]{0xee, 0xee};
    //Mobile app version 1.4.2
    public static final int[] APP_VERSION = new int[]{0x01, 0x04};
    //HASH send by mobile APP
    public static final int[] MOBILE_APP_HASH = new int[]{0x47, 0x11};

//  public static final int DIRECTION_SAME = 0x00;
//  public static final int DIRECTION_FORWARDS = 0x01;
//  public static final int DIRECTION_BACKWARDS = 0x02;
//  public static final int DIRECTION_SWITCH = 0x03;
}
