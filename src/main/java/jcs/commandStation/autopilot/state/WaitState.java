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

  WaitState(Dispatcher dispatcher) {
    super(dispatcher);

    defaultWaitTime = Integer.getInteger("default.waittime", 5);
  }

  @Override
  DispatcherState execute(Dispatcher locRunner) {
    //Stub
    //TODO Obtain the wait time from either the block in the database of from the locomotive
    long waitTime = 1 * defaultWaitTime;

    //when the thread is running and the loc is in automode wait until the wait time has past.
    //then switch state.
    //incase the auto mode is disabled switch to Idle mode
    Logger.debug("Waiting for " + waitTime + " s.");

    for (; waitTime >= 0; waitTime--) {
      if (this.dispatcher.isLocomotiveAutomodeOn()) {
        String s = this.dispatcher.getDispatcherState() + " (" + waitTime + ")";
        this.dispatcher.fireStateListeners(s);

        //For manual testing
        if (this.dispatcher.isRunning()) {
          pause(1000);
        }
      } else {
        //Locomotive automode is disabled break the loop
        break;
      }
    }

    //canAdvanceToNextState = this.dispatcher.isRunning() && this.dispatcher.isLocomotiveAutomodeOn(); 
    DispatcherState newState;
    if (this.dispatcher.isLocomotiveAutomodeOn()) {
      newState = new PrepareRouteState(dispatcher);
    } else {
      newState = new IdleState(dispatcher);
    }
    return newState;

  }

}
