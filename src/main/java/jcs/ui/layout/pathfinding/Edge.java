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

import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;

/**
 *
 * @author fransjacobs
 */
public class Edge {

  private Node from;
  private Node to;

  private String fromId;
  private String toId;
  private Orientation travelOrientation;
  private AccessoryValue pathDirection;

  public Edge(Node from, Node to) {
    this.from = from;
    this.to = to;
  }

  public Edge(String fromId, String toId, double cost) {
    this(fromId, toId, null, null);
  }

  public Edge(String fromId, String toId, Orientation travelOrientation) {
    this(fromId, toId, travelOrientation, AccessoryValue.OFF);
  }

  public Edge(String fromId, String toId, Orientation travelOrientation, AccessoryValue pathDirection) {
    this.fromId = fromId;
    this.toId = toId;
    this.travelOrientation = travelOrientation;
    this.pathDirection = pathDirection;
  }

  public String getFromId() {
    return (from != null ? from.getId() : fromId);
  }

  public String getToId() {
    return (to != null ? to.getId() : toId);
  }

  @Override
  public String toString() {
    return "Edge (" + (from != null ? from.getId() : fromId) + " -> " + (to != null ? to.getId() : toId) + ") dir: " + travelOrientation + (AccessoryValue.OFF!=pathDirection?" path: " + pathDirection:"");
  }

}
