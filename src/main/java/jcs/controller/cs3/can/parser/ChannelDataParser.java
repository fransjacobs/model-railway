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
package jcs.controller.cs3.can.parser;

import java.io.Serializable;
import java.util.List;
import jcs.controller.cs3.MeasurementChannel;
import jcs.controller.cs3.can.CanMessage;
import static jcs.controller.cs3.can.MarklinCan.STATUS_CONFIG;
import static jcs.controller.cs3.can.MarklinCan.SYSTEM_COMMAND;
import static jcs.controller.cs3.can.MarklinCan.SYSTEM_SUB_STATUS;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class ChannelDataParser implements Serializable {

    private final MeasurementChannel channel;

    public ChannelDataParser(CanMessage message) {
        channel = new MeasurementChannel();
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

    public void parseMessage(CanMessage message) {
        if (message.getCommand() == STATUS_CONFIG) {
            List<CanMessage> responses = message.getResponses();
            int packets = getNumberOfPackets(message);
            Logger.trace("Channel Configuration responses count " + responses.size() + " packet size: " + packets);

            //Create one array with data
            byte[] data = new byte[8 * packets];

            if (packets > 0) {
                for (int i = 0; i < packets; i++) {
                    byte[] d = responses.get(i).getDataBytes();
                    System.arraycopy(d, 0, data, (i * d.length), d.length);
                }
                int number = data[0];
                int scale = (byte) data[1];

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
                String endVal = ByteUtil.bytesToString(strArr);

                double endValue = Double.parseDouble(endVal);
                this.channel.setEndValue(endValue);

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
        } else if (message.getCommand() == SYSTEM_COMMAND && message.getSubCommand() == SYSTEM_SUB_STATUS) {
            CanMessage response = message.getResponse();
            byte[] data = response.getDataBytes();

            int number = Byte.toUnsignedInt(data[5]);

            int value = ByteUtil.toInt(new byte[]{data[6], data[7]});
            Logger.trace("Channel Measurement response for channel " + number + " raw value: " + value);

            if (this.channel.getNumber() == number) {
                this.channel.setValue(value);
            } else {
                Logger.warn("Can't set value for " + channel.getNumber() + " as the response is for channel " + number);
            }
        } else {
            Logger.trace("Command: " + ByteUtil.toHexString(message.getCommand()) + " Sub Command: " + message.getSubCommand());
        }
    }

    public MeasurementChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ChannelDataParser{" + "channel=" + channel + "}";
    }

}
