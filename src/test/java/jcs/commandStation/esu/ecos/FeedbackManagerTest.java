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
package jcs.commandStation.esu.ecos;

import jcs.commandStation.entities.FeedbackModule;
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
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
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
  @Test
  public void testUpdate() {
    System.out.println("update");
    EcosMessage size = new EcosMessage("get(26, size)");
    size.addResponse("<REPLY get(26, size)>26 size[1]<END 0 (OK)>");

    EcosMessage m1 = new EcosMessage("get(100, state, ports)");
    m1.addResponse("<REPLY get(100, state, ports)>100 state[0x0]100 ports[16]<END 0 (OK)>");

    FeedbackManager instance = new FeedbackManager(null, size);
    instance.update(m1);

    int expResult = 1;
    int result = instance.getSize();
    assertEquals(expResult, result);
    assertNotNull(instance.getFeedbackModule(100));

    assertEquals(false, instance.getFeedbackModule(100).isPort(0));

    instance.update(new EcosMessage("<EVENT 100>100 state[0x1]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(0));

    assertEquals(false, instance.getFeedbackModule(100).isPort(1));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x2]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(1));

    assertEquals(false, instance.getFeedbackModule(100).isPort(2));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x4]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(2));

    assertEquals(false, instance.getFeedbackModule(100).isPort(3));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x8]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(3));

    assertEquals(false, instance.getFeedbackModule(100).isPort(4));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x10]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(4));

    assertEquals(false, instance.getFeedbackModule(100).isPort(5));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x20]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(5));

    assertEquals(false, instance.getFeedbackModule(100).isPort(6));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x40]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(6));

    assertEquals(false, instance.getFeedbackModule(100).isPort(7));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x80]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(7));

    assertEquals(false, instance.getFeedbackModule(100).isPort(8));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x100]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(8));

    assertEquals(false, instance.getFeedbackModule(100).isPort(9));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x200]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(9));

    assertEquals(false, instance.getFeedbackModule(100).isPort(10));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x400]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(10));

    assertEquals(false, instance.getFeedbackModule(100).isPort(11));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x800]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(11));

    assertEquals(false, instance.getFeedbackModule(100).isPort(12));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x1000]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(12));

    assertEquals(false, instance.getFeedbackModule(100).isPort(13));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x2000]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(13));

    assertEquals(false, instance.getFeedbackModule(100).isPort(14));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x4000]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(14));

    assertEquals(false, instance.getFeedbackModule(100).isPort(15));
    instance.update(new EcosMessage("<EVENT 100>100 state[0x8000]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(15));
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

    FeedbackManager instance = new FeedbackManager(null, info);
    instance.update(m1);

    int expResult = 1;
    int result = instance.getSize();
    assertEquals(expResult, result);

    assertNotNull(instance.getFeedbackModule(100));

    assertEquals(false, instance.getFeedbackModule(100).isPort(1));

    instance.update(new EcosMessage("<EVENT 100>100 state[0x1]<END 0 (OK)>"));

    FeedbackModule fbmb = instance.getFeedbackModule(100);
    boolean x = fbmb.isPort(0);

    assertEquals(true, instance.getFeedbackModule(100).isPort(0));

    instance.update(new EcosMessage("<EVENT 100>100 state[0x3]<END 0 (OK)>"));
    assertEquals(true, instance.getFeedbackModule(100).isPort(0));
    assertEquals(true, instance.getFeedbackModule(100).isPort(1));
  }

}
