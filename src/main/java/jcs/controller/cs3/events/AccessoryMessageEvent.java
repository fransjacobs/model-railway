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
            int[] data = resp.getData();
            Integer address = data[3] & 0xff;
            Integer position = data[4] & 0xff;
            //CS is zero based
            address = address + 1;
            this.accessoryBean = new AccessoryBean(new BigDecimal(address), null, null, position, null, null, null);
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
