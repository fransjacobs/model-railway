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

@Deprecated
public class DriveWay extends ControllableDevice {

    private BigDecimal fromLatiId;
    private BigDecimal toLatiId;
    private BigDecimal locoId;
    private boolean active;
    private boolean reserved;
    private boolean occupied;

    public DriveWay() {
        this(null, null, null);
    }

    public DriveWay(Integer address, String name, String description) {
        this(null, address, name, description, null, null, null, false, false, false);
    }

    public DriveWay(BigDecimal id, Integer address, String name, String description, BigDecimal fromLatiId, BigDecimal toLatiId, BigDecimal locoId, boolean active, boolean reserved, boolean occupied) {
        super(id, address, name, description);
        this.fromLatiId = fromLatiId;
        this.toLatiId = toLatiId;
        this.locoId = locoId;
        this.active = active;
        this.reserved = reserved;
        this.occupied = occupied;
    }

    public BigDecimal getFromLatiId() {
        return fromLatiId;
    }

    public void setFromLatiId(BigDecimal fromLatiId) {
        this.fromLatiId = fromLatiId;
    }

    public BigDecimal getToLatiId() {
        return toLatiId;
    }

    public void setToLatiId(BigDecimal toLatiId) {
        this.toLatiId = toLatiId;
    }

    public BigDecimal getLocoId() {
        return locoId;
    }

    public void setLocoId(BigDecimal locoId) {
        this.locoId = locoId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(super.hashCode());
        hash = 59 * hash + Objects.hashCode(this.fromLatiId);
        hash = 59 * hash + Objects.hashCode(this.toLatiId);
        hash = 59 * hash + Objects.hashCode(this.locoId);
        hash = 59 * hash + (this.active ? 1 : 0);
        hash = 59 * hash + (this.reserved ? 1 : 0);
        hash = 59 * hash + (this.occupied ? 1 : 0);
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
        final DriveWay other = (DriveWay) obj;
        if (!super.equals(other)) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        if (this.reserved != other.reserved) {
            return false;
        }
        if (this.occupied != other.occupied) {
            return false;
        }
        if (!Objects.equals(this.fromLatiId, other.fromLatiId)) {
            return false;
        }
        if (!Objects.equals(this.toLatiId, other.toLatiId)) {
            return false;
        }
        return Objects.equals(this.locoId, other.locoId);
    }

    @Override
    public String toString() {
        return "DriveWay{" + "fromLatiId=" + fromLatiId + ", toLatiId=" + toLatiId + ", locoId=" + locoId + ", active=" + active + ", reserved=" + reserved + ", occupied=" + occupied + '}';
    }

    @Override
    public String toLogString() {
        return toString();
    }

}
