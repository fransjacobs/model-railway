/*
 * Copyright 2023 frans.
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
package jcs.ui.layout.tiles;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.SignalType;
import jcs.entities.enums.SignalValue;
import jcs.entities.enums.TileType;
import jcs.ui.layout.tiles.enums.Direction;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public final class TileImageCache {

  private static final Map<String, BufferedImage> tileImageCache = new HashMap<>();

  public static void put(Tile tile, BufferedImage image) {
    String key = getKey(tile);
    tileImageCache.put(key, image);
    //Logger.trace(key);
  }

  public static boolean contains(Tile tile) {
    String key = getKey(tile);
    boolean exist = tileImageCache.containsKey(key);
    //Logger.trace(key + " " + (exist ? "exist" : "unknown"));
    return exist;
  }

  public static BufferedImage get(Tile tile) {
    String key = getKey(tile);
    //Logger.trace(key);
    return tileImageCache.get(key);
  }

  public static void remove(Tile tile) {
    String key = getKey(tile);
    if (tileImageCache.containsKey(key)) {
      tileImageCache.remove(key);
      Logger.trace(key);
    }
  }

  public static String getKey(Tile tile) {
    TileType tileType = tile.getTileType();
    Orientation orientation = tile.getOrientation();
    Direction direction = tile.getDirection();
    AccessoryValue accessoryValue;
    AccessoryValue routeValue;
    if (tile instanceof Cross || tile instanceof Switch) {
      accessoryValue = ((Switch) tile).getAccessoryValue();
      routeValue = ((Switch) tile).getRouteValue();
    } else {
      accessoryValue = AccessoryValue.OFF;
      routeValue = AccessoryValue.OFF;
    }
    SignalType signalType;
    SignalValue signalValue;
    if (tile instanceof Signal) {
      signalType = ((Signal) tile).getSignalType();
      signalValue = ((Signal) tile).getSignalValue();
    } else {
      signalType = SignalType.NONE;
      signalValue = SignalValue.OFF;
    }
    boolean active;
    if (tile instanceof Sensor) {
      active = ((Sensor) tile).isActive();
    } else {
      active = false;
    }
    String id = tile.getId();
    String tc = tile.getTrackColor().toString();
    String bc = tile.getBackgroundColor().toString();
    String ol = tile.isDrawOutline() ? "y" : "n";

    StringBuilder sb = new StringBuilder();
    sb.append(tileType.getTileType());
    sb.append("~");
    sb.append(orientation.getOrientation());
    sb.append("~");
    sb.append(direction.getDirection());
    sb.append("~");
    sb.append(accessoryValue.getValue());
    sb.append("~");
    sb.append(routeValue.getValue());
    sb.append("~");
    if (signalType == null) {
      signalType = SignalType.NONE;
    }
    sb.append(signalType.getSignalType());
    sb.append("~");
    if (signalValue == null) {
      signalValue = SignalValue.OFF;
    }
    sb.append(signalValue.getSignalValue());
    sb.append("~");
    sb.append(active);
    sb.append("~");
    sb.append(tc);
    sb.append("~");
    sb.append(bc);
    sb.append("~");
    sb.append(ol);
    if (TileType.BLOCK.equals(tileType)) {
      sb.append("~");
      sb.append(id);
    }
    return sb.toString();
  }

}
