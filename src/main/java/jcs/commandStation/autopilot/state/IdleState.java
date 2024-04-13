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

import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Entry State when a Locomotive is enabled in a block
 */
public class IdleState extends DispatcherState {

  IdleState(LocomotiveBean locomotive) {
    super(locomotive);
  }

  @Override
  public void next(TrainDispatcher dispatcher) {
    //Next state is only possibe when this locomotive is on the track and in a block
    if (canAdvanceState) {
      Logger.debug("Locomotive " + locomotive.getName() + " [" + locomotive.getId() + "] is in a block");
      dispatcher.setState(new SearchRouteState(locomotive));
    } else {
      Logger.debug("Locomotive " + locomotive.getName() + " [" + locomotive.getId() + "] is not in a block");
      dispatcher.setState(this);
    }
  }

  @Override
  public void prev(TrainDispatcher dispatcher) {
    Logger.debug("This is the root state");
  }

  @Override
  void onHalt(TrainDispatcher dispatcher) {
    Logger.debug("HALT!");
  }

  @Override
  public boolean performAction() {
    BlockBean block = PersistenceFactory.getService().getBlockByLocomotiveId(this.locomotive.getId());
    canAdvanceState = block != null;
    return canAdvanceState;
  }

}
