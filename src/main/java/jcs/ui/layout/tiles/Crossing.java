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
import java.util.Map;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import jcs.entities.TileBean.TileType;

public class Crossing extends Straight {

  public Crossing(TileBean tileBean) {
    super(tileBean);
  }

  public Crossing(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  public Crossing(Orientation orientation, int x, int y) {
    this(orientation, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  public Crossing(Orientation orientation, int x, int y, int width, int height) {
    super(orientation, x, y, width, height);
    this.tileType = TileType.CROSSING;
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    // Horizontal
    neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
    neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
    // Vertical
    neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
    neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    // Horizontal
    edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
    edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
    // Vertical
    edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
    edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
    return edgeConnections;
  }

  protected void renderVerticalAndDividers(Graphics2D g2) {
    int xxn, yyn, xxs, yys, w, h;
    xxn = 175;
    yyn = 0;
    xxs = 175;
    yys = 325;
    w = 50;
    h = 75;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);

    //North
    g2.fillRect(xxn, yyn, w, h);
    //South
    g2.fillRect(xxs, yys, w, h);

    //Dividers
    int[] xNorthPoly = new int[]{85, 115, 285, 315};
    int[] yNorthPoly = new int[]{85, 125, 125, 85};

    int[] xSouthPoly = new int[]{85, 115, 285, 315};
    int[] ySouthPoly = new int[]{315, 275, 275, 315};

    g2.setPaint(Color.darkGray);
    g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    g2.drawPolyline(xNorthPoly, yNorthPoly, xNorthPoly.length);
    g2.drawPolyline(xSouthPoly, ySouthPoly, xSouthPoly.length);
  }

  @Override
  public void renderTile(Graphics2D g2) {
    renderStraight(g2);
    renderVerticalAndDividers(g2);
  }

  protected void renderRouteVertical(Graphics2D g2) {
    int xxn, yyn, xxs, yys, w, h;
    xxn = 190;
    yyn = 0;
    xxs = 190;
    yys = 325;
    w = 20;
    h = 75;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(this.trackRouteColor);

    //North
    g2.fillRect(xxn, yyn, w, h);
    //South
    g2.fillRect(xxs, yys, w, h);
  }

  @Override
  public void renderTileRoute(Graphics2D g2) {
    if (isHorizontal()) {
      if (Orientation.EAST == incomingSide || Orientation.WEST == incomingSide) {
        renderRouteStraight(g2);
      } else {
        renderRouteVertical(g2);
      }
    } else {
      if (Orientation.NORTH == incomingSide || Orientation.SOUTH == incomingSide) {
        renderRouteStraight(g2);
      } else {
        renderRouteVertical(g2);
      }
    }
  }

}
