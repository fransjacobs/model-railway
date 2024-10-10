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

  /**
   * Test of getMessage method, of class EcosMessage.
   */
  //@Test
  public void testGetMessage() {
    System.out.println("getMessage");
    String tx = "get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion,commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix,updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus,railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)";
   
    EcosMessage instance = new EcosMessage(tx);
    String expResult = tx;
    String result = instance.getMessage();
    assertEquals(expResult, result);
    assertFalse(instance.isResponse());
    
    assertEquals(1, instance.getObjectId());
    assertEquals("get", instance.getCommand());
  }

  /**
   * Test of setResponse method, of class EcosMessage.
   */
  @Test
  public void testSetResponse() {
    System.out.println("setResponse");
    String tx = "get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion,commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix,updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus,railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)";
    String rx =  "<REPLY get(1,objectclass,view,listview,control,list,size,minarguments,protocolversion,commandstationtype,name,serialnumber,hardwareversion,applicationversion,applicationversionsuffix,updateonerror,status,status2,prog-status,m4-status,railcomplus-status,watchdog,railcom,railcomplus,railcomplus-range,railcomplus-mode,allowlocotakeover,stoponlastdisconnect)>";

    EcosMessage instance = new EcosMessage(tx);
    instance.setResponse(rx);
    
    assertTrue(instance.isResponse());
    
    assertEquals(1, instance.getObjectId());
    assertEquals("get", instance.getCommand());
  }

  /**
   * Test of hasResponse method, of class EcosMessage.
   */
  //@Test
  public void testHasResponse() {
    System.out.println("hasResponse");
    
    
    EcosMessage instance = null;
    boolean expResult = false;
    boolean result = instance.isResponse();
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
