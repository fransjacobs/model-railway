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

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import static jcs.entities.TileBean.Direction.LEFT;
import static jcs.entities.TileBean.Direction.RIGHT;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.tiles.ui.SwitchUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a Switch or Turnout on the layout
 */
public class Switch extends Tile implements AccessoryEventListener {

  public Switch(Orientation orientation, Direction direction, Point center) {
    this(orientation, direction, center.x, center.y);
  }

  public Switch(Orientation orientation, Direction direction, int x, int y) {
    this(orientation, direction, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  public Switch(Orientation orientation, Direction direction, int x, int y, int width, int height) {
    this(TileType.SWITCH, orientation, direction, x, y, width, height);
  }

  protected Switch(TileType tileType, Orientation orientation, Direction direction, int x, int y, int width, int height) {
    super(tileType, orientation, direction, x, y, width, height);
    setModel(new DefaultTileModel(orientation));
    initUI();
  }

  public Switch(TileBean tileBean) {
    this(tileBean, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  protected Switch(TileBean tileBean, int width, int height) {
    super(tileBean, width, height);
    setModel(new DefaultTileModel(tileBean.getOrientation()));
    initUI();
  }

  private void initUI() {
    updateUI();
  }

  @Override
  public String getUIClassID() {
    return SwitchUI.UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    UIManager.put(TileUI.UI_CLASS_ID, "jcs.ui.layout.tiles.ui.SwitchUI");
    setUI((TileUI) UIManager.getUI(this));
    invalidate();
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
  public void onAccessoryChange(AccessoryEvent event) {
    if (getAccessoryBean() != null && event.isEventFor(accessoryBean)) {
      setAccessoryValue(event.getAccessoryBean().getAccessoryValue());
    }
  }

  @Override
  public boolean isJunction() {
    return true;
  }

  @Override
  public AccessoryValue accessoryValueForRoute(Orientation from, Orientation to) {
    if (from != null && to != null && this.getDirection() != null) {
      switch (this.getDirection()) {
        case LEFT -> {
          if (this.isHorizontal()) {
            if ((from == Orientation.WEST && to == Orientation.EAST) || (from == Orientation.EAST && to == Orientation.WEST)) {
              return AccessoryValue.GREEN;
            } else if (((from == Orientation.EAST && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.EAST)) && Orientation.EAST == this.getOrientation()) {
              return AccessoryValue.RED;
            } else if (((from == Orientation.WEST && to == Orientation.NORTH) || (from == Orientation.NORTH && to == Orientation.WEST)) && Orientation.WEST == this.getOrientation()) {
              return AccessoryValue.RED;
            } else {
              return AccessoryValue.OFF;
            }
          } else {
            //Vertical
            if ((from == Orientation.NORTH && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.NORTH)) {
              return AccessoryValue.GREEN;
            } else if (((from == Orientation.SOUTH && to == Orientation.WEST) || (from == Orientation.WEST && to == Orientation.SOUTH)) && Orientation.SOUTH == this.getOrientation()) {
              return AccessoryValue.RED;
            } else if (((from == Orientation.NORTH && to == Orientation.EAST) || (from == Orientation.EAST && to == Orientation.NORTH)) && Orientation.NORTH == this.getOrientation()) {
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
            } else if (((from == Orientation.EAST && to == Orientation.NORTH) || (from == Orientation.NORTH && to == Orientation.EAST)) && Orientation.EAST == this.getOrientation()) {
              return AccessoryValue.RED;
            } else if (((from == Orientation.WEST && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.WEST)) && Orientation.WEST == this.getOrientation()) {
              return AccessoryValue.RED;
            } else {
              return AccessoryValue.OFF;
            }
          } else {
            //Vertical
            if ((from == Orientation.NORTH && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.NORTH)) {
              return AccessoryValue.GREEN;
            } else if (((from == Orientation.EAST && to == Orientation.SOUTH) || (from == Orientation.SOUTH && to == Orientation.EAST)) && Orientation.SOUTH == this.getOrientation()) {
              return AccessoryValue.RED;
            } else if (((from == Orientation.WEST && to == Orientation.NORTH) || (from == Orientation.NORTH && to == Orientation.WEST)) && Orientation.NORTH == this.getOrientation()) {
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
}
