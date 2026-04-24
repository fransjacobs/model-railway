/*
 * Copyright 2026 Frans Jacobs
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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base State
 */
abstract class AbstractState {

  protected final State state;

  protected volatile Dispatcher dispatcher;

  protected AbstractState(State state) {
    this.state = state;
  }

  /**
   * Execute when entering the state, before the execute
   *
   * @param dispatcher
   */
  void onEnter(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  /**
   * The main state execution
   *
   * @return the next state or self when the conditions for a transition are not met.
   */
  abstract AbstractState execute();

  /**
   * Executed when the State is exiting. Use for cleanup etc.
   */
  abstract void onExit();

  /**
   * Indicate whether is is possible to stop the thread or not.
   *
   * @return true when the state machine can be stopped
   */
  abstract boolean canStopLocomotive();

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  /**
   * 
   * @return the current State 
   */
  public State getState() {
    return this.state;
  }

  /**
   *
   * @return the name of the State
   */
  public String getName() {
    return this.state.getName();
  }

  public enum State {
    IDLE("Idle"), PREPROUTE("PrepareRoute"), DEPARTING("Departing"), RUNNING("Running"), APPROACH("Approaching"), BRAKE("Braking"), PASSTHROUGH("PassingThrough"), ARRIVED("Arrived"), PREPNEXTROUTE("PrepareNextRoute"), WAIT("Waiting");

    private final String name;

    private static final Map<String, State> ENUM_MAP;

    State(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }

    static {
      Map<String, State> map = new ConcurrentHashMap<>();
      for (State instance : State.values()) {
        map.put(instance.getName(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static State get(String name) {
      if (name == null) {
        return null;
      }
      return ENUM_MAP.get(name);
    }
  }

}
