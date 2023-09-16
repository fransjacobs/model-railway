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

public enum Orientation {
  NORTH("North"), SOUTH("South"), EAST("East"), WEST("West");

  private final String orientation;
  private static final Map<String, Orientation> ENUM_MAP;

  Orientation(String orientation) {
    this.orientation = orientation;
  }

  static {
    Map<String, Orientation> map = new ConcurrentHashMap<>();
    for (Orientation instance : Orientation.values()) {
      map.put(instance.getOrientation(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public String getOrientation() {
    return this.orientation;
  }

  public static Orientation get(String direction) {
    return ENUM_MAP.get(direction);
  }
}
