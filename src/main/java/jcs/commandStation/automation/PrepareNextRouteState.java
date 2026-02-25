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
 * Prepare next nextRoute state is to check for a free nextRoute when the Locomotive is entering a block.<br>
 */
class PrepareNextRouteState extends AbstractState {

    private Integer inSensorId;
  //private boolean inSensorTriggerred = false;

  
  PrepareNextRouteState() {
    super("PrepareNextRoute");
  }
  
 @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);
    
     BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    RouteBean route = dispatcher.getRouteBean();

    Logger.trace("Locomotive " + dispatcher.getLocomotiveBean().getName() + " has entered destination " + destinationBlock.getDescription() + " and prepares to stop...");

    //Subscribe the IN sensor
    inSensorId = dispatcher.getInSensorId();
    //dispatcher.getSensorMonitor().subscribe(inSensorId, this);
    Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId);

    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);
   
    
    
    
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
