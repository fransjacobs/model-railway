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
package jcs.entities.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decoder Types of a locomotive
 */
public enum DecoderType {
  MM("mm_prg"), MM_DIL("mm2_dil8"), MFX("mfx"), DCC("dcc"), SX1("sx1");

  private final String decoderType;

  private static final Map<String, DecoderType> ENUM_MAP;

  DecoderType(String decoderType) {
    this.decoderType = decoderType;
  }

  public String getDecoderType() {
    return this.decoderType;
  }

  static {
    Map<String, DecoderType> map = new ConcurrentHashMap<>();
    for (DecoderType instance : DecoderType.values()) {
      map.put(instance.getDecoderType(), instance);
    }
    ENUM_MAP = Collections.unmodifiableMap(map);
  }

  public static DecoderType get(String decoderType) {
    return ENUM_MAP.get(decoderType);
  }

}
