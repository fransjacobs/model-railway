/*
 * Copyright 2024 Frans Jacobs
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
abstract class DispatcherState {

  protected final Dispatcher dispatcher;
  protected boolean canAdvanceToNextState;

  protected DispatcherState(Dispatcher trainDispatcher) {
    this.dispatcher = trainDispatcher;
  }

  //abstract DispatcherState next(Dispatcher dispatcher);

  abstract DispatcherState execute(Dispatcher dispatcher);

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  public String getName() {
    return this.getClass().getSimpleName();
  }

  void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }
}
