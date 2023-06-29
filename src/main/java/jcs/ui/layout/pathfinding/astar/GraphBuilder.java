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
import java.util.List;
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

  public GraphBuilder() {
    this.tileCache = new TileCache();

    Heuristic heuristic = new TrackTraveler();
    this.graph = new Graph<>(heuristic);

  }

  public double manhattanDistance(Node from, Node to) {
    int dx = Math.abs(to.getX() - from.getX());
    int dy = Math.abs(to.getY() - from.getY());
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

  public void routeAll() {
    //this.routes.clear();
    List<List<Node>> blockToBlockList = getAllBlockToBlockNodes();
    Logger.trace("Try to route " + blockToBlockList.size() + " Possible block to block routes");

    for (List<Node> fromTo : blockToBlockList) {
      //List<Node> fromTo = blockToBlockList.get(0);

      Node from = fromTo.get(0);
      Node to = fromTo.get(1);

      //if ("bk-1+".equals(from.getId()) && "bk-2+".equals(to.getId())) {
//        if ("bk-3-".equals(from.getId()) && "bk-1+".equals(to.getId()) ) {
//          if ("bk-2+".equals(from.getId()) && "bk-3-".equals(to.getId()) ||
//              "bk-3-".equals(from.getId()) && "bk-2+".equals(to.getId()) ||
//              "bk-3-".equals(from.getId()) && "bk-1+".equals(to.getId()) ||
//              "bk-1+".equals(from.getId()) && "bk-3-".equals(to.getId())   ) {
      //List<Node> route = search(from, to);
      Logger.trace("From " + from.getId() + " -> " + to.getId());

//        if (route != null) {
//          Logger.trace("Found a route from " + from.getId() + " to " + to.getId() + ": " + pathToString(from, route));
//
//          RouteBean routeBean = createRouteFromPath(from, route);
//          this.routes.put(routeBean.getId(), routeBean);
//        }
//        }
    }
    //Logger.trace("Found " + routes.size() + " routes");

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
      Logger.trace("Current node: " + node.getId() + " has " + neighborPoints.size() + " neighbors " + (node.isBlock() ? "Block" : "") + (node.isJunction() ? "Junction" : ""));

      for (Point p : neighborPoints) {
        if (tileCache.contains(p)) {
          Node neighbor = graph.getNode(tileCache.getTileId(p));
          //Logger.trace("Neighbor: " + neighbor.getId());
          if (node.getTile().isAdjacent(neighbor.getTile())) {
            //Calculate the manhatten distance:
            double cost = this.manhattanDistance(node, neighbor);

            String fromSuffix = null;
            if (node.isBlock()) {
              fromSuffix = node.getTile().getIdSuffix(neighbor.getTile());
            }
            String toSuffix = null;
            if (neighbor.isBlock()) {
              toSuffix = neighbor.getTile().getIdSuffix(node.getTile());
            }

            Edge edge = new Edge(node, fromSuffix, neighbor, toSuffix, cost);
            Edge oppositeEdge = new Edge(neighbor, toSuffix, node, fromSuffix, cost);
            AccessoryValue fromPathStatus = null;
            if (node.isJunction()) {
              fromPathStatus = node.getTile().getSwitchValueTo(neighbor.getTile());
              edge.setAccessoryState(fromPathStatus);
            }
            AccessoryValue toPathStatus = null;
            if (neighbor.isJunction()) {
              toPathStatus = neighbor.getTile().getSwitchValueTo(node.getTile());
              oppositeEdge.setAccessoryState(toPathStatus);
            }

            node.addEdge(edge);
            Logger.trace(edge);

            neighbor.addEdge(oppositeEdge);
            Logger.trace(oppositeEdge);
          }
        }
      }
    }
  }

  public static void main(String[] a) {
    List<Tile> tiles = TileFactory.convert(PersistenceFactory.getService().getTiles(), false, false);

    GraphBuilder gb = new GraphBuilder();
    gb.buildGraph(tiles);

    List<Node> blockList = gb.graph.getBlockNodes();
    for (Node b : blockList) {
      Logger.trace(b.getId());
    }

    
    gb.routeAll();
    
    //gb.getAllBlockToBlockNodes();
  }

}
