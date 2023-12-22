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
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import jcs.commandStation.dccex.DccExConnection;
import jcs.commandStation.dccex.events.DccExMessageListener;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class DccExSerialConnection implements DccExConnection {

  private String portName;

  private static SerialPort commPort;
  private Writer writer;
  private boolean debug = false;

  private SerialMessageReceiver messageReceiver;

  public DccExSerialConnection(String portName) {
    this.portName = portName;
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");

    obtainSerialPort(portName);
  }

  private void obtainSerialPort(String portName) {
    try {
      commPort = SerialPort.getCommPort(portName);

      commPort.setBaudRate(115200);
      commPort.openPort();
      commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
      writer = new BufferedWriter(new OutputStreamWriter(commPort.getOutputStream()));

      messageReceiver = new SerialMessageReceiver(commPort);
      messageReceiver.setDaemon(true);
      messageReceiver.start();
    } catch (SerialPortInvalidPortException ioe) {
      Logger.error("Can't find com port: " + portName + "; " + ioe.getMessage());
    }
  }

  @Override
  public void sendMessage(String message) {
    try {
      writer.write(message);
      writer.flush();
      if (debug) {
        Logger.trace("TX: " + message);
      }
    } catch (IOException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public boolean isConnected() {
    if(commPort != null) {
    return commPort.isOpen();
    } else {
      return false;
    }
  }

  @Override
  public void close() throws Exception {
    this.messageReceiver.quit();
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException ex) {
        Logger.error("Can't close output. Cause: " + ex.getMessage());
        Logger.trace(ex);
      }
    }
    commPort.closePort();
  }

  @Override
  public void setMessageListener(DccExMessageListener messageListener) {
    if (messageReceiver != null) {
      this.messageReceiver.setMessageListener(messageListener);
    }
  }

  public static SerialPort[] listComPorts() {
    SerialPort[] comPorts = SerialPort.getCommPorts();
    for (SerialPort comPort : comPorts) {
      Logger.trace(comPort.getDescriptivePortName() + "; " + comPort.getSystemPortName());
    }
    return comPorts;
  }

  private class SerialMessageReceiver extends Thread {

    private boolean quit = true;
    private BufferedReader reader;
    private DccExMessageListener messageListener;

    public SerialMessageReceiver(SerialPort serialPort) {
      try {
        reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
      } catch (Exception ex) {
        Logger.error(ex);
      }
    }

    synchronized void quit() {
      this.quit = true;
    }

    synchronized boolean isRunning() {
      return !this.quit;
    }

    void setMessageListener(DccExMessageListener messageListener) {
      this.messageListener = messageListener;
    }

    @Override
    public void run() {
      this.quit = false;
      Thread.currentThread().setName("DCC-EX-SERIAL-RX");

      Logger.trace("Started listening on port " + commPort.getSystemPortName() + " ...");

      while (isRunning()) {
        try {
          String message = reader.readLine();
          this.messageListener.onMessage(message);
        } catch (SerialPortTimeoutException se) {
          Logger.error(se.getMessage());
          quit();
        } catch (IOException ioe) {
          Logger.error(ioe);
        }
      }

      Logger.debug("Stop receiving");
      try {
        reader.close();
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }
  }

  //testing
  public static void main(String[] a) {

    String portName = "cu.usbmodem14201";

    DccExSerialConnection sercon = new DccExSerialConnection(portName);
    SerialPort comPort = DccExSerialConnection.commPort;

    comPort.openPort();
    comPort.setBaudRate(115200);

//    PacketListener listener = sercon.createPacketListener();
//    comPort.addDataListener(listener);

    try {
      Thread.sleep(15000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    comPort.removeDataListener();
    comPort.closePort();

  }
}
