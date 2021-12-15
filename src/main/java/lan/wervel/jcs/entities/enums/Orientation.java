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

public enum Orientation {
    NORTH("North"), SOUTH("South"), EAST("East"), WEST("West");

    private final String orientation;
    private static final Map<String, Orientation> ENUM_MAP;

    Orientation(String orientation) {
        this.orientation = orientation;
    }

    static {
        Map<String, Orientation> map = new ConcurrentHashMap<>();
        for (Orientation instance : Orientation.values()) {
            map.put(instance.getOrientation(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public String getOrientation() {
        return this.orientation;
    }

    public static Orientation get(String direction) {
        return ENUM_MAP.get(direction);
    }
}