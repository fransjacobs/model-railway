/*
 * Copyright 2025 Frans Jacobs.
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
 * Prepare next nextRoute state is to check for a free nextRoute when the Locomotive is entering a block.<br>
 */
class PrepareNextRouteState extends DispatcherState {

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    int permits = AutoPilot.avialablePermits();
    Logger.trace("Obtaining a lock. There is currently " + permits + " available permits...");

    boolean foundNextRoute = false;
    if (AutoPilot.tryAquireLock()) {
      try {
        Logger.trace("##### Locked ####");
        if (searchNextRoute(dispatcher)) {
          foundNextRoute = reserveNextRoute(dispatcher);
        }
      } finally {
        //Make sure the lock is released
        AutoPilot.releaseLock();
        Logger.trace("##### Released ####");
      }
    } else {
      Logger.trace("No Semaphore available");
      foundNextRoute = false;
    }

    if (dispatcher.isLocomotiveAutomodeOn()) {
      if (foundNextRoute) {
        return new ContinueState();
      } else {
        return new BrakeState();
      }
    } else {
      //Rollback changes
      RouteBean nextRoute = dispatcher.getNextRouteBean();
      if (nextRoute != null) {
        nextRoute.setLocked(false);
        String nextDestinationTileId = nextRoute.getToTileId();
        BlockBean nextDestinationBlock = PersistenceFactory.getService().getBlockByTileId(nextDestinationTileId);
        nextDestinationBlock.setBlockState(BlockBean.BlockState.FREE);
        nextDestinationBlock.setArrivalSuffix(null);
        nextDestinationBlock.setLocomotive(null);
        PersistenceFactory.getService().persist(nextRoute);
        PersistenceFactory.getService().persist(nextDestinationBlock);
        dispatcher.showBlockState(nextDestinationBlock);
        Dispatcher.resetRoute(nextRoute);
      }
      return new BrakeState();
    }
  }

  boolean searchNextRoute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    Logger.trace("Search a free next route for " + locomotive.getName() + "...");

    // In this state the we are checking whether there is a valid nextRoute from the destination to the next block.
    BlockBean departureBlock = dispatcher.getDestinationBlock();

    //Use the current running locomotive direction
    Direction logicalDirection = locomotive.getDirection();

    TileBean tileBean = PersistenceFactory.getService().getTileBean(departureBlock.getTileId());
    Orientation blockOrientation = tileBean.getOrientation();

    String departureSuffix = departureBlock.getDepartureSuffix();
    if (departureSuffix == null) {
      departureSuffix = Block.getDepartureSuffix(blockOrientation, logicalDirection);
    }

    Logger.trace("Loco " + locomotive.getName() + " is entering block " + departureBlock.getId() + ". Direction " + logicalDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    //Search for the possible routes
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
    Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");

    List<RouteBean> checkedRoutes = new ArrayList<>();
    boolean commuter = locomotive.isCommuter();

    //Found possible routes check on the destination for the sensors and permissions
    for (RouteBean route : routes) {
      String nextDestinationTileId = route.getToTileId();
      BlockBean nextDestinationBlock = PersistenceFactory.getService().getBlockByTileId(nextDestinationTileId);
      //Check the sensors 
      boolean plusInActive = !nextDestinationBlock.getPlusSensorBean().isActive();
      boolean minInActive = !nextDestinationBlock.getMinSensorBean().isActive();

      boolean allowCommuter = nextDestinationBlock.isAllowCommuterOnly();
      boolean allowNonCommuter = nextDestinationBlock.isAllowNonCommuterOnly();

      boolean allowed = isAllowed(allowCommuter, allowNonCommuter, commuter);

      Logger.trace("Next Destination " + nextDestinationBlock.getId() + " Train type commuter: " + commuter + " Permission " + allowed + " sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));

      if (plusInActive && minInActive && allowed && turnoutsNotLocked(route)) {
        checkedRoutes.add(route);
      }
    }

    //Randomly pick a nextRoute in case multiple routes are found...
    int rIdx = 0;
    if (checkedRoutes.size() > 1) {
      //Choose randomly the nextRoute
      for (int i = 0; i < 10; i++) {
        //Seed a bit....
        dispatcher.getRandomNumber(0, checkedRoutes.size());
      }
      rIdx = dispatcher.getRandomNumber(0, checkedRoutes.size());
    }

    RouteBean nextRoute = null;
    if (!checkedRoutes.isEmpty()) {
      nextRoute = checkedRoutes.get(rIdx);
      Logger.trace("Choosen route " + nextRoute.toLogString());
      //persist the departure block
      //PersistenceFactory.getService().persist(departureBlock);
    } else {
      Logger.debug("No route available for " + locomotive.getName() + " ...");
    }
    dispatcher.setNextRouteBean(nextRoute);
    return nextRoute != null;
  }

  boolean reserveNextRoute(Dispatcher dispatcher) {
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
        AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        dispatcher.switchAccessory(turnout, av);
        //TODO configurable wait time between switches
        pause(500);
      }
      Logger.trace("Turnouts set for " + nextRoute);

      PersistenceFactory.getService().persist(nextDestinationBlock);

      dispatcher.showRoute(nextRoute, Color.yellow);
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

}
