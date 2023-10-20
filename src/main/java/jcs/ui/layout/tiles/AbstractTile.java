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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.ui.layout.LayoutUtil;
import static jcs.ui.layout.tiles.Tile.DEFAULT_TRACK_COLOR;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

/**
 * Basic graphic element to display a track, turnout, etc on the screen. By default the drawing of a Tile is Horizontal from L to R or West to East. Default orientation is East
 *
 * <p>
 * The default size of a Tile is 40 x 40 pixels. The center point of a Tile is stored and always snapped to the nearest grid point. The basic grid is 20x 20 pixels.
 *
 * <p>
 * A Tile can be rotated (always clockwise). Rotation will change the orientation from East -> South -> West -> North -> East.
 *
 * <p>
 * A Tile is rendered to a Buffered Image to speed up the display
 */
abstract class AbstractTile extends TileBean implements Tile {

  protected int width;
  protected int height;
  protected int renderWidth;
  protected int renderHeight;

  protected int offsetX = 0;
  protected int offsetY = 0;

  protected int renderOffsetX = 0;
  protected int renderOffsetY = 0;

  protected Color trackColor;

  protected Color trackRouteColor;
  protected Orientation incomingSide;

  protected Color backgroundColor;
  protected boolean drawName = true;

  protected boolean drawOutline = false;

  protected boolean scaleImage = true;

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
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;

    this.renderWidth = RENDER_WIDTH;
    this.renderHeight = RENDER_HEIGHT;

    this.trackColor = DEFAULT_TRACK_COLOR;

    this.backgroundColor = backgroundColor;
    if (this.backgroundColor == null) {
      this.backgroundColor = Color.white;
    }
  }

  protected AbstractTile(TileBean tileBean) {
    copyInto(tileBean);
    this.trackColor = DEFAULT_TRACK_COLOR;
    this.backgroundColor = Color.white;
    this.renderWidth = RENDER_WIDTH;
    this.renderHeight = RENDER_HEIGHT;
  }

  private void copyInto(TileBean other) {
    this.id = other.getId();
    this.type = other.getType();
    this.tileOrientation = other.getTileOrientation();
    this.tileDirection = other.getTileDirection();
    this.x = other.getX();
    this.y = other.getY();

    if (other instanceof AbstractTile abstractTile) {
      this.renderWidth = abstractTile.getRenderWidth();
      this.renderHeight = abstractTile.getRenderHeight();
    }

    this.setSignalType(other.getSignalType());
    this.accessoryId = other.getAccessoryId();
    this.signalAccessoryType = other.getSignalAccessoryType();
    this.sensorId = other.getSensorId();

    this.accessoryBean = other.getAccessoryBean();
    this.sensorBean = other.getSensorBean();
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
    // tb.setNeighbours(this.neighbours);
    tb.setAccessoryBean(this.accessoryBean);
    tb.setSensorBean(this.sensorBean);

    return tb;
  }

  @Override
  public Color getTrackColor() {
    return trackColor;
  }

  @Override
  public final void setTrackColor(Color trackColor) {
    //if (!Objects.equals(this.trackColor, trackColor)) {
    this.trackColor = trackColor;
    //}
  }

  public Color getTrackRouteColor() {
    return trackRouteColor;
  }

  @Override
  public final void setTrackRouteColor(Color trackRouteColor, Orientation incomingSide) {
    this.trackRouteColor = trackRouteColor;
    this.incomingSide = incomingSide;
  }

  @Override
  public Color getBackgroundColor() {
    return backgroundColor;
  }

  @Override
  public void setBackgroundColor(Color backgroundColor) {
    //if (!Objects.equals(this.backgroundColor, backgroundColor)) {
    this.backgroundColor = backgroundColor;
    //}
  }

  /**
   * Draw the AbstractTile
   *
   * @param g2d The graphics handle
   * @param drawOutline
   */
  @Override
  public void drawTile(Graphics2D g2d, boolean drawOutline) {
    // by default and image is rendered in the EAST orientation
    Orientation o = getOrientation();
    if (o == null) {
      o = Orientation.EAST;
    }

    if (!TileImageCache.contains(this)) {
      BufferedImage nbi = createImage();

      Graphics2D g2di = nbi.createGraphics();
      if (trackColor == null) {
        trackColor = DEFAULT_TRACK_COLOR;
      }

      if (backgroundColor == null) {
        backgroundColor = Color.white;
      }

      AffineTransform backup = g2di.getTransform();
      AffineTransform trans = new AffineTransform();

      g2di.setBackground(backgroundColor);
      g2di.clearRect(0, 0, this.getRenderWidth(), this.getRenderHeight());

      int ox = 0, oy = 0;

      switch (o) {
        case SOUTH -> {
          trans.rotate(Math.PI / 2, this.renderWidth / 2, this.renderHeight / 2);
          ox = (this.renderHeight - this.renderWidth) / 2;
          oy = (this.renderWidth - this.renderHeight) / 2;
          trans.translate(-ox, -oy);
        }
        case WEST -> {
          trans.rotate(Math.PI, this.renderWidth / 2, this.renderHeight / 2);
          trans.translate(ox, oy);
        }
        case NORTH -> {
          trans.rotate(-Math.PI / 2, this.renderWidth / 2, this.renderHeight / 2);
          ox = (this.renderHeight - this.renderWidth) / 2;
          oy = (this.renderWidth - this.renderHeight) / 2;
          trans.translate(-ox, -oy);
        }
        default -> {
          trans.rotate(0.0, this.renderWidth / 2, this.renderHeight / 2);
          trans.translate(ox, oy);
        }
      }

      g2di.setTransform(trans);
      renderTile(g2di, trackColor, backgroundColor);

      g2di.setTransform(backup);

      //When the line grid is one the scale tile must be a little smaller
      int sw, sh;
      if (drawOutline) {
        sw = this.getWidth() - 2;
        sh = this.getHeight() - 2;
      } else {
        sw = this.getWidth();
        sh = this.getHeight();
      }
      // Scale the image back...
      if (scaleImage) {
        nbi = Scalr.resize(nbi, Method.QUALITY, Mode.FIT_EXACT, sw, sh, Scalr.OP_ANTIALIAS);
      }

      g2di.dispose();
      TileImageCache.put(this, nbi);
    }

    BufferedImage cbi = TileImageCache.get(this);

    int ox, oy;
    if (scaleImage) {
      ox = this.offsetX;
      oy = this.offsetY;
    } else {
      ox = this.renderOffsetX;
      oy = this.renderOffsetY;
    }

    g2d.drawImage(cbi, (x - cbi.getWidth() / 2) + ox, (y - cbi.getHeight() / 2) + oy, null);
  }

  /**
   * Render a tile image Always starts at (0,0) used the default width and height
   *
   * @param g2 the Graphic context
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
      // Also draw the alt points
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

    //    Font currentFont = g2d.getFont();
    //    Font newFont = currentFont.deriveFont(currentFont.getSize() * 0.8F);
    //    g2d.setFont(newFont);
    g2d.translate((float) x, (float) y);
    g2d.rotate(Math.toRadians(angle));
    g2d.drawString(text, 0, 0);
    g2d.rotate(-Math.toRadians(angle));
    g2d.translate(-x, -y);
  }

  public static BufferedImage flipHorizontally(BufferedImage source) {
    BufferedImage output
            = new BufferedImage(source.getHeight(), source.getWidth(), source.getType());

    AffineTransform flip = AffineTransform.getScaleInstance(1, -1);
    flip.translate(0, -source.getHeight());
    AffineTransformOp op = new AffineTransformOp(flip, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

    op.filter(source, output);

    return output;
  }

  public static BufferedImage flipVertically(BufferedImage source) {
    BufferedImage output
            = new BufferedImage(source.getHeight(), source.getWidth(), source.getType());

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
    return new BufferedImage(this.renderWidth, this.renderHeight, BufferedImage.TYPE_INT_RGB);
  }

  @Override
  public int getCenterX() {
    if (this.x > 0) {
      return this.x;
    } else {
      return GRID;
    }
  }

  @Override
  public int getCenterY() {
    if (this.y > 0) {
      return this.y;
    } else {
      return GRID;
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

  public boolean isScaleImage() {
    return scaleImage;
  }

  public void setScaleImage(boolean scaleImage) {
    this.scaleImage = scaleImage;
  }

  @Override
  public void setDrawOutline(boolean drawOutline) {
    if (this.drawOutline != drawOutline) {
      this.drawOutline = drawOutline;
    }
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
            + " {id: "
            + id
            + ", orientation: "
            + getOrientation()
            + ", direction: "
            + getDirection()
            + ", center: "
            + xyToString()
            + "}";
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

    // top left dX and dY
    tlx = cx - w / 2;
    tly = cy - h / 2;

    // Check if X and Y range is ok
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
      this.propertyChangeListener.propertyChange(
              new PropertyChangeEvent(this, "repaintTile", this, this));
    }
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  int getRenderWidth() {
    return this.renderWidth;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  int getRenderHeight() {
    return this.renderHeight;
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
    return (Orientation.EAST == getOrientation() || Orientation.WEST == getOrientation())
            && TileType.CURVED != getTileType();
  }

  /**
   * The main route of the tile is vertical
   *
   * @return true when main route goes from North to South or vv
   */
  @Override
  public boolean isVertical() {
    return (Orientation.NORTH == getOrientation() || Orientation.SOUTH == getOrientation())
            && TileType.CURVED != getTileType();
  }

  @Override
  public boolean isJunction() {
    return false;
  }

  @Override
  public boolean isBlock() {
    return false;
  }

  @Override
  public boolean isDirectional() {
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

  @Override
  public boolean isCrossing() {
    return TileType.CROSSING == getTileType();
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
  public Map<Point, Orientation> getNeighborOrientations() {
    Map<Point, Orientation> edgeOrientations = new HashMap<>();

    Map<Orientation, Point> neighborPoints = getNeighborPoints();

    for (Orientation o : Orientation.values()) {
      edgeOrientations.put(neighborPoints.get(o), o);
    }
    return edgeOrientations;
  }

  @Override
  public Map<Point, Orientation> getEdgeOrientations() {
    Map<Point, Orientation> edgeOrientations = new HashMap<>();

    Map<Orientation, Point> edgeConnections = getEdgePoints();

    for (Orientation o : Orientation.values()) {
      edgeOrientations.put(edgeConnections.get(o), o);
    }
    return edgeOrientations;
  }

  @Override
  public boolean isAdjacent(Tile other) {
    boolean adjacent = false;

    if (other != null) {
      Collection<Point> thisEdgePoints = getEdgePoints().values();
      Collection<Point> otherEdgePoints = other.getEdgePoints().values();

      for (Point p : thisEdgePoints) {
        adjacent = otherEdgePoints.contains(p);
        if (adjacent) {
          break;
        }
      }
    }

    return adjacent;
  }

  @Override
  public AccessoryValue getSwitchValueTo(Tile other) {
    if (other.isJunction()) {
      return other.getSwitchValueTo(this);
    } else {
      return AccessoryValue.OFF;
    }
  }

  /**
   * When the Tile is a Turnout then the switch side is the side of the tile which is the "central" point. From the switch side a Green or Red path is possible.
   *
   * @param other A Tile
   * @return true when other is connected to the switch side of the Turnout
   */
  @Override
  public boolean isSwitchSide(Tile other) {
    return false;
  }

  /**
   * When the Tile is a Turnout then the diverging side is the "limp" side of the tile. From the diverging side a Red path is possible.
   *
   * @param other A Tile
   * @return true when other is connected to the diverging side of the Turnout
   */
  @Override
  public boolean isDivergingSide(Tile other) {
    return false;
  }

  /**
   * When the Tile is a Turnout then the Straight side is the "through" side of the tile. From the Straight side a Green path is possible.
   *
   * @param other A Tile
   * @return true when other is connected to the straight side of the Turnout
   */
  @Override
  public boolean isStraightSide(Tile other) {
    return false;
  }

  /**
   * When the tile has a specific direction a train may travel then this method will indicate whether the other tile is in on the side where the arrow is pointing to
   *
   * @param other A Tile
   * @return true where other is on the side of this tile where the arrow points to
   */
  @Override
  public boolean isArrowDirection(Tile other) {
    return true;
  }

  @Override
  public AccessoryValue accessoryValueForRoute(Orientation from, Orientation to) {
    return AccessoryValue.OFF;
  }

  protected StringBuilder getImageKeyBuilder() {
    StringBuilder sb = new StringBuilder();
    sb.append(id);
    sb.append("~");
    sb.append(this.getOrientation().getOrientation());
    sb.append("~");
    sb.append(this.getDirection().getDirection());
    sb.append("~");
    sb.append(this.getBackgroundColor().toString());
    sb.append("~");
    sb.append(this.getTrackColor().toString());
    sb.append("~");
    sb.append(isDrawOutline() ? "y" : "n");
    sb.append("~");
    int r = this.backgroundColor.getRed();
    int g = this.backgroundColor.getGreen();
    int b = this.backgroundColor.getBlue();
    sb.append("#");
    sb.append(r);
    sb.append("#");
    sb.append(g);
    sb.append("#");
    sb.append(b);
    if (this.incomingSide != null) {
      sb.append("~");
      sb.append(this.incomingSide.getOrientation());
    }
    if (this.trackRouteColor != null) {
      sb.append("~");
      sb.append(this.trackRouteColor.toString());
    }

    sb.append("~");

    //Tile specific properties
    //AccessoryValue
    //SignalType
    //SignalValue
    //active;
    return sb;
  }

}
