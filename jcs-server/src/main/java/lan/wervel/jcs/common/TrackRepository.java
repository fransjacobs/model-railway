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
package lan.wervel.jcs.common;

import java.util.Map;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;
import lan.wervel.jcs.server.rmi.ServerInfo;
import lan.wervel.jcs.repository.model.Crane;
import lan.wervel.jcs.repository.model.DriveWay;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;

/**
 * The Track repository contain all track item which are used on the Track This can be Locomotives, Turnouts, Signals, etc There For
 * future use the implementation of the Repository could be changed to an other storage provider
 *
 * @author frans
 */
public interface TrackRepository {

  /**
   *
   * @return a Map with Locomotives
   */
  public Map<Integer, Locomotive> getLocomotives();

  /**
   *
   * @return a Map with Cranes
   */
  public Map<Integer, Crane> getCranes();

  /**
   *
   * @return a Map with Feedback modules
   */
  public Map<Integer, FeedbackModule> getFeedbackModules();

  /**
   *
   * @param moduleNumber
   * @return
   */
  public FeedbackModule getFeedbackModule(Integer moduleNumber);

  /**
   *
   * @param evt the item to update, this can be the status or Speed etc
   */
  public void updateControllableItem(AttributeChangedEvent evt);

  /**
   *
   * @return
   */
  public Map<Integer, SolenoidAccessoiry> getSolenoidAccessoiries();

  /**
   *
   * @param address
   * @return
   */
  public SolenoidAccessoiry getSolenoidAccessoiry(Integer address);

  /**
   *
   * @param address the address of the Locomotive wanted
   * @return a Locomotive object or null when not found
   */
  public Locomotive getLocomotive(Integer address);

  /**
   *
   * @param address the address of the Crane wanted
   * @return a Crane object or null when not found
   */
  public Crane getCrane(Integer address);

  /**
   *
   * @return a Map with all driveWays
   */
  public Map<Integer, DriveWay> getDriveWays();

  /**
   * 
   * @return Driveways which are a track 
   */
  public Map<Integer, DriveWay> getDriveWayTracks();
  
  /**
   *
   * @param address the address of the DriveWay to retrieve
   * @return a DriveWay object or null when not found
   */
  public DriveWay getDriveWay(Integer address);

  /**
   *
   * @param controller a Controller Object to add. A Controller takes care of sending commands to the Track
   */
  public void addController(ControllerProvider controller);

  /**
   *
   * @param controller the Controller object to remove
   */
  public void removeController(ControllerProvider controller);

  /**
   *
   * @return info about the server
   */
  public ServerInfo getServerInfo();

  /**
   * Start the feedback cycle on all feedback modules of the repository
   */
  public void startFeedbackCycle();

  /**
   * End the feedback cycle on all feedback modules of the repository
   */
  public void stopFeedbackCycle();

  /**
   *
   * @return true when the feedback cycle is running
   */
  public boolean isFeedbackCycleRunning();

  /**
   *
   * @return
   */
  public boolean feedbackCycleClock();

}
