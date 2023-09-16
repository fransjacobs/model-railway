/*
 * Copyright 2023 frans.
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
package jcs.controller.events;

import jcs.entities.MeasurementChannel;

/**
 *
 * @author frans
 */
public class MeasurementEvent {

  private final MeasurementChannel measurementChannel;

  public MeasurementEvent(MeasurementChannel measurementChannel) {
    this.measurementChannel = measurementChannel;
  }

  public MeasurementChannel getMeasurementChannel() {
    return measurementChannel;
  }

  public Integer getCannel() {
    return this.measurementChannel.getNumber();
  }

  public String getFormattedValue() {
    return this.measurementChannel.getHumanValue() + " " + this.measurementChannel.getUnit();
  }

}
