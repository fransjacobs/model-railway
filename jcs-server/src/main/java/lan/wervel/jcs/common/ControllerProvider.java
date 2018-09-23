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

import lan.wervel.jcs.repository.model.AttributeChangedEvent;

/**
 *
 * @author frans
 */
public interface ControllerProvider {

  /**
   * Stop all, will cut the power to the track
   */
  void powerOff();

  /**
   * Go, will enable the power on the track
   */
  void powerOn();

  /**
   *
   * @return true when the track is powered on
   */
  boolean isPowerOn();

  /**
   * Connect with the Controller
   *
   * @return true when the connection succeeded
   */
  boolean connect();

  /**
   * @return true when the controller is connected and running
   */
  boolean isConnected();

  /**
   * Disconnect from the Controller, stop the controller
   */
  void disconnect();

  /**
   *
   * @param evt the event which contains the changed items which should be updated on the track
   */
  void updateTrack(AttributeChangedEvent evt);

  /**
   *
   * @return the name of the provider
   */
  String getName();

}
