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
package jcs.commandStation.uhlenbrock.connection;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import jcs.util.ByteUtil;
import jcs.util.SerialPortUtil;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class IntelliBoxSerialConnection implements IntelliBoxConnection {

  private String lastUsedPortName = null;
  private static SerialPort intelliBoxPort;
  private Writer writer;

  private DataOutputStream dos;

  private IntelliBoxSerialPortListener listener;

  private boolean portOpen = false;
  private boolean debug = false;

  private static final long TIMEOUT = 6000L;

  public static final int IB_PORT_VENDOR = 4292;
  public static final int IB_PORT_PRODUCT_ID = 60000;
  public static final String IB_PORT_MANUFACTURER = "Silicon Labs";

  IntelliBoxSerialConnection() {
    this(null);
  }

  IntelliBoxSerialConnection(String portName) {
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    lastUsedPortName = portName;
    intelliBoxPort = aquireSerialPort();
  }

  void connect() {
    if (intelliBoxPort == null) {
      intelliBoxPort = aquireSerialPort();
    }

    if (intelliBoxPort != null) {
      portOpen = intelliBoxPort.openPort();
      intelliBoxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

      //writer = new BufferedWriter(new OutputStreamWriter(intelliBoxPort.getOutputStream()));
      //dos = new DataOutputStream(intelliBoxPort.getOutputStream());
      //listener = new IntelliBoxSerialPortListener(this);
      //intelliBoxPort.addDataListener(listener);
      Logger.trace("Manufacturer: {}. Connected to {}", intelliBoxPort.getManufacturer(), intelliBoxPort.getDescriptivePortName());
    }
  }

  private SerialPort aquireSerialPort() {
    SerialPort serialPort = null;
    boolean ibAvailable = isIntelliBoxPortAvailable();
    if (lastUsedPortName == null && ibAvailable) {
      // Use the first avalable port port
      List<SerialPort> ports = aquireIntelliBoxSerialPorts();
      serialPort = ports.getLast();
      lastUsedPortName = serialPort.getSystemPortName();
    } else if (ibAvailable) {
      serialPort = SerialPort.getCommPort(lastUsedPortName);
    }
    Logger.trace("IntelliBox is {}", (ibAvailable ? "available" : "not available"));

    if (serialPort != null) {
      serialPort.setBaudRate(115200);
      //serialPort.setBaudRate(19200);

      serialPort.setNumDataBits(8);
      serialPort.setNumStopBits(1);
      serialPort.setParity(0);
    }

    return serialPort;
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.trace(e.getMessage());
    }
  }

  @Override
  public synchronized String sendMessage(byte[] message) {
    //String txm = ByteUtil.bytesToString(message);
    //String response = reply;
    try {
      //writer.write(reply);
      //writer.flush();
      //dos.write(message);
      //dos.flush();
      Logger.trace("TX: {}", ByteUtil.toHexString(message));

      OutputStream os = intelliBoxPort.getOutputStream();
      os.write(message);
      os.flush();

      //intelliBoxPort.writeBytes(message, MAX_ERRORS);
      InputStream in = intelliBoxPort.getInputStream();
      for (int j = 0; j < 10; ++j) {
        Logger.trace("RX {}: {}", j, ByteUtil.toHexString(in.read()));
      }
      in.close();

    } catch (Exception ex) {
      Logger.error(ex);
    }

//    if (responseCallback != null) {
//      long now = System.currentTimeMillis();
//      long start = now;
//      long timeout = now + TIMEOUT;
//
//      //Wait for the response
//      boolean responseComplete = responseCallback.isResponseComplete();
//      while (!responseComplete && now < timeout) {
//        pause(10);
//        responseComplete = responseCallback.isResponseComplete();
//        now = System.currentTimeMillis();
//      }
//
//      response = responseCallback.getResponse();
//      if (debug) {
//        if (responseComplete) {
//          Logger.trace("Got Response in " + (now - start) + " ms: " + response);
//        } else {
//          Logger.trace("No Response for " + reply + " in " + (now - start) + " ms");
//        }
//      }
//    }
//
//    responseCallback = null;
    return "";
  }

//  private void messageReceived(DccExMessage dccExMessage) {
//    if (dccExListeners.isEmpty()) {
//      startupMessages.add(dccExMessage);
//    } else {
//      for (DccExMessageListener listener : dccExListeners) {
//        listener.onMessage(dccExMessage);
//      }
//    }
//  }
  @Override
  public boolean isConnected() {
    if (!portOpen) {
      aquireSerialPort();
    }
    return portOpen;
  }

  private void disconnected() {
    Logger.warn("DISCONNECTED!");
//    try {
//      Logger.trace("Port " + intelliBoxPort.getSystemPortName() + " is Disconnected");
//
//      String msg = intelliBoxPort.getDescriptivePortName() + " [" + intelliBoxPort.getSystemPortName() + "]";
//      ConnectionEvent de = new ConnectionEvent(msg, false, false);
//
//      for (DccExMessageListener listener : dccExListeners) {
//        listener.onDisconnect(de);
//      }
//      close();
//    } catch (Exception e) {
//      Logger.error("Error while trying to close port " + e.getMessage());
//    }
  }

  @Override
  public void close() {
    try {
      portOpen = false;

      intelliBoxPort.removeDataListener();
      intelliBoxPort.closePort();
      intelliBoxPort = null;
    } catch (Exception e) {
      Logger.trace("Error while closing serial port. {}", e.getMessage());
    }
    Logger.debug("Serial port closed");
  }

//  @Override
//  public void setMessageListener(DccExMessageListener messageListener) {
//    Boolean firstListener = dccExListeners.isEmpty();
//    this.dccExListeners.add(messageListener);
//    if (firstListener) {
//      for (DccExMessage m : startupMessages) {
//        messageReceived(m);
//      }
//    }
//  }
//  public List<DccExMessage> getStartupMessages() {
//    return this.startupMessages;
//  }
//  private final class DccExSerialPortListener implements SerialPortMessageListener {
//
//    private final IntelliBoxSerialConnection dccExSerialConnection;
//
//    DccExSerialPortListener(IntelliBoxSerialConnection hsiSerialConnection) {
//      this.dccExSerialConnection = hsiSerialConnection;
//    }
//
//    @Override
//    public int getListeningEvents() {
//      return SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
//    }
//
//    @Override
//    public byte[] getMessageDelimiter() {
//      return MESSAGE_DELIMITER.getBytes();
//    }
//
//    @Override
//    public boolean delimiterIndicatesEndOfMessage() {
//      return true;
//    }
//
//    @Override
//    public void serialEvent(SerialPortEvent event) {
//      switch (event.getEventType()) {
//        case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED -> {
//          this.dccExSerialConnection.disconnected();
//        }
//        case SerialPort.LISTENING_EVENT_DATA_RECEIVED -> {
//          byte[] reply = event.getReceivedData();
//
//          if (responseCallback != null && responseCallback.isSubscribedfor(reply)) {
//            //a "synchroneous" response
//            responseCallback.setResponse(reply);
//          } else {
//            //a "asynchroneous" response
//            DccExMessage ddcExm = new DccExMessage(reply);
//            dccExSerialConnection.messageReceived(ddcExm);
//          }
//        }
//      }
//    }
//  }
//
//  private class ResponseCallback {
//
//    private final String tx;
//    private final String rxOpcode;
//    private String rx;
//
//    ResponseCallback(final String tx) {
//      this.tx = tx;
//      this.rxOpcode = DccExMessageFactory.getResponseOpcodeFor(tx);
//    }
//
//    boolean isSubscribedfor(final byte[] rx) {
//      String response = new String(rx).replaceAll("\n", "").replaceAll("\r", "");
//      String opcode = response.substring(1, 2);
//
//      return opcode.equals(rxOpcode);
//    }
//
//    void setResponse(byte[] rx) {
//      this.rx = new String(rx).replaceAll("\n", "").replaceAll("\r", "");
//    }
//
//    String getResponse() {
//      if (this.rx != null) {
//        return this.rx;
//      } else {
//        return tx;
//      }
//    }
//
//    boolean isResponseComplete() {
//      return rx != null && !rx.isBlank() && rx.startsWith("<") && rx.endsWith(">");
//    }
//  }
  public static boolean isIntelliBoxPortAvailable() {
    return !aquireIntelliBoxSerialPorts().isEmpty();
  }

  public static List<SerialPort> obtainIntelliBoxPorts() {
    return aquireIntelliBoxSerialPorts();
  }

  static List<SerialPort> aquireIntelliBoxSerialPorts() {
    SerialPort[] ports = SerialPortUtil.listComPorts();

    List<SerialPort> ibSerialPorts = new ArrayList<>();
    for (SerialPort port : ports) {
      int vendor = port.getVendorID();
      int productId = port.getProductID();
      if (IB_PORT_VENDOR == vendor && IB_PORT_PRODUCT_ID == productId) {
        ibSerialPorts.add(port);
      }
    }
    return ibSerialPorts;
  }

  private final class IntelliBoxSerialPortListener implements SerialPortMessageListener {

    private final IntelliBoxSerialConnection ibSerialConnection;

    IntelliBoxSerialPortListener(IntelliBoxSerialConnection ibSerialConnection) {
      this.ibSerialConnection = ibSerialConnection;
    }

    @Override
    public int getListeningEvents() {
      return SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    @Override
    public byte[] getMessageDelimiter() {
      return MESSAGE_DELIMITER.getBytes();
    }

    @Override
    public boolean delimiterIndicatesEndOfMessage() {
      return true;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
      switch (event.getEventType()) {
        case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED -> {
          ibSerialConnection.disconnected();
        }
        case SerialPort.LISTENING_EVENT_DATA_RECEIVED -> {
          byte[] reply = event.getReceivedData();
          Logger.trace("RX: {}", ByteUtil.toHexString(reply));
        }
        default -> {
          byte[] reply = event.getReceivedData();
          Logger.trace("#RX: {}", ByteUtil.toHexString(reply));
        }

      }
    }
  }

  ////////For testing
  ///
  public static void main(String[] a) {

    //SerialPort[] ports = SerialPortUtil.listComPorts();
//    Boolean isIntelliBoxAvailable = isIntelliBoxPortAvailable();
//
//    Logger.trace("IntelliBox is {}", (isIntelliBoxAvailable ? "available" : "not available"));
//
//    if (Logger.isTraceEnabled() && isIntelliBoxAvailable) {
//      List<SerialPort> ibPorts = aquireIntelliBoxSerialPorts();
//      Logger.trace("IB Ports:");
//
//      for (SerialPort port : ibPorts) {
//        Logger.trace("Port: {}", port.getDescriptivePortName());
//      }
//    }
    IntelliBoxSerialConnection ibc = new IntelliBoxSerialConnection();

    ibc.connect();

    //  public static final int GO_COMMAND = 96; 0x60
    // public static final Integer STOP_COMMAND = 97; 0x61
    //"xZzA1"
    //78h
    //check baudrate via s88 module 4
    byte[] baud = new byte[]{(byte) 0xC4};
    byte[] baud2 = new byte[]{(byte) 0x58, (byte) 0x0D, (byte) 0x0A};

    byte[] powerOn = new byte[]{(byte) 0x82, (byte) 0x7D};
    byte[] powerOff = new byte[]{(byte) 0x83, (byte) 0x7C};

    byte[] xZzA1 = "xZzA1".getBytes();

    //byte[] msg = new byte[]{(byte) 0xa6, (byte) 0xee};  //166
    byte[] msg = new byte[]{(byte) 0xA7};
    ///167
    
    ibc.pause(100);

    ibc.sendMessage(powerOff);

    ibc.pause(1000);

    ibc.sendMessage(powerOn);

    //ibc.sendMessage(xZzA1); 
    //ibc.sendMessage(msg);
    //ibc.sendMessage(msg);
    ibc.pause(10000);
    ibc.close();

  }

  
//TRACE	2026-07-16 19:42:24.819 [main] IntelliBoxSerialConnection.sendMessage(): RX 3: 02
//TRACE	2026-07-16 19:42:24.819 [main] IntelliBoxSerialConnection.sendMessage(): RX 4: 39
//TRACE	2026-07-16 19:42:24.820 [main] IntelliBoxSerialConnection.sendMessage(): RX 5: 64
//TRACE	2026-07-16 19:42:25.379 [main] IntelliBoxSerialConnection.sendMessage(): RX 6: a0
//TRACE	2026-07-16 19:42:25.380 [main] IntelliBoxSerialConnection.sendMessage(): RX 7: 01
//TRACE	2026-07-16 19:42:25.380 [main] IntelliBoxSerialConnection.sendMessage(): RX 8: 02
//TRACE	2026-07-16 19:42:25.381 [main] IntelliBoxSerialConnection.sendMessage(): RX 9: 5c
  
  
}
