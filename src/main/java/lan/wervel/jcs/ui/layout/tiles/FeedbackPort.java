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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.feedback.FeedbackPortListener;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_WIDTH;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.MIN_GRID;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.ui.layout.tiles.enums.Rotation;

/**
 * Draw a FeedbackPort Track
 *
 * @author frans
 */
public class FeedbackPort extends AbstractTile implements FeedbackPortListener {

    private boolean portActive;

    public FeedbackPort(LayoutTile layoutTile) {
        super(layoutTile);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;

    }

    public FeedbackPort(Rotation rotation, int x, int y) {
        this(rotation, Direction.CENTER, x, y);
    }

    public FeedbackPort(Rotation rotation, Direction direction, int x, int y) {
        this(rotation, direction, new Point(x, y));
    }

    public FeedbackPort(Rotation rotation, Direction direction, Point center) {
        super(rotation, direction, center);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;

    }

    @Override
    public Rectangle2D getBounds2D() {
        return getBounds().getBounds2D();
    }

    @Override
    public void flipHorizontal() {
        if (Rotation.R0.equals(this.rotation) || Rotation.R180.equals(this.rotation)) {
            rotate();
            rotate();
        }
    }

    @Override
    public void flipVertical() {
        if (Rotation.R90.equals(this.rotation) || Rotation.R270.equals(this.rotation)) {
            rotate();
            rotate();
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

    @Override
    public void renderTile(Graphics2D g2, Color trackColor) {
        Graphics2D g2d = (Graphics2D) g2.create();

        double x, y, w, h;
        int x1, y1, x2, y2;

        x = MIN_GRID - 4;
        y = MIN_GRID - 8;
        w = 8;
        h = 16;

        x1 = 0;
        y1 = MIN_GRID;
        x2 = DEFAULT_WIDTH;
        y2 = y1;

        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        g2d.setStroke(new BasicStroke(1));
        if (!this.portActive) {
            g2d.setPaint(Color.gray);
            g2d.fill(new Ellipse2D.Double(x, y, w, h));
        }
        //Track
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(trackColor);
        g2d.drawLine(x1, y1, x2, y2);

        if (this.portActive) {
            g2d.setPaint(Color.MAGENTA);
            g2d.fill(new Ellipse2D.Double(x, y, w, h));
        }

        g2d.dispose();
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
