/*
 * Copyright 2023 frans.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.controller.dccex;

import java.awt.Image;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.controller.CommandStation;
import jcs.controller.dccex.connection.DccExConnectionFactory;
import jcs.controller.events.AccessoryEventListener;
import jcs.controller.events.LocomotiveDirectionEventListener;
import jcs.controller.events.LocomotiveFunctionEventListener;
import jcs.controller.events.LocomotiveSpeedEventListener;
import jcs.controller.events.PowerEventListener;
import jcs.controller.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.entities.Device;
import jcs.entities.LocomotiveBean;
import jcs.entities.MeasurementChannel;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Direction;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class DccExCommandStationImpl implements CommandStation {

  private CommandStationBean commandStationBean;
  private DccExConnection connection;
  private boolean connected = false;
  private final Map<Integer, Device> devices;
  private Device mainDevice;

  Map<Integer, MeasurementChannel> measurementChannels;

  private final List<PowerEventListener> powerEventListeners;
  private final List<AccessoryEventListener> accessoryEventListeners;
  private final List<SensorEventListener> sensorEventListeners;

  private final List<LocomotiveFunctionEventListener> locomotiveFunctionEventListeners;
  private final List<LocomotiveDirectionEventListener> locomotiveDirectionEventListeners;
  private final List<LocomotiveSpeedEventListener> locomotiveSpeedEventListeners;

  private ExecutorService executor;
  private boolean power;

  private boolean debug = false;

  private int defaultSwitchTime;

  public DccExCommandStationImpl() {
    this(System.getProperty("skip.commandStation.autoconnect", "true").equalsIgnoreCase("true"));
  }

  public DccExCommandStationImpl(boolean autoConnect) {
    devices = new HashMap<>();
    measurementChannels = new HashMap<>();
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    defaultSwitchTime = Integer.getInteger("default.switchtime", 300);

    powerEventListeners = new LinkedList<>();
    sensorEventListeners = new LinkedList<>();
    accessoryEventListeners = new LinkedList<>();

    locomotiveFunctionEventListeners = new LinkedList<>();
    locomotiveDirectionEventListeners = new LinkedList<>();
    locomotiveSpeedEventListeners = new LinkedList<>();

    executor = Executors.newCachedThreadPool();

    //Can't auto connect yet as the commandStation Bean is not set....
    //if (autoConnect) {
    //  connect();
    //}
  }

  @Override
  public CommandStationBean getCommandStationBean() {
    return commandStationBean;
  }

  @Override
  public void setCommandStationBean(CommandStationBean commandStationBean) {
    this.commandStationBean = commandStationBean;
  }

  @Override
  public final boolean connect() {
    if (!this.connected) {
      Logger.trace("Connecting to a DCC-EX Command Station...");
      if (executor == null || executor.isShutdown()) {
        executor = Executors.newCachedThreadPool();
      }

      if (this.commandStationBean == null) {
        Logger.error("No DCC-EX Command Station Configuration set!");
        return false;
      } else {
        Logger.trace("Connect using " + this.commandStationBean.getConnectionType());
      }

      //TODO: can be a little more elegant...
      if (ConnectionType.NETWORK == this.commandStationBean.getConnectionType()) {
        DccExConnectionFactory.writeLastUsedIpAddressProperty(this.commandStationBean.getIpAddress());
      }

      this.connection = DccExConnectionFactory.getConnection(this.commandStationBean.getConnectionType());

      if (connection != null) {
        //Wait, if needed until the receiver thread has started
        long now = System.currentTimeMillis();
        long timeout = now + 1000L;

        while (!connected && now < timeout) {
          connected = connection.isConnected();
          now = System.currentTimeMillis();
        }

        if (connected) {

          JCS.logProgress("Obtaining Device information...");

          DccExMessage deviceInfoRequest = new DccExMessage("<s>");
          connection.sendMessage(deviceInfoRequest);
          Logger.trace("Connected with: " + deviceInfoRequest);

        } else {
          Logger.warn("Can't connect with a DCC-EX Command Station!");
          JCS.logProgress("Can't connect with DCC-EX Command Station!");
        }

      }
    }

    return this.connected;
  }

  @Override
  public boolean isConnected() {
    return this.connection != null && this.connection.isConnected();
  }

  @Override
  public void disconnect() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isPower() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean power(boolean on
  ) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changeDirection(int locUid, Direction direction
  ) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changeVelocity(int locUid, int speed) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchAccessory(int address, AccessoryValue value) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchAccessory(int address, AccessoryValue value, int switchTime) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addPowerEventListener(PowerEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removePowerEventListener(PowerEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addSensorEventListener(SensorEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeSensorEventListener(SensorEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addAccessoryEventListener(AccessoryEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeAccessoryEventListener(AccessoryEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Image getLocomotiveImage(String icon) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<AccessoryBean> getSwitches() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<AccessoryBean> getSignals() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Device getDevice() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Device> getDevices() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void clearCaches() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Map<Integer, MeasurementChannel> getTrackMeasurements() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  // For testing only

  public static void main(String[] a) {

    CommandStationBean csb = new CommandStationBean();
    csb.setId("cs.DccEX.network");
    csb.setName("DCC-EX Network");
    csb.setClassName("jcs.controller.dccex.DccExCommandStationImpl");
    csb.setConnectionSpecifier("NETWORK");
    csb.setIpAddress("192.168.178.73");
    csb.setNetworkPort(2560);
    csb.setDefault(true);
    csb.setAutoIpConfiguration(false);
    csb.setShow(true);

    CommandStation cs = new DccExCommandStationImpl();
    cs.setCommandStationBean(csb);

    cs.connect();

  }

}
