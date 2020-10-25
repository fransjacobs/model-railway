/*
 * Copyright (C) 2020 Frans Jacobs.
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
package lan.wervel.jcs.controller.cs2;

import java.io.Serializable;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.entities.enums.Direction;
import org.pmw.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class DirectionInfo implements Serializable {

    private Direction direction;

    public DirectionInfo(CanMessage locDirection) {
        parseMessage(locDirection);
    }

    private void parseMessage(CanMessage locDirection) {
        Logger.debug(locDirection);
        int[] data = locDirection.getResponse(0).getData();
        int dir = data[4];

        this.direction = Direction.cs2Get(dir);
    }

    @Override
    public String toString() {
        return "DirectionInfo{" + "direction=" + direction + '}';
    }

    public Direction getDirection() {
        return direction;
    }

}
