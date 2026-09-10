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
package jcs.commandStation.loconet.connection;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import jcs.commandStation.loconet.LoconetMessage;
import jcs.commandStation.loconet.LoconetMessageParser;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import org.tinylog.Logger;

/**
 * Loconet Connection Implementation for the Uhlenbrock Intellibox 2
 */
class IntelliboxConnectionImpl implements LoconetConnection {

  private final SerialPort serialPort;

  private static final long DEFAULT_ECHO_TIMEOUT_MS = Long.getLong("loconet.echo.timeout.ms", 250L);
  private static final long DEFAULT_REPLY_TIMEOUT_MS = Long.getLong("loconet.reply.timeout.ms", 500L);
  private static final boolean DEBUG = System.getProperty("message.debug", "false").equalsIgnoreCase("true");

  private OutputStream output;
  private LoconetMessageReceiver loconetMessageReceiver;
  private final BlockingQueue<LoconetMessage> messagesQueue;

  private final Object echoMonitor = new Object();
  private LoconetMessage expectedEcho;
  private LoconetMessage receivedEcho;

  private final ExecutorService txExecutor;
  private final Object writeMonitor = new Object();

  private final List<PendingMessage> pendingMessages;

  IntelliboxConnectionImpl(SerialPort serialPort) {
    this.messagesQueue = new LinkedBlockingQueue<>();
    this.serialPort = serialPort;
    output = serialPort.getOutputStream();
    pendingMessages = new CopyOnWriteArrayList<>();

    initReceiver();

    this.txExecutor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "INBX-LN-TX");
      thread.setDaemon(true);
      return thread;
    });

  }

  private void initReceiver() {
    loconetMessageReceiver = new LoconetMessageReceiver(serialPort);
    loconetMessageReceiver.start();
  }

  @Override
  public boolean isConnected() {
    return serialPort.isOpen() && loconetMessageReceiver != null && loconetMessageReceiver.isRunning();
  }

  @Override
  public BlockingQueue<LoconetMessage> getMessageQueue() {
    return this.messagesQueue;
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      Logger.trace(e.getMessage());
    }
  }

  @Override
  public void close() {
    try {
      txExecutor.shutdownNow();

      messagesQueue.clear();
      clearPendingEcho();

      if (output != null) {
        output.flush();
      }

      if (loconetMessageReceiver != null) {
        loconetMessageReceiver.quit();
        try {
          loconetMessageReceiver.join(1000L);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          Logger.trace("Interrupted while waiting for receiver shutdown. {}", e.getMessage());
        }
      }

      if (output != null) {
        output.close();
      }

      loconetMessageReceiver = null;
      output = null;

    } catch (IOException e) {
      Logger.trace("Error while closing. {}", e.getMessage());
    }

    Logger.debug("Connection closed");
  }

  @Override
  public LoconetMessage sendMessage(LoconetMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (output == null || !isConnected()) {
      Logger.warn("Cannot send LocoNet message; connection is not open.");
      return null;
    }

    PendingMessage echoPending = null;

    try {
      echoPending = registerPendingMessage(received -> received.sameMessage(message), true, DEFAULT_ECHO_TIMEOUT_MS);

      Logger.trace("TX: {}", message);

      synchronized (writeMonitor) {
        output.write(message.getMessageBytes());
        output.flush();
      }

      return echoPending.future.get(DEFAULT_ECHO_TIMEOUT_MS + 50, TimeUnit.MILLISECONDS);

    } catch (IOException | InterruptedException | ExecutionException | TimeoutException ex) {
      Logger.error("Could not send LocoNet message: {}", ex.getMessage());
      return null;
    } finally {
      if (echoPending != null) {
        pendingMessages.remove(echoPending);
      }
    }
  }

  @Override
  public void sendMessageNoWait(LoconetMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (output == null || !isConnected()) {
      Logger.warn("Cannot send LocoNet message; connection is not open.");
      return;
    }

    try {
      Logger.trace("TX no-wait: {}", message);

      synchronized (writeMonitor) {
        output.write(message.getMessageBytes());
        output.flush();
      }

    } catch (IOException ex) {
      Logger.error("Could not send LocoNet message: {}", ex.getMessage());
    }
  }

  private void registerExpectedEcho(LoconetMessage sent) {
    synchronized (echoMonitor) {
      expectedEcho = sent;
      receivedEcho = null;
    }
  }

  private void clearPendingEcho() {
    synchronized (echoMonitor) {
      expectedEcho = null;
      receivedEcho = null;
      echoMonitor.notifyAll();
    }
  }

  @Override
  public void sendMessageNoWaitConsumeEcho(LoconetMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (output == null || !isConnected()) {
      Logger.warn("Cannot send LocoNet message; connection is not open.");
      return;
    }

    try {
      registerPendingMessage(received -> received.sameMessage(message), true, DEFAULT_ECHO_TIMEOUT_MS);

      Logger.trace("TX no-wait consume-echo: {}", message);

      synchronized (writeMonitor) {
        output.write(message.getMessageBytes());
        output.flush();
      }

    } catch (IOException ex) {
      Logger.error("Could not send LocoNet message: {}", ex.getMessage());
    }
  }

  @Override
  public CompletableFuture<LoconetMessage> sendMessageAsyncAwaitEcho(LoconetMessage message) {
    return CompletableFuture.supplyAsync(() -> sendMessage(message), txExecutor);
  }

  private LoconetMessage waitForEcho(long timeoutMillis) {
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);

    synchronized (echoMonitor) {
      while (receivedEcho == null) {
        if (expectedEcho == null) {
          return null;
        }
        long remainingNanos = deadline - System.nanoTime();
        if (remainingNanos <= 0L) {
          expectedEcho = null;
          return null;
        }

        try {
          TimeUnit.NANOSECONDS.timedWait(echoMonitor, remainingNanos);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          expectedEcho = null;
          Logger.trace("Interrupted while waiting for LocoNet echo. {}", e.getMessage());
          return null;
        }
      }

      LoconetMessage echo = receivedEcho;
      expectedEcho = null;
      receivedEcho = null;
      return echo;
    }
  }

  @Override
  public LoconetMessage sendMessageAwaitEchoAndReply(LoconetMessage message, Predicate<LoconetMessage> replyMatcher, long replyTimeoutMillis) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (replyMatcher == null) {
      throw new IllegalArgumentException("replyMatcher may not be null");
    }

    if (output == null || !isConnected()) {
      Logger.warn("Cannot send LocoNet message; connection is not open.");
      return null;
    }

    PendingMessage echoPending = null;
    PendingMessage replyPending = null;

    try {
      echoPending = registerPendingMessage(received -> received.sameMessage(message), true, DEFAULT_ECHO_TIMEOUT_MS);

      replyPending = registerPendingMessage(replyMatcher, true, replyTimeoutMillis > 0 ? replyTimeoutMillis : DEFAULT_REPLY_TIMEOUT_MS);

      Logger.trace("TX: {}", message);

      synchronized (writeMonitor) {
        output.write(message.getMessageBytes());
        output.flush();
      }

      LoconetMessage echo = echoPending.future.get(DEFAULT_ECHO_TIMEOUT_MS + 50, TimeUnit.MILLISECONDS);

      if (echo == null) {
        Logger.trace("No echo received within {} ms for TX: {}", DEFAULT_ECHO_TIMEOUT_MS, message);
      } else {
        Logger.trace("TX echo confirmed: {}", echo);
      }

      LoconetMessage reply = replyPending.future.get(replyTimeoutMillis > 0 ? replyTimeoutMillis + 50 : DEFAULT_REPLY_TIMEOUT_MS + 50, TimeUnit.MILLISECONDS);

      if (reply == null) {
        Logger.trace("No follow-up reply received for TX: {}", message);
      }

      return reply;

    } catch (IOException | InterruptedException | ExecutionException | TimeoutException ex) {
      Logger.error("Could not complete LocoNet request/reply transaction: {}", ex.getMessage());
      return null;

    } finally {
      if (echoPending != null) {
        pendingMessages.remove(echoPending);
      }
      if (replyPending != null) {
        pendingMessages.remove(replyPending);
      }
    }
  }

  @Override
  public CompletableFuture<LoconetMessage> sendMessageAsyncAwaitEchoAndReply(LoconetMessage message, Predicate<LoconetMessage> replyMatcher, long replyTimeoutMillis) {
    return CompletableFuture.supplyAsync(() -> sendMessageAwaitEchoAndReply(message, replyMatcher, replyTimeoutMillis), txExecutor);
  }

  private void messageReceived(LoconetMessage received) {
    Logger.trace("RX: {}", received.toString());

//    if (consumeIfExpectedEcho(received)) {
//      Logger.trace("RX echo consumed: {}", received.toString());
//      return;
//    }
    if (completePendingMessage(received)) {
      Logger.trace("RX consumed by pending transaction: {}", received);
      return;
    }

    messagesQueue.offer(received);
  }

  private boolean consumeIfExpectedEcho(LoconetMessage received) {
    synchronized (echoMonitor) {
      if (expectedEcho != null && received.sameMessage(expectedEcho)) {
        receivedEcho = received;
        expectedEcho = null;
        echoMonitor.notifyAll();
        return true;
      }
      return false;
    }
  }

  private PendingMessage registerPendingMessage(Predicate<LoconetMessage> matcher, boolean consume, long timeoutMillis) {
    PendingMessage pending = new PendingMessage(matcher, consume);
    pendingMessages.add(pending);

    CompletableFuture.delayedExecutor(timeoutMillis, TimeUnit.MILLISECONDS).execute(() -> {
      if (pendingMessages.remove(pending)) {
        pending.future.complete(null);
      }
    });

    return pending;
  }

  private boolean completePendingMessage(LoconetMessage received) {
    for (PendingMessage pending : pendingMessages) {
      boolean matches;

      try {
        matches = pending.matcher.test(received);
      } catch (Exception ex) {
        Logger.error("Pending LocoNet matcher failed: {}", ex.getMessage());
        matches = false;
      }

      if (matches) {
        pendingMessages.remove(pending);
        pending.future.complete(received);
        return pending.consume;
      }
    }

    return false;
  }

  private class LoconetMessageReceiver extends Thread {

    private volatile boolean running = false;
    private final SerialPort serialPort;
    private final InputStream in;
    private final LoconetMessageParser parser;

    LoconetMessageReceiver(SerialPort serialPort) {
      super("INBX-LN-RX");
      this.serialPort = serialPort;
      this.in = serialPort.getInputStream();
      this.parser = new LoconetMessageParser();
    }

    void quit() {
      running = false;
      try {
        in.close();
      } catch (IOException e) {
        Logger.trace("Error while closing receiver input stream. {}", e.getMessage());
      }
    }

    boolean isRunning() {
      return running;
    }

    @Override
    public void run() {
      running = true;
      Logger.trace("Started listening on port " + serialPort.getDescriptivePortName() + "...");

      while (running) {
        try {
          LoconetMessage received = parser.readMessage(in);
          if (received != null) {
            messageReceived(received);
          }
          //Logger.trace("RX: {}", ByteUtil.toHexString(in.read()));
        } catch (IOException e) {
          if (running) {
            Logger.trace("Error: {}", e.getMessage());
          }
        }
      }

      cleanup();
      Logger.debug("Stopped receiving");
    }

    private void cleanup() {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        Logger.error("Error closing input stream", ex);
      }
    }
  }

  private static final class PendingMessage {

    final Predicate<LoconetMessage> matcher;
    final CompletableFuture<LoconetMessage> future;
    final boolean consume;

    PendingMessage(Predicate<LoconetMessage> matcher, boolean consume) {
      this.matcher = matcher;
      this.consume = consume;
      this.future = new CompletableFuture<>();
    }
  }
}
