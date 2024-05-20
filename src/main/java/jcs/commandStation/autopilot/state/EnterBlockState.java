/*
 * Copyright 2024 frans.
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
class EnterBlockState extends DispatcherState {

  private boolean locomotiveBraking = false;

  EnterBlockState(TrainDispatcher dispatcher, boolean running) {
    super(dispatcher, running);
  }

  @Override
  public synchronized void next(TrainDispatcher locRunner) {
    if (dispatcher.isInDestinationBlock()) {
      DispatcherState newState = new InBlockState(dispatcher, isRunning());
      locRunner.setDispatcherState(newState);
    } else {
      locRunner.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    if (!locomotiveBraking && dispatcher.isEnterDestinationBlock()) {

      BlockBean destinationBlock = dispatcher.getDestinationBlock();
      Logger.debug("Locomotive " + locomotive.getName() + " has entered destination " + destinationBlock.getDescription() + ". Slowing down....");
      //Slowdown
      this.dispatcher.changeLocomotiveVelocity(locomotive, 100);
      locomotiveBraking = true;

      //Register for the In event
      RouteBean route = dispatcher.getRouteBean();
      String arrivalSuffix = route.getToSuffix();

      String inSensorId;
      if ("-".equals(arrivalSuffix)) {
        inSensorId = destinationBlock.getPlusSensorId();
      } else {
        inSensorId = destinationBlock.getMinSensorId();
      }
      
      dispatcher.registerInHandler(inSensorId);
      Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId+" Enter sensorId: "+dispatcher.getEnterSensorId());

      //Change Block statuses 
      BlockBean departureBlock = this.dispatcher.getDepartureBlock();
      departureBlock.setBlockState(BlockBean.BlockState.LEAVING);
      destinationBlock.setBlockState(BlockBean.BlockState.ARRIVING);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      //Switch the departure block sensors on again
      dispatcher.clearIgnoreEventHandlers();

      //Show the new states in the UI
      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);
      dispatcher.showRoute(route, Color.magenta);

      Logger.trace("Waiting for the in event from SensorId: " + dispatcher.getInSensorId() + " Running loco: " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " current velocity: " + locomotive.getVelocity());
    }
    this.canAdvanceToNextState = this.dispatcher.isInDestinationBlock();
  }

}
