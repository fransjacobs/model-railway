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
import java.util.List;
import java.util.Random;
import jcs.JCS;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Block;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * RouteManager is in charge of finding a route.
 */
class RouteManager {

  private final Dispatcher dispatcher;
  private volatile boolean swapLocomotiveDirection;
  private volatile Direction logicalDirection;
  private volatile RouteBean route;

  RouteManager(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  boolean searchAndReserveRoute() {
    if (searchRoute()) {
      return reserveRoute();
    } else {
      return false;
    }
  }

  boolean searchRoute() {
    Logger.trace("Search a free route for " + dispatcher.getName() + "...");
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    Direction locomotiveDirection = locomotive.getDirection();
    BlockBean departureBlock = PersistenceFactory.getService().getBlock(dispatcher.getDepartureBlockId());

    if (departureBlock.getLogicalDirection() == null) {
      departureBlock.setLogicalDirection(locomotive.getDirection().getDirection());
      Logger.trace("Setting departure Block logicalDirection to: " + departureBlock.getLogicalDirection());
      PersistenceFactory.getService().persist(departureBlock);
    }

    logicalDirection = LocomotiveBean.Direction.get(departureBlock.getLogicalDirection());

    if (locomotiveDirection != logicalDirection) {
      Logger.warn(dispatcher.getName() + " Locomotive Direction " + locomotiveDirection + " differs with the logica lDirection " + logicalDirection + "!");
    }

    TileBean tileBean = PersistenceFactory.getService().getTileBean(departureBlock.getTileId());
    TileBean.Orientation blockOrientation = tileBean.getOrientation();

    String departureSuffix = departureBlock.getDepartureSuffix();
    if (departureSuffix == null) {
      departureSuffix = Block.getDepartureSuffix(blockOrientation, logicalDirection);
    }

    Logger.trace("Loco " + dispatcher.getName() + " is in block " + departureBlock.getId() + ". Direction " + logicalDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
    Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");

    List<RouteBean> checkedRoutes = new ArrayList<>();
    boolean commuter = locomotive.isCommuter();

    //No routes found or possible.
    //When the Locomotive is a commuter train and the departure block allows a direction change, may be a route is available.
    //Reverse the direction and try again...
    Direction oldDirection = logicalDirection;
    if (routes.isEmpty() && commuter && departureBlock.isAllowDirectionChange()) {
      Direction newDirection = LocomotiveBean.toggle(oldDirection);
      Logger.trace("Reversing " + dispatcher.getName() + " from " + oldDirection + " to " + newDirection + " re-try to find a route...");

      //Do NOT persist the new direction yet, just test....
      departureBlock.setLogicalDirection(newDirection.getDirection());
      //Now flip the departure direction
      if ("-".equals(departureSuffix)) {
        departureSuffix = "+";
      } else {
        departureSuffix = "-";
      }

      Logger.trace("2nd attempt for Loco " + dispatcher.getName() + " is in block " + departureBlock.getId() + ". Direction " + newDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");
      routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
      Logger.trace("After the 2nd attempt, there " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s). " + (!routes.isEmpty() ? "Direction of " + locomotive.getName() + " must be swapped!" : ""));
      swapLocomotiveDirection = !routes.isEmpty();
    }

    //Check the possible routes, check on the destination for active sensors and permissions
    for (RouteBean possibleRoute : routes) {
      String destinationTileId = possibleRoute.getToTileId();
      BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(destinationTileId);
      //Check the sensors 
      boolean plusInActive = !destinationBlock.getPlusSensorBean().isActive();
      boolean minInActive = !destinationBlock.getMinSensorBean().isActive();

      boolean allowCommuter = destinationBlock.isAllowCommuterOnly();
      boolean allowNonCommuter = destinationBlock.isAllowNonCommuterOnly();

      boolean allowed = isAllowed(allowCommuter, allowNonCommuter, commuter);

      Logger.trace("Destination " + destinationBlock.getId() + " Train type commuter: " + commuter + " Permission " + allowed + " sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));

      if (plusInActive && minInActive && allowed && turnoutsNotLocked(possibleRoute)) {
        checkedRoutes.add(possibleRoute);
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

    route = null;
    if (!checkedRoutes.isEmpty()) {
      route = checkedRoutes.get(rIdx);
      Logger.trace("Choosen route " + route.toLogString());
    }
    return route != null;
  }

  boolean reserveRoute() {
    if (this.route == null) {
      return false;
    }
    Logger.debug("Reserving route " + route);
    route.setLocked(true);

    //Reserve the destination
    String destinationTileId = route.getToTileId();
    String arrivalSuffix = route.getToSuffix();
    String departureSuffix = route.getFromSuffix();

    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    Logger.debug("Destination: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block. Loco direction: " + locomotive.getDirection().getDirection());

    BlockBean departureBlock = PersistenceFactory.getService().getBlockByTileId(route.getFromTileId());
    departureBlock.setBlockState(BlockBean.BlockState.OCCUPIED);
    departureBlock.setDepartureSuffix(route.getFromSuffix());

    BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(route.getToTileId());
    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setArrivalSuffix(arrivalSuffix);
    destinationBlock.setLogicalDirection(locomotive.getDirection().getDirection());

    // Set Turnouts in the right state
    List<RouteElementBean> turnouts = getTurnouts(route);
    Logger.trace("There are " + turnouts.size() + " turnouts in this route");

    //Now start to persist and perform critical thinks
    if (turnoutsNotLocked(route)) {
      PersistenceFactory.getService().persist(route);

      for (RouteElementBean reb : turnouts) {
        AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        switchAccessory(turnout, av);

        //TODO: configurable wait time between switches
        pause(250);
      }
      Logger.trace("Turnouts set for " + route);

      //Now that we have reserved the route lets determine which sensors are playing a role.
      //On the departure side we have the OccupiedSensor, i.e. the IN sensor when arriving.
      //The exit sensor i.e the last sensor to leave the departure block.
      if ("+".equals(departureSuffix)) {
        dispatcher.setOccupationSensorId(departureBlock.getMinSensorId());
        dispatcher.setExitSensorId(departureBlock.getPlusSensorId());
      } else {
        dispatcher.setOccupationSensorId(departureBlock.getPlusSensorId());
        dispatcher.setExitSensorId(departureBlock.getMinSensorId());
      }

      //On the destination side we have the enterSensor end the IN sensor.
      //From which side on the block is the train expected to arrive?
      //Integer enterSensorId, inSensorId;
      if ("+".equals(arrivalSuffix)) {
        dispatcher.setEnterSensorId(destinationBlock.getPlusSensorId());
        dispatcher.setInSensorId(destinationBlock.getMinSensorId());
      } else {
        dispatcher.setEnterSensorId(destinationBlock.getMinSensorId());
        dispatcher.setInSensorId(destinationBlock.getPlusSensorId());
      }

      Logger.trace("Departure: " + departureBlock.getId() + " Occupancy Sensor: " + dispatcher.getOccupationSensorId() + " Exit Sensor: " + dispatcher.getExitSensorId());
      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + dispatcher.getEnterSensorId() + " In Sensor: " + dispatcher.getInSensorId());

      showRoute(route, Color.green);
      Logger.trace(route + " Locked");

      if (swapLocomotiveDirection) {
        //LocomotiveBean.Direction curDir = locomotive.getDirection();
        Direction newDir = LocomotiveBean.toggle(logicalDirection);
        Logger.trace("Swapping direction of " + dispatcher.getName() + " from: " + logicalDirection + " Swap to " + newDir);
        Logger.trace("Changing Direction to " + newDir);
        dispatcher.changeLocomotiveDirection(newDir);
        //Wait a while to let the command station executing the command
        pause(500);
        swapLocomotiveDirection = false;
      }

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);
      dispatcher.setRouteBean(route);

      return true;
    } else {
      //Can't lock route
      route.setLocked(false);
      PersistenceFactory.getService().persist(route);
      Logger.trace(route + " NOT Locked");
      dispatcher.setRouteBean(null);
      swapLocomotiveDirection = false;
      return false;
    }
  }

  Integer getEstimatedNextRouteSwitchTime() {
    RouteBean nextRoute = dispatcher.getNextRouteBean();
    //Return a default switchtime 
    int avgSwitchTime = PersistenceFactory.getService().getAverageAccessorySwitchTime(nextRoute).intValue();
    avgSwitchTime = avgSwitchTime + 250;

    return avgSwitchTime;
  }

  boolean searchAndReserveNextRoute() {
    if (searchNextRoute()) {
      return reserveNextRoute();
    } else {
      return false;
    }
  }

  boolean searchNextRoute() {
    Logger.trace("Search a free next route for " + dispatcher.getLocomotiveBean().getName() + "...");

    // In this state the we are checking whether there is a valid nextRoute from the destination to the next block.
    BlockBean departureBlock = dispatcher.getDestinationBlock();

    //Use the current running locomotive direction
    LocomotiveBean.Direction nextLogicalDirection = dispatcher.getLocomotiveBean().getDirection();

    TileBean tileBean = PersistenceFactory.getService().getTileBean(departureBlock.getTileId());
    TileBean.Orientation blockOrientation = tileBean.getOrientation();

    String departureSuffix = departureBlock.getDepartureSuffix();
    if (departureSuffix == null) {
      departureSuffix = Block.getDepartureSuffix(blockOrientation, nextLogicalDirection);
    }

    Logger.trace("Loco " + dispatcher.getLocomotiveBean().getName() + " is entering block " + departureBlock.getId() + ". Direction " + nextLogicalDirection.getDirection() + ". DepartureSuffix " + departureSuffix + "...");

    //Search for the possible routes
    //List<RouteBean> routes = Collections.emptyList();
    //Is the departure block part of a station.
    //Search for the possible routes
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(departureBlock.getId(), departureSuffix);
    Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");

    List<RouteBean> checkedRoutes = new ArrayList<>();
    //Found possible routes check on the destination for the sensors and permissions
    for (RouteBean nextRoute : routes) {
      String nextDestinationTileId = nextRoute.getToTileId();
      BlockBean nextDestinationBlock = PersistenceFactory.getService().getBlockByTileId(nextDestinationTileId);
      //Check the sensors 
      boolean plusInActive = !nextDestinationBlock.getPlusSensorBean().isActive();
      boolean minInActive = !nextDestinationBlock.getMinSensorBean().isActive();

      boolean allowCommuter = nextDestinationBlock.isAllowCommuterOnly();
      boolean allowNonCommuter = nextDestinationBlock.isAllowNonCommuterOnly();

      boolean allowed = isAllowed(allowCommuter, allowNonCommuter, dispatcher.getLocomotiveBean().isCommuter());

      Logger.trace("Next Destination " + nextDestinationBlock.getId() + " Train type commuter: " + dispatcher.getLocomotiveBean().isCommuter() + " Permission " + allowed + " sensor: " + (plusInActive ? "Free" : "Occupied") + " - sensor: " + (minInActive ? "Free" : "Occupied"));

      if (plusInActive && minInActive && allowed && turnoutsNotLocked(nextRoute)) {
        checkedRoutes.add(nextRoute);
      }
    }

    //Randomly pick a nextRoute in case multiple routes are found...
    int rIdx = 0;
    if (checkedRoutes.size() > 1) {
      //Choose randomly the nextRoute
      Random random = new Random();
      for (int i = 0; i < 10; i++) {
        //Seed a bit....
        random.ints(0, checkedRoutes.size()).findFirst();
      }
      rIdx = random.ints(0, checkedRoutes.size()).findFirst().getAsInt();
    }

    RouteBean nextRoute = null;
    if (!checkedRoutes.isEmpty()) {
      nextRoute = checkedRoutes.get(rIdx);
      Logger.trace("Choosen route " + nextRoute.toLogString());
    }
    dispatcher.setNextRouteBean(nextRoute);
    return nextRoute != null;
  }

  boolean reserveNextRoute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    RouteBean nextRoute = dispatcher.getNextRouteBean();

    if (nextRoute == null) {
      return false;
    }
    Logger.debug("Reserving next route " + nextRoute);
    nextRoute.setLocked(true);

    //Reserve the destination
    String nextDestinationTileId = nextRoute.getToTileId();
    String nextArrivalSuffix = nextRoute.getToSuffix();
    Logger.debug("Next Destination: " + nextDestinationTileId + " Arrival on the " + nextArrivalSuffix + " side of the block. Loco direction: " + locomotive.getDispatcherDirection());

    BlockBean departureBlock = dispatcher.getDestinationBlock();
    BlockBean nextDestinationBlock = dispatcher.getNextDestinationBlock();

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

      int turnoutCount = turnouts.size();
      int turnoutIdx = 0;
      for (RouteElementBean reb : turnouts) {
        AccessoryBean.AccessoryValue av = reb.getAccessoryValue();
        AccessoryBean turnout = reb.getTileBean().getAccessoryBean();
        Logger.debug("Setting Turnout " + turnout.getName() + " [" + turnout.getAddress() + "] to : " + av.getValue());
        switchAccessory(turnout, av);
        turnoutIdx++;
        //No need to wait...
        if (turnoutCount > turnoutIdx) {
          //TODO configurable wait time between switches
          pause(100);
        }
      }
      Logger.trace("Turnouts set for " + nextRoute);

      PersistenceFactory.getService().persist(nextDestinationBlock);
      PersistenceFactory.getService().persist(nextRoute);
      dispatcher.setNextRouteBean(nextRoute);

      showRoute(nextRoute, Color.yellow);
      Logger.trace(nextRoute + " Locked");

      dispatcher.showBlockState(nextDestinationBlock);

      return true;
    } else {
      //Can't lock nextRoute
      nextRoute.setLocked(false);
      PersistenceFactory.getService().persist(nextRoute);
      Logger.trace(nextRoute + " NOT Locked");

      return false;
    }
  }

  void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private volatile boolean switched;

  void switchAccessory(AccessoryBean accessory, AccessoryBean.AccessoryValue value) {
    AccessoryListener al = new AccessoryListener(accessory, value, this);
    try {
      switched = false;
      JCS.getJcsCommandStation().addAccessoryEventListener(accessory.getId(), al);
      long now = System.currentTimeMillis();
      long timemax = now + 5000;
      long started = now;

      JCS.getJcsCommandStation().switchAccessory(accessory, value);
      Logger.trace("Switched Accessory " + accessory.getId() + " to " + value + " Waiting for confirmation...");

      while (!switched && now < timemax) {
        now = System.currentTimeMillis();
        pause(10);
      }
      Logger.trace("Accessory " + accessory.getId() + " has switched to " + value + " in " + (now - started) + " ms.");
    } catch (Exception e) {
      Logger.error("Error switching accessory " + accessory.getId() + " to " + value + " Cause: " + e.getMessage());
    } finally {
      JCS.getJcsCommandStation().removeAccessoryEventListener(accessory.getId(), al);
    }
  }

  void waitForAccessory(boolean switched) {
    this.switched = switched;
  }

  private boolean isAllowed(boolean allowCommuter, boolean allowNonCommuter, boolean commuter) {
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

  private boolean turnoutsNotLocked(RouteBean route) {
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

  private List<RouteElementBean> getTurnouts(RouteBean routeBean) {
    List<RouteElementBean> rel = routeBean.getRouteElements();
    List<RouteElementBean> turnouts = new ArrayList<>();
    for (RouteElementBean reb : rel) {
      if (reb.isTurnout()) {
        turnouts.add(reb);
      }
    }
    return turnouts;
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

  private class AccessoryListener implements AccessoryEventListener {

    private final AccessoryBean accessoryBean;
    private final RouteManager routeManager;
    private final AccessoryBean.AccessoryValue value;

    AccessoryListener(AccessoryBean accessoryBean, AccessoryBean.AccessoryValue value, RouteManager callback) {
      this.accessoryBean = accessoryBean;
      this.routeManager = callback;
      this.value = value;
    }

    @Override
    public void onAccessoryChange(AccessoryEvent accessoryEvent) {
      if (accessoryEvent.isEventFor(accessoryBean)) {
        routeManager.waitForAccessory(value == accessoryEvent.getValue());
      }
    }

  }

}
