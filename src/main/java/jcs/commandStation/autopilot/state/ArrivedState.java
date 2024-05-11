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

import jcs.JCS;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class ArrivedState extends DispatcherState {

  public ArrivedState(TrainDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    Logger.trace("canAdvanceState: " + canAdvanceToNextState);
    if (canAdvanceToNextState) {
      DispatcherState newState = new WaitState(this.dispatcher);
      newState.setRunning(running);
      locRunner.setDispatcherState(newState);
    } else {
      locRunner.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
    while (!dispatcher.isInDestinationBlock()) {
      try {
        //wait
        //this.wait();
        Thread.sleep(1000L);
      } catch (InterruptedException ex) {
        Logger.trace(ex);
      }
    }

    if (this.dispatcher.isInDestinationBlock()) {
      LocomotiveBean locomotive = this.dispatcher.getLocomotiveBean();
      BlockBean destinationBlock = dispatcher.getDestinationBlock();
      Logger.debug("Locomotive " + locomotive.getName() + " has arrived in destination " + destinationBlock.getDescription() + ". Stopping....");
      JCS.getJcsCommandStation().changeLocomotiveSpeed(0, locomotive);

      BlockBean departureBlock = this.dispatcher.getDepartureBlock();
      departureBlock.setBlockState(BlockBean.BlockState.FREE);
      departureBlock.setLocomotive(null);

      destinationBlock.setLocomotive(locomotive);
      destinationBlock.setBlockState(BlockBean.BlockState.OCCUPIED);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      dispatcher.resetAllRouteEventHandlers();

      RouteBean route = dispatcher.getRouteBean();
      route.setLocked(false);
      PersistenceFactory.getService().persist(route);

      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);

      this.dispatcher.resetRoute(route);

      this.dispatcher.setRouteBean(null);

      //refreshBlockTiles();
      this.canAdvanceToNextState = true;
    }

    Logger.trace("Can advance to next state: " + canAdvanceToNextState);
  }

}
