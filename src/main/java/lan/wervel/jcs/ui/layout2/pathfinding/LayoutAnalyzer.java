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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import lan.wervel.jcs.entities.enums.Orientation;
import static lan.wervel.jcs.entities.enums.Orientation.NORTH;
import static lan.wervel.jcs.entities.enums.Orientation.SOUTH;
import static lan.wervel.jcs.entities.enums.Orientation.WEST;
import lan.wervel.jcs.entities.enums.TileType;
import static lan.wervel.jcs.entities.enums.TileType.BLOCK;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.ui.layout2.LayoutUtil;
import lan.wervel.jcs.ui.layout2.Tile;
import lan.wervel.jcs.ui.layout2.pathfinding.graph.Node;
import org.tinylog.Logger;

/**
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
    
    private List<Node> getSwitchNode(Tile switchTile) {
        //A switch has 2 sides, but it depends on the setting of the switch
        //When the switch has the AccessoryValue Green the travel direction is
        //straight through in both sides.
        //When the switch has the AccessoryValue Red the travel direction
        //depends on the Orientation and Direction
        List<Node> nodes = new ArrayList<>();
        String idG = switchTile.getId() + "-G";
        String idR = switchTile.getId() + "-R";
        Node nodeGreen;
        Node nodeRed;
        
        if (!nodeCache.containsKey(idG)) {
            int x = switchTile.getCenterX();
            int y = switchTile.getCenterY();
            int w = switchTile.getWidth();
            int h = switchTile.getHeight();
            Orientation o = switchTile.getOrientation();
            Direction d = switchTile.getDirection();
            
            int oX = w / 2 + Tile.GRID;
            int oY = h / 2 + Tile.GRID;
            
            Point neighborGreen1, neighborGreen2, neighborRed1, neighborRed2;
            switch (o) {
                case SOUTH:
                    neighborGreen1 = new Point(x, y + oY);
                    neighborGreen2 = new Point(x, y - oY);
                    neighborRed1 = new Point(x, y + oY);
                    
                    if (Direction.LEFT.equals(d)) {
                        neighborRed2 = new Point(x - oX, y);
                    } else {
                        neighborRed2 = new Point(x + oX, y);
                    }
                    break;
                case WEST:
                    neighborGreen1 = new Point(x - oX, y);
                    neighborGreen2 = new Point(x + oX, y);
                    neighborRed1 = new Point(x - oX, y);
                    
                    if (Direction.LEFT.equals(d)) {
                        neighborRed2 = new Point(x, y - oY);
                    } else {
                        neighborRed2 = new Point(x, y + oY);
                    }
                    break;
                case NORTH:
                    neighborGreen1 = new Point(x, y - oY);
                    neighborGreen2 = new Point(x, y + oY);
                    neighborRed1 = new Point(x, y - oY);
                    
                    if (Direction.LEFT.equals(d)) {
                        neighborRed2 = new Point(x + oX, y);
                    } else {
                        neighborRed2 = new Point(x - oX, y);
                    }
                    break;
                default:
                    //East 
                    neighborGreen1 = new Point(x + oX, y);
                    neighborGreen2 = new Point(x - oX, y);
                    neighborRed1 = new Point(x + oX, y);
                    
                    if (Direction.LEFT.equals(d)) {
                        neighborRed2 = new Point(x, y + oY);
                    } else {
                        neighborRed2 = new Point(x, y - oY);
                    }
                    break;
            }
            
            nodeGreen = new Node(switchTile, idG);
            this.nodeCache.put(idG, nodeGreen);
            
            Tile nbtg1 = findTile(neighborGreen1);
            if (nbtg1 != null) {
                List<Node> nnbp = this.getNodes(nbtg1);
                for (Node n : nnbp) {
                    double ed = LayoutUtil.euclideanDistance(nodeGreen.getP(), neighborGreen1);
                    nodeGreen.addBranch(ed, n);
                }
            }
            
            Tile nbtg2 = findTile(neighborGreen2);
            if (nbtg2 != null) {
                List<Node> nnbp = this.getNodes(nbtg2);
                for (Node n : nnbp) {
                    double ed = LayoutUtil.euclideanDistance(nodeGreen.getP(), neighborGreen2);
                    nodeGreen.addBranch(ed, n);
                }
            }
            
            nodeRed = new Node(switchTile, idR);
            this.nodeCache.put(idR, nodeRed);
            
            Tile nbtr1 = findTile(neighborRed1);
            if (nbtr1 != null) {
                List<Node> nnbp = this.getNodes(nbtg1);
                for (Node n : nnbp) {
                    double ed = LayoutUtil.euclideanDistance(nodeRed.getP(), neighborRed1);
                    nodeRed.addBranch(ed, n);
                }
            }
            
            Tile nbtr2 = findTile(neighborRed2);
            if (nbtr2 != null) {
                List<Node> nnbp = this.getNodes(nbtr2);
                for (Node n : nnbp) {
                    double ed = LayoutUtil.euclideanDistance(nodeRed.getP(), neighborRed2);
                    nodeRed.addBranch(ed, n);
                }
            }

            //Logger.trace("Node: " + nodeGreen.getId() + " has " + nodeGreen.getNeighbors().size() + " Neighbor(s):");
            //Logger.trace("Node: " + nodeRed.getId() + " has " + nodeRed.getNeighbors().size() + " Neighbor(s):");
        } else {
            nodeGreen = this.nodeCache.get(idG);
            nodeRed = this.nodeCache.get(idR);
        }
        
        nodes.add(nodeGreen);
        nodes.add(nodeRed);
        
        return nodes;
    }
    
    private List<Node> getBlockNode(Tile block) {
        //A block has 2 sides, a plus (+) and a minue (-) hence 2 nodes are created
        //The 2 nodes are connected to each other.
        List<Node> nodes = new ArrayList<>();
        String idp = block.getId() + "+";
        String idm = block.getId() + "-";
        Node nodePlus;
        Node nodeMin;
        //Only check the + as it is a pair
        if (!nodeCache.containsKey(idp)) {
            int x = block.getCenterX();
            int y = block.getCenterY();
            int w = block.getWidth();
            int h = block.getHeight();
            Orientation o = block.getOrientation();
            
            Point cpPlus, cpMin, neighborPlus, neighborMin;
            switch (o) {
                case SOUTH:
                    cpPlus = new Point(x, y + h / 3);
                    neighborPlus = new Point(x, y + h / 3 + Tile.GRID * 2);
                    
                    cpMin = new Point(x, y - h / 3);
                    neighborMin = new Point(x, y - h / 3 - Tile.GRID * 2);
                    break;
                case WEST:
                    cpPlus = new Point(x - w / 3, y);
                    neighborPlus = new Point(x - w / 3 - Tile.GRID * 2, y);
                    
                    cpMin = new Point(x + w / 3, y);
                    neighborMin = new Point(x + w / 3 + Tile.GRID * 2, y);
                    break;
                case NORTH:
                    cpPlus = new Point(x, y - h / 3);
                    neighborPlus = new Point(x, y - h / 3 - Tile.GRID * 2);
                    
                    cpMin = new Point(x, y + h / 3);
                    neighborMin = new Point(x, y + h / 3 + Tile.GRID * 2);
                    break;
                default:
                    //East 
                    cpPlus = new Point(x + w / 3, y);
                    neighborPlus = new Point(x + w / 3 + 40, y);
                    
                    cpMin = new Point(x - w / 3, y);
                    neighborMin = new Point(x - w / 3 - 40, y);
                    break;
            }
            
            nodePlus = new Node(block, idp, cpPlus);
            this.nodeCache.put(idp, nodePlus);
            
            nodeMin = new Node(block, idm, cpMin);
            this.nodeCache.put(idm, nodeMin);

            //the first is from + to -
            double d = LayoutUtil.euclideanDistance(cpMin, cpPlus);
            nodePlus.addBranch(d, nodeMin);
            
            Tile tnbp = findTile(neighborPlus);
            if (tnbp != null) {
                List<Node> nnbp = this.getNodes(tnbp);
                for (Node n : nnbp) {
                    d = LayoutUtil.euclideanDistance(cpPlus, neighborPlus);
                    nodePlus.addBranch(d, n);
                }
            }

            // and from - to +
            d = LayoutUtil.euclideanDistance(cpPlus, cpMin);
            nodeMin.addBranch(d, nodePlus);
            Tile tnbm = this.findTile(neighborMin);
            if (tnbm != null) {
                List<Node> nnbp = this.getNodes(tnbm);
                for (Node n : nnbp) {
                    d = LayoutUtil.euclideanDistance(cpMin, neighborMin);
                    nodeMin.addBranch(d, n);
                }
            }

            //Logger.trace("Node: " + nodePlus.getId() + " has " + nodePlus.getNeighbors().size() + " Neighbor(s):");
            //Logger.trace("Node: " + nodeMin.getId() + " has " + nodeMin.getNeighbors().size() + " Neighbor(s):");
        } else {
            nodePlus = this.nodeCache.get(idp);
            nodeMin = this.nodeCache.get(idm);
        }
        
        nodes.add(nodePlus);
        nodes.add(nodeMin);
        
        return nodes;
    }
    
    private List<Node> getCuvedNode(Tile tile) {
        List<Node> nodes = new ArrayList<>();
        String id = tile.getId();
        Node node;
        
        if (!nodeCache.containsKey(id)) {
            node = new Node(tile);
            this.nodeCache.put(id, node);
            
            int x = tile.getCenterX();
            int y = tile.getCenterY();
            int w = tile.getWidth();
            int h = tile.getHeight();
            Orientation o = tile.getOrientation();
            
            int oX = w / 2 + Tile.GRID;
            int oY = h / 2 + Tile.GRID;
            
            Point neighbor1, neighbor2;
            switch (o) {
                case SOUTH:
                    neighbor1 = new Point(x - oX, y);
                    neighbor2 = new Point(x, y + oY);
                    break;
                case WEST:
                    neighbor1 = new Point(x - oX, y);
                    neighbor2 = new Point(x, y - oY);
                    break;
                case NORTH:
                    neighbor1 = new Point(x + oX, y);
                    neighbor2 = new Point(x, y - oY);
                    break;
                default:
                    //EAST
                    neighbor1 = new Point(x + oX, y);
                    neighbor2 = new Point(x, y + oY);
                    break;
            }
            
            Tile nbt1 = findTile(neighbor1);
            if (nbt1 != null) {
                List<Node> adjn = this.getNodes(nbt1);
                for (Node n : adjn) {
                    double d = LayoutUtil.euclideanDistance(node.getP(), neighbor1);
                    node.addBranch(d, n);
                }
            }
            
            Tile nbt2 = findTile(neighbor2);
            if (nbt2 != null) {
                List<Node> adjn = this.getNodes(nbt2);
                for (Node n : adjn) {
                    double d = LayoutUtil.euclideanDistance(node.getP(), neighbor2);
                    node.addBranch(d, n);
                }
            }

            //Logger.trace("Node: " + node.getId() + " has " + node.getNeighbors().size() + " Neighbor(s):");
        } else {
            node = this.nodeCache.get(id);
        }
        
        nodes.add(node);
        return nodes;
    }
    
    private List<Node> getDefaultNode(Tile tile) {
        //Straight, sensor, signal
        List<Node> nodes = new ArrayList<>();
        String id = tile.getId();
        Node node;
        
        if (!nodeCache.containsKey(id)) {
            node = new Node(tile);
            this.nodeCache.put(id, node);
            
            int x = tile.getCenterX();
            int y = tile.getCenterY();
            int w = tile.getWidth();
            int h = tile.getHeight();
            Orientation o = tile.getOrientation();

            //Find the adjacent tiles
            Point neighbor1, neighbor2;
            if (Orientation.EAST.equals(o) || Orientation.WEST.equals(o)) {
                int oX = w / 2 + Tile.GRID;
                neighbor1 = new Point(x + oX, y);
                neighbor2 = new Point(x - oX, y);
            } else {
                int oY = h / 2 + Tile.GRID;
                neighbor1 = new Point(x, y + oY);
                neighbor2 = new Point(x, y - oY);
            }
            
            Tile nbt1 = findTile(neighbor1);
            if (nbt1 != null) {
                List<Node> adjn = this.getNodes(nbt1);
                for (Node n : adjn) {
                    double d = LayoutUtil.euclideanDistance(node.getP(), neighbor1);
                    node.addBranch(d, n);
                }
            }
            
            
           // dit vind een block op cente punt maar moet een block 2+mvidnen....
            Tile nbt2 = findTile(neighbor2);
            if (nbt2 != null) {
                List<Node> adjn = this.getNodes(nbt2);
                for (Node n : adjn) {
                    double d = LayoutUtil.euclideanDistance(node.getP(), neighbor2);
                    node.addBranch(d, n);
                }
            }

            //Logger.trace("Node: " + node.getId() + " has " + node.getNeighbors().size() + " Neighbor(s):");
        } else {
            node = this.nodeCache.get(id);
        }
        
        nodes.add(node);
        return nodes;
    }
    
    private List<Node> getNodes(Tile tile) {
        List<Node> nodes = null;
        if (tile != null && tile.getTileType() != null) {
            TileType tt = tile.getTileType();
            switch (tt) {
                case BLOCK:
                    nodes = getBlockNode(tile);
                    break;
                case SWITCH:
                    nodes = getSwitchNode(tile);
                    break;
                case CURVED:
                    nodes = getCuvedNode(tile);
                    break;
                default:
                    nodes = getDefaultNode(tile);
                    break;
            }
        }
        if (nodes == null) {
            nodes = Collections.EMPTY_LIST;
        }
        return nodes;
    }
    
    public void createGraph() {
        loadLayout();
        nodeCache.clear();
        graph.clear();
        Set<Node> nodes = new HashSet<>();

        //Iterate trough all Tiles
        Set<Point> keySet = this.tiles.keySet();
        for (Point cp : keySet) {
            Tile tile = this.tiles.get(cp);
            
            Logger.trace("Evaluating " + tile.getTileType() + ": " + tile.getId() + " @ (" + tile.getCenterX() + "," + tile.getCenterY() + ")...");
            List<Node> tileNodes = getNodes(tile);
            
            Logger.trace("Found " + tileNodes.size()+ " node for " + tile.getId() + "...");
            
            for(Node n : tileNodes) {
                this.logNode(n);
            }
            
            nodes.addAll(tileNodes);
        }
        
        this.graph.addAll(nodes);
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
    
    public Node aStar(Node from, Node to) {
        
        PriorityQueue<Node> closedList = new PriorityQueue<>();
        PriorityQueue<Node> openList = new PriorityQueue<>();
        
        from.f = from.g + from.calculateHeuristic(to);
        openList.add(from);
        
        while (!openList.isEmpty()) {
            Node n = openList.peek();
            if (n == to) {
                return n;
            }
            
            for (Node.Edge edge : n.getNeighbors()) {
                Node m = edge.getNode();
                double totalWeight = n.g + edge.getWeight();
                
                if (!openList.contains(m) && !closedList.contains(m)) {
                    m.setParent(n);
                    m.g = totalWeight;
                    m.f = m.g + m.calculateHeuristic(to);
                    openList.add(m);
                } else {
                    if (totalWeight < m.g) {
                        m.setParent(n);
                        m.g = totalWeight;
                        m.f = m.g + m.calculateHeuristic(to);
                        
                        if (closedList.contains(m)) {
                            closedList.remove(m);
                            openList.add(m);
                        }
                    }
                }
            }
            openList.remove(n);
            closedList.add(n);
        }
        return null;
    }
    
    private void logPath(Node to) {
        Node n = to;
        
        if (n == null) {
            return;
        }
        
        List<String> ids = new ArrayList<>();
        
        while (n.getParent() != null) {
            ids.add(n.getId());
            n = n.getParent();
        }
        ids.add(n.getId());
        
        Collections.reverse(ids);
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            sb.append(id);
            sb.append(" ");
        }
        Logger.debug(sb.toString());
    }
    
    private void logNode(Node node) {
        
        String id = node.getId();
        Set<Node.Edge> neighbors = node.getNeighbors();
        
        List<String> neigborIds = new ArrayList<>();
        
        for (Node.Edge e : neighbors) {
            Node n = e.getNode();
            //double d = e.getWeight();
            if (n != null) {
                neigborIds.add(n.getId());
            } else {
                Logger.error("Edge without node on node: " + id);
            }
        }
        
        Logger.debug("Node " + id + " has " + neighbors.size() + " neighbors: " + neigborIds);
        
    }
    
    public static void main(String[] a) {
        System.setProperty("trackServiceAlwaysUseDemo", "true");
        
        LayoutAnalyzer la = new LayoutAnalyzer();
        la.createGraph();
        
        for (Node n : la.graph) {
            la.logNode(n);
        }

        //Node f = la.nodeCache.get("bk-1+");
        //Node t = la.nodeCache.get("bk-3+");
        //Logger.debug("From: " + f.getId() + " Tile: " + f.getTile());
        //Logger.debug("To: " + t.getId() + " Tile: " + t.getTile());
        //Node res = la.aStar(f, t);
        //la.logPath(res);
        System.exit(0);
    }
}
