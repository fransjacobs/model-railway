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
package lan.wervel.jcs.entities.enums;

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

}
