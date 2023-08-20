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
package jcs.controller.cs.net;

import java.net.InetAddress;
import jcs.controller.cs.can.CanMessage;
import jcs.controller.cs.events.CanPingListener;
import jcs.controller.cs.events.FeedbackEventListener;
import jcs.controller.cs.events.SystemEventListener;

/**
 *
 * @author Frans Jacobs
 */
public interface CSConnection extends AutoCloseable {

  static final int MAX_ERRORS = 15;

  static final int CS_TX_PORT = 15730;

  static final int CS_RX_PORT = 15731;

  CanMessage sendCanMessage(CanMessage message);

  //void setCanMessageListener(CanMessageListener listener);

  void setCanPingRequestListener(CanPingListener listener);
  
  void setFeedbackEventListener(FeedbackEventListener listener);

  void setSystemEventListener(SystemEventListener systemEventListener);
  
  InetAddress getControllerAddress();

  boolean isConnected();

}
