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
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import jcs.entities.TileBean.TileType;

/**
 * @author frans
 */
public interface Tile extends Shape {

  public static final int GRID = 20;
  public static final int DEFAULT_WIDTH = GRID * 2;
  public static final int DEFAULT_HEIGHT = GRID * 2;

  static final int RENDER_GRID = GRID * 10;
  static final int RENDER_WIDTH = RENDER_GRID * 2;
  static final int RENDER_HEIGHT = RENDER_GRID * 2;

  public static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
  public static final Color DEFAULT_TRACK_COLOR = Color.lightGray;
  public static final Color DEFAULT_ROUTE_TRACK_COLOR = Color.blue;

  boolean isDrawRoute();

  void setDrawRoute(boolean drawRoute);

  Color getTrackColor();

  void setTrackColor(Color trackColor);

  Color getTrackRouteColor();

  void setTrackRouteColor(Color trackRouteColor);

  Orientation getIncomingSide();

  void setIncomingSide(Orientation incomingSide);

  Color getBackgroundColor();

  void setBackgroundColor(Color backgroundColor);

  String getId();

  void setId(String id);

  //String getImageKey();
  
  BufferedImage getTileImage();

  void drawTile(Graphics2D g2d, boolean drawOutline);

  void renderTile(Graphics2D g2d);

  void renderTileRoute(Graphics2D g2d);

  void drawName(Graphics2D g2);

  void drawCenterPoint(Graphics2D g2d);

  void drawCenterPoint(Graphics2D g2, Color color);

  void drawCenterPoint(Graphics2D g2d, Color color, double size);

  void drawBounds(Graphics2D g2d);

  void rotate();

  void flipHorizontal();

  void flipVertical();

  void move(int newX, int newY);

  TileBean.Orientation getOrientation();

  void setOrientation(TileBean.Orientation orientation);

  Direction getDirection();

  void setDirection(Direction direction);

  Point getCenter();

  void setCenter(Point center);

  /**
   * @return a Set of alternative points in case the tile is not a square
   */
  Set<Point> getAltPoints();

  /**
   * @return All points relevant for the Object on the Canvas
   */
  Set<Point> getAllPoints();

  int getOffsetX();

  void setOffsetX(int offsetX);

  int getOffsetY();

  void setOffsetY(int offsetY);

  int getHeight();

  int getWidth();

  /**
   * @return the X (pixel) coordinate of the center of the tile
   */
  int getCenterX();

  /**
   * @return then Y (pixel) coordinate of the center of the tile
   */
  int getCenterY();

  TileBean getTileBean();

  boolean isDrawOutline();

  void setDrawOutline(boolean drawOutline);

  TileType getTileType();

  void setPropertyChangeListener(PropertyChangeListener listener);

  String xyToString();

  /**
   * @return the X number of the grid square (grid is 40 x 40 pix)
   */
  int getGridX();

  /**
   * @return the Y number of the grid square (grid is 40 x 40 pix)
   */
  int getGridY();

  /**
   * The main route of the tile is horizontal
   *
   * @return true when main route goes from East to West or vv
   */
  boolean isHorizontal();

  /**
   * The main route of the tile is vertical
   *
   * @return true when main route goes from North to South or vv
   */
  boolean isVertical();

  /**
   * The main route of the tile is diagonal
   *
   * @return true when main route goes from North to East or West to South and vv
   */
  boolean isDiagonal();

  boolean isJunction();

  boolean isBlock();

  boolean isDirectional();

  boolean isCrossing();

  Map<Orientation, Point> getNeighborPoints();

  Map<Point, Orientation> getNeighborOrientations();

  Map<Orientation, Point> getEdgePoints();

  Map<Point, Orientation> getEdgeOrientations();

  AccessoryValue accessoryValueForRoute(Orientation from, Orientation to);

  /**
   * @param other a tile to check with this tile
   * @return true when the other tile is adjacent to this and the "tracks" connect
   */
  boolean isAdjacent(Tile other);

  String getIdSuffix(Tile other);

  /**
   * When the tile has a specific direction a train may travel then this method will indicate whether the other tile is in on the side where the arrow is pointing to
   *
   * @param other A Tile
   * @return true where other is on the side of this tile where the arrow points to
   */
  boolean isArrowDirection(Tile other);
}
