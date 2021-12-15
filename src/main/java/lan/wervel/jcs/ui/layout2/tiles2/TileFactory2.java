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
package lan.wervel.jcs.ui.layout2.tiles2;

import java.awt.Point;
import lan.wervel.jcs.entities.TileBean;
import lan.wervel.jcs.entities.enums.TileType;
import static lan.wervel.jcs.entities.enums.TileType.BLOCK;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.entities.enums.Orientation;
import org.tinylog.Logger;
import static lan.wervel.jcs.entities.enums.TileType.CURVED;
import lan.wervel.jcs.ui.layout2.Tile;

/**
 *
 * @author frans
 */
public class TileFactory2 {

    //create id here...
    private TileFactory2() {

    }

    private static int straightIdSeq;
    private static int curvedIdSeq;
    private static int switchIdSeq;
    private static int crossIdSeq;
    private static int signalIdSeq;
    private static int sensorIdSeq;
    private static int blockIdSeq;

    private static int getIdSeq(String id) {
        String idnr = id.substring(3);
        int idSeq = Integer.parseInt(idnr);
        return idSeq;
    }

    private static int getHeighestIdSeq(int currentId, int newId) {
        if (currentId < newId) {
            return newId;
        } else {
            return currentId;
        }
    }

    public static Tile createTile(TileBean tileBean, boolean drawOutline) {
        if (tileBean == null) {
            return null;
        }

        TileType tileType = tileBean.getTileType();
        AbstractTile2 tile = null;
        switch (tileType) {
            case STRAIGHT:
                tile = new Straight(tileBean);
                straightIdSeq = getHeighestIdSeq(straightIdSeq, getIdSeq(tileBean.getId()));
                tile.setIdSeq(straightIdSeq);
                break;
            case CURVED:
                tile = new Curved(tileBean);
                curvedIdSeq = getHeighestIdSeq(curvedIdSeq, getIdSeq(tileBean.getId()));
                tile.setIdSeq(curvedIdSeq);
                break;
            case SWITCH:
                tile = new Switch(tileBean);
                switchIdSeq = getHeighestIdSeq(switchIdSeq, getIdSeq(tileBean.getId()));
                tile.setIdSeq(switchIdSeq);
                break;
            case CROSS:
                tile = new Cross(tileBean);
                crossIdSeq = getHeighestIdSeq(crossIdSeq, getIdSeq(tileBean.getId()));
                tile.setIdSeq(crossIdSeq);
                break;
            case SIGNAL:
                tile = new Signal(tileBean);
                signalIdSeq = getHeighestIdSeq(signalIdSeq, getIdSeq(tileBean.getId()));
                tile.setIdSeq(signalIdSeq);
                break;
            case SENSOR:
                tile = new Sensor(tileBean);
                sensorIdSeq = getHeighestIdSeq(sensorIdSeq, getIdSeq(tileBean.getId()));
                tile.setIdSeq(sensorIdSeq);
                break;
            case BLOCK:
                tile = new Block(tileBean);
                blockIdSeq = getHeighestIdSeq(blockIdSeq, getIdSeq(tileBean.getId()));
                tile.setIdSeq(blockIdSeq);
                break;
            default:
                Logger.warn("Unknown Tile Type " + tileType);
        }

        if (tile != null) {
            tile.setDrawOutline(drawOutline);
        }
        return (Tile) tile;
    }

    public static Tile createTile(TileType tileType, Orientation orientation, Direction direction, Point center, boolean drawOutline) {
        AbstractTile2 tile = null;
        switch (tileType) {
            case STRAIGHT:
                tile = new Straight(orientation, center);
                break;
            case CURVED:
                tile = new Curved(orientation, center);
                break;
            case SWITCH:
                tile = new Switch(orientation, direction, center);
                break;
            case CROSS:
                return new Cross(orientation, direction, center);
            case SIGNAL:
                tile = new Signal(orientation, center);
                break;
            case SENSOR:
                tile = new Sensor(orientation, center);
                break;
            case BLOCK:
                tile = new Block(orientation, center);
                break;
            default:
                Logger.warn("Unknown Tile Type " + tileType);
        }

        if (tile != null) {
            tile.setDrawOutline(drawOutline);
        }
        return (Tile) tile;
    }

}