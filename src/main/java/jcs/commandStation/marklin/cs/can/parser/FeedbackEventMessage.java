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
package jcs.commandStation.marklin.cs.can.parser;

import java.util.Date;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.SensorBean;
import jcs.util.ByteUtil;
import org.tinylog.Logger;

/**
 *
 * Parse Sensor messages
 */
public class FeedbackEventMessage {

  public static SensorBean parse(CanMessage message, Date eventDate) {
    CanMessage resp;
    if (!message.isResponseMessage()) {
      resp = message.getResponse();
    } else {
      resp = message;
    }

    if (resp.isResponseMessage() && CanMessage.S88_EVENT_RESPONSE == resp.getCommand()) {
      byte[] data = resp.getData();

      Integer identifier = ByteUtil.toInt(new byte[]{data[0], data[1]});
      Integer contactId = ByteUtil.toInt(new byte[]{data[2], data[3]});

      int previousStatus = data[4];
      int status = data[5];

      Integer millis = ByteUtil.toInt(new byte[]{data[6], data[7]}) * 10;

      SensorBean sensorBean = new SensorBean(contactId, null, contactId, contactId, identifier, status, previousStatus, millis, System.currentTimeMillis());
      return sensorBean;
    } else {
      Logger.warn("Can't parse message, not a Sensor Response! " + resp);
      return null;
    }
  }

}
