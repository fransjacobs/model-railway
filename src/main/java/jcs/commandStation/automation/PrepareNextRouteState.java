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

import jcs.entities.BlockBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Prepare next nextRoute state is to check for a free nextRoute when the Locomotive is entering a block.<br>
 */
class PrepareNextRouteState extends AbstractState {

  PrepareNextRouteState() {
    super("PrepareNextRoute");
  }

  @Override
  AbstractState execute() {
    int permits = RailwayController.avialablePermits();
    Logger.trace("Obtaining a lock. There is currently " + permits + " available permits...");

    boolean foundNextRoute = false;
    if (RailwayController.tryAquireLock()) {
      try {
        Logger.trace("##### Locked ####");
        if (dispatcher.searchRoute(false)) {
          foundNextRoute = dispatcher.reserveNextRoute();
        }
      } finally {
        //Make sure the lock is released
        RailwayController.releaseLock();
        Logger.trace("##### Released ####");
      }
    } else {
      Logger.trace("No Semaphore available");
      foundNextRoute = false;
    }

    if (dispatcher.isLocomotiveStarted()) {
      if (foundNextRoute) {
        return new ProceedingState();
      } else {
        return new BrakingState();
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
        dispatcher.resetRoute(nextRoute);
      }
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
