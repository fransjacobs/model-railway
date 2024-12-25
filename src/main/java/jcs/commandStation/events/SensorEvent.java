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
 *
 * @author Frans Jacobs
 */
public class SensorEvent {

  private final SensorBean sensorBean;

  public SensorEvent(SensorBean sensorBean) {
    this.sensorBean = sensorBean;
  }

  public SensorBean getSensorBean() {
    return sensorBean;
  }

  public String getId() {
    if (sensorBean.getId() != null) {
      return sensorBean.getId();
    } else {
      //TODO: Number format? check with both CS 3 and HSI 88 life sensors
      Integer deviceId = sensorBean.getDeviceId();
      Integer contactId = sensorBean.getContactId();
      String cn = ((contactId) > 9 ? "" : "0");
      if (cn.length() == 2) {
        cn = "00" + cn;
      } else if (cn.length() == 3) {
        cn = "0" + cn;
      }
      return deviceId + "-" + cn;
    }
  }

  public boolean isChanged() {
    boolean active = sensorBean.isActive();
    boolean prevActive = sensorBean.isPreviousActive();
    return active != prevActive;
  }

  public boolean isActive() {
    return sensorBean.isActive();
  }

  @Override
  public String toString() {
    return "SensorEvent{" + "id=" + getId() + ", active=" + (isActive() ? "1" : "0") + "}";
  }

}
