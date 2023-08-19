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
package jcs.controller.cs3.events;

import java.io.Serializable;
import jcs.controller.cs.can.CanMessage;
import jcs.controller.cs.can.MarklinCan;
import jcs.entities.AccessoryBean;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class AccessoryMessageEvent implements Serializable {

  private AccessoryBean accessoryBean;

  public AccessoryMessageEvent(AccessoryBean accessoryBean) {
    this.accessoryBean = accessoryBean;
  }

  public AccessoryMessageEvent(CanMessage message) {
    parseMessage(message);
  }

  private void parseMessage(CanMessage message) {
    CanMessage resp;
    if (!message.isResponseMessage()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }

    if (resp.isResponseMessage() && MarklinCan.ACCESSORY_SWITCHING_RESP == resp.getCommand()) {
      byte[] data = resp.getData();
      Integer address = data[3] & 0xff;
      Integer position = data[4] & 0xff;
      //CS is zero based
      address = address + 1;

      long id = address;

      this.accessoryBean = new AccessoryBean(id, address, null, null, position, null, null, null);
      if (resp.getDlc() == MarklinCan.DLC_8) {
        Integer switchTime = ByteUtil.toInt(new int[]{data[6], data[7]});
        this.accessoryBean.setSwitchTime(switchTime);
      }
    } else {
      Logger.warn("Can't parse message, not an Accessory Response! " + resp);
    }
  }

  public AccessoryBean getAccessoryBean() {
    return accessoryBean;
  }
}
