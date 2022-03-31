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

import jcs.entities.enums.AccessoryValue;
import jcs.util.ByteUtil;

/**
 * Factory for creating the request CAN Messages
 */
public class CanMessageFactory implements MarklinCan {

    public static final int FUNCTION_0 = 0;
    public static final int FUNCTION_1 = 1;
    public static final int FUNCTION_2 = 2;
    public static final int FUNCTION_3 = 3;
    public static final int FUNCTION_4 = 4;

    private static int[] getEmptyData() {
        int[] data = new int[CanMessage.DATA_SIZE];
        //Enshure it is filled with 0x00
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
        return data;
    }

    /**
     * System Stop
     *
     * Track format processor stops operation on main and programming
     * track.Electrical energy is no longer supplied.All speed levels/function
     * values and settings are retained. As a special form, attention must be
     * paid to a general stop command, which affects all track format
     * processors.
     *
     * @param go true GO, false STOP
     * @param gfpUid the uid of the GFP
     * @return CanMessage: 0x00 0x00 0x47 0x11 0x05 0x00 0x00 0x00 0x00 0x00
     * 0x00 0x00 0x00
     */
    public static CanMessage systemStopGo(boolean go, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
            int[] uid = ByteUtil.to4ByteArray(gfpUid);
            System.arraycopy(uid, 0, data, 0, uid.length);
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }
        if (go) {
            data[SUBCMD_IDX] = GO_SUBCMD;
        } else {
            data[SUBCMD_IDX] = STOP_SUBCMD;
        }

        CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, hash, DLC_5, data);
        return cm;
    }

    /**
     * Query the Power Status of the System.Undocumented feature, by by sending
     * query DLC you get an answer which contains the Power Status of the
     * System, but also the GFP UID which is needed for further processing...
     *
     * @param gfpUid UID of the GFP
     * @return CanMessage: [0x00 0x00 0x07 0x69 0x04 0x00 0x00 0x00 0x00 0x00
     * 0x00 0x00 0x00] in case the GFP UID is 0
     */
    public static CanMessage querySystem(int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
            int[] uid = ByteUtil.to4ByteArray(gfpUid);
            System.arraycopy(uid, 0, data, 0, uid.length);
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }

        data[SUBCMD_IDX] = STOP_SUBCMD;

        CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, hash, DLC_4, data, true);
        return cm;
    }

    /**
     * Each device responds with the appropriate data. In this way, the
     * configuration query of all participants that can be reached on the CAN
     * bus is achieved. DLC = 0: Query of all participants on the bus. DLC = 8:
     * When responding, the UID is replaced by that of the responding device.
     * Thus, the graphical user interface processor can determine which devices
     * are connected. Version number is an identifier of the software version.
     *
     * @return CanMessage: 0x00 0x30 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00
     * 0x00 0x00 0x00
     */
    public static CanMessage getMemberPing() {
        int[] data = getEmptyData();

        CanMessage m = new CanMessage();
        m.setPriority(PRIO_1);
        m.setCommand(SW_STATUS_REQ);
        m.setHash(MAGIC_HASH);
        m.setDlc(SW_STATUS_DLC);
        m.setData(data);

        return m;
    }

    /**
     *
     * @return
     */
    public static CanMessage getMobileAppPingRequest() {
        int[] data = getEmptyData();

        System.arraycopy(MarklinCan.MOBILE_APP_UID, 0, data, 0, MOBILE_APP_UID.length);
        System.arraycopy(APP_VERSION, 0, data, 4, APP_VERSION.length);
        System.arraycopy(WIRELESS_DEVICE_ID, 0, data, 6, APP_VERSION.length);

        CanMessage m = new CanMessage();
        m.setPriority(PRIO_1);
        m.setCommand(REQ_PING);
        m.setHash(MOBILE_APP_HASH);
        m.setDlc(DLC_8);
        m.setData(data);

        return m;
    }

    /**
     *
     * @param channel the number of the channel to get the measurement value for
     * @param gfpUid the GFP UID
     * @return
     */
    public static CanMessage systemStatus(int channel, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
            int[] uid = ByteUtil.to4ByteArray(gfpUid);
            System.arraycopy(uid, 0, data, 0, uid.length);
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }
        data[SUBCMD_IDX] = SYSTEM_SUB_STATUS;

        data[5] = channel & 0xff;

        CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, hash, DLC_6, data);
        return cm;
    }

    public static CanMessage switchAccessory(int address, AccessoryValue value, boolean on, int gfpUid) {
        int[] data = getEmptyData();
        //TODO support for DCC
        //localID = address - 1; // GUI-address is 1-based, protocol-address is 0-based
        //if (protocol == ProtocolDCC) { localID |= 0x3800; } else { localID |= 0x3000;}
        int[] hash;
        if (gfpUid > 0) {
//            int[] uid = ByteUtil.to4ByteArray(gfpUid);
//            System.arraycopy(uid, 0, data, 0, uid.length);
             hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }

        data[ACCESSORY_CAN_ADDRESS_IDX] = ACCESSORY_CAN_ADDRESS;
        data[ACCESSORY_ADDRESS_IDX] = address - 1;
        data[ACCESSORY_VALUE_IDX] = AccessoryValue.GREEN.equals(value) ? 1 : 0;
        data[ACCESSORY_ACTIVE_IDX] = on ? 1 : 0;

        CanMessage cm = new CanMessage(PRIO_1, ACCESSORY_SWITCHING, hash, DLC_6, data);
        return cm;
    }

    /**
     * Querying the description of the measured value data and the configuration
     * data of a device.
     *
     * @param index
     * @param gfpUid
     * @return
     */
    public static CanMessage statusDataConfig(int index, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
            int[] uid = ByteUtil.to4ByteArray(gfpUid);
            System.arraycopy(uid, 0, data, 0, uid.length);
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }

        data[STATUS_CONFIG_INDEX] = index & 0xFF;

        CanMessage cm = new CanMessage(PRIO_1, STATUS_CONFIG, hash, DLC_5, data);
        return cm;
    }

    public static CanMessage queryFunction(int address, int functionNumber, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
//            int[] uid = ByteUtil.to4ByteArray(gfpUid);
//            System.arraycopy(uid, 0, data, 0, uid.length);
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }

        int[] locid = ByteUtil.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);
        data[4] = functionNumber & 0xff;
        CanMessage cm = new CanMessage(PRIO_1, LOC_FUNCTION, hash, LOC_FUNCTION_QUERY_DLC, data);
        return cm;
    }

    public static CanMessage setFunction(int address, int functionNumber, int value, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
//            int[] uid = ByteUtil.to4ByteArray(gfpUid);
//            System.arraycopy(uid, 0, data, 0, uid.length);
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }

        int[] locid = ByteUtil.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);
        data[4] = functionNumber & 0xff;
        data[5] = value & 0xff;
        CanMessage cm = new CanMessage(PRIO_1, LOC_FUNCTION, hash, LOC_FUNCTION_SET_DLC, data);
        return cm;
    }

    public static CanMessage queryDirection(int address, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }

        int[] locid = ByteUtil.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);

        CanMessage cm = new CanMessage(PRIO_1, LOC_DIRECTION, hash, LOC_DIRECTION_QUERY_DLC, data);
        return cm;
    }

    public static CanMessage setDirection(int address, int cs2direction, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }

        int[] locid = ByteUtil.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);
        data[LOC_DIRECTION_VALUE_IDX] = cs2direction & 0xff;

        CanMessage cm = new CanMessage(PRIO_1, LOC_DIRECTION, hash, LOC_DIRECTION_SET_DLC, data);
        return cm;
    }

    public static CanMessage querySpeed(int address, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }
        int[] locid = ByteUtil.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);

        CanMessage cm = new CanMessage(PRIO_1, LOC_SPEED, hash, LOC_SPEED_QUERY_DLC, data);
        return cm;
    }

    public static CanMessage setLocSpeed(int address, int speed, int gfpUid) {
        int[] data = getEmptyData();
        int[] hash;
        if (gfpUid > 0) {
            hash = CanMessage.generateHash(gfpUid);
        } else {
            hash = MAGIC_HASH;
        }
        int[] locid = ByteUtil.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);
        int[] sb = ByteUtil.to2ByteArray(speed);
        System.arraycopy(sb, 0, data, 4, sb.length);

        CanMessage cm = new CanMessage(PRIO_1, LOC_SPEED, hash, LOC_SPEED_SET_DLC, data);
        return cm;
    }

//    public static CanMessage requestConfig(String filename) {
//        int[] data = getEmptyData();
//
//        byte[] fba = filename.getBytes();
//        for (int i = 0; i < fba.length; i++) {
//            data[i] = fba[i] & 0xff;
//        }
//
//        CanMessage cm = new CanMessage(PRIO_1, REQUEST_CONFIG_DATA, REQUEST_CONFIG_DATA_DLC, data, guiUidNumber);
//        return cm;
//    }
//    public static CanMessage feedbackEvent(int contactId) {
//        int[] data = getEmptyData();
//        int[] start = ByteUtil.to2ByteArray(contactId);
//        System.arraycopy(start, 0, data, 2, start.length);
//        CanMessage cm = new CanMessage(PRIO_1, S88_EVENT, S88_EVENT_QUERY_DLC, data);
//        return cm;
//    }
//    public static CanMessage querySensor(int contactId) {
//        int[] data = getEmptyData();
//        int[] start = ByteUtil.to2ByteArray(contactId);
//        System.arraycopy(start, 0, data, 2, start.length);
//        data[4] = PARAM_PIN_WATCH_CHANGE;
//        //parameter
//        CanMessage cm = new CanMessage(PRIO_1, S88_EVENT, S88_EVENT_QUERY_PIN_DLC, data);
//        return cm;
//    }
//    public static CanMessage querySensors(int fromContactId, int toContactId) {
//        int[] data = getEmptyData();
//        int[] from = ByteUtil.to2ByteArray(fromContactId);
//        int[] to = ByteUtil.to2ByteArray(toContactId);
//        System.arraycopy(from, 0, data, 2, from.length);
//        System.arraycopy(to, 0, data, 4, to.length);
//        data[6] = PARAM_BROADCAST_ON;
//        //parameter
//        CanMessage cm = new CanMessage(PRIO_1, S88_EVENT, S88_EVENT_QUERY_BLOCK_DLC, data);
//        return cm;
//    }
    //Mainly for testing....
    public static void main(String[] a) {
        System.out.println("getMemberPing:      " + getMemberPing());
        System.out.println("querySystem:        " + querySystem(1668498828));
        System.out.println("stop:               " + systemStopGo(false, 1668498828));
        System.out.println("go:                 " + systemStopGo(true, 1668498828));
        System.out.println();
        System.out.println("statusDataConfig:   " + statusDataConfig(1, 1668498828));
        System.out.println("systemStatus ch 1:  " + systemStatus(1, 1668498828));
        System.out.println("systemStatus ch 4:  " + systemStatus(4, 1668498828));
        
        System.out.println("switchAccessory 1g: " + switchAccessory(1,AccessoryValue.GREEN,true, 1668498828));
        System.out.println("switchAccessory 1g: " + switchAccessory(1,AccessoryValue.GREEN,false, 1668498828));
        
        System.out.println("switchAccessory 1g: " + switchAccessory(1,AccessoryValue.RED,true, 1668498828));
        System.out.println("switchAccessory 1g: " + switchAccessory(1,AccessoryValue.RED,false, 1668498828));

        //System.out.println("querySystem byte array : " + querySystem(new int[]{0x63, 0x73, 0x45, 0x8D}));
//        System.out.println("ping : " + getMobileAppPingRequest());
//        System.out.println("go   : " + go());
//        System.out.println("statusconfig   : " + statusConfig(null));
//
//        System.out.println("W1 S : " + switchAccessory(1, AccessoryValue.GREEN, true));
//        System.out.println("W1 C : " + switchAccessory(1, AccessoryValue.RED, true));
//
//        System.out.println("toggleFunction A 10 light on : " + setFunction(10, FUNCTION_0, 1));
//
//        System.out.println("speed A:3 S:800: " + setLocSpeed(16389, 800));
//        System.out.println("querySystem: " + querySystem());
//
//        System.out.println("Request feedback event: " + feedbackEvent(1));
//        System.out.println("Query Sensor 1: " + querySensor(1));
//        System.out.println("Query Sensor 1 to 16: " + querySensors(1, 16));
//
//        System.out.println("Request config data 'magstat' " + requestConfig("magstat"));
    }

}
