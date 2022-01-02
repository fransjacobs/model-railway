/*
 * Copyright (C) 2019 fransjacobs.
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
 * @author fransjacobs
 */
public enum Direction {
  SAME("Same"), FORWARDS("Forwards"), BACKWARDS("Backwards"), SWITCH("Switch");

  private final String direction;

  private static final Map<String, Direction> ENUM_MAP;

  Direction(String direction) {
    this.direction = direction;
  }

  public String getDirection() {
    return this.direction;
  }

  static {
    Map<String, Direction> map = new ConcurrentHashMap<>();
    for (Direction instance : Direction.values()) {
      map.put(instance.getDirection(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public static Direction get(String direction) {
    return ENUM_MAP.get(direction);
  }

  private static int translate2CS2Value(String value) {
    switch (value) {
      case "Forwards":
        return 1;
      case "Backwards":
        return 2;
      case "Switch":
        return 2;
      default:
        return 0;
    }
  }

  public int getCS2Value() {
    return translate2CS2Value(this.direction);
  }

  private static String translateCS2Value(int value) {
    switch (value) {
      case 1:
        return "Forwards";
      case 2:
        return "Backwards";
      case 3:
        return "Switch";
      default:
        return "Same";
    }
  }

  public static Direction cs2Get(int cs2Value) {
    return ENUM_MAP.get(translateCS2Value(cs2Value));
  }

  public Direction toggle() {
    switch (this.direction) {
      case "Forwards":
        return BACKWARDS;
      case "Backwards":
        return FORWARDS;
      default:
        return SAME;
    }
  }

}
