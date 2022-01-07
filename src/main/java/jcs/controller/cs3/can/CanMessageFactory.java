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

/**
 * Factory for creating the request CAN Messages
 */
public class CanMessageFactory implements MarklinCan {

    public static final int FUNCTION_0 = 0;
    public static final int FUNCTION_1 = 1;
    public static final int FUNCTION_2 = 2;
    public static final int FUNCTION_3 = 3;
    public static final int FUNCTION_4 = 4;

    private static int[] deviceUid = new int[]{00, 00, 00, 00};
    private static int gfpUidNumber = -1;
    private static int guiUidNumber = -1;

    public static void setGFPUid(int gfpUid) {
        gfpUidNumber = gfpUid;
    }
    
    public static void setGUIUid(int guiUid) {
        guiUidNumber = guiUid;
    }

//    public static void setDeviceUidNumber(int[] deviceUid) {
//        deviceUid = deviceUid;
//    }

    private static int[] getEmptyData() {
        int[] data = new int[CanMessage.DATA_SIZE];
        //Enshure it is filled with 0x00
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
        return data;
    }

    public static CanMessage getMemberPing() {
        int[] data = getEmptyData();

        CanMessage m = new CanMessage();
        m.setPriority(PRIO_1);
        m.setCommand(SW_STATUS_REQ);
        m.setDlc(SW_STATUS_DLC);
        m.setData(data);

        return m;
    }

    public static CanMessage getCanBootloader() {
        int[] data = getEmptyData();

        CanMessage m = new CanMessage();
        m.setPriority(PRIO_1);
        m.setCommand(BOOTLOADER_CAN_SERVICE);
        m.setDlc(SW_STATUS_DLC);
        m.setData(data);

        return m;
    }

    public static CanMessage getMobileAppPingRequest() {
        int[] data = getEmptyData();

        System.arraycopy(MarklinCan.MOBILE_APP_UID, 0, data, 0, MOBILE_APP_UID.length);
        System.arraycopy(APP_VERSION, 0, data, 4, APP_VERSION.length);
        System.arraycopy(WIRELESS_DEVICE_ID, 0, data, 6, APP_VERSION.length);

        CanMessage m = new CanMessage();
        m.setPriority(PRIO_1);
        m.setCommand(REQ_PING);
        m.setHash(MOBILE_APP_HASH);
        m.setDlc(REQ_PING_DLC);
        m.setData(data);

        return m;
    }

    public static CanMessage powerStatus() {
        int[] data = getEmptyData();
        //data[SUBCMD_IDX] = STOP_SUBCMD;
        CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, STOP_AND_GO_QUERY_DLC, data);
        return cm;
    }

    public static CanMessage stop() {
        int[] data = getEmptyData();
        if (gfpUidNumber > 0 && deviceUid != null) {
            System.arraycopy(deviceUid, 0, data, 0, deviceUid.length);
        }
        data[SUBCMD_IDX] = STOP_SUBCMD;

        CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, STOP_AND_GO_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage go() {
        int[] data = getEmptyData();
        if (gfpUidNumber > 0 && deviceUid != null) {
            System.arraycopy(deviceUid, 0, data, 0, deviceUid.length);
        }
        data[SUBCMD_IDX] = GO_SUBCMD;

        CanMessage cm = new CanMessage(PRIO_1, SYSTEM_COMMAND, STOP_AND_GO_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage switchAccessory(int address, AccessoryValue value, boolean on) {
        int[] data = getEmptyData();
        //TODO support for DCC
        //localID = address - 1; // GUI-address is 1-based, protocol-address is 0-based
        //if (protocol == ProtocolDCC) { localID |= 0x3800; } else { localID |= 0x3000;}

        data[ACCESSORY_CAN_ADDRESS_IDX] = ACCESSORY_CAN_ADDRESS;
        data[ACCESSORY_ADDRESS_IDX] = address - 1;
        data[ACCESSORY_VALUE_IDX] = AccessoryValue.GREEN.equals(value) ? 1 : 0;
        data[ACCESSORY_ACTIVE_IDX] = on ? 1 : 0;

        CanMessage cm = new CanMessage(PRIO_1, ACCESSORY_SWITCHING, ACCESSORY_SWITCHING_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage statusConfig(int gpfUid) {
        int[] cs2Uid = CanMessage.to4ByteArray(gpfUid);
        return statusConfig(cs2Uid);
    }

    public static CanMessage statusConfig(int[] cs2Uid) {
        int[] data = getEmptyData();
        if (cs2Uid != null) {
            System.arraycopy(cs2Uid, 0, data, 0, cs2Uid.length);
        }

        CanMessage cm = new CanMessage(PRIO_1, STATUS_CONFIG, STATUS_CONFIG_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage queryFunction(int address, int functionNumber) {
        int[] data = getEmptyData();
        int[] locid = CanMessage.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);
        data[4] = functionNumber & 0xff;
        CanMessage cm = new CanMessage(PRIO_1, LOC_FUNCTION, LOC_FUNCTION_QUERY_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage setFunction(int address, int functionNumber, int value) {
        int[] data = getEmptyData();
        int[] locid = CanMessage.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);
        data[4] = functionNumber & 0xff;
        data[5] = value & 0xff;
        CanMessage cm = new CanMessage(PRIO_1, LOC_FUNCTION, LOC_FUNCTION_SET_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage queryDirection(int address) {
        int[] data = getEmptyData();
        int[] locid = CanMessage.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);

        CanMessage cm = new CanMessage(PRIO_1, LOC_DIRECTION, LOC_DIRECTION_QUERY_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage setDirection(int address, int cs2direction) {
        int[] data = getEmptyData();
        int[] locid = CanMessage.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);
        data[LOC_DIRECTION_VALUE_IDX] = cs2direction & 0xff;

        CanMessage cm = new CanMessage(PRIO_1, LOC_DIRECTION, LOC_DIRECTION_SET_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage querySpeed(int address) {
        int[] data = getEmptyData();
        int[] locid = CanMessage.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);

        CanMessage cm = new CanMessage(PRIO_1, LOC_SPEED, LOC_SPEED_QUERY_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage setLocSpeed(int address, int speed) {
        int[] data = getEmptyData();
        int[] locid = CanMessage.to4ByteArray(address);
        System.arraycopy(locid, 0, data, 0, locid.length);
        int[] sb = CanMessage.to2ByteArray(speed);
        System.arraycopy(sb, 0, data, 4, sb.length);

        CanMessage cm = new CanMessage(PRIO_1, LOC_SPEED, LOC_SPEED_SET_DLC, data, gfpUidNumber);
        return cm;
    }

    public static CanMessage requestConfig(String filename) {
        int[] data = getEmptyData();

        byte[] fba = filename.getBytes();
        for (int i = 0; i < fba.length; i++) {
            data[i] = fba[i] & 0xff;
        }

        CanMessage cm = new CanMessage(PRIO_1, REQUEST_CONFIG_DATA, REQUEST_CONFIG_DATA_DLC, data, guiUidNumber);
        return cm;
    }

    public static CanMessage feedbackEvent(int contactId) {
        int[] data = getEmptyData();
        int[] start = CanMessage.to2ByteArray(contactId);
        System.arraycopy(start, 0, data, 2, start.length);
        CanMessage cm = new CanMessage(PRIO_1, S88_EVENT, S88_EVENT_QUERY_DLC, data);
        return cm;
    }

    public static CanMessage querySensor(int contactId) {
        int[] data = getEmptyData();
        int[] start = CanMessage.to2ByteArray(contactId);
        System.arraycopy(start, 0, data, 2, start.length);
        data[4] = PARAM_PIN_WATCH_CHANGE;
        //parameter
        CanMessage cm = new CanMessage(PRIO_1, S88_EVENT, S88_EVENT_QUERY_PIN_DLC, data);
        return cm;
    }

    public static CanMessage querySensors(int fromContactId, int toContactId) {
        int[] data = getEmptyData();
        int[] from = CanMessage.to2ByteArray(fromContactId);
        int[] to = CanMessage.to2ByteArray(toContactId);
        System.arraycopy(from, 0, data, 2, from.length);
        System.arraycopy(to, 0, data, 4, to.length);
        data[6] = PARAM_BROADCAST_ON;
        //parameter
        CanMessage cm = new CanMessage(PRIO_1, S88_EVENT, S88_EVENT_QUERY_BLOCK_DLC, data);
        return cm;
    }

    //Mainly for testing....
    public static void main(String[] a) {
        setGFPUid(1129552448);
        setGUIUid(1129552449);
        
        System.out.println("ping : " + getMobileAppPingRequest());
        System.out.println("stop : " + stop());
        System.out.println("go   : " + go());
        System.out.println("statusconfig   : " + statusConfig(null));

        System.out.println("W1 S : " + switchAccessory(1, AccessoryValue.GREEN, true));
        System.out.println("W1 C : " + switchAccessory(1, AccessoryValue.RED, true));

        System.out.println("toggleFunction A 10 light on : " + setFunction(10, FUNCTION_0, 1));

        System.out.println("speed A:3 S:800: " + setLocSpeed(16389, 800));
        System.out.println("powerStatus: " + powerStatus());

        System.out.println("Request feedback event: " + feedbackEvent(1));
        System.out.println("Query Sensor 1: " + querySensor(1));
        System.out.println("Query Sensor 1 to 16: " + querySensors(1, 16));

        System.out.println("Request config data 'magstat' " + requestConfig("magstat"));
    }

}
