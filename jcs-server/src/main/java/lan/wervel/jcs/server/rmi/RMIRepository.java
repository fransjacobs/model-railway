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
import java.util.Map;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;
import lan.wervel.jcs.repository.model.Crane;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;

/**
 *
 * @author frans
 */
public interface RMIRepository extends Remote {

  public static final String RMI_SERVICE = "RepositoryServer";

  /**
   *
   * @return a Map with Locomotives
   * @throws java.rmi.RemoteException
   */
  public Map<Integer, Locomotive> getLocomotives() throws RemoteException;

  /**
   *
   * @return a Map with Cranes
   * @throws java.rmi.RemoteException
   */
  public Map<Integer, Crane> getCranes() throws RemoteException;

  /**
   *
   * @return a Map with Feedback modules
   * @throws java.rmi.RemoteException
   */
  public Map<Integer, FeedbackModule> getFeedbackModules() throws RemoteException;

  /**
   *
   * @param moduleNumber
   * @return
   * @throws java.rmi.RemoteException
   */
  public FeedbackModule getFeedbackModule(Integer moduleNumber) throws RemoteException;

  /**
   *
   * @param evt the item to update, this can be the status or Speed etc
   * @throws java.rmi.RemoteException
   */
  public void updateControllableItem(AttributeChangedEvent evt) throws RemoteException;

  /**
   *
   * @return @throws java.rmi.RemoteException
   */
  public Map<Integer, SolenoidAccessoiry> getSolenoidAccessoiries() throws RemoteException;

  /**
   *
   * @param address
   * @return
   * @throws java.rmi.RemoteException
   */
  public SolenoidAccessoiry getSolenoidAccessoiry(Integer address) throws RemoteException;

  /**
   *
   * @param address the address of the Locomotive wanted
   * @return a Locomotive object or null when not found
   * @throws java.rmi.RemoteException
   */
  public Locomotive getLocomotive(Integer address) throws RemoteException;

  /**
   *
   * @param address the address of the Crane wanted
   * @return a Crane object or null when not found
   * @throws java.rmi.RemoteException
   */
  public Crane getCrane(Integer address) throws RemoteException;
  
    /**
   *
   * @return a Map with all driveWays
   * @throws java.rmi.RemoteException
   */
  public Map<Integer, DriveWay> getDriveWays() throws RemoteException;

  /**
   *
   * @param address the address of the DriveWay to retrieve
   * @return a DriveWay object or null when not found
   * @throws java.rmi.RemoteException
   */
  public DriveWay getDriveWay(Integer address) throws RemoteException;


  /**
   *
   * @return A ServerInfo Object with some info about the Server
   * @throws RemoteException
   */
  public ServerInfo getServerInfo() throws RemoteException;

  /**
   * Start the feedback cycle on all feedback modules of the repository
   *
   * @throws java.rmi.RemoteException
   */
  public void startFeedbackCycle() throws RemoteException;

  /**
   * End the feedback cycle on all feedback modules of the repository
   *
   * @throws java.rmi.RemoteException
   */
  public void stopFeedbackCycle() throws RemoteException;

  /**
   *
   * @return @throws RemoteException
   */
  public boolean isFeedbackCycleRunning() throws RemoteException;
  
  /**
   * 
   * @return
   * @throws RemoteException 
   */
  public boolean feedbackCycleClock() throws RemoteException;
  
  /**
   * 
   * @return the current server Date 
   * @throws java.rmi.RemoteException 
   */
  public Date getServerTime() throws RemoteException;
}
