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

import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import org.tinylog.Logger;

/**
 * Reserve the exit and entry sensors
 */
class StartState extends DispatcherState {

  private boolean locomotiveStarted = false;

  StartState(TrainDispatcher dispatcher, boolean running) {
    super(dispatcher, running);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    if (dispatcher.isEnterDestinationBlock()) {
      DispatcherState newState = new EnterBlockState(this.dispatcher, isRunning());
      locRunner.setDispatcherState(newState);
    } else {
      locRunner.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();

    if (!locomotiveStarted) {
      //Which sensors do we need to watch
      RouteBean route = dispatcher.getRouteBean();
      String destinationTileId = route.getToTileId();
      //From which side on the block is the train expected to arrive?
      String arrivalSuffix = route.getToSuffix();
      Logger.trace("Destination tile: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block");

      BlockBean departureBlock = dispatcher.getDepartureBlock();
      //The sensors on the departure block do not yet play a role,
      //but they can switch on so they have to be removed from the ghost list
      String exitMinId = departureBlock.getMinSensorId();
      String exitPlusId = departureBlock.getPlusSensorId();

      BlockBean destinationBlock = dispatcher.getDestinationBlock();

      String enterSensorId;
      if ("+".equals(arrivalSuffix)) {
        enterSensorId = destinationBlock.getPlusSensorId();
      } else {
        enterSensorId = destinationBlock.getMinSensorId();
      }

      //Register handlers
      this.dispatcher.registerIgnoreEventHandler(exitMinId);
      this.dispatcher.registerIgnoreEventHandler(exitPlusId);

      this.dispatcher.registerEnterHandler(enterSensorId);
      Logger.debug("Enter SensorId: " + enterSensorId + " Ignore Departure Sensors minId: " + exitMinId + ", plusId: " + exitPlusId);

      //TODO rely on the acceleration delay of the loco decoder or do something our selves..
      this.dispatcher.changeLocomotiveVelocity(locomotive, 750);
      locomotiveStarted = true;

      Logger.trace("Waiting for the enter event from SensorId: " + dispatcher.getEnterSensorId() + " Running loco: " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " current velocity: " + locomotive.getVelocity());
    }
    canAdvanceToNextState = dispatcher.isEnterDestinationBlock();
  }

}
