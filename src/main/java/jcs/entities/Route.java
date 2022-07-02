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

import java.awt.Color;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class Route implements JCSEntity, Serializable {

    private String id;
    private String fromId;
    private String toId;
    private boolean locked;
    private boolean blocked;
    private Color color;
    private BigDecimal locoId;

    private List<String> elementIds;

    private List<RouteElement> elements;

    public Route() {
    }

    public Route(String fromId, String toId, List<String> elementIds) {
        this.fromId = fromId;
        this.toId = toId;
        this.id = fromId + "|" + toId;

        this.elementIds = elementIds;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(Object id) {
        this.id = (String) id;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BigDecimal getLocoId() {
        return locoId;
    }

    public void setLocoId(BigDecimal locoId) {
        this.locoId = locoId;
    }

    public List<String> getElementIds() {
        return elementIds;
    }

    public void setElementIds(List<String> elementIds) {
        this.elementIds = elementIds;
    }

    public List<RouteElement> getElements() {
        return elements;
    }

    public void setElements(List<RouteElement> elements) {
        this.elements = elements;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        hash = 79 * hash + Objects.hashCode(this.fromId);
        hash = 79 * hash + Objects.hashCode(this.toId);
        hash = 79 * hash + (this.locked ? 1 : 0);
        hash = 79 * hash + (this.blocked ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.color);
        hash = 79 * hash + Objects.hashCode(this.locoId);
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
        final Route other = (Route) obj;
        if (this.locked != other.locked) {
            return false;
        }
        if (this.blocked != other.blocked) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.fromId, other.fromId)) {
            return false;
        }
        if (!Objects.equals(this.toId, other.toId)) {
            return false;
        }
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        return Objects.equals(this.locoId, other.locoId);
    }

    @Override
    public String toString() {
        return "Route{" + "id=" + id + ", fromTileId=" + fromId + ", toTileId=" + toId + '}';
    }

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Path: ");
        sb.append(this.id);
        sb.append(": ");
        if (this.elementIds != null && !this.elementIds.isEmpty()) {
            for (String e : this.elementIds) {
                sb.append(e);
                if(!e.equals(this.toId)) {
                  sb.append(" -> ");
                }
            }
        }
        return sb.toString();
    }

}
