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
package lan.wervel.jcs.controller.cs2.can;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CS 2 CAN message.
 */
public class CanMessage implements Serializable {

    public static final int MESSAGE_SIZE = 13;

    public static final int HASH_SIZE = 2;
    public static final int DATA_SIZE = 8;

    private static final int PRIO_IDX = 0;
    private static final int CMD_IDX = 1;
    private static final int SUB_CMD_IDX = 10;
    private static final int HASH_IDX = 2;
    private static final int DLC_IDX = 4;
    private static final int DATA_IDX = 5;

    private final int deviceUidNumber;

    private final int[] message;

    private final List<CanMessage> responses;

    public CanMessage() {
        this.message = getEmptyMessage();
        this.responses = new ArrayList<>();
        deviceUidNumber = -1;
    }

    public CanMessage(byte[] message) {
        this.message = getEmptyMessage();
        this.responses = new ArrayList<>();
        deviceUidNumber = -1;
        for (int i = 0; i < this.message.length; i++) {
            this.message[i] = message[i] & 0xFF;
        }
    }

    public CanMessage(int[] message) {
        this.message = getEmptyMessage();
        this.responses = new ArrayList<>();
        deviceUidNumber = -1;
        System.arraycopy(message, 0, this.message, 0, message.length);
    }

    public CanMessage(int priority, int command, int dlc, int[] data) {
        this(priority, command, null, dlc, data, 0);
    }

    public CanMessage(int priority, int command, int dlc, int[] data, int deviceUidNumber) {
        this(priority, command, null, dlc, data, deviceUidNumber);
    }

    public CanMessage(int priority, int command, int[] hash, int dlc, int[] data, int deviceUidNumber) {
        this.message = getEmptyMessage();
        this.responses = new ArrayList<>();
        this.deviceUidNumber = deviceUidNumber;
        this.setPriority(priority);
        this.setCommand(command);
        this.setDlc(dlc);
        this.setData(data);

        if (hash == null) {
            this.setHash(this.generateHash());
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
        return getResponse(0);
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
        int otherCmd = other.getCommand();
        int cmd = this.getCommand();
        return (cmd - 1) == otherCmd;
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

    public boolean expectsAcknowledge() {
        int cmd = this.getCommand();

        switch (cmd) {
            case MarklinCan.STATUS_CONFIG:
                return true;
            default:
                return false;
        }
    }

    public boolean isMemberPing() {
        int cmd = this.getCommand();

        switch (cmd) {
            case MarklinCan.SW_STATUS_REQ:
                return true;
            case MarklinCan.REQ_PING:
                return true;
            default:
                return false;
        }
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
        int[] uid = new int[4]; //CS2 UID is 4 bytes long
        System.arraycopy(this.message, DATA_IDX, uid, 0, uid.length);
        return uid;
    }

    public int getDeviceUidNumberFromMessage() {
        return toInt(getDeviceUidFromMessage());
    }

    public int getDeviceUidNumber() {
        return deviceUidNumber;
    }

    public String responseString() {
        return this.responses.get(0).toString();
    }

    public String getMessageName() {
        int cmd = getCommand();
        switch (cmd) {
            case MarklinCan.SYSTEM_COMMAND:
                int subcmd = this.getSubCommand();
                switch (subcmd) {
                    case MarklinCan.STOP_SUBCMD:
                        int dlc = this.getDlc();
                        if (dlc == MarklinCan.STOP_AND_GO_QUERY_DLC) {
                            return "Power Status";
                        } else {
                            return "Stop";
                        }
                    case MarklinCan.GO_SUBCMD:
                        return "Go";
                    case MarklinCan.SYSTEM_SUB_HALT:
                        return "Halt";
                    case MarklinCan.SYSTEM_SUB_LOC_EMERGENCY_STOP:
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
                    case MarklinCan.SYSTEM_SUB_SYS_OVERLOAD:
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
            case MarklinCan.LOC_SPEED:
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
        return toHexString(this.message);
    }

    public static String toHexString(int[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            sb.append("0x");
            sb.append(CanMessage.toHexString(bytes[i]));
            if (i + 1 < bytes.length) {
                sb.append(" ");
            }
        }
        return sb.toString();
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

    public final int generateHashInt() {
        int uid;
        if (this.deviceUidNumber > 0) {
            uid = this.deviceUidNumber;
        } else {
            uid = this.getUidInt();
        }

        int msb = uid >> 16;
        int lsb = uid & 0xffff;
        int hash = msb ^ lsb;
        hash = (((hash << 3) & 0xFF00) | 0x0300) | (hash & 0x7F);
        return hash;
    }

    public final int[] generateHash() {
        int gh = generateHashInt();
        int[] hash = to2ByteArray(gh);
        return hash;
    }

    public final int getHashInt() {
        int[] h = this.getHash();

        return ((h[0] & 0xFF) << 8)
                | ((h[1] & 0xFF));
    }

    public static int[] to2ByteArray(int value) {
        int[] bts = new int[]{
            (value >> 8) & 0xFF,
            value & 0XFF};

        return bts;
    }

    public static int toInt(int[] value) {
        int val;
        switch (value.length) {
            case 2:
                val = ((value[0] & 0xFF) << 8) | (value[1] & 0xFF);
                break;
            case 4:
                val = ((value[0] & 0xFF) << 24)
                        | ((value[1] & 0xFF) << 16)
                        | ((value[2] & 0xFF) << 8)
                        | (value[3] & 0xFF);
                break;
            default:
                val = 0;
                break;
        }
        return val;
    }

    public static int[] to4ByteArray(int value) {
        int[] bts = new int[]{
            (value >> 24) & 0xFF,
            (value >> 16) & 0xFF,
            (value >> 8) & 0xFF,
            value & 0XFF};

        return bts;
    }

    private static String toHexString(int b) {
        String h = Integer.toHexString((b & 0xff));
        if (h.length() == 1) {
            h = "0" + h;
        }
        return h;
    }

    private static int[] getEmptyMessage() {
        int[] msg = new int[MESSAGE_SIZE];
        //Enshure it is filled with 0x00
        for (int i = 0; i < msg.length; i++) {
            msg[i] = 0;
        }
        return msg;
    }

}
