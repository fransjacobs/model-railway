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
package jcs.commandStation.hsis88;

/**
 *
 * @author Frans Jacobs
 */
public interface HSIConnection extends AutoCloseable {

  static final int MAX_ERRORS = 15;

  static final String MESSAGE_DELIMITER = "\r";

  static final String COMMAND_TOGGLE_TERMINAL_MODE = "t\r";

  static final String COMMAND_QUERY = "m\r";

  static final String COMMAND_SET = "s\r";

  static final String COMMAND_VERSION = "v\r";

  static final String RESPONSE_ID_MODULES = "i";

  static final String RESPONSE_ID_TERMINAL_MODE = "t";

  static final String RESPONSE_ID_SET = "s";

  String sendMessage(String message);

  void addMessageListener(HSIMessageListener messageListener);

  boolean isConnected();

}
