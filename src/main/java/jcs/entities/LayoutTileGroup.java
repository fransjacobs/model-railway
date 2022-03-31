/*
 * Copyright (C) 2020 Frans Jacobs.
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

/**
 *
 * @author Frans Jacobs
 */
public class LayoutTileGroup extends ControllableDevice {

    private String color;
    private String direction;
    private BigDecimal startLatiId;
    private BigDecimal endLatiId;

    public LayoutTileGroup() {
        this(null, null, null, null, null, null, null);
    }

    public LayoutTileGroup(Integer groupNumber) {
        this(null, null, null, null, null, null, groupNumber);
    }

    public LayoutTileGroup(Integer groupNumber, String direction) {
        this(null, null, null, direction, null, null, groupNumber);
    }

    public LayoutTileGroup(BigDecimal id, String name, Integer groupNumber) {
        this(id, name, null, null, null, null, groupNumber);
    }

    public LayoutTileGroup(BigDecimal id, String name, String color, String direction, Integer groupNumber) {
        this(id, name, color, direction, null, null, groupNumber);
    }

    public LayoutTileGroup(BigDecimal id, String name, String color, String direction, BigDecimal startLatiId, BigDecimal endLatiId, Integer groupNumber) {
        super(id, groupNumber, name);
        this.color = color;
        this.direction = direction;
        this.startLatiId = startLatiId;
        this.endLatiId = endLatiId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public BigDecimal getStartLatiId() {
        return startLatiId;
    }

    public void setStartLatiId(BigDecimal startLatiId) {
        this.startLatiId = startLatiId;
    }

    public BigDecimal getEndLatiId() {
        return endLatiId;
    }

    public void setEndLatiId(BigDecimal endLatiId) {
        this.endLatiId = endLatiId;
    }

    public Integer getGroupNumber() {
        return this.address;
    }

    public void setGroupNumber(Integer groupNumber) {
        this.address = groupNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.id == null && this.address == null && this.name == null) {
            sb.append("");
        } else {
            if (this.name != null) {
                sb.append(this.name);
                if (this.address != null) {
                    sb.append(" ");
                    sb.append(this.address);
                }
            } else {
                if (this.address != null && this.address > 0) {
                    sb.append("Block: ");
                    sb.append(this.address);
                } else {
                    sb.append("Block Id: ");
                    sb.append(this.id);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String toLogString() {
        return this.id + ";" + this.name + ";" + this.direction + ";" + this.color + ";" + this.address;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 89 * hash + Objects.hashCode(this.color);
        hash = 89 * hash + Objects.hashCode(this.direction);
        hash = 89 * hash + Objects.hashCode(this.startLatiId);
        hash = 89 * hash + Objects.hashCode(this.endLatiId);
        hash = 89 * hash + Objects.hashCode(this.address);
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
        final LayoutTileGroup other = (LayoutTileGroup) obj;
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (!Objects.equals(this.direction, other.direction)) {
            return false;
        }
        if (!Objects.equals(this.startLatiId, other.startLatiId)) {
            return false;
        }
        if (!Objects.equals(this.endLatiId, other.endLatiId)) {
            return false;
        }

        return super.equals(other);
    }

}
