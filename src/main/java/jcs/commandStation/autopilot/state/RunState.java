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
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Reserve the exit and entry sensors
 */
public class RunState extends DispatcherState {

  public RunState(TrainDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    Logger.trace("canAdvanceState: " + canAdvanceToNextState);
    if (canAdvanceToNextState) {
      DispatcherState newState = new ArrivalState(this.dispatcher);
      newState.setRunning(running);
      locRunner.setDispatcherState(newState);
    } else {
      locRunner.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
    //Which sensors do we need to watch
    RouteBean route = dispatcher.getRouteBean();
    String departureTileId = route.getFromTileId();
    String destinationTileId = route.getToTileId();
    //From which side on the block is the train expected to arrive?
    String arrivalSuffix = route.getToSuffix();

    Logger.trace("Destination tile: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block");
    BlockBean departureBlock = PersistenceFactory.getService().getBlockByTileId(departureTileId);

    //The sensors on the departure block do not yet play a role, but they can switch on so they have to be removed
    //from the ghost list
    String exitMinId = departureBlock.getMinSensorId();
    String exitPlusId = departureBlock.getPlusSensorId();

    BlockBean destinationBlock = PersistenceFactory.getService().getBlockByTileId(destinationTileId);

    String enterSensorId;
    if ("+".equals(arrivalSuffix)) {
      enterSensorId = destinationBlock.getPlusSensorId();
    } else {
      enterSensorId = destinationBlock.getMinSensorId();
    }

    String inSensorId;
    if ("-".equals(arrivalSuffix)) {
      inSensorId = destinationBlock.getPlusSensorId();
    } else {
      inSensorId = destinationBlock.getMinSensorId();
    }

    //Register handlers
    this.dispatcher.registerNullEventHandler(exitMinId);
    this.dispatcher.registerNullEventHandler(exitPlusId);
    this.dispatcher.registerEnterHandler(enterSensorId);
    this.dispatcher.registerArrivalHandler(inSensorId);

    Logger.debug("Enter SensorId: " + enterSensorId + " In SensorId: " + inSensorId + " Ignore exitMinId: " + exitMinId + ", exitPlusId: " + exitPlusId);

    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    //TODO rely on the acceleration delay of the loco decoder or do somthing our selves..
    JCS.getJcsCommandStation().changeLocomotiveSpeed(750, locomotive);
    Logger.debug("Started " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " Speed: " + locomotive.getVelocity());

    canAdvanceToNextState = true;
    Logger.trace("Can advance to next state: " + canAdvanceToNextState);
  }

}
