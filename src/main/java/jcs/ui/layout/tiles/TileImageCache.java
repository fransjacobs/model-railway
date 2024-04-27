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
import org.tinylog.Logger;

/**
 * @author frans
 */
public final class TileImageCache {

  private static final Map<String, BufferedImage> tileImageCache = new HashMap<>();

  public static void put(Tile tile, BufferedImage image) {
    String key = tile.getImageKey();
    put(key, image);
  }

  public static void put(String key, BufferedImage image) {
    //Logger.trace("Adding: " + key);
    tileImageCache.put(key, image);
  }

  public static boolean contains(Tile tile) {
    String key = tile.getImageKey();
    return contains(key);
  }

  public static boolean contains(String key) {
    boolean exist = tileImageCache.containsKey(key);
    if (!exist) {
      Logger.trace("Key: " + key + (exist ? " exists" : " does not exist"));
    }
    return exist;
  }

  public static BufferedImage get(Tile tile) {
    String key = tile.getImageKey();
    return get(key);
  }

  public static BufferedImage get(String key) {
    //Logger.trace(key);
    return tileImageCache.get(key);
  }

  public static void remove(Tile tile) {
    String key = tile.getImageKey();
    if (tileImageCache.containsKey(key)) {
      tileImageCache.remove(key);
      Logger.trace(key);
    }
  }
}
