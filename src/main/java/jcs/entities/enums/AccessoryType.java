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
    return switch (dbType) {
      case "S" ->
        "Signal";
      case "T" ->
        "Turnout";
      default ->
        "General";
    };
  }

  private static String translate2DBValue(String type) {
    return switch (type) {
      case "Signal" ->
        "S";
      case "Turnout" ->
        "T";
      default ->
        "g";
    };
  }

}
