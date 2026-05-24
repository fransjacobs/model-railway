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
import static jcs.commandStation.automation.AbstractState.State.PASSTHROUGH;
import static jcs.commandStation.automation.RailController.TAG;
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
  private volatile boolean inSensorTriggered = false;

  /**
   * When the IN sensor has been triggered means<br>
   * that the locomotive had to be stopped i.e. an emergency stop.
   *
   * @param inSensorTriggered
   */
  PassingThroughState(boolean inSensorTriggered) {
    super(PASSTHROUGH);
    this.inSensorTriggered = inSensorTriggered;
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);
    inSensorId = dispatcher.getInSensorId();
    dispatcher.getSensorMonitor().subscribe(inSensorId, this);

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    RouteBean route = dispatcher.getRouteBean();

    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.getRouteManager().showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);

    Logger.tag(TAG).debug("Locomotive: " + dispatcher.getName() + " in " + destinationBlock.getDescription() + " Current Route: " + dispatcher.getRouteBean().getId() + " Next Route: " + dispatcher.getNextRouteBean().getId() + " Speed: " + dispatcher.getLocomotiveBean().getVelocity() + " waiting for In SensorId: " + inSensorId + " InsensorTriggered: " + inSensorTriggered + " ...");
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
        Logger.tag(TAG).trace("In Event from Sensor " + event.getSensorId() + " for " + dispatcher.getName());
        dispatcher.wakeup();
      }
    }
  }

}
