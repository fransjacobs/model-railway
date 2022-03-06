/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.entities;

import java.math.BigDecimal;
import java.util.Objects;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.SignalValue;

public class AccessoryBean implements JCSEntity {

    private BigDecimal id;
    private String name;
    private String type;
    private Integer position;
    private Integer switchTime;
    private String decoderType;
    private String decoder;

    public AccessoryBean() {

    }

    public AccessoryBean(Integer address,String name, AccessoryValue accessoryValue) {
        this(new BigDecimal(address), name, null, accessoryValue.getCS3Value(), null, null, null);
    }

    public AccessoryBean(BigDecimal id, String name, String type, Integer position, Integer switchTime, String decoderType, String decoder) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.position = position;
        this.switchTime = switchTime;
        this.decoderType = decoderType;
        this.decoder = decoder;
    }

    @Override
    public BigDecimal getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        if (id instanceof BigDecimal) {
            this.id = (BigDecimal) id;
        }
    }
    
    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public AccessoryValue getAccessoryValue() {
        return AccessoryValue.cs3Get(this.position);
    }

    public void setAccessoryValue(AccessoryValue accessoryValue) {
        this.setPosition(accessoryValue.getCS3Value());
    }

    public SignalValue getSignalValue() {
        return SignalValue.cs3Get(this.position);
    }

    public void setAccessoryValue(SignalValue signalValue) {
        this.setPosition(signalValue.getCS3Value());
    }

    public Integer getSwitchTime() {
        return switchTime;
    }

    public void setSwitchTime(Integer switchTime) {
        this.switchTime = switchTime;
    }

    public String getDecoderType() {
        return decoderType;
    }

    public void setDecoderType(String decoderType) {
        this.decoderType = decoderType;
    }

    public String getDecoder() {
        return decoder;
    }

    public void setDecoder(String decoder) {
        this.decoder = decoder;
    }

    @Override
    public String toString() {
        return "AccessoryBean{" + "id=" + id + ", name=" + name + ", type=" + type + ", position=" + position + ", switchTime=" + switchTime + ", decoderType=" + decoderType + ", decoder=" + decoder + '}';
    }

    @Override
    public String toLogString() {
        return toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.position);
        hash = 53 * hash + Objects.hashCode(this.switchTime);
        hash = 53 * hash + Objects.hashCode(this.decoderType);
        hash = 53 * hash + Objects.hashCode(this.decoder);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.position, other.position)) {
            return false;
        }
        return Objects.equals(this.switchTime, other.switchTime);
    }
}
