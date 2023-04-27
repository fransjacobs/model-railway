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
package jcs.ui.layout.tiles.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Direction {
    RIGHT("Right"), LEFT("Left"), CENTER("Center");

    private final String direction;
    private static final Map<String, Direction> ENUM_MAP;

    Direction(String direction) {
        this.direction = direction;
    }

    static {
        Map<String, Direction> map = new ConcurrentHashMap<>();
        for (Direction instance : Direction.values()) {
            map.put(instance.getDirection(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public String getDirection() {
        return this.direction;
    }

    public static Direction get(String direction) {
        return ENUM_MAP.get(direction);
    }
}
