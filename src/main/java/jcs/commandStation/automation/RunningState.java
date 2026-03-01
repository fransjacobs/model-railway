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

import java.util.Date;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.StationBean;
import jcs.entities.StationBlockBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Running state of the Autopilot State machine.<br>
 * This state is entered when a locomotive is departing.<br>
 * This state will subscribe to the enter sensor.<br>
 * The state will advance to the next state when the enter sensor becomes active.
 */
class RunningState extends AbstractState implements SensorEventCallback {

  private Integer enterSensorId;
  private boolean enterSensorTriggered = false;

  RunningState() {
    super("Running");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    BlockBean departureBlock = dispatcher.getDepartureBlock();
    Integer occupancySensorId = dispatcher.getOccupationSensorId();
    Integer exitSensorId = dispatcher.getExitSensorId();

    //Register the both sensors of the departure block to ignore events form these sensors.
    dispatcher.getSensorMonitor().subscribeWithoutCallback(occupancySensorId);
    dispatcher.getSensorMonitor().subscribeWithoutCallback(exitSensorId);

    Logger.trace("Departure: " + departureBlock.getId() + " Ignoring Occupancy Sensor: " + occupancySensorId + " and Exit Sensor: " + exitSensorId);

    //Register the enter Sensor, which will trigger switch to the arrival state.
    enterSensorId = dispatcher.getEnterSensorId();
    dispatcher.getSensorMonitor().subscribe(enterSensorId, this);

    //If the departure is from a station, lower the locCount
    //TODO: is this really necessary as the locCount can also be determined dynamically.....
    StationBean station = dispatcher.getStation(departureBlock);
    if (station != null) {
      StationBlockBean sbb = station.getStationBlockBean(departureBlock);
      //reset arrival time
      sbb.setLastUpdated(new Date());

      int locCount = station.getLocomotiveCount();
      locCount = locCount + 1;
      if (locCount < 0) {
        locCount = 0;
      }
      station.setLocomotiveCount(locCount);
      PersistenceFactory.getService().persist(station);
    }

    Logger.trace("Waiting for the enter event from SensorId: " + enterSensorId + " Running loco: " + dispatcher.getLocomotiveBean().getName() + " Direction: " + dispatcher.getLocomotiveBean().getDirection().getDirection() + " current velocity: " + dispatcher.getLocomotiveBean().getVelocity());
  }

  @Override
  AbstractState execute() {
    if (enterSensorTriggered) {
      return new ApproachingState();
    } else {
      return this;
    }
  }

  @Override
  void onExit() {
    //Remove the Callback
    dispatcher.getSensorMonitor().unsubscribe(enterSensorId, this);

    //Disable the entersensor
    dispatcher.getSensorMonitor().subscribeWithoutCallback(enterSensorId);
  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }

  @Override
  public void onEvent(SensorEvent event) {
    if (enterSensorId.equals(event.getSensorId())) {
      if (event.isActive()) {
        enterSensorTriggered = true;
        Logger.trace("Enter Event from Sensor " + event.getSensorId());
      }
    } else {
      Logger.trace("Event for " + event.getSensorId() + " not for this state...");
    }
  }
}
