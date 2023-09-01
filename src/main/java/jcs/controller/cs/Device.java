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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jcs.controller.cs.can.CanMessage;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 * A Device is a Component which lives on the CAN Bus. It can be a Central Station or GFP or Link-S88, etc Devices respond to the Ping Request.
 */
public class Device {

  private int uid;
  private int deviceId;
  private String version;
  private String serialNumber;
  private String articleNumber;
  private String deviceName = "";
  private int measureChannels;
  private int configChannels;

  private String name;
  private String typeName;
  private String identifier;
  private Integer queryInterval;
  private boolean present;

  private final Map<String, MeasurementChannel> channels;
  private final Map<Integer, SxxBus> sxxBusses;

  public static final String MAIN = "MAIN";
  public static final String PROG = "PROG";
  public static final String VOLT = "VOLT";
  public static final String TEMP = "TEMP";

  public static final String BUS1 = "Länge Bus 1 (RJ45-1)";
  public static final String BUS2 = "Länge Bus 2 (RJ45-2)";
  public static final String BUS3 = "Länge Bus 3 (6-Polig)";

  public Device() {
    this(null);
  }

  public Device(CanMessage message) {
    channels = new HashMap<>();
    sxxBusses = new HashMap<>();
    if (message != null) {
      buildFromMessage(message);
    }
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
      //TODO: Version is not same displayed in the CS
      this.version = "" + CanMessage.toInt(vera);
      //TODO: in case of a Link S88 it is offset by 1 so device ID + 1
      this.deviceId = CanMessage.toInt(deva);
    }
  }

  public void updateFromMessage(CanMessage message) {
    //Filter the responses
    List<CanMessage> responses = new ArrayList<>(message.getResponses().size());
    for (CanMessage resp : message.getResponses()) {
      if (CanMessage.STATUS_CONFIG_RESP == resp.getCommand()) {
        responses.add(resp);
      }
    }

//    Logger.trace(message);
//    for (CanMessage r : responses) {
//      Logger.trace(r);
//    }

    if (!responses.isEmpty()) {
      //The last response has the total response messages
      CanMessage last = responses.get(responses.size() - 1);
      int packets = 0;

      if (last.getDlc() == CanMessage.DLC_6) {
        packets = last.getDataByte(5);
      } else if (last.getDlc() == CanMessage.DLC_5) {
        //CS-2 lets assume the number packets to be the size
        packets = responses.size() - 1;
      }
      if (responses.size() - 1 != packets) {
        Logger.warn("Config Data might be invalid. Packages expected: " + packets + " received: " + (responses.size() - 1));
        Logger.trace(message);
        for (CanMessage m : responses) {
          Logger.trace(m);
        }
      } else {
        //Reset the device name
        deviceName = "";
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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
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

  public int getMeasureChannels() {
    return measureChannels;
  }

  public int getConfigChannels() {
    return configChannels;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public boolean isDataComplete() {
    return this.deviceName != null && this.articleNumber != null && this.articleNumber.length() > 4;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public Integer getQueryInterval() {
    return queryInterval;
  }

  public void setQueryInterval(Integer queryInterval) {
    this.queryInterval = queryInterval;
  }

  public boolean isPresent() {
    return present;
  }

  public void setPresent(boolean present) {
    this.present = present;
  }

  public boolean isCS3() {
    return !("60213".equals(this.articleNumber) || "60214".equals(this.articleNumber) || "60215".equals(this.articleNumber));
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
        "CS2/3-GUI (Master)";
      default ->
        "Unknown " + this.deviceName;
    };
  }

  public void setChannel(MeasurementChannel channel) {
    if (channel == null) {
      return;
    }
    switch (channel.getName()) {
      case MAIN ->
        this.channels.put(MAIN, channel);
      case PROG ->
        this.channels.put(PROG, channel);
      case VOLT ->
        this.channels.put(VOLT, channel);
      case TEMP ->
        this.channels.put(TEMP, channel);
      default -> {
      }
    }
  }

  public void addSxxBus(SxxBus bus) {
    if (bus == null) {
      return;
    }
    switch (bus.getName()) {
      case BUS1 ->
        this.sxxBusses.put(1, bus);
      case BUS2 ->
        this.sxxBusses.put(2, bus);
      case BUS3 ->
        this.sxxBusses.put(3, bus);
      default -> {
      }
    }
    //do nothing;
  }

  public Map<Integer, SxxBus> getSxxBusses() {
    return this.sxxBusses;
  }

  public int getBusLength(Integer busNr) {
    if (busNr == 0) {
      //the LinkSxx self is 1
      return 1;
    } else if (this.sxxBusses.containsKey(busNr)) {
      SxxBus bus = sxxBusses.get(busNr);
      if (bus.getLength() == null) {
        return 0;
      } else {
        return bus.getLength();
      }
    } else {
      return 0;
    }
  }

  public Integer getContactIdOffset(Integer busNr) {
    if (busNr == 0) {
      //the LinkSxx self is 0
      return 0;
    } else if (this.sxxBusses.containsKey(busNr)) {
      SxxBus bus = sxxBusses.get(busNr);
      if (bus.getLength() == null) {
        return 0;
      } else {
        return bus.getContactIdOffset();
      }
    } else {
      return 0;
    }
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + this.uid;
    hash = 97 * hash + this.deviceId;
    hash = 97 * hash + Objects.hashCode(this.version);
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
    if (!Objects.equals(this.version, other.version)) {
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
    return "Device{articleNumber=" + articleNumber + ", deviceName=" + (!"".equals(deviceName) ? deviceName : getDevice()) + ", uid=" + uid + ", serialNumber=" + serialNumber + ", version=" + version + ", deviceId=" + deviceId + ", measureChannels=" + measureChannels + ", configChannels=" + configChannels + "}";
  }

}
