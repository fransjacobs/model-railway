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
package jcs.commandStation.entities;

import java.util.Date;
import java.util.Objects;

/**
 * A Bean which hold measured values
 */
public class MeasurementBean {

  private Integer channelNumber;
  private String name;
  private boolean valid;
  private Long measurementMillis;

  private Integer measuredValue;
  private String unit;

  private Double displayValue;

  public MeasurementBean() {
  }

  public MeasurementBean(Integer channelNumber, String name, boolean valid, Long measurementMillis) {
    this(channelNumber, name, valid, measurementMillis, null, null, null);
  }

  public MeasurementBean(Integer channelNumber, String name, Long measurementMillis, Integer measuredValue, String unit, Double displayValue) {
    this(channelNumber, name, true, measurementMillis, measuredValue, unit, displayValue);
  }

  public MeasurementBean(Integer channelNumber, String name, boolean valid, Long measurementMillis, Integer measuredValue, String unit, Double displayValue) {
    this.channelNumber = channelNumber;
    this.name = name;
    this.valid = valid;
    this.measurementMillis = measurementMillis;
    this.measuredValue = measuredValue;
    this.unit = unit;
    this.displayValue = displayValue;
  }

  public Integer getChannelNumber() {
    return channelNumber;
  }

  public void setChannelNumber(Integer channelNumber) {
    this.channelNumber = channelNumber;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public Date getMeasurementTime() {
    return new Date(measurementMillis);
  }

  public void setMeasurementTime(Date measurementTime) {
    this.measurementMillis = measurementTime.getTime();
  }

  public Long getMeasurementMillis() {
    return measurementMillis;
  }

  public void setMeasurementMillis(Long measurementMillis) {
    this.measurementMillis = measurementMillis;
  }

  public Integer getMeasuredValue() {
    return measuredValue;
  }

  public void setMeasuredValue(Integer measuredValue) {
    this.measuredValue = measuredValue;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Double getDisplayValue() {
    return displayValue;
  }

  public void setDisplayValue(Double displayValue) {
    this.displayValue = displayValue;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("CanDevice{");
    if (channelNumber != null) {
      sb.append("channelNumber=").append(channelNumber);
    }
    if (name != null) {
      sb.append(", name=").append(name);
    }
    sb.append(", valid=").append(valid);
    if (measurementMillis != null) {
      sb.append(", measurementTime=").append(getMeasurementTime());
    }
    if (measuredValue != null) {
      sb.append(", measuredValue=").append(measuredValue);
    }
    if (displayValue != null) {
      sb.append(", humanValue=").append(displayValue);
    }
    if (unit != null) {
      sb.append(", unit=").append(unit);
    }

    sb.append("}");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 19 * hash + Objects.hashCode(this.channelNumber);
    hash = 19 * hash + Objects.hashCode(this.name);
    hash = 19 * hash + (this.valid ? 1 : 0);
    hash = 19 * hash + Objects.hashCode(this.measurementMillis);
    hash = 19 * hash + Objects.hashCode(this.measuredValue);
    hash = 19 * hash + Objects.hashCode(this.unit);
    hash = 19 * hash + Objects.hashCode(this.displayValue);
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
    final MeasurementBean other = (MeasurementBean) obj;
    if (this.valid != other.valid) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.unit, other.unit)) {
      return false;
    }
    if (!Objects.equals(this.channelNumber, other.channelNumber)) {
      return false;
    }
    if (!Objects.equals(this.measurementMillis, other.measurementMillis)) {
      return false;
    }
    if (!Objects.equals(this.measuredValue, other.measuredValue)) {
      return false;
    }
    return Objects.equals(this.displayValue, other.displayValue);
  }

}
