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
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean.Protocol;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 *
 * AccessoryManager (id=11)
 */
class AccessoryManager {

  public static final int ID = 11;
  public static final int ACCESSORY_OFFSET = 20000;

  private int size;
  private final EsuEcosCommandStationImpl ecosCommandStation;
  private final Map<String, AccessoryBean> accessories;

  AccessoryManager(EsuEcosCommandStationImpl ecosCommandStation, EcosMessage message) {
    this.ecosCommandStation = ecosCommandStation;
    accessories = new HashMap<>();

    parse(message);
  }

  private void parse(EcosMessage message) {
    Logger.trace(message.getMessage());
    Logger.trace(message.getResponse());

    boolean event = message.isEvent();

    Map<String, Object> values = message.getValueMap();
    int objectId = message.getObjectId();
    if (ID == objectId) {
      //An Accessory list
      for (Object o : values.values()) {
        AccessoryBean accessory;
        if (o instanceof Map) {
          Map<String, Object> vm = (Map<String, Object>) o;
          accessory = parseValues(vm, event);
        } else {
          //Details
          accessory = parseValues(values, event);
        }
        this.accessories.put(accessory.getId(), accessory);
      }

      if (values.containsKey(Ecos.SIZE)) {
        this.size = Integer.parseInt(values.get(Ecos.SIZE).toString());
      } else {
        this.size = values.size();
      }

    } else if (objectId >= 20000 && objectId < 29999 && values.containsKey(ID)) {
      //Details
      AccessoryBean accessory = parseValues(values, event);
      this.accessories.put(accessory.getId(), accessory);

    } else {
      Logger.warn("Unkown object Id:" + objectId);
    }
  }

  private AccessoryBean parseValues(Map<String, Object> values, boolean event) {
    boolean hasKey = values.containsKey(Ecos.ID);
    String id = values.get(Ecos.ID).toString();
    AccessoryBean accessory;
    if (accessories.containsKey(id)) {
      accessory = this.accessories.get(id);
    } else {
      accessory = new AccessoryBean();
      accessory.setId(id);
      if (ecosCommandStation != null) {
        accessory.setCommandStationId(ecosCommandStation.getCommandStationBean().getId());
      } else {
        accessory.setCommandStationId(EcosMessage.ECOS_COMMANDSTATION_ID);
      }
    }
    if (values.containsKey(Ecos.NAME1)) {
      String name1 = values.get(Ecos.NAME1).toString();
      accessory.setName(name1);
    }

//// private String id;
////  private Integer address;
////  private String name;
//  private String type;
//  private Integer state;
//  private Integer switchTime;
//  private String decType;
//  private String decoder;
//  private Integer states;
//  private String group;
//  private String icon;
//  private String iconFile;
//  private String source;
//  private String commandStationId;
//  private boolean synchronize;    
    if (values.containsKey(Ecos.NAME2)) {
      String name2 = values.get(Ecos.NAME2).toString();
      //accessory.setName(name);
    }

    if (values.containsKey(Ecos.NAME3)) {
      String name3 = values.get(Ecos.NAME3).toString();
      //accessory.setName(name);
    }

    if (values.containsKey(Ecos.ADDRESS)) {
      String addr = values.get(Ecos.ADDRESS).toString();
      int address = Integer.parseInt(addr);
      accessory.setAddress(address);
    }

    if (values.containsKey(Ecos.PROTOCOL)) {
      String protocol = values.get(Ecos.PROTOCOL).toString();
      if (null == protocol) {
        Logger.warn("Unknown protocol " + protocol);
      } else {
        switch (protocol) {
          case "MOT" ->
            accessory.setProtocol(AccessoryBean.Protocol.MM);
          case "DCC" ->
            accessory.setProtocol(AccessoryBean.Protocol.DCC);
          default ->
            Logger.warn("Unknown protocol " + protocol);
        }
      }
    }

    if (values.containsKey(Ecos.MODE)) {
      String mode = values.get(Ecos.MODE).toString();
      //accessory.setName(name);
    }

    if (values.containsKey(Ecos.SYMBOL)) {
      String symbol = values.get(Ecos.SYMBOL).toString();
      accessory.setIcon(symbol);
    }

    if (values.containsKey(Ecos.STATE)) {
      String state = values.get(Ecos.STATE).toString();
      if ("1".equals(state)) {
        accessory.setAccessoryValue(AccessoryBean.AccessoryValue.RED);
      } else {
        accessory.setAccessoryValue(AccessoryBean.AccessoryValue.GREEN);
      }
    }

    if (values.containsKey(Ecos.ADDREXT)) {
      String addrext = values.get(Ecos.ADDREXT).toString();
      //contains the addresses also in case of multi addresses.
      //accessory.setName(name);
    }

    if (values.containsKey(Ecos.DURATION)) {
      String dur = values.get(Ecos.DURATION).toString();
      int switchTime = Integer.parseInt(dur);
      accessory.setSwitchTime(switchTime);
    }

    if (values.containsKey(Ecos.GATES)) {
      String gts = values.get(Ecos.GATES).toString();
      int states = Integer.parseInt(gts);
      accessory.setStates(states);
    }

    if (values.containsKey(Ecos.VARIANT)) {
      String var = values.get(Ecos.VARIANT).toString();
    }

    if (values.containsKey(Ecos.POSITION)) {
      String var = values.get(Ecos.POSITION).toString();
      //accessory.setName(name);
    }

    if (values.containsKey(Ecos.SWITCHING)) {
      String var = values.get(Ecos.SWITCHING).toString();
      //accessory.setName(name);
    }

    return accessory;
  }

  //TRACE	2024-12-15 19:28:52.058 [main] EsuEcosCommandStationImpl.main(): queryObjects(11, name1,name2,name3, addr, protocol, type) ->
  //<REPLY queryObjects(11, name1,name2,name3, addr, protocol, type)>
//  20000 name1["W1"] name2["artikel"] name3[">0001<"] addr[1] protocol[DCC] type[ACCESSORY]
//  20001 name1["W2"] name2["artikel"] name3[">0001<"] addr[2] protocol[DCC] type[ACCESSORY]
  //<END 0 (OK, but obsolete attribute at 53)>
//queryObjects(11, name1,name2,name3, addr, protocol, type) ->
//<REPLY queryObjects(11, name1,name2,name3, addr, protocol, type)>
//20000 name1["W1"] name2["artikel"] name3[">0001<"] addr[1] protocol[DCC] type[ACCESSORY]
//20001 name1["W2"] name2["artikel"] name3[">0001<"] addr[2] protocol[DCC] type[ACCESSORY]
//20002 name1["MW01"] name2["MM Articel"] name3["naam3"] addrext[3r] protocol[MOT] type[ACCESSORY]
//20003 name1["S125"] name2["signal"] name3[">0001<"] addrext[20r] protocol[MOT] type[ACCESSORY]
//20004 name1["S125"] name2["signal"] name3[">0001<"] addr[1] protocol[DCC] type[ACCESSORY]
//20005 name1["WE1"] name2["artikel"] name3[">0001<"] addr[12] protocol[DCC] type[ACCESSORY]
  //<END 0 (OK, but obsolete attribute at 53)>
  //Curverd or red
//get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20000 name1["W1"]20000 name2["artikel"]20000 name3[">0001<"]20000 addr[1]20000 protocol[DCC]20000 mode[SWITCH]20000 symbol[1]
//20000 state[1]20000 type[ACCESSORY]20000 addrext[1g,1r]20000 duration[250]20000 gates[2]20000 variant[0]20000 position[ok]20000 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//TX:get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)
//TRACE	2024-12-18 21:02:11.962 [ESU-ECOS-RX] EcosTCPConnection$ClientMessageReceiver.run(): RX: <REPLY get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
  //Straight green
//      get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20000 name1["W1"]20000 name2["artikel"]20000 name3[">0001<"]20000 addr[1]20000 protocol[DCC]20000 mode[SWITCH]20000 symbol[1]
//20000 state[0]20000 type[ACCESSORY]20000 addrext[1g,1r]20000 duration[250]20000 gates[2]20000 variant[0]20000 position[ok]20000 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//
//get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)
//<REPLY get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20001 name1["W2"]20001 name2["artikel"]20001 name3[">0001<"]20001 addr[2]20001 protocol[DCC]20001 mode[SWITCH]20001 symbol[0]
//20001 state[0]20001 type[ACCESSORY]20001 addrext[2g,2r]20001 duration[250]20001 gates[2]20001 variant[0]20001 position[ok]20001 switching[0]
//  <END 0 (OK, but obsolete attribute at 64)>
//state  kennelijk is 1 rood en 0 groen. hoe werkt een sein?
//<REPLY queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)>
//20000 name1["W1"] name2["artikel"] name3[">0001<"] addr[1] protocol[DCC] mode[SWITCH] symbol[1]
//20001 name1["W2"] name2["artikel"] name3[">0001<"] addr[2] protocol[DCC] mode[SWITCH] symbol[0]
//20002 name1["Sein"] name2["2 aspect"] name3[">0001<"] addr[10] protocol[MOT] mode[SWITCH] symbol[9]
//20003 name1["Sein 3"] name2["3 aspect"] name3[">0012<"] addr[12] protocol[MOT] mode[SWITCH] symbol[11]
//20004 name1["Sein 4"] name2["4 aspect"] name3[">0001<"] addr[14] protocol[MOT] mode[SWITCH] symbol[12]
//20005 name1["Sein mini"] name2["artikel"] name3[">0001<"] addr[16] protocol[MOT] mode[SWITCH] symbol[13]
  // <END 0 (OK)>
  //type is niet nodig
//get(20002, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20002, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20002 name1["Sein"]20002 name2["2 aspect"]20002 name3[">0001<"]20002 addr[10]20002 protocol[MM]20002 mode[SWITCH]20002 symbol[9]20002 
//state[0]20002 type[ACCESSORY]20002 addrext[10g,10r]20002 duration[250]20002 gates[2]20002 variant[0]20002 position[ok]20002 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//get(20003, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)
//<REPLY get(20003, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20003 name1["Sein 3"]20003 name2["3 aspect"]20003 name3[">0012<"]20003 addr[12]20003 protocol[MM]20003 mode[SWITCH]20003 symbol[11]20003
//state[0]20003 type[ACCESSORY]20003 addrext[12g,12r,13g]20003 duration[250]20003 gates[3]20003 variant[0]20003 position[ok]20003 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//get(20004, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20004, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20004 name1["Sein 4"]20004 name2["4 aspect"]20004 name3[">0001<"]20004 addr[14]20004 protocol[MM]20004 mode[SWITCH]20004 symbol[12]
//20004 state[0]20004 type[ACCESSORY]20004 addrext[14g,14r,15g,15r]20004 duration[250]20004 gates[4]20004 variant[0]20004 position[ok]20004 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//get(20005, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20005, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20005 name1["Sein mini"]20005 name2["artikel"]20005 name3[">0001<"]20005 addr[16]20005 protocol[MM]20005 mode[SWITCH]20005 symbol[13]20005
//state[0]20005 type[ACCESSORY]20005 addrext[16g,16r]20005 duration[500]20005 gates[2]20005 variant[0]20005 position[ok]20005 switching[0]
//  <END 0 (OK, but obsolete attribute at 64)>
  void update(EcosMessage message
  ) {
    parse(message);
  }

  int getSize() {
    return this.size;
  }

  Map<String, AccessoryBean> getAccessories() {
    return accessories;
  }

}
