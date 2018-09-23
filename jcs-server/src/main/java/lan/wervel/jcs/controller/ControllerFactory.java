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
package lan.wervel.jcs.controller;

import lan.wervel.jcs.common.ControllerProvider;
import lan.wervel.jcs.controller.marklin.M6051Controller;
import lan.wervel.jcs.controller.marklin.M6051DummyController;
import lan.wervel.jcs.util.RunUtil;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class ControllerFactory {

  private final ControllerProvider controllerProvider;
  private static ControllerFactory controllerFactory;

  private ControllerFactory() {
    long start = System.currentTimeMillis();
    if (RunUtil.hasSerialPort()) {
      controllerProvider = new M6051Controller();
    } else {
      controllerProvider = new M6051DummyController();
      Logger.warn("No SerialPort found! Using Dummy Serial controller...");
    }
    long end = System.currentTimeMillis();
    Logger.debug("Controller initalized. Duration: "+(end - start)+" ms.");
  }

  public static ControllerFactory getInstance() {
    if (controllerFactory == null) {
      controllerFactory = new ControllerFactory();
    }
    return controllerFactory;
  }

  public static ControllerProvider getController() {
    return ControllerFactory.getInstance().getControllerProvider();
  }

  public ControllerProvider getControllerProvider() {
    return this.controllerProvider;
  }

}
