/*
 * Copyright (C) 2022 fransjacobs.
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
package jcs.controller.cs3.devices;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author fransjacobs
 */
public class LinkSxx {

    private String uid;
    private String name;
    private String typeName;
    private String identifier;
    private Integer type;
    private String articleNumber;
    private String serialNumber;
    private Integer queryInterval;
    private String version;
    private final Map<Integer, SxxBus> sxxBusses;
    private boolean present;

    public static final String BUS1 = "Länge Bus 1 (RJ45-1)";
    public static final String BUS2 = "Länge Bus 2 (RJ45-2)";
    public static final String BUS3 = "Länge Bus 3 (6-Polig)";

    public LinkSxx() {
        sxxBusses = new HashMap<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Integer getDeviceId() {
        if (this.identifier != null) {
            return Integer.parseInt(this.identifier.substring(2), 16);
        } else {
            return 0;
        }
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getQueryInterval() {
        return queryInterval;
    }

    public void setQueryInterval(Integer queryInterval) {
        this.queryInterval = queryInterval;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void addSxxBus(SxxBus bus) {
        if (bus == null) {
            return;
        }
        switch (bus.getName()) {
            case BUS1:
                this.sxxBusses.put(1, bus);
                break;
            case BUS2:
                this.sxxBusses.put(2, bus);
                break;
            case BUS3:
                this.sxxBusses.put(3, bus);
                break;
            default:
                //do nothing;
                break;
        }
    }

    public Map<Integer, SxxBus> getSxxBusses() {
        return this.sxxBusses;
    }

    public int getBusLength(Integer busNr) {
        if (busNr == 0) {
            //the LinkSxx self is 1
            return 1;
        } else if (this.sxxBusses.containsKey(busNr)) {
            SxxBus bus = sxxBusses.get(busNr);
            if (bus.getLength() == null) {
                return 0;
            } else {
                return bus.getLength();
            }
        } else {
            return 0;
        }
    }

    public Integer getContactIdOffset(Integer busNr) {
        if (busNr == 0) {
            //the LinkSxx self is 0
            return 0;
        } else if (this.sxxBusses.containsKey(busNr)) {
            SxxBus bus = sxxBusses.get(busNr);
            if (bus.getLength() == null) {
                return 0;
            } else {
                return bus.getContactIdOffset();
            }
        } else {
            return 0;
        }
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.uid);
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.typeName);
        hash = 29 * hash + Objects.hashCode(this.identifier);
        hash = 29 * hash + Objects.hashCode(this.type);
        hash = 29 * hash + Objects.hashCode(this.articleNumber);
        hash = 29 * hash + Objects.hashCode(this.serialNumber);
        hash = 29 * hash + Objects.hashCode(this.version);
        hash = 29 * hash + (this.present ? 1 : 0);
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
        final LinkSxx other = (LinkSxx) obj;
        if (this.present != other.present) {
            return false;
        }
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.typeName, other.typeName)) {
            return false;
        }
        if (!Objects.equals(this.identifier, other.identifier)) {
            return false;
        }
        if (!Objects.equals(this.articleNumber, other.articleNumber)) {
            return false;
        }
        if (!Objects.equals(this.serialNumber, other.serialNumber)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return Objects.equals(this.queryInterval, other.queryInterval);
    }

    @Override
    public String toString() {
        return "LinkSxx{" + "uid=" + uid + ", name=" + name + ", identifier=" + identifier + '}';
    }

}
