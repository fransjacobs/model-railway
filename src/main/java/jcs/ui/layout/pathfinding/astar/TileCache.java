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
package jcs.ui.layout.pathfinding.astar;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.ui.layout.tiles.Tile;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TileCache {

  private final Map<Point, Tile> tilePointMap;
  private final Map<String, Tile> tileIdMap;

  public TileCache() {
    this(null);
  }

  public TileCache(List<Tile> tiles) {
    this.tilePointMap = new HashMap<>();
    this.tileIdMap = new HashMap<>();
    reload(tiles);
  }

  public final synchronized void reload(List<Tile> tiles) {
    this.tilePointMap.clear();
    this.tileIdMap.clear();

    if (tiles != null && !tiles.isEmpty()) {
      for (Tile tile : tiles) {
        if (tile.isBlock()) {
          //A block is s rectangle which fils the space of 3 tiles hence there are 3 "center" points.
          for (Point p : tile.getAltPoints()) {
            this.tilePointMap.put(p, tile);
          }
        } else {
          //A tile is a square, center is the point too look for
          this.tilePointMap.put(tile.getCenter(), tile);
        }

        this.tileIdMap.put(tile.getId(), tile);
      }
      Logger.trace("Loaded " + tiles.size() + " tiles");
    }
  }

  public Tile getTile(String id) {
    return this.tileIdMap.get(id);
  }

  public boolean contains(String id) {
    return this.tileIdMap.containsKey(id);
  }

  public Tile getTile(Point p) {
    return this.tilePointMap.get(p);
  }

  public String getTileId(Point p) {
    if (this.tilePointMap.containsKey(p)) {
      return this.tilePointMap.get(p).getId();
    } else {
      return null;
    }
  }

  public boolean contains(Point p) {
    return this.tilePointMap.containsKey(p);
  }

}
