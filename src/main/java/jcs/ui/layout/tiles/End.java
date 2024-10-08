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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jcs.entities.TileBean;

public class End extends AbstractTile implements Tile {

  End(TileBean tileBean) {
    super(tileBean);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  End(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);

  }

  End(Orientation orientation, int x, int y) {
    super(orientation, x, y);
    this.type = TileType.END.getTileType();
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH ->
        neighbors.put(Orientation.SOUTH, new Point(cx, cy - Tile.GRID * 2));
      case WEST ->
        neighbors.put(Orientation.WEST, new Point(cx + Tile.GRID * 2, cy));
      case NORTH ->
        neighbors.put(Orientation.NORTH, new Point(cx, cy + Tile.GRID * 2));
      default -> //EAST
        neighbors.put(Orientation.EAST, new Point(cx - Tile.GRID * 2, cy));
    }
    return neighbors;
  }

  @Override
  public Set<Point> getAllPoints() {
    Set<Point> aps = new HashSet<>();
    aps.add(getCenter());
    return aps;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH ->
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy - Tile.GRID));
      case WEST ->
        edgeConnections.put(Orientation.WEST, new Point(cx + Tile.GRID, cy));
      case NORTH ->
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy + Tile.GRID));
      default -> //EAST
        edgeConnections.put(Orientation.EAST, new Point(cx - Tile.GRID, cy));
    }
    return edgeConnections;
  }

  protected void renderEnd(Graphics2D g2) {
    int xx, yy, w, h;
    xx = 0;
    yy = 175;

    w = RENDER_GRID;
    h = 50;

    g2.setStroke(new BasicStroke(40, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillRect(xx, yy, w, h);

    xx = RENDER_GRID;
    yy = 100;

    w = 30;
    h = 200;

    g2.setPaint(Color.DARK_GRAY);
    g2.fillRect(xx, yy, w, h);
  }

  @Override
  public void renderTile(Graphics2D g2) {
    renderEnd(g2);
  }

  @Override
  public void renderTileRoute(Graphics2D g2d) {
  }

}
