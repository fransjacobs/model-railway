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

import jcs.ui.layout.Tile;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jcs.controller.cs3.events.AccessoryMessageEvent;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.NORTH;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;
import jcs.entities.enums.TileType;
import jcs.trackservice.events.AccessoryListener;
import jcs.ui.layout.tiles.enums.Direction;
import org.tinylog.Logger;

public class Switch extends AbstractTile implements Tile, AccessoryListener {

  private static int idSeq;

  protected AccessoryValue accessoryValue;
  protected AccessoryValue routeValue;
  protected Color routeColor;

  public Switch(TileBean tileBean) {
    super(tileBean);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
  }

  public Switch(Orientation orientation, Direction direction, int x, int y) {
    this(orientation, direction, new Point(x, y));
  }

  public Switch(Orientation orientation, Direction direction, Point center) {
    super(orientation, direction, center.x, center.y);
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    this.type = TileType.SWITCH.getTileType();
  }

  @Override
  protected String getNewId() {
    idSeq++;
    return "sw-" + idSeq;
  }

  @Override
  protected void setIdSeq(int id) {
    idSeq = id;
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
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        } else {
          neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        }
      }
      case WEST -> {
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
        } else {
          neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
        }
      }
      case NORTH -> {
        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        } else {
          neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        }
      }
      default -> {
        //EAST
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
        } else {
          neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
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
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        } else {
          edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
        }
      }
      case WEST -> {
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
        } else {
          edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
        }
      }
      case NORTH -> {
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
        } else {
          edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        }
      }
      default -> {
        //EAST
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
        } else {
          edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
        }
      }
    }
    return edgeConnections;
  }

  @Override
  public boolean isAdjacent(Tile other) {
    boolean adjacent = false;

    if (other != null) {

      Point tc = getCenter();
      Point oc = other.getCenter();

      Collection<Point> thisEdgePoints = getEdgePoints().values();
      Collection<Point> otherEdgePoints = other.getEdgePoints().values();

      Collection<Point> thisNeighborPoints = getNeighborPoints().values();
      Collection<Point> otherNeighborPoints = other.getNeighborPoints().values();

      for (Point p : thisEdgePoints) {
        adjacent = otherEdgePoints.contains(p);
        if (adjacent) {
          break;
        }
      }

//      for (Point p : thisNeighborPoints) {
//        adjacent = p.equals(oc);
//        if (adjacent) {
//          break;
//        }
//      }
//      for (Point p : this.getEdgePoints().values()) {
//        adjacent = otherEdgePoints.contains(p);
//        if (adjacent) {
//          break;
//        }
//      }
    }

    return adjacent;
  }

  public AccessoryValue getAccessoryValue() {
    if (this.accessoryValue == null) {
      return AccessoryValue.OFF;
    } else {
      return accessoryValue;
    }
  }

  public void setValue(AccessoryValue value) {
    this.accessoryValue = value;
  }

  /**
   *
   * @param other A Tile
   * @return true when other is connected to the switch side of the Turnout
   */
  @Override
  public boolean isSwitchSide(Tile other) {
    Orientation orientation = this.getOrientation();
    Point switchSide = this.getNeighborPoints().get(orientation);
    Logger.trace(other.xyToString());
    return isAdjacent(other) && switchSide.equals(other.getCenter());
  }

  /**
   *
   * @param other A Tile
   * @return true when other is connected to the diverging side of the Turnout
   */
  @Override
  public boolean isDivergingSide(Tile other) {
    Orientation orientation = this.getOrientation();

    if (Orientation.NORTH == orientation || Orientation.SOUTH == orientation) {
      //Vertical so diverging side is either on the EAST or WEST side
      Point eastSide = this.getNeighborPoints().get(Orientation.EAST);
      Point westSide = this.getNeighborPoints().get(Orientation.WEST);

      return isAdjacent(other) && (eastSide.equals(other.getCenter()) || westSide.equals(other.getCenter()));
    } else {
      //Horizontal so diverging side is either on the NORTH or SOUTH side
      Point northSide = this.getNeighborPoints().get(Orientation.NORTH);
      Point southSide = this.getNeighborPoints().get(Orientation.SOUTH);

      return isAdjacent(other) && (northSide.equals(other.getCenter()) || southSide.equals(other.getCenter()));
    }
  }

  /**
   *
   * @param other A Tile
   * @return true when other is connected to the straight side of the Turnout
   */
  @Override
  public boolean isStraightSide(Tile other) {
    Orientation orientation = this.getOrientation();
    Point straightSide;
    straightSide = switch (orientation) {
      case NORTH ->
        getNeighborPoints().get(Orientation.SOUTH);
      case SOUTH ->
        getNeighborPoints().get(Orientation.NORTH);
      case WEST ->
        getNeighborPoints().get(Orientation.EAST);
      default ->
        getNeighborPoints().get(Orientation.WEST);
    };

    return isAdjacent(other) && straightSide.equals(other.getCenter());
  }

  public void setRouteValue(AccessoryValue value, Color routeColor) {
    this.routeValue = value;
    this.routeColor = routeColor;
  }

  protected void renderStraight(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int xx, yy, w, h;
    xx = 0;
    yy = 17;
    w = DEFAULT_WIDTH;
    h = 6;

    g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillRect(xx, yy, w, h);
  }

  protected void renderDiagonal(Graphics2D g2, Color trackColor, Color backgroundColor) {
    int[] xPoints, yPoints;
    if (Direction.RIGHT.equals(getDirection())) {
      xPoints = new int[]{40, 40, 16, 24};
      yPoints = new int[]{16, 24, 0, 0};
    } else {
      xPoints = new int[]{40, 40, 16, 24};
      yPoints = new int[]{24, 16, 40, 40};
    }

    g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setPaint(trackColor);
    g2.fillPolygon(xPoints, yPoints, xPoints.length);
  }

  @Override
  public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
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
        renderStraight(g2, trackColor, backgroundColor);
        renderDiagonal(g2, Color.red, backgroundColor);
      }
      case GREEN -> {
        renderDiagonal(g2, trackColor, backgroundColor);
        renderStraight(g2, Color.green, backgroundColor);
      }
      default -> {
        switch (this.routeValue) {
          case RED -> {
            renderStraight(g2, trackColor, backgroundColor);
            renderDiagonal(g2, this.routeColor, backgroundColor);
          }
          case GREEN -> {
            renderDiagonal(g2, trackColor, backgroundColor);
            renderStraight(g2, this.routeColor, backgroundColor);
          }
          default -> {
            renderStraight(g2, trackColor, backgroundColor);
            renderDiagonal(g2, trackColor, backgroundColor);
          }
        }
      }
    }
  }

  @Override
  public void onChange(AccessoryMessageEvent event) {
    if (this.getAccessoryBean() != null && this.getAccessoryId().equals(event.getAccessoryBean().getId())) {
      setValue(event.getAccessoryBean().getAccessoryValue());
      repaintTile();
    }
  }

  @Override
  public boolean isJunction() {
    return true;
  }

  @Override
  public AccessoryValue getSwitchValueTo(Tile other) {
    AccessoryValue switchDirection = AccessoryValue.OFF;
    if (isAdjacent(other)) {
      if (this.isHorizontal() && other.isHorizontal() || this.isVertical() && other.isVertical()) {
        //Both are horizontal or vertical so it is the longes side hence Green
        switchDirection = AccessoryValue.GREEN;
      } else {
        //one is on the "limp" so must be Red?
        switchDirection = AccessoryValue.RED;
      }
    }
    return switchDirection;
  }

}
