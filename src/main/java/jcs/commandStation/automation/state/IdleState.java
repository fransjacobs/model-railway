/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.automation.state;

import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 * Idle State, do nothing
 */
public class IdleState extends AbstractState {

  private LocomotiveBean locomotive;
  private BlockBean block;

  public IdleState() {
    super("Idle");
  }

  /**
   *
   * @return
   */
  @Override
  AbstractState execute() {
    boolean canAdvanceToNextState = block != null && dispatcher.isLocomotiveStarted();

    if (block != null && dispatcher.isLocomotiveStarted()) {
      Logger.trace("Locomotive " + locomotive.getName() + " [" + locomotive.getId() + "] is in block " + block.getDescription() + " [" + block.getId() + "] dir: " + locomotive.getDirection().getDirection() + " Can advance: " + canAdvanceToNextState);
    } else {
      if (dispatcher.isLocomotiveStarted()) {
        Logger.trace("Locomotive " + locomotive.getName() + " [" + locomotive.getId() + "] is not in a block!");
      } else {
        Logger.trace("State of " + dispatcher.getName() + " will not change as the Locomotive is not started");
      }
    }

    if (canAdvanceToNextState) {
      return this; // new PrepareRouteState();
    } else {
      return this;
    }
  }

  @Override
  void onExit() {

  }

}
