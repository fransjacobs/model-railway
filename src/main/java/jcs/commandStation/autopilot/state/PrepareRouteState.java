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
import java.util.Random;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Lock the route, set the block states and turnout directions
 */
class PrepareRouteState extends DispatcherState {

  private boolean swapLocomotiveDirection = false;
  private boolean canAdvanceToNextState = false;

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    int permits = AutoPilot.getInstance().avialablePermits();
    Logger.trace("Obtaining a lock. There is currently " + permits + " available permits...");

    if (AutoPilot.getInstance().tryAquireLock()) {
      try {
        Logger.trace("##### Locked ####");
        if (searchRoute(dispatcher)) {
          canAdvanceToNextState = reserveRoute(dispatcher);
        }
      } finally {
        //Make sure the lock is released
        AutoPilot.getInstance().releaseLock();
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

    BlockBean blockBean = dispatcher.getDepartureBlock();

    String departureSuffix = blockBean.getDepartureSuffix();
    LocomotiveBean.Direction locDir = locomotive.getDirection();

    Logger.trace("Loco " + locomotive.getName() + " is in block " + blockBean.getId() + ". Direction " + locDir.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    //Search for the possible routes
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(blockBean.getId(), departureSuffix);
    Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");

    List<RouteBean> checkedRoutes = new ArrayList<>();

    if (routes.isEmpty() && locomotive.isCommuter()) {
      //No routes possible. When the Locomotive is a commuter train it can reverse direction, so
      Direction oldDirection = locomotive.getDirection();
      Direction newDirection = locomotive.toggleDirection();
      Logger.trace("Reversing Locomotive, from " + oldDirection + " to " + newDirection + "...");

      this.swapLocomotiveDirection = true;

      //Do NOT yet set the direction yet just test....
      locomotive.setDirection(newDirection);
      blockBean.setLocomotive(locomotive);

      //also flip the departure direction
      if ("-".equals(departureSuffix)) {
        departureSuffix = "+";
      } else {
        departureSuffix = "-";
      }

      Logger.trace("2nd; Loco " + locomotive.getName() + " is in block " + blockBean.getId() + ". Direction " + locDir.getDirection() + ". DepartureSuffix " + departureSuffix + "...");
      routes = PersistenceFactory.getService().getRoutes(blockBean.getId(), departureSuffix);

      Logger.trace("2nd attempt, there " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s). " + (!routes.isEmpty() ? "Direction of " + locomotive.getName() + " must be swapped!" : ""));
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

    int rIdx = 0;
    if (checkedRoutes.size() > 1) {
      //Choose randomly the route
      for (int i = 0; i < 10; i++) {
        //Seed a bit....
        getRandomNumber(0, checkedRoutes.size());
      }
      rIdx = getRandomNumber(0, checkedRoutes.size());
    }

    RouteBean route = null;
    if (!checkedRoutes.isEmpty()) {
      route = checkedRoutes.get(rIdx);
      Logger.trace("Choosen route " + route.toLogString());
    } else {
      Logger.debug("No route available for " + locomotive.getName() + " ...");

      if (swapLocomotiveDirection) {
        Direction newDirection = locomotive.toggleDirection();
        Logger.trace("Rollback Locomotive reverse to " + newDirection + "...");
        locomotive.setDirection(newDirection);
      }

    }
    dispatcher.setRouteBean(route);
    return !checkedRoutes.isEmpty();
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

    Logger.debug("Destination: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block. Loco direction: " + locomotive.getDirection());

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    departureBlock.setBlockState(BlockBean.BlockState.OCCUPIED);

    departureBlock.setDepartureSuffix(route.getFromSuffix());

    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setArrivalSuffix(arrivalSuffix);
    destinationBlock.setReverseArrival(departureBlock.isReverseArrival());

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
      }
      Logger.trace("Turnouts set for " + route);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);

      dispatcher.showRoute(route, Color.green);
      Logger.trace(route + " Locked");

      if (swapLocomotiveDirection) {
        Direction newDir = locomotive.getDirection();
        Logger.trace("Changing Direction to " + newDir);
        dispatcher.changeLocomotiveDirection(locomotive, newDir);
      }

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

  public int getRandomNumber(int min, int max) {
    Random random = new Random();
    return random.ints(min, max).findFirst().getAsInt();
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
