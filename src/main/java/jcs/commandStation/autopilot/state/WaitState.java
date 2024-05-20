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

import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class WaitState extends DispatcherState {

  WaitState(TrainDispatcher dispatcher, boolean running) {
    super(dispatcher, running);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    if (canAdvanceToNextState) {
      DispatcherState newState = new ReserveRouteState(this.dispatcher, isRunning());
      locRunner.setDispatcherState(newState);
    } else {
      locRunner.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
    //Stub 
    int waitTime = 5;

    for (; waitTime >= 0; waitTime--) {
      if (isRunning()) {
        String s = this.dispatcher.getDispatcherState() + " (" + waitTime + ")";
        this.dispatcher.fireStateListeners(s);
        pause(1000);
       }

    }

    //TODO make this configurable
    Logger.debug("Waiting");
    if (isRunning()) {
      pause(5000);
    }

    canAdvanceToNextState = true; //running;
  }

}
