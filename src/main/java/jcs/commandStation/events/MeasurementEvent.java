/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.commandStation.events;

import java.util.Map;
import jcs.commandStation.entities.MeasurementBean;
import jcs.commandStation.marklin.cs.can.CanMessage;

/**
 * Contains the latest measurement(s)
 */
public class MeasurementEvent {

  private final Map<String, MeasurementBean> measurement;

  public MeasurementEvent(Map<String, MeasurementBean> measurement) {
    this.measurement = measurement;
  }

  public MeasurementBean getMain() {
    return measurement.get(CanMessage.MAIN);
  }

  public MeasurementBean getProg() {
    return measurement.get(CanMessage.PROG);
  }

  public MeasurementBean getVolt() {
    return measurement.get(CanMessage.VOLT);
  }

  public MeasurementBean getTemp() {
    return measurement.get(CanMessage.TEMP);
  }

}
