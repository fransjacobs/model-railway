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
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class InBlockState extends DispatcherState {

  InBlockState(TrainDispatcher dispatcher, boolean running) {
    super(dispatcher, running);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    if (canAdvanceToNextState) {
      DispatcherState newState = new WaitState(this.dispatcher, isRunning());
      locRunner.setDispatcherState(newState);
    } else {
      locRunner.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
    LocomotiveBean locomotive = this.dispatcher.getLocomotiveBean();
    this.dispatcher.changeLocomotiveVelocity(locomotive, 0);

    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    Logger.trace("Locomotive " + locomotive.getName() + " has arrived in destination " + destinationBlock.getDescription() + ". Stopping....");

    BlockBean departureBlock = this.dispatcher.getDepartureBlock();
    departureBlock.setBlockState(BlockBean.BlockState.FREE);
    departureBlock.setLocomotive(null);

    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setBlockState(BlockBean.BlockState.OCCUPIED);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.clearRouteEventHandlers();

    RouteBean route = dispatcher.getRouteBean();
    route.setLocked(false);
    PersistenceFactory.getService().persist(route);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showBlockState(destinationBlock);

    this.dispatcher.resetRoute(route);
    this.dispatcher.setRouteBean(null);
    this.canAdvanceToNextState = true;
  }

}
