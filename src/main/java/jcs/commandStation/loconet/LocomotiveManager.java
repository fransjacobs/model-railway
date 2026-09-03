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
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import static jcs.commandStation.loconet.Intellibox2Impl.COMMAND_STATION_ID;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
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

  LocomotiveManager(Intellibox2Impl intelliboxImpl) {
    this.intelliboxImpl = intelliboxImpl;
    locomotives = new HashMap<>();
    locomotiveAddresses = new HashMap<>();
    locomotiveSlots = new HashMap<>();
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

    for (LocomotiveBean loc : locomotiveList) {
      Long id = loc.getId();
      Integer address = loc.getAddress();

      locomotives.put(id, loc);
      locomotiveAddresses.put(address, id);

    }
    Logger.trace("There are {} locomotives.", locomotives.size());
  }

  //Workflow when a locomotive change is requeste is to check whether the locomotive has a slow.
  //when not try to obtain a slot.
  //may be obtain a slow for the locomotives whci are shown as there are engoug slots avalable
  void registerSlots() {
    for (LocomotiveBean locomotive : locomotives.values()) {
      if (locomotive.isShow()) {
        //obtain the slot
        int address = locomotive.getAddress();

        requestAddress(address);
      }
    }
  }

  void requestAddress(Integer address) {
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

  }

//  ; FORMAT = <OPC>,<ARG1>,<ARG2>,<CKSUM>
//;
//OPC_LOCO_ADR 0xBF ;REQ loco ADR ; <0xBF>,<0>,<ADR>,<CHK> REQ loco ADR
//;DATA return <E7>, is SLOT#,DATA that ADR was found in
//;IF ADR not found, MASTER puts ADR in FREE slot
//;and sends DATA/STATUS return <E7>......
//;IF no FREE slot,Fail LACK,0 is returned [<B4>,<3F>,<0>,<CHK>]
  void update(LoconetMessage message) {

  

  ///parse(message);
  }

  int getSize() {
    return this.size;
  }

  void parseSlotData(LoconetMessage message) {

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
