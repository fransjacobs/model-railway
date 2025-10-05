/*
 * Copyright 2025 Frans Jacobs.
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
import org.tinylog.Logger;

/**
 * Reset State to rollback all dispatcher settings
 */
class ResettingState extends DispatcherState {

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    BlockBean block = dispatcher.getDepartureBlock();

    dispatcher.resetStateMachine();

    //reset all settings and reservations.
    dispatcher.resetAttributes();

    resetRequested = false;
    
    Logger.trace("Resetted dispatcher for "+locomotive.getName()+" in block "+block.getId());
    
    return new IdleState();
  }

}
