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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class BreathFirst {

    private final LayoutAnalyzer layoutAnalyzer;
    private final Map<String, Node> nodeCache;

    public BreathFirst() {
        this.nodeCache = new HashMap<>();
        this.layoutAnalyzer = new LayoutAnalyzer();
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

        logRoute(from, path);
        return path;
    }

    //Check if the path is valid
    private void logRoute(Node from, List<Node> path) {

        StringBuilder sb = new StringBuilder();
        sb.append("Path: ");
        sb.append(from.getId());
        sb.append(" -> ");
        for (int i = 0; i < path.size(); i++) {
            Node t = path.get(i);
            sb.append(t.getId());
            if (i + 1 < path.size()) {
                sb.append(" -> ");
            }
        }
        Logger.trace(sb);
    }

    public List<Node> search(Node from, Node to) {
        //Logger.trace("Search from: " + from.getId() + " to: " + to.getId());

        LinkedList<String> visited = new LinkedList<>();
        LinkedList<String> searchList = new LinkedList<>();
        searchList.add(from.getId());
        from.setParent(null);

        String fromBk = from.getId();
        while (!searchList.isEmpty()) {
            String nodeId = searchList.removeFirst();
            if (nodeId.equals(to.getId())) {
                List<Node> route = constructPath(from, to);
                //Logger.trace("Path from " + from.getId() + " to " + nodeId);

                return route;
            } else {
                visited.add(nodeId);
                Node node = this.layoutAnalyzer.getGraph().get(nodeId);
                List<Edge> edges = node.getEdges();

                for (Edge edge : edges) {
                    String tgt = edge.getTargetId();
                    Node tgtNode = this.layoutAnalyzer.getGraph().get(tgt);

                    if (!tgt.equals(fromBk)) {
                        if (!visited.contains(tgt) && !searchList.contains(tgt) && tgtNode.canTraverse(node)) {
                            tgtNode.setParent(node);
                            searchList.add(tgt);
                        }
                    }
                }
            }
        }

        //Logger.trace("No path from " + from.getId() + " to " + to.getId());
        return null;
    }

    public void createGraph() {
        layoutAnalyzer.buildGraph();
        nodeCache.clear();
        nodeCache.putAll(layoutAnalyzer.getGraph());
        Logger.trace("Created lookup map with " + nodeCache.size() + " entries...");
    }

    public static void main(String[] a) {
        System.setProperty("trackServiceAlwaysUseDemo", "true");
        BreathFirst bf = new BreathFirst();
        bf.createGraph();

        List<List<Node>> candidateRoutes = bf.layoutAnalyzer.getBlockToBlockNodes();

        Logger.trace("Try to route " + candidateRoutes.size() + " Possible block to block driveways");

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
