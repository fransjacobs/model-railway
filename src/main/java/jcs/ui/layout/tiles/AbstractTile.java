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
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.NORTH;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;
import jcs.entities.enums.TileType;
import static jcs.entities.enums.TileType.BLOCK;
import static jcs.entities.enums.TileType.CROSS;
import static jcs.entities.enums.TileType.CURVED;
import static jcs.entities.enums.TileType.END;
import static jcs.entities.enums.TileType.SWITCH;
import jcs.ui.layout.LayoutUtil;
import jcs.ui.layout.Tile;
import static jcs.ui.layout.Tile.DEFAULT_TRACK_COLOR;
import jcs.ui.layout.tiles.enums.Direction;

/**
 *
 * Basic graphic element to display a track, turnout, etc on the screen. By default the drawing of a Tile is Horizontal from L to R or West to East. Default orientation is East
 *
 * The default size of a Tile is 40 x 40 pixels. The center point of a Tile is stored and always snapped to the nearest grid point. The basic grid is 20x 20 pixels.
 *
 * A Tile can be rotated (always clockwise). Rotation will change the orientation from East -> South -> West -> North -> East.
 *
 * A Tile is rendered to a Buffered Image to speed up the display
 *
 */
abstract class AbstractTile extends TileBean implements Tile {

  protected int width;
  protected int height;
  protected int offsetX = 0;
  protected int offsetY = 0;

  protected Color trackColor;

  protected Color backgroundColor;
  protected boolean drawName = true;

  protected boolean drawOutline = false;

  protected PropertyChangeListener propertyChangeListener;

  protected AbstractTile(Point center) {
    this(Orientation.EAST, Direction.CENTER, center.x, center.y);
  }

  protected AbstractTile(Orientation orientation, Point center) {
    this(orientation, Direction.CENTER, center.x, center.y);
  }

  protected AbstractTile(Orientation orientation, int x, int y) {
    this(orientation, Direction.CENTER, x, y);
  }

  protected AbstractTile(Orientation orientation, Direction direction, int x, int y) {
    this(orientation, direction, x, y, null);
  }

  protected AbstractTile(Orientation orientation, Direction direction, int x, int y, Color backgroundColor) {
    this.tileOrientation = orientation.getOrientation();
    this.tileDirection = direction.getDirection();

    this.x = x;
    this.y = y;
    this.width = getDefaultWidth();
    this.height = getDefaultHeight();

    this.trackColor = DEFAULT_TRACK_COLOR;
    this.backgroundColor = backgroundColor;
    if (this.backgroundColor == null) {
      this.backgroundColor = Color.white;
    }
    init();
  }

  /**
   *
   * @return a new tile specific unique id
   */
  protected abstract String getNewId();

  /**
   *
   * @param id the last tile specific unique sequence number
   */
  abstract void setIdSeq(int id);

  protected AbstractTile(TileBean tileBean) {
    copyInto(tileBean);
    this.trackColor = DEFAULT_TRACK_COLOR;
    this.backgroundColor = Color.white;
  }

  private void copyInto(TileBean other) {
    this.id = other.getId();
    this.type = other.getType();
    this.tileOrientation = other.getTileOrientation();
    this.tileDirection = other.getTileDirection();
    this.x = other.getX();
    this.y = other.getY();
    this.setSignalType(other.getSignalType());
    this.accessoryId = other.getAccessoryId();
    this.sensorId = other.getSensorId();
    //this.neighbours = other.getNeighbours();
  }

  @Override
  public TileBean getTileBean() {
    TileBean tb = new TileBean();

    tb.setId(this.id);
    tb.setX(this.x);
    tb.setY(this.y);
    tb.setType(this.type);
    tb.setTileOrientation(this.tileOrientation);
    tb.setTileDirection(this.tileDirection);
    tb.setSignalAccessoryType(this.signalAccessoryType);
    tb.setAccessoryId(this.accessoryId);
    tb.setSensorId(this.sensorId);
    //tb.setNeighbours(this.neighbours);
    tb.setAccessoryBean(this.accessoryBean);
    tb.setSensorBean(this.sensorBean);

    return tb;
  }

  private void init() {
    this.id = getNewId();
  }

  @Override
  public Color getTrackColor() {
    return trackColor;
  }

  @Override
  public final void setTrackColor(Color trackColor) {
    if (!Objects.equals(this.trackColor, trackColor)) {
      this.trackColor = trackColor;
    }
  }

  @Override
  public Color getBackgroundColor() {
    return backgroundColor;
  }

  @Override
  public void setBackgroundColor(Color backgroundColor) {
    if (!Objects.equals(this.backgroundColor, backgroundColor)) {
      this.backgroundColor = backgroundColor;
    }
  }

  /**
   * Draw the AbstractTile
   *
   * @param g2d The graphics handle
   * @param drawOutline
   */
  @Override
  public void drawTile(Graphics2D g2d, boolean drawOutline) {
    //by default and image is rendered in the EAST orientation
    Orientation o = getOrientation();
    if (o == null) {
      o = Orientation.EAST;
    }

    BufferedImage cbi = TileImageCache.get(this);
    if (cbi == null) {
      BufferedImage bi = createImage();

      Graphics2D g2di = bi.createGraphics();
      g2di.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (trackColor == null) {
        trackColor = DEFAULT_TRACK_COLOR;
      }

      if (backgroundColor == null) {
        backgroundColor = Color.white;
      }

      AffineTransform backup = g2di.getTransform();
      AffineTransform trans = new AffineTransform();

      g2di.setBackground(backgroundColor);
      g2di.clearRect(0, 0, this.getWidth(), this.height);

      int ox = 0, oy = 0;

      switch (o) {
        case SOUTH -> {
          trans.rotate(Math.PI / 2, getWidth() / 2, getHeight() / 2);
          ox = (this.height - this.width) / 2;
          oy = (this.width - this.height) / 2;
          trans.translate(-ox, -oy);
        }
        case WEST -> {
          trans.rotate(Math.PI, getWidth() / 2, getHeight() / 2);
          trans.translate(ox, oy);
        }
        case NORTH -> {
          trans.rotate(-Math.PI / 2, getWidth() / 2, getHeight() / 2);
          ox = (this.height - this.width) / 2;
          oy = (this.width - this.height) / 2;
          trans.translate(-ox, -oy);
        }
        default -> {
          trans.rotate(0.0, getWidth() / 2, getHeight() / 2);
          trans.translate(ox, oy);
        }
      }

      g2di.setTransform(trans);

      renderTile(g2di, trackColor, backgroundColor);

      //outline, but only when the (line) grid is on!
      if (drawOutline) {
        g2di.setPaint(Color.lightGray);
        g2di.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (this instanceof Block) {
          g2di.drawRect(0, 0, DEFAULT_WIDTH * 3, DEFAULT_HEIGHT);
        } else if (this instanceof Cross) {
          g2di.drawRect(0, 0, DEFAULT_WIDTH * 2, DEFAULT_HEIGHT);
        } else {
          g2di.drawRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
      }
      g2di.setTransform(backup);
      g2di.dispose();
      TileImageCache.put(this, bi);
      cbi = bi;
    }

    g2d.drawImage(cbi, (x - cbi.getWidth() / 2) + this.offsetX, (y - cbi.getHeight() / 2) + this.offsetY, null);
  }

  /**
   * Render a tile image Always starts at (0,0) used the default width and height
   *
   * @param g2d the Graphic context
   */
  @Override
  public void drawName(Graphics2D g2) {

  }

  @Override
  public void drawCenterPoint(Graphics2D g2d) {
    drawCenterPoint(g2d, Color.GRAY);
  }

  @Override
  public void drawCenterPoint(Graphics2D g2, Color color) {
    drawCenterPoint(g2, color, 4);
  }

  @Override
  public void drawCenterPoint(Graphics2D g2d, Color color, double size) {
    double dX = (this.x - size / 2);
    double dY = (this.y - size / 2);

    g2d.setColor(color);
    g2d.fill(new Ellipse2D.Double(dX, dY, size, size));

    if (!getAltPoints().isEmpty()) {
      //Also draw the alt points
      Set<Point> alt = new HashSet<>(getAltPoints());

      for (Point ap : alt) {
        g2d.fill(new Ellipse2D.Double(ap.x, ap.y, size - 1, size - 1));
      }
    }
  }

  @Override
  public void drawBounds(Graphics2D g2d) {
    g2d.setColor(Color.yellow);
    g2d.draw(getBounds());
  }

  /**
   * Rotate the tile clockwise 90 deg
   */
  @Override
  public void rotate() {
    switch (getOrientation()) {
      case EAST ->
        setOrientation(Orientation.SOUTH);
      case SOUTH ->
        setOrientation(Orientation.WEST);
      case WEST ->
        setOrientation(Orientation.NORTH);
      default ->
        setOrientation(Orientation.EAST);
    }
  }

  @Override
  public void flipHorizontal() {
    if (Orientation.NORTH.equals(getOrientation()) || Orientation.SOUTH.equals(getOrientation())) {
      rotate();
      rotate();
    }
  }

  @Override
  public void flipVertical() {
    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      rotate();
      rotate();
    }
  }

  @Override
  public void move(int newX, int newY) {
    Point cs = LayoutUtil.snapToGrid(newX, newY);
    this.setCenter(cs);
  }

  protected static void drawRotate(Graphics2D g2d, double x, double y, int angle, String text) {
    g2d.translate((float) x, (float) y);
    g2d.rotate(Math.toRadians(angle));
    g2d.drawString(text, 0, 0);
    g2d.rotate(-Math.toRadians(angle));
    g2d.translate(-x, -y);
  }

  public static BufferedImage flipHorizontally(BufferedImage source) {
    BufferedImage output = new BufferedImage(source.getHeight(), source.getWidth(), source.getType());

    AffineTransform flip = AffineTransform.getScaleInstance(1, -1);
    flip.translate(0, -source.getHeight());
    AffineTransformOp op = new AffineTransformOp(flip, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

    op.filter(source, output);

    return output;
  }

  public static BufferedImage flipVertically(BufferedImage source) {
    BufferedImage output = new BufferedImage(source.getHeight(), source.getWidth(), source.getType());

    AffineTransform flip = AffineTransform.getScaleInstance(-1, 1);
    flip.translate(-source.getWidth(), 0);
    AffineTransformOp op = new AffineTransformOp(flip, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

    op.filter(source, output);

    return output;
  }

  @Override
  public void setOrientation(Orientation orientation) {
    this.tileOrientation = orientation.getOrientation();
  }

  @Override
  public void setDirection(Direction direction) {
    this.tileDirection = direction.getDirection();
  }

  @Override
  public void setCenter(Point center) {
    this.x = center.x;
    this.y = center.y;
  }

  @Override
  @Deprecated
  public Set<Point> getAltPoints() {
    return Collections.EMPTY_SET;
  }

  @Override
  @Deprecated
  public Set<Point> getAllPoints() {
    Set<Point> aps = new HashSet<>();
    aps.add(getCenter());
    aps.addAll(this.getAltPoints());
    return aps;
  }

  @Override
  public int getOffsetX() {
    return offsetX;
  }

  @Override
  public void setOffsetX(int offsetX) {
    this.offsetX = offsetX;
  }

  @Override
  public int getOffsetY() {
    return offsetY;
  }

  @Override
  public void setOffsetY(int offsetY) {
    this.offsetY = offsetY;
  }

  protected BufferedImage createImage() {
    return new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
  }

  @Override
  public int getCenterX() {
    if (this.x > 0) {
      return this.x;
    } else {
      return getDefaultWidth() / 2;
    }
  }

  @Override
  public int getCenterY() {
    if (this.y > 0) {
      return this.y;
    } else {
      return getDefaultHeight() / 2;
    }
  }

  public boolean isDrawName() {
    return drawName;
  }

  public void setDrawName(boolean drawName) {
    this.drawName = drawName;
  }

  @Override
  public boolean isDrawOutline() {
    return drawOutline;
  }

  @Override
  public void setDrawOutline(boolean drawOutline) {
    if (this.drawOutline != drawOutline) {
      this.drawOutline = drawOutline;
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " {id: " + id + ", orientation: " + getOrientation() + ", direction: " + getDirection() + ", center: " + xyToString() + "}";
  }

  @Override
  public Rectangle getBounds() {
    int w, h, cx, cy;
    if (this.width > 0 & this.height > 0) {
      w = this.width;
      h = this.height;
    } else {
      w = DEFAULT_WIDTH;
      h = DEFAULT_HEIGHT;
    }

    if (this.x > 0 && this.y > 0) {
      cx = this.x + this.offsetX;
      cy = this.y + this.offsetY;
    } else {
      cx = w / 2;
      cy = h / 2;
    }

    int ltx = cx - w / 2;
    int lty = cy - h / 2;
    return new Rectangle(ltx, lty, w, h);
  }

  @Override
  public Rectangle2D getBounds2D() {
    return getBounds().getBounds2D();
  }

  @Override
  public boolean contains(double x, double y) {
    int w, h, cx, cy, tlx, tly;
    if (this.width > 0 & this.height > 0) {
      w = this.width;
      h = this.height;
    } else {
      w = DEFAULT_WIDTH;
      h = DEFAULT_HEIGHT;
    }

    if (this.width > 0 & this.height > 0) {
      cx = this.x;
      cy = this.y;
    } else {
      cx = w / 2;
      cy = h / 2;
    }

    //top left dX and dY
    tlx = cx - w / 2;
    tly = cy - h / 2;

    //Check if X and Y range is ok
    return !(x < tlx || x > (tlx + w) || y < tly || y > (tly + h));
  }

  @Override
  public String xyToString() {
    return "(" + this.x + "," + this.y + ")";
  }

  @Override
  public boolean contains(Point2D p) {
    return this.contains(p.getX(), p.getY());
  }

  @Override
  public boolean intersects(double x, double y, double w, double h) {
    return getBounds().intersects(x, y, w, h);
  }

  @Override
  public boolean intersects(Rectangle2D r2d) {
    return getBounds().intersects(r2d);
  }

  @Override
  public boolean contains(double x, double y, double w, double h) {
    return getBounds().contains(x, y, w, h);
  }

  @Override
  public boolean contains(Rectangle2D r2d) {
    return getBounds().contains(r2d);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at) {
    return getBounds().getPathIterator(at);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return getBounds().getPathIterator(at, flatness);
  }

  public PropertyChangeListener getPropertyChangeListener() {
    return this.propertyChangeListener;
  }

  @Override
  public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListener = propertyChangeListener;
  }

  protected void repaintTile() {
    if (this.propertyChangeListener != null) {
      this.propertyChangeListener.propertyChange(new PropertyChangeEvent(this, "repaintTile", this, this));
    }
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  protected final int getDefaultWidth() {
    switch (this.getTileType()) {
      case BLOCK -> {
        if (Orientation.EAST.equals(this.getOrientation()) || Orientation.WEST.equals(getOrientation())) {
          return DEFAULT_WIDTH * 3;
        } else {
          return DEFAULT_WIDTH;
        }
      }
      case CROSS -> {
        if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
          return DEFAULT_WIDTH * 2;
        } else {
          return DEFAULT_WIDTH;
        }
      }
      default -> {
        //Straight,Curved,Sensor,Signal,Switch
        return DEFAULT_WIDTH;
      }
    }
  }

  protected final int getDefaultHeight() {
    switch (this.getTileType()) {
      case BLOCK -> {
        if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
          return DEFAULT_HEIGHT;
        } else {
          return DEFAULT_HEIGHT * 3;
        }
      }
      case CROSS -> {
        if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
          return DEFAULT_HEIGHT;
        } else {
          return DEFAULT_HEIGHT * 2;
        }
      }
      default -> {
        //Straight,Curved,Sensor,Signal,Switch
        return DEFAULT_HEIGHT;
      }
    }
  }

  @Override
  public int getGridX() {
    return (getCenterX() - Tile.GRID) / (Tile.GRID * 2);
  }

  @Override
  public int getGridY() {
    return (getCenterY() - Tile.GRID) / (Tile.GRID * 2);
  }

  /**
   * The main route of the tile is horizontal
   *
   * @return true when main route goes from East to West or vv
   */
  @Override
  public boolean isHorizontal() {
    return (Orientation.EAST == getOrientation() || Orientation.WEST == getOrientation()) && TileType.CURVED != getTileType();
  }

  /**
   * The main route of the tile is vertical
   *
   * @return true when main route goes from North to South or vv
   */
  @Override
  public boolean isVertical() {
    return (Orientation.NORTH == getOrientation() || Orientation.SOUTH == getOrientation()) && TileType.CURVED != getTileType();
  }

  @Override
  public boolean isJunction() {
    return false;
  }

  @Override
  public boolean isBlock() {
    return false;
  }

  /**
   * The main route of the tile is diagonal
   *
   * @return true when main route goes from North to East or West to South and vv
   */
  @Override
  public boolean isDiagonal() {
    return TileType.CURVED == getTileType();
  }

  public List<TileBean> getNeighbours() {
    return neighbours;
  }

  public void setNeighbours(List<TileBean> neighbours) {
    this.neighbours = neighbours;
  }

  @Override
  public String getIdSuffix(Tile other) {
    return "";
  }

  @Override
  public Map<Orientation, Point> getNeighborPoints() {
    Map<Orientation, Point> neighbors = new HashMap<>();
    TileType tiletype = this.getTileType();
    Orientation orientation = this.getOrientation();
    Direction direction = this.getDirection();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (tiletype) {
      case BLOCK -> {
        //Horizontal
        if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
          neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 4, cy));
          neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 4, cy));
        } else {
          //Vertical
          neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 4));
          neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 4));
        }
        break;
      }
      case END -> {
        switch (orientation) {
          case SOUTH ->
            neighbors.put(Orientation.SOUTH, new Point(cx, cy - Tile.GRID * 2));
          case WEST ->
            neighbors.put(Orientation.WEST, new Point(cx + Tile.GRID * 2, cy));
          case NORTH ->
            neighbors.put(Orientation.NORTH, new Point(cx, cy + Tile.GRID * 2));
          default -> //EAST
            neighbors.put(Orientation.EAST, new Point(cx - Tile.GRID * 2, cy));
        }
      }
      case CURVED -> {
        switch (orientation) {
          case SOUTH -> {
            neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
            neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
          }
          case WEST -> {
            neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
            neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
          }
          case NORTH -> {
            neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
            neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
          }
          default -> {
            //EAST
            neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
            neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
          }
        }
      }
      case SWITCH -> {
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
      }
      case CROSS -> {
        //TODO
      }
      default -> {
        //STRAIGHT, STRAIGHT_DIR, SIGNAL, SENSOR
        if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
          //Horizontal
          neighbors.put(Orientation.EAST, new Point(cx + Tile.GRID * 2, cy));
          neighbors.put(Orientation.WEST, new Point(cx - Tile.GRID * 2, cy));
        } else {
          //Vertical
          neighbors.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 2));
          neighbors.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 2));
        }
      }
    }
    return neighbors;
  }

  @Override
  public Map<Orientation, Point> getEdgeConnections() {
    Map<Orientation, Point> edgeConnections = new HashMap<>();
    TileType tiletype = this.getTileType();
    Orientation orientation = this.getOrientation();
    Direction direction = this.getDirection();
    int cx = this.getCenterX();
    int cy = this.getCenterY();

    switch (tiletype) {
      case BLOCK -> {
        //Horizontal
        if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
          edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID * 3, cy));
          edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID * 3, cy));
        } else {
          //Vertical
          edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID * 3));
          edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID * 3));
        }
        break;
      }
      case END -> {
        switch (orientation) {
          case SOUTH ->
            edgeConnections.put(Orientation.SOUTH, new Point(cx, cy - Tile.GRID));
          case WEST ->
            edgeConnections.put(Orientation.WEST, new Point(cx + Tile.GRID, cy));
          case NORTH ->
            edgeConnections.put(Orientation.NORTH, new Point(cx, cy + Tile.GRID));
          default -> //EAST
            edgeConnections.put(Orientation.EAST, new Point(cx - Tile.GRID, cy));
        }
      }
      case CURVED -> {
        switch (orientation) {
          case SOUTH -> {
            edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
            edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
          }
          case WEST -> {
            edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
            edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
          }
          case NORTH -> {
            edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
            edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
          }
          default -> {
            //EAST
            edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
            edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
          }
        }
      }
      case SWITCH -> {
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
      }
      case CROSS -> {
        //TODO
      }
      default -> {
        //STRAIGHT, STRAIGHT_DIR, SIGNAL, SENSOR
        if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
          //Horizontal
          edgeConnections.put(Orientation.EAST, new Point(cx + Tile.GRID, cy));
          edgeConnections.put(Orientation.WEST, new Point(cx - Tile.GRID, cy));
        } else {
          //Vertical
          edgeConnections.put(Orientation.NORTH, new Point(cx, cy - Tile.GRID));
          edgeConnections.put(Orientation.SOUTH, new Point(cx, cy + Tile.GRID));
        }
      }
    }
    return edgeConnections;
  }

  @Override
  public boolean canTraverseTo(Tile other) {
    boolean canTravel = false;

    for (Point p : this.getEdgeConnections().values()) {
      if (other != null) {
        canTravel = other.getEdgeConnections().containsValue(p);
        if (canTravel) {
          break;
        }
      }
    }

    return canTravel;
  }

  @Override
  public AccessoryValue getSwitchValueTo(Tile other) {
    return AccessoryValue.OFF;
  }

}
