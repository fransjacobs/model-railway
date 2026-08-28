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
    LoconetMessage obtainSlot = LoconetMessageFactory.requestLocoAddress(address);

    this.intelliboxImpl.loconet.sendMessage(obtainSlot);
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
