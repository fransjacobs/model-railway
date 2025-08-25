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
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class InBlockState extends DispatcherState {

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();

    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    BlockBean departureBlock = dispatcher.getDepartureBlock();

    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setBlockState(BlockBean.BlockState.OCCUPIED);
    destinationBlock.setLogicalDirection(locomotive.getDirection().getDirection());
    destinationBlock.setArrivalSuffix(dispatcher.getRouteBean().getToSuffix());

    boolean alwaysStop = destinationBlock.isAlwaysStop();

    Logger.trace("Locomotive " + locomotive.getName() + " has arrived in destination " + destinationBlock.getDescription() + " and must stop " + alwaysStop);

    if (alwaysStop || dispatcher.getNextRouteBean() == null || !dispatcher.isLocomotiveAutomodeOn()) {
      //Stop the locomotive
      dispatcher.changeLocomotiveVelocity(locomotive, 0);
      Logger.trace("Locomotive " + locomotive.getName() + " is stopped....");
    }

    //Switch the departure block sensors on again
    dispatcher.clearDepartureIgnoreEventHandlers();
    dispatcher.setOccupationSensorId(null);
    dispatcher.setExitSensorId(null);

    departureBlock.setBlockState(BlockBean.BlockState.FREE);
    departureBlock.setLocomotive(null);
    departureBlock.setArrivalSuffix(null);
    departureBlock.setLogicalDirection(null);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    RouteBean route = dispatcher.getRouteBean();
    route.setLocked(false);
    PersistenceFactory.getService().persist(route);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showBlockState(destinationBlock);

    Dispatcher.resetRoute(route);
    dispatcher.setRouteBean(null);

    if (dispatcher.getNextRouteBean() != null && dispatcher.isLocomotiveAutomodeOn()) {
      //Now setup the next route
      route = dispatcher.getNextRouteBean();
      dispatcher.setRouteBean(route);
      // New Departure and destination block should be set...
      departureBlock = dispatcher.getDepartureBlock();
      destinationBlock = dispatcher.getDestinationBlock();

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

      dispatcher.showRoute(route, Color.green);

      //Clear the next routes
      dispatcher.setNextRouteBean(null);

      return new StartState();
    } else {
      if (dispatcher.isLocomotiveAutomodeOn()) {
        if (alwaysStop) {
          return new WaitState();
        } else {
          return new PrepareRouteState();
        }
      } else {
        return new IdleState();
      }
    }
  }

}
