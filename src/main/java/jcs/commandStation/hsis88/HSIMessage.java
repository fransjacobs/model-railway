/*
 * Copyright 2024 fransjacobs.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class HSIMessage {

  private final byte[] rx;

  private final int length;
  private final String responseIdentifier;
  private int numberOfModules;
  private final List<S88Module> modules;
  private String info;

  HSIMessage(final byte[] message) {
    rx = message;
    modules = new ArrayList<>(numberOfModules);
    length = message.length;
    responseIdentifier = Character.toString(message[0]);

    if ("i".equals(responseIdentifier) | "m".equals(responseIdentifier)) {
      numberOfModules = message[1];
      int checkLen = numberOfModules * 3 + 3;

      if (length == checkLen) {
        int offset = 3;
        for (int i = 2; i < message.length - 1; i = i + offset) {
          int mn = message[i];
          int hb = message[i + 1] & 0xff;
          int lb = message[i + 2] & 0xff;
          S88Module mod = new S88Module(mn, hb, lb);
          modules.add(mod);
        }
      } else {
        Logger.trace("Length diff may be mode is T");
      }
    } else {
      this.info = new String(rx);
    }
  }

  public class S88Module {
    private final int moduleNumber;
    private final int highByte;
    private final int lowByte;

    S88Module(int moduleNumber, int highByte, int lowByte) {
      this.moduleNumber = moduleNumber;
      this.highByte = highByte;
      this.lowByte = lowByte;
    }

    public int getModuleNumber() {
      return moduleNumber;
    }

    public int getHighByte() {
      return highByte;
    }

    public int getLowByte() {
      return lowByte;
    }

    //LB = 1  port 1 lb & 0x01 -> 1 lb & 0x02 -> 2 lb & 0x04 -> 3
    //LB = 2  port 2
    //LB = 4  port 3
    // ..  8  port 4
    // ..  16 port 5
    // ..  32 port 6
    // ..  64 port 7
    // .. 128 port 8
    //dus 3: port 1 en 2 hoog
    ///omzetten in sinlge events zoals CS 3 dus module 1 1 - 16
    //module 2 = 17 - 32 etc
    @Override
    public String toString() {
      return "#" + moduleNumber + ": [" + highByte + ", " + lowByte + "]";
    }
  }

  public byte[] getMessage() {
    return this.rx;
  }

  public int getLength() {
    return length;
  }

  public String getResponseIdentifier() {
    return responseIdentifier;
  }

  public int getNumberOfModules() {
    return numberOfModules;
  }

  public String getInfo() {
    return info;
  }

  public List<S88Module> getModules() {
    return modules;
  }

  @Override
  public String toString() {
    if (this.info != null) {
      return info;
    } else {
      return "HSI msg {" + "length=" + length + ", responseIdentifier=" + responseIdentifier + ", numberOfModules=" + numberOfModules + ", modules=" + modules + "}";
    }
  }
}
