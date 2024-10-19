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

  //@Test
  public void testGetMessage() {
    System.out.println("getMessage");
    String tx = "get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion,commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix,updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus,railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)";

    EcosMessage instance = new EcosMessage(tx);
    String expResult = tx;
    String result = instance.getMessage();
    assertEquals(expResult, result);
    assertFalse(instance.isResponseComplete());

    assertEquals(1, instance.getObjectId());
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

    Map<String, String> valueMap = instance.getValueMap();

    assertEquals(26, valueMap.size());

    assertEquals("0.5", valueMap.get("protocolversion"));
    assertEquals("64", valueMap.get("minarguments"));
    assertEquals("\"0x000480b5\"", valueMap.get("serialnumber"));
    assertEquals("4.2.13", valueMap.get("applicationversion"));

  }

  /**
   * Test of hasResponse method, of class EcosMessage.
   */
  //@Test
  public void testHasResponse() {
    System.out.println("hasResponse");

    EcosMessage instance = null;
    boolean expResult = false;
    boolean result = instance.isResponseComplete();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getObjectId method, of class EcosMessage.
   */
  //@Test
  public void testGetObjectId() {
    System.out.println("getObjectId");
    EcosMessage instance = null;
    int expResult = 0;
    int result = instance.getObjectId();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of isValid method, of class EcosMessage.
   */
  //@Test
  public void testIsValid() {
    System.out.println("isValid");
    EcosMessage instance = null;
    boolean expResult = false;
    boolean result = instance.isValid();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class EcosMessage.
   */
  //Test
  public void testToString() {
    System.out.println("toString");
    EcosMessage instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
