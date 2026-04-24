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

  @Test
  public void testGetSignalValue() {
    System.out.println("getSignalValue");

    // Null state → OFF
    AccessoryBean instance = new AccessoryBean();
    assertEquals(AccessoryBean.SignalValue.Hp0, instance.getSignalValue());

    // State 0 → Hp0 (stop)
    instance.setState(0);
    assertEquals(AccessoryBean.SignalValue.Hp0, instance.getSignalValue());

    // State 1 → Hp1 (proceed)
    instance.setState(1);
    assertEquals(AccessoryBean.SignalValue.Hp1, instance.getSignalValue());

    // State 2 → Hp0Sh1 (stop + shunting allowed)
    instance.setState(2);
    assertEquals(AccessoryBean.SignalValue.Hp0Sh1, instance.getSignalValue());

    // State 3 → Hp2 (proceed slowly)
    instance.setState(3);
    assertEquals(AccessoryBean.SignalValue.Hp2, instance.getSignalValue());
  }

  @Test
  public void testIsSignal() {
    System.out.println("isSignal");

    // Null type → false
    AccessoryBean instance = new AccessoryBean();
    assertFalse(instance.isSignal());

    // Turnout type → false
    instance.setType("linksweiche");
    assertFalse(instance.isSignal());

    // Plain lichtsignal → true
    instance.setType("lichtsignal_HP01");
    assertTrue(instance.isSignal());

    // Prefixed lichtsignal → true
    instance.setType("urc_lichtsignal_HP012");
    assertTrue(instance.isSignal());

    // 4-state lichtsignal → true
    instance.setType("lichtsignal_HP012_SH01");
    assertTrue(instance.isSignal());
  }

  @Test
  public void testIsTurnout() {
    System.out.println("isTurnout");

    // Null type → false
    AccessoryBean instance = new AccessoryBean();
    assertFalse(instance.isTurnout());

    // Signal type → false
    instance.setType("lichtsignal_HP01");
    assertFalse(instance.isTurnout());

    // Left turnout (linksweiche) → true
    instance.setType("linksweiche");
    assertTrue(instance.isTurnout());

    // Right turnout (rechtsweiche) → true
    instance.setType("rechtsweiche");
    assertTrue(instance.isTurnout());

    // Three-way switch (dreiwegweiche) → true, also contains "weiche"
    instance.setType("dreiwegweiche");
    assertTrue(instance.isTurnout());
  }

  @Test
  public void testIsOther() {
    System.out.println("isOther");

    // Null type → neither signal nor turnout, so isOther = true
    AccessoryBean instance = new AccessoryBean();
    assertTrue(instance.isOther());

    // Turnout → not other
    instance.setType("linksweiche");
    assertFalse(instance.isOther());

    // Signal → not other
    instance.setType("lichtsignal_HP01");
    assertFalse(instance.isOther());

    // Uncoupler or generic accessory → other
    instance.setType("entkuppler");
    assertTrue(instance.isOther());
  }

}
