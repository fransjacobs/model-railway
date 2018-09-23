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
package lan.wervel.jcs.server.rmi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lan.wervel.jcs.common.ServerVersion;
import lan.wervel.jcs.controller.ControllerFactory;

/**
 *
 * @author frans
 */
public class ServerInfoProvider implements ServerVersion {

  public static ServerInfo getServerInfo() {
    String osName = System.getProperty("os.name");
    String osArch = System.getProperty("os.arch");
    String osVersion = System.getProperty("os.version");
    String dataModel = System.getProperty("sun.arch.data.model");
    String javaVersion = System.getProperty("java.version");
    String endian = System.getProperty("sun.cpu.endian");
    String userName = System.getProperty("user.name");
    String userHome = System.getProperty("user.home");
    String userDir = System.getProperty("user.dir");
    String hostName;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ex) {
      hostName = "unknown";
    }
    String serverVersion = ServerVersion.SERVER_VERSION;
    
    String provider = ControllerFactory.getController().getName();

    return new ServerInfo(osName, osArch, osVersion, dataModel, javaVersion, endian, userName, userHome, userDir, hostName, serverVersion, provider);
  }

}
