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
package jcs.commandStation.marklin.cs.can.parser;

import java.io.Serializable;
import java.util.List;
import jcs.commandStation.marklin.cs.can.CanMessage;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class StatusDataConfig implements Serializable {

  private String serialNumber;
  private String articleNumber;
  private String deviceName;

  private int measurementCount;
  private int channelCount;

  private int index;
  private int packetCount;

  public StatusDataConfig(CanMessage message) {
    parseMessageIndex(message);
  }

  private void parseMessageIndex(CanMessage statusRequest) {
    List<CanMessage> responses = statusRequest.getResponses();
    if (!responses.isEmpty()) {
      CanMessage r0 = responses.get(0);
      byte[] data1 = r0.getData();
      byte[] sn = new byte[2];
      System.arraycopy(data1, 6, sn, 0, sn.length);
      int serial = (sn[0] << 8 | sn[1]);
      if (this.serialNumber == null) {
        this.serialNumber = serial + "";
      }
      measurementCount = data1[0];
      channelCount = data1[1];

      if (responses.size() > 1) {
        byte[] data2 = responses.get(1).getData();
        articleNumber = CanMessage.toString(data2);
      }

      if (responses.size() > 2) {
        byte[] data3 = responses.get(2).getData();
        deviceName = CanMessage.toString(data3);
      }

      if (responses.size() > 3) {
        byte[] data4 = responses.get(3).getData();
        deviceName = deviceName + CanMessage.toString(data4);
      }

      if (responses.size() > 3) {
        byte[] data5 = responses.get(4).getData();
        deviceName = deviceName + CanMessage.toString(data5);
        deviceName = deviceName.trim();
      }

      if (responses.size() > 4) {
        //Fifth is the confimation and channels
        if (responses.get(5).getDlc() == CanMessage.DLC_6) {
          //Have the last packet
          byte[] data6 = responses.get(5).getData();
          index = data6[CanMessage.STATUS_CONFIG_INDEX];
          packetCount = data6[CanMessage.STATUS_CONFIG_PACKET_COUNT];
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
