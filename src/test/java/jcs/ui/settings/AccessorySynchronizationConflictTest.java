package jcs.ui.settings;

import jcs.entities.AccessoryBean;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccessorySynchronizationConflictTest {

  private static AccessoryBean accessory(String id, int address, AccessoryBean.Protocol protocol) {
    AccessoryBean a = new AccessoryBean();
    a.setId(id);
    a.setAddress(address);
    a.setProtocol(protocol);
    a.setCommandStationId("esu-ecos");
    return a;
  }

  @Test
  void sameAddressAndProtocolConflictDespiteDifferentEcosIds() {
    AccessoryBean s8 = accessory("20005", 8, AccessoryBean.Protocol.DCC);
    AccessoryBean s566 = accessory("20006", 8, AccessoryBean.Protocol.DCC);
    assertEquals(AccessorySettingsPanel.synchronizationAddressKey(s8),
                 AccessorySettingsPanel.synchronizationAddressKey(s566));
  }

  @Test
  void differentProtocolsDoNotConflict() {
    assertNotEquals(
        AccessorySettingsPanel.synchronizationAddressKey(accessory("20000", 1, AccessoryBean.Protocol.DCC)),
        AccessorySettingsPanel.synchronizationAddressKey(accessory("20007", 1, AccessoryBean.Protocol.MM)));
  }

  @Test
  void missingAddressOrProtocolIsNotImportable() {
    AccessoryBean a = new AccessoryBean();
    assertNull(AccessorySettingsPanel.synchronizationAddressKey(a));
    a.setAddress(8);
    assertNull(AccessorySettingsPanel.synchronizationAddressKey(a));
  }
}
