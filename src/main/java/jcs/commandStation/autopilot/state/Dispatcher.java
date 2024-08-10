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

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.SensorEventHandler;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.events.TileEvent;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * The Dispatcher is the controlling class during auto mode of one Locomotive<br>
 * When a Locomotive runs a separate stateMachineThread is started which handles all the states.<br>
 *
 */
public class Dispatcher {

  private final LocomotiveBean locomotiveBean;

  final AutoPilot autoPilot;
  private RouteBean routeBean;

  private String departureBlockId;
  private String destinationBlockId;

  private final List<StateEventListener> stateEventListeners;

  private StateMachineThread stateMachineThread;

  public Dispatcher(LocomotiveBean locomotiveBean, AutoPilot autoPilot) {
    this.locomotiveBean = locomotiveBean;
    this.autoPilot = autoPilot;

    this.stateEventListeners = new LinkedList<>();
    this.stateMachineThread = new StateMachineThread(this);
  }

  void startStateMachineThread() {
    this.stateMachineThread.start();
  }

  StateMachineThread getStateMachineThread() {
    return stateMachineThread;
  }

  public Long getId() {
    return this.locomotiveBean.getId();
  }

  public String getName() {
    return this.locomotiveBean.getName();
  }

  public LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }

  RouteBean getRouteBean() {
    return routeBean;
  }

  void setRouteBean(RouteBean routeBean) {
    this.routeBean = routeBean;
    if (routeBean == null) {
      this.departureBlockId = null;
      this.destinationBlockId = null;
    } else {
      this.departureBlockId = routeBean.getFromTileId();
      this.destinationBlockId = routeBean.getToTileId();
    }
  }

  boolean isLocomotiveAutomodeOn() {
    return this.stateMachineThread.isEnableAutomode();
  }

//  public final void startStateMachine() {
//    //thread.runStateMachine(true);
//  }
//  public void stopStateMachine() {
//    //thread.runStateMachine(false);
//  }
  public void startLocomotiveAutomode() {
    //thread.stopLocomotiveAutomode();
    stateMachineThread.setEnableAutomode(true);
  }

  public void stopLocomotiveAutomode() {
    //thread.stopLocomotiveAutomode();
    stateMachineThread.setEnableAutomode(false);
  }

  //@Deprecated
  public void startRunning() {
    if (this.stateMachineThread != null && this.stateMachineThread.isThreadRunning()) {
      return;
    }

    if (this.stateMachineThread == null || !this.stateMachineThread.isAlive()) {
      stateMachineThread = new StateMachineThread(this);
    }

    this.stateMachineThread.start();
//    startStateMachine();
  }

  //@Deprecated
  public void stopRunning() {
    if (stateMachineThread != null && stateMachineThread.isThreadRunning()) {
      stateMachineThread.stopRunningThread();
    }
//    stopLocomotiveAutomode();
  }

  public void forceStopRunning() {
//    if (stateMachineThread != null && stateMachineThread.isThreadRunning()) {
//      this.stateMachineThread.forceStop();
//    }
  }

  void resetDispatcher() {
    this.routeBean = null;
    this.departureBlockId = null;
    this.destinationBlockId = null;
    this.stateEventListeners.clear();
    this.stateMachineThread = new StateMachineThread(this);
  }

  public void reset() {
    Logger.trace("Resetting dispatcher " + getName() + " StateMachine...");
    this.stateMachineThread.reset();
    resetDispatcher();
  }

  ///????
  public boolean isRunning() {
    if (stateMachineThread != null) {
      return stateMachineThread.isThreadRunning();
    } else {
      return false;
    }
  }

  private BlockBean getBlock(String tileId) {
    BlockBean block = PersistenceFactory.getService().getBlockByTileId(tileId);
    return block;
  }

  //Make sure the last copy/ status of the block is represented
  BlockBean getDepartureBlock() {
    if (departureBlockId != null) {
      return PersistenceFactory.getService().getBlockByTileId(departureBlockId);
    } else if (routeBean != null) {
      departureBlockId = routeBean.getFromTileId();
      return PersistenceFactory.getService().getBlockByTileId(departureBlockId);
    } else {
      BlockBean departureBlock = PersistenceFactory.getService().getBlockByLocomotiveId(locomotiveBean.getId());
      this.departureBlockId = departureBlock.getTileId();
      return departureBlock;
    }
  }

  BlockBean getDestinationBlock() {
    if (destinationBlockId != null) {
      return PersistenceFactory.getService().getBlockByTileId(destinationBlockId);
    } else if (routeBean != null) {
      destinationBlockId = routeBean.getToTileId();
      return PersistenceFactory.getService().getBlockByTileId(destinationBlockId);
    } else {
      return null;
    }
  }

  String swapSuffix(String suffix) {
    if ("+".equals(suffix)) {
      return "-";
    } else {
      return "+";
    }
  }

  String getDepartureArrivalSuffix() {
    if (routeBean == null) {
      return null;
    }
    String departureSuffix = routeBean.getFromSuffix();
    return swapSuffix(departureSuffix);
  }

  String getDestinationArrivalSuffix() {
    if (routeBean == null) {
      return null;
    }
    String destinationArrivalSuffix = routeBean.getToSuffix();
    return destinationArrivalSuffix;
  }

  public String getDispatcherState() {
    if (stateMachineThread != null) {
      return stateMachineThread.getState().getClass().getSimpleName();
    } else {
      return "#Idle";
    }
  }

  void registerIgnoreEventHandler(String sensorId) {
    if (!autoPilot.isSensorHandlerRegistered(sensorId)) {
      IgnoreSensorHandler ish = new IgnoreSensorHandler(sensorId, this);
      autoPilot.addHandler(ish, sensorId);
      Logger.trace("Added sensor " + sensorId + " to the ignore handlers");
    } else {
      Logger.trace("Sensor " + sensorId + " is allready ignored");
    }
  }

  synchronized void clearDepartureIgnoreEventHandlers() {
    if (departureBlockId != null) {
      BlockBean departureBlock = this.getDepartureBlock();
      String minSensorId = departureBlock.getMinSensorId();
      autoPilot.removeHandler(minSensorId);
      String plusSensorId = departureBlock.getPlusSensorId();
      autoPilot.removeHandler(plusSensorId);
    }
  }

  synchronized void onIgnoreEvent(SensorEvent event) {
    //Logger.trace("Event for a ignored listener: " + event.getId() + " Changed: " + event.isChanged() + ", active: " + event.getSensorBean().isActive());
  }

  synchronized void switchAccessory(AccessoryBean accessory, AccessoryValue value) {
    JCS.getJcsCommandStation().switchAccessory(accessory, value);
  }

  synchronized void changeLocomotiveVelocity(LocomotiveBean locomotive, int velocity) {
    JCS.getJcsCommandStation().changeLocomotiveSpeed(velocity, locomotive);
    locomotiveBean.setVelocity(velocity);
  }

  synchronized void changeLocomotiveDirection(LocomotiveBean locomotive, Direction newDirection) {
    JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, locomotive);
    locomotiveBean.setDirection(newDirection);
  }

  synchronized void fireStateListeners(String s) {
    for (StateEventListener sel : stateEventListeners) {
      sel.onStateChange(this);
    }
  }

  public void addStateEventListener(StateEventListener listener) {
    stateEventListeners.add(listener);
  }

  public void removeStateEventListener(StateEventListener listener) {
    stateEventListeners.remove(listener);
  }

  public static void resetRoute(RouteBean route) {
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

  private class IgnoreSensorHandler implements SensorEventHandler {

    private final String sensorId;
    private final Dispatcher trainDispatcher;

    IgnoreSensorHandler(String sensorId, Dispatcher trainDispatcher) {
      this.sensorId = sensorId;
      this.trainDispatcher = trainDispatcher;
    }

    @Override
    public void handleEvent(SensorEvent event) {
      if (this.sensorId.equals(event.getId())) {
        this.trainDispatcher.onIgnoreEvent(event);
      }
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(locomotiveBean.getId());
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Dispatcher other = (Dispatcher) obj;
    return Objects.equals(locomotiveBean.getId(), other.locomotiveBean.getId());
  }
}
