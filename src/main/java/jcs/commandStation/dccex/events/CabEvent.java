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
import java.util.ArrayList;
import java.util.List;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.LocomotiveBean.Direction;
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

  private Direction direction;
  private Integer velocity;

  public CabEvent(String messageContent) {
    parseMessage(messageContent);
  }

  private void parseMessage(String message) {
    String[] response = message.split(" ");
    for (int i = 0; i < response.length; i++) {
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

    //Translate the message int velocity, direction and functions
    if (speedDir >= 128) {
      this.direction = Direction.FORWARDS;

      velocity = speedDir - 128;
      if (velocity > 0) {
        velocity = velocity - 1;
        velocity = velocity * 8;
      }
    } else {
      this.direction = Direction.BACKWARDS;
      velocity = speedDir;
      if (velocity > 0) {
        velocity = velocity - 1;
        velocity = velocity * 8;
      }
    }

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

  public Direction getDirection() {
    return direction;
  }

  public Integer getVelocity() {
    return velocity;
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
      if (f == '1') {
        Logger.trace("F" + i + " " + f);
      }
      FunctionBean fb = new FunctionBean((long) address, i, (f == '1' ? 1 : 0));
      fbl.add(fb);
    }
    return fbl;
  }

  public LocomotiveBean getLocomotiveBean() {
    LocomotiveBean lb = new LocomotiveBean();
    lb.setId((long) this.address);
    lb.setAddress(this.address);
    lb.setDecoderTypeString(DecoderType.DCC.getDecoderType());
    lb.setDirection(this.direction);
    lb.setVelocity(velocity);
    lb.addAllFunctions(getFunctionBeans());
    return lb;
  }

}
