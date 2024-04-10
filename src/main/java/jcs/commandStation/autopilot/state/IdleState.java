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

import org.tinylog.Logger;

/**
 * Entry State when a Locomotive is enabled in a block
 */
public class IdleState implements DispatcherState {

  @Override
  public void next(TrainDispatcher locRunner) {
    locRunner.setState(new RouteSearchState());
  }

  @Override
  public void prev(TrainDispatcher locRunner) {
    Logger.debug("This is the root state");
  }

  @Override
  public void printStatus() {
    Logger.trace("Idle, waiting to do something...");
  }

  @Override
  public String toString() {
    return "IdleState{}";
  }

}
