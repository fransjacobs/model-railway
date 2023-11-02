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
package jcs.controller.dccex.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jcs.entities.FunctionBean;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class CabEvent implements Serializable {

  private int address = 0;
  private int reg = 0;
  private int speedDir = 0;
  private int funcMap = 0;

  public CabEvent(String messageContent) {
    parseMessage(messageContent);
  }

  private void parseMessage(String message) {
    String[] response = message.split(" ");
    //8 0 128 1
    for (int i = 0; i < response.length; i++) {
      Logger.trace("i: " + i + " val: " + response[i]);
      switch (i) {
        case 0 -> {
          if (response[i] != null) {
            address = Integer.parseUnsignedInt(response[i]);
          }
        }
        case 1 -> {
          if (response[i] != null) {
            reg = Integer.parseUnsignedInt(response[i]);
          }
        }
        case 2 -> {
          if (response[i] != null) {
            speedDir = Integer.parseUnsignedInt(response[i]);
          }
        }
        case 3 -> {
          if (response[i] != null) {
            funcMap = Integer.parseUnsignedInt(response[i]);
          }
        }
      }
    }
    Logger.trace("Address: " + address + " reg: " + reg + " speedDir: " + speedDir + " funcMap: " + funcMap);

  }

  public int getAddress() {
    return address;
  }

  public int getReg() {
    return reg;
  }

  public int getSpeedDir() {
    return speedDir;
  }

  public int getFuncMap() {
    return funcMap;
  }

  public List<FunctionBean> getFunctionBeans() {
    List<FunctionBean> fbl = new ArrayList<>(28);
    String fms = Integer.toBinaryString(funcMap);

    int prefixCount = 28 - fms.length();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < prefixCount; i++) {
      sb.append("0");
    }
    sb.append(fms);
    sb.reverse();
    fms = sb.toString();
    char[] fma = fms.toCharArray();

    for (int i = 0; i < fma.length; i++) {
      char f = fma[i];
      Logger.trace("F" + i + " " + f);
      FunctionBean fb = new FunctionBean((long) address, i, (f == '1' ? 1 : 0));
      fbl.add(fb);
    }
    return fbl;
  }

}
