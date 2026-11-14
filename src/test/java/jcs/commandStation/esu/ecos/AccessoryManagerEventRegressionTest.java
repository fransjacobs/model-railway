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
package jcs.commandStation.esu.ecos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccessoryManagerEventRegressionTest {

  private AccessoryManager manager() {
    String tx = "queryObjects(11,name1,addr,protocol)";
    EcosMessage response = new EcosMessage(tx);
    response.addResponse("<REPLY " + tx + ">\n"
            + "20000 name1[\"W1\"] addr[1] protocol[DCC]\n"
            + "<END 0 (OK)>\n");
    return new AccessoryManager(null, response);
  }

  @Test
  void handlesOriginalConcatenatedEvent() {
    AccessoryManager manager = manager();
    assertEquals("20006", manager.updateManager(new EcosMessage(
            "<EVENT 11>11 msg[LIST_CHANGED]20006 appended11 size[7]<END 0 (OK)>")));
    assertEquals(7, manager.getSize());
  }

  @Test
  void handlesRealMultilineEvent() {
    AccessoryManager manager = manager();
    assertEquals("20006", manager.updateManager(new EcosMessage(
            "<EVENT 11>\n11 msg[LIST_CHANGED]\n20006 appended\n11 size[7]\n<END 0 (OK)>")));
    assertEquals(7, manager.getSize());
  }

  @Test
  void ignoresEventsWithoutAppendedAccessory() {
    AccessoryManager manager = manager();
    assertNull(manager.updateManager(new EcosMessage(
            "<EVENT 11>\n11 msg[LIST_CHANGED]\n11 size[0]\n<END 0 (OK)>")));
    assertEquals(0, manager.getSize());
  }
}
