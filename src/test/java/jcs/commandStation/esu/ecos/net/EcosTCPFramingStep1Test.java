package jcs.commandStation.esu.ecos.net;

import java.io.StringWriter;
import jcs.commandStation.esu.ecos.EcosMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EcosTCPFramingStep1Test {

  @Test
  void outgoingCommandsHaveExactlyOneRequiredLineFeed() throws Exception {
    StringWriter out = new StringWriter();
    EcosTCPConnection.writeCommand(out, new EcosMessage("get(1,status)"));
    assertEquals("get(1,status)\n", out.toString());
  }

  @Test
  void staleReplyDoesNotConfirmCurrentRequest() {
    assertFalse(EcosTCPConnection.isReplyFor(
        "<REPLY set(1000,speed[50])>\n<END 0 (OK)>", "set(1000,speed[0])"));
    assertTrue(EcosTCPConnection.isReplyFor(
        "<REPLY get(1, status, status2)>\n1 status[GO]\n<END 0 (OK)>",
        "get(1,status,status2)"));
    assertFalse(EcosTCPConnection.isReplyFor("<EVENT 1>\n<END 0 (OK)>", "get(1,status)"));
  }
}
