/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.automation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jcs.JCS;
import jcs.entities.AccessoryBean;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.StationBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.TileCache;
import jcs.ui.layout.tiles.Tile;
import org.tinylog.Logger;

/**
 * The Dispatcher is the controlling class during auto mode for one Locomotive<br>
 * When a Locomotive runs a State Machine is started which handles all the states.<br>
 *
 */
public class Dispatcher {

  private final RailwayController railwayController;
  private final LocomotiveBean locomotiveBean;

  private final List<StateEventListener> stateEventListeners;

  private StateMachine stateMachine;

  private final RouteManager routeManager;

  private RouteBean routeBean;
  private RouteBean nextRouteBean;

  private String departureBlockId;
  private String destinationBlockId;

  //Enter Sensor of the destination
  private Integer enterSensorId;
  //In Sensor of the destination
  private Integer inSensorId;
  //The Occupation sensor of the departure 
  private Integer occupationSensorId;
  //The exit of the departure
  private Integer exitSensorId;

  private boolean locomotiveStarted = false;
  //private boolean locomotiveStopRequest = false;

  /**
   * Dispatcher is created when auto mode is enabled.<br>
   * Every Locomotive on track will create a dispatcher.<br>
   * A dispatcher always has a state.<br>
   *
   * A dispatcher has a dispatcher thread which is started when the locomotive is started.<br>
   * The thread is stopped when the locomotive is stopped, or<br>
   * when auto mode is disabled and the state of the dispatcher is occupied,<br>
   * i.e. the locomotive has arrived in the target block and has touched the in sensor.<br>
   *
   *
   * @param railwayController
   * @param locomotiveBean
   */
  Dispatcher(RailwayController railwayController, LocomotiveBean locomotiveBean) {
    this.railwayController = railwayController;
    this.locomotiveBean = locomotiveBean;
    //Prefill with the current locomotive direction
    this.locomotiveBean.setDispatcherDirection(locomotiveBean.getDirection());

    this.routeManager = new RouteManager(this);
    this.stateEventListeners = new ArrayList<>();
  }

  @SuppressWarnings("unused")
  RailwayController getRailwayController() {
    return railwayController;
  }

  SensorMonitor getSensorMonitor() {
    return railwayController.getSensorMonitor();
  }

  @SuppressWarnings("unused")
  Long getId() {
    return locomotiveBean.getId();
  }

  RouteManager getRouteManager() {
    return routeManager;
  }

  public String getName() {
    return locomotiveBean.getName();
  }

  public LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }

  RouteBean getRouteBean() {
    return routeBean;
  }

  RouteBean getNextRouteBean() {
    return nextRouteBean;
  }

  void setRouteBean(RouteBean routeBean) {
    this.routeBean = routeBean;
    if (routeBean == null) {
      departureBlockId = null;
      destinationBlockId = null;
    } else {
      departureBlockId = routeBean.getFromTileId();
      destinationBlockId = routeBean.getToTileId();
    }
  }

  void setNextRouteBean(RouteBean nextRouteBean) {
    this.nextRouteBean = nextRouteBean;
  }

  void enable() {
    if (railwayController.isAutoModeActive()) {
      if (stateMachine == null || (stateMachine != null && !stateMachine.isRunning())) {
        locomotiveStarted = false;
        stateMachine = new StateMachine(this, new IdleState());
      } else {
        Logger.debug("There is a running dispatcherRunner");
      }
    } else {
      Logger.trace("Can't start Locomotive " + getName() + " automode is not enabled");
    }
  }

  public void startLocomotive() {
    if (railwayController.isAutoModeActive()) {
      if (stateMachine != null && !stateMachine.isRunning()) {
        Logger.debug("There is a running dispatcherRunner");
      } else {
        stateMachine = new StateMachine(this, new IdleState());
        stateMachine.startStateMachineThread();
        locomotiveStarted = stateMachine.isRunning();
      }
    } else {
      Logger.trace("Can't start Locomotive " + getName() + " automode is not enabled");
    }
  }

  //For test mocking only!!!
  void setLocomotiveStarted(boolean locomotiveStarted) {
    this.locomotiveStarted = locomotiveStarted;
  }

  public boolean isLocomotiveStarted() {
    if (stateMachine != null && stateMachine.isThreadEnabled()) {
      return stateMachine.isRunning();
    } else {
      return locomotiveStarted;
    }
  }

  public void stopLocomotive() {
    //   locomotiveStopRequest = true;
    if (stateMachine != null) {
      locomotiveStarted = !stateMachine.getCurrentState().canStopLocomotive();
      stateMachine.stopStateMachineThread();
    } else {
      locomotiveStarted = false;
    }
  }

  StateMachine getStateMachine() {
    return stateMachine;
  }

  ThreadGroup getThreadGroup() {
    return railwayController.getThreadGroup();
  }

  public void reset() {
    //TODO!
//    if (isRunning()) {
//      //In which state are we?
//      AbstractState currentState = stateMachine.getDispatcherState();
//      currentState.requestReset();
//    } else {
//      stateMachine.reset();
//    }
  }

  void resetAttributes() {
    routeBean = null;
    nextRouteBean = null;
    destinationBlockId = null;
    //waitingForSensorId = null;
    enterSensorId = null;
    inSensorId = null;
    exitSensorId = null;
    stateEventListeners.clear();
  }

  //Make sure to obtain the last status of the block is represented...
  BlockBean getDepartureBlock() {
    if (departureBlockId != null) {
      return PersistenceFactory.getService().getBlockByTileId(departureBlockId);
    } else if (routeBean != null) {
      departureBlockId = routeBean.getFromTileId();
      return PersistenceFactory.getService().getBlockByTileId(departureBlockId);
    } else {
      BlockBean departureBlock = PersistenceFactory.getService().getBlockByLocomotiveId(locomotiveBean.getId());
      departureBlockId = departureBlock.getTileId();
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

  void setDestinationBlockId(String destinationBlockId) {
    this.destinationBlockId = destinationBlockId;
  }

  BlockBean getNextDestinationBlock() {
    if (nextRouteBean != null) {
      String nextDestinationBlockId = nextRouteBean.getToTileId();
      return PersistenceFactory.getService().getBlockByTileId(nextDestinationBlockId);
    } else {
      return null;
    }
  }

  StationBean getStation(BlockBean blockBean) {
    return PersistenceFactory.getService().getStation(blockBean);
  }

  public String getStateName() {
    if (stateMachine != null) {
      return stateMachine.getCurrentStateName();
    } else {
      return "#Idle";
    }
  }

  public Integer getEnterSensorId() {
    return enterSensorId;
  }

  void setEnterSensorId(Integer enterSensorId) {
    this.enterSensorId = enterSensorId;
  }

  public Integer getInSensorId() {
    return inSensorId;
  }

  void setInSensorId(Integer inSensorId) {
    this.inSensorId = inSensorId;
  }

  public Integer getOccupationSensorId() {
    return occupationSensorId;
  }

  void setOccupationSensorId(Integer occupationSensorId) {
    this.occupationSensorId = occupationSensorId;
  }

  public Integer getExitSensorId() {
    return exitSensorId;
  }

  void setExitSensorId(Integer exitSensorId) {
    this.exitSensorId = exitSensorId;
  }

  void changeLocomotiveVelocity(double velocity) {
    int commandStationVelocity = (int) velocity;
    locomotiveBean.setVelocity(commandStationVelocity);
    try {
      JCS.getJcsCommandStation().changeLocomotiveSpeed(commandStationVelocity, locomotiveBean);
    } catch (Exception e) {
      Logger.error("Error changing velocity of locomotive " + locomotiveBean.getId() + " to " + velocity + " Cause: " + e.getMessage());
    }
  }

  void changeLocomotiveDirection(Direction newDirection) {
    try {
      JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, locomotiveBean);
    } catch (Exception e) {
      Logger.error("Error changing direction of locomotive " + locomotiveBean.getId() + " to " + newDirection + " Cause: " + e.getMessage());
    }
    locomotiveBean.setDirection(newDirection);
  }

  void fireStateListeners(String oldState, String newState, String comment) {
    for (StateEventListener sel : stateEventListeners) {
      sel.onStateChange(this, oldState, newState, comment);
    }
  }

  public void addStateEventListener(StateEventListener listener) {
    stateEventListeners.add(listener);
  }

  public void removeStateEventListener(StateEventListener listener) {
    stateEventListeners.remove(listener);
  }

  public void removeAllStateEventListeners() {
    stateEventListeners.clear();
  }

  void resetRoute(RouteBean route) {
    List<RouteElementBean> routeElements = route.getRouteElements();
    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      Tile tile = TileCache.findTile(tileId);
      if (tile != null) {
        if (tile.isBlock()) {
          if (tile.getLocomotive() != null) {
            tile.setBlockState(BlockBean.BlockState.OCCUPIED);
          } else {
            tile.setBlockState(BlockBean.BlockState.FREE);
          }
        }
        if (tile.isJunction()) {
          tile.setRouteValue(AccessoryBean.AccessoryValue.OFF);
        }
        tile.setShowRoute(false);
      } else {
        Logger.warn(("Tile with id " + tileId + " NOT in TileCache!"));
      }
    }
  }

  void showBlockState(BlockBean blockBean) {
    Tile tile = TileCache.findTile(blockBean.getTileId());
    if (tile != null) {
      tile.setBlockBean(blockBean);
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
