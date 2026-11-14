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

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression for the newline-separated ECoS frames now emitted by the
 * virtual connection. The main EsuEcosCommandStationImplTest exercises the
 * complete virtual connection and persistence-backed roster.
 */
class EcosVirtualResponseParsingTest {

  @Test
  void virtualBaseObjectMustExposeListAndSizeSeparately() {
    EcosMessage reply = new EcosMessage("get(1,list,size,status,status2)");
    reply.addResponse("""
        <REPLY get(1,list,size,status,status2)>
        1 list
        1 size[10]
        1 status[GO]
        1 status2[ALL]
        <END 0 (OK)>
        """);
    Map<String, Object> values = reply.getValueMap();
    assertTrue(reply.isResponseComplete());
    assertTrue(values.containsKey("list"));
    assertNull(values.get("list"));
    assertEquals("10", values.get("size"));
    assertEquals("GO", values.get("status"));
  }

  @Test
  void virtualRosterMustPreserveAttributesForSameLocomotive() {
    EcosMessage reply = new EcosMessage("queryObjects(10,name,addr,protocol)");
    reply.addResponse("""
        <REPLY queryObjects(10,name,addr,protocol)>
        1000 name["NS 6505"]
        1000 addr[8]
        1000 protocol[DCC28]
        <END 0 (OK)>
        """);
    @SuppressWarnings("unchecked")
    Map<String, Object> loco = (Map<String, Object>) reply.getValueMap().get("1000");
    assertNotNull(loco);
    assertEquals("NS 6505", loco.get("name"));
    assertEquals("8", loco.get("addr"));
    assertEquals("DCC28", loco.get("protocol"));
  }

  @Test
  void virtualStopReplyMustReportStop() {
    EcosMessage reply = new EcosMessage("get(1, status, status2)");
    reply.addResponse("""
        <REPLY get(1, status, status2)>
        1 status[STOP]
        1 status2[NONE]
        <END 0 (OK)>
        """);
    assertTrue(reply.isValid());
    assertEquals("STOP", reply.getValueMap().get("status"));
  }
}
