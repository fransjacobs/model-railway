/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.commandStation.marklin.cs;

import jcs.commandStation.marklin.cs.can.device.CanDevice;
import jcs.commandStation.marklin.cs.can.parser.FeedbackEventMessage;
import java.awt.Image;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TransferQueue;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.CanMessageFactory;
import static jcs.commandStation.marklin.cs.can.CanMessageFactory.getStatusDataConfigResponse;
import jcs.commandStation.marklin.cs.can.parser.MessageInflator;
import jcs.commandStation.marklin.cs.can.parser.SystemStatus;
import jcs.commandStation.marklin.cs.net.CSConnection;
import jcs.commandStation.marklin.cs.net.CSConnectionFactory;
import jcs.commandStation.marklin.cs2.AccessoryBeanParser;
import jcs.commandStation.marklin.cs2.LocomotiveBeanParser;
import jcs.commandStation.marklin.cs3.FunctionSvgToPngConverter;
import jcs.commandStation.marklin.cs3.LocomotiveBeanJSONParser;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.CommandStationBean;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.marklin.cs.can.parser.AccessoryMessage;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.marklin.cs2.LocomotiveDirectionEventParser;
import jcs.commandStation.marklin.cs2.LocomotiveFunctionEventParser;
import jcs.commandStation.marklin.cs.can.parser.LocomotiveVelocityMessage;
import jcs.commandStation.marklin.cs.can.parser.PowerEventParser;
import jcs.commandStation.VirtualConnection;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.DriveSimulator;
import jcs.commandStation.entities.Device;
import jcs.commandStation.entities.MeasuredChannels;
import jcs.commandStation.entities.MeasurementBean;
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.marklin.cs.can.device.ConfigChannel;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import jcs.commandStation.marklin.parser.CanDeviceParser;
import jcs.commandStation.marklin.cs.can.parser.LocomotiveEmergencyStopMessage;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.SensorBean;
import jcs.util.RunUtil;
import org.tinylog.Logger;
import jcs.commandStation.marklin.cs.net.CSHTTPConnection;
import jcs.commandStation.marklin.parser.GeraetParser;
import jcs.commandStation.marklin.parser.SystemStatusMessage;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.MeasurementEventListener;
import jcs.commandStation.marklin.cs.can.parser.OverloadEventParser;
import jcs.commandStation.marklin.cs.can.parser.SensorStatusQuery;
import jcs.commandStation.marklin.parser.CanDeviceJSONParser;
import jcs.util.Ping;

/**
 * Command Station Implementation for Marklin CS-2/3
 */
public class MarklinCentralStationImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController, ConnectionEventListener {

  private CSConnection connection;

  private InfoBean infoBean;
  private Map<Integer, CanDevice> canDevices;

  private int csUid;
  private int linkS88Uid;
  private EventMessageHandler eventMessageHandler;

  private DriveSimulator simulator;

  private Long canBootLoaderLastCallMillis;
  private WatchdogTask watchdogTask;
  private Timer watchDogTimer;

  private MeasurementTask measurementTask;
  private Timer measurementTimer;

  private SortedMap<Long, MeasuredChannels> measuredValues;

  public MarklinCentralStationImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public MarklinCentralStationImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    canDevices = new HashMap<>();
    measuredValues = new ConcurrentSkipListMap<>();

    if (commandStationBean != null) {
      if (autoConnect) {
        Logger.trace("Perform auto connect");
        connect();
      }
    } else {
      Logger.error("Command Station NOT SET!");
    }
  }

  int getCsUid() {
    return csUid;
  }

  boolean isCS3() {
    if (infoBean != null && infoBean.getArticleNumber() != null) {
      return "60216".equals(infoBean.getArticleNumber()) || "60226".equals(infoBean.getArticleNumber());
    } else {
      return false;
    }
  }

  @Override
  public String getIp() {
    return CSConnectionFactory.getControllerIp();
  }

  @Override
  public void setVirtual(boolean flag) {
    this.virtual = flag;
    Logger.info("Switching Virtual Mode " + (flag ? "On" : "Off"));
    disconnect();
    connect();
  }

  CanDevice getCanDevice(String name) {
    for (CanDevice d : canDevices.values()) {
      if (name.equals(d.getName())) {
        return d;
      }
    }
    return null;
  }

  @Override
  public final synchronized boolean connect() {
    if (!connected) {
      Logger.trace("Connecting to a " + (virtual ? "Virtual " : "") + "Central Station " + (commandStationBean != null ? commandStationBean.getDescription() : "Unknown"));

      if (executor == null || executor.isShutdown()) {
        executor = Executors.newCachedThreadPool();
      }

      if (commandStationBean == null) {
        Logger.error("Marklin Command Station Configuration NOT set!");
        return false;
      } else {
        Logger.trace("Connect using " + commandStationBean.getConnectionType());
      }

      CommandStationBean.ConnectionType conType = commandStationBean.getConnectionType();

      boolean canConnect;
      if (conType == CommandStationBean.ConnectionType.NETWORK) {
        try {
          InetAddress csAddr;
          if (virtual) {
            csAddr = InetAddress.getLocalHost();
          } else {
            csAddr = InetAddress.getByName(commandStationBean.getIpAddress());
          }
          commandStationBean.setIpAddress(csAddr.getHostAddress());
          canConnect = csAddr.getHostAddress() != null;
        } catch (UnknownHostException ex) {
          Logger.error("Invalid ip address : " + commandStationBean.getIpAddress());
          return false;
        }
      } else {
        if (virtual) {
          canConnect = true;
        } else {
          canConnect = Ping.IsReachable(commandStationBean.getIpAddress());
        }
      }

      if (!canConnect) {
        Logger.error("Can't connect to " + (commandStationBean.getIpAddress() == null ? "ip Address not set" : "can't reach ip " + commandStationBean.getIpAddress()));
        return false;
      }

      connection = CSConnectionFactory.getConnection(commandStationBean);

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
          CanDevice gfp = getGFP();
          canDevices.put(gfp.getUidInt(), gfp);
          csUid = gfp.getUidInt();

          canBootLoaderLastCallMillis = System.currentTimeMillis();

          JCS.logProgress("Obtaining Device information...");
          if (virtual) {
            List<CanDevice> devices = getCS3Devices();
            for (CanDevice device : devices) {
              if (CanDeviceJSONParser.GFP.equals(device.getName())) {
                canDevices.put(device.getUidInt(), device);
              }
            }
          } else {
            csUid = Integer.parseUnsignedInt(gfp.getUid(), 16);
            obtainDevices();
          }

          //The eventMessageHandler Thread is in charge to handle all event messages which are send from the CS to JCS
          eventMessageHandler = new EventMessageHandler(connection);
          eventMessageHandler.start();

          connection.addDisconnectionEventListener(this);
          startWatchdog();

          power = isPower();
          JCS.logProgress("Power is " + (power ? "On" : "Off"));

//          if (gfp.getMeasureChannelCount() != null && gfp.getMeasureChannelCount() > 0) {
//            Logger.trace("Measurements are possible...");
//          } 
//          else {
//            queryDevice(gfp);
//            Logger.trace("GFP Measurement Count: " + gfp.getMeasureChannelCount());
//          }
          startMeasurements();

          Logger.trace("Connected to " + gfp.getName() + ", " + gfp.getArticleNumber() + " SerialNumber: " + gfp.getSerial());
        }
      } else {
        Logger.warn("Can't connect with Central Station!");
        JCS.logProgress("Can't connect with Central Station!");
      }
    }

    if (isVirtual()) {
      simulator = new DriveSimulator();
      Logger.info("Marklin Central Station Virtual Mode Enabled!");
    }

    return connected;
  }

  //The device information can be retrieved via CAN, but using a shortcut via http goes much faster.
  //The Central station has a "geraet.vrs" file which can be retrieved via HTTP.
  //Based on the info in this file it is quicker to know whether the CS is a version 2 or 3.
  //In case of a CS-3 the information can be retrieved via JSON else use CAN
  CanDevice getGFP() {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    String geraet = httpCon.getInfoFile();
    CanDevice gfp = GeraetParser.parseFile(geraet);
    return gfp;
  }

  InfoBean createInfoBean(Map<Integer, CanDevice> canDevices) {
    InfoBean ib = new InfoBean(commandStationBean);
    if (connection != null && connection.getControllerAddress() != null) {
      ib.setIpAddress(connection.getControllerAddress().getHostAddress());
    }

    for (CanDevice d : canDevices.values()) {
      Logger.trace("Checking device: " + d);
      String name = d.getName();
      if (name == null) {
        name = "null";
      }
      switch (name) {
        case "Central Station 3" -> {
          ib.setGfpUid(d.getUid());
          //String uid = d.getUid();
          //uid = uid.replace("0x", "");
          //csUid = Integer.parseUnsignedInt(uid, 16);
          Logger.trace("GFP uid: " + d.getUid() + " -> " + csUid);

          ib.setArticleNumber(d.getArticleNumber());
          ib.setProductName(d.getName());
          ib.setSerialNumber(d.getSerial());
          ib.setHardwareVersion(d.getVersion());
          //ib.setSupportMeasurements(d.getMeasureChannelCount() > 0);

          //TODO: Only CS 2 and CS3 plus...?
          //ib.setFeedbackBus0ModuleCount(0);
          //ib.setFeedbackSupport(true);
          //Is the System property still needed?
          //if (System.getProperty("cs.article") == null) {
          //System.setProperty("cs.article", mainDevice.getArticleNumber());
          //System.setProperty("cs.serial", mainDevice.getSerial());
          //System.setProperty("cs.name", mainDevice.getName());
          //System.setProperty("cs.cs3", (isCS3() ? "true" : "false"));
          //}
        }
        case "GFP3-1" -> {
          //Virtual
          ib.setGfpUid(d.getUid());
          Logger.trace("GFP uid: " + d.getUid() + " -> " + csUid);

          ib.setArticleNumber(d.getArticleNumber());
          ib.setProductName(d.getName());
          ib.setSerialNumber(d.getSerial());
          ib.setHardwareVersion(d.getVersion());
          //ib.setSupportMeasurements(d.getMeasureChannelCount() > 0);
        }
        case "Link S88" -> {
          ib.setFeedbackSupport(true);
          linkS88Uid = d.getUidInt();
          ConfigChannel bus1 = d.getConfigChannel(2);
          ConfigChannel bus2 = d.getConfigChannel(3);
          ConfigChannel bus3 = d.getConfigChannel(4);

          ib.setFeedbackBus1ModuleCount(bus1.getActualValue());
          ib.setFeedbackBus2ModuleCount(bus2.getActualValue());
          ib.setFeedbackBus3ModuleCount(bus3.getActualValue());
        }
        case "CS2/3-GUI (Master)" -> {
          ib.setSoftwareVersion(d.getVersion());
        }
        default -> {
          Logger.info("A yet unknown CAN Device " + d);
        }
      }
    }
    return ib;
  }

  /**
   * Obtain information about the connected CAN Device in the Central Station
   */
  void obtainDevices() {
    CanMessage msg = CanMessageFactory.getMembersPing();
    connection.sendCanMessage(msg);

    List<CanDevice> devices = CanDeviceParser.parse(msg);
    Logger.trace("Found " + devices.size() + " CANDevices");
    for (CanDevice d : devices) {
      if (!canDevices.containsKey(d.getUidInt())) {
        canDevices.put(d.getUidInt(), d);
      } else {
        CanDevice ed = canDevices.get(d.getUidInt());
        if (d.getSerial() != null) {
          ed.setSerial(d.getSerial());
        }
        if (d.getVersion() != null) {
          ed.setVersion(d.getVersion());
        }
        if (d.getIdentifier() != null) {
          ed.setIdentifier(d.getIdentifier());
        }
        if (d.getMeasureChannelCount() != null) {
          ed.setMeasureChannelCount(d.getMeasureChannelCount());
        }
        if (d.getConfigChannelCount() != null) {
          ed.setConfigChannelCount(d.getConfigChannelCount());
        }
        if (d.getArticleNumber() != null) {
          ed.setArticleNumber(d.getArticleNumber());
        }
        if (d.getName() != null) {
          ed.setName(d.getName());
        }
        Logger.trace("Updated: " + ed);
      }
    }

    //Lets get some info about these members
    for (CanDevice device : canDevices.values()) {
      queryDevice(device);
    }
  }

  void queryDevice(CanDevice device) {
    Logger.trace("Query for information about device " + device);
    CanMessage updateMessage = sendMessage(CanMessageFactory.statusDataConfig(device.getUidInt(), 0));

    if (!updateMessage.hasValidResponse() && device.getGuiUid() != null) {
      Logger.trace("Trying fallback " + device.getGuiUid());
      updateMessage = sendMessage(CanMessageFactory.statusDataConfig(device.getGuiUidInt(), 0));
    }

    if (updateMessage.hasValidResponse()) {
      CanDeviceParser.parse(device, updateMessage);

      int measurementChannels;
      if (device.getMeasureChannelCount() == null) {
        measurementChannels = 0;
      } else {
        measurementChannels = device.getMeasureChannelCount();
      }
      int configChannels;
      if (device.getConfigChannelCount() == null) {
        configChannels = 0;
      } else {
        configChannels = device.getConfigChannelCount();
      }

      int channels = measurementChannels + configChannels;
      if (channels > 0) {
        Logger.trace("Quering " + channels + " channels for device " + device);
        for (int index = 1; index <= channels; index++) {
          Logger.trace("Query channel " + index);
          updateMessage = sendMessage(CanMessageFactory.statusDataConfig(device.getUidInt(), index));
          CanDeviceParser.parse(device, updateMessage);

          if (index <= measurementChannels) {
            Logger.trace("M#" + index + "; " + device.getMeasuringChannel(index));
          } else {
            int configChannelNumber = index - measurementChannels;
            Logger.trace("C#" + configChannelNumber + "; " + device.getConfigChannel(configChannelNumber));
          }
        }
      }
    } else {
      Logger.trace("No response data in query for " + device);
    }
  }

  /**
   * The CS3 has a Web App API which is used for the Web GUI.<br>
   * The Internal devices can be obtained calling this API which returns a JSON string.<br>
   * From this JSON all devices are found.<br>
   * Most important is the GFP which is the heart of the CS 3 most CAN Commands need the GFP UID.<br>
   * This data can also be obtained using the CAN Member PING command, but The JSON gives a little more detail.
   */
  private List<CanDevice> getCS3Devices() {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();

    String devJson = httpCon.getDevicesJSON();
    List<CanDevice> devices = CanDeviceJSONParser.parse(devJson);
    return devices;
  }

  @Override
  public InfoBean getCommandStationInfo() {
    if (infoBean == null) {
      infoBean = createInfoBean(canDevices);
    }
    return infoBean;
  }

  @Override
  public List<Device> getDevices() {
    List<Device> devices = new ArrayList<>();
    for (CanDevice cd : canDevices.values()) {
      Device d = new Device();
      d.setId(cd.getUid());
      d.setName(cd.getName());
      d.setSerialNumber(cd.getSerial());
      d.setSoftwareVersion(cd.getVersion());
      d.setHardwareVersion(cd.getHwVersion());
      d.setChannels(cd.getConfigChannelCount());
      d.setFeedback(CanDevice.FEEDBACK_DEVICE_NAME.equals(cd.getName()));
      devices.add(d);
    }

    return devices;
  }

  @Override
  public List<FeedbackModule> getFeedbackModules() {
    //Feedbackmodules can be queried from the Link S88 if available.
    //In case of a CS-3 Plus or CS-2 there should be a node "0" which could have max 32 S88 modules.
    //
    //TODO: Test with CS-3 Plus and CS-2.
    //
    //Link S88
    List<FeedbackModule> feedbackModules = new ArrayList<>();
    CanDevice links88 = getCanDevice(CanDevice.FEEDBACK_DEVICE_NAME);
    int bus1Len = 0, bus2Len = 0, bus3Len = 0, nodeId;

    if (links88 != null) {
      nodeId = links88.getIdentifierInt() + 1;
      for (ConfigChannel cc : links88.getConfigChannels()) {
        if (cc.getNumber() == 2) {
          bus1Len = cc.getActualValue();
        }
        if (cc.getNumber() == 3) {
          bus2Len = cc.getActualValue();
        }
        if (cc.getNumber() == 4) {
          bus3Len = cc.getActualValue();
        }
      }

      Logger.trace("nodeId: " + nodeId + ", bus1Len: " + bus1Len + ", bus2Len: " + bus2Len + ", bus3Len: " + bus3Len);

      //Link S88 has 16 sensors starting from 0
      //Bus 1 offset 1000, Bus 2 offset 2000 and Bus 3 offset 3000
      FeedbackModule l = new FeedbackModule();
      l.setId(0);

      l.setAddressOffset(0);
      l.setModuleNumber(1);
      l.setPortCount(16);
      l.setIdentifier(nodeId);
      l.setBusNumber(0);
      l.setCommandStationId(commandStationBean.getId());
      l.setBusSize(1);
      feedbackModules.add(l);

      for (int i = 0; i < bus1Len; i++) {
        FeedbackModule b1 = new FeedbackModule();
        //Use the offset plus module nr as the id
        b1.setId(1000 + i);
        b1.setAddressOffset(1000);
        b1.setModuleNumber(i + 1);
        b1.setPortCount(16);
        b1.setIdentifier(nodeId);
        b1.setBusNumber(1);
        b1.setCommandStationId(commandStationBean.getId());
        b1.setBusSize(bus1Len);
        feedbackModules.add(b1);
      }
      for (int i = 0; i < bus2Len; i++) {
        FeedbackModule b2 = new FeedbackModule();
        b2.setId(2000 + i);
        b2.setAddressOffset(2000);
        b2.setModuleNumber(i + 1);
        b2.setPortCount(16);
        b2.setIdentifier(nodeId);
        b2.setBusNumber(2);
        b2.setCommandStationId(commandStationBean.getId());
        b2.setBusSize(bus2Len);

        feedbackModules.add(b2);
      }
      for (int i = 0; i < bus3Len; i++) {
        FeedbackModule b3 = new FeedbackModule();
        b3.setId(3000 + i);
        b3.setAddressOffset(3000);
        b3.setModuleNumber(i + 1);
        b3.setPortCount(16);
        b3.setIdentifier(nodeId);
        b3.setBusNumber(3);
        b3.setCommandStationId(commandStationBean.getId());
        b3.setBusSize(bus3Len);

        feedbackModules.add(b3);
      }
    }

    return feedbackModules;
  }

  @Override
  public SensorBean getSensorStatus(SensorBean sensorBean) {
    return getSensorStatus(sensorBean.getNodeId(), sensorBean.getId());
  }

  SensorBean getSensorStatus(Integer nodeId, Integer sensorId) {
    int uid;
    if (linkS88Uid > 0) {
      uid = linkS88Uid;
    } else {
      uid = csUid;
    }
    CanMessage sensorStatusMessage = sendMessage(CanMessageFactory.querySensorStatus(nodeId, sensorId, uid));
    SensorBean sb = SensorStatusQuery.parse(sensorStatusMessage, new Date());
    return sb;
  }

  /**
   * Query the System Status
   *
   * @return true the track power is on else off.
   */
  @Override
  public boolean isPower() {
    if (connected) {
      power = SystemStatus.parseSystemPowerMessage(sendMessage(CanMessageFactory.querySystem(csUid)));
    } else {
      power = false;
    }
    return power;
  }

  /**
   * System Stop and GO When on = true then the GO command is issued:<br>
   * The track format processor activates the operation and supplies electrical energy.<br>
   * Any speed levels/functions that may still exist or have been saved will be sent again.<br>
   * When false the Stop command is issued: Track format processor stops operation on main and programming track.<br>
   * Electrical energy is no longer supplied. All speed levels/function values and settings are retained.
   *
   * @param on true Track power On else Off
   * @return true the Track power is On else Off
   */
  @Override
  public boolean power(boolean on) {
    if (connected) {
      Logger.trace("Switch Track Power " + (on ? "On" : "Off"));
      power = SystemStatus.parseSystemPowerMessage(sendMessage(CanMessageFactory.systemStopGo(on, csUid)));
      PowerEvent pe = new PowerEvent(power);
      notifyPowerEventListeners(pe);
      return power;
    } else {
      return false;
    }
  }

  @Override
  public void disconnect() {
    Logger.trace("Start disconnecting...");
    //Stop all schedules
    if (measurementTimer != null) {
      measurementTimer.cancel();
    }
    if (watchDogTimer != null) {
      watchDogTimer.cancel();
    }
    //Stop Threads
    if (executor != null) {
      executor.shutdown();
    }

    //Signal listeners that there are no measurements
    MeasuredChannels measuredChannels = new MeasuredChannels(System.currentTimeMillis());
    MeasurementEvent me = new MeasurementEvent(measuredChannels);
    for (MeasurementEventListener listener : measurementEventListeners) {
      listener.onMeasurement(me);
    }

    try {
      if (connection != null) {
        if (eventMessageHandler != null) {
          eventMessageHandler.quit();
          eventMessageHandler.join(2000);
        }
        connection.close();
        connected = false;
      }

      executor = null;
      connection = null;

      CSConnectionFactory.disconnectAll();
    } catch (Exception ex) {
      Logger.error(ex);
    }
    Logger.trace("Disconnected");
  }

  @Override
  public void onConnectionChange(ConnectionEvent event) {
    String s = event.getSource();
    boolean con = event.isConnected();
    if (con) {
      Logger.trace(s + " has re-connected");
    } else {
      disconnect();
    }

    for (ConnectionEventListener listener : connectionEventListeners) {
      listener.onConnectionChange(event);
    }
  }

  @Override
  public boolean isSupportTrackMeasurements() {
    if (!virtual) {
      CanDevice gfp = canDevices.get(csUid);
      return gfp != null && gfp.getMeasureChannelCount() != null && gfp.getMeasureChannelCount() > 0;
    } else {
      return false;
    }
  }

  void performMeasurements() {
    //The measurable channels are in the GFP. 
    CanDevice gfp = canDevices.get(csUid);
    if (gfp != null) {
      List<MeasuringChannel> channels = gfp.getMeasuringChannels();
      long now = System.currentTimeMillis();
      MeasuredChannels measuredChannels = new MeasuredChannels(now);
      for (MeasuringChannel channel : channels) {
        int channelNumber = channel.getNumber();

        CanMessage message = sendMessage(CanMessageFactory.systemStatus(csUid, channelNumber));
        MeasurementBean measurement = SystemStatusMessage.parse(channel, message, now);
        measuredChannels.addMeasurement(measurement);
        //Logger.trace(measurement);
        measuredValues.put(now, measuredChannels);
        if (measuredValues.size() > 100) {
          long first = measuredValues.firstKey();
          measuredValues.remove(first);
        }

        MeasurementEvent me = new MeasurementEvent(measuredChannels);
        for (MeasurementEventListener listener : measurementEventListeners) {
          listener.onMeasurement(me);
        }
      }
    } else {
      Logger.warn("No measurable channels available");
    }
  }

  public List<MeasuredChannels> getMeasurements() {
    return new ArrayList<>(measuredValues.values());
  }

  public MeasuredChannels getLastMeasurment() {
    return measuredValues.firstEntry().getValue();
  }

  /**
   * Blocking call to the message sender thread which send the message and await the response.<br>
   * When there is no response within 1s or 5s timeout the waiting is cancelled.<br>
   * The time out depends on the message request
   *
   * @param canMessage to send
   * @return the CanMessage with responses
   */
  private CanMessage sendMessage(CanMessage canMessage) {
    if (connection != null && connected) {
      connection.sendCanMessage(canMessage);
    } else {
      Logger.warn("NOT connected!");
      Logger.trace("Message: " + canMessage + " NOT Send!");
    }
    return canMessage;
  }

  @Override
  public void changeDirection(int locUid, Direction direction) {
    if (power && connected) {
      Logger.trace("Change direction to " + direction + " CS val " + direction.getMarklinValue());
      CanMessage message = sendMessage(CanMessageFactory.setDirection(locUid, direction.getMarklinValue(), this.csUid));
      //query velocity of give a not halt
      LocomotiveDirectionEvent dme = LocomotiveDirectionEventParser.parse(message);
      notifyLocomotiveDirectionEventListeners(dme);
    }
  }

  @Override
  public void changeVelocity(int locUid, int speed, Direction direction) {
    if (power && connected) {
      CanMessage message = CanMessageFactory.setLocSpeed(locUid, speed, csUid);
      Logger.trace("Ch Velocity for uid: " + locUid + " -> " + message);
      message = sendMessage(message);

      LocomotiveSpeedEvent vme = LocomotiveVelocityMessage.parse(message);
      notifyLocomotiveSpeedEventListeners(vme);

      if (isVirtual()) {
        //When a locomotive has a speed change (>0) check if Auto mode is on.
        //When in Auto mode try to simulate the first sensor the locomotive is suppose to hit.
        if (AutoPilot.isAutoModeActive() && speed > 0) {
          //simulateDriving(locUid, speed, direction);
          simulator.simulateDriving(locUid, speed, direction);
        }
      }
    }
  }

  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
    if (power && connected) {
      CanMessage message = sendMessage(CanMessageFactory.setFunction(locUid, functionNumber, flag, this.csUid));
      notifyLocomotiveFunctionEventListeners(LocomotiveFunctionEventParser.parseMessage(message));
    }
  }

  @Override
  public void switchAccessory(Integer address, String protocol, AccessoryValue value, Integer switchTime) {
    if (power && connected) {
      //make sure a time is set!
      int st;
      if (switchTime == null || switchTime == 0) {
        st = defaultSwitchTime / 10;
      } else {
        //CS 2/3 Switchtime is in 10 ms increments!
        st = switchTime / 10;
      }

      int adr; // zero based!
      if ("dcc".equals(protocol)) {
        adr = address - 1;
        adr = adr + CanMessage.DCC_ACCESSORY_OFFSET;
      } else {
        //assume MM
        adr = address - 1;
        adr = adr + CanMessage.MM_ACCESSORY_OFFSET;
      }

      CanMessage switchMessage = CanMessageFactory.switchAccessory(adr, value, true, st, csUid);

      Logger.trace("Switching accessory " + adr + " to: " + value + " Message: " + switchMessage);

      CanMessage message = sendMessage(switchMessage);
      //Notify listeners
      AccessoryEvent ae = AccessoryMessage.parse(message);
      notifyAccessoryEventListeners(ae);
    } else {
      Logger.trace("Trackpower is OFF! Can't switch Accessory: " + address + " to: " + value + "!");
    }
  }

  void sendJCSUIDMessage() {
    sendMessage(CanMessageFactory.getMemberPingResponse(CanMessage.JCS_UID, 1, CanMessage.JCS_DEVICE_ID));
  }

  void sentJCSInformationMessage() {
    List<CanMessage> messages = getStatusDataConfigResponse(CanMessage.JCS_SERIAL, 0, 0, "JCS", "Java Central Station", CanMessage.JCS_UID);
    for (CanMessage msg : messages) {
      sendMessage(msg);
    }
  }

  List<LocomotiveBean> getLocomotivesViaCAN() {
    CanMessage message = CanMessageFactory.requestConfigData(csUid, "loks");
    connection.sendCanMessage(message);
    String lokomotive = MessageInflator.inflateConfigDataStream(message, "locomotive");

    LocomotiveBeanParser lp = new LocomotiveBeanParser();
    return lp.parseLocomotivesFile(lokomotive);
  }

  List<LocomotiveBean> getLocomotivesViaHttp() {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    String csLocos = httpCon.getLocomotivesFile();
    LocomotiveBeanParser lp = new LocomotiveBeanParser();
    return lp.parseLocomotivesFile(csLocos);
  }

  List<LocomotiveBean> getLocomotivesViaJSON() {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    String json = httpCon.getLocomotivesJSON();
    LocomotiveBeanJSONParser lp = new LocomotiveBeanJSONParser();
    return lp.parseLocomotives(json);
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    List<LocomotiveBean> locomotives;
    if (this.isCS3()) {
      //For the CS-3 use the JSON for everything as otherwise some function icons are missed
      locomotives = getLocomotivesViaJSON();
    } else {
      if (System.getProperty("locomotive.list.via", "can").equalsIgnoreCase("http")) {
        locomotives = getLocomotivesViaHttp();
      } else {
        locomotives = getLocomotivesViaCAN();
      }
    }

    String csId = commandStationBean.getId();
    for (LocomotiveBean loc : locomotives) {
      loc.setCommandStationId(csId);
    }
    return locomotives;
  }

  List<AccessoryBean> getAccessoriesViaHttp() {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    if (isCS3() && System.getProperty("accessory.list.via", "JSON").equalsIgnoreCase("JSON")) {
      Logger.trace("Obtaining accessory data via JSON...");
      String json = httpCon.getAccessoriesJSON();
      return AccessoryBeanParser.parseAccessoryJSON(json, commandStationBean.getId(), commandStationBean.getShortName());
    } else {
      Logger.trace("Obtaining accessory data via file...");
      String file = httpCon.getAccessoriesFile();
      return AccessoryBeanParser.parseAccessoryFile(file, commandStationBean.getId(), commandStationBean.getShortName());
    }
  }

  List<AccessoryBean> getAccessoriesViaCan() {
    Logger.trace("Obtaining accessory data via CAN...");
    CanMessage message = CanMessageFactory.requestConfigData(csUid, "mags");
    this.connection.sendCanMessage(message);
    String canFile = MessageInflator.inflateConfigDataStream(message, "magnetartikel");
    return AccessoryBeanParser.parseAccessoryFile(canFile, commandStationBean.getId(), commandStationBean.getShortName());
  }

  @Override
  public List<AccessoryBean> getAccessories() {
    List<AccessoryBean> accessories;
    if (System.getProperty("accessory.list.via", "can").equalsIgnoreCase("http")) {
      accessories = getAccessoriesViaHttp();
    } else {
      accessories = getAccessoriesViaCan();
    }
    return accessories;
  }

  @Override
  public Image getLocomotiveImage(String icon) {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    Image locIcon = httpCon.getLocomotiveImage(icon);
    return locIcon;
  }

  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    if (this.isCS3()) {
      if (!FunctionSvgToPngConverter.isSvgCacheLoaded()) {
        Logger.trace("Loading SVG Cache");
        String json = httpCon.getFunctionsSvgJSON();
        FunctionSvgToPngConverter.loadSvgCache(json);
      }
      return FunctionSvgToPngConverter.getFunctionImageCS3(icon);
    } else {
      return httpCon.getFunctionImageCS2(icon);
    }
  }

  private void notifyPowerEventListeners(final PowerEvent powerEvent) {
    power = powerEvent.isPower();
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }

  @Override
  public void fireAllSensorEventsListeners(final SensorEvent sensorEvent) {
    List<AllSensorEventsListener> snapshot = new ArrayList<>(allSensorEventsListeners);
    for (AllSensorEventsListener listener : snapshot) {
      listener.onSensorChange(sensorEvent);
    }
  }

  @Override
  public void simulateSensor(SensorEvent sensorEvent) {
    if (connection instanceof VirtualConnection virtualConnection) {
      virtualConnection.sendEvent(sensorEvent);
    }
  }

  private void notifyAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    for (AccessoryEventListener listener : this.accessoryEventListeners) {
      listener.onAccessoryChange(accessoryEvent);
    }
  }

  private void notifyLocomotiveFunctionEventListeners(final LocomotiveFunctionEvent functionEvent) {
    for (LocomotiveFunctionEventListener listener : this.locomotiveFunctionEventListeners) {
      listener.onFunctionChange(functionEvent);
    }
  }

  private void notifyLocomotiveDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
      listener.onDirectionChange(directionEvent);
    }
  }

  private void notifyLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent speedEvent) {
    for (LocomotiveSpeedEventListener listener : this.locomotiveSpeedEventListeners) {
      listener.onSpeedChange(speedEvent);
    }
  }

  /**
   * Handle Event Message, which are unsolicited messages from the CS.
   */
  private class EventMessageHandler extends Thread {

    @SuppressWarnings("FieldMayBeFinal")
    private boolean stop = false;
    private boolean quit = true;

    private final TransferQueue<CanMessage> eventMessageQueue;

    public EventMessageHandler(CSConnection csConnection) {
      eventMessageQueue = csConnection.getEventQueue();
    }

    void quit() {
      this.quit = true;
    }

    boolean isRunning() {
      return !this.quit;
    }

    boolean isFinished() {
      return this.stop;
    }

    @Override
    public void run() {
      quit = false;
      Thread.currentThread().setName("CS-EVENT-MESSAGE-HANDLER");

      Logger.trace("Event Handler Started...");

      while (isRunning()) {
        try {
          try {
            CanMessage eventMessage = eventMessageQueue.take();
            //Logger.trace("# " + eventMessage);

            int command = eventMessage.getCommand();
            int dlc = eventMessage.getDlc();
            int uid = eventMessage.getDeviceUidNumberFromMessage();
            int subcmd = eventMessage.getSubCommand();

            switch (command) {
              case CanMessage.PING_REQ -> {
                //Lets do this the when we know all of the CS...
                if (CanMessage.DLC_0 == dlc) {
                  //Logger.trace("Answering Ping RQ: " + eventMessage);
                  //sendJCSUIDMessage();
                }
              }
              case CanMessage.PING_RESP -> {
                if (CanMessage.DLC_8 == dlc) {
//                Logger.trace("Ping Response RX: " + eventMessage);
//                List<CanDevice> devices = CanDeviceParser.parse(eventMessage);
//                if (!devices.isEmpty()) {
//                  CanDevice deviceU = devices.get(0);
//                  CanDevice device = canDevices.get(deviceU.getUidInt());
//                  Logger.trace("Found " + device+" GFP "+(csUid==deviceU.getUidInt()?"yes":"no"));
//                }
                  //updateDevice(eventMessage);
                }
              }
              case CanMessage.STATUS_CONFIG -> {
                if (CanMessage.JCS_UID == uid && CanMessage.DLC_5 == dlc) {
                  Logger.trace("StatusConfig RQ: " + eventMessage);
                  //sentJCSInformationMessage();
                }
              }
              case CanMessage.STATUS_CONFIG_RESP -> {
                Logger.trace("StatusConfigResponse RX: " + eventMessage);
                //add to an list of resposne check

              }
              case CanMessage.S88_EVENT_RESPONSE -> {
                if (CanMessage.DLC_8 == dlc) {
                  Logger.trace("FeedbackSensorEvent RX: " + eventMessage);

                  SensorBean sb = FeedbackEventMessage.parse(eventMessage, new Date());
                  Logger.trace("Sensor " + sb.getId() + " value " + sb.getStatus());
                  SensorEvent sme = new SensorEvent(sb);
                  if (sme.getSensorBean() != null) {
                    fireAllSensorEventsListeners(sme);
                  }
                }
              }
              case CanMessage.SX1_EVENT -> {
                if (CanMessage.DLC_8 == dlc) {
                  SensorBean sb = FeedbackEventMessage.parse(eventMessage, new Date());
                  SensorEvent sme = new SensorEvent(sb);
                  if (sme.getSensorBean() != null) {
                    fireAllSensorEventsListeners(sme);
                  }
                }
              }
              case CanMessage.SYSTEM_COMMAND -> {
                //Logger.trace("SystemConfigCommand RX: " + eventMessage);
              }
              case CanMessage.SYSTEM_COMMAND_RESP -> {
                switch (subcmd) {
                  case CanMessage.STOP_SUB_CMD -> {
                    PowerEvent spe = PowerEventParser.parse(eventMessage);
                    notifyPowerEventListeners(spe);
                  }
                  case CanMessage.GO_SUB_CMD -> {
                    PowerEvent gpe = PowerEventParser.parse(eventMessage);
                    notifyPowerEventListeners(gpe);
                  }
                  case CanMessage.HALT_SUB_CMD -> {
                    PowerEvent gpe = PowerEventParser.parse(eventMessage);
                    notifyPowerEventListeners(gpe);
                  }
                  case CanMessage.LOC_STOP_SUB_CMD -> {
                    //stop specific loc
                    LocomotiveSpeedEvent lse = LocomotiveEmergencyStopMessage.parse(eventMessage);
                    notifyLocomotiveSpeedEventListeners(lse);
                  }
                  case CanMessage.OVERLOAD_SUB_CMD -> {
                    PowerEvent gpe = OverloadEventParser.parse(eventMessage);
                    notifyPowerEventListeners(gpe);
                  }
                }
              }
              case CanMessage.ACCESSORY_SWITCHING -> {
                Logger.trace("AccessorySwitching RX: " + eventMessage);
              }
              case CanMessage.ACCESSORY_SWITCHING_RESP -> {
                AccessoryEvent ae = AccessoryMessage.parse(eventMessage);
                notifyAccessoryEventListeners(ae);
              }
              case CanMessage.LOC_VELOCITY -> {
                Logger.trace("VelocityChange# " + eventMessage);

              }
              case CanMessage.LOC_VELOCITY_RESP -> {
                Logger.trace("VelocityChange " + eventMessage);
                notifyLocomotiveSpeedEventListeners(LocomotiveVelocityMessage.parse(eventMessage));
              }
              case CanMessage.LOC_DIRECTION -> {
                Logger.trace("DirectionChange# " + eventMessage);

              }
              case CanMessage.LOC_DIRECTION_RESP -> {
                Logger.trace("DirectionChange " + eventMessage);
                notifyLocomotiveDirectionEventListeners(LocomotiveDirectionEventParser.parse(eventMessage));
              }
              case CanMessage.LOC_FUNCTION -> {

              }
              case CanMessage.LOC_FUNCTION_RESP -> {
                Logger.trace("FunctionChange " + eventMessage);
                notifyLocomotiveFunctionEventListeners(LocomotiveFunctionEventParser.parseMessage(eventMessage));
              }
              case CanMessage.BOOTLOADER_CAN -> {
                //Update the last time millis. Used for the watchdog timer.
                canBootLoaderLastCallMillis = System.currentTimeMillis();
              }
              default -> {
              }
            }
          } catch (InterruptedException ex) {
            Logger.error(ex);
          }
        } catch (Exception e) {
          Logger.error("Error in Handling Thread. Cause: " + e.getMessage());
        }
      }
      Logger.debug("Stop Event handling");
    }
  }

  private void startWatchdog() {
    long checkInterval = Long.parseLong(System.getProperty("connection.watchdog.interval", "30"));
    checkInterval = checkInterval * 1000;

    if (checkInterval > 0 && !virtual) {
      watchdogTask = new WatchdogTask(this);
      watchDogTimer = new Timer("WatchDogTimer");
      watchDogTimer.schedule(watchdogTask, 0, checkInterval);
      Logger.debug("Started Watchdog Timer with an interval of " + checkInterval + "ms");
    } else {
      Logger.debug("Skipping Watchdog Timer");
    }
  }

  private class WatchdogTask extends TimerTask {

    private final MarklinCentralStationImpl commandStation;
    private final long checkInterval;

    WatchdogTask(MarklinCentralStationImpl commandStation) {
      this.commandStation = commandStation;
      checkInterval = Long.parseLong(System.getProperty("connection.watchdog.interval", "30")) * 1000;
    }

    @Override
    public void run() {
      if (commandStation.isConnected() && !virtual) {
        Long now = System.currentTimeMillis();
        long diff = now - commandStation.canBootLoaderLastCallMillis;
        boolean connectionLost = checkInterval < diff;

        if (connectionLost) {
          Logger.trace("The last CANBootLoader request is received more than " + (checkInterval / 1000) + "s ago!");
          ConnectionEvent de = new ConnectionEvent("Marklin Central Station", false);
          commandStation.onConnectionChange(de);
        } else {
          synchronized (this) {
            notifyAll();
          }
        }
      } else {
        //Try to reconnect
        if (!virtual && "true".equalsIgnoreCase(System.getProperty("controller.autoconnect", "true"))) {
          boolean con = commandStation.connect();
          if (con) {
            ConnectionEvent de = new ConnectionEvent("Marklin Central Station", true);
            commandStation.onConnectionChange(de);
          }
        }
      }
    }
  }

  private void startMeasurements() {
    long measureInterval = Long.parseLong(System.getProperty("measurement.interval", "5"));
    measureInterval = measureInterval * 1000;

    if (measureInterval > 0 && !virtual) {
      measurementTask = new MeasurementTask(this);
      measurementTimer = new Timer("MeasurementsTimer");
      measurementTimer.schedule(measurementTask, 10, measureInterval);
      Logger.debug("Started Measurements Timer with an interval of " + measureInterval + "ms");
    } else {
      Logger.debug("Skipping Measurements Timer");
    }
  }

  private class MeasurementTask extends TimerTask {

    private final MarklinCentralStationImpl commandStation;

    MeasurementTask(MarklinCentralStationImpl commandStation) {
      this.commandStation = commandStation;
    }

    @Override
    public void run() {
      if (commandStation.isConnected() && !virtual) {
        try {
          if (commandStation.isSupportTrackMeasurements()) {
            commandStation.performMeasurements();
          } else {
            Logger.debug("Track Measurement are not supported. Cancelling the Measurements schedule...");
            measurementTimer.cancel();
          }
        } catch (Exception e) {
          Logger.error(e);
        }
      }
    }
  }

  //////////// For Testing only.....//////
  /// @param a
  ///
  public static void main(String[] a) {
    RunUtil.loadExternalProperties();

    CommandStationBean csb = new CommandStationBean();
    csb.setId("marklin.cs");
    csb.setDescription("Marklin Central Station 2/3");
    csb.setShortName("CS");
    csb.setClassName("jcs.commandStation.marklin.cs.MarklinCentralStationImpl");
    csb.setConnectVia("NETWORK");
    csb.setIpAddress("192.168.1.180");
    csb.setDecoderControlSupport(true);
    csb.setAccessorySynchronizationSupport(true);
    csb.setFeedbackSupport(true);
    csb.setLocomotiveFunctionSynchronizationSupport(true);
    csb.setLocomotiveImageSynchronizationSupport(true);
    csb.setLocomotiveSynchronizationSupport(true);
    csb.setNetworkPort(15731);
    csb.setProtocols("DCC,MFX,MM");
    csb.setDefault(true);
    csb.setEnabled(true);
    csb.setVirtual(false);

    MarklinCentralStationImpl cs = new MarklinCentralStationImpl(csb, false);
    cs.debug = true;

    System.setProperty("tinylog.writer.level", "trace");

    Logger.debug((cs.connect() ? "Connected" : "NOT Connected"));

    if (cs.isConnected()) {
//      Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));
//      cs.power(false);
//      Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));
      cs.power(true);
      //cs.pause(1000);
//      Logger.debug("Switch Accessory 2 to Red");
      //cs.switchAccessory(1, "MM", AccessoryValue.RED, 200);
      //cs.pause(1000);
      //cs.switchAccessory(1, "MM", AccessoryValue.GREEN, 200);

//
//      Logger.debug("Switch Accessory 2 to Green");
      //cs.switchAccessory(2, AccessoryValue.GREEN, 250);
//      List<FeedbackModule> feedbackModules = cs.getFeedbackModules();
//      Logger.trace("There are " + feedbackModules + " Feedback Modules");
//      for (FeedbackModule fm : feedbackModules) {
//        Logger.trace("Module id: " + fm.getId() + " Module nr: " + fm.getModuleNumber() + " ports: " + fm.getPortCount() + " NodeId: " + fm.getIdentifier() + " BusNr: " + fm.getBusNumber());
//        Logger.trace("FBModule id: " + fm.getId() + " S 1 id:" + fm.getSensor(0).getId() + " contactId: " + fm.getSensor(0).getContactId() + " ModuleNr: " + fm.getSensor(0).getDeviceId() + " Name " + fm.getSensor(0).getName());
//        Logger.trace("FBModule id: " + fm.getId() + " S 15 id:" + fm.getSensor(15).getId() + " contactId: " + fm.getSensor(15).getContactId() + " ModuleNr: " + fm.getSensor(15).getDeviceId() + " Name " + fm.getSensor(15).getName());
//      }
      //cs.getLocomotivesViaCAN();
      //cs.getAccessoriesViaCan();
      
      Logger.debug("Getting all accessories...");
      List<AccessoryBean> accessories = cs.getAccessories();

      Logger.debug("List with "+accessories.size()+" accessories...");
      
      for(AccessoryBean ab : accessories) {
        Logger.debug("Id: "+ab.getId()+" address: "+ab.getAddress()+" name "+ab.getName());
      }
      
//      Logger.debug("Query SensorStatus for sensor : 1 node 65");
//      SensorBean sb = cs.getSensorStatus(65, 1006);
//      Logger.trace("1 SensorStatus: " + sb.toLogString());
//      cs.pause(1000);
//      sb = cs.getSensorStatus(65, 1006);
//      Logger.trace("2 SensorStatus: " + sb.toLogString());
//      cs.pause(1000);
//      sb = cs.getSensorStatus(65, 1006);
//      Logger.trace("3 SensorStatus: " + sb.toLogString());
//      cs.pause(1000);
//      sb = cs.getSensorStatus(65, 1006);
//      Logger.trace("4 SensorStatus: " + sb.toLogString());
//      cs.pause(1000);
//      sb = cs.getSensorStatus(65, 1006);
//      Logger.trace("5 SensorStatus: " + sb.toLogString());

//      
      //cs.getMembers();
      //cs.memberPing();
      //Logger.trace("getStatusDataConfig CS3");
      //cs.getStatusDataConfigCS3();
      //cs.pause(2000);
      //Logger.trace("getStatusDataConfig");
      //cs3.getStatusDataConfig();
      //cs3.pause(1000);
      //cs3.pause(4000);
      //
      //cs3.pause(500);
      //Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));
      //cs3.sendJCSInfo();
      //cs3.pause(500);
      //Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));
      //cs3.sendJCSInfo();
      //cs3.pause(500);
      //Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));
      //cs3.sendJCSInfo();
      //SystemConfiguration data
//      cs.performMeasurements();
    }

    //PingResponse pr2 = cs.memberPing();
    //Logger.info("Query direction of loc 12");
    //DirectionInfo info = cs.getDirectionMarkin(12, DecoderType.MM);
    //cs3.pause(500L);
    //Logger.debug("Wait for 1m");
    //cs.pause(1000 * 60 * 1);
//    List<AccessoryBean> accessories = cs.getAccessoriesViaCan();
//    for (AccessoryBean accessory : accessories) {
//      Logger.trace((accessory.isSignal() ? "Signal" : "Turnout") + ": " + accessory);
//    }
//    List<LocomotiveBean> locs = cs.getLocomotivesViaCAN();
//    for (LocomotiveBean loc : locs) {
//      Logger.trace(loc);
//    }
    //cs.pause(40000);
    cs.pause(40000);
    cs.disconnect();
//    cs.pause(100L);
    Logger.debug("DONE");
    System.exit(0);
  }
  //for (int i = 0; i < 16; i++) {
  //    cs.requestFeedbackEvents(i + 1);
  //}

}
