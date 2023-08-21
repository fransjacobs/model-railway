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
import java.util.HashMap;
import java.util.Map;
import jcs.controller.events.AccessoryEvent;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.NORTH;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;
import jcs.entities.enums.TileType;
import jcs.trackservice.events.AccessoryListener;
import jcs.ui.layout.tiles.enums.Direction;

public class Switch extends AbstractTile implements Tile, AccessoryListener {

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

  public AccessoryValue getAccessoryValue() {
    if (this.accessoryValue == null) {
      return AccessoryValue.OFF;
    } else {
      return accessoryValue;
    }
  }

  public AccessoryValue getRouteValue() {
    if (this.routeValue == null) {
      return AccessoryValue.OFF;
    } else {
      return routeValue;
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
    boolean switchSide = false;
    if (other != null) {
      Orientation orientation = getOrientation();
      Point switchPoint = getEdgePoints().get(orientation);
      Collection<Point> otherEdgePoints = other.getEdgePoints().values();
      switchSide = otherEdgePoints.contains(switchPoint);
    }
    return switchSide && isAdjacent(other);
  }

  /**
   *
   * @param other A Tile
   * @return true when other is connected to the diverging side of the Turnout
   */
  @Override
  public boolean isDivergingSide(Tile other) {
    boolean divergingSide = false;
    if (other != null) {
      Orientation orientation = this.getOrientation();
      Direction direction = this.getDirection();
      Point divergingPoint;
      switch (orientation) {
        case NORTH -> {
          if (Direction.LEFT == direction) {
            divergingPoint = getEdgePoints().get(Orientation.EAST);
          } else {
            divergingPoint = getEdgePoints().get(Orientation.WEST);
          }
        }
        case SOUTH -> {
          if (Direction.LEFT == direction) {
            divergingPoint = getEdgePoints().get(Orientation.WEST);
          } else {
            divergingPoint = getEdgePoints().get(Orientation.EAST);
          }
        }
        case WEST -> {
          if (Direction.LEFT == direction) {
            divergingPoint = getEdgePoints().get(Orientation.NORTH);
          } else {
            divergingPoint = getEdgePoints().get(Orientation.SOUTH);
          }
        }
        default -> {
          if (Direction.LEFT == direction) {
            divergingPoint = getEdgePoints().get(Orientation.SOUTH);
          } else {
            divergingPoint = getEdgePoints().get(Orientation.NORTH);
          }
        }
      }

      Collection<Point> otherEdgePoints = other.getEdgePoints().values();
      divergingSide = otherEdgePoints.contains(divergingPoint);
    }
    return divergingSide && isAdjacent(other);
  }

  /**
   *
   * @param other A Tile
   * @return true when other is connected to the straight side of the Turnout
   */
  @Override
  public boolean isStraightSide(Tile other
  ) {
    boolean straightSide = false;
    if (other != null) {
      Orientation orientation = this.getOrientation();
      Collection<Point> otherEdgePoints = other.getEdgePoints().values();

      Point straightPoint;
      straightPoint = switch (orientation) {
        case NORTH ->
          getEdgePoints().get(Orientation.SOUTH);
        case SOUTH ->
          getEdgePoints().get(Orientation.NORTH);
        case WEST ->
          getEdgePoints().get(Orientation.EAST);
        default ->
          getEdgePoints().get(Orientation.WEST);
      };

      straightSide = otherEdgePoints.contains(straightPoint);
    }
    return straightSide && isAdjacent(other);
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
  public void onChange(AccessoryEvent event) {
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
    AccessoryValue switchDirection;
    if (this.isDivergingSide(other)) {
      //Other is on the diverging side
      switchDirection = AccessoryValue.RED;
    } else if (this.isSwitchSide(other) || this.isStraightSide(other)) {
      switchDirection = AccessoryValue.GREEN;
    } else {
      switchDirection = AccessoryValue.OFF;
    }
    return switchDirection;
  }

}
