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
package jcs.commandStation.marklin.cs.net;

import java.net.InetAddress;
import java.util.concurrent.TransferQueue;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.events.ConnectionEventListener;

public interface CSConnection extends AutoCloseable {

  static final int MAX_ERRORS = 15;

  static final int CS_TX_PORT = 15730;

  static final int CS_RX_PORT = 15731;

  CanMessage sendCanMessage(CanMessage message);

  InetAddress getControllerAddress();

  boolean isConnected();

  TransferQueue<CanMessage> getEventQueue();
  
  void addDisconnectionEventListener(ConnectionEventListener listener);


}
