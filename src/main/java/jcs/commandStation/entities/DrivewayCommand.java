/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.commandStation.entities;

import java.util.Objects;
import jcs.entities.AccessoryBean;

/**
 *
 * Bean to display properties and command(s) for a Driveway
 */
public class DrivewayCommand {

  private Long id;
  private String routeId;
  private String tileId;
  private String accessoryId;
  private Integer address;
  private String protocol;
  private String name;
  private AccessoryBean.AccessoryValue accessoryValue;
  private Integer sortOrder;

  public DrivewayCommand() {

  }

  public DrivewayCommand(Long id, String routeId, String tileId, String accessoryId, Integer address, String protocol, String name, AccessoryBean.AccessoryValue accessoryValue, Integer sortOrder) {
    this.routeId = routeId;
    this.tileId = tileId;
    this.accessoryId = accessoryId;
    this.address = address;
    this.protocol = protocol;
    this.name = name;
    this.accessoryValue = accessoryValue;
    this.sortOrder = sortOrder;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getTileId() {
    return tileId;
  }

  public void setTileId(String tileId) {
    this.tileId = tileId;
  }

  public String getAccessoryId() {
    return accessoryId;
  }

  public void setAccessoryId(String accessoryId) {
    this.accessoryId = accessoryId;
  }

  public Integer getAddress() {
    return address;
  }

  public void setAddress(Integer address) {
    this.address = address;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AccessoryBean.AccessoryValue getAccessoryValue() {
    return accessoryValue;
  }

  public void setAccessoryValue(AccessoryBean.AccessoryValue accessoryValue) {
    this.accessoryValue = accessoryValue;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 79 * hash + Objects.hashCode(this.id);
    hash = 79 * hash + Objects.hashCode(this.routeId);
    hash = 79 * hash + Objects.hashCode(this.accessoryId);
    hash = 79 * hash + Objects.hashCode(this.address);
    hash = 79 * hash + Objects.hashCode(this.protocol);
    hash = 79 * hash + Objects.hashCode(this.name);
    hash = 79 * hash + Objects.hashCode(this.accessoryValue);
    hash = 79 * hash + Objects.hashCode(this.sortOrder);
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
    final DrivewayCommand other = (DrivewayCommand) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.routeId, other.routeId)) {
      return false;
    }
    if (!Objects.equals(this.tileId, other.tileId)) {
      return false;
    }
    if (!Objects.equals(this.accessoryId, other.accessoryId)) {
      return false;
    }
    if (!Objects.equals(this.protocol, other.protocol)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.address, other.address)) {
      return false;
    }
    if (this.accessoryValue != other.accessoryValue) {
      return false;
    }
    return Objects.equals(this.sortOrder, other.sortOrder);
  }

}
