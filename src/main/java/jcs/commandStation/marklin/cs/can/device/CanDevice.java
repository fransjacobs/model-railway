/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.commandStation.marklin.cs.can.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A CanDevice is a Device inside or connected to the Marklin Central Station<br>
 * Example devices are:<br>
 * - Central Station self<br>
 * - GFP (Gleis Format Prozessor)<br>
 * - Link S88<br>
 */
public class CanDevice {

//  public static final String MAIN = "MAIN";
//  public static final String PROG = "PROG";
//  public static final String VOLT = "VOLT";
//  public static final String TEMP = "TEMP";
//  public static final String BUS0 = "Auswertung 1 - 16";
//  public static final String BUS1 = "Bus 1 (RJ45-1)";
//  public static final String BUS2 = "Bus 2 (RJ45-2)";
//  public static final String BUS3 = "Bus 3 (6-Polig)";
//  Absender Geräte UID
//  SW-Versionsnummer
//  Gerätekennung
//  Anzahl der Messwerte im Gerät.
//  Anzahl der Konfigurationskanäle
//  frei.
//  Seriennummer CS2.
//  8 Byte Artikelnummer.
//  Gerätebezeichnung, \0 Terminiert
  private String uid;
  private String guiUid;
  private String version;
  private String hwVersion;
  private String identifier;
  private Integer measureChannelCount;
  private Integer configChannelCount;
  private String serial;
  private String articleNumber;
  private String name;

  private final Map<Integer, MeasuringChannel> measuringChannels;
  private final Map<Integer, ConfigChannel> configChannels;

  public CanDevice() {
    measuringChannels = new HashMap<>();
    configChannels = new HashMap<>();
  }

  public String getUid() {
    return uid;
  }

  public Integer getUidInt() {
    String ui = uid.replace("0x", "");
    return Integer.parseUnsignedInt(ui, 16);
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public void setUid(Integer uid) {
    this.uid = "0x" + uid;
  }

  public String getGuiUid() {
    return guiUid;
  }

  public Integer getGuiUidInt() {
    String ui = guiUid.replace("0x", "");
    return Integer.parseUnsignedInt(ui, 16);
  }

  public void setGuiUid(String guiUid) {
    this.guiUid = guiUid;
  }

  public String getName() {
    if (name == null && this.identifier != null && this.identifier.equals("0xffff")) {
      return getDeviceType();
    }
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public Integer getIdentifierInt() {
    String id = identifier.replace("0x", "");
    return Integer.parseUnsignedInt(id, 16);
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

  public void setSerial(Integer serial) {
    this.serial = serial.toString();
  }

  public Integer getMeasureChannelCount() {
    return measureChannelCount;
  }

  public void setMeasureChannelCount(Integer measureChannelCount) {
    this.measureChannelCount = measureChannelCount;
  }

  public Integer getConfigChannelCount() {
    return configChannelCount;
  }

  public void setConfigChannelCount(Integer configChannelCount) {
    this.configChannelCount = configChannelCount;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getHwVersion() {
    return hwVersion;
  }

  public void setHwVersion(String HwVersion) {
    this.hwVersion = HwVersion;
  }

  public String getDeviceType() {
    return switch (getIdentifierInt()) {
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
      case 0x0050 ->
        "CS2/3-GFP";
      case 0xffe0 ->
        "Wireless Devices";
      case 0xffff ->
        "CS2/3-GUI (Master)";
      default ->
        "Unknown " + name;
    };
  }

  public MeasuringChannel getMeasuringChannel(Integer number) {
    return measuringChannels.get(number);
  }

  public void addMeasuringChannel(MeasuringChannel measuringChannel) {
    measuringChannels.put(measuringChannel.getNumber(), measuringChannel);
  }

  public List<MeasuringChannel> getMeasuringChannels() {
    return new ArrayList<>(measuringChannels.values());
  }

  public void addConfigChannel(ConfigChannel configChannel) {
    configChannels.put(configChannel.getNumber(), configChannel);
  }

  public ConfigChannel getConfigChannel(Integer number) {
    return configChannels.get(number);
  }

  public List<ConfigChannel> getConfigChannels() {
    return new ArrayList<>(configChannels.values());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("CanDevice{");
    if (uid != null) {
      sb.append("uid=").append(uid);
    }
    if (name != null) {
      sb.append(", name=").append(name);
    } else if (getName() != null) {
      sb.append(", derivedname=").append(getName());
    }
    if (identifier != null) {
      sb.append(", identifier=").append(identifier);
    }
    if (articleNumber != null) {
      sb.append(", articleNumber=").append(articleNumber);
    }
    if (serial != null) {
      sb.append(", serial=").append(serial);
    }
    if (measureChannelCount != null) {
      sb.append(", measureChannelCount=").append(measureChannelCount);
    }
    if (configChannelCount != null) {
      sb.append(", configChannelCount=").append(configChannelCount);
    }
    if (version != null) {
      sb.append(", version=").append(version);
    }
    if (hwVersion != null) {
      sb.append(", hwVersion=").append(hwVersion);
    }
    if (!measuringChannels.isEmpty()) {
      sb.append(", measuringChannels=").append(measuringChannels);
    }
    if (!configChannels.isEmpty()) {
      sb.append(", configChannels=").append(configChannels);
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + Objects.hashCode(this.uid);
    hash = 79 * hash + Objects.hashCode(this.version);
    hash = 79 * hash + Objects.hashCode(this.hwVersion);
    hash = 79 * hash + Objects.hashCode(this.identifier);
    hash = 79 * hash + Objects.hashCode(this.measureChannelCount);
    hash = 79 * hash + Objects.hashCode(this.configChannelCount);
    hash = 79 * hash + Objects.hashCode(this.serial);
    hash = 79 * hash + Objects.hashCode(this.articleNumber);
    hash = 79 * hash + Objects.hashCode(this.name);
    hash = 79 * hash + Objects.hashCode(this.measuringChannels);
    hash = 79 * hash + Objects.hashCode(this.configChannels);
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
    final CanDevice other = (CanDevice) obj;
    if (!Objects.equals(this.uid, other.uid)) {
      return false;
    }
    if (!Objects.equals(this.version, other.version)) {
      return false;
    }
    if (!Objects.equals(this.hwVersion, other.hwVersion)) {
      return false;
    }
    if (!Objects.equals(this.identifier, other.identifier)) {
      return false;
    }
    if (!Objects.equals(this.serial, other.serial)) {
      return false;
    }
    if (!Objects.equals(this.articleNumber, other.articleNumber)) {
      return false;
    }
    if (!Objects.equals(this.measureChannelCount, other.measureChannelCount)) {
      return false;
    }
    if (!Objects.equals(this.configChannelCount, other.configChannelCount)) {
      return false;
    }
    if (!Objects.equals(this.measuringChannels, other.measuringChannels)) {
      return false;
    }
    if (!Objects.equals(this.configChannels, other.configChannels)) {
      return false;
    }
    return Objects.equals(this.name, other.name);
  }

}
