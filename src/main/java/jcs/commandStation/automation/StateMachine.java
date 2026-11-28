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
package jcs.commandStation.automation;

import static jcs.commandStation.automation.RailController.TAG;
import jcs.entities.BlockBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * In Auto mode every on-track locomotive get a Dispatcher Thread.<br>
 * The Dispatcher Thread performs the driving of the locomotive.<br>
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
 */
class StateMachine {

  private final Dispatcher dispatcher;
  private volatile AbstractState currentState;
  private volatile StateMachineRunner stateMachineRunner;
  private volatile boolean requestStop = false;
  private volatile boolean suspendedForConnectionLoss = false;

  /**
   *
   * @param dispatcher
   * @param initialState
   */
  StateMachine(Dispatcher dispatcher, AbstractState initialState) {
    this.dispatcher = dispatcher;
    currentState = initialState;
    currentState.onEnter(dispatcher);
  }

  synchronized void startStateMachineThread() {
    if (suspendedForConnectionLoss || !(currentState instanceof IdleState)) {
      return;
    }
    if (stateMachineRunner == null || !stateMachineRunner.isAlive()) {
      requestStop = false;
      stateMachineRunner = new StateMachineRunner(this);
      stateMachineRunner.start();
    }
  }

  /**
   * Emergency suspension: retain the current state/route for reconciliation after reconnection. Do not run the normal on-exit speed command.
   */
  void suspendForConnectionLoss() {
    suspendedForConnectionLoss = true;
    StateMachineRunner runner = stateMachineRunner;
    if (runner != null) {
      runner.shutdown();
      runner.wakeUp();
    }
  }

  void stopStateMachineThread() {
    //A stop is requested. The state machine can only stop in certain states.
    //If  the statemachine is not in the right state regard this as a request to stop,
    //when the state machine reaches a state where it can stop it will stop.
    requestStop = true;
    if (stateMachineRunner != null) {
      stateMachineRunner.wakeUp();
    }
  }

  void wakeUp() {
    if (stateMachineRunner != null) {
      stateMachineRunner.wakeUp();
    }
  }

  Dispatcher getDispatcher() {
    return dispatcher;
  }

  AbstractState getCurrentState() {
    return currentState;
  }

  boolean isRunning() {
    return !suspendedForConnectionLoss
            && stateMachineRunner != null && stateMachineRunner.isAlive();
  }

  boolean isRequestStop() {
    return requestStop;
  }

  boolean isSuspendedForConnectionLoss() {
    return suspendedForConnectionLoss;
  }

  boolean isThreadEnabled() {
    return stateMachineRunner != null;
  }

  public String getCurrentStateName() {
    return currentState.getName();
  }

  //Reset the statemachine
  synchronized void reset() {
    Logger.tag(TAG).trace("Resetting in state " + currentState.getName());
    dispatcher.changeLocomotiveVelocity(0);

    // Stop runner FIRST and wait for it
    StateMachineRunner runner = stateMachineRunner;
    if (runner != null && runner.isAlive()) {
      runner.shutdown();
      runner.wakeUp();  // Wake it so it can exit
      try {
        runner.join(5000);  // Wait for thread to finish
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      if (runner.isAlive()) {
        Logger.tag(TAG).warn("Reset refused: runner has not terminated");
        return;
      }
    }
    dispatcher.changeLocomotiveVelocity(0);
    Logger.tag(TAG).trace(dispatcher.getName() + " StateMachineRunner stopped in state " + currentState.getName());
    currentState.onExit();

    //Remove listeners is still registered....
    Integer enterSensorId = dispatcher.getEnterSensorId();
    Integer inSensorId = dispatcher.getInSensorId();
    Integer occupationSensorId = dispatcher.getOccupationSensorId();
    Integer exitSensorId = dispatcher.getExitSensorId();

    if (enterSensorId != null && dispatcher.getSensorMonitor().isSensorRegisteredWithCallback(enterSensorId)) {
      Logger.tag(TAG).trace(dispatcher.getName() + " Unsubscribe enterSensorId " + enterSensorId);
      if (currentState instanceof SensorEventCallback sensorEventCallback) {
        dispatcher.getSensorMonitor().unsubscribe(enterSensorId, sensorEventCallback);
      } else {
        dispatcher.getSensorMonitor().unsubscribe(enterSensorId, null);
      }
    }

    if (inSensorId != null && dispatcher.getSensorMonitor().isSensorRegisteredWithCallback(inSensorId)) {
      Logger.tag(TAG).trace(dispatcher.getName() + " Unsubscribe inSensorId " + inSensorId);
      if (currentState instanceof SensorEventCallback sensorEventCallback) {
        dispatcher.getSensorMonitor().unsubscribe(inSensorId, sensorEventCallback);
      } else {
        dispatcher.getSensorMonitor().unsubscribe(inSensorId, null);
      }
    }

    //The occupationSensorId and exitSensorId might be subscribe
    if (occupationSensorId != null && dispatcher.getSensorMonitor().isSensorRegisteredWithoutCallback(occupationSensorId)) {
      Logger.tag(TAG).trace(dispatcher.getName() + " Unsubscribe occupationSensorId " + occupationSensorId);
      dispatcher.getSensorMonitor().unsubscribe(occupationSensorId, null);
    }

    if (exitSensorId != null && dispatcher.getSensorMonitor().isSensorRegisteredWithoutCallback(exitSensorId)) {
      Logger.tag(TAG).trace(dispatcher.getName() + " Unsubscribe exitSensorId " + exitSensorId);
      dispatcher.getSensorMonitor().unsubscribe(exitSensorId, null);
    }

    // Stop dispatcher and set state to Idle
    String oldState = currentState.getName();
    dispatcher.setLocomotiveStarted(false);
    requestStop = false;

    dispatcher.setEnterSensorId(null);
    dispatcher.setInSensorId(null);
    dispatcher.setExitSensorId(null);
    dispatcher.setOccupationSensorId(null);

    BlockBean destination = dispatcher.getDestinationBlock();
    if (destination != null) {
      destination.setLocomotive(null);
      destination.setBlockState(BlockBean.BlockState.FREE);
      destination.setArrivalSuffix(null);
      PersistenceFactory.getService().persist(destination);

      dispatcher.showBlockState(destination);
    }

    BlockBean departure = dispatcher.getDepartureBlock();
    departure.setBlockState(BlockBean.BlockState.OCCUPIED);

    RouteBean nextRoute = dispatcher.getNextRouteBean();
    if (nextRoute != null) {
      BlockBean nextDestination = dispatcher.getNextDestinationBlock();
      if (nextDestination != null && dispatcher.getLocomotiveId().equals(nextDestination.getLocomotiveId())) {
        nextDestination.setLocomotive(null);
        nextDestination.setBlockState(BlockBean.BlockState.FREE);
        nextDestination.setArrivalSuffix(null);
        PersistenceFactory.getService().persist(nextDestination);
        dispatcher.showBlockState(nextDestination);
      }
      nextRoute.setLocked(false);
      PersistenceFactory.getService().persist(nextRoute);
      dispatcher.resetRoute(nextRoute);
    }

    RouteBean route = dispatcher.getRouteBean();
    if (route != null) {
      route.setLocked(false);
      departure.setDepartureSuffix(route.getFromSuffix());
      PersistenceFactory.getService().persist(route);
      dispatcher.resetRoute(route);
    }

    PersistenceFactory.getService().persist(departure);
    dispatcher.showBlockState(departure);

    dispatcher.setNextRouteBean(null);
    dispatcher.setRouteBean(null);
    dispatcher.setDestinationBlockId(null);

    currentState = new IdleState();
    currentState.onEnter(dispatcher);
    stateMachineRunner = null;
    dispatcher.fireStateListeners(oldState, currentState.getName(), null);
    Logger.debug(dispatcher.getName() + " has been Reset");
  }

  private void transitionTo(AbstractState nextState) {
    String oldState = currentState.getName();
    currentState.onExit();
    currentState = nextState;
    nextState.onEnter(dispatcher);
    dispatcher.fireStateListeners(oldState, nextState.getName(), null);
  }

  private boolean stopAtSafeState() {
    if ((requestStop || !dispatcher.getRailController().isAutoModeActive())
            && currentState.canStopLocomotive()) {
      if (!(currentState instanceof IdleState)) {
        transitionTo(new IdleState());
      }
      return true;
    }
    return false;
  }

  boolean executeState() {
    AbstractState previous = currentState;
    if (stopAtSafeState()) {
      return previous != currentState;
    }
    AbstractState nextState = currentState.execute();
    if (nextState != currentState) {
      if ((requestStop || !dispatcher.getRailController().isAutoModeActive())
              && nextState.canStopLocomotive()) {
        nextState = new IdleState();
      }
      transitionTo(nextState);
      return true;
    }
    return false;
  }

  private class StateMachineRunner extends Thread {

    private final Object lock = new Object();
    private final StateMachine stateMachine;
    private final long threadSleepMillis;

    private volatile boolean running = true;

    StateMachineRunner(StateMachine stateMachine) {
      super(stateMachine.getDispatcher().getThreadGroup(), "STM->" + stateMachine.getDispatcher().getLocomotiveBean().getName().toUpperCase());
      this.stateMachine = stateMachine;
      threadSleepMillis = Long.parseUnsignedLong(System.getProperty("autopilot.thread.wait.millis", "2000"));
    }

    void shutdown() {
      this.running = false;
    }

    boolean isRunning() {
      return running;
    }

    @Override
    public void run() {
      try {
        while (running && !suspendedForConnectionLoss) {

          if (Logger.isTraceEnabled() && 1 == 2) {
            BlockBean departureBlock = dispatcher.getDepartureBlock();
            BlockBean destinationBlock = dispatcher.getDestinationBlock();
            BlockBean nextDestinationBlock = dispatcher.getNextDestinationBlock();
            RouteBean route = dispatcher.getRouteBean();
            RouteBean nextRoute = dispatcher.getNextRouteBean();

            StringBuilder sb = new StringBuilder();

            if (departureBlock != null) {
              sb.append("Departure: ");
              sb.append(departureBlock.getId());
            }
            if (destinationBlock != null) {
              sb.append("-> Destination: ");
              sb.append(destinationBlock.getId());
            }
            if (nextDestinationBlock != null) {
              sb.append("--> NextDestination: ");
              sb.append(nextDestinationBlock.getId());
            }
            if (route != null) {
              sb.append(" -Route: ");
              sb.append(route.getId());
            }
            if (nextRoute != null) {
              sb.append(" ->NextRoute: ");
              sb.append(nextRoute.getId());
            }
            if (departureBlock != null && departureBlock.getLocomotive() != null) {
              sb.append(" Loc Dir: ");
              sb.append(departureBlock.getLocomotive().getDirection().toString());
            }

            Logger.trace(">>" + sb.toString());
          }

          // The connection can disappear while executeState() is working.
          if (suspendedForConnectionLoss) {
            break;
          }
          boolean stateChanged = stateMachine.executeState();
          if (suspendedForConnectionLoss) {
            break;
          }

          if (!running || stopAtSafeState()) {
            break;
          }

          if (!stateChanged) {
            try {
              synchronized (lock) {
                lock.wait(threadSleepMillis);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              break;
            }

          }
        }

      } finally {
        // Do not send a speed command after the command station is lost.
        // Its physical locomotive state is unknown until communication resumes.
        if (!suspendedForConnectionLoss) {
          dispatcher.changeLocomotiveVelocity(0);
        }

        dispatcher.locomotiveStarted = false;
        running = false;
        Logger.tag(TAG).debug("StateMachineThread {} finished...", stateMachine.getDispatcher().getName());
      }
    }

    void wakeUp() {
      synchronized (lock) {
        lock.notify();
      }
    }
  }

}
