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
import org.tinylog.Logger;

/**
 * Reserve the exit and entry sensors
 */
class StartState extends DispatcherState implements SensorEventListener {

  private boolean locomotiveStarted = false;
  private String enterSensorId;

  StartState(LocomotiveDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  DispatcherState next(LocomotiveDispatcher locRunner) {
    if (canAdvanceToNextState) {
      DispatcherState newState = new EnterBlockState(dispatcher);
      //Remove handler as the state will now change
      JCS.getJcsCommandStation().removeSensorEventListener(this);
      //For the remaining states ignore events from the enter sensor
      this.dispatcher.registerIgnoreEventHandler(enterSensorId);
      
      return newState;
    } else {
      return this;
    }
  }

  @Override
  public void execute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    if (!locomotiveStarted) {
      //Which sensors do we need to watch?
      RouteBean route = dispatcher.getRouteBean();
      BlockBean departureBlock = dispatcher.getDepartureBlock();

      //From which side on the block is the train expected to arrive?
      String arrivalSuffix = route.getToSuffix();
      Logger.trace("Destination tile: " + departureBlock.getId() + " Arrival on the " + arrivalSuffix + " side of the block");

      //The sensors on the departure block do not yet play a role,
      //but they can switch on so they have to be removed from the ghost list
      String exitMinId = departureBlock.getMinSensorId();
      String exitPlusId = departureBlock.getPlusSensorId();

      //Should already be in the ignore list... just to sure...
      this.dispatcher.registerIgnoreEventHandler(exitMinId);
      this.dispatcher.registerIgnoreEventHandler(exitPlusId);

      BlockBean destinationBlock = dispatcher.getDestinationBlock();

      if ("+".equals(arrivalSuffix)) {
        enterSensorId = destinationBlock.getPlusSensorId();
      } else {
        enterSensorId = destinationBlock.getMinSensorId();
      }

      //Register this state as a SensorEventListener
      JCS.getJcsCommandStation().addSensorEventListener(this);
      //Remove the enter sensor from the ghost detection
      this.dispatcher.registerIgnoreEventHandler(enterSensorId);

      Logger.debug("Enter SensorId: " + enterSensorId + " Ignoring Departure Sensors minId: " + exitMinId + ", plusId: " + exitPlusId);

      //TODO rely on the acceleration delay of the loco decoder or do something our selves..
      dispatcher.changeLocomotiveVelocity(locomotive, 600);

      locomotiveStarted = true;
      Logger.trace("Waiting for the enter event from SensorId: " + this.enterSensorId + " Running loco: " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " current velocity: " + locomotive.getVelocity());
    }
  }

  @Override
  public void onSensorChange(SensorEvent sensorEvent) {
    if (this.enterSensorId.equals(sensorEvent.getId())) {
      if (sensorEvent.isActive()) {
        this.canAdvanceToNextState = true;
        Logger.trace("Enter Event from Sensor " + sensorEvent.getId());
        synchronized (this) {
          notify();
        }
      }
    }
  }

}
