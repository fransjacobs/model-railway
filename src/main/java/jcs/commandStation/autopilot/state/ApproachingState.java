/*
 * Copyright 2024 Frans Jacobs.
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
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Approaching state of the Autopilot State Machine.<br>
 * When the enter sensor is active the state machine advances to this state. In the state it is checked whether the destination block has the property alwayStop set.<br>
 * If so the locomotive has to stop when it reaches the IN sensor.<br>
 * Therefor next state will be the BrakingStage.<br>
 *
 * When the locomotive does not have to stop, the next possible route to the next block is searched.<br>
 * This is done in the PrepareNextRouteState.
 */
class ApproachingState extends DispatcherState {

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    RouteBean route = dispatcher.getRouteBean();
    boolean startBraking = destinationBlock.isAlwaysStop();
    Logger.trace("Locomotive " + locomotive.getName() + " has entered destination " + destinationBlock.getDescription() + " Must stop: " + startBraking);

    //Change Block statuses 
    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);

    if (dispatcher.isLocomotiveAutomodeOn()) {
      if (startBraking) {
        return new BrakingState();
      } else {
        //Lets check whether there is a next available route to continue....
        return new PrepareNextRouteState();
      }
    } else {
      return new BrakingState();
    }
  }

}
