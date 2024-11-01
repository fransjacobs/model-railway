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
package jcs.commandStation.esu.ecos.entities;

import java.util.HashMap;
import java.util.Map;
import jcs.commandStation.esu.ecos.Ecos;
import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.entities.SensorBean;
import org.tinylog.Logger;

/**
 *
 * Feedback-Manager (id=26)
 */
public class FeedbackManager {

  public static final int ID = 26;
  public static final int S88_OFFSET = 100;

  private int size;

  private final Map<Integer, S88> modules;

  public FeedbackManager(EcosMessage message) {
    modules = new HashMap<>();
    parse(message);
  }

  private void parse(EcosMessage message) {
    //Logger.trace("S: " + message.getMessage() + " R: " + message.getResponse());
    Map<String, String> values = message.getValueMap();
    int objectId = message.getObjectId();
    if (ID == objectId) {
      if (values.containsKey(Ecos.SIZE)) {
        String vsize = values.get(Ecos.SIZE);
        if (vsize != null) {
          this.size = Integer.parseInt(vsize);
        }
      }
    } else {
      int ports = 0;
      String state = null;
      if (values.containsKey(Ecos.PORTS)) {
        String vports = values.get(Ecos.PORTS);
        if (vports != null) {
          ports = Integer.parseInt(vports);
        }
      }
      if (values.containsKey(Ecos.STATE)) {
        state = values.get(Ecos.STATE);
      }

      if (this.modules.containsKey(objectId)) {
        this.modules.get(objectId).updateState(state);
      } else {
        S88 s88 = new S88(objectId, ports, state);
        this.modules.put(objectId, s88);
      }
    }
  }

  public void update(EcosMessage message) {
    parse(message);
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

    private final int id;
    private final int[] ports;

    S88(int id, int ports, String state) {
      this.id = id;
      if (ports == 0) {
        //use default 16
        this.ports = new int[16];
      } else {
        this.ports = new int[ports];
      }
      updateState(state);
    }

    public int getId() {
      return id;
    }

    public final void updateState(String state) {
      String val = state.replace("0x", "");
      int l = 4 - val.length();
      for (int i = 0; i < l; i++) {
        val = "0" + val;
      }

      String lb = val.substring(2);
      String hb = val.substring(0, 2);
      int low = Integer.parseInt(lb, 16);
      int high = Integer.parseInt(hb, 16);

      for (int i = 0; i < 8; i++) {
        int m = ((int) Math.pow(2, i));
        int lv = (low & m) > 0 ? 1 : 0;
        int hv = (high & m) > 0 ? 1 : 0;
        ports[i] = lv;
        ports[(i + 8)] = hv;
      }

      Logger.trace(state + " -> " + Integer.parseInt(val, 16));

      String p = "16[" + ports[15] + "] 15[" + ports[14] + "] 14[" + ports[13] + "] 13[" + ports[12] + "] 12[" + ports[11] + "] 11[" + ports[10] + "] 10[" + ports[9] + "] 9[" + ports[8]
              + "] 8[" + ports[7] + "] 7[" + ports[6] + "] 6[" + ports[5] + "] 5[" + ports[4] + "] 4[" + ports[3] + "] 3[" + ports[2] + "] 2[ " + ports[1] + "] 1[" + ports[0] + "]";
      Logger.trace(p);
    }

    //0 based index
    public boolean isPort(int port) {
      return ports[port] == 1;
    }

    //0 based index
    public SensorBean getSensor(int port) {
      SensorBean sb = new SensorBean(id, port, ports[port]);
      return sb;
    }

  }

}
