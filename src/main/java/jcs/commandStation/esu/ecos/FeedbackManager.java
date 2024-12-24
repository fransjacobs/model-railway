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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.SensorBean;
import org.tinylog.Logger;

/**
 *
 * Feedback-Manager (id=26)
 */
class FeedbackManager {

  public static final int ID = 26;
  public static final int S88_OFFSET = 100;

  private int size;

  private final EsuEcosCommandStationImpl ecosCommandStation;
  private final Map<Integer, S88> modules;

  FeedbackManager(EsuEcosCommandStationImpl ecosCommandStation, EcosMessage message) {
    this.ecosCommandStation = ecosCommandStation;
    modules = new HashMap<>();
    parse(message);
  }

  private List<SensorEvent> parse(EcosMessage message) {
    Logger.trace(message.getMessage());
    Logger.trace(message.getResponse());

    List<SensorEvent> changedSensors = null;
    boolean event = message.isEvent();
    Map<String, Object> values = message.getValueMap();
    int objectId = message.getObjectId();
    if (ID == objectId) {
      if (values.containsKey(Ecos.SIZE)) {
        String vsize = values.get(Ecos.SIZE).toString();
        if (vsize != null) {
          this.size = Integer.parseInt(vsize);
        }
      }
      changedSensors = Collections.EMPTY_LIST;
    } else {
      int ports = 0;
      if (values.containsKey(Ecos.PORTS)) {
        String vports = values.get(Ecos.PORTS).toString();
        if (vports != null) {
          ports = Integer.parseInt(vports);
        }
      }
      if (values.containsKey(Ecos.STATE)) {
        String state = values.get(Ecos.STATE).toString();

        S88 s88;
        if (this.modules.containsKey(objectId)) {
          s88 = this.modules.get(objectId);
        } else {
          s88 = new S88(objectId, ports, state);
          this.modules.put(objectId, s88);
        }
        changedSensors = s88.updateState(state);

        if (event) {
          if (this.ecosCommandStation != null) {
            for (SensorEvent sensorEvent : changedSensors) {
              this.ecosCommandStation.fireSensorEventListeners(sensorEvent);
            }
          }
        }
      }
    }
    if (changedSensors == null) {
      changedSensors = Collections.EMPTY_LIST;
    }
    return changedSensors;
  }

  List<SensorEvent> update(EcosMessage message) {
    List<SensorEvent> changedSensors = parse(message);
    return changedSensors;
  }

  public int getSize() {
    return this.size;
  }

  public Map<Integer, S88> getModules() {
    return modules;
  }

  public S88 getS88(int id) {
    return this.modules.get(id);
  }

  public class S88 {

    private String state;
    private String prevState;
    private final int id;
    private final int[] ports;
    private final int[] prevPorts;

    S88(int id, int ports, String state) {
      this.id = id;
      this.state = state;
      if (ports == 0) {
        //use default 16
        this.ports = new int[16];
        this.prevPorts = new int[16];
      } else {
        this.ports = new int[ports];
        this.prevPorts = new int[ports];
      }
      updateState(state);
    }

    public int getId() {
      return id;
    }

    final List<SensorEvent> updateState(String state) {
      this.prevState = this.state;
      this.state = state;
      String val = state.replace("0x", "");
      int l = 4 - val.length();
      for (int i = 0; i < l; i++) {
        val = "0" + val;
      }

      //Set the previous ports State
      System.arraycopy(ports, 0, this.prevPorts, 0, ports.length);

      int stateVal = Integer.parseInt(val, 16);
      //Logger.trace(state + " -> " + stateVal);
      for (int i = 0; i < ports.length; i++) {
        int m = ((int) Math.pow(2, i));
        int pv = (stateVal & m) > 0 ? 1 : 0;
        ports[i] = pv;
      }

      //String p = "16[" + ports[15] + "] 15[" + ports[14] + "] 14[" + ports[13] + "] 13[" + ports[12] + "] 12[" + ports[11] + "] 11[" + ports[10] + "] 10[" + ports[9] + "] 9[" + ports[8]
      //        + "] 8[" + ports[7] + "] 7[" + ports[6] + "] 6[" + ports[5] + "] 5[" + ports[4] + "] 4[" + ports[3] + "] 3[" + ports[2] + "] 2[" + ports[1] + "] 1[" + ports[0] + "]";
      //Logger.trace(p);
      return getChangedSensors();
    }

    //0 based index
    boolean isPort(int port) {
      return ports[port] == 1;
    }

    //0 based index
    SensorBean getSensor(int port) {
      SensorBean sb = new SensorBean(id, port, ports[port]);
      return sb;
    }

    List<SensorEvent> getChangedSensors() {
      List<SensorEvent> changedSensors = new ArrayList<>(ports.length);

      for (int i = 0; i < ports.length; i++) {
        if (ports[i] != prevPorts[i]) {
          SensorBean sb = new SensorBean(id, i, ports[i]);
          SensorEvent se = new SensorEvent(sb);
          changedSensors.add(se);
        }
      }
      return changedSensors;
    }

  }

}
