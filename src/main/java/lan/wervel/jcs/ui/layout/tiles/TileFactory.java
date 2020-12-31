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
package lan.wervel.jcs.ui.layout.tiles;

import java.awt.Point;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.ui.layout.TileType;
import static lan.wervel.jcs.ui.layout.TileType.BLOCK;
import static lan.wervel.jcs.ui.layout.TileType.DIAGONAL;
import static lan.wervel.jcs.ui.layout.TileType.SENSOR;
import static lan.wervel.jcs.ui.layout.TileType.SIGNAL;
import static lan.wervel.jcs.ui.layout.TileType.TURNOUT;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.ui.layout.tiles.enums.Orientation;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TileFactory {

    private TileFactory() {

    }

    public static AbstractTile createTile(LayoutTile layoutTile) {
        if (layoutTile == null) {
            return null;
        }

        String tt = layoutTile.getTiletype();
        switch (tt) {
            case "StraightTrack":
                return new StraightTrack(layoutTile);
            case "DiagonalTrack":
                return new DiagonalTrack(layoutTile);
            case "TurnoutTile":
                return new TurnoutTile(layoutTile);
            case "SignalTile":
                return new SignalTile(layoutTile);
            case "SensorTile":
                return new SensorTile(layoutTile);
            case "BlockTile":
                return new BlockTile(layoutTile);
            default:
                return null;
        }
    }

    public static AbstractTile createTile(TileType tileType, Orientation orientation, Point center) {
        return createTile(tileType, orientation, Direction.CENTER, center);
    }

    public static AbstractTile createTile(TileType tileType, Orientation orientation, Direction direction, Point center) {
        AbstractTile tile = null;
        switch (tileType) {
            case STRAIGHT:
                tile = new StraightTrack(orientation, center);
                break;
            case DIAGONAL:
                tile = new DiagonalTrack(orientation, center);
                break;
            case TURNOUT:
                tile = new TurnoutTile(orientation, direction, center);
                break;
            case SIGNAL:
                tile = new SignalTile(orientation, center);
                break;
            case SENSOR:
                tile = new SensorTile(orientation, center);
                break;
            case BLOCK:
                tile = new BlockTile(orientation, center);
                break;
            default:
                Logger.warn("Unknown Tile Type " + tileType);
        }
        return tile;
    }

}
