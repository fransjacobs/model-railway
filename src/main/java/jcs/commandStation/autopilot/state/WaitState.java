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

  int defaultWaitTime;

  WaitState(LocomotiveDispatcher dispatcher) {
    super(dispatcher);

    defaultWaitTime = Integer.getInteger("default.waittime", 5);
  }

  @Override
  DispatcherState next(LocomotiveDispatcher locRunner) {
    if (canAdvanceToNextState) {
      DispatcherState newState = new ReserveRouteState(dispatcher);
      return newState;
    } else {
      return this;
    }
  }

  @Override
  void execute() {
    //Stub 
    long waitTime = 1 * defaultWaitTime;

    Logger.debug("Waiting for " + waitTime + " s.");

    for (; waitTime >= 0; waitTime--) {
      if (this.dispatcher.isRunning()) {
        String s = this.dispatcher.getDispatcherState() + " (" + waitTime + ")";
        this.dispatcher.fireStateListeners(s);
        pause(1000);
      }
    }

    canAdvanceToNextState = true; //running;
  }

}
