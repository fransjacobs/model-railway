package jcs.commandStation;

import java.util.List;
import java.util.function.Supplier;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Regression tests for reconnect recovery ordering and power policy. */
class ReconnectSafetyTest {

  private LocomotiveBean locomotive() {
    LocomotiveBean loco = mock(LocomotiveBean.class);
    when(loco.getDirection()).thenReturn(Direction.values()[0]);
    return loco;
  }

  @Test
  void powerPreviouslyOnStopsBeforeRestoringGoAndResendsZero() {
    DecoderController controller = mock(DecoderController.class);
    when(controller.isPower()).thenReturn(false, true);
    when(controller.power(true)).thenReturn(true);
    LocomotiveBean loco = locomotive();

    boolean success = JCSCommandStation.safelyRestoreAfterReconnect(
        controller, true, () -> List.of(loco), l -> 123);
    assertTrue(success);

    var ordered = inOrder(controller);
    ordered.verify(controller).power(false);
    ordered.verify(controller).isPower();
    ordered.verify(controller).changeVelocity(123, 0, Direction.values()[0]);
    ordered.verify(controller).power(true);
    ordered.verify(controller).isPower();
    ordered.verify(controller).changeVelocity(123, 0, Direction.values()[0]);
    ordered.verifyNoMoreInteractions();
  }

  @Test
  void powerPreviouslyOffIsNeverRestored() {
    DecoderController controller = mock(DecoderController.class);
    when(controller.isPower()).thenReturn(false);
    LocomotiveBean loco = locomotive();

    assertTrue(JCSCommandStation.safelyRestoreAfterReconnect(
        controller, false, () -> List.of(loco), l -> 123));

    verify(controller).power(false);
    verify(controller, never()).power(true);
    verify(controller, times(1)).changeVelocity(123, 0, Direction.values()[0]);
  }

  @Test
  void failedStopNeverEnablesPower() {
    DecoderController controller = mock(DecoderController.class);
    when(controller.isPower()).thenReturn(true);
    @SuppressWarnings("unchecked")
    Supplier<List<LocomotiveBean>> supplier = mock(Supplier.class);

    assertFalse(JCSCommandStation.safelyRestoreAfterReconnect(
        controller, true, supplier, l -> 123));
    verify(controller).power(false);
    verify(controller, never()).power(true);
    verifyNoInteractions(supplier);
  }

  @Test
  void powerRestoreFailureKeepsReconnectPending() {
    DecoderController controller = mock(DecoderController.class);
    when(controller.isPower()).thenReturn(false);
    when(controller.power(true)).thenReturn(false);

    assertFalse(JCSCommandStation.safelyRestoreAfterReconnect(
        controller, true, () -> List.of(locomotive()), l -> 123));

    verify(controller).power(true);
    verify(controller, times(1)).changeVelocity(eq(123), eq(0), any());
  }

  @Test
  void runtimeFailureDoesNotReportSuccessfulReconnect() {
    DecoderController controller = mock(DecoderController.class);
    when(controller.isPower()).thenReturn(false);
    doThrow(new IllegalStateException("transport closed"))
        .when(controller).changeVelocity(eq(123), eq(0), any());

    assertFalse(JCSCommandStation.safelyRestoreAfterReconnect(
        controller, true, () -> List.of(locomotive()), l -> 123));
    verify(controller, never()).power(true);
  }
}
