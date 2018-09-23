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
package lan.wervel.jcs.controller.marklin;

import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;

/**
 * Dummy Implementation of the (old) serial Marklin 6050/6051 Interface
 *
 * @author frans
 */
public class M6051DummyController implements ControllerProvider {

  private final String portname = "Dummy";

  private boolean connected;
  private boolean powerOn;

  public M6051DummyController() {

  }

  @Override
  public void powerOff() {
    this.powerOn = false;
  }

  @Override
  public void powerOn() {
    this.powerOn = true;
  }

  @Override
  public boolean isPowerOn() {
    return this.powerOn;
  }

  @Override
  public boolean connect() {
    this.connected = true;
    return this.connected;
  }

  @Override
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  public void disconnect() {
    this.connected = false;
  }

  @Override
  public void updateTrack(AttributeChangedEvent evt) {
    
    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getName() {
    return "Dummy 6050/6051 Interface";
  }

 
}
