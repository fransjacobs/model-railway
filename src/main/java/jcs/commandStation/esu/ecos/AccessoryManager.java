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

/**
 *
 * AccessoryManager (id=11)
 */
class AccessoryManager {
  
  public static final int ID = 11;
  public static final int ACCESSORY_OFFSET = 20000;
  
   private int size;
   private final EsuEcosCommandStationImpl ecosCommandStation;
   
   AccessoryManager(EsuEcosCommandStationImpl ecosCommandStation, EcosMessage message) {
     this.ecosCommandStation = ecosCommandStation;
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

  
  
  
}
