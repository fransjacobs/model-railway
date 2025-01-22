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
package jcs.ui.layout;

import jcs.ui.layout.tiles.Tile;
import java.awt.Point;
import jcs.entities.TileBean.Orientation;
import static jcs.ui.layout.tiles.Block.BLOCK_HEIGHT;
import static jcs.ui.layout.tiles.Block.BLOCK_WIDTH;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;

public class LayoutUtil {

  private LayoutUtil() {
  }

  public static final Point snapToGrid(Point p) {
    return snapToGrid(p.x, p.y);
  }

  /**
   * Snap coordinates to the nearest grid point
   *
   * @param x the X
   * @param y the Y
   * @return Coordinates which are the X en Y wrapped
   */
  public static final Point snapToGrid(int x, int y) {
    int steps = x / Tile.DEFAULT_WIDTH;
    int sx = steps;
    sx = sx * Tile.DEFAULT_WIDTH + Tile.GRID;

    steps = y / Tile.DEFAULT_HEIGHT;
    int sy = steps;
    sy = sy * Tile.DEFAULT_HEIGHT + Tile.GRID;

    return new Point(sx, sy);
  }

  public static int getGridX(int x) {
    int steps = x / Tile.DEFAULT_WIDTH;
    int sx = steps * Tile.DEFAULT_WIDTH + Tile.GRID;
    return (sx - Tile.GRID) / (Tile.GRID * 2);
  }

  public static int getGridY(int y) {
    int steps = y / Tile.DEFAULT_HEIGHT;
    int sy = steps * Tile.DEFAULT_HEIGHT + Tile.GRID;
    return (sy - Tile.GRID) / (Tile.GRID * 2);
  }

  /**
   * Returns the euclidean distance of 2 Points
   *
   * @param p1
   * @param p2
   * @return the distance between p1 and p2
   */
  public static double euclideanDistance(Point p1, Point p2) {
    double a = (p2.x - p1.x);
    double b = (p2.y - p1.y);
    double d = Math.hypot(a, b);
    return d;
  }

  public static int blockWidth(Orientation orientation) {
    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      return BLOCK_WIDTH;
    } else {
      return DEFAULT_WIDTH;
    }
  }

  public static int blockHeight(Orientation orientation) {
    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      return DEFAULT_HEIGHT;
    } else {
      return BLOCK_HEIGHT;
    }
  }

}
