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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.dccex.connection.DccExConnectionFactory;
import jcs.commandStation.dccex.connection.DccExMessageListener;
import jcs.commandStation.dccex.events.CabEvent;
import jcs.commandStation.dccex.events.DccExMeasurementEvent;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.DisconnectionEvent;
import jcs.commandStation.events.DisconnectionEventListener;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.entities.DeviceBean;
import jcs.entities.FunctionBean;
import jcs.entities.InfoBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.util.KeyValuePair;
import jcs.util.SerialPortUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class DccExCommandStationImpl extends AbstractController implements DecoderController, AccessoryController {
  
  private DccExConnection connection;
  private InfoBean infoBean;
  private DeviceBean mainDevice;
  private boolean powerStatusSet = false;
  
  Map<Integer, ChannelBean> measurementChannels;
  
  private int defaultSwitchTime;
  
  public DccExCommandStationImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }
  
  public DccExCommandStationImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    measurementChannels = new HashMap<>();
    defaultSwitchTime = Integer.getInteger("default.switchtime", 300);
    this.executor = Executors.newCachedThreadPool();
    
    if (commandStationBean != null) {
      if (autoConnect) {
        Logger.trace("Perform auto connect");
        connect();
      }
    } else {
      Logger.error("Command Station NOT SET!");
    }
  }
  
  private void initMeasurements() {
    //Prepare the measurements as far as I know DCC-EX has only track current for both PROG and MAIN 
    String response = connection.sendMessage(DccExMessageFactory.trackManagerConfigRequest());
    if (response != null) {
      DccExMeasurementEvent crq = new DccExMeasurementEvent(response);
      handleMeasurement(crq);
    }
    response = connection.sendMessage(DccExMessageFactory.maxCurrentRequest());
    if (response != null) {
      DccExMeasurementEvent mcrq = new DccExMeasurementEvent(response);
      handleMeasurement(mcrq);
    }
    
    response = this.connection.sendMessage(DccExMessageFactory.currentStatusRequest());
    if (response != null) {
      DccExMeasurementEvent csrq = new DccExMeasurementEvent(response);
      handleMeasurement(csrq);
    }
  }
  
  @Override
  public final boolean connect() {
    if (!connected) {
      Logger.trace("Connecting to a DCC-EX Command Station...");
      if (executor == null || executor.isShutdown()) {
        executor = Executors.newCachedThreadPool();
      }
      
      if (commandStationBean == null) {
        Logger.error("No DCC-EX Command Station Configuration set!");
        return false;
      } else {
        Logger.trace("Connect using " + commandStationBean.getConnectionType());
      }
      
      ConnectionType conType = commandStationBean.getConnectionType();
      boolean canConnect = true;
      if (conType == ConnectionType.NETWORK) {
        if (commandStationBean.getIpAddress() != null) {
          DccExConnectionFactory.writeLastUsedIpAddressProperty(commandStationBean.getIpAddress());
        } else {
          canConnect = false;
          Logger.error("Can't connect; IP Address not set");
        }
      }
      
      if (conType == ConnectionType.SERIAL) {
        if (commandStationBean.getSerialPort() != null) {
          DccExConnectionFactory.writeLastUsedSerialPortProperty(commandStationBean.getSerialPort());
        } else {
          canConnect = false;
          Logger.error("Can't connect; ComPort not set");
        }
      }
      
      if (canConnect) {
        connection = DccExConnectionFactory.getConnection(conType);
        
        if (connection != null) {
          long now = System.currentTimeMillis();
          long timeout = now + 5000L;
          
          while (!connected && now < timeout) {
            connected = connection.isConnected();
            now = System.currentTimeMillis();
          }
          if (!connected && now > timeout) {
            Logger.error("Could not establish a connection");
          }
          
          if (connected) {
            DccExMessageListener systemEventListener = new MessageListener(this);
            this.connection.setMessageListener(systemEventListener);

            //When connected to serial DCC-EX flushes a lot of startup messages.
            //DCC-EX will only be receptive for command when all these messages are send.
            //Lats one is the powerstatus
            //With a serial connection de version information is send with the startup messages
            now = System.currentTimeMillis();
            long start = now;
            timeout = now + (conType == ConnectionType.NETWORK ? 200L : 10000L);
            
            while (this.mainDevice == null && now < timeout) {
              pause(100);
              now = System.currentTimeMillis();
            }
            
            if (mainDevice != null) {
              if (debug) {
                Logger.trace("Main Device found in " + (now - start) + " ms");
              }
            } else {
              if (conType == ConnectionType.NETWORK) {
                //When using the net work the DCC-EX does not braodcast all kind of setting so ask them
                JCS.logProgress("Obtaining Device information...");
                String response = connection.sendMessage(DccExMessageFactory.versionHarwareInfoRequest());
                Logger.trace(response);
                
                DccExMessage rsp = new DccExMessage(response);
                
                if ("i".equals(rsp.getOpcode())) {
                  String content = rsp.getFilteredContent();
                  DeviceBean d = new DeviceBean();
                  String[] dccexdev = content.split(" ");
                  d.setName(content);
                  for (int i = 0; i < dccexdev.length; i++) {
                    switch (i) {
                      case 0 ->
                        d.setName(dccexdev[i]);
                      case 1 ->
                        d.setVersion(dccexdev[i]);
                      case 5 ->
                        d.setTypeName(dccexdev[i]);
                      case 6 ->
                        d.setSerial(dccexdev[i]);
                    }
                  }
                  this.mainDevice = d;
                  Logger.trace("Main Device set to: " + d);
                }
                
              }
              if (debug && mainDevice == null) {
                Logger.trace("No Main Device found in " + (now - start) + " ms");
              }
            }

            //Create Info
            this.infoBean = new InfoBean();
            this.infoBean.setProductName(commandStationBean.getDescription());
            this.infoBean.setArticleNumber(commandStationBean.getShortName());
            
            if (conType == ConnectionType.NETWORK) {
              this.infoBean.setHostname(this.commandStationBean.getIpAddress());
            } else {
              this.infoBean.setHostname(this.commandStationBean.getSerialPort());
            }

            //Wait for the power status to be set
            now = System.currentTimeMillis();
            start = now;
            timeout = now + (conType == ConnectionType.NETWORK ? 200L : 5000L);
            
            while (!this.powerStatusSet && now < timeout) {
              pause(50);
              now = System.currentTimeMillis();
            }
            
            if (powerStatusSet) {
              if (debug) {
                Logger.trace("Power Status set in " + (now - start) + " ms");
              }
            } else {
              if (debug) {
                Logger.trace("Power Status not set in " + (now - start) + " ms");
              }
              
              JCS.logProgress("Try to switch Power ON...");
              //Switch one the power
              String response = connection.sendMessage(DccExMessageFactory.changePowerRequest(true));
              Logger.trace(response);
            }
            
            initMeasurements();
            Logger.trace("Connected with: " + (this.mainDevice != null ? this.mainDevice.getName() : "Unknown"));
            JCS.logProgress("Power is " + (this.power ? "On" : "Off"));
          } else {
            Logger.warn("Can't connect with a DCC-EX Command Station!");
            JCS.logProgress("Can't connect with DCC-EX Command Station!");
          }
        }
      }
    }
    return this.connected;
  }
  
  @Override
  public String getIp() {
    return this.commandStationBean.getIpAddress();
  }
  
  public int getDefaultSwitchTime() {
    return defaultSwitchTime;
  }
  
  @Override
  public void disconnect() {
    try {
      if (executor != null) {
        executor.shutdown();
      }
      
      if (connection != null) {
        connection.close();
        connected = false;
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
  public boolean power(boolean on) {
    if (connected) {
      String response = this.connection.sendMessage(DccExMessageFactory.changePowerRequest(on));
      DccExMessage rm = new DccExMessage(response);
      if ("p".equals(rm.getOpcode())) {
        this.power = "1".equals(rm.getFilteredContent());
        PowerEvent pe = new PowerEvent(power);
        fireAllPowerEventListeners(pe);
      }
    }
    return this.power;
  }
  
  @Override
  public void changeDirection(int address, Direction newDirection) {
    if (power && connected) {
      int dir = newDirection.getDccExValue();
      
      String message = DccExMessageFactory.cabChangeSpeedRequest(address, 0, dir);
      this.connection.sendMessage(message);
    }
  }
  
  @Override
  public void changeVelocity(int address, int newSpeed, Direction direction) {
    if (power && connected) {
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

  //Direct approach no feed back...
  //maybe register the accessories and then via id?
  @Override
  public void switchAccessory(Integer address, AccessoryValue value) {
    switchAccessory(address, value, null);
  }
  
  @Override
  public void switchAccessory(Integer address, AccessoryValue value, Integer switchTime) {
    if (this.power) {
      String activate = AccessoryValue.GREEN == value ? "0" : "1";
      String message = DccExMessageFactory.activateAccessory(address, activate);
      this.connection.sendMessage(message);
      
      AccessoryBean ab = new AccessoryBean();
      ab.setAddress(address);
      ab.setAccessoryValue(value);
      String id = address + "";
      if (id.length() == 1) {
        id = "00" + id;
      } else if (id.length() == 2) {
        id = "0" + id;
      }
      ab.setId(id);
      ab.setCommandStationId(this.commandStationBean.getId());
      
      AccessoryEvent ae = new AccessoryEvent(ab);
      notifyAccessoryEventListeners(ae);
    }
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
  public List<AccessoryBean> getAccessories() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public DeviceBean getDevice() {
    return this.mainDevice;
  }
  
  @Override
  public List<DeviceBean> getDevices() {
    List<DeviceBean> devices = new ArrayList<>();
    devices.add(this.mainDevice);
    return devices;
  }
  
  @Override
  public InfoBean getCommandStationInfo() {
    return this.infoBean;
  }
  
  @Override
  public Map<Integer, ChannelBean> getTrackMeasurements() {
    //Measure the currents
    this.connection.sendMessage(DccExMessageFactory.currentStatusRequest());
    ConnectionType conType = commandStationBean.getConnectionType();
    long millis = (conType == ConnectionType.SERIAL?500:50);
    pause(millis);
    return this.measurementChannels;
  }
  
  private void fireAllDisconnectionEventListeners(final DisconnectionEvent disconnectionEvent) {
    for (DisconnectionEventListener listener : this.disconnectionEventListeners) {
      listener.onDisconnect(disconnectionEvent);
    }
    disconnect();
  }
  
  private void notifyPowerEventListeners(final PowerEvent powerEvent) {
    this.power = powerEvent.isPower();
    if (!powerStatusSet) {
      powerStatusSet = true;
    }
    
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
  
  private void notifyAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    executor.execute(() -> fireAllAccessoryEventListeners(accessoryEvent));
  }

  /**
   * The Command echo of the DCC-EX protocol results is 3 different events:<br>
   * - Loco Speed Event<br>
   * - Loco Direction Event<br>
   * - Loco Function Events (1 event for each function)
   *
   * @param cabEvent the cabEvent
   */
  private void fireLocomotiveEventListeners(final CabEvent cabEvent) {
    LocomotiveBean lb = cabEvent.getLocomotiveBean();
    Logger.trace("Loc id: " + lb.getId() + " address: " + lb.getAddress() + " Velocity: " + lb.getVelocity() + " Direction: " + lb.getDirection());
    
    LocomotiveDirectionEvent directionEvent = new LocomotiveDirectionEvent(lb);
    Logger.trace("Notifing " + this.locomotiveDirectionEventListeners.size() + " loc direction listeners with Loc: " + lb.getId() + " address: " + lb.getAddress() + " Direction: " + lb.getDirection());
    for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
      listener.onDirectionChange(directionEvent);
    }
    
    LocomotiveSpeedEvent speedEvent = new LocomotiveSpeedEvent(lb);
    Logger.trace("Notifing " + this.locomotiveSpeedEventListeners.size() + " loc speed listeners with Loc: " + lb.getId() + " address: " + lb.getAddress() + " Velocity: " + lb.getVelocity());
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
  
  private void fireAllAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    for (AccessoryEventListener listener : this.accessoryEventListeners) {
      listener.onAccessoryChange(accessoryEvent);
    }
  }
  
  private void handleMeasurement(DccExMeasurementEvent measurementEvent) {
    if ("=".equals(measurementEvent.getOpcode())) {
      // config
      KeyValuePair track = measurementEvent.getTrack();
      if (track != null) {
        if ("A".equals(track.getKey())) {
          //Main, or channel 1
          ChannelBean main = this.measurementChannels.get(1);
          if (main == null) {
            main = new ChannelBean();
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
          ChannelBean prog = this.measurementChannels.get(0);
          if (prog == null) {
            prog = new ChannelBean();
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
      } else {
        Logger.trace("No 'Track' available");
      }
    } else if ("j".equals(measurementEvent.getOpcode())) {
      if (measurementEvent.isMeasurement()) {
        ChannelBean main = this.measurementChannels.get(1);
        if (main == null) {
          main = new ChannelBean();
          measurementChannels.put(1, main);
        }
        main.setValue(measurementEvent.getCurrentMain());
        main.setHumanValue((double) measurementEvent.getCurrentMain());
        
        ChannelBean prog = this.measurementChannels.get(0);
        if (prog == null) {
          prog = new ChannelBean();
          measurementChannels.put(0, prog);
        }
        prog.setValue(measurementEvent.getCurrentProg());
        prog.setHumanValue((double) measurementEvent.getCurrentProg());
      } else {
        ChannelBean main = this.measurementChannels.get(1);
        if (main == null) {
          main = new ChannelBean();
          measurementChannels.put(1, main);
        }
        main.setRangeMax(measurementEvent.getCurrentMainMax());
        main.setEndValue((double) measurementEvent.getCurrentMainMax());
        
        ChannelBean prog = this.measurementChannels.get(0);
        if (prog == null) {
          prog = new ChannelBean();
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

//    private static int getIntValue(String s) {
//      int r = -1;
//      if (s != null && s.length() > 0) {
//        Integer.parseUnsignedInt(s);
//      }
//      return r;
//    }
    @Override
    public void onMessage(DccExMessage message) {
      try {
        if (message.isValid()) {
          String opcode = "";
          String content;
          if (message.isDiagnosticMessage()) {
            content = message.getFilteredDiagnosticMessage();
          } else {
            opcode = message.getOpcode();
            content = message.getFilteredContent();
          }
          
          if (debug) {
            if (message.isDiagnosticMessage()) {
              Logger.trace("D mse: " + content);
            } else {
              Logger.trace("Opcode: " + opcode + " Content: " + content);
            }
          }
          
          switch (opcode) {
            case "p" -> {
              // Power on/off response. The character right after the opcode represents the power state
              boolean power = "1".equals(content);
              PowerEvent pe = new PowerEvent(power);
              this.commandStation.notifyPowerEventListeners(pe);
            }
            case "i" -> {
              // System information
              DeviceBean d = new DeviceBean();
              String[] dccexdev = content.split(" ");
              d.setName(content);
              for (int i = 0; i < dccexdev.length; i++) {
                switch (i) {
                  case 0 ->
                    d.setName(dccexdev[i]);
                  case 1 ->
                    d.setVersion(dccexdev[i]);
                  case 5 ->
                    d.setTypeName(dccexdev[i]);
                  case 6 ->
                    d.setSerial(dccexdev[i]);
                }
              }
              this.commandStation.mainDevice = d;
              Logger.trace("Main Device set to: " + d);
            }
            case "c" -> {
            }
            case "l" -> {
              // Locomotive changed.
              CabEvent ce = new CabEvent(content, this.commandStation.getCommandStationBean().getId());
              this.commandStation.notifyLocomotiveEventListeners(ce);
            }
            case "=" -> {
              DccExMeasurementEvent me1 = new DccExMeasurementEvent(opcode, content);
              this.commandStation.handleMeasurement(me1);
            }
            case "j" -> {
              DccExMeasurementEvent me2 = new DccExMeasurementEvent(opcode, content);
              this.commandStation.handleMeasurement(me2);
            }
            case "H" -> {
            }
            case "q" -> {
            }
            case "Q" -> {
            }
            case "Y" -> {
            }
          }
          // Track current
          // Turnout response. TODO: obtain the parameters
          // Sensor/Input: ACTIVE to INACTIVE
          // Sensor/Input: INACTIVE to ACTIVE
          // Output response
        }
        
      } catch (Exception e) {
        Logger.trace("Error in Message: '" + message + "' -> " + e.getMessage());
      }
    }
    
    @Override
    public void onDisconnect(DisconnectionEvent event) {
      this.commandStation.fireAllDisconnectionEventListeners(event);
    }
    
  }

//////////////////////////////////////////////////////////////////////////////////////  
  // For testing only
  public static void main(String[] a) {
    
    System.setProperty("message.debug", "true");
    
    SerialPortUtil.logComports();
    
    if (1 == 1) {
      CommandStationBean csb = new CommandStationBean();
      csb.setId("dcc-ex");
      csb.setDescription("DCC-EX");
      csb.setClassName("jcs.commandStation.dccex.DccExCommandStationImpl");
      //csb.setConnectVia("NETWORK");
      //csb.setIpAddress("192.168.178.73");
      csb.setNetworkPort(2560);
      csb.setConnectVia("SERIAL");
      csb.setSerialPort("cu.usbmodem14101");
      //csb.setSerialPort("cu.usbmodem14201");

      csb.setDefault(true);
      csb.setIpAutoConfiguration(false);
      csb.setEnabled(true);
      csb.setShortName("dcc-ex");
      csb.setDecoderControlSupport(true);
      csb.setAccessorySynchronizationSupport(false);
      csb.setFeedbackSupport(false);
      csb.setLocomotiveFunctionSynchronizationSupport(false);
      csb.setLocomotiveImageSynchronizationSupport(false);
      csb.setLocomotiveSynchronizationSupport(false);
      csb.setProtocols("DCC");

      //CommandStation cs = CommandStationFactory.getCommandStation(csb);
      DccExCommandStationImpl cs = new DccExCommandStationImpl(csb);
      
      cs.connect();

      //((DccExCommandStationImpl) cs).pause(500L);
      //cs.power(true);
      Logger.trace("Power is: " + (cs.isPower() ? "On" : "Off"));

//      ((DccExCommandStationImpl) cs).pause(500L);
//      cs.power(false);
//      Logger.trace("Power is: " + (cs.isPower() ? "On" : "Off"));
//      
//      ((DccExCommandStationImpl) cs).pause(500L);
//      cs.power(true);
//      Logger.trace("Power is: " + (cs.isPower() ? "On" : "Off"));
      /////
      //((DccExCommandStationImpl) cs).pause(500L);
//      cs.changeFunctionValue(8, 0, true);
//      
//      ((DccExCommandStationImpl) cs).pause(1500L);
//      
//      cs.changeFunctionValue(8, 1, true);
//      
//      ((DccExCommandStationImpl) cs).pause(1500L);
//      cs.changeFunctionValue(8, 1, false);
//      cs.switchAccessory(2, AccessoryValue.RED);
//      ((DccExCommandStationImpl) cs).pause(500L);
//      cs.switchAccessory(2, AccessoryValue.GREEN);
//      ((DccExCommandStationImpl) cs).pause(500L);
//      cs.switchAccessory(2, AccessoryValue.RED);
//      ((DccExCommandStationImpl) cs).pause(500L);
//      cs.switchAccessory(2, AccessoryValue.GREEN);
//      ((DccExCommandStationImpl) cs).pause(500L);
//      cs.switchAccessory(2, AccessoryValue.RED);
// 
//      //Check the measurements
//      ((DccExCommandStationImpl) cs).pause(1500L);
//      
//      Logger.trace("#### Measurements....");
//      
//      cs.getTrackMeasurements();
//      
//      while (1 == 1) {
//        try {
//          Thread.sleep(1000);
//        } catch (InterruptedException e) {
//          
//        }
//      }
//    } else {
    }
    
  }
  
}
