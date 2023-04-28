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

import jcs.controller.cs3.can.CanMessage;
import org.tinylog.Logger;
import static jcs.controller.cs3.can.MarklinCan.SYSTEM_COMMAND_RESP;

/**
 *
 * @author frans
 */
public class PowerEvent {

    private boolean power;

    public PowerEvent(CanMessage message) {
        parseMessage(message);
    }

    private void parseMessage(CanMessage message) {
        CanMessage resp;
        if (!message.isResponseMessage()) {
            resp = message.getResponse();
        } else {
            resp = message;
        }

        int cmd = message.getCommand();
        int subCmd = message.getSubCommand();

        if (resp.isResponseMessage() && SYSTEM_COMMAND_RESP == cmd && (subCmd == 0 | subCmd == 1)) {

            this.power = subCmd == 1;
        } else {
            Logger.warn("Can't parse message, not a System Go or Stop Response! " + resp);
        }
    }

    public boolean isPower() {
        return power;
    }

}
