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

import jcs.commandStation.marklin.cs.can.parser.DirectionInfo;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.entities.LocomotiveBean.Direction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class DirectionInfoTest {

  private CanMessage message;

  public DirectionInfoTest() {
  }

  @Before
  public void setUp() {
    message = new CanMessage(new byte[]{0x00, 0x0a, (byte) 0xcb, 0x13, 0x04, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x00});
    CanMessage response = new CanMessage(new byte[]{0x00, 0x0b, (byte) 0xcb, 0x13, 0x08, 0x00, 0x00, 0x00, 0x0c, 0x02, 0x00, 0x00, 0x00});
    message.addResponse(response);
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of toString method, of class DirectionInfo.
   */
  @Test
  public void testToString() {
    System.out.println("toString");
    DirectionInfo instance = new DirectionInfo(message);
    String expResult = "DirectionInfo{direction=BACKWARDS}";
    String result = instance.toString();
    assertEquals(expResult, result);
  }

  /**
   * Test of getDirection method, of class DirectionInfo.
   */
  @Test
  public void testGetDirection() {
    System.out.println("getDirection");
    DirectionInfo instance = new DirectionInfo(message);
    Direction expResult = Direction.BACKWARDS;
    Direction result = instance.getDirection();
    assertEquals(expResult, result);
  }

}
