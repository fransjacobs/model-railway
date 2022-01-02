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
package jcs.ui.layout2.pathfinding;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import jcs.ui.layout2.LayoutUtil;
import jcs.ui.layout2.Tile;
import org.tinylog.Logger;

/**
 * Analyze the layout, which consists out of tiles. Convert it to an Directed
 * Graph with nodes and edges.
 *
 * @author fransjacobs
 */
public class LayoutAnalyzer {

    private final Map<String, Node> graph;

    public LayoutAnalyzer() {
        this.graph = new HashMap<>();
    }

    public String nodeIdForAdjacentSwitch(Tile tile, Tile adjacentSwitch) {
        Orientation o = adjacentSwitch.getOrientation();
        int tileX = tile.getCenterX();
        int tileY = tile.getCenterY();
        int adjX = adjacentSwitch.getCenterX();
        int adjY = adjacentSwitch.getCenterY();
        switch (o) {
            case SOUTH:
                if (adjX == tileX && adjY != tileY) {
                    //North or South
                    if (adjX < tileX) {
                        //Common
                        return adjacentSwitch.getId();
                    } else {
                        //Green
                        return adjacentSwitch.getId() + "-G";
                    }
                } else {
                    //Red
                    return adjacentSwitch.getId() + "-R";
                }
            case WEST:
                //East
                if (adjX != tileX && adjY == tileY) {
                    //The common of a East L or R Switch 
                    if (adjX > tileX) {
                        //Common    
                        return adjacentSwitch.getId();
                    } else {
                        //Green    
                        return adjacentSwitch.getId() + "-G";
                    }
                } else {
                    //Red
                    return adjacentSwitch.getId() + "-R";
                }
            case NORTH:
                if (adjX == tileX && adjY != tileY) {
                    //North or South
                    if (adjX > tileX) {
                        //Common
                        return adjacentSwitch.getId();
                    } else {
                        //Green
                        return adjacentSwitch.getId() + "-G";
                    }
                } else {
                    //Red
                    return adjacentSwitch.getId() + "-R";
                }
            default:
                //East
                if (adjX != tileX && adjY == tileY) {
                    //The common of a East L or R Switch 
                    if (adjX < tileX) {
                        //Common    
                        return adjacentSwitch.getId();
                    } else {
                        //Green    
                        return adjacentSwitch.getId() + "-G";
                    }
                } else {
                    //Red
                    return adjacentSwitch.getId() + "-R";
                }
        }
    }

    private void createSwitchNodes(Tile zwitch) {
        List<String> nodeIds = new ArrayList<>();
        nodeIds.add(zwitch.getId());
        nodeIds.add(zwitch.getId() + "-G");
        nodeIds.add(zwitch.getId() + "-R");
        Point tcp = zwitch.getCenter();

        for (String id : nodeIds) {
            Node node;
            Set<Point> adjacentPoints;
            if (id.contains("-G")) {
                adjacentPoints = LayoutUtil.adjacentPointsFor(zwitch, AccessoryValue.GREEN);
            } else if (id.contains("-R")) {
                adjacentPoints = LayoutUtil.adjacentPointsFor(zwitch, AccessoryValue.RED);
            } else {
                adjacentPoints = LayoutUtil.adjacentPointsFor(zwitch, AccessoryValue.OFF);
            }

            if (this.graph.containsKey(id)) {
                node = this.graph.get(id);
            } else {
                node = new Node(id,true);
            }

            for (Point adj : adjacentPoints) {
                //Check whether adjacent is tile
                if (LayoutUtil.isTile(adj)) {
                    Tile t = LayoutUtil.findTile(adj);
                    Point cp = t.getCenter();
                    //Determine the nodeids of the adjacent nodes
                    String nodeId;
                    switch (t.getTileType()) {
                        case BLOCK:
                            //A block has 2 sides ie 2 nodes so get the nearest point which is  bk-n+/-
                            if (LayoutUtil.isPlusAdjacent(t, tcp)) {
                                cp = LayoutUtil.getPlusCenter(t);
                                nodeId = t.getId() + "+";
                            } else {
                                cp = LayoutUtil.getMinusCenter(t);
                                nodeId = t.getId() + "-";
                            }
                            break;
                        case SWITCH:
                            // As switch has 3 adjacent nodes, depending on the direction
                            nodeId = nodeIdForAdjacentSwitch(zwitch, t);
                            break;
                        default:
                            nodeId = t.getId();
                            break;
                    }

                    Edge e1 = new Edge(node.getId(), nodeId, LayoutUtil.euclideanDistance(tcp, cp));
                    node.addEdge(e1);
                    Logger.trace(e1);

                    if(node.getId().endsWith("-G")) {
                        Edge ec = new Edge(node.getId(), node.getId().replaceAll("-G",""), 0);
                        node.addEdge(ec);
                        Logger.trace(ec);
                    } 
                    else if(node.getId().endsWith("-R")) {
                        Edge ec = new Edge(node.getId(), node.getId().replaceAll("-R",""), 0);
                        node.addEdge(ec);
                        Logger.trace(ec);
                    } 
                    else  {
                        //Common! add the G and R
                        Edge eg = new Edge(node.getId(), node.getId() + "-G", 0);
                        node.addEdge(eg);
                        Logger.trace(eg);
                        Edge er = new Edge(node.getId(), node.getId() + "-R", 0);
                        node.addEdge(er);
                        Logger.trace(er);
                    }

                    //Common of a Switch has 3 edges.. sw-n -> adjent tile and sw-n -> sw-n-G and sw-n-R 
                }
            }
            this.graph.put(id, node);
            Logger.trace("Added " + node);
        }
    }

    private void createBlockNodes(Tile block) {
        //A block has 2 sides, a plus (+) and a minus (-) so 2 nodes
        List<String> nodeIds = new ArrayList<>();
        nodeIds.add(block.getId() + "+");
        nodeIds.add(block.getId() + "-");

        for (String id : nodeIds) {
            Node node;
            Point tcp;
            Point adj;
            if (id.contains("+")) {
                tcp = LayoutUtil.getPlusCenter(block);
                adj = LayoutUtil.getPlusAdjacent(block);
            } else {
                tcp = LayoutUtil.getMinusCenter(block);
                adj = LayoutUtil.getMinusAdjacent(block);
            }
            if (this.graph.containsKey(id)) {
                node = this.graph.get(id);
            } else {
                node = new Node(id);
            }

            //Check whether adjacent is tile
            if (LayoutUtil.isTile(adj)) {
                Tile t = LayoutUtil.findTile(adj);
                Point cp = t.getCenter();
                //Determine the nodeids of the adjacent nodes
                String nodeId;
                switch (t.getTileType()) {
                    case BLOCK:
                        //A block has 2 sides ie 2 nodes so get the nearest point which is  bk-n+/-
                        if (LayoutUtil.isPlusAdjacent(t, tcp)) {
                            cp = LayoutUtil.getPlusCenter(t);
                            nodeId = t.getId() + "+";
                        } else {
                            cp = LayoutUtil.getMinusCenter(t);
                            nodeId = t.getId() + "-";
                        }
                        break;
                    case SWITCH:
                        // As switch has 3 adjacent nodes, depending on the direction
                        nodeId = nodeIdForAdjacentSwitch(block, t);
                        break;
                    default:
                        nodeId = t.getId();
                        break;
                }

                Edge e1 = new Edge(node.getId(), nodeId, LayoutUtil.euclideanDistance(tcp, cp));
                node.addEdge(e1);
                Logger.trace(e1);
            }
            this.graph.put(id, node);
            Logger.trace("Added " + node);
        }
    }

    private void findEdgesFor(Node node) {
        Tile tile = LayoutUtil.findTile(node.getId());
        Point tcp = tile.getCenter();
        Set<Point> adjacentPoints = LayoutUtil.adjacentPointsFor(tile);

        //filter the points which do have a tile
        for (Point adj : adjacentPoints) {
            if (LayoutUtil.isTile(adj)) {
                Tile t = LayoutUtil.findTile(adj);
                Point cp = t.getCenter();

                //Determine the nodeid of the adjacent node
                String nodeId;
                switch (t.getTileType()) {
                    case BLOCK:
                        //A block has 2 sides ie 2 nodes so get the nearest point which is  bk-n+/-
                        if (LayoutUtil.isPlusAdjacent(t, tcp)) {
                            cp = LayoutUtil.getPlusCenter(t);
                            nodeId = t.getId() + "+";
                        } else {
                            cp = LayoutUtil.getMinusCenter(t);
                            nodeId = t.getId() + "-";
                        }
                        break;
                    case SWITCH:
                        // As switch has 3 adjacent nodes, depending on the direction
                        nodeId = nodeIdForAdjacentSwitch(tile, t);
                        break;
                    default:
                        nodeId = t.getId();
                        break;
                }
                Edge e1 = new Edge(node.getId(), nodeId, LayoutUtil.euclideanDistance(tcp, cp));
                node.addEdge(e1);
                Logger.trace(e1);
            }
        }
    }

    private void createNode(Tile tile) {
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
                    String id = tile.getId();
                    Node node;
                    if (graph.containsKey(id)) {
                        node = graph.get(id);
                        Logger.trace(node + " allready exists");
                    } else {
                        node = new Node(tile);
                        findEdgesFor(node);
                        this.graph.put(id, node);
                        Logger.trace("Added " + node);
                    }
                    break;
            }
        }
    }

    public List<List<Node>> getBlockToBlockNodes() {
        List<List<Node>> fromToList = new ArrayList<>();

        Collection<Node> fromNodes = this.graph.values();
        Collection<Node> toNodes = this.graph.values();

        for (Node from : fromNodes) {
            Tile fromTile = LayoutUtil.findTile(from.getId());
            boolean fromBlock = LayoutUtil.isBlock(from.getId());
            String fromTileId = fromTile.getId();
            for (Node to : toNodes) {
                Tile toTile = LayoutUtil.findTile(to.getId());
                boolean toBlock = LayoutUtil.isBlock(to.getId());
                String toTileId = toTile.getId();
                if (fromBlock && toBlock && !from.getId().equals(to.getId()) && !fromTileId.equals(toTileId)) {
                    List<Node> fromTo = new ArrayList<>();
                    fromTo.add(from);
                    fromTo.add(to);
                    fromToList.add(fromTo);
                }
            }
        }
        return fromToList;
    }

    public void buildGraph() {
        graph.clear();

        //Iterate through all the Tiles from the layout
        Collection<Tile> tiles = LayoutUtil.loadLayout(true).values();
        for (Tile tile : tiles) {
            Logger.trace("Evaluating tile: " + tile.getTileType() + ": " + tile.getId()); // + " @ (" + tile.getCenterX() + "," + tile.getCenterY() + ")...");
            //A Tile can result in one or more nodes
            createNode(tile);
        }
        Logger.trace("Graph has " + graph.size() + " nodes...");
    }

    public Map<String, Node> getGraph() {
        return graph;
    }

    public static void main(String[] a) {
        System.setProperty("trackServiceAlwaysUseDemo", "true");

        LayoutAnalyzer la = new LayoutAnalyzer();
        la.buildGraph();

        System.exit(0);
    }
}
