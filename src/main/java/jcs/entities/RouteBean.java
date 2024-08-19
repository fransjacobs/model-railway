/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.entities;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.beryx.awt.color.ColorFactory;

@Table(name = "routes", indexes = {
  @Index(name = "route_from_to_idx", columnList = "from_tile_id,from_suffix, to_tile_id,to_suffix", unique = true)})
public class RouteBean implements Serializable {

  private String id;
  private String fromTileId;
  private String fromSuffix;
  private String toTileId;
  private String toSuffix;
  private String color;
  private boolean locked;
  private String status;

  private List<RouteElementBean> routeElements;

  public RouteBean() {
  }

  public RouteBean(String id, String fromTileId, String fromSuffix, String toTileId, String toSuffix) {
    this(id, fromTileId, fromSuffix, toTileId, toSuffix, null);
  }

  public RouteBean(String id, String fromTileId, String fromSuffix, String toTileId, String toSuffix, String color) {
    this(id, fromTileId, fromSuffix, toTileId, toSuffix, color, false);
  }

  public RouteBean(String id, String fromTileId, String fromSuffix, String toTileId, String toSuffix, String color, boolean locked) {
    this(id, fromTileId, fromSuffix, toTileId, toSuffix, color, locked, new LinkedList<>());
  }

  public RouteBean(String id, String fromTileId, String fromSuffix, String toTileId, String toSuffix, String color, boolean locked, List<RouteElementBean> routeElements) {
    this.id = id;
    this.fromTileId = fromTileId;
    this.fromSuffix = fromSuffix;
    this.toTileId = toTileId;
    this.toSuffix = toSuffix;
    this.color = color;
    this.locked = locked;
    this.routeElements = routeElements;
  }

  @Id
  @Column(name = "id", nullable = false)
  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Column(name = "from_tile_id", nullable = false)
  public String getFromTileId() {
    return fromTileId;
  }

  public void setFromTileId(String fromTileId) {
    this.fromTileId = fromTileId;
  }

  @Column(name = "from_suffix", nullable = false)
  public String getFromSuffix() {
    return fromSuffix;
  }

  public void setFromSuffix(String fromSuffix) {
    this.fromSuffix = fromSuffix;
  }

  @Column(name = "to_tile_id", nullable = false)
  public String getToTileId() {
    return toTileId;
  }

  public void setToTileId(String toTileId) {
    this.toTileId = toTileId;
  }

  @Column(name = "to_suffix", nullable = false)
  public String getToSuffix() {
    return toSuffix;
  }

  public void setToSuffix(String toSuffix) {
    this.toSuffix = toSuffix;
  }

  @Column(name = "route_color", length = 255)
  public String getRouteColor() {
    return color;
  }

  public void setRouteColor(String color) {
    this.color = color;
  }

  private Color stringToColor(String colorString) {
    if (colorString != null) {
      if (colorString.startsWith("RGB:")) {
        String rgb = colorString.replaceAll("RGB:", "");
        return Color.decode(rgb);
      } else {
        return ColorFactory.valueOf(colorString);
      }
    } else {
      return null;
    }
  }

  @Transient
  public Color getColor() {
    return stringToColor(this.color);
  }

  public void setColor(Color color) {
    this.color = color.toString();
  }

  @Column(name = "locked", nullable = false, columnDefinition = "locked bool default '0'")
  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;

    //also lock unlock the routeselements
    if (routeElements != null && !routeElements.isEmpty()) {
      for (RouteElementBean reb : this.routeElements) {
        reb.setLocked(locked);
      }
    }
  }

  @Column(name = "status", length = 255)
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Transient
  public RouteState getRouteState() {
    if (this.status != null) {
      return RouteState.get(status);
    } else {
      return RouteState.FREE;
    }
  }

  public void setBlockState(RouteState routeState) {
    this.status = routeState.getState();
  }

  @Transient
  public List<RouteElementBean> getRouteElements() {
    return routeElements;
  }

  public void setRouteElements(List<RouteElementBean> routeElements) {
    this.routeElements = routeElements;

    for (RouteElementBean reb : this.routeElements) {
      reb.setLocked(this.locked);
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + Objects.hashCode(this.id);
    hash = 79 * hash + Objects.hashCode(this.fromTileId);
    hash = 79 * hash + Objects.hashCode(this.fromSuffix);
    hash = 79 * hash + Objects.hashCode(this.toTileId);
    hash = 79 * hash + Objects.hashCode(this.toSuffix);
    hash = 79 * hash + Objects.hashCode(this.color);
    hash = 79 * hash + Objects.hashCode(this.locked);
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
    final RouteBean other = (RouteBean) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.fromTileId, other.fromTileId)) {
      return false;
    }
    if (!Objects.equals(this.fromSuffix, other.fromSuffix)) {
      return false;
    }
    if (!Objects.equals(this.toTileId, other.toTileId)) {
      return false;
    }
    if (!Objects.equals(this.toSuffix, other.toSuffix)) {
      return false;
    }
    if (!Objects.equals(this.color, other.color)) {
      return false;
    }
    return (Objects.equals(this.locked, other.locked));
  }

  @Override
  public String toString() {
    return "Route{" + "id=" + id + ", fromTile=" + fromTileId + "[" + fromSuffix + "], toTile=" + toTileId + "[" + toSuffix + "], color=" + color + ", locked=" + locked + "}";
  }

  public String toLogString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Route: ");
    sb.append(this.id);
    sb.append(": ");
    if (this.routeElements != null && !this.routeElements.isEmpty()) {
      for (RouteElementBean re : this.routeElements) {
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

        if (!re.getTileId().equals(this.toTileId)) {
          sb.append(" -> ");
        }
      }
    }
    return sb.toString();
  }

  public enum RouteState {
    FREE("Free"), RESERVED("Reserved"), BLOCKED("Blocked");

    private final String state;

    private static final Map<String, RouteState> ENUM_MAP;

    RouteState(String state) {
      this.state = state;
    }

    public String getState() {
      return this.state;
    }

    static {
      Map<String, RouteState> map = new ConcurrentHashMap<>();
      for (RouteState instance : RouteState.values()) {
        map.put(instance.getState(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static RouteState get(String state) {
      if (state == null) {
        return null;
      }
      return ENUM_MAP.get(state);
    }
  }

}
