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
package jcs.controller.cs.can.parser;

import java.io.Serializable;
import java.util.List;
import jcs.controller.cs.can.CanMessage;
import jcs.controller.cs.can.MarklinCan;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class SystemStatusParser implements Serializable {

  private boolean power;
  private byte[] gfpUid;

  public SystemStatusParser(CanMessage message) {
    parseMessage(message);
  }

  //There might be more than 1 responses.
  //when there are more use the one which contains a valid gfp uid
  private void parseMessage(CanMessage message) {
    if (message != null) {
      CanMessage resp = null;
      List<CanMessage> respList = message.getResponses();
      if (respList.isEmpty()) {
        Logger.warn("No response for: " + message);
        gfpUid = message.getDeviceUidFromMessage();
        int status = message.getData()[4];
        power = status == 1;
      } else {
        for (CanMessage cm : respList) {
          if (cm.isResponseMessage() && cm.isDeviceUidValid()) {
            resp = cm;
          }
        }
        if (resp == null) {
          resp = message;
        }

        if (MarklinCan.SYSTEM_COMMAND_RESP == resp.getCommand() && resp.isDeviceUidValid()) {
          byte[] data = resp.getData();
          gfpUid = resp.getDeviceUidFromMessage();
          int status = data[4];
          power = status == 1;
        }
      }
    } else {
      power = false;
      gfpUid = new byte[]{0, 0, 0, 0};
    }
  }

  @Override
  public String toString() {
    return "SystemStatus{" + " power: " + (power ? "On" : "Off") + " GFP UID: " + ByteUtil.toHexString(gfpUid) + " }";
  }

  public boolean isPower() {
    return power;
  }

  public byte[] getGfpUid() {
    return gfpUid;
  }
}
