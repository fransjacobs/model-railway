/*
 * Copyright 2025 Frans Jacobs
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
package jcs.entities;

import java.util.List;
import jcs.commandStation.events.SensorEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author frans
 */
public class FeedbackModuleBeanTest {

  public FeedbackModuleBeanTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testGetId() {
    System.out.println("getId");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    Integer expResult = 0;

    Integer result = instance.getId();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetModuleNumber() {
    System.out.println("getModuleNumber");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    Integer expResult = 0;
    Integer result = instance.getModuleNumber();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetPortCount() {
    System.out.println("getPortCount");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    Integer expResult = 16;
    Integer result = instance.getPortCount();
    assertEquals(expResult, result);
  }

  //@Test
  public void testGetAddressOffset() {
    System.out.println("getAddressOffset");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    Integer expResult = 1000;
    Integer result = instance.getAddressOffset();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetIdentifier() {
    System.out.println("getIdentifier");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    instance.setIdentifier(65);
    Integer expResult = 65;
    Integer result = instance.getIdentifier();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetPorts() {
    System.out.println("getPorts");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    instance.setIdentifier(65);

    int[] expResult = new int[16];
    for (int i = 0; i < expResult.length; i++) {
      expResult[i] = 0;
    }

    int[] result = instance.getPorts();
    assertArrayEquals(expResult, result);
  }

  @Test
  public void testSetPortValue() {
    System.out.println("setPortValue");
    int port = 3;
    boolean active = true;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    instance.setIdentifier(65);

    instance.setPortValue(port, active);

    int[] expResult = new int[16];
    expResult[3] = 1;

    int[] result = instance.getPorts();
    assertArrayEquals(expResult, result);
  }

  @Test
  public void testIsPort() {
    System.out.println("isPort");
    int port = 6;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    instance.setIdentifier(65);

    instance.setPortValue(port, true);

    boolean expResult = true;
    boolean result = instance.isPort(port);
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAccumulatedPortsValue() {
    System.out.println("getAccumulatedPortsValue");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    instance.setIdentifier(65);

    instance.setPortValue(0, true);
    instance.setPortValue(1, true);
 
    int expResult = 3;
    int result = instance.getAccumulatedPortsValue();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetPrevPorts() {
    System.out.println("getPrevPorts");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    instance.setIdentifier(65);

    instance.setPortValue(5, true);

    int[] expResult = new int[16];
    int[] expPrevResult = new int[16];
    
    expResult[5] = 1;

    int[] result = instance.getPorts();
    
    assertArrayEquals(expResult, result);
    result = instance.getPrevPorts();
    assertArrayEquals(expPrevResult, result);
 
    instance.setPortValue(5, false);
    expResult[5] = 0;
    expPrevResult[5] = 1;
    result = instance.getPorts();
    assertArrayEquals(expResult, result);
    result = instance.getPrevPorts();
    assertArrayEquals(expPrevResult, result);
  }

  @Test
  public void testGetSensor() {
    System.out.println("getSensor");
    int port = 1;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    instance.setIdentifier(65);
    
    SensorBean expResult = new SensorBean();
   // expResult.setId("65-1001");
    expResult.setContactId(1);
    expResult.setDeviceId(65);
    expResult.setName("B1-1001");
    
    SensorBean result = instance.getSensor(port);
    assertEquals(expResult, result);
  }

  //@Test
  public void testGetChangedSensors() {
    System.out.println("getChangedSensors");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    List<SensorEvent> expResult = null;
    List<SensorEvent> result = instance.getChangedSensors();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetSensors() {
    System.out.println("getSensors");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    List<SensorBean> expResult = null;
    List<SensorBean> result = instance.getSensors();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testPortToString() {
    System.out.println("portToString");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    String expResult = "";
    String result = instance.portToString();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testToString() {
    System.out.println("toString");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }
}
