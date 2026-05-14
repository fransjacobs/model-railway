/*
 * Copyright 2024 frans.
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

import java.util.Map;
import jcs.entities.AccessoryBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author frans
 */
public class AccessoryManagerTest {

  public AccessoryManagerTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testGetSize() {
    System.out.println("getSize");

    String tx = "queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)";
    String rx = "<REPLY queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)>20000 name1[\"W1\"] name2[\"artikel\"] name3[\">0001<\"] addr[1] protocol[DCC] mode[SWITCH] symbol[1]20001 name1[\"W2\"] name2[\"artikel\"] name3[\">0001<\"] addr[2] protocol[DCC] mode[SWITCH] symbol[0]20002 name1[\"Sein\"] name2[\"2 aspect\"] name3[\">0001<\"] addr[10] protocol[MOT] mode[SWITCH] symbol[9]20003 name1[\"Sein 3\"] name2[\"3 aspect\"] name3[\">0012<\"] addr[12] protocol[MOT] mode[SWITCH] symbol[11]20004 name1[\"Sein 4\"] name2[\"4 aspect\"] name3[\">0001<\"] addr[14] protocol[MOT] mode[SWITCH] symbol[12]20005 name1[\"Sein mini\"] name2[\"artikel\"] name3[\">0001<\"] addr[16] protocol[MOT] mode[SWITCH] symbol[13]<END 0 (OK)>";

    EcosMessage query = new EcosMessage(tx);
    query.addResponse(rx);

    AccessoryManager instance = new AccessoryManager(null, query);
    int expResult = 6;
    int result = instance.getSize();
    assertEquals(expResult, result);
  }

  @Test
  public void testUpdate() {
    System.out.println("update");
    String tx = "queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)";
    String rx = "<REPLY queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)>20000 name1[\"W1\"] name2[\"artikel\"] name3[\">0001<\"] addr[1] protocol[DCC] mode[SWITCH] symbol[1]20001 name1[\"W2\"] name2[\"artikel\"] name3[\">0001<\"] addr[2] protocol[DCC] mode[SWITCH] symbol[0]20002 name1[\"Sein\"] name2[\"2 aspect\"] name3[\">0001<\"] addr[10] protocol[MOT] mode[SWITCH] symbol[9]20003 name1[\"Sein 3\"] name2[\"3 aspect\"] name3[\">0012<\"] addr[12] protocol[MOT] mode[SWITCH] symbol[11]20004 name1[\"Sein 4\"] name2[\"4 aspect\"] name3[\">0001<\"] addr[14] protocol[MOT] mode[SWITCH] symbol[12]20005 name1[\"Sein mini\"] name2[\"artikel\"] name3[\">0001<\"] addr[16] protocol[MOT] mode[SWITCH] symbol[13]<END 0 (OK)>";
    EcosMessage query = new EcosMessage(tx);
    query.addResponse(rx);
    AccessoryManager instance = new AccessoryManager(null, query);

    String getTx = "get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)";
    String getRx = "<REPLY get(20001,name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)>20001 name1[\"W2\"]20001 name2[\"artikel\"]20001 name3[\">0001<\"]20001 addr[2]20001 protocol[DCC]20001 mode[SWITCH]20001 symbol[0]20001 state[0]20001 addrext[2g,2r]20001 duration[250]20001 gates[2]20001 variant[0]20001 position[ok]20001 switching[0]<END 0 (OK)>";

    EcosMessage get = new EcosMessage(getTx);
    get.addResponse(getRx);
    instance.update(get);

    Map<String, AccessoryBean> am = instance.getAccessories();
    AccessoryBean a = am.get("20001");
    assertEquals(6, instance.getSize());
    assertNotNull(a);

    assertEquals("W2", a.getName());
    assertEquals(2, a.getStates());
    assertEquals(AccessoryBean.AccessoryValue.GREEN, a.getAccessoryValue());
  }

  @Test
  public void testUpdateManager() {
    System.out.println("updateManager");

    String tx = "queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)";
    String rx = "<REPLY queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)>20000 name1[\"W1\"] name2[\"artikel\"] name3[\">0001<\"] addr[1] protocol[DCC] mode[SWITCH] symbol[1]20001 name1[\"W2\"] name2[\"artikel\"] name3[\">0001<\"] addr[2] protocol[DCC] mode[SWITCH] symbol[0]20002 name1[\"Sein\"] name2[\"2 aspect\"] name3[\">0001<\"] addr[10] protocol[MOT] mode[SWITCH] symbol[9]20003 name1[\"Sein 3\"] name2[\"3 aspect\"] name3[\">0012<\"] addr[12] protocol[MOT] mode[SWITCH] symbol[11]20004 name1[\"Sein 4\"] name2[\"4 aspect\"] name3[\">0001<\"] addr[14] protocol[MOT] mode[SWITCH] symbol[12]20005 name1[\"Sein mini\"] name2[\"artikel\"] name3[\">0001<\"] addr[16] protocol[MOT] mode[SWITCH] symbol[13]<END 0 (OK)>";
    EcosMessage query = new EcosMessage(tx);
    query.addResponse(rx);
    AccessoryManager instance = new AccessoryManager(null, query);

    EcosMessage event = new EcosMessage("<EVENT 11>11 msg[LIST_CHANGED]20006 appended11 size[7]<END 0 (OK)");

    String id = instance.updateManager(event);

    assertEquals("20006", id);
  }

  //<EVENT 11>11 msg[LIST_CHANGED]20006 appended11 size[7]<END 0 (OK)>
//  @Test
  public void testGetAccessories() {
    System.out.println("getAccessories");
    AccessoryManager instance = null;
    Map<String, AccessoryBean> expResult = null;
    Map<String, AccessoryBean> result = instance.getAccessories();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }
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
//Curverd or red
//get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20000 name1["W1"]20000 name2["artikel"]20000 name3[">0001<"]20000 addr[1]20000 protocol[DCC]20000 mode[SWITCH]20000 symbol[1]
//20000 state[1]20000 type[ACCESSORY]20000 addrext[1g,1r]20000 duration[250]20000 gates[2]20000 variant[0]20000 position[ok]20000 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//TX:get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)
//TRACE	2024-12-18 21:02:11.962 [ESU-ECOS-RX] EcosTCPConnection$ClientMessageReceiver.run(): RX: <REPLY get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//Straight green
//      get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20000 name1["W1"]20000 name2["artikel"]20000 name3[">0001<"]20000 addr[1]20000 protocol[DCC]20000 mode[SWITCH]20000 symbol[1]
//20000 state[0]20000 type[ACCESSORY]20000 addrext[1g,1r]20000 duration[250]20000 gates[2]20000 variant[0]20000 position[ok]20000 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//
//get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)
//<REPLY get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20001 name1["W2"]20001 name2["artikel"]20001 name3[">0001<"]20001 addr[2]20001 protocol[DCC]20001 mode[SWITCH]20001 symbol[0]
//20001 state[0]20001 type[ACCESSORY]20001 addrext[2g,2r]20001 duration[250]20001 gates[2]20001 variant[0]20001 position[ok]20001 switching[0]
//  <END 0 (OK, but obsolete attribute at 64)>
//state  kennelijk is 1 rood en 0 groen. hoe werkt een sein?
//<REPLY queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)>
//20000 name1["W1"] name2["artikel"] name3[">0001<"] addr[1] protocol[DCC] mode[SWITCH] symbol[1]
//20001 name1["W2"] name2["artikel"] name3[">0001<"] addr[2] protocol[DCC] mode[SWITCH] symbol[0]
//20002 name1["Sein"] name2["2 aspect"] name3[">0001<"] addr[10] protocol[MOT] mode[SWITCH] symbol[9]
//20003 name1["Sein 3"] name2["3 aspect"] name3[">0012<"] addr[12] protocol[MOT] mode[SWITCH] symbol[11]
//20004 name1["Sein 4"] name2["4 aspect"] name3[">0001<"] addr[14] protocol[MOT] mode[SWITCH] symbol[12]
//20005 name1["Sein mini"] name2["artikel"] name3[">0001<"] addr[16] protocol[MOT] mode[SWITCH] symbol[13]
// <END 0 (OK)>
//type is niet nodig
//get(20002, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20002, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20002 name1["Sein"]20002 name2["2 aspect"]20002 name3[">0001<"]20002 addr[10]20002 protocol[MM]20002 mode[SWITCH]20002 symbol[9]20002 
//state[0]20002 type[ACCESSORY]20002 addrext[10g,10r]20002 duration[250]20002 gates[2]20002 variant[0]20002 position[ok]20002 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//get(20003, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)
//<REPLY get(20003, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20003 name1["Sein 3"]20003 name2["3 aspect"]20003 name3[">0012<"]20003 addr[12]20003 protocol[MM]20003 mode[SWITCH]20003 symbol[11]20003
//state[0]20003 type[ACCESSORY]20003 addrext[12g,12r,13g]20003 duration[250]20003 gates[3]20003 variant[0]20003 position[ok]20003 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//get(20004, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20004, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20004 name1["Sein 4"]20004 name2["4 aspect"]20004 name3[">0001<"]20004 addr[14]20004 protocol[MM]20004 mode[SWITCH]20004 symbol[12]
//20004 state[0]20004 type[ACCESSORY]20004 addrext[14g,14r,15g,15r]20004 duration[250]20004 gates[4]20004 variant[0]20004 position[ok]20004 switching[0]
//<END 0 (OK, but obsolete attribute at 64)>
//get(20005, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching) ->
//<REPLY get(20005, name1,name2,name3, addr, protocol,mode,symbol,state,type,addrext,duration,gates,variant,position,switching)>
//20005 name1["Sein mini"]20005 name2["artikel"]20005 name3[">0001<"]20005 addr[16]20005 protocol[MM]20005 mode[SWITCH]20005 symbol[13]20005
//state[0]20005 type[ACCESSORY]20005 addrext[16g,16r]20005 duration[500]20005 gates[2]20005 variant[0]20005 position[ok]20005 switching[0]
//  <END 0 (OK, but obsolete attribute at 64)>


////
//<EVENT 20000>20000 switching[1]<END 0 (OK)>
//<EVENT 20000>20000 state[0]<END 0 (OK)>
//<EVENT 20000>20000 switching[1]<END 0 (OK)>
//<EVENT 20000>20000 state[1]<END 0 (OK)>
/*
<REPLY queryObjects(11,name1,name2,name3,addr,addrext,protocol,mode,symbol)>
20000 name1["W1"] name2["artikel"] name3[">0001<"] addrext[1g,1r] addr[1] protocol[DCC] mode[SWITCH] symbol[1]
20001 name1["W2"] name2["artikel"] name3[">0001<"] addrext[2g,2r] addr[2] protocol[DCC] mode[SWITCH] symbol[0]
20002 name1["Sein"] name2["2 aspect"] name3[">0001<"] addrext[9g,9r] addr[9] protocol[MOT] mode[SWITCH] symbol[9]
20003 name1["Sein 3"] name2["3 aspect"] name3[">0012<"] addrext[11g,11r,12g] addr[11] protocol[MOT] mode[SWITCH] symbol[11]
20004 name1["Sein 4"] name2["4 aspect"] name3[">0001<"] addrext[13g,13r,14g,14r] addr[13] protocol[MOT] mode[SWITCH] symbol[12]
20005 name1["Sein mini"] name2["artikel"] name3[">0001<"] addrext[18g,18r] addr[18] protocol[MOT] mode[SWITCH] symbol[13]
20006 name1["Symbol"] name2["artikel"] name3[">0001<"] addrext[5g,5r] addr[5] protocol[DCC] mode[SWITCH] symbol[9]
20007 name1["viesmann"] name2["artikel"] name3[">0001<"] addrext[1g,1r] addr[1] protocol[MOT] mode[SWITCH] symbol[3]
20008 name1["3-weg-wissel"] name2["artikel"] name3[">0001<"] addrext[25g,25r,26g,26r] addr[25] protocol[DCC] mode[SWITCH] symbol[2]
<END 0 (OK)>
*/

/*
INFO	2026-05-14 15:39:30.577 [main] EcosMessage.getValueMap(): C->
20000 name1["W1"]
 name2["artikel"]
 name3[">0001<"]
 addrext[1g,1r]
 addr[1]
 protocol[DCC]
 mode[SWITCH]
 symbol[1]

20001 name1["W2"]
 name2["artikel"]
 name3[">0001<"]
 addrext[2g,2r]
 addr[2]
 protocol[DCC]
 mode[SWITCH]
 symbol[0]

20002 name1["Sein"]
 name2["2 aspect"]
 name3[">0001<"]
 addrext[9g,9r]
 addr[9]
 protocol[MOT]
 mode[SWITCH]
 symbol[9]

20003 name1["Sein 3"]
 name2["3 aspect"]
 name3[">0012<"]
 addrext[11g,11r,12g]
 addr[11]
 protocol[MOT]
 mode[SWITCH]
 symbol[11]

20004 name1["Sein 4"]
 name2["4 aspect"]
 name3[">0001<"]
 addrext[13g,13r,14g,14r]
 addr[13]
 protocol[MOT]
 mode[SWITCH]
 symbol[12]

20005 name1["Sein mini"]
 name2["artikel"]
 name3[">0001<"]
 addrext[18g,18r]
 addr[18]
 protocol[MOT]
 mode[SWITCH]
 symbol[13]

20006 name1["Symbol"]
 name2["artikel"]
 name3[">0001<"]
 addrext[5g,5r]
 addr[5]
 protocol[DCC]
 mode[SWITCH]
 symbol[9]

20007 name1["viesmann"]
 name2["artikel"]
 name3[">0001<"]
 addrext[1g,1r]
 addr[1]
 protocol[MOT]
 mode[SWITCH]
 symbol[3]

20008 name1["3-weg-wissel"]
 name2["artikel"]
 name3[">0001<"]
 addrext[25g,25r,26g,26r]
 addr[25]
 protocol[DCC]
 mode[SWITCH]
 symbol[2]



*/