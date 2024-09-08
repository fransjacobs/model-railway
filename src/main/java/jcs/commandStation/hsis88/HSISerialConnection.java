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
package jcs.commandStation.hsis88;

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
import jcs.commandStation.events.DisconnectionEvent;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class HSISerialConnection implements HSIConnection {

  private final String lastUsedPortName;
  private static SerialPort commPort;
  private boolean debug = false;
  private boolean portOpen = false;
  private Writer writer;

  private ResponseCallback responseCallback;
  private final List<HSIMessageListener> feedbackListeners;

  private static final long TIMEOUT = 3000L;

  HSISerialConnection(String portName) {
    lastUsedPortName = portName;

    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    feedbackListeners = new ArrayList<>();
    obtainSerialPort(portName);
  }

  private void obtainSerialPort(String portName) {
    try {
      commPort = SerialPort.getCommPort(portName);
      //TODO: HSI settings are hardware determined, but for the future make it configurable
      commPort.setBaudRate(9600);
      commPort.setNumDataBits(8);
      commPort.setNumStopBits(1);
      commPort.setParity(0);

      portOpen = commPort.openPort();
      commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 10000, 1000);
      writer = new BufferedWriter(new OutputStreamWriter(commPort.getOutputStream()));

      HSISerialPortListener listener = new HSISerialPortListener(this);
      commPort.addDataListener(listener);

      Logger.trace("Manufacturer: " + commPort.getManufacturer() + " ProductId: " + commPort.getProductID());

    } catch (SerialPortInvalidPortException ioe) {
      Logger.error("Can't find com port: " + portName + "; " + ioe.getMessage());
    }
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.trace(e.getMessage());
    }
  }

  @Override
  //Some message are more or less synchroneous, so for those a reply is retuned
  public synchronized String sendMessage(String message) {
    String response = message;
    if (COMMAND_TOGGLE_TERMINAL_MODE.equals(message) || COMMAND_SET.equals(message) || COMMAND_VERSION.equals(message)) {
      //semi synchroneous commands
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

  private class ResponseCallback {

    private final String tx;
    private byte[] rx;

    ResponseCallback(final String tx) {
      this.tx = tx;
    }

    boolean isSubscribedfor(final byte[] rx) {
      String responseId = Character.toString(rx[0]);
      String c = tx.substring(0, 1);
      return c.equalsIgnoreCase(responseId);
    }

    void setResponse(byte[] rx) {
      this.rx = rx;
    }

    String getResponse() {
      if (this.rx != null) {
        return new String(this.rx);
      } else {
        return tx;
      }
    }

    boolean isResponseComplete() {
      return rx != null && rx.length > 1;
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

      for (HSIMessageListener listener : feedbackListeners) {
        listener.onDisconnect(de);
      }
      close();
    } catch (Exception e) {
      Logger.error("Error while trying to close port " + e.getMessage());
    }
  }

  private void messageReceived(HSIMessage hsim) {
    for (HSIMessageListener listener : feedbackListeners) {
      listener.onMessage(hsim);
    }
  }

  @Override
  public void close() throws Exception {
    feedbackListeners.clear();
    portOpen = false;
    if (writer != null) {
      writer.close();
    }
    commPort.removeDataListener();
    commPort.closePort();
  }

  @Override
  public void addMessageListener(HSIMessageListener messageListener) {
    this.feedbackListeners.add(messageListener);
  }

  private final class HSISerialPortListener implements SerialPortMessageListener {

    private final HSISerialConnection hsiSerialConnection;

    HSISerialPortListener(HSISerialConnection hsiSerialConnection) {
      this.hsiSerialConnection = hsiSerialConnection;
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
          this.hsiSerialConnection.disconnected();
        }
        case SerialPort.LISTENING_EVENT_DATA_RECEIVED -> {
          byte[] message = event.getReceivedData();

          if (responseCallback != null && responseCallback.isSubscribedfor(message)) {
            //a "synchroneous" response
            responseCallback.setResponse(message);
          } else {
            //a "asynchroneous" response
            HSIMessage hsim = new HSIMessage(message);
            hsiSerialConnection.messageReceived(hsim);
          }
        }
      }
    }
  }
}
