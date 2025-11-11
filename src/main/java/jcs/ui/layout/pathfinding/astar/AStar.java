/*
 * Copyright 2023 frans.
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
package jcs.ui.layout.pathfinding.astar;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jcs.entities.BlockBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * Build a Graph from Layout and calculate the paths from Block to block
 *
 * @author frans
 */
public class AStar {

  private final Graph graph;
  private final Map<String, RouteBean> routes;

  public AStar() {
    //this.tileCache = new TileCache();
    this.routes = new HashMap<>();
    this.graph = new Graph();
  }

  public List<List<Node>> getAllBlockToBlockNodes() {
    List<List<Node>> fromToList = new ArrayList<>();

    List<Node> fromNodes = graph.getBlockNodes();
    List<Node> toNodes = graph.getBlockNodes();

    for (Node from : fromNodes) {
      boolean fromBlock = from.isBlock();
      String fromId = from.getId();
      if (fromBlock) {
        for (Node to : toNodes) {
          if (to.isBlock()) {
            String toId = to.getId();

            if (!fromId.equals(toId) && !from.getTile().equals(to.getTile())) {
              List<Node> fromTo = new ArrayList<>();
              fromTo.add(from);

              fromTo.add(to);
              fromToList.add(fromTo);
            }
          }
        }
      }
    }
    return fromToList;
  }

  public String pathToString(List<Node> path) {
    if (path.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(path.get(0).getId());
    if (path.get(0).isBlock()) {
      sb.append(path.get(0).getSuffix());
    }
    sb.append("]->[");
    int last = path.size() - 1;
    sb.append(path.get(last).getId());
    if (path.get(last).isBlock()) {
      sb.append(path.get(last).getSuffix());
    }
    sb.append("]: ");

    for (int i = 0; i < path.size(); i++) {
      Node n = path.get(i);
      sb.append(n.getId());
      if (n.isBlock()) {
        sb.append(n.getSuffix());
        sb.append("[");
        sb.append(n.getId());
        sb.append("]");
      }
      if (n.isJunction()) {
        sb.append("[");
        sb.append(n.getAccessoryState());
        sb.append("]");
      }
      if (i + 1 < path.size()) {
        sb.append(" -> ");
      }
    }
    return sb.toString();
  }

  private RouteBean createRouteBeanFromNodePath(List<Node> path) {
    Node first = path.get(0);
    Node last = path.get(path.size() - 1);

    String fromId = first.getId();
    String fromSuffix = first.getSuffix();

    String toId = last.getId();
    String toSuffix = last.getSuffix();

    String routeId = "[" + fromId + fromSuffix + "]->[" + toId + toSuffix + "]";
    RouteBean route = new RouteBean(routeId, fromId, fromSuffix, toId, toSuffix);
    Logger.trace("From " + fromId + " to: " + toId + " route id " + routeId + "; " + route);

    List<RouteElementBean> rel = new LinkedList<>();

    //Start with the first element"
    rel.add(new RouteElementBean(routeId, fromId, fromId, first.getAccessoryState(), 0));
    int elementOrder = 1;
    for (Node n : path) {
      String nodeId = n.getId() + (n.getSuffix() != null ? n.getSuffix() : "");
      RouteElementBean re = new RouteElementBean(routeId, nodeId, n.getId(), n.getAccessoryState(), elementOrder);
      re.setIncomingOrientation(n.getIncomingSide());
      elementOrder++;
      rel.add(re);
    }

    route.setRouteElements(rel);

    return route;
  }

  public void persistRoutes() {
    for (RouteBean route : routes.values()) {
      PersistenceFactory.getService().persist(route);
    }

    List<Node> blockNodes = graph.getBlockNodes();

    for (Node block : blockNodes) {
      BlockBean bb = block.getTile().getBlockBean();
      if (bb == null) {
        bb = new BlockBean(block.getTile().getTileBean());
        bb.setAlwaysStop(true);
        bb.setMinWaitTime(10);
      } else {
        Logger.trace("Using existing BlockBean: " + bb);
        if (bb.getMinWaitTime() == null) {
          bb.setMinWaitTime(10);
        }
      }
      PersistenceFactory.getService().persist(bb);
    }
  }

  public RouteBean getRoute(String id) {
    return this.routes.get(id);
  }

  public Map<String, RouteBean> getRoutes() {
    return this.routes;
  }

  public List<Node> findPath(String fromNodeId, String fromSuffix, String toNodeId, String toSuffix) {
    Node from = graph.getNode(fromNodeId);
    Node to = graph.getNode(toNodeId);
    return findPath(from, fromSuffix, to, toSuffix);
  }

  public List<Node> findPath(Node from, String fromSuffix, Node to, String toSuffix) {
    return graph.findPath(from, fromSuffix, to, toSuffix);
  }

  public List<RouteBean> routeAll() {
    routes.clear();
    List<List<Node>> blockToBlockList = getAllBlockToBlockNodes();
    Logger.trace("Try to route " + blockToBlockList.size() * 2 * 2 + " Possible block to block routes");
    Logger.trace("=============================================================================");

    for (List<Node> fromTo : blockToBlockList) {
      Node from = fromTo.get(0);
      Node to = fromTo.get(1);
      Set<Edge> fromEdges = from.getEdges();
      Set<Edge> toEdges = from.getEdges();

      for (Edge fromEdge : fromEdges) {
        if (fromEdge.getFrom().isBlock()) {
          String fromSuffix = fromEdge.getFromSuffix();
          for (Edge toEdge : toEdges) {
            if (toEdge.getFrom().isBlock()) {
              String toSuffix = toEdge.getFromSuffix();

              String fid = from.getId() + fromSuffix;
              String tid = to.getId() + toSuffix;

              //if ("bk-2-".equals(fid) && "bk-3+".equals(tid)) {
              //|| ("bk-1-".equals(fid) && "bk-2-".equals(tid))) {
              //if ("bk-2-".equals(fid) && "bk-3+".equals(tid)) {
              List<Node> path = findPath(from, fromSuffix, to, toSuffix);

              if (path.isEmpty()) {
                Logger.debug("No Path from " + fid + " to " + tid);
              } else {
                RouteBean routeBean = createRouteBeanFromNodePath(path);
                routes.put(routeBean.getId(), routeBean);
              }

            }
            //}
          }
        }
      }
    }

    Logger.trace("Found " + routes.size() + " routes");
    return this.routes.values().stream().collect(Collectors.toList());
  }

  public void buildGraph(List<Tile> tiles) {
    graph.clear();
    //Every Tile becomes a node
    for (Tile tile : tiles) {
      Node n = new Node(tile);
      graph.addNode(n);
    }

    Logger.trace("Graph has " + graph.size() + " nodes...");

    //Create the links or connection between the Nodes
    for (Node node : graph.getNodes()) {
      Collection<Point> neighborPoints = node.getTile().getNeighborPoints().values();
      Logger.trace("Node: " + node.getId() + " has " + neighborPoints.size() + " neighbors " + (node.isBlock() ? "[Block]" : "") + (node.isJunction() ? "[Junction]" : ""));

      for (Point p : neighborPoints) {
        if (TileCache.contains(p)) {
          Node neighbor = graph.getNode(TileCache.findTile(p).getId());

          if (node.getTile().isAdjacent(neighbor.getTile())) {
            double distance;
            if (node.isBlock()) {
              String fromSuffix = node.getTile().getIdSuffix(neighbor.getTile());
              Point altPoint = node.getAltPoint(fromSuffix);
              distance = Graph.manhattanDistance(altPoint, neighbor.getAltPoint(null));
            } else {
              if (neighbor.isBlock()) {
                String toSuffix = neighbor.getTile().getIdSuffix(node.getTile());
                Point altPoint = neighbor.getAltPoint(toSuffix);
                distance = Graph.manhattanDistance(altPoint, node.getAltPoint(null));
              } else {
                distance = Graph.manhattanDistance(node, neighbor);
              }
            }
            //Logger.trace("Neighbor: " + neighbor.getId() + " Distance: " + distance);
            graph.link(node, neighbor, distance);
          }
        }
      }
    }
  }

  public List<Node> getNodes() {
    return graph.getNodes();
  }

  public static void main(String[] a) {
    List<Tile> tiles = TileCache.loadTiles();

    AStar gb = new AStar();
    gb.buildGraph(tiles);

    List<RouteBean> routes = gb.routeAll();

    //gb.persistRoutes();
    Logger.trace("#########");
    if (1 == 2) {
      gb.persistRoutes();
      routes = PersistenceFactory.getService().getRoutes();
    }
    for (RouteBean r : routes) {
      Logger.trace(r.toLogString());
    }

  }
}
