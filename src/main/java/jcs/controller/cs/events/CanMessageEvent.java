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
package jcs.controller.cs.events;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import jcs.controller.cs.can.CanMessage;

public class CanMessageEvent implements Serializable {

    private final CanMessage canMessage;
    private final InetAddress sourceAddress;
    private final Date eventDate;

    public CanMessageEvent(CanMessage canMessage) {
        this(canMessage, null);
    }

    public CanMessageEvent(CanMessage canMessage, InetAddress sourceAddress) {
        this(canMessage, sourceAddress, new Date());
    }

    public CanMessageEvent(CanMessage canMessage, InetAddress sourceAddress, Date eventDate) {
        this.canMessage = canMessage;
        this.sourceAddress = sourceAddress;
        this.eventDate = eventDate;
    }

    public CanMessage getCanMessage() {
        return canMessage;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    public Date getEventDate() {
        return eventDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CanMessageEvent{message: ").append(canMessage);
        if (sourceAddress != null) {
            sb.append(", from: ").append(sourceAddress.getHostAddress());
        }
        sb.append('}');
        return sb.toString();
    }

}
