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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.common.TrackRepository;
import lan.wervel.jcs.repository.model.AttributeChangeListener;
import lan.wervel.jcs.server.rmi.RMIRepository;
import lan.wervel.jcs.server.rmi.ServerInfo;
import lan.wervel.jcs.repository.model.Crane;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import org.pmw.tinylog.Logger;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;

/**
 *
 * @author frans
 */
public class RemoteRepository implements TrackRepository, AttributeChangeListener {

  private final String remoteIp;
  private final int remotePort;

  private RMIRepository rmiRepository;
  private final List<ControllerProvider> controllers;

  boolean connected;

  public RemoteRepository(String remoteIp, int remotePort) {
    this.controllers = new ArrayList<>();
    this.remoteIp = remoteIp;
    this.remotePort = remotePort;
    rmiRepository = reconnect(null);
  }

  @Override
  public Map<Integer, Locomotive> getLocomotives() {
    Map<Integer, Locomotive> m = null;
    try {
      m = getCheckedRepo().getLocomotives();
      for (Locomotive l : m.values()) {
        l.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return m;
  }

  @Override
  public Map<Integer, Crane> getCranes() {
    Map<Integer, Crane> m = null;
    try {
      m = getCheckedRepo().getCranes();
      for (Crane c : m.values()) {
        c.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return m;
  }

  @Override
  public Map<Integer, FeedbackModule> getFeedbackModules() {
    Map<Integer, FeedbackModule> m = null;
    try {
      m = getCheckedRepo().getFeedbackModules();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return m;
  }

  @Override
  public FeedbackModule getFeedbackModule(Integer moduleNumber) {
    FeedbackModule fm = null;
    try {
      fm = getCheckedRepo().getFeedbackModule(moduleNumber);
      if (fm != null) {
        fm.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return fm;
  }

  @Override
  public Map<Integer, SolenoidAccessoiry> getSolenoidAccessoiries() {
    Map<Integer, SolenoidAccessoiry> m = null;
    try {
      m = getCheckedRepo().getSolenoidAccessoiries();
      for (SolenoidAccessoiry s : m.values()) {
        s.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return m;
  }

  @Override
  public SolenoidAccessoiry getSolenoidAccessoiry(Integer address) {
    SolenoidAccessoiry s = null;
    try {
      s = getCheckedRepo().getSolenoidAccessoiry(address);
      if (s != null) {
        s.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return s;
  }

  @Override
  public Locomotive getLocomotive(Integer address) {
    Locomotive l = null;
    try {
      l = getCheckedRepo().getLocomotive(address);
      if (l != null) {
        l.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return l;
  }

  @Override
  public Crane getCrane(Integer address) {
    Crane c = null;
    try {
      c = getCheckedRepo().getCrane(address);
      if (c != null) {
        c.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return c;
  }

  @Override
  public Map<Integer, DriveWay> getDriveWays() {
    Map<Integer, DriveWay> m = null;
    try {
      m = getCheckedRepo().getDriveWays();
      for (DriveWay dw : m.values()) {
        dw.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return m;
  }

  @Override
  public Map<Integer, DriveWay> getDriveWayTracks() {
    Map<Integer, DriveWay> dws = getDriveWays();
    Map<Integer, DriveWay> dwt = new HashMap<>();

    dws.values().forEach((dw) -> {
      if (dw.isTrack()) {
        dwt.put(dw.getAddress(), dw);
      }
    });

    return dwt;
  }

  @Override
  public DriveWay getDriveWay(Integer address) {
    DriveWay dw = null;
    try {
      dw = getCheckedRepo().getDriveWay(address);
      if (dw != null) {
        dw.addAttributeChangeListener(this);
      }
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return dw;
  }

  @Override
  public ServerInfo getServerInfo() {
    ServerInfo si = null;
    try {
      si = getCheckedRepo().getServerInfo();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return si;
  }

  @Override
  public void startFeedbackCycle() {
    try {
      getCheckedRepo().startFeedbackCycle();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public void stopFeedbackCycle() {
    try {
      getCheckedRepo().stopFeedbackCycle();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public boolean isFeedbackCycleRunning() {
    boolean running = false;
    try {
      running = getCheckedRepo().isFeedbackCycleRunning();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return running;
  }

  @Override
  public boolean feedbackCycleClock() {
    boolean tick = false;
    try {
      tick = getCheckedRepo().feedbackCycleClock();
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
    return tick;
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

  @Override
  public void updateControllableItem(AttributeChangedEvent evt) {
    try {
      getCheckedRepo().updateControllableItem(evt);
    } catch (RemoteException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public void addController(ControllerProvider controller) {
    this.controllers.add(controller);
  }

  @Override
  public void removeController(ControllerProvider controller) {
    this.controllers.remove(controller);
  }

  @Override
  public void controllableItemChange(AttributeChangedEvent evt) {
    //An item has changed, first check whether the change of the item should result into track command(s)
    if (!this.controllers.isEmpty()) {
      this.controllers.forEach((controller) -> {
        //Disable recursive calling
        evt.getSource().setEnableAttributeChangeHandling(false);
        //set the repo for callback
        evt.setRepository(this);
        controller.updateTrack(evt);
      });
    }
  }

  /**
   * @return RMIRepository which has been check for a valid connection and reconnected if necessary
   */
  private RMIRepository getCheckedRepo() {
    try {
      if (rmiRepository == null) {
        rmiRepository = reconnect(null);
      }
      rmiRepository.getServerTime();
    } catch (RemoteException ex) {
      this.connected = false;
      rmiRepository = reconnect(ex);
    }
    if (rmiRepository != null) {
      //reconnected or still connected
      connected = true;
    }
    return rmiRepository;
  }

  private RMIRepository reconnect(Exception ex) {
    RMIRepository rmiRepo = null;
    try {
      InetAddress inetAddress = InetAddress.getByName(remoteIp);
      String hostName = inetAddress.getHostName();

      Logger.debug("Try to (re)connect RMI to IP: " + remoteIp + " Hostname: " + hostName + " port: " + remotePort);

      rmiRepo = (RMIRepository) Naming.lookup("rmi://" + remoteIp + ":" + remotePort + "/" + RMIRepository.RMI_SERVICE);
      Date sd = rmiRepo.getServerTime();
      Logger.debug("Current Server Time: " + sd);
    } catch (RemoteException | NotBoundException | MalformedURLException | UnknownHostException re) {
      if (ex != null) {
        Logger.error(ex);
      }
      Logger.error(re);
    }
    return rmiRepo;
  }

}
