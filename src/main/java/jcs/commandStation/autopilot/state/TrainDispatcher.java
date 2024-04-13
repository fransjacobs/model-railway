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
public class TrainDispatcher {

  private LocomotiveBean locomotiveBean;

  private DispatcherState state;
  private DispatcherState previousState;
  private final RouteDisplayCallBack callback;

  private String name;

  public TrainDispatcher(LocomotiveBean locomotiveBean, RouteDisplayCallBack callback) {
    this.locomotiveBean = locomotiveBean;
    this.state = new IdleState(locomotiveBean);
    this.callback = callback;

    this.name = "DP->" + locomotiveBean.getName();
  }

  public LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }

  public void setLocomotiveBean(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
  }

  public DispatcherState getState() {
    return state;
  }

  public void setState(DispatcherState state) {
    this.previousState = this.state;
    this.state = state;
    if (previousState == state) {
      Logger.debug("State has not changed. Current state " + state.toString());
    } else {
      Logger.debug("State changed to " + state.toString());
    }
  }

  public void previousState() {
    state.prev(this);
  }

  public void nextState() {
    state.next(this);
  }

  public boolean performAction() {
    boolean action = state.performAction();

    if (state instanceof ReserveRouteState && action) {
      if (callback != null) {
        callback.setSelectRoute(state.route);
      }
    }

    if (state instanceof RunState && action) {
      if (callback != null) {
        callback.setSelectRoute(state.route);
        
        callback.refresh();
      }
    }

    return action;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
