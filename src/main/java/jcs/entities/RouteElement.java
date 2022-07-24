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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import jcs.entities.enums.AccessoryValue;

public class RouteElement implements JCSEntity, Serializable {

    private BigDecimal id;
    private String routeId;
    private String nodeId;
    private String tileId;
    private AccessoryValue accessoryValue;
    private Integer elementOrder;

    public RouteElement() {
    }

    public RouteElement(String routeId, String nodeId, String tileId, AccessoryValue accessoryValue, Integer elementOrder) {
        this(routeId, nodeId, tileId, accessoryValue, elementOrder, null);
    }

    public RouteElement(String routeId, String nodeId, String tileId, AccessoryValue accessoryValue, Integer elementOrder, BigDecimal id) {
        this.routeId = routeId;
        this.nodeId = nodeId;
        this.tileId = tileId;
        this.accessoryValue = accessoryValue;
        this.elementOrder = elementOrder;
        this.id = id;
    }

    @Override
    public BigDecimal getId() {
        return this.id;
    }

    @Override
    public void setId(Object id) {
        this.id = (BigDecimal) id;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTileId() {
        return tileId;
    }

    public void setTileId(String TileId) {
        this.tileId = TileId;
    }

    public AccessoryValue getAccessoryValue() {
        return accessoryValue;
    }

    public void setAccessoryValue(AccessoryValue accessoryValue) {
        this.accessoryValue = accessoryValue;
    }

    public Integer getElementOrder() {
        return elementOrder;
    }

    public void setElementOrder(Integer elementOrder) {
        this.elementOrder = elementOrder;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.id);
        hash = 23 * hash + Objects.hashCode(this.routeId);
        hash = 23 * hash + Objects.hashCode(this.nodeId);
        hash = 23 * hash + Objects.hashCode(this.tileId);
        hash = 23 * hash + Objects.hashCode(this.accessoryValue);
        hash = 23 * hash + Objects.hashCode(this.elementOrder);
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
        final RouteElement other = (RouteElement) obj;
        if (!Objects.equals(this.elementOrder, other.elementOrder)) {
            return false;
        }
        if (!Objects.equals(this.routeId, other.routeId)) {
            return false;
        }
        if (!Objects.equals(this.nodeId, other.nodeId)) {
            return false;
        }
        if (!Objects.equals(this.tileId, other.tileId)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.accessoryValue, other.accessoryValue);
    }

    @Override
    public String toString() {
        return "RouteElement{routeId=" + routeId + ", nodeId=" + nodeId + ", tileId=" + tileId + ", accessoryValue=" + accessoryValue + ", elementOrder=" + elementOrder + ", id=" + id + "}";
    }

    @Override
    public String toLogString() {
        return toString();
    }

}
