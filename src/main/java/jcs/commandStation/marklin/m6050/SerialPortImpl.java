/*
 * Copyright 2018 Frans Jacobs.
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
package jcs.commandStation.marklin.m6050;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import java.util.HashMap;
import java.util.Map;
import org.tinylog.Logger;

/**
 * Class that represents the (old) serial P50 interface. This interface is connected via a RS232 serial connection To reset the 6050 enter, before starting JCS, address 9193 on the 6021
 *
 * @author frans
 */
class SerialPortImpl {

  // Marklin 6050/6051 has the following serial settings
  private static final int BAUDRATE = 2400; //SerialPort.BAUDRATE_1200 * 2;
  private static final int DATABITS = 8; //SerialPort.DATABITS_8;
  private static final int STOPBITS = SerialPort.TWO_STOP_BITS;
  private static final int PARITY = SerialPort.NO_PARITY;

  static final int PAUSE_BETWEEN_CMD = 100;
  //static final int PAUSE_BETWEEN_CMD = 200; //used with srcp implementation
  //static final int PAUSE_BETWEEN_BYTES = 2; //used with srcp implementation
  static final int PAUSE_BETWEEN_BYTES = 5;

  static final long CTS_TIMEOUT = 200;
  static final long RESPONSE_TIMEOUT = 2000L;

  static final int MAX_FB_ADDRESS = 31;
  static final int MAX_LOC_ADDRESS = 80;
  static final int MAX_GA_ADDRESS = 256;
  static final int MIN_GA_ACTIVE_TIME = 75;

  public static final int GO_COMMAND = 96;
  public static final Integer STOP_COMMAND = 97;

  private final String portname;
  private SerialPort serialPort;
  private SerialPortListener serialPortListener;
  private final Map<Integer, int[]> feedbackResults;

  private boolean powerOn;
  private boolean busy;

  private boolean solenoidActive;

  SerialPortImpl(String portname) {
    this.portname = portname;
    feedbackResults = new HashMap<>();

    try {
      if (portname == null) {
        Logger.error("Can't initializen a Serialport Interface, missing port name!");
      } else {
        serialPort = initSerialPort(portname);
        if (serialPort == null) {
          Logger.error("Could not find a Serialport: " + portname);
        } else {
          //Send a reset command
        }
      }
    } catch (SerialPortInvalidPortException ex) {
      Logger.error("Can't initialize Serialport " + portname + "; " + ex.getMessage());
    }
  }

  /**
   * @return a SerialPort or null
   */
  private SerialPort initSerialPort(String serialPortName) throws SerialPortInvalidPortException {
    SerialPort sp;
    //try {
    sp = SerialPort.getCommPort(serialPortName);
    sp.openPort();
    boolean portOK = sp.setComPortParameters(BAUDRATE, DATABITS, STOPBITS, PARITY);
    Logger.debug("Port: " + serialPortName + " parameters set: result: " + (portOK ? "OK" : "Not OK") + "...");

    if (portOK) {
      sp.setFlowControl(SerialPort.FLOW_CONTROL_CTS_ENABLED);
      //int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS;
      //sp.setEventsMask(mask);
      this.serialPortListener = new SerialPortListener(sp);
      //sp.addEventListener(serialPortListener);
      return sp;
    } //else {
    //Logger.error("Can't initialize port " + serialPortName + "...");
    //throw new SerialPortInvalidPortException(serialPortName, "Can't initialze port with default parameters",
    //        SerialPortInvalidPortExceptionTYPE_PARAMETER_IS_NOT_CORRECT);
    //}
    //} //catch (SerialPortInvalidPortException spe) {
    //if (SerialPortInvalidPortException.TYPE_PORT_BUSY.equals(spe.getExceptionType())) {
    //  Logger.error("Port already opened by an other process!");
    //} else {
    //  throw spe;
    //}
    //}
    return null;
  }

  void disconnectSerialPort() {
    try {
      //wait for command ending
      boolean waiting = this.busy;
      long now = System.currentTimeMillis();
      long timeout = now + 2000;
      while (waiting && now < timeout) {
        now = System.currentTimeMillis();
        waiting = this.busy;
      }

      this.serialPortListener = null;
      this.serialPort.closePort();
      this.serialPort = null;
    } catch (SerialPortInvalidPortException ex) {
      Logger.error(ex);
    }
    Logger.info("Port " + this.portname + " disconnected.");
  }

  boolean connectSerialPort() {
    if (this.serialPort != null && this.serialPort.isOpen()) {
      Logger.trace("Port allready up...");
      powerOn = sendCommand(new CommandAddressPair(GO_COMMAND));
      if (powerOn) {
        Logger.info("6050 Powered ON");
      }
      return true;
    }

    boolean result = false;
    Logger.debug("Connecting with M 6050/6051...");

    //check the one or the other
    if (this.serialPort != null) {
      //port allready closed
      this.serialPortListener = null;
      this.serialPort = null;
    }

    //Reconnect
    try {
      this.serialPort = this.initSerialPort(portname);
      result = sendCommand(new CommandAddressPair(GO_COMMAND));
      this.powerOn = result;
      if (powerOn) {
        Logger.info("6050 Powered ON");
      }
    } catch (SerialPortInvalidPortException ex) {
      Logger.error("Failed to initialize serial port " + portname + "! " + ex.getMessage());
    }
    return (this.serialPort != null) && this.serialPort.isOpen() && result;
  }

  private void waitForPort() {
    try {
      //Time to finish a byte us apx 4.5 ms
      Thread.sleep(PAUSE_BETWEEN_BYTES);
    } catch (InterruptedException ie) {
      Logger.warn("Interrupted while waiting " + PAUSE_BETWEEN_BYTES + " ms." + ie.getMessage());
    }

    long now = System.currentTimeMillis();
    long timeout = now + CTS_TIMEOUT;
    if (!this.serialPortListener.isCts()) {
      //Lets wait a while
      while (timeout > now && !this.serialPortListener.isCts()) {
        now = System.currentTimeMillis();
      }
      if (!this.serialPortListener.isCts() && timeout < now) {
        Logger.warn("CTS timeout exceeded!");
      }
    }
  }

  private void pauseBetweenCommands() {
    try {
      //Time to finish a byte us apx 4.5 ms
      Thread.sleep(PAUSE_BETWEEN_CMD);
    } catch (InterruptedException ie) {
      Logger.warn("Interrupted while waiting " + PAUSE_BETWEEN_CMD + " ms." + ie.getMessage());
    }
  }

  boolean sendSingleCommand(int command) {
    Logger.trace("Sending single command: " + command + "...");
    return sendCommand(new CommandAddressPair(command));
  }

  boolean sendCommand(CommandAddressPair cp) {
    this.busy = true;
    boolean result = false;
    try {
      if (cp.isPowerOffCommand()) {
        this.powerOn = false;
        Logger.debug("Power OFF!");
      }
      if (cp.isPowerOnCommand()) {
        this.powerOn = false;
        Logger.debug("Power ON");
      }

      if (cp.isFeedbackExpected()) {
        Logger.trace("Feedback expected for command: " + cp.getCommand());
        if (feedbackResults.get(cp.getModuleNumber()) != null) {
          feedbackResults.put(cp.getModuleNumber(), null);
        }
        this.serialPortListener.setCallback(cp);
      }

      //result = serialPort.writeInt(cp.getCommand());
//      //be very sure
//      if (cp.isPowerOffCommand() || cp.isPowerOnCommand()) {
//        pauseBetweenCommands();
//        result = serialPort.writeInt(cp.getCommand());
//      }

      Logger.trace("### C-> " + cp.getCommand() + " Send to serialport. result: " + result);
      // when false command apparently not sent correctly... should we try again?...
      if (!result) {
        Logger.warn("Command " + cp.getCommand() + " not sent...");
      }
      // wait for the serial port..
      waitForPort();

      if (cp.hasAddress()) {
        //result = serialPort.writeInt(cp.getAddress());
        Logger.trace("### A-> " + cp.getAddress() + " Send to serialport. result: " + result);

        if (!result) {
          Logger.warn("Address " + cp.getAddress() + " not sent...");
        }
        waitForPort();
      }

      if (cp.isPowerOnCommand() && result) {
        this.powerOn = true;
      }

      if ((cp.isPowerOnCommand() || cp.isPowerOffCommand()) && !result) {
        Logger.warn("Command Power " + (cp.isPowerOnCommand() ? "On" : "Off") + " send, result is " + result + "!");
      }

      if (cp.isSwitchOffCommand()) {
        this.solenoidActive = false;
      }

      if (cp.isFeedbackExpected()) {
        Logger.trace("Waiting for feedback...");
        this.feedbackResults.put(cp.getModuleNumber(), cp.getFeedback());
      }
    } catch (SerialPortInvalidPortException ex) {
      Logger.error(ex);
      result = false;
    }
    this.busy = false;
    return result;
  }

  int[] getFeedback(Integer moduleNumber) {
    return this.feedbackResults.get(moduleNumber);
  }

  boolean isPowerOn() {
    return this.powerOn;
  }

  boolean isSolenoidActive() {
    return solenoidActive;
  }

  void setSolenoidActive(boolean solenoidActive) {
    this.solenoidActive = solenoidActive;
  }
}
