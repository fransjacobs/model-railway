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
}
