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
import jcs.entities.enums.AccessoryType;
import static jcs.entities.enums.AccessoryType.SIGNAL;
import static jcs.entities.enums.AccessoryType.TURNOUT;
import jcs.entities.enums.AccessoryValue;

public abstract class SolenoidAccessory extends ControllableDevice {

    protected AccessoryValue value;
    protected AccessoryType accessoiryType;
    protected BigDecimal soacId;
    protected Integer lightImages;
    protected Integer switchTime;

    protected SolenoidAccessory(Integer address, String description, String catalogNumber, BigDecimal id, AccessoryType type, AccessoryValue value, BigDecimal soacId, Integer lightImages) {
        super(address, (type != null ? type.toString() : null), description, catalogNumber, id);
        this.accessoiryType = type;
        this.value = value;
        this.soacId = soacId;
        this.lightImages = lightImages;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.name == null) {
            if (null == this.accessoiryType) {
                sb.append("[G]");
            } else {
                switch (this.accessoiryType) {
                    case SIGNAL:
                        sb.append("[S]");
                        break;
                    case TURNOUT:
                        sb.append("[T]");
                        break;
                    default:
                        sb.append("[G]");
                        break;
                }
            }
            sb.append(": ");
        } else {
            sb.append(this.name);
        }
        if (this.address != null && this.address > 0) {
            sb.append(" (");
            sb.append(this.address);
            sb.append(")");
        }

        return sb.toString();
    }

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (this.address != null && this.address > 0) {
            sb.append(this.address);
        }
        sb.append("] ");
        if (null == this.accessoiryType) {
            sb.append("G: ");
        } else {
            sb.append("");
            switch (this.accessoiryType) {
                case SIGNAL:
                    sb.append("S: ");
                    break;
                case TURNOUT:
                    sb.append("T: ");
                    break;
                default:
                    sb.append("G: ");
                    break;
            }
        }
        sb.append(this.description);
        sb.append(" {");
        sb.append(this.value);
        sb.append("}");
        return sb.toString();
    }

    public AccessoryType getAccessoiryType() {
        return accessoiryType;
    }

    public Integer getLightImages() {
        return lightImages;
    }

    public void setLightImages(Integer lightImages) {
        this.lightImages = lightImages;
    }

    public void setAccessoiryType(AccessoryType accessoiryType) {
        this.accessoiryType = accessoiryType;
    }

    public void setRed() {
        setValue(AccessoryValue.RED);
    }

    public boolean isRed() {
        return AccessoryValue.RED.equals(this.value);
    }

    public void setGreen() {
        setValue(AccessoryValue.GREEN);
    }

    public boolean isGreen() {
        return AccessoryValue.GREEN.equals(this.value);
    }

    public void setValue(String value) {
        this.value = AccessoryValue.get(value);
    }

    public void setValue(AccessoryValue value) {
        this.value = value;
    }

    public AccessoryValue getValue() {
        return value;
    }

    public AccessoryValue toggleValue() {
        if (AccessoryValue.GREEN.equals(this.value)) {
            this.value = AccessoryValue.RED;
        } else {
            this.value = AccessoryValue.GREEN;
        }
        return this.value;
    }

    public BigDecimal getSoacId() {
        return soacId;
    }

    public void setSoacId(BigDecimal soacId) {
        this.soacId = soacId;
    }

    public boolean isSignal() {
        return AccessoryType.SIGNAL.equals(this.accessoiryType);
    }

    public boolean isTurnout() {
        return AccessoryType.TURNOUT.equals(this.accessoiryType);
    }

    public Integer getSwitchTime() {
        return switchTime;
    }

    public void setSwitchTime(Integer switchTime) {
        this.switchTime = switchTime;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(super.hashCode());
        hash = 17 * hash + Objects.hashCode(this.value);
        hash = 17 * hash + Objects.hashCode(this.accessoiryType);
        hash = 17 * hash + Objects.hashCode(this.soacId);
        hash = 17 * hash + Objects.hashCode(this.lightImages);
        hash = 17 * hash + Objects.hashCode(this.switchTime);
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
        final SolenoidAccessory other = (SolenoidAccessory) obj;
        if (!super.equals(other)) {
            return false;
        }
        if (this.value != other.value) {
            return false;
        }
        if (this.accessoiryType != other.accessoiryType) {
            return false;
        }
        if (!Objects.equals(this.soacId, other.soacId)) {
            return false;
        }
        if (!Objects.equals(this.switchTime, other.switchTime)) {
            return false;
        }
        return Objects.equals(this.lightImages, other.lightImages);
    }

}
