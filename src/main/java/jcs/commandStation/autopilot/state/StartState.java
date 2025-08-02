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
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Reserve the exit and entry sensors
 */
class StartState extends DispatcherState implements SensorEventListener {

  private boolean locomotiveStarted = false;
  private boolean canAdvanceToNextState = false;
  private Integer enterSensorId;

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    //Register this state as a SensorEventListener, therefor a handle to the dispacher is needed
    this.dispatcher = dispatcher;
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    
    if (!locomotiveStarted) {
      BlockBean departureBlock = dispatcher.getDepartureBlock();
      BlockBean destinationBlock = dispatcher.getDestinationBlock();

      Integer occupancySensorId = dispatcher.getOccupationSensorId();
      Integer exitSensorId = dispatcher.getExitSensorId();

      //Register them both to ignore event form these sensors.
      ExpectedSensorEventHandler osh = new ExpectedSensorEventHandler(occupancySensorId, dispatcher);
      AutoPilot.addSensorEventHandler(osh);

      ExpectedSensorEventHandler xsh = new ExpectedSensorEventHandler(exitSensorId, dispatcher);
      AutoPilot.addSensorEventHandler(xsh);

      Logger.trace("Departure: " + departureBlock.getId() + " Ignoring Occupancy Sensor: " + occupancySensorId + " and Exit Sensor: " + exitSensorId);

      //The enter Sensor triggering will switch states.
      enterSensorId = dispatcher.getEnterSensorId();
      Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + enterSensorId + "...");

      JCS.getJcsCommandStation().addSensorEventListener(this);

      //Register the sensor also a an expected event
      ExpectedSensorEventHandler esh = new ExpectedSensorEventHandler(enterSensorId, dispatcher);
      AutoPilot.addSensorEventHandler(esh);

      //TODO This is the simulator can be improved!
      dispatcher.setWaitForSensorid(enterSensorId);

      departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
      PersistenceFactory.getService().persist(departureBlock);

      destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);
      PersistenceFactory.getService().persist(destinationBlock);

      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);

      //TODO: for now rely on the acceleration delay of the loco decoder. Future make a smooth accelerator our selves..
      Logger.trace("Starting " + locomotive.getName() + " Direction " + locomotive.getDirection());
      //Speed to 70% or speed 3
      Integer speed3 = locomotive.getSpeedThree();
      if(speed3 == null || speed3 == 0) {
        speed3 = 70;
      } 
      
      //1000 is full scale
      //TODO: get the full scale from a global variable
      Integer velocity = speed3/100 * 1000;
      dispatcher.changeLocomotiveVelocity(locomotive, velocity);

      locomotiveStarted = true;
      Logger.trace("Waiting for the enter event from SensorId: " + enterSensorId + " Running loco: " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " current velocity: " + locomotive.getVelocity());
    }

    if (canAdvanceToNextState) {
      DispatcherState newState = new EnterBlockState();
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
    if (enterSensorId.equals(sensorEvent.getSensorId())) {
      if (sensorEvent.isActive()) {
        canAdvanceToNextState = true;
        Logger.trace("Enter Event from Sensor " + sensorEvent.getSensorId());
        synchronized (this) {
          this.notifyAll();
        }
      }
    }
  }

}
