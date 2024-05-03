/*
 * Copyright 2024 frans.
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

import java.util.ArrayList;
import java.util.List;
import jcs.JCS;
import jcs.commandStation.autopilot.TrainDispatcher;
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
 *
 * @author frans
 */
public class LockRouteState extends DispatcherState {

  public LockRouteState(TrainDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void next(TrainDispatcher dispatcher) {
    if (canAdvanceToNextState) {
      DispatcherState newState = new RunState(this.dispatcher);
      newState.setRunning(running);
      dispatcher.setDispatcherState(newState);
    } else {
      dispatcher.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
    if (JCS.getJcsCommandStation() == null) {
      Logger.error("Can't obtain a Command Station");
      canAdvanceToNextState = false;
      running = false;
    }
    RouteBean route = dispatcher.getRouteBean();
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();

    if (route == null) {
      Logger.debug("Can't reserve a route for " + this.dispatcher.getLocomotiveBean().getName() + " ...");
      return;
    }

    Logger.debug("Reserving route " + route);
    route.setLocked(true);

    if (dispatcher.isSwapLocomotiveDirection()) {
      //During testing the locomotive direction is swapped   
      Direction newDirection = locomotive.getDirection();
      JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, locomotive);
      dispatcher.setSwapLocomotiveDirection(false);
    }

    // need the turnouts
    List<RouteElementBean> turnouts = getTurnouts(route);
    Logger.trace("There are " + turnouts.size() + " turnouts in this route");

    //Set the turnout in the right direction
    for (RouteElementBean reb : turnouts) {
      AccessoryValue av = reb.getAccessoryValue();
      AccessoryBean turnout = reb.getTileBean().getAccessoryBean();

      Logger.debug("Setting turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
      JCS.getJcsCommandStation().switchAccessory(turnout, av);
    }
    Logger.debug("Locked route " + route);

    //Lock the destination
    String departureTileId = route.getFromTileId();
    String destinationTileId = route.getToTileId();
    //From which side on the block is the train expected to arrive?
    String arrivalSuffix = route.getToSuffix();

    Logger.trace("Destination tile: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block");

    BlockBean departureBlock = PersistenceFactory.getService().getBlockByTileId(departureTileId);
    departureBlock.setBlockState(BlockBean.BlockState.DEPARTING);
    this.dispatcher.setDepartureBlock(departureBlock);
    BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(destinationTileId);
    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    this.dispatcher.setDestinationBlock(destinationBlock);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    this.dispatcher.showRoute(route);

    canAdvanceToNextState = true;
    Logger.trace("Can advance to next state: " + canAdvanceToNextState);
  }

  List<RouteElementBean> getTurnouts(RouteBean routeBean) {
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
