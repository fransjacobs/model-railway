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

import lan.wervel.jcs.ui.layout.tiles.enums.Rotation;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.feedback.FeedbackPortListener;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_WIDTH;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.MIN_GRID;

/**
 * Draw a OccupancyDetector
 *
 * @author frans
 */
public class OccupancyDetector extends AbstractTile implements FeedbackPortListener {

    private boolean portActive;

    public OccupancyDetector(LayoutTile layoutTile) {
        super(layoutTile);
        if (Rotation.R0.equals(rotation) || Rotation.R180.equals(rotation)) {
            this.width = DEFAULT_WIDTH * 2;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT * 2;
        }
    }

    public OccupancyDetector(Rotation rotation, int x, int y) {
        this(rotation, Direction.CENTER, x, y);
    }

    public OccupancyDetector(Rotation rotation, Direction direction, int x, int y) {
        this(rotation, direction, new Point(x, y));
    }

    public OccupancyDetector(Rotation rotation, Direction direction, Point center) {
        super(rotation, direction, center);
        if (Rotation.R0.equals(rotation) || Rotation.R180.equals(rotation)) {
            this.width = DEFAULT_WIDTH * 2;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT * 2;
        }
    }

    @Override
    public void rotate() {
        if (Rotation.R90.equals(rotation)) {
            this.setRotation(Rotation.R0);
            this.width = DEFAULT_WIDTH * 2;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.setRotation(Rotation.R90);
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT * 2;
        }
    }

    @Override
    public Integer getModuleNumber() {
        if (layoutTile != null && layoutTile.getFeedbackModule() != null) {
            return layoutTile.getFeedbackModule().getModuleNumber();
        }
        return null;
    }

    @Override
    public Integer getPort() {
        if (layoutTile != null) {
            return layoutTile.getPort();
        }
        return null;
    }

    @Override
    public void setValue(boolean value) {
        if (value != this.portActive) {
            this.portActive = value;
            this.image = null;
        }
        if (this.reDrawListener != null) {
            this.reDrawListener.reDraw();
        }
    }

//  @Override
//  public int getWidth() {
//    if (Rotation.R0.equals(rotation) || Rotation.R180.equals(rotation)) {
//      return DEFAULT_WIDTH * 2;
//    } else {
//      return DEFAULT_WIDTH;
//    }
//  }
//  @Override
//  public int getHeight() {
//    if (Rotation.R0.equals(rotation) || Rotation.R180.equals(rotation)) {
//      return DEFAULT_HEIGHT;
//    } else {
//      return DEFAULT_HEIGHT * 2;
//    }
//  }
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
            g2.setPaint(Color.gray);
            g2.draw(new RoundRectangle2D.Double(x, y, w, h, 10, 10));
        }

        //Track
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(trackColor);
        g2d.drawLine(x1, y1, x2, y2);

        if (this.portActive) {
            g2.setPaint(Color.magenta);
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

}
