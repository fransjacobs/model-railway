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
package jcs.commandStation.marklin.cs.net;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import jcs.commandStation.marklin.cs.can.CanMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for shutdown of the Märklin CAN-over-TCP transport.
 *
 * Uses a local fake Central Station on the fixed Märklin TCP port (15731).<br>
 * These tests must not run in parallel with another test using that port.
 */
class TcpShutdownTest {

  private static final InetAddress LOOPBACK = InetAddress.getLoopbackAddress();

  @Test
  @DisplayName("Remote TCP EOF notifies once and can be closed from the listener")
  @Timeout(10)
  void unexpectedCloseNotifiesOnceWithoutDeadlock() throws Exception {
    try (ServerSocket server = new ServerSocket(CSConnection.CS_RX_PORT, 1, LOOPBACK)) {
      server.setSoTimeout(3_000);
      CSTCPConnection connection = new CSTCPConnection(LOOPBACK);
      try (Socket peer = server.accept()) {
        CountDownLatch callbackFinished = new CountDownLatch(1);
        AtomicInteger callbacks = new AtomicInteger();
        AtomicReference<Throwable> callbackFailure = new AtomicReference<>();

        connection.addDisconnectionEventListener(event -> {
          try {
            callbacks.incrementAndGet();
            // Previously could trigger a receiver-thread self-join deadlock.
            connection.close();
          } catch (Throwable error) {
            callbackFailure.set(error);
          } finally {
            callbackFinished.countDown();
          }
        });

        peer.close(); // Simulate the Central Station being powered off.

        assertTrue(callbackFinished.await(3, TimeUnit.SECONDS),
                "No disconnection callback after remote EOF");
        assertNull(callbackFailure.get(), "Disconnect callback must not fail");
        assertEquals(1, callbacks.get(), "Disconnect should only be reported once");
        assertFalse(connection.isConnected(), "Connection still reports connected");
        connection.close(); // Idempotent: closing twice must be safe.
      } finally {
        connection.close();
      }
    }
  }

  @Test
  @DisplayName("Intentional close releases a waiting CAN sender without a loss event")
  @Timeout(10)
  void intentionalCloseReleasesWaitingSenderWithoutNotification() throws Exception {
    try (ServerSocket server = new ServerSocket(CSConnection.CS_RX_PORT, 1, LOOPBACK)) {
      server.setSoTimeout(3_000);
      CSTCPConnection connection = new CSTCPConnection(LOOPBACK);
      try (Socket peer = server.accept()) {
        CountDownLatch unexpectedEvent = new CountDownLatch(1);
        connection.addDisconnectionEventListener(event -> unexpectedEvent.countDown());

        AtomicReference<Throwable> senderFailure = new AtomicReference<>();
        Thread sender = new Thread(() -> {
          try {
            CanMessage request = new CanMessage(
                    0, CanMessage.LOC_VELOCITY, 0, 4, new byte[CanMessage.DATA_SIZE]);
            connection.sendCanMessage(request);
          } catch (Throwable error) {
            senderFailure.set(error);
          }
        }, "CAN-SENDER-TEST");
        sender.setDaemon(true); // A regression must not prevent the test JVM from exiting.
        sender.start();

        peer.setSoTimeout(3_000);
        byte[] sentMessage = peer.getInputStream().readNBytes(CanMessage.MESSAGE_SIZE);
        assertEquals(CanMessage.MESSAGE_SIZE, sentMessage.length, "CAN request was not sent");

        // No reply is sent, so the sender is waiting for its response.
        connection.close();
        sender.join(1_500);

        assertFalse(sender.isAlive(), "CAN sender remained blocked after close");
        assertNull(senderFailure.get(), "CAN sender must not throw");
        assertFalse(unexpectedEvent.await(200, TimeUnit.MILLISECONDS),
                "Intentional close must not publish a lost-connection event");
      } finally {
        connection.close();
      }
    }
  }
}
