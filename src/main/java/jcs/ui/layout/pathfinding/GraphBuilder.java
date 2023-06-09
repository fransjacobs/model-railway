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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
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

  private final Map<Point, Tile> tiles;
  private final Map<String, Node> graph;

  public GraphBuilder(List<Tile> tiles) {
    this.graph = new HashMap<>();
    this.tiles = new HashMap<>();
    for (Tile tile : tiles) {
      this.tiles.put(tile.getCenter(), tile);
    }
    Logger.trace("Loaded " + tiles.size() + " tiles");

    buildGraph();
  }

  private void addNode(Tile tile) {
    String id = tile.getId();
    if (graph.containsKey(id)) {
      Node node = graph.get(id);
      Logger.trace(node + " allready exists");
    } else {
      Node node = new Node(tile);
      Logger.trace("Node: " + node + (node.isJunction() ? " Junction" : "") + "...");

      //Check all sides of the tile fpt neighbors
      int cnt = 0;
      for (Orientation dir : Orientation.values()) {
        Point p = tile.getNeighborPoints().get(dir);
        if (p != null) {
          cnt++;
          //Found a neighbor, is it possible to travel from this tile to the neighbor?
          Tile neighbor = this.tiles.get(p);
          if (tile.canTraverseTo(neighbor)) {
            //When the node is a Switch there is a direction Green or Red...
            Edge edge;
            //if (TileType.SWITCH == tile.getTileType() || TileType.CROSS == tile.getTileType() || TileType.SWITCH == neighbor.getTileType() || TileType.CROSS == neighbor.getTileType()) {
            if (tile.isJunction() || neighbor.isJunction()) {
              //Logger.debug((node.isJunction() ? " Tile is Junction" : "") + " " + (neighbor.isJunction() ? " Neighbor is Junction" : "") + ", id: " + neighbor.getId());
              AccessoryValue junctionDir = node.isJunction() ? tile.getSwitchValueTo(neighbor) : neighbor.getSwitchValueTo(tile);
              edge = new Edge(node.getId(), neighbor.getId(), dir, junctionDir);
            } else if (tile.isBlock() || neighbor.isBlock()) {
              //Logger.debug((node.isBlock() ? " Tile is Block" : "") + " " + (neighbor.isBlock() ? " Neighbor is Block" : "") + ", id: " + neighbor.getId());
              String fromSuffix = null, toSuffix = null;
              if (node.isBlock()) {
                fromSuffix = tile.getIdSuffix(neighbor);
              }
              if (neighbor.isBlock()) {
                toSuffix = neighbor.getIdSuffix(tile);
              }
              edge = new Edge(node.getId(), neighbor.getId(), fromSuffix, toSuffix, dir);
            } else {
              edge = new Edge(node.getId(), neighbor.getId(), dir);
            }
            node.addEdge(edge);
            Logger.trace(" ->" + edge);
          }
        }
      }

      this.graph.put(id, node);
      Logger.trace("Added " + node + " with " + cnt + " edges");
    }

  }

  private void buildGraph() {
    for (Tile tile : this.tiles.values()) {
      addNode(tile);
    }
  }

  public Map<String, Node> getGraph() {
    return graph;
  }

  public List<List<Node>> getAllBlockToBlockNodes() {
    List<List<Node>> fromToList = new ArrayList<>();

    Collection<Node> fromNodes = this.graph.values();
    Collection<Node> toNodes = this.graph.values();

    for (Node from : fromNodes) {
      boolean fromBlock = from.isBlock();
      String fromId = from.getId();
      if (fromBlock) {
        for (Node to : toNodes) {
          if (to.isBlock()) {
            String toId = to.getId();

            if (!fromId.equals(toId)) {
              List<Node> fromTo = new ArrayList<>();
              fromTo.add(from);
              fromTo.add(to);
              fromToList.add(fromTo);
              Logger.trace("From: " + fromId + " To: " + toId);

            }
          }
        }
      }
    }
    return fromToList;
  }

  public static void main(String[] a) {
    List<Tile> tiles = TileFactory.convert(PersistenceFactory.getService().getTiles(), false, false);

    GraphBuilder gb = new GraphBuilder(tiles);

    gb.getAllBlockToBlockNodes();

    //gb.buildGraph();
  }

}
