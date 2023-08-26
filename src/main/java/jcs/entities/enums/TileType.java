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

public enum TileType {
  STRAIGHT("Straight"), STRAIGHT_DIR("StraightDirection"), CURVED("Curved"), SWITCH("Switch"), CROSS("Cross"), SIGNAL("Signal"), SENSOR("Sensor"), BLOCK("Block"), END("End");

  private final String tileType;

  private static final Map<String, TileType> ENUM_MAP;

  TileType(String tileType) {
    this.tileType = tileType;
  }

  public String getTileType() {
    return this.tileType;
  }

  static {
    Map<String, TileType> map = new ConcurrentHashMap<>();
    for (TileType instance : TileType.values()) {
      map.put(instance.getTileType(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public static TileType get(String tileType) {
    if (tileType == null) {
      return null;
    }
    return ENUM_MAP.get(tileType);
  }

}
