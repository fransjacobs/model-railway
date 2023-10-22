/*
 * Copyright 2018 Frans Jacobs.
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
package jcs.controller.marklin.m6050;

import java.util.concurrent.LinkedBlockingQueue;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class M6050CommandHandler extends Thread {

  private final LinkedBlockingQueue<CommandAddressPair> commandQueue;
  private boolean running = false;
  private final SerialPortImpl serialP50;
  private Long lastSolenoidCommand;

  static final int SOLENOID_MAX_ACTIVE_TIME = 2000;
  static final int PAUSE_BETWEEN_CMD = SerialPortImpl.PAUSE_BETWEEN_CMD;
  static final int PAUSE_BETWEEN_BYTES = SerialPortImpl.PAUSE_BETWEEN_BYTES;
  static final Integer SWITCH_OFF = 32;

  M6050CommandHandler(SerialPortImpl serialImpl) {
    commandQueue = new LinkedBlockingQueue<>();
    serialP50 = serialImpl;
  }

  void enqueue(CommandAddressPair command) {
    if (serialP50.isPowerOn()) {
      commandQueue.offer(command);
      Logger.trace(command);

      if (!isRunning()) {
        Logger.debug("Not running restart...");
        start();
      }

    }
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

  private boolean sendCommandSequence(CommandAddressPair commandAddressPair) {
    Logger.trace("Sending command: " + commandAddressPair);
    //send command to serial port...
    boolean result = serialP50.sendCommand(commandAddressPair);

    if (commandAddressPair.isSolenoidCommand()) {
      lastSolenoidCommand = System.currentTimeMillis();
      serialP50.setSolenoidActive(true);
      Logger.trace("Last SolenoidCommand @: " + lastSolenoidCommand + " Solenoid Active: " + serialP50.isSolenoidActive());
    }
    return result;
  }

  @Override
  public void run() {
    Logger.debug("Starting P50 Command Handler...");
    this.running = true;
    while (running) {

      while (!commandQueue.isEmpty()) {
        CommandAddressPair commandAddressPair = commandQueue.poll();

        Logger.trace("Polled: " + commandAddressPair);

        boolean result = sendCommandSequence(commandAddressPair);
        Logger.debug("Command: " + commandAddressPair + " send. "+(result?"":" NOK!"));

        //Ensure the command thread is not stopping unexpectedly hence the try catch
        try {
          if (commandAddressPair.isFeedbackExpected()) {
            //when it is a feedback event get the feedback from the result        
            int[] response = serialP50.getFeedback(commandAddressPair.getModuleNumber());

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(response[0]);
            sb.append(",");
            sb.append(response[1]);
            sb.append("]");

            if (response != null && response.length == 2) {
              Logger.trace("Response for Module: " + commandAddressPair.getModuleNumber() + " -> " + sb);
            } else {
              Logger.warn("No Response for Module: " + commandAddressPair.getModuleNumber() + "...");
            }
          }
        } catch (Exception e) {
          Logger.error("Error during feedback processing! " + e.getMessage());
        }
        //we are done...
      }
      pause(PAUSE_BETWEEN_CMD);
      if (serialP50 != null && serialP50.isSolenoidActive() && (lastSolenoidCommand + SOLENOID_MAX_ACTIVE_TIME) < System.currentTimeMillis()) {
        Logger.debug("Deactivating Solenoids...");
        serialP50.sendSingleCommand(SWITCH_OFF);
        pause(PAUSE_BETWEEN_CMD);
      }
    }
  }

  void purge() {
    commandQueue.clear();
  }

  boolean isRunning() {
    return running;
  }

  void quit() {
    if (serialP50.isSolenoidActive()) {
      Logger.debug("Solenoid active switch off before quitting...");
      serialP50.sendSingleCommand(SWITCH_OFF);
    }
    commandQueue.clear();
    pause(200);
    running = false;
    Logger.debug("Done...");
  }
}
