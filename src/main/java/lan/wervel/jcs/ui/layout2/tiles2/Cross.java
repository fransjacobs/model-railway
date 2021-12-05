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
package lan.wervel.jcs.ui.layout2.tiles2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import lan.wervel.jcs.entities.TileBean;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import static lan.wervel.jcs.ui.layout2.tiles2.AbstractTile2.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout2.tiles2.AbstractTile2.DEFAULT_WIDTH;

/**
 *
 * @author frans
 */
public class Cross extends Switch {

    private static int idSeq;

    public static final int CROSS_WIDTH = DEFAULT_WIDTH * 2;
    public static final int CROSS_HEIGHT = DEFAULT_HEIGHT * 2;
    public static final int CROSS_OFFSET = GRID;

    public Cross(TileBean tileBean) {
        super(tileBean);
        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
            this.width = CROSS_WIDTH;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = CROSS_HEIGHT;
        }
    }

    public Cross(Orientation orientation, Direction direction, int x, int y) {
        this(orientation, direction, new Point(x, y));
    }

    public Cross(Orientation orientation, Direction direction, Point center) {
        super(orientation, direction, center);
        // As a cross is asymetrical an offset is necessary
        switch (orientation) {
            case SOUTH:
                this.offsetX = 0;
                this.offsetY = CROSS_OFFSET;
                this.width = DEFAULT_WIDTH;
                this.height = CROSS_HEIGHT;
                break;
            case WEST:
                this.offsetX = -CROSS_OFFSET;
                this.offsetY = 0;
                this.width = CROSS_WIDTH;
                this.height = DEFAULT_HEIGHT;
                break;
            case NORTH:
                this.offsetX = 0;
                this.offsetY = -CROSS_OFFSET;
                this.width = DEFAULT_WIDTH;
                this.height = CROSS_HEIGHT;
                break;
            default:
                //East so default 
                this.offsetX = +CROSS_OFFSET;
                this.offsetY = 0;
                this.width = CROSS_WIDTH;
                this.height = DEFAULT_HEIGHT;
                break;
        }
    }

    @Override
    public Set<Point> getAltPoints() {
        int x = this.center.x;
        int y = this.center.y;
        Set<Point> alternatives = new HashSet<>();

        switch (orientation) {
            case SOUTH:
                Point sp = new Point(x, (y + DEFAULT_HEIGHT));
                alternatives.add(sp);
                break;
            case WEST:
                Point wp = new Point((x - DEFAULT_WIDTH), y);
                alternatives.add(wp);
                break;
            case NORTH:
                Point np = new Point(x, (y - DEFAULT_HEIGHT));
                alternatives.add(np);
                break;
            default:
                //East so default 
                Point ep = new Point((x + DEFAULT_WIDTH), y);
                alternatives.add(ep);
                break;
        }
        return alternatives;
    }

    @Override
    public void rotate() {
        super.rotate();

        //Due to the asymetical shape (center is on the left)
        //the offset has to be changedr the rotation
        switch (orientation) {
            case SOUTH:
                this.offsetX = 0;
                this.offsetY = CROSS_OFFSET;
                this.width = DEFAULT_WIDTH;
                this.height = CROSS_HEIGHT;
                break;
            case WEST:
                this.offsetX = -CROSS_OFFSET;
                this.offsetY = 0;
                this.width = CROSS_WIDTH;
                this.height = DEFAULT_HEIGHT;
                break;
            case NORTH:
                this.offsetX = 0;
                this.offsetY = -CROSS_OFFSET;
                this.width = DEFAULT_WIDTH;
                this.height = CROSS_HEIGHT;
                break;
            default:
                //East so default 
                this.offsetX = +CROSS_OFFSET;
                this.offsetY = 0;
                this.width = CROSS_WIDTH;
                this.height = DEFAULT_HEIGHT;
                break;
        }
    }

    @Override
    protected String getNewId() {
        idSeq++;
        return "cs-" + idSeq;
    }

    @Override
    protected void renderStraight(Graphics2D g2, Color trackColor, Color backgroundColor) {
        int x, y, w, h;
        x = 0;
        y = 17;
        w = DEFAULT_WIDTH * 2;
        h = 6;

        g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
        g2.setPaint(trackColor);
        g2.fillRect(x, y, w, h);
    }

    @Override
    protected void renderDiagonal(Graphics2D g2, Color trackColor, Color backgroundColor) {
        int[] xPoints, yPoints;
        if (Direction.RIGHT.equals(this.direction)) {
            xPoints = new int[]{40, 40, 16, 24};
            yPoints = new int[]{16, 24, 0, 0};
        } else {
            xPoints = new int[]{40, 40, 16, 24};
            yPoints = new int[]{24, 16, 40, 40};
        }

        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setPaint(trackColor);

        g2.fillPolygon(xPoints, yPoints, xPoints.length);
    }

    protected void renderDiagonal2(Graphics2D g2, Color trackColor, Color backgroundColor) {
        int[] xPoints, yPoints;
        if (Direction.RIGHT.equals(this.direction)) {
            xPoints = new int[]{40, 40, 56, 64};
            yPoints = new int[]{16, 24, 40, 40};
        } else {
            xPoints = new int[]{40, 40, 56, 64};
            yPoints = new int[]{24, 16, 0, 0};
        }

        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setPaint(trackColor);
        //g2.setPaint(Color.BLUE);

        g2.fillPolygon(xPoints, yPoints, xPoints.length);
    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
        renderStraight(g2, trackColor, backgroundColor);
        renderDiagonal(g2, trackColor, backgroundColor);
        renderDiagonal2(g2, trackColor, backgroundColor);
    }

    @Override
    public Set<Point> getAdjacentPoints() {
        Set<Point> adjacent = new HashSet<>();

        //TOD Cross has 4 adjacent points and is asymetrical!
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

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }

}
