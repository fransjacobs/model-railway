/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.marklin.cs.can.parser;

import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.AccessoryBean;
import org.tinylog.Logger;

public class AccessoryMessage {

  private AccessoryMessage() {

  }

  public static AccessoryEvent parse(CanMessage message) {
    if (message == null) {
      return null;
    }
    CanMessage msg;
    if (message.isResponseMessage()) {
      msg = message.getResponse();
    } else {
      msg = message;
    }

    int cmd = msg.getCommand();
    int dlc = msg.getDlc();
    byte[] data = msg.getData();

    if (CanMessage.ACCESSORY_SWITCHING_RESP == cmd || CanMessage.ACCESSORY_SWITCHING == cmd) {
      byte[] addressData = new byte[]{data[2], data[3]};
      int address = CanMessage.toInt(addressData);
      String protocol;
      //CS is zero based
      if (address >= CanMessage.DCC_ACCESSORY_OFFSET) {
        protocol = "dcc";
        address = address - CanMessage.DCC_ACCESSORY_OFFSET + 1;
      } else {
        protocol = "mm";
        address = address + 1;
        address = address - CanMessage.MM_ACCESSORY_OFFSET;
      }

      int position = data[4];

      String id = Integer.toString(address);
      AccessoryBean accessoryBean = new AccessoryBean(id, address, null, null, null, position, null, protocol, null, CanMessage.MARKLIN_COMMANDSTATION_ID);

      if (CanMessage.DLC_8 == dlc) {
        int switchTime = CanMessage.toInt(new byte[]{data[6], data[7]});
        switchTime = switchTime * 10;
        accessoryBean.setSwitchTime(switchTime);
      }

      //Do we have to do something with the accessory power on and off bit,
      //as every accessory change will send 2 message one with power on one with power off
      return new AccessoryEvent(accessoryBean);
    } else {
      Logger.warn("Can't parse message, not an Accessory Message! " + message);
      return null;
    }
  }

}
