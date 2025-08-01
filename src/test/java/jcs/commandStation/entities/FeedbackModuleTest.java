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
package jcs.commandStation.entities;

import java.util.ArrayList;
import java.util.List;
import jcs.entities.SensorBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FeedbackModuleTest {

  public FeedbackModuleTest() {
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
    FeedbackModule instance = new FeedbackModule();
    instance.setId(0);
    Integer expResult = 0;

    Integer result = instance.getId();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetModuleNumber() {
    System.out.println("getModuleNumber");
    FeedbackModule instance = new FeedbackModule();
    instance.setId(0);
    instance.setModuleNumber(1);
    Integer expResult = 1;
    Integer result = instance.getModuleNumber();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetPortCount() {
    System.out.println("getPortCount");
    FeedbackModule instance = new FeedbackModule();
    instance.setId(0);
    instance.setModuleNumber(0);
    instance.setPortCount(16);
    Integer expResult = 16;
    Integer result = instance.getPortCount();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAddressOffset() {
    System.out.println("getAddressOffset");
    FeedbackModule instance = new FeedbackModule();
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
    FeedbackModule instance = new FeedbackModule();
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
    FeedbackModule instance = new FeedbackModule();
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
    FeedbackModule instance = new FeedbackModule();
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
    FeedbackModule instance = new FeedbackModule();
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
    FeedbackModule instance = new FeedbackModule();
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
    FeedbackModule instance = new FeedbackModule();
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
  public void testGetSensorMarklin() {
    System.out.println("getSensorMarklin");
    int port = 2;
    FeedbackModule instance = new FeedbackModule();
    instance.setCommandStationId("marklin.cs");
    instance.setId(0);
    instance.setModuleNumber(1);
    instance.setPortCount(16);
    instance.setAddressOffset(2000);
    instance.setIdentifier(65);
    instance.setBusNumber(2);

    SensorBean expResult = new SensorBean();
    expResult.setId(2003);

    expResult.setCommandStationId("marklin.cs");
    expResult.setContactId(port + 1);
    expResult.setDeviceId(1);
    expResult.setNodeId(65);
    expResult.setStatus(0);
    expResult.setPreviousStatus(0);
    expResult.setName("B2-M01-C03");
    expResult.setBusNr(2);

    SensorBean result = instance.getSensor(port);
    assertEquals(expResult, result);
  }

  @Test
  public void testGetSensorEsu() {
    System.out.println("getSensorEsu");
    int port = 5;
    FeedbackModule instance = new FeedbackModule();
    instance.setCommandStationId("esu-ecos");
    instance.setId(101);
    instance.setModuleNumber(2);
    instance.setPortCount(16);
    instance.setAddressOffset(0);
    instance.setIdentifier(null);
    instance.setPortValue(5, true);

    SensorBean expResult = new SensorBean();
    expResult.setId(21);
    expResult.setCommandStationId("esu-ecos");
    expResult.setContactId(6);
    expResult.setDeviceId(2);
    expResult.setNodeId(null);
    expResult.setStatus(1);
    expResult.setPreviousStatus(0);
    expResult.setName("M02-C06");

    SensorBean result = instance.getSensor(port);
    assertEquals(expResult, result);
  }

  @Test
  public void testGetSensor1Bus1Marklin() {
    System.out.println("getSensor1Bus1Marklin");
    int port = 1; // 2nd port
    FeedbackModule instance = new FeedbackModule();
    instance.setCommandStationId("marklin.cs");
    instance.setId(1);
    instance.setModuleNumber(1);
    instance.setPortCount(16);
    instance.setAddressOffset(1000);
    instance.setIdentifier(65);
    instance.setPortValue(1, true);
    instance.setBusNumber(1);

    SensorBean expResult = new SensorBean();
    expResult.setId(1002);
    expResult.setCommandStationId("marklin.cs");
    expResult.setContactId(port + 1);
    expResult.setDeviceId(1);
    expResult.setNodeId(65);
    expResult.setStatus(1);
    expResult.setPreviousStatus(0);
    expResult.setName("B1-M01-C02");
    expResult.setBusNr(1);

    SensorBean result = instance.getSensor(port);
    assertEquals(expResult, result);
  }

  @Test
  public void testGetSensors() {
    System.out.println("getSensors");
    FeedbackModule instance = new FeedbackModule();
    instance.setCommandStationId("esu-ecos");
    instance.setId(102);
    instance.setIdentifier(0);
    instance.setModuleNumber(2);
    instance.setPortCount(FeedbackModule.DEFAULT_PORT_COUNT);
    instance.setAddressOffset(0);

    List<SensorBean> expResult = new ArrayList<>();

    for (int i = 0; i < FeedbackModule.DEFAULT_PORT_COUNT; i++) {
      SensorBean sb = new SensorBean();
      sb.setCommandStationId("esu-ecos");
      sb.setId(1 * FeedbackModule.DEFAULT_PORT_COUNT + i);
      sb.setContactId(i + 1);
      sb.setDeviceId(2);
      sb.setStatus(0);
      sb.setNodeId(0);
      sb.setPreviousStatus(0);
      sb.setName("M02-C" + String.format("%02d", (i + 1)));
      expResult.add(sb);
    }

    List<SensorBean> result = instance.getSensors();

    assertEquals(expResult, result);
  }

  @Test
  public void testGetSensorsMarklin() {
    System.out.println("getSensorsMarklin");
    FeedbackModule instance = new FeedbackModule();
    instance.setCommandStationId("marklin.cs");
    instance.setId(1);
    instance.setIdentifier(65);
    instance.setBusNumber(1);
    instance.setModuleNumber(1);
    instance.setPortCount(FeedbackModule.DEFAULT_PORT_COUNT);
    instance.setAddressOffset(1000);

    List<SensorBean> expResult = new ArrayList<>();

    for (int i = 0; i < FeedbackModule.DEFAULT_PORT_COUNT; i++) {
      SensorBean sb = new SensorBean();
      sb.setCommandStationId("marklin.cs");
      sb.setId(1000 + i + 1);
      sb.setContactId(i + 1);
      sb.setDeviceId(1);
      sb.setStatus(0);
      sb.setNodeId(65);
      sb.setPreviousStatus(0);
      sb.setName("B1-M01-C" + String.format("%02d", (i + 1)));
      sb.setBusNr(1);
      expResult.add(sb);
    }

    List<SensorBean> result = instance.getSensors();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetChangedSensors() {
    System.out.println("getChangedSensors");
    int port = 5;

    FeedbackModule instance = new FeedbackModule();
    instance.setCommandStationId("esu-ecos");
    instance.setId(100);
    instance.setIdentifier(0);
    instance.setModuleNumber(1);
    instance.setPortCount(FeedbackModule.DEFAULT_PORT_COUNT);
    instance.setAddressOffset(0);

    List<SensorBean> expResult = new ArrayList<>();
    List<SensorBean> result = instance.getChangedSensors();

    assertEquals(expResult, result);

    instance.setPortValue(port, true);

    result = instance.getChangedSensors();

    SensorBean expChangedResult = new SensorBean();

    expChangedResult.setId(5);
    expChangedResult.setCommandStationId("esu-ecos");
    expChangedResult.setContactId(6);
    expChangedResult.setDeviceId(1);
    expChangedResult.setNodeId(0);
    expChangedResult.setStatus(1);
    expChangedResult.setPreviousStatus(0);
    expChangedResult.setName("M01-C06");

    expResult.add(expChangedResult);

    assertEquals(expResult, result);
  }

}
