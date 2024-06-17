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
  private boolean forceStop = false;

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
    boolean waitState = dispatcherState instanceof IdleState || dispatcherState instanceof WaitState;
    return this.running || !waitState;
  }

  synchronized void stopRunning() {
    this.running = false;

    boolean waitState = dispatcherState instanceof IdleState || dispatcherState instanceof WaitState;

    Logger.trace("Stopping " + dispatcher.getName() + " Reached waitState " + waitState);
    notify();
  }

  synchronized void forceStopRunning() {
    this.running = false;
    forceStop = true;
    Logger.trace("KILLING " + dispatcher.getName());
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

    boolean waitState = dispatcherState instanceof IdleState || dispatcherState instanceof WaitState;

    boolean logOnce = true;
    while ((running || !waitState) && !forceStop) {
      waitState = dispatcherState instanceof IdleState || dispatcherState instanceof WaitState;

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
      } else {
        waitState = dispatcherState instanceof IdleState || dispatcherState instanceof WaitState;
      }
      dispatcher.fireStateListeners(dispatcherState.getClass().getSimpleName());
      
      if (!running && logOnce) {
        Logger.trace(this.getName() + " is ending. Current State: " + dispatcherState.getClass().getSimpleName());
        logOnce = false;
      }
    }

    Logger.trace(getName() + " in state " + dispatcherState.getClass().getSimpleName() + " is Stopping...");

    //Make sure that also the linked locomotive is stopped
    dispatcher.changeLocomotiveVelocity(dispatcher.getLocomotiveBean(), 0);
    dispatcher.fireStateListeners(getName() + " Finished");

    //TODO do the cleanup in in a different way
    dispatcher.clearDepartureIgnoreEventHandlers();
    //dispatcher.unRegisterListeners();
    if (!forceStop) {
      Logger.trace(getName() + " last state " + dispatcherState.getClass().getSimpleName() + " is Finished");
    } else {
      Logger.trace(getName() + " last state " + dispatcherState.getClass().getSimpleName() + " KILLED!");

    }
  }

}
