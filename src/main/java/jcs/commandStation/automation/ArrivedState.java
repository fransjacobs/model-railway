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
import java.util.Date;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.StationBean;
import jcs.entities.StationBlockBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Arrive or InBlock State. The state when a train has arrived in the destination block.<br>
 */
class ArrivedState extends AbstractState {

  boolean alwaysStop = true;

  public ArrivedState() {
    super("Arrived");
  }

  /**
   * Handle the arrival. Free the departure block. Occupy the destination block. Reset the Route.<br>
   * When the Locomotive has to stop send the stop command to the Command Station.<br>
   *
   * @param dispatcher
   */
  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    BlockBean departureBlock = dispatcher.getDepartureBlock();

    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    alwaysStop = destinationBlock.isAlwaysStop();

    Logger.trace("Locomotive " + locomotive.getName() + " has arrived in " + destinationBlock.getDescription() + " and " + (alwaysStop ? "must stop" : " may continue"));

    if (alwaysStop || dispatcher.getNextRouteBean() == null || !dispatcher.isLocomotiveStarted()) {
      //Stop the locomotive
      dispatcher.changeLocomotiveVelocity(0);
      Logger.trace("Locomotive " + locomotive.getName() + " is stopped...");
    }

    //Enable the sensors of the departure block
    dispatcher.getSensorMonitor().unsubscribe(dispatcher.getOccupationSensorId(), null);
    dispatcher.getSensorMonitor().unsubscribe(dispatcher.getExitSensorId(), null);

    dispatcher.setOccupationSensorId(null);
    dispatcher.setExitSensorId(null);

    departureBlock.setBlockState(BlockBean.BlockState.FREE);
    departureBlock.setLocomotive(null);
    departureBlock.setArrivalSuffix(null);
    departureBlock.setLogicalDirection(null);

    PersistenceFactory.getService().persist(departureBlock);

    destinationBlock.setBlockState(BlockBean.BlockState.OCCUPIED);
    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setLogicalDirection(locomotive.getDirection().getDirection());
    destinationBlock.setArrivalSuffix(dispatcher.getRouteBean().getToSuffix());

    PersistenceFactory.getService().persist(destinationBlock);

    RouteBean route = dispatcher.getRouteBean();
    route.setLocked(false);
    PersistenceFactory.getService().persist(route);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showBlockState(destinationBlock);

    dispatcher.resetRoute(route);
    dispatcher.setRouteBean(null);

    //Is the destinationBlock block part of a station.
    StationBean station = dispatcher.getStation(destinationBlock);
    if (station != null) {
      //Set the arrival time
      StationBlockBean sbb = station.getStationBlockBean(destinationBlock);
      sbb.setLastUpdated(new Date());

      int locCount = station.getLocomotiveCount();
      locCount = locCount + 1;
      station.setLocomotiveCount(locCount);
      PersistenceFactory.getService().persist(station);
    }
  }

  @Override
  AbstractState execute() {
    if (dispatcher.getNextRouteBean() != null) {
      //Now setup the next route
      RouteBean route = dispatcher.getNextRouteBean();
      dispatcher.setRouteBean(route);
      // New Departure and destination block should be set...
      BlockBean departureBlock = dispatcher.getDepartureBlock();
      BlockBean destinationBlock = dispatcher.getDestinationBlock();

      //String destinationTileId = route.getToTileId();
      String arrivalSuffix = route.getToSuffix();
      String departureSuffix = route.getFromSuffix();

      //Now that we have set the next route lets determine which sensors are playing a role.
      //On the departure side we have the OccupiedSensor, ie the IN sensor when arriving.
      //The exit sensor i.e the last sensor to leave the departure block.
      Integer occupancySensorId, exitSensorId;
      if ("+".equals(departureSuffix)) {
        occupancySensorId = departureBlock.getMinSensorId();
        exitSensorId = departureBlock.getPlusSensorId();
      } else {
        occupancySensorId = departureBlock.getPlusSensorId();
        exitSensorId = departureBlock.getMinSensorId();
      }
      dispatcher.setOccupationSensorId(occupancySensorId);
      dispatcher.setExitSensorId(exitSensorId);

      //On the destination side we have the enterSensor end the IN sensor.
      //From which side on the block is the train expected to arrive?
      Integer enterSensorId, inSensorId;
      if ("+".equals(arrivalSuffix)) {
        enterSensorId = destinationBlock.getPlusSensorId();
        inSensorId = destinationBlock.getMinSensorId();
      } else {
        enterSensorId = destinationBlock.getMinSensorId();
        inSensorId = destinationBlock.getPlusSensorId();
      }

      dispatcher.setEnterSensorId(enterSensorId);
      dispatcher.setInSensorId(inSensorId);

      Logger.trace("Departure: " + departureBlock.getId() + " Occupancy Sensor: " + occupancySensorId + " Exit Sensor: " + exitSensorId);
      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + enterSensorId + " In Sensor: " + inSensorId);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      //dispatcher.showRoute(route, Color.green);
      dispatcher.getRouteManager().showRoute(route, Color.green);

      //Clear the next routes
      dispatcher.setNextRouteBean(null);

      return new DepartingState();
    } else {
      if (dispatcher.isLocomotiveStarted() && alwaysStop) {
        return new WaitingState();
      } else {
        return new PrepareRouteState();
      }
    }
  }

  @Override
  void onExit() {

  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }

}
