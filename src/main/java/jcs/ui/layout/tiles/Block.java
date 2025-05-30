/*
 * Copyright 2024 Frans Jacobs.
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
import jcs.entities.LocomotiveBean;
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
import jcs.ui.layout.tiles.ui.StraightUI;
import jcs.ui.layout.tiles.ui.TileUI;

/**
 * Representation of a Block on the layout
 */
public class Block extends Tile {

  public static final int BLOCK_WIDTH = DEFAULT_WIDTH * 3;
  public static final int BLOCK_HEIGHT = DEFAULT_HEIGHT * 3;

  public Block(TileBean tileBean) {
    super(tileBean);
    setModel(new DefaultTileModel(tileBean.getOrientation()));
    populateModel();
    initUI();
  }

  public Block(Orientation orientation, Point center) {
    this(orientation, center.x, center.y);
  }

  public Block(Orientation orientation, int x, int y) {
    this(orientation, x, y, tileWidth(orientation, TileType.BLOCK), tileHeight(orientation, TileType.BLOCK));
  }

  public Block(Orientation orientation, int x, int y, int width, int height) {
    super(TileType.BLOCK, orientation, x, y, width, height);
    setModel(new DefaultTileModel(orientation));
    initUI();
  }

  private void initUI() {
    updateUI();
  }

  @Override
  public String getUIClassID() {
    return StraightUI.UI_CLASS_ID;
  }

  @Override
  public void updateUI() {
    UIManager.put(TileUI.UI_CLASS_ID, "jcs.ui.layout.tiles.ui.BlockUI");
    setUI((TileUI) UIManager.getUI(this));
    invalidate();
  }

  @Override
  public Set<Point> getAltPoints() {
    int xx = this.tileX;
    int yy = this.tileY;
    Set<Point> alternatives = new HashSet<>();

    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      // West
      Point wp = new Point((xx - DEFAULT_WIDTH), yy);
      Point ep = new Point((xx + DEFAULT_WIDTH), yy);
      alternatives.add(wp);
      alternatives.add(ep);
    } else {
      Point np = new Point(xx, (yy - DEFAULT_HEIGHT));
      Point sp = new Point(xx, (yy + DEFAULT_HEIGHT));
      alternatives.add(np);
      alternatives.add(sp);
    }

    return alternatives;
  }

  @Override
  public Set<Point> getAllPoints() {
    Set<Point> aps = new HashSet<>();
    aps.add(getCenter());
    aps.addAll(getAltPoints());
    return aps;
  }

  @Override
  Set<Point> getAllPoints(Point center) {
    Set<Point> points = getAltPoints(center);
    points.add(center);
    return points;
  }

  @Override
  Set<Point> getAltPoints(Point center) {
    Set<Point> alts = new HashSet<>();

    if (Orientation.EAST == model.getTileOrienation() || Orientation.WEST == model.getTileOrienation()) {
      // West
      Point wp = new Point((center.x - DEFAULT_WIDTH), center.y);
      Point ep = new Point((center.x + DEFAULT_WIDTH), center.y);
      alts.add(wp);
      alts.add(ep);
    } else {
      Point np = new Point(center.x, (center.y - DEFAULT_HEIGHT));
      Point sp = new Point(center.x, (center.y + DEFAULT_HEIGHT));
      alts.add(np);
      alts.add(sp);
    }

    return alts;
  }

  public Point getAltPoint(String suffix) {
    int cx = this.getCenterX();
    int cy = this.getCenterY();
    if ("+".equals(suffix)) {
      return switch (this.getOrientation()) {
        case WEST ->
          new Point(cx - Tile.GRID * 2, cy);
        case NORTH ->
          new Point(cx, cy - Tile.GRID * 2);
        case SOUTH ->
          new Point(cx, cy + Tile.GRID * 2);
        default ->
          new Point(cx + Tile.GRID * 2, cy);
      };
    } else {
      return switch (this.getOrientation()) {
        case EAST ->
          new Point(cx - Tile.GRID * 2, cy);
        case SOUTH ->
          new Point(cx, cy - Tile.GRID * 2);
        case NORTH ->
          new Point(cx, cy + Tile.GRID * 2);
        default ->
          new Point(cx + Tile.GRID * 2, cy);
      };
    }
  }

  @Override
  public boolean isBlock() {
    return true;
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    // Horizontal
    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 4, cy));
      neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 4, cy));
    } else {
      // Vertical
      neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 4));
      neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 4));
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgePoints() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    Orientation orientation = this.getOrientation();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    // Horizontal
    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID * 3, cy));
      edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID * 3, cy));
    } else {
      // Vertical
      edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 3));
      edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 3));
    }
    return edgeConnections;
  }

  public Point getNeighborPoint(String suffix) {
    int cx = getCenterX();
    int cy = getCenterY();
    if ("+".equals(suffix)) {
      return switch (getOrientation()) {
        case WEST ->
          new Point(cx - Tile.GRID * 4, cy);
        case NORTH ->
          new Point(cx, cy - Tile.GRID * 4);
        case SOUTH ->
          new Point(cx, cy + Tile.GRID * 4);
        default ->
          new Point(cx + Tile.GRID * 4, cy);
      };
    } else {
      return switch (getOrientation()) {
        case EAST ->
          new Point(cx - Tile.GRID * 4, cy);
        case SOUTH ->
          new Point(cx, cy - Tile.GRID * 4);
        case NORTH ->
          new Point(cx, cy + Tile.GRID * 4);
        default ->
          new Point(cx + Tile.GRID * 4, cy);
      };
    }
  }

  public Orientation getTravelDirection(String suffix) {
    if ("+".equals(suffix)) {
      return getOrientation();
    } else {
      return switch (getOrientation()) {
        case EAST ->
          Orientation.WEST;
        case SOUTH ->
          Orientation.NORTH;
        case NORTH ->
          Orientation.SOUTH;
        default ->
          Orientation.EAST;
      };
    }
  }

  @Override
  public String getIdSuffix(Tile other) {
    String suffix = null;
    Orientation match = null;
    if (isAdjacent(other)) {
      Map<Orientation, Point> blockSides = this.getEdgePoints();
      Map<Orientation, Point> otherSides = other.getEdgePoints();

      for (Orientation bo : Orientation.values()) {
        Point bp = blockSides.get(bo);

        if (bp != null) {
          for (Orientation oo : Orientation.values()) {
            Point op = otherSides.get(oo);
            if (op != null) {
              if (op.equals(bp)) {
                match = bo;
                break;
              }
            }
          }
        }
      }
    }

    Orientation tileOrientation = model.getTileOrienation();
    if (match != null) {
      if (Orientation.EAST == tileOrientation && Orientation.EAST == match) {
        suffix = "+";
      }
      if (Orientation.WEST == tileOrientation && Orientation.WEST == match) {
        suffix = "+";
      }
      if (Orientation.EAST == tileOrientation && Orientation.WEST == match) {
        suffix = "-";
      }
      if (Orientation.WEST == tileOrientation && Orientation.EAST == match) {
        suffix = "-";
      }
      if (Orientation.NORTH == tileOrientation && Orientation.NORTH == match) {
        suffix = "+";
      }
      if (Orientation.NORTH == tileOrientation && Orientation.SOUTH == match) {
        suffix = "-";
      }
      if (Orientation.SOUTH == tileOrientation && Orientation.SOUTH == match) {
        suffix = "+";
      }
      if (Orientation.SOUTH == tileOrientation && Orientation.NORTH == match) {
        suffix = "-";
      }
    }
    return suffix;
  }

  @Override
  public Orientation rotate() {
    super.rotate();

    Orientation tileOrientation = model.getTileOrienation();
    int w = tileWidth(tileOrientation, TileType.BLOCK);
    int h = tileHeight(tileOrientation, TileType.BLOCK);

    Dimension d = new Dimension(w, h);
    setPreferredSize(d);
    setSize(d);
    setBounds(getTileBounds());
    return model.getTileOrienation();
  }

  public static String getDepartureSuffix(Orientation tileOrientation, boolean reverseArrival, LocomotiveBean.Direction direction) {
    if (LocomotiveBean.Direction.FORWARDS == direction) {
      if (Orientation.EAST == tileOrientation || Orientation.SOUTH == tileOrientation) {
        if (reverseArrival) {
          return "-";
        } else {
          return "+";
        }
      } else {
        if (reverseArrival) {
          return "+";
        } else {
          return "-";
        }
      }
    } else {
      if (Orientation.EAST == tileOrientation || Orientation.SOUTH == tileOrientation) {
        if (reverseArrival) {
          return "+";
        } else {
          return "-";
        }
      } else {
        if (reverseArrival) {
          return "-";
        } else {
          return "+";
        }
      }
    }
  }

  @Override
  public Rectangle getTileBounds() {
    int multiplier = (model.isScaleImage() ? 1 : 10);
    Orientation tileOrientation = model.getTileOrienation();

    int xx, yy;
    if (tileOrientation == Orientation.EAST || tileOrientation == Orientation.WEST) {
      xx = tileX - GRID * multiplier - GRID * multiplier * 2;
      yy = tileY - GRID * multiplier;
    } else {
      xx = tileX - GRID * multiplier;
      yy = tileY - GRID * multiplier - GRID * multiplier * 2;
    }

    if (model.isScaleImage()) {
      return new Rectangle(xx, yy, tileWidth(tileOrientation, TileType.BLOCK), tileHeight(tileOrientation, TileType.BLOCK));
    } else {
      int renderWidth = getUI().getRenderWidth();
      int renderHeight = getUI().getRenderHeight();

      return new Rectangle(xx, yy, renderWidth, renderHeight);
    }
  }

}
