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
import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class EnterBlockState extends DispatcherState implements SensorEventListener {

  private boolean locomotiveBraking = false;
  private String inSensorId;

  EnterBlockState(LocomotiveDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  synchronized DispatcherState next(LocomotiveDispatcher locRunner) {
    if (this.canAdvanceToNextState) {
      DispatcherState newState = new InBlockState(dispatcher);
      //Remove handler as the state will now change
      JCS.getJcsCommandStation().removeSensorEventListener(this);
      //For the remaining states ignore events from the in sensor
      this.dispatcher.registerIgnoreEventHandler(inSensorId);
      return newState;
    } else {
      return this;
    }
  }

  @Override
  void execute() {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    if (!locomotiveBraking) {
      //Slowdown
      this.dispatcher.changeLocomotiveVelocity(locomotive, 100);

      BlockBean destinationBlock = dispatcher.getDestinationBlock();
      RouteBean route = dispatcher.getRouteBean();
      Logger.trace("Locomotive " + locomotive.getName() + " has entered destination " + destinationBlock.getDescription() + ". Slowing down....");

      String arrivalSuffix = route.getToSuffix();
      locomotiveBraking = true;

      //Register for the In event
      if ("-".equals(arrivalSuffix)) {
        inSensorId = destinationBlock.getPlusSensorId();
      } else {
        inSensorId = destinationBlock.getMinSensorId();
      }

      //Register this state as a SensorEventListener
      JCS.getJcsCommandStation().addSensorEventListener(this);
      Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId);
      //Remove the in sensor from the ghost detection
      this.dispatcher.registerIgnoreEventHandler(inSensorId);

      //Change Block statuses 
      BlockBean departureBlock = this.dispatcher.getDepartureBlock();
      departureBlock.setBlockState(BlockBean.BlockState.LEAVING);
      destinationBlock.setBlockState(BlockBean.BlockState.ARRIVING);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      //Switch the departure block sensors on again
      dispatcher.clearDepartureIgnoreEventHandlers();

      //Show the new states in the UI
      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);
      dispatcher.showRoute(route, Color.magenta);

      Logger.trace("Waiting for the in event from SensorId: " + this.inSensorId + " Running loco: " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " current velocity: " + locomotive.getVelocity());
    }
  }

  @Override
  public void onSensorChange(SensorEvent sensorEvent) {
    if (this.inSensorId.equals(sensorEvent.getId())) {
      if (sensorEvent.isActive()) {
        this.canAdvanceToNextState = true;
        Logger.trace("In Event from Sensor " + sensorEvent.getId());
        synchronized (this) {
          notify();
        }
      }
    }
  }

}
