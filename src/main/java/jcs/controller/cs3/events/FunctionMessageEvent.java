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

    if (resp.isResponseMessage() && CanMessage.LOC_FUNCTION_RESP == resp.getCommand()) {
      byte[] data = resp.getData();
      Long id = ByteUtil.toLong(new int[]{data[0], data[1], data[2], data[3]});

      Integer functionNumber = data[4] & 0xff;
      Integer functionValue = data[5] & 0xff;
      this.locomotiveBean = new LocomotiveBean();

      FunctionBean function = new FunctionBean(functionNumber, id);
      function.setValue(functionValue);

      this.locomotiveBean.setId(id);
      this.locomotiveBean.addFunction(function);
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
