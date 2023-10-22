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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collection;
import jcs.entities.TileBean;

public class StraightDirection extends Straight {

  StraightDirection(TileBean tileBean) {
    super(tileBean);
  }

  StraightDirection(Orientation orientation, int x, int y) {
    this(orientation, new Point(x, y));
  }

  StraightDirection(Orientation orientation, Point center) {
    super(orientation, center);
    this.type = TileType.STRAIGHT_DIR.getTileType();
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

  private void renderDirectionArrow(Graphics2D g2) {

    //   |\
    // ==|+===
    //   |/
    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.green.darker());

    g2.fillPolygon(new int[]{150, 150, 270}, new int[]{130, 270, 200}, 3);
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    Graphics2D g2d = (Graphics2D) g2.create();

    renderStraight(g2d, trackColor, backgroundColor);
    renderDirectionArrow(g2d);
    g2d.dispose();
  }

}
