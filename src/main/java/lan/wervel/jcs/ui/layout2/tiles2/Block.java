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
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import lan.wervel.jcs.entities.TileBean;
import static lan.wervel.jcs.entities.TileBean.DEFAULT_WIDTH;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.ui.layout2.LayoutUtil;
import static lan.wervel.jcs.ui.layout2.LayoutUtil.DEFAULT_HEIGHT;

/**
 * Draw a OccupancyDetector
 *
 * @author frans
 */
public class Block extends AbstractTile2 implements Tile {

    private static int idSeq;

    public static final int BLOCK_WIDTH = LayoutUtil.DEFAULT_WIDTH * 3;
    public static final int BLOCK_HEIGHT = DEFAULT_HEIGHT * 3;

    public Block(TileBean tileBean) {
        super(tileBean);
        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
            this.width = BLOCK_WIDTH;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = BLOCK_HEIGHT;
        }
    }

    public Block(int x, int y) {
        this(Orientation.EAST, x, y);
    }

    public Block(Orientation orientation, int x, int y) {
        this(orientation, new Point(x, y));
    }

    public Block(Orientation orientation, Point center) {
        super(orientation, Direction.CENTER, center);
        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
            this.width = DEFAULT_WIDTH * 3;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT * 3;
        }
    }

    @Override
    protected final String getNewId() {
        idSeq++;
        return "bk-" + idSeq;
    }

    @Override
    public Set<Point> getAltPoints() {
        int x = this.center.x;
        int y = this.center.y;
        Set<Point> alternatives = new HashSet<>();

        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
            //West
            Point wp = new Point((x - DEFAULT_WIDTH), y);
            Point ep = new Point((x + DEFAULT_WIDTH), y);
            alternatives.add(wp);
            alternatives.add(ep);
        } else {
            Point np = new Point(x, (y - DEFAULT_HEIGHT));
            Point sp = new Point(x, (y + DEFAULT_HEIGHT));
            alternatives.add(np);
            alternatives.add(sp);
        }

        return alternatives;
    }

    @Override
    public void rotate() {
        super.rotate();
        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
            this.width = DEFAULT_WIDTH * 3;
            this.height = DEFAULT_HEIGHT;
        } else {
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT * 3;
        }
    }

    @Override
    public void renderTile(Graphics2D g2, Color trackColor, Color backgroundColor) {
        int x, y, w, h;

        x = 2;
        y = 10;
        w = DEFAULT_WIDTH * 3 - 4;
        h = 20;

        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        g2.setPaint(Color.darkGray);
        g2.drawRect(x, y, w, h);
        
        //Block needs to have a direction so travel from a to b or from - to +
        //so in east direction the block is -[ - bk-nn + ]- 
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
        g2.setPaint(Color.black);
        
        //a - at the start and end of the block
        g2.drawLine(x+4, y+10, x+10, y+10);
        g2.drawLine(w-10, y+10, w-4, y+10);
        //a | at the end of the block 
        g2.drawLine(w-7, y+7, w-7, y+13);

        drawName(g2);
    }

    @Override
    public void drawName(Graphics2D g2d) {

        switch (this.orientation) {
            case EAST:
                drawRotate(g2d, 16, GRID + 4, 0, getId());
                break;
            case WEST:
                drawRotate(g2d, 104, GRID - 4, 180, getId());
                break;
            case NORTH:
                drawRotate(g2d, 20, GRID + 4, 0, getId());
                break;
            case SOUTH:
                drawRotate(g2d, 100, GRID - 4, 180, getId());
                break;
        }
    }

    @Override
    protected void setIdSeq(int id) {
        idSeq = id;
    }
}
