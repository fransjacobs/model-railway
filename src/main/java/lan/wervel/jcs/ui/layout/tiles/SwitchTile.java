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

import jcs.ui.layout.tiles.enums.Direction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import jcs.entities.LayoutTile;
import jcs.entities.enums.AccessoryValue;
import jcs.trackservice.AccessoryEvent;
import jcs.trackservice.events.AccessoryListener;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_HEIGHT;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.DEFAULT_WIDTH;
import static lan.wervel.jcs.ui.layout.tiles.AbstractTile.MIN_GRID;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.EAST;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;

/**
 * Draw a Turnout
 *
 * @author frans
 */
@Deprecated
public class SwitchTile extends AbstractTile implements AccessoryListener {

    private AccessoryValue accessoryValue;

    public SwitchTile(LayoutTile layoutTile) {
        super(layoutTile);
        this.width = DEFAULT_WIDTH * 2;
        this.height = DEFAULT_HEIGHT * 2;
    }

    public SwitchTile(Orientation orientation, Direction direction, int x, int y) {
        this(orientation, direction, new Point(x, y));
    }

    public SwitchTile(Orientation orientation, Direction direction, Point center) {
        super(orientation, direction, center);
        this.width = DEFAULT_WIDTH * 2;
        this.height = DEFAULT_HEIGHT * 2;
    }

    @Override
    public void flipHorizontal() {
        //By default a turnout is a R horizontal (R0)
        //so when in this position a H flip is performed the direction of the turnout is changed from R to L
        if (Orientation.EAST.equals(this.orientation) || Orientation.WEST.equals(this.orientation)) {
            if (Direction.LEFT.equals(this.direction)) {
                this.setDirection(Direction.RIGHT);
            } else {
                this.setDirection(Direction.LEFT);
            }
            this.image = null;
        } else {
            this.rotate();
            this.rotate();
        }
    }

    @Override
    public void flipVertical() {
        if (Orientation.NORTH.equals(this.orientation) || Orientation.SOUTH.equals(this.orientation)) {
            if (Direction.LEFT.equals(this.direction)) {
                this.setDirection(Direction.RIGHT);
            } else {
                this.setDirection(Direction.LEFT);
            }
            this.image = null;
        } else {
            this.rotate();
            this.rotate();
        }
    }

    protected void setAccessoryValue(AccessoryValue accessoryValue) {
        if (!accessoryValue.equals(this.accessoryValue)) {
            this.image = null;
        }

        this.accessoryValue = accessoryValue;

    }

    @Override
    public void switched(AccessoryEvent event) {
        if (layoutTile != null && layoutTile.getSolenoidAccessoiry() != null && event.isEventFor(layoutTile.getSolenoidAccessoiry())) {
            setAccessoryValue(event.getValue());
        }
        if (this.reDrawListener != null) {
            this.reDrawListener.reDraw();
        }
    }

    /**
     * Depending on the status when OFF all lines are gray When Straight, line 1
     * and 4 are Green When curved line 1,2 and 3 are Red and 4 is Gray
     *
     * @param g2 the Graphic context
     * @param trackColor the color of the track
     */
    @Override
    public void renderTile(Graphics2D g2, Color trackColor) {
        Graphics2D g2d = (Graphics2D) g2.create();

        GeneralPath line1 = new GeneralPath();
        GeneralPath line2 = new GeneralPath();
        GeneralPath line3 = new GeneralPath();
        GeneralPath line4 = new GeneralPath();
        GeneralPath line5 = new GeneralPath();

        if (Direction.RIGHT.equals(direction)) {
            line1.moveTo(75, 60);
            line1.lineTo(80, 60);

            line2.moveTo(75, 60);
            line2.curveTo(75, 60, 55, 60, 35, 40);

            line3.moveTo(5, 20);
            line3.curveTo(5, 20, 15, 20, 35, 40);

            line4.moveTo(5, 20);
            line4.lineTo(0, 20);

            line5.moveTo(75, 60);
            line5.lineTo(0, 60);

        } else {
            line1.moveTo(0, 60);
            line1.lineTo(5, 60);

            line2.moveTo(5, 60);
            line2.curveTo(5, 60, 25, 60, 45, 40);

            line3.moveTo(75, 20);
            line3.curveTo(75, 20, 65, 20, 45, 40);

            line4.moveTo(75, 20);
            line4.lineTo(80, 20);

            line5.moveTo(5, 60);
            line5.lineTo(80, 60);
        }

        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, DEFAULT_WIDTH * 2, DEFAULT_HEIGHT * 2);

        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (accessoryValue == null) {
            accessoryValue = AccessoryValue.OFF;
        }

        switch (accessoryValue) {
            case GREEN:
                g2d.setPaint(Color.lightGray);
                g2d.draw(line2);
                g2d.draw(line3);
                g2d.draw(line4);
                g2d.setPaint(Color.green);
                g2d.draw(line1);
                g2d.draw(line5);
                break;
            case RED:
                g2d.setPaint(Color.lightGray);
                g2d.draw(line5);
                g2d.setPaint(Color.red);
                g2d.draw(line1);
                g2d.draw(line2);
                g2d.draw(line3);
                g2d.draw(line4);
                break;
            default:
                g2d.setPaint(Color.lightGray);
                g2d.draw(line1);
                g2d.draw(line2);
                g2d.draw(line3);
                g2d.draw(line4);
                g2d.draw(line5);
                break;
        }
        g2d.dispose();
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

    @Override
    public void drawCenterPoint(Graphics2D g2d, Color color, double size) {
        Color c;
        if (!AccessoryValue.OFF.equals(this.accessoryValue)) {
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
