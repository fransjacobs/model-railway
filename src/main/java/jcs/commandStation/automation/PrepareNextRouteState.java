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
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
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

  PrepareNextRouteState() {
    super("PrepareNextRoute");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

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

    //To avoid a ghost when the route searching is taking more time
    //then the time it takes for the locomotive to reach the In sensor
    //subscribe to the In sensor so that we can stop if worse comes to worst.
    //When a route is found and the average switch time is longer than 250 ms slow down a bit to buy some time 
    //Subscribe the IN sensor
    inSensorId = dispatcher.getInSensorId();
    dispatcher.getSensorMonitor().subscribe(inSensorId, this);
    Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId);

    //Search the next route...
    nextRouteFound = dispatcher.getRouteManager().searchNextRoute();

    if (nextRouteFound) {
      // If max switch time exceeds a threshold, slow down preemptively
      int estimatedSwitchTime = PersistenceFactory.getService().getAverageAccessorySwitchTime(dispatcher.getNextRouteBean()).intValue();
      if (estimatedSwitchTime > 500) {
        // Slow to speed 1 so we have more margin
        Integer speed1 = dispatcher.getLocomotiveBean().getSpeedOne();
        //Speed to ~10% or speed 1
        if (speed1 == null || speed1 == 0) {
          speed1 = 10;
        }

        dispatcher.changeLocomotiveVelocity(speed1);
        Logger.trace("Long switch time expected (" + estimatedSwitchTime + "ms), slowing train");
      }
    }
  }

  @Override
  AbstractState execute() {
    BlockBean destinationBlock = dispatcher.getDestinationBlock();

    boolean nextRouteAvaliable = false;
    if (nextRouteFound) {
      //Try to reserve the next route
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
      if (dispatcher.getStateMachine().isRequestStop() || !dispatcher.isLocomotiveStarted() || inSensorTriggered) {
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
    }

    Logger.trace("Locomotive " + dispatcher.getName() + " has entered destination " + destinationBlock.getDescription() + " and " + (nextRouteAvaliable ? "will continue" : "starts braking") + "...");

    if (nextRouteAvaliable) {
      return new PassingThroughState();
    } else if (inSensorTriggered) {
      return new ArrivedState();
    } else {
      return new BrakingState();
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

        Logger.trace("In Event from Sensor " + event.getSensorId() + " for " + dispatcher.getName() + " during route preparation!");
        dispatcher.wakeup();
      }
    } else {
      Logger.trace("Event for " + event.getSensorId() + " not for this state...");
    }
  }
}
