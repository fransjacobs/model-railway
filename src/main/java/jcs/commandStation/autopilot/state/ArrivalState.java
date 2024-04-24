/*
 * Copyright 2024 frans.
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

import jcs.JCS;
import jcs.commandStation.autopilot.TrainDispatcher;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class ArrivalState extends DispatcherState {

  private SensorListener enterListener;
  private SensorListener arrivalListener;

  private BlockBean departureBlock;
  private BlockBean destinationBlock;

  boolean entered = false;
  boolean arrived = false;

  public ArrivalState(TrainDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    Logger.trace("canAdvanceState: " + canAdvanceToNextState);
    if (canAdvanceToNextState) {
      DispatcherState newState = new ArrivedState(this.dispatcher);
      newState.setRunning(running);
      locRunner.setDispatcherState(newState);
    } else {
      locRunner.setDispatcherState(this);
    }
  }

  @Override
  public boolean execute() {
    if (this.arrived) {
      //TODO rethink this
      this.canAdvanceToNextState = true;
      return this.canAdvanceToNextState;
    }
    //Which sensors do we need to watch
    RouteBean route = this.dispatcher.getRouteBean();
    String departureTileId = route.getFromTileId();
    String destinationTileId = route.getToTileId();
    //From which side on the block is the train expected to arrive?
    String arrivalSuffix = route.getToSuffix();

    Logger.trace("Destination tile: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block");
    departureBlock = PersistenceFactory.getService().getBlockByTileId(departureTileId);
    destinationBlock = PersistenceFactory.getService().getBlockByTileId(destinationTileId);

    SensorBean enterSensor;
    if ("+".equals(arrivalSuffix)) {
      enterSensor = destinationBlock.getPlusSensorBean();
    } else {
      enterSensor = destinationBlock.getMinSensorBean();
    }

    enterListener = new SensorListener(enterSensor, true, this);

    SensorBean inSensor;
    if ("-".equals(arrivalSuffix)) {
      inSensor = destinationBlock.getPlusSensorBean();
    } else {
      inSensor = destinationBlock.getMinSensorBean();
    }

    arrivalListener = new SensorListener(inSensor, false, this);

    Logger.trace("Register the enter sensor listener ");
    JCS.getJcsCommandStation().addSensorEventListener(enterListener);
    JCS.getJcsCommandStation().addSensorEventListener(arrivalListener);

    Logger.debug("Enter Sensor: " + enterSensor.getName() + " [" + enterSensor.getId() + "] In Sensor: " + inSensor.getName() + " [" + inSensor.getId() + "]");

    LocomotiveBean locomotive = this.dispatcher.getLocomotiveBean();
    //Start The locomotive runner thread and start a loco
    JCS.getJcsCommandStation().changeLocomotiveSpeed(750, locomotive);

    long startTime = System.currentTimeMillis();
    //boolean logged = false;

    Logger.debug("Waiting for enter event..." + enterSensor.getContactId());
    while (!entered) {
      Thread.yield();
      //Wait for enter
      //if (!logged) {
      //  Logger.debug("Waiting for enter event..."+enterSensor.getContactId());
      //}
      //logged = true;
    }

    Logger.debug("Train has entered the destination block. Slow down");
    JCS.getJcsCommandStation().changeLocomotiveSpeed(100, locomotive);
    long enterTime = System.currentTimeMillis();
    Logger.debug(locomotive.getName() + " has entered destination " + route.getToTileId() + " travel time: " + (enterTime - startTime / 1000) + "s.");

    //logged = false;
    Logger.debug("Waiting for arrival event..." + inSensor.getContactId());
    while (!arrived) {
      Thread.yield();

      //if (!logged) {
      //  Logger.debug("Waiting for arrival event..."+inSensor.getContactId());
      //}
      //logged = true;
    }
    long arrivalTime = System.currentTimeMillis();

    arrived();

    Logger.debug(locomotive.getName() + " has arrived destination " + route.getToTileId() + " total travel time: " + (arrivalTime - startTime / 1000) + "s.");

    canAdvanceToNextState = arrived;
    route = null;
    return canAdvanceToNextState;
  }

  private synchronized void onEnter() {
    Logger.debug("got an enter event");
    this.entered = true;
    notify();
  }

  private synchronized void onArrival() {
    Logger.debug("got an Arrival event..");
    arrived = true;
    notify();
  }

  private void arrived() {
    LocomotiveBean locomotive = this.dispatcher.getLocomotiveBean();

    Logger.debug("Train has arrived in the destination block");

    JCS.getJcsCommandStation().changeLocomotiveSpeed(0, locomotive);
    //set the loco in the destination block
    //remove the loco from the source block
    departureBlock.setLocomotive(null);
    destinationBlock.setLocomotive(locomotive);

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    JCS.getJcsCommandStation().removeSensorEventListener(enterListener);
    JCS.getJcsCommandStation().removeSensorEventListener(arrivalListener);

    RouteBean route = this.dispatcher.getRouteBean();
    route.setLocked(false);
    PersistenceFactory.getService().persist(route);

    refreshBlockTiles();
  }

  private class SensorListener implements SensorEventListener {

    private final Integer deviceId;
    private final Integer contactId;
    private final boolean enter;
    private final ArrivalState runState;

    SensorListener(SensorBean sensor, boolean enter, ArrivalState runState) {
      this.deviceId = sensor.getDeviceId();
      this.contactId = sensor.getContactId();
      this.enter = enter;
      this.runState = runState;
      Logger.trace("deviceId: " + deviceId + " contactId: " + contactId + " enter: " + enter);
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      SensorBean sensor = event.getSensorBean();

      if (deviceId.equals(sensor.getDeviceId()) && contactId.equals(sensor.getContactId())) {
        Logger.trace("-> deviceId: " + deviceId + " contactId: " + contactId + " enter: " + enter);
        if (sensor.isActive()) {
          if (enter) {
            runState.onEnter();
          } else {
            runState.onArrival();
          }
        }
      }
    }
  }

}
