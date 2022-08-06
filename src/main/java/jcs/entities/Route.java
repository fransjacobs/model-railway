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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import jcs.entities.enums.AccessoryValue;
import org.beryx.awt.color.ColorFactory;
import org.tinylog.Logger;

public class Route implements JCSEntity, Serializable {

    private String id;
    private String fromId;
    private String toId;
    private Color color;

    private List<String> elementIds;
    private List<RouteElement> routeElements;

    public Route() {
    }

    public Route(String id, String fromId, String toId, String color) {
        this(id, fromId, toId, color, new LinkedList<>());
    }

    public Route(String fromId, String toId, List<String> elementIds) {
        this(null, fromId, toId, null, elementIds, createdRouteElementsFromElements(fromId, toId, elementIds));
    }

    public Route(String fromId, String toId, String color, List<String> elementIds) {
        this(null, fromId, toId, color, elementIds, createdRouteElementsFromElements(fromId, toId, elementIds));
    }

    public Route(String id, String fromId, String toId, String color, List<RouteElement> routeElements) {
        this(id, fromId, toId, color, toElementIds(routeElements), routeElements);
    }

    public Route(String id, String fromId, String toId, String color, List<String> elementIds, List<RouteElement> routeElements) {
        if (id == null) {
            this.id = fromId + "|" + toId;
        } else {
            this.id = id;
        }
        this.fromId = fromId;
        this.toId = toId;

        if (color != null) {
            if (color.startsWith("RGB:")) {
                String rgb = color.replaceAll("RGB:", "");
                this.color = Color.decode(rgb);
            } else {
                this.color = ColorFactory.valueOf(color);
            }
        }
        this.elementIds = elementIds;
        this.routeElements = routeElements;
    }

    private static List<RouteElement> createdRouteElementsFromElements(String fromId, String toId, List<String> elementIds) {
        List<RouteElement> rel = new LinkedList<>();
        for (int i = 0; i < elementIds.size(); i++) {
            String nodeId = elementIds.get(i);
            
            String tileId;
            if(nodeId.endsWith("-R")) {
                tileId = nodeId.replace("-R","");
            }
            else if(nodeId.endsWith("-G")) {
                tileId = nodeId.replace("-G","");
            }
            else if(nodeId.endsWith("-") || nodeId.endsWith("+")) {
                tileId = nodeId.substring(0,nodeId.length() -1);
            }
            else {
                tileId = nodeId;
            }

            AccessoryValue accessoryValue = null;
            if (nodeId.endsWith("-R")) {
                accessoryValue = AccessoryValue.RED;
            } else if (nodeId.endsWith("-G")) {
                accessoryValue = AccessoryValue.GREEN;
            }
            Integer elementOrder = i;

            RouteElement routeElement = new RouteElement(fromId + "|" + toId, nodeId, tileId, accessoryValue, elementOrder);
            rel.add(routeElement);

            Logger.trace(routeElement);
        }
        return rel;
    }

    private static List<String> toElementIds(List<RouteElement> routeElements) {
        List<String> eids = new LinkedList<>();
        for (RouteElement re : routeElements) {
            eids.add(re.getNodeId());
        }
        return eids;
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<String> getElementIds() {
        return elementIds;
    }

    public void setElementIds(List<String> elementIds) {
        this.elementIds = elementIds;
    }

    public List<RouteElement> getRouteElements() {
        return routeElements;
    }

    public void setRouteElements(List<RouteElement> routeElements) {
        this.routeElements = routeElements;
        this.elementIds = toElementIds(routeElements);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        hash = 79 * hash + Objects.hashCode(this.fromId);
        hash = 79 * hash + Objects.hashCode(this.toId);
        hash = 79 * hash + Objects.hashCode(this.color);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.fromId, other.fromId)) {
            return false;
        }
        if (!Objects.equals(this.toId, other.toId)) {
            return false;
        }
        return (Objects.equals(this.color, other.color));
    }

    @Override
    public String toString() {
        return "Route{" + "id=" + id + ", fromTileId=" + fromId + ", toTileId=" + toId + ", color=" + color + "}";
    }

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Route: ");
        sb.append(this.id);
        sb.append(": ");
        if (this.routeElements != null && !this.routeElements.isEmpty()) {
            for (RouteElement re : this.routeElements) {
                sb.append(re.getNodeId());
                if (!re.getNodeId().equals(re.getTileId())) {
                    sb.append("[");
                    sb.append(re.getTileId());
                    sb.append("]");
                }

                if (re.getAccessoryValue() != null) {
                    sb.append("[");
                    sb.append(re.getAccessoryValue());
                    sb.append("]");
                }

                if (!re.getNodeId().equals(this.toId)) {
                    sb.append(" -> ");
                }
            }
        }
        return sb.toString();
    }

}
