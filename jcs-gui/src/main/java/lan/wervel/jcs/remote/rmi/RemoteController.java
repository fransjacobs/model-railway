/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.wervel.jcs.remote.rmi;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;
import lan.wervel.jcs.server.rmi.RMIController;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class RemoteController implements ControllerProvider {

  private final String remoteIp;
  private final int remotePort;

  private RMIController rmiController;

  boolean connected;

  public RemoteController(String remoteIp, int remotePort) {
    this.remoteIp = remoteIp;
    this.remotePort = remotePort;
    rmiController = reconnect(null);
  }

  @Override
  public void powerOff() {
    try {
      getCheckedController().powerOff();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public void powerOn() {
    try {
      getCheckedController().powerOn();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public boolean isPowerOn() {
    boolean b = false;
    try {
      b = getCheckedController().isPowerOn();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return b;
  }

  @Override
  public boolean connect() {
    boolean b = false;
    try {
      b = getCheckedController().connect();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return b;
  }

  @Override
  public boolean isConnected() {
    boolean b = false;
    try {
      b = getCheckedController().isConnected();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return b;
  }

  @Override
  public void disconnect() {
    try {
      getCheckedController().disconnect();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public String getName() {
    String s = null;
    try {
      s = getCheckedController().getName();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return s;
  }

  @Override
  public void updateTrack(AttributeChangedEvent evt) {
    try {
      getCheckedController().updateTrack(evt);
      if (evt.getRepository() != null) {
        evt.getRepository().updateControllableItem(evt);
      }
      evt.getSource().setEnableAttributeChangeHandling(true);
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  public String getRemoteIp() {
    return remoteIp;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public boolean isRemoteConnected() {
    return connected;
  }

  /**
   * @return RMIRepository which has been check for a valid connection and reconnected if necessary
   */
  private RMIController getCheckedController() {
    try {
      if (rmiController == null) {
        rmiController = reconnect(null);
      }
      rmiController.getServerTime();
    } catch (RemoteException ex) {
      connected = false;
      rmiController = reconnect(ex);
    }
    if (rmiController != null) {
      //reconnected or still connected
      connected = true;
    }
    return rmiController;
  }

  private RMIController reconnect(Exception ex) {
    RMIController rmiCtr = null;
    try {
      InetAddress inetAddress = InetAddress.getByName(remoteIp);
      String hostName = inetAddress.getHostName();

      Logger.debug("Try to (re)connect RMI to IP: " + remoteIp + " Hostname: " + hostName + " port: " + remotePort);

      rmiCtr = (RMIController) Naming.lookup("rmi://" + remoteIp + ":" + remotePort + "/" + RMIController.RMI_SERVICE);
      Date sd = rmiCtr.getServerTime();
      Logger.debug("Current Server Time: " + sd);
    } catch (RemoteException | NotBoundException | MalformedURLException | UnknownHostException re) {
      if (ex != null) {
        Logger.error(ex);
      }
      Logger.error(re);
    }
    return rmiCtr;
  }

}
