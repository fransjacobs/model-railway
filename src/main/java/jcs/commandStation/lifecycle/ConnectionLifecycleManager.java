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

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connection/retry coordinator for ONE command station.
 *
 * The connector is responsible for discovery, opening the transport and fully initializing its command station.<br>
 * It returns true only when ready. It must use finite network timeouts and honor interruption.
 *
 * No transport or Swing dependencies. Call connectionLost() from a network callback; it never waits for the connector or closes sockets on that thread.
 */
public final class ConnectionLifecycleManager implements AutoCloseable {

  @FunctionalInterface
  public interface Connector {

    boolean connect() throws Exception;
  }

  private final String id;
  private final Connector connector;
  private final Runnable disconnect;
  private final ScheduledExecutorService scheduler;
  private final CopyOnWriteArrayList<ConnectionStateListener> listeners = new CopyOnWriteArrayList<>();
  private final Object lock = new Object();
  private final long[] retryDelaysMillis;
  private final AtomicBoolean shutdown = new AtomicBoolean();
  private volatile Thread workerThread;
  private volatile ConnectionState state = ConnectionState.DISCONNECTED;
  private ScheduledFuture<?> scheduled;
  private boolean desiredConnected;
  private boolean attemptInProgress;
  private long generation;
  private int failures;

  public ConnectionLifecycleManager(String id, Connector connector, Runnable disconnect) {
    this(id, connector, disconnect, new long[]{1000, 2000, 5000, 10000, 30000});
  }

  /**
   * Package-visible for deterministic tests and configurable backoff.
   */
  ConnectionLifecycleManager(String id, Connector connector, Runnable disconnect, long[] delays) {
    this.id = Objects.requireNonNull(id);
    this.connector = Objects.requireNonNull(connector);
    this.disconnect = Objects.requireNonNull(disconnect);
    if (delays == null || delays.length == 0) {
      throw new IllegalArgumentException("delays required");
    }
    for (long delay : delays) {
      if (delay < 0) {
        throw new IllegalArgumentException("negative delay");
      }
    }
    this.retryDelaysMillis = delays.clone();
    ThreadFactory factory = task -> {
      Thread thread = new Thread(task, "JCS-CONNECTION-" + id);
      thread.setDaemon(true);
      workerThread = thread;
      return thread;
    };
    scheduler = Executors.newSingleThreadScheduledExecutor(factory);
  }

  public ConnectionState getState() {
    return state;
  }

  public boolean isConnected() {
    return state == ConnectionState.CONNECTED;
  }

  public void addConnectionStateListener(ConnectionStateListener listener) {
    listeners.add(Objects.requireNonNull(listener));
  }

  public void removeConnectionStateListener(ConnectionStateListener listener) {
    listeners.remove(listener);
  }

  /**
   * Request a connection, idempotently.
   */
  public void start() {
    synchronized (lock) {
      if (shutdown.get() || desiredConnected) {
        return;
      }
      desiredConnected = true;
      failures = 0;
      generation++;
      scheduleLocked(0);
    }
  }

  /**
   * Report a lost live connection; safe to call from the transport receiver.
   */
  public void connectionLost(Throwable cause) {
    synchronized (lock) {
      if (shutdown.get() || !desiredConnected || state != ConnectionState.CONNECTED) {
        return;
      }
      generation++;
      failures = 0;
      publish(ConnectionState.RECONNECTING, cause == null ? "Connection lost" : cause.getMessage());
      // Cleanup happens on the scheduler, not on the network callback thread.
      long token = generation;
      scheduler.execute(() -> {
        if (!current(token)) {
          return;
        }
        safeDisconnect();
        synchronized (lock) {
          if (current(token)) {
            scheduleLocked(retryDelaysMillis[0]);
          }
        }
      });
    }
  }

  /**
   * Manually disconnect and suppress automatic retries. May block while the connector's close operation finishes; callers should not call on the EDT.
   */
  public void stop() {
    synchronized (lock) {
      if (shutdown.get()) {
        return;
      }
      desiredConnected = false;
      generation++;
      if (scheduled != null) {
        scheduled.cancel(false);
      }
      publish(ConnectionState.DISCONNECTED, "Disconnected by user");
    }
    safeDisconnect();
  }

  private void scheduleLocked(long delayMillis) {
    if (shutdown.get() || !desiredConnected) {
      return;
    }
    long token = generation;
    scheduled = scheduler.schedule(() -> attempt(token), delayMillis, TimeUnit.MILLISECONDS);
  }

  private boolean current(long token) {
    synchronized (lock) {
      return !shutdown.get() && desiredConnected && generation == token;
    }
  }

  private void attempt(long token) {
    synchronized (lock) {
      if (!current(token) || attemptInProgress) {
        return;
      }
      attemptInProgress = true;
      publish(failures == 0 ? ConnectionState.CONNECTING : ConnectionState.RECONNECTING,
              "Connecting");
    }
    boolean success = false;
    String error = null;
    try {
      success = connector.connect();
      if (!success) {
        error = "Connection attempt failed";
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      error = "Connection interrupted";
    } catch (Exception e) {
      error = e.getMessage() == null ? e.toString() : e.getMessage();
    } finally {
      synchronized (lock) {
        attemptInProgress = false;
      }
    }
    if (!current(token)) {
      // A late successful connect must not resurrect an intentionally closed session.
      if (success) {
        safeDisconnect();
      }
      return;
    }
    if (success) {
      synchronized (lock) {
        if (!current(token)) {
          safeDisconnect();
          return;
        }
        failures = 0;
        publish(ConnectionState.CONNECTED, "Connected");
      }
    } else {
      safeDisconnect();
      synchronized (lock) {
        if (!current(token)) {
          return;
        }
        int delayIndex = Math.min(failures, retryDelaysMillis.length - 1);
        failures++;
        publish(ConnectionState.RECONNECTING, error);
        scheduleLocked(retryDelaysMillis[delayIndex]);
      }
    }
  }

  private void safeDisconnect() {
    try {
      disconnect.run();
    } catch (RuntimeException e) {
      // Cleanup should continue even when the driver reports an exception.
      System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
              "Error closing command station " + id, e);
    }
  }

  private void publish(ConnectionState next, String message) {
    ConnectionState old = state;
    if (old == next) {
      return;
    }
    state = next;
    ConnectionStateEvent event
            = new ConnectionStateEvent(id, old, next, message, Instant.now());
    for (ConnectionStateListener listener : listeners) {
      try {
        listener.onConnectionStateChanged(event);
      } catch (RuntimeException ex) {
        System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                "Connection state listener failed", ex);
      }
    }
  }

  /**
   * Terminal shutdown: no reconnects can be scheduled afterward.
   */
  @Override
  public void close() {
    if (!shutdown.compareAndSet(false, true)) {
      return;
    }
    synchronized (lock) {
      desiredConnected = false;
      generation++;
      if (scheduled != null) {
        scheduled.cancel(true);
      }
      publish(ConnectionState.STOPPING, "Application shutting down");
    }
    scheduler.shutdownNow();
    safeDisconnect(); // unblock socket operations before waiting on worker
    if (Thread.currentThread() == workerThread) {
      return;
    }
    try {
      if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
        System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                "Connection worker did not stop within 2 seconds: " + id);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
