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

import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Start state of the Autopilot State machine. This state is entered when a valid route is found, or reserved.<br>
 * This state will start the locomotive by sending the direction and start velocity commands to the command station.<br>
 * Then it will automatically proceed to the running state
 */
class StartingState extends DispatcherState {

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();

    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    PersistenceFactory.getService().persist(departureBlock);

    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showBlockState(destinationBlock);

    Logger.trace("Starting " + locomotive.getName() + " Direction " + locomotive.getDirection());

    //First time starting as current velocity is zero, ensure the direction is right
    if (locomotive.getVelocity() == 0) {
      dispatcher.changeLocomotiveDirection(locomotive, locomotive.getDirection());
    }

    //Speed to ~75% or speed 3
    Integer speed3 = locomotive.getSpeedThree();
    if (speed3 == null || speed3 == 0) {
      speed3 = 75;
    }

    int fullscale = locomotive.getTachoMax();
    double velocity = (speed3 / (double) fullscale) * 1000;
    dispatcher.changeLocomotiveVelocity(locomotive, velocity);

    DispatcherState newState;
    if (resetRequested) {
      newState = new ResettingState();
    } else {
      newState = new RunningState();
    }
    return newState;
  }
}
