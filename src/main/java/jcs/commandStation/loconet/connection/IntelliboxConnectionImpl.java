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
import java.util.concurrent.LinkedBlockingQueue;
import jcs.commandStation.loconet.LoconetMessage;
import jcs.commandStation.loconet.LoconetMessageParser;
import org.tinylog.Logger;

/**
 * Loconet Connection Implementation for the Uhlenbrock Intellibox 2
 */
class IntelliboxConnectionImpl implements LoconetConnection {

  private final SerialPort serialPort;

  private OutputStream output;
  private LoconetMessageReceiver loconetMessageReceiver;
  private final BlockingQueue<LoconetMessage> messagesQueue;

  //private final List<LoconetMessage> pendingResponses = new CopyOnWriteArrayList<>();
  private boolean debug = false;

  IntelliboxConnectionImpl(SerialPort serialPort) {
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    this.messagesQueue = new LinkedBlockingQueue<>();
    this.serialPort = serialPort;
    output = serialPort.getOutputStream();
    initReceiver();
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
      messagesQueue.clear();
      output.flush();
      if (loconetMessageReceiver != null) {
        loconetMessageReceiver.quit();

        while (loconetMessageReceiver.isRunning()) {
          pause(50);
        }
      }

      output.close();
      loconetMessageReceiver = null;
      output = null;

    } catch (IOException e) {
      Logger.trace("Error while closing. {}", e.getMessage());
    }
    Logger.debug("Connection closed");
  }

  @Override
  public LoconetMessage sendMessage(LoconetMessage message) {
    try {
      if (output != null) {
        Logger.trace("TX: {}", message.toString());

        output.write(message.getMessageBytes());
        output.flush();
      }
    } catch (IOException ex) {
      Logger.error(ex);
    }
    return message;
  }

//  @Override
//  public CompletableFuture<LoconetMessage> sendAndAwaitEcho(LoconetMessage message, Duration timeout) {
//    throw new UnsupportedOperationException("Not supported yet.");
//  }
//  @Override
//  public CompletableFuture<LoconetMessage> sendAndAwait(LoconetMessage message, Predicate<LoconetMessage> matcher, Duration timeout) {
//    throw new UnsupportedOperationException("Not supported yet.");
//  }
  @Override
  public void addMessageListener(LoconetMessageListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeMessageListener(LoconetMessageListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
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
            Logger.trace("RX: {}", received.toString());
            messagesQueue.offer(received);
          }
          //Logger.trace("RX: {}", ByteUtil.toHexString(in.read()));
        } catch (IOException e) {
          Logger.trace("Error: {}", e.getMessage());
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
