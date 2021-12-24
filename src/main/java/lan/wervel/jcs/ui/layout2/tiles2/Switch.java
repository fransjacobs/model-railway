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
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import static lan.wervel.jcs.ui.layout2.LayoutUtil.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout2.LayoutUtil.DEFAULT_WIDTH;

/**
 * Draw a Railway Switch Depending on the Direction it is a Left or Right switch
 */
public class Switch extends AbstractTile2 implements Tile {

    private static int idSeq;

    public Switch(TileBean tileBean) {
        super(tileBean);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    public Switch(Orientation orientation, Direction direction, int x, int y) {
        this(orientation, direction, new Point(x, y));
    }

    public Switch(Orientation orientation, Direction direction, Point center) {
        super(orientation, direction, center);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    @Override
    protected String getNewId() {
        idSeq++;
        return "sw-" + idSeq;
    }

    protected void renderStraight(Graphics2D g2, Color trackColor, Color backgroundColor) {
        int x, y, w, h;
        x = 0;
        y = 17;
        w = DEFAULT_WIDTH;
        h = 6;

        g2.setStroke(new BasicStroke(4, BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND));
        g2.setPaint(trackColor);
        g2.fillRect(x, y, w, h);
    }

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

    @Override
    public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
        renderStraight(g2, trackColor, backgroundColor);
        renderDiagonal(g2, trackColor, backgroundColor);
    }

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }
}
