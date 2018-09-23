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
import java.util.Map;
import lan.wervel.jcs.repository.RepositoryFactory;
import lan.wervel.jcs.common.TrackRepository;
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
public class LocalRepository extends UnicastRemoteObject implements RMIRepository {

  private static final long serialVersionUID = 1880159118423187255L;

  private final TrackRepository repository;

  public LocalRepository(int port) throws RemoteException {
    super(port);
    this.repository = RepositoryFactory.getRepository();
  }

  @Override
  public Map<Integer, Locomotive> getLocomotives() throws RemoteException {
    return this.repository.getLocomotives();
  }

  @Override
  public Map<Integer, Crane> getCranes() throws RemoteException {
    return this.repository.getCranes();
  }

  @Override
  public Map<Integer, FeedbackModule> getFeedbackModules() throws RemoteException {
    return this.repository.getFeedbackModules();
  }

  @Override
  public FeedbackModule getFeedbackModule(Integer moduleNumber) throws RemoteException {
    return this.repository.getFeedbackModule(moduleNumber);
  }

  @Override
  public void updateControllableItem(AttributeChangedEvent evt) throws RemoteException {
    this.repository.updateControllableItem(evt);
  }

  @Override
  public Map<Integer, SolenoidAccessoiry> getSolenoidAccessoiries() throws RemoteException {
    return this.repository.getSolenoidAccessoiries();
  }

  @Override
  public SolenoidAccessoiry getSolenoidAccessoiry(Integer address) throws RemoteException {
    return this.repository.getSolenoidAccessoiry(address);
  }

  @Override
  public Locomotive getLocomotive(Integer address) throws RemoteException {
    return this.repository.getLocomotive(address);
  }

  @Override
  public Crane getCrane(Integer address) throws RemoteException {
    return this.repository.getCrane(address);
  }

  @Override
  public Map<Integer, DriveWay> getDriveWays() throws RemoteException {
    return this.repository.getDriveWays();
  }

  @Override
  public DriveWay getDriveWay(Integer address) throws RemoteException {
    return this.repository.getDriveWay(address);
  }

  @Override
  public ServerInfo getServerInfo() throws RemoteException {
    return ServerInfoProvider.getServerInfo();
  }

  @Override
  public void startFeedbackCycle() {
    this.repository.startFeedbackCycle();
  }

  @Override
  public void stopFeedbackCycle() {
    this.repository.stopFeedbackCycle();
  }

  @Override
  public boolean isFeedbackCycleRunning() throws RemoteException {
    return this.repository.isFeedbackCycleRunning();
  }

  @Override
  public boolean feedbackCycleClock() throws RemoteException {
    return this.repository.feedbackCycleClock();
  }

  public TrackRepository getRepository() {
    return this.repository;
  }

  @Override
  public Date getServerTime() throws RemoteException {
    return new Date();
  }
}
