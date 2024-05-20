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

import jcs.JCS;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
abstract class DispatcherState {

  protected final TrainDispatcher dispatcher;

  private boolean running;

  //protected int waitTime;
  protected boolean canAdvanceToNextState;

  protected DispatcherState(TrainDispatcher trainDispatcher, boolean running) {
    this.dispatcher = trainDispatcher;
    this.running = running;
  }

  abstract void next(TrainDispatcher dispatcher);

  abstract void execute();

  //int getWaitTime() {
  //  return waitTime;
  //}
  boolean canAdvanceToNextState() {
    return canAdvanceToNextState;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }

  synchronized boolean isRunning() {
    return running;
  }

  synchronized void setRunning(boolean running) {
    if (running && JCS.getJcsCommandStation() == null) {
      Logger.error("Can't obtain a Command Station");
      this.running = false;
    } else {
      this.running = running;
    }
  }

}
