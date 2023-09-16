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

public enum SignalType {
  HP01("HP01"), HP012SH1("HP012SH1"), HP0SH1("HP0SH1"), HP012("HP012"), NONE("NONE");

  private final String signalType;
  private static final Map<String, SignalType> ENUM_MAP;

  SignalType(String sgnalType) {
    this.signalType = sgnalType;
  }

  static {
    Map<String, SignalType> map = new ConcurrentHashMap<>();
    for (SignalType instance : SignalType.values()) {
      map.put(instance.getSignalType(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public String getSignalType() {
    return this.signalType;
  }

  public static SignalType get(String signalType) {
    if (signalType != null) {
      return ENUM_MAP.get(signalType);
    } else {
      return null;
    }
  }

  public static SignalType getSignalType(String marklinType) {
    return ENUM_MAP.get(translateSignalString(marklinType));
  }

  private static String translateSignalString(String marklinType) {
    if (marklinType != null) {
      return switch (marklinType) {
        case "lichtsignal_HP01" ->
          "HP01";
        case "lichtsignal_HP02" ->
          "HP012";
        case "lichtsignal_HP012" ->
          "HP012";
        case "lichtsignal_HP012_SH01" ->
          "HP012SH1";
        case "lichtsignal_SH01" ->
          "HP0SH1";
        case "formsignal_HP01" ->
          "HP01";
        case "formsignal_HP02" ->
          "HP012";
        case "formsignal_HP012" ->
          "HP012";
        case "formsignal_HP012_SH01" ->
          "HP012SH1";
        case "formsignal_SH01" ->
          "HP0SH1";
        case "urc_lichtsignal_HP01" ->
          "HP01";
        case "urc_lichtsignal_HP012" ->
          "HP012";
        case "urc_lichtsignal_HP012_SH01" ->
          "HP012SH1";
        case "urc_lichtsignal_SH01" ->
          "HP0SH1";
        default ->
          "NONE";
      };
    } else {
      return "NONE";
    }
  }

}
