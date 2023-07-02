/*
 * Copyright 2023 frans.
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

import jcs.entities.enums.AccessoryValue;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TrackTraveler implements Heuristic {

  boolean canTravelTo(Node from, Node to) {
    if (from == null || to == null) {
      return false;
    }

    if (from.getPreviousNode() != null && from.getTile().isJunction()) {
      //Check is the full path is possible
      Logger.trace("Checking path from: " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId());

      boolean isParentOnSwitchSide = from.getTile().isSwitchSide(from.getPreviousNode().getTile());
      boolean isParentOnStraightSide = from.getTile().isStraightSide(from.getPreviousNode().getTile());
      boolean isParentOnDivergingSide = from.getTile().isDivergingSide(from.getPreviousNode().getTile());

      Logger.trace("From " + from.getPreviousNode().getId() + " switchSide: " + isParentOnSwitchSide + " straightSide: " + isParentOnStraightSide + " divergingSide: " + isParentOnDivergingSide);

      boolean isToOnSwitchSide = from.getTile().isSwitchSide(to.getTile());
      boolean isToOnStraightSide = from.getTile().isStraightSide(to.getTile());
      boolean isToOnDivergingSide = from.getTile().isDivergingSide(to.getTile());

      Logger.trace("To " + to.getId() + " switchSide: " + isToOnSwitchSide + " straightSide: " + isToOnStraightSide + " divergingSide: " + isToOnDivergingSide);

      if (isParentOnSwitchSide && (isToOnDivergingSide || isToOnStraightSide)) {
        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is possible using " + (isToOnDivergingSide ? AccessoryValue.RED : AccessoryValue.GREEN));
        return from.getTile().isAdjacent(to.getTile());
      } else if (isParentOnStraightSide && isToOnSwitchSide) {
        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is possible using " + AccessoryValue.GREEN);
        return from.getTile().isAdjacent(to.getTile());
      } else if (isParentOnDivergingSide && isToOnSwitchSide) {
        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is possible using " + AccessoryValue.RED);
        return from.getTile().isAdjacent(to.getTile());
      } else {
        Logger.trace("Path from " + from.getPreviousNode().getId() + " via " + from.getId() + " to " + to.getId() + " is NOT possible");
        return false;
      }
    } else {
      return from.getTile().isAdjacent(to.getTile());
    }
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

  @Override
  public double calculate(Node current, Node to) {

    boolean canTravel = this.canTravelTo(current, to);

    AccessoryValue accessoryStatus = this.getAccessoryStatus(current, to);

    int dx = Math.abs(to.getX() - current.getX());
    int dy = Math.abs(to.getY() - current.getY());
    
    Logger.trace("canTravel: "+canTravel+" accessoryStatus: "+accessoryStatus);

    return dx + dy + (canTravel ? 0D : Double.MAX_VALUE);
  }

}
