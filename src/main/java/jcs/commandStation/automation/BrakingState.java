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
 * Braking state of the Autopilot State Machine.<br>
 * The locomotive has to stop in this block, therefor the speed is decreased.<br>
 * Subscribe the in sensor as callback to be able to advance to the next state.
 *
 */
class BrakingState extends AbstractState implements SensorEventCallback {

  private Integer inSensorId;
  private boolean inSensorTriggerred = false;

  BrakingState(boolean inSensorTriggerred) {
    super("Braking");
    this.inSensorTriggerred = inSensorTriggerred;
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    //Subscribe the IN sensor
    inSensorId = dispatcher.getInSensorId();
    dispatcher.getSensorMonitor().subscribe(inSensorId, this);

    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    //Slowdown Speed to ~10% or speed 1
    Integer speed1 = locomotive.getSpeedOne();
    if (speed1 == null || speed1 == 0) {
      speed1 = 10;
    }

    int fullscale = locomotive.getTachoMax();
    double velocity = (speed1 / (double) fullscale) * 1000;
    dispatcher.changeLocomotiveVelocity(velocity);

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    RouteBean route = dispatcher.getRouteBean();

    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.INBOUND);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.getRouteManager().showRoute(route, Color.magenta);
    dispatcher.showBlockState(destinationBlock);

    Logger.debug("Slowdown: " + dispatcher.getName() + " in " + destinationBlock.getDescription() + " Direction: " + dispatcher.getLocomotiveBean().getDirection().getDirection() + " Route: " + dispatcher.getRouteBean().getId() + " Speed: " + dispatcher.getLocomotiveBean().getVelocity() + " Now waiting for In sensorId: " + inSensorId + "...");
  }

  @Override
  AbstractState execute() {
    if (inSensorTriggerred) {
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
        inSensorTriggerred = true;
        Logger.trace("In Event from Sensor " + event.getSensorId() + " for " + dispatcher.getName());
        dispatcher.wakeup();
      }
    } 
  }

}
