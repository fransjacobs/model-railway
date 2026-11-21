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

class EcosMessageStep1Test {

  private static EcosMessage reply(String command, String body) {
    EcosMessage message = new EcosMessage(command);
    message.addResponse("<REPLY " + command + ">\n" + body + "\n<END 0 (OK)>\n");
    return message;
  }

  @Test
  void repeatedAttributesBelongToSameLocomotive() {
    EcosMessage message = reply("queryObjects(10,name,addr,protocol)",
            "1000 name[\"Big Boy\"]\n1000 addr[3]\n1000 protocol[DCC128]\n"
            + "1001 name[\"Second\"]\n1001 addr[7]");
    @SuppressWarnings("unchecked")
    Map<String, Object> loco = (Map<String, Object>) message.getValueMap().get("1000");
    assertEquals("Big Boy", loco.get(Ecos.NAME));
    assertEquals("3", loco.get(Ecos.ADDRESS));
    assertEquals("DCC128", loco.get(Ecos.PROTOCOL));
    assertEquals("7", ((Map<?, ?>) message.getValueMap().get("1001")).get(Ecos.ADDRESS));
  }

  @Test
  void concatenatedVirtualAttributesAndFunctionValues() {
    EcosMessage message = reply("get(1000,func,funcdesc)",
            "1000 name[\"A]B\"]1000 func[0,1]1000 func[1,0]1000 funcdesc[1,7,moment]");
    assertEquals("A]B", message.getValueMap().get(Ecos.NAME));
    assertEquals("[0,1],[1,0]", message.getValueMap().get(Ecos.FUNCTION));
    assertEquals("[1,7,moment]", message.getValueMap().get(Ecos.FUNCTION_DESC));
  }

  @Test
  void eventIdIsTakenFromHeaderEvenWithoutBody() {
    EcosMessage message = new EcosMessage("<EVENT 100>\n100 state[0x03]\n<END 0 (OK)>\n");
    assertEquals(100, message.getObjectId());
    assertTrue(message.isEvent());
    assertEquals("0x03", message.getValueMap().get(Ecos.STATE));
  }

  @Test
  void idOnlyQueriesAndSingleOptionCommands() {
    EcosMessage message = reply("queryObjects(10)", "1000\n1001\n1005");
    assertEquals(3, message.getValueMap().size());
    assertEquals("1005", message.getValueMap().get("1005"));
    assertEquals("1", new EcosMessage("get(1)").getId());
  }

  @Test
  void incompleteAndFailedRepliesAreNotSuccessful() {
    assertFalse(EcosMessage.isComplete("<REPLY get(1,status)>\n1 status[GO]\n<END"));
    EcosMessage message = new EcosMessage("get(1,status)");
    message.addResponse("<REPLY get(1,status)>\n<END 25 (NERROR_NOCONTROL)>\n");
    assertTrue(message.isResponseComplete());
    assertFalse(message.isValid());
    assertEquals(25, message.getErrorCode());
    assertEquals("NERROR_NOCONTROL", message.getResponseCode());
  }

  @Test
  void quotedEmbeddedDoubleQuoteIsPreserved() {
    EcosMessage message = reply("get(1000,name)", "1000 name[\"My \"\"Engine\"\"\"]");
    assertEquals("My \"Engine\"", message.getValueMap().get(Ecos.NAME));
  }
}
