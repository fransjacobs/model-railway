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
package jcs.ui.layout.tiles.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Rotation {
  R0("R0"), R90("R90"), R180("R180"), R270("R270");

  private final String rotation;
  private static final Map<String, Rotation> ENUM_MAP;

  Rotation(String rotation) {
    this.rotation = rotation;
  }

  static {
    Map<String, Rotation> map = new ConcurrentHashMap<>();
    for (Rotation instance : Rotation.values()) {
      map.put(instance.getRotation(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public String getRotation() {
    return this.rotation;
  }

  public static Rotation get(String rotation) {
    return ENUM_MAP.get(rotation);
  }
}
