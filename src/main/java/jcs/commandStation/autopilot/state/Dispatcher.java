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
import java.util.Random;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
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
import jcs.ui.layout.TileCache;
import jcs.ui.layout.events.TileEvent;
import org.tinylog.Logger;

/**
 * The Dispatcher is the controlling class during auto mode of one Locomotive<br>
 * When a Locomotive runs a separate stateMachineThread is started which handles all the states.<br>
 *
 */
public class Dispatcher {

  private final LocomotiveBean locomotiveBean;

  private RouteBean routeBean;

  private String departureBlockId;
  private String destinationBlockId;

  private String waitingForSensorId;
  //Enter Sensor of the destination
  private String enterSensorId;
  //In Sensor of the destination
  private String inSensorId;

  //The Occupation sensor of the departure 
  private String occupationSensorId;
  //The exit of the departure
  private String exitSensorId;

  private final List<StateEventListener> stateEventListeners;

  private final ThreadGroup parent;

  private StateMachineThread stateMachineThread;

  public Dispatcher(ThreadGroup parent, LocomotiveBean locomotiveBean) {
    this.parent = parent;
    this.locomotiveBean = locomotiveBean;
    //Prefill with the current locomotive direction
    this.locomotiveBean.setDispatcherDirection(locomotiveBean.getDirection());
    this.stateEventListeners = new LinkedList<>();
    this.stateMachineThread = new StateMachineThread(parent, this);
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

  public boolean isLocomotiveAutomodeOn() {
    return this.stateMachineThread.isEnableAutomode();
  }

  public boolean startLocomotiveAutomode() {
    //Only when the Autopilot is ON!
    if (AutoPilot.isAutoModeActive()) {
      stateMachineThread.setEnableAutomode(true);
      //is the thread running?
      startRunning();
    }
    return this.stateMachineThread.isEnableAutomode();
  }

  public void stopLocomotiveAutomode() {
    stateMachineThread.setEnableAutomode(false);
  }

  void startRunning() {
    if (this.stateMachineThread != null && this.stateMachineThread.isThreadRunning()) {
      return;
    }

    if (this.stateMachineThread == null || !this.stateMachineThread.isAlive()) {
      stateMachineThread = new StateMachineThread(this.parent, this);
    }

    this.stateMachineThread.setEnableAutomode(true);
    if (!this.stateMachineThread.isThreadRunning()) {
      this.stateMachineThread.start();
    }
  }

  public void stopRunning() {
    if (stateMachineThread != null && stateMachineThread.isThreadRunning()) {
      stateMachineThread.stopRunningThread();

      try {
        Logger.trace(this.getName() + " Thread Joining...");
        stateMachineThread.join();
      } catch (InterruptedException ex) {
        Logger.trace("Join error " + ex);
      }
      Logger.trace(this.getName() + " Thread Joined!");
    }
  }

  void resetDispatcher() {
    this.routeBean = null;
    this.departureBlockId = null;
    this.destinationBlockId = null;
    this.waitingForSensorId = null;
    this.enterSensorId = null;
    this.inSensorId = null;
    this.exitSensorId = null;
    this.stateEventListeners.clear();
    this.locomotiveBean.setDispatcherDirection(Direction.SWITCH);
  }

  public void reset() {
    Logger.trace("Resetting dispatcher " + getName() + " StateMachine...");
    this.stateMachineThread.reset();
    resetDispatcher();
  }

  public boolean isRunning() {
    if (stateMachineThread != null) {
      return stateMachineThread.isThreadRunning();
    } else {
      return false;
    }
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

  public String getStateName() {
    if (stateMachineThread != null) {
      return stateMachineThread.getDispatcherStateName();
    } else {
      return "#Idle";
    }
  }

  void setWaitForSensorid(String sensorId) {
    this.waitingForSensorId = sensorId;
  }

  public String getWaitingForSensorId() {
    return waitingForSensorId;
  }

  public String getEnterSensorId() {
    return enterSensorId;
  }

  void setEnterSensorId(String enterSensorId) {
    this.enterSensorId = enterSensorId;
  }

  public String getInSensorId() {
    return inSensorId;
  }

  void setInSensorId(String inSensorId) {
    this.inSensorId = inSensorId;
  }

  public String getOccupationSensorId() {
    return occupationSensorId;
  }

  void setOccupationSensorId(String occupationSensorId) {
    this.occupationSensorId = occupationSensorId;
  }

  public String getExitSensorId() {
    return exitSensorId;
  }

  void setExitSensorId(String exitSensorId) {
    this.exitSensorId = exitSensorId;
  }

  void clearDepartureIgnoreEventHandlers() {
    if (departureBlockId != null) {
      BlockBean departureBlock = getDepartureBlock();
      String minSensorId = departureBlock.getMinSensorId();
      AutoPilot.removeHandler(minSensorId);
      String plusSensorId = departureBlock.getPlusSensorId();
      AutoPilot.removeHandler(plusSensorId);
    }
  }

  public void onIgnoreEvent(SensorEvent event) {
    //Only in Simulator mode
    if (JCS.getJcsCommandStation().getCommandStationBean().isVirtual()) {
      if (this.waitingForSensorId != null && this.waitingForSensorId.equals(event.getId())) {
        if (event.isActive()) {
          this.waitingForSensorId = null;
        }
      }

      if (this.enterSensorId != null && this.enterSensorId.equals(event.getId())) {
        if (!event.isActive()) {
          this.enterSensorId = null;
        }
      }

      if (this.inSensorId != null && this.inSensorId.equals(event.getId())) {
        if (!event.isActive()) {
          this.inSensorId = null;
        }
      }

      if (this.exitSensorId != null && this.exitSensorId.equals(event.getId())) {
        if (!event.isActive()) {
          this.exitSensorId = null;
        }
      }

    }
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

  public static void resetRoute(RouteBean route) {
    List<RouteElementBean> routeElements = route.getRouteElements();
    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      TileEvent tileEvent = new TileEvent(tileId, false);
      TileCache.fireTileEventListener(tileEvent);
    }
  }

  void showBlockState(BlockBean blockBean) {
    Logger.trace("Show block " + blockBean);
    TileEvent tileEvent = new TileEvent(blockBean);
    TileCache.fireTileEventListener(tileEvent);
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
      TileCache.fireTileEventListener(tileEvent);
    }
  }

  int getRandomNumber(int min, int max) {
    Random random = new Random();
    return random.ints(min, max).findFirst().getAsInt();
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
