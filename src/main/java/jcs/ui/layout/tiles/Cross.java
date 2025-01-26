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
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jcs.entities.AccessoryBean.AccessoryValue;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.GRID;
import static jcs.ui.layout.tiles.Tile.tileHeight;
import static jcs.ui.layout.tiles.Tile.tileWidth;
import org.tinylog.Logger;

public class Cross extends Switch {

  public static final int CROSS_WIDTH = DEFAULT_WIDTH * 2;
  public static final int CROSS_HEIGHT = DEFAULT_HEIGHT * 2;

  public static final Color VERY_LIGHT_RED = new Color(255, 102, 102);
  public static final Color LIGHT_RED = new Color(255, 51, 51);
  public static final Color DARK_RED = new Color(204, 0, 0);

  public static final Color VERY_LIGHT_GREEN = new Color(102, 255, 102);
  public static final Color LIGHT_GREEN = new Color(0, 255, 51);
  public static final Color DARK_GREEN = new Color(0, 153, 0);

  public Cross(Orientation orientation, Direction direction, Point center) {
    this(orientation, direction, center.x, center.y);
  }

  public Cross(Orientation orientation, Direction direction, int x, int y) {
    this(orientation, direction, x, y, tileWidth(orientation, TileType.CROSS), tileHeight(orientation, TileType.CROSS));
  }

  public Cross(Orientation orientation, Direction direction, int x, int y, int width, int height) {
    super(TileType.CROSS, orientation, direction, x, y, width, height);
    changeRenderSizeAndOffsets();
  }

  public Cross(TileBean tileBean) {
    super(tileBean, tileWidth(tileBean.getOrientation(), TileType.CROSS), tileHeight(tileBean.getOrientation(), TileType.CROSS));
    changeRenderSizeAndOffsets();
  }

  private void changeRenderSizeAndOffsets() {
    //Reset offsets
    this.offsetY = 0;
    this.renderOffsetY = 0;
    this.offsetX = 0;
    this.renderOffsetX = 0;

    if (isHorizontal()) {
      //this.width = DEFAULT_WIDTH * 2;
      //this.height = DEFAULT_HEIGHT;
      this.renderWidth = RENDER_GRID * 4;
      this.renderHeight = RENDER_GRID * 2;

      this.offsetY = 0;
      this.renderOffsetY = 0;
    } else {
      //this.width = DEFAULT_WIDTH;
      //this.height = DEFAULT_HEIGHT * 2;
      this.renderWidth = RENDER_GRID * 2;
      this.renderHeight = RENDER_GRID * 4;

      this.offsetX = 0;
      this.renderOffsetX = 0;
    }

    //Due to the asymetical shape (center is on the left)
    //the offset has to be changed with the rotation
    switch (getOrientation()) {
      case SOUTH -> {
        this.offsetY = +GRID;
        this.renderOffsetY = RENDER_GRID;
      }
      case WEST -> {
        this.offsetX = -GRID;
        this.renderOffsetX = -RENDER_GRID;
      }
      case NORTH -> {
        this.offsetY = -GRID;
        this.renderOffsetY = -RENDER_GRID;
      }
      default -> {
        //East so default 
        this.offsetX = +GRID;
        this.renderOffsetX = +RENDER_GRID;
      }
    }
  }

//  private void changeRenderSize() {
//    if (Orientation.EAST == tileOrientation || Orientation.WEST == tileOrientation) {
//      this.renderWidth = RENDER_WIDTH * 2;
//      this.renderHeight = RENDER_HEIGHT;
//    } else {
//      this.renderWidth = RENDER_WIDTH;
//      this.renderHeight = RENDER_HEIGHT * 2;
//    }
//  }
  /**
   * A Cross has a width in horizontal position of 2 tiles and a height of 1 tile in Vertical position.<br>
   *
   * @return the set of center point which mark the position of the Cross
   */
  @Override
  public Set<Point> getAltPoints() {
    Set<Point> alternatives = new HashSet<>();

    switch (getOrientation()) {
      case SOUTH -> {
        Point sp = new Point(tileX, (tileY + DEFAULT_HEIGHT));
        alternatives.add(sp);
      }
      case WEST -> {
        Point wp = new Point((tileX - DEFAULT_WIDTH), tileY);
        alternatives.add(wp);
      }
      case NORTH -> {
        Point np = new Point(tileX, (tileY - DEFAULT_HEIGHT));
        alternatives.add(np);
      }
      default -> {
        //East so default 
        Point ep = new Point((tileX + DEFAULT_WIDTH), tileY);
        alternatives.add(ep);
      }
    }
    return alternatives;
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    Orientation orientation = this.getOrientation();
    Direction direction = this.getDirection();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 4));

        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy + Tile.GRID * 2));
          neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        } else {
          neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
          neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy + Tile.GRID * 2));
        }
      }
      case WEST -> {
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 4, cy));

        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx - Tile.GRID * 2, cy + Tile.GRID * 2));
        } else {
          neighbors.put(Orientation.NORTH, new Point(cx - Tile.GRID * 2, cy - Tile.GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
        }
      }
      case NORTH -> {
        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 4));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));

        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
          neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy - Tile.GRID * 2));
        } else {
          neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy - Tile.GRID * 2));
          neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        }
      }
      default -> {
        //EAST
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 4, cy));
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));

        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.NORTH, new Point(cx + Tile.GRID * 2, cy - Tile.GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
        } else {
          neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx + Tile.GRID * 2, cy + Tile.GRID * 2));
        }
      }
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    Orientation orientation = this.getOrientation();
    Direction direction = this.getDirection();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 3));

        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy + Tile.GRID * 2));
          edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        } else {
          edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
          edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy + Tile.GRID * 2));
        }
      }
      case WEST -> {
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID * 3, cy));

        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx - Tile.GRID * 2, cy + Tile.GRID));
        } else {
          edgeConnections.put(Orientation.NORTH, new Point(cx - Tile.GRID * 2, cy - Tile.GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
        }
      }
      case NORTH -> {
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 3));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));

        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
          edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy - Tile.GRID * 2));
        } else {
          edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy - Tile.GRID * 2));
          edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        }
      }
      default -> {
        //EAST
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID * 3, cy));
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));

        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.NORTH, new Point(cx + Tile.GRID * 2, cy - Tile.GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
        } else {
          edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx + Tile.GRID * 2, cy + Tile.GRID));
        }
      }
    }
    return edgeConnections;
  }

  @Override
  protected void renderStraight(Graphics2D g2, Color color) {
    int xx, yy, w, h;
    xx = 0;
    yy = 170;
    w = RENDER_WIDTH;
    h = 60;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillRect(xx, yy, w, h);
  }

  @Override
  protected void renderRouteStraight(Graphics2D g2, Color color) {
    int xx, yy, w, h;
    xx = 0;
    yy = 190;
    w = RENDER_WIDTH;
    h = 20;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderStraight2(Graphics2D g2, Color color) {
    int xx, yy, w, h;
    xx = RENDER_WIDTH;
    yy = 170;
    w = RENDER_WIDTH;
    h = 60;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderRouteStraight2(Graphics2D g2, Color color) {
    int xx, yy, w, h;
    xx = RENDER_WIDTH;
    yy = 190;
    w = RENDER_WIDTH;
    h = 20;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillRect(xx, yy, w, h);
  }

  @Override
  protected void renderDiagonal(Graphics2D g2, Color color) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{400, 400, 167, 230};
      yPoints = new int[]{170, 230, 0, 0};
    } else {
      xPoints = new int[]{400, 400, 170, 230};
      yPoints = new int[]{230, 170, 400, 400};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  protected void renderRouteDiagonal(Graphics2D g2, Color color) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{420, 400, 190, 210};
      yPoints = new int[]{210, 210, 0, 0};
    } else {
      xPoints = new int[]{400, 400, 190, 210};
      yPoints = new int[]{210, 190, 400, 400};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderDiagonal2(Graphics2D g2, Color color) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{400, 400, 570, 630};
      yPoints = new int[]{170, 230, 400, 400};
    } else {
      xPoints = new int[]{400, 400, 570, 630};
      yPoints = new int[]{230, 170, 0, 0};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderRouteDiagonal2(Graphics2D g2, Color color) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{400, 380, 590, 610};
      yPoints = new int[]{190, 190, 400, 400};
    } else {
      xPoints = new int[]{400, 380, 590, 610};
      yPoints = new int[]{210, 210, 0, 0};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(color);
    g2.setPaint(Color.cyan);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTile(Graphics2D g2) {
    if (accessoryValue == null) {
      this.accessoryValue = AccessoryValue.OFF;
    }

    switch (accessoryValue) {
      case RED -> {
        renderStraight2(g2, Cross.LIGHT_RED);
        renderDiagonal(g2, Cross.LIGHT_RED);
        renderStraight(g2, Cross.DARK_RED);
        renderDiagonal2(g2, Cross.DARK_RED);
      }
      case GREEN -> {
        renderDiagonal(g2, Cross.VERY_LIGHT_GREEN);
        renderDiagonal2(g2, Cross.VERY_LIGHT_GREEN);
        renderStraight(g2, Cross.DARK_GREEN);
        renderStraight2(g2, Cross.DARK_GREEN);
      }
      default -> {
        renderStraight(g2, trackColor);
        renderStraight2(g2, trackColor);
        renderDiagonal(g2, trackColor);
        renderDiagonal2(g2, trackColor);
      }
    }
  }

  @Override
  public void renderTileRoute(Graphics2D g2) {
    if (routeValue == null) {
      routeValue = AccessoryValue.OFF;
    }
    if (incomingSide == null) {
      incomingSide = getOrientation();
    }

    if (isHorizontal()) {
      if (AccessoryValue.GREEN == routeValue && (Orientation.NORTH == incomingSide || Orientation.SOUTH == incomingSide)) {
        renderRouteDiagonal(g2, trackRouteColor);
        renderRouteDiagonal2(g2, trackRouteColor);
      } else if (AccessoryValue.GREEN == routeValue && (Orientation.EAST == incomingSide || Orientation.WEST == incomingSide)) {
        renderRouteStraight(g2, trackRouteColor);
        renderRouteStraight2(g2, trackRouteColor);
      } else if (AccessoryValue.RED == routeValue && Orientation.EAST == getOrientation()) {
        if (Direction.RIGHT == getDirection() && (Orientation.EAST == incomingSide || Orientation.NORTH == incomingSide)) {
          renderRouteStraight2(g2, trackRouteColor);
          renderRouteDiagonal(g2, trackRouteColor);
        } else if (Direction.RIGHT == getDirection() && (Orientation.WEST == incomingSide || Orientation.SOUTH == incomingSide)) {
          renderRouteDiagonal2(g2, trackRouteColor);
          renderRouteStraight(g2, trackRouteColor);
        } else if (Direction.LEFT == getDirection() && (Orientation.EAST == incomingSide || Orientation.SOUTH == incomingSide)) {
          renderRouteStraight2(g2, trackRouteColor);
          renderRouteDiagonal(g2, trackRouteColor);
        } else if (Direction.LEFT == getDirection() && (Orientation.WEST == incomingSide || Orientation.NORTH == incomingSide)) {
          renderRouteStraight(g2, trackColor);
          renderRouteDiagonal2(g2, trackColor);
        }
      } else if (AccessoryValue.RED == routeValue && Orientation.WEST == getOrientation()) {
        if (Direction.RIGHT == getDirection() && (Orientation.EAST == incomingSide || Orientation.NORTH == incomingSide)) {
          renderRouteStraight(g2, trackRouteColor);
          renderRouteDiagonal2(g2, trackRouteColor);
        } else if (Direction.RIGHT == getDirection() && (Orientation.WEST == incomingSide || Orientation.SOUTH == incomingSide)) {
          renderRouteDiagonal(g2, trackRouteColor);
          renderRouteStraight2(g2, trackRouteColor);
        } else if (Direction.LEFT == getDirection() && (Orientation.EAST == incomingSide || Orientation.SOUTH == incomingSide)) {
          renderRouteStraight(g2, trackRouteColor);
          renderRouteDiagonal2(g2, trackRouteColor);
        } else if (Direction.LEFT == getDirection() && (Orientation.WEST == incomingSide || Orientation.NORTH == incomingSide)) {
          renderRouteStraight2(g2, trackColor);
          renderRouteDiagonal(g2, trackColor);
        }
      }
    } else {
      if (AccessoryValue.GREEN == routeValue && (Orientation.NORTH == incomingSide || Orientation.SOUTH == incomingSide)) {
        renderRouteStraight(g2, trackRouteColor);
        renderRouteStraight2(g2, trackRouteColor);
      } else if (AccessoryValue.GREEN == routeValue && (Orientation.EAST == incomingSide || Orientation.WEST == incomingSide)) {
        renderRouteDiagonal(g2, trackRouteColor);
        renderRouteDiagonal2(g2, trackRouteColor);
      } else if (AccessoryValue.RED == routeValue && Orientation.SOUTH == getOrientation()) {
        if (Direction.RIGHT == getDirection() && (Orientation.EAST == incomingSide || Orientation.SOUTH == incomingSide)) {
          renderRouteStraight2(g2, trackRouteColor);
          renderRouteDiagonal(g2, trackRouteColor);
        } else if (Direction.RIGHT == getDirection() && (Orientation.WEST == incomingSide || Orientation.NORTH == incomingSide)) {
          renderRouteDiagonal2(g2, trackRouteColor);
          renderRouteStraight(g2, trackRouteColor);
        } else if (Direction.LEFT == getDirection() && (Orientation.EAST == incomingSide || Orientation.NORTH == incomingSide)) {
          renderRouteStraight(g2, trackRouteColor);
          renderRouteDiagonal2(g2, trackRouteColor);
        } else if (Direction.LEFT == getDirection() && (Orientation.WEST == incomingSide || Orientation.SOUTH == incomingSide)) {
          renderRouteStraight2(g2, trackColor);
          renderRouteDiagonal(g2, trackColor);
        }
      } else if (AccessoryValue.RED == routeValue && Orientation.NORTH == getOrientation()) {
        if (Direction.RIGHT == getDirection() && (Orientation.EAST == incomingSide || Orientation.SOUTH == incomingSide)) {
          renderRouteStraight(g2, trackRouteColor);
          renderRouteDiagonal2(g2, trackRouteColor);
        } else if (Direction.RIGHT == getDirection() && (Orientation.WEST == incomingSide || Orientation.NORTH == incomingSide)) {
          renderRouteDiagonal(g2, trackRouteColor);
          renderRouteStraight2(g2, trackRouteColor);
        } else if (Direction.LEFT == getDirection() && (Orientation.EAST == incomingSide || Orientation.NORTH == incomingSide)) {
          renderRouteStraight2(g2, trackRouteColor);
          renderRouteDiagonal(g2, trackRouteColor);
        } else if (Direction.LEFT == getDirection() && (Orientation.WEST == incomingSide || Orientation.SOUTH == incomingSide)) {
          renderRouteStraight(g2, trackColor);
          renderRouteDiagonal2(g2, trackColor);
        }
      }
    }
  }

  @Override
  public AccessoryValue accessoryValueForRoute(Orientation from, Orientation to) {
    if (from != null && to != null && this.getDirection() != null) {
      switch (this.getDirection()) {
        case LEFT -> {
          if (this.isHorizontal()) {
            if ((from == Orientation.WEST && to == Orientation.EAST) || (from == Orientation.EAST && to == Orientation.WEST)) {
              return AccessoryValue.GREEN;
            } else if ((from == Orientation.NORTH && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.NORTH)) {
              return AccessoryValue.GREEN;
            } else if ((from == Orientation.NORTH && to == Orientation.WEST) || (from == Orientation.WEST && to == Orientation.NORTH)) {
              return AccessoryValue.RED;
            } else if ((from == Orientation.EAST && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.EAST)) {
              return AccessoryValue.RED;
            } else {
              return AccessoryValue.OFF;
            }
          } else {
            //Vertical
            if ((from == Orientation.WEST && to == Orientation.EAST) || (from == Orientation.EAST && to == Orientation.WEST)) {
              return AccessoryValue.GREEN;
            } else if ((from == Orientation.NORTH && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.NORTH)) {
              return AccessoryValue.GREEN;
            } else if ((from == Orientation.SOUTH && to == Orientation.WEST) || (from == Orientation.WEST && to == Orientation.SOUTH)) {
              return AccessoryValue.RED;
            } else if ((from == Orientation.EAST && to == Orientation.NORTH) || (from == Orientation.NORTH && to == Orientation.EAST)) {
              return AccessoryValue.RED;
            } else {
              return AccessoryValue.OFF;
            }
          }
        }
        case RIGHT -> {
          if (this.isHorizontal()) {
            if ((from == Orientation.WEST && to == Orientation.EAST) || (from == Orientation.EAST && to == Orientation.WEST)) {
              return AccessoryValue.GREEN;
            } else if ((from == Orientation.NORTH && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.NORTH)) {
              return AccessoryValue.GREEN;
            } else if ((from == Orientation.NORTH && to == Orientation.EAST) || (from == Orientation.EAST && to == Orientation.NORTH)) {
              return AccessoryValue.RED;
            } else if ((from == Orientation.WEST && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.WEST)) {
              return AccessoryValue.RED;
            } else {
              return AccessoryValue.OFF;
            }
          } else {
            //Vertical
            if ((from == Orientation.WEST && to == Orientation.EAST) || (from == Orientation.EAST && to == Orientation.WEST)) {
              return AccessoryValue.GREEN;
            } else if ((from == Orientation.NORTH && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.NORTH)) {
              return AccessoryValue.GREEN;
            } else if ((from == Orientation.SOUTH && to == Orientation.EAST) || (from == Orientation.EAST && to == Orientation.SOUTH)) {
              return AccessoryValue.RED;
            } else if ((from == Orientation.WEST && to == Orientation.NORTH) || (from == Orientation.NORTH && to == Orientation.WEST)) {
              return AccessoryValue.RED;
            } else {
              return AccessoryValue.OFF;
            }
          }
        }
        default -> {
          return AccessoryValue.OFF;
        }
      }
    } else {
      return AccessoryValue.OFF;
    }
  }

  @Override
  protected void drawCenterPoint(Graphics2D g2d, Color color, double size) {
    //A Cross has 1 alternate point
    //1st square holds the centerpoint
    //2nd square 
    double dX1, dX2, dY1, dY2;
    Orientation tileOrientation = model.getTileOrienation();
    switch (tileOrientation) {
      case SOUTH -> {
        dX1 = renderWidth / 2 - size / 2;
        dY1 = renderHeight / 2 - renderHeight / 4 - size / 2;
        dX2 = renderWidth / 2 + renderWidth - size / 4;
        dY2 = renderHeight / 2 - renderHeight / 4 - size / 4;
      }
      case WEST -> {
        dX1 = renderWidth / 2 - renderWidth / 4 - size / 2;
        dY1 = renderHeight / 2 - size / 2;
        dX2 = renderWidth / 2 + renderWidth / 4 - size / 4;
        dY2 = renderHeight / 2 - size / 4;
      }
      case NORTH -> {
        dX1 = renderWidth / 2 - size / 2;
        dY1 = renderHeight / 2 - renderHeight / 4 - size / 2;
        dX2 = renderWidth / 2 + renderWidth - size / 4;
        dY2 = renderHeight / 2 - renderHeight / 4 - size / 4;
      }
      default -> {
        //East
        dX1 = renderWidth / 2 - renderWidth / 4 - size / 2;
        dY1 = renderHeight / 2 - size / 2;
        dX2 = renderWidth / 2 + renderWidth / 4 - size / 4;
        dY2 = renderHeight / 2 - size / 4;
      }
    }

    g2d.setColor(color);
    g2d.fill(new Ellipse2D.Double(dX1, dY1, size, size));
    g2d.fill(new Ellipse2D.Double(dX2, dY2, size / 2, size / 2));
  }

  @Override
  public Rectangle getTileBounds() {
    int multiplier = (model.isScaleImage() ? 1 : 10);
    int xx, yy;
    //Centerpoint is inbalanced
    Orientation tileOrientation = model.getTileOrienation();
    switch (tileOrientation) {
      case SOUTH -> {
        xx = tileX - GRID * multiplier;
        yy = tileY - GRID * multiplier;
      }
      case WEST -> {
        xx = tileX - GRID * multiplier - GRID * 2 * multiplier;
        yy = tileY - GRID * multiplier;
      }
      case NORTH -> {
        xx = tileX - GRID * multiplier;
        yy = tileY - GRID * multiplier - GRID * 2 * multiplier;
      }
      default -> {
        //East
        xx = tileX - GRID * multiplier;
        yy = tileY - GRID * multiplier;
      }
    }

    if (model.isScaleImage()) {
      return new Rectangle(xx, yy, tileWidth(tileOrientation, TileType.CROSS), tileHeight(tileOrientation, TileType.CROSS));
    } else {
      return new Rectangle(xx, yy, renderWidth, renderHeight);
    }
  }

}
