package jcs.commandStation.loconet.connection;

import com.fazecast.jSerialComm.SerialPort;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Hardware-free tests for port selection. Retry scheduling is tested in the
 * existing ConnectionLifecycleManagerTest, not in the serial factory.
 */
class LoconetConnectionFactoryTest {

  @Test
  void autoSelectsOnlyIntelliboxUsbIds() {
    SerialPort ib = port("cu.Intellibox", 4292, 60000);
    SerialPort other = port("cu.Other", 1, 2);

    List<SerialPort> selected =
            LoconetConnectionFactory.matchingPorts(new SerialPort[]{other, ib}, "AUTO");

    assertEquals(List.of(ib), selected);
  }

  @Test
  void explicitSerialNameIsHonoredEvenWithoutUsbIds() {
    SerialPort ib = port("cu.Intellibox", 4292, 60000);
    SerialPort adapter = port("cu.SerialAdapter", 10, 20);

    List<SerialPort> selected =
            LoconetConnectionFactory.matchingPorts(new SerialPort[]{ib, adapter}, "cu.SerialAdapter");

    assertEquals(List.of(adapter), selected);
  }

  @Test
  void absentPortDoesNotTriggerFallbackToAnotherDevice() {
    SerialPort ib = port("cu.Intellibox", 4292, 60000);

    assertTrue(LoconetConnectionFactory.matchingPorts(
            new SerialPort[]{ib}, "cu.Missing").isEmpty());
  }

  private static SerialPort port(String name, int vendor, int product) {
    SerialPort port = mock(SerialPort.class);
    when(port.getSystemPortName()).thenReturn(name);
    when(port.getVendorID()).thenReturn(vendor);
    when(port.getProductID()).thenReturn(product);
    return port;
  }
}
