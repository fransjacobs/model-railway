package jcs.commandStation.marklin.cs.can;

import jcs.entities.AccessoryBean.AccessoryValue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Golden-wire assertions from Märklin CS2 CAN protocol v2.0 pp. 12-15, 30, 37. */
class CanMessageFactoryRecoveryTest {

  @Test
  void zeroVelocityUsesLocVelocityDlcSixAndBigEndianUid() {
    CanMessage message = CanMessageFactory.setLocSpeed(0xC003, 0, 0);
    assertEquals(CanMessage.LOC_VELOCITY, message.getCommand());
    assertEquals(6, message.getDlc());
    assertArrayEquals(new byte[] {0, 0, (byte) 0xC0, 3, 0, 0, 0, 0}, message.getData());
    assertEquals(13, message.getMessage().length);
  }

  @Test
  void systemStopGoAndLocomotiveEmergencyStopUseCorrectSubcommands() {
    CanMessage stop = CanMessageFactory.systemStopGo(false, 0);
    CanMessage go = CanMessageFactory.systemStopGo(true, 0);
    CanMessage emergency = CanMessageFactory.locomotiveEmergencyStop(0xC003, 0);
    assertEquals(CanMessage.SYSTEM_COMMAND, stop.getCommand());
    assertEquals(5, stop.getDlc());
    assertEquals(0, stop.getDataByte(4));
    assertEquals(1, go.getDataByte(4));
    assertEquals(3, emergency.getDataByte(4));
    assertArrayEquals(new byte[] {0, 0, (byte) 0xC0, 3, 3, 0, 0, 0}, emergency.getData());
  }

  @Test
  void pingResponseUsesItsParametersInsteadOfHardcodedJcsValues() {
    CanMessage ping = CanMessageFactory.getMemberPingResponse(0x12345678, 0x0102, 0x5678);
    assertEquals(CanMessage.PING_RESP, ping.getCommand());
    assertArrayEquals(new byte[] {0x12, 0x34, 0x56, 0x78, 1, 2, 0x56, 0x78}, ping.getData());
    assertArrayEquals(CanMessage.generateHash(0x12345678), ping.getHash());
  }

  @Test
  void accessorySupportsMmMultiAspectPositions() {
    assertEquals(0, CanMessageFactory.switchAccessory(1, AccessoryValue.RED, true, 20, 0).getDataByte(4));
    assertEquals(1, CanMessageFactory.switchAccessory(1, AccessoryValue.GREEN, true, 20, 0).getDataByte(4));
    assertEquals(2, CanMessageFactory.switchAccessory(1, AccessoryValue.YELLOW, true, 20, 0).getDataByte(4));
    assertEquals(3, CanMessageFactory.switchAccessory(1, AccessoryValue.WHITE, true, 20, 0).getDataByte(4));
  }

  @Test
  void negativeAndOutOfRangeSpeedsAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> CanMessageFactory.setLocSpeed(1, -1, 0));
    assertThrows(IllegalArgumentException.class, () -> CanMessageFactory.setLocSpeed(1, 1025, 0));
    assertEquals(1024, CanMessage.toInt(new byte[] {
        CanMessageFactory.setLocSpeed(1, 1024, 0).getDataByte(4),
        CanMessageFactory.setLocSpeed(1, 1024, 0).getDataByte(5)}));
  }
}
