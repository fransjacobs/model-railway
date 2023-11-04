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
  private String name;
  private String className;
  private boolean defaultCs;
  private String connectionSpecifier;
  private String serialPort;
  private String ipAddress;
  private Integer networkPort;

  private boolean autoIpConfiguration;
  private boolean show;
  private String protocols;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Column(name = "name", nullable = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Column(name = "class_name", length = 255, nullable = false)
  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  @Column(name = "default_cs", nullable = false, columnDefinition = "default_cs boolean default '0'")
  public boolean isDefault() {
    return defaultCs;
  }

  public void setDefault(boolean aFlag) {
    this.defaultCs = aFlag;
  }

  @Column(name = "connection_type", length = 255, nullable = false)
  public String getConnectionSpecifier() {
    return connectionSpecifier;
  }

  public void setConnectionSpecifier(String connectionSpecifier) {
    this.connectionSpecifier = connectionSpecifier;
  }

  @Transient
  public ConnectionType getConnectionType() {
    if (connectionSpecifier != null) {
      return ConnectionType.get(connectionSpecifier);
    } else {
      return null;
    }
  }

  public void setConnectionType(ConnectionType connectionType) {
    if (connectionType == null) {
      connectionSpecifier = null;
    } else {
      connectionSpecifier = connectionType.getConnectionType();
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

  @Column(name = "auto_conf", nullable = false, columnDefinition = "auto_conf bool default '0'")
  public boolean isAutoIpConfiguration() {
    return autoIpConfiguration;
  }

  public void setAutoIpConfiguration(boolean autoIpConfiguration) {
    this.autoIpConfiguration = autoIpConfiguration;
  }

  @Column(name = "show", nullable = false, columnDefinition = "show bool default '1'")
  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  @Column(name = "protocols", length = 255, nullable = false)
  public String getProtocols() {
    return protocols;
  }

  public void setProtocols(String protocols) {
    this.protocols = protocols;
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
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + Objects.hashCode(this.id);
    hash = 53 * hash + Objects.hashCode(this.name);
    hash = 53 * hash + Objects.hashCode(this.className);
    hash = 53 * hash + (this.defaultCs ? 1 : 0);
    hash = 53 * hash + Objects.hashCode(this.connectionSpecifier);
    hash = 53 * hash + (this.show ? 1 : 0);
    hash = 53 * hash + Objects.hashCode(this.protocols);
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
    if (this.defaultCs != other.defaultCs) {
      return false;
    }
    if (this.show != other.show) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.className, other.className)) {
      return false;
    }
    if (!Objects.equals(this.protocols, other.protocols)) {
      return false;
    }
    return Objects.equals(this.connectionSpecifier, other.connectionSpecifier);
  }

  @Override
  public String toString() {
    return name;
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
