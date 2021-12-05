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
import java.util.Objects;

public class JCSProperty extends ControllableDevice {

  private String key;
  private String value;

  public JCSProperty() {
    this(null, null);
  }

  public JCSProperty(String key, String value) {
    this(null, key, value);
  }

  public JCSProperty(BigDecimal id, String key, String value) {
    super(id, key);
    this.key = key;
    this.value = value;
  }

  @Override
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

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
    sb.append(",");
    sb.append(this.value);
    return sb.toString();
  }

  @Override
  public String toLogString() {
    return toString();
  }

  public boolean getBooleanValue() {
    return "true".equalsIgnoreCase(this.value) | "y".equalsIgnoreCase(this.value) | "1".equals(this.value);
  }

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
    final JCSProperty other = (JCSProperty) obj;
    if (!Objects.equals(this.key, other.key)) {
      return false;
    }
    return Objects.equals(this.value, other.value);
  }

}
