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
 * Entry State when a Locomotive is enabled in a block
 */
public class IdleState extends DispatcherState {

  public IdleState(TrainDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void next(TrainDispatcher dispatcher) {
    //Next state is only possible when this locomotive is on the track and in a block
    if (canAdvanceToNextState) {
      DispatcherState newState = new SearchRouteState(this.dispatcher);
      newState.setRunning(running);
      dispatcher.setDispatcherState(newState);
    } else {
      dispatcher.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    BlockBean block = PersistenceFactory.getService().getBlockByLocomotiveId(locomotive.getId());
    dispatcher.setDepartureBlock(block);

    if (block != null) {
      Logger.debug("Locomotive " + locomotive.getName() + " [" + locomotive.getId() + "] is in block " + block.getDescription() + " [" + block.getId() + "] dir: "+locomotive.getDirection().getDirection());
    } else {
      Logger.debug("Locomotive " + locomotive.getName() + " [" + locomotive.getId() + "] is not in a block");
    }

    canAdvanceToNextState = block != null;
    Logger.trace("Can advance to next state: " + canAdvanceToNextState);
  }

}
