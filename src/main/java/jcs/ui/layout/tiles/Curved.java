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

public class Curved extends AbstractTile implements Tile {

  Curved(TileBean tileBean) {
    super(tileBean);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  Curved(Orientation orientation, int x, int y) {
    this(orientation, new Point(x, y));
  }

  Curved(Orientation orientation, Point center) {
    super(orientation, center);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    this.type = TileType.CURVED.getTileType();
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
      }
      case WEST -> {
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
      }
      case NORTH -> {
        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
      }
      default -> {
        //EAST
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
      }
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();

    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        //       |  |
        // b  \  |\ |
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
      }
      case WEST -> {
        //       |/ |
        // t /   |  |
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
      }
      case NORTH -> {
        //  t \
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
      }
      default -> {
        //EAST b /
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
      }
    }
    return edgeConnections;
  }

  @Override
  public Set<Point> getAllPoints() {
    Set<Point> aps = new HashSet<>();
    aps.add(getCenter());
    return aps;
  }

  @Override
  public void renderTile(Graphics2D g2) {
    int[] xPoints = new int[]{400, 400, 170, 230};
    int[] yPoints = new int[]{230, 170, 400, 400};

    g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);

    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTileRoute(Graphics2D g2) {
    int[] xPoints = new int[]{400, 400, 190, 210};
    int[] yPoints = new int[]{210, 190, 400, 400};

    g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackRouteColor);

    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

}
