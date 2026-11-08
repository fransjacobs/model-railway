package jcs.commandStation.lifecycle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Lifecycle owner tests independent of real ECoS hardware. */
class EcosStep2LifecycleTest {

  @Test
  void reconnectsAfterRealConnectionLoss() throws Exception {
    AtomicInteger opens = new AtomicInteger();
    CountDownLatch connectedTwice = new CountDownLatch(2);
    try (ConnectionLifecycleManager manager = new ConnectionLifecycleManager(
            "esu-ecos", () -> { opens.incrementAndGet(); return true; },
            () -> {}, new long[] {10L, 20L})) {
      manager.addConnectionStateListener(event -> {
        if (event.state() == ConnectionState.CONNECTED) {
          connectedTwice.countDown();
        }
      });
      manager.start();
      long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
      while (!manager.isConnected() && System.nanoTime() < until) {
        Thread.sleep(5);
      }
      assertTrue(manager.isConnected());
      manager.connectionLost(new IllegalStateException("Transport EOF"));
      assertTrue(connectedTwice.await(3, TimeUnit.SECONDS));
      assertTrue(opens.get() >= 2);
    }
  }

  @Test
  void explicitStopNeverReopensSession() throws Exception {
    AtomicInteger opens = new AtomicInteger();
    try (ConnectionLifecycleManager manager = new ConnectionLifecycleManager(
            "esu-ecos", () -> { opens.incrementAndGet(); return true; },
            () -> {}, new long[] {100L})) {
      manager.start();
      long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
      while (!manager.isConnected() && System.nanoTime() < until) {
        Thread.sleep(5);
      }
      assertTrue(manager.isConnected());
      manager.stop();
      int completed = opens.get();
      Thread.sleep(200);
      assertEquals(completed, opens.get());
      assertEquals(ConnectionState.DISCONNECTED, manager.getState());
    }
  }
}
