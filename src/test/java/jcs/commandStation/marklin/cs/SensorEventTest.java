/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.commandStation.marklin.cs;

import jcs.commandStation.marklin.cs.can.CanMessage;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author fransjacobs
 */
public class SensorEventTest {

  private CanMessage message;

  public SensorEventTest() {
  }

  @Before
  public void setUp() {
    message = new CanMessage(new byte[]{0x00, 0x23, (byte)0xcb, 0x12, 0x08, 0x00, 0x00, 0x00, 0x30, 0x00, 0x01, 0x0f, 0x59});
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of isNewValue method, of class FeedbackEventStatus.
   */
//    @Test
//    public void testIsNewValue() {
//        System.out.println("isNewValue");
//        SensorMessageEvent instance = new SensorMessageEvent(message);
//        boolean expResult = true;
//        boolean result = instance.isNewValue();
//        assertEquals(expResult, result);
//    }
  /**
   * Test of isOldValue method, of class FeedbackEventStatus.
   */
//    @Test
//    public void testIsOldValue() {
//        System.out.println("isOldValue");
//        SensorMessageEvent instance = new SensorMessageEvent(message);
//        boolean expResult = false;
//        boolean result = instance.isOldValue();
//        assertEquals(expResult, result);
//    }
  /**
   * Test of getContactId method, of class FeedbackEventStatus.
   */
//    @Test
//    public void testGetContactId() {
//        System.out.println("getContactId");
//        SensorMessageEvent instance = new SensorMessageEvent(message);
//        int expResult = 48;
//        int result = instance.getContactId();
//        assertEquals(expResult, result);
//    }
  /**
   * Test of getDeviceId method, of class FeedbackEventStatus.
   */
//    @Test
//    public void testGetDeviceId() {
//        System.out.println("getDeviceId");
//        SensorMessageEvent instance = new SensorMessageEvent(message);
//        int expResult = 0;
//        int result = instance.getDeviceId();
//        assertEquals(expResult, result);
//    }
  /**
   * Test of getMillis method, of class FeedbackEventStatus.
   */
//    @Test
//    public void testGetMillis() {
//        System.out.println("getMillis");
//        SensorMessageEvent instance = new SensorMessageEvent(message);
//        int expResult = 39290;
//        int result = instance.getMillis();
//        assertEquals(expResult, result);
//    }
  /**
   * Test of toString method, of class FeedbackEventStatus.
   */
//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        SensorMessageEvent instance = new SensorMessageEvent(message);
//        String expResult = "SensorEvent{contactId: 48 deviceId: 0 value: true prevValue: false  millis: 39290}";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//    }
}
