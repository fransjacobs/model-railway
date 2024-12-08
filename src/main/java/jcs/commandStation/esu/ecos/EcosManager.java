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
package jcs.commandStation.esu.ecos;

import java.util.Map;
import java.util.Objects;
import jcs.commandStation.esu.ecos.Ecos;
import jcs.commandStation.esu.ecos.EcosMessage;

/**
 * ECoS (id=1)
 */
public class EcosManager {

  public static final int ID = 1;

  private String objectClass;
  private String view;
  private String listView;
  private String control;
  private String list;
  private int size;
  private int minArguments;
  private String protocolVersion;
  private String commandStationType;
  private String name;
  private String serialNumber;
  private String hardwareVersion;
  private String applicationVersion;
  private String applicationVersionSuffix;
  private boolean updateOnError;
  private String status;
  private String status2;
  private String progStatus;
  private String m4Status;
  private String railcomplusStatus;
  private String watchdog;
  private boolean railcom;
  private boolean railcomplus;
  private int railcomplusRange;
  private String railcomplusMode;
  private boolean allowLocoTakeover;
  private boolean stopOnLastDisconnect;

  public EcosManager() {

  }

  public EcosManager(EcosMessage msg) {
    parse(msg);
  }

  public void update(EcosMessage msg) {
    parse(msg);
  }

  private void parse(EcosMessage msg) {
    if (msg == null) {
      return;
    }
    Map<String, Object> values = msg.getValueMap();

    if (values.containsKey(Ecos.OBJECTCLASS)) {
      this.objectClass = values.get(Ecos.OBJECTCLASS).toString();
    }
    if (values.containsKey(Ecos.VIEW)) {
      this.view = values.get(Ecos.VIEW).toString();
    }
    if (values.containsKey(Ecos.LISTVIEW)) {
      this.listView = values.get(Ecos.LISTVIEW).toString();
    }
    if (values.containsKey(Ecos.CONTROL)) {
      this.control = values.get(Ecos.CONTROL).toString();
    }
    if (values.containsKey(Ecos.LIST)) {
      this.list = values.get(Ecos.LIST).toString();
    }
    if (values.containsKey(Ecos.SIZE)) {
      String vsize = values.get(Ecos.SIZE).toString();
      if (vsize != null) {
        this.size = Integer.parseInt(vsize);
      }
    }
    if (values.containsKey(Ecos.MINARGUMENTS)) {
      String vminarguments = values.get(Ecos.MINARGUMENTS).toString();
      if (vminarguments != null) {
        this.minArguments = Integer.parseInt(vminarguments);
      }
    }
    if (values.containsKey(Ecos.PROTOCOLVERSION)) {
      this.protocolVersion = values.get(Ecos.PROTOCOLVERSION).toString();
    }
    if (values.containsKey(Ecos.COMMANDSTATIONTYPE)) {
      this.commandStationType = values.get(Ecos.COMMANDSTATIONTYPE).toString();
      if (this.commandStationType != null) {
        this.commandStationType = this.commandStationType.replaceAll("\"", "");
      }
    }
    if (values.containsKey(Ecos.NAME)) {
      this.name = values.get(Ecos.NAME).toString();
      if (this.name != null) {
        this.name = this.name.replaceAll("\"", "");
      }
    }

    if (values.containsKey(Ecos.SERIALNUMBER)) {
      this.serialNumber = values.get(Ecos.SERIALNUMBER).toString();
      if (this.serialNumber != null) {
        this.serialNumber = this.serialNumber.replaceAll("\"", "");
      }
    }
    if (values.containsKey(Ecos.HARDWAREVERSION)) {
      this.hardwareVersion = values.get(Ecos.HARDWAREVERSION).toString();
    }
    if (values.containsKey(Ecos.APPLICATIONVERSION)) {
      this.applicationVersion = values.get(Ecos.APPLICATIONVERSION).toString();
    }
    if (values.containsKey(Ecos.APPLICATIONVERSIONSUFFIX)) {
      this.applicationVersionSuffix = values.get(Ecos.APPLICATIONVERSIONSUFFIX).toString();
      if (this.applicationVersionSuffix != null) {
        this.applicationVersionSuffix = this.applicationVersionSuffix.replaceAll("\"", "");
      }
    }
    if (values.containsKey(Ecos.UPDATEONERROR)) {
      this.updateOnError = values.get(Ecos.UPDATEONERROR).equals("1");
    }
    if (values.containsKey(Ecos.STATUS)) {
      this.status = values.get(Ecos.STATUS).toString();
    }
    if (values.containsKey(Ecos.STATUS2)) {
      this.status2 = values.get(Ecos.STATUS2).toString();
    }
    if (values.containsKey(Ecos.PROG_STATUS)) {
      this.progStatus = values.get(Ecos.PROG_STATUS).toString();
    }
    if (values.containsKey(Ecos.M4_STATUS)) {
      this.m4Status = values.get(Ecos.M4_STATUS).toString();
    }
    if (values.containsKey(Ecos.RAILCOMPLUS_STATUS)) {
      this.railcomplusStatus = values.get(Ecos.RAILCOMPLUS_STATUS).toString();
    }
    if (values.containsKey(Ecos.WATCHDOG)) {
      this.watchdog = values.get(Ecos.WATCHDOG).toString();
    }
    if (values.containsKey(Ecos.RAILCOM)) {
      this.railcom = values.get(Ecos.RAILCOM).equals("1");
    }
    if (values.containsKey(Ecos.RAILCOMPLUS)) {
      this.railcomplus = values.get(Ecos.RAILCOMPLUS).equals("1");
    }
    if (values.containsKey(Ecos.RAILCOMPLUS_RANGE)) {
      String vrailcomplusRange = values.get(Ecos.RAILCOMPLUS_RANGE).toString();
      if (vrailcomplusRange != null) {
        this.railcomplusRange = Integer.parseInt(vrailcomplusRange);
      }
    }
    if (values.containsKey(Ecos.RAILCOMPLUS_MODE)) {
      this.railcomplusMode = values.get(Ecos.RAILCOMPLUS_MODE).toString();
    }
    if (values.containsKey(Ecos.ALLOWLOCOTAKEOVER)) {
      this.allowLocoTakeover = values.get(Ecos.ALLOWLOCOTAKEOVER).equals("1");
    }
    if (values.containsKey(Ecos.STOPONLASTCONNECT)) {
      this.stopOnLastDisconnect = values.get(Ecos.STOPONLASTCONNECT).equals("1");
    }
  }

  public String getObjectClass() {
    return objectClass;
  }

  public void setObjectClass(String objectClass) {
    this.objectClass = objectClass;
  }

  public String getView() {
    return view;
  }

  public void setView(String view) {
    this.view = view;
  }

  public String getListView() {
    return listView;
  }

  public void setListView(String listView) {
    this.listView = listView;
  }

  public String getControl() {
    return control;
  }

  public void setControl(String control) {
    this.control = control;
  }

  public String getList() {
    return list;
  }

  public void setList(String list) {
    this.list = list;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getMinArguments() {
    return minArguments;
  }

  public void setMinArguments(int minArguments) {
    this.minArguments = minArguments;
  }

  public String getProtocolVersion() {
    return protocolVersion;
  }

  public void setProtocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  public String getCommandStationType() {
    return commandStationType;
  }

  public void setCommandStationType(String commandStationType) {
    this.commandStationType = commandStationType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getHardwareVersion() {
    return hardwareVersion;
  }

  public void setHardwareVersion(String hardwareVersion) {
    this.hardwareVersion = hardwareVersion;
  }

  public String getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(String applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public String getApplicationVersionSuffix() {
    return applicationVersionSuffix;
  }

  public void setApplicationVersionSuffix(String applicationVersionSuffix) {
    this.applicationVersionSuffix = applicationVersionSuffix;
  }

  public boolean isUpdateOnError() {
    return updateOnError;
  }

  public void setUpdateOnError(boolean updateOnError) {
    this.updateOnError = updateOnError;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus2() {
    return status2;
  }

  public void setStatus2(String status2) {
    this.status2 = status2;
  }

  public String getProgStatus() {
    return progStatus;
  }

  public void setProgStatus(String progStatus) {
    this.progStatus = progStatus;
  }

  public String getM4Status() {
    return m4Status;
  }

  public void setM4Status(String m4Status) {
    this.m4Status = m4Status;
  }

  public String getRailcomplusStatus() {
    return railcomplusStatus;
  }

  public void setRailcomplusStatus(String railcomplusStatus) {
    this.railcomplusStatus = railcomplusStatus;
  }

  public String getWatchdog() {
    return watchdog;
  }

  public void setWatchdog(String watchdog) {
    this.watchdog = watchdog;
  }

  public boolean isRailcom() {
    return railcom;
  }

  public void setRailcom(boolean railcom) {
    this.railcom = railcom;
  }

  public boolean isRailcomplus() {
    return railcomplus;
  }

  public void setRailcomplus(boolean railcomplus) {
    this.railcomplus = railcomplus;
  }

  public int getRailcomplusRange() {
    return railcomplusRange;
  }

  public void setRailcomplusRange(int railcomplusRange) {
    this.railcomplusRange = railcomplusRange;
  }

  public String getRailcomplusMode() {
    return railcomplusMode;
  }

  public void setRailcomplusMode(String railcomplusMode) {
    this.railcomplusMode = railcomplusMode;
  }

  public boolean isAllowLocoTakeover() {
    return allowLocoTakeover;
  }

  public void setAllowLocoTakeover(boolean allowLocoTakeover) {
    this.allowLocoTakeover = allowLocoTakeover;
  }

  public boolean isStopOnLastDisconnect() {
    return stopOnLastDisconnect;
  }

  public void setStopOnLastDisconnect(boolean stopOnLastDisconnect) {
    this.stopOnLastDisconnect = stopOnLastDisconnect;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 79 * hash + Objects.hashCode(this.objectClass);
    hash = 79 * hash + Objects.hashCode(this.view);
    hash = 79 * hash + Objects.hashCode(this.listView);
    hash = 79 * hash + Objects.hashCode(this.control);
    hash = 79 * hash + Objects.hashCode(this.list);
    hash = 79 * hash + this.size;
    hash = 79 * hash + this.minArguments;
    hash = 79 * hash + Objects.hashCode(this.protocolVersion);
    hash = 79 * hash + Objects.hashCode(this.commandStationType);
    hash = 79 * hash + Objects.hashCode(this.name);
    hash = 79 * hash + Objects.hashCode(this.serialNumber);
    hash = 79 * hash + Objects.hashCode(this.hardwareVersion);
    hash = 79 * hash + Objects.hashCode(this.applicationVersion);
    hash = 79 * hash + Objects.hashCode(this.applicationVersionSuffix);
    hash = 79 * hash + (this.updateOnError ? 1 : 0);
    hash = 79 * hash + Objects.hashCode(this.status);
    hash = 79 * hash + Objects.hashCode(this.status2);
    hash = 79 * hash + Objects.hashCode(this.progStatus);
    hash = 79 * hash + Objects.hashCode(this.m4Status);
    hash = 79 * hash + Objects.hashCode(this.railcomplusStatus);
    hash = 79 * hash + Objects.hashCode(this.watchdog);
    hash = 79 * hash + (this.railcom ? 1 : 0);
    hash = 79 * hash + (this.railcomplus ? 1 : 0);
    hash = 79 * hash + this.railcomplusRange;
    hash = 79 * hash + Objects.hashCode(this.railcomplusMode);
    hash = 79 * hash + (this.allowLocoTakeover ? 1 : 0);
    hash = 79 * hash + (this.stopOnLastDisconnect ? 1 : 0);
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
    final EcosManager other = (EcosManager) obj;
    if (this.size != other.size) {
      return false;
    }
    if (this.minArguments != other.minArguments) {
      return false;
    }
    if (this.updateOnError != other.updateOnError) {
      return false;
    }
    if (this.railcom != other.railcom) {
      return false;
    }
    if (this.railcomplus != other.railcomplus) {
      return false;
    }
    if (this.railcomplusRange != other.railcomplusRange) {
      return false;
    }
    if (this.allowLocoTakeover != other.allowLocoTakeover) {
      return false;
    }
    if (this.stopOnLastDisconnect != other.stopOnLastDisconnect) {
      return false;
    }
    if (!Objects.equals(this.objectClass, other.objectClass)) {
      return false;
    }
    if (!Objects.equals(this.view, other.view)) {
      return false;
    }
    if (!Objects.equals(this.listView, other.listView)) {
      return false;
    }
    if (!Objects.equals(this.control, other.control)) {
      return false;
    }
    if (!Objects.equals(this.list, other.list)) {
      return false;
    }
    if (!Objects.equals(this.protocolVersion, other.protocolVersion)) {
      return false;
    }
    if (!Objects.equals(this.commandStationType, other.commandStationType)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.serialNumber, other.serialNumber)) {
      return false;
    }
    if (!Objects.equals(this.hardwareVersion, other.hardwareVersion)) {
      return false;
    }
    if (!Objects.equals(this.applicationVersion, other.applicationVersion)) {
      return false;
    }
    if (!Objects.equals(this.applicationVersionSuffix, other.applicationVersionSuffix)) {
      return false;
    }
    if (!Objects.equals(this.status, other.status)) {
      return false;
    }
    if (!Objects.equals(this.status2, other.status2)) {
      return false;
    }
    if (!Objects.equals(this.progStatus, other.progStatus)) {
      return false;
    }
    if (!Objects.equals(this.m4Status, other.m4Status)) {
      return false;
    }
    if (!Objects.equals(this.railcomplusStatus, other.railcomplusStatus)) {
      return false;
    }
    if (!Objects.equals(this.watchdog, other.watchdog)) {
      return false;
    }
    return Objects.equals(this.railcomplusMode, other.railcomplusMode);
  }

  @Override
  public String toString() {
    return "BaseObject{" + "commandStationType=" + commandStationType + ", name=" + name + ", serialNumber=" + serialNumber + ", hardwareVersion=" + hardwareVersion + ", applicationVersion=" + applicationVersion + ", status=" + status + ", status2=" + status2 + "}";
  }

}
