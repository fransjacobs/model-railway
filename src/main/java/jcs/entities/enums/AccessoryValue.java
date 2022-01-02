/*
 * Copyright (C) 2019 Frans Jacobs.
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
package jcs.entities.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author frans
 */
public enum AccessoryValue {
  RED("RED"), GREEN("GREEN"), OFF("OFF");

  private final String value;
  private static final Map<String, AccessoryValue> ENUM_MAP;

  AccessoryValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

  public String getDBValue() {
    return translate2DBValue(this.value);
  }

  static {
    Map<String, AccessoryValue> map = new ConcurrentHashMap<>();
    for (AccessoryValue instance : AccessoryValue.values()) {
      map.put(instance.getValue(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public static AccessoryValue get(String value) {
    if (value == null) {
      return null;
    }
    return ENUM_MAP.get(value);
  }

  public static AccessoryValue dbGet(String dbValue) {
    if (dbValue == null) {
      return null;
    }
    return ENUM_MAP.get(translateDBValue(dbValue));
  }

  private static String translateDBValue(String dbValue) {
    if (dbValue == null) {
      return null;
    }
    switch (dbValue) {
      case "R":
        return "RED";
      case "G":
        return "GREEN";
      default:
        return "OFF";
    }
  }

  private static String translate2DBValue(String value) {
    switch (value) {
      case "RED":
        return "R";
      case "GREEN":
        return "G";
      default:
        return "O";
    }
  }

}
