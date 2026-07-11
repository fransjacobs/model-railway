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
package jcs.commandStation.uhlenbrock.p50x;

/**
 * <p>
 * P50x Message
 * <p>
 * Message start with command which is usually 1 byte
 * <P>
 * the command can have parameters which is max 4 bytes
 * <p>
 * A reply will usually follow which can be any size bytes
 */
class P50xMessage {

  private final int command;
  private int[] parameters;
  private int[] reply;

  P50xMessage(int command) {
    this(command, new int[0]);
  }

  P50xMessage(int command, int[] parameters) {
    this.command = command;
    this.parameters = parameters;
  }

  int getCommand() {
    return this.command;
  }

  void addReply(int[] reply) {
  }

}
