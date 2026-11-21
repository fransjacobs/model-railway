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

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EcosStartupParserTest {
  @Test
  void parsesRealEcosStartupResponseWithValuelessList() {
    EcosMessage message = new EcosMessage("get(1,objectclass,list,size,status)");
    message.addResponse("""
        <REPLY get(1,objectclass,list,size,status)>
        1 objectclass[model]
        1 view[none]
        1 list
        1 size[10]
        1 minarguments[64]
        1 protocolversion[0.5]
        1 commandstationtype["ECoS"]
        1 name["ECoS-50000"]
        1 serialnumber["0x000480b5"]
        1 hardwareversion[1.3]
        1 applicationversion[4.3.3]
        1 applicationversionsuffix[""]
        1 updateonerror[0]
        1 status[STOP]
        1 status2[NONE]
        1 prog-status[0]
        1 m4-status[none]
        1 railcomplus-status[none]
        1 watchdog[0,0]
        1 railcom[1]
        1 railcomplus[1]
        1 railcomplus-range[1000]
        1 railcomplus-mode[manual]
        1 allowlocotakeover[1]
        1 stoponlastdisconnect[0]
        <END 0 (OK)>
        """);
    Map<String, Object> values = message.getValueMap();
    assertTrue(values.containsKey("list"));
    assertNull(values.get("list"));
    assertEquals("10", values.get("size"));
    assertEquals("ECoS-50000", values.get("name"));
    assertEquals("STOP", values.get("status"));
    assertEquals("0,0", values.get("watchdog"));
    assertTrue(message.isValid());
  }

  @Test
  void retainsMultipleAttributesForSameLocomotive() {
    EcosMessage message = new EcosMessage("queryObjects(10,name,addr,protocol)");
    message.addResponse("""
        <REPLY queryObjects(10,name,addr,protocol)>
        1000 name["Big Boy"]
        1000 addr[3]
        1000 protocol[DCC128]
        <END 0 (OK)>
        """);
    Map<?,?> loco = (Map<?,?>) message.getValueMap().get("1000");
    assertEquals("Big Boy", loco.get("name"));
    assertEquals("3", loco.get("addr"));
    assertEquals("DCC128", loco.get("protocol"));
  }
}
