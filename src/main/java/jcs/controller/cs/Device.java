/*
 * Copyright 2023 fransjacobs.
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
package jcs.controller.cs;

import java.util.List;
import java.util.Objects;
import jcs.controller.cs.can.CanMessage;
import jcs.controller.cs.can.MarklinCan;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class Device {

  private int uid;
  private int deviceId;
  private int version;
  private String serialNumber;
  private String articleNumber;
  private String deviceName;
  private int measureChannels;
  private int configChannels;

  public Device() {

  }

  public Device(CanMessage message) {
    buildFromMessage(message);
  }

  private void buildFromMessage(CanMessage message) {
    if (message.getDlc() == MarklinCan.DLC_8) {
      int[] uida = new int[4];
      int[] vera = new int[2];
      int[] deva = new int[2];
      System.arraycopy(message.getData(), 0, uida, 0, uida.length);
      System.arraycopy(message.getData(), 4, vera, 0, vera.length);
      System.arraycopy(message.getData(), 6, deva, 0, deva.length);
      this.uid = ByteUtil.toInt(uida);
      this.version = ByteUtil.toInt(vera);
      this.deviceId = ByteUtil.toInt(deva);
    }
  }

  public void updateFromMessage(CanMessage message) {
    List<CanMessage> responses = message.getResponses();
    if (!responses.isEmpty()) {
      //The last response has the total response messages
      int packets = 0;
      CanMessage last = responses.get(responses.size() - 1);
      Logger.trace("Last: " + last);
      if (last.getDlc() == CanMessage.DLC_6) {
        packets = last.getDataByte(5);
      } else if (last.getDlc() == CanMessage.DLC_5) {
        //CS-2 lets assume the number packets to be the size
        packets = responses.size() - 1;
      }
      Logger.trace("Last: " + last + " Packets: " + packets);

      for (int i = 0; i < responses.size(); i++) {
        CanMessage rsp = responses.get(i);
        int pkgIdx = rsp.getPackageNumber();
        int[] data = rsp.getData();

        if (pkgIdx == 1) {
          //Serial number and channels
          int[] sn = new int[2];
          System.arraycopy(data, 6, sn, 0, sn.length);
          int serial = ((sn[0] & 0xFF) << 8) | (sn[1] & 0xFF);
          serialNumber = serial + "";
          measureChannels = data[0];
          configChannels = data[1];
        } else if (pkgIdx == 2) {
          //Article
          articleNumber = ByteUtil.bytesToString(data);
          articleNumber = articleNumber.trim();
        } else if (pkgIdx > 2 && rsp.getDlc() == MarklinCan.DLC_8) {
          // Device name
          if (deviceName == null) {
            deviceName = "";
          }
          deviceName = deviceName + ByteUtil.bytesToString(data);
        } else {
          //should be the last
          deviceName = deviceName.trim();

          if (responses.size() - 1 != packets) {
            Logger.warn("Config Data Invalid");
          }

        }
      }
    }
//      CanMessage r0 = responses.get(0);
//      int[] data1 = r0.getData();
//      int[] sn = new int[2];
//      System.arraycopy(data1, 6, sn, 0, sn.length);
//      int serial = ((sn[0] & 0xFF) << 8) | (sn[1] & 0xFF);
//      if (this.serialNumber == null) {
//        this.serialNumber = serial + "";
//      }
//      measureChannels = data1[0];
//      configChannels = data1[1];
//
//      if (responses.size() > 1) {
//        byte[] data2 = responses.get(1).getDataBytes();
//        articleNumber = ByteUtil.bytesToString(data2);
//      }
//
//      
//      if (responses.size() > 2) {
//        byte[] data3 = responses.get(2).getDataBytes();
//        deviceName = ByteUtil.bytesToString(data3);
//      }
//
//      if (responses.size() > 3) {
//        byte[] data4 = responses.get(3).getDataBytes();
//        deviceName = deviceName + ByteUtil.bytesToString(data4);
//      }
//
//      if (responses.size() > 3) {
//        byte[] data5 = responses.get(4).getDataBytes();
//        deviceName = deviceName + ByteUtil.bytesToString(data5);
//        deviceName = deviceName.trim();
//      }
//
//      int packetCount = 0;
//      if (responses.size() > 4) {
//        //Fifth is the confimation and channels
//        if (responses.get(5).getDlc() == MarklinCan.DLC_6) {
//          //Have the last packet
//          byte[] data6 = responses.get(5).getDataBytes();
//          int index = data6[MarklinCan.STATUS_CONFIG_INDEX];
//          packetCount = data6[MarklinCan.STATUS_CONFIG_PACKET_COUNT];
//        }
//      }
//
//      if (responses.size() - 1 != packetCount) {
//        Logger.warn("Config Data Invalid");
//      }
//    }
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public int getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }

  public int getVersion() {
    return version;
  }

  public String getArticleNumber() {
    return articleNumber;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public int getMeasureChannels() {
    return measureChannels;
  }

  public int getConfigChannels() {
    return configChannels;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public boolean isDataComplete() {
    return this.deviceName != null;
  }

  public String getDevice() {
    return switch (this.deviceId) {
      case 0x0000 ->
        "GFP";
      case 0x0010 ->
        "Gleisbox 60112 und 60113";
      case 0x0020 ->
        "Connect 6021 Art-Nr.60128";
      case 0x0030 ->
        "MS 2 60653, Txxxxx";
      case 0xffe0 ->
        "Wireless Devices";
      case 0xffff ->
        "CS2-GUI (Master)";
      default ->
        "Unknown " + this.deviceName;
    };
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + this.uid;
    hash = 97 * hash + this.deviceId;
    hash = 97 * hash + this.version;
    hash = 97 * hash + Objects.hashCode(this.serialNumber);
    hash = 97 * hash + Objects.hashCode(this.articleNumber);
    hash = 97 * hash + Objects.hashCode(this.deviceName);
    hash = 97 * hash + this.measureChannels;
    hash = 97 * hash + this.configChannels;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Device other = (Device) obj;
    if (this.uid != other.uid) {
      return false;
    }
    if (this.deviceId != other.deviceId) {
      return false;
    }
    if (this.version != other.version) {
      return false;
    }
    if (this.measureChannels != other.measureChannels) {
      return false;
    }
    if (this.configChannels != other.configChannels) {
      return false;
    }
    if (!Objects.equals(this.serialNumber, other.serialNumber)) {
      return false;
    }
    if (!Objects.equals(this.articleNumber, other.articleNumber)) {
      return false;
    }
    return Objects.equals(this.deviceName, other.deviceName);
  }

  @Override
  public String toString() {
    return "Device{" + "uid=" + uid + ", deviceId=" + deviceId + ", version=" + version + ", serialNumber=" + serialNumber + ", articleNumber=" + articleNumber + ", deviceName=" + deviceName + ", measureChannels=" + measureChannels + ", configChannels=" + configChannels + "}";
  }

}
