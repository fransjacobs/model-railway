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
import jcs.JCS;
import jcs.util.NetworkUtil;
import jcs.util.Ping;
import jcs.util.RunUtil;
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

  private static EcosConnectionFactory instance;

  private EcosConnection controllerConnection;
  private EcosHTTPConnection httpConnection;
  private InetAddress controllerHost;

  private static final String LAST_USED_IP_PROP_FILE = RunUtil.DEFAULT_PATH + "last-used-esu-ecos-ip.properties";

  private EcosConnectionFactory() {
  }

  public static EcosConnectionFactory getInstance() {
    if (instance == null) {
      instance = new EcosConnectionFactory();
    }
    return instance;
  }

  EcosConnection getConnectionImpl(boolean virtual) {
    if (!virtual) {
      if (controllerConnection == null) {
        String lastUsedIp = RunUtil.readProperty(LAST_USED_IP_PROP_FILE, "ip-address");
        if (lastUsedIp != null) {
          try {
            if (Ping.IsReachable(lastUsedIp)) {
              this.controllerHost = InetAddress.getByName(lastUsedIp);
              Logger.trace("Using last used IP Address: " + lastUsedIp);
            } else {
              Logger.trace("Last used IP Address: " + lastUsedIp + " is not reachable");
            }
          } catch (UnknownHostException ex) {
            Logger.trace("Last used ESU ECoS IP: " + lastUsedIp + " is invalid!");
            lastUsedIp = null;
          }
        }

        if (this.controllerHost == null) {
          Logger.trace("Try to discover a ESU ECoS...");
          JCS.logProgress("Discovering a ESU ECoS...");
          controllerHost = discoverEcos();
        }

        if (controllerHost != null) {
          if (lastUsedIp == null) {
            //Write the last used IP Addres for faster discovery next time
            writeLastUsedIpAddressProperty(controllerHost.getHostAddress());
          }
          Logger.trace("ESU ECoS ip: " + controllerHost.getHostName());

          controllerConnection = new EcosTCPConnection(controllerHost);
        } else {
          Logger.warn("Can't find a ESU ECoS Controller host!");
          JCS.logProgress("Can't find a ESU ECoS Controller on the Network");
        }
      }
    } else {
      //Virtual connection
      controllerConnection = new EcosVirtualConnection(NetworkUtil.getIPv4HostAddress());
    }
    return this.controllerConnection;
  }

  public static EcosConnection getConnection() {
    return getInstance().getConnectionImpl(false);
  }

  public static EcosConnection getConnection(boolean virtual) {
    return getInstance().getConnectionImpl(virtual);
  }

  EcosHTTPConnection getHttpConnectionImpl() {
    if (httpConnection == null) {
      httpConnection = new EcosHTTPConnection(controllerHost);
    }
    return httpConnection;
  }

  public static EcosHTTPConnection getHttpConnection() {
    return getInstance().getHttpConnectionImpl();
  }

  public static void disconnectAll() {
    if (instance.controllerConnection != null) {
      try {
        instance.controllerConnection.close();
      } catch (Exception ex) {
        Logger.trace("Error during disconnect " + ex);
      }
    }
    instance.controllerConnection = null;
    instance.controllerHost = null;
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

  public static void writeLastUsedIpAddressProperty(String ipAddress) {
    if (ipAddress != null) {
      RunUtil.writeProperty(LAST_USED_IP_PROP_FILE, "ip-address", ipAddress);
    }
  }

}
