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

import java.util.Objects;
import jcs.entities.enums.AccessoryValue;

/**
 *
 * @author fransjacobs
 */
public class Edge {

  private String id;

  private String fromId;
  private String fromSuffix;
  private String toId;
  private String toSuffix;

  //private Orientation travelOrientation;
  private AccessoryValue pathDirection;

  public Edge(String fromId, String toId, double cost) {
    this(fromId, toId);
  }

  public Edge(String fromId, String toId) {
    this(fromId, toId, AccessoryValue.OFF);
  }

  public Edge(String fromId, String toId, AccessoryValue pathDirection) {
    this(fromId, toId, null, null, pathDirection);
  }

  public Edge(String fromId, String toId, String fromSuffix, String toSuffix) {
    this(fromId, toId, fromSuffix, toSuffix, AccessoryValue.OFF);
  }

  public Edge(String fromId, String toId, String fromSuffix, String toSuffix, AccessoryValue pathDirection) {
    this.fromId = fromId;
    this.toId = toId;
    this.fromSuffix = fromSuffix;
    this.toSuffix = toSuffix;

    this.pathDirection = pathDirection;

    this.id = fromId + (fromSuffix != null ? fromSuffix : "") + "->" + toId + (toSuffix != null ? toSuffix : "");
  }

  public String getId() {
    return id;
  }

  public String getFromId() {
    return fromId;
  }

  public String getToId() {
    return toId;
  }

  //public Orientation getTravelOrientation() {
  //  return travelOrientation;
  //}
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
    //return "Edge (" + fromId + (fromSuffix != null ? fromSuffix : "") + " -> " + toId + (toSuffix != null ? toSuffix : "") + ") dir: " + travelOrientation + (AccessoryValue.OFF != pathDirection ? " path: " + pathDirection : "");
    //return "Edge [" + id + "] dir: " + travelOrientation + (AccessoryValue.OFF != pathDirection ? " path: " + pathDirection : "");
    return "Edge [" + id + "]" + (AccessoryValue.OFF != pathDirection ? " path: " + pathDirection : "");
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(this.id);
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
    final Edge other = (Edge) obj;
    return Objects.equals(this.id, other.id);
  }

}
