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
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Proceeding state of the Autopilot State Machine.<br>
 * The locomotive does not have to stop in this block, therefor the speed is maintained.
 */
class PassingThroughState extends AbstractState implements SensorEventCallback {

  private Integer inSensorId;
  private boolean inSensorTriggerred = false;

  PassingThroughState() {
    super("PassingThrough");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    RouteBean route = dispatcher.getRouteBean();

    Logger.trace("Locomotive " + dispatcher.getLocomotiveBean().getName() + " has entered destination " + destinationBlock.getDescription() + " and prepares to stop...");

    //Subscribe the IN sensor
    inSensorId = dispatcher.getInSensorId();
    dispatcher.getSensorMonitor().subscribe(inSensorId, this);
    Logger.trace("Destination block " + destinationBlock.getId() + " In SensorId: " + inSensorId);

    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.getRouteManager().showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);
  }

  @Override
  AbstractState execute() {
    if (inSensorTriggerred) {
      return new InBlockState();
    } else {
      return this;
    }
  }

  @Override
  void onExit() {
    dispatcher.getSensorMonitor().subscribeWithoutCallback(inSensorId);
    dispatcher.getSensorMonitor().unsubscribe(inSensorId, this);
  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }

  @Override
  public void onEvent(SensorEvent event) {
    if (inSensorId.equals(event.getSensorId())) {
      if (event.isActive()) {
        inSensorTriggerred = true;
        Logger.trace("Enter Event from Sensor " + event.getSensorId());
      }
    } else {
      Logger.trace("Event for " + event.getSensorId() + " not for this state...");
    }
  }

}
