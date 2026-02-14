/*
 * Copyright 2026 frans.
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
package jcs.commandStation.automation.statemachine;


// Event types
enum EventType {
    SENSOR_TRIGGERED,
    LOCOMOTIVE_ARRIVED,
    TIMEOUT,
    EMERGENCY_STOP
}

// Event data
class Event {
    private final EventType type;
    private final Object data;
    private final long timestamp;
    
    public Event(EventType type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public EventType getType() { return type; }
    public Object getData() { return data; }
    public long getTimestamp() { return timestamp; }
}
