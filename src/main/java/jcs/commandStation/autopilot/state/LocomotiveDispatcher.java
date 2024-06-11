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
public class LocomotiveDispatcher {

  private final LocomotiveBean locomotiveBean;
  final AutoPilot autoPilot;
  private RouteBean routeBean;

  private BlockBean departureBlock;
  private BlockBean destinationBlock;

  private final List<StateEventListener> stateEventListeners;
  private LocomotiveVelocityListener locomotiveVelocityListener;
  private LocomotiveDirectionChangeListener locomotiveDirectionChangeListener;

  private LocomotiveRunnerThread thread;

  //For Testing a dialog per dispatcher to be replaced by a panel in de main screen
  //private DispatcherTestDialog dispatcherDialog;

  public LocomotiveDispatcher(LocomotiveBean locomotiveBean, AutoPilot autoPilot) {
    this.locomotiveBean = locomotiveBean;
    this.autoPilot = autoPilot;

    this.stateEventListeners = new LinkedList<>();
    initializeListeners();

    //Initialize a worker thread, don't start it yet
    thread = new LocomotiveRunnerThread(this);

    initTestDialog();
  }

  private void initTestDialog() {
    //dispatcherDialog = DispatcherTestDialog.showDialog(this);
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
      this.departureBlock = null;
      this.destinationBlock = null;
    } else {
      this.departureBlock = getBlock(routeBean.getFromTileId());
      this.destinationBlock = getBlock(routeBean.getToTileId());
    }
  }

  public void startRunning() {
    if (this.thread != null && this.thread.isRunning()) {
      return;
    }

    if (this.thread == null || !this.thread.isAlive()) {
      thread = new LocomotiveRunnerThread(this);
    }

    this.thread.start();
  }

  public void stopRunning() {
    if (this.thread != null && this.thread.isRunning()) {
      this.thread.stopRunning();
    }

    //disposeDialog();
  }

  //For testing....
  //public void disposeDialog() {
  //  this.dispatcherDialog.dispose();
  //}

  public boolean isRunning() {
    if (this.thread != null) {
      return this.thread.isRunning();
    } else {
      return false;
    }
  }

  private void initializeListeners() {
    locomotiveVelocityListener = new LocomotiveVelocityListener(this);
    JCS.getJcsCommandStation().addLocomotiveSpeedEventListener(locomotiveVelocityListener);

    locomotiveDirectionChangeListener = new LocomotiveDirectionChangeListener(this);
    JCS.getJcsCommandStation().addLocomotiveDirectionEventListener(locomotiveDirectionChangeListener);
  }

  void unRegisterListeners() {
    JCS.getJcsCommandStation().removeLocomotiveSpeedEventListener(locomotiveVelocityListener);
    JCS.getJcsCommandStation().removeLocomotiveDirectionEventListener(locomotiveDirectionChangeListener);

    locomotiveVelocityListener = null;
    locomotiveDirectionChangeListener = null;
  }

  private BlockBean getBlock(String tileId) {
    BlockBean block = PersistenceFactory.getService().getBlockByTileId(tileId);
    return block;
  }

  BlockBean getDepartureBlock() {
    if (departureBlock == null) {
      if (routeBean != null) {
        departureBlock = getBlock(routeBean.getFromTileId());
      } else {
        departureBlock = PersistenceFactory.getService().getBlockByLocomotiveId(locomotiveBean.getId());
      }
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
    if (this.thread != null) {
      return this.thread.getDispatcherState().getClass().getSimpleName();
    } else {
      return "#Idle";
    }
  }

  void registerIgnoreEventHandler(String sensorId) {
    if (!this.autoPilot.isSensorRegistered(sensorId)) {
      IgnoreSensorHandler ish = new IgnoreSensorHandler(sensorId, this);
      this.autoPilot.addHandler(ish, sensorId);
      //this.ignoreSensorEventHandlers.add(ish);
      Logger.trace("Added sensor " + sensorId + " to the ignore handlers");
    } else {
      Logger.trace("Ssensor " + sensorId + " is already ignored");
    }
  }

  void clearDepartureIgnoreEventHandlers() {
    if (this.departureBlock != null) {
      String minSensorId = this.departureBlock.getMinSensorId();
      autoPilot.removeHandler(minSensorId);
      String plusSensorId = this.departureBlock.getPlusSensorId();
      autoPilot.removeHandler(plusSensorId);
    }
  }

  synchronized void onIgnoreEvent(SensorEvent event) {
    Logger.trace("Event for a ignored listener: " + event.getId() + " Changed: " + event.isChanged() + ", active: " + event.getSensorBean().isActive());
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

  void fireStateListeners(String s) {
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
    private final LocomotiveDispatcher trainDispatcher;

    IgnoreSensorHandler(String sensorId, LocomotiveDispatcher trainDispatcher) {
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

  private class LocomotiveVelocityListener implements LocomotiveSpeedEventListener {

    private final LocomotiveDispatcher trainDispatcher;

    LocomotiveVelocityListener(LocomotiveDispatcher trainDispatcher) {
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

    private final LocomotiveDispatcher trainDispatcher;

    LocomotiveDirectionChangeListener(LocomotiveDispatcher trainDispatcher) {
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

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(this.locomotiveBean);
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
    final LocomotiveDispatcher other = (LocomotiveDispatcher) obj;
    return Objects.equals(this.locomotiveBean, other.locomotiveBean);
  }

  //Testing
  //void performManualStep() {
  //  if (this.thread != null) {
  //    this.thread.manualStep();
  //  }
  //}

}
