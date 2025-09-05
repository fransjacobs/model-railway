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

  public AccessoryEvent(AccessoryBean accessoryBean) {
    this.accessoryBean = accessoryBean;
  }

  public AccessoryBean getAccessoryBean() {
    return accessoryBean;
  }

  @Deprecated
  public boolean isValid() {
    return accessoryBean != null && (accessoryBean.getAddress() != null || accessoryBean.getId() != null);
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

  public Integer getAddress() {
    return accessoryBean.getAddress();
  }

  public String getId() {
    return accessoryBean.getId();
  }

}
