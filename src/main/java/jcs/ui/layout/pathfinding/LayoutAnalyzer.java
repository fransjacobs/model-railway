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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.NORTH;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;
import jcs.entities.enums.TileType;
import jcs.ui.layout.LayoutUtil;
import jcs.ui.layout.Tile;
import org.tinylog.Logger;

/**
 * Analyze the layout, which consists out of tiles. Convert it to an Directed Graph with nodes and edges.
 *
 * @author fransjacobs
 */
public class LayoutAnalyzer {

  private final Map<Point, Tile> tiles;
  private final Map<Point, Tile> altTilesLookup;

  private final Map<String, Tile> tileIdLookup;
  private final Map<String, Node> graph;

  public LayoutAnalyzer() {
    this.tiles = new HashMap<>();
    this.altTilesLookup = new HashMap<>();
    this.tileIdLookup = new HashMap<>();
    this.graph = new HashMap<>();
  }

  Tile findTile(String id) {
    Tile result = this.tileIdLookup.get(id);
    if (result == null) {
      //check also with the original Id
      String orgId;
      if (id.endsWith("-") || id.endsWith("+")) {
        orgId = id.substring(0, id.length() - 1);
      } else {
        orgId = id.replace("-G", "").replace("-R", "");
      }
      result = this.tileIdLookup.get(orgId);
    }
    return result;
  }

  Tile findTile(Point cp) {
    Tile result = this.tiles.get(cp);

    if (result == null) {
      result = this.altTilesLookup.get(cp);
      if (result != null) {
      }
    }
    return result;
  }

  boolean isTile(Point cp) {
    return findTile(cp) != null;
  }

  boolean isBlock(String id) {
    Tile t = findTile(id);
    if (t == null) {
      return false;
    }
    return TileType.BLOCK.equals(t.getTileType());
  }

  String getNodeIdForAdjacentSwitch(Tile tile, Tile adjacentSwitch) {
    Orientation o = adjacentSwitch.getOrientation();
    int tileX = tile.getCenterX();
    int tileY = tile.getCenterY();
    int adjX = adjacentSwitch.getCenterX();
    int adjY = adjacentSwitch.getCenterY();
    switch (o) {
      case SOUTH:
        if (adjX == tileX && adjY != tileY) {
          //North or South
          if (adjX < tileX) {
            //Common
            return adjacentSwitch.getId();
          } else {
            //Green
            return adjacentSwitch.getId() + "-G";
          }
        } else {
          //Red
          return adjacentSwitch.getId() + "-R";
        }
      case WEST:
        //East
        if (adjX != tileX && adjY == tileY) {
          //The common of a East L or R Switch 
          if (adjX > tileX) {
            //Common    
            return adjacentSwitch.getId();
          } else {
            //Green    
            return adjacentSwitch.getId() + "-G";
          }
        } else {
          //Red
          return adjacentSwitch.getId() + "-R";
        }
      case NORTH:
        if (adjX == tileX && adjY != tileY) {
          //North or South
          if (adjX > tileX) {
            //Common
            return adjacentSwitch.getId();
          } else {
            //Green
            return adjacentSwitch.getId() + "-G";
          }
        } else {
          //Red
          return adjacentSwitch.getId() + "-R";
        }
      default:
        //East
        if (adjX != tileX && adjY == tileY) {
          //The common of a East L or R Switch 
          if (adjX < tileX) {
            //Common    
            return adjacentSwitch.getId();
          } else {
            //Green    
            return adjacentSwitch.getId() + "-G";
          }
        } else {
          //Red
          return adjacentSwitch.getId() + "-R";
        }
    }
  }

  private void addSwitchNodes(Tile zwitch) {
    List<String> nodeIds = new ArrayList<>();
    nodeIds.add(zwitch.getId());
    nodeIds.add(zwitch.getId() + "-G");
    nodeIds.add(zwitch.getId() + "-R");
    Point tcp = zwitch.getCenter();

    for (String id : nodeIds) {
      Node node;
      Set<Point> adjacentPoints;
      if (id.contains("-G")) {
        adjacentPoints = LayoutUtil.adjacentPointsFor(zwitch, AccessoryValue.GREEN);
      } else if (id.contains("-R")) {
        adjacentPoints = LayoutUtil.adjacentPointsFor(zwitch, AccessoryValue.RED);
      } else {
        adjacentPoints = LayoutUtil.adjacentPointsFor(zwitch, AccessoryValue.OFF);
      }

      if (this.graph.containsKey(id)) {
        node = this.graph.get(id);
      } else {
        //node = new Node(id, zwitch);
        node = new Node(zwitch);
      }

      for (Point adj : adjacentPoints) {
        //Check whether adjacent is tile
        if (isTile(adj)) {
          Tile t = findTile(adj);
          Point cp = t.getCenter();
          //Determine the nodeids of the adjacent nodes
          String nodeId;
          switch (t.getTileType()) {
            case BLOCK:
              //A block has 2 sides ie 2 nodes so get the nearest point which is  bk-n+/-
              if (isPlusAdjacent(t, tcp)) {
                cp = LayoutUtil.getPlusCenter(t);
                nodeId = t.getId() + "+";
              } else {
                cp = LayoutUtil.getMinusCenter(t);
                nodeId = t.getId() + "-";
              }
              break;
            case SWITCH:
              // As switch has 3 adjacent nodes, depending on the direction
              nodeId = getNodeIdForAdjacentSwitch(zwitch, t);
              break;
            default:
              nodeId = t.getId();
              break;
          }

          Edge e1 = new Edge(node.getId(), nodeId, LayoutUtil.euclideanDistance(tcp, cp));
          node.addEdge(e1);
          Logger.trace(e1);

          if (node.getId().endsWith("-G")) {
            Edge ec = new Edge(node.getId(), node.getId().replaceAll("-G", ""), 0);
            node.addEdge(ec);
            Logger.trace(ec);
          } else if (node.getId().endsWith("-R")) {
            Edge ec = new Edge(node.getId(), node.getId().replaceAll("-R", ""), 0);
            node.addEdge(ec);
            Logger.trace(ec);
          } else {
            //Common! add the G and R
            Edge eg = new Edge(node.getId(), node.getId() + "-G", 0);
            node.addEdge(eg);
            Logger.trace(eg);
            Edge er = new Edge(node.getId(), node.getId() + "-R", 0);
            node.addEdge(er);
            Logger.trace(er);
          }
        }
      }
      this.graph.put(id, node);
      Logger.trace("Added " + node);
    }
  }

  private void addBlockNodes(Tile block) {
    //A block has 2 sides, a plus (+) and a minus (-) so 2 nodes
    List<String> nodeIds = new ArrayList<>();
    nodeIds.add(block.getId() + "+");
    nodeIds.add(block.getId() + "-");

    for (String id : nodeIds) {
      Node node;
      Point tcp;
      Point adj;
      if (id.contains("+")) {
        tcp = LayoutUtil.getPlusCenter(block);
        adj = getPlusAdjacent(block);
      } else {
        tcp = LayoutUtil.getMinusCenter(block);
        adj = getMinusAdjacent(block);
      }
      if (this.graph.containsKey(id)) {
        node = this.graph.get(id);
      } else {
        //node = new Node(id);
        node = new Node(block);
      }

      //Check whether adjacent is tile
      if (isTile(adj)) {
        Tile t = findTile(adj);
        Point cp = t.getCenter();
        //Determine the node-ids of the adjacent nodes
        String nodeId;
        switch (t.getTileType()) {
          case BLOCK -> {
            //A block has 2 sides ie 2 nodes so get the nearest point which is bk-n+/-
            if (isPlusAdjacent(t, tcp)) {
              cp = LayoutUtil.getPlusCenter(t);
              nodeId = t.getId() + "+";
            } else {
              cp = LayoutUtil.getMinusCenter(t);
              nodeId = t.getId() + "-";
            }
          }
          case SWITCH -> // As switch has 3 adjacent nodes, depending on the direction
            nodeId = getNodeIdForAdjacentSwitch(block, t);
          //TODO: CROSS!
          default ->
            nodeId = t.getId();
        }

        Edge e1 = new Edge(node.getId(), nodeId, LayoutUtil.euclideanDistance(tcp, cp));
        node.addEdge(e1);
        Logger.trace(e1);
      }
      this.graph.put(id, node);
      Logger.trace("Added " + node);
    }
  }

  void findEdgesFor(Node node) {
    Tile tile = findTile(node.getId());
    Point tcp = tile.getCenter();
    Set<Point> adjacentPoints = LayoutUtil.adjacentPointsFor(tile);

    //filter the points which do have a tile
    for (Point adj : adjacentPoints) {
      if (isTile(adj)) {
        Tile t = findTile(adj);
        Point cp = t.getCenter();

        //Determine the node-id of the adjacent node
        String nodeId;
        switch (t.getTileType()) {
          case BLOCK -> {
            //A block has 2 sides ie 2 nodes so get the nearest point which is  bk-n+/-
            if (isPlusAdjacent(t, tcp)) {
              cp = LayoutUtil.getPlusCenter(t);
              nodeId = t.getId() + "+";
            } else {
              cp = LayoutUtil.getMinusCenter(t);
              nodeId = t.getId() + "-";
            }
          }
          case SWITCH -> // As switch has 3 adjacent nodes, depending on the direction
            nodeId = getNodeIdForAdjacentSwitch(tile, t);
          default ->
            nodeId = t.getId();
        }
        Edge e1 = new Edge(node.getId(), nodeId, LayoutUtil.euclideanDistance(tcp, cp));
        node.addEdge(e1);
        Logger.trace(e1);
      }
    }
  }

  private void addNode(Tile tile) {
    if (tile != null && tile.getTileType() != null) {
      TileType tt = tile.getTileType();
      switch (tt) {
        case BLOCK ->
          addBlockNodes(tile);
        case SWITCH ->
          addSwitchNodes(tile);
        default -> {
          //Straight, Sensor, Signal, Curved, StraighDir and End
          String id = tile.getId();
          Node node;
          if (graph.containsKey(id)) {
            node = graph.get(id);
            Logger.trace(node + " allready exists");
          } else {
            node = new Node(tile);
            findEdgesFor(node);
            this.graph.put(id, node);
            Logger.trace("Added " + node);
          }
        }
      }
    }
  }

  public List<List<Node>> getAllBlockToBlockNodes() {
    List<List<Node>> fromToList = new ArrayList<>();

    Collection<Node> fromNodes = this.graph.values();
    Collection<Node> toNodes = this.graph.values();

    for (Node from : fromNodes) {
      Tile fromTile = findTile(from.getId());
      boolean fromBlock = isBlock(from.getId());
      String fromTileId = fromTile.getId();

      for (Node to : toNodes) {
        Tile toTile = findTile(to.getId());
        boolean toBlock = isBlock(to.getId());
        String toTileId = toTile.getId();

        if (fromBlock && toBlock && !from.getId().equals(to.getId()) && !fromTileId.equals(toTileId)) {
          List<Node> fromTo = new ArrayList<>();
          fromTo.add(from);
          fromTo.add(to);
          fromToList.add(fromTo);
        }
      }
    }
    return fromToList;
  }

  public Map<String, Node> buildGraph(Map<Point, Tile> tiles) {
    this.tiles.clear();
    this.altTilesLookup.clear();
    this.tileIdLookup.clear();
    this.graph.clear();

    this.tiles.putAll(tiles);
    //Build Tile id lookup
    for (Tile tile : this.tiles.values()) {
      //To find tiles which are a-symetrical (ie a block) which has coordinates not beeing the tile center
      for (Point ap : tile.getAltPoints()) {
        this.altTilesLookup.put(ap, tile);
      }
      this.tileIdLookup.put(tile.getId(), tile);
    }

    //Iterate through the tiles        
    for (Tile tile : this.tiles.values()) {
      Logger.trace("Evaluating tile: " + tile.getTileType() + ": " + tile.getId() + "; " + tile.xyToString());
      //A Tile can result in one or more nodes
      addNode(tile);
    }
    Logger.trace("Graph has " + graph.size() + " nodes...");
    return graph;
  }

  Map<String, Node> getGraph() {
    return graph;
  }

  private static Point getAdjacentPoint(Tile block, String plusMinus) {
    int x = block.getCenterX();
    int y = block.getCenterY();
    int w = block.getWidth();
    int h = block.getHeight();
    Orientation o = block.getOrientation();

    Point neighborPlus, neighborMin;
    switch (o) {
      case SOUTH:
        neighborPlus = new Point(x, y + h / 3 + Tile.GRID * 2);
        neighborMin = new Point(x, y - h / 3 - Tile.GRID * 2);
        break;
      case WEST:
        neighborPlus = new Point(x - w / 3 - Tile.GRID * 2, y);
        neighborMin = new Point(x + w / 3 + Tile.GRID * 2, y);
        break;
      case NORTH:
        neighborPlus = new Point(x, y - h / 3 - Tile.GRID * 2);
        neighborMin = new Point(x, y + h / 3 + Tile.GRID * 2);
        break;
      default:
        //East 
        neighborPlus = new Point(x + w / 3 + Tile.GRID * 2, y);
        neighborMin = new Point(x - w / 3 - Tile.GRID * 2, y);
        break;
    }
    if ("+".equals(plusMinus)) {
      return neighborPlus;
    } else {
      return neighborMin;
    }
  }

  public static boolean isPlusAdjacent(Tile block, Point point) {
    Point p = getAdjacentPoint(block, "+");
    return p.equals(point);
  }

  public static Point getPlusAdjacent(Tile block) {
    Point p = getAdjacentPoint(block, "+");
    return p;
  }

  public static Point getMinusAdjacent(Tile block) {
    Point p = getAdjacentPoint(block, "-");
    return p;
  }

  public static void main(String[] a) {
    System.setProperty("trackServiceSkipControllerInit", "true");

    LayoutAnalyzer la = new LayoutAnalyzer();

    la.buildGraph(LayoutUtil.loadLayout(true, false));

    System.exit(0);
  }
}
