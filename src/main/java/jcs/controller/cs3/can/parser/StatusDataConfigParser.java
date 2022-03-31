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
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.MarklinCan;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class StatusDataConfigParser implements Serializable {

    private String serialNumber;
    private String articleNumber;
    private String deviceName;

    private int measurementCount;
    private int channelCount;

    private int index;
    private int packetCount;

    public StatusDataConfigParser(CanMessage message) {
        parseMessageIndex(message);
    }

    private void parseMessageIndex(CanMessage statusRequest) {
        List<CanMessage> responses = statusRequest.getResponses();
        if (!responses.isEmpty()) {
            CanMessage r0 = responses.get(0);
            int[] data1 = r0.getData();
            int[] sn = new int[2];
            System.arraycopy(data1, 6, sn, 0, sn.length);
            int serial = ((sn[0] & 0xFF) << 8) | (sn[1] & 0xFF);
            if (this.serialNumber == null) {
                this.serialNumber = serial + "";
            }
            measurementCount = data1[0];
            channelCount = data1[1];

            if (responses.size() > 1) {
                byte[] data2 = responses.get(1).getDataBytes();
                articleNumber = ByteUtil.bytesToString(data2);
            }

            if (responses.size() > 2) {
                byte[] data3 = responses.get(2).getDataBytes();
                deviceName = ByteUtil.bytesToString(data3);
            }

            if (responses.size() > 3) {
                byte[] data4 = responses.get(3).getDataBytes();
                deviceName = deviceName + ByteUtil.bytesToString(data4);
            }

            if (responses.size() > 3) {
                byte[] data5 = responses.get(4).getDataBytes();
                deviceName = deviceName + ByteUtil.bytesToString(data5);
                deviceName = deviceName.trim();
            }

            if (responses.size() > 4) {
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getMeasurementCount() {
        return measurementCount;
    }

    public int getChannelCount() {
        return channelCount;
    }

    @Override
    public String toString() {
        return "StatusDataConfigParser{" + "serialNumber=" + serialNumber + ", articleNumber=" + articleNumber + ", deviceName=" + deviceName + ", measurementCount=" + measurementCount + ", channelCount=" + channelCount + ", index=" + index + ", packetCount=" + packetCount + '}';
    }

}
