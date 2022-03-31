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

    private static int translate2CS3Value(String value) {
        switch (value) {
            case "Hp0":
                return 0;
            case "Hp1":
                return 1;
            case "Hp0Sh1":
                return 2;
            case "Hp2":
                return 3;
            default:
                return -1;
        }
    }

    public int getCS3Value() {
        return translate2CS3Value(this.signalValue);
    }

    private static String translateCS3Value(int value) {
        switch (value) {
            case 0:
                return "Hp0";
            case 1:
                return "Hp1";
            case 2:
                return "Hp0Sh1";
            case 3:
                return "Hp2";
            default:
                return "Off";
        }
    }

    public static SignalValue cs3Get(int cs2Value) {
        return ENUM_MAP.get(translateCS3Value(cs2Value));
    }

}
