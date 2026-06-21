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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.UIManager;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import static jcs.ui.layout.tiles.LayoutScale.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.LayoutScale.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.LayoutScale.GRID;
import static jcs.ui.layout.tiles.Tile.tileHeight;
import static jcs.ui.layout.tiles.Tile.tileWidth;
import jcs.ui.layout.tiles.ui.CrossSwitchUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a Cross switch on the layout
 */
public class CrossSwitch extends Switch implements AccessoryEventListener {

  //public static final int CROSS_SWITCH_WIDTH = DEFAULT_WIDTH * 2;
  //public static final int CROSS_SWITCH_HEIGHT = DEFAULT_HEIGHT * 2;
  public static final Color VERY_LIGHT_RED = new Color(255, 102, 102);
  public static final Color LIGHT_RED = new Color(255, 51, 51);
  public static final Color DARK_RED = new Color(204, 0, 0);

  public static final Color VERY_LIGHT_GREEN = new Color(102, 255, 102);
  public static final Color LIGHT_GREEN = new Color(0, 255, 51);
  public static final Color DARK_GREEN = new Color(0, 153, 0);
  private static final long serialVersionUID = 471193107877761707L;

  public CrossSwitch(Orientation orientation, Direction direction, Point center) {
    this(orientation, direction, center.x, center.y);
  }

  public CrossSwitch(Orientation orientation, Direction direction, int x, int y) {
    this(orientation, direction, x, y, tileWidth(orientation, TileType.CROSS_SWITCH), tileHeight(orientation, TileType.CROSS_SWITCH));
  }

  public CrossSwitch(Orientation orientation, Direction direction, int x, int y, int width, int height) {
    super(TileType.CROSS_SWITCH, orientation, direction, x, y, width, height);
    changeRenderSizeAndOffsets();
  }

  public CrossSwitch(TileBean tileBean) {
    super(tileBean, tileWidth(tileBean.getOrientation(), TileType.CROSS_SWITCH), tileHeight(tileBean.getOrientation(), TileType.CROSS_SWITCH));
    changeRenderSizeAndOffsets();
  }

  @Override
  public String getUIClassID() {
    return CrossSwitchUI.UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    UIManager.put(TileUI.UI_CLASS_ID, "jcs.ui.layout.tiles.ui.CrossSwitchUI");
    setUI((TileUI) UIManager.getUI(this));
    invalidate();
  }

  /**
   * A CrossSwitch has a width in horizontal position of 2 tiles and a height of 1 tile in Vertical position.<br>
   *
   * @return the Set of points which mark the position of the Cross
   */
  @Override
  public Set<Point> getAltPoints() {
    return getAltPoints(getCenter());
  }

  @Override
  public Set<Point> getAllPoints() {
    return getAllPoints(getCenter());
  }

  @Override
  public Set<Point> getAllPoints(Point center) {
    Set<Point> aps = getAltPoints(center);
    aps.add(center);
    return aps;
  }

  @Override
  Set<Point> getAltPoints(Point center) {
    Set<Point> alts = new HashSet<>();
    switch (getOrientation()) {
      case SOUTH -> {
        Point sp = new Point(center.x, (center.y + DEFAULT_HEIGHT));
        alts.add(sp);
      }
      case WEST -> {
        Point wp = new Point((center.x - DEFAULT_WIDTH), center.y);
        alts.add(wp);
      }
      case NORTH -> {
        Point np = new Point(center.x, (center.y - DEFAULT_HEIGHT));
        alts.add(np);
      }
      default -> {
        //East so default 
        Point ep = new Point((center.x + DEFAULT_WIDTH), center.y);
        alts.add(ep);
      }
    }
    return alts;
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
        neighbors.put(Orientation.NORTH, new Point(cx, cy - GRID * 2));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + GRID * 4));

        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.EAST, new Point(cx + GRID * 2, cy + GRID * 2));
          neighbors.put(Orientation.WEST, new Point(cx - GRID * 2, cy));
        } else {
          neighbors.put(Orientation.EAST, new Point(cx + GRID * 2, cy));
          neighbors.put(Orientation.WEST, new Point(cx - GRID * 2, cy + GRID * 2));
        }
      }
      case WEST -> {
        neighbors.put(Orientation.EAST, new Point(cx + GRID * 2, cy));
        neighbors.put(Orientation.WEST, new Point(cx - GRID * 4, cy));

        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.NORTH, new Point(cx, cy - GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx - GRID * 2, cy + GRID * 2));
        } else {
          neighbors.put(Orientation.NORTH, new Point(cx - GRID * 2, cy - GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx, cy + GRID * 2));
        }
      }
      case NORTH -> {
        neighbors.put(Orientation.NORTH, new Point(cx, cy - GRID * 4));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + GRID * 2));

        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.EAST, new Point(cx + GRID * 2, cy));
          neighbors.put(Orientation.WEST, new Point(cx - GRID * 2, cy - GRID * 2));
        } else {
          neighbors.put(Orientation.EAST, new Point(cx + GRID * 2, cy - GRID * 2));
          neighbors.put(Orientation.WEST, new Point(cx - GRID * 2, cy));
        }
      }
      default -> {
        //EAST
        neighbors.put(Orientation.EAST, new Point(cx + GRID * 4, cy));
        neighbors.put(Orientation.WEST, new Point(cx - GRID * 2, cy));

        if (Direction.LEFT == direction) {
          neighbors.put(Orientation.NORTH, new Point(cx + GRID * 2, cy - GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx, cy + GRID * 2));
        } else {
          neighbors.put(Orientation.NORTH, new Point(cx, cy - GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx + GRID * 2, cy + GRID * 2));
        }
      }
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    Orientation orientation = getOrientation();
    Direction direction = getDirection();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - GRID));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + GRID * 3));

        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.EAST, new Point(cx + GRID, cy + GRID * 2));
          edgeConnections.put(Orientation.WEST, new Point(cx - GRID, cy));
        } else {
          edgeConnections.put(Orientation.EAST, new Point(cx + GRID, cy));
          edgeConnections.put(Orientation.WEST, new Point(cx - GRID, cy + GRID * 2));
        }
      }
      case WEST -> {
        edgeConnections.put(Orientation.EAST, new Point(cx + GRID, cy));
        edgeConnections.put(Orientation.WEST, new Point(cx - GRID * 3, cy));

        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.NORTH, new Point(cx, cy - GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx - GRID * 2, cy + GRID));
        } else {
          edgeConnections.put(Orientation.NORTH, new Point(cx - GRID * 2, cy - GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + GRID));
        }
      }
      case NORTH -> {
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - GRID * 3));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + GRID));

        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.EAST, new Point(cx + GRID, cy));
          edgeConnections.put(Orientation.WEST, new Point(cx - GRID, cy - GRID * 2));
        } else {
          edgeConnections.put(Orientation.EAST, new Point(cx + GRID, cy - GRID * 2));
          edgeConnections.put(Orientation.WEST, new Point(cx - GRID, cy));
        }
      }
      default -> {
        //EAST
        edgeConnections.put(Orientation.EAST, new Point(cx + GRID * 3, cy));
        edgeConnections.put(Orientation.WEST, new Point(cx - GRID, cy));

        if (Direction.LEFT == direction) {
          edgeConnections.put(Orientation.NORTH, new Point(cx + GRID * 2, cy - GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + GRID));
        } else {
          edgeConnections.put(Orientation.NORTH, new Point(cx, cy - GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx + GRID * 2, cy + GRID));
        }
      }
    }
    return edgeConnections;
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
  public Rectangle getTileBounds() {
    LayoutScale scale = LayoutScale.getInstance();
    int s = scale.scaledGrid();  // = GRID at 100%, proportionally less at lower scales

    int dispX = scale.toDisplay(tileX);
    int dispY = scale.toDisplay(tileY);

    Orientation orientation = model.getTileOrienation();
    int w, h;

    if (model.isScaleImage()) {
      w = scale.toDisplay(tileWidth(orientation, TileType.CROSS_SWITCH));
      h = scale.toDisplay(tileHeight(orientation, TileType.CROSS_SWITCH));
    } else {
      // unscaled: full render size
      w = getUI().getRenderWidth();
      h = getUI().getRenderHeight();
      // in unscaled mode coordinates are render-space, s must also be render-space
      s = GRID * 10;
      dispX = tileX * 10;
      dispY = tileY * 10;
    }

    int xx, yy;
    switch (orientation) {
      case WEST -> {
        xx = dispX - 3 * s;
        yy = dispY - s;
      }
      case NORTH -> {
        xx = dispX - s;
        yy = dispY - 3 * s;
      }
      default -> {
        xx = dispX - s;
        yy = dispY - s;
      }  // EAST and SOUTH
    }

    return new Rectangle(xx, yy, w, h);
  }

  private void changeRenderSizeAndOffsets() {
    this.renderOffsetY = 0;
    this.renderOffsetX = 0;
    if (isHorizontal()) {
      this.renderOffsetY = 0;
    } else {
      this.renderOffsetX = 0;
    }
  }

  @Override
  public Orientation rotate() {
    super.rotate();

    Orientation tileOrientation = model.getTileOrienation();
    LayoutScale scale = LayoutScale.getInstance();

    int w = scale.toDisplay(tileWidth(tileOrientation, TileType.CROSS_SWITCH));
    int h = scale.toDisplay(tileHeight(tileOrientation, TileType.CROSS_SWITCH));

    Dimension d = new Dimension(w, h);
    setPreferredSize(d);
    setSize(d);
    changeRenderSizeAndOffsets();

    setBounds(getTileBounds());
    return tileOrientation;
  }

}
