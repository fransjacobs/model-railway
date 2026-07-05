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
package jcs.commandStation.uhlenbrock.connection;

import org.tinylog.Logger;

/**
 *
 */
public class P50xMessage {

  static final int SOLENOID_MAX_ACTIVE_TIME = 2000;
  static final int PAUSE_BETWEEN_CMD = SerialPortImpl.PAUSE_BETWEEN_CMD;
  static final int PAUSE_BETWEEN_BYTES = SerialPortImpl.PAUSE_BETWEEN_BYTES;

  static final int SWITCH_OFF = 32;
  static final int SWITCH_RED = 34;
  static final int SWITCH_GREEN = 33;
  static final int SWITCH_CURVED = 34;
  static final int SWITCH_STRAIGHT = 33;

  static final int REVERSE_COMMAND = 15;
  static final int FUNCTION_OFFSET = 16;
  static final int SPECIAL_FUNCTION_OFFSET = 64;
  private static final int MAX_SPEED_VAL = 1023;
  static final int MAX_SPEED = 14;
  static final int MIN_SPEED = 0;

  static final int FEEDBACK_MULTIPLE_FIRST = 129;
  static final int FEEDBACK_MULTIPLE_LAST = 159;
  static final int FEEDBACK_OFFSET = 192;
  static final int FEEDBACK_FIRST = 193;
  static final int FEEDBACK_LAST = 223;

  private final int address;
  private final int command;
  private int[] response;
  private int resCount = 0;

  P50xMessage(int command) {
    this(command, -1);
  }

  P50xMessage(int command, int address) {
    this.command = command;
    this.address = address;

    if (isFeedbackExpected()) {
      this.response = new int[2];
    }
  }

  final boolean isFeedbackExpected() {
    return false;
    //((M6050Controller.FEEDBACK_MULTIPLE_FIRST <= command && M6050Controller.FEEDBACK_MULTIPLE_LAST >= command)
    //        || (M6050Controller.FEEDBACK_FIRST <= command && M6050Controller.FEEDBACK_LAST >= command));
  }

  final boolean isPowerOnCommand() {
    return SerialPortImpl.GO_COMMAND == this.command;
  }

  final boolean isPowerOffCommand() {
    return SerialPortImpl.STOP_COMMAND == this.command;
  }

  final boolean isSolenoidCommand() {
    return false; //M6050Controller.SWITCH_RED == this.command || M6050Controller.SWITCH_GREEN == this.command;
  }

  final boolean isSwitchOffCommand() {
    return false; //M6050Controller.SWITCH_OFF == this.command;
  }

  int getCommand() {
    return this.command;
  }

  int getAddress() {
    return this.address;
  }

  boolean hasAddress() {
    return this.address > 0;
  }

  void addResponse(int response) {
    if (resCount < 2) {
      this.response[resCount] = response;
      resCount++;
    }
  }

  int getModuleNumber() {
    return 0; //this.command - M6050Controller.FEEDBACK_OFFSET;
  }

  int[] getResponse() {
    return this.response;
  }

  boolean isResponseComplete() {
    return this.resCount == 2;
  }

  /**
   * Blocking call for result
   *
   * @return a List with the 2 result bytes as a integer
   */
  int[] getFeedback() {
    // wait on the feedback but ensure a timeout...
    long now = System.currentTimeMillis();
    long timeout = now + SerialPortImpl.RESPONSE_TIMEOUT;

    while (this.resCount < 2 && timeout > now) {
      now = System.currentTimeMillis();
    }

    if (this.resCount != 2 && timeout < now) {
      Logger.warn("Feedback timeout exceeded! Response size: " + this.resCount + "....");
    }
    return this.response;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    if (command < 10) {
      sb.append("0");
    }
    sb.append(command);
    if (hasAddress()) {
      sb.append(",[");
      if (address < 10) {
        sb.append("0");
      }
      sb.append(address);
      sb.append("]");
    }
    sb.append("}");
    return sb.toString();
  }

}
