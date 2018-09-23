/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.controller.marklin.serial;

import lan.wervel.jcs.controller.marklin.M6050CommandThread;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class CommandPair {

  private final int address;
  private final int command;
  private Integer[] response;
  private int resCount = 0;

  public CommandPair(int command) {
    this(command, -1);
  }

  public CommandPair(int command, int address) {
    this.command = command;
    this.address = address;

    if (isFeedbackExpected()) {
      this.response = new Integer[2];
    }
  }

  public final boolean isFeedbackExpected() {
    return ((M6050CommandThread.FEEDBACK_MULTIPLE_FIRST <= command && M6050CommandThread.FEEDBACK_MULTIPLE_LAST >= command)
            || (M6050CommandThread.FEEDBACK_FIRST <= command && M6050CommandThread.FEEDBACK_LAST >= command));
  }

  public final boolean isPowerOnCommand() {
    return Serial6050.GO_COMMAND == this.command;
  }

  public final boolean isPowerOffCommand() {
    return Serial6050.STOP_COMMAND == this.command;
  }

  public final boolean isSolenoidCommand() {
    return M6050CommandThread.SWITCH_RED == this.command || M6050CommandThread.SWITCH_GREEN == this.command;
  }

  public final boolean isSwitchOffCommand() {
    return M6050CommandThread.SWITCH_OFF == this.command;
  }

  public int getCommand() {
    return this.command;
  }

  public int getAddress() {
    return this.address;
  }

  public boolean isAddress() {
    return this.address > 0;
  }

  public void addResponse(Integer response) {
    if(resCount < 2) {
      this.response[resCount] = response;
      resCount++;
    }  
  }

  public int getModuleNumber() {
    return this.command - M6050CommandThread.FEEDBACK_OFFSET;
  }

  public Integer[] getResponse() {
    return this.response;
  }
  
  public boolean isResponseComplete() {
    return this.resCount == 2;
  }

  /**
   * Blocking call for result
   *
   * @return a List with the 2 result bytes as a integer
   */
  public Integer[] getFeedback() {
    // wait on the feedback but ensure a timeout...
    long now = System.currentTimeMillis();
    long timeout = now + Serial6050.RESPONSE_TIMEOUT;
    //Logger.trace("Waiting max. " + Serial6050.RESPONSE_TIMEOUT + " ms for feedback...");

    while (this.resCount < 2 && timeout > now) {
      now = System.currentTimeMillis();
    }
    
    if (this.resCount != 2 && timeout < now) {
      Logger.warn("Feedback timeout exceeded! Response size: " + this.resCount + "....");
    }
    return this.response;
  }
}
