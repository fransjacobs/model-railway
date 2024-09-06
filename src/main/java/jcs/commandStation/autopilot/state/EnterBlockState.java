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

import jcs.commandStation.autopilot.ExpectedSensorEventHandler;
import java.awt.Color;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

class EnterBlockState extends DispatcherState implements SensorEventListener {

  private boolean locomotiveBraking = false;
  private boolean canAdvanceToNextState = false;
  private String inSensorId;
  private Dispatcher dispatcher;

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    if (!locomotiveBraking) {
      BlockBean departureBlock = dispatcher.getDepartureBlock();
      BlockBean destinationBlock = dispatcher.getDestinationBlock();
      RouteBean route = dispatcher.getRouteBean();

      Logger.trace("Locomotive " + locomotive.getName() + " has entered destination " + destinationBlock.getDescription() + "...");

      inSensorId = dispatcher.getInSensorId();

      //For the remaining states ignore events from the in sensor
      ExpectedSensorEventHandler ish = new ExpectedSensorEventHandler(inSensorId, dispatcher);
      AutoPilot.addSensorEventHandler(ish);

      dispatcher.setWaitForSensorid(inSensorId);

      //Register this state as a SensorEventListener
      this.dispatcher = dispatcher;
      JCS.getJcsCommandStation().addSensorEventListener(this);
      Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId);

      //Slowdown
      Logger.trace("Slowdown " + locomotive.getName() + "...");
      dispatcher.changeLocomotiveVelocity(locomotive, 100);
      locomotiveBraking = true;

      //Change Block statuses 
      departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
      destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

      PersistenceFactory.getService().persist(departureBlock);
      dispatcher.showBlockState(departureBlock);

      dispatcher.showRoute(route, Color.magenta);

      PersistenceFactory.getService().persist(destinationBlock);
      dispatcher.showBlockState(destinationBlock);

      //Switch the departure block sensors on again
      dispatcher.clearDepartureIgnoreEventHandlers();

      dispatcher.setOccupationSensorId(null);
      dispatcher.setExitSensorId(null);

      Logger.trace("Now Waiting for the IN event from SensorId: " + this.inSensorId + " Running loco: " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " current velocity: " + locomotive.getVelocity());
    }

    if (canAdvanceToNextState) {
      DispatcherState newState = new InBlockState();
      //Remove handler as the state will now change
      JCS.getJcsCommandStation().removeSensorEventListener(this);

      return newState;
    } else {
      if ("true".equals(System.getProperty("state.machine.stepTest", "false"))) {
        Logger.debug("StateMachine StepTest is enabled. Dispatcher: " + dispatcher.getName() + " State: " + dispatcher.getStateName());
      } else {
        try {
          synchronized (this) {
            wait(10000);
          }
        } catch (InterruptedException ex) {
          Logger.trace("Interrupted: " + ex.getMessage());
        }
      }
      return this;
    }
  }

  @Override
  public void onSensorChange(SensorEvent sensorEvent) {
    if (this.inSensorId.equals(sensorEvent.getId())) {
      if (sensorEvent.isActive()) {
        this.canAdvanceToNextState = true;
        Logger.trace("In Event from Sensor " + sensorEvent.getId());
        synchronized (this) {
          this.notifyAll();
        }
      }
    }
  }

}
