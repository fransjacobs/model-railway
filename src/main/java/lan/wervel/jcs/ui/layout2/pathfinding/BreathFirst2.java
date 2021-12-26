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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lan.wervel.jcs.ui.layout2.Tile;
import lan.wervel.jcs.ui.layout2.pathfinding.graph.Node;
import lan.wervel.jcs.ui.layout2.tiles2.Block;
import lan.wervel.jcs.ui.layout2.tiles2.Curved;
import lan.wervel.jcs.ui.layout2.tiles2.Switch;
import org.tinylog.Logger;

/**
 * Inspired by https://www.peachpit.com/articles/article.aspx?p=101142
 *
 * @author frans
 */
public class BreathFirst2 {

    //private Map<Point, Tile> tiles;
    private final Map<String, Node> tileIdLookup;

    //private final Map<Point, Tile> tileLookup;
    //private final Map<Point, Tile> altTileLookup;
    public BreathFirst2() {
        //this.tiles = new HashMap<>();
        //this.tileLookup = new HashMap<>();
        //this.altTileLookup = new HashMap<>();
        this.tileIdLookup = new HashMap<>();
    }

//    public Set<Tile> getTileSet() {
//        Set<Tile> ts = new HashSet<>(this.tiles.values());
//        return ts;
//    }
//    private void createLookup(Set<Tile> tiles) {
//        for (Tile tile : tiles) {
//            this.tileIdLookup.put(tile.getId(), tile);
//
//            this.tileLookup.put(tile.getCenter(), tile);
//            for (Point p : tile.getAltPoints()) {
//                this.altTileLookup.put(p, tile);
//            }
//        }
//
//        Logger.debug("Id Lookup: " + tileIdLookup.size() + " Tile lookup: " + tileLookup.size());
//
//    }
//    private Tile findTile(Point cp) {
//        Tile result = this.tiles.get(cp);
//        if (result == null) {
//            //Logger.trace("Using alternative points...");
//            result = this.altTileLookup.get(cp);
//            if (result != null) {
//                //Logger.trace("Found " + result + " in alt tiles");
//            }
//        }
//
//        return result;
//    }
//    private Set<Tile> findNeighbours(Set<Point> adjacent) {
//        Set<Tile> ats = new HashSet<>();
//        for (Point p : adjacent) {
//            Tile a = findTile(p);
//            if (a != null) {
//                ats.add(a);
//            }
//        }
//        return ats;
//    }
    private List<Node> constructPath(Node from, Node to) {
        LinkedList<Node> path = new LinkedList<>();
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
    private List<Node> checkRoute(Node from, List<Node> toFrom) {

        ArrayList<Node> path = new ArrayList<>();
        path.add(from);
        path.addAll(toFrom);

        StringBuilder sb = new StringBuilder();
        sb.append("Path from: ");
        for (int i = 0; i < path.size(); i++) {
            Node t = path.get(i);
            sb.append(t.getId());
            if (i + 1 < path.size()) {
                sb.append(" -> ");
            }
        }
        Logger.trace(sb);

        int x = -1, y = -1, px = -1, py = -1;
        String xdir = "none", pxdir = "none";
        String ydir = "none", pydir = "none";
        Node t = null, pt = null;
        boolean directionChanged = false;
        for (int i = 0; i < path.size(); i++) {
            pt = t;
            t = path.get(i);
            if (i == 0) {
                Logger.trace(t.getId() + " is start");
            } else if (i == path.size() - 1) {
                Logger.trace(t.getId() + " is end");
            } else {
                Logger.trace(t.getId() + " " + t.getTile().getOrientation() + " " + t.getTile().getDirection());
            }

            if (i == 0) {
                px = t.getCP().x;
                py = t.getCP().y;
                pxdir = "none";
                pydir = "none";
                pt = t;
            } else {
                px = x;
                py = y;
                pxdir = xdir;
                pydir = ydir;
            }

            x = t.getCP().x;
            y = t.getCP().y;

            //Travel direction
            int dx = x - px;
            int dy = y - py;

            String travelX;
            if (dx == 0) {
                if ((t.getTile() instanceof Curved || t.getTile() instanceof Switch) && "none".equals(pydir)) {
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
                if ((t.getTile() instanceof Curved || t.getTile() instanceof Switch) && "none".equals(pxdir)) {
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

    public List<Node> search(Node from, Node to) {
        Logger.trace("From: " + from.getId() + " (" + from.getCP().x + "," + from.getCP().y + ") to: " + to.getId() + " (" + to.getCP().x + "," + to.getCP().y + ")...");

        LinkedList<String> visited = new LinkedList<>();

        // list of nodes to visit (sorted)
        LinkedList<String> searchList = new LinkedList<>();
        
        Logger.trace("Adding from " + from.getId() + " to the 'search' list");
        searchList.add(from.getId());
        from.setParent(null);

        while (!searchList.isEmpty()) {
            String nodeId = searchList.removeFirst();

            Node node = this.tileIdLookup.get(nodeId);
            if(node == null) {
                Logger.error("Can't find node: "+nodeId);
            }

            if (nodeId.equals(to.getId())) {
                // path found!
                Logger.trace("Path found from " + from.getId() + " to " + nodeId);
                List<Node> route = constructPath(from, to);

                return route;
            } else {
                visited.add(nodeId);
                // get the neighbour nodes
                if(node != null && node.getNeighbors() !=null) {
                    Set<Node.Edge> neighbours = node.getNeighbors();
                    Logger.trace("Node " + node.getId() + " (" + node.getCP().x + "," + node.getCP().y + ") has " + neighbours.size() + " neighbours");
   
                    for (Node.Edge neighbor : neighbours) {
                        Logger.trace("Checking neighbours of node " + node.getId() + "; Neighbor: " + neighbor.getNode().getId());

                        Logger.trace("F: " + node.getId() + " T: " + neighbor.getNode().getId()+"..." );

                        if (!visited.contains(neighbor.getNode().getId()) && !searchList.contains(neighbor.getNode().getId())) {
                            neighbor.getNode().setParent(node);
                            searchList.add(neighbor.getNode().getId());
                            Logger.trace("Added neighbor " + neighbor.getNode().getId() + " to searchlist. Node " + node.getId() + " is parent of " + neighbor.getNode().getId());
                        }
                    }
                }
            }
        }

        // no path found
        Logger.trace("No path found from " + from.getId() + " to " + to.getId());
        return null;
    }

    public void createGraph() {
        LayoutAnalyzer la = new LayoutAnalyzer();

        la.createGraph();

        Set<Node> nodes = la.getGraph();

        this.tileIdLookup.clear();
        for (Node n : nodes) {
            this.tileIdLookup.put(n.getId(), n);
        }

        Logger.trace("Created lookup map with " + tileIdLookup.size() + " entries...");
    }

    public List<List<Node>> routeAll() {
        List<Node> blocks = new ArrayList<>();
        //Find blocks as routes gor from block to block..
        Set<Node> snapshot = new HashSet<>(tileIdLookup.values());
        
        Logger.trace("Layout has " + snapshot.size() + " nodes...");
        for (Node b : snapshot) {
            if (b.getTile() instanceof Block) {
                blocks.add(b);
            }
        }
        Logger.trace("Layout has " + blocks.size() / 2 + " blocks...");

        List<List<Node>> routes = new ArrayList<>();
        

        for (Node from : blocks) {
            for (Node to : blocks) {
                //Do not route from and to the same tile
                if (! (from.getId().equals(to.getId())) && !(from.getTile().getId().equals(to.getTile().getId()))) {
                    Logger.trace("\n-----------------------------------------");

                    Logger.trace("Route from: " + from.getId() + " to: " + to.getId() +"...");

                    List<Node> route = search(from, to);
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
        System.setProperty("trackServiceAlwaysUseDemo", "true");

        BreathFirst2 bf2 = new BreathFirst2();

        bf2.createGraph();

        bf2.routeAll();

        System.exit(0);
    }

}
