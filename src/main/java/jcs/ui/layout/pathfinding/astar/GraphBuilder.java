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
import java.util.List;
import java.util.Map;
import java.util.Set;
import jcs.entities.RouteBean;
import jcs.entities.enums.AccessoryValue;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.Tile;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * Build a Graph from the Layout Tiles
 *
 * @author frans
 */
public class GraphBuilder {

  private final TileCache tileCache;

  private final Graph<Tile> graph;
  private final Map<String, RouteBean> routes;

  public GraphBuilder() {
    this.tileCache = new TileCache();
    this.routes = new HashMap<>();

    Heuristic heuristic = new TrackTraveler();
    this.graph = new Graph<>(heuristic);

  }

  public double manhattanDistance(Node from, Node to) {
    int dx = Math.abs(to.getX() - from.getX());
    int dy = Math.abs(to.getY() - from.getY());
    return dx + dy;
  }

  public double manhattanDistance(Point from, Point to) {
    int dx = Math.abs(to.x - from.x);
    int dy = Math.abs(to.y - from.y);
    return dx + dy;
  }

  public double linearDistance(Node from, Node to) {
    int dx = to.getX() - from.getX();
    int dy = to.getY() - from.getY();

    return Math.sqrt(dx * dx + dy * dy);
  }

  public List<List<Node>> getAllBlockToBlockNodes() {
    List<List<Node>> fromToList = new ArrayList<>();

    List<Node> fromNodes = this.graph.getBlockNodes();
    List<Node> toNodes = this.graph.getBlockNodes();

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

  private String pathToString(List<Node> path) {
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

  public void routeAll() {
    this.routes.clear();
    List<List<Node>> blockToBlockList = getAllBlockToBlockNodes();
    Logger.trace("Try to route " + blockToBlockList.size() * 2 * 2 + " Possible block to block routes");
    Logger.trace("=============================================================================");

    List<String> paths = new ArrayList<>();

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

              //[bk-3-]->[bk-1+]
              //[bk-3-]->[bk-2+]
              if (("bk-3-".equals(fid) && "bk-2+".equals(tid))
                      || ("bk-3-".equals(fid) && "bk-1+".equals(tid))) {

                List<Node> path = graph.findPath(from, fromSuffix, to, toSuffix);

                if (path.isEmpty()) {
                  Logger.debug("No Path from " + fid + " to " + tid);
                } else {
                  paths.add(pathToString(path));
                  //Logger.debug(pathToString(path));
                }

              }
            }
          }
        }
      }
    }

    Logger.trace("Found " + paths.size() + " routes");

    for (String p : paths) {
      Logger.trace(p);
    }

  }

  public void buildGraph(List<Tile> tiles) {
    this.tileCache.reload(tiles);

    //Every Tile becomes a node
    for (Tile tile : tiles) {
      Node n = new Node(tile);
      this.graph.addNode(n);
    }

    Logger.trace("Graph has " + this.graph.size() + " nodes...");

    //Create the links or connection between the Nodes
    for (Node node : graph.getNodes()) {
      Collection<Point> neighborPoints = node.getTile().getNeighborPoints().values();
      Logger.trace("Node: " + node.getId() + " has " + neighborPoints.size() + " neighbors " + (node.isBlock() ? "[Block]" : "") + (node.isJunction() ? "[Junction]" : "") + ":");

      for (Point p : neighborPoints) {
        if (tileCache.contains(p)) {
          Node neighbor = graph.getNode(tileCache.getTileId(p));
          if (node.getTile().isAdjacent(neighbor.getTile())) {
            double distance;
            if (node.isBlock()) {
              String fromSuffix = node.getTile().getIdSuffix(neighbor.getTile());
              Point altPoint = node.getAltPoint(fromSuffix);
              distance = manhattanDistance(altPoint, neighbor.getAltPoint(null));
            } else {
              if (neighbor.isBlock()) {
                String toSuffix = neighbor.getTile().getIdSuffix(node.getTile());
                Point altPoint = neighbor.getAltPoint(toSuffix);
                distance = manhattanDistance(altPoint, node.getAltPoint(null));
              } else {
                distance = manhattanDistance(node, neighbor);
              }
            }
            //Logger.trace("Neighbor: " + neighbor.getId() + " Distance: " + distance);

            Edge edge = this.graph.link(node, neighbor, distance);
            Logger.trace(edge);

//routeAll(): [bk-2+]->[bk-3-]: bk-2+[bk-2] -> se-3 -> st-4 -> ct-1 -> sw-2[RED] -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-[bk-3]
//     Route: [bk-2+]->[bk-3-]: bk-2+[bk-2] -> se-3 -> st-4 -> ct-1 -> sw-2[RED] -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-[bk-3]
//routeAll(): [bk-2-]->[bk-3+]: bk-2-[bk-2] -> se-4 -> st-3 -> ct-2 -> sw-1[RED] -> st-6 -> ct-3 -> st-7 -> st-8 -> st-9 -> st-10 -> ct-5 -> st-15 -> st-16 -> st-17 -> se-5 -> bk-3+[bk-3]
//     Route: [bk-2-]->[bk-3+]: bk-2-[bk-2] -> se-4 -> st-3 -> ct-2 -> sw-1[RED] -> st-6 -> ct-3 -> st-7 -> st-8 -> st-9 -> st-10 -> ct-5 -> st-15 -> st-16 -> st-17 -> se-5 -> bk-3+[bk-3]
//--routeAll(): [bk-3-]->[bk-2+]: bk-3-[bk-3] -> se-6 -> st-18 -> st-19 -> st-20 -> ct-6 -> st-14 -> st-13 -> st-12 -> st-11 -> ct-4 -> st-5 -> sw-2[GREEN] -> ct-1 -> st-4 -> se-3 -> bk-2+[bk-2]
//       Route: [bk-3-]->[bk-2+]: bk-3-[bk-3] -> se-6 -> st-18 -> st-19 -> st-20 -> ct-6 -> st-14 -> st-13 -> st-12 -> st-11 -> ct-4 -> st-5 -> sw-2[RED] -> ct-1 -> st-4 -> se-3 -> bk-2+[bk-2]
//routeAll(): [bk-3+]->[bk-2-]: bk-3+[bk-3] -> se-5 -> st-17 -> st-16 -> st-15 -> ct-5 -> st-10 -> st-9 -> st-8 -> st-7 -> ct-3 -> st-6 -> sw-1[GREEN] -> ct-2 -> st-3 -> se-4 -> bk-2-[bk-2]
//     Route: [bk-3+]->[bk-2-]: bk-3+[bk-3] -> se-5 -> st-17 -> st-16 -> st-15 -> ct-5 -> st-10 -> st-9 -> st-8 -> st-7 -> ct-3 -> st-6 -> sw-1[GREEN] -> ct-2 -> st-3 -> se-4 -> bk-2-[bk-2]
//routeAll(): [bk-3-]->[bk-1+]: bk-3-[bk-3] -> se-6 -> st-18 -> st-19 -> st-20 -> ct-6 -> st-14 -> st-13 -> st-12 -> st-11 -> ct-4 -> st-5 -> sw-2[GREEN] -> st-2 -> se-2 -> bk-1+[bk-1]
//----Route: [bk-3-]->[bk-1+]: bk-3-[bk-3] -> se-6 -> st-18 -> st-19 -> st-20 -> ct-6 -> st-14 -> st-13 -> st-12 -> st-11 -> ct-4 -> st-5 -> sw-2[RED] -> st-2 -> se-2 -> bk-1+[bk-1]
//routeAll(): [bk-3+]->[bk-1-]: bk-3+[bk-3] -> se-5 -> st-17 -> st-16 -> st-15 -> ct-5 -> st-10 -> st-9 -> st-8 -> st-7 -> ct-3 -> st-6 -> sw-1[GREEN] -> st-1 -> se-1 -> bk-1-[bk-1]
//     Route: [bk-3+]->[bk-1-]: bk-3+[bk-3] -> se-5 -> st-17 -> st-16 -> st-15 -> ct-5 -> st-10 -> st-9 -> st-8 -> st-7 -> ct-3 -> st-6 -> sw-1[GREEN] -> st-1 -> se-1 -> bk-1-[bk-1]
//routeAll(): [bk-1-]->[bk-3+]: bk-1-[bk-1] -> se-1 -> st-1 -> sw-1[GREEN] -> st-6 -> ct-3 -> st-7 -> st-8 -> st-9 -> st-10 -> ct-5 -> st-15 -> st-16 -> st-17 -> se-5 -> bk-3+[bk-3]
//     Route: [bk-1-]->[bk-3+]: bk-1-[bk-1] -> se-1 -> st-1 -> sw-1[GREEN] -> st-6 -> ct-3 -> st-7 -> st-8 -> st-9 -> st-10 -> ct-5 -> st-15 -> st-16 -> st-17 -> se-5 -> bk-3+[bk-3]
//routeAll(): [bk-1+]->[bk-3-]: bk-1+[bk-1] -> se-2 -> st-2 -> sw-2[GREEN] -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-[bk-3]
//     Route: [bk-1+]->[bk-3-]: bk-1+[bk-1] -> se-2 -> st-2 -> sw-2[GREEN] -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-[bk-3]
          }
        }
      }
    }
  }

  public static void main(String[] a) {
    List<Tile> tiles = TileFactory.convert(PersistenceFactory.getService().getTiles(), false, false);

    GraphBuilder gb = new GraphBuilder();
    gb.buildGraph(tiles);

//    List<Node> blockList = gb.graph.getBlockNodes();
//    for (Node b : blockList) {
//      Logger.trace(b.getId());
//    }
    gb.routeAll();
  }
}
