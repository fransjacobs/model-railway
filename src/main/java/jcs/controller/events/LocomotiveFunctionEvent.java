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
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class LocomotiveFunctionEvent implements Serializable {

  private LocomotiveBean locomotiveBean;

  private Integer updatedFunctionNumber;

  public LocomotiveFunctionEvent(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
  }

  public LocomotiveFunctionEvent(CanMessage message) {
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
      long id = CanMessage.toInt(new byte[]{data[0], data[1], data[2], data[3]});

      int functionNumber = data[4];
      int functionValue = data[5];
      this.locomotiveBean = new LocomotiveBean();

      FunctionBean function = new FunctionBean(functionNumber, id);
      function.setValue(functionValue);

      this.locomotiveBean.setId(id);
      this.locomotiveBean.addFunction(function);
      this.updatedFunctionNumber = functionNumber;
    } else {
      Logger.warn("Can't parse message, not an Locomotive Function Message! " + resp);
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

  public boolean isValid() {
    return this.locomotiveBean != null && this.locomotiveBean.getId() != null;
  }

  public boolean isEventFor(LocomotiveBean locomotive) {
    return this.locomotiveBean.getId().equals(locomotive.getId());
  }

}
