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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author frans
 */
@Table(name = "command_stations")
public class CommandStationBean {

  protected String id;
  protected String description;
  protected String shortName;
  protected String className;
  protected String connectVia;
  protected String serialPort;
  protected String ipAddress;
  protected Integer networkPort;
  protected boolean ipAutoConfiguration;
  protected boolean decoderControlSupport;
  protected boolean accessoryControlSupport;
  protected boolean feedbackSupport;
  protected boolean locomotiveSynchronizationSupport;
  protected boolean accessorySynchronizationSupport;
  protected boolean locomotiveImageSynchronizationSupport;
  protected boolean locomotiveFunctionSynchronizationSupport;
  protected String protocols;
  protected boolean defaultCs;
  protected boolean enabled;
  protected String lastUsedSerial;
  protected String supConnTypesStr;
  protected boolean virtual;

  protected String feedbackModuleIdentifier;
  protected Integer feedbackChannelCount;
  protected Integer feedbackBus0ModuleCount;
  protected Integer feedbackBus1ModuleCount;
  protected Integer feedbackBus2ModuleCount;
  protected Integer feedbackBus3ModuleCount;

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
    this.virtual = "VIR".equals(shortName);
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
    return ConnectionType.get(connectVia);
  }

  @Transient
  public void setConnectionType(ConnectionType connectionType) {
    this.connectVia = connectionType.getConnectionType();
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

  @Column(name = "supports_decoder_control", nullable = false, columnDefinition = "supports_decoder_control bool default '1'")
  public boolean isDecoderControlSupport() {
    return decoderControlSupport;
  }

  public void setDecoderControlSupport(boolean decoderControlSupport) {
    this.decoderControlSupport = decoderControlSupport;
  }

  @Column(name = "supports_accessory_control", nullable = false, columnDefinition = "supports_accessory_control bool default '1'")
  public boolean isAccessoryControlSupport() {
    return accessoryControlSupport;
  }

  public void setAccessoryControlSupport(boolean accessoryControlSupport) {
    this.accessoryControlSupport = accessoryControlSupport;
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

  @Column(name = "sup_conn_types", length = 255, nullable = false)
  public String getSupConnTypesStr() {
    return supConnTypesStr;
  }

  public void setSupConnTypesStr(String supConnTypesStr) {
    this.supConnTypesStr = supConnTypesStr;
  }

  @Column(name = "feedback_module_id", length = 255, nullable = true)
  public String getFeedbackModuleIdentifier() {
    return feedbackModuleIdentifier;
  }

  public void setFeedbackModuleIdentifier(String feedbackModuleIdentifier) {
    this.feedbackModuleIdentifier = feedbackModuleIdentifier;
  }

  @Column(name = "feedback_bus_count", nullable = true)
  public Integer getFeedbackChannelCount() {
    return feedbackChannelCount;
  }

  public void setFeedbackChannelCount(Integer feedbackChannelCount) {
    this.feedbackChannelCount = feedbackChannelCount;
  }

  @Column(name = "feedback_bus_0_module_count", nullable = true)
  public Integer getFeedbackBus0ModuleCount() {
    return feedbackBus0ModuleCount;
  }

  public void setFeedbackBus0ModuleCount(Integer feedbackBus0ModuleCount) {
    this.feedbackBus0ModuleCount = feedbackBus0ModuleCount;
  }

  @Column(name = "feedback_bus_1_module_count", nullable = true)
  public Integer getFeedbackBus1ModuleCount() {
    return feedbackBus1ModuleCount;
  }

  public void setFeedbackBus1ModuleCount(Integer feedbackBus1ModuleCount) {
    this.feedbackBus1ModuleCount = feedbackBus1ModuleCount;
  }

  @Column(name = "feedback_bus_2_module_count", nullable = true)
  public Integer getFeedbackBus2ModuleCount() {
    return feedbackBus2ModuleCount;
  }

  public void setFeedbackBus2ModuleCount(Integer feedbackBus2ModuleCount) {
    this.feedbackBus2ModuleCount = feedbackBus2ModuleCount;
  }

  @Column(name = "feedback_bus_3_module_count", nullable = true)
  public Integer getFeedbackBus3ModuleCount() {
    return feedbackBus3ModuleCount;
  }

  public void setFeedbackBus3ModuleCount(Integer feedbackBus3ModuleCount) {
    this.feedbackBus3ModuleCount = feedbackBus3ModuleCount;
  }

  @Transient
  public boolean isVirtual() {
    return virtual;
  }

  public void setVirtual(boolean virtual) {
    this.virtual = virtual;
  }

  @Transient
  public Set<ConnectionType> getSupportedConnectionTypes() {
    Set<ConnectionType> cts = new HashSet<>();
    if (supConnTypesStr != null) {
      String[] ct = this.supConnTypesStr.split(",");
      for (String ct1 : ct) {
        cts.add(ConnectionType.get(ct1));
      }
    }
    return cts;
  }

  public void addSupportedConnectionType(ConnectionType connectionType) {
    Set<ConnectionType> cts = new HashSet<>();
    if (supConnTypesStr != null) {
      String[] ct = this.supConnTypesStr.split(",");
      for (String ct1 : ct) {
        cts.add(ConnectionType.get(ct1));
      }
    }
    cts.add(connectionType);

    StringBuilder sb = new StringBuilder();
    Iterator<ConnectionType> cti = cts.iterator();
    while (cti.hasNext()) {
      sb.append(cti.next().getConnectionType());
      if (cti.hasNext()) {
        sb.append(",");
      }
    }
    supConnTypesStr = sb.toString();
  }

  public void removeSupportedConnectionType(ConnectionType connectionType) {
    Set<ConnectionType> cs = new HashSet<>();
    if (supConnTypesStr != null) {
      String[] cts = this.supConnTypesStr.split(",");
      for (String ct : cts) {
        cs.add(ConnectionType.get(ct.toUpperCase()));
      }
    }
    cs.remove(connectionType);

    StringBuilder sb = new StringBuilder();
    Iterator<ConnectionType> ci = cs.iterator();
    while (ci.hasNext()) {
      sb.append(ci.next().getConnectionType());
      if (ci.hasNext()) {
        sb.append(",");
      }
    }
    supConnTypesStr = sb.toString();
  }

  @Transient
  public Set<Protocol> getSupportedProtocols() {
    Set<Protocol> ps = new HashSet<>();
    if (protocols != null) {
      String[] pss = this.protocols.split(",");
      for (String ps1 : pss) {
        ps.add(Protocol.get(ps1.toLowerCase()));
      }
    }
    return ps;
  }

  public void addProtocol(Protocol protocol) {
    List<Protocol> pl = new ArrayList<>();
    if (protocols != null && protocols.length() > 1) {
      String[] pa = this.protocols.split(",");
      if (pa.length > 0) {
        for (String p : pa) {
          pl.add(Protocol.get(p.toLowerCase()));
        }
      }
    }
    pl.add(protocol);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < pl.size(); i++) {
      Protocol p = pl.get(i);
      sb.append(p.getProtocol());
      if (i + 1 < pl.size()) {
        sb.append(",");
      }
    }

    protocols = sb.toString();
  }

  public void removeProtocol(Protocol protocol) {
    Set<Protocol> ps = new HashSet<>();
    if (protocols != null) {
      String[] pts = this.protocols.split(",");
      for (String pt : pts) {
        ps.add(Protocol.get(pt.toLowerCase()));
      }
    }
    ps.remove(protocol);

    StringBuilder sb = new StringBuilder();
    Iterator<Protocol> pi = ps.iterator();
    while (pi.hasNext()) {
      sb.append(pi.next().getProtocol());
      if (pi.hasNext()) {
        sb.append(",");
      }
    }
    protocols = sb.toString();
  }

  @Override
  public String toString() {
    return description;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + Objects.hashCode(this.id);
    hash = 67 * hash + Objects.hashCode(this.description);
    hash = 67 * hash + Objects.hashCode(this.shortName);
    hash = 67 * hash + Objects.hashCode(this.className);
    hash = 67 * hash + Objects.hashCode(this.connectVia);
    hash = 67 * hash + Objects.hashCode(this.serialPort);
    hash = 67 * hash + Objects.hashCode(this.ipAddress);
    hash = 67 * hash + Objects.hashCode(this.networkPort);
    hash = 67 * hash + (this.ipAutoConfiguration ? 1 : 0);
    hash = 67 * hash + (this.decoderControlSupport ? 1 : 0);
    hash = 67 * hash + (this.accessoryControlSupport ? 1 : 0);
    hash = 67 * hash + (this.feedbackSupport ? 1 : 0);
    hash = 67 * hash + (this.locomotiveSynchronizationSupport ? 1 : 0);
    hash = 67 * hash + (this.accessorySynchronizationSupport ? 1 : 0);
    hash = 67 * hash + (this.locomotiveImageSynchronizationSupport ? 1 : 0);
    hash = 67 * hash + (this.locomotiveFunctionSynchronizationSupport ? 1 : 0);
    hash = 67 * hash + Objects.hashCode(this.protocols);
    hash = 67 * hash + (this.defaultCs ? 1 : 0);
    hash = 67 * hash + (this.enabled ? 1 : 0);
    hash = 67 * hash + Objects.hashCode(this.lastUsedSerial);
    hash = 67 * hash + Objects.hashCode(this.supConnTypesStr);
    hash = 67 * hash + Objects.hashCode(this.feedbackModuleIdentifier);
    hash = 67 * hash + Objects.hashCode(this.feedbackChannelCount);
    hash = 67 * hash + Objects.hashCode(this.feedbackBus0ModuleCount);
    hash = 67 * hash + Objects.hashCode(this.feedbackBus1ModuleCount);
    hash = 67 * hash + Objects.hashCode(this.feedbackBus2ModuleCount);
    hash = 67 * hash + Objects.hashCode(this.feedbackBus3ModuleCount);
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
    if (this.decoderControlSupport != other.decoderControlSupport) {
      return false;
    }
    if (this.accessoryControlSupport != other.accessoryControlSupport) {
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
    if (!Objects.equals(this.supConnTypesStr, other.supConnTypesStr)) {
      return false;
    }
    if (!Objects.equals(this.feedbackModuleIdentifier, other.feedbackModuleIdentifier)) {
      return false;
    }
    if (!Objects.equals(this.networkPort, other.networkPort)) {
      return false;
    }
    if (!Objects.equals(this.feedbackChannelCount, other.feedbackChannelCount)) {
      return false;
    }
    if (!Objects.equals(this.feedbackBus0ModuleCount, other.feedbackBus0ModuleCount)) {
      return false;
    }
    if (!Objects.equals(this.feedbackBus1ModuleCount, other.feedbackBus1ModuleCount)) {
      return false;
    }
    if (!Objects.equals(this.feedbackBus2ModuleCount, other.feedbackBus2ModuleCount)) {
      return false;
    }
    return Objects.equals(this.feedbackBus3ModuleCount, other.feedbackBus3ModuleCount);
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
