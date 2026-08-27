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
import org.tinylog.Logger;

/**
 * Loconet Connection Implementation for the Uhlenbrock Intellibox 2
 */
class IntelliboxConnectionImpl implements LoconetConnection {

  private final SerialPort serialPort;

  private static final long DEFAULT_ECHO_TIMEOUT_MS = Long.getLong("loconet.echo.timeout.ms", 250L);
  private static final boolean DEBUG = System.getProperty("message.debug", "false").equalsIgnoreCase("true");

  private OutputStream output;
  private LoconetMessageReceiver loconetMessageReceiver;
  private final BlockingQueue<LoconetMessage> messagesQueue;

  private final Object echoMonitor = new Object();
  private LoconetMessage expectedEcho;
  private LoconetMessage receivedEcho;

  private ExecutorService txExecutor;
  private final Object writeMonitor = new Object();

  IntelliboxConnectionImpl(SerialPort serialPort) {
    this.messagesQueue = new LinkedBlockingQueue<>();
    this.serialPort = serialPort;
    output = serialPort.getOutputStream();
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

    try {
      Logger.trace("TX: {}", message);

      registerExpectedEcho(message);

      synchronized (writeMonitor) {
        output.write(message.getMessageBytes());
        output.flush();
      }

      LoconetMessage echo = waitForEcho(DEFAULT_ECHO_TIMEOUT_MS);
      if (echo == null) {
        Logger.trace("No echo received within {} ms for TX: {}", DEFAULT_ECHO_TIMEOUT_MS, message);
      }

      return echo;

    } catch (IOException ex) {
      clearPendingEcho();
      Logger.error(ex);
      return null;
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
  public synchronized void sendMessageNoWaitConsumeEcho(LoconetMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("message may not be null");
    }

    if (output == null || !isConnected()) {
      Logger.warn("Cannot send LocoNet message; connection is not open.");
      return;
    }

    try {
      Logger.trace("TX no-wait consume-echo: {}", message);

      registerExpectedEcho(message);

      output.write(message.getMessageBytes());
      output.flush();

    } catch (IOException ex) {
      clearPendingEcho();
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

  private void messageReceived(LoconetMessage received) {
    Logger.trace("RX: {}", received.toString());

    if (consumeIfExpectedEcho(received)) {
      Logger.trace("RX echo consumed: {}", received.toString());
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

//  @Override
//  public void addMessageListener(LoconetMessageListener listener) {
//    throw new UnsupportedOperationException("Not supported yet.");
//  }
//  @Override
//  public void removeMessageListener(LoconetMessageListener listener) {
//    throw new UnsupportedOperationException("Not supported yet.");
//  }
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
}
