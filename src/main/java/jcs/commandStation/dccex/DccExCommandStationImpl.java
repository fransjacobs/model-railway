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
package jcs.commandStation.dccex;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.CommandStation;
import jcs.commandStation.dccex.connection.DccExConnectionFactory;
import jcs.commandStation.dccex.events.CabEvent;
import jcs.commandStation.dccex.events.DccExMeasurementEvent;
import jcs.commandStation.dccex.events.DccExMessageListener;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.entities.Device;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.MeasurementChannel;
import jcs.entities.enums.AccessoryValue;
import jcs.util.KeyValuePair;
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
  }

  @Override
  public CommandStationBean getCommandStationBean() {
    return commandStationBean;
  }

  @Override
  public void setCommandStationBean(CommandStationBean commandStationBean) {
    this.commandStationBean = commandStationBean;
  }

  private void initMeasurements() {
    //Prepare the measurements as fa as I know DCC-EX has only track curren fro both PROG and MAIN 
    this.connection.sendMessage(DccExMessageFactory.trackManagerConfigRequest());
    this.connection.sendMessage(DccExMessageFactory.maxCurrentRequest());

    this.connection.sendMessage(DccExMessageFactory.currentStatusRequest());
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
      if (ConnectionType.NETWORK == this.commandStationBean.getConnectionType() && this.commandStationBean.getIpAddress() != null) {
        DccExConnectionFactory.writeLastUsedIpAddressProperty(this.commandStationBean.getIpAddress());
      } else {
        Logger.error("Can't connect; IP Address not set");
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

          initMeasurements();

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
  public void changeVelocity(int address, int newSpeed, Direction direction) {
    if (this.power) {
      int dir = direction.getDccExValue();
      //Scale the speed 0 == 0 1024 is max, DCC max is 0 128 roughly so divided by 8
      int speed = newSpeed / 8;

      String message = DccExMessageFactory.cabChangeSpeedRequest(address, speed, dir);
      this.connection.sendMessage(message);
    }
  }

  @Override
  public void changeFunctionValue(int address, int functionNumber, boolean flag) {
    if (this.power) {
      String message = DccExMessageFactory.cabChangeFunctionsRequest(address, functionNumber, flag);
      this.connection.sendMessage(message);
    }
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
    this.sensorEventListeners.add(listener);
  }

  @Override
  public void removeSensorEventListener(SensorEventListener listener) {
    this.sensorEventListeners.remove(listener);
  }

  @Override
  public void addAccessoryEventListener(AccessoryEventListener listener) {
    this.accessoryEventListeners.add(listener);
  }

  @Override
  public void removeAccessoryEventListener(AccessoryEventListener listener) {
    this.accessoryEventListeners.remove(listener);
  }

  @Override
  public void addLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    this.locomotiveFunctionEventListeners.add(listener);
  }

  @Override
  public void removeLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    this.locomotiveFunctionEventListeners.remove(listener);
  }

  @Override
  public void addLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    this.locomotiveDirectionEventListeners.add(listener);
  }

  @Override
  public void removeLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    this.locomotiveDirectionEventListeners.remove(listener);
  }

  @Override
  public void addLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    this.locomotiveSpeedEventListeners.add(listener);
  }

  @Override
  public void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    this.locomotiveSpeedEventListeners.remove(listener);
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
    //Measure the currents
    this.connection.sendMessage(DccExMessageFactory.currentStatusRequest());
    this.pause(50);
    return this.measurementChannels;
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

  private void notifyLocomotiveEventListeners(final CabEvent cabEvent) {
    executor.execute(() -> fireLocomotiveEventListeners(cabEvent));
  }

  /**
   * The Command echo of the DCC-EX protocol results is 3 different events: - Loco Speed Event - Loco Direction Event - Loco Function Events (1 event for each function)
   *
   * @param cabEvent the cabEvent
   */
  private void fireLocomotiveEventListeners(final CabEvent cabEvent) {
    LocomotiveBean lb = cabEvent.getLocomotiveBean();
    Logger.trace("Loc: " + lb.getId() + " Velocity: " + lb.getVelocity() + " Direction: " + lb.getDirection());

    LocomotiveDirectionEvent directionEvent = new LocomotiveDirectionEvent(lb);

    for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
      listener.onDirectionChange(directionEvent);
    }

    LocomotiveSpeedEvent speedEvent = new LocomotiveSpeedEvent(lb);

    for (LocomotiveSpeedEventListener listener : this.locomotiveSpeedEventListeners) {
      listener.onSpeedChange(speedEvent);
    }

    List<FunctionBean> functions = cabEvent.getFunctionBeans();
    for (FunctionBean fb : functions) {
      LocomotiveFunctionEvent functionEvent = new LocomotiveFunctionEvent(fb);
      for (LocomotiveFunctionEventListener listener : this.locomotiveFunctionEventListeners) {
        listener.onFunctionChange(functionEvent);
      }
    }
  }

  private void handleMeasurement(DccExMeasurementEvent measurementEvent) {
    if ("=".equals(measurementEvent.getOpcode())) {
      // config
      KeyValuePair track = measurementEvent.getTrack();
      if ("A".equals(track.getKey())) {
        //Main, or channel 1
        MeasurementChannel main = this.measurementChannels.get(1);
        if (main == null) {
          main = new MeasurementChannel();
          measurementChannels.put(1, main);
        }
        main.setName(track.getValue());
        main.setNumber(1);
        main.setUnit("mA");
        main.setStartValue(0.0);
        main.setScale(1000);
        main.setHumanValue(0.0);
        main.setValue(0);
      } else if ("B".equals(track.getKey())) {
        //Prog, or channel 0
        MeasurementChannel prog = this.measurementChannels.get(0);
        if (prog == null) {
          prog = new MeasurementChannel();
          measurementChannels.put(0, prog);
        }
        prog.setName(track.getValue());
        prog.setNumber(0);
        prog.setUnit("mA");
        prog.setStartValue(0.0);
        prog.setScale(1000);
        prog.setHumanValue(0.0);
        prog.setValue(0);
      }
    } else if ("j".equals(measurementEvent.getOpcode())) {
      if (measurementEvent.isMeasurement()) {
        MeasurementChannel main = this.measurementChannels.get(1);
        if (main == null) {
          main = new MeasurementChannel();
          measurementChannels.put(1, main);
        }
        main.setValue(measurementEvent.getCurrentMain());
        main.setHumanValue((double) measurementEvent.getCurrentMain());

        MeasurementChannel prog = this.measurementChannels.get(0);
        if (prog == null) {
          prog = new MeasurementChannel();
          measurementChannels.put(0, prog);
        }
        prog.setValue(measurementEvent.getCurrentProg());
        prog.setHumanValue((double) measurementEvent.getCurrentProg());
      } else {
        MeasurementChannel main = this.measurementChannels.get(1);
        if (main == null) {
          main = new MeasurementChannel();
          measurementChannels.put(1, main);
        }
        main.setRangeMax(measurementEvent.getCurrentMainMax());
        main.setEndValue((double) measurementEvent.getCurrentMainMax());

        MeasurementChannel prog = this.measurementChannels.get(0);
        if (prog == null) {
          prog = new MeasurementChannel();
          measurementChannels.put(0, prog);
        }
        prog.setRangeMax(measurementEvent.getCurrentProgMax());
        prog.setEndValue((double) measurementEvent.getCurrentProgMax());
      }
    }
  }

  private class MessageListener implements DccExMessageListener {

    private final DccExCommandStationImpl commandStation;

    MessageListener(DccExCommandStationImpl commandStation) {
      this.commandStation = commandStation;
    }

    private static int getIntValue(String s) {
      int r = -1;
      if (s != null && s.length() > 0) {
        Integer.parseUnsignedInt(s);
      }
      return r;
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
            // Locomotive changed.
            CabEvent ce = new CabEvent(content);
            this.commandStation.notifyLocomotiveEventListeners(ce);
            break;
          case "=":
            DccExMeasurementEvent me1 = new DccExMeasurementEvent(opcode, content);
            this.commandStation.handleMeasurement(me1);
            break;
          case "j":
            DccExMeasurementEvent me2 = new DccExMeasurementEvent(opcode, content);
            this.commandStation.handleMeasurement(me2);
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

    if (1 == 1) {
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

      //CommandStation cs = CommandStationFactory.getCommandStation(csb);
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

      /////
      ((DccExCommandStationImpl) cs).pause(500L);
      cs.changeFunctionValue(8, 0, true);

      ((DccExCommandStationImpl) cs).pause(1500L);

      cs.changeFunctionValue(8, 1, true);

      ((DccExCommandStationImpl) cs).pause(1500L);
      cs.changeFunctionValue(8, 1, false);

      //Check the measurements
      ((DccExCommandStationImpl) cs).pause(1500L);

      Logger.trace("#### Measurements....");

      cs.getTrackMeasurements();

      while (1 == 1) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
      }
    } else {
    }

  }

}
