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

import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 */
public class LocomotiveVelocityMessage {

  public static LocomotiveSpeedEvent parse(CanMessage message) {
    LocomotiveBean locomotiveBean = new LocomotiveBean();
    locomotiveBean.setCommandStationId(CanMessage.MARKLIN_COMMANDSTATION_ID);

    CanMessage resp;
    if (!message.isResponseMessage()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }

    if (resp.isResponseMessage() && CanMessage.LOC_VELOCITY_RESP == resp.getCommand()) {
      byte[] data = resp.getData();
      long id = CanMessage.toInt(new byte[]{data[0], data[1], data[2], data[3]});

      int velocity = CanMessage.toInt(new byte[]{data[4], data[5]});

      locomotiveBean.setId(id);
      locomotiveBean.setVelocity(velocity);
      //Use the address range to determine the DecoderType
      if (locomotiveBean.getDecoderType() == null) {
        if (id > 49152) {
          locomotiveBean.setDecoderTypeString(LocomotiveBean.DecoderType.DCC.getDecoderType());
        } else if (id > 16384) {
          locomotiveBean.setDecoderTypeString(LocomotiveBean.DecoderType.MFX.getDecoderType());
        } else if (id > 2.048) {
          locomotiveBean.setDecoderTypeString(LocomotiveBean.DecoderType.SX1.getDecoderType());
        } else {
          locomotiveBean.setDecoderTypeString(LocomotiveBean.DecoderType.MM.getDecoderType());
        }
      }

    } else {
      Logger.warn("Can't parse message, not a Locomotive Velocity Message! " + resp);
      return null;
    }
    return new LocomotiveSpeedEvent(locomotiveBean);
  }

}
