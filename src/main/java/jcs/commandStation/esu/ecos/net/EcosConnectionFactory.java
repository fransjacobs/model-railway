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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import jcs.util.NetworkUtil;
import net.straylightlabs.hola.dns.Domain;
import net.straylightlabs.hola.sd.Instance;
import net.straylightlabs.hola.sd.Query;
import net.straylightlabs.hola.sd.Service;
import org.tinylog.Logger;

/**
 * Creates and owns an ECoS transport session.<br>
 * Does NOT start threads, retry, wait for other attempts or schedule reconnection.<br>
 * ConnectionLifecycleManager calls acquireConnection() once per scheduled attempt.
 *
 * The TCP transport owns its receiver and heartbeat threads; those are not connection-acquisition or retry threads.
 */
public final class EcosConnectionFactory {

  private static final String ESU_MRTP_SERVICE = "_esu-mrtp._tcp";
  private static final boolean FORCE_VIRTUAL = Boolean.getBoolean("connection.always.virtual");

  private static final EcosConnectionFactory INSTANCE = new EcosConnectionFactory();
  private static volatile InetAddress controllerHost;

  private final AtomicLong generation = new AtomicLong();
  private volatile EcosConnection controllerConnection;
  // Retain a failed session until lifecycle cleanup can close its executors.
  private EcosConnection retiredConnection;
  private volatile EcosHTTPConnection httpConnection;
  private volatile Consumer<EcosConnection> connectionLostListener;
  private volatile String ipAddress;
  private volatile boolean virtual;

  private EcosConnectionFactory() {
  }

  public static EcosConnectionFactory getInstance() {
    return INSTANCE;
  }

  public static boolean isForceVirtual() {
    return FORCE_VIRTUAL;
  }

  public static InetAddress getControllerHost() {
    return controllerHost;
  }

  public static void setControllerHost(InetAddress host) {
    controllerHost = host;
  }

  public boolean isVirtual() {
    return FORCE_VIRTUAL || virtual;
  }

  public void setVirtual(boolean value) {
    virtual = value;
  }

  public void setIpAddress(String value) {
    ipAddress = value;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getControllerIp() {
    return ipAddress;
  }

  public EcosConnection getConnection() {
    return controllerConnection;
  }

  public EcosHTTPConnection getHttpConnection() {
    return httpConnection;
  }

  public boolean isConnected() {
    EcosConnection current = controllerConnection;
    return current != null && current.isConnected();
  }

  /**
   * Notification only.<br>
   * The lifecycle manager decides when to retry; this callback runs on a transport thread and must not perform socket cleanup.
   */
  public void setConnectionLostListener(Consumer<EcosConnection> listener) {
    connectionLostListener = listener;
  }

  /**
   * One bounded connection attempt on the caller's thread. Never retries and never creates a connector worker thread.
   *
   * Connect should be called from ConnectionLifecycleManager, not Swing EDT.
   */
  public EcosConnection acquireConnection() {
    boolean useVirtual = isVirtual();
    EcosConnection existing = controllerConnection;
    if (existing != null && existing.isConnected() && existing.isVirtual() == useVirtual) {
      return existing;
    }

    // Invalidate stale sessions before creating a new one.
    disconnectAll();
    long token = generation.get();

    InetAddress address;
    if (useVirtual) {
      // Virtual mode must work even on hosts without a resolvable hostname.
      address = InetAddress.getLoopbackAddress();
    } else {
      address = resolveAddress(ipAddress);
      if (address == null) {
        address = discoverEcos();
      }
    }

    if (address == null || Thread.currentThread().isInterrupted()) {
      return null;
    }

    EcosConnection candidate = null;
    try {
      candidate = useVirtual ? new EcosVirtualConnection(address)
              : new EcosTCPConnection(address, this::onConnectionLost);

      if (!candidate.isConnected() || Thread.currentThread().isInterrupted()) {
        return closeCandidate(candidate);
      }

      synchronized (this) {
        if (token == generation.get()) {
          controllerConnection = candidate;
          controllerHost = candidate.getControllerAddress();
          httpConnection = new EcosHTTPConnection(address);
          ipAddress = address.getHostAddress();
          Logger.info("Connected to ESU ECoS at {}", ipAddress);
          return candidate;
        }
      }
      // disconnectAll() was invoked during connect (e.g. shutdown).
      return closeCandidate(candidate);
    } catch (RuntimeException ex) {
      Logger.warn("Failed to connect to ESU ECoS {}: {}", address, ex.getMessage());
      return closeCandidate(candidate);
    }
  }

  private static EcosConnection closeCandidate(EcosConnection candidate) {
    if (candidate != null) {
      try {
        candidate.close();
      } catch (Exception ex) {
        Logger.warn("Error closing unusable ECoS transport: {}", ex.getMessage());
      }
    }
    return null;
  }

  /**
   * Idempotent cleanup.<br>
   * In-flight attempts are invalidated with a generation token, so shutdown cannot publish a late successful connection.
   */
  public void disconnectAll() {
    EcosConnection old;
    EcosConnection retired;
    synchronized (this) {
      generation.incrementAndGet();
      old = controllerConnection;
      retired = retiredConnection;
      controllerConnection = null;
      retiredConnection = null;
      httpConnection = null;
      controllerHost = null;
    }
    closeCandidate(old);
    if (retired != old) {
      closeCandidate(retired);
    }
  }

  public static void disconnect() {
    INSTANCE.disconnectAll();
  }

  private void onConnectionLost(EcosConnection lost) {
    Consumer<EcosConnection> callback;
    synchronized (this) {
      if (controllerConnection != lost) {
        return;
      }
      generation.incrementAndGet();
      controllerConnection = null;
      retiredConnection = lost;
      httpConnection = null;
      controllerHost = null;
      callback = connectionLostListener;
    }
    // Never close or reconnect the transport from this callback.
    if (callback != null) {
      try {
        callback.accept(lost);
      } catch (RuntimeException ex) {
        Logger.warn("ECoS connection-loss listener failed: {}", ex.getMessage());
      }
    }
  }

  private static InetAddress resolveAddress(String host) {
    if (host == null || host.isBlank()) {
      return null;
    }
    try {
      return InetAddress.getByName(host);
    } catch (UnknownHostException ex) {
      Logger.warn("Invalid ECoS address {}: {}", host, ex.getMessage());
      return null;
    }
  }

  /**
   * Optional discovery as part of the caller-owned attempt.
   */
  public static InetAddress discoverEcos() {
    try {
      Service service = Service.fromName(ESU_MRTP_SERVICE);
      Query query = Query.createFor(service, Domain.LOCAL);
      Set<Instance> instances = query.runOnceOn(NetworkUtil.getIPv4HostAddress());
      for (Instance instance : instances) {
        for (InetAddress address : instance.getAddresses()) {
          if (address instanceof Inet4Address) {
            return address;
          }
        }
      }
    } catch (IOException ex) {
      Logger.warn("ECoS mDNS discovery failed: {}", ex.getMessage());
    }
    return null;
  }
}
