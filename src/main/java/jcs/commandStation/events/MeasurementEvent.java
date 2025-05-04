/*
 * Copyright 2025 frans.
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

import jcs.commandStation.entities.MeasuredChannels;
import jcs.commandStation.entities.MeasurementBean;

/**
 * Signals the lastMeasurment(s)
 */
public class MeasurementEvent {

  private final MeasuredChannels measuredChannels;

  public MeasurementEvent(MeasuredChannels measuredChannels) {
    this.measuredChannels = measuredChannels;
  }

  public MeasuredChannels getMeasuredChannels() {
    return measuredChannels;
  }

  public MeasurementBean getMain() {
    return measuredChannels.getMain();
  }

  public MeasurementBean getProg() {
    return measuredChannels.getProg();
  }

  public MeasurementBean getVolt() {
    return measuredChannels.getVolt();
  }

  public MeasurementBean getTemp() {
    return measuredChannels.getTemp();
  }
}
