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
import jcs.entities.FeedbackModuleBean;
import jcs.entities.SensorBean;
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

  //@Test
  public void testGetId() {
    System.out.println("getId");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    Integer expResult = null;
    Integer result = instance.getId();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testSetId() {
    System.out.println("setId");
    Integer id = null;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setId(id);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetModuleNumber() {
    System.out.println("getModuleNumber");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    Integer expResult = null;
    Integer result = instance.getModuleNumber();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testSetModuleNumber() {
    System.out.println("setModuleNumber");
    Integer moduleNumber = null;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setModuleNumber(moduleNumber);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetPortCount() {
    System.out.println("getPortCount");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    Integer expResult = null;
    Integer result = instance.getPortCount();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testSetPortCount() {
    System.out.println("setPortCount");
    Integer portCount = null;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setPortCount(portCount);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetAddressOffset() {
    System.out.println("getAddressOffset");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    Integer expResult = null;
    Integer result = instance.getAddressOffset();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testSetAddressOffset() {
    System.out.println("setAddressOffset");
    Integer addressOffset = null;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setAddressOffset(addressOffset);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetIdentifier() {
    System.out.println("getIdentifier");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    Integer expResult = null;
    Integer result = instance.getIdentifier();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testSetIdentifier() {
    System.out.println("setIdentifier");
    Integer identifier = null;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setIdentifier(identifier);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetPorts() {
    System.out.println("getPorts");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    int[] expResult = null;
    int[] result = instance.getPorts();
    assertArrayEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testSetPorts() {
    System.out.println("setPorts");
    int[] ports = null;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setPorts(ports);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testSetPortValue() {
    System.out.println("setPortValue");
    int port = 0;
    boolean active = false;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setPortValue(port, active);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetAccumulatedPortsValue() {
    System.out.println("getAccumulatedPortsValue");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    int expResult = 0;
    int result = instance.getAccumulatedPortsValue();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetPrevPorts() {
    System.out.println("getPrevPorts");
    FeedbackModuleBean instance = new FeedbackModuleBean();
    int[] expResult = null;
    int[] result = instance.getPrevPorts();
    assertArrayEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testSetPrevPorts() {
    System.out.println("setPrevPorts");
    int[] prevPorts = null;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    instance.setPrevPorts(prevPorts);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetSensor() {
    System.out.println("getSensor");
    int port = 0;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    SensorBean expResult = null;
    SensorBean result = instance.getSensor(port);
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testIsPort() {
    System.out.println("isPort");
    int port = 0;
    FeedbackModuleBean instance = new FeedbackModuleBean();
    boolean expResult = false;
    boolean result = instance.isPort(port);
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
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
