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
import java.awt.image.BufferedImage;
import lan.wervel.jcs.entities.LayoutTile;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_WIDTH;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.ui.layout.tiles.enums.Rotation;

/**
 * Draw a DiagonalTrack (Curved) Track
 *
 * @author frans
 */
public class DiagonalTrack extends AbstractTile {

    public DiagonalTrack(LayoutTile layoutTile) {
        super(layoutTile);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    public DiagonalTrack(Rotation rotation, int x, int y) {
        this(rotation, Direction.CENTER, x, y);
    }

    public DiagonalTrack(Rotation rotation, Direction direction, int x, int y) {
        this(rotation, direction, new Point(x, y));
    }

    public DiagonalTrack(Rotation rotation, Direction direction, Point center) {
        super(rotation, direction, center);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    @Override
    public void flipHorizontal() {
        rotate();
        rotate();
    }

    @Override
    public void flipVertical() {
        rotate();
        rotate();
    }

    @Override
    protected BufferedImage createImage() {
        return new BufferedImage(DEFAULT_WIDTH + 6, DEFAULT_HEIGHT + 6, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor) {
        Graphics2D g2d = (Graphics2D) g2.create();

        int x1, y1, x2, y2;
        x1 = 0;
        y1 = 0;
        x2 = DEFAULT_WIDTH + 6;
        y2 = DEFAULT_WIDTH + 6;

        //Track
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, DEFAULT_WIDTH + 6, DEFAULT_HEIGHT + 6);

        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(trackColor);
        g2d.drawLine(x1, y1, x2, y2);

        g2d.dispose();
    }

}
