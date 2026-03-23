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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.marklin.cs.can.CanMessage;
import org.tinylog.Logger;

public class CSTCPNioConnection implements CSConnection, AutoCloseable {

  private static final int CS_PORT = 15731; // Standaard Märklin poort
  private static final int CAN_FRAME_SIZE = 13;
  private static final Duration SHORT_TIMEOUT = Duration.ofMillis(1000);

  private final InetSocketAddress address;
  private SocketChannel clientChannel;
  private final BlockingQueue<CanMessage> eventQueue = new LinkedBlockingQueue<>();
  private final Map<Integer, CompletableFuture<CanMessage>> pendingRequests = new ConcurrentHashMap<>();
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final List<ConnectionEventListener> disconnectionEventListeners;

  public CSTCPNioConnection(InetAddress csAddress) {
    this.address = new InetSocketAddress(csAddress, CS_PORT);
    disconnectionEventListeners = new ArrayList<>();

    connect();
  }

  private void connect() {
    try {
      clientChannel = SocketChannel.open(address);
      clientChannel.configureBlocking(true); // Blocking mode is prima i.c.m. Virtual Threads
      running.set(true);

      // Start de receiver in een Virtual Thread
      Thread.ofVirtual().name("CS-CAN-Receiver").start(this::receiveLoop);

      Logger.info("Verbonden met Central Station op {}", address);
    } catch (IOException e) {
      Logger.error("Verbinding mislukt: {}", e.getMessage());
    }
  }

  @Override
  public CanMessage sendCanMessage(CanMessage message) {
    if (message == null || !clientChannel.isOpen()) {
      return null;
    }

    int responseCmd = message.getCommand() + 1;
    CompletableFuture<CanMessage> future = new CompletableFuture<>();

    if (message.expectsResponse()) {
      pendingRequests.put(responseCmd, future);
    }

    try {
      ByteBuffer buffer = ByteBuffer.wrap(message.getMessage());
      while (buffer.hasRemaining()) {
        clientChannel.write(buffer);
      }

      if (message.expectsResponse()) {
        // Wacht op antwoord zonder de thread te blokkeren (non-blocking wait)
        return future.get(SHORT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
      }
    } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
      Logger.error("Fout bij verzenden/ontvangen: {}", e.getMessage());
      pendingRequests.remove(responseCmd);
    }

    return message;
  }

  private void receiveLoop() {
    ByteBuffer headerBuffer = ByteBuffer.allocate(CAN_FRAME_SIZE);

    while (running.get() && clientChannel.isOpen()) {
      try {
        headerBuffer.clear();
        // NIO garandeert dat we precies lezen wat we nodig hebben
        while (headerBuffer.hasRemaining()) {
          if (clientChannel.read(headerBuffer) == -1) {
            throw new IOException("Connection closed");
          }
        }
        headerBuffer.flip();

        CanMessage rx = parseByteToCan(headerBuffer);

        // Afhandeling van antwoorden vs events
        CompletableFuture<CanMessage> waitingRequest = pendingRequests.remove(rx.getCommand());
        if (waitingRequest != null) {
          waitingRequest.complete(rx);
        } else {
          eventQueue.offer(rx);
        }

      } catch (IOException e) {
        if (running.get()) {
          Logger.error("Ontvangst fout: {}", e.getMessage());
          close();
        }
      }
    }
  }

  private CanMessage parseByteToCan(ByteBuffer buffer) {
    int prio = buffer.get() & 0xFF;
    int cmd = buffer.get() & 0xFF;
    int hash = buffer.getShort() & 0xFFFF;
    int dlc = buffer.get() & 0xFF;
    byte[] data = new byte[8];
    buffer.get(data);
    return new CanMessage(prio, cmd, hash, dlc, data);
  }

  @Override
  public void close() {
    running.set(false);
    try {
      if (clientChannel != null) {
        clientChannel.close();
      }
      pendingRequests.values().forEach(f -> f.cancel(true));
      pendingRequests.clear();
    } catch (IOException e) {
      Logger.trace(e);
    }
  }

  @Override
  public BlockingQueue<CanMessage> getEventQueue() {
    return eventQueue;
  }

  // ... Overige interface methodes (getControllerAddress, etc)
  @Override
  public InetAddress getControllerAddress() {
    return this.address.getAddress();
  }

  @Override
  public boolean isConnected() {
    return running.get();
  }

  @Override
  public void addDisconnectionEventListener(ConnectionEventListener listener) {
    this.disconnectionEventListeners.add(listener);
  }

}
