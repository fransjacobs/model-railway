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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import lan.wervel.jcs.repository.model.DriveWay;

/**
 *
 * @author frans
 */
public interface RMITrackMonitor extends Remote {

  public static final String RMI_SERVICE = "TrackMonitorServer";

  void startProcess() throws RemoteException;

  void stopProcess() throws RemoteException;

  List<DriveWay> getTracks() throws RemoteException ;

  boolean isRunning() throws RemoteException;
  
  public Integer getNextTrack() throws RemoteException;

  /**
   * 
   * @return the current server Date 
   * @throws java.rmi.RemoteException 
   */
  public Date getServerTime() throws RemoteException;
}
