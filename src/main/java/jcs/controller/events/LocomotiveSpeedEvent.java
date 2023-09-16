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
package jcs.controller.events;

import java.io.Serializable;
import jcs.controller.cs.can.CanMessage;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class LocomotiveSpeedEvent implements Serializable {

  private LocomotiveBean locomotiveBean;

  public LocomotiveSpeedEvent(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
  }

  public LocomotiveSpeedEvent(CanMessage message) {
    parseMessage(message);
  }

  private void parseMessage(CanMessage message) {
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

      LocomotiveBean lb = new LocomotiveBean();
      lb.setId(id);
      lb.setVelocity(0);

      if (lb.getId() != null && lb.getVelocity() != null) {
        this.locomotiveBean = lb;
      }
    } else if (resp.isResponseMessage() && CanMessage.LOC_VELOCITY_RESP == resp.getCommand()) {
      byte[] data = resp.getData();
      long id = CanMessage.toInt(new byte[]{data[0], data[1], data[2], data[3]});

      int velocity = CanMessage.toInt(new byte[]{data[4], data[5]});

      LocomotiveBean lb = new LocomotiveBean();
      lb.setId(id);
      lb.setVelocity(velocity);

      if (lb.getId() != null && lb.getVelocity() != null) {
        this.locomotiveBean = lb;
      }
    } else {
      Logger.warn("Can't parse message, not a Locomotive Velocity or a Locomotive Emergency Stop Message! " + resp);
    }
  }

  public LocomotiveBean getLocomotiveBean() {
    return locomotiveBean;
  }

  public void setLocomotiveBean(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
  }

  public boolean isValid() {
    return this.locomotiveBean != null && this.locomotiveBean.getId() != null;
  }

  public boolean isEventFor(LocomotiveBean locomotive) {
    return this.locomotiveBean.getId().equals(locomotive.getId());
  }

}
