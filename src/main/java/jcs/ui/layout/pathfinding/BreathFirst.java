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
package jcs.ui.layout.pathfinding;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.entities.Route;
import jcs.ui.layout.LayoutUtil;
import jcs.ui.layout.Tile;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class BreathFirst {

    private final LayoutAnalyzer layoutAnalyzer;
    private final Map<String, Node> nodeCache;
    private final Map<String, Route> routes;

    public BreathFirst() {
        this.nodeCache = new HashMap<>();
        this.routes = new HashMap<>();
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

        LinkedList<String> elements = buildElementsList(from, path);
        String first = elements.getFirst();
        String last = elements.getLast();

        Route route = new Route(first, last, elements);
        this.routes.put(route.getId(), route);
        return path;
    }

    //Check if the path is valid
    private LinkedList<String> buildElementsList(Node from, List<Node> path) {
        LinkedList<String> elements = new LinkedList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("Path: ");
        if (from != null) {
            elements.add(from.getId());

            sb.append(from.getId());
            sb.append(" -> ");
        }
        for (int i = 0; i < path.size(); i++) {
            Node t = path.get(i);
            elements.add(t.getId());

            sb.append(t.getId());
            if (i + 1 < path.size()) {
                sb.append(" -> ");
            }
        }
        Logger.trace(sb);
        return elements;
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
                        if (!visited.contains(tgt) && !searchList.contains(tgt) && node.canTravel(tgtNode)) {
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

    public void setGraph(Map<String, Node> graph) {
        this.nodeCache.clear();
        this.nodeCache.putAll(graph);
        this.routes.clear();
    }

    public void buildGraph(Map<Point, Tile> tiles) {
        setGraph(layoutAnalyzer.buildGraph(tiles));
    }

    public Route getRoute(String id) {
        return this.routes.get(id);
    }

    public List<Route> getRoutes() {
        List<Route> rl = new LinkedList<>();
        rl.addAll(this.routes.values());
        return rl;
    }

    public void routeAll() {
        this.routes.clear();

        List<List<Node>> candidateRoutes = layoutAnalyzer.getAllBlockToBlockNodes();
        Logger.trace("Try to route " + candidateRoutes.size() + " Possible block to block routes");
        for (List<Node> fromTo : candidateRoutes) {
            Node from = fromTo.get(0);
            Node to = fromTo.get(1);
            search(from, to);
        }
        Logger.trace("Found " + routes.size() + " routes");
    }

    public void persistRoutes() {
        for (Route route : this.routes.values()) {
            LayoutUtil.persist(route);
        }
    }

    public static void main(String[] a) {
        System.setProperty("trackServiceSkipControllerInit", "true");
        BreathFirst bf = new BreathFirst();

        //bf.setGraph(bf.layoutAnalyzer.buildGraph(jcs.ui.layout.LayoutUtil.loadLayout(true, false)));
        bf.buildGraph(jcs.ui.layout.LayoutUtil.loadLayout(true, false));

//        List<List<Node>> candidateRoutes = bf.layoutAnalyzer.getAllBlockToBlockNodes();
//
//        Logger.trace("Try to route " + candidateRoutes.size() + " Possible block to block routes");
//        for (List<Node> fromTo : candidateRoutes) {
//            Node from = fromTo.get(0);
//            Node to = fromTo.get(1);
//            bf.search(from, to);
//        }
//
//        Logger.trace("Found " + bf.routes.size() + " routes");
        bf.routeAll();

        List<Route> rl = bf.getRoutes();

        for (Route route : rl) {
            LayoutUtil.persist(route);
            Logger.trace(route.toLogString());
        }

        Logger.trace("#########");
        List<Route> prl = LayoutUtil.getRoutes();
        for (Route r : prl) {
            Logger.trace(r.toLogString());
        }

        System.exit(0);
    }

}
