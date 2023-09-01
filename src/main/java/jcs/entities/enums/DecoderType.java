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
 * Decoder Types of a locomotive
 */
public enum DecoderType {
  MM_DIL("mm2_dil8"), MFX("mfx"), DCC("dcc"), SX1("sx1"), MM_PRG("mm_prg"), MM2_PRG("mm2_prg");

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
    if (decoderType == null) {
      return null;
    }
    return ENUM_MAP.get(decoderType);

  }

}
