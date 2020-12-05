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
import lan.wervel.jcs.entities.enums.AccessoryType;
import lan.wervel.jcs.entities.enums.AccessoryValue;

public class Turnout extends SolenoidAccessory {

    public Turnout() {
        this(null, "?", null, null, null);
    }

    public Turnout(Integer address, String description) {
        this(address, description, null, null, null);
    }

    public Turnout(Integer address, String description, String catalogNumber) {
        this(address, description, catalogNumber, null, null);
    }

    public Turnout(Integer address, String description, String catalogNumber, BigDecimal id) {
        this(address, description, catalogNumber, id, null);
    }

    public Turnout(Integer address, String description, String catalogNumber, BigDecimal id, AccessoryValue value) {
        super(address, description, catalogNumber, id, AccessoryType.TURNOUT, value, null, 2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.name == null) {
            sb.append("[T]: ");
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
        sb.append("] T: ");
        sb.append(this.description);
        sb.append(" {");
        sb.append(this.value);
        sb.append("}");
        return sb.toString();
    }

    public void setCurved() {
        setValue(AccessoryValue.RED);
    }

    public boolean isCurved() {
        return AccessoryValue.RED.equals(this.value);
    }

    public void setStraight() {
        setValue(AccessoryValue.GREEN);
    }

    public boolean isStraight() {
        return AccessoryValue.GREEN.equals(this.value);
    }

}
