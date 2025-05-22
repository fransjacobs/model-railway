/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.esu.ecos.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import jcs.entities.CommandStationBean;
import jcs.util.NetworkUtil;
import net.straylightlabs.hola.dns.Domain;
import net.straylightlabs.hola.sd.Instance;
import net.straylightlabs.hola.sd.Query;
import net.straylightlabs.hola.sd.Service;
import org.tinylog.Logger;

/**
 * Try to connect with a ESU ECoS 50xxx.
 */
public class EcosConnectionFactory {

  private static final String ESU_MRTP_SERVICE = "_esu-mrtp._tcp";

  private static EcosConnection controllerConnection;

  private static EcosHTTPConnection httpConnection;

  private static InetAddress controllerHost;
  private static boolean forceVirtual = false;
  private static boolean virtual;

  static {
    forceVirtual = "true".equals(System.getProperty("connection.always.virtual", "false"));
  }

  public static EcosConnection getConnection(CommandStationBean commandStation) {
    return getConnection(commandStation, (virtual != commandStation.isVirtual()));
  }

  public static EcosConnection getConnection(CommandStationBean commandStation, boolean reconnect) {
    if (reconnect) {
      disconnectAll();
    }

    virtual = commandStation.isVirtual();
    if (!virtual && forceVirtual) {
      virtual = forceVirtual;
      Logger.info("Forcing a virtual connection!");
    }

    try {
      if (virtual) {
        controllerHost = InetAddress.getLocalHost();
      } else {
        controllerHost = InetAddress.getByName(commandStation.getIpAddress());
      }
    } catch (UnknownHostException ex) {
      Logger.error("Invalid ip address : " + commandStation.getIpAddress());
      return null;
    }

    if (controllerConnection == null) {
      if (virtual) {
        controllerConnection = new EcosVirtualConnection(controllerHost);
      } else {
        controllerConnection = new EcosTCPConnection(controllerHost);
      }
    }
    return controllerConnection;
  }

  public static EcosHTTPConnection getHttpConnection() {
    if (httpConnection == null) {
      httpConnection = new EcosHTTPConnection(controllerHost);
    }
    return httpConnection;
  }

  public static void disconnectAll() {
    httpConnection = null;

    if (controllerConnection != null) {
      try {
        controllerConnection.close();
      } catch (Exception ex) {
        Logger.trace("Error during disconnect " + ex);
      }
    }
    controllerConnection = null;
    controllerHost = null;
  }

  public static String getControllerIp() {
    if (controllerHost != null) {
      return controllerHost.getHostAddress();
    } else {
      return "Unknown";
    }
  }

  /**
   * Try to Automatically discover the ESU ECoS IP Address on the local network.<br>
   * mDNS is used to discover the ECoS
   *
   * @return the IP Address of the ECoS of null if not discovered.
   */
  public static InetAddress discoverEcos() {
    InetAddress ecosIp = null;

    try {
      Service ecosService = Service.fromName(ESU_MRTP_SERVICE);
      Query ecosQuery = Query.createFor(ecosService, Domain.LOCAL);

      Set<Instance> ecosInstances = ecosQuery.runOnceOn(NetworkUtil.getIPv4HostAddress());

      Logger.trace("Found " + ecosInstances.size());

      if (ecosInstances.isEmpty()) {
        Logger.warn("Could not find a ESU ECoS host on the local network!");
        return null;
      }

      Instance ecos = ecosInstances.iterator().next();
      Logger.trace("ESU ECoS: " + ecos);

      Set<InetAddress> addresses = ecos.getAddresses();

      //Find the first ip4 address
      for (InetAddress ia : addresses) {
        if (ia instanceof Inet4Address) {
          ecosIp = ia;
          break;
        }
      }
    } catch (IOException ex) {
      Logger.error(ex.getMessage());
    }
    return ecosIp;
  }

//  public static void writeLastUsedIpAddressProperty(String ipAddress) {
//    if (ipAddress != null) {
//      RunUtil.writeProperty(LAST_USED_IP_PROP_FILE, "ip-address", ipAddress);
//    }
//  }
}
