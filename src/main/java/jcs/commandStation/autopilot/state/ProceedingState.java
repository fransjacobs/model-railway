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

import java.awt.Color;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.ExpectedSensorEventHandler;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Proceeding state of the Autopilot State Machine.<br>
 * The locomotive does not have to stop in this block, therefor the speed is maintained.
 */
class ProceedingState extends DispatcherState implements SensorEventListener {

  private boolean canAdvanceToNextState = false;
  private Integer inSensorId;

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    RouteBean route = dispatcher.getRouteBean();
    Logger.trace("Locomotive " + locomotive.getName() + " has entered destination " + destinationBlock.getDescription() + " and continues...");

    //Change Block statuses 
    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);

    inSensorId = dispatcher.getInSensorId();

    //For the remaining states ignore events from the in sensor
    ExpectedSensorEventHandler ish = new ExpectedSensorEventHandler(inSensorId, dispatcher);
    AutoPilot.addSensorEventHandler(ish);

    dispatcher.setWaitForSensorid(inSensorId);

    //Register this state as a SensorEventListener
    JCS.getJcsCommandStation().addSensorEventListener(inSensorId, this);
    Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId);

    //Wait until the in sensor is hit by the locomotive
    //TODO: Timeout detection in case the locomotive has stopped....
    if (canAdvanceToNextState || resetRequested) {
      DispatcherState newState;
      if (resetRequested) {
        newState = new ResettingState();
      } else {
        newState = new InBlockState();
        //Remove handler as the state will now change
        JCS.getJcsCommandStation().removeSensorEventListener(inSensorId, this);
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
    return inSensorId;
  }

  @Override
  public void onSensorChange(SensorEvent sensorEvent) {
    if (inSensorId.equals(sensorEvent.getSensorId())) {
      if (sensorEvent.isActive()) {
        canAdvanceToNextState = true;
        Logger.trace("In Event from Sensor " + sensorEvent.getSensorId());
        synchronized (this) {
          notifyAll();
        }
      }
    }
  }
}
