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

import java.util.List;
import java.util.Map;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.entities.LocomotiveBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fransjacobs
 */
public class LocomotiveManagerTest {

  public LocomotiveManagerTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of refresh method, of class LocomotiveManager.
   */
  //@Test
  public void testRefresh() {
    System.out.println("refresh");
    LocomotiveManager instance = null;
    instance.refresh();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of shutdown method, of class LocomotiveManager.
   */
  //@Test
  public void testShutdown() {
    System.out.println("shutdown");
    LocomotiveManager instance = null;
    instance.shutdown();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of refreshLocomotives method, of class LocomotiveManager.
   */
  //@Test
  public void testRefreshLocomotives() {
    System.out.println("refreshLocomotives");
    List<LocomotiveBean> locomotiveList = null;
    LocomotiveManager instance = null;
    instance.refreshLocomotives(locomotiveList);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of registerSlots method, of class LocomotiveManager.
   */
  //@Test
  public void testRegisterSlots() {
    System.out.println("registerSlots");
    LocomotiveManager instance = null;
    instance.registerSlots();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of update method, of class LocomotiveManager.
   */
  //@Test
  public void testUpdate() {
    System.out.println("update");
    LoconetMessage message = null;
    LocomotiveManager instance = null;
    instance.update(message);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getSize method, of class LocomotiveManager.
   */
  //@Test
  public void testGetSize() {
    System.out.println("getSize");
    LocomotiveManager instance = null;
    int expResult = 0;
    int result = instance.getSize();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  @Test
  public void testParseSlotData8() {
    System.out.println("parseSlotData8");

    String opc_sl_rd_data = "0xe7 0x0e 0x08 0x32 0x48 0x00 0x34 0x07 0x00 0x00 0x00 0x00 0x00 0x57";
    LocomotiveBean locomotive = new LocomotiveBean();
    LocomotiveManager instance = new LocomotiveManager(null);
    LocomotiveBean expResult = new LocomotiveBean();
    expResult.setAddress(72);
    expResult.setDirection(LocomotiveBean.Direction.FORWARDS);
    expResult.setVelocity(0);
    expResult.setFunctionValue(0, false);
    expResult.setFunctionValue(1, false);
    expResult.setFunctionValue(2, false);
    expResult.setFunctionValue(3, false);
    expResult.setFunctionValue(4, false);

    LocomotiveBean result = instance.parseSlotData(LoconetMessage.parse(opc_sl_rd_data), locomotive);

    assertNotNull(result);
    assertEquals(72, result.getAddress());
    assertEquals(LocomotiveBean.Direction.FORWARDS, result.getDirection());
    assertEquals(0, result.getVelocity());

    assertTrue(result.getFunctionBean(0).isOn());
    assertFalse(result.getFunctionBean(1).isOn());
    assertFalse(result.getFunctionBean(2).isOn());
    assertTrue(result.getFunctionBean(3).isOn());
    assertFalse(result.getFunctionBean(4).isOn());

    assertEquals(expResult, result);
  }

  @Test
  public void testParseSlotData7() {
    System.out.println("parseSlotData7");

    String opc_sl_rd_data = "0xe7 0x0e 0x07 0x32 0x0c 0x00 0x32 0x07 0x00 0x00 0x00 0x00 0x00 0x1a";
    LocomotiveBean locomotive = new LocomotiveBean();
    LocomotiveManager instance = new LocomotiveManager(null);
    LocomotiveBean expResult = new LocomotiveBean();
    expResult.setAddress(12);
    expResult.setDirection(LocomotiveBean.Direction.FORWARDS);
    expResult.setVelocity(0);
    expResult.setFunctionValue(0, true);
    expResult.setFunctionValue(1, false);
    expResult.setFunctionValue(2, false);
    expResult.setFunctionValue(3, false);
    expResult.setFunctionValue(4, false);

    LocomotiveBean result = instance.parseSlotData(LoconetMessage.parse(opc_sl_rd_data), locomotive);

    assertNotNull(result);
    assertEquals(12, result.getAddress());
    assertEquals(LocomotiveBean.Direction.FORWARDS, result.getDirection());
    assertEquals(0, result.getVelocity());

    assertTrue(result.getFunctionBean(0).isOn());
    assertFalse(result.getFunctionBean(1).isOn());
    assertTrue(result.getFunctionBean(2).isOn());
    assertFalse(result.getFunctionBean(3).isOn());
    assertFalse(result.getFunctionBean(4).isOn());

    assertEquals(expResult, result);
  }

  /**
   * Test of parseLongAck method, of class LocomotiveManager.
   */
  //@Test
  public void testParseLongAck() {
    System.out.println("parseLongAck");
    LoconetMessage message = null;
    LoconetMessage request = null;
    LocomotiveManager instance = null;
    boolean expResult = false;
    boolean result = instance.parseLongAck(message, request);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getLocomotives method, of class LocomotiveManager.
   */
  //@Test
  public void testGetLocomotives() {
    System.out.println("getLocomotives");
    LocomotiveManager instance = null;
    Map<Long, LocomotiveBean> expResult = null;
    Map<Long, LocomotiveBean> result = instance.getLocomotives();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of onSpeedChange method, of class LocomotiveManager.
   */
  //@Test
  public void testOnSpeedChange() {
    System.out.println("onSpeedChange");
    LocomotiveSpeedEvent velocityEvent = null;
    LocomotiveManager instance = null;
    instance.onSpeedChange(velocityEvent);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of onDirectionChange method, of class LocomotiveManager.
   */
  //@Test
  public void testOnDirectionChange() {
    System.out.println("onDirectionChange");
    LocomotiveDirectionEvent directionEvent = null;
    LocomotiveManager instance = null;
    instance.onDirectionChange(directionEvent);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of onFunctionChange method, of class LocomotiveManager.
   */
  //@Test
  public void testOnFunctionChange() {
    System.out.println("onFunctionChange");
    LocomotiveFunctionEvent locomotiveFunctionEvent = null;
    LocomotiveManager instance = null;
    instance.onFunctionChange(locomotiveFunctionEvent);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
