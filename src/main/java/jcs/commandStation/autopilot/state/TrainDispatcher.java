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

import jcs.entities.LocomotiveBean;
import jcs.ui.layout.RouteDisplayCallBack;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class TrainDispatcher extends Thread {

  private LocomotiveBean locomotiveBean;

  private DispatcherState dispatcherState;
  private DispatcherState previousState;
  private final RouteDisplayCallBack callback;

  private boolean running;

  public TrainDispatcher(LocomotiveBean locomotiveBean, RouteDisplayCallBack callback) {
    this.locomotiveBean = locomotiveBean;
    this.dispatcherState = new IdleState(locomotiveBean);
    this.callback = callback;

    setName("LDT->" + locomotiveBean.getName());
  }

  public LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }

  public void setLocomotiveBean(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
  }

  public DispatcherState getDispatcherState() {
    return dispatcherState;
  }

  public void setDispatcherState(DispatcherState dispatcherState) {
    this.previousState = this.dispatcherState;
    this.dispatcherState = dispatcherState;
    if (previousState == dispatcherState) {
      Logger.debug("State has not changed. Current state " + dispatcherState.toString());
    } else {
      Logger.debug("State changed to " + dispatcherState.toString());
    }
  }

//  public void previousState() {
//    dispatcherState.prev(this);
//  }

  public void nextState() {

    dispatcherState.next(this);
  }

  public boolean performAction() {
    boolean action = dispatcherState.performAction();

    if (dispatcherState instanceof ReserveRouteState && action) {
      if (callback != null) {
        callback.setSelectRoute(dispatcherState.route);
      }
    }

    if (dispatcherState instanceof RunState && action) {
      if (callback != null) {
        callback.setSelectRoute(dispatcherState.route);

        callback.refresh();
      }
    }

    return action;
  }

  @Override
  public void run() {
    this.running = true;

    while (running) {
      Logger.trace(getName() + " " + getDispatcherState());
      //Perform the action for the current state
      dispatcherState.pause(100);
     
      performAction();

      Logger.trace("dispatcherState.canAdvanceState: "+dispatcherState.canAdvanceState);
      if (dispatcherState.canAdvanceState) {
        nextState();
      } else {
        //Lets wait for 1 s and try again
        dispatcherState.pause(2000);
      }
    }

    Logger.debug(this.getName() + " Finished");
  }

  public void stopRunning() {
    this.running = false;
  }

}
