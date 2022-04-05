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
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class DirectionMessageEvent implements Serializable {

    private LocomotiveBean locomotiveBean;

    private Integer updatedFunctionNumber;

    public DirectionMessageEvent(LocomotiveBean locomotiveBean) {
        this.locomotiveBean = locomotiveBean;
    }

    public DirectionMessageEvent(CanMessage message) {
        parseMessage(message);
    }

    private void parseMessage(CanMessage message) {
        CanMessage resp;
        if (!message.isResponseMessage()) {
            resp = message.getResponse();
        } else {
            resp = message;
        }

        if (resp.isResponseMessage() && MarklinCan.LOC_DIRECTION_RESP == resp.getCommand()) {
            int[] data = resp.getData();
            Integer locId = ByteUtil.toInt(new int[]{data[0], data[1], data[2], data[3]});

            Integer richtung = data[4] & 0xff;
            LocomotiveBean lb = new LocomotiveBean();
            BigDecimal id = new BigDecimal(locId);

            lb.setId(id);
            lb.setRichtung(richtung);

            if (lb.getId() != null && lb.getRichtung() != null) {
                this.locomotiveBean = lb;
            }
        } else {
            Logger.warn("Can't parse message, not an Locomotive Direction Response! " + resp);
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
