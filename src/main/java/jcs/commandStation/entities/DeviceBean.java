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
package jcs.commandStation.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.ChannelBean;
import jcs.util.ByteUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

/**
 * A Device is a Component which "lives" in side a Command Station.<br>
 * It can be a Central Station (Marklin) ECos (ESU) etc.
 */
public class DeviceBean {

  
  
  public static final String MAIN = "MAIN";
  public static final String PROG = "PROG";
  public static final String VOLT = "VOLT";
  public static final String TEMP = "TEMP";

  public static final String BUS0 = "Auswertung 1 - 16";
  public static final String BUS1 = "Bus 1 (RJ45-1)";
  public static final String BUS2 = "Bus 2 (RJ45-2)";
  public static final String BUS3 = "Bus 3 (6-Polig)";

  private String uid;
  private String name;
  private String typeName;
  private String identifier;
  private String type;
  private String articleNumber;
  private String serial;
  private Integer queryInteval;

  private String version;
  private Boolean present;
  private Integer available;
  private String config;
  private Boolean ready;
  private String path;
  private Boolean mounted;

  private final List<ChannelBean> channels;
  private final Map<String, ChannelBean> analogChannels;
  private final Map<Integer, ChannelBean> sensorBuses;

  public DeviceBean() {
    this((String) null);
  }

  public DeviceBean(String json) {
    channels = new LinkedList<>();
    analogChannels = new HashMap<>();
    sensorBuses = new HashMap<>();

    parse(json);
  }

  public DeviceBean(CanMessage message) {
    channels = new LinkedList<>();
    analogChannels = new HashMap<>();
    sensorBuses = new HashMap<>();

    if (message != null) {
      buildFromMessage(message);
    }
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public Integer getUidAsInt() {
    String ui = this.uid.replace("0x", "");
    return Integer.parseUnsignedInt(ui, 16);
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

  @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
  public Integer getIdentifierAsInt() {
    String id = this.identifier.replace("0x", "");
    return Integer.parseInt(id, 16);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getArticleNumber() {
    return articleNumber;
  }

  public void setArticleNumber(String articleNumber) {
    this.articleNumber = articleNumber;
  }

  public String getSerial() {
    return serial;
  }

  public void setSerial(String serial) {
    this.serial = serial;
  }

  public Integer getQueryInteval() {
    return queryInteval;
  }

  public void setQueryInteval(Integer queryInteval) {
    this.queryInteval = queryInteval;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getPresent() {
    return present;
  }

  public void setPresent(Boolean present) {
    this.present = present;
  }

  public Integer getAvailable() {
    return available;
  }

  public void setAvailable(Integer available) {
    this.available = available;
  }

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
  }

  public Boolean getReady() {
    return ready;
  }

  public void setReady(Boolean ready) {
    this.ready = ready;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Boolean getMounted() {
    return mounted;
  }

  public void setMounted(Boolean mounted) {
    this.mounted = mounted;
  }

  private void parse(String json) {
    if (json == null) {
      return;
    }
    JSONObject device = new JSONObject(json);

    this.uid = device.optString("_uid");
    this.name = device.optString("_name");
    this.typeName = device.optString("_typname");
    this.identifier = device.optString("_kennung");
    this.type = device.optString("_typ");
    this.articleNumber = device.optString("_artikelnr");
    this.serial = device.optString("_seriennr");
    this.queryInteval = device.optInt("_queryInterval");

    JSONObject versionObj = device.optJSONObject("_version");
    if (versionObj != null) {
      String major = versionObj.optString("major");
      String minor = versionObj.optString("minor");
      this.version = (major != null ? major : "") + (major != null ? "." : "") + (minor != null ? minor : "");
    }

    this.present = device.optBoolean("isPresent");
    this.available = device.optInt("present");
    this.config = device.optString("config");
    this.ready = device.optBoolean("_ready");
    this.path = device.optString("path");
    this.mounted = device.optBoolean("isMounted");

    JSONArray channelsJA = device.optJSONArray("_kanal");
    if (channelsJA != null) {
      for (int i = 0; i < channelsJA.length(); i++) {
        JSONObject kanal = channelsJA.getJSONObject(i);

        ChannelBean cb = new ChannelBean(kanal.toString());
        this.channels.add(cb);
        String n = cb.getName();
        if (n != null) {
          switch (n) {
            case MAIN ->
              this.analogChannels.put(MAIN, cb);
            case PROG ->
              this.analogChannels.put(PROG, cb);
            case TEMP ->
              this.analogChannels.put(TEMP, cb);
            case VOLT ->
              this.analogChannels.put(VOLT, cb);
          }
          if ((n.contains(BUS0) || n.contains(BUS1) || n.contains(BUS2) || n.contains(BUS3)) && cb.isS88Bus()) {
            Integer busNr = cb.getNumber() - 1;
            this.sensorBuses.put(busNr, cb);
          }

        }
      }
    }
  }

  private void buildFromMessage(CanMessage message) {
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

      int uidAsInt = resp.getDeviceUidNumberFromMessage();

      this.uid = "0x" + Integer.toHexString(uidAsInt);

      //this.uid = resp.getDeviceUidNumberFromMessage();
      //TODO: Version is not same displayed in the CS
      this.version = "" + CanMessage.toInt(vera);
      //TODO: in case of a Link S88 it is offset by 1 so device ID + 1

      int identifierAsInt = CanMessage.toInt(deva);
      this.identifier = "0x" + Integer.toHexString(identifierAsInt);
      //this.identifier = CanMessage.toInt(deva);
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
        name = "";
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
                //TODO!
                int measureChannels = data[0];
                int configChannels = data[1];
                byte[] sn = new byte[2];
                System.arraycopy(data, 6, sn, 0, sn.length);
                int serialnr = ((sn[0]) << 8) | (sn[1]);
                serial = serialnr + "";
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
                name = name + s;
              }

              if (packageNr == packets) {
                name = name.trim();
              }
            }
          }
        }
      }
    }
  }

  public boolean isDataComplete() {
    return name != null && name.length() > 2 && articleNumber != null && articleNumber.length() > 4;
  }

  public String getDevice() {
    return switch (getIdentifierAsInt()) {
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
        "Unknown " + this.name;
    };
  }

  public List<ChannelBean> getChannels() {
    return channels;
  }

  public Map<String, ChannelBean> getAnalogChannels() {
    return analogChannels;
  }

  public void setAnalogChannel(ChannelBean channel) {
    if (channel == null) {
      return;
    }
    switch (channel.getName()) {
      case MAIN ->
        analogChannels.put(MAIN, channel);
      case PROG ->
        analogChannels.put(PROG, channel);
      case VOLT ->
        analogChannels.put(VOLT, channel);
      case TEMP ->
        analogChannels.put(TEMP, channel);
      default -> {
      }
    }
  }

  public Map<Integer, ChannelBean> getSensorBuses() {
    return this.sensorBuses;
  }

  public int getBusLength(Integer busNr) {
    if (this.isFeedbackDevice()) {
      ChannelBean cb = this.sensorBuses.get(busNr);
      if (cb != null) {
        if (busNr == 0) {
          return 1;
        } else {
          return cb.getValue();
        }
      } else {
        return 0;
      }
    } else {
      return -1;
    }
  }

  public Integer getLinkS88ContactIdOffset(int busNr) {
    return (busNr - 1) * 1000;
  }

  public boolean isFeedbackDevice() {
    return "Link S88".equals(typeName);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(this.uid);
    hash = 97 * hash + Objects.hashCode(this.name);
    hash = 97 * hash + Objects.hashCode(this.typeName);
    hash = 97 * hash + Objects.hashCode(this.identifier);
    hash = 97 * hash + Objects.hashCode(this.type);
    hash = 97 * hash + Objects.hashCode(this.articleNumber);
    hash = 97 * hash + Objects.hashCode(this.serial);
    hash = 97 * hash + Objects.hashCode(this.queryInteval);
    hash = 97 * hash + Objects.hashCode(this.version);
    hash = 97 * hash + Objects.hashCode(this.present);
    hash = 97 * hash + Objects.hashCode(this.available);
    hash = 97 * hash + Objects.hashCode(this.config);
    hash = 97 * hash + Objects.hashCode(this.ready);
    hash = 97 * hash + Objects.hashCode(this.path);
    hash = 97 * hash + Objects.hashCode(this.mounted);
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
    final DeviceBean other = (DeviceBean) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.typeName, other.typeName)) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (!Objects.equals(this.articleNumber, other.articleNumber)) {
      return false;
    }
    if (!Objects.equals(this.serial, other.serial)) {
      return false;
    }
    if (!Objects.equals(this.version, other.version)) {
      return false;
    }
    if (!Objects.equals(this.config, other.config)) {
      return false;
    }
    if (!Objects.equals(this.path, other.path)) {
      return false;
    }
    if (!Objects.equals(this.uid, other.uid)) {
      return false;
    }
    if (!Objects.equals(this.identifier, other.identifier)) {
      return false;
    }
    if (!Objects.equals(this.queryInteval, other.queryInteval)) {
      return false;
    }
    if (!Objects.equals(this.present, other.present)) {
      return false;
    }
    if (!Objects.equals(this.available, other.available)) {
      return false;
    }
    if (!Objects.equals(this.ready, other.ready)) {
      return false;
    }
    return Objects.equals(this.mounted, other.mounted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("DeviceBean{");
    sb.append("uid=").append(uid);
    sb.append(", name=").append(name);
    sb.append(", typeName=").append(typeName);
    sb.append(", identifier=").append(identifier);
    sb.append(", type=").append(type);
    sb.append(", articleNumber=").append(articleNumber);
    sb.append(", serial=").append(serial);
    sb.append(", queryInteval=").append(queryInteval);
    sb.append(", version=").append(version);
    sb.append(", present=").append(present);
    sb.append(", available=").append(available);
    sb.append(", config=").append(config);
    sb.append(", ready=").append(ready);
    sb.append(", path=").append(path);
    sb.append(", mounted=").append(mounted);
    sb.append(", channels:").append(channels.size());
    sb.append("}");
    return sb.toString();
  }

}
