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
import org.tinylog.Logger;

/**
 * Lock the route, set the block states and turnout directions
 */
public class PrepareRouteState extends AbstractState {

  public PrepareRouteState() {
    super("PrepareRoute");
  }

  @Override
  AbstractState execute() {
    int permits = RailwayController.avialablePermits();
    boolean canAdvanceToNextState = false;
    Logger.trace("Obtaining a lock. There is currently " + permits + " available permits...");

    if (RailwayController.tryAquireLock()) {
      try {
        Logger.trace("##### Locked ####");
        if (dispatcher.searchRoute(true)) {
          canAdvanceToNextState = dispatcher.reserveRoute();
        }
      } finally {
        //Make sure the lock is released
        RailwayController.releaseLock();
        Logger.trace("##### Released ####");
      }
    } else {
      Logger.trace("No Semaphore available");
    }

    if (canAdvanceToNextState) {
      dispatcher.showRoute(dispatcher.getRouteBean(), Color.magenta);
      return new StartingState();
    } else {
      //Go back to waiting and try again
      return new WaitingState();
    }
  }
}
