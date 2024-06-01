/*
 * Copyright 2023 Frans Jacobs.
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import jcs.JCS;
import jcs.commandStation.dccex.DccExConnection;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.util.Ping;
import jcs.util.RunUtil;
import jcs.util.SerialPortUtil;
import org.tinylog.Logger;

public class DccExConnectionFactory {

  private static DccExConnectionFactory instance;

  private DccExConnection controllerConnection;
  private InetAddress controllerHost;

  private static final String LAST_USED_IP_PROP_FILE = RunUtil.DEFAULT_PATH + "last-used-dcc-ex-ip.properties";
  private static final String LAST_USED_COM_PORT_FILE = RunUtil.DEFAULT_PATH + "last-used-dcc-ex-serial.properties";

  private DccExConnectionFactory() {
  }

  public static DccExConnectionFactory getInstance() {
    if (instance == null) {
      instance = new DccExConnectionFactory();
    }
    return instance;
  }

  DccExConnection getConnectionImpl(ConnectionType connectionType) {
    if (controllerConnection == null) {
      if (null == connectionType) {
        throw new RuntimeException("Unknown connection type or connection type not set.");
      } else {
        switch (connectionType) {
          case NETWORK -> {
            String lastUsedIp = RunUtil.readProperty(LAST_USED_IP_PROP_FILE, "dcc-ex-ip-address");
            if (lastUsedIp != null) {
              try {
                if (Ping.IsReachable(lastUsedIp)) {
                  this.controllerHost = InetAddress.getByName(lastUsedIp);
                } else {
                  Logger.trace("IP Address: " + lastUsedIp + " is not reachable");
                }
              } catch (UnknownHostException ex) {
                Logger.trace("DCC-EX IP: " + lastUsedIp + " is invalid!");
                lastUsedIp = null;
              }
            }
            if (controllerHost != null) {
              if (lastUsedIp == null) {
                writeLastUsedIpAddressProperty(controllerHost.getHostAddress());
              }
              Logger.trace("DCC-EX ip: " + controllerHost.getHostName());
              controllerConnection = new DccExTCPConnection(controllerHost);
            } else {
              Logger.warn("Last used IP Address not set!");
              JCS.logProgress("Can't find a DCC-EX Command Station on the Network");
            }
          }
          case SERIAL -> {
            String lastUsedSerial = RunUtil.readProperty(LAST_USED_COM_PORT_FILE, "dcc-ex-com-port");
            if (lastUsedSerial != null) {
              if (SerialPortUtil.portSystemNameExists(lastUsedSerial)) {
                controllerConnection = new DccExSerialConnection(lastUsedSerial);
              } else {
                Logger.warn("Last used serial port: " + lastUsedSerial + " not found in the available ports!");
                JCS.logProgress("DCC-EX Can't find a Serial Port " + lastUsedSerial + "!");
              }
            } else {
              Logger.warn("Last used SerialPort not set!");
              JCS.logProgress("Can't find a DCC-EX Can't find a Serial Port!");
            }
          }
          default ->
            throw new RuntimeException("Unknown connection type or connection type not set.");
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
      Logger.trace("Error during disconnect " + ex);
    }
    instance.controllerConnection = null;
  }

  String getControllerIpImpl() {
    if (this.controllerHost != null) {
      return this.controllerHost.getHostAddress();
    } else {
      return "Unknown";
    }
  }

  public static String getControllerIp() {
    return getInstance().getControllerIpImpl();
  }

  public static void writeLastUsedIpAddressProperty(String ipAddress) {
    if (ipAddress != null) {
      RunUtil.writeProperty(LAST_USED_IP_PROP_FILE, "dcc-ex-ip-address", ipAddress);
    }
  }

  public static void writeLastUsedSerialPortProperty(String comPort) {
    if (comPort != null) {
      RunUtil.writeProperty(LAST_USED_COM_PORT_FILE, "dcc-ex-com-port", comPort);
    }
  }

}
