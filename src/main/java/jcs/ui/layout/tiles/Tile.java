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

import jcs.ui.layout.tiles.ui.TileUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
 * Tile follows the MVC Patten, hence all properties which could change during operation<br>
 * which have an influence on the screen are in the TileModel.<br>
 * All Drawing code is in the TileUI.
 */
public abstract class Tile extends JComponent implements Serializable {

  public static final int GRID = 20;
  public static final int DEFAULT_WIDTH = GRID * 2;
  public static final int DEFAULT_HEIGHT = GRID * 2;

  public static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
  public static final Color DEFAULT_TRACK_COLOR = Color.lightGray;
  public static final Color DEFAULT_ROUTE_TRACK_COLOR = Color.darkGray;
  public static final Color DEFAULT_SELECTED_COLOR = Color.yellow;
  public static final Color DEFAULT_WARN_COLOR = Color.red;

  public static final String MODEL_CHANGED_PROPERTY = "model";
//  public static final String CONTENT_AREA_FILLED_CHANGED_PROPERTY = "contentAreaFilled";
  private static final long serialVersionUID = -8117888635518142366L;

  /**
   * The data model that determines the Tile state.
   */
  protected TileModel model = null;

  protected String id;
  protected Integer tileX;
  protected Integer tileY;

  protected Direction tileDirection;

  protected TileType tileType;
  protected String accessoryId;
  protected Integer sensorId;

  protected AccessoryValue routeValue;

  protected SignalType signalType;

  protected TileBean tileBean;
  protected AccessoryBean accessoryBean;
  protected SensorBean sensorBean;
  protected BlockBean blockBean;

  protected List<TileBean> neighbours;

  protected int renderOffsetX = 0;
  protected int renderOffsetY = 0;

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
    this.tileDirection = direction;
    this.tileX = x;
    this.tileY = y;

    setLayout(null);
    Dimension d = new Dimension(width, height);
    setSize(d);
    setPreferredSize(d);
  }

  protected Tile(TileBean tileBean) {
    this(tileBean, tileWidth(tileBean.getOrientation(), tileBean.getTileType()), tileHeight(tileBean.getOrientation(), tileBean.getTileType()));
  }

  protected Tile(TileBean tileBean, int width, int height) {
    this.tileBean = tileBean;
    id = tileBean.getId();
    tileType = tileBean.getTileType();
    tileDirection = tileBean.getDirection();
    tileX = tileBean.getX();
    tileY = tileBean.getY();

    accessoryId = tileBean.getAccessoryId();
    accessoryBean = tileBean.getAccessoryBean();

    sensorId = tileBean.getSensorId();
    sensorBean = tileBean.getSensorBean();
    blockBean = tileBean.getBlockBean();
    setLayout(null);
    Dimension d = new Dimension(width, height);
    setSize(d);
    setPreferredSize(d);
  }

  @Override
  public String getUIClassID() {
    return TileUI.UI_CLASS_ID;
  }

  @Override
  public TileUI getUI() {
    return (TileUI) ui;
  }

  public void setUI(TileUI ui) {
    super.setUI(ui);
  }

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
    if (accessoryBean != null) {
      setAccessoryBean(accessoryBean);
    }
    if (sensorBean != null) {
      setSensorBean(sensorBean);
    }

  }

  public TileBean getTileBean() {
    if (tileBean == null) {
      tileBean = new TileBean();
    }
    if (tileBean.getId() == null) {
      tileBean.setId(id);
    }

    tileBean.setTileType(tileType);
    tileBean.setX(tileX);
    tileBean.setY(tileY);
    tileBean.setTileOrientation(model.getTileOrienation().getOrientation());

    tileBean.setTileDirection(tileDirection.getDirection());
    tileBean.setSignalType(signalType);
    tileBean.setAccessoryId(accessoryId);
    tileBean.setSensorId(sensorId);
    tileBean.setAccessoryBean(accessoryBean);
    tileBean.setSensorBean(sensorBean);

    if (blockBean != null && blockBean.getId() == null) {
      blockBean.setId(id);
    }
    tileBean.setBlockBean(blockBean);

    return tileBean;
  }

  /**
   *
   * @return true when the Tile is selected in the screen
   */
  public boolean isSelected() {
    return model.isSelected();
  }

  public void setSelected(boolean b) {
    model.setSelected(b);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
    //ensure to set the id also in chained objects
    if (this.tileBean != null && this.tileBean.getId() == null) {
      this.tileBean.setId(id);
    }
    if (this.blockBean != null && this.blockBean.getId() == null) {
      this.blockBean.setId(id);
    }
    if (this.blockBean != null && this.blockBean.getTileId() == null) {
      this.blockBean.setTileId(id);
    }
  }

  public SignalType getSignalType() {
    return signalType;
  }

  public void setSignalType(SignalType signalType) {
    this.signalType = signalType;
  }

  /**
   *
   * @return the Tile X coordinate on the screen.
   */
  public Integer getTileX() {
    return tileX;
  }

  /**
   *
   * @return the Tile Y coordinate on the screen
   */
  public Integer getTileY() {
    return tileY;
  }

  /**
   *
   * @return the Tile X and Y coordinates on the screen.
   */
  public Point getCenter() {
    return new Point(this.tileX, this.tileY);
  }

  public void setCenter(Point center) {
    tileX = center.x;
    tileY = center.y;
    if (tileBean != null) {
      tileBean.setCenter(center);
    }
  }

  /**
   *
   * @return The Tile Orientation. The default orientation is East or from left to right
   */
  public Orientation getOrientation() {
    return model.getTileOrienation();
  }

  public void setOrientation(Orientation orientation) {
    model.setTileOrienation(orientation);
    if (tileBean != null) {
      tileBean.setOrientation(orientation);
    }
  }

  public Direction getDirection() {
    return tileDirection;
  }

  public void setDirection(Direction direction) {
    this.tileDirection = direction;
    if (tileBean != null) {
      tileBean.setDirection(direction);
    }
  }

  public String getAccessoryId() {
    return accessoryId;
  }

  public void setAccessoryId(String accessoryId) {
    this.accessoryId = accessoryId;
    if (tileBean != null) {
      tileBean.setAccessoryId(accessoryId);
    }
  }

  public Integer getSensorId() {
    return sensorId;
  }

  public void setSensorId(Integer sensorId) {
    this.sensorId = sensorId;
  }

  public boolean isActive() {
    return model.isSensorActive();
  }

  public void setActive(boolean active) {
    model.setSensorActive(active);
    if (this.sensorBean != null) {
      this.sensorBean.setActive(active);
    }
  }

  public BlockState getBlockState() {
    return model.getBlockState();
  }

  public void setBlockState(BlockState blockState) {
    model.setBlockState(blockState);
    if (blockBean != null) {
      blockBean.setBlockState(blockState);
    } else {
      Logger.warn("Blockbean for " + id + " is NOT set!");
    }
  }

  public String getDepartureSuffix() {
    return model.getDepartureSuffix();
  }

  public void setDepartureSuffix(String suffix) {
    model.setDepartureSuffix(suffix);
    if (blockBean != null) {
      blockBean.setDepartureSuffix(suffix);
    } else {
      Logger.warn("Blockbean for " + id + " is NOT set!");
    }
  }

  public boolean isReverseArrival() {
    return model.isReverseArrival();
  }

  public void setReverseArrival(boolean reverseArrival) {
    model.setReverseArrival(reverseArrival);
    if (blockBean != null) {
      blockBean.setReverseArrival(reverseArrival);
    } else {
      Logger.warn("Blockbean for " + id + " is NOT set!");
    }
  }

  public LocomotiveBean.Direction getLogicalDirection() {
    return model.getLogicalDirection();
  }

  public void setLogicalDirection(LocomotiveBean.Direction logicalDirection) {
    model.setLogicalDirection(logicalDirection);
    if (blockBean != null) {
      blockBean.setLogicalDirection(logicalDirection.getDirection());
    } else {
      Logger.warn("Blockbean for " + id + " is NOT set!");
    }
  }

  public LocomotiveBean getLocomotive() {
    return model.getLocomotive();
  }

  public void setLocomotive(LocomotiveBean locomotive) {
    model.setLocomotive(locomotive);
    if (blockBean != null) {
      blockBean.setLocomotive(locomotive);
    } else {
      Logger.warn("Blockbean for " + id + " is NOT set!");
    }
  }

  public String getArrivalSuffix() {
    return model.getArrivalSuffix();
  }

  public void setArrivalSuffix(String arrivalSuffix) {
    model.setArrivalSuffix(arrivalSuffix);
    if (blockBean != null) {
      blockBean.setArrivalSuffix(arrivalSuffix);
    } else {
      Logger.warn("Blockbean for " + id + " is NOT set!");
    }
  }

  public AccessoryBean getAccessoryBean() {
    return accessoryBean;
  }

  public void setAccessoryBean(AccessoryBean accessoryBean) {
    this.accessoryBean = accessoryBean;
    if (accessoryBean != null) {
      accessoryId = accessoryBean.getId();
      if (accessoryBean.isSignal()) {
        signalType = SignalType.getSignalType(accessoryBean.getType());
        model.setSignalValue(accessoryBean.getSignalValue());
      } else if (accessoryBean.isTurnout()) {
        model.setAccessoryValue(accessoryBean.getAccessoryValue());
      } else {
        model.setAccessoryValue(AccessoryValue.OFF);
      }
    } else {
      accessoryId = null;
      signalType = SignalType.NONE;
      model.setAccessoryValue(AccessoryValue.OFF);
    }
  }

  public void setAccessoryValue(AccessoryValue value) {
    model.setAccessoryValue(value);
    if (accessoryBean != null) {
      accessoryBean.setAccessoryValue(value);
    }
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

  public void setSignalValue(AccessoryBean.SignalValue signalValue) {
    model.setSignalValue(signalValue);
    if (this.accessoryBean != null) {
      this.accessoryBean.setSignalValue(signalValue);
    }
  }

  public SensorBean getSensorBean() {
    return sensorBean;
  }

  public void setSensorBean(SensorBean sensorBean) {
    this.sensorBean = sensorBean;
    if (sensorBean != null) {
      sensorId = sensorBean.getId();
      model.setSensorActive(sensorBean.isActive());
    } else {
      model.setSensorActive(false);
      sensorId = null;
    }
  }

  public BlockBean getBlockBean() {
    return blockBean;
  }

  public void setBlockBean(BlockBean blockBean) {
    this.blockBean = blockBean;
    this.model.setBlockBean(blockBean);
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

  public void setTrackColor(Color trackColor) {
    if (getUI() != null) {
      getUI().setTrackColor(trackColor);
    }
  }

  public void setTrackRouteColor(Color trackRouteColor) {
    if (getUI() != null) {
      getUI().setTrackRouteColor(trackRouteColor);
    }
  }

  public Color getSelectedColor() {
    return model.getSelectedColor();
  }

  public void setSelectedColor(Color selectedColor) {
    model.setSelectedColor(selectedColor);
  }

  public Orientation getIncomingSide() {
    return model.getIncomingSide();
  }

  public void setIncomingSide(Orientation incomingSide) {
    model.setIncomingSide(incomingSide);
  }

  public boolean isShowRoute() {
    return model.isShowRoute();
  }

  public void setShowRoute(boolean drawRoute) {
    this.model.setShowRoute(drawRoute);
  }

  /**
   *
   * @return a Map of points which are adjacent of the Tile<br>
   * The key is the location with respect to this tile, i.e. is the neighbor point o the East (right side)><br>
   * on the west side (on the left) or on the top (north) or bottom (south).
   *
   */
  public abstract Map<Orientation, Point> getNeighborPoints();

  public abstract Map<Orientation, Point> getEdgePoints();

  Set<Point> getAltPoints(Point center) {
    return Collections.<Point>emptySet();
  }

  public Set<Point> getAllPoints() {
    return getAllPoints(getCenter());
  }

  Set<Point> getAllPoints(Point center) {
    Set<Point> aps = new HashSet<>();
    aps.add(center);
    return aps;
  }

  /**
   * Rotate the tile clockwise 90 deg
   *
   * @return the new Orientation
   */
  public Orientation rotate() {
    Orientation tileOrientation = model.getTileOrienation();
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
    return model.getTileOrienation();
  }

  public void flipHorizontal() {
    Orientation tileOrientation = model.getTileOrienation();
    if (Orientation.NORTH == tileOrientation || Orientation.SOUTH == tileOrientation) {
      rotate();
      rotate();
    }
  }

  public void flipVertical() {
    Orientation tileOrientation = model.getTileOrienation();
    if (Orientation.EAST == tileOrientation || Orientation.WEST == tileOrientation) {
      rotate();
      rotate();
    }
  }

  public void moveTile(int newX, int newY) {
    Point cs = LayoutUtil.snapToGrid(newX, newY);
    setCenter(cs);
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
    return Collections.<Point>emptySet();
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
      int renderWidth = getUI().getRenderWidth();
      int renderHeight = getUI().getRenderHeight();

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

  public String xyToString() {
    return "(" + this.tileX + "," + this.tileY + ")";
  }

  /**
   * The main route of the tile is horizontal
   *
   * @return true when main route goes from East to West or vv
   */
  public boolean isHorizontal() {
    Orientation tileOrientation = model.getTileOrienation();
    return (Orientation.EAST == tileOrientation || Orientation.WEST == tileOrientation) && TileType.CURVED != tileType;
  }

  /**
   * The main route of the tile is vertical
   *
   * @return true when main route goes from North to South or vv
   */
  public boolean isVertical() {
    Orientation tileOrientation = model.getTileOrienation();
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

  class Handler implements ActionListener, ChangeListener {

    @Override
    public void stateChanged(ChangeEvent e) {
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
    if (model.isScaleImage()) {
      return new Rectangle(tileX - GRID, tileY - GRID, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    } else {
      int renderWidth = getUI().getRenderWidth();
      int renderHeight = getUI().getRenderHeight();

      return new Rectangle(tileX - renderWidth / 2, tileY - renderHeight / 2, renderWidth, renderHeight);
    }
  }
}
