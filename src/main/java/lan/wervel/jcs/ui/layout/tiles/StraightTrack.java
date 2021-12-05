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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import lan.wervel.jcs.entities.LayoutTile;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.MIN_GRID;
import lan.wervel.jcs.entities.enums.Orientation;

/**
 * Draw a Straight Track
 *
 */
public class StraightTrack extends AbstractTile {

    public StraightTrack(LayoutTile layoutTile) {
        super(layoutTile);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    public StraightTrack(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public StraightTrack(Orientation orientation, Point center) {
        super(orientation, center);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor) {
        Graphics2D g2d = (Graphics2D) g2.create();

        int x1, y1, x2, y2;
        x1 = 0;
        y1 = MIN_GRID;
        x2 = DEFAULT_WIDTH;
        y2 = y1;

        //Track
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(trackColor);
        g2d.drawLine(x1, y1, x2, y2);

        //In and out side only mark the out        
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(Color.darkGray);
        g2d.drawLine(x2-1, y1, x2, y2);

        g2d.dispose();
    }
}
