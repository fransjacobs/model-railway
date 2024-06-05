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
class ReserveRouteState extends DispatcherState {

  private boolean swapLocomotiveDirection = false;

  ReserveRouteState(LocomotiveDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  DispatcherState next(LocomotiveDispatcher dispatcher) {
    if (canAdvanceToNextState) {
      DispatcherState newState = new StartState(dispatcher);
      return newState;
    } else {
      //Go back to waiting and try again
      DispatcherState newWaitState = new WaitState(dispatcher);
      return newWaitState;
    }
  }

  @Override
  public void execute() {
    ReserveRouteSemaphore semaphore = new ReserveRouteSemaphore();
    Logger.trace("Obtaining a lock. There is currently " + semaphore.avialablePermits() + " available lock...");

    try {
      //Blocking call....
      semaphore.tryLock();
      if (searchRoute()) {
        canAdvanceToNextState = reserveRoute();
      }
    } finally {
      //Make sure the lock is released
      semaphore.release();
    }
  }

  private String getDefaultDepartureSuffix(LocomotiveBean locomotive, BlockBean blockBean) {
    String departureSuffix;
    String arrivalSuffix;
    if ((Direction.FORWARDS == locomotive.getDirection() && !blockBean.isReverseArrival())
            || (Direction.BACKWARDS == locomotive.getDirection() && blockBean.isReverseArrival())) {
      departureSuffix = "+";
      arrivalSuffix = "-";
    } else {
      departureSuffix = "-";
      arrivalSuffix = "+";
    }

    blockBean.setArrivalSuffix(arrivalSuffix);
    return departureSuffix;
  }

  boolean searchRoute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    Logger.trace("Search a free route for " + locomotive.getName() + "...");

    BlockBean blockBean = this.dispatcher.getDepartureBlock();
    String departureSuffix = blockBean.getDepartureSuffix();
    if (departureSuffix == null || "".equals(departureSuffix)) {
      departureSuffix = getDefaultDepartureSuffix(locomotive, blockBean);
      Logger.trace("Using default departure suffix: " + departureSuffix);
    } else {
      Logger.trace("Using departure suffix: " + departureSuffix);
    }

    LocomotiveBean.Direction locDir = locomotive.getDirection();

    Logger.trace("Loco " + locomotive.getName() + " is in block " + blockBean.getId() + ". Direction " + locDir.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    //Search for the possible routes
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(blockBean.getId(), departureSuffix);
    Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");

    List<RouteBean> checkedRoutes = new ArrayList<>();

    if (routes.isEmpty() && locomotive.isCommuter()) {
      //No routes possible. When the Locomotive is a commuter train it can reverse direction, so
      Logger.debug("Reversing Locomotive...");
      //blockBean.setReverseArrival(!blockBean.isReverseArrival());

      Direction newDirection = locomotive.toggleDirection();
      //Do NOT set the final direction yet just test....
      locomotive.setDirection(newDirection);
      blockBean.setLocomotive(locomotive);

      locDir = locomotive.getDirection();
      departureSuffix = getDefaultDepartureSuffix(locomotive, blockBean);

      Logger.trace("2nd; Loco " + locomotive.getName() + " is in block " + blockBean.getId() + ". Direction " + locDir.getDirection() + ". DepartureSuffix " + departureSuffix + "...");
      routes = PersistenceFactory.getService().getRoutes(blockBean.getId(), departureSuffix);

      Logger.trace("2nd attempt, there " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s). " + (!routes.isEmpty() ? "Direction of " + locomotive.getName() + " must be swapped!" : ""));
      if (!routes.isEmpty()) {
        Logger.trace("Locomotive Direction Swap is needed!");
        swapLocomotiveDirection = true;
      }
    }

    //Found possible routes check on the destination for the sensors
    for (RouteBean route : routes) {
      String DestinationTileId = route.getToTileId();
      BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(DestinationTileId);
      //Check the sensors 
      boolean plusInActive = !destinationBlock.getPlusSensorBean().isActive();
      boolean minInActive = !destinationBlock.getMinSensorBean().isActive();

      Logger.trace("Destination " + destinationBlock.getId() + " + sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));

      if (plusInActive && minInActive) {
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
    }
    dispatcher.setRouteBean(route);
    return !checkedRoutes.isEmpty();
  }

  boolean reserveRoute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    RouteBean route = dispatcher.getRouteBean();

    if (route == null) {
      return false;
    }
    Logger.debug("Reserving route " + route);
    route.setLocked(true);

//    if (swapLocomotiveDirection) {
//      Direction newDirection = locomotive.getDirection();
//      dispatcher.changeLocomotiveDirection(locomotive, newDirection);
//    }
    //Reserve the destination
    String destinationTileId = route.getToTileId();
    String arrivalSuffix = route.getToSuffix();
    Logger.debug("Destination: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block. Loco direction: " + locomotive.getDirection());

    BlockBean departureBlock = this.dispatcher.getDepartureBlock();
    departureBlock.setBlockState(BlockBean.BlockState.DEPARTING);

    String departureArrivalSuffix = this.dispatcher.getDepartureArrivalSuffix();
    departureBlock.setArrivalSuffix(departureArrivalSuffix);

    BlockBean destinationBlock = this.dispatcher.getDestinationBlock();
    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setArrivalSuffix(arrivalSuffix);

    // Set Turnouts in the right state
    List<RouteElementBean> turnouts = getTurnouts(route);
    Logger.trace("There are " + turnouts.size() + " turnouts in this route");

    boolean switchesNotLocked = true;
    for (RouteElementBean reb : turnouts) {
      AccessoryValue av = reb.getAccessoryValue();
      AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
      //check if the accessory is not set by an other reserved route
      boolean locked = PersistenceFactory.getService().isAccessoryLocked(turnout.getId());
      if (locked) {
        switchesNotLocked = false;
        Logger.debug("Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] is locked!");
      }
    }

    //Now start to persist and perform critical thinks
    if (switchesNotLocked) {
      PersistenceFactory.getService().persist(route);

      if (swapLocomotiveDirection) {
        Direction newDirection = locomotive.getDirection();
        dispatcher.changeLocomotiveDirection(locomotive, newDirection);
      }

      for (RouteElementBean reb : turnouts) {
        AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        this.dispatcher.switchAccessory(turnout, av);
      }
      Logger.trace("Turnouts set for " + route);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);

      dispatcher.showRoute(route, Color.green);
      Logger.trace(route + " Locked");

      return true;
    } else {
      //Can't lock route
      route.setLocked(false);
      PersistenceFactory.getService().persist(route);
      Logger.trace(route + " NOT Locked");

      return false;
    }
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
