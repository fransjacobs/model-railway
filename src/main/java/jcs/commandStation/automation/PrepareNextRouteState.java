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
import jcs.entities.BlockBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Prepare next Route when possible to continue driving.<br>
 */
class PrepareNextRouteState extends AbstractState {

  PrepareNextRouteState() {
    super("PrepareNextRoute");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);
  }

  @Override
  AbstractState execute() {
    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    RouteBean route = dispatcher.getRouteBean();

    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.getRouteManager().showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);

    //try to find a route to the next block
    boolean nextRouteAvaliable = false;
    int permits = RailwayController.avialablePermits();
    Logger.trace("Obtaining a lock. There are currently " + permits + " available permits...");

    if (RailwayController.tryAquireLock()) {
      try {
        Logger.trace("##### Locked ####");
        nextRouteAvaliable = dispatcher.getRouteManager().searchAndReserveNextRoute();
      } finally {
        //Make sure the lock is released
        RailwayController.releaseLock();
        Logger.trace("##### Released ####");
      }
    } else {
      Logger.trace("No Semaphore available");
      nextRouteAvaliable = false;
    }

    //Check for a stop request
    if (dispatcher.getStateMachine().isRequestStop() || !dispatcher.isLocomotiveStarted()) {
      nextRouteAvaliable = false;
      RouteBean nextRoute = dispatcher.getNextRouteBean();
      if (nextRoute != null) {
        //Rollback changes due to stop request
        nextRoute.setLocked(false);
        String nextDestinationTileId = nextRoute.getToTileId();
        BlockBean nextDestinationBlock = PersistenceFactory.getService().getBlockByTileId(nextDestinationTileId);
        nextDestinationBlock.setBlockState(BlockBean.BlockState.FREE);
        nextDestinationBlock.setArrivalSuffix(null);
        nextDestinationBlock.setLocomotive(null);
        PersistenceFactory.getService().persist(nextRoute);
        PersistenceFactory.getService().persist(nextDestinationBlock);
        dispatcher.showBlockState(nextDestinationBlock);
        dispatcher.resetRoute(nextRoute);
      }
    }

    Logger.trace("Locomotive " + dispatcher.getName() + " has entered destination " + destinationBlock.getDescription() + " and " + (nextRouteAvaliable ? "will continue" : "starts braking") + "...");

    if (nextRouteAvaliable) {
      return new PassingThroughState();
    } else {
      return new BrakingState();
    }
  }

  @Override
  void onExit() {

  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }

}
