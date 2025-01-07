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
package jcs.commandStation.marklin.cs2;

import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.FunctionBean;
import org.tinylog.Logger;

/**
 *
 */
public class LocomotiveFunctionEventParser {

  public static LocomotiveFunctionEvent parseMessage(CanMessage message) {
    CanMessage resp;
    if (!message.isResponseMessage()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }

    if (resp.isResponseMessage() && CanMessage.LOC_FUNCTION_RESP == resp.getCommand()) {
      byte[] data = resp.getData();
      long locomotiveId = CanMessage.toInt(new byte[]{data[0], data[1], data[2], data[3]});

      int functionNumber = data[4];
      int functionValue = data[5];

      FunctionBean fb = new FunctionBean(functionNumber, locomotiveId);
      fb.setValue(functionValue);

      return new LocomotiveFunctionEvent(fb);
    } else {
      Logger.warn("Can't parse message, not an Locomotive Function Message! " + resp);
      return null;
    }
  }
}
