/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.ui.layout.tiles.ui;

import jcs.ui.layout.tiles.Tile;

public class LayoutScale {

  private static final LayoutScale INSTANCE = new LayoutScale();

  public static LayoutScale getInstance() {
    return INSTANCE;
  }

  // Valid steps: 100, 90, 80, 70, 60, 50 — all produce whole-pixel tile sizes
  public static final int[] SCALE_STEPS = {100, 90, 80, 70, 60, 50};
  private static final int DEFAULT_SCALE = 100;

  private int scalePercent = DEFAULT_SCALE;

  public int getScalePercent() {
    return scalePercent;
  }

  public void setScalePercent(int percent) {
    this.scalePercent = percent;
  }

  /**
   * Pixel size of one tile at current scale. At 100% = 40.
   */
  public int scaledTileSize() {
    return Tile.DEFAULT_WIDTH * scalePercent / 100;
  }

  /**
   * Half a tile (= grid unit). At 100% = 20.
   */
  public int scaledGrid() {
    return scaledTileSize() / 2;
  }

  /**
   * Convert a canonical (100% scale) coordinate to display pixels.
   */
  public int toDisplay(int canonical) {
    return canonical * scalePercent / 100;
  }

  /**
   * Convert display pixels back to canonical (100% scale) coordinate.
   */
  public int toCanonical(int display) {
    return display * 100 / scalePercent;
  }
}
