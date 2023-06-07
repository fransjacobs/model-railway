/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.ui.layout.pathfinding;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.enums.AccessoryValue;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.Tile;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class BreathFirst {

    private final LayoutAnalyzer layoutAnalyzer;
    private final Map<String, Node> nodeCache;
    private final Map<String, RouteBean> routes;

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

        //TODO!
        RouteBean route = new RouteBean(null, first, first, last, last, null);

        List<RouteElementBean> rel = createRouteElementsFromElements(first, last, elements);

        route.setRouteElements(rel);

        //public RouteBean(Integer id, String fromTileId, String fromTileSite, String toTileId, String toTileSite, String color) {
        String rid = first + first + last + last;

        this.routes.put(rid, route);
        return path;
    }

    private static List<RouteElementBean> createRouteElementsFromElements(String fromTileId, String toTileId, List<String> elementIds) {
        List<RouteElementBean> rel = new LinkedList<>();
        for (int i = 0; i < elementIds.size(); i++) {
            String nodeId = elementIds.get(i);

            String tileId;
            if (nodeId.endsWith("-R")) {
                tileId = nodeId.replace("-R", "");
            } else if (nodeId.endsWith("-G")) {
                tileId = nodeId.replace("-G", "");
            } else if (nodeId.endsWith("-") || nodeId.endsWith("+")) {
                tileId = nodeId.substring(0, nodeId.length() - 1);
            } else {
                tileId = nodeId;
            }

            AccessoryValue accessoryValue = AccessoryValue.OFF;
            if (nodeId.endsWith("-R")) {
                accessoryValue = AccessoryValue.RED;
            } else if (nodeId.endsWith("-G")) {
                accessoryValue = AccessoryValue.GREEN;
            }
            Integer elementOrder = i;

            //RouteElementBean routeElement = new RouteElementBean(fromTileId + "|" + toTileId, nodeId, tileId, accessoryValue, elementOrder);
            RouteElementBean routeElement = new RouteElementBean(null, null, nodeId, tileId, accessoryValue.getDBValue(), elementOrder);
            
            
            rel.add(routeElement);

            Logger.trace(routeElement);
        }
        return rel;
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
                    String tgt = edge.getToId();
                    Node tgtNode = this.layoutAnalyzer.getGraph().get(tgt);

                    if (!tgt.equals(fromBk)) {
                        if (!visited.contains(tgt) && !searchList.contains(tgt) && node.canTravelTo(tgtNode)) {
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

    public RouteBean getRoute(String id) {
        return this.routes.get(id);
    }

    public List<RouteBean> getRoutes() {
        List<RouteBean> rl = new LinkedList<>();
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
        for (RouteBean route : this.routes.values()) {
            PersistenceFactory.getService().persist(route);
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

        List<RouteBean> rl = bf.getRoutes();

        for (RouteBean route : rl) {
            PersistenceFactory.getService().persist(route);
            Logger.trace(route.toLogString());
        }

        Logger.trace("#########");
        List<RouteBean> prl = PersistenceFactory.getService().getRoutes();
        for (RouteBean r : prl) {
            Logger.trace(r.toLogString());
        }

        System.exit(0);
    }

}
