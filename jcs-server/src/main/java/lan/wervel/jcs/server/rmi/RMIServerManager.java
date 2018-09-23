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
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import lan.wervel.jcs.util.RunUtil;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class RMIServerManager {

  private Registry registry;
  private InetAddress inetAddress;
  private int registryPort;
  private int servicePort;

  private LocalController localController;
  private LocalRepository localRepository;
  private LocalTrackMonitor localTrackMonitor;

  public RMIServerManager(int registryPort, int servicePort) {
    this.registryPort = registryPort;
    this.servicePort = servicePort;

    try {
      inetAddress = RunUtil.getLocalHostLANAddress();
      Logger.debug("IP: " + inetAddress.getHostAddress() + " host: " + inetAddress.getHostName());

      if (RunUtil.getOsType() != RunUtil.OS_MAC_OS_X) {
        //Needed for linux, does not hurt on Windows, on osx it prevents the local host
        System.setProperty("java.rmi.server.hostname", inetAddress.getHostAddress());
      }

      localRepository = new LocalRepository(servicePort);
      localController = new LocalController(servicePort);
      //Also initalize the controller
      if (!this.localController.isConnected()) {
        localController.connect();
      }
      
      localTrackMonitor = new LocalTrackMonitor(servicePort);
      
      try {
        registry = LocateRegistry.createRegistry(registryPort);
      } catch (ExportException ee) {
        Logger.warn("Create Registry throws error", ee);
        registry = LocateRegistry.getRegistry(registryPort);
      }
      Logger.debug("RMI Registry created...");

      registry.rebind(RMIRepository.RMI_SERVICE, localRepository);
      registry.rebind(RMIController.RMI_SERVICE, localController);
      registry.rebind(RMITrackMonitor.RMI_SERVICE, localTrackMonitor);

      Logger.info("RMI ServerManager started...");
    } catch (ExportException ex) {
      Logger.error("Server cannot use RMI port " + registryPort
              + " as it is already in use, check that you are not running another instance of the server.");
    } catch (RemoteException re) {
      Logger.error("Can't initalize RMI Server...", re);
    } catch (UnknownHostException uhe) {
      Logger.error("Can't get my IP address...", uhe);
    }
  }

  public Registry getRegistry() {
    return registry;
  }

  public InetAddress getInetAddress() {
    return inetAddress;
  }

  public int getRegistryPort() {
    return registryPort;
  }

  public int getServicePort() {
    return servicePort;
  }

  public LocalController getLocalController() {
    return localController;
  }

  public LocalRepository getLocalRepository() {
    return localRepository;
  }
}
