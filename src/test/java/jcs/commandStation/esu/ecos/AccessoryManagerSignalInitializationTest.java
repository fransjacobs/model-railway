package jcs.commandStation.esu.ecos;

import jcs.entities.AccessoryBean;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression for the S17 startup failure: the ECoS detail reply lists
 * state[2] before gates[3], although the manager initially knows no gates.
 */
class AccessoryManagerSignalInitializationTest {

  private static EcosMessage reply(String command, String body) {
    EcosMessage message = new EcosMessage(command);
    message.addResponse("<REPLY " + command + ">\n"
            + body + "<END 0 (OK)>\n");
    return message;
  }

  private static AccessoryManager initialManager() {
    return new AccessoryManager(null,
            reply("queryObjects(11,name1,addr,protocol,mode,symbol)",
                    "20016 name1[\"S 17\"] addr[17] protocol[DCC] mode[SWITCH] symbol[11]\n"
                    + "20028 name1[\"W101\"] addr[101] protocol[DCC] mode[SWITCH] symbol[2]\n"));
  }

  @Test
  void signalStateBeforeGatesDoesNotAbortInitialization() {
    AccessoryManager manager = initialManager();
    AccessoryBean signal = manager.getAccessories().get("20016");

    assertNotNull(signal);
    assertTrue(signal.isSignal());
    assertNull(signal.getStates(), "queryObjects does not include gates");

    assertDoesNotThrow(() -> manager.update(reply("get(20016,state,gates)",
            "20016 state[2]\n"
            + "20016 gates[3]\n")));

    assertEquals(3, signal.getStates());
    assertEquals("lichtsignal_HP012", signal.getType());
    assertNotNull(signal.getAccessoryValue());
  }

  @Test
  void signalStateWithoutGatesDoesNotUnboxNull() {
    AccessoryManager manager = initialManager();

    assertDoesNotThrow(() -> manager.update(reply("get(20016,state)",
            "20016 state[2]\n")));

    assertNull(manager.getAccessories().get("20016").getStates(),
            "Do not invent missing ECoS metadata");
  }

  @Test
  void threeWayTurnoutStillParsesItsState() {
    AccessoryManager manager = initialManager();

    assertDoesNotThrow(() -> manager.update(reply("get(20028,state,gates)",
            "20028 state[2]\n"
            + "20028 gates[3]\n")));

    AccessoryBean turnout = manager.getAccessories().get("20028");
    assertTrue(turnout.is3WaySwitch());
    assertEquals(3, turnout.getStates());
    assertEquals(AccessoryBean.AccessoryValue.RED, turnout.getAccessoryValue());
  }
}
