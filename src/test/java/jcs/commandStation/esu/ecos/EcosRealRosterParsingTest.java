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

class EcosRealRosterParsingTest {

  static EcosMessage reply(String command, String content) {
    EcosMessage message = new EcosMessage(command);
    message.addResponse("<REPLY " + command + ">\n" + content + "\n<END 0 (OK)>\n");
    return message;
  }

  @Test
  void realEcosLocomotiveListMultipleAttributesPerLine() {
    EcosMessage response = reply("queryObjects(10,name,addr,protocol)", """
        1002 name["NS 6400"] addr[14] protocol[DCC28]
        1003 name["NS 6505"] addr[8] protocol[DCC28]
        1000 name["193 304-3 DB AG"] addr[0] protocol[MFX]
        """);
    Map<String,Object> values = response.getValueMap();
    assertEquals(3, values.size());
    Map<?,?> loco = (Map<?,?>) values.get("1003");
    assertEquals("1003", loco.get("id"));
    assertEquals("NS 6505", loco.get("name"));
    assertEquals("8", loco.get("addr"));
    assertEquals("DCC28", loco.get("protocol"));
  }

  @Test
  void realEcosAccessoryListWithSeveralAttributesPerLine() {
    EcosMessage response = reply("queryObjects(11,name1,name2,name3,addr,addrext,protocol,mode,symbol)", """
        20000 name1["Weiche 1"] addr[12] protocol[DCC] mode[0] symbol[0]
        20001 name1["Weiche 2"] addr[13] protocol[MOT] symbol[1]
        """);
    Map<?,?> accessory = (Map<?,?>) response.getValueMap().get("20000");
    assertEquals("Weiche 1", accessory.get("name1"));
    assertEquals("12", accessory.get("addr"));
    assertEquals("DCC", accessory.get("protocol"));
  }

  @Test
  void feedbackManagerSizeAndModuleState() {
    assertEquals("3", reply("get(26,size)", "26 size[3]")
        .getValueMap().get("size"));
    assertEquals("0x000f", reply("get(100,state,ports)", "100 state[0x000f] ports[16]")
        .getValueMap().get("state"));
  }

  @Test
  void missingListValueAndMultilineAttributesStillWork() {
    EcosMessage response = reply("get(1,list,size,status)", """
        1 list
        1 size[10]
        1 status[STOP]
        """);
    Map<String,Object> values = response.getValueMap();
    assertTrue(values.containsKey("list"));
    assertNull(values.get("list"));
    assertEquals("10", values.get("size"));
  }

  @Test
  void mixedRepeatedIdsAndOmittedIdsWork() {
    EcosMessage response = reply("get(1001,name,addr,func)", 
        "1001 name[\"A]B\"]1001 addr[7] func[0,1] func[1,0]");
    assertEquals("A]B", response.getValueMap().get("name"));
    assertEquals("7", response.getValueMap().get("addr"));
    assertEquals("[0,1],[1,0]", response.getValueMap().get("func"));
  }
}
