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
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class WaitState extends DispatcherState {

  WaitState() {
    super();
  }

  @Override
  DispatcherState execute(Dispatcher dispatcher) {

    BlockBean blockBean = dispatcher.getDepartureBlock();
    int minWait = blockBean.getMinWaitTime();
    int maxWait;
    if (blockBean.getMaxWaitTime() != null) {
      maxWait = blockBean.getMaxWaitTime();
    } else {
      maxWait = Integer.getInteger("default.max.waittime", 20);
    }

    long waitTime;
    if (blockBean.isRandomWait()) {
      //Seed a bit....
      for (int i = 0; i < 10; i++) {
        dispatcher.getRandomNumber(minWait, maxWait);
      }

      waitTime = dispatcher.getRandomNumber(minWait, maxWait);
    } else {
      waitTime = minWait;
    }

    Logger.debug("Waiting for " + waitTime + " s.");

    for (; waitTime >= 0; waitTime--) {
      if (dispatcher.isLocomotiveAutomodeOn()) {
        String s = dispatcher.getStateName() + " (" + waitTime + ")";
        dispatcher.fireStateListeners(s);

        //For manual testing the thread is not running, step mode
        if ("false".equals(System.getProperty("dispatcher.stepTest", "false"))) {
          //synchronized (this) {
          try {
            //wait(1000);
            Thread.sleep(1000L);
          } catch (InterruptedException ex) {
            Logger.trace("Wait loop interrupted");
          }
          //}
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
