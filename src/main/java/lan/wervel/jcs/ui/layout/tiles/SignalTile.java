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
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.entities.Signal;
import lan.wervel.jcs.entities.enums.SignalValue;
import lan.wervel.jcs.trackservice.AccessoryEvent;
import lan.wervel.jcs.trackservice.events.AccessoryListener;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_WIDTH;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.MIN_GRID;
import lan.wervel.jcs.entities.enums.Orientation;
import static lan.wervel.jcs.ui.layout.tiles.enums.Rotation.R180;
import static lan.wervel.jcs.ui.layout.tiles.enums.Rotation.R270;
import static lan.wervel.jcs.ui.layout.tiles.enums.Rotation.R90;

/**
 * Draw a Straight Track
 *
 * @author frans
 */
public class SignalTile extends AbstractTile implements AccessoryListener {

    private int signalType;
    private boolean midget;
    private SignalValue signalValue;

    public SignalTile(LayoutTile layoutTile) {
        super(layoutTile);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;

        if (layoutTile != null
                && layoutTile.getSolenoidAccessoiry() != null
                && layoutTile.getSolenoidAccessoiry() instanceof Signal) {

            Signal s = (Signal) layoutTile.getSolenoidAccessoiry();

            signalType = s.getLightImages();
            signalValue = s.getSignalValue();
            midget = "Midget".equals(s.getDescription());
            //Logger.trace("A: " + s.getAddress() + " A2: " + s.getAddress2() + " V: " + s.getValue() + " V2 " + s.getValue2() + " SV: " + s.getSignalValue());
        } else {
            signalType = Signal.HP01;
        }
    }

    public SignalTile(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public SignalTile(Orientation orientation, Point center) {
        super(orientation, center);
        signalType = Signal.HP01;
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    @Override
    public void switched(AccessoryEvent event) {
        if (layoutTile != null && layoutTile.getSolenoidAccessoiry() != null && event.isEventFor(layoutTile.getSolenoidAccessoiry())) {
            Signal s = (Signal) layoutTile.getSolenoidAccessoiry();
            s.setSignalValue(event.getSignalValue());
            this.setSignalValue(event.getSignalValue());
        }
        if (this.reDrawListener != null) {
            this.reDrawListener.reDraw();
        }
    }

    protected void setSignalValue(SignalValue signalValue) {
        if (this.signalValue == null || (this.signalValue != null && !this.signalValue.equals(signalValue))) {
            this.signalValue = signalValue;
            this.image = null;
        }
    }

    protected void setSignalType(int signalType) {
        this.signalType = signalType;
    }

    protected void setMidget(boolean midget) {
        this.midget = midget;
    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor) {
        Graphics2D g2d = (Graphics2D) g2.create();

        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        if (Signal.HP012SH1 == this.signalType) {
            renderSignal4(g2d);
        } else {
            if (midget) {
                renderSignal2m(g2d);
            } else {
                renderSignal2(g2d);
            }
        }

        int x1, y1, x2, y2;
        x1 = 0;
        y1 = MIN_GRID;
        x2 = DEFAULT_WIDTH;
        y2 = y1;

        //Track
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(trackColor);
        g2d.drawLine(x1, y1, x2, y2);

        //In and out side only mark the out        
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setPaint(Color.darkGray);
        g2d.drawLine(x2 - 1, y1, x2, y2);

        g2d.dispose();
    }

    /**
     * Render a Signal with 2 lights
     *
     * @param g2d the graphics context
     */
    protected void renderSignal2(Graphics2D g2d) {
        int rx = MIN_GRID;
        int ry = MIN_GRID + 5;
        int rw = 18;
        int rh = 10;
        int l1x = MIN_GRID + 2;
        int l1y = MIN_GRID + 7;
        int l2x = MIN_GRID + 10;
        int l2y = MIN_GRID + 7;

        Color color1 = Color.gray;
        Color color2 = Color.gray;

        if (this.signalValue == null) {
            this.signalValue = SignalValue.OFF;
        }

        switch (signalValue) {
            case Hp0:
                color1 = Color.red;
                color2 = Color.gray;
                break;
            case Hp1:
                color1 = Color.gray;
                color2 = Color.green;
                break;
            default:
                break;
        }

        g2d.setStroke(new BasicStroke(10f));
        g2d.setPaint(Color.darkGray);
        g2d.fillRoundRect(rx, ry, rw, rh, 0, 0);

        g2d.setPaint(color1);
        g2d.fillOval(l1x, l1y, 5, 5);
        g2d.setPaint(color2);
        g2d.fillOval(l2x, l2y, 5, 5);
    }

    /**
     * Render a entry Signal which can show 4 light images
     *
     * @param g2d the Graphics context
     */
    protected void renderSignal4(Graphics2D g2d) {
        int rx = MIN_GRID - 5;
        int ry = MIN_GRID + 5;
        int rw = 24;
        int rh = 12;
        int c1x = MIN_GRID + 14;
        int c1y = MIN_GRID + 6;
        int c2x = MIN_GRID + 9;
        int c2y = MIN_GRID + 6;
        int c3x = MIN_GRID + 9;
        int c3y = MIN_GRID + 11;
        int c4x = MIN_GRID + 5;
        int c4y = MIN_GRID + 12;
        int c5x = MIN_GRID + 2;
        int c5y = MIN_GRID + 7;
        int c6x = MIN_GRID - 4;
        int c6y = MIN_GRID + 6;

        //Initialize all "lights"
        Color color1 = Color.gray;
        Color color2 = Color.gray;
        Color color3 = Color.gray;
        Color color4 = Color.gray;
        Color color5 = Color.gray;
        Color color6 = Color.gray;

        if (this.signalValue == null) {
            this.signalValue = SignalValue.OFF;
        }

        switch (this.signalValue) {
            case Hp0:
                color2 = Color.red;
                color3 = Color.red;
                break;
            case Hp1:
                color1 = Color.green;
                break;
            case Hp2:
                color1 = Color.green;
                color6 = Color.yellow;
                break;
            case Hp0Sh1:
                color2 = Color.red;
                color4 = Color.white;
                color5 = Color.white;
                break;
            default:
                break;
        }

        g2d.setStroke(new BasicStroke(10f));
        g2d.setPaint(Color.darkGray);
        g2d.fillRoundRect(rx, ry, rw, rh, 0, 0);

        g2d.setPaint(color1);
        g2d.fillOval(c1x, c1y, 5, 5);
        g2d.setPaint(color2);
        g2d.fillOval(c2x, c2y, 5, 5);
        g2d.setPaint(color3);
        g2d.fillOval(c3x, c3y, 5, 5);
        g2d.setPaint(color4);
        g2d.fillOval(c4x, c4y, 3, 3);
        g2d.setPaint(color5);
        g2d.fillOval(c5x, c5y, 3, 3);
        g2d.setPaint(color6);
        g2d.fillOval(c6x, c6y, 5, 5);
    }

    /**
     * Render a midget Signal
     *
     * @param g2d the Graphics context
     */
    protected void renderSignal2m(Graphics2D g2d) {
        int rx = MIN_GRID + 5;
        int ry = MIN_GRID + 5;
        int rw = 12;
        int rh = 12;
        int c1x = MIN_GRID + 12;
        int c1y = MIN_GRID + 5;
        int c2x = MIN_GRID + 12;
        int c2y = MIN_GRID + 12;
        int c3x = MIN_GRID + 12;
        int c3y = MIN_GRID + 10;
        int c4x = MIN_GRID + 6;
        int c4y = MIN_GRID + 6;

        Color color1 = Color.gray;
        Color color2 = Color.gray;
        Color color3 = Color.gray;
        Color color4 = Color.gray;

        if (this.signalValue == null) {
            this.signalValue = SignalValue.OFF;
        }

        switch (this.signalValue) {
            case Hp0:
                color1 = Color.red;
                color2 = Color.red;
                break;
            case Hp1:
                color3 = Color.white;
                color4 = Color.white;
                break;
            default:
                break;
        }

        g2d.setStroke(new BasicStroke(10f));
        g2d.setPaint(Color.darkGray);
        g2d.fillRoundRect(rx, ry, rw, rh, 0, 0);

        g2d.setPaint(color1);
        g2d.fillOval(c1x, c1y, 5, 5);

        g2d.setPaint(color2);
        g2d.fillOval(c2x, c2y, 5, 5);

        g2d.setPaint(color3);
        g2d.fillOval(c3x, c3y, 4, 4);

        g2d.setPaint(color4);
        g2d.fillOval(c4x, c4y, 4, 4);
    }

    @Override
    public void drawName(Graphics2D g2) {
        if (this.drawName && layoutTile != null && layoutTile.getSolenoidAccessoiry() != null) {

            Graphics2D g2d = (Graphics2D) g2.create();

            int textOffsetX, textOffsetY;

            if (Direction.RIGHT.equals(direction)) {
                switch (this.orientation) {
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

            String name = layoutTile.getSolenoidAccessoiry().getName();
            int sx = this.center.x + textOffsetX;
            int sy = this.center.y + textOffsetY;
            g2d.drawString(name, sx, sy);

            g2d.dispose();
        }
    }

}
