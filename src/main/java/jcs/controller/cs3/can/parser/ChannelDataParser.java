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
package jcs.controller.cs3.can.parser;

import java.io.Serializable;
import java.util.List;
import jcs.controller.cs3.GFPChannel;
import jcs.controller.cs3.can.CanMessage;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class ChannelDataParser implements Serializable {

    private final GFPChannel channel;

    private int index;
    private int packetCount;

    public ChannelDataParser(CanMessage message) {
        channel = new GFPChannel();
        try {
            parseMessage(message);
        } catch (Exception e) {
            Logger.warn("Config Data Invalid! " + e.getMessage());
        }
    }

    private int getStringLength(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x00) {
                return i;
            }
        }
        return data.length;
    }

    private int getNumberOfPackets(CanMessage message) {
        int packets = -1;
        List<CanMessage> responses = message.getResponses();

        int lastIdx = responses.size();
        if (lastIdx > 0) {
            lastIdx = lastIdx - 1;
        } else {
            return -1;
        }
        CanMessage last = responses.get(lastIdx);

        if (last.getDlc() == CanMessage.DLC_6) {
            packets = last.getData()[5];
        }
        Logger.trace("Responses: " + responses.size() + " lastIdx: " + lastIdx + " packets: " + packets);

        return packets;
    }

    private void parseMessage(CanMessage message) {
        List<CanMessage> responses = message.getResponses();
        int packets = getNumberOfPackets(message);
        Logger.trace("Response message count " + responses.size() + " packet size: " + packets);

        //Create one array with data
        byte[] data = new byte[8 * packets];

        if (packets > 0) {
            for (int i = 0; i < packets; i++) {
                byte[] d = responses.get(i).getDataBytes();
                System.arraycopy(d, 0, data, (i * d.length), d.length);
            }
            int number = data[0];
            int scale = (byte) data[1];
            double multiplier = Math.pow(10, scale);

            int colorMax = Byte.toUnsignedInt(data[2]);
            int colorGreen = Byte.toUnsignedInt(data[3]);
            int colorYellow = Byte.toUnsignedInt(data[4]);
            int colorRed = Byte.toUnsignedInt(data[5]);
            double startValue = ((double) ByteUtil.toInt(new byte[]{data[6], data[7]}));

            this.channel.setNumber(number);
            this.channel.setScale(scale);
            this.channel.setColorMax(colorMax);
            this.channel.setColorGreen(colorGreen);
            this.channel.setColorYellow(colorYellow);
            this.channel.setColorRed(colorRed);
            this.channel.setStartValue(startValue);

            //1
            int rangeMax = ByteUtil.toInt(new byte[]{data[8], data[9]});
            int rangeGreen = ByteUtil.toInt(new byte[]{data[10], data[11]});
            int rangeYellow = ByteUtil.toInt(new byte[]{data[12], data[13]});
            int rangeRed = ByteUtil.toInt(new byte[]{data[14], data[15]});

            this.channel.setRangeMax(rangeMax);
            this.channel.setRangeGreen(rangeGreen);
            this.channel.setRangeYellow(rangeYellow);
            this.channel.setRangeRed(rangeRed);

            //2,3,4
            //parse the strings
            int idx = 16; //we are now @ byte 16; get all remaining bytes from here
            int fullLen = data.length - idx;
            byte[] stringdata = new byte[fullLen];
            System.arraycopy(data, idx, stringdata, 0, stringdata.length);
            //get the lenght util \0
            int len = this.getStringLength(stringdata);
            byte[] strArr = new byte[len];
            System.arraycopy(data, idx, strArr, 0, strArr.length);
            String name = ByteUtil.bytesToString(strArr);
            this.channel.setName(name);

            //next string
            idx = idx + len + 1;
            fullLen = data.length - idx;
            stringdata = new byte[fullLen];
            System.arraycopy(data, idx, stringdata, 0, stringdata.length);
            len = this.getStringLength(stringdata);
            strArr = new byte[len];
            System.arraycopy(data, idx, strArr, 0, strArr.length);
            String startVal = ByteUtil.bytesToString(strArr);
            double startValDouble = Double.parseDouble(startVal);
            this.channel.setStartValue(startValDouble);

            //next string
            idx = idx + len + 1;
            fullLen = data.length - idx;
            stringdata = new byte[fullLen];
            System.arraycopy(data, idx, stringdata, 0, stringdata.length);
            len = this.getStringLength(stringdata);
            strArr = new byte[len];
            System.arraycopy(data, idx, strArr, 0, strArr.length);
            String humVal = ByteUtil.bytesToString(strArr);

            double humanValue = Double.parseDouble(humVal);

            //TODO how does the scaling work?
            //I now see different values as I see in the CS 2/3??
//            if (number == 4) {
//                //The TEMP is alway 80.0 while on the cs display it is 48.
//                //Is there a muliplier used?
//                humanValue = humanValue * 0.6;
//            }
//            humanValue = humanValue * multiplier;
            this.channel.setHumanValue(humanValue);

            //next string
            idx = idx + len + 1;
            fullLen = data.length - idx;
            stringdata = new byte[fullLen];
            System.arraycopy(data, idx, stringdata, 0, stringdata.length);
            len = this.getStringLength(stringdata);
            strArr = new byte[len];
            System.arraycopy(data, idx, strArr, 0, strArr.length);
            String uom = ByteUtil.bytesToString(strArr);
            this.channel.setUnit(uom);

            //last string??
            idx = idx + len + 1;
            fullLen = data.length - idx;
            if (fullLen > 0) {
                stringdata = new byte[fullLen];
                System.arraycopy(data, idx, stringdata, 0, stringdata.length);
                len = this.getStringLength(stringdata);
                strArr = new byte[len];
                System.arraycopy(data, idx, strArr, 0, strArr.length);
                String xxx = ByteUtil.bytesToString(strArr);
                Logger.trace("Found string part: " + xxx);
            }
        } else {
            Logger.warn("Config packet data Invalid");
        }
    }

    public GFPChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ChannelDataParser{" + "channel=" + channel + "}";
    }

}
