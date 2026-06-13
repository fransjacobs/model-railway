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

import static jcs.commandStation.automation.AbstractState.State.DEPARTING;
import static jcs.commandStation.automation.RailController.TAG;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Departing state of the Auto Drive State machine.<br>
 * This state is entered when a valid route is found and reserved.<br>
 * This state will start the locomotive by sending the direction and start velocity commands to the command station.<br>
 * Then it will automatically transition to the running state
 */
class DepartingState extends AbstractState {

  DepartingState() {
    super(DEPARTING);
  }

  @Override
  AbstractState execute() {
    LocomotiveBean locomotive = PersistenceFactory.getService().getLocomotive(dispatcher.getLocomotiveId());

    boolean delay = dispatcher.handleSignal(state);

    if (delay) {
      //delay the start a while
      long startDelayTime = Long.getLong("default.start-delaytime", 2000L);
      Logger.tag(TAG).debug("Dispatcher " + dispatcher.getName() + " Delaying departure by " + startDelayTime + "ms due to signal proccessing...");
      try {
        Thread.sleep(startDelayTime);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        Logger.tag(TAG).warn("Dispatcher {} departure delay interrupted!", dispatcher.getName());
        return this;
      }
    }

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);

    //Speed to ~75% or speed 3
    Integer speed3 = locomotive.getSpeedThree();
    if (speed3 == null || speed3 == 0) {
      speed3 = 75;
    }

    int fullscale = locomotive.getTachoMax();
    double velocity = (speed3 / (double) fullscale) * 1000;
    dispatcher.changeLocomotiveVelocity(velocity);

    locomotive.setVelocity((int) velocity);
    departureBlock.setLocomotive(locomotive);
    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showBlockState(destinationBlock);

    Logger.tag(TAG).debug("Dispatcher {} departing in direction: {} Route: {} power: {}%",
            dispatcher.getName(), locomotive.getDirection(), dispatcher.getRouteBean().getId(), speed3);

    return new RunningState();
  }

  @Override
  void onExit() {

  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }

}
