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
import java.util.Objects;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class LocomotiveDirectionEvent implements Serializable {

  private LocomotiveBean locomotiveBean;

  public LocomotiveDirectionEvent(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
  }

  public LocomotiveDirectionEvent(long locomotiveBeanId, LocomotiveBean.Direction direction, String commandStationId) {
    createLocomotiveBean(locomotiveBeanId, direction, commandStationId);
  }

  //TODO: move Marklin code to the CS implementation
  public LocomotiveDirectionEvent(CanMessage message) {
    parseMessage(message);
  }

  private void createLocomotiveBean(long locomotiveBeanId, LocomotiveBean.Direction direction, String commandStationId) {
    locomotiveBean = new LocomotiveBean();
    locomotiveBean.setId(locomotiveBeanId);
    locomotiveBean.setCommandStationId(commandStationId);
    locomotiveBean.setVelocity(0);
    locomotiveBean.setDirection(direction);
  }

  private void parseMessage(CanMessage message) {
    CanMessage resp;
    if (!message.isResponseMessage()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }

    if (resp.isResponseMessage() && CanMessage.LOC_DIRECTION_RESP == resp.getCommand()) {
      byte[] data = resp.getData();
      long id = CanMessage.toInt(new byte[]{data[0], data[1], data[2], data[3]});

      int richtung = data[4];
      LocomotiveBean lb = new LocomotiveBean();

      lb.setId(id);
      lb.setRichtung(richtung);

      if (lb.getId() != null && lb.getRichtung() != null) {
        this.locomotiveBean = lb;
      }
    } else {
      Logger.warn("Can't parse message, not a Locomotive Direction Message! " + resp);
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

  public Direction getNewDirection() {
    return this.locomotiveBean.getDirection();
  }

  public boolean isEventFor(LocomotiveBean locomotive) {
    if (locomotive != null) {
      Long id = locomotiveBean.getId();

      String csId = locomotiveBean.getCommandStationId();
      int address = locomotiveBean.getAddress();
      LocomotiveBean.DecoderType decoderType = locomotiveBean.getDecoderType();

      if (Objects.equals(id, locomotive.getId())) {
        return true;
      } else {
        //Check also the logical key
        if (!Objects.equals(csId, locomotive.getCommandStationId())) {
          return false;
        }
        if (!Objects.equals(decoderType, locomotive.getDecoderType())) {
          return false;
        }
        return Objects.equals(address, locomotive.getAddress());
      }
    } else {
      return false;
    }
  }
}
