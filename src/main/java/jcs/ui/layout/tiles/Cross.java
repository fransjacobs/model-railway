/*
 * Copyright 2026 Frans Jacobs.
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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.UIManager;
import jcs.entities.AccessoryBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import static jcs.ui.layout.tiles.LayoutScale.GRID;
import static jcs.ui.layout.tiles.Tile.tileHeight;
import static jcs.ui.layout.tiles.Tile.tileWidth;
import jcs.ui.layout.tiles.ui.CrossUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a passive Cross (X) the layout
 */
public class Cross extends Tile {

  public Cross(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  public Cross(Orientation orientation, int x, int y) {
    this(orientation, x, y, tileWidth(orientation, TileType.CROSS), tileHeight(orientation, TileType.CROSS));
  }

  public Cross(Orientation orientation, int x, int y, int width, int height) {
    super(TileType.CROSS, orientation, x, y, width, height);
    changeRenderSizeAndOffsets();
    initUI();
  }

  public Cross(TileBean tileBean) {
    super(tileBean, tileWidth(tileBean.getOrientation(), TileType.CROSS), tileHeight(tileBean.getOrientation(), TileType.CROSS));
    changeRenderSizeAndOffsets();
    initUI();
  }

  private void initUI() {
    updateUI();
  }

  @Override
  public String getUIClassID() {
    return CrossUI.UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    UIManager.put(TileUI.UI_CLASS_ID, "jcs.ui.layout.tiles.ui.CrossUI");
    setUI((TileUI) UIManager.getUI(this));
    invalidate();
  }

  /**
   * A Cross has a width in horizontal position of 2 tiles and a height of 1 tile in Vertical position.<br>
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
  public Set<Point> getAllPoints(Point p) {
    Set<Point> aps = getAltPoints(p);
    aps.add(getCenter());
    return aps;
  }

  @Override
  Set<Point> getAltPoints(Point center) {
    Set<Point> alts = new HashSet<>();
    int cx = getCenterX();
    int cy = getCenterY();

    switch (getOrientation()) {
      case SOUTH -> {
        alts.add(new Point(cx, cy + 2 * GRID));
      }
      case WEST -> {
        alts.add(new Point(cx - 2 * GRID, cy));
      }
      case NORTH -> {
        alts.add(new Point(cx, cy - 2 * GRID));
      }
      default -> {
        alts.add(new Point(cx + 2 * GRID, cy));
      }
    }
    return alts;
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    Orientation orientation = getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        neighbors.put(Orientation.NORTH, new Point(cx + GRID * 2, cy));
        neighbors.put(Orientation.EAST, new Point(cx + GRID * 2, cy + GRID * 2));

        neighbors.put(Orientation.SOUTH, new Point(cx - GRID * 2, cy + GRID * 2));
        neighbors.put(Orientation.WEST, new Point(cx - GRID * 2, cy));
      }
      case WEST -> {
        neighbors.put(Orientation.EAST, new Point(cx, cy - GRID * 2));
        neighbors.put(Orientation.WEST, new Point(cx - GRID * 2, cy + GRID * 2));

        neighbors.put(Orientation.NORTH, new Point(cx - GRID * 2, cy - GRID * 2));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy + GRID * 2));
      }
      case NORTH -> {
        neighbors.put(Orientation.NORTH, new Point(cx + GRID * 2, cy - GRID * 2));
        neighbors.put(Orientation.EAST, new Point(cx + GRID * 2, cy));

        neighbors.put(Orientation.SOUTH, new Point(cx - GRID * 2, cy));
        neighbors.put(Orientation.WEST, new Point(cx - GRID * 2, cy - GRID * 2));
      }
      default -> {
        //EAST
        neighbors.put(Orientation.EAST, new Point(cx + GRID * 2, cy - GRID * 2));
        neighbors.put(Orientation.WEST, new Point(cx, cy + GRID * 2));

        neighbors.put(Orientation.NORTH, new Point(cx, cy - GRID * 2));
        neighbors.put(Orientation.SOUTH, new Point(cx + GRID * 2, cy + GRID * 2));
      }
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    Orientation orientation = getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        edgeConnections.put(Orientation.NORTH, new Point(cx + GRID, cy));
        edgeConnections.put(Orientation.EAST, new Point(cx + GRID, cy + GRID * 2));

        edgeConnections.put(Orientation.SOUTH, new Point(cx - GRID, cy + GRID * 2));
        edgeConnections.put(Orientation.WEST, new Point(cx - GRID, cy));
      }
      case WEST -> {
        edgeConnections.put(Orientation.EAST, new Point(cx, cy - GRID));
        edgeConnections.put(Orientation.WEST, new Point(cx - GRID * 2, cy + GRID));

        edgeConnections.put(Orientation.NORTH, new Point(cx - GRID * 2, cy - GRID));
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + GRID));
      }
      case NORTH -> {
        edgeConnections.put(Orientation.NORTH, new Point(cx + GRID, cy - GRID * 2));
        edgeConnections.put(Orientation.EAST, new Point(cx + GRID, cy));

        edgeConnections.put(Orientation.SOUTH, new Point(cx - GRID, cy));
        edgeConnections.put(Orientation.WEST, new Point(cx - GRID, cy - GRID * 2));
      }
      default -> {
        //EAST
        edgeConnections.put(Orientation.EAST, new Point(cx + GRID * 2, cy - GRID));
        edgeConnections.put(Orientation.WEST, new Point(cx, cy + GRID));

        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - GRID));
        edgeConnections.put(Orientation.SOUTH, new Point(cx + GRID * 2, cy + GRID));
      }
    }
    return edgeConnections;
  }

  @Override
  public boolean isDiagonalOpposite(Orientation from, Orientation to) {
    if ((from == EAST && to == WEST) || (from == WEST && to == EAST)) {
      return true;
    } else {
      return (from == NORTH && to == SOUTH) || (from == SOUTH && to == NORTH);
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
    renderOffsetY = 0;
    renderOffsetX = 0;
    if (isHorizontal()) {
      renderOffsetY = 0;
    } else {
      renderOffsetX = 0;
    }
  }

  @Override
  public Orientation rotate() {
    super.rotate();

    Orientation tileOrientation = model.getTileOrienation();
    LayoutScale scale = LayoutScale.getInstance();

    int w = scale.toDisplay(tileWidth(tileOrientation, TileType.CROSS));
    int h = scale.toDisplay(tileHeight(tileOrientation, TileType.CROSS));

    Dimension d = new Dimension(w, h);
    setPreferredSize(d);
    setSize(d);

    setBounds(getTileBounds());
    return tileOrientation;
  }

  @Override
  public AccessoryBean.AccessoryValue accessoryValueForRoute(Orientation from, Orientation to) {
    return AccessoryBean.AccessoryValue.OFF;
  }
}
