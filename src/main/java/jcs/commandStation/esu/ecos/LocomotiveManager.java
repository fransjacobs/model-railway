/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.esu.ecos;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jcs.commandStation.esu.ecos.Ecos;
import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.LocomotiveBean.Direction;
import org.tinylog.Logger;

/**
 *
 * LocomotiveManager (id=10)
 */
public class LocomotiveManager {

  public static final int ID = 10;
  public static final int LOCO_OFFSET = 1000;

  private int size;
  private final EsuEcosCommandStationImpl esuEcosCommandStationImpl;

  //private final EsuEcosCommandStationImpl esuEcosCommandStationImpl;
  private final Map<Long, LocomotiveBean> locomotives;

  public LocomotiveManager(EsuEcosCommandStationImpl esuEcosCommandStationImpl, EcosMessage message) {
    this.esuEcosCommandStationImpl = esuEcosCommandStationImpl;
    locomotives = new HashMap<>();
    parse(message);
  }

  private void parse(EcosMessage message) {
    //Logger.trace(message.getMessage());
    //Logger.trace(message.getResponse());

    boolean event = message.isEvent();

    Map<String, Object> values = message.getValueMap();
    int objectId = message.getObjectId();
    if (ID == objectId) {
      //A locomotive list?
      for (Object o : values.values()) {
        LocomotiveBean loco;
        if (o instanceof Map) {
          Map<String, Object> vm = (Map<String, Object>) o;
          loco = parseValues(vm, event);
        } else {
          //Details
          loco = parseValues(values, event);
        }
        this.locomotives.put(loco.getId(), loco);
      }

      if (values.containsKey(Ecos.SIZE)) {
        this.size = Integer.parseInt(values.get(Ecos.SIZE).toString());
      } else {
        this.size = values.size();
      }
    } else if (values.containsKey(Ecos.ID) && objectId == Integer.parseInt((String) values.get(Ecos.ID))) {
      //Single locomotive
      LocomotiveBean loco = parseValues(values, event);
      this.locomotives.put(loco.getId(), loco);
    } else {
      Logger.warn("Unknown id: " + message.getMessage());
      Logger.warn("Unknown response: " + message.getResponse());
    }
  }

  public void update(EcosMessage message) {
    parse(message);
  }

  public int getSize() {
    return this.size;
  }

  public Map<Long, LocomotiveBean> getLocomotives() {
    return locomotives;
  }

  private LocomotiveBean parseValues(Map<String, Object> values, boolean event) {
    long id = Long.parseLong(values.get(Ecos.ID).toString());
    LocomotiveBean locomotive;
    if (locomotives.containsKey(id)) {
      locomotive = this.locomotives.get(id);
    } else {
      locomotive = new LocomotiveBean();
      locomotive.setId(id);
    }
    if (values.containsKey(Ecos.NAME)) {
      String name = values.get(Ecos.NAME).toString();
      locomotive.setName(name);
    }

    if (values.containsKey(Ecos.ADDRESS)) {
      int addr = Integer.parseInt(values.get(Ecos.ADDRESS).toString());
      locomotive.setAddress(addr);
    }

    if (values.containsKey(Ecos.PROTOCOL)) {
      DecoderType dt = DecoderType.get(values.get(Ecos.PROTOCOL).toString());
      locomotive.setDecoderTypeString(dt.getDecoderType());
    }

    if (values.containsKey(Ecos.PROTOCOL)) {
      DecoderType dt = DecoderType.get(values.get(Ecos.PROTOCOL).toString());
      locomotive.setDecoderTypeString(dt.getDecoderType());
    }

    if (values.containsKey(Ecos.DIRECTION)) {
      Direction d = Direction.getDirectionEcos(values.get(Ecos.DIRECTION).toString());
      locomotive.setDirection(d);

      if (event) {
        if(this.esuEcosCommandStationImpl != null) {
          
        }
      }
    }

    if (values.containsKey(Ecos.SPEED)) {
      int velocity = Integer.parseInt(values.get(Ecos.SPEED).toString());
      //Scale the speed 0 == 0 1024 is max Ecos max = 128 so time 8
      velocity = velocity * 8;
      locomotive.setVelocity(velocity);

      if (event) {
        //Todo raise speed event
        if(this.esuEcosCommandStationImpl != null) {
          
        }
        
      }
    }

    if (values.containsKey(Ecos.SPEEDSTEP)) {
      int speedstep = Integer.parseInt(values.get(Ecos.SPEEDSTEP).toString());
      //TODO?
    }

    if (values.containsKey(Ecos.LOCODESC)) {
      String locodesc = values.get(Ecos.LOCODESC).toString();
      //TODO: parse the locodesc for the image;
      //locodesc[LOCO_TYPE_E,IMAGE_TYPE_USER,2]
      locomotive.setIcon(locodesc);
    }

    if (values.containsKey(Ecos.ACTIVE)) {
      boolean active = "1".equals(values.get(Ecos.ACTIVE).toString());
      locomotive.setActive(active);
    }

    if (values.containsKey(Ecos.FUNCTION)) {
      String func = values.get(Ecos.FUNCTION).toString();
      List<FunctionBean> functions = parseFunctions(func);
      locomotive.setFunctions(functions);

      if (event) {
        if(this.esuEcosCommandStationImpl != null) {
          
        }
      }

    }

    //Tachomax is needed for display so use the full scale for now
    locomotive.setTachoMax(128);

    return locomotive;
  }

  private List<FunctionBean> parseFunctions(String func) {
    List<FunctionBean> functions = new LinkedList<>();
    String[] fa = func.replace("[", "").split("],");

    for (String fs : fa) {
      String fu[] = fs.replace("]", "").split(",");
      int number = Integer.parseInt(fu[0]);
      int value = Integer.parseInt(fu[1]);
      //TODO: The function type a used for Function iamages from Marklin Check the function index number from ECoS
      int functionType = 50 + number;

      FunctionBean fb = new FunctionBean(null, number, value);
      fb.setFunctionType(functionType);
      functions.add(fb);
    }

    return functions;
  }

}
