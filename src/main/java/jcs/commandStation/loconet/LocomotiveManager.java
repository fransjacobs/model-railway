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
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import static jcs.commandStation.loconet.Intellibox2Impl.COMMAND_STATION_ID;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 */
class LocomotiveManager implements LocomotiveSpeedEventListener, LocomotiveDirectionEventListener, LocomotiveFunctionEventListener {

  private int size;
  private final Intellibox2Impl intelliboxImpl;

  private final Map<Long, LocomotiveBean> locomotives;
  private final Map<Integer, Long> locomotiveAddresses;
  private final Map<Integer, Long> locomotiveSlots;
  private final Map<Long, Integer> locomotiveSlotsReverse;

  LocomotiveManager(Intellibox2Impl intelliboxImpl) {
    this.intelliboxImpl = intelliboxImpl;
    locomotives = new ConcurrentHashMap<>();
    locomotiveAddresses = new HashMap<>();
    locomotiveSlots = new HashMap<>();
    locomotiveSlotsReverse = new HashMap<>();
  }

  void refresh() {
    refreshLocomotives(PersistenceFactory.getService().getLocomotivesByCommandStationId(COMMAND_STATION_ID));
  }

  void shutdown() {
  }

  synchronized void refreshLocomotives(List<LocomotiveBean> locomotiveList) {
    locomotives.clear();
    locomotiveAddresses.clear();
    locomotiveSlots.clear();
    locomotiveSlotsReverse.clear();

    for (LocomotiveBean loc : locomotiveList) {
      Long id = loc.getId();
      Integer address = loc.getAddress();

      locomotives.put(id, loc);
      locomotiveAddresses.put(address, id);
    }
    Logger.trace("There are {} locomotives.", locomotives.size());

    registerSlots();

  }

  void changeVelocity(int address, int speed, Direction direction) {
    Logger.trace("Changing speed for locomotive address {} to {}. Direction {}.", address, speed, direction);

    //Scale the spd
    // max scale of JCS is 1024 Intellibox 127 so roughly divede by 8
    int spd = speed / 8;

    if (locomotiveAddresses.containsKey(address)) {
      Long id = locomotiveAddresses.get(address);
      LocomotiveBean locomotive = locomotives.get(id);

      int slot;
      if (locomotiveSlotsReverse.containsKey(id)) {
        slot = locomotiveSlotsReverse.get(id);
      } else {
        slot = requestSlotDataByAddress(locomotive);
      }

      locomotive.setDirection(direction);
      locomotive.setVelocity(speed);

      LoconetMessage tx = LoconetMessageFactory.changeLocomotiveSpeed(slot, spd);
      intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(tx);

      LocomotiveSpeedEvent lse = new LocomotiveSpeedEvent(locomotive);
      intelliboxImpl.notifyLocomotiveSpeedEventListeners(lse);
    }
  }

  void changeDirection(int address, Direction direction) {
    if (locomotiveAddresses.containsKey(address)) {
      Long id = locomotiveAddresses.get(address);
      LocomotiveBean locomotive = locomotives.get(id);

      int slot;
      if (locomotiveSlotsReverse.containsKey(id)) {
        slot = locomotiveSlotsReverse.get(id);
      } else {
        slot = requestSlotDataByAddress(locomotive);
      }

      locomotive.setDirection(direction);
      Map<Integer, FunctionBean> functionValues = locomotive.getFunctions();
      boolean f0 = functionValues.get(0).isOn();
      boolean f1 = functionValues.get(1).isOn();
      boolean f2 = functionValues.get(2).isOn();
      boolean f3 = functionValues.get(3).isOn();
      boolean f4 = functionValues.get(4).isOn();

      LoconetMessage tx = LoconetMessageFactory.setDirectionAndFunctions(slot, direction, f0, f1, f2, f3, f4);
      intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(tx);

      LocomotiveDirectionEvent lde = new LocomotiveDirectionEvent(locomotive);
      intelliboxImpl.notifyLocomotiveDirectionEventListeners(lde);
    }
  }

  void changeFunctionValue(int address, int functionNumber, boolean flag) {
    if (locomotiveAddresses.containsKey(address)) {
      Long id = locomotiveAddresses.get(address);
      LocomotiveBean locomotive = locomotives.get(id);

      int slot;
      if (locomotiveSlotsReverse.containsKey(id)) {
        slot = locomotiveSlotsReverse.get(id);
      } else {
        slot = requestSlotDataByAddress(locomotive);
      }

      locomotive.setFunctionValue(functionNumber, flag);
      Direction dir = locomotive.getDirection();
      Map<Integer, FunctionBean> functionValues = locomotive.getFunctions();
      if (functionNumber < 5) {
        boolean f0 = functionValues.get(0).isOn();
        boolean f1 = functionValues.get(1).isOn();
        boolean f2 = functionValues.get(2).isOn();
        boolean f3 = functionValues.get(3).isOn();
        boolean f4 = functionValues.get(4).isOn();

        LoconetMessage tx = LoconetMessageFactory.setDirectionAndFunctions(slot, dir, f0, f1, f2, f3, f4);
        intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(tx);
      } else {
        boolean f5 = functionValues.get(5).isOn();
        boolean f6 = functionValues.get(6).isOn();
        boolean f7 = functionValues.get(7).isOn();
        boolean f8 = functionValues.get(8).isOn();

        LoconetMessage tx = LoconetMessageFactory.setFunctions(slot, f5, f5, f7, f8);
        intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(tx);
        //other functions.... TODO
      }

      FunctionBean changedFunction = locomotive.getFunctionBean(functionNumber);
      LocomotiveFunctionEvent lfe = new LocomotiveFunctionEvent(changedFunction);
      intelliboxImpl.notifyLocomotiveFunctionEventListeners(lfe);
    }
  }

  //When a locomotive change is requested check whether the locomotive has a slot#.
  //when not try to obtain a slot.
  //may be obtain a slow for the locomotives whci are shown as there are engoug slots avalable
  void registerSlots() {
    for (LocomotiveBean locomotive : locomotives.values()) {
      if (locomotive.isShow()) {
        //obtain the slot
        requestSlotDataByAddress(locomotive);
        Logger.debug("Slot# {} Locomotive {}, address: {}, Id: {}", locomotiveSlotsReverse.get(locomotive.getId()), locomotive.getName(), locomotive.getAddress(), locomotive.getId());
      }
    }
  }

  int requestSlotDataByAddress(LocomotiveBean locomotive) {
    if (locomotive.getAddress() == null) {
      return -1;
    }
    int address = locomotive.getAddress();
    LoconetMessage request = LoconetMessageFactory.requestLocoAddress(address);
    LoconetMessage reply = intelliboxImpl.loconet.sendMessageAwaitEchoAndReply(request, LoconetMessageParser.replyForLocoAddressRequest(request), 500);

    if (reply == null) {
      Logger.warn("No slot reply received for locomotive address {}", address);
      return -1;
    }

    if (reply.isExpectedsOpcode(LoconetMessage.OPC_SL_RD_DATA)) {
      // Happy flow: parse slot data and continue.
      Logger.trace("Received slot data: {}", reply);
    }

    if (reply.isExpectedsOpcode(LoconetMessage.OPC_LONG_ACK)) {
      // Failure flow: parse ACK1.
      Logger.warn("Locomotive address request failed: {}", reply);
    }
    parseSlotData(reply, locomotive, false);

    int slot;
    if (locomotiveSlotsReverse.containsKey(locomotive.getId())) {
      slot = locomotiveSlotsReverse.get(locomotive.getId());
    } else {
      slot = -1;
      Logger.debug("No valid slot found for locomotive id {}, name {} and address {}!", locomotive.getId(), locomotive.getName(), locomotive.getAddress());
    }

    return slot;
  }

//  void requestSlotDataBySlot(LocomotiveBean locomotive) {
//    if (locomotive.getAddress() == null) {
//      return;
//    }
//    int address = locomotive.getAddress();
//    LoconetMessage request = LoconetMessageFactory.requestLocoAddress(address);
//    LoconetMessage reply = intelliboxImpl.loconet.sendMessageAwaitEchoAndReply(request, LoconetMessageParser.replyForLocoAddressRequest(request), 500);
//
//    if (reply == null) {
//      Logger.warn("No slot reply received for locomotive address {}", address);
//      return;
//    }
//
//    if (reply.isExpectedsOpcode(LoconetMessage.OPC_SL_RD_DATA)) {
//      // Happy flow: parse slot data and continue.
//      Logger.trace("Received slot data: {}", reply);
//    }
//
//    if (reply.isExpectedsOpcode(LoconetMessage.OPC_LONG_ACK)) {
//      // Failure flow: parse ACK1.
//      Logger.warn("Locomotive address request failed: {}", reply);
//    }
//    parseSlotData(reply, locomotive, false);
//
//    LocomotiveSpeedEvent lse = new LocomotiveSpeedEvent(locomotive);
//    intelliboxImpl.notifyLocomotiveSpeedEventListeners(lse);
//  }
  void updateLocomotiveSpeed(LoconetMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (!message.isChecksumValid()) {
      throw new IllegalArgumentException("Checksum mismatch for message " + message);
    }

    if (!message.isExpectedsOpcode(LoconetMessage.OPC_LOCO_SPD)) {
      throw new IllegalArgumentException("Not an OPC_LOCO_SPD message, opcode=" + message.getHexOpcode());
    }

    int slot = message.getArgument(1);
    int spd = message.getArgument(2);

    if (locomotiveSlots.containsKey(slot)) {
      LocomotiveBean locomotive = locomotives.get(locomotiveSlots.get(slot));

      int velocity = spd * 8;

      if (locomotive.getVelocity() != velocity) {
        locomotive.setVelocity(velocity);
        LocomotiveSpeedEvent lse = new LocomotiveSpeedEvent(locomotive);
        intelliboxImpl.notifyLocomotiveSpeedEventListeners(lse);
      }
    } else {
      Logger.trace("No registered locomotive for slot {}!", slot);
    }
  }

  void updateLocomotiveDirf(LoconetMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (!message.isChecksumValid()) {
      throw new IllegalArgumentException("Checksum mismatch for message " + message);
    }

    if (!message.isExpectedsOpcode(LoconetMessage.OPC_LOCO_DIRF)) {
      throw new IllegalArgumentException("Not an OPC_LOCO_DIRF message, opcode=" + message.getHexOpcode());
    }

    int slot = message.getArgument(1);
    int dirf = message.getArgument(2);

    if (locomotiveSlots.containsKey(slot)) {
      LocomotiveBean locomotive = locomotives.get(locomotiveSlots.get(slot));
      LocomotiveBean.Direction direction = decodeSlotDirection(dirf);

      boolean f0 = (dirf & 0x10) != 0;
      boolean f1 = (dirf & 0x01) != 0;
      boolean f2 = (dirf & 0x02) != 0;
      boolean f3 = (dirf & 0x04) != 0;
      boolean f4 = (dirf & 0x08) != 0;

      Logger.trace("Update loco with id {}, {} on slot {} to Dir: {}, f0 {}, f1 {}, f2 {}, f3 {}, f4 {}",
              locomotive.getId(),
              locomotive.getName(), slot, direction,
              f0, f1, f2, f3, f4);

      List<LocomotiveFunctionEvent> changedFunctions = new ArrayList<>();

      //Make sure the functions are available
      if (!locomotive.hasFunction(0)) {
        locomotive.addFunction(new FunctionBean(0, locomotive.getId()));
      }
      if (!locomotive.hasFunction(1)) {
        locomotive.addFunction(new FunctionBean(1, locomotive.getId()));
      }
      if (!locomotive.hasFunction(2)) {
        locomotive.addFunction(new FunctionBean(2, locomotive.getId()));
      }
      if (!locomotive.hasFunction(3)) {
        locomotive.addFunction(new FunctionBean(3, locomotive.getId()));
      }
      if (!locomotive.hasFunction(4)) {
        locomotive.addFunction(new FunctionBean(4, locomotive.getId()));
      }

      if (locomotive.getFunctionBean(0).isOn() != f0) {
        locomotive.setFunctionValue(0, f0);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(0)));
      }

      if (locomotive.getFunctionBean(1).isOn() != f1) {
        locomotive.setFunctionValue(1, f1);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(1)));
      }
      if (locomotive.getFunctionBean(2).isOn() != f2) {
        locomotive.setFunctionValue(2, f2);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(2)));
      }
      if (locomotive.getFunctionBean(3).isOn() != f3) {
        locomotive.setFunctionValue(3, f3);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(3)));
      }
      if (locomotive.getFunctionBean(4).isOn() != f4) {
        locomotive.setFunctionValue(4, f4);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(4)));
      }

      if (locomotive.getDirection() != direction) {
        locomotive.setDirection(direction);
        LocomotiveDirectionEvent lde = new LocomotiveDirectionEvent(locomotive);
        intelliboxImpl.notifyLocomotiveDirectionEventListeners(lde);
      }

      for (LocomotiveFunctionEvent lfe : changedFunctions) {
        intelliboxImpl.notifyLocomotiveFunctionEventListeners(lfe);
      }
    } else {
      Logger.trace("No registered locomotive for slot {}!", slot);
    }
  }

  void updateLocomotiveSnd(LoconetMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (!message.isChecksumValid()) {
      throw new IllegalArgumentException("Checksum mismatch for message " + message);
    }

    if (!message.isExpectedsOpcode(LoconetMessage.OPC_LOCO_SND)) {
      throw new IllegalArgumentException("Not an OPC_LOCO_SND message, opcode=" + message.getHexOpcode());
    }

    int slot = message.getArgument(1);
    int snd = message.getArgument(2);

    if (locomotiveSlots.containsKey(slot)) {
      LocomotiveBean locomotive = locomotives.get(locomotiveSlots.get(slot));
      LocomotiveBean.Direction direction = decodeSlotDirection(snd);

      boolean f5 = (snd & 0x01) != 0;
      boolean f6 = (snd & 0x02) != 0;
      boolean f7 = (snd & 0x04) != 0;
      boolean f8 = (snd & 0x08) != 0;

      Logger.trace("Update loco with id {}, {} on slot {}, f5 {}, f6 {}, f7 {}, f8 {}",
              locomotive.getId(),
              locomotive.getName(), slot,
              f5, f6, f7, f8);

      List<LocomotiveFunctionEvent> changedFunctions = new ArrayList<>();

      //Make sure the functions are available
      if (!locomotive.hasFunction(5)) {
        locomotive.addFunction(new FunctionBean(5, locomotive.getId()));
      }
      if (!locomotive.hasFunction(6)) {
        locomotive.addFunction(new FunctionBean(6, locomotive.getId()));
      }
      if (!locomotive.hasFunction(7)) {
        locomotive.addFunction(new FunctionBean(7, locomotive.getId()));
      }
      if (!locomotive.hasFunction(8)) {
        locomotive.addFunction(new FunctionBean(8, locomotive.getId()));
      }

      if (locomotive.getFunctionBean(5).isOn() != f5) {
        locomotive.setFunctionValue(5, f5);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(5)));
      }
      if (locomotive.getFunctionBean(6).isOn() != f6) {
        locomotive.setFunctionValue(6, f6);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(6)));
      }
      if (locomotive.getFunctionBean(7).isOn() != f7) {
        locomotive.setFunctionValue(7, f7);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(7)));
      }
      if (locomotive.getFunctionBean(8).isOn() != f8) {
        locomotive.setFunctionValue(8, f8);
        changedFunctions.add(new LocomotiveFunctionEvent(locomotive.getFunctionBean(8)));
      }

      for (LocomotiveFunctionEvent lfe : changedFunctions) {
        intelliboxImpl.notifyLocomotiveFunctionEventListeners(lfe);
      }
    } else {
      Logger.trace("No registered locomotive for slot {}!", slot);
    }
  }

  int getSize() {
    return this.size;
  }

  LocomotiveBean parseSlotData(LoconetMessage message, LocomotiveBean locomotive,
          boolean updateDirAndFunc
  ) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (!message.isChecksumValid()) {
      throw new IllegalArgumentException("Checksum mismatch for message " + message);
    }

    if (!message.isExpectedsOpcode(LoconetMessage.OPC_SL_RD_DATA)) {
      throw new IllegalArgumentException("Not an OPC_SL_RD_DATA message, opcode=" + message.getHexOpcode());
    }

    if (message.getLength() < 14) {
      throw new IllegalArgumentException("OPC_SL_RD_DATA message too short: " + message);
    }

    int count = message.getArgument(1);
    int slot = message.getArgument(2);
    int stat1 = message.getArgument(3);
    int addressLow = message.getArgument(4);
    int speedByte = message.getArgument(5);
    int dirf = message.getArgument(6);
    int track = message.getArgument(7);
    int stat2 = message.getArgument(8);
    int addressHigh = message.getArgument(9);
    int sound = message.getArgument(10);

    int id1 = message.getArgument(11);
    int id2 = message.getArgument(12);

    if (count != message.getLength()) {
      Logger.warn("OPC_SL_RD_DATA count byte {} does not match message length {} for {}", count, message.getLength(), message);
    }

    int address = ((addressHigh & 0x7F) << 7) | (addressLow & 0x7F);
    if (locomotive == null && locomotiveAddresses.containsKey(address)) {
      Long locomotiveId = locomotiveAddresses.get(address);
      locomotive = locomotives.get(locomotiveId);
      if (locomotive != null) {
        Logger.trace("Obtained Locomotive {} with id {} and address {} from cached locomotives.", locomotive.getName(), locomotive.getId(), locomotive.getAddress());
      }
    }

    if (locomotive == null && locomotiveSlots.containsKey(slot)) {
      Long locomotiveId = locomotiveSlots.get(slot);
      locomotive = locomotives.get(locomotiveId);
      if (locomotive != null) {
        Logger.trace("Obtained Locomotive {} with id {} and address {} via slot {} from cached locomotives.", locomotive.getName(), locomotive.getId(), locomotive.getAddress(), slot);
      }
    }

    if (locomotive == null) {
      locomotive = new LocomotiveBean();

      locomotive.setAddress(address);
      Logger.warn("Received slot data for unknown locomotive address {} in slot {}: {}", address, slot, message);
    }

    if (locomotive.getId() != null) {
      locomotiveSlots.put(slot, locomotive.getId());
      locomotiveSlotsReverse.put(locomotive.getId(), slot);
      Logger.trace("Mapped slot# {} to Id: {}, Address: {} name: {}", slot, locomotive.getId(), locomotive.getAddress(), locomotive.getName());
    } else {
      locomotive.setAddress(address);
      Logger.trace("Can't Map slot# {} as Id is null", slot);
    }

    int velocity = decodeSlotSpeed(speedByte);
    LocomotiveBean.Direction direction = decodeSlotDirection(dirf);

    locomotive.setVelocity(velocity);
    //if (updateDirAndFunc) {
    //  locomotive.setDirection(direction);
    //updateSlotFunctions(locomotive, dirf, sound);
    //}

    int throttleId = ((id2 & 0x7F) << 7) | (id1 & 0x7F);
    boolean trackPower = (track & 0x01) != 0;
    boolean trackIdle = (track & 0x02) != 0;
    boolean slotInUse = isSlotInUse(stat1);

    Logger.trace("Updated locomotive {} address {} from slot# {}: speed: {}, direction: {}, slotInUse: {}, stat1: {}, stat2: {}, trackPower: {}, trackIdle: {}, throttleId: {}",
            locomotive.getId(),
            address,
            slot,
            velocity,
            direction,
            slotInUse,
            LoconetMessage.getByteHex(stat1),
            LoconetMessage.getByteHex(stat2),
            trackPower,
            trackIdle,
            throttleId
    );

    if (!slotInUse) {
      LoconetMessage tx = LoconetMessageFactory.activateSlot(slot, true);
      intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(tx);
    }

    return locomotive;
  }

  boolean parseLongAck(LoconetMessage message, LoconetMessage request
  ) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (!message.isChecksumValid()) {
      throw new IllegalArgumentException("Checksum mismatch for message " + message);
    }

    if (!message.isExpectedsOpcode(LoconetMessage.OPC_LONG_ACK)) {
      throw new IllegalArgumentException("Not an OPC_LONG_ACK message, opcode=" + message.getHexOpcode());
    }

    int lopc = message.getArgument(1);
    int ack1 = message.getArgument(2);
    int expectedLopc = request != null ? request.getOpcode() & 0x7F : -1;

    if (request != null && lopc != expectedLopc) {
      Logger.warn("LONG_ACK {} does not belong to request {}. LOPC={}, expected={}",
              message,
              request,
              LoconetMessage.getByteHex(lopc),
              LoconetMessage.getByteHex(expectedLopc));
      return false;
    }

    if (ack1 == 0x00) {
      Logger.warn("LocoNet LONG_ACK failure for request opcode {}: {}", LoconetMessage.getByteHex(lopc), message);
      return false;
    }

    Logger.trace("LocoNet LONG_ACK success for request opcode {} ACK1={}",
            LoconetMessage.getByteHex(lopc),
            LoconetMessage.getByteHex(ack1));
    return true;
  }

  private int decodeSlotSpeed(int speedByte) {
    /*
     * LocoNet slot speed:
     *   0x00 = stop
     *   0x01 = emergency stop
     *   0x02..0x7F = normal speed values
     *
     * Keep the normal slot speed value as-is for now. If the UI later needs
     * a strict 0..126 range, change this to "speedByte - 1" for values >= 2.
     */
    if (speedByte <= 0x01) {
      return 0;
    }
    return speedByte;
  }

  private boolean isSlotInUse(int stat1) {
    /*
     * STAT1 bits D5/D4 indicate slot activity:
     *   11 = in-use/refreshed
     *   10 = idle/not refreshed
     *   01 = common/refreshed
     *   00 = free/no valid data
     */
    return (stat1 & 0x30) != 0;
  }

  private LocomotiveBean.Direction decodeSlotDirection(int dirf) {
    boolean forwards = (dirf & 0x20) != 0;
    return forwards ? LocomotiveBean.Direction.FORWARDS : LocomotiveBean.Direction.BACKWARDS;
  }

//  private void updateSlotFunctions(LocomotiveBean locomotive, int dirf, int sound) {
//    if (!locomotive.hasFunction(0)) {
//      locomotive.addFunction(new FunctionBean(0, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(0, (dirf & 0x10) != 0);
//
//    if (!locomotive.hasFunction(1)) {
//      locomotive.addFunction(new FunctionBean(1, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(1, (dirf & 0x01) != 0);
//
//    if (!locomotive.hasFunction(2)) {
//      locomotive.addFunction(new FunctionBean(2, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(2, (dirf & 0x02) != 0);
//
//    if (!locomotive.hasFunction(3)) {
//      locomotive.addFunction(new FunctionBean(3, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(3, (dirf & 0x04) != 0);
//
//    if (!locomotive.hasFunction(4)) {
//      locomotive.addFunction(new FunctionBean(4, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(4, (dirf & 0x08) != 0);
//
//    if (!locomotive.hasFunction(5)) {
//      locomotive.addFunction(new FunctionBean(5, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(5, (sound & 0x01) != 0);
//
//    if (!locomotive.hasFunction(6)) {
//      locomotive.addFunction(new FunctionBean(6, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(6, (sound & 0x02) != 0);
//
//    if (!locomotive.hasFunction(7)) {
//      locomotive.addFunction(new FunctionBean(7, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(7, (sound & 0x04) != 0);
//
//    if (!locomotive.hasFunction(8)) {
//      locomotive.addFunction(new FunctionBean(8, locomotive.getId()));
//    }
//    locomotive.setFunctionValue(8, (sound & 0x08) != 0);
//  }
  Map<Long, LocomotiveBean> getLocomotives() {
    return locomotives;
  }

  @Override
  public void onSpeedChange(LocomotiveSpeedEvent velocityEvent) {
    if (this.locomotives.containsKey(velocityEvent.getId())) {
      LocomotiveBean lb = this.locomotives.get(velocityEvent.getId());
      lb.setVelocity(velocityEvent.getVelocity());
    }
  }

  @Override
  public void onDirectionChange(LocomotiveDirectionEvent directionEvent) {
    if (this.locomotives.containsKey(directionEvent.getId())) {
      LocomotiveBean lb = this.locomotives.get(directionEvent.getId());
      lb.setDirection(directionEvent.getNewDirection());
    }
  }

  @Override
  public void onFunctionChange(LocomotiveFunctionEvent locomotiveFunctionEvent) {
    if (this.locomotives.containsKey(locomotiveFunctionEvent.getId())) {
      LocomotiveBean lb = this.locomotives.get(locomotiveFunctionEvent.getId());
      FunctionBean fb = lb.getFunctionBean(locomotiveFunctionEvent.getNumber());
      if (fb != null) {
        fb.setValue((locomotiveFunctionEvent.isOn() ? 1 : 0));
      }
    }
  }

}
