/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.controller.marklin.serial;

import java.util.HashMap;
import java.util.Map;
import org.pmw.tinylog.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Class that represents the (old) serial Marklin 6051/5051 interface. This interface is connected via a RS232 serial connection
 *
 * @author frans
 */
public class Serial6050 {

  // Marklin 6050/6051 has the following serial settings
  private static final int BAUDRATE = SerialPort.BAUDRATE_1200 * 2;
  private static final int DATABITS = SerialPort.DATABITS_8;
  private static final int STOPBITS = SerialPort.STOPBITS_2;
  private static final int PARITY = SerialPort.PARITY_NONE;

  static final int PAUSE_BETWEEN_CMD = 100;
  static final int PAUSE_BETWEEN_BYTES = 5;

  static final long CTS_TIMEOUT = 200;
  static final long RESPONSE_TIMEOUT = 2000L;

  public static final int GO_COMMAND = 96;
  public static final Integer STOP_COMMAND = 97;
  
  private final String portname;
  private SerialPort serialPort;
  private SerialPortReader serialPortReader;
  private final Map<Integer, Integer[]> feedbackResults;

  private boolean powerOn;
  private boolean busy;

  private boolean solenoidActive;

  public Serial6050(String portname) {
    this.portname = portname;
    feedbackResults = new HashMap<>();

    try {
      if (portname == null) {
        Logger.error("Can't initialize Interface 6050/6051, missing port name!");
      } else {
        serialPort = initSerialPort(portname);
        if (serialPort == null) {
          Logger.error("Could not find a Serialport: " + portname);
        }
      }
    } catch (SerialPortException ex) {
      Logger.error("Can't initialize Serialport " + portname + "; " + ex.getMessage());
    }
  }

  /**
   * @return a SerialPort or null
   */
  private SerialPort initSerialPort(String serialPortName) throws SerialPortException {
    SerialPort sp;
    try {
      sp = new SerialPort(serialPortName);
      sp.openPort();
      boolean portOK = sp.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY, true, false);
      Logger.debug("Port: " + serialPortName + " parameters set: result: " + (portOK ? "OK" : "Not OK") + "...");

      if (portOK) {
        sp.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS;
        sp.setEventsMask(mask);
        this.serialPortReader = new SerialPortReader(sp);
        sp.addEventListener(serialPortReader);
        return sp;
      } else {
        Logger.error("Can't initialize port " + serialPortName + "...");
        throw new SerialPortException(serialPortName, "Can't initialze port with default parameters",
                SerialPortException.TYPE_PARAMETER_IS_NOT_CORRECT);
      }
    } catch (SerialPortException spe) {
      if (SerialPortException.TYPE_PORT_BUSY.equals(spe.getExceptionType())) {
        Logger.error("Port already opened by an other process!");
      } else {
        throw spe;
      }
    }
    return null;
  }

  public void disconnect6050() {
    try {
      //wait for command ending
      boolean waiting = this.busy;
      long now = System.currentTimeMillis();
      long timeout = now + 2000;
      while (waiting && now < timeout) {
        now = System.currentTimeMillis();
        waiting = this.busy;
      }

      this.serialPortReader = null;
      this.serialPort.closePort();
      this.serialPort = null;
    } catch (SerialPortException ex) {
      Logger.error(ex);
    }
    Logger.info("Port " + this.portname + " disconnected.");
  }

  public boolean connect6050() {
    if (this.serialPort != null && this.serialPort.isOpened()) {
      Logger.trace("Port allready up...");
      powerOn = sendCommand(new CommandPair(GO_COMMAND));
      if(powerOn) {
        Logger.info("6050 Powered ON");
      }  
      return true;
    }

    boolean result = false;
    Logger.debug("Connecting with M 6050/6051...");

    //check the one or the other
    if (this.serialPort != null) {
      //port allready closed
      this.serialPortReader = null;
      this.serialPort = null;
    }

    //Reconnect
    try {
      this.serialPort = this.initSerialPort(portname);
      result = sendCommand(new CommandPair(GO_COMMAND));
      this.powerOn = result;
      if(powerOn) {
        Logger.info("6050 Powered ON");
      }  
    } catch (SerialPortException ex) {
      Logger.error("Failed to initialize serial port " + portname + "! " + ex.getMessage());
    }
    return (this.serialPort != null) && this.serialPort.isOpened() && result;
  }

  private void waitForPort() {
    try {
      //Time to finish a byte us apx 4.5 ms
      Thread.sleep(PAUSE_BETWEEN_BYTES);
    } catch (InterruptedException ie) {
      Logger.trace("Interrupted while waiting 5 ms." + ie.getMessage());
    }

    long now = System.currentTimeMillis();
    long timeout = now + CTS_TIMEOUT;
    if (!this.serialPortReader.isCts()) {
      //Lets wait a while
      while (timeout > now && !this.serialPortReader.isCts()) {
        now = System.currentTimeMillis();
      }
      if (!this.serialPortReader.isCts() && timeout < now) {
        Logger.warn("CTS timeout exceeded!");
      }
    }
  }

  public synchronized boolean sendSingleCommand(int command) {
    Logger.trace("Sending single command: " + command + "...");
    return sendCommand(new CommandPair(command));
  }

  public synchronized boolean sendCommand(CommandPair cp) {
    this.busy = true;
    boolean result;
    try {
      if (cp.isPowerOffCommand()) {
        this.powerOn = false;
      }
      
      if (cp.isFeedbackExpected()) {
        Logger.trace("Feedback expected for command: " + cp.getCommand());
        if (feedbackResults.get(cp.getModuleNumber()) != null) {
          feedbackResults.put(cp.getModuleNumber(), null);
        }
        this.serialPortReader.setCallback(cp);
      }

      result = serialPort.writeInt(cp.getCommand());

      Logger.trace("### C-> " + cp.getCommand() + " Send to serialport. result: " + result);
      // when false command apparently not sent correctly... should we try again?...
      if (!result) {
        Logger.warn("Command " + cp.getCommand() + " not sent...");
      }
      // wait for the serial port..
      waitForPort();

      if (cp.isAddress()) {
        result = serialPort.writeInt(cp.getAddress());
        Logger.trace("### A-> " + cp.getAddress() + " Send to serialport. result: " + result);

        if (!result) {
          Logger.warn("Address " + cp.getAddress() + " not sent...");
        }
        waitForPort();
      }

      if (cp.isPowerOnCommand() && result) {
        this.powerOn = true;
      }

      if (cp.isSwitchOffCommand()) {
        this.solenoidActive = false;
      }

      if (cp.isFeedbackExpected()) {
        Logger.trace("Waiting for feedback...");
        this.feedbackResults.put(cp.getModuleNumber(), cp.getFeedback());
      }
    } catch (SerialPortException ex) {
      Logger.error(ex);
      result = false;
    }
    this.busy = false;
    return result;
  }

  public Integer[] getFeedback(Integer moduleNumber) {
    return this.feedbackResults.get(moduleNumber);
  }

  public boolean isPowerOn() {
    return this.powerOn;
  }

  public boolean isSolenoidActive() {
    return solenoidActive;
  }

  public void setSolenoidActive(boolean solenoidActive) {
    this.solenoidActive = solenoidActive;
  }
}
