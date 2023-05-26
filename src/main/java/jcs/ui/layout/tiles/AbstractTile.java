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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.DEFAULT_HEIGHT;
import static jcs.entities.TileBean.DEFAULT_WIDTH;
import jcs.ui.layout.tiles.enums.Direction;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.NORTH;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;
import jcs.ui.layout.LayoutUtil;
import jcs.ui.layout.Tile;
import static jcs.ui.layout.Tile.DEFAULT_TRACK_COLOR;

/**
 *
 * Basic graphic element to display a track, turnout, etc on the screen. By default the drawing of a Tile is Horizontal from L to R
 * or West to East. Default orientation is East
 *
 * The default size of a Tile is 40 x 40 pixels. The center point of a Tile is stored and always snapped to the nearest grid point.
 * The basic grid is 20x 20 pixels.
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

    this.trackColor = DEFAULT_TRACK_COLOR;

    this.backgroundColor = backgroundColor;
    if (this.backgroundColor == null) {
      this.backgroundColor = Color.white;
    }
    init();
  }

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
    this.neighbours = other.getNeighbours();
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
    tb.setNeighbours(this.neighbours);
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

  protected abstract String getNewId();

  abstract void setIdSeq(int id);

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
      g2di.clearRect(0, 0, this.width, this.height);

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
  public Set<Point> getAltPoints() {
    return Collections.EMPTY_SET;
  }

  @Override
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
  public int getHeight() {
    return this.height;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getCenterX() {
    if (this.x == 0) {
      int w;
      if (this.width > 0) {
        w = this.width;
      } else {
        w = DEFAULT_WIDTH;
      }
      return w / 2;
    } else {
      return this.x;
    }
  }

  @Override
  public int getCenterY() {
    if (this.y == 0) {
      int h;
      if (this.height > 0) {
        h = this.height;
      } else {
        h = DEFAULT_HEIGHT;
      }
      return h / 2;
    } else {
      return this.y;
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

  protected String xyToString() {
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

}
