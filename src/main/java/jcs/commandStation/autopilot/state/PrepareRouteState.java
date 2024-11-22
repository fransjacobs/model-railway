/*
 * Copyright 2024 Frans Jacobs.
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
import java.util.List;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Block;
import org.tinylog.Logger;

/**
 * Lock the route, set the block states and turnout directions
 */
class PrepareRouteState extends DispatcherState {

  private boolean swapLocomotiveDirection = false;
  private boolean canAdvanceToNextState = false;

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    int permits = AutoPilot.avialablePermits();
    Logger.trace("Obtaining a lock. There is currently " + permits + " available permits...");

    if (AutoPilot.tryAquireLock()) {
      try {
        Logger.trace("##### Locked ####");
        if (searchRoute(dispatcher)) {
          canAdvanceToNextState = reserveRoute(dispatcher);
        }
      } finally {
        //Make sure the lock is released
        AutoPilot.releaseLock();
        Logger.trace("##### Released ####");
      }
    } else {
      Logger.trace("No Semaphore avalaible");
      canAdvanceToNextState = false;
    }

    if (canAdvanceToNextState) {
      DispatcherState newState = new StartState();
      return newState;
    } else {
      //Go back to waiting and try again
      DispatcherState newWaitState = new WaitState();
      return newWaitState;
    }
  }

  boolean searchRoute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    Logger.trace("Search a free route for " + locomotive.getName() + "...");

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    if(departureBlock.getLogicalDirection() == null) {
      departureBlock.setLogicalDirection(locomotive.getDirection().getDirection());
    }
    Direction logicalDirection = Direction.get(departureBlock.getLogicalDirection());

    TileBean tileBean = PersistenceFactory.getService().getTileBean(departureBlock.getTileId());
    Orientation blockOrientation = tileBean.getOrientation();
    boolean reverseArrival = departureBlock.isReverseArrival();

    String departureSuffix = Block.getDepartureSuffix(blockOrientation, reverseArrival, logicalDirection);

    Logger.trace("Loco " + locomotive.getName() + " is in block " + departureBlock.getId() + ". Direction " + logicalDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    //Search for the possible routes
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
    Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");

    List<RouteBean> checkedRoutes = new ArrayList<>();

    //No routes found or possible. When the Locomotive is a commuter train it can reverse direction.
    //Lets try that...
    if (routes.isEmpty() && locomotive.isCommuter()) {
      Direction oldDirection = logicalDirection;
      //Direction newDirection = locomotive.toggleDispatcherDirection();
      Direction newDirection = LocomotiveBean.toggle(oldDirection);
      Logger.trace("Reversing Locomotive, from " + oldDirection + " to " + newDirection + "...");

      this.swapLocomotiveDirection = true;
      //Do NOT persist the direction yet, just test....
      //locomotive.setDispatcherDirection(newDirection);
      departureBlock.setLogicalDirection(newDirection.getDirection());
      //blockBean.setLocomotive(locomotive);

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

    //Found possible routes check on the destination for the sensors
    for (RouteBean route : routes) {
      String DestinationTileId = route.getToTileId();
      BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(DestinationTileId);
      //Check the sensors 
      boolean plusInActive = !destinationBlock.getPlusSensorBean().isActive();
      boolean minInActive = !destinationBlock.getMinSensorBean().isActive();

      Logger.trace("Destination " + destinationBlock.getId() + " + sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));

      if (plusInActive && minInActive && turnoutsNotLocked(route)) {
        checkedRoutes.add(route);
      }
    }

    //Randomly pick a route in case multiple routes are found...
    int rIdx = 0;
    if (checkedRoutes.size() > 1) {
      //Choose randomly the route
      for (int i = 0; i < 10; i++) {
        //Seed a bit....
        dispatcher.getRandomNumber(0, checkedRoutes.size());
      }
      rIdx = dispatcher.getRandomNumber(0, checkedRoutes.size());
    }

    RouteBean route = null;
    if (!checkedRoutes.isEmpty()) {
      route = checkedRoutes.get(rIdx);
      Logger.trace("Choosen route " + route.toLogString());
      //persist the departure block
      PersistenceFactory.getService().persist(departureBlock);     
    } else {
      Logger.debug("No route available for " + locomotive.getName() + " ...");
      if (swapLocomotiveDirection) {
        //Reverse the direction change
        Direction oldDirection = LocomotiveBean.toggle(Direction.get(departureBlock.getLogicalDirection()));
        Logger.trace("Rollback Locomotive reverse to " + oldDirection + "...");
        departureBlock.setLogicalDirection(oldDirection.getDirection());
        //Direction oldDirection = locomotive.toggleDispatcherDirection();
        //locomotive.setDispatcherDirection(oldDirection);
      }
    }
    dispatcher.setRouteBean(route);
    return route != null;
  }

  boolean reserveRoute(Dispatcher dispatcher) {
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
    destinationBlock.setReverseArrival(departureBlock.isReverseArrival());
    destinationBlock.setLogicalDirection(departureBlock.getLogicalDirection());

    // Set Turnouts in the right state
    List<RouteElementBean> turnouts = getTurnouts(route);
    Logger.trace("There are " + turnouts.size() + " turnouts in this route");

    //Now start to persist and perform critical thinks
    if (turnoutsNotLocked(route)) {
      PersistenceFactory.getService().persist(route);

      for (RouteElementBean reb : turnouts) {
        AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        dispatcher.switchAccessory(turnout, av);
        //TODO configurable wait time between switches
        pause(500);
      }
      Logger.trace("Turnouts set for " + route);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      //Now that we have reserved the route lets determine which sensors
      //Are playing a role.
      //On the departure side we have the OccupiedSensor, ie the IN sensor when arriving.
      //The exit sensor i.e the last sensor to leave the departure block.
      String occupancySensorId, exitSensorId;
      if ("+".equals(departureSuffix)) {
        occupancySensorId = departureBlock.getMinSensorId();
        exitSensorId = departureBlock.getPlusSensorId();
      } else {
        occupancySensorId = departureBlock.getPlusSensorId();
        exitSensorId = departureBlock.getMinSensorId();
      }
      dispatcher.setOccupationSensorId(occupancySensorId);
      dispatcher.setExitSensorId(exitSensorId);

      //On the destination side we have the enterSensor end the IN sensor.
      //From which side on the block is the train expected to arrive?
      String enterSensorId, inSensorId;
      if ("+".equals(arrivalSuffix)) {
        enterSensorId = destinationBlock.getPlusSensorId();
        inSensorId = destinationBlock.getMinSensorId();
      } else {
        enterSensorId = destinationBlock.getMinSensorId();
        inSensorId = destinationBlock.getPlusSensorId();
      }

      dispatcher.setEnterSensorId(enterSensorId);
      dispatcher.setInSensorId(inSensorId);

      Logger.trace("Departure: " + departureBlock.getId() + " Occupancy Sensor: " + occupancySensorId + " Exit Sensor: " + exitSensorId);
      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + enterSensorId + " In Sensor: " + inSensorId);

      dispatcher.showRoute(route, Color.green);
      Logger.trace(route + " Locked");

      if (swapLocomotiveDirection) {
        //Direction newDir = locomotive.getDispatcherDirection();
        Direction newDir = Direction.get(departureBlock.getLogicalDirection());      
        Logger.trace("Changing Direction to " + newDir);
        dispatcher.changeLocomotiveDirection(locomotive, newDir);
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

  boolean turnoutsNotLocked(RouteBean route) {
    List<RouteElementBean> turnouts = getTurnouts(route);

    boolean switchesNotLocked = true;
    for (RouteElementBean reb : turnouts) {
      AccessoryValue av = reb.getAccessoryValue();
      AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
      //check if the accessory is not set by an other reserved route
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

}
