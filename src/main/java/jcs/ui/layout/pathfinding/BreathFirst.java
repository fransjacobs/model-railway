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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.enums.AccessoryValue;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.Tile;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class BreathFirst {

  //private final LayoutAnalyzer layoutAnalyzer;
  private GraphBuilder graphBuilder;
  //private final Map<String, Node> nodeCache;
  private final Map<String, RouteBean> routes;

  public BreathFirst() {
    this.routes = new HashMap<>();
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

    LinkedList<String> elements = getNodeIdList(from, path);

    String first = elements.getFirst();
    String last = elements.getLast();

    //TODO!
    //RouteBean route = new RouteBean(null, first, first, last, last, null);
    //List<RouteElementBean> rel = createRouteElementsFromElements(first, last, nodeIds);
    //route.setRouteElements(rel);
    //public RouteBean(Integer id, String fromTileId, String fromTileSite, String toTileId, String toTileSite, String color) {
    //String rid = first + first + last + last;
    //this.routes.put(rid, route);
    return path;
  }

  private static List<RouteElementBean> createRouteElementsFromElements(Node from, List<Node> path) {
    
//    List<RouteElementBean> rel = new LinkedList<>();
//    for (int i = 0; i < elementIds.size(); i++) {
//      String nodeId = elementIds.get(i);

//      String tileId;
//      if (nodeId.endsWith("-R")) {
//        tileId = nodeId.replace("-R", "");
//      } else if (nodeId.endsWith("-G")) {
//        tileId = nodeId.replace("-G", "");
//      } else if (nodeId.endsWith("-") || nodeId.endsWith("+")) {
//        tileId = nodeId.substring(0, nodeId.length() - 1);
//      } else {
//        tileId = nodeId;
//      }

//      AccessoryValue accessoryValue = AccessoryValue.OFF;
//      if (nodeId.endsWith("-R")) {
//        accessoryValue = AccessoryValue.RED;
//      } else if (nodeId.endsWith("-G")) {
//        accessoryValue = AccessoryValue.GREEN;
//      }
//      Integer elementOrder = i;

      //RouteElementBean routeElement = new RouteElementBean(fromTileId + "|" + toTileId, nodeId, tileId, accessoryValue, elementOrder);
//      RouteElementBean routeElement = new RouteElementBean(null, null, nodeId, tileId, accessoryValue.getDBValue(), elementOrder);

      //    public RouteElementBean(Long routeId, String nodeId, String tileId, String accessoryValue, Integer elementOrder) {

      
//      rel.add(routeElement);

  //    Logger.trace(routeElement);
//    }
    return null;
  }

  private LinkedList<String> getNodeIdList(Node from, List<Node> path) {
    LinkedList<String> nodeIds = new LinkedList<>();

    if (from != null) {
      nodeIds.add(from.getId());
    }
    for (int i = 0; i < path.size(); i++) {
      Node t = path.get(i);
      nodeIds.add(t.getId());
    }
    return nodeIds;
  }

  private String pathToString(Node from, List<Node> path) {
    StringBuilder sb = new StringBuilder();
    //sb.append("Route: ");
    if (from != null) {
      sb.append(from.getId());
      sb.append(" -> ");
    }
    for (int i = 0; i < path.size(); i++) {
      Node t = path.get(i);
      sb.append(t.getId());
      if (i + 1 < path.size()) {
        sb.append(" -> ");
      }
    }
    return sb.toString();
  }

  public List<Node> search(Node from, Node to) {
    //Logger.trace("Searching for a route from: " + from.getId() + " to: " + to.getId());

    LinkedList<String> visited = new LinkedList<>();
    LinkedList<String> searchList = new LinkedList<>();

    searchList.add(from.getId());
    from.setParent(null);
    String fromBk = from.getId();

    while (!searchList.isEmpty()) {
      String nodeId = searchList.removeFirst();
      if (nodeId.equals(to.getId())) {
        List<Node> route = constructPath(from, to);
        //Logger.trace("Found a route from " + from.getId() + " to " + nodeId + ": " + pathToString(from, route));

        return route;
      } else {
        visited.add(nodeId);
        Node node = this.graphBuilder.getGraph().get(nodeId);
        List<Edge> edges = node.getEdges();

        for (Edge edge : edges) {
          String tgt = edge.getToId() + (edge.getToSuffix() != null ? edge.getToSuffix() : "");
          Node tgtNode = this.graphBuilder.getGraph().get(tgt);
          if (!tgt.equals(fromBk)) {
            //Logger.trace("Checking " + (node.getParent() != null ? "from: " + node.getParent().getId() + " via " + node.getId() : "") + " to: " + tgtNode.getId());
            if (!visited.contains(tgt) && !searchList.contains(tgt) && node.canTravelTo(tgtNode)) {
              tgtNode.setParent(node);
              searchList.add(tgt);
              //Logger.trace("Search: " + tgt + " Prev: " + tgtNode.getParent().getId());
            }
          }
        }
      }
    }

    //Logger.trace("No path from " + from.getId() + " -> " + to.getId());
    return null;
  }

  public GraphBuilder getGraphBuilder() {
    return graphBuilder;
  }

  public void setGraphBuilder(GraphBuilder graphBuilder) {
    this.graphBuilder = graphBuilder;
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
    if (this.graphBuilder != null) {
      List<List<Node>> blockToBlockList = this.graphBuilder.getAllBlockToBlockNodes();
      Logger.trace("Try to route " + blockToBlockList.size() + " Possible block to block routes");

      for (List<Node> fromTo : blockToBlockList) {
        //List<Node> fromTo = blockToBlockList.get(0);

        Node from = fromTo.get(0);
        Node to = fromTo.get(1);

        //if ("bk-1+".equals(from.getId()) && "bk-2+".equals(to.getId())) {
        //if ("bk-1+".equals(from.getId()) && "bk-3-".equals(to.getId()) || "bk-1+".equals(from.getId()) && "bk-2+".equals(to.getId())) {
        List<Node> route = search(from, to);
        if (route != null) {
          Logger.trace("Found a route from " + from.getId() + " to " + to.getId() + ": " + pathToString(from, route));
          
          //createRouteElementsFromElements(from.getId(), to.getId() , getNodeIdList(from, route));
          
        }

        //}
      }
      Logger.trace("Found " + routes.size() + " routes");
    } else {
      Logger.debug("No Layout Graph set");
    }
  }

  public void persistRoutes() {
    for (RouteBean route : this.routes.values()) {
      PersistenceFactory.getService().persist(route);
    }
  }

  public static void main(String[] a) {
    System.setProperty("trackServiceSkipControllerInit", "true");
    GraphBuilder gb = new GraphBuilder();
    BreathFirst bf = new BreathFirst();
    bf.setGraphBuilder(gb);
    List<Tile> tiles = TileFactory.convert(PersistenceFactory.getService().getTiles(), false, false);
    gb.buildGraph(tiles);
    //###~~

    Logger.trace("\n========================================================================================\n");
    bf.routeAll();

    //bf.buildGraph(jcs.ui.layout.LayoutUtil.loadLayout(true, false));
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
