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
package jcs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Table(name = "accessories")
public class AccessoryBean {

  private String id;
  private Integer address;
  private Integer address2;
  private String name;
  private String type;
  private Integer state;
  private Integer toggleState;
  private Integer switchTime;
  private String decType;
  private String decoder;
  private Integer states;
  private String group;
  private String icon;
  private String iconFile;
  private String source;
  private String commandStationId;
  private boolean synchronize;

  private boolean locked;

  public AccessoryBean() {
    this(null, null, null, null, null, null, null, null, null, null);
  }

  public AccessoryBean(String id, Integer address, Integer address2, String name, String type, Integer state, Integer switchTime, String protocol, String decoder, String commandStationId) {
    this(id, address, address2, name, type, state, null, switchTime, protocol, decoder, null, null, null, commandStationId);
  }

  public AccessoryBean(String id, Integer address, Integer address2, String name, String type, Integer state, Integer states, Integer switchTime, String protocol,
          String decoder, String group, String icon, String iconFile, String commandStationId) {
    this.id = id;
    this.address = address;
    this.address2 = address2;
    this.name = name;
    this.type = type;
    this.state = state;
    this.switchTime = switchTime;
    this.decType = protocol;
    this.decoder = decoder;
    this.group = group;
    this.icon = icon;
    this.iconFile = iconFile;
    this.states = states;
    this.commandStationId = commandStationId;

    if (this.state == null) {
      this.state = 0;
    }
  }

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Column(name = "address", nullable = false)
  public Integer getAddress() {
    return address;
  }

  public void setAddress(Integer address) {
    this.address = address;
  }

  @Column(name = "address2", nullable = true)
  public Integer getAddress2() {
    return address2;
  }

  public void setAddress2(Integer address) {
    this.address2 = address;
  }

  @Column(name = "name", length = 255, nullable = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Column(name = "type", length = 255, nullable = false)
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Column(name = "state")
  public Integer getState() {
    return state;
  }

  public void setState(Integer state) {
    this.state = state;
  }

  @Column(name = "states")
  public Integer getStates() {
    return states;
  }

  public void setStates(Integer states) {
    this.states = states;
  }

  @Transient
  public boolean isBiAddress() {
    return this.states > 2;
  }

  @Transient
  public Integer getToggleState() {
    return toggleState;
  }

  @Transient
  public void toggle() {
    // based on number of states
    if (states == null) {
      states = 2;
    }
    if (state == null) {
      this.state = 0;
    }

    if (states == 3 && isTurnout()) {
      if (toggleState == null) {
        toggleState = state;
      }

      toggleState++;
      switch (toggleState) {
        case 0 ->
          state = 0;
        case 1 ->
          state = 1;
        case 2 ->
          state = 2;
        case 3 ->
          state = 1;
        default -> {
          toggleState = 0;
          state = 0;
        }
      }
    } else {
      int s = states;
      if (s == 0) {
        s = 2;
      }
      s = s - 1;
      state = state + 1;
      if (state > s) {
        state = 0;
      }
    }
  }

  @Transient
  public AccessoryValue getAccessoryValue() {
    if (state != null) {
      return AccessoryValue.get(state);
    } else {
      return AccessoryValue.OFF;
    }
  }

  public void setAccessoryValue(AccessoryValue accessoryValue) {
    setState(accessoryValue.getState());
  }

  @Transient
  public SignalValue getSignalValue() {
    if (state != null) {
      return SignalValue.csGet(state);
    } else {
      return SignalValue.OFF;
    }
  }

  public void setSignalValue(SignalValue signalValue) {
    this.state = signalValue.getCSValue();
  }

  public void setAccessoryValue(SignalValue signalValue) {
    setState(signalValue.getCSValue());
  }

  @Column(name = "switch_time")
  public Integer getSwitchTime() {
    return switchTime;
  }

  public void setSwitchTime(Integer switchTime) {
    this.switchTime = switchTime;
  }

  @Column(name = "protocol", length = 255)
  public String getDecType() {
    return decType;
  }

  public void setDecType(String decType) {
    this.decType = decType;
  }

  @Transient
  public Protocol getProtocol() {
    return Protocol.get(decType);
  }

  public void setProtocol(Protocol protocol) {
    this.decType = protocol.getValue();
  }

  @Column(name = "decoder", length = 255)
  public String getDecoder() {
    return decoder;
  }

  public void setDecoder(String decoder) {
    this.decoder = decoder;
  }

  @Column(name = "accessory_group", length = 255)
  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  @Column(name = "icon", length = 255)
  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  @Column(name = "icon_file", length = 255)
  public String getIconFile() {
    return iconFile;
  }

  public void setIconFile(String iconFile) {
    this.iconFile = iconFile;
  }

  @Column(name = "imported", length = 255)
  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  @Column(name = "command_station_id", length = 255, nullable = false)
  public String getCommandStationId() {
    return commandStationId;
  }

  public void setCommandStationId(String commandStationId) {
    this.commandStationId = commandStationId;
  }

  @Column(name = "synchronize", nullable = false, columnDefinition = "synchronize bool default '0'")
  public boolean isSynchronize() {
    return synchronize;
  }

  public void setSynchronize(boolean synchronize) {
    this.synchronize = synchronize;
  }

  public void copyInto(AccessoryBean other) {
    this.id = other.id;
    this.address = other.address;
    this.address2 = other.address2;
    this.name = other.name;
    this.type = other.type;
    this.state = other.state;
    this.switchTime = other.switchTime;
    this.decType = other.decType;
    this.decoder = other.decoder;
    this.states = other.states;
    this.group = other.group;
    this.icon = other.icon;
    this.iconFile = other.iconFile;
    this.source = other.source;
    this.commandStationId = other.commandStationId;
    this.synchronize = other.synchronize;
    this.locked = other.locked;
  }

  @Transient
  public boolean isSignal() {
    //if (group != null) {
    //  return "lichtsignale".equals(this.group);
    //} else {
    return type != null && type.contains("lichtsignal");
    //}
  }

  @Transient
  public boolean isTurnout() {
    //if (group != null) {
    //  return "weichen".equals(group);
    //} else {
    return type != null && type.contains("weiche");
    //}
  }

  @Transient
  public boolean isOther() {
    return !isTurnout() && !isSignal();
  }

  @Override
  public String toString() {
    return name;
  }

  public String toLogString() {
    StringBuilder sb = new StringBuilder();
    sb.append("AccessoryBean{");
    sb.append("id=").append(id);
    sb.append(", address=").append(address);
    sb.append(", address2=").append(address2);
    sb.append(", name=").append(name);
    sb.append(", type=").append(type);
    sb.append(", position=").append(state);
    sb.append(", switchTime=").append(switchTime);
    sb.append(", protocol=").append(decType);
    sb.append(", decoder=").append(decoder);
    sb.append(", states=").append(states);
    sb.append(", group=").append(group);
    sb.append(", icon=").append(icon);
    sb.append(", iconFile=").append(iconFile);
    sb.append(", source=").append(source);
    sb.append(", commandStationId=").append(commandStationId);
    sb.append(", synchronize=").append(synchronize);
    sb.append("}");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + Objects.hashCode(this.id);
    hash = 67 * hash + Objects.hashCode(this.address);
    hash = 67 * hash + Objects.hashCode(this.address2);
    hash = 67 * hash + Objects.hashCode(this.name);
    hash = 67 * hash + Objects.hashCode(this.type);
    hash = 67 * hash + Objects.hashCode(this.state);
    hash = 67 * hash + Objects.hashCode(this.switchTime);
    hash = 67 * hash + Objects.hashCode(this.decType);
    hash = 67 * hash + Objects.hashCode(this.decoder);
    hash = 67 * hash + Objects.hashCode(this.states);
    hash = 67 * hash + Objects.hashCode(this.group);
    hash = 67 * hash + Objects.hashCode(this.icon);
    hash = 67 * hash + Objects.hashCode(this.iconFile);
    hash = 67 * hash + Objects.hashCode(this.source);
    hash = 67 * hash + Objects.hashCode(this.commandStationId);
    hash = 67 * hash + (this.synchronize ? 1 : 0);
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
    final AccessoryBean other = (AccessoryBean) obj;
    if (this.synchronize != other.synchronize) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (!Objects.equals(this.decType, other.decType)) {
      return false;
    }
    if (!Objects.equals(this.decoder, other.decoder)) {
      return false;
    }
    if (!Objects.equals(this.group, other.group)) {
      return false;
    }
    if (!Objects.equals(this.icon, other.icon)) {
      return false;
    }
    if (!Objects.equals(this.iconFile, other.iconFile)) {
      return false;
    }
    if (!Objects.equals(this.source, other.source)) {
      return false;
    }
    if (!Objects.equals(this.commandStationId, other.commandStationId)) {
      return false;
    }
    if (!Objects.equals(this.address, other.address)) {
      return false;
    }
    if (!Objects.equals(this.address2, other.address2)) {
      return false;
    }
    if (!Objects.equals(this.state, other.state)) {
      return false;
    }
    if (!Objects.equals(this.switchTime, other.switchTime)) {
      return false;
    }
    return Objects.equals(this.states, other.states);
  }

  public enum AccessoryValue {
    RED("Red"), RED2("Red2"), GREEN("Green"), WHITE("White"), YELLOW("Yellow"), OFF("Off");

    private final String value;
    private static final Map<String, AccessoryValue> ENUM_MAP;

    AccessoryValue(String value) {
      this.value = value;
    }

    public String getValue() {
      return this.value;
    }

    public String getDBValue() {
      return translate2DBValue(value);
    }

    static {
      Map<String, AccessoryValue> map = new ConcurrentHashMap<>();
      for (AccessoryValue instance : AccessoryValue.values()) {
        map.put(instance.getValue(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static AccessoryValue get(String value) {
      if (value == null) {
        return null;
      }
      return ENUM_MAP.get(value);
    }

    public static AccessoryValue dbGet(String dbValue) {
      if (dbValue == null) {
        return null;
      }
      return ENUM_MAP.get(translateDBValue(dbValue));
    }

    private static String translateDBValue(String dbValue) {
      if (dbValue == null) {
        return null;
      }
      return switch (dbValue) {
        case "R" ->
          "Red";
        case "R2" ->
          "Red2";
        case "G" ->
          "Green";
        default ->
          "Off";
      };
    }

    private static String translate2DBValue(String value) {
      return switch (value) {
        case "Red" ->
          "R";
        case "Red2" ->
          "R2";
        case "Green" ->
          "G";
        default ->
          "O";
      };
    }

    public Integer getState() {
      AccessoryValue av = get(value);
      return switch (av) {
        case GREEN ->
          1;
        case RED ->
          0;
        case RED2 ->
          2;
        default ->
          1;
      };
      //return AccessoryValue.GREEN.getValue().equals(value) ? 1 : 0;
    }

    public static AccessoryValue get1(Integer state) {
      return 1 == state ? GREEN : RED;
    }

    public static AccessoryValue get(Integer state) {
      if (state == null) {
        return GREEN;
      } else {
        return switch (state) {
          case 0 ->
            RED;
          case 2 ->
            RED2;
          default ->
            GREEN;
        };
        //return 1 == state ? GREEN : RED;
      }
    }
  }

  public enum SignalValue {
    Hp0("Hp0"), Hp1("Hp1"), Hp2("Hp2"), Hp0Sh1("Hp0Sh1"), OFF("OFF");

    private final String signalValue;
    private static final Map<String, SignalValue> ENUM_MAP;

    SignalValue(String signalValue) {
      this.signalValue = signalValue;
    }

    public String getSignalValue() {
      return this.signalValue;
    }

    static {
      Map<String, SignalValue> map = new ConcurrentHashMap<>();
      for (SignalValue instance : SignalValue.values()) {
        map.put(instance.getSignalValue(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static SignalValue get(String signalValue) {
      if (signalValue == null) {
        return null;
      }
      return ENUM_MAP.get(signalValue);
    }

    private static int translate2CSValue(String value) {
      return switch (value) {
        case "Hp0" ->
          0;
        case "Hp1" ->
          1;
        case "Hp0Sh1" ->
          2;
        case "Hp2" ->
          3;
        default ->
          -1;
      };
    }

    public int getCSValue() {
      return translate2CSValue(this.signalValue);
    }

    private static String translateCSValue(int value) {
      return switch (value) {
        case 0 ->
          "Hp0";
        case 1 ->
          "Hp1";
        case 2 ->
          "Hp0Sh1";
        case 3 ->
          "Hp2";
        default ->
          "Off";
      };
    }

    public static SignalValue csGet(int csValue) {
      return ENUM_MAP.get(translateCSValue(csValue));
    }
  }

  public enum SignalType {
    HP01("HP01"), HP012SH1("HP012SH1"), HP0SH1("HP0SH1"), HP012("HP012"), NONE("NONE");

    private final String signalType;
    private static final Map<String, SignalType> ENUM_MAP;

    SignalType(String sgnalType) {
      this.signalType = sgnalType;
    }

    static {
      Map<String, SignalType> map = new ConcurrentHashMap<>();
      for (SignalType instance : SignalType.values()) {
        map.put(instance.getSignalType(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public String getSignalType() {
      return this.signalType;
    }

    public static SignalType get(String signalType) {
      if (signalType != null) {
        return ENUM_MAP.get(signalType);
      } else {
        return null;
      }
    }

    //TODO move this to Marklin Command station as it is Marklin specific
    public static SignalType getSignalType(String marklinType) {
      return ENUM_MAP.get(translateSignalString(marklinType));
    }

    private static String translateSignalString(String marklinType) {
      if (marklinType != null) {
        return switch (marklinType) {
          case "lichtsignal_HP01" ->
            "HP01";
          case "lichtsignal_HP02" ->
            "HP012";
          case "lichtsignal_HP012" ->
            "HP012";
          case "lichtsignal_HP012_SH01" ->
            "HP012SH1";
          case "lichtsignal_SH01" ->
            "HP0SH1";
          case "formsignal_HP01" ->
            "HP01";
          case "formsignal_HP02" ->
            "HP012";
          case "formsignal_HP012" ->
            "HP012";
          case "formsignal_HP012_SH01" ->
            "HP012SH1";
          case "formsignal_SH01" ->
            "HP0SH1";
          case "urc_lichtsignal_HP01" ->
            "HP01";
          case "urc_lichtsignal_HP012" ->
            "HP012";
          case "urc_lichtsignal_HP012_SH01" ->
            "HP012SH1";
          case "urc_lichtsignal_SH01" ->
            "HP0SH1";
          default ->
            "NONE";
        };
      } else {
        return "NONE";
      }
    }
  }

  public enum Protocol {
    MM("mm"), DCC("dcc");

    private final String value;
    private static final Map<String, Protocol> ENUM_MAP;

    Protocol(String value) {
      this.value = value;
    }

    public String getValue() {
      return this.value;
    }

    static {
      Map<String, Protocol> map = new ConcurrentHashMap<>();
      for (Protocol instance : Protocol.values()) {
        map.put(instance.getValue(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static Protocol get(String value) {
      if (value == null) {
        return null;
      } else if (value.contains("mm")) {
        return ENUM_MAP.get("mm");
      } else {
        return ENUM_MAP.get(value);
      }
    }

  }
}
