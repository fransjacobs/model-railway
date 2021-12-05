/*
 * Copyright (C) 2019 Frans Jacobs.
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
package lan.wervel.jcs.ui.layout2.tiles2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lan.wervel.jcs.entities.TileBean;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.entities.enums.SignalType;
import lan.wervel.jcs.entities.enums.TileType;
import lan.wervel.jcs.ui.layout2.router.GraphNode;
import org.tinylog.Logger;

/**
 * Basic graphic element to display a track, turnout, etc on the screen. By
 * default the drawing of a Tile is Horizontal from L to R or West to East.
 * Default orientation is East
 *
 * The default size of a Tile is 40 x 40 pixels. The center point of a Tile is
 * stored and always snapped to the nearest grid point. The basic grid is 20x 20
 * pixels.
 *
 * A Tile can be rotated (always clockwise). Rotation will change the
 * orientation from East -> South -> West -> North -> East.
 *
 * A Tile is rendered to a Buffered Image to speed up the display
 *
 */
public abstract class AbstractTile2 implements Shape, GraphNode {

    public static final int GRID = 20;
    public static final int DEFAULT_WIDTH = GRID * 2;
    public static final int DEFAULT_HEIGHT = GRID * 2;

    protected Orientation orientation;
    protected Direction direction;

    protected BufferedImage image;

    protected Point center;

    protected int width;
    protected int height;
    protected int offsetX = 0;
    protected int offsetY = 0;

    protected TileBean tileBean;

    protected Color trackColor;
    protected Color backgroundColor;
    protected boolean drawName = true;

    protected String id;

    protected boolean drawOutline = false;
    
    protected Set<AbstractTile2> adjacentTiles;

    public final static Color DEFAULT_TRACK_COLOR = Color.lightGray;

    protected AbstractTile2(Point center) {
        this(Orientation.EAST, Direction.CENTER, center);
    }

    protected AbstractTile2(Orientation orientation, Point center) {
        this(orientation, Direction.CENTER, center);
    }

    protected AbstractTile2(Orientation orientation, Direction direction, Point center) {
        this(orientation, direction, center, null);
    }

    protected AbstractTile2(Orientation orientation, Direction direction, Point center, Color backgroundColor) {
        this.orientation = orientation;
        this.direction = direction;
        this.center = center;
        this.trackColor = DEFAULT_TRACK_COLOR;
        this.backgroundColor = backgroundColor;
        if (this.backgroundColor == null) {
            this.backgroundColor = Color.white;
        }
        this.adjacentTiles = new HashSet<>();
        init();
    }

    private void init() {
        this.id = getNewId();
    }

    protected AbstractTile2(TileBean tileBean) {
        setTileBean(tileBean);
        this.trackColor = DEFAULT_TRACK_COLOR;
        this.backgroundColor = Color.white;
    }

    public Color getTrackColor() {
        return trackColor;
    }

    public final void setTrackColor(Color trackColor) {
        if (!this.trackColor.equals(trackColor)) {
            this.trackColor = trackColor;
            this.image = null;
        }
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (!this.backgroundColor.equals(backgroundColor)) {
            this.backgroundColor = backgroundColor;
            this.image = null;
        }
    }

    protected abstract String getNewId();

    abstract void setIdSeq(int id);

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Draw the AbstractTile
     *
     * @param g2d The graphics handle
     * @param drawOutline
     */
    public void drawTile(Graphics2D g2d, boolean drawOutline) {
        if (image == null) {
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

            //by default and image is rendered in the EAST orientation
            Orientation o = orientation;
            if (o == null) {
                o = Orientation.EAST;
            }

            g2di.setBackground(backgroundColor);
            g2di.clearRect(0, 0, this.width, this.height);

            int ox = 0, oy = 0;

            switch (o) {
                case SOUTH:
                    trans.rotate(Math.PI / 2, getWidth() / 2, getHeight() / 2);
                    ox = (this.height - this.width) / 2;
                    oy = (this.width - this.height) / 2;
                    trans.translate(-ox, -oy);
                    break;
                case WEST:
                    trans.rotate(Math.PI, getWidth() / 2, getHeight() / 2);
                    trans.translate(ox, oy);
                    break;
                case NORTH:
                    trans.rotate(-Math.PI / 2, getWidth() / 2, getHeight() / 2);
                    ox = (this.height - this.width) / 2;
                    oy = (this.width - this.height) / 2;
                    trans.translate(-ox, -oy);
                    break;
                default:
                    trans.rotate(0.0, getWidth() / 2, getHeight() / 2);
                    trans.translate(ox, oy);
                    break;
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

            image = bi;
            g2di.setTransform(backup);

            g2di.dispose();
        }

        g2d.drawImage(image, (center.x - image.getWidth() / 2) + this.offsetX, (center.y - image.getHeight() / 2) + this.offsetY, null);
    }

    /**
     * Render a tile image Always starts at (0,0) used the default width and
     * height
     *
     * @param g2d the Graphic context
     */
    abstract void renderTile(Graphics2D g2d, Color trackColor, Color backgroundColor);

    public void drawName(Graphics2D g2) {

    }

    public void drawCenterPoint(Graphics2D g2d) {
        drawCenterPoint(g2d, Color.GRAY);
    }

    public void drawCenterPoint(Graphics2D g2, Color color) {
        drawCenterPoint(g2, color, 4);
    }

    public void drawCenterPoint(Graphics2D g2d, Color color, double size) {
        double x = (this.center.x - size / 2);
        double y = (this.center.y - size / 2);

        g2d.setColor(color);
        g2d.fill(new Ellipse2D.Double(x, y, size, size));

        if (!getAltPoints().isEmpty()) {
            //Also draw the alt points
            Set<Point> alt = new HashSet<>(getAltPoints());

            for (Point ap : alt) {
                g2d.fill(new Ellipse2D.Double(ap.x, ap.y, size - 1, size - 1));
            }
        }
    }

    public void drawBounds(Graphics2D g2d) {
        g2d.setColor(Color.yellow);
        g2d.draw(getBounds());
    }

    /**
     * Rotate the tile clockwise 90 deg
     */
    public void rotate() {
        switch (this.orientation) {
            case EAST:
                setOrientation(Orientation.SOUTH);
                break;
            case SOUTH:
                setOrientation(Orientation.WEST);
                break;
            case WEST:
                setOrientation(Orientation.NORTH);
                break;
            default:
                setOrientation(Orientation.EAST);
                break;
        }
    }

    public void flipHorizontal() {
        if (Orientation.NORTH.equals(this.orientation) || Orientation.SOUTH.equals(this.orientation)) {
            rotate();
            rotate();
        }
    }

    public void flipVertical() {
        if (Orientation.EAST.equals(this.orientation) || Orientation.WEST.equals(this.orientation)) {
            rotate();
            rotate();
        }
    }

    public void move(int newX, int newY) {
        Point cs = snapToGrid(newX, newY);
        this.setCenter(cs);
    }

    public static final Point snapToGrid(Point p) {
        return snapToGrid(p.x, p.y);
    }

    /**
     * Snap coordinates to the nearest grid point
     *
     * @param x the X
     * @param y the Y
     * @return Coordinates which are the X en Y wrapped
     */
    public static final Point snapToGrid(int x, int y) {
        int steps = x / DEFAULT_WIDTH;
        int sx = steps;
        sx = sx * DEFAULT_WIDTH + GRID;

        steps = y / DEFAULT_HEIGHT;
        int sy = steps;
        sy = sy * DEFAULT_HEIGHT + GRID;

        return new Point(sx, sy);
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

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (!this.orientation.equals(orientation)) {
            //image is cached, so remove will be created again
            image = null;
        }
        this.orientation = orientation;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (!this.direction.equals(direction)) {
            //image is cached, so remove will be created again
            image = null;
        }
        this.direction = direction;
    }

    public Point getCenter() {
        return this.center;
    }

    public void setCenter(Point center) {
        if (!this.center.equals(center)) {
            //image is cached, so remove will be created again
            image = null;
        }
        this.center = center;

    }

    public Set<Point> getAltPoints() {
        return Collections.EMPTY_SET;
    }

    public Set<Point> getAllPoints() {
        Set<Point> aps = new HashSet<>();
        aps.add(center);
        aps.addAll(this.getAltPoints());
        return aps;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    protected BufferedImage createImage() {
        return new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getCenterX() {
        int cx;
        if (this.center != null) {
            cx = this.center.x;
        } else {
            int w;
            if (this.width > 0) {
                w = this.width;
            } else {
                w = DEFAULT_WIDTH;
            }
            cx = w / 2;
        }
        return cx;
    }

    public int getCenterY() {
        int cy;
        if (this.center != null) {
            cy = this.center.y;
        } else {
            int h;
            if (this.height > 0) {
                h = this.height;
            } else {
                h = DEFAULT_HEIGHT;
            }
            cy = h / 2;
        }
        return cy;
    }

    public TileBean getTileBean() {
        if (tileBean == null) {
            Logger.trace("Create new TileBean for: " + getClass().getName());
            TileType tileType = TileType.get(getClass().getSimpleName());
            SignalType signalType = null;
            if (this instanceof Signal) {
                signalType = ((Signal) this).getSignalType();
            }

            tileBean = new TileBean(tileType, orientation, direction, center, id, signalType);
        } else {
            //Synchronize the bean
            this.tileBean.setOrientation(orientation);
            this.tileBean.setDirection(direction);
            this.tileBean.setCenter(center);
            if (this instanceof Signal) {
                this.tileBean.setSignalType(((Signal) this).getSignalType());
            }
        }

        return tileBean;
    }

    public final void setTileBean(TileBean tileBean) {
        this.tileBean = tileBean;
        this.orientation = tileBean.getOrientation();
        this.direction = tileBean.getDirection();
        this.center = tileBean.getCenter();

        if (tileBean.getTileType().equals(TileType.SIGNAL)) {
            ((Signal) this).setSignalType(tileBean.getSignalType());
        }
        this.id = tileBean.getId();
    }

    public boolean isDrawName() {
        return drawName;
    }

    public void setDrawName(boolean drawName) {
        this.drawName = drawName;
    }

    public boolean isDrawOutline() {
        return drawOutline;
    }

    public void setDrawOutline(boolean drawOutline) {
        if (this.drawOutline != drawOutline) {
            this.drawOutline = drawOutline;
            this.image = null;
        }
    }

//    public void setReDrawListener(ReDrawListener listener) {
//        this.reDrawListener = listener;
//    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.getClass().getSimpleName());
        hash = 59 * hash + Objects.hashCode(this.center);
        hash = 59 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!Objects.equals(getClass(), obj.getClass())) {
            return false;
        }
        final AbstractTile2 other = (AbstractTile2) obj;
        if (this.orientation != other.orientation) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        if (Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.center, other.center);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {id: " + id + ", orientation: " + orientation + ", direction: " + direction + ", center: " + center + "}";
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

        if (this.center != null) {
            cx = this.center.x + this.offsetX;
            cy = this.center.y + this.offsetY;
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

        if (this.center != null) {
            cx = this.center.x;
            cy = this.center.y;
        } else {
            cx = w / 2;
            cy = h / 2;
        }

        //top left x and y
        tlx = cx - w / 2;
        tly = cy - h / 2;

        //Check if X and Y range is ok
        return !(x < tlx || x > (tlx + w) || y < tly || y > (tly + h));
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

    public Set<AbstractTile2> getAdjacentTiles() {
        return adjacentTiles;
    }

    public void setAdjacentTiles(Set<AbstractTile2> adjacentTiles) {
        this.adjacentTiles = adjacentTiles;
    }

    
    public Set<Point> getAdjacentPoints() {
        Set<Point> adjacent = new HashSet<>();

//        } else if (this instanceof Cross) {
//            //TODO Cros is asymetrycal
//            int offsetX = tile.getWidth() / 2 + TileBean.DEFAULT_WIDTH / 2;
//            int offsetY = tile.getHeight() / 2 + TileBean.DEFAULT_HEIGHT / 2;
//}    
        if (Orientation.EAST.equals(this.orientation) || Orientation.WEST.equals(this.orientation)) {
            int oX = this.width / 2 + DEFAULT_WIDTH / 2;
            adjacent.add(new Point(this.getCenterX() + oX, this.getCenterY()));
            adjacent.add(new Point(this.getCenterX() - oX, this.getCenterY()));
        } else {
            int oY = this.height / 2 + DEFAULT_HEIGHT / 2;
            adjacent.add(new Point(this.getCenterX(), this.getCenterY() + oY));
            adjacent.add(new Point(this.getCenterX(), this.getCenterY() - oY));
        }
        return adjacent;
    }

}
