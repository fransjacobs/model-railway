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

//    protected AccessoryValue value;
//    protected AccessoryType accessoiryType;
//    protected Integer lightImages;
//    protected AccessoryBean(Integer address, String description, String catalogNumber, BigDecimal id, AccessoryType type, AccessoryValue value, BigDecimal soacId, Integer lightImages) {
//        super(address, (type != null ? type.toString() : null), description, catalogNumber, id);
//        this.accessoiryType = type;
//        this.value = value;
//        this.soacId = soacId;
//        this.lightImages = lightImages;
//    }
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        if (this.name == null) {
//            if (null == this.accessoiryType) {
//                sb.append("[G]");
//            } else {
//                switch (this.accessoiryType) {
//                    case SIGNAL:
//                        sb.append("[S]");
//                        break;
//                    case TURNOUT:
//                        sb.append("[T]");
//                        break;
//                    default:
//                        sb.append("[G]");
//                        break;
//                }
//            }
//            sb.append(": ");
//        } else {
//            sb.append(this.name);
//        }
//        if (this.address != null && this.address > 0) {
//            sb.append(" (");
//            sb.append(this.address);
//            sb.append(")");
//        }
//
//        return sb.toString();
//    }
//    @Override
//    public String toLogString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("[");
//        if (this.address != null && this.address > 0) {
//            sb.append(this.address);
//        }
//        sb.append("] ");
//        if (null == this.accessoiryType) {
//            sb.append("G: ");
//        } else {
//            sb.append("");
//            switch (this.accessoiryType) {
//                case SIGNAL:
//                    sb.append("S: ");
//                    break;
//                case TURNOUT:
//                    sb.append("T: ");
//                    break;
//                default:
//                    sb.append("G: ");
//                    break;
//            }
//        }
//        sb.append(this.description);
//        sb.append(" {");
//        sb.append(this.value);
//        sb.append("}");
//        return sb.toString();
//    }
//    public AccessoryType getAccessoiryType() {
//        return accessoiryType;
//    }
//    public Integer getLightImages() {
//        return lightImages;
//    }
//    public void setLightImages(Integer lightImages) {
//        this.lightImages = lightImages;
//    }
//    public void setAccessoiryType(AccessoryType accessoiryType) {
//        this.accessoiryType = accessoiryType;
//    }
//    public void setRed() {
//        setValue(AccessoryValue.RED);
//    }
//    public boolean isRed() {
//        return AccessoryValue.RED.equals(this.value);
//    }
//    public void setGreen() {
//        setValue(AccessoryValue.GREEN);
//    }
//    public boolean isGreen() {
//        return AccessoryValue.GREEN.equals(this.value);
//    }
//    public void setValue(String value) {
//        this.value = AccessoryValue.get(value);
//    }
//    public void setValue(AccessoryValue value) {
//        this.value = value;
//    }
//    public AccessoryValue getValue() {
//        return value;
//    }
//    public AccessoryValue toggleValue() {
//        if (AccessoryValue.GREEN.equals(this.value)) {
//            this.value = AccessoryValue.RED;
//        } else {
//            this.value = AccessoryValue.GREEN;
//        }
//        return this.value;
//    }
//    public boolean isSignal() {
//        return AccessoryType.SIGNAL.equals(this.accessoiryType);
//    }
//    public boolean isTurnout() {
//        return AccessoryType.TURNOUT.equals(this.accessoiryType);
//    }
}
