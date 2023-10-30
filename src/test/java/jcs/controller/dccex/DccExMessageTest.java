/*
 * Copyright 2023 frans.
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
package jcs.controller.dccex;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class DccExMessageTest {

  public DccExMessageTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetCommand() {
    System.out.println("getCommand");
    DccExMessage instance = new DccExMessage(DccExMessage.REQ_VERSION_HARDWARE_TURNOUTS);
    String expResult = "<s>";
    String result = instance.getCommand();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetFirstResponse() {
    System.out.println("getFirstResponse");
    DccExMessage instance = new DccExMessage(DccExMessage.REQ_VERSION_HARDWARE_TURNOUTS);
    instance.addResponse("<iDCC-EX V-5.0.3 / MEGA / STANDARD_MOTOR_SHIELD G-3bddf4d>");
    String expResult = "<iDCC-EX V-5.0.3 / MEGA / STANDARD_MOTOR_SHIELD G-3bddf4d>";
    String result = instance.getFirstResponse();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetFirstResponseContent() {
    System.out.println("getFirstResponseContent");
    DccExMessage instance = new DccExMessage(DccExMessage.REQ_VERSION_HARDWARE_TURNOUTS);
    instance.addResponse("<iDCC-EX V-5.0.3 / MEGA / STANDARD_MOTOR_SHIELD G-3bddf4d>");
    String expResult = "DCC-EX V-5.0.3 / MEGA / STANDARD_MOTOR_SHIELD G-3bddf4d";

    String result = instance.getFirstResponseContent();
    assertEquals(expResult, result);
  }

  @Test
  public void testIsResponseReceived() {
    System.out.println("isResponseReceived");
    DccExMessage instance = new DccExMessage(DccExMessage.REQ_VERSION_HARDWARE_TURNOUTS);
    boolean result = instance.isResponseReceived();

    assertEquals(false, result);
    instance.addResponse("<iDCC-EX V-5.0.3 / MEGA / STANDARD_MOTOR_SHIELD G-3bddf4d>");

    result = instance.isResponseReceived();
    assertEquals(true, result);
  }

  @Test
  public void testGetTXOpcode() {
    System.out.println("getTXOpcode");
    DccExMessage instance = new DccExMessage(DccExMessage.REQ_VERSION_HARDWARE_TURNOUTS);
    instance.addResponse("<iDCC-EX V-5.0.3 / MEGA / STANDARD_MOTOR_SHIELD G-3bddf4d>");

    String expResult = "s";
    String result = instance.getTXOpcode();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetRXOpcode() {
    System.out.println("getRXOpcode");
    DccExMessage instance = new DccExMessage(DccExMessage.REQ_VERSION_HARDWARE_TURNOUTS);
    instance.addResponse("<iDCC-EX V-5.0.3 / MEGA / STANDARD_MOTOR_SHIELD G-3bddf4d>");
    String result = instance.getRXOpcode();
    assertEquals("i", result);
  }

  @Test
  public void testIsSystemMessage() {
    System.out.println("getIsSystemMessage");
    DccExMessage instance = new DccExMessage(DccExMessage.REQ_VERSION_HARDWARE_TURNOUTS);
    instance.addResponse("<iDCC-EX V-5.0.3 / MEGA / STANDARD_MOTOR_SHIELD G-3bddf4d>");
    boolean result = instance.isSystemMessage();
    assertEquals(true, result);
  }

}
