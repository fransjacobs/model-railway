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

import java.util.ArrayList;
import java.util.List;
import jcs.ui.layout.Tile;

public class Node implements Comparable<Node> {

  public static enum State {
    UNVISITED, OPEN, CLOSED
  };

  private final Tile tile;
  private State state = State.UNVISITED;
  //private boolean blocked = false;

  private double cost; // cost
  private double h; // heuristic
  // f = cost + h

  private Node previousNode;

  private final List<Edge> edges = new ArrayList<>();

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

  //A block node is a special case 
  public boolean isBlock() {
    return this.tile.isBlock();
  }

  public boolean isJunction() {
    return this.tile.isJunction();
  }

  public State getState() {
    return state;
  }

  void setState(State state) {
    this.state = state;
  }

  public double getCost() {
    return cost;
  }

  void setCost(double cost) {
    this.cost = cost;
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

  public List<Edge> getEdges() {
    return edges;
  }

  public void addEdge(Edge edge) {
    edges.add(edge);
  }

  // f(n) = cost(n) + h(n) -> cost + heuristic
  public double getF() {
    return cost + h;
  }

  public void retrievePath(List<Node> path) {
    if (previousNode != null) {
      previousNode.retrievePath(path);
    }
    path.add(this);
  }

  @Override
  public int compareTo(Node o) {
    double dif = getF() - o.getF();
    return dif == 0 ? 0 : dif > 0 ? 1 : -1;
  }

  @Override
  public String toString() {
    //return "Node{" + "id=" + getId() + ", state=" + state + ", cost=" + cost + ", h=" + h + ", previousNode=" + (previousNode!=null?previousNode.getId():"") + ", edges=" + edges + "}";
    return "Node{" + "id=" + getId() + ", state=" + state + ", cost=" + cost + ", h=" + h + ", previousNode=" + (previousNode != null ? previousNode.getId() : "") + "}";
  }

}
