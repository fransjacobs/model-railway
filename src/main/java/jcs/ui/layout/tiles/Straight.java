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
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jcs.entities.TileBean;

public class Straight extends AbstractTile implements Tile {

  Straight(TileBean tileBean) {
    super(tileBean);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  Straight(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  Straight(Orientation orientation, int x, int y) {
    super(orientation, x, y);
    this.type = TileType.STRAIGHT.getTileType();
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      // Horizontal
      neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
      neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
    } else {
      // Vertical
      neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
      neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      // Horizontal
      edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
      edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
    } else {
      // Vertical
      edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
      edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
    }
    return edgeConnections;
  }

  @Override
  public Set<Point> getAllPoints() {
    Set<Point> aps = new HashSet<>();
    aps.add(getCenter());
    return aps;
  }

  protected void renderStraight(Graphics2D g2) {
    int xx, yy, w, h;
    xx = 0;
    yy = 170;
    w = RENDER_WIDTH;
    h = 60;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);

    g2.fillRect(xx, yy, w, h);
  }

  protected void renderRouteStraight(Graphics2D g2) {
    int xx, yy, w, h;
    xx = 0;
    yy = 190;
    w = RENDER_WIDTH;
    h = 20;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackRouteColor);

    g2.fillRect(xx, yy, w, h);
  }

  @Override
  public void renderTileRoute(Graphics2D g2) {
    renderRouteStraight(g2);
  }

  @Override
  public void renderTile(Graphics2D g2) {
    renderStraight(g2);
  }

}
