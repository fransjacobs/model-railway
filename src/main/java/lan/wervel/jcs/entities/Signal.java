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
package lan.wervel.jcs.entities;

import java.math.BigDecimal;
import java.util.Objects;
import lan.wervel.jcs.entities.enums.AccessoryType;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.SignalValue;

public class Signal extends SolenoidAccessory {

    public static final int HP01 = 2;
    public static final int HP012SH1 = 4;

    private BigDecimal id2;
    private Integer address2;
    private AccessoryValue value2;
    private SignalValue signalValue;

    public Signal() {
        this(null, "?", null, null, null, null, 2, null, null, null, null);
    }

    public Signal(Integer address, String description, String catalogNumber) {
        this(address, description, catalogNumber, null, null, null, 2, null, null, null, null);
    }

    public Signal(Integer address, String description, String catalogNumber, AccessoryValue value, Integer lightImages, Integer address2, AccessoryValue value2) {
        this(address, description, catalogNumber, null, value, null, lightImages, null, address2, value2, null);
    }

    public Signal(Integer address, String description, String catalogNumber, BigDecimal id, AccessoryValue value, BigDecimal soacId, Integer lightImages, SignalValue signalValue) {
        this(address, description, catalogNumber, id, value, soacId, lightImages, null, null, null, signalValue);
    }

    public Signal(Integer address, String description, String catalogNumber, BigDecimal id, AccessoryValue value, BigDecimal soacId, Integer lightImages, BigDecimal id2, Integer address2, AccessoryValue value2, SignalValue signalValue) {
        super(address, description, catalogNumber, id, AccessoryType.SIGNAL, value, soacId, lightImages);
        this.id2 = id2;
        this.value2 = value2;
        this.address2 = address2;
        this.signalValue = signalValue;

        if (this.signalValue == null) {
            setSignalValue(null, this.value, false);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.name == null) {
            sb.append("[S]");
            sb.append(": ");
        } else {
            sb.append(this.name);
        }
        if (!SignalValue.OFF.equals(this.signalValue)) {
            sb.append(", ");
            sb.append(this.signalValue);
        }
        return sb.toString();
    }

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("S: ");
        sb.append("[");
        if (this.address != null && this.address > 0) {
            sb.append(this.address);
        }
        sb.append("] ");
        sb.append(this.description);
        sb.append(" {");
        sb.append(this.value);
        sb.append("}");
        if (this.address2 != null && this.address2 > 0) {
            sb.append(" [");
            sb.append(this.address);
            sb.append("] ");
            sb.append(" {");
            sb.append(this.value2);
            sb.append("}");
        }
        sb.append(" ");
        sb.append(this.signalValue);

        return sb.toString();
    }

    public Integer getAddress2() {
        return address2;
    }

    public void setAddress2(Integer address2) {
        this.address2 = address2;
    }

    public void setRed2() {
        setValue2(AccessoryValue.RED);
    }

    public boolean isRed2() {
        return AccessoryValue.RED.equals(this.value2);
    }

    public void setGreen2() {
        setValue2(AccessoryValue.GREEN);
    }

    public boolean isGreen2() {
        return AccessoryValue.GREEN.equals(this.value2);
    }

    @Override
    public void setValue(String value) {
        setValue(AccessoryValue.get(value));
    }

    @Override
    public void setValue(AccessoryValue value) {
        AccessoryValue oldValue = this.value;
        super.setValue(value);
        setSignalValue(oldValue, value, false);
    }

    public void setValue2(String value2) {
        setValue2(AccessoryValue.get(value2));
    }

    public void setValue2(AccessoryValue value2) {
        AccessoryValue oldValue2 = this.value2;
        this.value2 = value2;
        setSignalValue(oldValue2, value2, true);
    }

    public AccessoryValue getValue2() {
        return value2;
    }

    public BigDecimal getId2() {
        return id2;
    }

    public void setId2(BigDecimal id2) {
        this.id2 = id2;
    }

    public SignalValue getSignalValue() {
        return signalValue;
    }

    private void setSignalValue(AccessoryValue oldValue, AccessoryValue newValue, boolean useValue2) {
        if (oldValue == null) {
            oldValue = AccessoryValue.OFF;
        }
        //Value 2 could be null...
        if (newValue == null) {
            this.signalValue = getSignalValue(oldValue, useValue2);
        } else {
            this.signalValue = getSignalValue(newValue, useValue2);
        }
    }

    public void setSignalValue(String signalValue) {
        setSignalValue(SignalValue.valueOf(signalValue));
    }

    public void setSignalValue(SignalValue signalValue) {
        this.signalValue = signalValue;
        //Also set the signal values
        switch (signalValue) {
            case Hp0:
                this.value = AccessoryValue.RED;
                break;
            case Hp1:
                this.value = AccessoryValue.GREEN;
                break;
            case Hp2:
                this.value2 = AccessoryValue.GREEN;
                break;
            case Hp0Sh1:
                this.value2 = AccessoryValue.RED;
                break;
            default:
                this.value = AccessoryValue.OFF;
                this.value2 = null;
                break;
        }
    }

//  @Override
//  public Signal copy() {
//    return new Signal(address, description, catalogNumber, id, value, soacId, lightImages, id2, address2, value2, signalValue);
//  }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 7 * hash + Objects.hashCode(super.hashCode());
        hash = 71 * hash + Objects.hashCode(this.id2);
        hash = 71 * hash + Objects.hashCode(this.value2);
        hash = 71 * hash + Objects.hashCode(this.address2);
        hash = 71 * hash + Objects.hashCode(this.signalValue);
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
        final Signal other = (Signal) obj;
        if (!super.equals(other)) {
            return false;
        }
        if (!Objects.equals(this.id2, other.id2)) {
            return false;
        }
        if (!Objects.equals(this.value2, other.value2)) {
            return false;
        }
        if (!Objects.equals(this.signalValue, other.signalValue)) {
            return false;
        }
        return Objects.equals(this.address2, other.address2);
    }

    private SignalValue getSignalValue(AccessoryValue value) {
        return getSignalValue(value, false);
    }

    private SignalValue getSignalValue(AccessoryValue value, boolean useValue2) {
        if (value == null) {
            return SignalValue.OFF;
        }
        switch (value) {
            case RED:
                return useValue2 ? SignalValue.Hp0Sh1 : SignalValue.Hp0;
            case GREEN:
                return useValue2 ? SignalValue.Hp2 : SignalValue.Hp1;
            default:
                return SignalValue.OFF;
        }
    }

}
