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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import jcs.JCS;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.StationBean;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Block;
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
  private boolean locomotiveStopRequest = false;
  //@SuppressWarnings("unused")
  //private boolean locomotiveStopRequested = false;
  //@SuppressWarnings("unused")
  //private boolean dispatcherDisableRequested = false;
  private boolean swapLocomotiveDirection;

  //Needed for testing without running thread
  //private boolean enabled = false;
  //private boolean stepTest = false;
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
    //this.parent = railwayController.getThreadGroup();
    this.locomotiveBean = locomotiveBean;
    //Prefill with the current locomotive direction
    this.locomotiveBean.setDispatcherDirection(locomotiveBean.getDirection());

    stateEventListeners = new ArrayList<>();

    //stepTest = Boolean.parseBoolean(System.getProperty("state.machine.stepTest", "false"));
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

  String getName() {
    return locomotiveBean.getName();
  }

  LocomotiveBean getLocomotiveBean() {
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
    locomotiveStopRequest = true;
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
    //stateEventListeners.clear();
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

  Integer getEnterSensorId() {
    return enterSensorId;
  }

  void setEnterSensorId(Integer enterSensorId) {
    this.enterSensorId = enterSensorId;
  }

  Integer getInSensorId() {
    return inSensorId;
  }

  void setInSensorId(Integer inSensorId) {
    this.inSensorId = inSensorId;
  }

  Integer getOccupationSensorId() {
    return occupationSensorId;
  }

  void setOccupationSensorId(Integer occupationSensorId) {
    this.occupationSensorId = occupationSensorId;
  }

  Integer getExitSensorId() {
    return exitSensorId;
  }

  void setExitSensorId(Integer exitSensorId) {
    this.exitSensorId = exitSensorId;
  }

  void switchAccessory(AccessoryBean accessory, AccessoryValue value) {
    try {
      JCS.getJcsCommandStation().switchAccessory(accessory, value);
    } catch (Exception e) {
      Logger.error("Error switching accessory " + accessory.getId() + " to " + value + " Cause: " + e.getMessage());
    }
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
      sel.onStateChange(oldState, newState, comment);
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

  boolean isAllowed(boolean allowCommuter, boolean allowNonCommuter, boolean commuter) {
    //both flags are the same → all trains allowed
    if (allowCommuter == allowNonCommuter) {
      return true;
    }

    // only non-commuter allowed
    if (!allowCommuter && allowNonCommuter) {
      return !commuter;
    }

    // only commuter allowed
    if (allowCommuter && !allowNonCommuter) {
      return commuter;
    }

    // Should never happen, but default deny
    return false;
  }

  boolean turnoutsNotLocked(RouteBean route) {
    List<RouteElementBean> turnouts = getTurnouts(route);

    boolean switchesNotLocked = true;
    for (RouteElementBean reb : turnouts) {
      //AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
      AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
      //check if the accessory is not set by an other reserved nextRoute
      boolean locked = PersistenceFactory.getService().isAccessoryLocked(turnout.getId());
      if (locked) {
        Logger.debug("Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] is locked!");
        return false;
      }
    }
    Logger.trace("There are " + turnouts.size() + " free turnouts in this route");
    return switchesNotLocked;
  }

  List<RouteElementBean> getTurnouts(RouteBean routeBean) {
    List<RouteElementBean> rel = routeBean.getRouteElements();
    List<RouteElementBean> turnouts = new ArrayList<>();
    for (RouteElementBean reb : rel) {
      if (reb.isTurnout()) {
        turnouts.add(reb);
      }
    }
    return turnouts;
  }

  void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  boolean searchRoute(boolean swapEnabled) {
    LocomotiveBean locomotive = getLocomotiveBean();
    Logger.trace("Search a free route for " + locomotive.getName() + "...");

    BlockBean departureBlock = getDepartureBlock();
    //Is the departure block part of a station.
    StationBean station = getStation(departureBlock);
    int locCount = 1;
    int minLocCount = 1;
    if (station != null) {
      minLocCount = station.getMinLocomotives();
      locCount = PersistenceFactory.getService().getLocomotiveCount(station).intValue();
      if (station.getLocomotiveCount() != locCount) {
        Logger.warn("Adjusting number of locomotives in station " + station.getName() + " to " + locCount);
        station.setLocomotiveCount(locCount);
        PersistenceFactory.getService().persist(station);
      }
      Logger.trace(departureBlock.getId() + " is member of Station " + station.getName() + " min Locs: " + minLocCount + " cur locs: " + locCount);
    }

    if (departureBlock.getLogicalDirection() == null) {
      departureBlock.setLogicalDirection(locomotive.getDirection().getDirection());
    }
    Direction logicalDirection = Direction.get(departureBlock.getLogicalDirection());

    TileBean tileBean = PersistenceFactory.getService().getTileBean(departureBlock.getTileId());
    TileBean.Orientation blockOrientation = tileBean.getOrientation();

    String departureSuffix = departureBlock.getDepartureSuffix();
    if (departureSuffix == null) {
      departureSuffix = Block.getDepartureSuffix(blockOrientation, logicalDirection);
    }

    Logger.trace("Loco " + locomotive.getName() + " is in block " + departureBlock.getId() + ". Direction " + logicalDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    //only search for a route if we can depart
    List<RouteBean> routes = Collections.emptyList();
    if (locCount >= minLocCount) {
      //Search for the possible routes
      routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
      Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");
    } else {
      String stationName;
      if (station != null) {
        stationName = station.getName();
      } else {
        stationName = "?";
      }
      Logger.trace("nr of locs (" + locCount + ") is lower than min locs (" + minLocCount + ") for station " + stationName);
    }

    List<RouteBean> checkedRoutes = new ArrayList<>();
    boolean commuter = locomotive.isCommuter();

    //No routes found or possible.
    //When the Locomotive is a commuter train and the departure block allows a direction change it can reverse direction. Lets try that...
    if (routes.isEmpty() && commuter && swapEnabled && departureBlock.isAllowDirectionChange() && locCount >= minLocCount) {
      Direction oldDirection = logicalDirection;
      //Direction newDirection = locomotive.toggleDispatcherDirection();
      Direction newDirection = LocomotiveBean.toggle(oldDirection);
      Logger.trace("Reversing Locomotive, from " + oldDirection + " to " + newDirection + "...");

      swapLocomotiveDirection = true;
      //Do NOT persist the direction yet, just test....
      departureBlock.setLogicalDirection(newDirection.getDirection());
      //Now flip the departure direction
      if ("-".equals(departureSuffix)) {
        departureSuffix = "+";
      } else {
        departureSuffix = "-";
      }

      Logger.trace("2nd attempt for Loco " + locomotive.getName() + " is in block " + departureBlock.getId() + ". Direction " + newDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");
      routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);

      Logger.trace("After the 2nd attempt, there " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s). " + (!routes.isEmpty() ? "Direction of " + locomotive.getName() + " must be swapped!" : ""));
    }

    //Found possible routes check on the destination for the sensors and permissions
    for (RouteBean route : routes) {
      String destinationTileId = route.getToTileId();
      BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(destinationTileId);
      //Check the sensors 
      boolean plusInActive = !destinationBlock.getPlusSensorBean().isActive();
      boolean minInActive = !destinationBlock.getMinSensorBean().isActive();

      boolean allowCommuter = destinationBlock.isAllowCommuterOnly();
      boolean allowNonCommuter = destinationBlock.isAllowNonCommuterOnly();

      boolean allowed = isAllowed(allowCommuter, allowNonCommuter, commuter);

      Logger.trace("Destination " + destinationBlock.getId() + " Train type commuter: " + commuter + " Permission " + allowed + " sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));

      if (plusInActive && minInActive && allowed && turnoutsNotLocked(route)) {
        checkedRoutes.add(route);
      }
    }

    //Randomly pick a route in case multiple routes are found...
    int rIdx = 0;
    if (checkedRoutes.size() > 1) {
      //Choose randomly the route
      Random random = new Random();
      for (int i = 0; i < 10; i++) {
        //Seed a bit....
        random.ints(0, checkedRoutes.size()).findFirst();
      }
      rIdx = random.ints(0, checkedRoutes.size()).findFirst().getAsInt();
    }

    RouteBean route = null;
    if (!checkedRoutes.isEmpty()) {
      route = checkedRoutes.get(rIdx);
      Logger.trace("Choosen route " + route.toLogString());
      //persist the departure block
      PersistenceFactory.getService().persist(departureBlock);
    } else {
      if (locCount >= minLocCount) {
        Logger.debug("No route available for " + locomotive.getName() + " ...");
      } else {
        Logger.debug("No route available because the Station occupation (" + locCount + ") is less then the min occupation (" + minLocCount + ")");
      }
      if (swapLocomotiveDirection) {
        //Reverse the direction change
        Direction oldDirection = LocomotiveBean.toggle(Direction.get(departureBlock.getLogicalDirection()));
        Logger.trace("Rollback Locomotive reverse to " + oldDirection + "...");
        departureBlock.setLogicalDirection(oldDirection.getDirection());
      }
    }
    setRouteBean(route);
    return route != null;
  }

  boolean reserveRoute() {
    LocomotiveBean locomotive = getLocomotiveBean();
    RouteBean route = getRouteBean();

    if (route == null) {
      return false;
    }
    Logger.debug("Reserving route " + route);
    route.setLocked(true);

    //Reserve the destination
    String destinationTileId = route.getToTileId();
    String arrivalSuffix = route.getToSuffix();
    String departureSuffix = route.getFromSuffix();

    Logger.debug("Destination: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block. Loco direction: " + locomotive.getDispatcherDirection());

    BlockBean departureBlock = getDepartureBlock();
    departureBlock.setBlockState(BlockBean.BlockState.OCCUPIED);

    departureBlock.setDepartureSuffix(route.getFromSuffix());

    BlockBean destinationBlock = getDestinationBlock();
    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setArrivalSuffix(arrivalSuffix);
    destinationBlock.setLogicalDirection(departureBlock.getLogicalDirection());

    // Set Turnouts in the right state
    List<RouteElementBean> turnouts = getTurnouts(route);
    Logger.trace("There are " + turnouts.size() + " turnouts in this route");

    //Now start to persist and perform critical thinks
    if (turnoutsNotLocked(route)) {
      PersistenceFactory.getService().persist(route);

      for (RouteElementBean reb : turnouts) {
        AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        switchAccessory(turnout, av);

        //TODO: configurable wait time between switches
        pause(500);
      }
      Logger.trace("Turnouts set for " + route);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      //Now that we have reserved the route lets determine which sensors are playing a role.
      //On the departure side we have the OccupiedSensor, i.e. the IN sensor when arriving.
      //The exit sensor i.e the last sensor to leave the departure block.
      Integer occupancySensorId; //, exitSensorId;
      if ("+".equals(departureSuffix)) {
        occupancySensorId = departureBlock.getMinSensorId();
        exitSensorId = departureBlock.getPlusSensorId();
      } else {
        occupancySensorId = departureBlock.getPlusSensorId();
        exitSensorId = departureBlock.getMinSensorId();
      }
      setOccupationSensorId(occupancySensorId);
      setExitSensorId(exitSensorId);

      //On the destination side we have the enterSensor end the IN sensor.
      //From which side on the block is the train expected to arrive?
      //Integer enterSensorId, inSensorId;
      if ("+".equals(arrivalSuffix)) {
        enterSensorId = destinationBlock.getPlusSensorId();
        inSensorId = destinationBlock.getMinSensorId();
      } else {
        enterSensorId = destinationBlock.getMinSensorId();
        inSensorId = destinationBlock.getPlusSensorId();
      }

      setEnterSensorId(enterSensorId);
      setInSensorId(inSensorId);

      Logger.trace("Departure: " + departureBlock.getId() + " Occupancy Sensor: " + occupancySensorId + " Exit Sensor: " + exitSensorId);
      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + enterSensorId + " In Sensor: " + inSensorId);

      showRoute(route, Color.green);
      Logger.trace(route + " Locked");

      if (swapLocomotiveDirection) {
        Direction newDir = Direction.get(departureBlock.getLogicalDirection());
        Logger.trace("Changing Direction to " + newDir);
        changeLocomotiveDirection(newDir);
      }

      showBlockState(departureBlock);
      showBlockState(destinationBlock);

      return true;
    } else {
      //Can't lock route
      route.setLocked(false);
      PersistenceFactory.getService().persist(route);
      Logger.trace(route + " NOT Locked");

      return false;
    }
  }

  boolean reserveNextRoute() {
    LocomotiveBean locomotive = getLocomotiveBean();
    RouteBean nextRoute = getNextRouteBean();

    if (nextRoute == null) {
      return false;
    }
    Logger.debug("Reserving next route " + nextRoute);
    nextRoute.setLocked(true);

    //Reserve the destination
    String nextDestinationTileId = nextRoute.getToTileId();
    String nextArrivalSuffix = nextRoute.getToSuffix();
    Logger.debug("Next Destination: " + nextDestinationTileId + " Arrival on the " + nextArrivalSuffix + " side of the block. Loco direction: " + locomotive.getDispatcherDirection());

    BlockBean departureBlock = getDestinationBlock();
    BlockBean nextDestinationBlock = getNextDestinationBlock();

    nextDestinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    nextDestinationBlock.setLocomotive(locomotive);
    nextDestinationBlock.setArrivalSuffix(nextArrivalSuffix);
    nextDestinationBlock.setLogicalDirection(departureBlock.getLogicalDirection());

    // Set Turnouts in the right state
    List<RouteElementBean> turnouts = getTurnouts(nextRoute);
    Logger.trace("There are " + turnouts.size() + " turnouts in the next route");

    //Now start to persist and perform critical thinks
    if (turnoutsNotLocked(nextRoute)) {
      PersistenceFactory.getService().persist(nextRoute);

      for (RouteElementBean reb : turnouts) {
        AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        switchAccessory(turnout, av);
        //TODO configurable wait time between switches
        pause(500);
      }
      Logger.trace("Turnouts set for " + nextRoute);

      PersistenceFactory.getService().persist(nextDestinationBlock);

      showRoute(nextRoute, Color.yellow);
      Logger.trace(nextRoute + " Locked");

      showBlockState(nextDestinationBlock);

      return true;
    } else {
      //Can't lock nextRoute
      nextRoute.setLocked(false);
      PersistenceFactory.getService().persist(nextRoute);
      Logger.trace(nextRoute + " NOT Locked");

      return false;
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
