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
import jcs.controller.cs3.can.MarklinCan;
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
            parseMessageIndex(message);
        } catch (Exception e) {
            Logger.warn("Config Data Invalid! " + e.getMessage());
        }
    }

    
    //Value for temp is not correct
    //need to use the scale when -3 = 10 -3 so /1000...
    private void parseMessageIndex(CanMessage statusRequest) {
        List<CanMessage> responses = statusRequest.getResponses();
        if (!responses.isEmpty()) {
            CanMessage r0 = responses.get(0);
            int[] data1 = r0.getData();
            channel.setNumber(data1[0]);
            int p = (byte) data1[1];
            channel.setScale(p);
            channel.setColorMax(data1[2]);
            channel.setColorGreen(data1[3]);
            channel.setColorYellow(data1[4]);
            channel.setColorRed(data1[5]);
            channel.setStartValue(((double) ByteUtil.toInt(new int[]{data1[6], data1[7]})));

            if (responses.size() > 1) {
                byte[] data2 = responses.get(1).getDataBytes();
                channel.setRangeMax(ByteUtil.toInt(new int[]{data2[0], data1[1]}));
                channel.setRangeGreen(ByteUtil.toInt(new int[]{data2[2], data1[3]}));
                channel.setRangeGreen(ByteUtil.toInt(new int[]{data2[4], data1[5]}));
                channel.setRangeYellow(ByteUtil.toInt(new int[]{data2[6], data1[7]}));
            }

            if (responses.size() > 2) {
                byte[] data3 = responses.get(2).getDataBytes();

                byte[] nba = new byte[4];
                System.arraycopy(data3, 0, nba, 0, nba.length);

                String name = ByteUtil.bytesToString(nba);
                channel.setName(name);

                byte[] sta = new byte[4];
                System.arraycopy(data3, 4, sta, 0, sta.length);
                String startVal = ByteUtil.bytesToString(sta);

                Logger.trace("Startval: " + startVal);

                double startValue = Double.parseDouble(startVal);
                channel.setStartValue(startValue);
            }

            if (responses.size() > 3) {
                byte[] data4 = responses.get(3).getDataBytes();

                byte[] mva = new byte[6];
                System.arraycopy(data4, 0, mva, 0, mva.length);
                String humVal = ByteUtil.bytesToString(mva);
                double humanValue = Double.parseDouble(humVal);
                channel.setHumanValue(humanValue);

                byte[] uomb = new byte[]{data4[6], data4[7]};
                String uom = ByteUtil.bytesToString(uomb);
                channel.setUnit(uom);
            }

            if (responses.size() > 4) {
                byte[] data5 = responses.get(4).getDataBytes();
                //axes counter not used
                //but can be the last packet

                if (responses.get(4).getDlc() == MarklinCan.DLC_6) {
                    //Have the last packet
                    index = data5[MarklinCan.STATUS_CONFIG_INDEX];
                    packetCount = data5[MarklinCan.STATUS_CONFIG_PACKET_COUNT];
                }
            }

            if (responses.size() > 5) {
                //Fifth is the confimation and channels
                if (responses.get(5).getDlc() == MarklinCan.DLC_6) {
                    //Have the last packet
                    byte[] data6 = responses.get(5).getDataBytes();
                    index = data6[MarklinCan.STATUS_CONFIG_INDEX];
                    packetCount = data6[MarklinCan.STATUS_CONFIG_PACKET_COUNT];
                }
            }

            if (responses.size() - 1 != packetCount) {
                Logger.warn("Config Data Invalid");
            }
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
