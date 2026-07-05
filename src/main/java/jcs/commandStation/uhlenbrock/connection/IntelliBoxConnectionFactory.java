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

import jcs.JCS;
import jcs.commandStation.dccex.DccExConnection;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.util.RunUtil;
import jcs.util.SerialPortUtil;
import org.tinylog.Logger;

public class IntelliBoxConnectionFactory {

  private static IntelliBoxConnectionFactory instance;

  private DccExConnection controllerConnection;

  private static final String LAST_USED_COM_PORT_FILE = RunUtil.DEFAULT_PATH + "last-used-p50x-serial.properties";

  private IntelliBoxConnectionFactory() {
  }

  public static IntelliBoxConnectionFactory getInstance() {
    if (instance == null) {
      instance = new IntelliBoxConnectionFactory();
    }
    return instance;
  }

  DccExConnection getConnectionImpl(ConnectionType connectionType) {
    if (controllerConnection == null) {
      if (null == connectionType) {
        throw new RuntimeException("Unknown connection type or connection type not set.");
      } else {
        String lastUsedSerial = RunUtil.readProperty(LAST_USED_COM_PORT_FILE, "dcc-ex-com-port");
        if (lastUsedSerial != null) {
          if (SerialPortUtil.portSystemNameExists(lastUsedSerial)) {
            controllerConnection = new IntelliBoxSerialConnection(lastUsedSerial);
          } else {
            Logger.warn("Last used serial port: {} not found in the available ports!", lastUsedSerial);
            JCS.logProgress("IntelliBox Can't find a Serial Port " + lastUsedSerial + "!");
          }
        } else {
          Logger.warn("Last used SerialPort not set!");
          JCS.logProgress("Can't find a IntelliBox Serial Port!");
        }
      }
    }
    return this.controllerConnection;
  }

  public static DccExConnection getConnection(ConnectionType connectionType) {
    return getInstance().getConnectionImpl(connectionType);
  }

  public static void disconnectAll() {
    try {
      instance.controllerConnection.close();
    } catch (Exception ex) {
      Logger.trace("Error during disconnect {}", ex.getMessage());
    }
    instance.controllerConnection = null;
  }

  public static void writeLastUsedSerialPortProperty(String comPort) {
    if (comPort != null) {
      RunUtil.writeProperty(LAST_USED_COM_PORT_FILE, "p50x-com-port", comPort);
    }
  }

}
