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
package jcs.commandStation.esu.ecos;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.entities.FeedbackModule;
import jcs.entities.SensorBean;
import org.tinylog.Logger;

/**
 *
 * Feedback-Manager (id=26)
 */
class FeedbackManager {

  public static final int ID = Ecos.FEEDBACK_MANAGER_ID;
  public static final int S88_OFFSET = 100;
  public static final int S88_DEFAULT_PORT_COUNT = 16;

  private static final String ESU_ECOS_CS = "esu-ecos";

  private final EsuEcosCommandStationImpl ecosCommandStation;
  private final Map<Integer, FeedbackModule> modules;
  private final Map<Integer, SensorBean> sensors;

  FeedbackManager(EsuEcosCommandStationImpl ecosCommandStation, EcosMessage message) {
    this.ecosCommandStation = ecosCommandStation;
    modules = new HashMap<>();
    sensors = new HashMap<>();
    parse(message);
  }

  private List<SensorEvent> parse(EcosMessage message) {
    Logger.trace(message.getResponse());

    List<SensorEvent> changedSensors;
    boolean event = message.isEvent();
    Map<String, Object> values = message.getValueMap();
    int objectId = message.getObjectId();

    if (ID != objectId) {
      FeedbackModule feedbackModule;
      if (modules.containsKey(objectId)) {
        feedbackModule = modules.get(objectId);
      } else {
        feedbackModule = new FeedbackModule();
        feedbackModule.setId(objectId);
        feedbackModule.setAddressOffset(0);
        feedbackModule.setModuleNumber(objectId - S88_OFFSET + 1);
        //ESU ECoS has 1 bus
        feedbackModule.setIdentifier(0);
        //In Unit Testcase the command station is null
        if (ecosCommandStation != null) {
          feedbackModule.setCommandStationId(ecosCommandStation.getCommandStationBean().getId());
        } else {
          feedbackModule.setCommandStationId(ESU_ECOS_CS);
        }
      }

      if (values.containsKey(Ecos.PORTS)) {
        String vports = values.get(Ecos.PORTS).toString();
        if (vports != null) {
          int ports = Integer.parseInt(vports);
          feedbackModule.setPortCount(ports);
        }
      } else {
        feedbackModule.setPortCount(S88_DEFAULT_PORT_COUNT);
      }

      if (values.containsKey(Ecos.STATE)) {
        String state = values.get(Ecos.STATE).toString();
        updatePorts(state, feedbackModule);
      }
      modules.put(objectId, feedbackModule);

      List<SensorBean> moduleSensors = feedbackModule.getSensors();
      for (SensorBean sb : moduleSensors) {
        if (sensors.containsKey(sb.getId())) {
          SensorBean sensor = sensors.get(sb.getId());
          sensor.setActive(sb.isActive());
          sensor.setLastUpdated(new Date());
        } else {
          sensors.put(sb.getId(), sb);
        }
      }

      changedSensors = feedbackModule.getChangedSensorEvents();

      if (event) {
        if (ecosCommandStation != null) {
          for (SensorEvent sensorEvent : changedSensors) {
            ecosCommandStation.fireAllSensorEventsListeners(sensorEvent);
          }
        }
      }
    } else {
      if (Ecos.FEEDBACK_MANAGER_ID == objectId) {
        if (values.containsKey(Ecos.SIZE)) {
          int size = Integer.parseInt(values.get(Ecos.SIZE).toString());
          for (int i = 0; i < size; i++) {
            FeedbackModule fbmb = new FeedbackModule();
            fbmb.setAddressOffset(0);
            fbmb.setModuleNumber(i + 1);
            fbmb.setId(S88_OFFSET + i);
            fbmb.setPortCount(S88_DEFAULT_PORT_COUNT);
            fbmb.setIdentifier(0);
            fbmb.setBusSize(size);

            //In Unit Testcase the command station is null
            if (ecosCommandStation != null) {
              fbmb.setCommandStationId(ecosCommandStation.getCommandStationBean().getId());
            } else {
              fbmb.setCommandStationId(ESU_ECOS_CS);
            }

            modules.put(fbmb.getId(), fbmb);
          }
        }
      }
      changedSensors = Collections.EMPTY_LIST;
    }
    return changedSensors;
  }

  List<SensorEvent> update(EcosMessage message) {
    List<SensorEvent> changedSensors = parse(message);
    return changedSensors;
  }

  public int getSize() {
    return this.modules.size();
  }

  void updatePorts(String state, FeedbackModule s88) {
    String val = state.replace("0x", "");
    int l = 4 - val.length();
    for (int i = 0; i < l; i++) {
      val = "0" + val;
    }

    int[] ports = s88.getPorts();
    int[] prevPorts = s88.getPrevPorts();

    if (ports == null) {
      ports = new int[FeedbackModule.DEFAULT_PORT_COUNT];
      prevPorts = new int[FeedbackModule.DEFAULT_PORT_COUNT];
    }
    //Set the previous ports State
    System.arraycopy(ports, 0, prevPorts, 0, ports.length);
    s88.setPrevPorts(prevPorts);

    int stateVal = Integer.parseInt(val, 16);
    //Logger.trace(state + " -> " + stateVal);
    for (int i = 0; i < ports.length; i++) {
      int m = ((int) Math.pow(2, i));
      int pv = (stateVal & m) > 0 ? 1 : 0;
      ports[i] = pv;
    }
    s88.setPorts(ports);
  }

  public Map<Integer, FeedbackModule> getModules() {
    return modules;
  }

  public FeedbackModule getFeedbackModule(int id) {
    return modules.get(id);
  }

  SensorBean getSensor(Integer id) {
    return sensors.get(id);
  }

}
