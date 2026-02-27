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
package jcs.commandStation.automation;

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
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * RouteManager is in charge of finding a route.
 */
class RouteManager {

  private final Dispatcher dispatcher;
  private boolean swapLocomotiveDirection;

  RouteManager(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  boolean searchAndReserveRoute() {
    if (searchRoute()) {
      return reserveRoute();
    } else {
      return false;
    }
  }

  boolean searchRoute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    Logger.trace("Search a free route for " + locomotive.getName() + "...");

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    //Is the departure block part of a station?
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
    dispatcher.setRouteBean(route);
    return route != null;
  }

  boolean reserveRoute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    RouteBean route = dispatcher.getRouteBean();

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
      if ("+".equals(departureSuffix)) {
        dispatcher.setOccupationSensorId(departureBlock.getMinSensorId());
        dispatcher.setExitSensorId(departureBlock.getPlusSensorId());
      } else {
        dispatcher.setOccupationSensorId(departureBlock.getPlusSensorId());
        dispatcher.setExitSensorId(departureBlock.getMinSensorId());
      }

      //On the destination side we have the enterSensor end the IN sensor.
      //From which side on the block is the train expected to arrive?
      //Integer enterSensorId, inSensorId;
      if ("+".equals(arrivalSuffix)) {
        dispatcher.setEnterSensorId(destinationBlock.getPlusSensorId());
        dispatcher.setInSensorId(destinationBlock.getMinSensorId());
      } else {
        dispatcher.setEnterSensorId(destinationBlock.getMinSensorId());
        dispatcher.setInSensorId(destinationBlock.getMinSensorId());
      }

      Logger.trace("Departure: " + departureBlock.getId() + " Occupancy Sensor: " + dispatcher.getOccupationSensorId() + " Exit Sensor: " + dispatcher.getExitSensorId());
      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + dispatcher.getEnterSensorId() + " In Sensor: " + dispatcher.getInSensorId());

      showRoute(route, Color.green);
      Logger.trace(route + " Locked");

      if (swapLocomotiveDirection) {
        LocomotiveBean.Direction newDir = LocomotiveBean.Direction.get(departureBlock.getLogicalDirection());
        Logger.trace("Changing Direction to " + newDir);
        dispatcher.changeLocomotiveDirection(newDir);
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

  boolean searchAndReserveNextRoute() {
    if (searchNextRoute()) {
      return reserveNextRoute();
    } else {
      return false;
    }
  }

  boolean searchNextRoute() {
    Logger.trace("Search a free next route for " + dispatcher.getLocomotiveBean().getName() + "...");

    // In this state the we are checking whether there is a valid nextRoute from the destination to the next block.
    BlockBean departureBlock = dispatcher.getDestinationBlock();
    //Is the departure block part of a station.
    StationBean station = dispatcher.getStation(departureBlock);
    int locCount = 1;
    int minLocCount = 1;
    if (station != null) {
      minLocCount = station.getMinLocomotives();
      //Todo use the persistence service?
      locCount = PersistenceFactory.getService().getLocomotiveCount(station).intValue();
      //locCount = station.getLocomotiveCount();
      Logger.trace(departureBlock.getId() + " is member of Station " + station.getName() + " min Locs: " + minLocCount + " cur locs: " + locCount);
    }

    //Use the current running locomotive direction
    LocomotiveBean.Direction logicalDirection = dispatcher.getLocomotiveBean().getDirection();

    TileBean tileBean = PersistenceFactory.getService().getTileBean(departureBlock.getTileId());
    TileBean.Orientation blockOrientation = tileBean.getOrientation();

    String departureSuffix = departureBlock.getDepartureSuffix();
    if (departureSuffix == null) {
      departureSuffix = Block.getDepartureSuffix(blockOrientation, logicalDirection);
    }

    Logger.trace("Loco " + dispatcher.getLocomotiveBean().getName() + " is entering block " + departureBlock.getId() + ". Direction " + logicalDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    //Search for the possible routes
    List<RouteBean> routes = Collections.emptyList();
    //Is the departure block part of a station.
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
    //Found possible routes check on the destination for the sensors and permissions
    for (RouteBean route : routes) {
      String nextDestinationTileId = route.getToTileId();
      BlockBean nextDestinationBlock = PersistenceFactory.getService().getBlockByTileId(nextDestinationTileId);
      //Check the sensors 
      boolean plusInActive = !nextDestinationBlock.getPlusSensorBean().isActive();
      boolean minInActive = !nextDestinationBlock.getMinSensorBean().isActive();

      boolean allowCommuter = nextDestinationBlock.isAllowCommuterOnly();
      boolean allowNonCommuter = nextDestinationBlock.isAllowNonCommuterOnly();

      boolean allowed = isAllowed(allowCommuter, allowNonCommuter, dispatcher.getLocomotiveBean().isCommuter());

      Logger.trace("Next Destination " + nextDestinationBlock.getId() + " Train type commuter: " + dispatcher.getLocomotiveBean().isCommuter() + " Permission " + allowed + " sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));

      if (plusInActive && minInActive && allowed && turnoutsNotLocked(route)) {
        checkedRoutes.add(route);
      }
    }

    //Randomly pick a nextRoute in case multiple routes are found...
    int rIdx = 0;
    if (checkedRoutes.size() > 1) {
      //Choose randomly the nextRoute
      Random random = new Random();
      for (int i = 0; i < 10; i++) {
        //Seed a bit....
        random.ints(0, checkedRoutes.size()).findFirst();
      }
      rIdx = random.ints(0, checkedRoutes.size()).findFirst().getAsInt();
    }

    RouteBean nextRoute = null;
    if (!checkedRoutes.isEmpty()) {
      nextRoute = checkedRoutes.get(rIdx);
      Logger.trace("Choosen route " + nextRoute.toLogString());
    } else {
      if (locCount >= minLocCount) {
        Logger.debug("No route available for " + dispatcher.getLocomotiveBean().getName() + " ...");
      } else {
        Logger.debug("No route available because the Station occupation (" + locCount + ") is less then the min occupation (" + minLocCount + ")");
      }
    }
    dispatcher.setNextRouteBean(nextRoute);
    return nextRoute != null;
  }

  boolean reserveNextRoute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    RouteBean nextRoute = dispatcher.getNextRouteBean();

    if (nextRoute == null) {
      return false;
    }
    Logger.debug("Reserving next route " + nextRoute);
    nextRoute.setLocked(true);

    //Reserve the destination
    String nextDestinationTileId = nextRoute.getToTileId();
    String nextArrivalSuffix = nextRoute.getToSuffix();
    Logger.debug("Next Destination: " + nextDestinationTileId + " Arrival on the " + nextArrivalSuffix + " side of the block. Loco direction: " + locomotive.getDispatcherDirection());

    BlockBean departureBlock = dispatcher.getDestinationBlock();
    BlockBean nextDestinationBlock = dispatcher.getNextDestinationBlock();

    nextDestinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    nextDestinationBlock.setLocomotive(locomotive);
    nextDestinationBlock.setArrivalSuffix(nextArrivalSuffix);
    nextDestinationBlock.setLogicalDirection(departureBlock.getLogicalDirection());

    // Set Turnouts in the right state
    List<RouteElementBean> turnouts = getTurnouts(nextRoute);
    Logger.trace("There are " + turnouts.size() + " turnouts in the next route");

    //Now start to persist and perform critical thinks
    if (turnoutsNotLocked(nextRoute)) {
      PersistenceFactory.getService().persist(nextRoute);

      for (RouteElementBean reb : turnouts) {
        AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        switchAccessory(turnout, av);
        //TODO configurable wait time between switches
        pause(500);
      }
      Logger.trace("Turnouts set for " + nextRoute);

      PersistenceFactory.getService().persist(nextDestinationBlock);

      showRoute(nextRoute, Color.yellow);
      Logger.trace(nextRoute + " Locked");

      dispatcher.showBlockState(nextDestinationBlock);

      return true;
    } else {
      //Can't lock nextRoute
      nextRoute.setLocked(false);
      PersistenceFactory.getService().persist(nextRoute);
      Logger.trace(nextRoute + " NOT Locked");

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

//  void changeLocomotiveDirection(LocomotiveBean.Direction newDirection) {
//    try {
//      JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, dispatcher.getLocomotiveBean());
//    } catch (Exception e) {
//      Logger.error("Error changing direction of locomotive " + dispatcher.getLocomotiveBean().getId() + " to " + newDirection + " Cause: " + e.getMessage());
//    }
//    dispatcher.getLocomotiveBean().setDirection(newDirection);
//  }
  private boolean isAllowed(boolean allowCommuter, boolean allowNonCommuter, boolean commuter) {
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

  private boolean turnoutsNotLocked(RouteBean route) {
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

  private List<RouteElementBean> getTurnouts(RouteBean routeBean) {
    List<RouteElementBean> rel = routeBean.getRouteElements();
    List<RouteElementBean> turnouts = new ArrayList<>();
    for (RouteElementBean reb : rel) {
      if (reb.isTurnout()) {
        turnouts.add(reb);
      }
    }
    return turnouts;
  }

  void showRoute(RouteBean routeBean, Color routeColor) {
    Logger.trace("Show route " + routeBean.toLogString());
    List<RouteElementBean> routeElements = routeBean.getRouteElements();

    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      Tile tile = TileCache.findTile(tileId);
      if (tile != null) {
        TileBean.Orientation incomingSide = re.getIncomingOrientation();

        tile.setIncomingSide(incomingSide);
        tile.setTrackRouteColor(Tile.DEFAULT_ROUTE_TRACK_COLOR);

        if (re.isTurnout()) {
          AccessoryBean.AccessoryValue routeState = re.getAccessoryValue();
          tile.setRouteValue(routeState);
        } else if (re.isBlock()) {
          if (re.getTileId().equals(routeBean.getFromTileId())) {
            //departure block
            tile.setBlockState(BlockBean.BlockState.OUTBOUND);
          } else {
            tile.setBlockState(BlockBean.BlockState.INBOUND);
          }
        }
        tile.setShowRoute(true);
      } else {
        Logger.warn("Tile with id " + tileId + " NOT in TileCache!");
      }
    }
  }

}
