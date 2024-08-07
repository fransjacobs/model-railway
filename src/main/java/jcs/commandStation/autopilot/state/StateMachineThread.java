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
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
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
class StateMachineThread extends Thread {

  private final Dispatcher dispatcher;
  private DispatcherState dispatcherState;

  private boolean running = false;

  private boolean enableAutomode = false;
  private boolean resetRequested = false;

  private boolean forceStop = false;

  StateMachineThread(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
    this.dispatcherState = new IdleState();
    setName("DT->" + dispatcher.getLocomotiveBean().getName());
    //this.running = true;
  }

  boolean isThreadRunning() {
    return this.running;
  }

  synchronized void stopRunningThread() {
    this.running = false;
    notify();
  }

  boolean isEnableAutomode() {
    return this.enableAutomode;
  }

  //(Re)Start automode for the locomotive
  synchronized void setEnableAutomode(boolean start) {
    this.enableAutomode = start;
    //this.stateMachineRunning = start;
    if (running) {
      notify();
    }
  }

  //Reset the statemachine
  synchronized void reset() {
    //Switch of running
    this.enableAutomode = false;
    this.resetRequested = true;

    Logger.trace("Reset requested...");
    //Notify the running thread
    if (running) {
      notify();
    }

  }

  DispatcherState getDispatcherState() {
    return dispatcherState;
  }

  String getDispatcherStateName() {
    return dispatcherState.getName();
  }

  void handleState() {
    //Obtain current dispatcherState
    DispatcherState previousState = dispatcherState;
    //Execute the action for the current dispatcherState   
    dispatcherState = dispatcherState.execute(dispatcher);

    if (!this.dispatcher.autoPilot.isAutoModeActive()) {
      //Automode has stopped, let the Thread finish when WaitState is reached
      if (dispatcherState instanceof IdleState || dispatcherState instanceof WaitState) {
        Logger.trace(getName() + " Stopping thread as Autopilot automode is stopped");
        this.running = false;
      }

//      if (inLoop) {
//        //No dispatcherState change so lets wait a while
//        try {
//          synchronized (this) {
//            wait(100);
//          }
//        } catch (InterruptedException ex) {
//          Logger.trace(ex.getMessage());
//        }
//      }
    }

    if (previousState != dispatcherState || dispatcherState instanceof WaitState) {
      //handle the dispatcherState changes
      dispatcher.fireStateListeners(dispatcherState.getName());
    }
  }

  //Reset the statemachine
  void resetStateMachine() {
    this.enableAutomode = false;
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    dispatcher.changeLocomotiveVelocity(locomotive, 0);
    Logger.trace("Stoppped " + locomotive.getName() + "...");

    //Remove the sensor listeners
    DispatcherState currentState = dispatcherState;
    if (currentState instanceof SensorEventListener sensorEventListener) {
      Logger.trace("Removing " + sensorEventListener.toString() + " as Sensor Listener...");
      JCS.getJcsCommandStation().removeSensorEventListener(sensorEventListener);
    }

    dispatcher.clearDepartureIgnoreEventHandlers();

    //Restore the Departure block state
    BlockBean departureBlock = dispatcher.getDepartureBlock();
    departureBlock.setBlockState(BlockBean.BlockState.OCCUPIED);

    //Clear the destination block
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    destinationBlock.setBlockState(BlockBean.BlockState.FREE);
    destinationBlock.setLocomotive(null);
    destinationBlock.setArrivalSuffix(null);
    destinationBlock.setReverseArrival(false);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    //reset the route
    RouteBean route = dispatcher.getRouteBean();
    route.setLocked(false);
    Dispatcher.resetRoute(route);

    PersistenceFactory.getService().persist(route);
    dispatcher.setRouteBean(null);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showBlockState(destinationBlock);

    dispatcherState = new IdleState();

    this.resetRequested = false;
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
      this.running = true;
      Logger.trace(getName() + " Started. State " + dispatcherState.getName() + "...");
    }

    while (running) {
      if (resetRequested) {
        resetStateMachine();
      } else {
        handleState();
      }
    }

    Logger.trace(getName() + " in state " + dispatcherState.getClass().getSimpleName() + " is ending...");

    if (forceStop) {
      //Make sure that also the linked locomotive is stopped
      dispatcher.changeLocomotiveVelocity(dispatcher.getLocomotiveBean(), 0);
      Logger.trace(getName() + " Send stop to locomotive " + dispatcher.getLocomotiveBean().getName() + "...");

      //Remove the sensor listeners if applicable
      DispatcherState currentState = dispatcherState;
      if (currentState instanceof SensorEventListener sensorEventListener) {
        Logger.trace("Removing " + currentState.toString() + " as Sensor Listener...");
        JCS.getJcsCommandStation().removeSensorEventListener(sensorEventListener);
      }

      dispatcher.clearDepartureIgnoreEventHandlers();
    }

    dispatcher.fireStateListeners(getName() + " Finished");
    Logger.trace(getName() + " State listeners fired...");

    if (!forceStop) {
      Logger.trace(getName() + " last state " + dispatcherState.getClass().getSimpleName() + " is Finished");
    } else {
      Logger.trace(getName() + " last state " + dispatcherState.getClass().getSimpleName() + " KILLED!");
    }

    this.running = false;
  }

}
