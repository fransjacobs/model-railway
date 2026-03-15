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
  private boolean inSensorTriggered = false;

  PassingThroughState() {
    super("PassingThrough");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);
    inSensorId = dispatcher.getInSensorId();
    dispatcher.getSensorMonitor().subscribe(inSensorId, this);

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    RouteBean route = dispatcher.getRouteBean();

    Logger.trace("Locomotive " + dispatcher.getLocomotiveBean().getName() + " has entered destination " + destinationBlock.getDescription() + " will continue at current speed...");

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
    if (inSensorTriggered) {
      return new ArrivedState();
    } else {
      return this;
    }
  }

  @Override
  void onExit() {
    dispatcher.getSensorMonitor().unsubscribe(inSensorId, this);
    dispatcher.getSensorMonitor().subscribeWithoutCallback(inSensorId);
  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }

  @Override
  public void onEvent(SensorEvent event) {
    if (inSensorId.equals(event.getSensorId())) {
      if (event.isActive()) {
        inSensorTriggered = true;
        Logger.trace("In Event from Sensor " + event.getSensorId() + " for " + dispatcher.getName());
        dispatcher.wakeup();
      }
    } else {
      Logger.trace("Event for " + event.getSensorId() + " not for this state...");
    }
  }

}
