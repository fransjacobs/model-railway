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
import jcs.entities.enums.Orientation;
import jcs.ui.layout.tiles.enums.Direction;

public class RouteElement implements JCSEntity, Serializable {

    private BigDecimal id;
    private String routeId;
    private String TileId;
    private int elementOrder;
    private Orientation tileOrientation;
    private Direction tileDirection;

    public RouteElement() {
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

    public String getTileId() {
        return TileId;
    }

    public void setTileId(String TileId) {
        this.TileId = TileId;
    }

    public int getElementOrder() {
        return elementOrder;
    }

    public void setElementOrder(int elementOrder) {
        this.elementOrder = elementOrder;
    }

    public Orientation getTileOrientation() {
        return tileOrientation;
    }

    public void setTileOrientation(Orientation tileOrientation) {
        this.tileOrientation = tileOrientation;
    }

    public Direction getTileDirection() {
        return tileDirection;
    }

    public void setTileDirection(Direction tileDirection) {
        this.tileDirection = tileDirection;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.id);
        hash = 23 * hash + Objects.hashCode(this.routeId);
        hash = 23 * hash + Objects.hashCode(this.TileId);
        hash = 23 * hash + this.elementOrder;
        hash = 23 * hash + Objects.hashCode(this.tileOrientation);
        hash = 23 * hash + Objects.hashCode(this.tileDirection);
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
        if (this.elementOrder != other.elementOrder) {
            return false;
        }
        if (!Objects.equals(this.routeId, other.routeId)) {
            return false;
        }
        if (!Objects.equals(this.TileId, other.TileId)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.tileOrientation != other.tileOrientation) {
            return false;
        }
        return this.tileDirection == other.tileDirection;
    }

    @Override
    public String toString() {
        return "RouteElement{" + "id=" + id + ", routeId=" + routeId + ", TileId=" + TileId + ", elementOrder=" + elementOrder + '}';
    }

    @Override
    public String toLogString() {
        return toString();
    }

}
