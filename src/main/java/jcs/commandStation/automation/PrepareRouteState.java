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
import jcs.entities.LocomotiveBean;
import jcs.entities.StationBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Lock the route, set the block states and turnout directions
 */
class PrepareRouteState extends AbstractState {

  boolean canDepart;

  PrepareRouteState() {
    super("PrepareRoute");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    //Is the departure block part of a station?
    StationBean station = dispatcher.getStation(departureBlock);

    if (station != null) {
      int minLocCount = station.getMinLocomotives();
      int locCount = PersistenceFactory.getService().getLocomotiveCount(station).intValue();
      if (minLocCount <= locCount) {
        //The station has enough locomotives check whether this dispatcher is for the first locomotive to leave
        LocomotiveBean locomotive = PersistenceFactory.getService().getFirstLocomotive(station);
        canDepart = locomotive.getId().equals(dispatcher.getLocomotiveBean().getId());
      }
    } else {
      canDepart = true;
    }
  }

  @Override
  AbstractState execute() {
    int permits = RailwayController.avialablePermits();
    boolean canAdvanceToNextState = false;
    Logger.trace("Obtaining a lock. There is currently " + permits + " available permits...");

    if (RailwayController.tryAquireLock()) {
      try {
        Logger.trace("##### Locked ####");
        canAdvanceToNextState = dispatcher.getRouteManager().searchAndReserveRoute();
      } finally {
        //Make sure the lock is released
        RailwayController.releaseLock();
        Logger.trace("##### Released ####");
      }
    } else {
      Logger.trace("No Semaphore available");
    }

    if (canAdvanceToNextState) {
      dispatcher.getRouteManager().showRoute(dispatcher.getRouteBean(), Color.magenta);

      return new DepartingState();
    } else {
      //Go back to waiting and try again
      return new WaitingState();
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
