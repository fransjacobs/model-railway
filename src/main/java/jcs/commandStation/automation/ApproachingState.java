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
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Approaching state of the Autopilot State Machine.<br>
 * When the enter sensor is active the state machine advances to this state.<br>
 * In the state it is checked whether the destination block has the property alwayStop set.<br>
 * If so the locomotive has to stop when it reaches the IN sensor.<br>
 * If not try to find the next route so that the locomotive can continue.<br>
 * Therefor next state will be the BrakingStage.<br>
 *
 * When the locomotive does not have to stop, the next possible route to the next block is searched.<br>
 * This is done in the PrepareNextRouteState.
 */
class ApproachingState extends AbstractState {

  ApproachingState() {
    super("Approaching");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();

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

    Logger.trace("Locomotive " + locomotive.getName() + " has entered destination " + destinationBlock.getDescription() + " and" + (destinationBlock.isAlwaysStop() ? " will stop" : " can continue") + "...");
  }

  @Override
  AbstractState execute() {
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    boolean alwaysStop = destinationBlock.isAlwaysStop();

    boolean automodeInActive = !dispatcher.getRailwayController().isAutoModeActive();

    if (alwaysStop || automodeInActive) {
      return new BrakingState();
    } else {
      return new PrepareNextRouteState();
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
