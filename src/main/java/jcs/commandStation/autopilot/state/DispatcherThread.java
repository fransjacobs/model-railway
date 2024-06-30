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
 * In Auto mode every on-track locomotive get a Dispatcher Thread.<br>
 * The Dispatcher Thread doe the driving of the locomotive.<br>
 * The Thread "knows the dispatcherState", (should) knows where the locomotive is.<br>
 * On dispatcherState changes the screen updates are also handled by this Thread.<br>
 *
 * When the Locomotive is stopped, but Autopilot is still in auto mode, the dispatcherState handling<br>
 * is continued, but will jump into Idle instead of Wait after the IN dispatcherState.<br>
 *
 * When the Autopilot is stopped the dispatcherState handling continues, but after the IN dispatcherState the thread is finished.<br>
 *
 * A Locomotive Reset means, force the State Machine into Idle, clean up the route.<br>
 * Locomotive is in the departure block.
 *
 *
 * @author frans
 */
class DispatcherThread extends Thread {

  private final Dispatcher dispatcher;
  private DispatcherState dispatcherState;

  private boolean running = false;
  private boolean stateMachineRunning = false;
  private boolean locomotiveAutomodeOn = false;
  private boolean resetRequested = false;

  private boolean forceStop = false;

  DispatcherThread(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
    this.dispatcherState = new IdleState(dispatcher);
    setName("DT->" + dispatcher.getLocomotiveBean().getName());
    this.running = true;
  }

  boolean isThreadRunning() {
    return this.running;
  }

  boolean isLocomotiveAutomodeOn() {
    return this.locomotiveAutomodeOn;
  }

  //(Re)Start automode for the locomotive
  synchronized void startStateMachine() {
    if(!running) {
      start();
    }
    this.locomotiveAutomodeOn = true;
    this.stateMachineRunning = true;
    notify();
  }

  //Stop automode for the locomotive
  synchronized void stopLocomotiveAutomode() {
    this.locomotiveAutomodeOn = false;
    notify();
  }
  
  //Stop the dispatcher Thread
  synchronized void stopStateMachine() {    
    this.running = false;
    this.stateMachineRunning = false;
    this.locomotiveAutomodeOn = false;
    notify();
  }

//  synchronized void forceStop() {
//    this.running = false;
//    this.forceStop = true;
//    Logger.trace("KILLING " + dispatcher.getName() + " State " + dispatcherState.getName());
//    notify();
//  }

  DispatcherState getDispatcherState() {
    return dispatcherState;
  }

  String getDispatcherStateName() {
    return dispatcherState.getName();
  }

  /**
   * The Locomotive Driving handling.<br>
   * Normal flow is:<br>
   * Idle<br>
   * Prepare<br>
   * Start<br>
   * Enter<br>
   * In<br>
   * Wait<br>
   * After Waiting time Prepare.
   *
   */
  @Override
  public void run() {
    if (JCS.getJcsCommandStation() == null) {
      Logger.error("Can't start due to missing Command Station!");
      this.running = false;
      return;
    } else {
      Logger.trace(getName() + " StateMachine Started. State " + dispatcherState.getName() + "...");
    }

    //boolean waitState = dispatcherState instanceof IdleState || dispatcherState instanceof WaitState;
    //boolean logOnce = true;
    //It forcestop check nodig?
    while (running) {
      //Obtain current dispatcherState
      DispatcherState currentState = dispatcherState;
      //Execute the action for the current dispatcherState      
      currentState.execute();
      //TODO let the execute return the next dispatcherState
      DispatcherState newState = currentState.next(dispatcher);

      if (!locomotiveAutomodeOn && newState instanceof WaitState) {
        //stop (automode) for this locomotice is requested, the WaitState is reached.
        //Force to IdleState.
        newState = new IdleState(dispatcher);
      } else if (!locomotiveAutomodeOn && currentState instanceof IdleState && !(newState instanceof IdleState)) {
        //Still stopped keep current state
        newState = currentState;
      } else {
        if (!this.dispatcher.autoPilot.isAutoModeActive()) {
          //Automode has stopped, so finish the Thread when the Wait dispatcherState is reached
          if (currentState instanceof IdleState || currentState instanceof WaitState) {
            Logger.trace(getName()+" Stopping thread as Autopilot automode is stopped");
            this.running = false;
          }
        }
      }

      if (currentState != newState) {
        //State has changed
        dispatcherState = newState;
        //handle the dispatcherState changes
        dispatcher.fireStateListeners(dispatcherState.getName());
      } else {
        //Check if we need to (force) stop?
        //in the wait dispatcherState the name also hold the time so
        if (dispatcherState instanceof WaitState) {
          dispatcher.fireStateListeners(dispatcherState.getName());
        }

        //No dispatcherState change so lets wait a while
        try {
          synchronized (this) {
            wait(100);
          }
        } catch (InterruptedException ex) {
          Logger.trace(ex.getMessage());
        }
      }

//      if (!running) {
//        Logger.trace(this.getName() + " is ending. Current State: " + dispatcherState.getClass().getSimpleName());
//      }
    }

    Logger.trace(getName() + " in state " + dispatcherState.getClass().getSimpleName() + " is ending...");

    //Make sure that also the linked locomotive is stopped
    dispatcher.changeLocomotiveVelocity(dispatcher.getLocomotiveBean(), 0);

    Logger.trace(getName() + " Send stop to locomotive " + dispatcher.getLocomotiveBean().getName() + "...");

    dispatcher.fireStateListeners(getName() + " Finished");

    Logger.trace(getName() + " State listeners fired...");

    //TODO do the cleanup in in a different way
    if (!forceStop) {
      dispatcher.clearDepartureIgnoreEventHandlers();
      //dispatcher.unRegisterListeners();
    }

    if (!forceStop) {
      Logger.trace(getName() + " last state " + dispatcherState.getClass().getSimpleName() + " is Finished");
    } else {
      Logger.trace(getName() + " last state " + dispatcherState.getClass().getSimpleName() + " KILLED!");
    }

    this.running = false;

  }

}
