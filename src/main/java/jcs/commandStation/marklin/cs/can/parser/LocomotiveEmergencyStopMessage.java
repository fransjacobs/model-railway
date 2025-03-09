/*
 * Copyright 2025 Frans Jacobs.
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

import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 * Emergency Stop for a specific locomotive
 */
public class LocomotiveEmergencyStopMessage {

  public static LocomotiveSpeedEvent parse(CanMessage message) {
    CanMessage resp;
    if (!message.isResponseMessage()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }
    int cmd = resp.getCommand();
    int subCmd = resp.getSubCommand();
    int dlc = resp.getDlc();
    byte[] data = resp.getData();

    if ((cmd == CanMessage.SYSTEM_COMMAND_RESP || cmd == CanMessage.SYSTEM_COMMAND) && subCmd == CanMessage.LOC_STOP_SUB_CMD && dlc == CanMessage.DLC_5) {
      long id = CanMessage.toInt(new byte[]{data[0], data[1], data[2], data[3]});

      LocomotiveBean locomotiveBean = new LocomotiveBean();
      locomotiveBean.setCommandStationId(CanMessage.MARKLIN_COMMANDSTATION_ID);
      locomotiveBean.setId(id);
      locomotiveBean.setVelocity(0);
      return new LocomotiveSpeedEvent(locomotiveBean);
    } else {
      Logger.warn("Can't parse message, not a Locomotive Emergency Stop Message! " + resp);
      return null;
    }

  }

}
