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
 * A Node is the representation in the Graph of a tile with some extra feature to enable the track routing
 *
 */
public class Node {

  private final Tile tile;
  private final TileType tileType;
  private final Orientation orientation;

  private Node parent;
  private final List<Edge> edges;

  private final boolean junction;

  public Node(Tile tile) {
    this.tile = tile;
    this.tileType = tile.getTileType();
    this.orientation = tile.getOrientation();
    this.junction = TileType.SWITCH.equals(tile.getTileType()) || TileType.CROSS.equals(tile.getTileType());
    this.edges = new LinkedList<>();
  }

  /**
   *
   * @return the X (pixel) coordinate of the center of the tile
   */
  public int getX() {
    return tile.getCenterX();
  }

  /**
   *
   * @return the X number of the grid square (grid is 40 x 40 pix)
   */
  public int getGridX() {
    return (tile.getCenterX() - Tile.GRID) / (Tile.GRID * 2);
  }

  /**
   *
   * @return then Y (pixel) coordinate of the center of the tile
   */
  public int getY() {
    return tile.getCenterY();
  }

  /**
   *
   * @return the Y number of the grid square (grid is 40 x 40 pix)
   */
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

  /**
   * The main route of the tile is horizontal
   *
   * @return true when main route goes from East to West or vv
   */
  public boolean isHorizontal() {
    return Orientation.EAST == this.orientation || Orientation.WEST == this.orientation;
  }

  /**
   * The main route of the tile is vertical
   *
   * @return true when main route goes from North to South or vv
   */
  public boolean isVertical() {
    return Orientation.NORTH == this.orientation || Orientation.SOUTH == this.orientation;
  }

  /**
   * The main route of the tile is diagonal
   *
   * @return true when main route goes from North to East or West to South and vv
   */
  public boolean isDiagonal() {
    return TileType.CURVED.equals(tile.getTileType());
  }

  public TileType getTileType() {
    return tileType;
  }

  public Orientation getOrientation() {
    return orientation;
  }

  /**
   * Applicable for Straight, StraightDirection,Signal and Sensor A 'normal' Tile is 40 x 40 pix or one grid square. A block is 3 squares long.
   *
   * @param other node to traverse to
   * @return true whether it is possible to traverse from this node to the other node
   */
  private boolean canTraverseFromSquare(Node other) {
    if (isHorizontal()) {
      if (!(getGridY() == other.getGridY()) && !other.isJunction() && !other.isDiagonal()) {
        //both tiles need to be on the same Y
        return false;
      }
      if (!(TileType.CURVED == other.tileType || TileType.SWITCH == other.tileType || TileType.CROSS == other.tileType || TileType.END == other.tileType)) {
        //Check 'normal' rectangle shaped tiles
        int gXw, gXe; // check west and east
        //Block is 3 squares long so add +/-1 to the sides
        if (TileType.BLOCK == other.tileType) {
          gXw = other.getGridX() + 1;
          gXe = other.getGridX() - 1;
        } else {
          gXw = other.getGridX();
          gXe = other.getGridX();
        }
        int gX = getGridX();
        return (gX - 1) == gXw || (gX + 1) == gXe;
      } else if (TileType.CURVED == other.tileType) {
        // Curved; when horizontal on the east side a Curved W and S is possibe, on the west side a N and E is possible
        //determine on which side the curved tile exist
        if (getGridX() - 1 == other.getGridX()) {
          //west side
          return Orientation.NORTH == other.orientation || Orientation.EAST == other.orientation;
        } else if (getGridX() + 1 == other.getGridX()) {
          // east side
          return Orientation.WEST == other.orientation || Orientation.SOUTH == other.orientation;
        } else {
          //should never occur
          return false;
        }
      } else if (TileType.END == other.tileType) {
        //And end stop has only 1 connection
        //determine on which side the end tile exist
        if (getGridX() - 1 == other.getGridX()) {
          //west side
          return Orientation.WEST == other.orientation;
        } else if (getGridX() + 1 == other.getGridX()) {
          // east side
          return Orientation.EAST == other.orientation;
        } else {
          return false;
        }
      } else if (TileType.SWITCH == other.tileType) {
        return false;  //stub
      } else if (TileType.CROSS == other.tileType) {
        return false;  //stub
      } else {
        //Todo other tiletypes
        return false; //stub
      }
    } else {
      //Vertical
      if (!(getGridX() == other.getGridX()) && !other.isJunction() && !other.isDiagonal()) {
        return false;
      }
      if (!(TileType.CURVED.equals(other.tileType) || (TileType.SWITCH.equals(other.tileType)) || (TileType.CROSS.equals(other.tileType)) || (TileType.END.equals(other.tileType)))) {
        //Check 'normal' rectangle shaped tiles
        int gYn, gYs; //check north and south
        //Block is 3 squares long so add +/-1 to the sides
        if (TileType.BLOCK.equals(other.tileType)) {
          gYn = other.getGridY() + 1;
          gYs = other.getGridY() - 1;
        } else {
          gYn = other.getGridY();
          gYs = other.getGridY();
        }
        int gY = getGridY();
        return (gY - 1) == gYn || (gY + 1) == gYs;
      } else if (TileType.CURVED == other.tileType) {
        // Curved; when verstical on the north side a Curved E and S is possibe, on the south side a N and W is possible
        //determine on which side the curved tile exist
        if (getGridY() - 1 == other.getGridY()) {
          //north side
          return Orientation.SOUTH == other.orientation || Orientation.EAST == other.orientation;
        } else if (getGridY() + 1 == other.getGridY()) {
          // east side
          return Orientation.WEST == other.orientation || Orientation.NORTH == other.orientation;
        } else {
          //should never occur
          return false;
        }
      } else if (TileType.END == other.tileType) {
        //And end stop has only 1 connection
        //determine on which side the end tile exist
        if (getGridY() - 1 == other.getGridY()) {
          //north side
          return Orientation.NORTH == other.orientation;
        } else if (getGridY() + 1 == other.getGridY()) {
          // south side
          return Orientation.SOUTH == other.orientation;
        } else {
          return false;
        }
      } else if (TileType.SWITCH == other.tileType) {
        return false;  //stub
      } else if (TileType.CROSS == other.tileType) {
        return false;  //stub
      } else {
        //Todo other tile types
        return false; //stub
      }
    }
  }

  public boolean canTraverseTo(Node other) {
    //Can this node traverse to the node other.
    //A Tile is a rectangle (in most cases even a square.
    switch (tile.getTileType()) {
      case STRAIGHT:
        return canTraverseFromSquare(other);

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
