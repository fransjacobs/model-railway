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

public abstract class ControllableDevice implements JCSEntity, Serializable, Comparable {

    protected Integer address;
    protected String name;
    protected String description;
    protected String catalogNumber;
    protected BigDecimal id;

    protected ControllableDevice(BigDecimal id) {
        this(null, null, null, null, id);
    }

    protected ControllableDevice(BigDecimal id, String name) {
        this(null, name, null, null, id);
    }

    protected ControllableDevice(BigDecimal id, Integer address, String name) {
        this(address, name, null, null, id);
    }

    protected ControllableDevice(BigDecimal id, Integer address, String name, String description) {
        this(address, name, description, null, id);
    }

    protected ControllableDevice(Integer address, String name, String description) {
        this(address, name, description, null);
    }

    protected ControllableDevice(Integer address, String catalogNumber) {
        this(address, null, null, catalogNumber);
    }

    protected ControllableDevice(Integer address, String name, String description, String catalogNumber) {
        this(address, name, description, catalogNumber, null);
    }

    protected ControllableDevice(Integer address, String name, String description, String catalogNumber, BigDecimal id) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.catalogNumber = catalogNumber;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public Integer getAddress() {
        return this.address;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public BigDecimal getId() {
        return id;
    }

    public String getKey() {
        if (this.id == null) {
            return null;
        }
        return this.id.toString();
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

    @Override
    public int compareTo(Object other) {
        return Integer.compare(this.address, ((ControllableDevice) other).address);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.id);
        hash = 79 * hash + Objects.hashCode(this.address);
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.description);
        hash = 79 * hash + Objects.hashCode(this.catalogNumber);
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
        final ControllableDevice other = (ControllableDevice) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.catalogNumber, other.catalogNumber)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.address, other.address);
    }

    //public abstract ControllableDevice copy();
    public abstract String toLogString();

}
