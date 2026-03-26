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

  private volatile boolean canDepart;

  PrepareRouteState() {
    super("PrepareRoute");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    //Check if we can reallt depart. Departure could be determined when the departure block is part of a Station.
    //If the is the case, should the Station act as a FIFO, or can we just leave based on the numbers?    
    BlockBean departureBlock = dispatcher.getDepartureBlock();
    StationBean station = dispatcher.getStation(departureBlock);
    if (station != null) {
      int minLocCount = station.getMinLocomotives();
      if (minLocCount > 0) {
        int locCount = PersistenceFactory.getService().getLocomotiveCount(station).intValue();
        if (minLocCount <= locCount) {
          //The station has enough locomotives check whether this dispatcher is for the first locomotive to leave
          if (station.isFifo()) {
            Long firstToLeaveLocomotiveId = PersistenceFactory.getService().getFirstLocomotiveId(station);
            canDepart = dispatcher.getLocomotiveId().equals(firstToLeaveLocomotiveId);
            if (!canDepart) {
              Logger.trace("Locomotive " + dispatcher.getName() + " is not the first to leave Station " + station.getName() + "...");
            }
          } else {
            canDepart = true;
          }
        }
      } else {
        canDepart = true;
      }
    } else {
      canDepart = true;
    }
  }

  @Override
  AbstractState execute() {
    boolean canAdvanceToNextState = false;
    if (canDepart) {
      BlockBean blockBean = dispatcher.getDepartureBlock();
      LocomotiveBean locomotiveBean = dispatcher.getLocomotiveBean();
      Logger.debug("Locomotive " + locomotiveBean.getName() + " Direction: " + locomotiveBean.getDirection().getDirection() + " search for route from block " + blockBean.getId() + " logicalDir: " + blockBean.getLogicalDirection() + " Arrived at " + blockBean.getArrivalSuffix());

      int permits = RailController.avialablePermits();
      Logger.trace("Obtaining a lock. There is currently " + permits + " available permits...");

      if (RailController.tryAquireLock()) {
        try {
          Logger.trace("##### Locked ####");
          canAdvanceToNextState = dispatcher.getRouteManager().searchAndReserveRoute();
        } finally {
          //Make sure the lock is released
          RailController.releaseLock();
          Logger.trace("##### Released ####");
        }
      } else {
        Logger.trace("No Semaphore available");
      }
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
