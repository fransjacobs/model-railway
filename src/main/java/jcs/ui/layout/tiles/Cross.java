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
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import static jcs.ui.layout.tiles.Tile.DEFAULT_HEIGHT;
import static jcs.ui.layout.tiles.Tile.DEFAULT_WIDTH;
import static jcs.ui.layout.tiles.Tile.GRID;
import static jcs.ui.layout.tiles.Tile.tileHeight;
import static jcs.ui.layout.tiles.Tile.tileWidth;
import jcs.ui.layout.tiles.ui.CrossUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a passive Cross (X) the layout
 */
public class Cross extends Tile {

  public static final int CROSS_WIDTH = DEFAULT_WIDTH * 2;
  public static final int CROSS_HEIGHT = DEFAULT_HEIGHT * 2;

  public Cross(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  public Cross(Orientation orientation, int x, int y) {
    this(orientation, x, y, tileWidth(orientation, TileType.CROSS), tileHeight(orientation, TileType.CROSS));
  }

  public Cross(Orientation orientation, int x, int y, int width, int height) {
    super(TileType.CROSS, orientation, x, y, width, height);
    setModel(new DefaultTileModel(orientation));

    changeRenderSizeAndOffsets();
    initUI();
  }

  public Cross(TileBean tileBean) {
    super(tileBean, tileWidth(tileBean.getOrientation(), TileType.CROSS), tileHeight(tileBean.getOrientation(), TileType.CROSS));
    setModel(new DefaultTileModel(tileBean.getOrientation()));

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
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (orientation) {
      case SOUTH -> {
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy + Tile.GRID * 2));
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy + Tile.GRID * 2));
      }
      case WEST -> {
        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
        neighbors.put(Orientation.SOUTH, new Point(cx, cy - Tile.GRID * 2));
        neighbors.put(Orientation.NORTH, new Point(cx + Tile.GRID * 2, cy - Tile.GRID * 2));
        neighbors.put(Orientation.SOUTH, new Point(cx + Tile.GRID * 2, cy + Tile.GRID * 2));
      }
      case NORTH -> {
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
        neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy + Tile.GRID * 2));
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy + Tile.GRID * 2));
      }
      default -> {
        //EAST
        neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy - Tile.GRID * 2));
        neighbors.put(Orientation.WEST, new Point(cx, cy + Tile.GRID * 2));

        neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
        neighbors.put(Orientation.SOUTH, new Point(cx + Tile.GRID * 2, cy + Tile.GRID * 2));
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
        //NW
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        //NE
        edgeConnections.put(Orientation.WEST, new Point(cx + Tile.GRID, cy));
        //SW
        edgeConnections.put(Orientation.EAST, new Point(cx - Tile.GRID, cy + Tile.GRID * 2));
        //SE
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy + Tile.GRID * 2));
      }
      case WEST -> {
        //NW
        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
        //NE
        edgeConnections.put(Orientation.NORTH, new Point(cx + Tile.GRID * 2, cy - Tile.GRID));
        //SW
        edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
        //SE
        edgeConnections.put(Orientation.SOUTH, new Point(cx + Tile.GRID * 2, cy + Tile.GRID));
      }
      case NORTH -> {
        //NW
        edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        //NE
        edgeConnections.put(Orientation.WEST, new Point(cx + Tile.GRID, cy));
        //SW
        edgeConnections.put(Orientation.EAST, new Point(cx - Tile.GRID, cy + Tile.GRID * 2));
        //SE
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy + Tile.GRID * 2));
      }
      default -> {
        //EASTd
        //[ -X ] / \
        //er is een south west naar north east
        // en enn north west naar south east
        // + is helder war nsew is nu is het x dus 45gr gedraai x  naar rechts.. dus
        //als orientation east is is de east connect south
        // south is west
        // west is north
        //north is east
        //hoe zat dit ook alsweer?
        edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy - Tile.GRID));
        edgeConnections.put(Orientation.WEST, new Point(cx, cy + Tile.GRID));

        edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
        edgeConnections.put(Orientation.SOUTH, new Point(cx + Tile.GRID * 2, cy + Tile.GRID));
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
    Orientation tileOrientation = model.getTileOrienation();
    int xx, yy, w, h, multiplier;
    if (model.isScaleImage()) {
      w = tileWidth(tileOrientation, TileType.CROSS);
      h = tileHeight(tileOrientation, TileType.CROSS);
      multiplier = 1;
    } else {
      int renderWidth = getUI().getRenderWidth();
      int renderHeight = getUI().getRenderHeight();

      w = renderWidth;
      h = renderHeight;
      multiplier = 10;
    }

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
      return new Rectangle(xx, yy, w, h);
    } else {
      int renderWidth = getUI().getRenderWidth();
      int renderHeight = getUI().getRenderHeight();

      return new Rectangle(xx, yy, renderWidth, renderHeight);
    }
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
    int w = tileWidth(tileOrientation, TileType.CROSS);
    int h = tileHeight(tileOrientation, TileType.CROSS);

    Dimension d = new Dimension(w, h);
    setPreferredSize(d);
    setSize(d);
    changeRenderSizeAndOffsets();

    setBounds(getTileBounds());
    return tileOrientation;
  }

}
