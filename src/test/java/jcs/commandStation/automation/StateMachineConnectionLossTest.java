package jcs.commandStation.automation;

import jcs.entities.LocomotiveBean;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.mockito.Mockito.*;

class StateMachineConnectionLossTest {

  @Test
  void suspensionPreservesCurrentStateWithoutResettingRoutes() {
    Dispatcher dispatcher = mock(Dispatcher.class);
    AbstractState state = mock(AbstractState.class);
    StateMachine machine = new StateMachine(dispatcher, state);

    machine.suspendForConnectionLoss();

    assertTrue(machine.isSuspendedForConnectionLoss());
    assertSame(state, machine.getCurrentState());
    assertFalse(machine.isRunning());
    verify(dispatcher, never()).resetRoute(any());
    verify(dispatcher, never()).changeLocomotiveVelocity(anyDouble());
  }

  @Test
  @Timeout(5)
  void runningStateMachineStopsWithoutAttemptingCanVelocityChange() throws Exception {
    Dispatcher dispatcher = mock(Dispatcher.class);
    LocomotiveBean locomotive = mock(LocomotiveBean.class);
    AbstractState state = mock(AbstractState.class);
    when(dispatcher.getThreadGroup()).thenReturn(new ThreadGroup("TEST-RAIL-CONTROLLER"));
    when(dispatcher.getLocomotiveBean()).thenReturn(locomotive);
    when(locomotive.getName()).thenReturn("TEST-LOCO");
    when(state.execute()).thenReturn(state);

    StateMachine machine = new StateMachine(dispatcher, state);
    machine.startStateMachineThread();

    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
    while (!machine.isRunning() && System.nanoTime() < deadline) {
      Thread.sleep(10);
    }
    assertTrue(machine.isRunning(), "State machine did not start");

    machine.suspendForConnectionLoss();
    deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(2);

    while (machine.isRunning() && System.nanoTime() < deadline) {
      Thread.sleep(10);
    }
    assertFalse(machine.isRunning());
    assertSame(state, machine.getCurrentState());
    // The runner must not transmit a speed command across the failed connection.
    Thread.sleep(200);
    verify(dispatcher, never()).changeLocomotiveVelocity(anyDouble());
  }
}
