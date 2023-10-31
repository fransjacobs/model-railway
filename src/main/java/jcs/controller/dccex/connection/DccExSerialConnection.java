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
package jcs.controller.dccex.connection;

import com.fazecast.jSerialComm.SerialPort;
import jcs.controller.dccex.DccExConnection;
import org.tinylog.Logger;
import jcs.controller.dccex.events.DccExMessageListener;

/**
 *
 * @author frans
 */
public class DccExSerialConnection implements DccExConnection {

  private String portName;

  private static SerialPort serialPort;

  public DccExSerialConnection(String portName) {
    this.portName = portName;
  }

  @Override
  public void sendMessage(String message) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isConnected() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void close() throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setMessageListener(DccExMessageListener systemListener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public static void main(String[] a) {

    SerialPort[] serialPorts = SerialPort.getCommPorts();
    for (int i = 0; i < serialPorts.length; i++) {
      Logger.trace(serialPorts[i].getDescriptivePortName() + "; " + serialPorts[i].getSystemPortName());
    }

  }
}
