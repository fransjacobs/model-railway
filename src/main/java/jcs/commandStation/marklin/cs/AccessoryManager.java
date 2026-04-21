/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.commandStation.marklin.cs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.CanMessageFactory;
import jcs.commandStation.marklin.cs.can.parser.AccessoryMessage;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN2;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import static jcs.entities.AccessoryBean.AccessoryValue.RED2;
import org.tinylog.Logger;

/**
 * Accessory Manager for Marklin CS. <br>
 * This Manager check whether an accessory has 2 addresses, like 3 way turnouts or Signals.<br>
 * The order of events are given sometime matters, hence all accessory commands go via this class.
 *
 */
class AccessoryManager {

  private final Map<Integer, AccessoryBean> accessories;
  private final Map<Integer, AccessoryBean> accessories2;
  private final Map<Integer, AccessoryBean> accessoryEvents;
  private final MarklinCentralStationImpl marklinCentralStationImpl;

  private final int defaultSwitchTime;

  AccessoryManager(MarklinCentralStationImpl marklinCentralStationImpl) {
    this.marklinCentralStationImpl = marklinCentralStationImpl;
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

  void update(final AccessoryEvent accessoryEvent) {

    AccessoryBean ab = accessories.get(accessoryEvent.getAddress());
    if (ab == null) {
      //might be the 2nd address
      ab = accessories2.get(accessoryEvent.getAddress());
      if (ab != null) {
        Logger.trace("2nd Address " + accessoryEvent.getAddress() + " Protocol: " + accessoryEvent.getProtocol() + " value: " + accessoryEvent.getValue() + " Millis: " + accessoryEvent.getSystemtime());
        ab.setAccessoryValue2(accessoryEvent.getValue());
      } else {
        Logger.warn("AccessoryEvent from unknown Accessory with address: " + accessoryEvent.getProtocol() + " " + accessoryEvent.getAddress() + " and Value " + accessoryEvent.getValue());
      }
    } else {
      Logger.trace("1st Address " + accessoryEvent.getAddress() + " Protocol: " + accessoryEvent.getProtocol() + " value: " + accessoryEvent.getValue() + " Millis: " + accessoryEvent.getSystemtime());
      ab.setAccessoryValue(accessoryEvent.getValue());
    }

    if (ab != null) {
      if (ab.isSignal()) {
        Logger.trace("SignalValue: " + ab.getSignalValue().getSignalValue() + " State: " + ab.getState());
      }

      fireAccessoryEventListeners(new AccessoryEvent(ab));
    }
  }

  private int getCSAddress(Integer address, String protocol) {
    //in the CS an accessory address is 0 based.
    int adr = address - 1;

    if ("dcc".equals(protocol)) {
      adr = adr + CanMessage.DCC_ACCESSORY_OFFSET;
    } else {
      //assume MM
      adr = adr + CanMessage.MM_ACCESSORY_OFFSET;
    }
    return adr;
  }

  void switchAccessory(Integer address, String protocol, AccessoryValue value, Integer switchTime) {
    Logger.trace("Try to switch accessory " + protocol + " " + address + " to " + value + " Switchtime: " + switchTime);

    //obtain the accessory
    AccessoryBean accessory = accessories.get(address);
    if (accessory == null) {
      accessory = accessories2.get(address);
    }

    if (accessory == null) {
      Logger.warn("Try to switch an unknown Accessory with address: " + protocol + " " + address + " and Value " + value + " Skipping!");
      return;
    }

    Integer st;
    if(switchTime != null) {
      st = switchTime / 10;
    } else {
      st = accessory.getSwitchTime() / 10;
    }  
    
    
    
    //For 3 way switch 2 messages must be send
    if (accessory.isBiAddress() && accessory.is3WaySwitch()) {
      //need to send 2 messages on both addresses       
      int adr = getCSAddress(address, protocol);
      int adr2 = adr + 1;

      Logger.trace("Bi Address Accessory: " + adr + " and " + adr2 + "...");

      CanMessage switchMessage;
      CanMessage switchMessage2;
      switch (value) {
        case RED -> {
          switchMessage = CanMessageFactory.switchAccessory(adr2, AccessoryValue.GREEN, true, st, marklinCentralStationImpl.getCsUid());
          switchMessage2 = CanMessageFactory.switchAccessory(adr, value, true, st, marklinCentralStationImpl.getCsUid());
        }
        case RED2 -> {
          switchMessage = CanMessageFactory.switchAccessory(adr, AccessoryValue.GREEN, true, st, marklinCentralStationImpl.getCsUid());
          switchMessage2 = CanMessageFactory.switchAccessory(adr2, AccessoryValue.RED, true, st, marklinCentralStationImpl.getCsUid());
        }
        default -> {
          switchMessage = CanMessageFactory.switchAccessory(adr, value, true, st, marklinCentralStationImpl.getCsUid());
          switchMessage2 = CanMessageFactory.switchAccessory(adr2, value, true, st, marklinCentralStationImpl.getCsUid());
        }
      }

      CanMessage message = marklinCentralStationImpl.sendMessage(switchMessage);
      CanMessage message2 = marklinCentralStationImpl.sendMessage(switchMessage2);

      AccessoryEvent ae = AccessoryMessage.parse(message);
      AccessoryEvent ae2 = AccessoryMessage.parse(message2);
      update(ae);
      update(ae2);
    } else if (accessory.isBiAddress()) {
      int adr;
      AccessoryValue val;
      switch (value) {
        case RED -> {
          adr = getCSAddress(accessory.getAddress(), protocol);
          val = value;
        }
        case GREEN -> {
          adr = getCSAddress(accessory.getAddress(), protocol);
          val = value;
        }
        case RED2 -> {
          adr = getCSAddress(accessory.getAddress2(), protocol);
          val = AccessoryValue.RED;
        }
        case WHITE -> {
          adr = getCSAddress(accessory.getAddress2(), protocol);
          val = AccessoryValue.RED;
        }
        case GREEN2 -> {
          adr = getCSAddress(accessory.getAddress2(), protocol);
          val = AccessoryValue.GREEN;
        }
        case YELLOW -> {
          adr = getCSAddress(accessory.getAddress2(), protocol);
          val = AccessoryValue.GREEN;
        }
        default -> {
          adr = getCSAddress(accessory.getAddress(), protocol);
          val = AccessoryValue.OFF;
        }
      }

      CanMessage signalMessage = CanMessageFactory.switchAccessory(adr, val, true, st, marklinCentralStationImpl.getCsUid());

      Logger.trace("Switching accessory " + adr + " to: " + val + " Message: " + signalMessage);
      CanMessage message = marklinCentralStationImpl.sendMessage(signalMessage);
      AccessoryEvent ae = AccessoryMessage.parse(message);
      update(ae);
    } else {
      int adr = getCSAddress(address, protocol);
      CanMessage switchMessage = CanMessageFactory.switchAccessory(adr, value, true, st, marklinCentralStationImpl.getCsUid());

      Logger.trace("Switching accessory " + adr + " to: " + value + " Message: " + switchMessage);
      CanMessage message = marklinCentralStationImpl.sendMessage(switchMessage);

      AccessoryEvent ae = AccessoryMessage.parse(message);
      update(ae);
    }
  }

  void fireAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    List<AccessoryEventListener> snapshot = new ArrayList<>(marklinCentralStationImpl.getAccessoryEventListeners());

    for (AccessoryEventListener listener : snapshot) {
      if (accessoryEvent.getAccessoryBean().isSignal()) {
        Logger.trace("Id: " + accessoryEvent.getId() + " " + accessoryEvent.getSignalValue() + " State: " + accessoryEvent.getAccessoryBean().getState() + " listener: " + listener.getClass().getSimpleName());
      }
      listener.onAccessoryChange(accessoryEvent);
    }
  }

}
