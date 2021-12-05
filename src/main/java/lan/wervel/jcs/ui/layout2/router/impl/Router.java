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
package lan.wervel.jcs.ui.layout2.router.impl;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lan.wervel.jcs.ui.layout2.router.Graph;
import lan.wervel.jcs.ui.layout2.router.RouteFinder;
import lan.wervel.jcs.ui.layout2.tiles2.AbstractTile2;
import static org.h2.util.ThreadDeadlockDetector.init;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class Router {

    private final Map<Point, AbstractTile2> layout;

    private Graph<AbstractTile2> layoutGraph;
    private RouteFinder<AbstractTile2> routeFinder;

    public Router() {
        layout = new HashMap<>();
        init();
    }

    private Set<AbstractTile2> findAdjacentTiles(AbstractTile2 tile) {
        Set<AbstractTile2> adjacentTiles = new HashSet<>();

        Set<Point> adjacentPoints = tile.getAdjacentPoints();
        Logger.trace("Node " + tile.getId() + "; (" + tile.getCenterX() + "," + tile.getCenterY() + "); " + tile.getClass().getSimpleName() + "; has " + adjacentPoints.size() + "  adjacent points...");
        for (Point p : adjacentPoints) {
            if (layout.containsKey(p)) {
                AbstractTile2 adjacent = layout.get(p);
                Logger.trace("Found " + adjacent.getId() + "; (" + tile.getCenterX() + "," + tile.getCenterY() + "); " + tile.getClass().getSimpleName());
                adjacentTiles.add(adjacent);
            }
        }

        return adjacentTiles;
    }

    private Set<AbstractTile2> findConnections(Set<AbstractTile2> tiles) {
        Logger.trace("Search " + tiles.size() + " tiles for connections...");

        for (AbstractTile2 tile : tiles) {
            Set<AbstractTile2> adjacentTiles = findAdjacentTiles(tile);
            tile.setAdjacentTiles(adjacentTiles);
        }
        return tiles;

    }

    public void createGraph(Set<AbstractTile2> tiles) {

        for (AbstractTile2 tile : tiles) {
            layout.put(tile.getCenter(), tile);
            if (!tile.getAltPoints().isEmpty()) {
                Set<Point> alt = tile.getAltPoints();
                for (Point ap : alt) {
                    layout.put(ap, tile);
                }
            }
        }

        Set<AbstractTile2> nodes = findConnections(tiles);

        Logger.trace("Loaded " + nodes.size() + " Tiles...");

        Map<String, Set<String>> connections = new HashMap<>();

        for (AbstractTile2 n : nodes) {
            Set<AbstractTile2> ans = n.getAdjacentTiles();

            String id = n.getId();
            Set<String> neighbourIds = new HashSet<>();

            for (AbstractTile2 an : ans) {
                String nid = an.getId();
                neighbourIds.add(nid);
            }

            StringBuilder sb = new StringBuilder();
            for (String s : neighbourIds) {
                sb.append(s);
                sb.append(" ");
            }
            Logger.trace("Tile: " + id + " Connections: " + sb.toString());

            connections.put(id, neighbourIds);
        }

        Logger.trace("Created Tile Set with " + tiles.size() + " tiles and " + connections.size() + " connections...");

        layoutGraph = new Graph<>(nodes, connections);
        routeFinder = new RouteFinder<>(layoutGraph, new PythagorasScorer(), new PythagorasScorer());
    }

    public List<AbstractTile2> route(AbstractTile2 from, AbstractTile2 to) {

        AbstractTile2 fromNode = this.layoutGraph.getNode(from.getId());
        AbstractTile2 toNode = this.layoutGraph.getNode(to.getId());

        List<AbstractTile2> routeFromTo = this.routeFinder.findRoute(fromNode, toNode);

        Logger.trace("Route from " + from + " to " + to + "...");
        for (AbstractTile2 t : routeFromTo) {
            Logger.trace(t);
        }

        return routeFromTo;
    }

}
