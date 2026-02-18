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

import jcs.commandStation.automation.AbstractState;
import jcs.commandStation.automation.Dispatcher;
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

  //extends Thread {
  private final Dispatcher dispatcher;

  private AbstractState currentState;

  //private boolean running = false;
  //private boolean enableAutomode = false;
  //private final long threadSleepMillis;
  private StateMachineRunner stateMachineRunner;

//  StateMachine(ThreadGroup parent, Dispatcher dispatcher) {
//    super(parent, "STM->" + dispatcher.getLocomotiveBean().getName().toUpperCase());
//    this.dispatcher = dispatcher;
//    this.dispatcherState = new IdleState();
//  }
  /**
   *
   * @param dispatcher
   * @param initialState
   */
  StateMachine(Dispatcher dispatcher, AbstractState initialState) {
    //super(dispatcher.getThreadGroup(), "STM->" + dispatcher.getLocomotiveBean().getName().toUpperCase());
    this.dispatcher = dispatcher;
    currentState = initialState;
    currentState.onEnter(dispatcher);
    //threadSleepMillis = Long.parseUnsignedLong(System.getProperty("autopilot.thread.wait.millis", "1000"));
  }

  void startStateMachineThread() {
    if (stateMachineRunner == null || !stateMachineRunner.isRunning()) {
      stateMachineRunner = new StateMachineRunner(this);
      stateMachineRunner.start();
    }
  }

  void stopStateMachineThread() {
    if (stateMachineRunner != null && stateMachineRunner.isRunning()) {
      stateMachineRunner.shutdown();
    }
  }

  Dispatcher getDispatcher() {
    return dispatcher;
  }

  AbstractState getCurrentState() {
    return currentState;
  }

  void setCurrentState(AbstractState currentState) {
    this.currentState = currentState;
  }

  public boolean isRunning() {
    return stateMachineRunner != null && stateMachineRunner.isRunning();
  }

  public String getCurrentStateName() {
    return currentState.getName();
  }

  //Reset the statemachine
  void reset() {
    Logger.trace("Reset requested...");
    //resetStateMachine();
  }

  private class StateMachineRunner extends Thread {

    private final StateMachine stateMachine;
    private final long threadSleepMillis;

    private boolean running = false;

    StateMachineRunner(StateMachine stateMachine) {
      super(stateMachine.getDispatcher().getThreadGroup(), "STM->" + stateMachine.getDispatcher().getLocomotiveBean().getName().toUpperCase());
      this.stateMachine = stateMachine;
      threadSleepMillis = Long.parseUnsignedLong(System.getProperty("autopilot.thread.wait.millis", "1000"));
    }

    void shutdown() {
      this.running = false;
    }

    boolean isRunning() {
      return running;
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
      running = true;

      while (running) {
        AbstractState currentState = stateMachine.getCurrentState();
        AbstractState nextState = currentState.execute();

        if (nextState != currentState) {
          currentState.onExit();
          stateMachine.setCurrentState(nextState);
          nextState.onEnter(stateMachine.getDispatcher());
        }

        try {
          Thread.sleep(threadSleepMillis);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
  }

}
