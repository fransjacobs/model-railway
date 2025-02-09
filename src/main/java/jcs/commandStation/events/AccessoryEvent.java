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

public class AccessoryEvent implements JCSActionEvent {

  private final AccessoryBean accessoryBean;

  public AccessoryEvent(AccessoryBean accessoryBean) {
    this.accessoryBean = accessoryBean;
  }

  public AccessoryBean getAccessoryBean() {
    return accessoryBean;
  }

  public boolean isKnownAccessory() {
    return this.accessoryBean != null && (this.accessoryBean.getAddress() != null || this.accessoryBean.getId() != null);
  }

  public boolean isEventFor(AccessoryBean accessory) {
    boolean addressEquals = this.accessoryBean.getAddress().equals(accessory.getAddress());
    boolean idEquals = this.accessoryBean.getId().equals(accessory.getId());

    return addressEquals || idEquals;
  }

  public SignalValue getSignalValue() {
    return this.accessoryBean.getSignalValue();
  }

  public AccessoryValue getValue() {
    return this.accessoryBean.getAccessoryValue();
  }

  @Override
  public String getId() {
    return this.accessoryBean.getId();
  }

}
