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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.List;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.marklin.track.monitor.TrackMonitor;
import lan.wervel.marklin.track.monitor.TrackMonitorFactory;

/**
 *
 * @author frans
 */
public class LocalTrackMonitor extends UnicastRemoteObject implements RMITrackMonitor {

  private static final long serialVersionUID = 2093519753009764460L;

  private final TrackMonitor trackMonitor;

  public LocalTrackMonitor(int port) throws RemoteException {
    super(port);
    this.trackMonitor = TrackMonitorFactory.getTrackMonitor();
  }

  @Override
  public void startProcess() throws RemoteException {
    this.trackMonitor.startProcess();
  }

  @Override
  public void stopProcess() throws RemoteException {
    this.trackMonitor.stopProcess();
  }

  @Override
  public List<DriveWay> getTracks() throws RemoteException {
    return this.trackMonitor.getTracks();
  }

  @Override
  public boolean isRunning() throws RemoteException {
    return this.trackMonitor.isRunning();
  }

  @Override
  public Integer getNextTrack() throws RemoteException {
    return this.trackMonitor.getNextTrack();
  }

  /**
   * 
   * @return the current server Date 
   * @throws java.rmi.RemoteException 
   */
  @Override
  public Date getServerTime() throws RemoteException {
    return new Date();
  }
}
