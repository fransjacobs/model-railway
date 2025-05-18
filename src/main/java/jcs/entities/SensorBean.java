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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.Date;
import java.util.Objects;

@Table(name = "sensors", indexes = {
  @Index(name = "sens_devi_cont_idx", columnList = "device_id, contact_id", unique = true)})
public class SensorBean {

  private Integer id;
  private String name;
  private Integer deviceId;
  private Integer contactId;
  private Integer status;
  private Integer previousStatus;
  private Integer millis;
  private Long lastUpdated;
  private Integer nodeId;
  private String commandStationId;

  public SensorBean() {
    this(null, null, null, null, null, null, null);
  }

  public SensorBean(Integer id, Integer deviceId, Integer contactId, Integer nodeId, Integer status, Integer previousStatus, String commandStationId) {
    this(id, null, deviceId, contactId, nodeId, status, previousStatus, (Integer) null, (Long) null, commandStationId);
  }

  public SensorBean(Integer id, String name, Integer deviceId, Integer contactId, Integer nodeId, Integer status, Integer previousStatus, Integer millis, String commandStationId) {
    this(id, name, deviceId, contactId, nodeId, status, previousStatus, millis, (Long) null, commandStationId);
  }

  public SensorBean(Integer id, String name, Integer deviceId, Integer contactId, Integer nodeId, Integer status, Integer previousStatus, Integer millis, Date lastUpdated, String commandStationId) {
    this(id, name, deviceId, contactId, nodeId, status, previousStatus, millis, (lastUpdated != null ? lastUpdated.getTime() : null), commandStationId);
  }

  public SensorBean(Integer id, String name, Integer deviceId, Integer contactId, Integer nodeId, Integer status, Integer previousStatus, Integer millis, Long lastUpdated, String commandStationId) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.previousStatus = previousStatus;
    this.deviceId = deviceId;
    this.contactId = contactId;
    this.nodeId = nodeId;
    this.millis = millis;
    this.lastUpdated = lastUpdated;
    this.commandStationId = commandStationId;

    if (name == null) {
      this.name = generateName();
    }
  }

  private String generateName() {
    if (deviceId != null && contactId != null && nodeId != null) {

      String dn = deviceId.toString();
      int dnl = dn.length();
      for (int x = 0; x < 2 - dnl; x++) {
        dn = "0" + dn;
      }

      String cn = contactId.toString();
      int cnl = cn.length();
      for (int x = 0; x < 4 - cnl; x++) {
        cn = "0" + cn;
      }

      return dn + "-" + cn;
    } else {
      return null;
    }
  }

  @Id
  @Column(name = "id", nullable = false)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Column(name = "name", length = 255, nullable = false)
  public String getName() {
    if (name == null) {
      name = generateName();
    }
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Column(name = "device_id")
  public Integer getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(Integer deviceId) {
    this.deviceId = deviceId;
  }

  @Column(name = "contact_id")
  public Integer getContactId() {
    return contactId;
  }

  public void setContactId(Integer contactId) {
    this.contactId = contactId;
  }

  @Column(name = "node_id")
  public Integer getNodeId() {
    return nodeId;
  }

  public void setNodeId(Integer nodeId) {
    this.nodeId = nodeId;
  }

  @Column(name = "status")
  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  @Column(name = "previous_status")
  public Integer getPreviousStatus() {
    return previousStatus;
  }

  public void setPreviousStatus(Integer previousStatus) {
    this.previousStatus = previousStatus;
  }

  @Column(name = "command_station_id", length = 255, nullable = false)
  public String getCommandStationId() {
    return commandStationId;
  }

  public void setCommandStationId(String commandStationId) {
    this.commandStationId = commandStationId;
  }

  @Transient
  public void toggle() {
    if (status == null) {
      status = 0;
    }
    previousStatus = status;
    Long lastChanged = lastUpdated;
    if (lastChanged == null) {
      lastChanged = System.currentTimeMillis();
    }
    if (status == 0) {
      status = 1;
    } else {
      status = 0;
    }
    lastUpdated = System.currentTimeMillis();
    Long m = (lastUpdated - lastChanged) / 10;
    this.millis = m.intValue();
  }

  @Column(name = "millis")
  public Integer getMillis() {
    return millis;
  }

  public void setMillis(Integer millis) {
    this.millis = millis;
  }

  @Transient
  public Long getLastUpdatedMillis() {
    return this.lastUpdated;
  }

  public void setLastUpdatedMillis(Long updatedOn) {
    this.lastUpdated = updatedOn;
  }

  @Column(name = "last_updated")
  public Date getLastUpdated() {
    if (lastUpdated != null) {
      return new Date(lastUpdated);
    } else {
      return null;
    }
  }

  public void setLastUpdated(Date updatedOn) {
    Long prevUpdated = lastUpdated;
    if (updatedOn != null) {
      lastUpdated = updatedOn.getTime();
    }

    if (lastUpdated != null && prevUpdated != null) {
      Long m = (lastUpdated - prevUpdated) / 10;
      millis = m.intValue();
    }
  }

  @Transient
  public boolean isActive() {
    if (status != null) {
      return this.status > 0;
    } else {
      return false;
    }
  }

  public void setActive(boolean active) {
    previousStatus = status;
    status = active ? 1 : 0;
  }

  public void setPreviousActive(boolean active) {
    previousStatus = active ? 1 : 0;
  }

  @Transient
  public boolean isPreviousActive() {
    if (previousStatus != null) {
      return previousStatus > 0;
    } else {
      return false;
    }
  }

  @Transient
  public boolean hasChanged() {
    return !status.equals(previousStatus);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + Objects.hashCode(this.id);
    hash = 41 * hash + Objects.hashCode(this.name);
    hash = 41 * hash + Objects.hashCode(this.deviceId);
    hash = 41 * hash + Objects.hashCode(this.contactId);
    hash = 41 * hash + Objects.hashCode(this.nodeId);
    hash = 41 * hash + Objects.hashCode(this.status);
    hash = 41 * hash + Objects.hashCode(this.previousStatus);
    hash = 41 * hash + Objects.hashCode(this.millis);
    hash = 41 * hash + Objects.hashCode(this.lastUpdated);
    hash = 41 * hash + Objects.hashCode(this.commandStationId);
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
    final SensorBean other = (SensorBean) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.deviceId, other.deviceId)) {
      return false;
    }
    if (!Objects.equals(this.contactId, other.contactId)) {
      return false;
    }
    if (!Objects.equals(this.nodeId, other.nodeId)) {
      return false;
    }
    if (!Objects.equals(this.status, other.status)) {
      return false;
    }
    if (!Objects.equals(this.previousStatus, other.previousStatus)) {
      return false;
    }
    if (!Objects.equals(this.millis, other.millis)) {
      return false;
    }
    if (!Objects.equals(this.commandStationId, other.commandStationId)) {
      return false;
    }
    return Objects.equals(this.lastUpdated, other.lastUpdated);
  }

  public boolean equalsDeviceIdAndContactId(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SensorBean other = (SensorBean) obj;
    if (!Objects.equals(this.commandStationId, other.commandStationId)) {
      return false;
    }
    if (!Objects.equals(this.deviceId, other.deviceId)) {
      return false;
    }
    return Objects.equals(this.contactId, other.contactId);
  }

  public boolean equalsId(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SensorBean other = (SensorBean) obj;
    return Objects.equals(this.id, other.id);
  }

  @Override
  public String toString() {
    //return name;
    return toLogString();
  }

  public String toLogString() {
//    String ids;
//    if (id == null) {
//      ids = "(" + generateId() + ")";
//    } else {
//      ids = id;
//    }

    return "SensorBean{"
            + "id="
            + id
            + ", name="
            + name
            + ", deviceId="
            + deviceId
            + ", contactId="
            + contactId
            + ", nodeId="
            + nodeId
            + ", status="
            + status
            + ", previousStatus="
            + previousStatus
            + ", millis="
            + millis
            + ", lastUpdated="
            + lastUpdated
            + ", commandStationId="
            + commandStationId
            + "}";
  }
}
