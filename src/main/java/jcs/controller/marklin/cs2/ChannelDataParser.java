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
package jcs.controller.marklin.cs2;

import java.io.Serializable;
import java.util.List;
import jcs.entities.MeasurementChannel;
import jcs.controller.marklin.cs.can.CanMessage;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class ChannelDataParser implements Serializable {

  public ChannelDataParser() {

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
      packets = last.getDataByte(5);
    } else if (last.getDlc() == CanMessage.DLC_5) {
      //CS-2 lets assume the number packets to be the size
      packets = responses.size() - 1;
    }
    return packets;
  }

  public MeasurementChannel parseConfigMessage(CanMessage message) {
    MeasurementChannel channel = new MeasurementChannel();
    if (message.getCommand() == CanMessage.STATUS_CONFIG) {
      List<CanMessage> responses = message.getResponses();
      int packets = getNumberOfPackets(message);

      //Create one array with data
      byte[] data = new byte[8 * packets];

      if (packets > 0) {
        for (int i = 0; i < packets; i++) {
          byte[] d = responses.get(i).getData();
          System.arraycopy(d, 0, data, (i * d.length), d.length);
        }
        int number = Byte.toUnsignedInt(data[0]);
        int scale = data[1];

        int colorMax = Byte.toUnsignedInt(data[2]);
        int colorGreen = Byte.toUnsignedInt(data[3]);
        int colorYellow = Byte.toUnsignedInt(data[4]);
        int colorRed = Byte.toUnsignedInt(data[5]);
        double startValue = ((double) ByteUtil.toInt(new byte[]{data[6], data[7]}));

        channel.setNumber(number);
        channel.setScale(scale);
        channel.setColorMax(colorMax);
        channel.setColorGreen(colorGreen);
        channel.setColorYellow(colorYellow);
        channel.setColorRed(colorRed);
        channel.setStartValue(startValue);

        //1
        int rangeMax = ByteUtil.toInt(new byte[]{data[8], data[9]});
        int rangeGreen = ByteUtil.toInt(new byte[]{data[10], data[11]});
        int rangeYellow = ByteUtil.toInt(new byte[]{data[12], data[13]});
        int rangeRed = ByteUtil.toInt(new byte[]{data[14], data[15]});

        channel.setRangeMax(rangeMax);
        channel.setRangeGreen(rangeGreen);
        channel.setRangeYellow(rangeYellow);
        channel.setRangeRed(rangeRed);

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
        channel.setName(name);

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
        channel.setStartValue(startValDouble);

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
        channel.setEndValue(endValue);

        //next string
        idx = idx + len + 1;
        fullLen = data.length - idx;
        stringdata = new byte[fullLen];
        System.arraycopy(data, idx, stringdata, 0, stringdata.length);
        len = this.getStringLength(stringdata);
        strArr = new byte[len];
        System.arraycopy(data, idx, strArr, 0, strArr.length);
        String uom = ByteUtil.bytesToString(strArr);
        channel.setUnit(uom);

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
    } else {
      Logger.trace("Command: " + ByteUtil.toHexString(message.getCommand()) + " Sub Command: " + ByteUtil.toHexString(message.getSubCommand()));
    }
    return channel;
  }

  public MeasurementChannel parseUpdateMessage(CanMessage message, MeasurementChannel channel) {
    if (message.getCommand() == CanMessage.SYSTEM_COMMAND && message.getSubCommand() == CanMessage.SYSTEM_SUB_STATUS) {
      CanMessage response = message.getResponse();
      byte[] data = response.getData();
      int number = data[5];
      int value = CanMessage.toInt(new byte[]{data[6], data[7]});

      if (channel.getNumber() == number) {
        channel.setValue(value);
      } else {
        Logger.warn("Can't set value for " + channel.getNumber() + " as the response is for channel " + number);
      }
    } else {
      Logger.trace("Command: " + ByteUtil.toHexString(message.getCommand()) + " Sub Command: " + ByteUtil.toHexString(message.getSubCommand()));
    }
    return channel;
  }

}
