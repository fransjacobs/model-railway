/*
 * Copyright 2025 Frans Jacobs.
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
import org.tinylog.Logger;

/**
 * Running state of the Autopilot State machine.<br>
 * This state is entered when a locomotive is started.<br>
 * This state will subscribe to the enter sensor.<br>
 * The state will advance to a next state when the enter sensor becomes active or a reset is requested.
 */
class RunningState extends DispatcherState implements SensorEventListener {
  
  private boolean sensorsRegistered = false;
  private boolean canAdvanceToNextState = false;
  private Integer enterSensorId;
  
  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    
    if (!sensorsRegistered) {
      BlockBean departureBlock = dispatcher.getDepartureBlock();
      BlockBean destinationBlock = dispatcher.getDestinationBlock();
      
      Integer occupancySensorId = dispatcher.getOccupationSensorId();
      Integer exitSensorId = dispatcher.getExitSensorId();

      //Register them both to ignore event for these sensors.
      ExpectedSensorEventHandler osh = new ExpectedSensorEventHandler(occupancySensorId, dispatcher);
      AutoPilot.addSensorEventHandler(osh);
      
      ExpectedSensorEventHandler xsh = new ExpectedSensorEventHandler(exitSensorId, dispatcher);
      AutoPilot.addSensorEventHandler(xsh);
      
      Logger.trace("Departure: " + departureBlock.getId() + " Ignoring Occupancy Sensor: " + occupancySensorId + " and Exit Sensor: " + exitSensorId);

      //The enter Sensor triggering will switch states.
      enterSensorId = dispatcher.getEnterSensorId();
      if (enterSensorId != null && destinationBlock != null) {
        Logger.trace("Destination: " + destinationBlock.getId() + " Enter Sensor: " + enterSensorId + "...");
        JCS.getJcsCommandStation().addSensorEventListener(enterSensorId, this);

        //Register the sensor also a an expected event
        ExpectedSensorEventHandler esh = new ExpectedSensorEventHandler(enterSensorId, dispatcher);
        AutoPilot.addSensorEventHandler(esh);
      } else {
        Logger.warn("Can't register the enterSensor. Is is null!");
      }

      //TODO This is the simulator can be improved!
      dispatcher.setWaitForSensorid(enterSensorId);
      sensorsRegistered = true;
      Logger.trace("Waiting for the enter event from SensorId: " + enterSensorId + " Running loco: " + locomotive.getName() + " [" + locomotive.getDecoderType().getDecoderType() + " (" + locomotive.getAddress() + ")] Direction: " + locomotive.getDirection().getDirection() + " current velocity: " + locomotive.getVelocity());
    }
    
    if (canAdvanceToNextState || resetRequested) {
      DispatcherState newState;
      if (resetRequested) {
        newState = new ResettingState();
      } else {
        newState = new ApproachingState();
        //Remove handler as the state will now change
        JCS.getJcsCommandStation().removeSensorEventListener(enterSensorId, this);
      }
      return newState;
    } else {
      if ("true".equals(System.getProperty("state.machine.stepTest", "false"))) {
        Logger.debug("StateMachine StepTest is enabled. Dispatcher: " + dispatcher.getName() + " State: " + dispatcher.getStateName());
      } else {
        try {
          synchronized (this) {
            wait(threadWaitMillis);
          }
        } catch (InterruptedException ex) {
          Logger.trace("Interrupted: " + ex.getMessage());
        }
      }
      return this;
    }
  }
  
  @Override
  public Integer getSensorId() {
    return enterSensorId;
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
