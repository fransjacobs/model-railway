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
package jcs.commandStation.esu.ecos.entities;

import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.commandStation.esu.ecos.entities.FeedbackManager.S88;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author fransjacobs
 */
public class FeedbackManagerTest {

  public FeedbackManagerTest() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of update method, of class FeedbackManager.
   */
  //@Test
  public void testUpdate() {
    System.out.println("update");
    EcosMessage message = null;
    FeedbackManager instance = null;
    instance.update(message);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getSize method, of class FeedbackManager.
   */
  @Test
  public void testGetSize() {
    System.out.println("getSize");

    EcosMessage info = new EcosMessage("get(26, size)");
    info.addResponse("<REPLY get(26, size)>26 size[1]<END 0 (OK)>");

    EcosMessage m1 = new EcosMessage("get(100, state, ports)");
    m1.addResponse("<REPLY get(100, state, ports)>100 state[0x0]100 ports[16]<END 0 (OK)>");

    EcosMessage e1 = new EcosMessage("<EVENT 100>100 state[0x1]<END 0 (OK)>");
    EcosMessage e2 = new EcosMessage("<EVENT 100>100 state[0x2]<END 0 (OK)>");

    EcosMessage ee12 = new EcosMessage("<EVENT 100>100 state[0x3]<END 0 (OK)>");

    EcosMessage e3 = new EcosMessage("<EVENT 100>100 state[0x4]<END 0 (OK)>");
    EcosMessage e4 = new EcosMessage("<EVENT 100>100 state[0x8]<END 0 (OK)>");

    EcosMessage e5 = new EcosMessage("<EVENT 100>100 state[0x10]<END 0 (OK)>");
    EcosMessage e6 = new EcosMessage("<EVENT 100>100 state[0x20]<END 0 (OK)>");
    EcosMessage e7 = new EcosMessage("<EVENT 100>100 state[0x40]<END 0 (OK)>");
    EcosMessage e8 = new EcosMessage("<EVENT 100>100 state[0x80]<END 0 (OK)>");

    EcosMessage e9 = new EcosMessage("<EVENT 100>100 state[0x100]<END 0 (OK)>");
    EcosMessage e10 = new EcosMessage("<EVENT 100>100 state[0x200]<END 0 (OK)>");
    EcosMessage e11 = new EcosMessage("<EVENT 100>100 state[0x400]<END 0 (OK)>");
    EcosMessage e12 = new EcosMessage("<EVENT 100>100 state[0x800]<END 0 (OK)>");

    EcosMessage e13 = new EcosMessage("<EVENT 100>100 state[0x1000]<END 0 (OK)>");
    EcosMessage e14 = new EcosMessage("<EVENT 100>100 state[0x2000]<END 0 (OK)>");
    EcosMessage e15 = new EcosMessage("<EVENT 100>100 state[0x4000]<END 0 (OK)>");
    EcosMessage e16 = new EcosMessage("<EVENT 100>100 state[0x8000]<END 0 (OK)>");

    FeedbackManager instance = new FeedbackManager(info);
    instance.update(m1);

    int expResult = 1;
    int result = instance.getSize();
    assertEquals(expResult, result);

    assertNotNull(instance.getS88(100));

    assertEquals(false, instance.getS88(100).isPort(1));

    instance.update(e1);

    assertEquals(true, instance.getS88(100).isPort(0));

    instance.update(ee12);
    assertEquals(true, instance.getS88(100).isPort(0));
    assertEquals(true, instance.getS88(100).isPort(1));

  }

}
