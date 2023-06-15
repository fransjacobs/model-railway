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

  private String fromId;
  private String fromSuffix;
  private String toId;
  private String toSuffix;
  private Orientation travelOrientation;
  private AccessoryValue pathDirection;

  public Edge(String fromId, String toId, double cost) {
    this(fromId, toId, null, null);
  }

  public Edge(String fromId, String toId, Orientation travelOrientation) {
    this(fromId, toId, travelOrientation, AccessoryValue.OFF);
  }

  public Edge(String fromId, String toId, Orientation travelOrientation, AccessoryValue pathDirection) {
    this(fromId, toId, null, null, travelOrientation, pathDirection);
  }

  public Edge(String fromId, String toId, String fromSuffix, String toSuffix, Orientation travelOrientation) {
    this(fromId, toId, fromSuffix, toSuffix, travelOrientation, AccessoryValue.OFF);
  }

  public Edge(String fromId, String toId, String fromSuffix, String toSuffix, Orientation travelOrientation, AccessoryValue pathDirection) {
    this.fromId = fromId;
    this.toId = toId;
    this.fromSuffix = fromSuffix;
    this.toSuffix = toSuffix;

    this.travelOrientation = travelOrientation;
    this.pathDirection = pathDirection;
  }

  public String getFromId() {
    return fromId;
  }

  public String getToId() {
    return toId;
  }

  public Orientation getTravelOrientation() {
    return travelOrientation;
  }

  public String getFromSuffix() {
    return fromSuffix;
  }

  public String getToSuffix() {
    return toSuffix;
  }

  public AccessoryValue getPathDirection() {
    return pathDirection;
  }

  @Override
  public String toString() {
    return "Edge (" + fromId + (fromSuffix != null ? fromSuffix : "") + " -> " + toId + (toSuffix != null ? toSuffix : "") + ") dir: " + travelOrientation + (AccessoryValue.OFF != pathDirection ? " path: " + pathDirection : "");
  }

}
