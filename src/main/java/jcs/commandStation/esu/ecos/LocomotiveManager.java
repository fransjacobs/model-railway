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
import java.util.Map;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.LocomotiveBean.Direction;
import org.tinylog.Logger;

/**
 *
 * LocomotiveManager (id=10)
 */
class LocomotiveManager implements LocomotiveSpeedEventListener, LocomotiveDirectionEventListener, LocomotiveFunctionEventListener {

  public static final int ID = 10;
  public static final int LOCO_OFFSET = 1000;

  private int size;
  private final EsuEcosCommandStationImpl ecosCommandStation;

  private final Map<Long, LocomotiveBean> locomotives;

  LocomotiveManager(EsuEcosCommandStationImpl esuEcosCommandStationImpl, EcosMessage message) {
    this.ecosCommandStation = esuEcosCommandStationImpl;
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

  void update(EcosMessage message) {
    parse(message);
  }

  int getSize() {
    return this.size;
  }

  Map<Long, LocomotiveBean> getLocomotives() {
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
      if (ecosCommandStation != null) {
        locomotive.setCommandStationBean(ecosCommandStation.getCommandStationBean());
      } else {
        locomotive.setCommandStationId(EcosMessage.ECOS_COMMANDSTATION_ID);
      }
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
        if (ecosCommandStation != null) {
          LocomotiveDirectionEvent directionEvent = new LocomotiveDirectionEvent(locomotive);
          ecosCommandStation.fireDirectionEventListeners(directionEvent);
        }
      }
    }

    if (values.containsKey(Ecos.SPEED)) {
      int velocity = Integer.parseInt(values.get(Ecos.SPEED).toString());
      //Scale the speed 0 == 0 1024 is max Ecos max = 128 so time 8
      velocity = velocity * 8;
      locomotive.setVelocity(velocity);

      if (event) {
        if (ecosCommandStation != null) {
          LocomotiveSpeedEvent speedEvent = new LocomotiveSpeedEvent(locomotive);
          ecosCommandStation.fireLocomotiveSpeedEventListeners(speedEvent);
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

      //http://192.168.1.110/loco/image?type=internal&index=0
      locomotive.setIcon(locodesc);
    }

    if (values.containsKey(Ecos.ACTIVE)) {
      boolean active = "1".equals(values.get(Ecos.ACTIVE).toString());
      locomotive.setActive(active);
    }

    if (values.containsKey(Ecos.FUNCTION)) {
      String func = values.get(Ecos.FUNCTION).toString();
      Map<Integer, FunctionBean> functions = parseFunctions(func, locomotive);

      //Need to obtain the changed functions....
      if (event) {
        if (this.ecosCommandStation != null) {
          for (FunctionBean chFb : functions.values()) {
            if (chFb.getLocomotiveId() == null) {
              chFb.setLocomotiveId(locomotive.getId());
            }
            LocomotiveFunctionEvent functionEvent = new LocomotiveFunctionEvent(chFb);
            ecosCommandStation.fireFunctionEventListeners(functionEvent);
          }
        }
      }
    }

    if (values.containsKey(Ecos.FUNCTION_DESC)) {
      String funcdesc = values.get(Ecos.FUNCTION_DESC).toString();
      Map<Integer, FunctionBean> functions = parseFunctionDetails(funcdesc, locomotive);
    }

    //Tachomax is needed for display so use the full scale for now
    locomotive.setTachoMax(126);

    return locomotive;
  }

  private Map<Integer, FunctionBean> parseFunctions(String func, LocomotiveBean locomotive) {
    Long locomotiveId = locomotive.getId();

    Map<Integer, FunctionBean> functions = locomotive.getFunctions();

    String[] fa = func.replace("[", "").split("],");

    for (String fs : fa) {
      String fu[] = fs.replace("]", "").split(",");
      int number = Integer.parseInt(fu[0]);
      int value = Integer.parseInt(fu[1]);

      FunctionBean fb;
      if (functions.containsKey(number)) {
        fb = functions.get(number);
      } else {
        fb = new FunctionBean(locomotiveId, number, value);
        fb.setLocomotiveId(locomotiveId);
      }

      //Put a default as it can't be null
      int functionType = 50 + number;
      fb.setFunctionType(functionType);
      functions.put(number, fb);
    }

    return functions;
  }

  private Map<Integer, FunctionBean> parseFunctionDetails(String funcdesc, LocomotiveBean locomotive) {
    Long locomotiveId = locomotive.getId();

    Map<Integer, FunctionBean> functions = locomotive.getFunctions();

    ////1001 funcdesc[1,7]1001 funcdesc[2,37,momentary]
    String[] fa = funcdesc.replace("[", "").split("],");

    for (String fs : fa) {
      String fu[] = fs.replace("]", "").split(",");
      int number = Integer.parseInt(fu[0]);
      int functionType = Integer.parseInt(fu[1]);
      boolean momentary = false;
      if (fu.length == 3) {
        String m = fu[2];
        momentary = "moment".equals(m);
      }

      FunctionBean fb;
      if (functions.containsKey(number)) {
        fb = functions.get(number);
      } else {
        fb = new FunctionBean(locomotiveId, number, 0);
        fb.setLocomotiveId(locomotiveId);
      }

      fb.setFunctionType(functionType);
      fb.setMomentary(momentary);
      fb.setIcon(functionType + "");
      functions.put(number, fb);
    }

    return functions;
  }

  @Override
  public void onSpeedChange(LocomotiveSpeedEvent velocityEvent) {
    if (this.locomotives.containsKey(velocityEvent.getId())) {
      LocomotiveBean lb = this.locomotives.get(velocityEvent.getId());
      lb.setVelocity(velocityEvent.getVelocity());
    }
  }

  @Override
  public void onDirectionChange(LocomotiveDirectionEvent directionEvent) {
    if (this.locomotives.containsKey(directionEvent.getId())) {
      LocomotiveBean lb = this.locomotives.get(directionEvent.getId());
      lb.setDirection(directionEvent.getNewDirection());
    }
  }

  @Override
  public void onFunctionChange(LocomotiveFunctionEvent locomotiveFunctionEvent) {
    if (this.locomotives.containsKey(locomotiveFunctionEvent.getLocomotiveId())) {
      LocomotiveBean lb = this.locomotives.get(locomotiveFunctionEvent.getLocomotiveId());
      FunctionBean fb = lb.getFunctionBean(locomotiveFunctionEvent.getNumber());
      if (fb != null) {
        fb.setValue((locomotiveFunctionEvent.isOn() ? 1 : 0));
      }
    }
  }

}
