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
 * When a Locomotive runs a separate thread is started which handles all the states.<br>
 *
 */
public class Dispatcher {

  private final LocomotiveBean locomotiveBean;

  final AutoPilot autoPilot;
  private RouteBean routeBean;

  private BlockBean departureBlock;
  private BlockBean destinationBlock;

  private final List<StateEventListener> stateEventListeners;

  private DispatcherThread thread;

  public Dispatcher(LocomotiveBean locomotiveBean, AutoPilot autoPilot) {
    this.locomotiveBean = locomotiveBean;
    this.autoPilot = autoPilot;

    this.stateEventListeners = new LinkedList<>();
    thread = new DispatcherThread(this);
    startDispatcherThread();
  }

  private void startDispatcherThread() {
    this.thread.start();
  }

  DispatcherThread getDispatcherThread() {
    return thread;
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

  public final void startStateMachine() {
    thread.startStateMachine();
  }

  public void stopStateMachine() {
    thread.stopStateMachine();
  }

  public void stopLocomotiveAutomode() {
    thread.stopLocomotiveAutomode();
  }

  @Deprecated
  public void startRunning() {
//    if (this.thread != null && this.thread.isRunning()) {
//      return;
//    }
//
//    if (this.thread == null || !this.thread.isAlive()) {
//      thread = new DispatcherThread(this);
//    }
//
//    this.thread.start();
    startStateMachine();
  }

  @Deprecated
  public void stopRunning() {
//    if (thread != null && thread.isThreadRunning()) {
//      thread.stopThread();
//    }
    stopLocomotiveAutomode();
  }

  public void forceStopRunning() {
//    if (thread != null && thread.isThreadRunning()) {
//      this.thread.forceStop();
//    }
  }

  public void reset() {
    if (this.isRunning()) {
      Logger.trace("Resetting dispatcher " + getName() + "...");

      BlockBean fromBlock = departureBlock;
      BlockBean toBlock = destinationBlock;

      //Stop the thread
      forceStopRunning();

      if (fromBlock == null) {
        Logger.trace("No From block where " + this.locomotiveBean.getName() + " currently is?");
        return;
      }

      //Wait until the thread is really stopped
      //TODO does this need a timeout?
//      boolean waiting = thread.isRunning();
//      Logger.trace("Thread " + thread.getName() + " Running: " + waiting);
//      while (waiting) {
      try {
//          //synchronized (this) {
//            //wait(25L);
        Thread.sleep(100L);
//            waiting = thread.isRunning();
//            Logger.trace("Loop Thread "+thread.getName()+" Running: "+waiting);
//          //}
      } catch (InterruptedException ex) {
        Logger.trace(ex.getMessage());
      }
//      }
      boolean waiting = thread.isThreadRunning();
      Logger.trace("Thread " + thread.getName() + " Running: " + waiting);

//Thread is stopped
      Logger.trace("Dispatcher Thread for " + getName() + " has stopped, reset block statusses...");

      clearDepartureIgnoreEventHandlers();

      Logger.trace("Listeners cleared for " + getName() + "...");

      if (routeBean != null) {
        routeBean.setLocked(false);
        PersistenceFactory.getService().persist(routeBean);
        resetRoute(routeBean);
        Logger.trace("Unlocked route " + routeBean.getId());
      }

      if (toBlock != null) {
        toBlock.setLocomotive(null);
        toBlock.setBlockState(BlockBean.BlockState.FREE);
        toBlock.setArrivalSuffix(null);

        PersistenceFactory.getService().persist(toBlock);
        showBlockState(toBlock);
        Logger.trace("Reset toBlock " + toBlock.getId());
      }

      fromBlock.setLocomotive(this.locomotiveBean);
      fromBlock.setBlockState(BlockBean.BlockState.OCCUPIED);
      PersistenceFactory.getService().persist(fromBlock);

      this.departureBlock = fromBlock;
      showBlockState(fromBlock);
      Logger.trace("Reset toBlock " + fromBlock.getId());

      this.destinationBlock = null;
      this.routeBean = null;

      //Initialize a new worker thread
      thread = new DispatcherThread(this);
      Logger.trace("Created new Thread for " + getName() + "...");
    } else {
      Logger.trace("Dispatcher " + getName() + " is not running...");
    }
  }

  ///????
  public boolean isRunning() {
    if (thread != null) {
      return thread.isThreadRunning();
    } else {
      return false;
    }
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

  BlockBean getDestinationBlock() {
    if (routeBean != null) {
      String destinationTileId = routeBean.getToTileId();
      BlockBean blockBean = PersistenceFactory.getService().getBlockByTileId(destinationTileId);
      destinationBlock = blockBean;
    }
    return destinationBlock;
  }

  public String getDispatcherState() {
    if (thread != null) {
      return thread.getState().getClass().getSimpleName();
    } else {
      return "#Idle";
    }
  }

  void registerIgnoreEventHandler(String sensorId) {
    if (!autoPilot.isSensorRegistered(sensorId)) {
      IgnoreSensorHandler ish = new IgnoreSensorHandler(sensorId, this);
      autoPilot.addHandler(ish, sensorId);
      Logger.trace("Added sensor " + sensorId + " to the ignore handlers");
    } else {
      Logger.trace("Sensor " + sensorId + " is allready ignored");
    }
  }

  synchronized void clearDepartureIgnoreEventHandlers() {
    if (departureBlock != null) {
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
