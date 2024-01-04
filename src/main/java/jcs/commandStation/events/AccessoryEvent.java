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

import java.io.Serializable;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.AccessoryBean.SignalValue;
import org.tinylog.Logger;

public class AccessoryEvent implements Serializable {

  private AccessoryBean accessoryBean;

  public AccessoryEvent(AccessoryBean accessoryBean) {
    this.accessoryBean = accessoryBean;
  }

  public AccessoryEvent(CanMessage message) {
    parseMessage(message);
  }

  private void parseMessage(CanMessage message) {
    CanMessage resp;
    if (!message.isResponseMessage()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }

    if (resp.isResponseMessage() && CanMessage.ACCESSORY_SWITCHING_RESP == resp.getCommand()) {
      byte[] data = resp.getData();
      int address = data[3];
      int position = data[4];
      //CS is zero based
      address = address + 1;
      String id = address+"";

      this.accessoryBean = new AccessoryBean(id, address, null, null, position, null, null, null,null);
      if (resp.getDlc() == CanMessage.DLC_8) {
        int switchTime = CanMessage.toInt(new byte[]{data[6], data[7]});
        this.accessoryBean.setSwitchTime(switchTime);
      }
    } else {
      Logger.warn("Can't parse message, not an Accessory Response! " + resp);
    }
  }

  public AccessoryBean getAccessoryBean() {
    return accessoryBean;
  }

  public boolean isKnownAccessory() {
    return this.accessoryBean != null && this.accessoryBean.getAddress() != null;
  }

  public boolean isEventFor(AccessoryBean accessory) {
    return this.accessoryBean.getAddress().equals(accessory.getAddress());
  }

  public SignalValue getSignalValue() {
    return this.accessoryBean.getSignalValue();
  }

  public AccessoryValue getValue() {
    return this.accessoryBean.getAccessoryValue();
  }

}
