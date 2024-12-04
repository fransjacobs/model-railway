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
package jcs.commandStation.esu.ecos.entities;

import java.util.HashMap;
import java.util.Map;
import jcs.commandStation.esu.ecos.Ecos;
import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.LocomotiveBean.Direction;
import org.tinylog.Logger;

/**
 *
 * LokManager (id=10)
 */
public class LokManager {

  public static final int ID = 10;
  public static final int LOCO_OFFSET = 1000;

  private int size;

  //private final EsuEcosCommandStationImpl esuEcosCommandStationImpl;
  private final Map<Long, LocomotiveBean> locomotives;

  public LokManager(EcosMessage message) {
    locomotives = new HashMap<>();
    parse(message);
  }

  private void parse(EcosMessage message) {
    Logger.trace(message.getMessage());
    Logger.trace(message.getResponse());
    
    Map<String, Object> values = message.getValueMap();
    int objectId = message.getObjectId();
    if (ID == objectId) {
      if (values.containsKey(Ecos.SIZE)) {
        String vsize = values.get(Ecos.SIZE).toString();
        if (vsize != null) {
          this.size = Integer.parseInt(vsize);
        }
      } else {
        if (values.size() > 1) {
          if (values.containsKey(Ecos.ID)) {
            this.size = values.size() - 1;
          } else {
            this.size = values.size();
          }
        }
      }

      //parse all loco's
      for (Object o : values.values()) {
        if (o instanceof Map) {
          Map<String, String> valueValueMap = (Map<String, String>) o;
          //There MUST be an id!
          long id = Long.parseLong(valueValueMap.get(Ecos.ID));
          LocomotiveBean loco;
          if (locomotives.containsKey(id)) {
            loco = this.locomotives.get(id);
          } else {
            loco = new LocomotiveBean();
            loco.setId(id);
          }

          String name = valueValueMap.get(Ecos.NAME);
          loco.setName(name);

          if (valueValueMap.containsKey(Ecos.ADDRESS)) {
            int addr = Integer.parseInt(valueValueMap.get(Ecos.ADDRESS));
            loco.setAddress(addr);
          }

          if (valueValueMap.containsKey(Ecos.PROTOCOL)) {
            DecoderType dt = DecoderType.get(valueValueMap.get(Ecos.PROTOCOL));
            loco.setDecoderTypeString(dt.getDecoderType());
          }

          if (valueValueMap.containsKey(Ecos.PROTOCOL)) {
            DecoderType dt = DecoderType.get(valueValueMap.get(Ecos.PROTOCOL));
            loco.setDecoderTypeString(dt.getDecoderType());
          }
                    
          //1005 name["DB-141-015-8"]1005 addr[12]1005 protocol[MM14]1005 
          //dir[1]1005
          //speed[0]1005
          //speedstep[0]1005
          //active[0]1005
          //locodesc[LOCO_TYPE_E,IMAGE_TYPE_INT,37]
          //1005 func[0,0]1005 func[3,0]1005 func[4,0]          

          if (valueValueMap.containsKey(Ecos.DIRECTION)) {
            Direction d = Direction.getDirectionEcos(valueValueMap.get(Ecos.DIRECTION));
            loco.setDirection(d);
          }

          if (valueValueMap.containsKey(Ecos.SPEED)) {
            int velocity = Integer.parseInt(valueValueMap.get(Ecos.SPEED));
            //Scale the speed 0 == 0 1024 is max Ecos max = 128 so time 8
            velocity = velocity * 8;
            loco.setVelocity(velocity);
          }
          
          if (valueValueMap.containsKey(Ecos.SPEEDSTEP)) {
            int speedstep = Integer.parseInt(valueValueMap.get(Ecos.SPEEDSTEP));
            //TODO? loco.set setVelocity(velocity);
          }

          if (valueValueMap.containsKey(Ecos.LOCODESC)) {
            String locodesc = valueValueMap.get(Ecos.LOCODESC);
            //TODO: pars the locodesc for the image;
          }

          if (valueValueMap.containsKey(Ecos.ACTIVE)) {
            boolean active = "1".equals(valueValueMap.get(Ecos.ACTIVE));
            //TODO: do we nee to know whether the loc is used in the ecos itself?
          }

          if (valueValueMap.containsKey(Ecos.FUNCTION)) {
            String func = valueValueMap.get(Ecos.FUNCTION);
            //TODO: do we nee to know whether the loc is used in the ecos itself?
          }


          
          this.locomotives.put(id, loco);
        }

      }

    }

  }

  public void update(EcosMessage message) {
    parse(message);

    //TODO: speed, direction and function listeners listeners
  }

  public int getSize() {
    return this.size;
  }

  public Map<Long, LocomotiveBean> getLocomotives() {
    return locomotives;
  }

}
