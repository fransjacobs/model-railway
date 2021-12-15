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
import lan.wervel.jcs.entities.TileBean;
import static lan.wervel.jcs.entities.TileBean.DEFAULT_WIDTH;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.ui.layout2.LayoutUtil;
import static lan.wervel.jcs.ui.layout2.LayoutUtil.DEFAULT_HEIGHT;

/**
 * Draw a Straight Track
 *
 */
public class Straight extends AbstractTile2 implements Tile {

    private static int idSeq;

    public Straight(TileBean tileBean) {
        super(tileBean);
        this.width = LayoutUtil.DEFAULT_WIDTH;
        this.height = LayoutUtil.DEFAULT_HEIGHT;
    }

    public Straight(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public Straight(Orientation orientation, Point center) {
        super(orientation, center);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    @Override
    protected String getNewId() {
        idSeq++;
        return "st-" + idSeq;
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

    @Override
    public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
        renderStraight(g2, trackColor, backgroundColor);
    }

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }
}