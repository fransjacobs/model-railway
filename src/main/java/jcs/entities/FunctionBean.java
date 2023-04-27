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

@Table(name = "locomotive_functions", indexes = {
    @Index(name = "lofu_loco_func_idx", columnList = "locomotive_id, number", unique = true)})
public class FunctionBean implements Serializable {

    private Long id;
    private Long locomotiveId;
    private Integer number;
    private Integer functionType;
    private Integer value;

    public FunctionBean() {
    }

    public FunctionBean(Integer number) {
        this(null, number, null, null);
    }

    public FunctionBean(Integer number, Long locomotiveId) {
        this(locomotiveId, number, null, null);
    }

    public FunctionBean(Long locomotiveId, Integer number, Integer functionType, Integer value) {
        this(null, locomotiveId, number, functionType, value);
    }

    public FunctionBean(Long id, Long locomotiveId, Integer number, Integer functionType, Integer value) {
        this.locomotiveId = locomotiveId;
        this.number = number;
        this.functionType = functionType;
        this.value = value;
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

    @Column(name = "f_number", nullable = false)
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setNumber(String number) {
        this.number = Integer.valueOf(number);
    }

    @Column(name = "f_type", nullable = false)
    public Integer getFunctionType() {
        return functionType;
    }

    public void setFunctionType(Integer functionType) {
        this.functionType = functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = Integer.valueOf(functionType);
    }

    @Column(name = "f_value", nullable = false)
    public Integer getValue() {
        return value;
    }

    @Transient
    public boolean isOn() {
        return this.value >= 1;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = Integer.valueOf(value);
    }

    @Column(name = "locomotive_id", nullable = false)
    public Long getLocomotiveId() {
        return locomotiveId;
    }

    public void setLocomotiveId(Long locomotiveId) {
        this.locomotiveId = locomotiveId;
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

    public String toLogString() {
        return toString();
    }

}
