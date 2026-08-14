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

import java.util.Objects;
import jcs.entities.AccessoryBean.AccessoryValue;

public class Edge<T> {

  private double distance;
  private final Node from;
  private String fromSuffix;
  private final Node to;
  private String toSuffix;

  private AccessoryValue pointValue;

  public Edge(Node from, Node to, double distance) {
    this.from = from;
    this.to = to;
    this.distance = distance;
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

  public Node getOpposite(Node thisNode) {
    if (thisNode == from) {
      return to;
    } else if (thisNode == to) {
      return from;
    }
    return null;
  }

  public AccessoryValue getPointValue() {
    return pointValue;
  }

  public void setPointValue(AccessoryValue pointValue) {
    this.pointValue = pointValue;
  }

  @Override
  public String toString() {
    return "Edge from: " + from.getId() + (fromSuffix != null ? fromSuffix : "") + " to: " + to.getId() + (toSuffix != null ? toSuffix : "") + ", distance: " + distance;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + Objects.hashCode(this.from);
    hash = 97 * hash + Objects.hashCode(this.fromSuffix);
    hash = 97 * hash + Objects.hashCode(this.to);
    hash = 97 * hash + Objects.hashCode(this.toSuffix);
    hash = 97 * hash + Objects.hashCode(this.pointValue);
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
    final Edge<?> other = (Edge<?>) obj;
    if (!Objects.equals(this.fromSuffix, other.fromSuffix)) {
      return false;
    }
    if (!Objects.equals(this.toSuffix, other.toSuffix)) {
      return false;
    }
    if (!Objects.equals(this.from, other.from)) {
      return false;
    }
    if (!Objects.equals(this.to, other.to)) {
      return false;
    }
    return this.pointValue == other.pointValue;
  }

}
