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
public enum DrivewayType {
  MANUAL("Manual"), FEEDBACK_PORT("FeedbackPort"), TIMED("Timed");

  private final String type;
  private static final Map<String, DrivewayType> ENUM_MAP;

  DrivewayType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

  static {
    Map<String, DrivewayType> map = new ConcurrentHashMap<>();
    for (DrivewayType instance : DrivewayType.values()) {
      map.put(instance.getType(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public static DrivewayType get(String type) {
    return ENUM_MAP.get(type);
  }

}
