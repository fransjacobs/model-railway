/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.autopilot.state;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import jcs.JCS;
import jcs.entities.AccessoryBean;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.StationBean;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Block;
import org.tinylog.Logger;

/**
 * RouteManager is in charge of finding a route.
 */
class RouteManager {

  private Dispatcher dispatcher;

  private RouteBean routeBean;

  private boolean swapLocomotiveDirection;

  private String departureBlockId;
  private String destinationBlockId;

  //Enter Sensor of the destination
  private Integer enterSensorId;
  //In Sensor of the destination
  private Integer inSensorId;
  //The Occupation sensor of the departure 
  private Integer occupationSensorId;
  //The exit of the departure
  private Integer exitSensorId;

  RouteManager(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;

  }

//          if (searchRoute(dispatcher)) {
//          canAdvanceToNextState = reserveRoute(dispatcher);
//        }
//
  RouteBean getRouteBean() {
    return routeBean;
  }

  void setRouteBean(RouteBean routeBean) {
    this.routeBean = routeBean;
  }

  String getDepartureBlockId() {
    return departureBlockId;
  }

  void setDepartureBlockId(String departureBlockId) {
    this.departureBlockId = departureBlockId;
  }

  String getDestinationBlockId() {
    return destinationBlockId;
  }

  void setDestinationBlockId(String destinationBlockId) {
    this.destinationBlockId = destinationBlockId;
  }

  Integer getEnterSensorId() {
    return enterSensorId;
  }

  void setEnterSensorId(Integer enterSensorId) {
    this.enterSensorId = enterSensorId;
  }

  Integer getInSensorId() {
    return inSensorId;
  }

  void setInSensorId(Integer inSensorId) {
    this.inSensorId = inSensorId;
  }

  Integer getOccupationSensorId() {
    return occupationSensorId;
  }

  void setOccupationSensorId(Integer occupationSensorId) {
    this.occupationSensorId = occupationSensorId;
  }

  Integer getExitSensorId() {
    return exitSensorId;
  }

  void setExitSensorId(Integer exitSensorId) {
    this.exitSensorId = exitSensorId;
  }

  boolean searchRoute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    Logger.trace("Search a free route for " + locomotive.getName() + "...");

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    //Is the departure block part of a station.
    StationBean station = dispatcher.getStation(departureBlock);
    int locCount = 1;
    int minLocCount = 1;
    if (station != null) {
      minLocCount = station.getMinLocomotives();
      locCount = PersistenceFactory.getService().getLocomotiveCount(station).intValue();
      if (station.getLocomotiveCount() != locCount) {
        Logger.warn("Adjusting number of locomotives in station " + station.getName() + " to " + locCount);
        station.setLocomotiveCount(locCount);
        PersistenceFactory.getService().persist(station);
      }
      Logger.trace(departureBlock.getId() + " is member of Station " + station.getName() + " min Locs: " + minLocCount + " cur locs: " + locCount);
    }

    if (departureBlock.getLogicalDirection() == null) {
      departureBlock.setLogicalDirection(locomotive.getDirection().getDirection());
    }
    LocomotiveBean.Direction logicalDirection = LocomotiveBean.Direction.get(departureBlock.getLogicalDirection());

    TileBean tileBean = PersistenceFactory.getService().getTileBean(departureBlock.getTileId());
    TileBean.Orientation blockOrientation = tileBean.getOrientation();

    String departureSuffix = departureBlock.getDepartureSuffix();
    if (departureSuffix == null) {
      departureSuffix = Block.getDepartureSuffix(blockOrientation, logicalDirection);
    }

    Logger.trace("Loco " + locomotive.getName() + " is in block " + departureBlock.getId() + ". Direction " + logicalDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    //only search for a route if we can depart
    List<RouteBean> routes = Collections.emptyList();
    if (locCount >= minLocCount) {
      //Search for the possible routes
      routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
      Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");
    } else {
      String stationName;
      if (station != null) {
        stationName = station.getName();
      } else {
        stationName = "?";
      }
      Logger.trace("nr of locs (" + locCount + ") is lower than min locs (" + minLocCount + ") for station " + stationName);
    }

    List<RouteBean> checkedRoutes = new ArrayList<>();
    boolean commuter = locomotive.isCommuter();

    //No routes found or possible.
    //When the Locomotive is a commuter train and the departure block allows a direction change it can reverse direction. Lets try that...
    if (routes.isEmpty() && commuter && departureBlock.isAllowDirectionChange() && locCount >= minLocCount) {
      LocomotiveBean.Direction oldDirection = logicalDirection;
      //Direction newDirection = locomotive.toggleDispatcherDirection();
      LocomotiveBean.Direction newDirection = LocomotiveBean.toggle(oldDirection);
      Logger.trace("Reversing Locomotive, from " + oldDirection + " to " + newDirection + "...");

      swapLocomotiveDirection = true;
      //Do NOT persist the direction yet, just test....
      departureBlock.setLogicalDirection(newDirection.getDirection());
      //Now flip the departure direction
      if ("-".equals(departureSuffix)) {
        departureSuffix = "+";
      } else {
        departureSuffix = "-";
      }

      Logger.trace("2nd attempt for Loco " + locomotive.getName() + " is in block " + departureBlock.getId() + ". Direction " + newDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");
      routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);

      Logger.trace("After the 2nd attempt, there " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s). " + (!routes.isEmpty() ? "Direction of " + locomotive.getName() + " must be swapped!" : ""));
    }

    //Found possible routes check on the destination for the sensors and permissions
    for (RouteBean route : routes) {
      String destinationTileId = route.getToTileId();
      BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(destinationTileId);
      //Check the sensors 
      boolean plusInActive = !destinationBlock.getPlusSensorBean().isActive();
      boolean minInActive = !destinationBlock.getMinSensorBean().isActive();

      boolean allowCommuter = destinationBlock.isAllowCommuterOnly();
      boolean allowNonCommuter = destinationBlock.isAllowNonCommuterOnly();

      boolean allowed = isAllowed(allowCommuter, allowNonCommuter, commuter);

      Logger.trace("Destination " + destinationBlock.getId() + " Train type commuter: " + commuter + " Permission " + allowed + " sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));

      if (plusInActive && minInActive && allowed && turnoutsNotLocked(route)) {
        checkedRoutes.add(route);
      }
    }

    //Randomly pick a route in case multiple routes are found...
    int rIdx = 0;
    if (checkedRoutes.size() > 1) {
      //Choose randomly the route
      Random random = new Random();
      for (int i = 0; i < 10; i++) {
        //Seed a bit....
        random.ints(0, checkedRoutes.size()).findFirst();
      }
      rIdx = random.ints(0, checkedRoutes.size()).findFirst().getAsInt();
    }

    RouteBean route = null;
    if (!checkedRoutes.isEmpty()) {
      route = checkedRoutes.get(rIdx);
      Logger.trace("Choosen route " + route.toLogString());
      //persist the departure block
      PersistenceFactory.getService().persist(departureBlock);
    } else {
      if (locCount >= minLocCount) {
        Logger.debug("No route available for " + locomotive.getName() + " ...");
      } else {
        Logger.debug("No route available because the Station occupation (" + locCount + ") is less then the min occupation (" + minLocCount + ")");
      }
      if (swapLocomotiveDirection) {
        //Reverse the direction change
        LocomotiveBean.Direction oldDirection = LocomotiveBean.toggle(LocomotiveBean.Direction.get(departureBlock.getLogicalDirection()));
        Logger.trace("Rollback Locomotive reverse to " + oldDirection + "...");
        departureBlock.setLogicalDirection(oldDirection.getDirection());
      }
    }
    //setRouteBean(route);
    //return route != null;
    this.routeBean = route;
    return route != null;
  }

  boolean reserveRoute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    //RouteBean route = getRouteBean();
    RouteBean route = this.routeBean;

    if (route == null) {
      return false;
    }
    Logger.debug("Reserving route " + route);
    route.setLocked(true);

    //Reserve the destination
    String destinationTileId = route.getToTileId();
    String arrivalSuffix = route.getToSuffix();
    String departureSuffix = route.getFromSuffix();

    Logger.debug("Destination: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block. Loco direction: " + locomotive.getDispatcherDirection());

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    departureBlock.setBlockState(BlockBean.BlockState.OCCUPIED);

    departureBlock.setDepartureSuffix(route.getFromSuffix());

    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setArrivalSuffix(arrivalSuffix);
    destinationBlock.setLogicalDirection(departureBlock.getLogicalDirection());

    // Set Turnouts in the right state
    List<RouteElementBean> turnouts = getTurnouts(route);
    Logger.trace("There are " + turnouts.size() + " turnouts in this route");

    //Now start to persist and perform critical thinks
    if (turnoutsNotLocked(route)) {
      PersistenceFactory.getService().persist(route);

      for (RouteElementBean reb : turnouts) {
        AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        switchAccessory(turnout, av);

        //TODO: configurable wait time between switches
        pause(500);
      }
      Logger.trace("Turnouts set for " + route);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      //Now that we have reserved the route lets determine which sensors are playing a role.
      //On the departure side we have the OccupiedSensor, i.e. the IN sensor when arriving.
      //The exit sensor i.e the last sensor to leave the departure block.
      Integer occupancySensorId; //, exitSensorId;
      if ("+".equals(departureSuffix)) {
        occupancySensorId = departureBlock.getMinSensorId();
        exitSensorId = departureBlock.getPlusSensorId();
      } else {
        occupancySensorId = departureBlock.getPlusSensorId();
        exitSensorId = departureBlock.getMinSensorId();
      }
      setOccupationSensorId(occupancySensorId);
      setExitSensorId(exitSensorId);

      //On the destination side we have the enterSensor end the IN sensor.
      //From which side on the block is the train expected to arrive?
      //Integer enterSensorId, inSensorId;
      if ("+".equals(arrivalSuffix)) {
        enterSensorId = destinationBlock.getPlusSensorId();
        inSensorId = destinationBlock.getMinSensorId();
      } else {
        enterSensorId = destinationBlock.getMinSensorId();
        inSensorId = destinationBlock.getPlusSensorId();
      }

      setEnterSensorId(enterSensorId);
      setInSensorId(inSensorId);

      Logger.trace("Departure: " + departureBlock.getId() + " Occupancy Sensor: " + occupancySensorId + " Exit Sensor: " + exitSensorId);
      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + enterSensorId + " In Sensor: " + inSensorId);

      dispatcher.showRoute(route, Color.green);
      Logger.trace(route + " Locked");

      if (swapLocomotiveDirection) {
        LocomotiveBean.Direction newDir = LocomotiveBean.Direction.get(departureBlock.getLogicalDirection());
        Logger.trace("Changing Direction to " + newDir);
        changeLocomotiveDirection(newDir);
      }

      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);

      return true;
    } else {
      //Can't lock route
      route.setLocked(false);
      PersistenceFactory.getService().persist(route);
      Logger.trace(route + " NOT Locked");

      return false;
    }
  }

  void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  void switchAccessory(AccessoryBean accessory, AccessoryBean.AccessoryValue value) {
    try {
      JCS.getJcsCommandStation().switchAccessory(accessory, value);
    } catch (Exception e) {
      Logger.error("Error switching accessory " + accessory.getId() + " to " + value + " Cause: " + e.getMessage());
    }
  }

  void changeLocomotiveDirection(LocomotiveBean.Direction newDirection) {
    LocomotiveBean locomotiveBean = dispatcher.getLocomotiveBean();
    try {
      JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, locomotiveBean);
    } catch (Exception e) {
      Logger.error("Error changing direction of locomotive " + locomotiveBean.getId() + " to " + newDirection + " Cause: " + e.getMessage());
    }
    locomotiveBean.setDirection(newDirection);
  }
  
  
  
//  boolean reserveNextRoute() {
//    LocomotiveBean locomotive = getLocomotiveBean();
//    RouteBean nextRoute = getNextRouteBean();
//
//    if (nextRoute == null) {
//      return false;
//    }
//    Logger.debug("Reserving next route " + nextRoute);
//    nextRoute.setLocked(true);
//
//    //Reserve the destination
//    String nextDestinationTileId = nextRoute.getToTileId();
//    String nextArrivalSuffix = nextRoute.getToSuffix();
//    Logger.debug("Next Destination: " + nextDestinationTileId + " Arrival on the " + nextArrivalSuffix + " side of the block. Loco direction: " + locomotive.getDispatcherDirection());
//
//    BlockBean departureBlock = getDestinationBlock();
//    BlockBean nextDestinationBlock = getNextDestinationBlock();
//
//    nextDestinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
//    nextDestinationBlock.setLocomotive(locomotive);
//    nextDestinationBlock.setArrivalSuffix(nextArrivalSuffix);
//    nextDestinationBlock.setLogicalDirection(departureBlock.getLogicalDirection());
//
//    // Set Turnouts in the right state
//    List<RouteElementBean> turnouts = getTurnouts(nextRoute);
//    Logger.trace("There are " + turnouts.size() + " turnouts in the next route");
//
//    //Now start to persist and perform critical thinks
//    if (turnoutsNotLocked(nextRoute)) {
//      PersistenceFactory.getService().persist(nextRoute);
//
//      for (RouteElementBean reb : turnouts) {
//        AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
//        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
//        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
//        switchAccessory(turnout, av);
//        //TODO configurable wait time between switches
//        pause(500);
//      }
//      Logger.trace("Turnouts set for " + nextRoute);
//
//      PersistenceFactory.getService().persist(nextDestinationBlock);
//
//      showRoute(nextRoute, Color.yellow);
//      Logger.trace(nextRoute + " Locked");
//
//      showBlockState(nextDestinationBlock);
//
//      return true;
//    } else {
//      //Can't lock nextRoute
//      nextRoute.setLocked(false);
//      PersistenceFactory.getService().persist(nextRoute);
//      Logger.trace(nextRoute + " NOT Locked");
//
//      return false;
//    }
//  }

  boolean isAllowed(boolean allowCommuter, boolean allowNonCommuter, boolean commuter) {
    //both flags are the same → all trains allowed
    if (allowCommuter == allowNonCommuter) {
      return true;
    }

    // only non-commuter allowed
    if (!allowCommuter && allowNonCommuter) {
      return !commuter;
    }

    // only commuter allowed
    if (allowCommuter && !allowNonCommuter) {
      return commuter;
    }

    // Should never happen, but default deny
    return false;
  }

  boolean turnoutsNotLocked(RouteBean route) {
    List<RouteElementBean> turnouts = getTurnouts(route);

    boolean switchesNotLocked = true;
    for (RouteElementBean reb : turnouts) {
      //AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
      AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
      //check if the accessory is not set by an other reserved nextRoute
      boolean locked = PersistenceFactory.getService().isAccessoryLocked(turnout.getId());
      if (locked) {
        Logger.debug("Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] is locked!");
        return false;
      }
    }
    Logger.trace("There are " + turnouts.size() + " free turnouts in this route");
    return switchesNotLocked;
  }

  List<RouteElementBean> getTurnouts(RouteBean routeBean) {
    List<RouteElementBean> rel = routeBean.getRouteElements();
    List<RouteElementBean> turnouts = new ArrayList<>();
    for (RouteElementBean reb : rel) {
      if (reb.isTurnout()) {
        turnouts.add(reb);
      }
    }
    return turnouts;
  }

}
