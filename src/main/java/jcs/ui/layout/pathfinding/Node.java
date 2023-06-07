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
 * A Node is the representation in the Graph of a tile.
 *
 * Every tile each tile represents a rail. The rail are drawn in the middle of the tile, so in case of a horizontal straight they connect in the middle of the west and east sides. So the edge
 * connection points should match on every connected tile/node
 *
 */
public class Node {

  private final Tile tile;
  private final TileType tileType;
  private final Orientation orientation;

  private Node parent;
  private final List<Edge> edges;

  public Node(Tile tile) {
    this.tile = tile;
    this.tileType = tile.getTileType();
    this.orientation = tile.getOrientation();

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
    return tile.getGridX();
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
    return tile.getGridY();
  }

  public boolean isBlock() {
    return TileType.BLOCK == this.tileType;
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
    return (Orientation.EAST == this.orientation || Orientation.WEST == this.orientation) && TileType.CURVED != tileType;
  }

  /**
   * The main route of the tile is vertical
   *
   * @return true when main route goes from North to South or vv
   */
  public boolean isVertical() {
    return (Orientation.NORTH == this.orientation || Orientation.SOUTH == this.orientation) && TileType.CURVED != tileType;
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

  public boolean canTravelTo(Node other) {
    return this.tile.canTraverseTo(other.tile);
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
    return TileType.SWITCH == this.tileType || TileType.CROSS == this.tileType;
  }

  public boolean contains(Edge edge) {
    List<Edge> snaphot = new LinkedList<>(this.edges);

    for (Edge e : snaphot) {
      if (e.getFromId().equals(edge.getFromId()) && e.getToId().equals(edge.getToId())) {
        return true;
      }
    }
    return false;
  }

  public Tile getTile() {
    return tile;
  }

  @Override
  public String toString() {
    return "Node{" + "id=" + tile.getId() + '}';
  }

}
