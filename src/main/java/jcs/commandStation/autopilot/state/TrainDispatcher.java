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

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.SensorEventHandler;
import jcs.commandStation.events.SensorEvent;
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
  private final AutoPilot autoPilot;
  private RouteBean routeBean;

  private BlockBean departureBlock;
  private BlockBean destinationBlock;

  private DispatcherState dispatcherState;
  private DispatcherState previousState;

  private final List<StateEventListener> stateEventListeners;

  private final List<NullSensorHandler> nullSensorEventHandlers;
  private EnterSensorHandler enterHandler;
  private ArrivalSensorHandler arrivalHandler;

  private boolean enterDestinationBlock = false;
  private boolean inDestinationBlock = false;
  private boolean swapLocomotiveDirection = false;

  private boolean running;

  public TrainDispatcher(LocomotiveBean locomotiveBean, AutoPilot autoPilot) {
    this.locomotiveBean = locomotiveBean;
    this.autoPilot = autoPilot;
    this.nullSensorEventHandlers = new ArrayList<>();

    this.dispatcherState = new IdleState(this);
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

  void setDepartureBlock(BlockBean departureBlock) {
    this.departureBlock = departureBlock;
  }

  public BlockBean getDestinationBlock() {
    return destinationBlock;
  }

  void setDestinationBlock(BlockBean destinationBlock) {
    this.destinationBlock = destinationBlock;
  }

  public boolean isSwapLocomotiveDirection() {
    return swapLocomotiveDirection;
  }

  void setSwapLocomotiveDirection(boolean swapLocomotiveDirection) {
    this.swapLocomotiveDirection = swapLocomotiveDirection;
  }

  public DispatcherState getDispatcherState() {
    return dispatcherState;
  }

  void setDispatcherState(DispatcherState dispatcherState) {
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

  void registerArrivalHandler(String sensorId) {
    ArrivalSensorHandler ash = new ArrivalSensorHandler(sensorId, this);
    this.arrivalHandler = ash;
    this.autoPilot.addHandler(ash, sensorId);
  }

  void registerEnterHandler(String sensorId) {
    EnterSensorHandler esh = new EnterSensorHandler(sensorId, this);
    this.enterHandler = esh;
    this.autoPilot.addHandler(esh, sensorId);
  }

  void registerNullEventHandler(String sensorId) {
    NullSensorHandler nsh = new NullSensorHandler(sensorId, this);
    this.nullSensorEventHandlers.add(nsh);
    this.autoPilot.addHandler(nsh, sensorId);
  }

  void resetAllRouteEventHandlers() {
    for (NullSensorHandler nseh : nullSensorEventHandlers) {
      autoPilot.removeHandler(nseh.sensorId);
    }
    autoPilot.removeHandler(this.arrivalHandler.sensorId);
    autoPilot.removeHandler(this.enterHandler.sensorId);

    enterDestinationBlock = false;
    inDestinationBlock = false;
  }

  public synchronized void onEnter(SensorEvent event) {
    Logger.debug("got an enter event");
    enterDestinationBlock = true;
    //wakeup
    notify();
  }

  public synchronized void onArrival(SensorEvent event) {
    Logger.debug("got an Arrival event..");
    inDestinationBlock = true;
    //wakeup
    notify();
  }

  String getEnterSensorId() {
    if (this.enterHandler != null) {
      return this.enterHandler.sensorId;
    } else {
      return null;
    }
  }

  String getInSensorId() {
    if (this.arrivalHandler.sensorId != null) {
      return this.arrivalHandler.sensorId;
    } else {
      return null;
    }
  }

  public synchronized void onNullEvent(SensorEvent event) {
    Logger.debug("got an event from a inhibited listener: " + event.getId() + " Changed: " + event.isChanged() + ", active: " + event.getSensorBean().isActive());
  }

  void nextState() {
//    if(dispatcherState instanceof RunState) {
//      //Run state can only be advanced when the arrival (enter) event has occurred
//    }

    dispatcherState.next(this);
  }

  void execute() {
    dispatcherState.execute();
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

    fireStateListeners(getName() + " Finished");
    Logger.debug(getName() + " Finished");
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

  void addStateEventListener(StateEventListener listener) {
    stateEventListeners.add(listener);
  }

  void removeStateEventListener(StateEventListener listener) {
    stateEventListeners.remove(listener);
  }

  void resetRoute(RouteBean route) {
    List<RouteElementBean> routeElements = route.getRouteElements();
    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      TileEvent tileEvent = new TileEvent(tileId, false);
      TileFactory.fireTileEventListener(tileEvent);
    }
  }

  void showBlockState(BlockBean blockBean) {
    Logger.trace("Show block " + blockBean);
    TileEvent tileEvent = new TileEvent(blockBean);
    TileFactory.fireTileEventListener(tileEvent);
  }

  void showRoute(RouteBean routeBean, Color routeColor) {
    Logger.trace("Show route " + routeBean.toLogString());
    List<RouteElementBean> routeElements = routeBean.getRouteElements();
    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      TileBean.Orientation incomingSide = re.getIncomingOrientation();

      TileEvent tileEvent;
      if (re.isTurnout()) {
        AccessoryBean.AccessoryValue routeState = re.getAccessoryValue();
        tileEvent = new TileEvent(tileId, true, incomingSide, routeState, routeColor);
      } else {
        tileEvent = new TileEvent(tileId, true, incomingSide, routeColor);
      }
      TileFactory.fireTileEventListener(tileEvent);
    }
  }

  private class NullSensorHandler implements SensorEventHandler {

    private final String sensorId;
    private final TrainDispatcher trainDispatcher;

    NullSensorHandler(String sensorId, TrainDispatcher trainDispatcher) {
      this.sensorId = sensorId;
      this.trainDispatcher = trainDispatcher;
    }

    @Override
    public void handleEvent(SensorEvent event) {
      this.trainDispatcher.onNullEvent(event);
    }
  }

  private class EnterSensorHandler implements SensorEventHandler {

    private final String sensorId;
    private final TrainDispatcher trainDispatcher;

    EnterSensorHandler(String sensorId, TrainDispatcher trainDispatcher) {
      this.sensorId = sensorId;
      this.trainDispatcher = trainDispatcher;
    }

    @Override
    public void handleEvent(SensorEvent event) {
      trainDispatcher.onEnter(event);
    }
  }

  private class ArrivalSensorHandler implements SensorEventHandler {

    private final String sensorId;
    private final TrainDispatcher trainDispatcher;

    ArrivalSensorHandler(String sensorId, TrainDispatcher trainDispatcher) {
      this.sensorId = sensorId;
      this.trainDispatcher = trainDispatcher;
    }

    @Override
    public void handleEvent(SensorEvent event) {
      trainDispatcher.onArrival(event);
    }
  }

}
