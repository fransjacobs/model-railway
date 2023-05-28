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

import java.util.LinkedList;
import java.util.List;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import static jcs.entities.enums.TileType.BLOCK;
import static jcs.entities.enums.TileType.CROSS;
import static jcs.entities.enums.TileType.CURVED;
import static jcs.entities.enums.TileType.END;
import static jcs.entities.enums.TileType.SENSOR;
import static jcs.entities.enums.TileType.SIGNAL;
import static jcs.entities.enums.TileType.STRAIGHT;
import static jcs.entities.enums.TileType.STRAIGHT_DIR;
import static jcs.entities.enums.TileType.SWITCH;
import jcs.ui.layout.Tile;

/**
 *
 * @author fransjacobs
 */
public class Node {

  private final Tile tile;

  private Node parent;
  private List<Edge> edges;

  private final boolean junction;

  public Node(Tile tile) {
    this.tile = tile;
    this.junction = TileType.SWITCH.equals(tile.getTileType()) || TileType.CROSS.equals(tile.getTileType());
    this.edges = new LinkedList<>();
  }

//  public Node(String id) {
//    this(id, null);
//  }
//  public Node(String id, Tile tile) {
//    this.id = id;
//    this.tile = tile;
//  }
  public int getX() {
    return tile.getCenterX();
  }

  public int getGridX() {
    return (tile.getCenterX() - Tile.GRID) / (Tile.GRID * 2);
  }

  public int getY() {
    return tile.getCenterY();
  }

  public int getGridY() {
    return (tile.getCenterY() - Tile.GRID) / (Tile.GRID * 2);
  }

  /**
   * A tile has 1 or more connection or transitions, i.e. where the tiles connect
   *
   * @return the number of connection possibilities a node has.
   */
  public int transitions() {
    return switch (tile.getTileType()) {
      case STRAIGHT ->
        2;
      case STRAIGHT_DIR ->
        2;
      case SENSOR ->
        2;
      case SIGNAL ->
        2;
      case CURVED ->
        2;
      case BLOCK ->
        2;
      case SWITCH ->
        3;
      case CROSS ->
        4;
      case END ->
        1;
      default ->
        0;
    };
  }

  public boolean isHorizontal() {
    return Orientation.EAST.equals(tile.getOrientation()) || Orientation.WEST.equals(tile.getOrientation());
  }

  public boolean isVertical() {
    return Orientation.NORTH.equals(tile.getOrientation()) || Orientation.SOUTH.equals(tile.getOrientation());
  }

  TileType getTileType() {
    return tile.getTileType();
  }

  //een standard tile is precies 1 vakje van het grid tile met xy 10,10 is grid 0,0; xy 50,10 is dus 1,0, 50,50 is 1,1   
  // v een rechte tile op 90,90 (2,2) die horizontaal is heeft een verbinding aan de linkerkan met 50,90 (1,2) en rechts met 130,90 (3,2)
// waar moet je op letten om the checken of je van deze haar de ander kan
  //als dit tte rechte is dan kan het alleen horizontaal of vertical. the oande moet in geval van een recte ook horizontall of verticall zijn
  // pseudo code
  // kan reizen
  /**
   * Applicable for Straight, StraightDirection,Signal and Sensor
   *
   * @param other node to traverse to
   * @return true whet it is possible to traverse from this node to the other node
   */
  private boolean canTraverseToSquare(Node other) {
    if (!(TileType.STRAIGHT.equals(other.getTileType())
            || TileType.SENSOR.equals(other.getTileType())
            || TileType.SIGNAL.equals(other.getTileType())
            || TileType.STRAIGHT_DIR.equals(other.getTileType()))) {
      return false;
    }
    if (isHorizontal()) {
      if (!(other.isHorizontal() && (getGridY() == other.getGridY()))) {
        return false;
      }
      return ((getGridX() + 1 == other.getGridX()) || (getGridX() - 1 == other.getGridX()));
    } else {
      if (!(other.isVertical() && (getGridX() == other.getGridX()))) {
        return false;
      }
      return ((getGridY() + 1 == other.getGridY()) || (getGridY() - 1 == other.getGridY()));
    }
  }

  public boolean canTraverseTo(Node other) {
    //Can this node traverse to the node other.
    //A Tile is a rectangle (in most cases even a square.
    switch (tile.getTileType()) {
      case STRAIGHT:
        return canTraverseToSquare(other);

      default:
        return false;
    }
  }

  public String getId() {
    return this.tile.getId();
  }

  public void addEdge(Edge edge) {
    this.edges.add(edge);
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public boolean isJunction() {
    return junction;
  }

  public boolean contains(Edge edge) {
    List<Edge> snaphot = new LinkedList<>(this.edges);

    for (Edge e : snaphot) {
      if (e.getSourceId().equals(edge.getSourceId()) && e.getTargetId().equals(edge.getTargetId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if travel is possible toNode this node toNode the 'toNode' Node
   *
   * @param toNode the travel destination node
   * @return true when travel is possible
   */
  @Deprecated
  public boolean canTravel(Node toNode) {
    if (this.isJunction() && this.getParent() != null && this.getParent().isJunction() && toNode.isJunction()) {
      //Logger.trace("Junctions: " + this.getParent().getId() + " -> " + this.getId() + " -> " + toNode.getId());

      String parentId = this.getParent().getId();
      String tgtId = toNode.getId();

      //a path from xx-G via xx to xx-R or vv is not possible
      if (parentId.replace("-G", "").replace("-R", "").equals(tgtId.replace("-G", "").replace("-R", ""))) {
        if (parentId.equals(tgtId)) {
          //Logger.trace("Can Travel from: " + parentId + this.getId() + " -> " + tgtId);
          return true;
        } else {
          //Logger.trace("Can't travel from: " + parentId + " -> " + this.getId() + " -> " + tgtId);
          return false;
        }
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  public Tile getTile() {
    return tile;
  }

  @Override
  public String toString() {
    return "Node{" + "id=" + tile.getId() + '}';
  }

}
