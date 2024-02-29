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
package jcs.commandStation.dccex.events;

import java.io.Serializable;
import jcs.util.KeyValuePair;

/**
 *
 * @author Frans Jacobs
 */
public class DccExMeasurementEvent implements Serializable {

  private KeyValuePair track;
  private int currentMain;
  private int currentProg;
  private int currentMainMax;
  private int currentProgMax;

  private boolean measurement;

  private String opcode;
  private String messageContent;

  public DccExMeasurementEvent(String message) {
      this(message.substring(1, 2), message.replaceAll("<", "").replaceAll(">", ""));
  }

  public DccExMeasurementEvent(String opcode, String messageContent) {
    this.opcode = opcode;
    this.messageContent = messageContent;
    parseMessage(opcode, messageContent);
  }

  private void parseMessage(String opcode, String message) {
    String[] response = message.split(" ");

    switch (opcode) {
      case "=" -> {
        track = new KeyValuePair(response);
        measurement = false;
      }
      case "j" -> {
        if ("I".equals(response[0])) {
          currentMain = Integer.parseInt(response[1]);
          currentProg = Integer.parseInt(response[2]);
          measurement = true;
        } else if ("G".equals(response[0])) {
          currentMainMax = Integer.parseInt(response[1]);
          currentProgMax = Integer.parseInt(response[2]);
          measurement = false;
        }
      }
    }
  }

  public String getOpcode() {
    return opcode;
  }

  public String getMessageContent() {
    return messageContent;
  }

  public KeyValuePair getTrack() {
    return track;
  }

  public int getCurrentMain() {
    return currentMain;
  }

  public int getCurrentProg() {
    return currentProg;
  }

  public int getCurrentMainMax() {
    return currentMainMax;
  }

  public int getCurrentProgMax() {
    return currentProgMax;
  }

  public boolean isMeasurement() {
    return measurement;
  }

}
