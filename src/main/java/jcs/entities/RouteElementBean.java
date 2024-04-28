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

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.TileBean.TileType;

@Table(name = "route_elements", indexes = {
  @Index(name = "roel_rout_node+tile_idx", columnList = "route_id,node_id,tile_id", unique = true)})
public class RouteElementBean implements Serializable {

  private Long id;
  private String routeId;
  private String nodeId;
  private String tileId;
  private String accessoryState;
  private Integer elementOrder;
  private String incomingSide;

  private TileBean tileBean;

  public RouteElementBean() {
  }

  public RouteElementBean(String routeId, String nodeId, String tileId, AccessoryValue accessoryValue, Integer elementOrder) {
    this(null, routeId, nodeId, tileId, (accessoryValue != null ? accessoryValue.getDBValue() : null), elementOrder);
  }

  public RouteElementBean(Long id, String routeId, String nodeId, String tileId, String accessoryValue, Integer elementOrder) {
    this.id = id;
    this.routeId = routeId;
    this.nodeId = nodeId;
    this.tileId = tileId;
    this.accessoryState = accessoryValue;
    this.elementOrder = elementOrder;
  }

  @Id
  @GeneratedValue
  @Column(name = "id")
  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "route_id", nullable = false)
  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  @Column(name = "node_id", length = 255, nullable = false)
  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  @Column(name = "tile_id", length = 255, nullable = false)
  public String getTileId() {
    return tileId;
  }

  public void setTileId(String TileId) {
    this.tileId = TileId;
  }

  @Column(name = "accessory_value", length = 255)
  public String getAccessoryState() {
    return accessoryState;
  }

  public void setAccessoryState(String accessoryState) {
    this.accessoryState = accessoryState;
  }

  @Transient
  public AccessoryValue getAccessoryValue() {
    return AccessoryValue.dbGet(this.accessoryState);
  }

  public void setAccessoryValue(AccessoryValue accessoryValue) {
    this.accessoryState = accessoryValue.getDBValue();
  }

  @Column(name = "order_seq", columnDefinition = "order_seq integer default 0")
  public Integer getElementOrder() {
    return elementOrder;
  }

  public void setElementOrder(Integer elementOrder) {
    this.elementOrder = elementOrder;
  }

  @Column(name = "incoming_side", length = 255)
  public String getIncomingSide() {
    return incomingSide;
  }

  public void setIncomingSide(String incomingSide) {
    this.incomingSide = incomingSide;
  }

  @Transient
  public TileBean.Orientation getIncomingOrientation() {
    if (incomingSide != null) {
      return TileBean.Orientation.get(this.incomingSide);
    } else {
      return null;
    }
  }

  public void setIncomingOrientation(TileBean.Orientation orientation) {
    if (orientation != null) {
      this.incomingSide = orientation.getOrientation();
    } else {
      this.incomingSide = null;
    }
  }

  @Transient
  public TileBean getTileBean() {
    return tileBean;
  }

  public void setTileBean(TileBean tileBean) {
    this.tileBean = tileBean;
  }

  @Transient
  public boolean isTurnout() {
    if (tileBean != null) {
      return TileType.SWITCH == tileBean.getTileType() || TileType.CROSS == tileBean.getTileType();
    } else {
      return false;
    }
  }

  @Transient
  public boolean isSignal() {
    if (tileBean != null) {
      return TileType.SIGNAL == tileBean.getTileType();
    } else {
      return false;
    }
  }

  @Transient
  public boolean isSensor() {
    if (tileBean != null) {
      return TileType.SENSOR == tileBean.getTileType();
    } else {
      return false;
    }
  }

  @Transient
  public boolean isBlock() {
    if (tileBean != null) {
      return TileType.BLOCK == tileBean.getTileType();
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + Objects.hashCode(this.id);
    hash = 23 * hash + Objects.hashCode(this.routeId);
    hash = 23 * hash + Objects.hashCode(this.nodeId);
    hash = 23 * hash + Objects.hashCode(this.tileId);
    hash = 23 * hash + Objects.hashCode(this.accessoryState);
    hash = 23 * hash + Objects.hashCode(this.elementOrder);
    hash = 23 * hash + Objects.hashCode(this.incomingSide);
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
    final RouteElementBean other = (RouteElementBean) obj;
    if (!Objects.equals(this.incomingSide, other.incomingSide)) {
      return false;
    }
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
    return Objects.equals(this.accessoryState, other.accessoryState);
  }

  @Override
  public String toString() {
    return "RouteElement{id=" + id + ", routeId=" + routeId + ", nodeId=" + nodeId + ", tileId=" + tileId + ", accessoryValue=" + accessoryState + ", elementOrder=" + elementOrder + ", incomingSide=" + incomingSide + "}";
  }

  public String toLogString() {
    return toString();
  }

}
