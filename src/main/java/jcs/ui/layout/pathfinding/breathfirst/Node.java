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
package jcs.ui.layout.pathfinding.breathfirst;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import jcs.ui.layout.Tile;

/**
 * A Node is the representation in the Graph of a tile.
 *
 * Every tile each tile represents a rail. The rail are drawn in the middle of the tile, so in case of a horizontal straight they connect in the middle of the west and east sides. So the edge
 * connection points should match on every connected tile/node
 *
 */
public class Node implements Comparable<Node> {

  private final Tile tile;
  private final TileType tileType;
  private final Orientation orientation;
  private AccessoryValue accessoryState;

  private final String suffix;

  private Node parent;
  private final Set<Edge> edges;

  public Node(Tile tile) {
    this(tile, null);
  }

  public Node(Tile tile, String suffix) {
    this.tile = tile;
    this.tileType = tile.getTileType();
    this.orientation = tile.getOrientation();
    this.suffix = suffix;
    this.edges = new HashSet<>();
  }

  public int getX() {
    return tile.getCenterX();
  }

  public int getGridX() {
    return tile.getGridX();
  }

  public int getY() {
    return tile.getCenterY();
  }

  public int getGridY() {
    return tile.getGridY();
  }

  public boolean isBlock() {
    return TileType.BLOCK == this.tileType;
  }

  public String getSuffix() {
    return this.suffix;
  }

  public TileType getTileType() {
    return tileType;
  }

  public Orientation getOrientation() {
    return orientation;
  }

  public AccessoryValue getAccessoryState() {
    return accessoryState;
  }

  public void setAccessoryState(AccessoryValue accessoryState) {
    this.accessoryState = accessoryState;
  }

  public String getId() {
    return this.tile.getId() + (this.suffix != null ? this.suffix : "");
  }

  public String getTileId() {
    return this.tile.getId();
  }

  public void addEdge(Edge edge) {
    this.edges.add(edge);
  }

  public void addEdges(Collection<Edge> edges) {
    this.edges.addAll(edges);
  }

  public Set<Edge> getEdges() {
    return edges;
  }

  public List<Edge> getEdgeList() {
    return edges.stream().collect(Collectors.toList());
  }

  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public boolean isJunction() {
    return this.tile.isJunction();
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
  public int compareTo(Node other) {
    return this.getId().compareTo(other.getId());
  }

  public double calculateHeuristic(Node target) {
    double a = (target.getX() - this.getX());
    double b = (target.getY() - this.getY());
    double d = Math.hypot(a, b);

    return d;
  }

  @Override
  public String toString() {
    return tile.getId();
  }

  public String toLogString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Node: ");
    sb.append(getId());
    for (Edge edge : this.edges) {
      sb.append(" ");
      sb.append(edge.toString());
    }
    return sb.toString();
    //return "Node{" + "id=" + tile.getId() + '}';
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 73 * hash + Objects.hashCode(this.tile);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Node other = (Node) obj;
    return Objects.equals(this.tile, other.tile);
  }

}
