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
package jcs.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author frans
 */
@Table(name = "command_stations")
public class CommandStationBean {

  private String id;
  private String description;
  private String shortName;
  private String className;
  private String connectVia;
  private String serialPort;
  private String ipAddress;
  private Integer networkPort;
  private boolean ipAutoConfiguration;
  private boolean commandAndControlSupport;
  private boolean feedbackSupport;
  private boolean locomotiveSynchronizationSupport;
  private boolean accessorySynchronizationSupport;
  private boolean locomotiveImageSynchronizationSupport;
  private boolean locomotiveFunctionSynchronizationSupport;
  private String protocols;
  private boolean defaultCs;
  private boolean enabled;
  private String lastUsedSerial;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Column(name = "description", nullable = false)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Column(name = "short_name", length = 255, nullable = false)
  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  @Column(name = "class_name", length = 255, nullable = false)
  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  @Column(name = "connect_via", length = 255, nullable = false)
  public String getConnectVia() {
    return connectVia;
  }

  public void setConnectVia(String connectVia) {
    this.connectVia = connectVia;
  }

  @Transient
  public ConnectionType getConnectionType() {
    if (connectVia != null) {
      return ConnectionType.get(connectVia);
    } else {
      return null;
    }
  }

  public void setConnectionType(ConnectionType connectionType) {
    if (connectionType == null) {
      connectVia = null;
    } else {
      connectVia = connectionType.getConnectionType();
    }
  }

  @Column(name = "serial_port", length = 255, nullable = true)
  public String getSerialPort() {
    return serialPort;
  }

  public void setSerialPort(String serialPort) {
    this.serialPort = serialPort;
  }

  @Column(name = "ip_address", length = 255, nullable = true)
  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  @Column(name = "network_port", nullable = true)
  public Integer getNetworkPort() {
    return networkPort;
  }

  public void setNetworkPort(Integer networkPort) {
    this.networkPort = networkPort;
  }

  @Column(name = "ip_auto_conf", nullable = false, columnDefinition = "ip_auto_conf bool default '0'")
  public boolean isIpAutoConfiguration() {
    return ipAutoConfiguration;
  }

  public void setIpAutoConfiguration(boolean ipAutoConfiguration) {
    this.ipAutoConfiguration = ipAutoConfiguration;
  }

  @Column(name = "supports_command_control", nullable = false, columnDefinition = "supports_command_control bool default '1'")
  public boolean isCommandAndControlSupport() {
    return commandAndControlSupport;
  }

  public void setCommandAndControlSupport(boolean commandAndControlSupport) {
    this.commandAndControlSupport = commandAndControlSupport;
  }

  @Column(name = "supports_feedback", nullable = false, columnDefinition = "supports_feedback bool default '1'")
  public boolean isFeedbackSupport() {
    return feedbackSupport;
  }

  public void setFeedbackSupport(boolean feedbackSupport) {
    this.feedbackSupport = feedbackSupport;
  }

  @Column(name = "supports_loco_synch", nullable = false, columnDefinition = "supports_loco_synch bool default '0'")
  public boolean isLocomotiveSynchronizationSupport() {
    return locomotiveSynchronizationSupport;
  }

  public void setLocomotiveSynchronizationSupport(boolean locomotiveSynchronizationSupport) {
    this.locomotiveSynchronizationSupport = locomotiveSynchronizationSupport;
  }

  @Column(name = "supports_accessory_synch", nullable = false, columnDefinition = "supports_accessory_synch bool default '0'")
  public boolean isAccessorySynchronizationSupport() {
    return accessorySynchronizationSupport;
  }

  public void setAccessorySynchronizationSupport(boolean accessorySynchronizationSupport) {
    this.accessorySynchronizationSupport = accessorySynchronizationSupport;
  }

  @Column(name = "supports_loco_image_synch", nullable = false, columnDefinition = "supports_loco_image_synch bool default '0'")
  public boolean isLocomotiveImageSynchronizationSupport() {
    return locomotiveImageSynchronizationSupport;
  }

  public void setLocomotiveImageSynchronizationSupport(boolean locomotiveImageSynchronizationSupport) {
    this.locomotiveImageSynchronizationSupport = locomotiveImageSynchronizationSupport;
  }

  @Column(name = "supports_loco_function_synch", nullable = false, columnDefinition = "supports_loco_function_synch bool default '0'")
  public boolean isLocomotiveFunctionSynchronizationSupport() {
    return locomotiveFunctionSynchronizationSupport;
  }

  public void setLocomotiveFunctionSynchronizationSupport(boolean locomotiveFunctionSynchronizationSupport) {
    this.locomotiveFunctionSynchronizationSupport = locomotiveFunctionSynchronizationSupport;
  }

  @Column(name = "protocols", length = 255, nullable = false)
  public String getProtocols() {
    return protocols;
  }

  public void setProtocols(String protocols) {
    this.protocols = protocols;
  }

  @Column(name = "default_cs", nullable = false, columnDefinition = "default_cs boolean default '0'")
  public boolean isDefault() {
    return defaultCs;
  }

  public void setDefault(boolean aFlag) {
    this.defaultCs = aFlag;
  }

  @Column(name = "enabled", nullable = false, columnDefinition = "enabled bool default '0'")
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Column(name = "last_used_serial", length = 255, nullable = true)
  public String getLastUsedSerial() {
    return lastUsedSerial;
  }

  public void setLastUsedSerial(String lastUsedSerial) {
    this.lastUsedSerial = lastUsedSerial;
  }

  public Set<Protocol> supportedProtocols() {
    Set<Protocol> ps = new HashSet<>();

    String[] pss = this.protocols.split(",");
    for (String ps1 : pss) {
      ps.add(Protocol.get(ps1));
    }
    return ps;
  }

  @Override
  public String toString() {
    return description;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + Objects.hashCode(this.id);
    hash = 23 * hash + Objects.hashCode(this.description);
    hash = 23 * hash + Objects.hashCode(this.shortName);
    hash = 23 * hash + Objects.hashCode(this.className);
    hash = 23 * hash + Objects.hashCode(this.connectVia);
    hash = 23 * hash + Objects.hashCode(this.serialPort);
    hash = 23 * hash + Objects.hashCode(this.ipAddress);
    hash = 23 * hash + Objects.hashCode(this.networkPort);
    hash = 23 * hash + (this.ipAutoConfiguration ? 1 : 0);
    hash = 23 * hash + (this.commandAndControlSupport ? 1 : 0);
    hash = 23 * hash + (this.feedbackSupport ? 1 : 0);
    hash = 23 * hash + (this.locomotiveSynchronizationSupport ? 1 : 0);
    hash = 23 * hash + (this.accessorySynchronizationSupport ? 1 : 0);
    hash = 23 * hash + (this.locomotiveImageSynchronizationSupport ? 1 : 0);
    hash = 23 * hash + (this.locomotiveFunctionSynchronizationSupport ? 1 : 0);
    hash = 23 * hash + Objects.hashCode(this.protocols);
    hash = 23 * hash + (this.defaultCs ? 1 : 0);
    hash = 23 * hash + (this.enabled ? 1 : 0);
    hash = 23 * hash + Objects.hashCode(this.lastUsedSerial);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CommandStationBean other = (CommandStationBean) obj;
    if (this.ipAutoConfiguration != other.ipAutoConfiguration) {
      return false;
    }
    if (this.commandAndControlSupport != other.commandAndControlSupport) {
      return false;
    }
    if (this.feedbackSupport != other.feedbackSupport) {
      return false;
    }
    if (this.locomotiveSynchronizationSupport != other.locomotiveSynchronizationSupport) {
      return false;
    }
    if (this.accessorySynchronizationSupport != other.accessorySynchronizationSupport) {
      return false;
    }
    if (this.locomotiveImageSynchronizationSupport != other.locomotiveImageSynchronizationSupport) {
      return false;
    }
    if (this.locomotiveFunctionSynchronizationSupport != other.locomotiveFunctionSynchronizationSupport) {
      return false;
    }
    if (this.defaultCs != other.defaultCs) {
      return false;
    }
    if (this.enabled != other.enabled) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.description, other.description)) {
      return false;
    }
    if (!Objects.equals(this.shortName, other.shortName)) {
      return false;
    }
    if (!Objects.equals(this.className, other.className)) {
      return false;
    }
    if (!Objects.equals(this.connectVia, other.connectVia)) {
      return false;
    }
    if (!Objects.equals(this.serialPort, other.serialPort)) {
      return false;
    }
    if (!Objects.equals(this.ipAddress, other.ipAddress)) {
      return false;
    }
    if (!Objects.equals(this.protocols, other.protocols)) {
      return false;
    }
    if (!Objects.equals(this.lastUsedSerial, other.lastUsedSerial)) {
      return false;
    }
    return Objects.equals(this.networkPort, other.networkPort);
  }

  public enum ConnectionType {
    NETWORK("NETWORK"),
    SERIAL("SERIAL");

    private final String connectionType;
    private static final Map<String, ConnectionType> ENUM_MAP;

    ConnectionType(String connectionType) {
      this.connectionType = connectionType;
    }

    static {
      Map<String, ConnectionType> map = new ConcurrentHashMap<>();
      for (ConnectionType instance : ConnectionType.values()) {
        map.put(instance.getConnectionType(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public String getConnectionType() {
      return this.connectionType;
    }

    public static ConnectionType get(String connectionType) {
      return ENUM_MAP.get(connectionType);
    }

    public static Object[] toArray() {
      return ENUM_MAP.values().toArray();
    }
  }

  public enum Protocol {
    MM("mm"), MFX("mfx"), DCC("dcc"), SX("sx");

    private final String protocol;

    private static final Map<String, Protocol> ENUM_MAP;

    Protocol(String protocol) {
      this.protocol = protocol;
    }

    public String getProtocol() {
      return this.protocol;
    }

    static {
      Map<String, Protocol> map = new ConcurrentHashMap<>();
      for (Protocol instance : Protocol.values()) {
        map.put(instance.getProtocol(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static Protocol get(String protocol) {
      if (protocol == null) {
        return null;
      }
      return ENUM_MAP.get(protocol);
    }
  }
}
