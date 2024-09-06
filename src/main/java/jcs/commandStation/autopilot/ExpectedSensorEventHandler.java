/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.autopilot;

import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.commandStation.events.SensorEvent;

/**
 * Handle Sensor event which are expected
 */
public class ExpectedSensorEventHandler implements SensorEventHandler {

  private final String sensorId;
  private final Dispatcher dispatcher;

  public ExpectedSensorEventHandler(String sensorId, Dispatcher dispatcher) {
    this.sensorId = sensorId;
    this.dispatcher = dispatcher;
  }

  @Override
  public void handleEvent(SensorEvent event) {
    if (this.sensorId.equals(event.getId())) {
      this.dispatcher.onIgnoreEvent(event);
    }
  }

  @Override
  public String getSensorId() {
    return this.sensorId;
  }

}
