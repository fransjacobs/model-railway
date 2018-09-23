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
package lan.wervel.jcs;

import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.common.ServerVersion;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.controller.ControllerFactory;
import lan.wervel.jcs.repository.RepositoryFactory;
import lan.wervel.jcs.server.rmi.RMIServerManager;
import org.pmw.tinylog.Logger;

import lan.wervel.jcs.util.ServiceAnnouncer;

/**
 * Single Locomotive Control This is the run time start point for this the server daemon.
 */
public class JCSServer extends Thread implements ServerVersion {

  private final RMIServerManager rmiServerManager;
  private static JCSServer instance = null;

  private static final int RMI_REGISTRY_PORT = 2024;
  private static final int RMI_SERVICE_PORT = 2025;

  private final TrackRepository repository;
  private final ControllerProvider controller;

  private JCSServer() {
    repository = RepositoryFactory.getRepository();
    controller = ControllerFactory.getController();

    rmiServerManager = new RMIServerManager(RMI_REGISTRY_PORT, RMI_SERVICE_PORT);
  }

  public static void main(String[] args) {
    JCSServer server = JCSServer.getInstance();
    server.init();
  }

  private void init() {
    repository.addController(controller);
    controller.connect();

    String serviceHost = rmiServerManager.getInetAddress().getHostAddress();
    int port = rmiServerManager.getRegistryPort();

    Logger.debug("JCS Server Version: " + ServerVersion.SERVER_VERSION + " Starting...");

    ServiceAnnouncer.getInstance(ServiceAnnouncer.DEFAULT_ANNOUNCE_PORT, port).start();
    
    Logger.info("JCSServer @ " + serviceHost + " on port " + port + " Running. Ctrl-C to shutdown.");
  }

  /**
   * Executed at shutdown in response to a Ctrl-C etc.
   */
  @Override
  public void run() {
    // Perform shutdown methods.
    Logger.info("JCSServer is Shutting Down...");
  }

  /**
   * Gets us in the singleton pattern.
   *
   * @return Us
   */
  public static JCSServer getInstance() {
    if (instance == null) {
      instance = new JCSServer();
      // Prepare for shutdown...
      Runtime.getRuntime().addShutdownHook(instance);
    }
    return instance;
  }

}
