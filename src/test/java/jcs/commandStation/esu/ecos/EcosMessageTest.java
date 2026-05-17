/*
 * Copyright 2024 fransjacobs.
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fransjacobs
 */
public class EcosMessageTest {

  public EcosMessageTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testGetMessage() {
    System.out.println("getMessage");
    String tx = "get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion,commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix,updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus,railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)";

    EcosMessage instance = new EcosMessage(tx);
    String expResult = tx;
    String result = instance.getMessage();
    assertEquals(expResult, result);
    assertFalse(instance.isResponseComplete());

    assertEquals(1, instance.getObjectId());

    String cmd = instance.getCommand();
    assertEquals("get", instance.getCommand());
  }

  /**
   * Test of addResponse method, of class EcosMessage.
   */
  @Test
  public void testSetResponse() {
    System.out.println("setResponse");
    String tx = "get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion,commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix,updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus,railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)";

    String rx = "<REPLY get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion,commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix,updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus,railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)>1 objectclass[model]1 view[none]1 listview[none]1 control[none]1 list1 size[10]1 minarguments[64]1 protocolversion[0.5]1 commandstationtype[\"ECoS\"]1 name[\"ECoS-50000\"]1 serialnumber[\"0x000480b5\"]1 hardwareversion[1.3]1 applicationversion[4.2.13]1 applicationversionsuffix[\"\"]1 updateonerror[0]1 status[GO]1 status2[ALL]1 prog-status[0]1 m4-status[none]1 railcomplus-status[none]1 watchdog[0,0]1 railcom[1]1 railcomplus[1]1 railcomplus-range[1000]1 railcomplus-mode[manual]1 allowlocotakeover[1]1 stoponlastdisconnect[0]<END 0 (OK)>";
    String content = "1 objectclass[model]1 view[none]1 listview[none]1 control[none]1 list1 size[10]1 minarguments[64]1 protocolversion[0.5]1 commandstationtype[\"ECoS\"]1 name[\"ECoS-50000\"]1 serialnumber[\"0x000480b5\"]1 hardwareversion[1.3]1 applicationversion[4.2.13]1 applicationversionsuffix[\"\"]1 updateonerror[0]1 status[GO]1 status2[ALL]1 prog-status[0]1 m4-status[none]1 railcomplus-status[none]1 watchdog[0,0]1 railcom[1]1 railcomplus[1]1 railcomplus-range[1000]1 railcomplus-mode[manual]1 allowlocotakeover[1]1 stoponlastdisconnect[0]";

    EcosMessage instance = new EcosMessage(tx);
    instance.addResponse(rx);

    assertTrue(instance.isResponseComplete());

    assertEquals(1, instance.getObjectId());
    assertEquals("get", instance.getCommand());
    assertEquals(0, instance.getErrorCode());
    assertEquals("OK", instance.getResponseCode());
    assertEquals(content, instance.getResponseContent());

    Map<String, Object> valueMap = instance.getValueMap();

    assertEquals(27, valueMap.size());

    assertEquals("1", valueMap.get("id"));
    assertEquals("0.5", valueMap.get("protocolversion"));
    assertEquals("64", valueMap.get("minarguments"));
    assertEquals("0x000480b5", valueMap.get("serialnumber"));
    assertEquals("4.2.13", valueMap.get("applicationversion"));
  }

  /**
   * Test of hasResponse method, of class EcosMessage.
   */
  @Test
  public void testEvent() {
    System.out.println("Event");

    String rx = "<EVENT 1>1 status2[NONE]1 status[STOP]<END 0 (OK)>";
    String content = "1 status2[NONE]1 status[STOP]";
    EcosMessage instance = new EcosMessage(rx);

    assertTrue(instance.isResponseComplete());
    assertTrue(instance.isEvent());
    assertEquals(1, instance.getObjectId());

    assertEquals(0, instance.getErrorCode());
    assertEquals("OK", instance.getResponseCode());
    assertEquals(content, instance.getResponseContent());

    Map<String, Object> valueMap = instance.getValueMap();
    assertEquals(3, valueMap.size());

    assertEquals("1", valueMap.get("id"));
    assertEquals("NONE", valueMap.get("status2"));
    assertEquals("STOP", valueMap.get("status"));

  }

  /**
   * Test of getObjectId method, of class EcosMessage.
   */
  @Test
  public void testReplyWithMultipleIds() {
    System.out.println("replyWithMultipleIds");
    String tx = "queryObjects(10, name, addr, protocol)";
    String rx = "<REPLY queryObjects(10, name, addr, protocol)>\n1002 name[\"FS236-002\"] addr[14] protocol[DCC28]\n1003 name[\"NS 6505\"] addr[8] protocol[DCC28]\n1000 name[\"193 304-3 DB AG\"] addr[0] protocol[MFX]\n1001 name[\"SNCB/NMBS HLE 27\"] addr[3] protocol[DCC28]\n1004 name[\"NS 1205\"] addr[11] protocol[DCC28]\n1005 name[\"DB-141-015-8\"] addr[12] protocol[MM14]\n1006 name[\"NS 1139\"] addr[13] protocol[DCC128]\n1007 name[\"NS 1139\"] addr[5] protocol[DCC14]\n1008 name[\"NS 1309\"] addr[68] protocol[DCC28]<END 0 (OK)>\n";
    String content = "\n1002 name[\"FS236-002\"] addr[14] protocol[DCC28]\n1003 name[\"NS 6505\"] addr[8] protocol[DCC28]\n1000 name[\"193 304-3 DB AG\"] addr[0] protocol[MFX]\n1001 name[\"SNCB/NMBS HLE 27\"] addr[3] protocol[DCC28]\n1004 name[\"NS 1205\"] addr[11] protocol[DCC28]\n1005 name[\"DB-141-015-8\"] addr[12] protocol[MM14]\n1006 name[\"NS 1139\"] addr[13] protocol[DCC128]\n1007 name[\"NS 1139\"] addr[5] protocol[DCC14]\n1008 name[\"NS 1309\"] addr[68] protocol[DCC28]";

    EcosMessage instance = new EcosMessage(tx);
    instance.addResponse(rx);

    assertTrue(instance.isResponseComplete());
    assertFalse(instance.isEvent());
    assertEquals(10, instance.getObjectId());
    assertEquals(0, instance.getErrorCode());
    assertEquals("OK", instance.getResponseCode());
    assertEquals(content, instance.getResponseContent());

    Map<String, Object> valueMap = instance.getValueMap();
    assertEquals(9, valueMap.size());

    Map<String, String> valueValueMap = (Map<String, String>) valueMap.get("1000");
    assertEquals(4, valueValueMap.size());

    assertEquals("1000", valueValueMap.get("id"));
    assertEquals("193 304-3 DB AG", valueValueMap.get("name"));
    assertEquals("0", valueValueMap.get("addr"));
    assertEquals("MFX", valueValueMap.get("protocol"));

    valueValueMap = (Map<String, String>) valueMap.get("1002");
    assertEquals(4, valueValueMap.size());

    assertEquals("1002", valueValueMap.get("id"));
    assertEquals("FS236-002", valueValueMap.get("name"));
    assertEquals("14", valueValueMap.get("addr"));
    assertEquals("DCC28", valueValueMap.get("protocol"));

    valueValueMap = (Map<String, String>) valueMap.get("1005");
    assertEquals(4, valueValueMap.size());

    assertEquals("1005", valueValueMap.get("id"));
    assertEquals("DB-141-015-8", valueValueMap.get("name"));
    assertEquals("12", valueValueMap.get("addr"));
    assertEquals("MM14", valueValueMap.get("protocol"));
  }

  @Test
  public void testLocomotiveFunctions() {
    System.out.println("locomotiveFunctions");

    String tx = "get(1001,name,addr,protocol,dir,speed,speedstep,active,locodesc,func,funcdesc)";
    String rx = "<REPLY get(1001,name,addr,protocol,dir,speed,speedstep,active,locodesc,func,funcdesc)>\n1001 name[\"SNCB/NMBS HLE 27\"]\n1001 addr[3]\n1001 protocol[DCC128]\n1001 dir[0]\n1001 speed[0]\n1001 speedstep[0]\n1001 active[1]\n1001 locodesc[LOCO_TYPE_E,IMAGE_TYPE_INT,2]\n1001 func[0,0]\n1001 func[1,0]\n1001 func[2,0]\n1001 func[3,0]\n1001 func[4,0]\n1001 func[5,0]\n1001 func[6,0]\n1001 func[7,0]\n1001 func[8,0]\n1001 func[9,0]\n1001 func[10,0]\n1001 func[11,0]\n1001 func[12,0]\n1001 func[13,0]\n1001 func[14,0]\n1001 func[15,0]\n1001 func[16,0]\n1001 func[17,0]\n1001 func[18,0]\n1001 func[19,0]\n1001 func[20,0]\n1001 func[21,0]\n1001 func[22,0]\n1001 func[23,0]\n1001 func[24,0]\n1001 func[25,0]\n1001 func[26,0]\n1001 func[27,0]\n1001 func[28,0]\n1001 func[29,0]\n1001 func[30,0]\n1001 funcdesc[0,3]\n1001 funcdesc[1,7]\n1001 funcdesc[2,37,moment]\n1001 funcdesc[3,37,moment]\n1001 funcdesc[4,34]\n1001 funcdesc[5,260]\n1001 funcdesc[6,10]\n1001 funcdesc[7,4]\n1001 funcdesc[8,5]\n1001 funcdesc[9,3]\n1001 funcdesc[10,7]\n1001 funcdesc[11,1287]\n1001 funcdesc[12,12039]\n1001 funcdesc[13,2055,moment]\n1001 funcdesc[14,9,moment]\n1001 funcdesc[15,40]1001 funcdesc[16,39]1001 funcdesc[17,12039]\n1001 funcdesc[18,9,moment]\n1001 funcdesc[19,11527,moment]\n1001 funcdesc[20,11015]\n1001 funcdesc[21,8,moment]\n1001 funcdesc[22,9,moment]\n1001 funcdesc[23,1033,moment]\n1001 funcdesc[24,809]\n1001 funcdesc[25,11783,moment]\n1001 funcdesc[26,300]\n1001 funcdesc[27,263]\n1001 funcdesc[28,12039]\n1001 funcdesc[29,1033,moment]\n1001 funcdesc[30,1033,moment]\n<END 0 (OK)>\n";

    String expFunc = "[0,0],[1,0],[2,0],[3,0],[4,0],[5,0],[6,0],[7,0],[8,0],[9,0],[10,0],[11,0],[12,0],[13,0],[14,0],[15,0],[16,0],[17,0],[18,0],[19,0],[20,0],[21,0],[22,0],[23,0],[24,0],[25,0],[26,0],[27,0],[28,0],[29,0],[30,0]";

    String expFuncDesc = "[0,3],[1,7],[2,37,moment],[3,37,moment],[4,34],[5,260],[6,10],[7,4],[8,5],[9,3],[10,7],[11,1287],[12,12039],[13,2055,moment],[14,9,moment],[15,40],[16,39],[17,12039],[18,9,moment],[19,11527,moment],[20,11015],[21,8,moment],[22,9,moment],[23,1033,moment],[24,809],[25,11783,moment],[26,300],[27,263],[28,12039],[29,1033,moment],[30,1033,moment]";

    EcosMessage instance = new EcosMessage(tx);
    instance.addResponse(rx);

    assertTrue(instance.isResponseComplete());
    assertFalse(instance.isEvent());
    assertEquals(1001, instance.getObjectId());
    assertEquals(0, instance.getErrorCode());
    assertEquals("OK", instance.getResponseCode());

    Map<String, Object> valueMap = instance.getValueMap();
    assertEquals(11, valueMap.size());

    assertEquals("1001", valueMap.get("id"));

    assertEquals("DCC128", valueMap.get("protocol"));

    assertEquals(expFunc, valueMap.get("func"));
    assertEquals(expFuncDesc, valueMap.get("funcdesc"));

  }

  @Test
  public void testGetId() {
    System.out.println("getId");
    EcosMessage instance = new EcosMessage("get(11, name1,name2,name3,addr,protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)");
    String expResult = "11";
    String result = instance.getId();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetValueMapForSwitchEvent() {
    System.out.println("getValueMapForSwitchEvent");
    String rx = "<EVENT 20000>\n20000 state[1]\n<END 0 (OK)>\n";
    EcosMessage instance = new EcosMessage(rx);

    assertTrue(instance.isEvent());

    Map<String, Object> valMap = instance.getValueMap();
    assertTrue(valMap.containsKey(Ecos.ID));
    assertTrue(valMap.containsKey(Ecos.STATE));

    assertEquals("20000", valMap.get(Ecos.ID));
    assertEquals("1", valMap.get(Ecos.STATE));

  }

}
