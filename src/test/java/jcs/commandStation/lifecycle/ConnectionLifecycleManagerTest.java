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
package jcs.commandStation.lifecycle;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ConnectionLifecycleManagerTest {

  @Test
  @Timeout(10)
  void retriesFailuresAndPublishesConnected() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    CountDownLatch connected = new CountDownLatch(1);
    try (var manager = new ConnectionLifecycleManager("test", () -> calls.incrementAndGet() >= 3,
            () -> {
            }, new long[]{20, 20})) {
      manager.addConnectionStateListener(e -> {
        if (e.state() == ConnectionState.CONNECTED) {
          connected.countDown();
        }
      });
      manager.start();
      assertTrue(connected.await(3, TimeUnit.SECONDS));
      assertEquals(3, calls.get());
      assertTrue(manager.isConnected());
    }
  }

  @Test
  @Timeout(10)
  void lostConnectionReestablishesAndClosePreventsFurtherRetries() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    CountDownLatch connectedTwice = new CountDownLatch(2);
    var manager = new ConnectionLifecycleManager("test", () -> {
      calls.incrementAndGet();
      return true;
    }, () -> {
    }, new long[]{20});
    try {
      manager.addConnectionStateListener(e -> {
        if (e.state() == ConnectionState.CONNECTED) {
          connectedTwice.countDown();
        }
      });
      manager.start();
      awaitState(manager, ConnectionState.CONNECTED);
      manager.connectionLost(new IllegalStateException("EOF"));
      assertTrue(connectedTwice.await(3, TimeUnit.SECONDS));
      assertEquals(2, calls.get());
    } finally {
      manager.close();
    }
    manager.connectionLost(new IllegalStateException("after close"));
    manager.start();
    Thread.sleep(80);
    assertEquals(2, calls.get());
    assertEquals(ConnectionState.STOPPING, manager.getState());
  }

  @Test
  @Timeout(10)
  void manualStopCancelsRetries() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    try (var manager = new ConnectionLifecycleManager("test", () -> {
      calls.incrementAndGet();
      return false;
    }, () -> {
    }, new long[]{500})) {
      manager.start();
      awaitState(manager, ConnectionState.RECONNECTING);
      manager.stop();
      Thread.sleep(650);
      assertEquals(1, calls.get());
      assertEquals(ConnectionState.DISCONNECTED, manager.getState());
    }
  }

  @Test
  @Timeout(10)
  void stoppingAnInFlightAttemptMustNotPublishConnected() throws Exception {
    CountDownLatch entered = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    AtomicInteger disconnects = new AtomicInteger();
    AtomicInteger connectedEvents = new AtomicInteger();
    var manager = new ConnectionLifecycleManager("test", () -> {
      entered.countDown();
      release.await(3, TimeUnit.SECONDS);
      return true;
    }, disconnects::incrementAndGet, new long[]{20});
    try {
      manager.addConnectionStateListener(event -> {
        if (event.state() == ConnectionState.CONNECTED) {
          connectedEvents.incrementAndGet();
        }
      });
      manager.start();
      assertTrue(entered.await(2, TimeUnit.SECONDS));
      manager.stop();
      release.countDown();
      Thread.sleep(150);
      assertEquals(ConnectionState.DISCONNECTED, manager.getState());
      assertEquals(0, connectedEvents.get());
      assertTrue(disconnects.get() >= 1);
    } finally {
      release.countDown();
      manager.close();
    }
  }

  private static void awaitState(ConnectionLifecycleManager manager, ConnectionState expected)
          throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
    while (manager.getState() != expected && System.nanoTime() < deadline) {
      Thread.sleep(5);
    }
    assertEquals(expected, manager.getState());
  }
}
