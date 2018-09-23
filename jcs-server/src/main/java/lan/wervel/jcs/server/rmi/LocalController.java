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
import lan.wervel.jcs.controller.ControllerFactory;
import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;

/**
 *
 * @author frans
 */
public class LocalController extends UnicastRemoteObject implements RMIController {
  
  private static final long serialVersionUID = 2093519753009764460L;
  
  private final ControllerProvider controllerProvider;
  
  public LocalController(int port) throws RemoteException {
    super(port);
    this.controllerProvider = ControllerFactory.getController();
  }
  
  @Override
  public void powerOff() throws RemoteException {
    this.controllerProvider.powerOff();
  }
  
  @Override
  public void powerOn() throws RemoteException {
    this.controllerProvider.powerOn();
  }
  
  @Override
  public boolean isPowerOn() throws RemoteException {
    return this.controllerProvider.isPowerOn();
  }
  
  @Override
  public boolean connect() throws RemoteException {
    return this.controllerProvider.connect();
  }
  
  @Override
  public boolean isConnected() throws RemoteException {
    return this.controllerProvider.isConnected();
  }
  
  @Override
  public void disconnect() throws RemoteException {
    this.controllerProvider.disconnect();
  }
  
  @Override
  public void updateTrack(AttributeChangedEvent evt) throws RemoteException {
    this.controllerProvider.updateTrack(evt);
  }
  
  @Override
  public String getName() throws RemoteException {
    return this.controllerProvider.getName();
  }
  
  public ControllerProvider getControllerProvider() {
    return this.controllerProvider;
  }
  
  @Override
  public Date getServerTime() throws RemoteException {
    return new Date();
  }
  
}
