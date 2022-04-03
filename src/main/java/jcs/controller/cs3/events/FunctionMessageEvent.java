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
public class FunctionMessageEvent implements Serializable {
    
    private LocomotiveBean locomotiveBean;
    
    private Integer updatedFunctionNumber;
    
    public FunctionMessageEvent(LocomotiveBean locomotiveBean) {
        this.locomotiveBean = locomotiveBean;
    }
    
    public FunctionMessageEvent(CanMessage message) {
        parseMessage(message);
    }
    
    private void parseMessage(CanMessage message) {
        CanMessage resp;
        if (!message.isResponseMessage()) {
            resp = message.getResponse();
        } else {
            resp = message;
        }
        
        if (resp.isResponseMessage() && MarklinCan.LOC_FUNCTION_RESP == resp.getCommand()) {
            int[] data = resp.getData();
            Integer locId = ByteUtil.toInt(new int[]{data[0], data[1], data[2], data[3]});
            
            Integer functionNumber = data[4] & 0xff;
            Integer functionValue = data[5] & 0xff;
            this.locomotiveBean = new LocomotiveBean();
            BigDecimal id = new BigDecimal(locId);
            
            FunctionBean function = new FunctionBean(functionNumber, id);
            function.setValue(functionValue);
            
            this.locomotiveBean.setId(id);
            this.locomotiveBean.addFunctions(function);
            this.updatedFunctionNumber = functionNumber;
        } else {
            Logger.warn("Can't parse message, not an Locomotive Function Response! " + resp);
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
