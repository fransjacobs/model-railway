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
    public Set<Point> getAdjacentPoints() {
        Set<Point> adjacent = new HashSet<>();

        int oX = this.width / 2 + DEFAULT_WIDTH / 2;
        int oY = this.height / 2 + DEFAULT_HEIGHT / 2;

//        switch(from) {
//            case SOUTH:
//                break;
//            case WEST:
//                break;
//            case NORTH:
//                break;
//            default:
//                //EAST
//                
//        }
        switch (this.orientation) {
            case SOUTH:
                adjacent.add(new Point(this.getCenterX(), this.getCenterY() + oY));
                adjacent.add(new Point(this.getCenterX(), this.getCenterY() - oY));

                if (Direction.RIGHT.equals(this.direction)) {
                    adjacent.add(new Point(this.getCenterX() + oX, this.getCenterY()));
                } else if (Direction.LEFT.equals(this.direction)) {
                    adjacent.add(new Point(this.getCenterX() - oX, this.getCenterY()));
                }
                break;
            case WEST:
                adjacent.add(new Point(this.getCenterX() + oX, this.getCenterY()));
                adjacent.add(new Point(this.getCenterX() - oX, this.getCenterY()));

                if (Direction.RIGHT.equals(this.direction)) {
                    adjacent.add(new Point(this.getCenterX(), this.getCenterY() + oY));
                } else if (Direction.LEFT.equals(this.direction)) {
                    adjacent.add(new Point(this.getCenterX(), this.getCenterY() - oY));
                }
                break;
            case NORTH:
                adjacent.add(new Point(this.getCenterX(), this.getCenterY() + oY));
                adjacent.add(new Point(this.getCenterX(), this.getCenterY() - oY));

                if (Direction.RIGHT.equals(this.direction)) {
                    adjacent.add(new Point(this.getCenterX() - oX, this.getCenterY()));
                } else if (Direction.LEFT.equals(this.direction)) {
                    adjacent.add(new Point(this.getCenterX() + oX, this.getCenterY()));
                }
                break;
            default:
                //EAST
                adjacent.add(new Point(this.getCenterX() + oX, this.getCenterY()));
                adjacent.add(new Point(this.getCenterX() - oX, this.getCenterY()));
                if (Direction.RIGHT.equals(this.direction)) {
                    adjacent.add(new Point(this.getCenterX(), this.getCenterY() - oY));
                } else if (Direction.LEFT.equals(this.direction)) {
                    adjacent.add(new Point(this.getCenterX(), this.getCenterY() + oY));
                }
                break;
        }

        return adjacent;
    }

    @Override
    public Set<Point> getConnectingPoints() {
        //Get the straight endge points
        Set<Point> connecting = super.getConnectingPoints();
        int x = this.getCenterX();
        int y = this.getCenterY();

        int ox = this.width / 2;
        int oy = this.height / 2;

        if (Direction.RIGHT.equals(this.direction)) {
            switch (this.orientation) {
                case SOUTH:
                    connecting.add(new Point(x + ox, y));
                    break;
                case WEST:
                    connecting.add(new Point(x, y + oy));
                    break;
                case NORTH:
                    connecting.add(new Point(x - ox, y));
                    break;
                default:
                    //EAST
                    connecting.add(new Point(x, y - oy));
                    break;
            }
        } else if (Direction.LEFT.equals(this.direction)) {
            switch (this.orientation) {
                case SOUTH:
                    connecting.add(new Point(x - ox, y));
                    break;
                case WEST:
                    connecting.add(new Point(x, y - oy));
                    break;
                case NORTH:
                    connecting.add(new Point(x + ox, y));
                    break;
                default:
                    //EAST
                    connecting.add(new Point(x, y + oy));
                    break;
            }
        }

        return connecting;
    }

    @Override
    public Point getWest() {
        if (Orientation.EAST.equals(this.orientation) || Orientation.WEST.equals(this.orientation)) {
            //Horizontal
            return new Point(this.center.x - this.width / 2, this.center.y);
        } else {
            if ((Direction.RIGHT.equals(this.direction) && Orientation.NORTH.equals(this.orientation))
                    || (Direction.LEFT.equals(this.direction) && Orientation.SOUTH.equals(this.orientation))) {
                return new Point(this.center.x - this.width / 2, this.center.y);
            } else {
                return null;
            }
        }
    }

    @Override
    public Point getEast() {
        if (Orientation.EAST.equals(this.orientation) || Orientation.WEST.equals(this.orientation)) {
            //Horizontal
            return new Point(this.center.x + this.width / 2, this.center.y);
        } else {
            if ((Direction.RIGHT.equals(this.direction) && Orientation.SOUTH.equals(this.orientation))
                    || (Direction.LEFT.equals(this.direction) && Orientation.NORTH.equals(this.orientation))) {
                return new Point(this.center.x + this.width / 2, this.center.y);
            } else {
                return null;
            }
        }
    }

    @Override
    public Point getSouth() {
        if (Orientation.NORTH.equals(this.orientation) || Orientation.SOUTH.equals(this.orientation)) {
            //Vertical
            return new Point(this.center.x, this.center.y + this.height / 2);
        } else {
            if ((Direction.RIGHT.equals(this.direction) && Orientation.WEST.equals(this.orientation))
                    || (Direction.LEFT.equals(this.direction) && Orientation.EAST.equals(this.orientation))) {
                return new Point(this.center.x, this.center.y + this.height / 2);
            } else {
                return null;
            }
        }
    }

    @Override
    public Point getNorth() {
        if (Orientation.NORTH.equals(this.orientation) || Orientation.SOUTH.equals(this.orientation)) {
            //Vertical
            return new Point(this.center.x, this.center.y - this.height / 2);
        } else {
            if ((Direction.RIGHT.equals(this.direction) && Orientation.EAST.equals(this.orientation))
                    || (Direction.LEFT.equals(this.direction) && Orientation.WEST.equals(this.orientation))) {
                return new Point(this.center.x, this.center.y - this.height / 2);
            } else {
                return null;
            }
        }
    }

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }
}
