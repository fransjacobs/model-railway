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

  private static final String MARKLIN_CS = "marklin.cs";

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

      //Derive the busNumber
      int busNumber;
      if (contactId < 1000) {
        busNumber = 0;
      } else if (contactId >= 1000 && contactId < 2000) {
        busNumber = 1;
      } else if (contactId >= 2000 && contactId < 3000) {
        busNumber = 2;
      } else {
        busNumber = 3;
      }

      //SensorBean sensorBean = new SensorBean(contactId, null, null, null, identifier, status, previousStatus, millis, System.currentTimeMillis(), MARKLIN_CS, busNumber);
      SensorBean sensorBean = new SensorBean(contactId, null, null, null, identifier, status, previousStatus, millis, System.currentTimeMillis(), MARKLIN_CS, busNumber);
      return sensorBean;
    } else {
      Logger.warn("Can't parse message, not a Sensor Response! " + resp);
      return null;
    }
  }

}

//TRACE	2025-07-07 22:21:44.635 [CS-EVENT-MESSAGE-HANDLER] MarklinCentralStationImpl$EventMessageHandler.run(): FeedbackSensorEvent RX: 0x00 0x23 0x3f 0x3c 0x08 0x00 0x41 0x07 0xd1 0x00 0x01 0x04 0xb0
//TRACE	2025-07-07 22:21:44.635 [CS-EVENT-MESSAGE-HANDLER] MarklinCentralStationImpl$EventMessageHandler.run(): Sensor 2001 value 1
//TRACE	2025-07-07 22:21:45.354 [CS-EVENT-MESSAGE-HANDLER] MarklinCentralStationImpl$EventMessageHandler.run(): FeedbackSensorEvent RX: 0x00 0x23 0x3f 0x3c 0x08 0x00 0x41 0x07 0xd1 0x01 0x00 0x00 0x46
//TRACE	2025-07-07 22:21:45.355 [CS-EVENT-MESSAGE-HANDLER] MarklinCentralStationImpl$EventMessageHandler.run(): Sensor 2001 value 0
//TRACE	2025-07-07 22:21:48.144 [CS-EVENT-MESSAGE-HANDLER] MarklinCentralStationImpl$EventMessageHandler.run(): FeedbackSensorEvent RX: 0x00 0x23 0x3f 0x3c 0x08 0x00 0x41 0x07 0xd2 0x00 0x01 0x08 0xc0
//TRACE	2025-07-07 22:21:48.144 [CS-EVENT-MESSAGE-HANDLER] MarklinCentralStationImpl$EventMessageHandler.run(): Sensor 2002 value 1
//TRACE	2025-07-07 22:21:48.844 [CS-EVENT-MESSAGE-HANDLER] MarklinCentralStationImpl$EventMessageHandler.run(): FeedbackSensorEvent RX: 0x00 0x23 0x3f 0x3c 0x08 0x00 0x41 0x07 0xd2 0x01 0x00 0x00 0x46
//TRACE	2025-07-07 22:21:48.844 [CS-EVENT-MESSAGE-HANDLER] MarklinCentralStationImpl$EventMessageHandler.run(): Sensor 2002 value 0
