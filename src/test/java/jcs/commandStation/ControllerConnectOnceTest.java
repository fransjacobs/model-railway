package jcs.commandStation;

import java.util.IdentityHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Connection retries belong to ConnectionLifecycleManager, not to each
 * decoder/accessory/feedback role sharing one physical controller.
 */
class ControllerConnectOnceTest {

  @Test
  void failedSharedControllerIsAttemptedOnlyOncePerLifecycleAttempt() {
    GenericController shared = mock(GenericController.class);
    when(shared.isConnected()).thenReturn(false);
    when(shared.connect()).thenReturn(false);

    Map<GenericController, Boolean> attempts = new IdentityHashMap<>();
    assertFalse(JCSCommandStation.connectControllerOnce(shared, attempts)); // decoder
    assertFalse(JCSCommandStation.connectControllerOnce(shared, attempts)); // accessory
    assertFalse(JCSCommandStation.connectControllerOnce(shared, attempts)); // feedback

    verify(shared, times(1)).connect();
  }

  @Test
  void successfulSharedControllerIsAttemptedOnlyOnce() {
    GenericController shared = mock(GenericController.class);
    when(shared.isConnected()).thenReturn(false);
    when(shared.connect()).thenReturn(true);

    Map<GenericController, Boolean> attempts = new IdentityHashMap<>();
    assertTrue(JCSCommandStation.connectControllerOnce(shared, attempts));
    assertTrue(JCSCommandStation.connectControllerOnce(shared, attempts));
    assertTrue(JCSCommandStation.connectControllerOnce(shared, attempts));
    verify(shared, times(1)).connect();
  }

  @Test
  void separatePhysicalControllersEachConnectOnce() {
    GenericController decoder = mock(GenericController.class);
    GenericController accessory = mock(GenericController.class);
    when(decoder.connect()).thenReturn(false);
    when(accessory.connect()).thenReturn(true);

    Map<GenericController, Boolean> attempts = new IdentityHashMap<>();
    assertFalse(JCSCommandStation.connectControllerOnce(decoder, attempts));
    assertTrue(JCSCommandStation.connectControllerOnce(accessory, attempts));
    assertFalse(JCSCommandStation.connectControllerOnce(decoder, attempts));
    verify(decoder, times(1)).connect();
    verify(accessory, times(1)).connect();
  }

  @Test
  void newLifecycleAttemptMayTryAgain() {
    GenericController controller = mock(GenericController.class);
    when(controller.connect()).thenReturn(false, true);

    assertFalse(JCSCommandStation.connectControllerOnce(controller, new IdentityHashMap<>()));
    assertTrue(JCSCommandStation.connectControllerOnce(controller, new IdentityHashMap<>()));
    verify(controller, times(2)).connect();
  }

  @Test
  void alreadyConnectedControllerDoesNotReconnect() {
    GenericController controller = mock(GenericController.class);
    when(controller.isConnected()).thenReturn(true);

    assertTrue(JCSCommandStation.connectControllerOnce(controller, new IdentityHashMap<>()));
    verify(controller, never()).connect();
  }
}
