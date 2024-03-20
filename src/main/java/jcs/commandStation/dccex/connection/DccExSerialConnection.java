/*
 * Copyright 2023 frans.
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
package jcs.commandStation.dccex.connection;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.dccex.DccExConnection;
import static jcs.commandStation.dccex.DccExConnection.MESSAGE_DELIMITER;
import jcs.commandStation.dccex.DccExMessage;
import jcs.commandStation.dccex.DccExMessageFactory;
import jcs.commandStation.events.DisconnectionEvent;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class DccExSerialConnection implements DccExConnection {

  private final String lastUsedPortName;
  private static SerialPort commPort;
  private Writer writer;
  private boolean portOpen = false;
  private boolean debug = false;

  private final List<DccExMessageListener> dccExListeners;
  private ResponseCallback responseCallback;
  private static final long TIMEOUT = 3000L;

  private final List<DccExMessage> startupMessages;

  DccExSerialConnection(String portName) {
    lastUsedPortName = portName;
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    dccExListeners = new ArrayList<>();
    startupMessages = new ArrayList<>();

    obtainSerialPort(portName);
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.trace(e.getMessage());
    }
  }

  private void obtainSerialPort(String portName) {
    try {
      commPort = SerialPort.getCommPort(portName);
      //TODO: DCC-EX settings are hardware determined, but for the future make it configurable
      commPort.setBaudRate(115200);
      commPort.setNumDataBits(8);
      commPort.setNumStopBits(1);
      commPort.setParity(0);

      portOpen = commPort.openPort();
      commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
      writer = new BufferedWriter(new OutputStreamWriter(commPort.getOutputStream()));

      DccExSerialPortListener listener = new DccExSerialPortListener(this);
      commPort.addDataListener(listener);
    } catch (SerialPortInvalidPortException ioe) {
      Logger.error("Can't find com port: " + portName + "; " + ioe.getMessage());
    }
  }

  @Override
  public synchronized String sendMessage(String message) {
    String response = message;
    String rxOpcode = DccExMessageFactory.getResponseOpcodeFor(message);
    if (rxOpcode != null) {
      this.responseCallback = new ResponseCallback(message);
    }
    try {
      writer.write(message);
      writer.flush();
      if (debug) {
        Logger.trace("TX: " + message);
      }
    } catch (IOException ex) {
      Logger.error(ex);
    }

    if (responseCallback != null) {
      long now = System.currentTimeMillis();
      long start = now;
      long timeout = now + TIMEOUT;

      //Wait for the response
      boolean responseComplete = responseCallback.isResponseComplete();
      while (!responseComplete && now < timeout) {
        pause(10);
        responseComplete = responseCallback.isResponseComplete();
        now = System.currentTimeMillis();
      }

      response = responseCallback.getResponse();
      if (debug) {
        if (responseComplete) {
          Logger.trace("Got Response in " + (now - start) + " ms");
        } else {
          Logger.trace("No Response for " + message + " in " + (now - start) + " ms");
        }
      }
    }

    responseCallback = null;
    return response;
  }

  private void messageReceived(DccExMessage dccExMessage) {
    if (dccExListeners.isEmpty()) {
      startupMessages.add(dccExMessage);
    } else {
      for (DccExMessageListener listener : dccExListeners) {
        listener.onMessage(dccExMessage);
      }
    }
  }

  @Override
  public boolean isConnected() {
    if (!portOpen) {
      obtainSerialPort(lastUsedPortName);
    }
    return portOpen;
  }

  private void disconnected() {
    try {
      Logger.trace("Port " + commPort.getSystemPortName() + " is Disconnected");

      String msg = commPort.getDescriptivePortName() + " [" + commPort.getSystemPortName() + "]";
      DisconnectionEvent de = new DisconnectionEvent(msg);

      for (DccExMessageListener listener : dccExListeners) {
        listener.onDisconnect(de);
      }
      close();
    } catch (Exception e) {
      Logger.error("Error while trying to close port " + e.getMessage());
    }
  }

  @Override
  public void close() throws Exception {
    dccExListeners.clear();
    startupMessages.clear();
    portOpen = false;
    if (writer != null) {
      writer.close();
    }
    commPort.removeDataListener();
    commPort.closePort();
  }

  @Override
  public void setMessageListener(DccExMessageListener messageListener) {
    Boolean firstListener = dccExListeners.isEmpty();
    this.dccExListeners.add(messageListener);
    if (firstListener) {
      for (DccExMessage m : startupMessages) {
        messageReceived(m);
      }
    }
  }

  public List<DccExMessage> getStartupMessages() {
    return this.startupMessages;
  }

  private final class DccExSerialPortListener implements SerialPortMessageListener {

    private final DccExSerialConnection dccExSerialConnection;

    DccExSerialPortListener(DccExSerialConnection hsiSerialConnection) {
      this.dccExSerialConnection = hsiSerialConnection;
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
          this.dccExSerialConnection.disconnected();
        }
        case SerialPort.LISTENING_EVENT_DATA_RECEIVED -> {
          byte[] message = event.getReceivedData();

          if (responseCallback != null && responseCallback.isSubscribedfor(message)) {
            //a "synchroneous" response
            responseCallback.setResponse(message);
          } else {
            //a "asynchroneous" response
            DccExMessage ddcExm = new DccExMessage(message);
            dccExSerialConnection.messageReceived(ddcExm);
          }
        }
      }
    }
  }

  private class ResponseCallback {

    private final String tx;
    private final String rxOpcode;
    private String rx;

    ResponseCallback(final String tx) {
      this.tx = tx;
      this.rxOpcode = DccExMessageFactory.getResponseOpcodeFor(tx);
    }

    boolean isSubscribedfor(final byte[] rx) {
      String response = new String(rx).replaceAll("\n", "").replaceAll("\r", "");
      String opcode = response.substring(1, 2);

      return opcode.equals(rxOpcode);
    }

    void setResponse(byte[] rx) {
      this.rx = new String(rx).replaceAll("\n", "").replaceAll("\r", "");
    }

    String getResponse() {
      if (this.rx != null) {
        return this.rx;
      } else {
        return tx;
      }
    }

    boolean isResponseComplete() {
      return rx != null && !rx.isBlank() && rx.startsWith("<") && rx.endsWith(">");
    }
  }

}
