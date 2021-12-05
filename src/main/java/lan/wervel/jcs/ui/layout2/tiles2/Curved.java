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
import java.util.HashSet;
import java.util.Set;
import lan.wervel.jcs.entities.TileBean;
import lan.wervel.jcs.entities.enums.Orientation;
import static lan.wervel.jcs.ui.layout2.tiles2.AbstractTile2.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout2.tiles2.AbstractTile2.DEFAULT_WIDTH;

/**
 * Draw a Curved Track
 *
 */
public class Curved extends AbstractTile2 {

    private static int idSeq;

    public Curved(TileBean tileBean) {
        super(tileBean);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    public Curved(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public Curved(Orientation orientation, Point center) {
        super(orientation, center);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
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

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }
}
