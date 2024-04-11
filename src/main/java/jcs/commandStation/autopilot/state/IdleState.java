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
import java.util.stream.Collectors;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Entry State when a Locomotive is enabled in a block
 */
public class IdleState implements DispatcherState {
  
  private final LocomotiveBean locomotive;
  
  IdleState(LocomotiveBean locomotive) {
    this.locomotive = locomotive;
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    //Next state is only possibe when this locomotive is on the track and in a block
    if(isInBlock()) {
      Logger.debug("Locomotive "+this.locomotive.getName()+" ["+this.locomotive.getId()+"] is in a block");
      locRunner.setState(new RouteSearchState(locomotive));
    } else {
      Logger.debug("Locomotive "+this.locomotive.getName()+" ["+this.locomotive.getId()+"] is not in a block");
      locRunner.setState(this);
    }  
  }

  @Override
  public void prev(TrainDispatcher locRunner) {
    Logger.debug("This is the root state");
  }

  @Override
  public void logState() {
    Logger.trace(this.getClass().getSimpleName()+" waiting to do something...");
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  private boolean isInBlock() {
    BlockBean block = PersistenceFactory.getService().getBlockByLocomotiveId(this.locomotive.getId());
    
    return block != null;
  }
  
  
}
