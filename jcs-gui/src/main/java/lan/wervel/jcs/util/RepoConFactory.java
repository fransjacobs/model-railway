package lan.wervel.jcs.util;

import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.remote.rmi.RemoteController;
import lan.wervel.jcs.remote.rmi.RemoteRepository;
import lan.wervel.jcs.remote.rmi.RemoteTrackMonitor;
import lan.wervel.marklin.track.monitor.TrackMonitor;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class RepoConFactory {

  private final TrackRepository repository;
  private final ControllerProvider controller;
  private final TrackMonitor monitor;

  private static RepoConFactory controllerFactory;

  //private final static String XML_REPO_IMPL = "lan.wervel.jcs.repository.XmlRepository";
  //private final static String LOCAL_IMPL = "lan.wervel.jcs.controller.marklin.M6051Controller";
  private RepoConFactory() {
    Logger.debug("Trying to use the Remote Implementations...");
    JCSServiceFinder jcsServiceFinder = getServiceFinder();
    long now = System.currentTimeMillis();
    long timeout = now + 2000;

    while (!jcsServiceFinder.isServiceFound() && now < timeout) {
      now = System.currentTimeMillis();
    }

    if (jcsServiceFinder.isServiceFound()) {
      String serviceHost = jcsServiceFinder.getHostAddress();
      int servicePort = jcsServiceFinder.getServicePort();

      Logger.debug("Found host " + serviceHost + " on port " + servicePort + " as jcs-server...");

      repository = new RemoteRepository(serviceHost, servicePort);
      controller = new RemoteController(serviceHost, servicePort);
      monitor = new RemoteTrackMonitor(serviceHost, servicePort);
    } else {
      //Use a Dummy implementation
      Logger.info("Use Demo Implementation...");
      DummyRepoCon drc = new DummyRepoCon();
      repository = drc;
      controller = drc;
      monitor = drc;
    }

    if (controller != null) {
      //Connect the controller with the repository
      repository.addController(controller);
      Logger.info("Initialized Controller " + this.controller.getClass().getSimpleName() + ".");
    } else {
      Logger.error("Could not initialize a Controller!");
    }

    if (!controller.isConnected()) {
      Logger.debug("Connect Provider...");
      controller.connect();
    }
  }

  private JCSServiceFinder getServiceFinder() {
    JCSServiceFinder jcsServiceFinder = new JCSServiceFinder();
    jcsServiceFinder.start();
    return jcsServiceFinder;
  }

  public boolean isConnected() {
    if (RunUtil.hasSerialPort()) {
      return controller.isConnected();
    } else {
      return ((RemoteRepository) repository).isRemoteConnected() && ((RemoteController) controller).isRemoteConnected();
    }
  }

  public static RepoConFactory getInstance() {
    if (controllerFactory == null) {
      controllerFactory = new RepoConFactory();
    }
    return controllerFactory;
  }

  public static ControllerProvider getController() {
    return RepoConFactory.getInstance().getControllerProvider();
  }

  public static TrackRepository getRepository() {
    return RepoConFactory.getInstance().getTrackRepository();
  }
  
  public static TrackMonitor getMonitor() {
    return RepoConFactory.getInstance().getTrackMonitor();
  }

  private TrackRepository getTrackRepository() {
    return repository;
  }

  private ControllerProvider getControllerProvider() {
    return this.controller;
  }
  
  private TrackMonitor getTrackMonitor() {
    return this.monitor;
  }

}
