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
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.util.SerialPortUtil;
import org.tinylog.Logger;

/**
 * Owns the current Intellibox 2 serial transport, but DOES NOT reconnect.
 *
 * Each acquireConnection() call makes one bounded attempt. The caller's ConnectionLifecycleManager schedules retries and owns disconnect/cleanup. The jSerialComm callback only reports a lost port; it
 * never opens or closes a replacement connection on the callback thread.
 */
public final class LoconetConnectionFactory {

  private static final LoconetConnectionFactory INSTANCE = new LoconetConnectionFactory();

  public static final int IB_PORT_VENDOR = 4292;
  public static final int IB_PORT_PRODUCT_ID = 60000;
  public static final int BAUD_RATE = 115200;
  public static final int DATA_BITS = 8;

  private final List<ConnectionEventListener> connectionEventListeners = new CopyOnWriteArrayList<>();

  private SerialPort serialPort;
  private LoconetConnection loconetConnection;
  private boolean disconnectNotified;
  private long transportGeneration;

  private LoconetConnectionFactory() {
  }

  public static LoconetConnectionFactory getInstance() {
    return INSTANCE;
  }

  /**
   * Acquire once. AUTO / null selects the first matching Intellibox USB VID/PID; an explicit port name restricts the search to that device.
   *
   * No background retry threads are created. On failure, the lifecycle manager decides when to invoke this method again (including after USB reinsertion).
   */
  public static LoconetConnection acquireConnection(String configuredPort) {
    return INSTANCE.openOnce(configuredPort);
  }

  private synchronized LoconetConnection openOnce(String configuredPort) {
    if (loconetConnection != null && loconetConnection.isConnected()) {
      return loconetConnection;
    }

    // Drop a failed attempt before acquiring another port.
    closeConnection();

    List<SerialPort> ports = matchingPorts(SerialPortUtil.listComPorts(), configuredPort);
    if (ports.isEmpty()) {
      Logger.debug("No Intellibox serial port found (requested: {})", configuredPort);
      return null;
    }

    for (SerialPort candidate : ports) {
      if (Thread.currentThread().isInterrupted()) {
        return null;
      }
      try {
        if (!candidate.openPort()) {
          Logger.debug("Could not open Intellibox port {}", candidate.getSystemPortName());
          continue;
        }

        // Own the handle immediately so even a configuration exception closes it.
        serialPort = candidate;
        disconnectNotified = true;

        candidate.setBaudRate(BAUD_RATE);
        candidate.setNumDataBits(DATA_BITS);
        candidate.setNumStopBits(SerialPort.ONE_STOP_BIT);
        candidate.setParity(SerialPort.NO_PARITY);
        candidate.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

        serialPort = candidate;
        disconnectNotified = false;
        long generation = ++transportGeneration;

        boolean listenerAdded = candidate.addDataListener(new SerialportConnectionEventListener(candidate, generation));

        if (!listenerAdded) {
          Logger.warn("Could not monitor Intellibox USB disconnect on {}", candidate.getSystemPortName());
        }

        loconetConnection = new IntelliboxConnectionImpl(candidate, () -> notifyDisconnected(candidate, generation));

        // The receiver starts on a new thread. Allow it to enter its read loop
        // before deciding whether the new connection is usable.
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(500);
        while (!loconetConnection.isConnected() && System.nanoTime() < deadline) {
          if (Thread.currentThread().isInterrupted()) {
            closeConnection();
            return null;
          }
          zleep(10);
        }

        if (loconetConnection.isConnected()) {
          Logger.info("Connected to Intellibox on serial port {}", candidate.getSystemPortName());
          return loconetConnection;
        }
        Logger.warn("LocoNet receiver did not become ready on {}", candidate.getSystemPortName());
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        closeConnection();
        return null;
      } catch (RuntimeException ex) {
        Logger.warn("Could not initialize Intellibox port {}: {}",
                candidate.getSystemPortName(), ex.getMessage());
      }
      closeConnection();
    }
    return null;
  }

  private void zleep(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }

  /**
   * Package-visible for selection tests.<br>
   * An explicit port takes precedence over Vendor Id / Product Id because USB adapters may not expose the expected identifiers.
   */
  static List<SerialPort> matchingPorts(SerialPort[] ports, String configuredPort) {
    List<SerialPort> matches = new ArrayList<>();
    boolean automatic = configuredPort == null || configuredPort.isBlank() || "AUTO".equalsIgnoreCase(configuredPort);

    if (ports == null) {
      return matches;
    }

    for (SerialPort port : ports) {
      if (automatic) {
        if (port.getVendorID() == IB_PORT_VENDOR && port.getProductID() == IB_PORT_PRODUCT_ID) {
          matches.add(port);
        }
      } else if (configuredPort != null && configuredPort.equals(port.getSystemPortName())) {
        matches.add(port);
      }
    }
    return matches;
  }

  public static void closeConnection() {
    INSTANCE.closePort();
  }

  private void closePort() {
    final LoconetConnection oldConnection;
    final SerialPort oldPort;
    synchronized (this) {
      oldConnection = loconetConnection;
      oldPort = serialPort;
      loconetConnection = null;
      serialPort = null;
      disconnectNotified = true; // Ignore disconnect notifications during explicit close.
      transportGeneration++;
    }

    if (oldPort != null) {
      try {
        oldPort.removeDataListener();
      } catch (RuntimeException ex) {
        Logger.debug("Could not remove serial disconnect listener: {}", ex.getMessage());
      }
    }
    if (oldConnection != null) {
      try {
        oldConnection.close();
      } catch (RuntimeException ex) {
        Logger.warn("Could not close LocoNet transport: {}", ex.getMessage());
      }
    }
    if (oldPort != null) {
      try {
        if (oldPort.isOpen()) {
          oldPort.closePort();
        }
      } catch (RuntimeException ex) {
        Logger.warn("Could not close Intellibox serial port: {}", ex.getMessage());
      }
    }
  }

  private void notifyDisconnected(SerialPort failedPort, long generation) {
    synchronized (this) {
      if (failedPort != serialPort || disconnectNotified || generation != transportGeneration) {
        return;
      }
      disconnectNotified = true;
    }

    Logger.warn("Intellibox serial port disconnected: {}", failedPort.getSystemPortName());

    ConnectionEvent event = new ConnectionEvent(failedPort.getDescriptivePortName(), false, false);
    for (ConnectionEventListener listener : connectionEventListeners) {
      try {
        listener.onConnectionChange(event);
      } catch (RuntimeException ex) {
        Logger.warn("Intellibox disconnect listener failed: {}", ex.getMessage());
      }
    }
  }

  public void registerConnectionListener(ConnectionEventListener listener) {
    connectionEventListeners.add(listener);
  }

  public void unRegisterConnectionListener(ConnectionEventListener listener) {
    connectionEventListeners.remove(listener);
  }

  private final class SerialportConnectionEventListener implements SerialPortDataListener {

    private final SerialPort watchedPort;
    private final long watchedGeneration;

    SerialportConnectionEventListener(SerialPort watchedPort, long generation) {
      this.watchedPort = watchedPort;
      this.watchedGeneration = generation;
    }

    @Override
    public int getListeningEvents() {
      return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
      notifyDisconnected(watchedPort, watchedGeneration);
    }
  }
}
