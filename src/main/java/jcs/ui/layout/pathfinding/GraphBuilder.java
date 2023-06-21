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

  private void addJunctionNode(Tile tile) {
    //Logger.trace("Creating Nodes for tile " + tile.getId() + " Junction Checking " + tile.getNeighborPoints().size() + " possible neighbors...");

    String id = tile.getId();
    if (graph.containsKey(id)) {
      //Node node = graph.get(id);
      //Logger.trace(node + " allready exists");
    } else {
      Node node = new Node(tile);

      //A tile has multitiple edges, depending on the direction
      //Check all sides of the tile for neighbors
      //Logger.trace("Tile " + id + " has " + tile.getNeighborPoints().size() + " neighbors" + " CP: " + tile.xyToString());

      for (Orientation dir : Orientation.values()) {
        Point p = tile.getNeighborPoints().get(dir);
        if (p != null && this.tilePointMap.containsKey(p)) {
          //Found a neighbor
          Tile neighbor = this.tilePointMap.get(p);
          //Logger.trace("Neighbor " + neighbor.getId() + " CP: " + neighbor.xyToString());

          if (tile.isAdjacent(neighbor)) {
            //When the tile is a Switch there is a Green or Red path...
            AccessoryValue nodeSwitchStatus = tile.getSwitchValueTo(neighbor);
            Edge edge;
            if (neighbor.isJunction()) {
              //Logger.trace("Neighbor is also Junction, id: " + neighbor.getId());
              //AccessoryValue neighborSwitchStatus = neighbor.getSwitchValueTo(tile);

              //Logger.trace("NodeSwitchStatus: " + nodeSwitchStatus + " neighborSwitchStatus: " + neighborSwitchStatus);
              edge = new Edge(node.getId(), neighbor.getId(), dir, nodeSwitchStatus);
            } else if (neighbor.isBlock()) {
              //Logger.debug((node.isBlock() ? " Tile is Block" : "") + " " + (neighbor.isBlock() ? " Neighbor is Block" : "") + ", id: " + neighbor.getId());
              String fromSuffix = null, toSuffix = null;
              if (neighbor.isBlock()) {
                toSuffix = neighbor.getIdSuffix(tile);
              }
              edge = new Edge(node.getId(), neighbor.getId(), fromSuffix, toSuffix, dir, nodeSwitchStatus);
            } else {
              edge = new Edge(node.getId(), neighbor.getId(), dir,nodeSwitchStatus);
            }
            node.addEdge(edge);
            Logger.trace(" ->" + edge);
          }
        }
      }

      this.graph.put(id, node);

      Logger.trace("Added " + node + " nodes");
    }
  }

  private void addBlockNode(Tile tile) {
    //Logger.trace("Creating Nodes for tile " + tile.getId() + " Block, Checking " + tile.getNeighborPoints().size() + " possible neighbors...");

    String suffixP = "+";
    String suffixM = "-";
    List<String> suffixIds = new ArrayList<>();
    //Check for existence
    if (!graph.containsKey(tile.getId() + suffixP)) {
      suffixIds.add(suffixP);
    } else {
      //Logger.trace(tile.getId() + suffixP + " allready exists");
    }
    if (!graph.containsKey(tile.getId() + suffixM)) {
      suffixIds.add(suffixM);
    } else {
      //Logger.trace(tile.getId() + suffixM + " allready exists");
    }

    //Loop through the suffixes (both sides of the block
    for (String suffix : suffixIds) {
      Node node = new Node(tile, suffix);
      //Check the suffix side of the (block) tile for neighbors
      Point p = ((Block) tile).getNeighborPoint(suffix);
      if (p != null && this.tilePointMap.containsKey(p)) {
        //Found a neighbor, is it possible to travel from this tile (side) to the neighbor
        Tile neighbor = this.tilePointMap.get(p);

        if (tile.isAdjacent(neighbor)) {
          //When the tile is a Switch there is a Green or Red path...
          String fromSuffix = tile.getIdSuffix(neighbor);
          Edge edge;
          if (neighbor.isJunction()) {
            //Are we connected to the diverging side?
            boolean diverging = neighbor.isDivergingSide(tile);
            AccessoryValue pathStatus = neighbor.getSwitchValueTo(tile);

            //Logger.trace("Neighbor" + (neighbor.isJunction() ? " is a Junction" : "") + ", id: " + neighbor.getId() + " is via the " + (diverging ? "Red" : "Green") + " path from " + node.getId() + " pathStatus: " + pathStatus);

            edge = new Edge(node.getId(), neighbor.getId(), fromSuffix, null, ((Block) tile).getTravelDirection(suffix), pathStatus);

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

  /**
   * Every tile is a Node. A tile depicts a rail track. The track can be connected to each other depending on the kind of Track the Tile is referencing. A Node can be connected to a different Node.
   * When a (Track) connection is possible the Node has an Edge.
   *
   * @param tile The tile to add a s Node
   */
  private void addNode(Tile tile) {
    if (tile.isBlock()) {
      addBlockNode(tile);
    } else if (tile.isJunction()) {
      addJunctionNode(tile);
    } else {
      //Logger.trace("Creating Node for tile " + tile.getId() + (tile.isDiagonal() ? " Diagonal" : "") + " Checking " + tile.getNeighborPoints().size() + " possible neighbors...");
      String id = tile.getId();
      if (graph.containsKey(id)) {
        Node node = graph.get(id);
        //Logger.trace(node + " allready exists");
      } else {
        Node node = new Node(tile);
        Map<Orientation, Point> neighborPoints = tile.getNeighborPoints();

        //Check all sides of the tile for neighbors
        for (Orientation dir : Orientation.values()) {
          Point p = neighborPoints.get(dir);
          if (p != null && this.tilePointMap.containsKey(p)) {
            //Found a neighbor Point, lookup the neighbor tile based on the CP or Alternative Points
            Tile neighbor = this.tilePointMap.get(p);

            //Is this neighbor ajacent with this tile?            
            if (tile.isAdjacent(neighbor)) {
              //When the neighbor tile is a Switch there is a Green or Red path...
              Edge edge;
              if (neighbor.isJunction()) {
                //Are we connected to the diverging side?
                boolean diverging = neighbor.isDivergingSide(tile);
                AccessoryValue pathStatus = neighbor.getSwitchValueTo(tile);

                //Logger.trace("Neighbor" + (neighbor.isJunction() ? " is a Junction" : "") + ", id: " + neighbor.getId() + " is via the " + (diverging ? "Red" : "Green") + " path from " + node.getId() + " pathStatus: " + pathStatus);

                edge = new Edge(node.getId(), neighbor.getId(), dir, pathStatus);
              } else if (neighbor.isBlock()) {
                //A Bloc is connected via the + or - side
                //Logger.debug((node.isBlock() ? " Tile is Block" : "") + " " + (neighbor.isBlock() ? " Neighbor is Block" : "") + ", id: " + neighbor.getId());
                String toSuffix = neighbor.getIdSuffix(tile);
                edge = new Edge(node.getId(), neighbor.getId(), null, toSuffix, dir);
              } else {
                edge = new Edge(node.getId(), neighbor.getId(), dir);
              }
              node.addEdge(edge);
              Logger.trace(" ->" + edge);
            }
          }
        }

        this.graph.put(id, node);
        Logger.trace("Added " + node);
      }
    }
  }

  public void buildGraph(List<Tile> tiles) {
    this.tilePointMap.clear();
    this.tileIdMap.clear();
    this.graph.clear();

    for (Tile tile : tiles) {
      if (tile.isBlock()) {
        //A block is s rectangle which fils the space of 3 tiles hence the points are different
        for (Point p : tile.getAltPoints()) {
          this.tilePointMap.put(p, tile);
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
