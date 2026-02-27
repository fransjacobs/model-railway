/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.automation;

import java.awt.Color;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Proceeding state of the Autopilot State Machine.<br>
 * The locomotive does not have to stop in this block, therefor the speed is maintained.
 */
class ProceedingState extends AbstractState implements SensorEventCallback {

  ProceedingState() {
    super("Proceeding");
  }

  private boolean canAdvanceToNextState = false;
  private Integer inSensorId;

  @Override
  AbstractState execute() {
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
    dispatcher.getRouteManager().showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);

    inSensorId = dispatcher.getInSensorId();

    //For the remaining states ignore events from the in sensor
    //ExpectedSensorEventHandler ish = new ExpectedSensorEventHandler(inSensorId, dispatcher);
    //dispatcher.getRailwayController().addSensorEventHandler(ish);
    //dispatcher.getRailwayController().registerSensorEventCallback(new SensorEventCallbackHandler(inSensorId, this, true));
    dispatcher.getSensorMonitor().subscribe(inSensorId, this);

    //dispatcher.setWaitForSensorid(inSensorId);
    //Register this state as a SensorEventListener
    //JCS.getJcsCommandStation().addSensorEventListener(inSensorId, this);
    Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId);

    //Wait until the in sensor is hit by the locomotive
    //TODO: Timeout detection in case the locomotive has stopped....
    if (canAdvanceToNextState) {
      return new InBlockState();
      //Remove handler as the state will now change
      //JCS.getJcsCommandStation().removeSensorEventListener(inSensorId, this);
    } else {
      return this;
    }
  }

  @Override
  void onExit() {
    dispatcher.getSensorMonitor().unsubscribe(inSensorId, this);
  }

  @Override
  public void onEvent(SensorEvent event) {
    if (inSensorId.equals(event.getSensorId())) {
      if (event.isActive()) {
        canAdvanceToNextState = true;
        Logger.trace("Enter Event from Sensor " + event.getSensorId());
        synchronized (this) {
          this.notifyAll();
        }
      }
    }
  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }
}
