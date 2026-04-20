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
package jcs.entities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class AccessoryBeanTest {

  public AccessoryBeanTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of isBiAddress method, of class AccessoryBean.
   */
  @Test
  public void testIsBiAddress() {
    System.out.println("isBiAddress");
    AccessoryBean instance = new AccessoryBean("W1", 1, 2, "W 01", "linksweiche", 0, 2, 20, "mm", "marklin.cs");

    assertFalse(instance.isBiAddress());
    instance.setStates(3);
    assertTrue(instance.isBiAddress());
    instance.setStates(4);
    assertTrue(instance.isBiAddress());
  }

  /**
   * Test of is3WaySwitch method, of class AccessoryBean.
   */
  @Test
  public void testIs3WaySwitch() {
    System.out.println("is3WaySwitch");
    AccessoryBean instance = new AccessoryBean("W1", 1, 2, "W 01", "linksweiche", 0, 2, 20, "mm", "marklin.cs");

    assertFalse(instance.is3WaySwitch());

    instance.setType("dreiwegweiche");

    assertTrue(instance.is3WaySwitch());
  }

  @Test
  public void testToggleTurnout() {
    System.out.println("toggleTurnout");
    AccessoryBean instance = new AccessoryBean("W1", 1, 2, "W 01", "linksweiche", 0, 2, 20, "mm", "marklin.cs");

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(0, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.GREEN, instance.getAccessoryValue());
    assertEquals(1, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(0, instance.getState());

    instance.setStates(3);
    instance.setType("dreiwegweiche");

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(0, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.GREEN, instance.getAccessoryValue());
    assertEquals(1, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.RED2, instance.getAccessoryValue());
    assertEquals(2, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.GREEN, instance.getAccessoryValue());
    assertEquals(1, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(0, instance.getState());

  }

  @Test
  public void testToggleSignal() {
    System.out.println("toggleSignal");
    AccessoryBean instance = new AccessoryBean("S1", 1, 2, "S 01", "lichtsignal_HP01", 0, 2, 20, "mm", "marklin.cs");

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp0, instance.getSignalValue());
    assertEquals(0, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.GREEN, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp1, instance.getSignalValue());
    assertEquals(1, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp0, instance.getSignalValue());
    assertEquals(0, instance.getState());

    instance.setStates(3);
    instance.setType("urc_lichtsignal_HP012");
    instance.setDecType("dcc");

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp0, instance.getSignalValue());
    assertEquals(0, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.GREEN, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp1, instance.getSignalValue());
    assertEquals(1, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.YELLOW, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp2, instance.getSignalValue());
    assertEquals(3, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp0, instance.getSignalValue());
    assertEquals(0, instance.getState());

    instance.setStates(4);
    instance.setType("urc_lichtsignal_HP012_SH01");
    instance.setDecType("dcc");

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp0, instance.getSignalValue());
    assertEquals(0, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.GREEN, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp1, instance.getSignalValue());
    assertEquals(1, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.WHITE, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp0Sh1, instance.getSignalValue());
    assertEquals(2, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.YELLOW, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp2, instance.getSignalValue());
    assertEquals(3, instance.getState());

    instance.toggle();

    assertEquals(AccessoryBean.AccessoryValue.RED, instance.getAccessoryValue());
    assertEquals(AccessoryBean.SignalValue.Hp0, instance.getSignalValue());
    assertEquals(0, instance.getState());
  }

  /**
   * Test of getSignalValue method, of class AccessoryBean.
   */
  //@Test
  public void testGetSignalValue() {
    System.out.println("getSignalValue");
    AccessoryBean instance = new AccessoryBean();
    AccessoryBean.SignalValue expResult = null;
    AccessoryBean.SignalValue result = instance.getSignalValue();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of isSignal method, of class AccessoryBean.
   */
  //@Test
  public void testIsSignal() {
    System.out.println("isSignal");
    AccessoryBean instance = new AccessoryBean();
    boolean expResult = false;
    boolean result = instance.isSignal();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of isTurnout method, of class AccessoryBean.
   */
  //@Test
  public void testIsTurnout() {
    System.out.println("isTurnout");
    AccessoryBean instance = new AccessoryBean();
    boolean expResult = false;
    boolean result = instance.isTurnout();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of isOther method, of class AccessoryBean.
   */
  //@Test
  public void testIsOther() {
    System.out.println("isOther");
    AccessoryBean instance = new AccessoryBean();
    boolean expResult = false;
    boolean result = instance.isOther();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
