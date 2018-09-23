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

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class SerialPortReader implements SerialPortEventListener {

    private CommandPair callback;
    private boolean cts;
    private final SerialPort serialPort;

    public SerialPortReader(SerialPort serialPort) {
      this.serialPort = serialPort;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
      if (event.isRXCHAR()) {
        try {
          int[] resp = serialPort.readIntArray();
          Logger.trace("Response: " + resp[0]);
          this.callback.addResponse(resp[0]);
        } catch (SerialPortException spe) {
          Logger.error("Can't Read from SerialPort" + spe);
        } catch (Exception e) {
          Logger.error("Error in SerialPort event handling!", e);
        }
      } else if (event.isCTS()) {
        this.cts = event.getEventValue() == 1;
        Logger.trace("CTS: " + (this.cts ? "On" : "Off"));
      }
    }

    public boolean isCts() {
      return this.cts;
    }

    public void setCallback(CommandPair callback) {
      this.callback = callback;
    }

    public CommandPair getCallback() {
      return this.callback;
    }

    public void clearCallback() {
      this.callback = null;
    }
  }