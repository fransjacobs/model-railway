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
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.SensorEventHandler;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
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
 * The context
 *
 * @author frans
 */
public class TrainDispatcher extends Thread {
  
  private final LocomotiveBean locomotiveBean;
  final AutoPilot autoPilot;
  private RouteBean routeBean;
  
  private BlockBean departureBlock;
  private BlockBean destinationBlock;
  
  private DispatcherState dispatcherState;
  //private DispatcherState previousState;
  
  private final List<StateEventListener> stateEventListeners;
  
  private final List<IgnoreSensorHandler> ignoreSensorEventHandlers;
  private EnterSensorHandler enterHandler;
  private InSensorHandler inHandler;
  
  private boolean enterDestinationBlock = false;
  private boolean inDestinationBlock = false;

  private LocomotiveVelocityListener locomotiveVelocityListener;
  private LocomotiveDirectionChangeListener locomotiveDirectionChangeListener;
  
  public TrainDispatcher(LocomotiveBean locomotiveBean, AutoPilot autoPilot) {
    this.locomotiveBean = locomotiveBean;
    this.autoPilot = autoPilot;
    this.ignoreSensorEventHandlers = new ArrayList<>();
    
    this.dispatcherState = new IdleState(this, false);
    this.stateEventListeners = new LinkedList<>();
    
    setName("LDT->" + locomotiveBean.getName());
    initializeListeners();
  }
  
  private void initializeListeners() {
    locomotiveVelocityListener = new LocomotiveVelocityListener(this);
    JCS.getJcsCommandStation().addLocomotiveSpeedEventListener(locomotiveVelocityListener);
    
    locomotiveDirectionChangeListener = new LocomotiveDirectionChangeListener(this);
    JCS.getJcsCommandStation().addLocomotiveDirectionEventListener(locomotiveDirectionChangeListener);
  }
  
  LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }
  
  RouteBean getRouteBean() {
    return routeBean;
  }
  
  void setRouteBean(RouteBean routeBean) {
    if (routeBean == null) {
      this.departureBlock = null;
      this.destinationBlock = null;
    }
    this.routeBean = routeBean;
  }
  
  BlockBean getDepartureBlock() {
    if (routeBean != null) {
      String departureTileId = routeBean.getFromTileId();
      BlockBean blockBean = PersistenceFactory.getService().getBlockByTileId(departureTileId);
      departureBlock = blockBean;
    }
    //Or via the block the locomotive is in
    if (departureBlock == null) {
      BlockBean blockBean = PersistenceFactory.getService().getBlockByLocomotiveId(locomotiveBean.getId());
      departureBlock = blockBean;
    }
    return departureBlock;
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
    String departureSuffix = this.routeBean.getFromSuffix();
    return swapSuffix(departureSuffix);
  }
  
  String getDestinationArrivalSuffix() {
    if (this.routeBean == null) {
      return null;
    }
    String destinationArrivalSuffix = this.routeBean.getToSuffix();
    return destinationArrivalSuffix;
  }
  
  BlockBean getDestinationBlock() {
    if (routeBean != null) {
      String destinationTileId = routeBean.getToTileId();
      BlockBean blockBean = PersistenceFactory.getService().getBlockByTileId(destinationTileId);
      destinationBlock = blockBean;
    }
    return destinationBlock;
  }
  
  public String getDispatcherState() {
    return dispatcherState.getClass().getSimpleName();
  }
  
  synchronized void setDispatcherState(DispatcherState dispatcherState) {
    DispatcherState previousState = this.dispatcherState;
    this.dispatcherState = dispatcherState;
    
    if (previousState == dispatcherState) {
      Logger.trace("State has not changed. Current state " + dispatcherState.toString());
    } else {
      Logger.debug("State changed to " + dispatcherState.toString());
    }
    String s = dispatcherState.toString();
    fireStateListeners(s);
  }
  
  void registerInHandler(String sensorId) {
    InSensorHandler ash = new InSensorHandler(sensorId, this);
    this.inHandler = ash;
    this.autoPilot.addHandler(ash, sensorId);
  }
  
  void registerEnterHandler(String sensorId) {
    EnterSensorHandler esh = new EnterSensorHandler(sensorId, this);
    this.enterHandler = esh;
    this.autoPilot.addHandler(esh, sensorId);
  }
  
  void registerIgnoreEventHandler(String sensorId) {
    IgnoreSensorHandler nsh = new IgnoreSensorHandler(sensorId, this);
    this.ignoreSensorEventHandlers.add(nsh);
    this.autoPilot.addHandler(nsh, sensorId);
  }
  
  void clearIgnoreEventHandlers() {
    for (IgnoreSensorHandler nseh : ignoreSensorEventHandlers) {
      autoPilot.removeHandler(nseh.sensorId);
    }
    Logger.trace("Enter sensor: " + getEnterSensorId() + " In sensor: " + getInSensorId());
  }
  
  synchronized void clearRouteEventHandlers() {
    for (IgnoreSensorHandler nseh : ignoreSensorEventHandlers) {
      autoPilot.removeHandler(nseh.sensorId);
    }
    autoPilot.removeHandler(this.inHandler.sensorId);
    autoPilot.removeHandler(this.enterHandler.sensorId);
    
    enterDestinationBlock = false;
    inDestinationBlock = false;
  }
  
  synchronized void onEnter(SensorEvent event) {
    Logger.debug("got an enter event");
    enterDestinationBlock = true;
    //wakeup
    notify();
  }
  
  synchronized void onArrival(SensorEvent event) {
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
    if (this.inHandler.sensorId != null) {
      return this.inHandler.sensorId;
    } else {
      return null;
    }
  }
  
  synchronized void onNullEvent(SensorEvent event) {
    Logger.debug("got an event from a inhibited listener: " + event.getId() + " Changed: " + event.isChanged() + ", active: " + event.getSensorBean().isActive());
  }
  
  void nextState() {
    dispatcherState.next(this);
  }
  
  void execute() {
    dispatcherState.execute();
  }
  
  synchronized void switchAccessory(AccessoryBean accessory, AccessoryValue value) {
    JCS.getJcsCommandStation().switchAccessory(accessory, value);
  }
  
  synchronized void changeLocomotiveVelocity(LocomotiveBean locomotive, int velocity) {
    JCS.getJcsCommandStation().changeLocomotiveSpeed(velocity, locomotive);
  }
  
  synchronized void changeLocomotiveDirection(LocomotiveBean locomotive, Direction newDirection) {
    JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, locomotive);
  }
  
  @Override
  public void run() {
    dispatcherState.setRunning(true);
    Logger.trace(getName() + " " + getDispatcherState() + " Started...");
    
    while (dispatcherState.isRunning()) {
      //Perform the action for the current state
      execute();
      
      if (!dispatcherState.isRunning()) {
        Logger.debug("Dispatcher State Maching encountered an error hence stopping");
      }

      //Logger.trace("Can AdvanceState: " + dispatcherState.canAdvanceToNextState());
      if (dispatcherState.canAdvanceToNextState()) {
        nextState();
      } else {
        try {
          synchronized (this) {
            wait(1000);
          }
        } catch (InterruptedException ex) {
          Logger.trace(ex.getMessage());
        }
      }
    }
    
    fireStateListeners(getName() + " Finished");
    Logger.trace(getName() + " " + getDispatcherState() + " Finished");
  }
  
  void fireStateListeners(String s) {
    for (StateEventListener sel : stateEventListeners) {
      sel.onStateChange(s);
    }
  }
  
  public void stopRunning() {
    dispatcherState.setRunning(false);
  }
  
  public boolean isRunning() {
    return dispatcherState.isRunning();
  }
  
  boolean isEnterDestinationBlock() {
    return enterDestinationBlock;
  }
  
  boolean isInDestinationBlock() {
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
  
  private class IgnoreSensorHandler implements SensorEventHandler {
    
    private final String sensorId;
    private final TrainDispatcher trainDispatcher;
    
    IgnoreSensorHandler(String sensorId, TrainDispatcher trainDispatcher) {
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
  
  private class InSensorHandler implements SensorEventHandler {
    
    private final String sensorId;
    private final TrainDispatcher trainDispatcher;
    
    InSensorHandler(String sensorId, TrainDispatcher trainDispatcher) {
      this.sensorId = sensorId;
      this.trainDispatcher = trainDispatcher;
    }
    
    @Override
    public void handleEvent(SensorEvent event) {
      trainDispatcher.onArrival(event);
    }
  }
  
  private class LocomotiveVelocityListener implements LocomotiveSpeedEventListener {
    
    private final TrainDispatcher trainDispatcher;
    
    LocomotiveVelocityListener(TrainDispatcher trainDispatcher) {
      this.trainDispatcher = trainDispatcher;
    }
    
    @Override
    public void onSpeedChange(LocomotiveSpeedEvent velocityEvent) {
      if (velocityEvent.isEventFor(this.trainDispatcher.getLocomotiveBean())) {
        this.trainDispatcher.getLocomotiveBean().setVelocity(velocityEvent.getVelocity());
        Logger.trace("Updated velocity to " + velocityEvent.getVelocity() + " of " + this.trainDispatcher.getLocomotiveBean().getName());
      }
    }
  }
  
  private class LocomotiveDirectionChangeListener implements LocomotiveDirectionEventListener {
    
    private final TrainDispatcher trainDispatcher;
    
    LocomotiveDirectionChangeListener(TrainDispatcher trainDispatcher) {
      this.trainDispatcher = trainDispatcher;
    }
    
    @Override
    public void onDirectionChange(LocomotiveDirectionEvent directionEvent) {
      if (directionEvent.isEventFor(this.trainDispatcher.getLocomotiveBean())) {
        this.trainDispatcher.getLocomotiveBean().setDirection(directionEvent.getNewDirection());
        Logger.trace("Updated direction to " + directionEvent.getNewDirection() + " of " + this.trainDispatcher.getLocomotiveBean().getName());
      }
    }
    
  }
}
