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

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.SignalValue;

@Table(name = "accessories")
public class AccessoryBean {

  private Long id;
  private Integer address;
  private String name;
  private String type;
  private Integer position;
  private Integer states;
  private Integer switchTime;
  private String decoderType;
  private String decoder;
  private String group;
  private String icon;
  private String iconFile;

  public AccessoryBean() {
    this(null, null, null, null, null, null, null, null);
  }

  public AccessoryBean(Integer address, String name, String type, Integer position, Integer switchTime, String decoderType, String decoder) {
    this(null, address, name, type, position, switchTime, decoderType, decoder);
  }

  public AccessoryBean(Long id, Integer address, String name, String type, Integer position, Integer switchTime, String decoderType, String decoder) {
    this(id, address, name, type, position, null, switchTime, decoderType, decoder, null, null, null);
  }

  public AccessoryBean(Long id, Integer address, String name, String type, Integer position, Integer states, Integer switchTime, String decoderType, String decoder, String group, String icon, String iconFile) {
    this.id = id;
    this.address = address;
    this.name = name;
    this.type = type;
    this.position = position;
    this.switchTime = switchTime;
    this.decoderType = decoderType;
    this.decoder = decoder;
    this.group = group;
    this.icon = icon;
    this.iconFile = iconFile;
    this.states = states;
  }

  @Id
  @GeneratedValue
  @Column(name = "id")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "address", nullable = false)
  public Integer getAddress() {
    return address;
  }

  public void setAddress(Integer address) {
    this.address = address;
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

  @Column(name = "position")
  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  @Column(name = "states")
  public Integer getStates() {
    return states;
  }

  public void setStates(Integer states) {
    this.states = states;
  }

  @Transient
  public void toggle() {
    //based on number of states
    int s = this.states;
    if (s == 0) {
      s = 2;
    }

    s = s - 1;
    position = position + 1;

    if (position > s) {
      position = 0;
    }
  }

  @Transient
  public AccessoryValue getAccessoryValue() {
    if (this.position != null) {
      return AccessoryValue.cs3Get(this.position);
    } else {
      return AccessoryValue.OFF;
    }
  }

  public void setAccessoryValue(AccessoryValue accessoryValue) {
    this.setPosition(accessoryValue.getCS3Value());
  }

  @Transient
  public SignalValue getSignalValue() {
    if (this.position != null) {
      return SignalValue.csGet(this.position);
    } else {
      return SignalValue.OFF;
    }
  }

  public void setSignalValue(SignalValue signalValue) {
    this.position = signalValue.getCSValue();
  }

  public void setAccessoryValue(SignalValue signalValue) {
    this.setPosition(signalValue.getCSValue());
  }

  @Column(name = "switch_time")
  public Integer getSwitchTime() {
    return switchTime;
  }

  public void setSwitchTime(Integer switchTime) {
    this.switchTime = switchTime;
  }

  @Column(name = "decoder_type", length = 255)
  public String getDecoderType() {
    return decoderType;
  }

  public void setDecoderType(String decoderType) {
    this.decoderType = decoderType;
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

  @Transient
  public boolean isSignal() {
    return "lichtsignale".equals(this.group);
  }

  @Override
  public String toString() {
    return name;
  }

  public String toLogString() {
    return "AccessoryBean{" + "id=" + id + ", address=" + address + ", name=" + name + ", type=" + type + ", position=" + position + ", switchTime=" + switchTime + ", decoderType=" + decoderType + ", decoder=" + decoder + ", group=" + group + ", states=" + states + "}";
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.id);
    hash = 53 * hash + Objects.hashCode(this.address);
    hash = 53 * hash + Objects.hashCode(this.name);
    hash = 53 * hash + Objects.hashCode(this.type);
    hash = 53 * hash + Objects.hashCode(this.position);
    hash = 53 * hash + Objects.hashCode(this.states);
    hash = 53 * hash + Objects.hashCode(this.switchTime);
    hash = 53 * hash + Objects.hashCode(this.decoderType);
    hash = 53 * hash + Objects.hashCode(this.decoder);
    hash = 53 * hash + Objects.hashCode(this.group);
    hash = 53 * hash + Objects.hashCode(this.icon);
    hash = 53 * hash + Objects.hashCode(this.iconFile);
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
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (!Objects.equals(this.decoderType, other.decoderType)) {
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
    if (!Objects.equals(this.address, other.address)) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.position, other.position)) {
      return false;
    }
    if (!Objects.equals(this.states, other.states)) {
      return false;
    }
    return Objects.equals(this.switchTime, other.switchTime);
  }
}
