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
import java.util.Date;
import java.util.Objects;

public class SensorBean implements JCSEntity, Serializable {

    private BigDecimal id;
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

    public SensorBean(Integer deviceId, Integer contactId, Integer status, Integer previousStatus, Integer millis, Date lastUpdated) {
        this(null, null, deviceId, contactId, status, previousStatus, millis, lastUpdated);
    }

    public SensorBean(String name, Integer deviceId, Integer contactId, Integer status, Integer previousStatus, Integer millis, Date lastUpdated) {
        this(null, name, deviceId, contactId, status, previousStatus, millis, lastUpdated);
    }

    public SensorBean(BigDecimal id, String name, Integer deviceId, Integer contactId, Integer status, Integer previousStatus, Integer millis, Date lastUpdated) {
        this.id = id;
        this.name = name;

        this.status = status;
        this.previousStatus = previousStatus;
        this.deviceId = deviceId;
        this.contactId = contactId;
        this.millis = millis;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public BigDecimal getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        if (id instanceof BigDecimal) {
            this.id = (BigDecimal) id;
        }
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getContactId() {
        return contactId;
    }

    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Integer previousStatus) {
        this.previousStatus = previousStatus;
    }

    public Integer getMillis() {
        return millis;
    }

    public void setMillis(Integer millis) {
        this.millis = millis;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isActive() {
        return this.status > 0;
    }

    public void setActive(boolean active) {
        this.status = active ? 1 : 0;
    }

    public void setPreviousActive(boolean active) {
        this.previousStatus = active ? 1 : 0;
    }

    public boolean isPreviousActive() {
        return this.previousStatus > 0;
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

//    @Override
//    public String toString() {
//        if (contactId == null) {
//            return "-";
//        } else {
//            return contactId + ", M: " + calculateModuleNumber(contactId) + " P: " + calculatePortNumber(contactId);
//        }
//    }
    @Override
    public String toString() {
        return "SensorBean{" + "id=" + id + ", name=" + name + ", deviceId=" + deviceId + ", contactId=" + contactId + ", status=" + status + ", previousStatus=" + previousStatus + ", millis=" + millis + ", lastUpdated=" + lastUpdated + '}';
    }

    @Override
    public String toLogString() {
        return toString();
    }

}
