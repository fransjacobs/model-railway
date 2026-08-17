/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.loconet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN2;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import static jcs.entities.AccessoryBean.AccessoryValue.RED2;
import org.tinylog.Logger;

/**
 * Accessory Manager for Intellibox2. <br>
 * This Manager checks whether an accessory has 2 addresses, like 3 way turnouts or Signals.<br>
 * The order of events are given sometimes matters, hence all accessory commands go via this class.
 *
 */
class AccessoryManager {

  private final Map<Integer, AccessoryBean> accessories;
  private final Map<Integer, AccessoryBean> accessories2;
  private final Map<Integer, AccessoryBean> accessoryEvents;
  private final Intellibox2Impl intelliboxImpl;

  private final int defaultSwitchTime;

  AccessoryManager(Intellibox2Impl intelliboxImpl) {
    this.intelliboxImpl = intelliboxImpl;
    accessories = new ConcurrentHashMap<>();
    accessories2 = new ConcurrentHashMap<>();
    accessoryEvents = new HashMap<>();
    defaultSwitchTime = Integer.getInteger("default.switchtime", 100);
  }

  void refreshAccessories(List<AccessoryBean> accessoryList) {
    accessories.clear();

    for (AccessoryBean ac : accessoryList) {
      Integer address = ac.getAddress();

      //Check is a switchtime is set, is not set a default
      Integer switchTime = ac.getSwitchTime();
      if (switchTime == null || switchTime == 0) {
        switchTime = defaultSwitchTime / 10;
        ac.setSwitchTime(switchTime);
      }

      accessories.put(address, ac);

      if (ac.isBiAddress()) {
        Integer address2 = ac.getAddress2();
        accessories2.put(address2, ac);
        Logger.trace("Added accessory " + ac.getId() + ", " + ac.getName() + " with address: " + ac.getAddress() + " and address2: " + ac.getAddress2());
      }

      if (address == 25 || address == 27 || address == 29 || address == 31) {
        Logger.trace("Outgoing signal " + ac);
      }
    }
  }

  AccessoryBean getAccessory(Integer address) {
    return this.accessories.get(address);
  }

  void update(final AccessoryBean accessoryBean) {
    //AccessoryBean ab = accessories.get(accessoryEvent.getAddress());
    if (accessoryBean != null && accessoryBean.isOn()) {
      Logger.trace("Accessory: {} Value: {}", accessoryBean.getId(), accessoryBean.getAccessoryValue());

      fireAccessoryEventListeners(new AccessoryEvent(accessoryBean));
    }

//    if (ab == null) {
//      //might be the 2nd address
//      ab = accessories2.get(accessoryEvent.getAddress());
//      if (ab != null) {
//        Logger.trace("2nd Address " + accessoryEvent.getAddress() + " Protocol: " + accessoryEvent.getProtocol() + " value: " + accessoryEvent.getValue() + " Millis: " + accessoryEvent.getSystemtime());
//        ab.setAccessoryValue2(accessoryEvent.getValue());
//      } else {
//        Logger.warn("AccessoryEvent from unknown Accessory with address: " + accessoryEvent.getProtocol() + " " + accessoryEvent.getAddress() + " and Value " + accessoryEvent.getValue());
//      }
//    } else {
//      Logger.trace("1st Address " + accessoryEvent.getAddress() + " Protocol: " + accessoryEvent.getProtocol() + " value: " + accessoryEvent.getValue() + " Millis: " + accessoryEvent.getSystemtime());
//      ab.setAccessoryValue(accessoryEvent.getValue());
//    }
//
//    if (ab != null) {
//      if (ab.isSignal()) {
//        Logger.trace("Id: " + ab.getId() + " " + ab.getProtocol() + " Address: " + ab.getAddress() + (ab.isBiAddress() ? " Address2: " + ab.getAddress2() : "") + "  SignalValue: " + ab.getSignalValue().getSignalValue() + " State: " + ab.getState() + " of states: " + ab.getStates() + "...");
//      }
//
//      fireAccessoryEventListeners(new AccessoryEvent(ab));
//    }
  }

  private int getAddress(Integer address, String protocol) {
    int adr = address - 1;

//    if ("dcc".equals(protocol)) {
//      adr = adr + CanMessage.DCC_ACCESSORY_OFFSET;
//    } else {
//      //assume MM
//      adr = adr + CanMessage.MM_ACCESSORY_OFFSET;
//    }
    return adr;
  }

  void switchAccessory(Integer address, String protocol, AccessoryValue value, Integer switchTime) {
    Logger.trace("Try to switch accessory " + protocol + " " + address + " to " + value + " Switchtime: " + switchTime);

    //TODO obtain the accessory
//    AccessoryBean accessory = accessories.get(address);
//    if (accessory == null) {
//      accessory = accessories2.get(address);
//    }
//
//    if (accessory == null) {
//      Logger.warn("Try to switch an unknown Accessory with address: " + protocol + " " + address + " and Value " + value + " Skipping!");
//      return;
//    }
    Integer st;
    if (switchTime != null) {
      st = switchTime;
    } else {
      st = defaultSwitchTime;
    }

    //TODO how to hande biAddress accessories?
    //TODO how to hande the protocol?    
    LoconetMessage changeAccessoryOn = LoconetMessageFactory.switchAccessory(address, value, true);
    LoconetMessage changeAccessoryOff = LoconetMessageFactory.switchAccessory(address, value, false);

    LoconetMessage acknowledge = LoconetMessageFactory.longAcknowlegde(0, 0);

    this.intelliboxImpl.loconet.sendMessage(changeAccessoryOn);
    pause(st);
    this.intelliboxImpl.loconet.sendMessage(changeAccessoryOff);

  }

  protected void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

  void fireAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    List<AccessoryEventListener> snapshot = new ArrayList<>(intelliboxImpl.getAccessoryEventListeners());
    
    Logger.trace("Firing {} Accessory listeners for {} value {}",snapshot.size(),accessoryEvent.getId(), accessoryEvent.getValue() );

    for (AccessoryEventListener listener : snapshot) {
      if (accessoryEvent.getAccessoryBean().isSignal()) {
        Logger.trace("Id: " + accessoryEvent.getId() + " " + accessoryEvent.getSignalValue() + " State: " + accessoryEvent.getAccessoryBean().getState() + " listener: " + listener.getClass().getSimpleName());
      }
      listener.onAccessoryChange(accessoryEvent);
    }
  }

}
