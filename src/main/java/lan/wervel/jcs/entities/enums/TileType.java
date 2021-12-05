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

public enum TileType {
    STRAIGHT("Straight"), CURVED("Curved"), SWITCH("Switch"), CROSS("Cross"), SIGNAL("Signal"), SENSOR("Sensor"), BLOCK("Block");

    private final String tileType;

    private static final Map<String, TileType> ENUM_MAP;

    TileType(String tileType) {
        this.tileType = tileType;
    }

    public String getTileType() {
        return this.tileType;
    }

    static {
        Map<String, TileType> map = new ConcurrentHashMap<>();
        for (TileType instance : TileType.values()) {
            map.put(instance.getTileType(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static TileType get(String tileType) {
        if (tileType == null) {
            return null;
        }
        return ENUM_MAP.get(tileType);
    }

}
