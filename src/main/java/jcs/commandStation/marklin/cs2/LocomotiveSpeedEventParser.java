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
package jcs.commandStation.marklin.cs2;

import jcs.commandStation.events.*;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 */
public class LocomotiveSpeedEventParser {
  
  public static LocomotiveSpeedEvent parseMessage(CanMessage message) {
    LocomotiveBean locomotiveBean = new LocomotiveBean();
    locomotiveBean.setCommandStationId(CanMessage.MARKLIN_COMMANDSTATION_ID);

    CanMessage resp;
    if (!message.isResponseMessage()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }

    if (resp.isResponseMessage() && CanMessage.SYSTEM_COMMAND == resp.getCommand() && CanMessage.LOC_STOP_SUB_CMD == resp.getSubCommand() && CanMessage.DLC_5 == resp.getDlc()) {
      //Loco halt command could be issued due to a direction change.
      byte[] data = resp.getData();
      long id = CanMessage.toInt(new byte[]{data[0], data[1], data[2], data[3]});

      locomotiveBean.setId(id);
      locomotiveBean.setVelocity(0);
    } else if (resp.isResponseMessage() && CanMessage.LOC_VELOCITY_RESP == resp.getCommand()) {
      byte[] data = resp.getData();
      long id = CanMessage.toInt(new byte[]{data[0], data[1], data[2], data[3]});

      int velocity = CanMessage.toInt(new byte[]{data[4], data[5]});

      locomotiveBean.setId(id);
      locomotiveBean.setVelocity(velocity);

    } else {
      Logger.warn("Can't parse message, not a Locomotive Velocity or a Locomotive Emergency Stop Message! " + resp);
      return null;
    }
    return new LocomotiveSpeedEvent(locomotiveBean);
  }

}
