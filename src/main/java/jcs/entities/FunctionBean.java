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

public class FunctionBean implements JCSEntity, Serializable {

    private BigDecimal id;
    private BigDecimal locomotiveId;
    private Integer number;
    private Integer functionType;
    private Integer value;

    public FunctionBean() {
    }

    public FunctionBean(Integer number) {
        this(null, number, null, null);
    }

    public FunctionBean(Integer number, BigDecimal locomotiveId) {
        this(locomotiveId, number, null, null);
    }

    public FunctionBean(BigDecimal locomotiveId, Integer number, Integer functionType, Integer value) {
        this.locomotiveId = locomotiveId;
        this.number = number;
        this.functionType = functionType;
        this.value = value;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setNumber(String number) {
        this.number = Integer.parseInt(number);
    }

    public Integer getFunctionType() {
        return functionType;
    }

    public void setFunctionType(Integer functionType) {
        this.functionType = functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = Integer.parseInt(functionType);
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = Integer.parseInt(value);
    }

    public BigDecimal getLocomotiveId() {
        return locomotiveId;
    }

    public void setLocomotiveId(BigDecimal locomotiveId) {
        this.locomotiveId = locomotiveId;
    }

    @Override
    public BigDecimal getId() {
        return this.id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.locomotiveId);
        hash = 71 * hash + Objects.hashCode(this.number);
        hash = 71 * hash + Objects.hashCode(this.functionType);
        hash = 71 * hash + Objects.hashCode(this.value);
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
        final FunctionBean other = (FunctionBean) obj;
        if (!Objects.equals(this.locomotiveId, other.locomotiveId)) {
            return false;
        }
        if (!Objects.equals(this.number, other.number)) {
            return false;
        }
        if (!Objects.equals(this.functionType, other.functionType)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return "locoId:" + locomotiveId + ", number:" + number + ";type: " + functionType + ", value: " + value;
    }

    @Override
    public String toLogString() {
        return toString();
    }

}
