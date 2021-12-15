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

import lan.wervel.jcs.ui.layout2.Tile;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import lan.wervel.jcs.entities.TileBean;
import static lan.wervel.jcs.entities.TileBean.DEFAULT_WIDTH;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.ui.layout2.LayoutUtil;
import static lan.wervel.jcs.ui.layout2.LayoutUtil.DEFAULT_HEIGHT;

/**
 * Draw a Curved Track
 *
 */
public class Curved extends AbstractTile2 implements Tile {

    private static int idSeq;

    public Curved(TileBean tileBean) {
        super(tileBean);
        this.width = LayoutUtil.DEFAULT_WIDTH;
        this.height = LayoutUtil.DEFAULT_HEIGHT;
    }

    public Curved(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public Curved(Orientation orientation, Point center) {
        super(orientation, center);
        this.width = LayoutUtil.DEFAULT_WIDTH;
        this.height = LayoutUtil.DEFAULT_HEIGHT;
    }

    @Override
    protected final String getNewId() {
        idSeq++;
        return "ct-" + idSeq;
    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
        Graphics2D g2d = (Graphics2D) g2.create();

        int[] xPoints = new int[]{40, 40, 16, 24};
        int[] yPoints = new int[]{24, 16, 40, 40};

        g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(trackColor);

        g2d.fillPolygon(xPoints, yPoints, xPoints.length);
        g2d.dispose();
    }

    @Override
    public Set<Point> getAdjacentPoints() {
        Set<Point> adjacent = new HashSet<>();

        int oX = this.width / 2 + DEFAULT_WIDTH / 2;
        int oY = this.height / 2 + DEFAULT_HEIGHT / 2;

        switch (this.orientation) {
            case SOUTH:
                adjacent.add(new Point(this.getCenterX() - oX, this.getCenterY()));
                adjacent.add(new Point(this.getCenterX(), this.getCenterY() + oY));
                break;
            case WEST:
                adjacent.add(new Point(this.getCenterX() - oX, this.getCenterY()));
                adjacent.add(new Point(this.getCenterX(), this.getCenterY() - oY));
                break;
            case NORTH:
                adjacent.add(new Point(this.getCenterX() + oX, this.getCenterY()));
                adjacent.add(new Point(this.getCenterX(), this.getCenterY() - oY));
                break;
            default:
                //EAST
                adjacent.add(new Point(this.getCenterX() + oX, this.getCenterY()));
                adjacent.add(new Point(this.getCenterX(), this.getCenterY() + oY));
                break;
        }
        return adjacent;
    }

    /**
     *
     * @return a Set with Points which are on the edges of the Tile
     */
    @Override
    public Set<Point> getConnectingPoints() {
        Set<Point> connecting = new HashSet<>();
        int x = this.getCenterX();
        int y = this.getCenterY();

        int ox = this.width / 2;
        int oy = this.height / 2;

        switch (this.orientation) {
            case SOUTH:
                connecting.add(new Point((x - ox), y));
                connecting.add(new Point(x, y + oy));
                break;
            case WEST:
                connecting.add(new Point((x - ox), y));
                connecting.add(new Point(x, (y - oy)));
                break;
            case NORTH:
                connecting.add(new Point((x + ox), y));
                connecting.add(new Point(x, (y - oy)));
                break;
            default:
                //EAST
                connecting.add(new Point((x + ox), y));
                connecting.add(new Point(x, (y + oy)));
                break;
        }
        return connecting;
    }

    @Override
    public Point getWest() {
        if (Orientation.WEST.equals(this.orientation) || Orientation.SOUTH.equals(this.orientation)) {
            return new Point(this.center.x - this.width / 2, this.center.y);
        } else {
            return null;
        }
    }

    @Override
    public Point getEast() {
        if (Orientation.EAST.equals(this.orientation) || Orientation.NORTH.equals(this.orientation)) {
            return new Point(this.center.x + this.width / 2, this.center.y);
        } else {
            return null;
        }
    }

    @Override
    public Point getSouth() {
        if (Orientation.EAST.equals(this.orientation) || Orientation.SOUTH.equals(this.orientation)) {
            return new Point(this.center.x, this.center.y + this.height / 2);
        } else {
            return null;
        }
    }

    @Override
    public Point getNorth() {
        if (Orientation.NORTH.equals(this.orientation) || Orientation.WEST.equals(this.orientation)) {
            return new Point(this.center.x, this.center.y - this.height / 2);
        } else {
            return null;
        }
    }

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }
}
