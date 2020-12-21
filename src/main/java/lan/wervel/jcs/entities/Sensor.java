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
package lan.wervel.jcs.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class Sensor extends ControllableDevice {

    private Integer value;
    private Integer previousValue;
    private Integer deviceId;
    private Integer millis;
    private Date lastUpdated;

    public Sensor() {
        this(null, null, null, null, null, null, null, null, null);
    }

    public Sensor(Integer contactId, Integer value, Integer previousValue, Integer deviceId, Integer millis, Date lastUpdated) {
        this(null, contactId, null, null, value, previousValue, deviceId, millis, lastUpdated);
    }

    public Sensor(Integer contactId, String name, String description, Integer value, Integer previousValue, Integer deviceId, Integer millis) {
        this(null, contactId, name, description, value, previousValue, deviceId, millis, null);
    }

    public Sensor(Integer contactId, String name, String description, Integer value, Integer previousValue, Integer deviceId, Integer millis, Date lastUpdated) {
        this(null, contactId, name, description, value, previousValue, deviceId, millis, lastUpdated);
    }

    public Sensor(BigDecimal id, Integer contactId, String name, String description, Integer value, Integer previousValue, Integer deviceId, Integer millis, Date lastUpdated) {
        super(id, contactId, name, description);
        this.value = value;
        this.previousValue = previousValue;
        this.deviceId = deviceId;
        this.millis = millis;
        this.lastUpdated = lastUpdated;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isActive() {
        return this.value > 0;
    }

    public void setActive(boolean active) {
        this.value = active ? 1 : 0;
    }

    public int getPreviousValue() {
        return previousValue;
    }

    public void setPreviousActive(boolean active) {
        this.previousValue = active ? 1 : 0;
    }

    public void setPreviousValue(int previousValue) {
        this.previousValue = previousValue;
    }

    public int getContactId() {
        return address;
    }

    public void setContactId(int contactId) {
        this.address = deviceId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getMillis() {
        return millis;
    }

    public void setMillis(int millis) {
        this.millis = millis;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public static Integer calculateModuleNumber(int contactId) {
        int module = (contactId - 1) / 16 + 1;
        return module;
    }

    public static int calculatePortNumber(int contactId) {
        int module = (contactId - 1) / 16 + 1;
        int mport = contactId - (module - 1) * 16;
        return mport;
    }

    public static int calculateContactId(int module, int port) {
        //Bei einer CS2 errechnet sich der richtige Kontakt mit der Formel M - 1 * 16 + N
        module = module - 1;
        int contactId = module * 16;
        return contactId + port;
    }

    @Override
    public String toString() {
        if (address == null) {
            return "-";
        } else {
            return address + ", M: " + calculateModuleNumber(address) + " P: " + calculatePortNumber(address);
        }
    }

    @Override
    public String toLogString() {
        return "ContactId: " + address + ", deviceId: " + deviceId + ", value=" + value + ", previousValue=" + previousValue + ", millis=" + millis;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.value;
        hash = 41 * hash + this.previousValue;
        hash = 41 * hash + this.deviceId;
        hash = 41 * hash + this.millis;
        hash = 41 * hash + Objects.hashCode(this.lastUpdated);
        hash = 41 * hash + Objects.hashCode(super.hashCode());
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
        final Sensor other = (Sensor) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.previousValue, other.previousValue)) {
            return false;
        }
        if (!Objects.equals(this.deviceId, other.deviceId)) {
            return false;
        }
        if (!Objects.equals(this.millis, other.millis)) {
            return false;
        }
        if (!Objects.equals(this.lastUpdated, other.lastUpdated)) {
            return false;
        }
        return super.equals(other);
    }

}
