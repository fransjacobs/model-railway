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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.AccessoryBean.SignalType;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import jcs.entities.TileBean.TileType;
import jcs.ui.layout.LayoutUtil;
import static jcs.ui.layout.tiles.Block.BLOCK_HEIGHT;
import static jcs.ui.layout.tiles.Block.BLOCK_WIDTH;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.tinylog.Logger;

/**
 * Basic graphic element to display a track, turnout, etc on the screen.<br>
 * By default the drawing of a Tile is Horizontal from L to R or West to East.<br>
 * The default orientation is East.
 *
 * <p>
 * The default size of a Tile is 40 tileX 40 pixels.<br>
 * The center point of a Tile is stored and always snapped to the nearest grid point.<br>
 * The basic grid is 20x 20 pixels.<br>
 *
 * <p>
 * A Tile can be rotated (always clockwise).<br>
 * Rotation will change the orientation from East -> South -> West -> North -> East.<br>
 *
 * <p>
 * A Tile is rendered to a Buffered Image to speed up the display
 */
public abstract class Tile extends JComponent { //implements TileEventListener { //, ItemSelectable {

  public static final int GRID = 20;
  public static final int DEFAULT_WIDTH = GRID * 2;
  public static final int DEFAULT_HEIGHT = GRID * 2;

  static final int RENDER_GRID = GRID * 10;
  static final int RENDER_WIDTH = RENDER_GRID * 2;
  static final int RENDER_HEIGHT = RENDER_GRID * 2;

  public static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
  public static final Color DEFAULT_TRACK_COLOR = Color.lightGray;
  public static final Color DEFAULT_ROUTE_TRACK_COLOR = Color.blue;
  public static final Color DEFAULT_SELECTED_COLOR = Color.yellow;

  public static final String MODEL_CHANGED_PROPERTY = "model";
  public static final String CONTENT_AREA_FILLED_CHANGED_PROPERTY = "contentAreaFilled";

  /**
   * The data model that determines the button's state.
   */
  protected TileModel model = null;

  protected String id;
  protected Integer tileX;
  protected Integer tileY;

  protected int renderWidth;
  protected int renderHeight;

  protected Orientation tileOrientation;
  protected Direction tileDirection;

  protected TileType tileType;
  protected String accessoryId;
  protected String sensorId;

  protected AccessoryValue accessoryValue;
  protected AccessoryValue routeValue;

  protected SignalType signalType;
  protected AccessoryBean.SignalValue signalValue;

  protected TileBean tileBean;
  protected AccessoryBean accessoryBean;
  protected SensorBean sensorBean;
  protected BlockBean blockBean;

  protected List<TileBean> neighbours;

  protected int offsetX = 0;
  protected int offsetY = 0;

  protected int renderOffsetX = 0;
  protected int renderOffsetY = 0;

  protected Color selectedColor;
  protected Color trackColor;
  protected Color trackRouteColor;
  protected Orientation incomingSide;

  protected Color backgroundColor;
  protected boolean drawName = true;

  protected BufferedImage tileImage;

  protected PropertyChangeListener propertyChangeListener;

  protected ChangeListener changeListener = null;
  protected ActionListener actionListener = null;

  protected transient ChangeEvent changeEvent;

  private Handler handler;

  protected Tile(TileType tileType, Orientation orientation, Point center, int width, int height) {
    this(tileType, orientation, Direction.CENTER, center.x, center.y, width, height);
  }

  protected Tile(TileType tileType, Orientation orientation, int x, int y, int width, int height) {
    this(tileType, orientation, Direction.CENTER, x, y, width, height);
  }

  protected Tile(TileType tileType, Orientation orientation, Direction direction, int x, int y, int width, int height) {
    this(tileType, orientation, direction, x, y, width, height, null, null);
  }

  protected Tile(TileType tileType, Orientation orientation, Direction direction, int x, int y, int width, int height, Color backgroundColor, Color selectedColor) {
    this.tileType = tileType;
    this.tileOrientation = orientation;
    this.tileDirection = direction;
    this.tileX = x;
    this.tileY = y;

    setLayout(null);
    Dimension d = new Dimension(width, height);
    setSize(d);
    setPreferredSize(d);

    //int w = getWidth();
    //int h = getHeight();
    this.renderWidth = RENDER_WIDTH;
    this.renderHeight = RENDER_HEIGHT;

    this.trackColor = DEFAULT_TRACK_COLOR;
    this.backgroundColor = backgroundColor;
    this.selectedColor = selectedColor;

    if (this.backgroundColor == null) {
      this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
    }
    if (this.selectedColor == null) {
      this.selectedColor = DEFAULT_SELECTED_COLOR;
    }
  }

  protected Tile(TileBean tileBean) {
    //this(tileBean, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    this(tileBean, tileWidth(tileBean.getOrientation(), tileBean.getTileType()), tileHeight(tileBean.getOrientation(), tileBean.getTileType()));
  }

  protected Tile(TileBean tileBean, int width, int height) {
    this.tileBean = tileBean;
    //Quick properties
    this.id = tileBean.getId();
    this.tileType = tileBean.getTileType();
    this.tileOrientation = tileBean.getOrientation();
    this.tileDirection = tileBean.getDirection();
    this.tileX = tileBean.getX();
    this.tileY = tileBean.getY();

    this.accessoryId = tileBean.getAccessoryId();
    this.accessoryBean = tileBean.getAccessoryBean();
    this.signalType = tileBean.getSignalType();

    this.sensorId = tileBean.getSensorId();
    this.sensorBean = tileBean.getSensorBean();
    this.blockBean = tileBean.getBlockBean();

    setLayout(null);
    Dimension d = new Dimension(width, height);
    setSize(d);
    setPreferredSize(d);

    this.trackColor = DEFAULT_TRACK_COLOR;
    this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
    this.selectedColor = DEFAULT_SELECTED_COLOR;
    this.renderWidth = RENDER_WIDTH;
    this.renderHeight = RENDER_HEIGHT;
  }

  protected static int tileWidth(Orientation orientation, TileType tileType) {
    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      if (null == tileType) {
        return DEFAULT_WIDTH;
      } else {
        return switch (tileType) {
          case BLOCK ->
            BLOCK_WIDTH;
          case CROSS ->
            DEFAULT_WIDTH * 2;
          default ->
            DEFAULT_WIDTH;
        };
      }
    } else {
      return DEFAULT_WIDTH;
    }
  }

  protected static int tileHeight(Orientation orientation, TileType tileType) {
    if (Orientation.EAST == orientation || Orientation.WEST == orientation) {
      return DEFAULT_HEIGHT;
    } else {
      if (null == tileType) {
        return DEFAULT_HEIGHT;
      } else {
        return switch (tileType) {
          case BLOCK ->
            BLOCK_HEIGHT;
          case CROSS ->
            DEFAULT_HEIGHT * 2;
          default ->
            DEFAULT_HEIGHT;
        };
      }
    }
  }

  protected void populateModel() {
    if (this.blockBean != null) {
      this.model.setBlockState(this.blockBean.getBlockState());
      this.model.setLocomotive(this.blockBean.getLocomotive());
      this.model.setArrivalSuffix(this.blockBean.getArrivalSuffix());
      this.model.setLogicalDirection(LocomotiveBean.Direction.get(this.blockBean.getLogicalDirection()));
    }
  }

  public TileBean getTileBean() {
    if (tileBean == null) {
      tileBean = new TileBean();
      tileBean.setId(this.id);
      tileBean.setX(this.tileX);
      tileBean.setY(this.tileY);
      tileBean.setTileType(this.tileType);
      tileBean.setTileOrientation(this.tileOrientation.getOrientation());
      tileBean.setTileDirection(this.tileDirection.getDirection());
      tileBean.setSignalType(this.signalType);
      tileBean.setAccessoryId(this.accessoryId);
      tileBean.setSensorId(this.sensorId);
      tileBean.setAccessoryBean(this.accessoryBean);
      tileBean.setSensorBean(this.sensorBean);
      tileBean.setBlockBean(this.blockBean);
    }
    return tileBean;
  }

  public boolean isSelected() {
    return model.isSelected();
  }

  public void setSelected(boolean b) {
    //boolean oldValue = isSelected();
    model.setSelected(b);
  }

  //@Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SignalType getSignalType() {
    return signalType;
  }

  public void setSignalType(SignalType signalType) {
    this.signalType = signalType;
  }

  public Integer getTileX() {
    return tileX;
  }

  public Integer getTileY() {
    return tileY;
  }

  public Point getCenter() {
    return new Point(this.tileX, this.tileY);
  }

  public void setCenter(Point center) {
    tileX = center.x;
    tileY = center.y;
    if (this.tileBean != null) {
      this.tileBean.setCenter(center);
    }
  }

  public Orientation getOrientation() {
    return tileOrientation;
  }

  public void setOrientation(Orientation orientation) {
    this.tileOrientation = orientation;
    if (tileBean != null) {
      tileBean.setOrientation(orientation);
    }
  }

  public Direction getDirection() {
    return tileDirection;
  }

  public void setDirection(Direction direction) {
    this.tileDirection = direction;
    if (this.tileBean != null) {
      this.tileBean.setDirection(direction);
    }
  }

  public String getAccessoryId() {
    return accessoryId;
  }

  public void setAccessoryId(String accessoryId) {
    this.accessoryId = accessoryId;
    if (this.tileBean != null) {
      this.tileBean.setAccessoryId(accessoryId);
    }
  }

  public String getSensorId() {
    return sensorId;
  }

  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }

  public boolean isActive() {
    return model.isSensorActive();
  }

  public void setActive(boolean active) {
    model.setSensorActive(active);
  }

  public BlockState getBlockState() {
    return model.getBlockState();
  }

  public void setBlockState(BlockState blockState) {
    if (blockBean != null) {
      blockBean.setBlockState(blockState);
      LocomotiveBean locomotive = model.getLocomotive();
      model.setOverlayImage(locomotive != null && locomotive.getLocIcon() != null && (blockState == BlockState.OCCUPIED || blockState == BlockState.INBOUND || blockState == BlockState.OUTBOUND));
    }
    model.setBlockState(blockState);
  }

  public String getDepartureSuffix() {
    return model.getDepartureSuffix();
  }

  public void setDepartureSuffix(String suffix) {
    if (blockBean != null) {
      blockBean.setDepartureSuffix(suffix);
    }
    model.setDepartureSuffix(suffix);
  }

  public boolean isReverseArrival() {
    return model.isReverseArrival();
  }

  public void setReverseArrival(boolean reverseArrival) {
    if (blockBean != null) {
      blockBean.setReverseArrival(reverseArrival);
    }
    model.setReverseArrival(reverseArrival);
  }

  public LocomotiveBean.Direction getLogicalDirection() {
    return model.getLogicalDirection();
  }

  public void setLogicalDirection(LocomotiveBean.Direction logicalDirection) {
    if (blockBean != null) {
      blockBean.setLogicalDirection(logicalDirection.getDirection());
    }
    model.setLogicalDirection(logicalDirection);
  }

  public LocomotiveBean getLocomotive() {
    return model.getLocomotive();
  }

  public void setLocomotive(LocomotiveBean locomotive) {
    if (blockBean != null) {
      blockBean.setLocomotive(locomotive);
      model.setOverlayImage(locomotive != null && locomotive.getLocIcon() != null && (model.getBlockState() == BlockState.OCCUPIED || model.getBlockState() == BlockState.INBOUND || model.getBlockState() == BlockState.OUTBOUND));
    }

    model.setLocomotive(locomotive);
  }

  public AccessoryBean getAccessoryBean() {
    return accessoryBean;
  }

  public void setAccessoryBean(AccessoryBean accessoryBean) {
    this.accessoryBean = accessoryBean;

    if (accessoryBean != null) {
      this.accessoryId = accessoryBean.getId();
      this.signalValue = accessoryBean.getSignalValue();
      this.signalType = SignalType.getSignalType(accessoryBean.getType());
    } else {
      this.accessoryId = null;
      this.signalType = SignalType.NONE;
      this.signalValue = AccessoryBean.SignalValue.OFF;
    }
  }

  public AccessoryValue getAccessoryValue() {
    if (this.accessoryValue == null) {
      return AccessoryValue.OFF;
    } else {
      return accessoryValue;
    }
  }

  public void setAccessoryValue(AccessoryValue value) {
    this.accessoryValue = value;
    repaint();
  }

  public AccessoryValue getRouteValue() {
    if (routeValue == null) {
      return AccessoryValue.OFF;
    } else {
      return routeValue;
    }
  }

  public void setRouteValue(AccessoryValue value) {
    this.routeValue = value;
    repaint();
  }

  public AccessoryBean.SignalValue getSignalValue() {
    return signalValue;
  }

  public void setSignalValue(AccessoryBean.SignalValue signalValue) {
    this.signalValue = signalValue;
    this.repaint();
  }

  public SensorBean getSensorBean() {
    return sensorBean;
  }

  public void setSensorBean(SensorBean sensorBean) {
    this.sensorBean = sensorBean;
  }

  public BlockBean getBlockBean() {
    return blockBean;
  }

  public void setBlockBean(BlockBean blockBean) {
    this.blockBean = blockBean;
  }

  public void setRenderWidth(int renderWidth) {
    this.renderWidth = renderWidth;
  }

  public void setRenderHeight(int renderHeight) {
    this.renderHeight = renderHeight;
  }

  public int getRenderOffsetX() {
    return renderOffsetX;
  }

  public void setRenderOffsetX(int renderOffsetX) {
    this.renderOffsetX = renderOffsetX;
  }

  public int getRenderOffsetY() {
    return renderOffsetY;
  }

  public void setRenderOffsetY(int renderOffsetY) {
    this.renderOffsetY = renderOffsetY;
  }

  public TileBean.TileType getTileType() {
    return this.tileType;
  }

  public final void setTileType(TileType tileType) {
    this.tileType = tileType;
  }

  public Color getTrackColor() {
    return trackColor;
  }

  public final void setTrackColor(Color trackColor) {
    this.trackColor = trackColor;
  }

  public Color getTrackRouteColor() {
    return trackRouteColor;
  }

  public void setTrackRouteColor(Color trackRouteColor) {
    this.trackRouteColor = trackRouteColor;
  }

  public Color getSelectedColor() {
    return selectedColor;
  }

  public void setSelectedColor(Color selectedColor) {
    this.selectedColor = selectedColor;
  }

  public Orientation getIncomingSide() {
    return incomingSide;
  }

  public void setIncomingSide(Orientation incomingSide) {
    this.incomingSide = incomingSide;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public boolean isDrawRoute() {
    return model.isShowRoute();
  }

  public void setDrawRoute(boolean drawRoute) {
    this.model.setShowRoute(drawRoute);
  }

  public int getRenderWidth() {
    return renderWidth;
  }

  public int getRenderHeight() {
    return renderHeight;
  }

  abstract void renderTile(Graphics2D g2d);

  abstract void renderTileRoute(Graphics2D g2d);

  /**
   * Draw the Tile
   *
   * @param g2d The graphics handle
   */
  public void drawTile(Graphics2D g2d) {
    // by default and image is rendered in the EAST orientation
    if (tileOrientation == null) {
      tileOrientation = Orientation.EAST;
    }

    BufferedImage bf = createImage();
    Graphics2D g2di = bf.createGraphics();

    //Avoid errors
    if (model.isShowRoute() && incomingSide == null) {
      incomingSide = getOrientation();
    }

    if (model.isSelected()) {
      g2di.setBackground(selectedColor);
    } else {
      g2di.setBackground(backgroundColor);
    }

    g2di.clearRect(0, 0, renderWidth, renderHeight);
    int ox = 0, oy = 0;

    AffineTransform trans = new AffineTransform();
    switch (tileOrientation) {
      case SOUTH -> {
        trans.rotate(Math.PI / 2, renderWidth / 2, renderHeight / 2);
        ox = (renderHeight - renderWidth) / 2;
        oy = (renderWidth - renderHeight) / 2;
        trans.translate(-ox, -oy);
      }
      case WEST -> {
        trans.rotate(Math.PI, renderWidth / 2, renderHeight / 2);
        trans.translate(ox, oy);
      }
      case NORTH -> {
        trans.rotate(-Math.PI / 2, renderWidth / 2, renderHeight / 2);
        ox = (renderHeight - renderWidth) / 2;
        oy = (renderWidth - renderHeight) / 2;
        trans.translate(-ox, -oy);
      }
      default -> {
        trans.rotate(0.0, this.renderWidth / 2, this.renderHeight / 2);
        trans.translate(ox, oy);
      }
    }

    //Logger.trace(tileOrientation.getOrientation() + " renderWidth: " + renderWidth + " renderHeight: " + renderHeight + " CP: (" + renderWidth / 2 + "," + renderHeight / 2 + ")");
    //Logger.trace(tileOrientation.getOrientation() + " ox: " + ox + " oy: " + oy);
    g2di.setTransform(trans);

    renderTile(g2di);

    if (model.isShowRoute()) {
      renderTileRoute(g2di);
    }

    if (model.isShowCenter()) {
      drawCenterPoint(g2di);
    }

    // Scale the image back...
    if (model.isScaleImage()) {
      tileImage = Scalr.resize(bf, Method.AUTOMATIC, Mode.FIT_EXACT, getWidth(), getHeight(), Scalr.OP_ANTIALIAS);
    } else {
      tileImage = bf;
    }

    g2di.dispose();
  }

  public BufferedImage getTileImage() {
    return tileImage;
  }

  /**
   * Render a tile image Always starts at (0,0) used the default width and height
   *
   * @param g2 the Graphic context
   */
  public void drawName(Graphics2D g2) {
  }

  protected void drawCenterPoint(Graphics2D g2d) {
    drawCenterPoint(g2d, Color.magenta);
  }

  protected void drawCenterPoint(Graphics2D g2, Color color) {
    drawCenterPoint(g2, color, 60);
  }

  protected void drawCenterPoint(Graphics2D g2d, Color color, double size) {
    double dX = (renderWidth / 2 - size / 2);
    double dY = (renderHeight / 2 - size / 2);

    g2d.setColor(color);
    g2d.fill(new Ellipse2D.Double(dX, dY, size, size));
  }

  /**
   * Rotate the tile clockwise 90 deg
   *
   * @return the new Orientation
   */
  public Orientation rotate() {
    switch (tileOrientation) {
      case EAST ->
        setOrientation(Orientation.SOUTH);
      case SOUTH ->
        setOrientation(Orientation.WEST);
      case WEST ->
        setOrientation(Orientation.NORTH);
      default ->
        setOrientation(Orientation.EAST);
    }
    return tileOrientation;
  }

  public void flipHorizontal() {
    if (Orientation.NORTH == tileOrientation || Orientation.SOUTH == tileOrientation) {
      rotate();
      rotate();
    }
  }

  public void flipVertical() {
    if (Orientation.EAST.equals(getOrientation()) || Orientation.WEST.equals(getOrientation())) {
      rotate();
      rotate();
    }
  }

  @Override
  public void move(int newX, int newY) {
    Point cs = LayoutUtil.snapToGrid(newX, newY);
    setCenter(cs);
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

  public Set<Point> getAltPoints() {
    return Collections.EMPTY_SET;
  }

  public final int getOffsetX() {
    return offsetX;
  }

  public void setOffsetX(int offsetX) {
    this.offsetX = offsetX;
  }

  public final int getOffsetY() {
    return offsetY;
  }

  public void setOffsetY(int offsetY) {
    this.offsetY = offsetY;
  }

  protected BufferedImage createImage() {
    return new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_RGB);
  }

  public int getCenterX() {
    if (tileX > 0) {
      return this.tileX;
    } else {
      return GRID;
    }
  }

  public int getCenterY() {
    if (tileY > 0) {
      return this.tileY;
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

  public boolean isScaleImage() {
    return model.isScaleImage();
  }

  public void setScaleImage(boolean scaleImage) {
    Dimension d;
    if (scaleImage) {
      d = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    } else {
      d = new Dimension(renderWidth, renderHeight);
    }

    setSize(d);
    setPreferredSize(d);

    model.setScaleImage(scaleImage);
  }

  public boolean isDrawCenterPoint() {
    return model.isShowCenter();
  }

  public void setDrawCenterPoint(boolean drawCenterPoint) {
    model.setShowCenter(drawCenterPoint);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
            + " {id: "
            + this.id
            + ", orientation: "
            + getOrientation()
            + ", direction: "
            + getDirection()
            + ", center: "
            + xyToString()
            + "}";
  }

//  @Override
//  public Rectangle getBounds() {
//    int w, h, cx, cy;
//    //TODO: Check this may by the componet does this already
//    if (this.getWidth() > 0 & this.getHeight() > 0) {
//      //if (this.width > 0 & this.height > 0) {
//      //w = this.width;
//      w = this.getPreferredSize().width;
//      //h = this.height;
//      h = this.getPreferredSize().height;
//    } else {
//      w = DEFAULT_WIDTH;
//      h = DEFAULT_HEIGHT;
//    }
//
//    if (this.tileX > 0 && this.tileY > 0) {
//      cx = this.tileX + this.offsetX;
//      cy = this.tileY + this.offsetY;
//    } else {
//      cx = w / 2;
//      cy = h / 2;
//    }
//
//    int ltx = cx - w / 2;
//    int lty = cy - h / 2;
//    return new Rectangle(ltx, lty, w, h);
//  }
//  public Rectangle2D getBounds2D() {
//    return getBounds().getBounds2D();
//  }
//  public boolean contains(double x, double y) {
//    int w, h, cx, cy, tlx, tly;
//    if (this.getWidth() > 0 & this.getHeight() > 0) {
//      //if (this.width > 0 & this.height > 0) {
////      w = this.width;
////      h = this.height;
//      w = this.getPreferredSize().width;
//      h = this.getPreferredSize().height;
//
//    } else {
//      w = DEFAULT_WIDTH;
//      h = DEFAULT_HEIGHT;
//    }
//
//    if (this.getWidth() > 0 & this.getHeight() > 0) {
//      //if (this.width > 0 & this.height > 0) {
//      cx = this.tileX;
//      cy = this.tileY;
//    } else {
//      cx = w / 2;
//      cy = h / 2;
//    }
//
//    // top left dX and dY
//    tlx = cx - w / 2;
//    tly = cy - h / 2;
//
//    // Check if X and Y range is ok
//    return !(x < tlx || x > (tlx + w) || y < tly || y > (tly + h));
//  }
  public String xyToString() {
    return "(" + this.tileX + "," + this.tileY + ")";
  }

//  public boolean contains(Point2D p) {
//    return this.contains(p.getX(), p.getY());
//  }
//  public boolean intersects(double x, double y, double w, double h) {
//    return getBounds().intersects(x, y, w, h);
//  }
//  public boolean intersects(Rectangle2D r2d) {
//    return getBounds().intersects(r2d);
//  }
//  public boolean contains(double x, double y, double w, double h) {
//    return getBounds().contains(x, y, w, h);
//  }
//  public boolean contains(Rectangle2D r2d) {
//    return getBounds().contains(r2d);
//  }
//  public PathIterator getPathIterator(AffineTransform at) {
//    return getBounds().getPathIterator(at);
//  }
//  public PathIterator getPathIterator(AffineTransform at, double flatness) {
//    return getBounds().getPathIterator(at, flatness);
//  }
//  @Deprecated
//  public PropertyChangeListener getPropertyChangeListener() {
//    return this.propertyChangeListener;
//  }
//  @Deprecated
//  public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
//    this.propertyChangeListener = propertyChangeListener;
//  }
  //@Override
//  @Deprecated
//  public void onTileChange(TileEvent tileEvent) {
//    Logger.warn("Deprecated! " + tileEvent.getTileId());
//    if (tileEvent.isEventFor(this)) {
//      boolean drawRoute = tileEvent.isShowRoute();
//      setIncomingSide(tileEvent.getIncomingSide());
//
//      if (isJunction()) {
//        //       setRouteValue(tileEvent.getRouteState());
//      }
//
//      if (tileEvent.getBlockBean() != null) {
//        this.setBlockBean(tileEvent.getBlockBean());
//      }
//
//      //if (tileEvent.getTileBean() != null) {
//      //  TileBean other = tileEvent.getTileBean();
//      //  this.copyInto(other);
//      //}
//      if (isBlock()) {
//        //      ((Block) this).setRouteBlockState(tileEvent.getBlockState());
//        if (!drawRoute) {
//          //        ((Block) this).setRouteBlockState(null);
//        }
//      }
//
//      setBackgroundColor(tileEvent.getBackgroundColor());
//      setTrackColor(tileEvent.getTrackColor());
//      setTrackRouteColor(tileEvent.getTrackRouteColor());
//
////      if (tileEvent.getBlockBean() != null) {
////        setBlockBean(tileEvent.getBlockBean());
////      }
////      repaintTile();
//    }
//  }
  /**
   * The main route of the tile is horizontal
   *
   * @return true when main route goes from East to West or vv
   */
  public boolean isHorizontal() {
    return (Orientation.EAST == tileOrientation || Orientation.WEST == tileOrientation) && TileType.CURVED != tileType;
  }

  /**
   * The main route of the tile is vertical
   *
   * @return true when main route goes from North to South or vv
   */
  public boolean isVertical() {
    return (Orientation.NORTH == tileOrientation || Orientation.SOUTH == tileOrientation) && TileType.CURVED != tileType;
  }

  public boolean isJunction() {
    return TileType.SWITCH == tileType || TileType.CROSS == tileType;
  }

  public boolean isBlock() {
    return TileType.BLOCK == tileType;
  }

  public boolean isDirectional() {
    return TileType.STRAIGHT_DIR == tileType;
  }

  /**
   * The main route of the tile is diagonal
   *
   * @return true when main route goes from North to East or West to South and vv
   */
  public boolean isDiagonal() {
    return TileType.CURVED == tileType;
  }

  public boolean isCrossing() {
    return TileType.CROSSING == tileType;
  }

  public List<TileBean> getNeighbours() {
    return neighbours;
  }

  public void setNeighbours(List<TileBean> neighbours) {
    this.neighbours = neighbours;
  }

  public String getIdSuffix(Tile other) {
    return "";
  }

  public Map<Point, Orientation> getNeighborOrientations() {
    Map<Point, Orientation> edgeOrientations = new HashMap<>();

    Map<Orientation, Point> neighborPoints = getNeighborPoints();

    for (Orientation o : Orientation.values()) {
      edgeOrientations.put(neighborPoints.get(o), o);
    }
    return edgeOrientations;
  }

  public Map<Point, Orientation> getEdgeOrientations() {
    Map<Point, Orientation> edgeOrientations = new HashMap<>();

    Map<Orientation, Point> edgeConnections = getEdgePoints();

    for (Orientation o : Orientation.values()) {
      edgeOrientations.put(edgeConnections.get(o), o);
    }
    return edgeOrientations;
  }

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

  /**
   * When the tile has a specific direction a train may travel, <br>
   * then this method will indicate whether the other tile is in on the side where the arrow is pointing to.
   *
   * @param other A Tile
   * @return true where other is on the side of this tile where the arrow points to
   */
  public boolean isArrowDirection(Tile other) {
    return true;
  }

  public AccessoryValue accessoryValueForRoute(Orientation from, Orientation to) {
    return AccessoryValue.OFF;
  }

  public abstract Map<Orientation, Point> getNeighborPoints();

  public abstract Map<Orientation, Point> getEdgePoints();

  public abstract Set<Point> getAllPoints();

  public TileModel getModel() {
    return model;
  }

  public void setModel(TileModel newModel) {
    TileModel oldModel = getModel();

    if (oldModel != null) {
      oldModel.removeChangeListener(changeListener);
      oldModel.removeActionListener(actionListener);
      changeListener = null;
      actionListener = null;
    }

    model = newModel;

    if (newModel != null) {
      changeListener = createChangeListener();
      actionListener = createActionListener();

      newModel.addChangeListener(changeListener);
      newModel.addActionListener(actionListener);
    }

    firePropertyChange(MODEL_CHANGED_PROPERTY, oldModel, newModel);
    if (newModel != oldModel) {
      revalidate();
      repaint();
    }
  }

//    public TileUI getUI() {
//        return (TileUI) ui;
//    }
//    public void setUI(TileUI ui) {
//        super.setUI(ui);
//    }
  @Override
  public void updateUI() {
  }

  protected ChangeListener createChangeListener() {
    return getHandler();
  }

  protected ActionListener createActionListener() {
    return getHandler();
  }

  private Handler getHandler() {
    if (handler == null) {
      handler = new Handler();
    }
    return handler;
  }

  class Handler implements ActionListener, ChangeListener, Serializable {

    @Override
    public void stateChanged(ChangeEvent e) {
      Object source = e.getSource();

      fireStateChanged();
      repaint();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      fireActionPerformed(event);
    }
  }

  protected void fireStateChanged() {
    Object[] listeners = listenerList.getListenerList();
    //reverse order
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        // Lazily create the event:
        if (changeEvent == null) {
          changeEvent = new ChangeEvent(this);
        }
        ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
      }
    }
  }

  protected void fireActionPerformed(ActionEvent event) {
    Object[] listeners = listenerList.getListenerList();
    ActionEvent e = null;
    // reverse
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ActionListener.class) {
        // Lazily create the event:
        if (e == null) {
          String actionCommand = event.getActionCommand();
          //if(actionCommand == null) {
          //   actionCommand = getActionCommand();
          //}
          e = new ActionEvent(Tile.this, ActionEvent.ACTION_PERFORMED, actionCommand, event.getWhen(), event.getModifiers());
        }
        ((ActionListener) listeners[i + 1]).actionPerformed(e);
      }
    }
  }

  public Rectangle getTileBounds() {
    return new Rectangle(tileX - GRID, tileY - GRID, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  @Override
  protected void paintComponent(Graphics g) {
    long started = System.currentTimeMillis();

    Graphics2D g2 = (Graphics2D) g.create();
    //Graphics2D g2 = (Graphics2D) g.create(tileX - GRID, tileY - GRID, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    drawTile(g2);
    g2.dispose();

    g.drawImage(tileImage, 0, 0, null);

    long now = System.currentTimeMillis();
    Logger.trace(id + " Duration: " + (now - started) + " ms. Tile (" + tileX + "," + tileY + ")");
  }

}
