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
import jcs.entities.AccessoryBean.AccessoryValue;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.GRID;

public class Cross extends Switch implements Tile {

  public static final int CROSS_WIDTH = DEFAULT_WIDTH * 2;
  public static final int CROSS_HEIGHT = DEFAULT_HEIGHT * 2;
  //public static final int CROSS_OFFSET = GRID;

  public static final Color VERY_LIGHT_RED = new Color(255, 102, 102);
  public static final Color LIGHT_RED = new Color(255, 51, 51);
  public static final Color DARK_RED = new Color(204, 0, 0);

  public static final Color VERY_LIGHT_GREEN = new Color(102, 255, 102);
  public static final Color LIGHT_GREEN = new Color(0, 255, 51);
  public static final Color DARK_GREEN = new Color(0, 153, 0);

  Cross(TileBean tileBean) {
    super(tileBean);
    setWidthHeightAndOffsets();
  }

  Cross(Orientation orientation, Direction direction, int x, int y) {
    this(orientation, direction, new Point(x, y));
  }

  public Cross(Orientation orientation, Direction direction, Point center) {
    super(orientation, direction, center);
    this.type = TileType.CROSS.getTileType();
    setWidthHeightAndOffsets();
  }

  private void setWidthHeightAndOffsets() {
    //Reset offsets
    this.offsetY = 0;
    this.renderOffsetY = 0;
    this.offsetX = 0;
    this.renderOffsetX = 0;

    if (isHorizontal()) {
      this.width = DEFAULT_WIDTH * 2;
      this.height = DEFAULT_HEIGHT;
      this.renderWidth = RENDER_GRID * 4;
      this.renderHeight = RENDER_GRID * 2;

      this.offsetY = 0;
      this.renderOffsetY = 0;
    } else {
      this.width = DEFAULT_WIDTH;
      this.height = DEFAULT_HEIGHT * 2;
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

  /**
   * A Cross has a width in horizontal position of 2 tiles and a height of 1 tile in Vertical position a width of 1 tile and a height of 2 tiles.
   *
   * @return the set of center point which mark the position of the Cross
   */
  @Override
  public Set<Point> getAltPoints() {
    int xx = this.x;
    int yy = this.y;
    Set<Point> alternatives = new HashSet<>();

    switch (getOrientation()) {
      case SOUTH -> {
        Point sp = new Point(xx, (yy + DEFAULT_HEIGHT));
        alternatives.add(sp);
      }
      case WEST -> {
        Point wp = new Point((xx - DEFAULT_WIDTH), yy);
        alternatives.add(wp);
      }
      case NORTH -> {
        Point np = new Point(xx, (yy - DEFAULT_HEIGHT));
        alternatives.add(np);
      }
      default -> {
        //East so default 
        Point ep = new Point((x + DEFAULT_WIDTH), yy);
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
  public void rotate() {
    super.rotate();
    setWidthHeightAndOffsets();
  }

  @Override
  protected void renderStraight(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int xx, yy, w, h;
    xx = 0;
    yy = 175;
    w = RENDER_WIDTH;
    h = 50;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderStraight2(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int xx, yy, w, h;
    xx = RENDER_WIDTH;
    yy = 175;
    w = RENDER_WIDTH;
    h = 50;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillRect(xx, yy, w, h);
  }

  @Override
  protected void renderDiagonal(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{400, 400, 167, 230};
      yPoints = new int[]{170, 230, 0, 0};
    } else {
      xPoints = new int[]{400, 400, 170, 230};
      yPoints = new int[]{230, 170, 400, 400};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  protected void renderDiagonal2(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{400, 400, 570, 630};
      yPoints = new int[]{170, 230, 400, 400};
    } else {
      xPoints = new int[]{400, 400, 570, 630};
      yPoints = new int[]{230, 170, 0, 0};
    }

    g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
    if (this.trackRouteColor != null) {
      if (routeValue == null) {
        this.routeValue = AccessoryValue.OFF;
      }
      if (this.isHorizontal()) {
        if (AccessoryValue.GREEN == this.routeValue && (Orientation.NORTH == this.incomingSide || Orientation.SOUTH == this.incomingSide)) {
          renderStraight(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderDiagonal(g2, this.trackRouteColor, backgroundColor);
          renderDiagonal2(g2, this.trackRouteColor, backgroundColor);
        } else if (AccessoryValue.GREEN == this.routeValue && (Orientation.EAST == this.incomingSide || Orientation.WEST == this.incomingSide)) {
          renderDiagonal(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderDiagonal2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight(g2, this.trackRouteColor, backgroundColor);
          renderStraight2(g2, this.trackRouteColor, backgroundColor);
        } else if (AccessoryValue.RED == this.routeValue && (Orientation.EAST == this.incomingSide || Orientation.NORTH == this.incomingSide) && Direction.RIGHT == this.getDirection()) {
          renderDiagonal2(g2, trackRouteColor, backgroundColor);
          renderStraight(g2, trackRouteColor, backgroundColor);
          renderStraight2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderDiagonal(g2, DEFAULT_TRACK_COLOR, backgroundColor);
        } else if (AccessoryValue.RED == this.routeValue && (Orientation.WEST == this.incomingSide || Orientation.SOUTH == this.incomingSide) && Direction.RIGHT == this.getDirection()) {
          renderDiagonal(g2, trackRouteColor, backgroundColor);
          renderStraight2(g2, trackRouteColor, backgroundColor);
          renderStraight(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderDiagonal2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
        } else if (AccessoryValue.RED == this.routeValue && (Orientation.EAST == this.incomingSide || Orientation.SOUTH == this.incomingSide) && Direction.LEFT == this.getDirection()) {          
          renderDiagonal2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight2(g2, this.trackRouteColor, backgroundColor);
          renderDiagonal(g2, this.trackRouteColor, backgroundColor);
        } else if (AccessoryValue.RED == this.routeValue && (Orientation.WEST == this.incomingSide || Orientation.NORTH == this.incomingSide) && Direction.LEFT == this.getDirection()) {
          renderDiagonal(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight(g2, this.trackRouteColor, backgroundColor);
          renderDiagonal2(g2, this.trackRouteColor, backgroundColor);
        }
      } else {
        //Vertical
        if (AccessoryValue.GREEN == this.routeValue && (Orientation.NORTH == this.incomingSide || Orientation.SOUTH == this.incomingSide)) {
          renderDiagonal(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderDiagonal2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight(g2, this.trackRouteColor, backgroundColor);
          renderStraight2(g2, this.trackRouteColor, backgroundColor);
        } else if (AccessoryValue.GREEN == this.routeValue && (Orientation.EAST == this.incomingSide || Orientation.WEST == this.incomingSide)) {          
          renderStraight(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderDiagonal(g2, this.trackRouteColor, backgroundColor);
          renderDiagonal2(g2, this.trackRouteColor, backgroundColor);
        } else if (AccessoryValue.RED == this.routeValue && (Orientation.EAST == this.incomingSide || Orientation.SOUTH == this.incomingSide) && Direction.RIGHT == this.getDirection()) {
          renderDiagonal(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight(g2, this.trackRouteColor, backgroundColor);
          renderDiagonal2(g2, this.trackRouteColor, backgroundColor);    
        } else if (AccessoryValue.RED == this.routeValue && (Orientation.WEST == this.incomingSide || Orientation.NORTH == this.incomingSide) && Direction.RIGHT == this.getDirection()) {
          renderDiagonal2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight2(g2, this.trackRouteColor, backgroundColor);
          renderDiagonal(g2, this.trackRouteColor, backgroundColor);       
        } else if (AccessoryValue.RED == this.routeValue && (Orientation.EAST == this.incomingSide || Orientation.NORTH == this.incomingSide) && Direction.LEFT == this.getDirection()) {
          renderDiagonal(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight(g2, this.trackRouteColor, backgroundColor);
          renderDiagonal2(g2, this.trackRouteColor, backgroundColor);
        } else if (AccessoryValue.RED == this.routeValue && (Orientation.WEST == this.incomingSide || Orientation.SOUTH == this.incomingSide) && Direction.LEFT == this.getDirection()) {
          renderDiagonal2(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight(g2, DEFAULT_TRACK_COLOR, backgroundColor);
          renderStraight2(g2, this.trackRouteColor, backgroundColor);
          renderDiagonal(g2, this.trackRouteColor, backgroundColor);
        }
      }
    } else {
      if (accessoryValue == null) {
        this.accessoryValue = AccessoryValue.OFF;
      }
      if (routeValue == null) {
        this.routeValue = AccessoryValue.OFF;
      }

      if (this.routeColor == null) {
        this.routeColor = trackColor;
      }

      switch (this.accessoryValue) {
        case RED -> {
          renderStraight2(g2, Cross.LIGHT_RED, backgroundColor);
          renderDiagonal(g2, Cross.LIGHT_RED, backgroundColor);
          renderStraight(g2, Cross.DARK_RED, backgroundColor);
          renderDiagonal2(g2, Cross.DARK_RED, backgroundColor);
        }
        case GREEN -> {
          renderDiagonal(g2, Cross.VERY_LIGHT_GREEN, backgroundColor);
          renderDiagonal2(g2, Cross.VERY_LIGHT_GREEN, backgroundColor);
          renderStraight(g2, Cross.DARK_GREEN, backgroundColor);
          renderStraight2(g2, Cross.DARK_GREEN, backgroundColor);
        }
        default -> {
          switch (this.routeValue) {
            case RED -> {
              renderStraight2(g2, this.routeColor, backgroundColor);
              renderDiagonal(g2, this.routeColor, backgroundColor);
              renderStraight(g2, this.routeColor, backgroundColor);
              renderDiagonal2(g2, this.routeColor, backgroundColor);
            }
            case GREEN -> {
              renderDiagonal(g2, this.routeColor, backgroundColor);
              renderDiagonal2(g2, this.routeColor, backgroundColor);
              renderStraight(g2, this.routeColor, backgroundColor);
              renderStraight2(g2, this.routeColor, backgroundColor);
            }
            default -> {
              renderStraight(g2, trackColor, backgroundColor);
              renderStraight2(g2, trackColor, backgroundColor);
              renderDiagonal(g2, trackColor, backgroundColor);
              renderDiagonal2(g2, trackColor, backgroundColor);
            }
          }
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
  public String getImageKey() {
    StringBuilder sb = getImageKeyBuilder();
    sb.append(getAccessoryValue());
    sb.append("~");
    sb.append(getRouteValue());
    return sb.toString();
  }
}
