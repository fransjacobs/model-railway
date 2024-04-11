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

/**
 *
 * @author frans
 */
public class TrainDispatcher {

  private LocomotiveBean locomotiveBean;

  private DispatcherState state;

  public TrainDispatcher(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
    this.state = new IdleState(locomotiveBean);
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
    this.state = state;
  }

  public void previousState() {
    state.prev(this);
  }

  public void nextState() {
    state.next(this);
  }

  public void showStatus() {
    state.logState();
  }

}
