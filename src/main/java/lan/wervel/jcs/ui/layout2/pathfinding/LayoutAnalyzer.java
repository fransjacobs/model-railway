/*
 * Copyright (C) 2021 fransjacobs.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.TileType;
import static lan.wervel.jcs.entities.enums.TileType.BLOCK;
import lan.wervel.jcs.ui.layout2.LayoutUtil;
import lan.wervel.jcs.ui.layout2.Tile;
import lan.wervel.jcs.ui.layout2.pathfinding.graph.Node;
import org.tinylog.Logger;

/**
 * Analyze the layout, which consists out of tiles. Convert it to an Graph with
 * nodes and edges so that we can route the layout from block to block
 *
 * @author fransjacobs
 */
public class LayoutAnalyzer {

    private final Map<Point, Tile> tiles;
    private final Map<Point, Tile> tileLookup;
    private final Map<Point, Tile> altTileLookup;
    private final Map<String, Node> nodeCache;

    private final Set<Node> graph;

    public LayoutAnalyzer() {
        this.tiles = new HashMap<>();
        this.tileLookup = new HashMap<>();
        this.altTileLookup = new HashMap<>();
        this.nodeCache = new HashMap<>();
        this.graph = new HashSet<>();
    }

    private Tile findTile(Point cp) {
        Tile result = this.tiles.get(cp);
        if (result == null) {
            result = this.altTileLookup.get(cp);
            if (result != null) {
            }
        }
        return result;
    }

    private boolean isTile(Point cp) {
        return findTile(cp) != null;
    }

    private void createSwitchNodes(Tile tile) {
        //A switch has 2 sides, but it depends on the setting of the switch
        //When the switch has the AccessoryValue Green the travel direction is
        //straight through in both sides.
        //When the switch has the AccessoryValue Red the travel direction
        //depends on the Orientation and Direction
        List<String> nodeIds = new ArrayList<>();
        nodeIds.add(tile.getId() + "-G");
        nodeIds.add(tile.getId() + "-R");
        Point cp = tile.getCenter();

        for (String id : nodeIds) {
            Node node;
            if (nodeCache.containsKey(id)) {
                node = nodeCache.get(id);
                if (node.isTileNotSet()) {
                    node.setTile(tile);
                }
            } else {
                node = new Node(tile, id);
                this.nodeCache.put(id, node);
            }

            if (node.isNeighborsNotSet()) {
                //Find the adjacent nodes
                List<Node> branches;
                if (id.contains("-G")) {
                    branches = getAdjacentNodesFor(tile, AccessoryValue.GREEN);
                } else {
                    branches = getAdjacentNodesFor(tile, AccessoryValue.RED);

                }

                for (Node n : branches) {
                    Point p = n.getCP();
                    double d = LayoutUtil.euclideanDistance(cp, p);
                    node.addBranch(d, n);
                }
            }
            Logger.trace("Added Node " + node);
        }
    }

    private void createBlockNodes(Tile tile) {
        //A block has 2 sides, a plus (+) and a minue (-) hence 2 nodes are created
        //The 2 nodes are connected to each other.
        List<String> nodeIds = new ArrayList<>();

        nodeIds.add(tile.getId() + "+");
        nodeIds.add(tile.getId() + "-");

        for (String id : nodeIds) {
            Node node;
            Point cp;
            if (nodeCache.containsKey(id)) {
                node = nodeCache.get(id);
                if (node.isTileNotSet()) {
                    node.setTile(tile);
                }
                cp = node.getCP();
            } else {
                if (id.contains("+")) {
                    cp = LayoutUtil.getPlusCenter(tile);
                } else {
                    cp = LayoutUtil.getMinusCenter(tile);
                }

                node = new Node(tile, id, cp);
                this.nodeCache.put(id, node);
            }

            if (node.isNeighborsNotSet()) {
                //Find the adjacent nodes
                List<Node> branches = getAdjacentNodesFor(tile);
                for (Node n : branches) {
                    Point p = n.getCP();
                    double d = LayoutUtil.euclideanDistance(cp, p);
                    node.addBranch(d, n);
                }
            }
            Logger.trace("Added Node " + node);
        }
    }

    private List<Node> getAdjacentNodesFor(Tile tile) {
        return getAdjacentNodesFor(tile, AccessoryValue.OFF);
    }

    private List<Node> getAdjacentNodesFor(Tile tile, AccessoryValue accessoryValue) {
        Point tcp = tile.getCenter();
        List<Node> adjacentNodes = new ArrayList<>();
        //Find the adjacent tiles
        Set<Point> adjacent = LayoutUtil.adjacentPointsFor(tile, accessoryValue);
        for (Point adj : adjacent) {
            if (isTile(adj)) {
                Tile t = findTile(adj);
                Point cp;
                List<String> nodeIds = new ArrayList<>();
                //String nodeId;
                switch (t.getTileType()) {
                    case BLOCK:
                        //A block has 2 sides so get the nearest point which is  bk-n+/-
                        if (LayoutUtil.isPlusAdjacent(t, tcp)) {
                            //Logger.trace("Tile: " + tile.getId() + " has " + adj + " on the + side of " + t.getId() + "...");
                            cp = LayoutUtil.getPlusCenter(t);
                            nodeIds.add(t.getId() + "+");
                        } else {
                            //Logger.trace("Tile: " + tile.getId() + " has " + adj + " on the - side of " + t.getId() + "...");
                            cp = LayoutUtil.getMinusCenter(t);
                            nodeIds.add(t.getId() + "-");
                        }
                        break;
                    case SWITCH:
                        // As switch has 2 adjacent nodes, depending on the direction
                        cp = t.getCenter();
                        //Logger.trace("From Tile: " + tile.getId() + " adj tile: " + t.getId() + " @ (" + t.getCenterX() + "," + t.getCenterY() + ")");
                        nodeIds.addAll(LayoutUtil.nodeIdsForAdjacentSwitch(tile, t));
                        break;
                    default:
                        //Single point tile
                        nodeIds.add(t.getId());
                        cp = t.getCenter();
                        break;
                }

                for (String nodeId : nodeIds) {
                    if (this.nodeCache.containsKey(nodeId)) {
                        adjacentNodes.add(this.nodeCache.get(nodeId));
                    } else {
                        Node node = new Node(nodeId, cp);
                        //add it to the cache for reuse 
                        this.nodeCache.put(nodeId, node);
                        adjacentNodes.add(node);
                    }
                }
            }
        }

        return adjacentNodes;
    }

    private void createNode(Tile tile) {
        String id = tile.getId();
        Node node;
        if (nodeCache.containsKey(id)) {
            node = nodeCache.get(id);
            if (node.isTileNotSet()) {
                node.setTile(tile);
            }
        } else {
            node = new Node(tile);
            this.nodeCache.put(id, node);
        }

        if (node.isNeighborsNotSet()) {
            //Find the adjacent nodes
            List<Node> branches = getAdjacentNodesFor(tile);
            Point cp = tile.getCenter();
            for (Node n : branches) {
                double d = LayoutUtil.euclideanDistance(cp, n.getCP());
                node.addBranch(d, n);
            }
        }

        Logger.trace("Added Node " + node);
    }

    private void createNodes(Tile tile) {
        if (tile != null && tile.getTileType() != null) {
            TileType tt = tile.getTileType();
            switch (tt) {
                case BLOCK:
                    createBlockNodes(tile);
                    break;
                case SWITCH:
                    createSwitchNodes(tile);
                    break;
                default:
                    //Straight, Sensor, Signal, Curved
                    createNode(tile);
                    break;
            }
        }
    }

    public void createGraph() {
        loadLayout();
        nodeCache.clear();
        graph.clear();

        //Iterate through all the Tiles from the layout
        Collection<Tile> layout = this.tiles.values();
        for (Tile tile : layout) {
            Logger.trace("Evaluating tile: " + tile.getTileType() + ": " + tile.getId() + " @ (" + tile.getCenterX() + "," + tile.getCenterY() + ")...");
            //A Tile can result in one or more nodes
            createNodes(tile);
        }
        
        //Add all nodes to the Graph
        for(Node node: nodeCache.values()) {
            Logger.trace("Node: "+node);
            this.graph.add(node);
        }
        Logger.trace("Graph has " + graph.size() + " nodes...");
    }

    public Set<Node> getGraph() {
        return graph;
    }

    private void loadLayout() {
        this.tiles.clear();
        this.tileLookup.clear();
        this.altTileLookup.clear();

        this.tiles.putAll(LayoutUtil.loadTiles(true));

        Set<Point> keySet = this.tiles.keySet();
        for (Point p : keySet) {
            Tile tile = this.tiles.get(p);
            this.tileLookup.put(tile.getCenter(), tile);
            for (Point ap : tile.getAltPoints()) {
                this.altTileLookup.put(ap, tile);
            }
        }
        Logger.trace("Loaded " + this.tiles.size() + " tiles...");
    }

//    public Node aStar(Node from, Node to) {
//
//        PriorityQueue<Node> closedList = new PriorityQueue<>();
//        PriorityQueue<Node> openList = new PriorityQueue<>();
//
//        from.f = from.g + from.calculateHeuristic(to);
//        openList.add(from);
//
//        while (!openList.isEmpty()) {
//            Node n = openList.peek();
//            if (n == to) {
//                return n;
//            }
//
//            for (Node.Edge edge : n.getNeighbors()) {
//                Node m = edge.getNode();
//                double totalWeight = n.g + edge.getWeight();
//
//                if (!openList.contains(m) && !closedList.contains(m)) {
//                    m.setParent(n);
//                    m.g = totalWeight;
//                    m.f = m.g + m.calculateHeuristic(to);
//                    openList.add(m);
//                } else {
//                    if (totalWeight < m.g) {
//                        m.setParent(n);
//                        m.g = totalWeight;
//                        m.f = m.g + m.calculateHeuristic(to);
//
//                        if (closedList.contains(m)) {
//                            closedList.remove(m);
//                            openList.add(m);
//                        }
//                    }
//                }
//            }
//            openList.remove(n);
//            closedList.add(n);
//        }
//        return null;
//    }
//
//    private void logPath(Node to) {
//        Node n = to;
//
//        if (n == null) {
//            return;
//        }
//
//        List<String> ids = new ArrayList<>();
//
//        while (n.getParent() != null) {
//            ids.add(n.getId());
//            n = n.getParent();
//        }
//        ids.add(n.getId());
//
//        Collections.reverse(ids);
//        StringBuilder sb = new StringBuilder();
//        for (String id : ids) {
//            sb.append(id);
//            sb.append(" ");
//        }
//        Logger.debug(sb.toString());
//    }
//
//    private void logNode(Node node) {
//
//        String id = node.getId();
//        Set<Node.Edge> neighbors = node.getNeighbors();
//
//        List<String> neigborIds = new ArrayList<>();
//
//        for (Node.Edge e : neighbors) {
//            Node n = e.getNode();
//            //double d = e.getWeight();
//            if (n != null) {
//                neigborIds.add(n.getId());
//            } else {
//                Logger.error("Edge without node on node: " + id);
//            }
//        }
//
//        Logger.debug("Node " + id + " has " + neighbors.size() + " neighbors: " + neigborIds);
//
//    }

    public static void main(String[] a) {
        System.setProperty("trackServiceAlwaysUseDemo", "true");

        LayoutAnalyzer la = new LayoutAnalyzer();
        la.createGraph();

//        for (Node n : la.graph) {
//            la.logNode(n);
//        }

        //Node f = la.nodeCache.get("bk-1+");
        //Node t = la.nodeCache.get("bk-3+");
        //Logger.debug("From: " + f.getId() + " Tile: " + f.getTile());
        //Logger.debug("To: " + t.getId() + " Tile: " + t.getTile());
        //Node res = la.aStar(f, t);
        //la.logPath(res);
        System.exit(0);
    }
}
