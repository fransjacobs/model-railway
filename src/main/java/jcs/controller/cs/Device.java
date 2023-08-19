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
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 * A Device is a Component which lives on the CAN Bus It can be a Central Station or GFP or Link-S88, etc
 *
 */
public class Device {

  private int uid;
  private int deviceId;
  private int version;
  private String serialNumber;
  private String articleNumber;
  private String deviceName = "";
  private int measureChannels;
  private int configChannels;

  public Device() {

  }

  public Device(CanMessage message) {
    buildFromMessage(message);
  }

  final void buildFromMessage(CanMessage message) {
    CanMessage resp;
    if (!message.isResponseMessage() && !message.getResponses().isEmpty()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }

    if (CanMessage.PING_RESP == resp.getCommand() && CanMessage.DLC_8 == resp.getDlc()) {
      byte[] data = resp.getData();

      byte[] uida = new byte[4];
      System.arraycopy(data, 0, uida, 0, uida.length);

      byte[] vera = new byte[2];
      System.arraycopy(data, 4, vera, 0, vera.length);

      byte[] deva = new byte[2];
      System.arraycopy(data, 6, deva, 0, deva.length);

      this.uid = resp.getDeviceUidNumberFromMessage();
      this.version = CanMessage.toInt(vera);
      this.deviceId = CanMessage.toInt(deva);
    }
  }

  public void updateFromMessage(CanMessage message) {
    List<CanMessage> responses = message.getResponses();
    if (!responses.isEmpty()) {
      //The last response has the total response messages
      int packets = 0;
      CanMessage last = responses.get(responses.size() - 1);
      if (last.getDlc() == CanMessage.DLC_6) {
        packets = last.getDataByte(5);
      } else if (last.getDlc() == CanMessage.DLC_5) {
        //CS-2 lets assume the number packets to be the size
        packets = responses.size() - 1;
      }
      if (responses.size() - 1 != packets) {
        Logger.warn("Config Data might be invalid. Packges expepected: " + packets + " received: " + (responses.size() - 1));
      }

      for (int i = 0; i < responses.size(); i++) {
        CanMessage msg = responses.get(i);
        byte[] data = msg.getData();
        int packageNr = msg.getPackageNumber();

        switch (i) {
          case 0 -> {
            if (CanMessage.DLC_5 == msg.getDlc()) {
            } else if (CanMessage.DLC_8 == msg.getDlc()) {
              //first packet?
              if (packageNr == 1) {
                measureChannels = data[0];
                configChannels = data[1];
                byte[] sn = new byte[2];
                System.arraycopy(data, 6, sn, 0, sn.length);
                int serial = ((sn[0]) << 8) | (sn[1]);
                serialNumber = serial + "";
              }
            }
          }
          case 1 -> {
            if (CanMessage.DLC_8 == msg.getDlc()) {
              if (packageNr == 2) {
                //Article
                articleNumber = ByteUtil.bytesToString(data);
                articleNumber = articleNumber.trim();
              }
            }
          }
          default -> {
            if (CanMessage.DLC_8 == msg.getDlc()) {
              String s = CanMessage.toString(data);
              if (s != null && s.length() > 0) {
                deviceName = deviceName + s;
              }

              if (packageNr == packets) {
                deviceName = deviceName.trim();
              }
            }
          }
        }
      }
    }
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
    return this.deviceName != null && this.articleNumber != null && this.deviceName.length() > 10 && this.articleNumber.length() > 4;
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
      case 0x0040 ->
        "Link-S88";
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
    return "Device{articleNumber=" + articleNumber + ", deviceName=" + (!"".equals(deviceName)?deviceName:getDevice()) + ", uid=" + uid + ", serialNumber=" + serialNumber + ", version=" + version + ", deviceId=" + deviceId + ", measureChannels=" + measureChannels + ", configChannels=" + configChannels + "}";
  }

}
