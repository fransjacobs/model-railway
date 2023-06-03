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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.NORTH;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;
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
import jcs.ui.layout.tiles.enums.Direction;

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
  private final Map<Orientation, Point> edgeConnections;

  private Node parent;
  private final List<Edge> edges;

  private final boolean junction;

  public Node(Tile tile) {
    this.tile = tile;
    this.tileType = tile.getTileType();
    this.orientation = tile.getOrientation();

    this.edgeConnections = new HashMap<>();

    this.junction = TileType.SWITCH.equals(tile.getTileType()) || TileType.CROSS.equals(tile.getTileType());
    this.edges = new LinkedList<>();

    putEdgeConnections();

  }

  private void putEdgeConnections() {
    switch (tileType) {
      case BLOCK -> {
        //Horizontal
        if (Orientation.EAST == this.orientation || Orientation.WEST == this.orientation) {
          this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() + Tile.GRID * 3, tile.getCenterY()));
          this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() - Tile.GRID * 3, tile.getCenterY()));
        } else {
          //Vertical
          this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID * 3));
          this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID * 3));
        }
        break;
      }
      case END -> {
        switch (this.orientation) {
          case SOUTH ->
            this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID));
          case WEST ->
            this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() + Tile.GRID, tile.getCenterY()));
          case NORTH ->
            this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID));
          default -> //EAST
            this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() - Tile.GRID, tile.getCenterY()));
        }
      }
      case CURVED -> {
        switch (this.orientation) {
          case SOUTH -> {
            this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() - Tile.GRID, tile.getCenterY()));
            this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID));
          }
          case WEST -> {
            this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() - Tile.GRID, tile.getCenterY()));
            this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID));
          }
          case NORTH -> {
            this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID));
            this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() + Tile.GRID, tile.getCenterY()));
          }
          default -> {
            //EAST
            this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() + Tile.GRID, tile.getCenterY()));
            this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID));
          }
        }
      }
      case SWITCH -> {
        switch (this.orientation) {
          case SOUTH -> {
            this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID));
            this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID));
            if (Direction.LEFT == tile.getDirection()) {
              this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() - Tile.GRID, tile.getCenterY()));
            } else {
              this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() + Tile.GRID, tile.getCenterY()));
            }
          }
          case WEST -> {
            this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() + Tile.GRID, tile.getCenterY()));
            this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() - Tile.GRID, tile.getCenterY()));
            if (Direction.LEFT == tile.getDirection()) {
              this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID));
            } else {
              this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID));
            }
          }
          case NORTH -> {
            this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID));
            this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID));
            if (Direction.LEFT == tile.getDirection()) {
              this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() + Tile.GRID, tile.getCenterY()));
            } else {
              this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() - Tile.GRID, tile.getCenterY()));
            }
          }
          default -> {
            //EAST
            this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() + Tile.GRID, tile.getCenterY()));
            this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() - Tile.GRID, tile.getCenterY()));
            if (Direction.LEFT == tile.getDirection()) {
              this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID));
            } else {
              this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID));
            }
          }
        }
      }
      case CROSS -> {
        //TODO
      }
      default -> {
        //STRAIGHT, STRAIGHT_DIR, SIGNAL, SENSOR
        if (Orientation.EAST == this.orientation || Orientation.WEST == this.orientation) {
          //Horizontal
          this.edgeConnections.put(Orientation.EAST, new Point(tile.getCenterX() + Tile.GRID, tile.getCenterY()));
          this.edgeConnections.put(Orientation.WEST, new Point(tile.getCenterX() - Tile.GRID, tile.getCenterY()));
        } else {
          //Vertical
          this.edgeConnections.put(Orientation.NORTH, new Point(tile.getCenterX(), tile.getCenterY() - Tile.GRID));
          this.edgeConnections.put(Orientation.SOUTH, new Point(tile.getCenterX(), tile.getCenterY() + Tile.GRID));
        }
      }
    }
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
    boolean canTravel = false;
//    Logger.trace(tileType + " O: " + orientation + " P: " + this.edgeConnections.get(this.orientation) + " C: " + this.tile.xyToString());
//    for (Point p : edgeConnections.values()) {
//      Logger.trace("O: " + orientation + " P: " + p + " C: " + tile.xyToString());
//    }
//    for (Point p : other.edgeConnections.values()) {
//      Logger.trace("Other: " + other.tileType + " O: " + other.orientation + " P: " + p + " C: " + other.tile.xyToString());
//    }

    for (Point p : this.edgeConnections.values()) {
      canTravel = other.edgeConnections.containsValue(p);
      if (canTravel) {
        break;
      }
    }

    return canTravel;
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

  public Tile getTile() {
    return tile;
  }

  @Override
  public String toString() {
    return "Node{" + "id=" + tile.getId() + '}';
  }

}
