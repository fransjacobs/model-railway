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

    void setRepaintListener(RepaintListener listener);

}
