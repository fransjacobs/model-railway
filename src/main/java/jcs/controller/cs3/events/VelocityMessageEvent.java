/*
 * Copyright (C) 2020 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.controller.cs3.events;

import java.io.Serializable;
import java.math.BigDecimal;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.MarklinCan;
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
            Integer locId = ByteUtil.toInt(new int[]{data[0], data[1], data[2], data[3]});
            BigDecimal id = new BigDecimal(locId);

            LocomotiveBean lb = new LocomotiveBean();
            lb.setId(id);
            lb.setVelocity(0);

            if (lb.getId() != null && lb.getVelocity() != null) {
                this.locomotiveBean = lb;
            }
        } else if (resp.isResponseMessage() && MarklinCan.LOC_VELOCITY_RESP == resp.getCommand()) {
            int[] data = resp.getData();
            Integer locId = ByteUtil.toInt(new int[]{data[0], data[1], data[2], data[3]});
            BigDecimal id = new BigDecimal(locId);

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
