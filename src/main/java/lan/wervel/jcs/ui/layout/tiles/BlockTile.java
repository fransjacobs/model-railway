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

import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import lan.wervel.jcs.entities.LayoutTile;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_WIDTH;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.MIN_GRID;
import lan.wervel.jcs.trackservice.events.SensorListener;
import lan.wervel.jcs.entities.enums.Orientation;

/**
 * Draw a OccupancyDetector
 *
 * @author frans
 */
public class BlockTile extends AbstractTile implements SensorListener {

    private boolean portActive;

    public BlockTile(LayoutTile layoutTile) {
        super(layoutTile);
        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
            this.width = DEFAULT_WIDTH * 2;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT * 2;
        }
    }

    public BlockTile(int x, int y) {
        this(Orientation.EAST, x, y);
    }

    public BlockTile(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public BlockTile(Orientation orientation, Point center) {
        super(orientation, Direction.CENTER, center);
        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
            this.width = DEFAULT_WIDTH * 2;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT * 2;
        }
    }

    @Override
    public void rotate() {
        super.rotate();
        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
            this.width = DEFAULT_WIDTH * 2;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT * 2;
        }
    }

    @Override
    public Integer getContactId() {
        if (layoutTile != null && layoutTile.getSensor() != null) {
            return layoutTile.getSensor().getContactId();
        }
        return null;
    }

    @Override
    public void setActive(boolean value) {
        if (value != this.portActive) {
            this.portActive = value;
            this.image = null;
        }
        if (this.reDrawListener != null) {
            this.reDrawListener.reDraw();
        }
    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor) {
        Graphics2D g2d = (Graphics2D) g2.create();

        double x, y, w, h;
        int x1, y1, x2, y2;

        x = MIN_GRID * 2 - 30;
        y = MIN_GRID - 6;
        w = 60;
        h = 12;

        x1 = 0;
        y1 = MIN_GRID;
        x2 = DEFAULT_WIDTH * 2;
        y2 = y1;

        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, DEFAULT_WIDTH * 2, DEFAULT_HEIGHT);

        if (!this.portActive) {
            g2.setStroke(new BasicStroke(1));
            g2.setPaint(Color.cyan);
            g2.draw(new RoundRectangle2D.Double(x, y, w, h, 10, 10));
        }

        //Track
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(trackColor);
        g2d.drawLine(x1, y1, x2, y2);

        //out
        g2d.setPaint(Color.darkGray);
        g2d.drawLine(x2 - 1, y1, x2, y2);

        if (this.portActive) {
            g2.setPaint(Color.cyan);
            g2.fill(new RoundRectangle2D.Double(x, y, w, h, 10, 10));
        }

        g2d.dispose();
    }

    @Override
    protected BufferedImage createImage() {
        return new BufferedImage(DEFAULT_WIDTH * 2, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void drawCenterPoint(Graphics2D g2d, Color color, double size) {
        Color c;
        if (this.portActive) {
            c = Color.darkGray;
        } else {
            c = color;
        }

        double x = this.center.x - size / 2;
        double y = this.center.y - size / 2;

        g2d.setColor(c);
        g2d.fill(new Ellipse2D.Double(x, y, size, size));
    }

    @Override
    public void drawName(Graphics2D g2) {
        if (this.drawName && layoutTile != null && layoutTile.getSensor() != null) {

            Graphics2D g2d = (Graphics2D) g2.create();

            int textOffsetX, textOffsetY;

            if (Direction.RIGHT.equals(direction)) {
                switch (orientation) {
                    case EAST:
                        textOffsetX = -1 * (MIN_GRID + MIN_GRID - MIN_GRID / 4);
                        textOffsetY = -1 * MIN_GRID / 2;
                        break;
                    case WEST:
                        textOffsetX = 0;
                        textOffsetY = -1 * MIN_GRID / 2;
                        break;
                    case SOUTH:
                        textOffsetX = 0;
                        textOffsetY = MIN_GRID;
                        break;
                    default:
                        textOffsetX = -1 * (MIN_GRID + MIN_GRID - MIN_GRID / 4);
                        textOffsetY = MIN_GRID;
                        break;
                }
            } else {
                switch (orientation) {
                    case EAST:
                        textOffsetX = 0;
                        textOffsetY = -1 * MIN_GRID / 2;
                        break;
                    case WEST:
                        textOffsetX = 0;
                        textOffsetY = MIN_GRID;
                        break;
                    case SOUTH:
                        textOffsetX = -1 * (MIN_GRID + MIN_GRID - MIN_GRID / 4);
                        textOffsetY = MIN_GRID;
                        break;
                    default:
                        textOffsetX = -1 * (MIN_GRID + MIN_GRID - MIN_GRID / 4);
                        textOffsetY = -1 * MIN_GRID / 2;
                        break;
                }
            }

            g2d.setPaint(Color.darkGray);

            String name = "# " + layoutTile.getSensor().getContactId();
            int sx = this.center.x + textOffsetX;
            int sy = this.center.y + textOffsetY;
            g2d.drawString(name, sx, sy);

            g2d.dispose();
        }
    }

}
