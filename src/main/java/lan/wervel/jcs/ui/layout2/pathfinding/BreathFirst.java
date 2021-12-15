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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lan.wervel.jcs.ui.layout2.Tile;
import lan.wervel.jcs.ui.layout2.tiles2.Block;
import lan.wervel.jcs.ui.layout2.tiles2.Curved;
import lan.wervel.jcs.ui.layout2.tiles2.Switch;
import org.tinylog.Logger;

/**
 * Inspired by https://www.peachpit.com/articles/article.aspx?p=101142
 * @author frans
 */
public class BreathFirst {

    private final Map<Point, Tile> tiles;
    private final Map<String, Tile> tileIdLookup;

    private final Map<Point, Tile> tileLookup;
    private final Map<Point, Tile> altTileLookup;

    public BreathFirst() {
        this.tiles = new HashMap<>();
        this.tileLookup = new HashMap<>();
        this.altTileLookup = new HashMap<>();
        this.tileIdLookup = new HashMap<>();
    }

    public Set<Tile> getTileSet() {
        Set<Tile> ts = new HashSet<>(this.tiles.values());
        return ts;
    }

    private void createLookup(Set<Tile> tiles) {
        for (Tile tile : tiles) {
            this.tileIdLookup.put(tile.getId(), tile);

            this.tileLookup.put(tile.getCenter(), tile);
            for (Point p : tile.getAltPoints()) {
                this.altTileLookup.put(p, tile);
            }
        }

        Logger.debug("Id Lookup: " + tileIdLookup.size() + " Tile lookup: " + tileLookup.size());

    }

    private Tile findTile(Point cp) {
        Tile result = this.tiles.get(cp);
        if (result == null) {
            //Logger.trace("Using alternative points...");
            result = this.altTileLookup.get(cp);
            if (result != null) {
                //Logger.trace("Found " + result + " in alt tiles");
            }
        }

        return result;
    }

    private void loadLayout() {
        TileLoader tl = new TileLoader();
        Map<Point, Tile> tm = tl.getTiles();

        Set<Point> ks = tm.keySet();
        for (Point p : ks) {
            this.tiles.put(p, tm.get(p));
        }
        Logger.trace("Loaded " + this.tiles.size() + " tiles...");
    }

    
    private Set<Tile> findNeighbours(Set<Point> adjacent) {
        Set<Tile> ats = new HashSet<>();
        for (Point p : adjacent) {
            Tile a = findTile(p);
            if (a != null) {
                ats.add(a);
            }
        }
        return ats;
    }

    private List<Tile> constructPath(Tile from, Tile to) {
        LinkedList<Tile> path = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        while (to.getParent() != null) {
            sb.append(to.getId());
            sb.append(" ");
            path.addFirst(to);
            to = to.getParent();
        }

        return checkRoute(from, path);
    }

    //Check if the toFrom is valid
    private List<Tile> checkRoute(Tile from, List<Tile> toFrom) {

        ArrayList<Tile> path = new ArrayList<>();
        path.add(from);
        path.addAll(toFrom);

        StringBuilder sb = new StringBuilder();
        sb.append("Path from: ");
        for (int i = 0; i < path.size(); i++) {
            Tile t = path.get(i);
            sb.append(t.getId());
            if (i + 1 < path.size()) {
                sb.append(" -> ");
            }
        }
        Logger.trace(sb);

        int x = -1, y = -1, px = -1, py = -1;
        String xdir = "none", pxdir = "none";
        String ydir = "none", pydir = "none";
        Tile t = null, pt = null;
        boolean directionChanged = false;
        for (int i = 0; i < path.size(); i++) {
            pt = t;
            t = path.get(i);
            if (i == 0) {
                Logger.trace(t.getId() + " is start");
            } else if (i == path.size() - 1) {
                Logger.trace(t.getId() + " is end");
            } else {
                Logger.trace(t.getId() + " " + t.getOrientation() + " " + t.getDirection());
            }

            if (i == 0) {
                px = t.getCenterX();
                py = t.getCenterY();
                pxdir = "none";
                pydir = "none";
                pt = t;
            } else {
                px = x;
                py = y;
                pxdir = xdir;
                pydir = ydir;
            }

            x = t.getCenterX();
            y = t.getCenterY();

            //Travel direction
            int dx = x - px;
            int dy = y - py;

            String travelX;
            if (dx == 0) {
                if ((t instanceof Curved || t instanceof Switch) && "none".equals(pydir)) {
                    //keep current x direction
                    travelX = pxdir;
                } else {
                    travelX = "none";
                }
            } else if (dx > 0) {
                travelX = "east";
            } else {
                travelX = "west";
            }

            String travelY;
            if (dy == 0) {
                if ((t instanceof Curved || t instanceof Switch) && "none".equals(pxdir)) {
                    //keep current y direction
                    travelY = pydir;
                } else {
                    travelY = "none";
                }
            } else if (dy > 0) {
                travelY = "south";
            } else {
                travelY = "north";
            }

            xdir = travelX;
            ydir = travelY;

            Logger.trace("x: " + x + " px: " + px + "; y: " + y + " py: " + py + "; Travel: " + pxdir + " -> " + xdir + "; " + pydir + " -> " + ydir + "...");

            if (!xdir.equals(pxdir) && !"none".equals(pxdir) && !"none".equals(xdir)) {
                Logger.trace("X direction change!");
                directionChanged = true;
            }
            if (!ydir.equals(pydir) && !"none".equals(pydir) && !"none".equals(ydir)) {
                Logger.trace("Y direction change!");
                directionChanged = true;
            }
        }

        if (directionChanged) {
            Logger.trace("INVALID ROUTE!");
            return null;
        }
        return toFrom;
    }

    private boolean checkEdges(Tile from, Tile to) {
        Set<Point> fromEdges = from.getConnectingPoints();
        Set<Point> toEdges = to.getConnectingPoints();
        boolean result = false;
        for (Point p : fromEdges) {
            if (toEdges.contains(p)) {
                result = true;
                Logger.trace("From " + from.getId() + " connected with " + to.getId() + " on " + p);
                break;
            }
        }
        return result;
    }

    public List<Tile> search(Tile from, Tile to) {
        Logger.trace("From: " + from.getId() + " (" + from.getCenterX() + "," + from.getCenterY() + ") to: " + to.getId() + " (" + to.getCenterX() + "," + to.getCenterY() + ")...");

        LinkedList<String> visited = new LinkedList<>();

        // list of nodes to visit (sorted)
        LinkedList<String> searchList = new LinkedList<>();
        Logger.trace("Adding from " + from.getId() + " to the 'search' list");
        searchList.add(from.getId());
        from.setParent(null);

        while (!searchList.isEmpty()) {
            String nodeId = searchList.removeFirst();

            Tile node = this.tileIdLookup.get(nodeId);

            if (nodeId.equals(to.getId())) {
                // path found!
                Logger.trace("Path found from " + from.getId() + " to " + nodeId);
                List<Tile> route = constructPath(from, to);

                return route;
            } else {
                visited.add(nodeId);
                //get neighbour points
                Set<Point> ajp = node.getAdjacentPoints();
                // get the neighbour tiles
                Set<Tile> neighbours = findNeighbours(ajp);
                Logger.trace("Node " + node.getId() + " (" + node.getCenterX() + "," + node.getCenterY() + ") has " + ajp.size() + " adjacent points and " + neighbours.size() + " neighbours");

                for (Tile neighbor : neighbours) {
                    Logger.trace("Checking neighbours of node " + node.getId() + "; Neighbor: " + neighbor.getId());

                    Logger.trace("F: " + node.getId() + " O: " + node.getOrientation() + " D: " + node.getDirection() + " T: " + neighbor.getId() + " O: " + neighbor.getOrientation() + " D: " + neighbor.getDirection());

                    //edge point of neighbor should match the edge of the tile
                    if (!visited.contains(neighbor.getId()) && !searchList.contains(neighbor.getId())) {
                        if (checkEdges(node, neighbor)) {
                            neighbor.setParent(node);
                            searchList.add(neighbor.getId());
                            Logger.trace("Added neighbor " + neighbor.getId() + " to searchlist. Node " + node.getId() + " is parent of " + neighbor.getId());
                        }
                    }
                }
            }
        }

        // no path found
        Logger.trace("No path found from " + from.getId() + " to " + to.getId());
        return null;
    }

    public void createGraph(Set<Tile> layout) {
        if(this.tiles.size() != layout.size()) {
            this.tiles.clear();
            
            for(Tile t : layout) {
                this.tiles.put(t.getCenter(), t);
            }
        }
        
        createLookup(layout);
        Logger.trace("Created lookup map with " + tileLookup.size() + " entries from " + tiles.size() + " tiles...");
    }

    public List<List<Tile>> routeAll() {
        List<Block> blocks = new ArrayList<>();
        //Find blocks..
        Set<Tile> snapshot = new HashSet<>(tiles.values());
        Logger.trace("Layout has " + snapshot.size() + " tiles...");
        for (Tile b : snapshot) {
            if (b instanceof Block) {
                blocks.add((Block) b);
            }
        }
        Logger.trace("Layout has " + blocks.size() + " blocks...");

        List<List<Tile>> routes = new ArrayList<>();

        for (Block from : blocks) {
            for (Block to : blocks) {
                if (!from.getId().equals(to.getId())) {
                    Logger.trace("Route from: " + from.getId() + " (" + from.getCenterX() + "," + from.getCenterY() + ") to: " + to.getId() + " (" + to.getCenterX() + "," + to.getCenterY() + ")...");

                    List<Tile> route = search(from, to);
                    if (route != null && !route.isEmpty()) {
                        routes.add(route);
                    }
                }
            }
        }

        Logger.debug("Found " + routes.size() + " routes...");
        return routes;
    }

    public static void main(String[] a) {
        BreathFirst bf = new BreathFirst();
        bf.loadLayout();
        bf.createGraph(bf.getTileSet());

        bf.routeAll();

        System.exit(0);
    }

}
