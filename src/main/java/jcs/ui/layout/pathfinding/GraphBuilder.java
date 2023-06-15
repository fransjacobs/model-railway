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
import jcs.ui.layout.tiles.Block;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * Build a Graph from the Layout Tiles
 *
 * @author frans
 */
public class GraphBuilder {

  private final Map<Point, Tile> tilePointMap;
  private final Map<String, Tile> tileIdMap;
  private final Map<String, Node> graph;

  public GraphBuilder() {
    this.graph = new HashMap<>();
    this.tilePointMap = new HashMap<>();
    this.tileIdMap = new HashMap<>();
  }

  private void addBlockNode(Tile tile) {
    Logger.trace("Creating Nodes for tile " + tile.getId() + (tile.isBlock() ? " Block" : "") + " Checking " + tile.getNeighborPoints().size() + " neighbors...");

    String suffixP = "+";
    String suffixM = "-";
    List<String> suffixIds = new ArrayList<>();
    //Check for existence
    if (!graph.containsKey(tile.getId() + suffixP)) {
      suffixIds.add(suffixP);
    } else {
      Logger.trace(tile.getId() + suffixP + " allready exists");
    }
    if (!graph.containsKey(tile.getId() + suffixM)) {
      suffixIds.add(suffixM);
    } else {
      Logger.trace(tile.getId() + suffixM + " allready exists");
    }

    for (String suffix : suffixIds) {
      Node node = new Node(tile, suffix);
      //Check the suffix side of the (block) tile for neighbors
      Point p = ((Block) tile).getNeighborPoint(suffix);
      if (p != null) {
        //Found a neighbor, is it possible to travel from this tile (side) to the neighbor
        Tile neighbor = this.tilePointMap.get(p);
        if (tile.isTileAdjacent(neighbor)) {
          //When the tile is a Switch there is a Green or Red path...
          String fromSuffix = tile.getIdSuffix(neighbor);
          Edge edge;
          if (neighbor.isJunction()) {
            AccessoryValue junctionDir = neighbor.getSwitchValueTo(tile);
            edge = new Edge(node.getId(), neighbor.getId(), fromSuffix, null, ((Block) tile).getTravelDirection(suffix), junctionDir);
          } else if (neighbor.isBlock()) {
            String toSuffix = neighbor.getIdSuffix(tile);
            edge = new Edge(node.getTileId(), neighbor.getId(), fromSuffix, toSuffix, ((Block) tile).getTravelDirection(suffix));
          } else {
            edge = new Edge(node.getTileId(), neighbor.getId(), fromSuffix, null, ((Block) tile).getTravelDirection(suffix));
          }
          node.addEdge(edge);
          Logger.trace(" ->" + edge);
        }
      }

      this.graph.put(node.getId(), node);
      Logger.trace("Added " + node);
    }
  }

  private void addNode(Tile tile) {
    if (tile.isBlock()) {
      addBlockNode(tile);
    } else {
      Logger.trace("Creating Node for tile " + tile.getId() + (tile.isJunction() ? " Junction" : "") + (tile.isDiagonal() ? " Diagonal" : "") + " Checking " + tile.getNeighborPoints().size() + " neighbors...");

      String id = tile.getId();

      if (graph.containsKey(id)) {
        Node node = graph.get(id);
        Logger.trace(node + " allready exists");
      } else {
        Node node = new Node(tile);
        //Check all sides of the tile fpt neighbors
        int cnt = tile.getNeighborPoints().size();
        for (Orientation dir : Orientation.values()) {
          Point p = tile.getNeighborPoints().get(dir);
          if (p != null) {
            //Found a neighbor, is it possible to travel from this tile to the neighbor?
            Tile neighbor = this.tilePointMap.get(p);
            if (tile.isTileAdjacent(neighbor)) {
              cnt--;
              //When the tile is a Switch there is a Green or Red path...
              Edge edge;
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

        Logger.trace("Added " + node + (cnt != 0 ? " Missing an edge!" : ""));
        //Log if we miss something
        if (cnt != 0) {
          StringBuilder sb = new StringBuilder();
          sb.append("Tile: ");
          sb.append(tile.xyToString());
          sb.append("->");

          for (Orientation o : Orientation.values()) {
            Point p = tile.getNeighborPoints().get(o);
            if (p != null) {
              sb.append(o);
              sb.append(": ");
              sb.append(p);
              sb.append(" ");
            }
          }

          Logger.trace(sb.toString());
        }
      }
    }
  }

  public void buildGraph(List<Tile> tiles) {
    this.tilePointMap.clear();
    this.tileIdMap.clear();
    this.graph.clear();
    for (Tile tile : tiles) {
      if (tile.isBlock()) {
        //A block is s rectangle hence the points are different
        for (Point p : tile.getAltPoints()) {
          this.tilePointMap.put(p, tile);
          //Logger.trace("Adding " + p + " " + tile);
        }
      } else {
        //A tile is a square, center is the point too look for
        this.tilePointMap.put(tile.getCenter(), tile);
      }
      this.tileIdMap.put(tile.getId(), tile);
    }
    Logger.trace("Loaded " + tiles.size() + " tiles");

    //Build the graph
    for (Tile tile : this.tilePointMap.values()) {
      addNode(tile);
    }
    Logger.debug("Graph has " + this.graph.size() + " nodes");
  }

  public Map<String, Node> getGraph() {
    return graph;
  }

  public Tile getTile(String id) {
    return tileIdMap.get(id);
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

  public static void main(String[] a) {
    List<Tile> tiles = TileFactory.convert(PersistenceFactory.getService().getTiles(), false, false);

    GraphBuilder gb = new GraphBuilder();
    gb.buildGraph(tiles);

    //gb.getAllBlockToBlockNodes();
  }

}
