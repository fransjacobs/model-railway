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
package jcs.ui.layout.pathfinding;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
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

  //private List<Tile> tiles;
  private Map<Point, Tile> tiles;

  private final Map<String, Node> graph;

  public GraphBuilder(List<Tile> tiles) {
    this.graph = new HashMap<>();
    this.tiles = new HashMap<>();
    for (Tile tile : tiles) {
      this.tiles.put(tile.getCenter(), tile);
    }
    Logger.trace("Loaded " + tiles.size() + " tiles");
  }

  void findEdgesFor(Node node) {
    //Tile tile = findTile(node.getId());
    //Point tcp = tile.getCenter();
    //Set<Point> adjacentPoints = LayoutUtil.adjacentPointsFor(tile);

    //filter the points which do have a tile
//    for (Point adj : adjacentPoints) {
//      if (isTile(adj)) {
//        Tile t = findTile(adj);
//        Point cp = t.getCenter();
//
//        //Determine the node-id of the adjacent node
//        String nodeId;
//        switch (t.getTileType()) {
//          case BLOCK -> {
//            //A block has 2 sides ie 2 nodes so get the nearest point which is  bk-n+/-
//            if (LayoutUtil.isPlusAdjacent(t, tcp)) {
//              cp = LayoutUtil.getPlusCenter(t);
//              nodeId = t.getId() + "+";
//            } else {
//              cp = LayoutUtil.getMinusCenter(t);
//              nodeId = t.getId() + "-";
//            }
//          }
//          case SWITCH -> // As switch has 3 adjacent nodes, depending on the direction
//            nodeId = getNodeIdForAdjacentSwitch(tile, t);
//          default -> nodeId = t.getId();
//        }
//        Edge e1 = new Edge(node.getId(), nodeId, LayoutUtil.euclideanDistance(tcp, cp));
//        node.addEdge(e1);
//        Logger.trace(e1);
//      }
//    }
  }

  private void addNode(Tile tile) {
    String id = tile.getId();
    if (graph.containsKey(id)) {
      Node node = graph.get(id);
      Logger.trace(node + " allready exists");
    } else {
      Node node = new Node(tile);
      Logger.trace("Node: " + node + (node.isJunction()?" Junction":"")+ "...");

      //Check all sides of the tile fpt neighbors
      for (Orientation dir : Orientation.values()) {
        Point p = tile.getNeighborPoints().get(dir);
        if (p != null) {
          //Found a neighbor, is it possible to travel from this tile to the neighbor?
          Tile neighbor = this.tiles.get(p);
          if (tile.canTraverseTo(neighbor)) {
            //When the node is a Switch there is a direction Green or Red...
            if(TileType.SWITCH == neighbor.getTileType() || TileType.CROSS == neighbor.getTileType()) {
              Logger.debug("Neighbor "+neighbor.getId()+" is a Junction");
            }
            Edge edge = new Edge(node.getId(), neighbor.getId(), dir);
            node.addEdge(edge);
            Logger.trace(" ->" + edge);
          }
        }
      }

      this.graph.put(id, node);
      Logger.trace("Added " + node);
    }

  }

  void buildGraph() {
    for (Tile tile : this.tiles.values()) {
      addNode(tile);
    }

  }

  public static void main(String[] a) {
    List<Tile> tiles = TileFactory.convert(PersistenceFactory.getService().getTiles(), false, false);

    GraphBuilder gb = new GraphBuilder(tiles);

    gb.buildGraph();
  }

}
