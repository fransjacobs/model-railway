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
package lan.wervel.jcs.ui.layout2.pathfinding;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lan.wervel.jcs.entities.TileBean;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import lan.wervel.jcs.ui.layout2.Tile;
import lan.wervel.jcs.ui.layout2.tiles2.TileFactory2;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TileLoader {

    private final Map<Point, Tile> tiles;

    public TileLoader() {
        this.tiles = new HashMap<>();
        loadTiles();
    }

    public Map<Point, Tile> getTiles() {
        return this.tiles;
    }
        
    private void loadTiles() {
        if (TrackServiceFactory.getTrackService() == null) {
            return;
        }

        Set<TileBean> beans = TrackServiceFactory.getTrackService().getTiles();
        Logger.trace("Start loading " + beans.size() + " Tiles. Currently there are " + this.tiles.size() + " tiles...");

        Set<Tile> snapshot = new HashSet<>();

        for (TileBean tb : beans) {
            Tile tile = TileFactory2.createTile(tb, true);
            snapshot.add(tile);
        }

        for (Tile t : snapshot) {
            tiles.put(t.getCenter(), t);
            //Alternative point(s) to be able to find all points
//            if (!t.getAltPoints().isEmpty()) {
//                Set<Point> alt = t.getAltPoints();
//                for (Point ap : alt) {
//                    this.altTiles.put(ap, t);
//                }
//            }
        }

        Logger.debug("Loaded " + this.tiles.size() + " from " + beans.size() + " tiles...");

    }
}
