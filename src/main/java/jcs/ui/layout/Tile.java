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
package jcs.ui.layout;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import jcs.ui.layout.tiles.enums.Direction;

/**
 *
 * @author frans
 */
public interface Tile extends Shape {

  public static final int GRID = 20;
  public static final int DEFAULT_WIDTH = GRID * 2;
  public static final int DEFAULT_HEIGHT = GRID * 2;

  public final static Color DEFAULT_TRACK_COLOR = Color.lightGray;

  Color getTrackColor();

  void setTrackColor(Color trackColor);

  Color getBackgroundColor();

  void setBackgroundColor(Color backgroundColor);

  String getId();

  void setId(String id);

  void drawTile(Graphics2D g2d, boolean drawOutline);

  void renderTile(Graphics2D g2d, Color trackColor, Color backgroundColor);

  void drawName(Graphics2D g2);

  void drawCenterPoint(Graphics2D g2d);

  void drawCenterPoint(Graphics2D g2, Color color);

  void drawCenterPoint(Graphics2D g2d, Color color, double size);

  void drawBounds(Graphics2D g2d);

  void rotate();

  void flipHorizontal();

  void flipVertical();

  void move(int newX, int newY);

  Orientation getOrientation();

  void setOrientation(Orientation orientation);

  Direction getDirection();

  void setDirection(Direction direction);

  Point getCenter();

  void setCenter(Point center);

  /**
   *
   * @return a Set of alternative "center" points in case the tile is not a square
   */
  Set<Point> getAltPoints();

  /**
   *
   * @return All point relevant for the Object on the Canvas
   */
  Set<Point> getAllPoints();
  
  
  int getOffsetX();

  void setOffsetX(int offsetX);

  int getOffsetY();

  void setOffsetY(int offsetY);

  int getHeight();

  int getWidth();

  /**
   *
   * @return the X (pixel) coordinate of the center of the tile
   */
  int getCenterX();

  /**
   *
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
   *
   * @return the X number of the grid square (grid is 40 x 40 pix)
   */
  int getGridX();

  /**
   *
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

  Map<Orientation, Point> getNeighborPoints();

  Map<Point, Orientation> getNeighborOrientations();

  Map<Orientation, Point> getEdgePoints();

  Map<Point, Orientation> getEdgeOrientations();

  /**
   *
   * @param other a tile to check with this tile
   * @return true when the other tile is adjacent to this and the "tracks" connect
   */
  boolean isAdjacent(Tile other);

  String getIdSuffix(Tile other);

  AccessoryValue getSwitchValueTo(Tile other);

  /**
   * When the Tile is a Turnout then the switch side is the side of the tile which is the "central" point. From the switch side a Green or Red path is possible.
   *
   * @param other A Tile
   * @return true when other is connected to the switch side of the Turnout
   */
  boolean isSwitchSide(Tile other);

  /**
   * When the Tile is a Turnout then the diverging side is the "limp" side of the tile. From the diverging side a Red path is possible.
   *
   * @param other A Tile
   * @return true when other is connected to the diverging side of the Turnout
   */
  boolean isDivergingSide(Tile other);

  /**
   * When the Tile is a Turnout then the Straight side is the "through" side of the tile. From the Straight side a Green path is possible.
   *
   * @param other A Tile
   * @return true when other is connected to the straight side of the Turnout
   */
  boolean isStraightSide(Tile other);

}
