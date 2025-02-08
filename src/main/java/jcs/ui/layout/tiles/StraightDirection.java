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
package jcs.ui.layout.tiles;

import java.awt.Point;
import java.util.Collection;
import javax.swing.UIManager;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.tiles.ui.StraightDirectionUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a Straight track where trans can only run in one direction on the layout
 */
public class StraightDirection extends Straight {

  public StraightDirection(TileBean tileBean) {
    super(tileBean);
    this.tileType = TileType.STRAIGHT_DIR;
  }

  public StraightDirection(Orientation orientation, int x, int y) {
    this(orientation, new Point(x, y));
  }

  public StraightDirection(Orientation orientation, Point center) {
    super(orientation, center);
    this.tileType = TileType.STRAIGHT_DIR;
  }

  @Override
  public String getUIClassID() {
    return StraightDirectionUI.UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    UIManager.put(TileUI.UI_CLASS_ID, "jcs.ui.layout.tiles.ui.StraightDirectionUI");
    setUI((TileUI) UIManager.getUI(this));
    invalidate();
  }

  @Override
  public boolean isDirectional() {
    return true;
  }

  /**
   * A Directional Tile a train can travel in the arrow direction
   *
   * @param other A Tile
   * @return true when other is connected on the arrow pointing side of this tile
   */
  @Override
  public boolean isArrowDirection(Tile other) {
    boolean arrowDirection = false;
    if (other != null) {
      Orientation orientation = getOrientation();
      Point switchPoint = getEdgePoints().get(orientation);
      Collection<Point> otherEdgePoints = other.getEdgePoints().values();
      arrowDirection = otherEdgePoints.contains(switchPoint);
    }
    return arrowDirection && isAdjacent(other);
  }
}
