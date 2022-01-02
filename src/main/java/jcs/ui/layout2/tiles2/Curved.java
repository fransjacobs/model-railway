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
package jcs.ui.layout2.tiles2;

import jcs.ui.layout2.Tile;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.DEFAULT_WIDTH;
import jcs.entities.enums.Orientation;
import jcs.ui.layout2.LayoutUtil;
import static jcs.ui.layout2.LayoutUtil.DEFAULT_HEIGHT;

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
    protected void setIdSeq(int id) {
        idSeq = id;
    }
}
