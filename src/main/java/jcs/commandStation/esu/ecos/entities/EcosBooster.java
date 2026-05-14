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
package jcs.commandStation.esu.ecos.entities;

import java.util.Objects;

/**
 *
 * ECoS Booster Bean
 */
public class EcosBooster {

  private String id;
  private String name;
  private String status;
  private Double voltage;
  private Double peakCurrent;
  private Double current;
  private Double temperature;
  private String currentUnit;
  private Integer limit;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getVoltage() {
    return voltage;
  }

  public void setVoltage(Double voltage) {
    this.voltage = voltage;
  }

  public Double getPeakCurrent() {
    return peakCurrent;
  }

  public void setPeakCurrent(Double peakCurrent) {
    this.peakCurrent = peakCurrent;
  }

  public Double getCurrent() {
    return current;
  }

  public void setCurrent(Double current) {
    this.current = current;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public String getCurrentUnit() {
    return currentUnit;
  }

  public void setCurrentUnit(String currentUnit) {
    this.currentUnit = currentUnit;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "EcosBooster{" + "name=" + name + '}';
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 19 * hash + Objects.hashCode(this.id);
    hash = 19 * hash + Objects.hashCode(this.name);
    hash = 19 * hash + Objects.hashCode(this.status);
    hash = 19 * hash + (int) (Double.doubleToLongBits(this.voltage) ^ (Double.doubleToLongBits(this.voltage) >>> 32));
    hash = 19 * hash + (int) (Double.doubleToLongBits(this.peakCurrent) ^ (Double.doubleToLongBits(this.peakCurrent) >>> 32));
    hash = 19 * hash + (int) (Double.doubleToLongBits(this.current) ^ (Double.doubleToLongBits(this.current) >>> 32));
    hash = 19 * hash + (int) (Double.doubleToLongBits(this.temperature) ^ (Double.doubleToLongBits(this.temperature) >>> 32));
    hash = 19 * hash + Objects.hashCode(this.currentUnit);
    hash = 19 * hash + Objects.hashCode(this.limit);
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
    final EcosBooster other = (EcosBooster) obj;
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return Objects.equals(this.name, other.name);
  }

}
