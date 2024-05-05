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
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "sensors", indexes = {
  @Index(name = "sens_devi_cont_idx", columnList = "device_id, contact_id", unique = true)})
public class SensorBean implements Serializable {

  private String id;
  private String name;
  private Integer deviceId;
  private Integer contactId;
  private Integer status;
  private Integer previousStatus;
  private Integer millis;
  private Date lastUpdated;

  public SensorBean() {
    this(null, null, null, null, null, null, null, null);
  }

  public SensorBean(Integer deviceId, Integer contactId, Integer status) {
    this(null, null, deviceId, contactId, status, null, null, null);
  }

  public SensorBean(Integer deviceId, Integer contactId, Integer status, Integer previousStatus, Integer millis, Date lastUpdated) {
    this(null, null, deviceId, contactId, status, previousStatus, millis, lastUpdated);
  }

  public SensorBean(String name, Integer deviceId, Integer contactId) {
    this(null, name, deviceId, contactId, null, null, null, null);
  }

  public SensorBean(String name, Integer deviceId, Integer contactId, Integer status, Integer previousStatus, Integer millis, Date lastUpdated) {
    this(null, name, deviceId, contactId, status, previousStatus, millis, lastUpdated);
  }

  public SensorBean(String id, String name, Integer deviceId, Integer contactId, Integer status, Integer previousStatus, Integer millis, Date lastUpdated) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.previousStatus = previousStatus;
    this.deviceId = deviceId;
    this.contactId = contactId;
    this.millis = millis;
    this.lastUpdated = lastUpdated;
  }

  @Id
  @Column(name = "id", nullable = false)
  public String getId() {
    if (id == null) {
      id = generateId();
    }
    return id;
  }

  private String generateId() {
    //Format the id start with the device then "-"
    //than a 4 char contact id
    String cn = contactId.toString();
    int cnl = cn.length();
    for (int x = 0; x < 4 - cnl; x++) {
      cn = "0" + cn;
    }
    return deviceId + "-" + cn;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Column(name = "name", length = 255, nullable = false)
  public String getName() {
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

  @Transient
  public void toggle() {
    if (status == null) {
      status = 0;
    }
    previousStatus = status;
    Date lastChanged = this.lastUpdated;
    if (lastChanged == null) {
      lastChanged = new Date();
    }
    if (status == 0) {
      status = 1;
    } else {
      status = 0;
    }
    lastUpdated = new Date();
    long prev = lastChanged.getTime();
    long now = lastUpdated.getTime();
    Long m = (now - prev) / 10;
    this.millis = m.intValue();
  }

  @Column(name = "millis")
  public Integer getMillis() {
    return millis;
  }

  public void setMillis(Integer millis) {
    this.millis = millis;
  }

  @Column(name = "last_updated")
  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
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
    this.status = active ? 1 : 0;
  }

  public void setPreviousActive(boolean active) {
    this.previousStatus = active ? 1 : 0;
  }

  @Transient
  public boolean isPreviousActive() {
    return this.previousStatus > 0;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 41 * hash + Objects.hashCode(this.id);
    hash = 41 * hash + Objects.hashCode(this.name);
    hash = 41 * hash + Objects.hashCode(this.deviceId);
    hash = 41 * hash + Objects.hashCode(this.contactId);
    hash = 41 * hash + Objects.hashCode(this.status);
    hash = 41 * hash + Objects.hashCode(this.previousStatus);
    hash = 41 * hash + Objects.hashCode(this.millis);
    hash = 41 * hash + Objects.hashCode(this.lastUpdated);
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
    if (!Objects.equals(this.status, other.status)) {
      return false;
    }
    if (!Objects.equals(this.previousStatus, other.previousStatus)) {
      return false;
    }
    if (!Objects.equals(this.millis, other.millis)) {
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
    if (!Objects.equals(this.deviceId, other.deviceId)) {
      return false;
    }
    return Objects.equals(this.contactId, other.contactId);
  }

  @Override
  public String toString() {
    return name;
  }

  public String toLogString() {
    return "SensorBean{"
            + "id="
            + id
            + ", name="
            + name
            + ", deviceId="
            + deviceId
            + ", contactId="
            + contactId
            + ", status="
            + status
            + ", previousStatus="
            + previousStatus
            + ", millis="
            + millis
            + ", lastUpdated="
            + lastUpdated
            + '}';
  }
}

//    public static Integer calculateModuleNumber(int contactId) {
//        int module = (contactId - 1) / 16 + 1;
//        return module;
//    }
//    public static int calculatePortNumber(int contactId) {
//        int module = (contactId - 1) / 16 + 1;
//        int mport = contactId - (module - 1) * 16;
//        return mport;
//    }
//    public static int calculateContactId(int module, int port) {
//        //Bei einer CS2 errechnet sich der richtige Kontakt mit der Formel M - 1 * 16 + N
//        module = module - 1;
//        int contactId = module * 16;
//        return contactId + port;
  //    }
