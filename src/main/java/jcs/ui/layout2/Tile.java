/*
 * Copyright (C) 2021 frans.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.ui.layout2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.util.Set;
import jcs.entities.TileBean;
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

    //Point snapToGrid(Point p);
    //Point snapToGrid(int x, int y);
    //BufferedImage flipHorizontally(BufferedImage source);
    //BufferedImage flipVertically(BufferedImage source);
    Orientation getOrientation();

    void setOrientation(Orientation orientation);

    Direction getDirection();

    void setDirection(Direction direction);

    Point getCenter();

    void setCenter(Point center);

    Set<Point> getAltPoints();

    Set<Point> getAllPoints();

    int getOffsetX();

    void setOffsetX(int offsetX);

    int getOffsetY();

    void setOffsetY(int offsetY);

    int getHeight();

    int getWidth();

    int getCenterX();

    int getCenterY();

    TileBean getTileBean();

    void setTileBean(TileBean tileBean);

    //boolean isDrawName();
    //void setDrawName(boolean drawName);
    boolean isDrawOutline();

    void setDrawOutline(boolean drawOutline);

    TileType getTileType();

}
