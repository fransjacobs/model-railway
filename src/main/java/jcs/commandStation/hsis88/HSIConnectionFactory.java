/*
 * Copyright 2023 fransjacobs.
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

import jcs.JCS;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class HSIConnectionFactory {

  private static HSIConnectionFactory instance;
  private HSIConnection controllerConnection;

  private static final String LAST_USED_COM_PORT_FILE = RunUtil.DEFAULT_PATH + "last-used-hsi-s88-serial.properties";

  private HSIConnectionFactory() {
  }

  public static HSIConnectionFactory getInstance() {
    if (instance == null) {
      instance = new HSIConnectionFactory();
    }
    return instance;
  }

  HSIConnection getConnectionImpl(ConnectionType connectionType) {
    if (controllerConnection == null) {
      String lastUsedSerial = RunUtil.readProperty(LAST_USED_COM_PORT_FILE, "hsi-s88-com-port");
      if (lastUsedSerial != null) {
        controllerConnection = new HSISerialConnection(lastUsedSerial);
      } else {
        Logger.warn("Last used serial port not available.");
        JCS.logProgress("HSI-S88 Can't find a Serial Port!");
      }
    } else {
      throw new RuntimeException("Unknown connection type or connection type not set.");
    }

    return this.controllerConnection;
  }

  public static HSIConnection getConnection(ConnectionType connectionType) {
    return getInstance().getConnectionImpl(connectionType);
  }

  public static void disconnectAll() {
    try {
      instance.controllerConnection.close();
    } catch (Exception ex) {
      Logger.trace("Error during disconnect " + ex);
    }
    instance.controllerConnection = null;
  }

  public static void writeLastUsedSerialPortProperty(String comPort) {
    if (comPort != null) {
      RunUtil.writeProperty(LAST_USED_COM_PORT_FILE, "hsi-s88-com-port", comPort);
    }
  }

}
