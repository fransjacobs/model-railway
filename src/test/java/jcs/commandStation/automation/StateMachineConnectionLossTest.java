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
package jcs.commandStation.automation;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import jcs.entities.LocomotiveBean;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
  @Timeout(10)
  void runningStateMachineStopsWithoutAttemptingCanVelocityChange() throws Exception {
    Dispatcher dispatcher = mock(Dispatcher.class);
    RailController railController = mock(RailController.class);
    LocomotiveBean locomotive = mock(LocomotiveBean.class);
    IdleState idle = mock(IdleState.class);
    AbstractState state = mock(AbstractState.class);

    when(dispatcher.getRailController()).thenReturn(railController);
    when(railController.isAutoModeActive()).thenReturn(true);
    when(dispatcher.getThreadGroup()).thenReturn(new ThreadGroup("TEST-RAIL-CONTROLLER"));
    when(dispatcher.getLocomotiveBean()).thenReturn(locomotive);
    when(locomotive.getName()).thenReturn("TEST-LOCO");
    when(idle.getName()).thenReturn("Idle");
    when(idle.execute()).thenReturn(state);
    when(state.getName()).thenReturn("Running");
    when(state.canStopLocomotive()).thenReturn(false);

    CountDownLatch enteredRunning = new CountDownLatch(1);
    AtomicReference<Thread> runner = new AtomicReference<>();
    when(state.execute()).thenAnswer(invocation -> {
      runner.set(Thread.currentThread());
      enteredRunning.countDown();
      return state;
    });

    StateMachine machine = new StateMachine(dispatcher, idle);
    try {
      machine.startStateMachineThread();

      assertTrue(enteredRunning.await(2, TimeUnit.SECONDS),
              "State machine did not reach the running state");
      assertTrue(machine.isRunning());
      assertSame(state, machine.getCurrentState());

      machine.suspendForConnectionLoss();

      // isRunning() reports false immediately upon suspension.
      // Join the actual runner before checking its exit behaviour.
      Thread thread = runner.get();
      assertNotNull(thread);
      thread.join(2500);
      assertFalse(thread.isAlive(), "State machine runner did not terminate");
      assertFalse(machine.isRunning());
      assertTrue(machine.isSuspendedForConnectionLoss());
      assertSame(state, machine.getCurrentState());
      verify(dispatcher, never()).resetRoute(any());
      verify(dispatcher, never()).changeLocomotiveVelocity(anyDouble());
    } finally {
      machine.suspendForConnectionLoss();
    }
  }
}
