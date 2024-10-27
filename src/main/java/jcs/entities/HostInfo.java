/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Object that provides some info about the server
 *
 * @author frans
 */
@Deprecated
public abstract class HostInfo implements Serializable {

  protected final String osName;
  protected final String osArch;
  protected final String osVersion;
  protected final String dataModel;
  protected final String javaVersion;
  protected final String endian;
  protected final String userName;
  protected final String userHome;
  protected final String userDir;
  protected final String hostName;
  protected final String serverVersion;
  protected final String providerName;
  protected final String jvmName;
  protected final int pid;
  protected final long initializeTime;

  protected long totalMemory;
  protected long freeMemory;
  protected long maxMemory;

  protected static final int MB = 1024 * 1024;

  protected HostInfo(String osName, String osArch, String osVersion, String dataModel, String javaVersion, String endian, String userName, String userHome, String userDir, String hostName, String serverVersion, String providerName, String jvmName, int pid) {
    this.osName = osName;
    this.osArch = osArch;
    this.osVersion = osVersion;
    this.dataModel = dataModel;
    this.javaVersion = javaVersion;
    this.endian = endian;
    this.userName = userName;
    this.userHome = userHome;
    this.userDir = userDir;
    this.hostName = hostName;
    this.serverVersion = serverVersion;
    this.providerName = providerName;
    this.jvmName = jvmName;
    this.pid = pid;
    this.initializeTime = System.currentTimeMillis();
  }

  public String getOsName() {
    return osName;
  }

  public String getOsArch() {
    return osArch;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public String getDataModel() {
    return dataModel;
  }

  public String getJavaVersion() {
    return javaVersion;
  }

  public String getEndian() {
    return endian;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserHome() {
    return userHome;
  }

  public String getUserDir() {
    return userDir;
  }

  public String getHostName() {
    return hostName;
  }

  public String getServerVersion() {
    return serverVersion;
  }

  public String getProviderName() {
    return this.providerName;
  }

  public String getJvmName() {
    return jvmName;
  }

  public int getPid() {
    return pid;
  }

  public long getInitializeTime() {
    return initializeTime;
  }

  public long getTotalMemory() {
    return totalMemory / MB;
  }

  public void setTotalMemory(long totalMemory) {
    this.totalMemory = totalMemory;
  }

  public long getFreeMemory() {
    return freeMemory / MB;
  }

  public void setFreeMemory(long freeMemory) {
    this.freeMemory = freeMemory;
  }

  public long getMaxMemory() {
    return maxMemory / MB;
  }

  public void setMaxMemory(long maxMemory) {
    this.maxMemory = maxMemory;
  }

  public long getUsedMemory() {
    return (this.totalMemory - this.freeMemory) / MB;
  }

  @Override
  public int hashCode() {
    long hash = 3;
    hash = 67 * hash + Objects.hashCode(this.osName);
    hash = 67 * hash + Objects.hashCode(this.osArch);
    hash = 67 * hash + Objects.hashCode(this.osVersion);
    hash = 67 * hash + Objects.hashCode(this.dataModel);
    hash = 67 * hash + Objects.hashCode(this.javaVersion);
    hash = 67 * hash + Objects.hashCode(this.endian);
    hash = 67 * hash + Objects.hashCode(this.userName);
    hash = 67 * hash + Objects.hashCode(this.userHome);
    hash = 67 * hash + Objects.hashCode(this.userDir);
    hash = 67 * hash + Objects.hashCode(this.hostName);
    hash = 67 * hash + Objects.hashCode(this.serverVersion);
    hash = 67 * hash + Objects.hashCode(this.providerName);
    hash = 67 * hash + Objects.hashCode(this.jvmName);
    hash = 67 * hash + this.pid;
    hash = 67 * hash + (int) (this.initializeTime ^ (this.initializeTime >>> 32));
    hash = 67 * hash + this.totalMemory;
    hash = 67 * hash + this.freeMemory;
    hash = 67 * hash + this.maxMemory;
    return (int)hash;
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
    final HostInfo other = (HostInfo) obj;
    if (this.pid != other.pid) {
      return false;
    }
    if (this.initializeTime != other.initializeTime) {
      return false;
    }
    if (this.totalMemory != other.totalMemory) {
      return false;
    }
    if (this.freeMemory != other.freeMemory) {
      return false;
    }
    if (this.maxMemory != other.maxMemory) {
      return false;
    }
    if (!Objects.equals(this.osName, other.osName)) {
      return false;
    }
    if (!Objects.equals(this.osArch, other.osArch)) {
      return false;
    }
    if (!Objects.equals(this.osVersion, other.osVersion)) {
      return false;
    }
    if (!Objects.equals(this.dataModel, other.dataModel)) {
      return false;
    }
    if (!Objects.equals(this.javaVersion, other.javaVersion)) {
      return false;
    }
    if (!Objects.equals(this.endian, other.endian)) {
      return false;
    }
    if (!Objects.equals(this.userName, other.userName)) {
      return false;
    }
    if (!Objects.equals(this.userHome, other.userHome)) {
      return false;
    }
    if (!Objects.equals(this.userDir, other.userDir)) {
      return false;
    }
    if (!Objects.equals(this.hostName, other.hostName)) {
      return false;
    }
    if (!Objects.equals(this.serverVersion, other.serverVersion)) {
      return false;
    }
    if (!Objects.equals(this.providerName, other.providerName)) {
      return false;
    }
    return Objects.equals(this.jvmName, other.jvmName);
  }
}
