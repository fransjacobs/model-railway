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
import static lan.wervel.jcs.ui.layout.DesignCanvas.GRID_SIZE;
import lan.wervel.jcs.ui.layout.ReDrawListener;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.ui.layout.tiles.enums.Rotation;
import org.pmw.tinylog.Logger;

/**
 * Basic graphic element to display a track, turnout, etc on the screen. The
 * layout is drawn by using "tiles". a tile is a small image of a symbolic
 * track, turnout, etc. When a Tile is placed it is always snapped to the
 * nearest grid point. The grid is 20x 20 pix the grid points are 40 x 40 pix.
 * An image could be 40x40 pix or 20x20 pix
 *
 * An Image has a rotation (0, 90, 180 or 270 deg) and a direction center, right
 * or left
 *
 * @author frans
 */
public abstract class AbstractTile implements Shape {

    public static final int MIN_GRID = 20;
    public static final int DEFAULT_WIDTH = MIN_GRID * 2;
    public static final int DEFAULT_HEIGHT = MIN_GRID * 2;

    protected Rotation rotation;
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

    protected AbstractTile(Rotation rotation, Direction direction, Point center) {
        this.center = center;
        this.rotation = rotation;
        this.direction = direction;
        setTrackColor(DEFAULT_TRACK_COLOR);
    }

    protected AbstractTile(LayoutTile layoutTile) {
        this.layoutTile = layoutTile;
        this.center = new Point(layoutTile.getX(), layoutTile.getY());
        this.rotation = toRotation(layoutTile.getRotation());
        this.direction = toDirection(layoutTile.getDirection());
        setTrackColor(DEFAULT_TRACK_COLOR);
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
            image = createImage();

            Graphics2D g2di = image.createGraphics();
            if (this.trackColor == null) {
                this.trackColor = DEFAULT_TRACK_COLOR;
            }

            renderTile(g2di, trackColor);
            image = rotateClockwise(image, rotation);
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
        switch (this.rotation) {
            case R0:
                setRotation(Rotation.R90);
                break;
            case R90:
                setRotation(Rotation.R180);
                break;
            case R180:
                setRotation(Rotation.R270);
                break;
            default:
                setRotation(Rotation.R0);
                break;
        }
    }

    public void flipHorizontal() {
    }

    public void flipVertical() {
    }

    public void move(int newX, int newY) {
        Point cs = snapToGrid(newX, newY);
        this.setCenter(cs);
    }

    private static Rotation toRotation(String s) {
        switch (s) {
            case "R90":
                return Rotation.R90;
            case "R180":
                return Rotation.R180;
            case "R270":
                return Rotation.R270;
            default:
                return Rotation.R0;
        }
    }

    private static Direction toDirection(String s) {
        switch (s) {
            case "LEFT":
                return Direction.LEFT;
            case "RIGHT":
                return Direction.RIGHT;
            default:
                return Direction.CENTER;
        }
    }

    public static final Point snapToGrid(Point p) {
        return snapToGrid(p.x, p.y);
    }

    /**
     * Snap coordinates to the neared grid point
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

    public static BufferedImage rotateClockwise(BufferedImage source, Rotation rotation) {
        if (Rotation.R0.equals(rotation)) {
            //nothing todo...
            return source;
        }

        BufferedImage output = new BufferedImage(source.getHeight(), source.getWidth(), source.getType());
        AffineTransform rat = new AffineTransform();

        double offset = (source.getWidth() - source.getHeight()) / 2;

        switch (rotation) {
            case R180:
                rat.rotate(Math.PI, source.getWidth() / 2, source.getHeight() / 2);
                rat.translate(offset, offset);
                break;
            case R270:
                rat.rotate(-Math.PI / 2, source.getWidth() / 2, source.getHeight() / 2);
                rat.translate(-offset, -offset);
                break;
            default:
                //R90...
                //Logger.debug("##### Rotate " + rotation + " Offset: "+offset+" W: "+source.getWidth()+" H: "+source.getHeight()+"...");
                //rat.rotate(-Math.PI / 2, source.getWidth() / 2, source.getHeight() / 2);
                rat.rotate(Math.PI / 2, source.getWidth() / 2, source.getHeight() / 2);
                rat.translate(offset, offset);
                break;
        }

        AffineTransformOp op = new AffineTransformOp(rat, AffineTransformOp.TYPE_BICUBIC);
        op.filter(source, output);
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

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        Rotation oldR = this.rotation;
        this.rotation = rotation;
        if (this.layoutTile != null) {
            this.layoutTile.setRotation(rotation.toString());
        }
        if (!rotation.equals(oldR) && this.image != null) {
            //images is cached, so remove will be created again
            image = null;
        }
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        if (this.layoutTile != null) {
            this.layoutTile.setDirection(direction.toString());
        }
    }

    public Point getCenter() {
        return this.center;
    }

    public void setCenter(Point center) {
        this.center = center;
        if (this.layoutTile != null) {
            this.layoutTile.setX(center.x);
            this.layoutTile.setY(center.y);
        }
    }

    protected BufferedImage createImage() {
        return new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
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
        if (this.layoutTile == null) {
            Logger.trace("Create new LayoutTile for: " + this.getClass().getName());
            layoutTile = new LayoutTile(this.getClass().getSimpleName(), this.rotation.toString(), this.direction.toString(), this.center.x, this.center.y);
        }
        return layoutTile;
    }

    public void setLayoutTile(LayoutTile layoutTile) {
        this.layoutTile = layoutTile;
        this.rotation = toRotation(layoutTile.getRotation());
        this.direction = toDirection(layoutTile.getDirection());
        this.center = new Point(layoutTile.getX(), layoutTile.getY());
    }

    public static AbstractTile createTile(LayoutTile layoutTile) {
        if (layoutTile == null) {
            return null;
        }

        String tt = layoutTile.getTiletype();
        switch (tt) {
            case "StraightTrack":
                return new StraightTrack(layoutTile);
            case "DiagonalTrack":
                return new DiagonalTrack(layoutTile);
            case "TurnoutTile":
                return new TurnoutTile(layoutTile);
            case "SignalTile":
                return new SignalTile(layoutTile);
            case "FeedbackPort":
                return new FeedbackPort(layoutTile);
            case "OccupancyDetector":
                return new OccupancyDetector(layoutTile);
            default:
                return null;
        }
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
        if (this.rotation != other.rotation) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        return Objects.equals(this.center, other.center);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {rotation: " + rotation + ", direction: " + direction + ", center: " + center + "}";
    }

    @Override
    public Rectangle getBounds() {
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
