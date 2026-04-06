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
package jcs.commandStation.automation;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.automation.Dispatcher;
import jcs.commandStation.automation.RailController;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Simulate driving by triggering the enter and in sensors 
 */
public class DriveSimulator {

  private final ScheduledExecutorService scheduledExecutor;

  public DriveSimulator() {
    this.scheduledExecutor = new ScheduledThreadPoolExecutor(30);
  }

  //Find the route the locomotive is doing....
  public void simulateDriving(int locUid, int speed, LocomotiveBean.Direction direction) {
    if ("true".equals(System.getProperty("do.not.simulate.virtual.drive", "false"))) {
      return;
    }
    //Check is the Dispatcher for the locomotive is running...
    CommandStationBean commandStationBean = PersistenceFactory.getService().getDefaultCommandStation();
    PersistenceFactory.getService().getLocomotive(locUid, commandStationBean.getId());

    Dispatcher dispatcher = RailController.getInstance().getDispatcher(locUid);

    if (dispatcher.isLocomotiveStarted()) {
      Logger.trace("Try to simulate the next sensor of " + dispatcher.getName());

      Integer occupationSensorId = dispatcher.getOccupationSensorId();
      if (occupationSensorId != null) {
        //Start a timer which execute a worker thread which fires the sensor
        scheduledExecutor.schedule(() -> setSensorValue(occupationSensorId, false), 500, TimeUnit.MILLISECONDS);
      }

      Integer exitSensorId = dispatcher.getExitSensorId();
      if (exitSensorId != null) {
        //Start a time which execute a worker thread which fires the sensor
        scheduledExecutor.schedule(() -> setSensorValue(exitSensorId, false), 1500, TimeUnit.MILLISECONDS);
      }

      Integer enterSensorId = dispatcher.getEnterSensorId();
      Integer inSensorId = dispatcher.getInSensorId();

      //depending on the state the sensor "waiting for" is the enter- or the in-sensor
      String state = dispatcher.getStateName();
      Logger.trace("Dispatcher state: " + state);

      Integer sensorId;
      switch (state) {
        case "Running" ->
          sensorId = enterSensorId;
        case "Braking" ->
          sensorId = inSensorId;
        default ->
          sensorId = enterSensorId;
      }

      int time = 5000;
      if (sensorId != null && sensorId.equals(enterSensorId)) {
        time = 3000;
        Logger.debug("Waiting " + time + "ms for Enter Sensor " + sensorId);
      }

      if (sensorId != null && sensorId.equals(inSensorId)) {
        time = 1000;
        Logger.debug("Waiting " + time + "ms for In Sensor " + sensorId);
      }

      if (sensorId != null) {
        //Start a time which execute a worker thread which fires the sensor
        scheduledExecutor.schedule(() -> setSensorValue(sensorId, true), time, TimeUnit.MILLISECONDS);
      }
    }
  }

  private void setSensorValue(Integer sensorId, boolean active) {
    SensorBean sensor = PersistenceFactory.getService().getSensor(sensorId);
    sensor.setActive(active);
    SensorEvent sensorEvent = new SensorEvent(sensor);
    Logger.trace("Fire Sensor " + sensorId + " to " + active);

    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
    for (FeedbackController fbc : acl) {
      fbc.simulateSensor(sensorEvent);
    }
  }

}
