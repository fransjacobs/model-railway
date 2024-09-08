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

import jcs.commandStation.autopilot.AutoPilot;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 * Entry State when a Locomotive is enabled in a block
 */
class IdleState extends DispatcherState {

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    BlockBean block = dispatcher.getDepartureBlock();

    boolean canAdvanceToNextState = block != null && dispatcher.isLocomotiveAutomodeOn();

    //TODO Reduce the number of log lines....
    if (block != null && AutoPilot.isAutoModeActive()) {
      Logger.debug("Locomotive " + locomotive.getName() + " [" + locomotive.getId() + "] is in block " + block.getDescription() + " [" + block.getId() + "] dir: " + locomotive.getDirection().getDirection() + " Can advance: " + canAdvanceToNextState);
    } else {
      if (AutoPilot.isAutoModeActive()) {
        Logger.debug("Locomotive " + locomotive.getName() + " [" + locomotive.getId() + "] is not in a block!");
      } else {
        Logger.debug("State of " + dispatcher.getName() + " will not change as the AutoPilot Automode is Off");
      }
    }

    if (canAdvanceToNextState) {
      DispatcherState newState = new PrepareRouteState();
      return newState;
    } else {
      if ("true".equals(System.getProperty("state.machine.stepTest", "false"))) {
        Logger.debug("StateMachine StepTest is enabled. Dispatcher: " + dispatcher.getName() + " State: " + dispatcher.getStateName());
      } else {
        if (AutoPilot.isAutoModeActive() && dispatcher.isLocomotiveAutomodeOn()) {
          Logger.trace(dispatcher.getName() + " is Idle...");
          try {
            synchronized (this) {
              wait(10000);
            }
          } catch (InterruptedException ex) {
            Logger.trace("Interrupted: " + ex.getMessage());
          }
        }
      }
      return this;
    }
  }

}
