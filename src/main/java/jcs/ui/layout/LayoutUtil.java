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

import java.awt.Point;
import jcs.ui.layout.tiles.LayoutScale;
import static jcs.ui.layout.tiles.LayoutScale.GRID;

public class LayoutUtil {

  private LayoutUtil() {
  }

  public static Point snapToGrid(Point p) {
    return snapToGrid(p.x, p.y);
  }

  /**
   * Snap coordinates to the nearest grid point
   *
   * @param x the X
   * @param y the Y
   * @return Coordinates which are the X en Y wrapped
   */
  public static Point snapToGridOld(int x, int y) {
    LayoutScale scale = LayoutScale.getInstance();
    int grid = scale.scaledGrid();
    int scaledTileWidth = grid * 2;
    int scaledTileHeight = grid * 2;

    int steps = x / scaledTileWidth;
    int sx = steps;
    sx = sx * scaledTileWidth + grid;

    steps = y / scaledTileHeight;
    int sy = steps;
    sy = sy * scaledTileHeight + grid;

    return new Point(sx, sy);
  }

  public static Point snapToGrid(int x, int y) {
    LayoutScale scale = LayoutScale.getInstance();
    int step = scale.scaledTileSize();
    // Snap display coordinate to nearest scaled grid point
    //int displayX = Math.round((float) x / step) * step;
    //int displayY = Math.round((float) y / step) * step;
    
    int displayX = (x / step) * step;
    int displayY = (y / step) * step;
    
    // Return canonical coordinate for TileCache lookups and storage
    
    int lx = scale.toCanonical(displayX) + GRID;
    int ly =scale.toCanonical(displayY) + GRID;
    
    return new Point(lx,ly);
  }

  public static int getGridX(int x) {
    LayoutScale scale = LayoutScale.getInstance();
    int grid = scale.scaledGrid();
    int scaledTileWidth = grid * 2;

    int steps = x / scaledTileWidth;
    int sx = steps * scaledTileWidth + grid;
    return (sx - grid) / (grid * 2);
  }

  public static int getGridY(int y) {
    LayoutScale scale = LayoutScale.getInstance();
    int grid = scale.scaledGrid();
    int scaledTileHeight = grid * 2;

    int steps = y / scaledTileHeight;
    int sy = steps * scaledTileHeight + grid;
    return (sy - grid) / (grid * 2);
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

}
