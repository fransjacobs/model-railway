/*
 * Copyright 2026 Frans Jacobs.
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

import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNull;

class SystemStatusMessageTest {

  @Test
  void unansweredSystemStatusIsNotTreatedAsAnInvalidResponse() {
    MeasuringChannel channel = new MeasuringChannel();
    channel.setName("VOLT");
    CanMessage request = new CanMessage(
            0, CanMessage.SYSTEM_COMMAND, 0, CanMessage.DLC_6,
            new byte[]{0, 0, 0, 1, CanMessage.SYSTEM_SUB_STATUS, 3, 0, 0});

    assertNull(SystemStatusMessage.parse(channel, request));
  }

  @Test
  void missingChannelAcknowledgementIsNotAValidMeasurement() {
    MeasuringChannel channel = new MeasuringChannel();
    channel.setName("TEMP");
    CanMessage request = new CanMessage(
            0, CanMessage.SYSTEM_COMMAND, 0, CanMessage.DLC_6,
            new byte[]{0, 0, 0, 1, CanMessage.SYSTEM_SUB_STATUS, 4, 0, 0});
    CanMessage unavailable = new CanMessage(
            0, CanMessage.SYSTEM_COMMAND + 1, 0, CanMessage.DLC_6,
            new byte[]{0, 0, 0, 1, CanMessage.SYSTEM_SUB_STATUS, 4, 0, 0});
    request.addResponse(unavailable);
    assertNull(SystemStatusMessage.parse(channel, request));
  }
}
