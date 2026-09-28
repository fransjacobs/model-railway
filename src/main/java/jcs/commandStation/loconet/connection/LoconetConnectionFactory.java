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
import java.util.concurrent.TimeUnit;
import static jcs.commandStation.automation.RailController.TAG;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.util.SerialPortUtil;
import org.tinylog.Logger;

public class LoconetConnectionFactory {

  private static LoconetConnectionFactory instance;

  private volatile SerialPort serialPort;
  private volatile boolean autoReAcquirePort = true;
  private volatile SerialportConnectionEventListener connectionEventListener;

  private volatile LoconetConnection loconetConnection;
  private volatile PortConnector portConnector;

  private volatile List<ConnectionEventListener> connectionEventListeners;

  /* Uhlenbrock Intellibox 2 */
  public static final int IB_PORT_VENDOR = 4292;
  public static final int IB_PORT_PRODUCT_ID = 60000;

  public static final int BAUD_RATE = 115200;
  public static final int DATA_BITS = 8;

  private LoconetConnectionFactory() {
    connectionEventListeners = new ArrayList<>();
  }

  public static LoconetConnectionFactory getInstance() {
    if (instance == null) {
      instance = new LoconetConnectionFactory();
    }
    return instance;
  }

  public boolean isAutoReAcquirePort() {
    return autoReAcquirePort;
  }

  public void setAutoReAcquirePort(boolean autoReAquirePort) {
    this.autoReAcquirePort = autoReAquirePort;
  }

  public void stopPortAcquire() {
    this.setAutoReAcquirePort(false);
    if (portConnector != null && portConnector.isRunning()) {
      portConnector.quit();
    }
    closePort();
  }

  public static void closeConnection() {
    if (instance != null) {
      instance.stopPortAcquire();
    }
  }

  public static LoconetConnection acquireConnection(long timeoutMillis) {
    LoconetConnectionFactory factory = getInstance();
    factory.setAutoReAcquirePort(true);
    factory.startPortAcquire();

    return factory.awaitConnection(timeoutMillis);
  }

  LoconetConnection awaitConnection(long timeoutMillis) {
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);

    while (System.nanoTime() < deadline) {
      LoconetConnection connection = loconetConnection;

      if (connection != null && connection.isConnected()) {

        //Let clean the port connector thread if needed
        if (this.portConnector != null && !this.portConnector.isAlive()) {
          this.portConnector = null;
        }
        return connection;
      }

      zleep(10);
    }

    Logger.warn("Can't establish connection with Intellibox within {} ms.", timeoutMillis);
    return null;
  }

  void closePort() {
    LoconetConnection oldConnection = loconetConnection;
    loconetConnection = null;

    try {
      if (oldConnection != null) {
        oldConnection.close();
      }
      if (serialPort != null) {
        if (serialPort.isOpen()) {
          serialPort.flushIOBuffers();
          serialPort.removeDataListener();
          serialPort.closePort();
        }
      }
    } catch (Exception e) {
      Logger.trace("Exception while closing port: {}", e.getMessage());
    } finally {
      connectionEventListener = null;
      serialPort = null;
    }
  }

  private void disconnected() {
    Logger.trace("Port is disconnected cleanup...");
    closePort();
    zleep(1000);

    if (autoReAcquirePort) {
      startPortAcquire();
    }
  }

  private synchronized void startPortAcquire() {
    if (loconetConnection != null && loconetConnection.isConnected()) {
      Logger.trace("SerialPort is connected...");
      return;
    }

    if (portConnector != null && portConnector.isAlive()) {
      Logger.trace("Port Connector is running...");
      return;
    }

    portConnector = new PortConnector(this);
    portConnector.start();

    if (serialPort != null) {
      String name = serialPort.getDescriptivePortName();
      String manu = serialPort.getManufacturer();
      String serial = serialPort.getSerialNumber();
      Logger.tag(TAG).debug("Aquired SerialPort {}. Manufacturer {}, Serial {} ", name, manu, serial);
    }
  }

  private void zleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void registerConnectionListener(ConnectionEventListener listener) {
    this.connectionEventListeners.add(listener);
  }

  public void unRegisterConnectionListener(ConnectionEventListener listener) {
    this.connectionEventListeners.remove(listener);
  }

  private class SerialportConnectionEventListener implements SerialPortDataListener {

    private final LoconetConnectionFactory loconetConnectionFactory;

    SerialportConnectionEventListener(LoconetConnectionFactory loconetConnectionFactory) {
      this.loconetConnectionFactory = loconetConnectionFactory;
    }

    @Override
    public int getListeningEvents() {
      return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
      Logger.tag(TAG).warn("Serialport Disconnected!");
      Thread reconnectThread = new Thread(loconetConnectionFactory::disconnected, "LOCONET-RECONNECT");
      reconnectThread.setDaemon(true);
      reconnectThread.start();

      ConnectionEvent ce = new ConnectionEvent(spe.getSerialPort().getDescriptivePortName(), false, false);
      for (ConnectionEventListener listener : connectionEventListeners) {
        listener.onConnectionChange(ce);
      }
    }
  }

  private class PortConnector extends Thread {

    private final LoconetConnectionFactory loconetConnectionFactory;
    private volatile boolean running;

    PortConnector(LoconetConnectionFactory loconetConnectionFactory) {
      super("LOCONET-SERIAL-PORT-CONFIG-THREAD");
      this.loconetConnectionFactory = loconetConnectionFactory;
    }

    /**
     * Obtain a list of Serial ports which are usable as Loconet connection.<br>
     * For each port check whether a Loconet client, like the Intelllibox, is connected.
     *
     * @return a List with usable Serial ports
     */
    List<SerialPort> aquireLoconetSerialPorts() {
      SerialPort[] comPorts = SerialPortUtil.listComPorts();

      List<SerialPort> ports = new ArrayList<>();
      for (SerialPort port : comPorts) {
        int vendor = port.getVendorID();
        int productId = port.getProductID();
        if (IB_PORT_VENDOR == vendor && IB_PORT_PRODUCT_ID == productId) {
          ports.add(port);
        }
      }
      //Logger.trace("There are {} Ports for Loconet available.", ports.size());
      return ports;
    }

    boolean isRunning() {
      return this.running;
    }

    void quit() {
      this.running = false;
    }

    @Override
    public void run() {
      running = true;
      Logger.trace("Try to connect to a Loconet USB port...");

      while (running) {
        List<SerialPort> ports = aquireLoconetSerialPorts();
        if (!ports.isEmpty()) {
          //Use the first port
          SerialPort comPort = ports.getFirst();
          // Try to open the port
          if (comPort.openPort()) {
            comPort.setBaudRate(BAUD_RATE);
            comPort.setNumDataBits(DATA_BITS);
            comPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
            comPort.setParity(SerialPort.NO_PARITY);

            comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

            loconetConnectionFactory.connectionEventListener = new SerialportConnectionEventListener(loconetConnectionFactory);
            comPort.addDataListener(connectionEventListener);
            loconetConnectionFactory.serialPort = comPort;
            loconetConnection = new IntelliboxConnectionImpl(comPort);
            //Port is connected so stop running the helper thread
            running = false;
          } else {
            Logger.trace("Can't open port {}", comPort.getSystemPortName());
          }
        } else {
          Logger.trace("No ports available");
        }

        //Not connected, sleep a while and try again...
        if (running) {
          zleep(1000);
        }
      }
      if (loconetConnection != null) {
        Logger.trace("Port Connected {}.", loconetConnection.isConnected());

        ConnectionEvent ce = new ConnectionEvent(serialPort.getDescriptivePortName(), true, false);
        for (ConnectionEventListener listener : connectionEventListeners) {
          listener.onConnectionChange(ce);
        }
      } else {
        Logger.trace("Port NOT Connected!");
      }
    }
  }
}
