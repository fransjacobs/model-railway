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

import jcs.commandStation.autopilot.TrainDispatcher;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public abstract class DispatcherState {

  protected LocomotiveBean locomotive;

  protected RouteBean route;

  protected int waitTime;

  protected boolean canAdvanceState;

  protected DispatcherState(LocomotiveBean locomotive) {
    this(locomotive, null);
  }

  protected DispatcherState(LocomotiveBean locomotive, RouteBean route) {
    this.locomotive = locomotive;
    this.route = route;
  }

  /**
   * React on a Halt event
   *
   * @param dispatcher
   */
  public abstract void onHalt(TrainDispatcher dispatcher);

  public abstract void next(TrainDispatcher dispatcher);

  //abstract void prev(TrainDispatcher dispatcher);

  public abstract boolean performAction();

  public LocomotiveBean getLocomotive() {
    return locomotive;
  }

  public RouteBean getRoute() {
    return route;
  }

  public int getWaitTime() {
    return waitTime;
  }

  public boolean isCanAdvanceState() {
    return canAdvanceState;
  }

  
  
  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  public void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }

}
