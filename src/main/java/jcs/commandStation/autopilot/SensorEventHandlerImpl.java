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

import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;

/**
 *
 * @author Frans Jacobs
 */
public class SensorEventHandlerImpl implements SensorEventListener {

  private final SensorEventHandler defaultHandler;
  private final String sensorId;
  private SensorEventHandler preferredHandler;

  public SensorEventHandlerImpl(SensorEventHandler defaultHandler, String sensorId) {
    this.defaultHandler = defaultHandler;
    this.sensorId = sensorId;
  }
  
  @Override
  public void onSensorChange(SensorEvent event) {
    if (preferredHandler != null) {
      preferredHandler.handleSensorEvent(event);
    } else {
      defaultHandler.handleSensorEvent(event);
    }
  }

  public SensorEventHandler getPreferredHandler() {
    return preferredHandler;
  }

  public void setPreferredHandler(SensorEventHandler preferredHandler) {
    this.preferredHandler = preferredHandler;
  }

  public SensorEventHandler getDefaultHandler() {
    return defaultHandler;
  }

  public String getSensorId() {
    return sensorId;
  }

  public void reset() {
    this.preferredHandler = null;
  }
}
