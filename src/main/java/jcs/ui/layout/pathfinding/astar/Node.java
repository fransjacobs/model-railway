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
package jcs.ui.layout.pathfinding.astar;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jcs.entities.enums.AccessoryValue;
import jcs.ui.layout.Tile;
import jcs.ui.layout.tiles.Block;
import org.tinylog.Logger;

public class Node implements Comparable<Node> {

  private final Tile tile;
  private String suffix;
  private AccessoryValue accessoryState;

  private double g;
  private double h;

  private Node previousNode;
  private final Set<Edge> edges = new HashSet<>();

  public Node(Tile tile) {
    this.tile = tile;
  }

  public Tile getTile() {
    return tile;
  }

  public String getId() {
    return tile.getId();
  }

  public int getX() {
    return this.tile.getCenterX();
  }

  public int getY() {
    return this.tile.getCenterY();
  }

  public int getX(String suffix) {
    if (isBlock()) {
      return ((Block) this.tile).getNeighborPoint(suffix).x;
    } else {
      return getX();
    }
  }

  public int getY(String suffix) {
    if (isBlock()) {
      return ((Block) this.tile).getNeighborPoint(suffix).y;
    } else {
      return getY();
    }
  }

  public Point getAltPoint(String suffix) {
    if (isBlock()) {
      return ((Block) this.tile).getAltPoint(suffix);
    } else {
      return this.tile.getCenter();
    }
  }

  public boolean isBlock() {
    return this.tile.isBlock();
  }

  public boolean isJunction() {
    return this.tile.isJunction();
  }

  public double getG() {
    return g;
  }

  void setG(double g) {
    this.g = g;
  }

  public double getH() {
    return h;
  }

  void setH(double h) {
    this.h = h;
  }

  public Node getPreviousNode() {
    return previousNode;
  }

  public void setPreviousNode(Node previousNode) {
    this.previousNode = previousNode;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public AccessoryValue getAccessoryState() {
    return accessoryState;
  }

  public void setAccessoryState(AccessoryValue accessoryState) {
    this.accessoryState = accessoryState;
  }

  public Set<Edge> getEdges() {
    return getEdges(null);
  }

  public Set<Edge> getEdges(String suffix) {
    if (suffix != null) {
      return edges.stream().filter(e -> (suffix.equals(e.getFromSuffix()) || suffix.equals(e.getToSuffix()))).collect(Collectors.toSet());
    } else {
      return edges;
    }
  }

  public void addEdge(Edge edge) {
    edges.add(edge);
  }

  public double getF() {
    return g + h;
  }

  AccessoryValue getAccessoryStatus(Node from, Node to) {
    if (from == null || to == null) {
      return AccessoryValue.OFF;
    }
    if (from.getPreviousNode() != null && from.getTile().isJunction()) {
      boolean isParentOnSwitchSide = from.getTile().isSwitchSide(from.getPreviousNode().getTile());
      boolean isParentOnStraightSide = from.getTile().isStraightSide(from.getPreviousNode().getTile());
      boolean isParentOnDivergingSide = from.getTile().isDivergingSide(from.getPreviousNode().getTile());

      boolean isToOnSwitchSide = from.getTile().isSwitchSide(to.getTile());
      boolean isToOnStraightSide = from.getTile().isStraightSide(to.getTile());
      boolean isToOnDivergingSide = from.getTile().isDivergingSide(to.getTile());

      if (isParentOnSwitchSide && (isToOnDivergingSide || isToOnStraightSide)) {
        return (isToOnDivergingSide ? AccessoryValue.RED : AccessoryValue.GREEN);
      } else if (isParentOnStraightSide && isToOnSwitchSide) {
        return AccessoryValue.GREEN;
      } else if (isParentOnDivergingSide && isToOnSwitchSide) {
        return AccessoryValue.RED;
      } else {
        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is NOT possible");
        return AccessoryValue.OFF;
      }
    } else {
      return AccessoryValue.OFF;
    }
  }

  public void retrievePath(List<Node> path) {
    if (previousNode != null) {
      previousNode.retrievePath(path);
    }

    path.add(this);
    if (previousNode != null && previousNode.isJunction()) {
      previousNode.accessoryState = this.getAccessoryStatus(previousNode, this);
    }
  }

  @Override
  public int compareTo(Node o) {
    double dif = getF() - o.getF();
    return dif == 0 ? 0 : dif > 0 ? 1 : -1;
  }

  @Override
  public String toString() {
    return "Node id: " + getId() + ", g: " + g + ", h: " + h + ", prevId: " + (previousNode != null ? previousNode.getId() : "") + (accessoryState != null ? " [" + accessoryState + "]" : "");
  }

}
