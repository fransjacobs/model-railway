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
package jcs.commandStation.automation.state;

import jcs.commandStation.automation.RailwayController;
import org.tinylog.Logger;

/**
 * Lock the route, set the block states and turnout directions
 */
public class PrepareRouteState extends AbstractState {

  public PrepareRouteState() {
    super("PrepareRoute");
  }

  //private boolean swapLocomotiveDirection = false;
  private boolean canAdvanceToNextState = false;

  //private LocomotiveBean locomotive;
  //private BlockBean block;
  @Override
  AbstractState execute() {
    int permits = RailwayController.avialablePermits();
    Logger.trace("Obtaining a lock. There is currently " + permits + " available permits...");

    if (RailwayController.tryAquireLock()) {
      try {
        Logger.trace("##### Locked ####");
        if (this.dispatcher.searchRoute(true)) {
          canAdvanceToNextState = this.dispatcher.reserveRoute();
        }
      } finally {
        //Make sure the lock is released
        RailwayController.releaseLock();
        Logger.trace("##### Released ####");
      }
    } else {
      Logger.trace("No Semaphore available");
      canAdvanceToNextState = false;
    }

    if (canAdvanceToNextState) {
      return new StartingState();
    } else {
      //Go back to waiting and try again
      return new WaitingState();
    }
  }

//  boolean searchRoute() {
//    //LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
//    Logger.trace("Search a free route for " + locomotive.getName() + "...");
//
//    BlockBean departureBlock = dispatcher.getDepartureBlock();
//    //Is the departure block part of a station.
//    StationBean station = dispatcher.getStation(departureBlock);
//    int locCount = 1;
//    int minLocCount = 1;
//    if (station != null) {
//      minLocCount = station.getMinLocomotives();
//      locCount = PersistenceFactory.getService().getLocomotiveCount(station).intValue();
//      if (station.getLocomotiveCount() != locCount) {
//        Logger.warn("Adjusting number of locomotives in station " + station.getName() + " to " + locCount);
//        station.setLocomotiveCount(locCount);
//        PersistenceFactory.getService().persist(station);
//      }
//      Logger.trace(departureBlock.getId() + " is member of Station " + station.getName() + " min Locs: " + minLocCount + " cur locs: " + locCount);
//    }
//
//    if (departureBlock.getLogicalDirection() == null) {
//      departureBlock.setLogicalDirection(locomotive.getDirection().getDirection());
//    }
//    Direction logicalDirection = Direction.get(departureBlock.getLogicalDirection());
//
//    TileBean tileBean = PersistenceFactory.getService().getTileBean(departureBlock.getTileId());
//    Orientation blockOrientation = tileBean.getOrientation();
//
//    String departureSuffix = departureBlock.getDepartureSuffix();
//    if (departureSuffix == null) {
//      departureSuffix = Block.getDepartureSuffix(blockOrientation, logicalDirection);
//    }
//
//    Logger.trace("Loco " + locomotive.getName() + " is in block " + departureBlock.getId() + ". Direction " + logicalDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");
//
//    //only search for a route if we can depart
//    List<RouteBean> routes = Collections.emptyList();
//    if (locCount >= minLocCount) {
//      //Search for the possible routes
//      routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
//      Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");
//    } else {
//      String stationName;
//      if (station != null) {
//        stationName = station.getName();
//      } else {
//        stationName = "?";
//      }
//      Logger.trace("nr of locs (" + locCount + ") is lower than min locs (" + minLocCount + ") for station " + stationName);
//    }
//
//    List<RouteBean> checkedRoutes = new ArrayList<>();
//    boolean commuter = locomotive.isCommuter();
//
//    //No routes found or possible.
//    //When the Locomotive is a commuter train and the departure block allows a direction change it can reverse direction. Lets try that...
//    if (routes.isEmpty() && commuter && departureBlock.isAllowDirectionChange() && locCount >= minLocCount) {
//      Direction oldDirection = logicalDirection;
//      //Direction newDirection = locomotive.toggleDispatcherDirection();
//      Direction newDirection = LocomotiveBean.toggle(oldDirection);
//      Logger.trace("Reversing Locomotive, from " + oldDirection + " to " + newDirection + "...");
//
//      swapLocomotiveDirection = true;
//      //Do NOT persist the direction yet, just test....
//      departureBlock.setLogicalDirection(newDirection.getDirection());
//      //Now flip the departure direction
//      if ("-".equals(departureSuffix)) {
//        departureSuffix = "+";
//      } else {
//        departureSuffix = "-";
//      }
//
//      Logger.trace("2nd attempt for Loco " + locomotive.getName() + " is in block " + departureBlock.getId() + ". Direction " + newDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");
//      routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
//
//      Logger.trace("After the 2nd attempt, there " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s). " + (!routes.isEmpty() ? "Direction of " + locomotive.getName() + " must be swapped!" : ""));
//    }
//
//    //Found possible routes check on the destination for the sensors and permissions
//    for (RouteBean route : routes) {
//      String destinationTileId = route.getToTileId();
//      BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(destinationTileId);
//      //Check the sensors 
//      boolean plusInActive = !destinationBlock.getPlusSensorBean().isActive();
//      boolean minInActive = !destinationBlock.getMinSensorBean().isActive();
//
//      boolean allowCommuter = destinationBlock.isAllowCommuterOnly();
//      boolean allowNonCommuter = destinationBlock.isAllowNonCommuterOnly();
//
//      boolean allowed = isAllowed(allowCommuter, allowNonCommuter, commuter);
//
//      Logger.trace("Destination " + destinationBlock.getId() + " Train type commuter: " + commuter + " Permission " + allowed + " sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));
//
//      if (plusInActive && minInActive && allowed && turnoutsNotLocked(route)) {
//        checkedRoutes.add(route);
//      }
//    }
//
//    //Randomly pick a route in case multiple routes are found...
//    int rIdx = 0;
//    if (checkedRoutes.size() > 1) {
//      //Choose randomly the route
//      for (int i = 0; i < 10; i++) {
//        //Seed a bit....
//        dispatcher.getRandomNumber(0, checkedRoutes.size());
//      }
//      rIdx = dispatcher.getRandomNumber(0, checkedRoutes.size());
//    }
//
//    RouteBean route = null;
//    if (!checkedRoutes.isEmpty()) {
//      route = checkedRoutes.get(rIdx);
//      Logger.trace("Choosen route " + route.toLogString());
//      //persist the departure block
//      PersistenceFactory.getService().persist(departureBlock);
//    } else {
//      if (locCount >= minLocCount) {
//        Logger.debug("No route available for " + locomotive.getName() + " ...");
//      } else {
//        Logger.debug("No route available because the Station occupation (" + locCount + ") is less then the min occupation (" + minLocCount + ")");
//      }
//      if (swapLocomotiveDirection) {
//        //Reverse the direction change
//        Direction oldDirection = LocomotiveBean.toggle(Direction.get(departureBlock.getLogicalDirection()));
//        Logger.trace("Rollback Locomotive reverse to " + oldDirection + "...");
//        departureBlock.setLogicalDirection(oldDirection.getDirection());
//      }
//    }
//    dispatcher.setRouteBean(route);
//    return route != null;
//  }
//  boolean reserveRoute() {
//    //LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
//    RouteBean route = dispatcher.getRouteBean();
//
//    if (route == null) {
//      return false;
//    }
//    Logger.debug("Reserving route " + route);
//    route.setLocked(true);
//
//    //Reserve the destination
//    String destinationTileId = route.getToTileId();
//    String arrivalSuffix = route.getToSuffix();
//    String departureSuffix = route.getFromSuffix();
//
//    Logger.debug("Destination: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block. Loco direction: " + locomotive.getDispatcherDirection());
//
//    BlockBean departureBlock = dispatcher.getDepartureBlock();
//    departureBlock.setBlockState(BlockBean.BlockState.OCCUPIED);
//
//    departureBlock.setDepartureSuffix(route.getFromSuffix());
//
//    BlockBean destinationBlock = dispatcher.getDestinationBlock();
//    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
//    destinationBlock.setLocomotive(locomotive);
//    destinationBlock.setArrivalSuffix(arrivalSuffix);
//    destinationBlock.setLogicalDirection(departureBlock.getLogicalDirection());
//
//    // Set Turnouts in the right state
//    List<RouteElementBean> turnouts = getTurnouts(route);
//    Logger.trace("There are " + turnouts.size() + " turnouts in this route");
//
//    //Now start to persist and perform critical thinks
//    if (turnoutsNotLocked(route)) {
//      PersistenceFactory.getService().persist(route);
//
//      for (RouteElementBean reb : turnouts) {
//        AccessoryValue av = reb.getAccessoryValue();
//        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
//        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
//        dispatcher.switchAccessory(turnout, av);
//        //TODO configurable wait time between switches
//        pause(500);
//      }
//      Logger.trace("Turnouts set for " + route);
//
//      PersistenceFactory.getService().persist(departureBlock);
//      PersistenceFactory.getService().persist(destinationBlock);
//
//      //Now that we have reserved the route lets determine which sensors are playing a role.
//      //On the departure side we have the OccupiedSensor, i.e. the IN sensor when arriving.
//      //The exit sensor i.e the last sensor to leave the departure block.
//      Integer occupancySensorId, exitSensorId;
//      if ("+".equals(departureSuffix)) {
//        occupancySensorId = departureBlock.getMinSensorId();
//        exitSensorId = departureBlock.getPlusSensorId();
//      } else {
//        occupancySensorId = departureBlock.getPlusSensorId();
//        exitSensorId = departureBlock.getMinSensorId();
//      }
//      dispatcher.setOccupationSensorId(occupancySensorId);
//      dispatcher.setExitSensorId(exitSensorId);
//
//      //On the destination side we have the enterSensor end the IN sensor.
//      //From which side on the block is the train expected to arrive?
//      Integer enterSensorId, inSensorId;
//      if ("+".equals(arrivalSuffix)) {
//        enterSensorId = destinationBlock.getPlusSensorId();
//        inSensorId = destinationBlock.getMinSensorId();
//      } else {
//        enterSensorId = destinationBlock.getMinSensorId();
//        inSensorId = destinationBlock.getPlusSensorId();
//      }
//
//      dispatcher.setEnterSensorId(enterSensorId);
//      dispatcher.setInSensorId(inSensorId);
//
//      Logger.trace("Departure: " + departureBlock.getId() + " Occupancy Sensor: " + occupancySensorId + " Exit Sensor: " + exitSensorId);
//      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + enterSensorId + " In Sensor: " + inSensorId);
//
//      dispatcher.showRoute(route, Color.green);
//      Logger.trace(route + " Locked");
//
//      if (swapLocomotiveDirection) {
//        Direction newDir = Direction.get(departureBlock.getLogicalDirection());
//        Logger.trace("Changing Direction to " + newDir);
//        dispatcher.changeLocomotiveDirection(locomotive, newDir);
//      }
//
//      dispatcher.showBlockState(departureBlock);
//      dispatcher.showBlockState(destinationBlock);
//
//      return true;
//    } else {
//      //Can't lock route
//      route.setLocked(false);
//      PersistenceFactory.getService().persist(route);
//      Logger.trace(route + " NOT Locked");
//
//      return false;
//    }
//  }
}
