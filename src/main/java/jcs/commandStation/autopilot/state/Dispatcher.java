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
import jcs.ui.layout.tiles.TileCache;
import jcs.ui.layout.tiles.Tile;
import org.tinylog.Logger;

/**
 * The Dispatcher is the controlling class during auto mode of one Locomotive<br>
 * When a Locomotive runs a separate stateMachine is started which handles all the states.<br>
 *
 */
public class Dispatcher {

  private final LocomotiveBean locomotiveBean;

  private RouteBean routeBean;
  private RouteBean nextRouteBean;

  private String departureBlockId;
  private String destinationBlockId;

  private Integer waitingForSensorId;
  //Enter Sensor of the destination
  private Integer enterSensorId;
  //In Sensor of the destination
  private Integer inSensorId;

  //The Occupation sensor of the departure 
  private Integer occupationSensorId;
  //The exit of the departure
  private Integer exitSensorId;

  private final List<StateEventListener> stateEventListeners;

  private final ThreadGroup parent;

  private StateMachine stateMachine;

  public Dispatcher(ThreadGroup parent, LocomotiveBean locomotiveBean) {
    this.parent = parent;
    this.locomotiveBean = locomotiveBean;
    //Prefill with the current locomotive direction
    this.locomotiveBean.setDispatcherDirection(locomotiveBean.getDirection());
    this.stateEventListeners = new LinkedList<>();
    this.stateMachine = new StateMachine(parent, this);
  }

  StateMachine getStateMachine() {
    return stateMachine;
  }

  public Long getId() {
    return locomotiveBean.getId();
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
//    if (nextRouteBean == null) {
//      nextDestinationBlockId = null;
//    } else {
//      nextDestinationBlockId = nextRouteBean.getToTileId();
//    }
  }

  public boolean isLocomotiveAutomodeOn() {
    return stateMachine.isAutomodeEnabled();
  }

  public boolean startLocomotiveAutomode() {
    //Only when the Autopilot is ON!
    if (AutoPilot.isAutoModeActive()) {
      stateMachine.setEnableAutomode(true);
      //is the thread running?
      startRunning();
    }
    return stateMachine.isAutomodeEnabled();
  }

  public void stopLocomotiveAutomode() {
    stateMachine.setEnableAutomode(false);
  }

  void startRunning() {
    if (stateMachine != null && stateMachine.isThreadRunning()) {
      return;
    }

    if (stateMachine == null || !stateMachine.isAlive()) {
      stateMachine = new StateMachine(this.parent, this);
    }

    stateMachine.setEnableAutomode(true);
    if (!stateMachine.isThreadRunning()) {
      stateMachine.start();
    }
  }

  public void stopRunning() {
    if (stateMachine != null && stateMachine.isThreadRunning()) {
      stateMachine.stopRunningThread();

      try {
        Logger.trace(getName() + " Thread Joining...");
        stateMachine.join();
      } catch (InterruptedException ex) {
        Logger.trace("Join error " + ex);
      }
      Logger.trace(getName() + " Thread Joined!");
    }
  }

  void resetDispatcher() {
    routeBean = null;
    nextRouteBean = null;
    departureBlockId = null;
    destinationBlockId = null;
    //nextDestinationBlockId = null;
    waitingForSensorId = null;
    enterSensorId = null;
    inSensorId = null;
    exitSensorId = null;
    stateEventListeners.clear();
    locomotiveBean.setDispatcherDirection(Direction.SWITCH);
  }

  public void reset() {
    Logger.trace("Resetting dispatcher " + getName() + " StateMachine...");
    this.stateMachine.reset();
    resetDispatcher();
  }

  public boolean isRunning() {
    if (stateMachine != null) {
      return stateMachine.isThreadRunning();
    } else {
      return false;
    }
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

  BlockBean getNextDestinationBlock() {
    //if (nextDestinationBlockId != null) {
    //  return PersistenceFactory.getService().getBlockByTileId(nextDestinationBlockId);
    //} else 
    if (nextRouteBean != null) {
      String nextDestinationBlockId = nextRouteBean.getToTileId();
      return PersistenceFactory.getService().getBlockByTileId(nextDestinationBlockId);
    } else {
      return null;
    }
  }

  public String getStateName() {
    if (stateMachine != null) {
      return stateMachine.getDispatcherStateName();
    } else {
      return "#Idle";
    }
  }

  void setWaitForSensorid(Integer waitingForSensorId) {
    this.waitingForSensorId = waitingForSensorId;
  }

  public Integer getWaitingForSensorId() {
    return waitingForSensorId;
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

  void clearDepartureIgnoreEventHandlers() {
    if (departureBlockId != null) {
      BlockBean departureBlock = getDepartureBlock();
      Integer minSensorId = departureBlock.getMinSensorId();
      AutoPilot.removeHandler(minSensorId);
      Integer plusSensorId = departureBlock.getPlusSensorId();
      AutoPilot.removeHandler(plusSensorId);
    }
  }

  public void onIgnoreEvent(SensorEvent event) {
    //Only in Simulator mode
    if (JCS.getJcsCommandStation().getCommandStationBean().isVirtual()) {
      if (waitingForSensorId != null && waitingForSensorId.equals(event.getSensorId())) {
        if (event.isActive()) {
          waitingForSensorId = null;
        }
      }

      if (enterSensorId != null && enterSensorId.equals(event.getSensorId())) {
        if (!event.isActive()) {
          enterSensorId = null;
        }
      }

      if (inSensorId != null && inSensorId.equals(event.getSensorId())) {
        if (!event.isActive()) {
          inSensorId = null;
        }
      }

      if (exitSensorId != null && exitSensorId.equals(event.getSensorId())) {
        if (!event.isActive()) {
          exitSensorId = null;
        }
      }
    }
    //Logger.trace("Event for a ignored listener: " + event.getIdString() + " Changed: " + event.isChanged() + ", active: " + event.getSensorBean().isActive());
  }

  void switchAccessory(AccessoryBean accessory, AccessoryValue value) {
    try {
      JCS.getJcsCommandStation().switchAccessory(accessory, value);
    } catch (Exception e) {
      Logger.error("Error switching accessory " + accessory.getId() + " to " + value + " Cause: " + e.getMessage());
    }
  }

  void changeLocomotiveVelocity(LocomotiveBean locomotive, double velocity) {
    int commandStationVelocity = (int) velocity;
    locomotiveBean.setVelocity(commandStationVelocity);
    try {
      JCS.getJcsCommandStation().changeLocomotiveSpeed(commandStationVelocity, locomotive);
    } catch (Exception e) {
      Logger.error("Error changing velocity of locomotive " + locomotive.getId() + " to " + velocity + " Cause: " + e.getMessage());
    }
  }

  void changeLocomotiveDirection(LocomotiveBean locomotive, Direction newDirection) {
    try {
      JCS.getJcsCommandStation().changeLocomotiveDirection(newDirection, locomotive);
    } catch (Exception e) {
      Logger.error("Error changing direction of locomotive " + locomotive.getId() + " to " + newDirection + " Cause: " + e.getMessage());
    }
    locomotiveBean.setDirection(newDirection);
  }

  public void fireStateListeners(String s) {
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

  public void removeAllStateEventListeners() {
    stateEventListeners.clear();
  }

  public static void resetRoute(RouteBean route) {
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

  void showRoute(RouteBean routeBean, Color routeColor) {
    Logger.trace("Show route " + routeBean.toLogString());
    List<RouteElementBean> routeElements = routeBean.getRouteElements();

    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      Tile tile = TileCache.findTile(tileId);
      if (tile != null) {
        TileBean.Orientation incomingSide = re.getIncomingOrientation();

        tile.setIncomingSide(incomingSide);
        tile.setTrackRouteColor(Tile.DEFAULT_ROUTE_TRACK_COLOR);

        if (re.isTurnout()) {
          AccessoryBean.AccessoryValue routeState = re.getAccessoryValue();
          tile.setRouteValue(routeState);
        } else if (re.isBlock()) {
          if (re.getTileId().equals(routeBean.getFromTileId())) {
            //departure block
            tile.setBlockState(BlockBean.BlockState.OUTBOUND);
          } else {
            tile.setBlockState(BlockBean.BlockState.INBOUND);
          }
        }
        tile.setShowRoute(true);
      } else {
        Logger.warn("Tile with id " + tileId + " NOT in TileCache!");
      }
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
