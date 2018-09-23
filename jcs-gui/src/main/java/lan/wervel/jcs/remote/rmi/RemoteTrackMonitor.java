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
import java.util.List;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.server.rmi.RMITrackMonitor;
import lan.wervel.marklin.track.monitor.TrackMonitor;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class RemoteTrackMonitor implements TrackMonitor {

  private final String remoteIp;
  private final int remotePort;

  private RMITrackMonitor rmiMonitor;

  boolean connected;

  public RemoteTrackMonitor(String remoteIp, int remotePort) {
    this.remoteIp = remoteIp;
    this.remotePort = remotePort;
    rmiMonitor = reconnect(null);
  }

  @Override
  public void startProcess() {
    Logger.debug("Starting...");
    try {
      getCheckedMonitor().startProcess();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public void stopProcess() {
    Logger.debug("Stopping...");
    try {
      getCheckedMonitor().stopProcess();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public List<DriveWay> getTracks() {
    List<DriveWay> dwl = null;
    try {
      dwl = getCheckedMonitor().getTracks();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return dwl;
  }

  @Override
  public boolean isRunning() {
    boolean b = false;
    try {
      b = getCheckedMonitor().isRunning();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return b;
  }

  @Override
  public Integer getNextTrack() {
    Integer i = null;
    try {
      i = getCheckedMonitor().getNextTrack();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return i;
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
  private RMITrackMonitor getCheckedMonitor() {
    try {
      if (rmiMonitor == null) {
        rmiMonitor = reconnect(null);
      }
      rmiMonitor.getServerTime();
    } catch (RemoteException ex) {
      connected = false;
      rmiMonitor = reconnect(ex);
    }
    if (rmiMonitor != null) {
      //reconnected or still connected
      connected = true;
    }
    return rmiMonitor;
  }

  private RMITrackMonitor reconnect(Exception ex) {
    RMITrackMonitor rmiMon = null;
    try {
      InetAddress inetAddress = InetAddress.getByName(remoteIp);
      String hostName = inetAddress.getHostName();

      Logger.debug("Try to (re)connect RMI to IP: " + remoteIp + " Hostname: " + hostName + " port: " + remotePort);

      rmiMon = (RMITrackMonitor) Naming.lookup("rmi://" + remoteIp + ":" + remotePort + "/" + RMITrackMonitor.RMI_SERVICE);
      Date sd = rmiMon.getServerTime();
      Logger.debug("Current Server Time: " + sd);
    } catch (RemoteException | NotBoundException | MalformedURLException | UnknownHostException re) {
      if (ex != null) {
        Logger.error(ex);
      }
      Logger.error(re);
    }
    return rmiMon;
  }

}
