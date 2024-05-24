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

import jcs.JCS;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class LocomotiveRunnerThread extends Thread {

  private final LocomotiveDispatcher dispatcher;
  private DispatcherState dispatcherState;
  private boolean running;

  LocomotiveRunnerThread(LocomotiveDispatcher dispatcher) {
    this.dispatcher = dispatcher;
    this.dispatcherState = new IdleState(dispatcher);
  }

  @Override
  public void start() {
    super.start();
    setName("LRT->" + this.dispatcher.getLocomotiveBean().getName());
    Logger.trace(getName() + " Started. in State: " + this.dispatcherState.getClass().getSimpleName() + "...");
  }

  boolean isRunning() {
    return this.running;
  }

  synchronized void stopRunning() {
    this.running = false;
    notify();
  }

  DispatcherState getDispatcherState() {
    return dispatcherState;
  }

  @Override
  public void run() {
    if (JCS.getJcsCommandStation() == null) {
      Logger.error("Can't due to missing Command Station");
      return;
    } else {
      this.running = true;
      Logger.trace(getName() + " " + this.dispatcher.getDispatcherState() + " Running...");
    }

    while (running) {
      //Perform the action for the current state
      dispatcherState.execute();

      DispatcherState previousState = this.dispatcherState;
      this.dispatcherState = dispatcherState.next(dispatcher);

      if (previousState == dispatcherState) {
        //State has not changed
        //Logger.trace(dispatcherState.getClass().getSimpleName() + " has not changed");
        try {
          synchronized (this) {
            wait(100);
          }
        } catch (InterruptedException ex) {
          Logger.trace(ex.getMessage());
        }
      }
      dispatcher.fireStateListeners(dispatcherState.getClass().getSimpleName());
    }

    Logger.trace(getName() + " in state " + dispatcherState.getClass().getSimpleName() + " is Stopping...");

    //Make sure that also the linked locomotive is stopped
    dispatcher.changeLocomotiveVelocity(dispatcher.getLocomotiveBean(), 0);
    dispatcher.fireStateListeners(getName() + " Finished");

    //TODO do the cleanup in in a different way
    dispatcher.clearDepartureIgnoreEventHandlers();
    dispatcher.unRegisterListeners();
    Logger.trace(getName() + " last state " + dispatcherState.getClass().getSimpleName() + " is Finished");
  }

  //Testing
  void manualStep() {
    dispatcherState.execute();
    DispatcherState previousState = this.dispatcherState;
    
    this.dispatcherState = dispatcherState.next(dispatcher);
    if (previousState == dispatcherState) {
      Logger.trace(dispatcherState.getClass().getSimpleName() + " has not changed");
    }
    dispatcher.fireStateListeners("Manual: " + dispatcherState.getClass().getSimpleName());
  }

}
