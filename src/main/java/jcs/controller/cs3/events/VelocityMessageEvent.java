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
import jcs.entities.LocomotiveBean;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class VelocityMessageEvent implements Serializable {

    private LocomotiveBean locomotiveBean;

    private Integer updatedFunctionNumber;

    public VelocityMessageEvent(LocomotiveBean locomotiveBean) {
        this.locomotiveBean = locomotiveBean;
    }

    public VelocityMessageEvent(CanMessage message) {
        parseMessage(message);
    }

    private void parseMessage(CanMessage message) {
        CanMessage resp;
        if (!message.isResponseMessage()) {
            resp = message.getResponse();
        } else {
            resp = message;
        }

        if (resp.isResponseMessage() && MarklinCan.SYSTEM_COMMAND == resp.getCommand() && MarklinCan.LOC_STOP_SUB_CMD == resp.getSubCommand() && MarklinCan.DLC_5 == resp.getDlc()) {
            //Loc halt command could be issued due to a direction change.
            int[] data = resp.getData();
            Long id = ByteUtil.toLong(new int[]{data[0], data[1], data[2], data[3]});

            LocomotiveBean lb = new LocomotiveBean();
            lb.setId(id);
            lb.setVelocity(0);

            if (lb.getId() != null && lb.getVelocity() != null) {
                this.locomotiveBean = lb;
            }
        } else if (resp.isResponseMessage() && MarklinCan.LOC_VELOCITY_RESP == resp.getCommand()) {
            int[] data = resp.getData();
            Long id = ByteUtil.toLong(new int[]{data[0], data[1], data[2], data[3]});

            Integer velocity = ByteUtil.toInt(new int[]{data[4], data[5]});

            LocomotiveBean lb = new LocomotiveBean();
            lb.setId(id);
            lb.setVelocity(velocity);

            if (lb.getId() != null && lb.getVelocity() != null) {
                this.locomotiveBean = lb;
            }
        } else {
            Logger.warn("Can't parse message, not an Locomotive Velocity or a Locomotive emergency stop Response! " + resp);
        }
    }

    public LocomotiveBean getLocomotiveBean() {
        return locomotiveBean;
    }

    public void setLocomotiveBean(LocomotiveBean locomotiveBean) {
        this.locomotiveBean = locomotiveBean;
    }

    public Integer getUpdatedFunctionNumber() {
        return updatedFunctionNumber;
    }

}
