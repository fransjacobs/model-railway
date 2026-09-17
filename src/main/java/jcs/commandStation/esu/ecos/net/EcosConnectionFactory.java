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
import jcs.util.NetworkUtil;
import jcs.util.Ping;
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

  private static EcosConnectionFactory ecosConnectionFactory;

  private volatile boolean autoReAcquireConnection = true;

  private static final long DEFAULT_ACQUIRE_TIMEOUT_MS = EcosConnection.DEFAULT_CONNECT_TIMEOUT_MS;
  private static final int LAST_IP_PING_TIMEOUT_MS = 500;

  private volatile EcosConnection controllerConnection;

  private volatile EcosHTTPConnection httpConnection;

  private static InetAddress controllerHost;
  private static boolean FORCE_VIRTUAL = false;

  private volatile String ipAddress;
  private volatile boolean virtual;

  private volatile EcosConnector ecosConnector;

  static {
    FORCE_VIRTUAL = "true".equals(System.getProperty("connection.always.virtual", "false"));
  }

  private EcosConnectionFactory() {

  }

  public static EcosConnectionFactory getInstance() {
    if (ecosConnectionFactory == null) {
      ecosConnectionFactory = new EcosConnectionFactory();
    }
    return ecosConnectionFactory;
  }

  public static boolean isForceVirtual() {
    return EcosConnectionFactory.FORCE_VIRTUAL;
  }

  public static InetAddress getControllerHost() {
    return controllerHost;
  }

  public static void setControllerHost(InetAddress controllerHost) {
    EcosConnectionFactory.controllerHost = controllerHost;
  }

  public boolean isVirtual() {
    return virtual;
  }

  public void setVirtual(boolean virtual) {
    this.virtual = virtual;
  }

  public boolean isAutoReAcquireConnection() {
    return autoReAcquireConnection;
  }

  public void setAutoReAcquireConnection(boolean autoReAcquireConnection) {
    this.autoReAcquireConnection = autoReAcquireConnection;
  }

  public void stopConnectionAcquire() {
    setAutoReAcquireConnection(false);
    if (ecosConnector != null && ecosConnector.isRunning()) {
      ecosConnector.quit();
    }
  }

  public EcosConnection getConnection() {
    return controllerConnection;
  }

  public EcosHTTPConnection getHttpConnection() {
    return httpConnection;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public boolean isConnected() {
    return controllerConnection != null && controllerConnection.isConnected();
  }

  public synchronized void startEcosConnector() {
    EcosConnection current = controllerConnection;
    boolean virt = FORCE_VIRTUAL || virtual;

    if (current != null && current.isConnected() && current.isVirtual() == virt) {
      return;
    }

    if (ecosConnector != null && ecosConnector.isRunning()) {
      Logger.trace("ECoS connector thread is already running...");
      return;
    }

    ecosConnector = new EcosConnector(this);
    ecosConnector.start();
  }

  public static void disconnect() {
    EcosConnectionFactory factory = EcosConnectionFactory.getInstance();
    factory.disconnectAll();
  }

  public synchronized void disconnectAll() {
    setAutoReAcquireConnection(false);
    EcosConnector connector = ecosConnector;
    ecosConnector = null;

    if (connector != null && connector.isRunning()) {
      connector.quit();
      connector.interrupt();
      try {
        connector.join(1000L);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }

    httpConnection = null;

    EcosConnection connection = controllerConnection;
    controllerConnection = null;
    controllerHost = null;

    if (connection != null) {
      try {
        connection.close();
      } catch (Exception ex) {
        Logger.trace("Error during disconnect " + ex);
      }
    }
  }

  public String getControllerIp() {
    return ipAddress;
  }

  public EcosConnection awaitConnection(long timeoutMillis) {
    long now = System.currentTimeMillis();
    long timeout = now + Math.max(1L, timeoutMillis);

    while (now < timeout) {
      EcosConnection connection = controllerConnection;
      if (connection != null && connection.isConnected()) {
        return connection;
      }
      zleep(10);
      now = System.currentTimeMillis();
    }

    EcosConnection connection = controllerConnection;

    if (connection != null && connection.isConnected()) {
      if (ecosConnector != null) {
        if (ecosConnector.isRunning()) {
          ecosConnector.quit();
          try {
            ecosConnector.join(1000L);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
          }
          ecosConnector = null;
        }
      }
    }

    return connection != null && connection.isConnected() ? connection : null;
  }

  public static InetAddress discoverEcos() {
    EcosConnectionFactory factory = EcosConnectionFactory.getInstance();
    return factory.discoverEcosMdns();
  }

  /**
   * Try to Automatically discover the ESU ECoS IP Address on the local network.<br>
   * mDNS is used to discover the ECoS
   *
   * @return the IP Address of the ECoS of null if not discovered.
   */
  private InetAddress discoverEcosMdns() {
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

  private void zleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  private static InetAddress resolveAddress(String ipAddress) {
    if (ipAddress == null || ipAddress.isBlank()) {
      return null;
    }

    try {
      return InetAddress.getByName(ipAddress);
    } catch (UnknownHostException ex) {
      Logger.warn("Invalid ESU ECoS IP address '{}': {}", ipAddress, ex.getMessage());
      return null;
    }
  }

  private EcosConnection createConnection(InetAddress address, boolean useVirtual) {
    EcosConnection connection = null;

    try {
      long now = System.currentTimeMillis();
      long timeout = now + Math.max(1L, DEFAULT_ACQUIRE_TIMEOUT_MS);

      connection = useVirtual ? new EcosVirtualConnection(address) : new EcosTCPConnection(address, this::onConnectionLost);

      while (!connection.isConnected() && now < timeout) {
        zleep(50);
        now = System.currentTimeMillis();
      }

      if (connection.isConnected()) {
        controllerHost = connection.getControllerAddress();
        controllerConnection = connection;
        Logger.info("Connected to ESU ECoS at {}", controllerHost.getHostAddress());

        return connection;
      }

      Logger.warn("Created ESU ECoS connection for {} but it is not connected", address.getHostAddress());
      connection.close();
      return null;

    } catch (Exception ex) {
      Logger.error("Could not create ESU ECoS connection to {}: {}", address.getHostAddress(), ex.getMessage());
      if (connection != null) {
        try {
          connection.close();
        } catch (Exception closeEx) {
          Logger.trace("Error closing failed ESU ECoS connection: {}", closeEx.getMessage());
        }
      }
      return null;
    }
  }

  private EcosHTTPConnection createHttpConnection(InetAddress host) {
    if (host == null) {
      Logger.warn("Cannot create ECoS HTTP connection because controller host is unknown");
      return null;
    }

    if (httpConnection == null) {
      httpConnection = new EcosHTTPConnection(host);
    }
    return httpConnection;
  }

  void onConnectionLost(EcosConnection lostConnection) {
    Logger.warn("ECoS connection lost.");

    if (controllerConnection == lostConnection) {
      controllerConnection = null;
      controllerHost = null;
      httpConnection = null;
    }

    if (autoReAcquireConnection) {
      startEcosConnector();
    }
  }

  private static class EcosConnector extends Thread {

    private final EcosConnectionFactory factory;
    private volatile boolean running;

    EcosConnector(EcosConnectionFactory ecosConnectionFactory) {
      super("ECOS-CONNECTION-CONNECTOR");
      this.factory = ecosConnectionFactory;
      setDaemon(true);
    }

    boolean isRunning() {
      return running;
    }

    void quit() {
      running = false;
    }

    @Override
    public void run() {
      running = true;
      Logger.trace("ECoS Connector thread is starting...");
      InetAddress ecosAddress;

      while (running && factory.controllerConnection == null) {
        try {
          if (factory.isVirtual()) {
            ecosAddress = InetAddress.getLocalHost();
          } else {
            if (factory.ipAddress != null) {
              Logger.trace("Trying last known ESU ECoS IP address {}", factory.ipAddress);
              ecosAddress = resolveAddress(factory.ipAddress);
              if (ecosAddress == null || !Ping.isReachable(factory.ipAddress, LAST_IP_PING_TIMEOUT_MS)) {
                Logger.trace("Last known ESU ECoS IP address {} is not reachable. Trying to discover it...", factory.ipAddress);
                ecosAddress = factory.discoverEcosMdns();
              }
            } else {
              Logger.trace("Trying to discover ESU ECoS using mDNS...");
              ecosAddress = factory.discoverEcosMdns();
            }
          }

          if (ecosAddress != null) {
            factory.ipAddress = ecosAddress.getHostAddress();
            Logger.trace("Trying to establish a connection with ip: {}", factory.ipAddress);

            factory.controllerConnection = factory.createConnection(ecosAddress, factory.virtual);
            factory.httpConnection = factory.createHttpConnection(ecosAddress);
          } else {
            Logger.warn("Could not discover an ESU ECoS on the local network");
          }
        } catch (UnknownHostException ex) {
          Logger.error("ESU ECoS connection attempt failed: {}", ex.getMessage());
        }

        if (factory.controllerConnection == null) {
          Logger.trace("No connection yet...");
          factory.zleep(1000);
        } else {
          running = false;
        }
      }
      Logger.trace("ECoS Connector thread is finished...");
    }
  }
}
