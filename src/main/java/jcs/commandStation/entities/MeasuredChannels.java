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
package jcs.commandStation.entities;

import java.util.Date;
import java.util.Objects;
import org.tinylog.Logger;

/**
 * Hold a group of measurements
 */
public class MeasuredChannels {

  private long measurementTime;
  private MeasurementBean main;
  private MeasurementBean prog;
  private MeasurementBean volt;
  private MeasurementBean temp;

  public MeasuredChannels() {

  }

  public MeasuredChannels(long measurementTime) {
    this.measurementTime = measurementTime;
  }

  public long getMeasurementTime() {
    return measurementTime;
  }

  public void addMeasurement(MeasurementBean measurement) {
    if (measurement != null && measurement.getName() != null) {
      switch (measurement.getName()) {
        case "MAIN" ->
          this.main = measurement;
        case "PROG" ->
          this.prog = measurement;
        case "VOLT" ->
          this.volt = measurement;
        case "TEMP" ->
          this.temp = measurement;
        default ->
          Logger.error("Unknown measurement " + measurement);
      }
    }
  }

  public MeasurementBean getMain() {
    return main;
  }

  public MeasurementBean getProg() {
    return prog;
  }

  public MeasurementBean getVolt() {
    return volt;
  }

  public MeasurementBean getTemp() {
    return temp;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 43 * hash + (int) (this.measurementTime ^ (this.measurementTime >>> 32));
    hash = 43 * hash + Objects.hashCode(this.main);
    hash = 43 * hash + Objects.hashCode(this.prog);
    hash = 43 * hash + Objects.hashCode(this.volt);
    hash = 43 * hash + Objects.hashCode(this.temp);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MeasuredChannels other = (MeasuredChannels) obj;
    if (this.measurementTime != other.measurementTime) {
      return false;
    }
    if (!Objects.equals(this.main, other.main)) {
      return false;
    }
    if (!Objects.equals(this.prog, other.prog)) {
      return false;
    }
    if (!Objects.equals(this.volt, other.volt)) {
      return false;
    }
    return Objects.equals(this.temp, other.temp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MeasuredChanels{measurementTime=");
    sb.append(new Date(measurementTime));
    if (main != null) {
      sb.append(", MAIN=");
      sb.append(main.getDisplayValue());
      sb.append(" ");
      sb.append(main.getUnit());
    }
    if (prog != null) {
      sb.append(", PROG=");
      sb.append(prog.getDisplayValue());
      sb.append(" ");
      sb.append(prog.getUnit());
    }
    if (volt != null) {
      sb.append(", VOLT=");
      sb.append(volt.getDisplayValue());
      sb.append(" ");
      sb.append(volt.getUnit());
    }
    if (temp != null) {
      sb.append(", TEMP=");
      sb.append(temp.getDisplayValue());
      sb.append(" ");
      sb.append(temp.getUnit());
    }
    sb.append("}");

    return sb.toString();
  }

}
