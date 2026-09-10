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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import static jcs.commandStation.loconet.Intellibox2Impl.COMMAND_STATION_ID;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.persistence.PersistenceFactory;
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

  private ScheduledExecutorService scheduler;

  private final int defaultSwitchTime;

  AccessoryManager(Intellibox2Impl intelliboxImpl) {
    this.intelliboxImpl = intelliboxImpl;
    accessories = new ConcurrentHashMap<>();
    accessories2 = new ConcurrentHashMap<>();
    accessoryEvents = new HashMap<>();
    defaultSwitchTime = Integer.getInteger("default.switchtime", 100);
  }

  private ScheduledExecutorService createScheduler() {
    return Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "INBX-LN-SCHED");
      thread.setDaemon(true);
      return thread;
    });
  }

  void start() {
    if (scheduler == null || scheduler.isShutdown()) {
      scheduler = createScheduler();
    }
  }

  void shutdown() {
    if (scheduler != null) {
      scheduler.shutdownNow();
    }
  }

  void refresh() {
    refreshAccessories(PersistenceFactory.getService().getAccessoriesByCommandStationId(COMMAND_STATION_ID));
  }

  synchronized void refreshAccessories(List<AccessoryBean> accessoryList) {
    accessories.clear();
    accessories2.clear();
    accessoryEvents.clear();

    for (AccessoryBean ac : accessoryList) {
      Integer address = ac.getAddress();

      //Check is a switchtime is set, is not set a default
      Integer switchTime = ac.getSwitchTime();
      if (switchTime == null || switchTime == 0) {
        switchTime = defaultSwitchTime;
        ac.setSwitchTime(switchTime);
      }

      accessories.put(address, ac);

      if (ac.isBiAddress()) {
        Integer address2 = ac.getAddress2();
        accessories2.put(address2, ac);
        Logger.trace("Added accessory {}, {} with address: {} and address2: {}", ac.getId(), ac.getName(), ac.getAddress(), ac.getAddress2());
      }
    }
    Logger.trace("There are {} accessories and {} bi-address accessories.", accessories.size(), accessories2.size());
  }

  AccessoryBean getAccessory(Integer address) {
    return this.accessories.get(address);
  }

  void update(final AccessoryBean accessoryBean) {

    if (accessoryBean != null && accessoryBean.isOn()) {
      Logger.trace("Accessory: {} Value: {}", accessoryBean.getId(), accessoryBean.getAccessoryValue());

      AccessoryBean registered = accessories.get(accessoryBean.getAddress());

      if (registered == null) {
        registered = accessories2.get(accessoryBean.getAddress());
      }

      if (registered == null) {
        Logger.warn("AccessoryEvent from unknown accessory address: {}", accessoryBean.getAddress());
        return;
      }

      registered.setAccessoryValue(accessoryBean.getAccessoryValue());

      fireAccessoryEventListeners(new AccessoryEvent(registered));
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
    Logger.trace("Try to switch accessory {} {} to {} Switchtime: {}", protocol, address, value, switchTime);

    AccessoryBean accessory = accessories.get(address);
    if (accessory == null) {
      accessory = accessories2.get(address);
    }

    if (accessory == null) {
      Logger.warn("Requested to switch an unregistered Accessory with protocol/address: {}/{} and Value {}. Skipping!", protocol, address, value);
      return;
    }

    int st;
    if (switchTime != null && switchTime > 0) {
      st = switchTime;
    } else if (accessory.getSwitchTime() != null && accessory.getSwitchTime() > 0) {
      st = accessory.getSwitchTime();
    } else {
      st = defaultSwitchTime;
    }

    LoconetMessage changeAccessoryOn = LoconetMessageFactory.switchAccessory(address, value, true);

    LoconetMessage changeAccessoryOff = LoconetMessageFactory.switchAccessory(address, value, false);

    CompletableFuture<LoconetMessage> onEchoFuture = this.intelliboxImpl.loconet.sendMessageAsyncAwaitEcho(changeAccessoryOn);

    scheduler.schedule(() -> {
      try {
        Logger.trace("Sending Accessory Off: {}", changeAccessoryOff);

        /*
         * Use no-wait here so the magnet OFF timing is not affected by echo wait.
         */
        this.intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(changeAccessoryOff);

      } catch (Exception ex) {
        Logger.error("Could not send accessory OFF message: {}", ex.getMessage());
      }
    }, st, TimeUnit.MILLISECONDS);

    onEchoFuture.thenAccept(reply -> {
      if (reply == null) {
        Logger.trace("No echo received for accessory ON message: {}", changeAccessoryOn);
        return;
      }

      try {
        Logger.trace("AccessoryReply: {}", reply);
        AccessoryBean ab = LoconetMessageParser.parseSwitchEvent(reply);
        update(ab);
      } catch (Exception ex) {
        Logger.error("Could not process accessory ON echo: {}", ex.getMessage());
      }
    });
  }

  void fireAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    List<AccessoryEventListener> snapshot = new ArrayList<>(intelliboxImpl.getAccessoryEventListeners());

    Logger.trace("Firing {} Accessory listeners for {} value {}", snapshot.size(), accessoryEvent.getId(), accessoryEvent.getValue());

    for (AccessoryEventListener listener : snapshot) {
      if (accessoryEvent.getAccessoryBean().isSignal()) {
        Logger.trace("Id: " + accessoryEvent.getId() + " " + accessoryEvent.getSignalValue() + " State: " + accessoryEvent.getAccessoryBean().getState() + " listener: " + listener.getClass().getSimpleName());
      }
      listener.onAccessoryChange(accessoryEvent);
    }
  }

}
