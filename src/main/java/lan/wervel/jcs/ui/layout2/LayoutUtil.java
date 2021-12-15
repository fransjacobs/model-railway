/*
 * Copyright (C) 2021 frans.
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
package lan.wervel.jcs.ui.layout2;

import java.awt.Point;

/**
 *
 * @author frans
 */
public class LayoutUtil {

    public static final int GRID = 20;
    public static final int DEFAULT_WIDTH = GRID * 2;
    public static final int DEFAULT_HEIGHT = GRID * 2;

    public static final Point snapToGrid(Point p) {
        return snapToGrid(p.x, p.y);
    }

    /**
     * Snap coordinates to the nearest grid point
     *
     * @param x the X
     * @param y the Y
     * @return Coordinates which are the X en Y wrapped
     */
    public static final Point snapToGrid(int x, int y) {
        int steps = x / DEFAULT_WIDTH;
        int sx = steps;
        sx = sx * DEFAULT_WIDTH + GRID;

        steps = y / DEFAULT_HEIGHT;
        int sy = steps;
        sy = sy * DEFAULT_HEIGHT + GRID;

        return new Point(sx, sy);
    }

}
