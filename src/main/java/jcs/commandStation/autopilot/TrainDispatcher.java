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
package jcs.commandStation.autopilot;

import java.util.LinkedList;
import java.util.List;
import jcs.commandStation.autopilot.state.DispatcherState;
import jcs.commandStation.autopilot.state.IdleState;
import jcs.commandStation.autopilot.state.ReserveRouteState;
import jcs.commandStation.autopilot.state.RunState;
import jcs.commandStation.autopilot.state.StateEventListener;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.TileBean;
import jcs.ui.layout.events.TileEvent;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * The context
 *
 * @author frans
 */
public class TrainDispatcher extends Thread {

  private final LocomotiveBean locomotiveBean;
  private RouteBean routeBean;

  private BlockBean departureBlock;
  private BlockBean destinationBlock;

  private DispatcherState dispatcherState;
  private DispatcherState previousState;
  //private final RouteDisplayCallBack callback;

  private final List<StateEventListener> stateEventListeners;

  private SensorEventListener enterEventListener;
  private SensorEventListener inEventListener;
  
  
  private boolean enterDestinationBlock = false;
  private boolean inDestinationBlock = false;


  private boolean running;

  public TrainDispatcher(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
    this.dispatcherState = new IdleState(this);
    //this.callback = callback;
    this.stateEventListeners = new LinkedList<>();

    setName("LDT->" + locomotiveBean.getName());
  }

  public LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }

  public RouteBean getRouteBean() {
    return routeBean;
  }

  public void setRouteBean(RouteBean routeBean) {
    this.routeBean = routeBean;
  }

  public BlockBean getDepartureBlock() {
    return departureBlock;
  }

  public void setDepartureBlock(BlockBean departureBlock) {
    this.departureBlock = departureBlock;
  }

  public BlockBean getDestinationBlock() {
    return destinationBlock;
  }

  public void setDestinationBlock(BlockBean destinationBlock) {
    this.destinationBlock = destinationBlock;
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
    String s = dispatcherState.toString();
    fireStateListeners(s);
  }

  public SensorEventListener getEnterEventListener() {
    return enterEventListener;
  }

  public void setEnterEventListener(SensorEventListener enterEventListener) {
    this.enterEventListener = enterEventListener;
  }

  public SensorEventListener getInEventListener() {
    return inEventListener;
  }

  public void setInEventListener(SensorEventListener inEventListener) {
    this.inEventListener = inEventListener;
  }

  public synchronized void onEnter() {
    Logger.debug("got an enter event");
    enterDestinationBlock = true;
    //wakeup
    notify();
  }

  public synchronized void onArrival() {
    Logger.debug("got an Arrival event..");
    inDestinationBlock = true;
    //wakeup
    notify();
  }

  public void nextState() {
    dispatcherState.next(this);
  }

  public boolean execute() {
    boolean action = dispatcherState.execute();

    if (dispatcherState instanceof ReserveRouteState && action) {
      //if (callback != null) {
      //callback.setSelectRoute(dispatcherState.getRoute());
      //}
    }

    if (dispatcherState instanceof RunState && action) {
      //if (callback != null) {
      // callback.setSelectRoute(dispatcherState.getRoute());

      //callback.refresh();
      //}
    }

    return action;
  }

  @Override
  public void run() {
    running = true;
    dispatcherState.setRunning(running);

    while (running) {
      Logger.trace(getName() + " " + getDispatcherState());
      //Perform the action for the current state
      //dispatcherState.pause(100);
      execute();

      if (!dispatcherState.isRunning()) {
        Logger.debug("Dispatcher State Maching encountered an error hense stopping");
        this.running = false;
      }

      Logger.trace("dispatcherState.canAdvanceState: " + dispatcherState.canAdvanceToNextState());
      if (dispatcherState.canAdvanceToNextState()) {
        nextState();
      } else {
        //STUB: Lets wait for 2 s and try again
        dispatcherState.pause(2000);
      }
    }

    fireStateListeners(this.getName() + " Finished");
    Logger.debug(this.getName() + " Finished");
  }

  private void fireStateListeners(String s) {
    for (StateEventListener sel : stateEventListeners) {
      sel.onStateChange(s);
    }
  }

  public synchronized void stopRunning() {
    running = false;
    dispatcherState.setRunning(running);
  }

  public boolean isRunning() {
    return running;
  }

  public boolean isEnterDestinationBlock() {
    return enterDestinationBlock;
  }

  public boolean isInDestinationBlock() {
    return inDestinationBlock;
  }
  
  
  

  public void addStateEventListener(StateEventListener listener) {
    stateEventListeners.add(listener);
  }

  public void removeStateEventListener(StateEventListener listener) {
    stateEventListeners.remove(listener);
  }

  public void resetRoute(RouteBean route) {
    List<RouteElementBean> routeElements = route.getRouteElements();
    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      TileEvent tileEvent = new TileEvent(tileId, false);
      TileFactory.fireTileEventListener(tileEvent);
    }
  }

  public void showRoute(RouteBean routeBean) {
    List<RouteElementBean> routeElements = routeBean.getRouteElements();
    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      TileBean.Orientation incomingSide = re.getIncomingOrientation();

      TileEvent tileEvent;
      if (re.isTurnout()) {
        AccessoryBean.AccessoryValue routeState = re.getAccessoryValue();
        tileEvent = new TileEvent(tileId, true, incomingSide, routeState);
      } else {
        tileEvent = new TileEvent(tileId, true, incomingSide);
      }
      TileFactory.fireTileEventListener(tileEvent);
    }
  }

}
