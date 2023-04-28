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

import java.net.InetAddress;
import jcs.controller.cs3.can.CanMessage;
import jcs.controller.cs3.can.MarklinCan;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public abstract class DiscoveryResponseListener implements CanMessageListener {

    @Override
    public void onCanMessage(CanMessageEvent canEvent) {
        CanMessage can = canEvent.getCanMessage();
        int command = can.getCommand();

        if (command == MarklinCan.SW_STATUS_REQ) {
            Logger.trace("Response Command: " + Integer.toHexString(command));
            //got response
            setHost(canEvent.getSourceAddress());

            //onResponse();
        }
    }

    public abstract void setHost(InetAddress hostAddress);

}
