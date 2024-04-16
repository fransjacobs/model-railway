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

import jcs.commandStation.autopilot.TrainDispatcher;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class WaitState extends DispatcherState {

  WaitState(LocomotiveBean locomotive) {
    super(locomotive);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    DispatcherState newState = new SearchRouteState(locomotive);
    newState.setRunning(running);
    locRunner.setDispatcherState(newState);
  }

  @Override
  public boolean execute() {
    Logger.debug("Waiting");
    if (running) {
      pause(5000);
    }

    canAdvanceToNextState = running;
    return canAdvanceToNextState;
  }

}
