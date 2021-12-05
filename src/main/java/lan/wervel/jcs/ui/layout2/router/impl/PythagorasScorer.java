/*
 * Copyright (C) 2020 frans.
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
package lan.wervel.jcs.ui.layout2.router.impl;

import lan.wervel.jcs.ui.layout2.router.Scorer;
import lan.wervel.jcs.ui.layout2.tiles2.AbstractTile2;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class PythagorasScorer implements Scorer<AbstractTile2> {

    @Override
    public double computeCost(AbstractTile2 from, AbstractTile2 to) {

        Logger.trace("From: " + from.getId() + " (" + from.getCenterX() + "," + from.getCenterY() + ") to: " + to.getId() + " (" + to.getCenterX() + "," + to.getCenterY() + ")");

        double dX = to.getCenterX() - from.getCenterX();
        double dY = to.getCenterY() - from.getCenterY();

        double a = Math.pow(dX, 2) + Math.pow(dY, 2);
        double c = Math.sqrt(a);

        Logger.trace(c + " is the distance between: " + from.getId() + " (" + from.getCenterX() + "," + from.getCenterY() + ") to: " + to.getId() + " (" + to.getCenterX() + "," + to.getCenterY() + ")");

        return c;
    }
}
