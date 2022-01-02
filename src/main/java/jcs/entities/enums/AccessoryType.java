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
public enum AccessoryType {
  SIGNAL("Signal"), TURNOUT("Turnout"), GENERAL("General");

  private final String type;
  private static final Map<String, AccessoryType> ENUM_MAP;

  AccessoryType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

  public String getDBType() {
    return translate2DBValue(this.type);
  }

  static {
    Map<String, AccessoryType> map = new ConcurrentHashMap<>();
    for (AccessoryType instance : AccessoryType.values()) {
      map.put(instance.getType(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public static AccessoryType get(String type) {
    return ENUM_MAP.get(type);
  }

  public static AccessoryType dbGet(String dbType) {
    if (dbType == null) {
      return null;
    }
    return ENUM_MAP.get(translateDBValue(dbType));
  }

  private static String translateDBValue(String dbType) {
    if (dbType == null) {
      return null;
    }
    switch (dbType) {
      case "S":
        return "Signal";
      case "T":
        return "Turnout";
      default:
        return "General";
    }
  }

  private static String translate2DBValue(String type) {
    switch (type) {
      case "Signal":
        return "S";
      case "Turnout":
        return "T";
      default:
        return "g";
    }
  }

}
