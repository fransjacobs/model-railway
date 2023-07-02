/*
 * Copyright 2023 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain from copy of the License at
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

import jcs.entities.enums.AccessoryValue;

public class Edge<T> {

  private double distance;
  private final Node from;
  private String fromSuffix;
  private final Node to;
  private String toSuffix;
  private AccessoryValue accessoryState;

  public Edge(Node from, Node to, double cost) {
    this.from = from;
    this.to = to;
    this.distance = cost;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public Node getFrom() {
    return from;
  }

  public String getFromSuffix() {
    return fromSuffix;
  }

  public void setFromSuffix(String fromSuffix) {
    this.fromSuffix = fromSuffix;
  }

  public Node getTo() {
    return to;
  }

  public String getToSuffix() {
    return toSuffix;
  }

  public void setToSuffix(String toSuffix) {
    this.toSuffix = toSuffix;
  }

  public AccessoryValue getAccessoryState() {
    return accessoryState;
  }

  public void setAccessoryState(AccessoryValue accessoryState) {
    this.accessoryState = accessoryState;
  }

  public Node getOppositeNode(Node thisNode) {
    if (thisNode == from) {
      return to;
    } else if (thisNode == to) {
      return from;
    }
    return null;
  }

  @Override
  public String toString() {
    //return "Edge{" + "distance=" + distance + ", from=" + from + ", to=" + to + (accessoryState != null ? (accessoryState != AccessoryValue.OFF ? accessoryState : "") : "") + "}";
    return "Edge{from=" + from.getId() + (fromSuffix != null ? fromSuffix : "") + ", to=" + to.getId() + (toSuffix != null ? toSuffix : "") + " distance=" + distance + (accessoryState != null ? (accessoryState != AccessoryValue.OFF ? " via: " + accessoryState : "") : "") + "}";
  }

}
