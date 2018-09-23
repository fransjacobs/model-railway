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
import lan.wervel.jcs.repository.model.AttributeChangedEvent;

/**
 *
 * @author frans
 */
public interface RMIController extends Remote {

  public static final String RMI_SERVICE = "ControllerServer";

  /**
   * Stop all, will cut the power to the track
   *
   * @throws java.rmi.RemoteException
   */
  public void powerOff() throws RemoteException;

  /**
   * Go, will enable the power on the track
   *
   * @throws java.rmi.RemoteException
   */
  public void powerOn() throws RemoteException;

  /**
   *
   * @return true when the track is powered on
   * @throws java.rmi.RemoteException
   */
  public boolean isPowerOn() throws RemoteException;

  /**
   * Connect with the Controller
   *
   * @return true when the connection succeeded
   * @throws java.rmi.RemoteException
   */
  public boolean connect() throws RemoteException;

  /**
   * @return true when the controller is connected and running
   * @throws java.rmi.RemoteException
   */
  public boolean isConnected() throws RemoteException;

  /**
   * Disconnect from the Controller, stop the controller
   *
   * @throws java.rmi.RemoteException
   */
  public void disconnect() throws RemoteException;

  /**
   *
   * @param evt the event which contains the changed items which should be updated on the track
   * @throws java.rmi.RemoteException
   */
  public void updateTrack(AttributeChangedEvent evt) throws RemoteException;

  /**
   *
   * @return the name of the provider
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * 
   * @return the current server Date 
   * @throws java.rmi.RemoteException 
   */
  public Date getServerTime() throws RemoteException;

}
