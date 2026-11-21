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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CAN heartbeat regression tests. Uses TCP port 15731, so do not run
 * concurrently with TcpShutdownTest or a real CS listener on this port.
 */
class TcpHeartbeatTest {

  private static final InetAddress LOOPBACK = InetAddress.getLoopbackAddress();

  @Test
  @Timeout(10)
  void silentPeerTriggersLossNotification() throws Exception {
    try (ServerSocket server = new ServerSocket(CSConnection.CS_RX_PORT, 1, LOOPBACK)) {
      server.setSoTimeout(3_000);
      CSTCPConnection connection = new CSTCPConnection(LOOPBACK);
      try (Socket peer = server.accept()) {
        CountDownLatch lost = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger();
        connection.addDisconnectionEventListener(event -> {
          count.incrementAndGet();
          lost.countDown();
        });

        long start = System.nanoTime();
        // Keep TCP open but never answer CAN ping: the same symptom as a
        // cable pull where no FIN/RST packet reaches the client.
        assertTrue(lost.await(5, TimeUnit.SECONDS),
                "Silent TCP connection was not detected as lost");
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertTrue(elapsedMs < 4_000,
                "Detection too slow on an idle connection: " + elapsedMs + " ms");
        assertEquals(1, count.get());
      } finally {
        connection.close();
      }
    }
  }

  @Test
  @Timeout(10)
  void pingRepliesKeepConnectionHealthyAndCloseStopsHeartbeat() throws Exception {
    try (ServerSocket server = new ServerSocket(CSConnection.CS_RX_PORT, 1, LOOPBACK)) {
      server.setSoTimeout(3_000);
      CSTCPConnection connection = new CSTCPConnection(LOOPBACK);
      try (Socket peer = server.accept()) {
        peer.setSoTimeout(3_000);
        AtomicInteger lost = new AtomicInteger();
        connection.addDisconnectionEventListener(event -> lost.incrementAndGet());

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        int replies = 0;
        while (replies < 3 && System.nanoTime() < deadline) {
          byte[] request = peer.getInputStream().readNBytes(13);
          assertEquals(13, request.length);
          assertEquals(0x30, request[1] & 0xff, "Expected CAN participant ping");

          byte[] answer = new byte[13];
          answer[1] = 0x31;   // PING_RESP
          answer[4] = 8;      // Required DLC
          answer[5] = 1;      // Arbitrary device UID and version fields
          peer.getOutputStream().write(answer);
          peer.getOutputStream().flush();
          replies++;
        }
        assertEquals(3, replies);
        assertTrue(connection.isConnected());
        assertEquals(0, lost.get());

        connection.close();
        assertFalse(connection.isConnected());
        assertEquals(0, lost.get(), "Intentional close must not report loss");
      } finally {
        connection.close();
      }
    }
  }
}
