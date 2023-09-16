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
public enum SignalValue {
  Hp0("Hp0"), Hp1("Hp1"), Hp2("Hp2"), Hp0Sh1("Hp0Sh1"), OFF("OFF");

  private final String signalValue;
  private static final Map<String, SignalValue> ENUM_MAP;

  SignalValue(String signalValue) {
    this.signalValue = signalValue;
  }

  public String getSignalValue() {
    return this.signalValue;
  }

  static {
    Map<String, SignalValue> map = new ConcurrentHashMap<>();
    for (SignalValue instance : SignalValue.values()) {
      map.put(instance.getSignalValue(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public static SignalValue get(String signalValue) {
    if (signalValue == null) {
      return null;
    }
    return ENUM_MAP.get(signalValue);
  }

  private static int translate2CSValue(String value) {
    return switch (value) {
      case "Hp0" ->
        0;
      case "Hp1" ->
        1;
      case "Hp0Sh1" ->
        2;
      case "Hp2" ->
        3;
      default ->
        -1;
    };
  }

  public int getCSValue() {
    return translate2CSValue(this.signalValue);
  }

  private static String translateCSValue(int value) {
    return switch (value) {
      case 0 ->
        "Hp0";
      case 1 ->
        "Hp1";
      case 2 ->
        "Hp0Sh1";
      case 3 ->
        "Hp2";
      default ->
        "Off";
    };
  }

  public static SignalValue csGet(int csValue) {
    return ENUM_MAP.get(translateCSValue(csValue));
  }

}
