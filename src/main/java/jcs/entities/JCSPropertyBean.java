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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.util.Objects;

@Table(name = "jcs_properties")
public class JCSPropertyBean {

    private String key;
    private String value;

    public JCSPropertyBean() {
        this(null, null);
    }

    public JCSPropertyBean(String key, String value) {
        this(null, key, value);
    }

    public JCSPropertyBean(BigDecimal id, String key, String value) {
        this.key = key;
        this.value = value;
    }

//CREATE TABLE JCS_PROPERTIES (
//  P_KEY    VARCHAR(255) NOT NULL,
//  P_VALUE  VARCHAR(255) NOT NULL,
//  CONSTRAINT PROP_PK PRIMARY KEY ( P_KEY )
//);
//
//CREATE UNIQUE INDEX PROP_PK_IDX ON JCS_PROPERTIES (P_KEY);
    @Id
    @Column(name = "p_key", length = 255, nullable = false)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "p_value", length = 255, nullable = false)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.key);
        sb.append("=");
        sb.append(this.value);
        return sb.toString();
    }

    public String toLogString() {
        return toString();
    }

    @Transient
    public boolean getBooleanValue() {
        return "true".equalsIgnoreCase(this.value) | "y".equalsIgnoreCase(this.value) | "1".equals(this.value);
    }

    @Transient
    public int getIntValue() {
        return Integer.getInteger(this.value);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.key);
        hash = 23 * hash + Objects.hashCode(this.value);
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
        final JCSPropertyBean other = (JCSPropertyBean) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

}
