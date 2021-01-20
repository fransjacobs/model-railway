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
package lan.wervel.jcs.ui.layout.tiles;

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
import java.util.Objects;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.ui.layout.ReDrawListener;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.entities.enums.TileType;
import org.pmw.tinylog.Logger;

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
public abstract class AbstractTile implements Shape {

    public static final int MIN_GRID = 20;
    public static final int DEFAULT_WIDTH = MIN_GRID * 2;
    public static final int DEFAULT_HEIGHT = MIN_GRID * 2;

    protected Orientation orientation;
    protected Direction direction;

    protected BufferedImage image;

    protected Point center;

    protected int width;
    protected int height;

    protected LayoutTile layoutTile;

    protected Color trackColor;
    protected boolean drawName = true;

    protected ReDrawListener reDrawListener;

    public final static Color DEFAULT_TRACK_COLOR = Color.lightGray;

    protected AbstractTile(Point center) {
        this(Orientation.EAST, Direction.CENTER, center);
    }

    protected AbstractTile(Orientation orientation, Point center) {
        this(orientation, Direction.CENTER, center);
    }

    protected AbstractTile(Orientation orientation, Direction direction, Point center) {
        this.orientation = orientation;
        this.direction = direction;
        this.center = center;
        this.trackColor = DEFAULT_TRACK_COLOR;
    }

    protected AbstractTile(LayoutTile layoutTile) {
        setLayoutTile(layoutTile);
        this.trackColor = DEFAULT_TRACK_COLOR;
    }

    public Color getTrackColor() {
        return trackColor;
    }

    public final void setTrackColor(Color trackColor) {
        this.trackColor = trackColor;
    }

    /**
     * Draw the AbstractTile
     *
     * @param g2d The graphics handle
     */
    public void drawTile(Graphics2D g2d) {
        if (image == null) {
            BufferedImage bi = createImage();

            Graphics2D g2di = bi.createGraphics();
            g2di.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (trackColor == null) {
                trackColor = DEFAULT_TRACK_COLOR;
            }

            renderTile(g2di, trackColor);
            image = rotateClockwise(bi, orientation);
            g2di.dispose();
        }
        g2d.drawImage(image, this.center.x - image.getWidth() / 2, this.center.y - image.getHeight() / 2, null);
    }

    /**
     * Render a tile image Always starts at (0,0) used the default width and
     * height
     *
     * @param g2d the Graphic context
     */
    abstract void renderTile(Graphics2D g2d, Color trackColor);

    public void drawName(Graphics2D g2) {

    }

    public void drawCenterPoint(Graphics2D g2d) {
        drawCenterPoint(g2d, Color.GRAY);
    }

    public void drawCenterPoint(Graphics2D g2, Color color) {
        drawCenterPoint(g2, color, 4);
    }

    public void drawCenterPoint(Graphics2D g2d, Color color, double size) {
        double x = this.center.x - size / 2;
        double y = this.center.y - size / 2;

        g2d.setColor(color);
        g2d.fill(new Ellipse2D.Double(x, y, size, size));
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
        int steps = x / MIN_GRID;
        int sx = steps * MIN_GRID;
        if (x - sx >= MIN_GRID / 2) {
            sx = sx + MIN_GRID;
        }

        steps = y / MIN_GRID;
        int sy = steps * MIN_GRID;
        if (y - sy >= MIN_GRID / 2) {
            sy = sy + MIN_GRID;
        }

        return new Point(sx, sy);
    }

    //maybe use this: https://www.javaxt.com
    protected BufferedImage rotateClockwise(BufferedImage source, Orientation orientation) {
        double angle;
        Orientation o = orientation;
        if (o == null) {
            o = Orientation.EAST;
        }

        switch (o) {
            case SOUTH:
                angle = 90;
                break;
            case WEST:
                angle = 180;
                break;
            case NORTH:
                angle = 270;
                break;
            default:
                angle = 0;
                break;
        }

        double sin = Math.abs(Math.sin(Math.toRadians(angle)));
        double cos = Math.abs(Math.cos(Math.toRadians(angle)));
        int w = source.getWidth(null);
        int h = source.getHeight(null);
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage output = new BufferedImage(newW, newH, source.getType());
        Graphics2D g2d = output.createGraphics();
        g2d.translate((newW - w) / 2, (newH - h) / 2);
        g2d.rotate(Math.toRadians(angle), w / 2, h / 2);
        g2d.drawRenderedImage(source, null);
        g2d.dispose();
        return output;
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
        if (this.layoutTile != null) {
            this.layoutTile.setOrientation(orientation);
        }
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
        if (this.layoutTile != null) {
            this.layoutTile.setDirection(direction.getDirection());
        }
    }

    public Point getCenter() {
        return this.center;
    }

    public void setCenter(Point center) {
        this.center = center;
        if (this.layoutTile != null) {
            this.layoutTile.setCenter(center);
        }
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

    public LayoutTile getLayoutTile() {
        if (layoutTile == null) {
            Logger.trace("Create new LayoutTile for: " + getClass().getName());
            TileType tt = TileType.get(getClass().getSimpleName());
            String d = direction.getDirection();

            layoutTile = new LayoutTile(tt, orientation, d, center);
        }
        return layoutTile;
    }

    public final void setLayoutTile(LayoutTile layoutTile) {
        this.layoutTile = layoutTile;
        this.orientation = layoutTile.getOrientation();
        this.direction = Direction.get(layoutTile.getDirection());
        this.center = layoutTile.getCenter();
    }

    public boolean isDrawName() {
        return drawName;
    }

    public void setDrawName(boolean drawName) {
        this.drawName = drawName;
    }

    public void setReDrawListener(ReDrawListener listener) {
        this.reDrawListener = listener;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.getClass().getSimpleName());
        hash = 59 * hash + Objects.hashCode(this.center);
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
        final AbstractTile other = (AbstractTile) obj;
        if (this.orientation != other.orientation) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        return Objects.equals(this.center, other.center);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {orientation: " + orientation + ", direction: " + direction + ", center: " + center + "}";
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
            cx = this.center.x;
            cy = this.center.y;
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
}
