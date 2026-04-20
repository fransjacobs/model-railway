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

import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.AccessoryBean.SignalValue;

public class AccessoryEvent {

  private final AccessoryBean accessoryBean;
  private boolean power;
  private Long systemtime;

  public AccessoryEvent(AccessoryBean accessoryBean) {
    this(accessoryBean, false, null);
  }

  public AccessoryEvent(AccessoryBean accessoryBean, boolean power) {
    this(accessoryBean, power, null);
  }

  public AccessoryEvent(AccessoryBean accessoryBean, Long systemtime) {
    this(accessoryBean, false, systemtime);
  }

  public AccessoryEvent(AccessoryBean accessoryBean, boolean power, Long systemtime) {
    this.accessoryBean = accessoryBean;
    this.power = power;
    if (systemtime != null) {
      this.systemtime = systemtime;
    } else {
      this.systemtime = System.currentTimeMillis();
    }
  }

  public AccessoryBean getAccessoryBean() {
    return accessoryBean;
  }

  public boolean isPower() {
    return power;
  }

  public boolean isEventFor(AccessoryBean accessory) {
    boolean addressEquals = accessoryBean.getAddress().equals(accessory.getAddress());
    boolean idEquals = accessoryBean.getId().equals(accessory.getId());

    return addressEquals || idEquals;
  }

  public SignalValue getSignalValue() {
    return accessoryBean.getSignalValue();
  }

  public AccessoryValue getValue() {
    return accessoryBean.getAccessoryValue();
  }

  public boolean isGreen() {
    return AccessoryValue.GREEN == accessoryBean.getAccessoryValue();
  }

  public boolean isRed() {
    return AccessoryValue.RED == accessoryBean.getAccessoryValue();
  }

  public boolean isRed2() {
    return AccessoryValue.RED2 == accessoryBean.getAccessoryValue();
  }

  public Integer getAddress() {
    return accessoryBean.getAddress();
  }

  public Integer getAddress2() {
    return accessoryBean.getAddress2();
  }

  public Long getSystemtime() {
    return systemtime;
  }

  public String getId() {
    return accessoryBean.getId();
  }

  public boolean isBiAddress() {
    return this.accessoryBean.isBiAddress();
  }

  public String getProtocol() {
    return this.accessoryBean.getDecType();
  }

}
