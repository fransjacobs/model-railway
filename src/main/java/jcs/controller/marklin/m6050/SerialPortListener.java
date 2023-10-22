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
package jcs.controller.marklin.m6050;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 *
 * @author frans
 */
class SerialPortListener implements SerialPortDataListener {

  private CommandAddressPair callback;
  private boolean cts;
  private final SerialPort serialPort;

  SerialPortListener(SerialPort serialPort) {
    this.serialPort = serialPort;
  }

  @Override
  public void serialEvent(SerialPortEvent event) {
//    if (event.isRXCHAR()) {
//      try {
//        int[] resp = serialPort.readIntArray();
//        Logger.trace("Response: " + resp[0]);
//        this.callback.addResponse(resp[0]);
//      } catch (SerialPortException spe) {
//        Logger.error("Can't Read from SerialPort" + spe);
//      } catch (Exception e) {
//        Logger.error("Error in SerialPort event handling!", e);
//      }
//    } else if (event.isCTS()) {
//      this.cts = event.getEventValue() == 1;
//      Logger.trace("CTS: " + (this.cts ? "On" : "Off"));
//    }
  }

  boolean isCts() {
    return this.cts;
  }

  void setCallback(CommandAddressPair callback) {
    this.callback = callback;
  }

  CommandAddressPair getCallback() {
    return this.callback;
  }

  void clearCallback() {
    this.callback = null;
  }

  @Override
  public int getListeningEvents() {
    throw new UnsupportedOperationException("Not supported yet."); 
  }
  
  
  
}
