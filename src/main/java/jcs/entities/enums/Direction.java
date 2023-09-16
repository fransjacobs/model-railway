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

  private static int translate2MarklinValue(String value) {
    return switch (value) {
      case "Forwards" ->
        1;
      case "Backwards" ->
        2;
      case "Switch" ->
        2;
      default ->
        0;
    };
  }

  public int getMarklinValue() {
    return translate2MarklinValue(this.direction);
  }

  private static String translate2DirectionString(int value) {
    return switch (value) {
      case 1 ->
        "Forwards";
      case 2 ->
        "Backwards";
      case 3 ->
        "Switch";
      default ->
        "Same";
    };
  }

  public static Direction getDirection(int marklinValue) {
    return ENUM_MAP.get(translate2DirectionString(marklinValue));
  }

  public Direction toggle() {
    return switch (this.direction) {
      case "Forwards" ->
        BACKWARDS;
      case "Backwards" ->
        FORWARDS;
      default ->
        SAME;
    };
  }

}
