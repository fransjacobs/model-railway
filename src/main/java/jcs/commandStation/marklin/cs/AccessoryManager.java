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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.commandStation.events.AccessoryEvent;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import org.tinylog.Logger;

/**
 * Accessory Manager for Marklin CS. <br>
 * This Manager check whether an accessory has 2 addresses like 3 way turnouts or Signals.<br>
 * The order event are give sometime matters, hence all accessory command go via this class.
 *
 * @author frans
 */
class AccessoryManager {

  private final Map<Integer, AccessoryBean> accessories;
  private final Map<Integer, AccessoryBean> accessoryEvents;
  private final MarklinCentralStationImpl marklinCentralStationImpl;

  AccessoryManager(MarklinCentralStationImpl marklinCentralStationImpl) {
    this.marklinCentralStationImpl = marklinCentralStationImpl;
    accessories = new HashMap<>();
    accessoryEvents = new HashMap<>();
  }

  void refreshAccessories(List<AccessoryBean> accessoryList) {
    accessories.clear();

    for (AccessoryBean ac : accessoryList) {
      Integer address = ac.getAddress();
      accessories.put(address, ac);
      if (ac.isBiAddress()) {
        Integer address2 = ac.getAddress2();
        accessories.put(address2, ac);
        Logger.trace("Added accessory " + ac.getId() + ", " + ac.getName() + " with address: " + ac.getAddress() + " and address2: " + ac.getAddress2());
      }
    }
  }

  AccessoryBean getAccessory(Integer address) {
    return this.accessories.get(address);
  }

  void notifyAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    AccessoryBean ab = accessoryEvent.getAccessoryBean();
    //Check is this event is for a biAddress accessory
    AccessoryBean storedAccessory = accessories.get(ab.getAddress());

    if (storedAccessory.isBiAddress()) {
      //Check if the first event was received based on the main address
      if (accessoryEvents.containsKey(storedAccessory.getAddress())) {
        //first event is received this is the second
        AccessoryBean firstValue = accessoryEvents.remove(storedAccessory.getAddress());
        AccessoryBean secondValue = ab;

        if (firstValue.getAccessoryValue() == AccessoryValue.GREEN && secondValue.getAccessoryValue() == AccessoryValue.GREEN) {
          storedAccessory.setAccessoryValue(AccessoryValue.GREEN);
        } else if (firstValue.getAddress().equals(storedAccessory.getAddress())) {
          Logger.trace("First is main address " + firstValue.getAddress() + " Value " + firstValue.getAddress() + " Second is address2 " + secondValue.getAddress() + " Value " + secondValue.getAccessoryValue());
          storedAccessory.setAccessoryValue(AccessoryValue.RED);

        } else {
          Logger.trace("First is address2 " + firstValue.getAddress() + " Value " + firstValue.getAddress() + " Second is main address " + secondValue.getAddress() + " Value " + secondValue.getAccessoryValue());
          storedAccessory.setAccessoryValue(AccessoryValue.RED2);
        }

        Logger.trace("Received 2 events for " + storedAccessory.getAddress() + ", " + storedAccessory.getName() + " A1: " + firstValue.getAddress() + " Value1: " + firstValue.getAccessoryValue() + " A2: " + secondValue.getAddress() + " Value2: " + secondValue.getAccessoryValue());

        //pass the 2nd on
        Logger.trace("Notify event for " + storedAccessory.getAddress() + " " + storedAccessory.getName() + " Value " + storedAccessory.getAccessoryValue());
        this.marklinCentralStationImpl.notifyAccessoryEventListeners(new AccessoryEvent(storedAccessory));
      } else {
        //wait for the second event, store this event for a shortwhile
        accessoryEvents.put(storedAccessory.getAddress(), ab);
        Logger.trace("Stored event for " + ab.getAddress() + " with main address: " + storedAccessory.getAddress() + " and value " + ab.getAccessoryValue());
      }
    } else {
      this.marklinCentralStationImpl.notifyAccessoryEventListeners(accessoryEvent);
    }
  }

}
