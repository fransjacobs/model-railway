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
package jcs.controller.dcc_ex;

/**
 *
 * @author Frans Jacobs
 */
public interface DccExConnection extends AutoCloseable {

  static final int MAX_ERRORS = 15;

  static final int PORT = 2560;
  
  //Connection Type Network or Serial

  String sendMessage(String message);

  //void setFeedbackListener(FeedbackListener feedbackListener);

  //void setSystemListener(SystemListener systemListener);

  //void setAccessoryListener(AccessoryListener accessoryListener);

  //void setLocomotiveListener(LocomotiveListener locomotiveListener);

  boolean isConnected();

}
