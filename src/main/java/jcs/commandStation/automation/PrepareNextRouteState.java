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
import static jcs.commandStation.automation.AbstractState.State.PREPNEXTROUTE;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Prepare next Route when possible to continue driving.<br>
 * I could be the case the the in sensor is triggered while searching for a route.<br>
 * To avoid a ghost subscribe to the in sensor event
 */
class PrepareNextRouteState extends AbstractState implements SensorEventCallback {

  private Integer inSensorId;
  private boolean nextRouteFound;
  private boolean inSensorTriggered = false;
  private boolean nextRouteAvaliable = false;

  PrepareNextRouteState() {
    super(PREPNEXTROUTE);
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    //To avoid a ghost when the route searching is taking more time
    //then the time it takes for the locomotive to reach the In sensor
    //subscribe to the In sensor so that we can stop if worse comes to worst.
    //When a route is found and the average switch time is longer than 500 ms slow down a bit to buy some time 
    //Subscribe the IN sensor
    inSensorId = dispatcher.getInSensorId();
    dispatcher.getSensorMonitor().subscribe(inSensorId, this);

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    RouteBean route = dispatcher.getRouteBean();

    Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId);

    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.getRouteManager().showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);

    //Search for a next route...
    nextRouteFound = dispatcher.getRouteManager().searchNextRoute();

    if (nextRouteFound) {
      // If max switch time exceeds a threshold, slow down preemptively
      int estimatedSwitchTime = dispatcher.getRouteManager().getEstimatedNextRouteSwitchTime();
      if (estimatedSwitchTime > 500) {
        // Slow to speed 1 so we have more margin
        Integer speed1 = dispatcher.getLocomotiveBean().getSpeedOne();
        //Speed to ~10% or speed 1
        if (speed1 == null || speed1 == 0) {
          speed1 = 10;
        }

        dispatcher.changeLocomotiveVelocity(speed1);
        Logger.trace("Reducing speed while reserving the next route. Est. switch time: (" + estimatedSwitchTime + "ms)...");
      }
    }
  }

  private void rollbackNextRoute() {
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

      Logger.trace("Rolled back next route for " + dispatcher.getName());
    }
  }

  @Override
  AbstractState execute() {
    BlockBean destinationBlock = dispatcher.getDestinationBlock();

    Logger.debug((nextRouteFound ? "Next" : "No") + " Route found for: " + dispatcher.getName() + " in " + destinationBlock.getDescription() + " Direction: " + dispatcher.getLocomotiveBean().getDirection().getDirection() + " Route: " + dispatcher.getRouteBean().getId() + " Speed: " + dispatcher.getLocomotiveBean().getVelocity() + " Listening for In sensorId: " + inSensorId + "...");

    if (nextRouteFound) {
      //Try to reserve the next route
      int permits = RailController.avialablePermits();
      Logger.trace("Obtaining a lock. There are currently " + permits + " available permits...");
      if (permits > 0) {
        if (RailController.tryAquireLock()) {
          try {
            Logger.trace("##### Locked ####");
            nextRouteAvaliable = dispatcher.getRouteManager().searchAndReserveNextRoute();
          } finally {
            //Make sure the lock is released
            RailController.releaseLock();
            Logger.trace("##### Released ####");
          }
        } else {
          Logger.trace("No Semaphore available");
          nextRouteAvaliable = false;
          nextRouteFound = false;
        }
      } else {
        nextRouteFound = false;
        Logger.trace("No lock permits available");
      }

      boolean automodeInActive = !dispatcher.getRailController().isAutoModeActive();

      //Check for a stop request
      if (dispatcher.getStateMachine().isRequestStop() || !dispatcher.isLocomotiveStarted() || automodeInActive) {
        nextRouteAvaliable = false;
        rollbackNextRoute();
      }
    }

    Logger.debug("Locomotive: " + dispatcher.getName() + " in " + destinationBlock.getDescription() + " and " + (nextRouteAvaliable ? "will continue" : "starts braking") + " Current Route: " + dispatcher.getRouteBean().getId() + " Speed: " + dispatcher.getLocomotiveBean().getVelocity() + "...");

    if (inSensorTriggered) {
      return new ArrivedState();
    } else if (nextRouteAvaliable) {
      dispatcher.handleSignal(state);
      return new PassingThroughState(inSensorTriggered);
    } else {
      return new BrakingState(inSensorTriggered);
    }
  }

  @Override
  void onExit() {
    dispatcher.getSensorMonitor().unsubscribe(inSensorId, this);
  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }

  @Override
  public void onEvent(SensorEvent event) {
    if (inSensorId.equals(event.getSensorId())) {
      if (event.isActive()) {
        inSensorTriggered = true;
        //Stop the locomotive!
        dispatcher.changeLocomotiveVelocity(0);

        Logger.debug("In Event from Sensor " + event.getSensorId() + " for " + dispatcher.getName() + " during route preparation!");
        nextRouteFound = false;
        dispatcher.wakeup();
      }
    } else {
      Logger.trace("Event for " + event.getSensorId() + " not for this state...");
    }
  }
}
