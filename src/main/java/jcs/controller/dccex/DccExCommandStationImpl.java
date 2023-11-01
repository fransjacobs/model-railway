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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.controller.CommandStation;
import jcs.controller.dccex.connection.DccExConnectionFactory;
import jcs.controller.dccex.events.DccExMessageListener;
import jcs.controller.events.AccessoryEventListener;
import jcs.controller.events.LocomotiveDirectionEventListener;
import jcs.controller.events.LocomotiveFunctionEventListener;
import jcs.controller.events.LocomotiveSpeedEventListener;
import jcs.controller.events.PowerEvent;
import jcs.controller.events.PowerEventListener;
import jcs.controller.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.entities.Device;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.MeasurementChannel;
import jcs.entities.enums.AccessoryValue;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class DccExCommandStationImpl implements CommandStation {

  private CommandStationBean commandStationBean;
  private DccExConnection connection;
  private boolean connected = false;
  //private final Map<Integer, Device> devices;
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
    //devices = new HashMap<>();
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
          DccExMessageListener systemEventListener = new MessageListener(this);
          this.connection.setMessageListener(systemEventListener);

          JCS.logProgress("Obtaining Device information...");
          connection.sendMessage(DccExMessageFactory.versionHarwareInfoRequest());

          //Wait a while to give the Command Station time to answer...
          pause(200);
          Logger.trace("Connected with: " + (this.mainDevice != null ? this.mainDevice.getName() : "Unknown"));
          JCS.logProgress("Power is " + (this.power ? "On" : "Off"));
        } else {
          Logger.warn("Can't connect with a DCC-EX Command Station!");
          JCS.logProgress("Can't connect with DCC-EX Command Station!");
        }
      }
    }

    return this.connected;
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

  @Override
  public boolean isConnected() {
    return this.connection != null && this.connection.isConnected();
  }

  @Override
  public void disconnect() {
    try {
      if (connection != null) {
        connection.close();
        connected = false;
      }

      if (executor != null) {
        executor.shutdown();
      }
      executor = null;
      connection = null;

      DccExConnectionFactory.disconnectAll();
    } catch (Exception ex) {
      Logger.error(ex);
    }
    Logger.trace("Disconnected");
  }

  @Override
  public boolean isPower() {
    return this.power;
  }

  @Override
  public boolean power(boolean on) {
    if (connected) {
      this.connection.sendMessage(DccExMessageFactory.changePowerRequest(on));
      //Let the Commandstation process the message
      pause(20);
    }
    return this.power;
  }

  @Override
  public void changeDirection(int address, Direction newDirection) {
    if (this.power) {
      int dir = newDirection.getDccExValue();

      String message = DccExMessageFactory.cabChangeSpeedRequest(address, 0, dir);
      this.connection.sendMessage(message);
    }
  }

  @Override
  public void changeVelocity(int address, int speed, Direction direction) {
    if (this.power) {
      int dir = direction.getDccExValue();

      String message = DccExMessageFactory.cabChangeSpeedRequest(address, 0, dir);
      this.connection.sendMessage(message);
    }
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
    this.powerEventListeners.add(listener);
  }

  @Override
  public void removePowerEventListener(PowerEventListener listener) {
    this.powerEventListeners.remove(listener);
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
    return this.mainDevice;
  }

  @Override
  public List<Device> getDevices() {
    List<Device> devices = new ArrayList<>();
    devices.add(this.mainDevice);
    return devices;
  }

  @Override
  public void clearCaches() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Map<Integer, MeasurementChannel> getTrackMeasurements() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private void notifyPowerEventListeners(final PowerEvent powerEvent) {
    this.power = powerEvent.isPower();
    executor.execute(() -> fireAllPowerEventListeners(powerEvent));
  }

  private void fireAllPowerEventListeners(final PowerEvent powerEvent) {
    this.power = powerEvent.isPower();
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }

  private class MessageListener implements DccExMessageListener {

    private final DccExCommandStationImpl commandStation;

    MessageListener(DccExCommandStationImpl commandStation) {
      this.commandStation = commandStation;
    }

    @Override
    public void onMessage(String message) {

      if (message.length() > 1 && message.startsWith("<")) {
        String opcode = message.substring(1, 2);
        String content = DccExMessage.filterContent(message);

        if (debug) {
          Logger.trace("Opcode: " + opcode + " Content: " + content);
        }

        switch (opcode) {
          case "p":
            // Power on/off response. The character right after the opcode represents the power state
            boolean power = "1".equals(content);
            PowerEvent pe = new PowerEvent(power);
            this.commandStation.notifyPowerEventListeners(pe);
            break;
          case "i":
            // System information
            Device d = new Device();
            String[] dccexdev = content.split(" ");
            d.setName(content);
            for (int i = 0; i < dccexdev.length; i++) {
              switch (i) {
                case 0 ->
                  d.setDeviceName(dccexdev[i]);
                case 1 ->
                  d.setVersion(dccexdev[i]);
                case 5 ->
                  d.setTypeName(dccexdev[i]);
                case 6 ->
                  d.setSerialNumber(dccexdev[i]);
              }
            }
            this.commandStation.mainDevice = d;
            break;
          case "c":
            // Track current
            break;
          case "l":
            // Locomotive changed. TODO: parameters
            break;
          case "=":
            // Locomotive changed. TODO: parameters
            break;
          case "H":
            // Turnout response. TODO: obtain the parameters
            break;
          case "q":
            // Sensor/Input: ACTIVE to INACTIVE
            break;
          case "Q":
            // Sensor/Input: INACTIVE to ACTIVE
            break;
          case "Y":
            // Output response
            break;
        }
      }
    }
  }

//////////////////////////////////////////////////////////////////////////////////////  
  // For testing only
  public static void main(String[] a) {

    System.setProperty("message.debug", "true");

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

    ((DccExCommandStationImpl) cs).pause(500L);

    cs.power(true);
    Logger.trace("Power is: " + (cs.isPower() ? "On" : "Off"));

    ((DccExCommandStationImpl) cs).pause(500L);
    cs.power(false);
    Logger.trace("Power is: " + (cs.isPower() ? "On" : "Off"));

    ((DccExCommandStationImpl) cs).pause(500L);
    cs.power(true);
    Logger.trace("Power is: " + (cs.isPower() ? "On" : "Off"));

    while (1 == 1) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {

      }
    }

  }

}
