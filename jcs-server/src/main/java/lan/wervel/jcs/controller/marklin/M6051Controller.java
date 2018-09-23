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
import lan.wervel.jcs.util.RunUtil;
import org.pmw.tinylog.Logger;
import lan.wervel.jcs.controller.marklin.serial.Serial6050;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;

/**
 * Implementation of the (old) serial Marklin 6050/6051 Interface
 *
 * @author frans
 */
public class M6051Controller implements ControllerProvider {

  private final String portname;
  private final Serial6050 serial6050;
  private M6050CommandThread commandThread;
  

  public M6051Controller() {
    portname = RunUtil.getDefaultPortname();
    serial6050 = new Serial6050(portname);
  }

  @Override
  public void powerOff() {
    this.serial6050.sendSingleCommand(Serial6050.STOP_COMMAND);
  }

  @Override
  public void powerOn() {
    this.serial6050.sendSingleCommand(Serial6050.GO_COMMAND);
  }

  @Override
  public boolean isPowerOn() {
    return this.serial6050.isPowerOn();
  }

  @Override
  public boolean connect() {
    boolean result = true;
    if (this.commandThread != null && this.commandThread.isRunning()) {
      Logger.debug("Allready connected and running...");
    }
    else {
      result = serial6050.connect6050();
      if (result) {
        commandThread = new M6050CommandThread(serial6050);
        commandThread.start();
      }
    }  

    return result && this.commandThread.isRunning();
  }

  @Override
  public boolean isConnected() {
    return RunUtil.hasSerialPort() && commandThread != null && commandThread.isRunning();
  }

  @Override
  public void disconnect() {
    commandThread.quit();

    commandThread = null;
    serial6050.disconnect6050();
  }

  @Override
  public synchronized void updateTrack(AttributeChangedEvent evt) {
    Logger.trace("Changed: " + evt);
    this.commandThread.enqueue(evt);
  }

  @Override
  public String getName() {
    return "Marklin 6050/6051 Interface";
  }

}
