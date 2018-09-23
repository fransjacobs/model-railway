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
 */package lan.wervel.jcs.server.rmi;

import java.io.Serializable;
import java.util.Objects;

/**
 * Object that provides some info about the server
 *
 * @author frans
 */
public class ServerInfo implements Serializable {

  private static final long serialVersionUID = -9073288684071007696L;

  private final String osName;
  private final String osArch;
  private final String osVersion;
  private final String dataModel;
  private final String javaVersion;
  private final String endian;
  private final String userName;
  private final String userHome;
  private final String userDir;
  private final String hostName;
  private final String serverVersion;
  private final String provider;

  public ServerInfo(String osName, String osArch, String osVersion, String dataModel, String javaVersion, String endian, String userName, String userHome, String userDir, String hostName, String serverVersion, String provider) {
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
    this.provider = provider;
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
    return this.provider;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 23 * hash + Objects.hashCode(this.osName);
    hash = 23 * hash + Objects.hashCode(this.osArch);
    hash = 23 * hash + Objects.hashCode(this.osVersion);
    hash = 23 * hash + Objects.hashCode(this.dataModel);
    hash = 23 * hash + Objects.hashCode(this.javaVersion);
    hash = 23 * hash + Objects.hashCode(this.endian);
    hash = 23 * hash + Objects.hashCode(this.userName);
    hash = 23 * hash + Objects.hashCode(this.userHome);
    hash = 23 * hash + Objects.hashCode(this.userDir);
    hash = 23 * hash + Objects.hashCode(this.hostName);
    hash = 23 * hash + Objects.hashCode(this.serverVersion);
    hash = 23 * hash + Objects.hashCode(this.provider);
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
    final ServerInfo other = (ServerInfo) obj;
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
    if (!Objects.equals(this.provider, other.provider)) {
      return false;
    }
    return true;
  }

}
