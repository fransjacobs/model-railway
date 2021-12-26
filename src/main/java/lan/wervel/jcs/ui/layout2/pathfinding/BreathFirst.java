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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lan.wervel.jcs.ui.layout2.pathfinding.graph.Node;
import lan.wervel.jcs.ui.layout2.tiles2.Curved;
import lan.wervel.jcs.ui.layout2.tiles2.Switch;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class BreathFirst {

    private final Map<String, Node> nodeCache;

    public BreathFirst() {
        this.nodeCache = new HashMap<>();
    }

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
        //return toFrom;
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
        Logger.trace("Search From: " + from.getId() + " to: " + to.getId());

        LinkedList<String> visited = new LinkedList<>();
        LinkedList<String> searchList = new LinkedList<>();

        Logger.trace("Adding from " + from.getId() + " to the 'search' list");
        searchList.add(from.getId());
        from.setParent(null);
        //Avoid routes trhough blocks ie a route from bkn- to bkn+ is invalid
        String fromBk = from.getTile().getId();

        while (!searchList.isEmpty()) {
            String nodeId = searchList.removeFirst();

            //if (nodeCache.containsKey(nodeId)) {
            Node node = nodeCache.get(nodeId);
            if (nodeId.equals(to.getId())) {
                List<Node> route = constructPath(from, to);

                if (route != null) {
                    Logger.trace("==========================================================");
                    Logger.trace("Path found from " + from.getId() + " to " + nodeId);
                    Logger.trace("==========================================================");
                }

                return route;
            } else {
                visited.add(nodeId);
                Set<Node.Edge> neighbours = node.getNeighbors();
                //Logger.trace("Node " + node);

                for (Node.Edge neighbor : neighbours) {
                    //Logger.trace("Checking neighbours of node " + node.getId() + "; Neighbor: " + neighbor.getNode().getId());

                    String nNid = neighbor.getNode().getTile().getId();

                    if (!nNid.equals(fromBk)) {
                        Logger.trace("Check: " + neighbor.getNodeId() + "...");

                        if (!visited.contains(neighbor.getNodeId()) && !searchList.contains(neighbor.getNodeId())) {
                            neighbor.getNode().setParent(node);
                            searchList.add(neighbor.getNodeId());
                            Logger.trace("Added neighbor node " + neighbor.getNodeId() + " to searchlist. Node " + node.getId() + " is parent of " + neighbor.getNodeId());
                        }
                    }
                }
            }
        }

        // no path found
        Logger.trace("No path found from " + from.getId() + " to " + to.getId());
        return null;
    }

    List<List<Node>> prepareFromToNodes() {
        List<List<Node>> fromToList = new ArrayList<>();

        Collection<Node> fromNodes = this.nodeCache.values();
        Collection<Node> toNodes = this.nodeCache.values();

        for (Node from : fromNodes) {
            for (Node to : toNodes) {
                if (from.isBlock() && to.isBlock() && from != to && !from.getTile().getId().equals(to.getTile().getId())) {
                    List<Node> fromTo = new ArrayList<>();
                    fromTo.add(from);
                    fromTo.add(to);
                    fromToList.add(fromTo);
                    //Logger.trace("From: "+from.getId()+" to: "+to.getId());
                }
            }
        }
        return fromToList;
    }

    public void createGraph() {
        LayoutAnalyzer la = new LayoutAnalyzer();
        la.createGraph();
        Set<Node> nodes = la.getGraph();

        this.nodeCache.clear();
        for (Node n : nodes) {
            this.nodeCache.put(n.getId(), n);
        }

        Logger.trace("Created lookup map with " + nodeCache.size() + " entries...");
    }

    public static void main(String[] a) {
        System.setProperty("trackServiceAlwaysUseDemo", "true");

        BreathFirst bf = new BreathFirst();

        bf.createGraph();

        List<List<Node>> candidateRoutes = bf.prepareFromToNodes();

        Logger.trace("Try to route " + candidateRoutes.size() + " Candidate Routes");

        List<List<Node>> driveways = new ArrayList<>();

        for (List<Node> fromTo : candidateRoutes) {
            Node from = fromTo.get(0);
            Node to = fromTo.get(1);
            List<Node> driveway = bf.search(from, to);
            if (driveway != null) {
                driveways.add(driveway);
            }
        }

        Logger.trace("Found " + driveways.size() + " driveways");
        System.exit(0);
    }

}
