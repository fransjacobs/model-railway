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

import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Reserve the exit and entry sensors
 */
class StartState extends DispatcherState implements SensorEventListener {

  private boolean locomotiveStarted = false;
  private boolean canAdvanceToNextState = false;
  private String enterSensorId;

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    if (!locomotiveStarted) {
      BlockBean departureBlock = dispatcher.getDepartureBlock();
      BlockBean destinationBlock = dispatcher.getDestinationBlock();

      String occupancySensorId = dispatcher.getOccupationSensorId();
      String exitSensorId = dispatcher.getExitSensorId();

      //Register them both to ignore event form these sensors.
      dispatcher.registerIgnoreEventHandler(occupancySensorId);
      dispatcher.registerIgnoreEventHandler(exitSensorId);
      Logger.trace("Departure: " + departureBlock.getId() + " Ignoring Occupancy Sensor: " + occupancySensorId + " and Exit Sensor: " + exitSensorId);

      //The enter Sensor triggering will switch states.
      enterSensorId = dispatcher.getEnterSensorId();
      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + enterSensorId + "...");

      //Register this state as a SensorEventListener
      JCS.getJcsCommandStation().addSensorEventListener(this);
      dispatcher.setWaitForSensorid(enterSensorId);

      departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
      PersistenceFactory.getService().persist(departureBlock);

      destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
      PersistenceFactory.getService().persist(destinationBlock);

      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);

      //TODO: for now rely on the acceleration delay of the loco decoder. Future make a smooth accelerator our selves..
      Logger.trace("Starting " + locomotive.getName() + " Direction " + locomotive.getDirection());
      //Speed to 70%
      //TODO: get the speed settings from locomotive
      dispatcher.changeLocomotiveVelocity(locomotive, 700);

      locomotiveStarted = true;
      Logger.trace("Waiting for the enter event from SensorId: " + enterSensorId + " Running loco: " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " current velocity: " + locomotive.getVelocity());
    }

    if (canAdvanceToNextState) {
      DispatcherState newState = new EnterBlockState();
      //Remove handler as the state will now change
      JCS.getJcsCommandStation().removeSensorEventListener(this);
      //For the remaining states ignore events from the enter sensor
      dispatcher.registerIgnoreEventHandler(enterSensorId);

      return newState;
    } else {
      return this;
    }
  }

  @Override
  public void onSensorChange(SensorEvent sensorEvent) {
    if (enterSensorId.equals(sensorEvent.getId())) {
      if (sensorEvent.isActive()) {
        canAdvanceToNextState = true;
        Logger.trace("Enter Event from Sensor " + sensorEvent.getId());
        synchronized (this) {
          notify();
        }
      }
    }
  }

}
