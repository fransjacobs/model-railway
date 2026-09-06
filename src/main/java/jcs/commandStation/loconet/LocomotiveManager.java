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

  void changeVelocity(int address, int speed, LocomotiveBean.Direction direction) {
    if (locomotiveAddresses.containsKey(address)) {
      Long id = locomotiveAddresses.get(address);
      LocomotiveBean locomotive = locomotives.get(id);

      locomotive.setDirection(direction);
      int slot = locomotiveSlotsReverse.get(id);

      LoconetMessage tx = LoconetMessageFactory.changeLocomotiveSpeed(slot, speed);
      intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(tx);

    }

  }

  void changeDirection(int address, Direction direction) {
    if (locomotiveAddresses.containsKey(address)) {
      Long id = locomotiveAddresses.get(address);
      LocomotiveBean locomotive = locomotives.get(id);

      locomotive.setDirection(direction);
      int slot = locomotiveSlotsReverse.get(id);
      Map<Integer, FunctionBean> functionValues = locomotive.getFunctions();
      boolean f0 = functionValues.get(0).isOn();
      boolean f1 = functionValues.get(1).isOn();
      boolean f2 = functionValues.get(2).isOn();
      boolean f3 = functionValues.get(3).isOn();
      boolean f4 = functionValues.get(4).isOn();

      LoconetMessage tx = LoconetMessageFactory.setDirectionAndFunctions(slot, direction, f0, f1, f2, f3, f4);
      intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(tx);
    }
  }

  void changeFunctionValue(int address, int functionNumber, boolean flag) {
    if (locomotiveAddresses.containsKey(address)) {
      Long id = locomotiveAddresses.get(address);
      LocomotiveBean locomotive = locomotives.get(id);

      locomotive.setFunctionValue(functionNumber, flag);
      Direction dir = locomotive.getDirection();
      int slot = locomotiveSlotsReverse.get(id);
      if (functionNumber < 5) {
        Map<Integer, FunctionBean> functionValues = locomotive.getFunctions();
        boolean f0 = functionValues.get(0).isOn();
        boolean f1 = functionValues.get(1).isOn();
        boolean f2 = functionValues.get(2).isOn();
        boolean f3 = functionValues.get(3).isOn();
        boolean f4 = functionValues.get(4).isOn();

        LoconetMessage tx = LoconetMessageFactory.setDirectionAndFunctions(slot, dir, f0, f1, f2, f3, f4);
        intelliboxImpl.loconet.sendMessageNoWaitConsumeEcho(tx);
      }

    }

  }

  //Workflow when a locomotive change is requeste is to check whether the locomotive has a slow.
  //when not try to obtain a slot.
  //may be obtain a slow for the locomotives whci are shown as there are engoug slots avalable
  void registerSlots() {
    for (LocomotiveBean locomotive : locomotives.values()) {
      if (locomotive.isShow()) {
        //obtain the slot
        requestSlotData(locomotive);
        Logger.debug("Slot# {} Locomotive {}, address: {}, Id: {}", locomotiveSlotsReverse.get(locomotive.getId()), locomotive.getName(), locomotive.getAddress(), locomotive.getId());
      }
    }
  }

  void requestSlotData(LocomotiveBean locomotive
  ) {
    if (locomotive.getAddress() == null) {
      return;
    }
    int address = locomotive.getAddress();
    LoconetMessage request = LoconetMessageFactory.requestLocoAddress(address);
    LoconetMessage reply = intelliboxImpl.loconet.sendMessageAwaitEchoAndReply(request, LoconetMessageParser.replyForLocoAddressRequest(request), 500);

    if (reply == null) {
      Logger.warn("No slot reply received for locomotive address {}", address);
      return;
    }

    if (reply.isExpectedsOpcode(LoconetMessage.OPC_SL_RD_DATA)) {
      // Happy flow: parse slot data and continue.
      Logger.trace("Received slot data: {}", reply);
    }

    if (reply.isExpectedsOpcode(LoconetMessage.OPC_LONG_ACK)) {
      // Failure flow: parse ACK1.
      Logger.warn("Locomotive address request failed: {}", reply);
    }

    parseSlotData(reply, locomotive);

    //Persist changes
  }

//  ; FORMAT = <OPC>,<ARG1>,<ARG2>,<CKSUM>
//;
//OPC_LOCO_ADR 0xBF ;REQ loco ADR ; <0xBF>,<0>,<ADR>,<CHK> REQ loco ADR
//;DATA return <E7>, is SLOT#,DATA that ADR was found in
//;IF ADR not found, MASTER puts ADR in FREE slot
//;and sends DATA/STATUS return <E7>......
//;IF no FREE slot,Fail LACK,0 is returned [<B4>,<3F>,<0>,<CHK>]
  void update(LoconetMessage message
  ) {

  

  ///parse(message);
  }

  int getSize() {
    return this.size;
  }

  LocomotiveBean parseSlotData(LoconetMessage message, LocomotiveBean locomotive
  ) {

//TX: 0xbf 0x00 0x48 0x08
//RX: 0xbf 0x00 0x48 0x08
//RX echo consumed: 0xbf 0x00 0x48 0x08
//slotread
//RX: 0xe7 0x0e 0x08 0x32 0x48 0x00 0x30 0x07 0x00 0x00 0x00 0x00 0x00 0x53
//
//
//  OPC_SL_RD_DATA 0xE7 ;SLOT DATA return, 10 bytes NO
// <0xE7>  -> OPC_SL_RD_DATA
// ,<0E>   -> len totaal 14 bytes
//  <SLOT#> -> slot nr "8"  SLOT NUMBER: ;0-7FH, 0 is special SLOT, 070H-07FH DIGITRAX reserved.
//  ,<STAT> 1) SLOT STATUS1:
//          2) SLOT LOCO ADR: 
//          3) SLOT SPEED: 
//   D7-SL_SPURGE 
//   D6-SL_CONUP ;1=SLOT purge en, ALSO adrSEL (INTERNAL use only) //; (not seen on NET!)
//                   CONDN/CONUP: bit encoding-Control double linked Consist List
//                11=LOGICAL MID CONSIST , Linked up AND down
//                10=LOGICAL CONSIST TOP, Only linked downwards
//                01=LOGICAL CONSIST SUB-MEMBER, Only linked upwards
//                00=FREE locomotive, no CONSIST indirection/linking number is now SLOT adr of SPD/DIR and STATUS of consist. i.e. is an Indirect pointer.
//                           This Slot has same BUSY/ACTIVE bits as TOP of Consist.
//                           TOP is loco with SPD/DIR for whole consist. (top of list). BUSY/ACTIVE: bit encoding for SLOT activity
//   D5-SL_BUSY   ;11=IN_USE loco adr in SLOT -REFRESHED
//   D4-SL_ACTIVE ;10=IDLE loco adr in SLOT -NOT refreshed ;
//                 01=COMMON loco adr IN SLOT -refreshed
//                 00=FREE SLOT, no valid DATA -not refreshed
//   D3-SL_CONDN ; shows other SLOT Consist linked INTO this slot,see SL_CONUP
//   D2-SL_SPDEX ; 3 BITS for Decoder TYPE encoding for this SLOT
//   D1-SL_SPD14 ;011=send 128 speed mode packets
//   D0-SL_SPD28 ;010=14 step MODE
//               ;001=28 step. Generate Trinary packets for this Mobile ADR
//               ;000=28 step/ 3 BYTE PKT regular mode
//               ;111=128 Step decoder, Allow Advanced DCC consisting
//               ;100=28 Step decoder ,Allow Advanced DCC consisting
//
//                  7|6|5|4|3|2|1|0
//    waarde 0x32:  0 0 1 1 0 0 2 0
//    14 STEP MODE in use refreshed data valid
//
//  ,<ADR> address low 0x48 -> 72
//  ,<SPD> sprre = 0
//  ,<DIRF> 
//           D7-0 ;always 0
//           D6-SL_XCNT ; reserved , set 0
//           D5-SL_DIR ;1=loco direction FORWARD
//           D4-SL_F0 ;1=Directional lighting ON
//           D3-SL_F4 ;1=F4 ON
//           D2-SL_F3 ;1=F3 ON
//           D1-SL_F2 ;1=F2 ON
//           D0-SL_F1 ;1=F1 ON
//  waarde 0x30   7|6|5|4|3|2|1|0
//                0 0 1 1 0 0 0 0   -> forwards F0 on F1..4 off
//  ,<TRK>  (GLOBAL system /track status)
//         D7-D4 Reserved
//         D3 GTRK_PROG_BUSY 1=Programming TRACK in this Master is BUSY.
//         D2 GTRK_MLOK1 , 1=This Master IMPLEMENTS LocoNet 1.1 capability
//                       , 0=Master is DT200
//         D1 GTRK_IDLE ; 0=TRACK is PAUSED, B'cast EMERG STOP.
//         D0 GTRK_POWER ; 1=DCC packets are ON in MASTER, Global POWER up
// waarde 0x07 7|6|5|4|3|2|1|0
//             0 0 0 0 0 1 1 1 ->master LN 1.1, Power On DCC is on
//  ,<SS2> SLOT STATUS2:
//   D3- 1=expansion IN ID1/2, 0=ENCODED alias
//   D2- 1=Expansion ID1/2 is NOT ID usage
//   D0- 1=this slot has SUPPRESSED ADV consist-
// waarde 0x00 
//  ,<ADR2>  high address byte
//  ,<SND>  0x00 
//SLOT SOUND: Slot sound/ Accesory Function mode II packets. F5-F8 (byte also sent as ARG2 in SND opcode)
//     D7-D4 reserved
//     D3-SL_SND4/F8
//     D2-SL_SND3/F7
//     D1-SL_SND2/F6
//     D0-SL_SND1/F5 ;1= SLOT Sound 1 function 1active (accessory 2)
//  ,<ID1>,<ID2> uid 0x00 not in use
//  ,<CHK>  
//; SLOT DATA READ, 10 bytes data /14 byte MSG
//;NOTE; If STAT2.2=0 EX1/EX2 encodes an ID#,[if STAT2.2=1 the STAT.3=0 means EX1/EX2 are ALIAS]
//;ID1/ID2 are two 7 bit values encoding a 14 bit unique DEVICE usage ID. ID1/ID2#'s 00/00 -means NO ID being used
//; 01/00 to 7F/01 -ID shows PC usage.Lo nibble is TYP PC# (PC can use hi values)
//; 00/02 to 7F/03 -SYSTEM reserved
//; 00/04 to 7F/7E -NORMAL throttle RANGE
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
    locomotive.setDirection(direction);

    updateSlotFunctions(locomotive, dirf, sound);

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
  //OPC_SLOT_STAT1 0xB5 ;WRITE slot stat1 ; <0xB5>,<SLOT>,<STAT1>,<CHK> WRITE stat1
  //OPC_RQ_SL_DATA 0xBB ;Request SLOT DATA/status block YES <E7>SLOT READ; <0xBB>,<SLOT>,<0>,<CHK> Request SLOT DATA/status block
  //OPC_WR_SL_DATA 0xEF ;WRITE SLOT DATA, 10 bytes YES LACK ; <0xEF>,<0E>,<SLOT#>,<STAT>,<ADR>,<SPD>,<DIRF>,<TRK> ;<SS2>,<ADR2>,<SND>,<ID1>,<ID2>,<CHK>
  //; SLOT DATA WRITE, 10 bytes data /14 byte MSG
  //OPC_SL_RD_DATA 0xE7 ;SLOT DATA return, 10 bytes NO
  //; <0xE7>,<0E>,<SLOT#>,<STAT>,<ADR>,<SPD>,<DIRF>,<TRK>
  //;<SS2>,<ADR2>,<SND>,<ID1>,<ID2>,<CHK>
  //; SLOT DATA READ, 10 bytes data /14 byte MSG
  //;NOTE; If STAT2.2=0 EX1/EX2 encodes an ID#,[if STAT2.2=1 the STAT.3=0 means EX1/EX2 are ALIAS]
  //;ID1/ID2 are two 7 bit values encoding a 14 bit unique DEVICE usage ID
  //;ID1/ID2#'s 00/00 -means NO ID being used
  // ; 01/00 to 7F/01 -ID shows PC usage.Lo nibble is TYP PC# (PC can use hi values)
  //; 00/02 to 7F/03 -SYSTEM reserved
  //; 00/04 to 7F/7E -NORMAL throttle RANGE
  //OPC_LOCO_DIRF 0xA1 ;SET SLOT dir,F0-4 state NO

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

  private void updateSlotFunctions(LocomotiveBean locomotive, int dirf, int sound) {
    if (!locomotive.hasFunction(0)) {
      locomotive.addFunction(new FunctionBean(0, locomotive.getId()));
    }
    locomotive.setFunctionValue(0, (dirf & 0x10) != 0);

    if (!locomotive.hasFunction(1)) {
      locomotive.addFunction(new FunctionBean(1, locomotive.getId()));
    }
    locomotive.setFunctionValue(1, (dirf & 0x01) != 0);

    if (!locomotive.hasFunction(2)) {
      locomotive.addFunction(new FunctionBean(2, locomotive.getId()));
    }
    locomotive.setFunctionValue(2, (dirf & 0x02) != 0);

    if (!locomotive.hasFunction(3)) {
      locomotive.addFunction(new FunctionBean(3, locomotive.getId()));
    }
    locomotive.setFunctionValue(3, (dirf & 0x04) != 0);

    if (!locomotive.hasFunction(4)) {
      locomotive.addFunction(new FunctionBean(4, locomotive.getId()));
    }
    locomotive.setFunctionValue(4, (dirf & 0x08) != 0);

    if (!locomotive.hasFunction(5)) {
      locomotive.addFunction(new FunctionBean(5, locomotive.getId()));
    }
    locomotive.setFunctionValue(5, (sound & 0x01) != 0);

    if (!locomotive.hasFunction(6)) {
      locomotive.addFunction(new FunctionBean(6, locomotive.getId()));
    }
    locomotive.setFunctionValue(6, (sound & 0x02) != 0);

    if (!locomotive.hasFunction(7)) {
      locomotive.addFunction(new FunctionBean(7, locomotive.getId()));
    }
    locomotive.setFunctionValue(7, (sound & 0x04) != 0);

    if (!locomotive.hasFunction(8)) {
      locomotive.addFunction(new FunctionBean(8, locomotive.getId()));
    }
    locomotive.setFunctionValue(8, (sound & 0x08) != 0);
  }

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
