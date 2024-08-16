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

  WaitState() {
    defaultWaitTime = Integer.getInteger("default.waittime", 5);
  }

  @Override
  DispatcherState execute(Dispatcher dispatcher) {

    //BlockBean blockBean = dispatcher.getDepartureBlock();
    //boolean random = blockBean.isRandomWait();
    //int minWait = blockBean.getMinWaitTime();
    //int defaultMaxWaitTime = Integer.getInteger("default.max.waittime", 20);
    //int maxWait = blockBean.getMaxWaitTime();
    //Choose randomly the route
//      for (int i = 0; i < 10; i++) {
//        //Seed a bit....
//        getRandomNumber(0, checkedRoutes.size());
//      }
//      rIdx = getRandomNumber(0, checkedRoutes.size());
    //hoe te wachen? aparte worker thread?
    //Stub
    //TODO Obtain the wait time from either the block in the database of from the locomotive
    long waitTime = 1 * defaultWaitTime;

    //when the thread is running and the loc is in automode wait until the wait time has past.
    //then switch state.
    //incase the auto mode is disabled switch to Idle mode
    Logger.debug("Waiting for " + waitTime + " s.");

    for (; waitTime >= 0; waitTime--) {
      if (dispatcher.isLocomotiveAutomodeOn()) {
        String s = dispatcher.getDispatcherStateString() + " (" + waitTime + ")";
        dispatcher.fireStateListeners(s);

        //For manual testing the thread is not running, step mode
        if (dispatcher.isRunning()) {
          pause(1000);
        } else {
          Logger.trace("Test mode: " + s);
        }
      } else {
        //Locomotive automode is disabled break the loop
        break;
      }
    }

    DispatcherState newState;
    if (dispatcher.isLocomotiveAutomodeOn()) {
      newState = new PrepareRouteState();
    } else {
      newState = new IdleState();
    }
    return newState;
  }

}
