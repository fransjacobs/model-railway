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
package jcs.commandStation.marklin.cs.can.parser;

import java.util.List;
import jcs.commandStation.marklin.cs.can.CanMessage;

/**
 * SystemStatus parser
 */
public class SystemStatus  {

  public static boolean parseSystemPowerMessage(CanMessage message) {
    if (message == null) {
      return false;
    }
    List<CanMessage> respList = message.getResponses();
    if (respList.isEmpty()) {
      return message.getData()[4] == 1;
    } else {
      for (CanMessage cm : respList) {
        if (CanMessage.SYSTEM_COMMAND_RESP == cm.getCommand() && cm.getDlc() == CanMessage.DLC_5) {
          return message.getData()[4] == 1;
        }
      }
    }
    return false;
  }
}
