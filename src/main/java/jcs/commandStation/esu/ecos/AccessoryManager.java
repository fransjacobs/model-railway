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
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.entities.AccessoryBean;
import org.tinylog.Logger;

/**
 *
 * AccessoryManager (id=11)
 */
class AccessoryManager implements AccessoryEventListener {

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
        if (accessory.getId() != null) {
          accessories.put(accessory.getId(), accessory);
        }
      }

      if (values.containsKey(Ecos.SIZE)) {
        size = Integer.parseInt(values.get(Ecos.SIZE).toString());
      } else {
        size = accessories.size();
      }

    } else if (objectId >= 20000 && objectId < 29999) {
      //Details
      AccessoryBean accessory = parseValues(values, event);
      if (accessory.getId() != null) {
        accessories.put(accessory.getId(), accessory);
      }
    } else {
      Logger.warn("Unkown object Id:" + objectId);
    }
  }

  private AccessoryBean parseValues(Map<String, Object> values, boolean event) {
    String id = null;
    if (values.containsKey(Ecos.ID) && values.get(Ecos.ID) != null) {
      id = values.get(Ecos.ID).toString();
    }
    AccessoryBean accessory;
    if (accessories.containsKey(id)) {
      accessory = accessories.get(id);
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
          case "MM" ->
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
      accessory.setType(deriveType(symbol));
    }

    if (values.containsKey(Ecos.STATE)) {
      String state = values.get(Ecos.STATE).toString();

      AccessoryBean.AccessoryValue value;
      switch (state) {
        case "0" ->
          value = AccessoryBean.AccessoryValue.GREEN;
        case "1" -> {
          //Could be the case the for a 3way switch the red must be a red2 and vice versa,,,
          value = AccessoryBean.AccessoryValue.RED;
        }
        case "2" -> {
          if (accessory.isTurnout()) {
            value = AccessoryBean.AccessoryValue.RED2;
          } else {
            value = AccessoryBean.AccessoryValue.WHITE;
          }
        }
        case "3" ->
          value = AccessoryBean.AccessoryValue.YELLOW;
        default ->
          value = AccessoryBean.AccessoryValue.GREEN;
      }
      accessory.setAccessoryValue(value);

      if (event) {
        Logger.debug(Ecos.STATE + " : " + accessory.getId() + " -> " + accessory.getAccessoryValue() + " State: " + state);

        //  AccessoryEvent ae = new AccessoryEvent(accessory);
        //  ecosCommandStation.fireAccessoryEventListeners(ae);
      }

    }

    if (values.containsKey(Ecos.ADDREXT)) {
      String addrext = values.get(Ecos.ADDREXT).toString();
      Integer address = accessory.getAddress();
      Integer address2 = null;
      if (addrext != null) {
        String ae[] = addrext.split(",");
        for (int i = 0; i < ae.length; i++) {
          try {
            //filter the switch values to obtain a address
            String a = ae[i].replaceAll("g", "").replaceAll("r", "");
            int addressExt = Integer.parseInt(a);
            //Extended address are enties 2 and 3
            if (i > 1) {
              //addresses are no equal so mustbe the extension...
              address2 = addressExt;
            } else {
              if (address == null) {
                address = addressExt;
              }
            }
          } catch (NumberFormatException e) {
            //ignore 
            Logger.trace("Filtered ADDREXT: " + addrext + " does not contain a number.");
          }
        }
      }

      if (accessory.getAddress() == null) {
        accessory.setAddress(address);
      }
      accessory.setAddress2(address2);
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
      //an accessory has changed value
      AccessoryBean.AccessoryValue value;
//      switch (var) {
//        case "0" ->
//          value = AccessoryBean.AccessoryValue.RED;
//        case "1" ->
//          value = AccessoryBean.AccessoryValue.GREEN;
//        case "2" -> {
//          if (accessory.isTurnout()) {
//            value = AccessoryBean.AccessoryValue.RED2;
//          } else {
//            value = AccessoryBean.AccessoryValue.WHITE;
//          }
//        }
//        case "3" ->
//          value = AccessoryBean.AccessoryValue.YELLOW;
//        default ->
//          value = AccessoryBean.AccessoryValue.GREEN;
//      }
//      accessory.setAccessoryValue(value);

//      if (event) {
//        Logger.debug(Ecos.SWITCHING + " : " + accessory.getId() + " -> " + accessory.getAccessoryValue() + " Var: " + var);
//        AccessoryEvent ae = new AccessoryEvent(accessory);
//        ecosCommandStation.fireAccessoryEventListeners(ae);
//
//      }
    }

    //Checks
    Integer adrs = accessory.getAddress();
    Integer adrsExt = accessory.getAddress2();

    if (adrs == null) {
      Logger.warn("Accessory " + accessory.getId() + " does not have an address?");
      try {
        Integer idInt = Integer.parseInt(accessory.getId());
        adrs = idInt;
        accessory.setAddress(adrs);
      } catch (NumberFormatException e) {
        //ignore for this check
      }
    }

    //Check address and address 2
    if (adrs != null && adrs.equals(adrsExt)) {
      accessory.setAddress2(null);
      Logger.trace("Reset address2 for address: " + accessory.getAddress());
    }

    //also check against the id in case the id is a number
    try {
      Integer idInt = Integer.parseInt(accessory.getId());
      if (idInt.equals(accessory.getAddress2())) {
        accessory.setAddress2(null);
        Logger.trace("Reset address2 for id: " + idInt);
      }
    } catch (NumberFormatException e) {
      //ignore for this check
    }

    if (event) {
      Logger.debug("Raise AccessoryEvent " + accessory.getId() + " " + accessory.getAccessoryValue());
      AccessoryEvent ae = new AccessoryEvent(accessory);
      ecosCommandStation.fireAccessoryEventListeners(ae);
    }

    return accessory;
  }

  void update(EcosMessage message) {
    parse(message);
  }

  int getSize() {
    return size;
  }

  Map<String, AccessoryBean> getAccessories() {
    return accessories;
  }

  AccessoryBean getAccessory(Integer address) {
    AccessoryBean result = null;
    for (AccessoryBean accessory : accessories.values()) {
      if (address.equals(accessory.getAddress())) {
        result = accessory;
        break;
      }
    }

    return result;
  }

  AccessoryBean getAccessoryViaAddress2(Integer address2) {
    AccessoryBean result = null;
    for (AccessoryBean accessory : accessories.values()) {
      if (address2.equals(accessory.getAddress2())) {
        result = accessory;
        break;
      }
    }

    return result;
  }

  String findId(Integer address) {
    String id = null;
    for (AccessoryBean accessory : this.accessories.values()) {
      if (address.equals(accessory.getAddress())) {
        id = accessory.getId();
        break;
      }
    }
    return id;
  }

  @Override
  public void onAccessoryChange(AccessoryEvent accessoryEvent) {
    AccessoryBean ab = accessoryEvent.getAccessoryBean();
    String id = accessoryEvent.getAccessoryBean().getId();
    if (!accessories.containsKey(id)) {
      id = findId(ab.getAddress());
    }

    AccessoryBean accessory = accessories.get(id);
    if (accessory != null) {
      accessory.setAccessoryValue(ab.getAccessoryValue());
    }
  }

  String updateManager(EcosMessage event) {
    Map<String, Object> values = event.getValueMap();

    String addedId = null;
    boolean accessoryAdded = false;
    int newSize = 0;
    for (String key : values.keySet()) {
      Object o = values.get(key);
      if (o instanceof Map) {
        Map<String, Object> vm = (Map<String, Object>) o;
        if (vm.containsKey(Ecos.ID)) {
          addedId = vm.get(Ecos.ID).toString();
        }

        Logger.trace(vm.keySet());
        Logger.trace(vm.values());
        //id, appended11 size]

        if (vm.containsKey("appended11 size")) {
          String s = vm.get("appended11 size").toString();
          newSize = Integer.parseInt(s);
        }

      } else {
        if (Ecos.MSG.equals(key)) {
          accessoryAdded = "LIST_CHANGED".equals(o);
        } else if (Ecos.SIZE.equals(key) && o != null) {
          size = Integer.parseInt(o.toString());
        }
      }
    }

    if (accessoryAdded && getSize() <= newSize) {
      if (ecosCommandStation != null) {
        EcosMessage reply = ecosCommandStation.getConnection().sendMessage(EcosMessageFactory.getAccessoryDetails(addedId));
        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
        //this.update(reply);
      }

      return addedId;
    } else {
      return null;
    }
  }

  static String deriveType(String symbol) {
    return switch (symbol) {
      case "0" ->
        "linksweiche";
      case "1" ->
        "rechtsweiche";
      case "2" ->
        "dreiwegweiche";
      case "3" ->
        "y_weiche";
      case "4" ->
        "y_weiche";
      case "5" ->
        "formsignal_HP01";
      case "6" ->
        "formsignal_HP02";
      case "7" ->
        "formsignal_HP012";
      case "8" ->
        "formsignal_SH01";
      case "9" ->
        "lichtsignal_HP01";
      case "10" ->
        "lichtsignal_HP02";
      case "11" ->
        "lichtsignal_HP012";
      case "12" ->
        "lichtsignal_HP012_SH01";
      case "13" ->
        "lichtsignal_SH01";
      case "17" ->
        "entkupplungsgleis";
      case "19" ->
        "linksweiche";
      case "20" ->
        "rechtsweiche";
      case "33" ->
        "y_weiche";
      case "34" ->
        "y_weiche";
      case "35" ->
        "y_weiche";
      default ->
        "weiche";
    };
  }
}
