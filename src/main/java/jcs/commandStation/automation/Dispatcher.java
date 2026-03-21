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
  private final Long locomotiveId;
  private final String name;
  //private volatile LocomotiveBean locomotiveBean;

  private volatile StateMachine stateMachine;
  private final RouteManager routeManager;

  private volatile RouteBean routeBean;
  private volatile RouteBean nextRouteBean;

  private volatile String departureBlockId;
  private volatile String destinationBlockId;

  //Enter Sensor of the destination
  private volatile Integer enterSensorId;
  //In Sensor of the destination
  private volatile Integer inSensorId;
  //The Occupation sensor of the departure 
  private volatile Integer occupationSensorId;
  //The exit of the departure
  private volatile Integer exitSensorId;

  volatile boolean locomotiveStarted = false;
  private final List<StateEventListener> stateEventListeners;

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
    locomotiveId = locomotiveBean.getId();
    name = locomotiveBean.getName();
    routeManager = new RouteManager(this);
    stateEventListeners = new ArrayList<>();
  }

  @SuppressWarnings("unused")
  RailwayController getRailwayController() {
    return railwayController;
  }

  SensorMonitor getSensorMonitor() {
    return railwayController.getSensorMonitor();
  }

  RouteManager getRouteManager() {
    return routeManager;
  }

  public String getName() {
    return this.name;
  }

  Long getLocomotiveId() {
    return locomotiveId;
  }

//  void setLocomotiveBean(LocomotiveBean locomotiveBean) {
//    this.locomotiveBean = locomotiveBean;
//  }
  //Convenience; Pass through a "fresh" locomotive
  public LocomotiveBean getLocomotiveBean() {
    return PersistenceFactory.getService().getLocomotive(locomotiveId);
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
      if (stateMachine == null || !stateMachine.isRunning()) {
        // Explicitly clean up old state machine if present
        if (stateMachine != null) {
          stateMachine.getCurrentState().onExit();
        }
        locomotiveStarted = false;
        stateMachine = new StateMachine(this, new IdleState());
      } else {
        Logger.debug("There is a running dispatcherRunner for " + getName());
      }
    }
  }

  public void startLocomotive() {
    if (railwayController.isAutoModeActive()) {
      if (stateMachine != null && !stateMachine.isRunning()) {
        stateMachine.startStateMachineThread();
        locomotiveStarted = stateMachine.isRunning();
      } else {
        Logger.debug("There is a running dispatcherRunner");
      }
    } else {
      Logger.trace("Can't start Locomotive " + getName() + " automode is not enabled");
    }
  }

  //For test mocking only!!!
  void setLocomotiveStarted(boolean locomotiveStarted
  ) {
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
    if (stateMachine != null) {
      stateMachine.reset();
    } else {
      resetAttributes();
    }
  }

  private void resetAttributes() {
    setEnterSensorId(null);
    setInSensorId(null);
    setExitSensorId(null);

    BlockBean destination = getDestinationBlock();
    if (destination != null) {
      destination.setLocomotive(null);
      destination.setBlockState(BlockBean.BlockState.FREE);
      destination.setArrivalSuffix(null);
      PersistenceFactory.getService().persist(destination);
      showBlockState(destination);
    }

    BlockBean departure = getDepartureBlock();
    departure.setBlockState(BlockBean.BlockState.OCCUPIED);

    RouteBean route = getRouteBean();
    if (route != null) {
      route.setLocked(false);
      departure.setDepartureSuffix(route.getFromSuffix());
      PersistenceFactory.getService().persist(route);
      resetRoute(route);
    }
    PersistenceFactory.getService().persist(departure);
    showBlockState(departure);

    RouteBean nextRoute = getNextRouteBean();
    if (nextRoute != null) {
      nextRoute.setLocked(false);
      resetRoute(nextRoute);
      PersistenceFactory.getService().persist(nextRoute);
    }

    setRouteBean(null);
    setNextRouteBean(null);
    setDestinationBlockId(null);

    Logger.trace(getName() + " has been Reset");

    stateEventListeners.clear();
  }

  String getDepartureBlockId() {
    return departureBlockId;
  }

  //Make sure to obtain the last status of the block is represented...
  BlockBean getDepartureBlock() {
    BlockBean departureBlock;
    if (departureBlockId != null) {
      departureBlock = PersistenceFactory.getService().getBlock(departureBlockId);
    } else if (routeBean != null) {
      departureBlock = PersistenceFactory.getService().getBlockByTileId(routeBean.getFromTileId());
      departureBlockId = departureBlock.getId();
    } else {
      departureBlock = PersistenceFactory.getService().getBlockByLocomotiveId(locomotiveId);
      departureBlockId = departureBlock.getId();
    }

    //Check suffixes
    if (routeBean != null) {
      String fromSuffix = routeBean.getFromSuffix();
      if (departureBlock.getArrivalSuffix() == null) {
        String newArrivalSuffix;
        if ("-".equals(fromSuffix)) {
          newArrivalSuffix = "+";
        } else {
          newArrivalSuffix = "-";
        }
        Logger.trace("Arrival Suffix is not set! Setting it to: " + newArrivalSuffix);
        departureBlock.setArrivalSuffix(newArrivalSuffix);
        PersistenceFactory.getService().persist(departureBlock);
      }
    }

    return departureBlock;
  }

  BlockBean getDestinationBlock() {
    BlockBean destinationBlock = null;
    if (destinationBlockId != null) {
      destinationBlock = PersistenceFactory.getService().getBlock(destinationBlockId);
    } else if (routeBean != null) {
      destinationBlock = PersistenceFactory.getService().getBlockByTileId(routeBean.getToTileId());
      destinationBlockId = destinationBlock.getId();
    }
    return destinationBlock;
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
    try {
      int newVelocity = (int) velocity;
      LocomotiveBean locomotive = getLocomotiveBean();
      locomotive.setVelocity(newVelocity);

      JCS.getJcsCommandStation().changeLocomotiveSpeed(newVelocity, locomotive);
    } catch (Exception e) {
      Logger.error("Error changing velocity of locomotive " + locomotiveId + " to " + velocity + " Cause: " + e.getMessage());
    }
  }

  void changeLocomotiveDirection(Direction newDirection) {
    try {
      LocomotiveBean locomotive = getLocomotiveBean();
      JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, locomotive);
    } catch (Exception e) {
      Logger.error("Error changing direction of locomotive " + locomotiveId + " to " + newDirection + " Cause: " + e.getMessage());
    }
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

  void wakeup() {
    if (stateMachine != null) {
      stateMachine.wakeUp();
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(locomotiveId);
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
    return Objects.equals(locomotiveId, other.locomotiveId);
  }

}
