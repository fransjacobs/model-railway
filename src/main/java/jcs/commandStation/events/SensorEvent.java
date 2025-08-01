/*
 * Copyright 2023 Frans Jacobs.
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

import jcs.entities.SensorBean;

/**
 * Value change happened on a Sensor.
 */
public class SensorEvent { 

  private final SensorBean sensorBean;
  private final boolean newValue;

  public SensorEvent(SensorBean sensorBean) {
    this(sensorBean, sensorBean.isActive());
  }

  public SensorEvent(SensorBean sensorBean, boolean newValue) {
    this.sensorBean = sensorBean;
    this.newValue = newValue;
  }

  public SensorBean getSensorBean() {
    return sensorBean;
  }

  public Integer getSensorId() {
    return sensorBean.getId();
  }

//  @Deprecated
//  @Override
//  public String getIdString() {
//    return sensorBean.getId().toString();
//  }

  public Integer getDeviceId() {
    return sensorBean.getDeviceId();
  }

  public Integer getContactId() {
    return sensorBean.getContactId();
  }

  public boolean isChanged() {
    //boolean active = sensorBean.isActive();
    boolean prevActive = sensorBean.isPreviousActive();
    return newValue != prevActive;
  }

  public boolean isActive() {
    return newValue; //sensorBean.isActive();
  }

  @Override
  public String toString() {
    return "SensorEvent{" + "id=" + sensorBean.getId() + ", active=" + (isActive() ? "1" : "0") + "}";
  }

}
