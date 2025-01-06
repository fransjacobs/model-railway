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
package jcs.commandStation.virtual;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import jcs.JCS;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class DriveSimulator {

  private final ScheduledExecutorService scheduledExecutor;
  private DecoderController decoderController;
  private AccessoryController accessoryController;
  private FeedbackController feedbackController;

  public DriveSimulator(DecoderController decoderController, AccessoryController accessoryController, FeedbackController feedbackController) {
    this.scheduledExecutor = new ScheduledThreadPoolExecutor(30);
    this.decoderController = decoderController;
    this.accessoryController = accessoryController;
    this.feedbackController = feedbackController;
  }

  //Find the route the locomotive is doing....
  public void simulateDriving(int locUid, int speed, LocomotiveBean.Direction direction) {
    if ("true".equals(System.getProperty("do.not.simulate.virtual.drive", "false"))) {
      return;
    }
    //Check is the Dispatcher for the locomotive is running...
    Dispatcher dispatcher = AutoPilot.getLocomotiveDispatcher(locUid);

    if (dispatcher.isLocomotiveAutomodeOn()) {
      Logger.trace("Try to simulate the next sensor of " + dispatcher.getName());

      String occupationSensorId = dispatcher.getOccupationSensorId();
      if (occupationSensorId != null) {
        //Start a timer which execute a worker thread which fires the sensor
        scheduledExecutor.schedule(() -> toggleSensor(occupationSensorId), 500, TimeUnit.MILLISECONDS);
      }

      String exitSensorId = dispatcher.getExitSensorId();
      if (exitSensorId != null) {
        //Start a time which execute a worker thread which fires the sensor
        scheduledExecutor.schedule(() -> toggleSensor(exitSensorId), 1500, TimeUnit.MILLISECONDS);
      }

      String enterSensorId = dispatcher.getEnterSensorId();
      String inSensorId = dispatcher.getInSensorId();

      String sensorId = dispatcher.getWaitingForSensorId();

      int time = 3000;
      if (sensorId != null && sensorId.equals(inSensorId)) {
        time = 1000;
      }

      if (sensorId != null) {
        //Start a time which execute a worker thread which fires the sensor
        scheduledExecutor.schedule(() -> toggleSensor(sensorId), time, TimeUnit.MILLISECONDS);
      }
    }
  }

  private void toggleSensor(String sensorId) {
    SensorBean sensor = PersistenceFactory.getService().getSensor(sensorId);
    
    //TODO why toggle and then set active?
    sensor.toggle();
    sensor.setActive((sensor.getStatus() == 1));

    SensorEvent sensorEvent = new SensorEvent(sensor);
    Logger.trace("Fire Sensor " + sensorId);

    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
    for (FeedbackController fbc : acl) {
      //fbc.fireSensorEventListeners(sensorEvent);
      
      fbc.simulateSensor(sensorEvent);
    }
  }

}
