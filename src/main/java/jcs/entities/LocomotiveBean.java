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

import java.awt.Image;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jcs.entities.enums.Direction;

public class LocomotiveBean implements JCSEntity, Serializable {

    private BigDecimal id;
    private String name;
    private String previousName;
    private Long uid;
    private Long mfxUid;
    private Integer address;
    private String icon;
    private String decoderType;
    private String mfxSid;
    private Integer tachoMax;
    private Integer vMin;
    private Integer accelerationDelay;
    private Integer brakeDelay;
    private Integer volume;
    private String spm;
    private Integer velocity;
    private Integer richtung;
    private String mfxType;
    private String blocks;

    private Image locIcon;

    private final Map<Integer, FunctionBean> functions;

    public LocomotiveBean() {
        functions = new HashMap<>();
    }

    public LocomotiveBean(BigDecimal id, String name, String previousName, Long uid,
            Long mfxUid, Integer address, String icon, String decoderType,
            String mfxSid, Integer tachoMax, Integer vMin, Integer accelerationDelay,
            Integer brakeDelay, Integer volume, String spm, Integer velocity,
            Integer direction, String mfxType, String blocks) {

        this.id = id;
        this.name = name;
        this.previousName = previousName;
        this.uid = uid;
        this.mfxUid = mfxUid;
        this.address = address;
        this.icon = icon;
        this.decoderType = decoderType;
        this.mfxSid = mfxSid;
        this.tachoMax = tachoMax;
        this.vMin = vMin;
        this.accelerationDelay = accelerationDelay;
        this.brakeDelay = brakeDelay;
        this.volume = volume;
        this.spm = spm;
        this.velocity = velocity;
        this.richtung = direction;
        this.mfxType = mfxType;
        this.blocks = blocks;

        functions = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviousName() {
        return previousName;
    }

    public void setPreviousName(String previousName) {
        this.previousName = previousName;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getMfxUid() {
        return mfxUid;
    }

    public void setMfxUid(Long mfxUid) {
        this.mfxUid = mfxUid;
    }

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDecoderType() {
        return decoderType;
    }

    public void setDecoderType(String decoderType) {
        this.decoderType = decoderType;
    }

    public String getMfxSid() {
        return mfxSid;
    }

    public void setMfxSid(String mfxSid) {
        this.mfxSid = mfxSid;
    }

    public Integer getTachoMax() {
        return tachoMax;
    }

    public void setTachoMax(Integer tachoMax) {
        this.tachoMax = tachoMax;
    }

    public Integer getvMin() {
        return vMin;
    }

    public void setvMin(Integer vMin) {
        this.vMin = vMin;
    }

    public Integer getAccelerationDelay() {
        return accelerationDelay;
    }

    public void setAccelerationDelay(Integer accelerationDelay) {
        this.accelerationDelay = accelerationDelay;
    }

    public Integer getBrakeDelay() {
        return brakeDelay;
    }

    public void setBrakeDelay(Integer brakeDelay) {
        this.brakeDelay = brakeDelay;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public String getSpm() {
        return spm;
    }

    public void setSpm(String spm) {
        this.spm = spm;
    }

    public Integer getVelocity() {
        return velocity;
    }

    public void setVelocity(Integer velocity) {
        this.velocity = velocity;
    }

    public Integer getRichtung() {
        return richtung;
    }

    public void setRichtung(Integer richtung) {
        this.richtung = richtung;
    }

    public Direction getDirection() {
        if (this.richtung != null) {
            return Direction.getDirection(this.richtung);
        } else {
            return Direction.FORWARDS;
        }
    }

    public void setDirection(Direction direction) {
        this.richtung = direction.getMarklinValue();
    }

    public String getMfxType() {
        return mfxType;
    }

    public void setMfxType(String mfxType) {
        this.mfxType = mfxType;
    }

    public String getBlocks() {
        return blocks;
    }

    public void setBlocks(String blocks) {
        this.blocks = blocks;
    }

    public Image getLocIcon() {
        return locIcon;
    }

    public void setLocIcon(Image locIcon) {
        this.locIcon = locIcon;
    }

    @Override
    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public Map<Integer, FunctionBean> getFunctions() {
        return functions;
    }

    public void addAllFunctions(List<FunctionBean> functions) {

        for (FunctionBean function : functions) {
            this.functions.put(function.getNumber(), function);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String toLogString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.uid);
        hash = 29 * hash + Objects.hashCode(this.address);
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
        final LocomotiveBean other = (LocomotiveBean) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        return Objects.equals(this.address, other.address);
    }

    //Convenience
    public boolean isFunctionValue(Integer number) {
        if (this.functions.containsKey(number)) {
            FunctionBean f = this.functions.get(number);

            return f.getValue() == 1;
        } else {
            return false;
        }
    }

    public boolean hasFunction(Integer number) {
        return this.functions.containsKey(number);
    }

    public void setFunctionValue(Integer number, boolean value) {
        if (this.functions.containsKey(number)) {
            FunctionBean f = this.functions.get(number);
            f.setValue(value ? 1 : 0);
        }
    }
}


