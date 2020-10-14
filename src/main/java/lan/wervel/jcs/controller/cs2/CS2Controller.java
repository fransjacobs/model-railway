/*
 * Copyright (C) 2020 Frans Jacobs.
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
package lan.wervel.jcs.controller.cs2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lan.wervel.jcs.controller.ControllerEvent;
import lan.wervel.jcs.controller.ControllerEventListener;
import lan.wervel.jcs.controller.ControllerInfo;
import lan.wervel.jcs.controller.ControllerService;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.controller.cs2.can.CanMessageFactory;
import static lan.wervel.jcs.controller.cs2.can.MarklinCan.FUNCTION_OFF;
import static lan.wervel.jcs.controller.cs2.can.MarklinCan.FUNCTION_ON;
import lan.wervel.jcs.controller.cs2.net.Connection;
import lan.wervel.jcs.controller.cs2.net.CS2ConnectionFactory;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.Direction;
import lan.wervel.jcs.entities.enums.DecoderType;
import org.pmw.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class CS2Controller implements ControllerService {

  private Connection connection;
  private boolean connected = false;
  private boolean powerOn;

  private final List<ControllerEventListener> controllerEventListeners;
  private final ExecutorService executor;

  private int[] cs2Uid;
  private int cs2id;

  public CS2Controller() {
    controllerEventListeners = new ArrayList<>();
    executor = Executors.newCachedThreadPool();
    this.connection = CS2ConnectionFactory.getConnection();

    //cs2Uid = null;
  }

  @Override
  public void powerOff() {
    this.connection.sendCanMessage(CanMessageFactory.stop(cs2Uid));

    powerOn = false;
  }

  @Override
  public void powerOn() {
    CanMessage reply = this.connection.sendCanMessage(CanMessageFactory.go(cs2Uid));
    if (reply != null && reply.getResponse().isResponseMessage()) {
      this.cs2Uid = reply.getResponse().getUid();
      Logger.trace("CS2 UID int: " + reply.getResponse().getUidInt());
      
      if (reply.getResponse().getUidInt() == 0) {
        reply = this.connection.sendCanMessage(CanMessageFactory.go(null));
        Logger.trace("Response valid: " + reply.isResponseMessage()+" UID: "+reply.getResponse().getUidInt());
      }
      else {
        this.cs2id = reply.getResponse().getUidInt();
        CanMessageFactory.setCs2Uid(cs2id);
      }

      Logger.trace("CS2 UID: " + this.cs2id);
      
      powerOn = true;
    }
  }

  @Override
  public boolean isPowerOn() {
    return this.powerOn;
  }

  @Override
  public boolean connect() {
    //try to connect to the CS2
    if (!connected) {
      Logger.debug("Connecting to CS2...");
      this.connection = CS2ConnectionFactory.getConnection();
      this.connected = this.connection != null;
    }
    executor.execute(() -> broadcastControllerEvent(new ControllerEvent(isPowerOn(), connected)));
    return connected;
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  @Override
  public void disconnect() {
    try {
      final Connection conn = this.connection;
      connected = false;
      this.connection = null;
      synchronized (conn) {
        conn.sendCanMessage(CanMessageFactory.stop(cs2Uid));
        wait200ms();
        conn.close();
        this.cs2Uid = null;
      }
      executor.shutdown();
    } catch (Exception ex) {
      Logger.error(ex);
    }
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  private int getLocoAddres(int address, DecoderType decoderType) {
    int locoAddress;
    switch (decoderType) {
      case MFX:
        locoAddress = 0x4000 + address;
        break;
      case DCC:
        locoAddress = 0xC000 + address;
        break;
      case SX1:
        locoAddress = 0x0800 + address;
        break;
      case MM2:
        locoAddress = address;
        break;
      default:
        locoAddress = address;
        break;
    }

    return locoAddress;
  }

  /**
   * Compatibility with 6050
   *
   * @param address the locomotive address
   * @param protocol
   * @param function the value of the function (F0)
   */
  @Override
  public void toggleDirection(int address, DecoderType protocol, boolean function) {
    toggleDirection(address, protocol);
    setFunction(address, protocol, 0, function);
  }

  @Override
  public void toggleDirection(int address, DecoderType decoderType) {
    int la = getLocoAddres(address, decoderType);
    CanMessage msg = this.connection.sendCanMessage(CanMessageFactory.queryDirection(la));
    DirectionInfo di = new DirectionInfo(msg);
    Logger.trace(di);
    Direction direction = di.getDirection();
    direction = direction.toggle();

    setDirection(address, decoderType, direction);
  }

  @Override
  public void setDirection(int address, DecoderType decoderType, Direction direction) {
    int la = getLocoAddres(address, decoderType);
    Logger.trace("Setting direction to: " + direction + " for loc address: " + la + " Decoder: " + decoderType);
    this.connection.sendCanMessage(CanMessageFactory.setDirection(la, direction.getCS2Value()));
  }

  /**
   * Compatibility 6050
   *
   * @param address the locomotive address
   * @param decoderType
   * @param function the function 0 value
   * @param speed the speed
   */
  @Override
  public void setSpeedAndFunction(int address, DecoderType decoderType, boolean function, int speed) {
    setSpeed(address, decoderType, speed);
    setFunction(address, decoderType, 0, function);
  }

  @Override
  public void setSpeed(int address, DecoderType decoderType, int speed) {
    int la = getLocoAddres(address, decoderType);
    Logger.trace("Setting speed to: " + speed + " for loc address: " + la + " Decoder: " + decoderType);

    //Calculate the speed??
    this.connection.sendCanMessage(CanMessageFactory.setLocSpeed(la, speed));
  }

  @Override
  public void setFunction(int address, DecoderType decoderType, int functionNumber, boolean flag) {
    int value = flag ? FUNCTION_ON : FUNCTION_OFF;
    int la = getLocoAddres(address, decoderType);
    this.connection.sendCanMessage(CanMessageFactory.setFunction(la, functionNumber, value));
  }

  /**
   * Compatibility with 6050
   *
   * @param address address of the locomotive
   * @param decoderType the locomotive decoder protocol
   * @param f1 value function 1
   * @param f2 value function 2
   * @param f3 value function 3
   * @param f4 value function 4
   */
  @Override
  public void setFunctions(int address, DecoderType decoderType, boolean f1, boolean f2, boolean f3, boolean f4) {
    setFunction(address, decoderType, 1, f1);
    setFunction(address, decoderType, 2, f2);
    setFunction(address, decoderType, 3, f3);
    setFunction(address, decoderType, 4, f4);
  }

  @Override
  public void switchAccessoiry(int address, AccessoryValue value) {
    executor.execute(() -> switchAccessoiryOnOff(address, value));
  }

  private void switchAccessoiryOnOff(int address, AccessoryValue value) {
    this.connection.sendCanMessage(CanMessageFactory.switchAccessory(address, value, true));
    wait200ms();
    this.connection.sendCanMessage(CanMessageFactory.switchAccessory(address, value, false));
  }

  @Override
  public int[] getFeedback(int moduleNumber) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public ControllerInfo getControllerInfo() {
    CanMessage msg = this.connection.sendCanMessage(CanMessageFactory.statusConfig(cs2Uid));
    return new ControllerInfo(msg);
  }

  @Override
  public void addControllerEventListener(ControllerEventListener listener) {
    this.controllerEventListeners.add(listener);
  }

  @Override
  public void removeControllerEventListener(ControllerEventListener listener) {
    this.controllerEventListeners.remove(listener);
  }

  @Override
  public void notifyAllControllerEventListeners() {
    Logger.info("Current Controller Power Status: " + (isPowerOn() ? "On" : "Off") + "...");
    executor.execute(() -> broadcastControllerEvent(new ControllerEvent(isPowerOn(), isConnected())));
  }

  public void getLocomotiveConfigData() {
    CanMessage msg = this.connection.sendCanMessage(CanMessageFactory.requestConfig("loks"));

  }

  //accesory address
//uint32_t localID = address - 1; // GUI-address is 1-based, protocol-address is 0-based
//		if (protocol == ProtocolDCC)
//		{
//			localID |= 0x3800;
//		}
//		else
//		{
//			localID |= 0x3000;
//		}
  private void broadcastControllerEvent(ControllerEvent event) {
    Set<ControllerEventListener> snapshot;
    synchronized (controllerEventListeners) {
      if (controllerEventListeners.isEmpty()) {
        snapshot = new HashSet<>();
      } else {
        snapshot = new HashSet<>(controllerEventListeners);
      }
    }

    for (ControllerEventListener listener : snapshot) {
      listener.notify(event);
    }
  }

  private void wait200ms() {
    try {
      Thread.sleep(200L);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

}
