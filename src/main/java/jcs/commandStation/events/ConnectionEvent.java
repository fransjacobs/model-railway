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
package jcs.commandStation.events;

/**
 * Event to signal Connection and Disconnection
 */
public class ConnectionEvent {

  private final String source;
  private final boolean connected;
  private final boolean virtual;

  public ConnectionEvent(String source, boolean connected, boolean virtual) {
    this.source = source;
    this.connected = connected;
    this.virtual = virtual;
  }

  public String getSource() {
    return source;
  }

  public boolean isConnected() {
    return connected;
  }

  public boolean isVirtual() {
    return virtual;
  }

}
