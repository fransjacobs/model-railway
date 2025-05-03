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
import jcs.commandStation.events.SensorEventListener;
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
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.marklin.cs.can.parser.AccessoryMessage;
import jcs.entities.FeedbackModuleBean;
import jcs.commandStation.marklin.cs2.LocomotiveDirectionEventParser;
import jcs.commandStation.marklin.cs2.LocomotiveFunctionEventParser;
import jcs.commandStation.marklin.cs.can.parser.LocomotiveVelocityMessage;
import jcs.commandStation.marklin.cs2.PowerEventParser;
import jcs.commandStation.VirtualConnection;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.autopilot.DriveSimulator;
import jcs.commandStation.entities.MeasuredChannels;
import jcs.commandStation.entities.MeasurementBean;
import jcs.commandStation.events.DisconnectionEvent;
import jcs.commandStation.events.DisconnectionEventListener;
import jcs.commandStation.marklin.cs.can.device.ConfigChannel;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import jcs.commandStation.marklin.cs.can.parser.CanDevices;
import jcs.commandStation.marklin.cs.can.parser.LocomotiveEmergencyStopMessage;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.SensorBean;
import jcs.util.RunUtil;
import org.tinylog.Logger;
import jcs.commandStation.marklin.cs.net.CSHTTPConnection;
import jcs.commandStation.marklin.parser.GeraetParser;
import jcs.commandStation.marklin.parser.SystemStatusMessage;

/**
 *
 * @author Frans Jacobs
 */
public class MarklinCentralStationImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController, DisconnectionEventListener {

  private CSConnection connection;

  private InfoBean infoBean;
  private Map<Integer, CanDevice> canDevices;

  private int csUid;
  private EventMessageHandler eventMessageHandler;

  private DriveSimulator simulator;

  private Long canBootLoaderLastCallMillis;
  private WatchdogTask watchdogTask;
  private Timer watchDogTimer;

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

  private boolean isCS3() {
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

  @Override
  public final synchronized boolean connect() {
    if (!connected) {
      Logger.trace("Connecting to a " + (virtual ? "Virtual " : "") + "Central Station " + (commandStationBean != null ? commandStationBean.getDescription() : "Unknown"));

      if (executor == null || executor.isShutdown()) {
        executor = Executors.newCachedThreadPool();
      }

      CSConnection csConnection = CSConnectionFactory.getConnection(virtual);

      connection = csConnection;

      if (connection != null) {
        //Wait until the receiver thread has started
        long now = System.currentTimeMillis();
        long timeout = now + 1000L;

        while (!connected && now < timeout) {
          connected = connection.isConnected();
          now = System.currentTimeMillis();
        }
        if (!connected && now > timeout) {
          Logger.error("Could not establish a connection");
        }

        if (connected) {
          canBootLoaderLastCallMillis = System.currentTimeMillis();

          JCS.logProgress("Obtaining Device information...");
          CanDevice gfp = getGFP();
          csUid = Integer.parseUnsignedInt(gfp.getUid(), 16);

          canDevices.put(gfp.getUidInt(), gfp);

          obtainDevices();
          infoBean = createInfoBean(canDevices);

          //The eventMessageHandler Thread is in charge to handle all event messages which are send from the CS to JCS
          eventMessageHandler = new EventMessageHandler(connection);
          eventMessageHandler.start();

          Logger.trace("Connected to " + infoBean.getProductName() + ", " + infoBean.getArticleNumber() + " SerialNumber: " + infoBean.getSerialNumber());

          csConnection.addDisconnectionEventListener(this);
          startWatchdog();
          power = isPower();
          JCS.logProgress("Power is " + (power ? "On" : "Off"));
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
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection(virtual);
    String geraet = httpCon.getInfoFile();
    CanDevice gfp = GeraetParser.parseFile(geraet);
    csUid = Integer.parseUnsignedInt(gfp.getUid(), 16);

//    //CS3?    
//    if ("60126".equals(ib.getArticleNumber()) || "60226".equals(ib.getArticleNumber())) {
//      String json = httpCon.getInfoJSON();
//      ib = GeraetParser.parseJson(json);
//      httpCon.setCs3(true);
//    }
//    if (ib.getIpAddress() == null) {
//      ib.setIpAddress(connection.getControllerAddress().getHostAddress());
//    }
    return gfp;
  }

  InfoBean createInfoBean(Map<Integer, CanDevice> canDevices) {
    InfoBean ib = new InfoBean(commandStationBean);
    ib.setIpAddress(connection.getControllerAddress().getHostAddress());

    for (CanDevice d : canDevices.values()) {
      Logger.trace("Checking device: " + d);
      String name = d.getName();
      if (name == null) {
        name = "null";
      }
      switch (name) {
        case "Central Station 3" -> {
          ib.setGfpUid(d.getUid());
          String uid = d.getUid();
          uid = uid.replace("0x", "");
          //csUid = Integer.parseUnsignedInt(uid, 16);
          Logger.trace("GFP uid: " + d.getUid() + " -> " + csUid);

          ib.setArticleNumber(d.getArticleNumber());
          ib.setProductName(d.getName());
          ib.setSerialNumber(d.getSerial());
          ib.setHardwareVersion(d.getVersion());
          ib.setSupportMeasurements(d.getMeasureChannelCount() > 0);

          //TODO: Only CS 2 and CS3 plus...
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
        case "Link S88" -> {
          ib.setFeedbackSupport(true);
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
    //Map<Integer, CanDevice> members = new HashMap<>();
    CanMessage msg = CanMessageFactory.getMembersPing();
    connection.sendCanMessage(msg);

    List<CanDevice> devices = CanDevices.parse(msg);
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
      Logger.trace("Obtaining data for device " + device);
      CanMessage updateMessage = sendMessage(CanMessageFactory.statusDataConfig(device.getUidInt(), 0));
      if (updateMessage.hasValidResponse()) {
        CanDevices.parse(device, updateMessage);

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
            CanDevices.parse(device, updateMessage);
            
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
  }

  /**
   * The CS3 has a Web App API which is used for the Web GUI.<br>
   * The Internal devices can be obtained calling this API which returns a JSON string.<br>
   * From this JSON all devices are found.<br>
   * Most important is the GFP which is the heart of the CS 3 most CAN Commands need the GFP UID.<br>
   * This data can also be obtained using the CAN Member PING command, but The JSON gives a little more detail.
   */
//  private void getAppDevicesCs3() {
//    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection(virtual);
//
//    String devJson = httpCon.getDevicesJSON();
//    List<DeviceBean> devs = DeviceJSONParser.parse(devJson);
//    //Update the devices
//    for (DeviceBean d : devs) {
//      if (devices.containsKey(d.getUidAsInt())) {
//        devices.put(d.getUidAsInt(), d);
//      } else {
//        devices.put(d.getUidAsInt(), d);
//      }
//      String an = d.getArticleNumber();
//      if ("60213".equals(an) || "60214".equals(an) || "60215".equals(an) || "60126".equals(an) || "60226".equals(an)) {
//        csUid = d.getUidAsInt();
//        mainDevice = d;
//        Logger.trace("MainDevice: " + d.getName());
//      }
//
//      if ("60883".equals(an)) {
//        feedbackDevice = d;
//        Logger.trace("FeedbackDevice: " + d.getName());
//      }
//
//    }
//    Logger.trace("Found " + devices.size() + " devices");
//  }
  @Override
  public InfoBean getCommandStationInfo() {
    return infoBean;
  }

  //TODO!
  @Override
  public List<FeedbackModuleBean> getFeedbackModules() {
    return null;
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
    try {
      if (connection != null) {
        if (eventMessageHandler != null) {
          eventMessageHandler.quit();
        }
        connection.close();
        connected = false;
      }

      if (executor != null) {
        executor.shutdown();
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
  public void onDisconnect(DisconnectionEvent event) {
    String s = event.getSource();
    disconnect();

    for (DisconnectionEventListener listener : disconnectionEventListeners) {
      listener.onDisconnect(event);
    }
  }

  @Override
  public boolean isSupportTrackMeasurements() {
    return true;
  }

  void performMeasurements() {
    //The measurable channels are in the GFP. 
    CanDevice gfp = canDevices.get(csUid);
    if (gfp != null) {
      List<MeasuringChannel> channels = gfp.getMeasuringChannels();

      Logger.trace("Take a measurment on " + channels.size() + " channels");

      MeasuredChannels measuredChannels = new MeasuredChannels(System.currentTimeMillis());
      for (MeasuringChannel channel : channels) {
        int channelNumber = channel.getNumber();

        CanMessage message = sendMessage(CanMessageFactory.systemStatus(csUid, channelNumber));

        MeasurementBean measurement = SystemStatusMessage.parse(channel, message);
        measuredChannels.addMeasurement(measurement);
        Logger.trace(measurement);
      }
      Logger.trace("Measurement: " + measuredChannels);
    } else {
      Logger.warn("No measurable channels avalaible");
    }
  }

  @Override
  public synchronized Map<Integer, ChannelBean> getTrackMeasurements() {
//    Logger.trace("Perform measurement...");
//    if (connected && mainDevice != null) {
//      //main device
//      int nrOfChannels = mainDevice.getAnalogChannels().size();
//
//      ChannelDataParser parser = new ChannelDataParser();
//
//      if (this.analogChannels.isEmpty()) {
//        //No channels configured so let do this first
//        for (int c = 1; c <= nrOfChannels; c++) {
//          Logger.trace("Quering config for channel " + c);
//          CanMessage message = sendMessage(CanMessageFactory.statusDataConfig(csUid, c));
//
//          ChannelBean ch = parser.parseConfigMessage(message);
//          analogChannels.put(c, ch);
//        }
//      }
//
//      for (int c = 1; c <= nrOfChannels; c++) {
//        ChannelBean ch = this.analogChannels.get(c);
//        if (ch != null) {
//          CanMessage message = sendMessage(CanMessageFactory.systemStatus(c, csUid));
//          ch = parser.parseUpdateMessage(message, ch);
//          analogChannels.put(c, ch);
//        }
//      }
//    }
//    return analogChannels;
    return null;
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
    if (connection != null) {
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
      LocomotiveDirectionEvent dme = LocomotiveDirectionEventParser.parseMessage(message);
      notifyLocomotiveDirectionEventListeners(dme);
    }
  }

  @Override
  public void changeVelocity(int locUid, int speed, Direction direction) {
    if (power && connected) {
      //VelocityChange 0x00 0x09 0x03 0x26 0x06 0x00 0x00 0x40 0x0c 0x00 0x30 0x00 0x00
      //16396
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
        st = switchTime / 10;
      }

      int adr; // zero based!
      if ("dcc".equals(protocol)) {
        adr = address - 1;
        adr = adr + CanMessage.DCC_ACCESSORY_OFFSET;
      } else {
        adr = address - 1;
      }
      //CS 2/3 Switchtime is in 10 ms increments!
      st = st / 10;
      CanMessage message = sendMessage(CanMessageFactory.switchAccessory(adr, value, true, st, this.csUid));
      //Notify listeners
      AccessoryEvent ae = AccessoryMessage.parse(message);
      notifyAccessoryEventListeners(ae);
    } else {
      Logger.trace("Trackpower is OFF! Can't switch Accessory: " + address + " to: " + value + "!");
    }
  }

  private void sendJCSUIDMessage() {
    sendMessage(CanMessageFactory.getMemberPingResponse(CanMessage.JCS_UID, 1, CanMessage.JCS_DEVICE_ID));
  }

  private void sentJCSInformationMessage() {
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
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection(virtual);
    String csLocos = httpCon.getLocomotivesFile();
    LocomotiveBeanParser lp = new LocomotiveBeanParser();
    return lp.parseLocomotivesFile(csLocos);
  }

  List<LocomotiveBean> getLocomotivesViaJSON() {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection(virtual);
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
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection(virtual);
    if (isCS3() && System.getProperty("accessory.list.via", "JSON").equalsIgnoreCase("JSON")) {
      String json = httpCon.getAccessoriesJSON();
      return AccessoryBeanParser.parseAccessoryJSON(json, commandStationBean.getId(), commandStationBean.getShortName());
    } else {
      String file = httpCon.getAccessoriesFile();
      return AccessoryBeanParser.parseAccessoryFile(file, commandStationBean.getId(), commandStationBean.getShortName());
    }
  }

  List<AccessoryBean> getAccessoriesViaCan() {
    CanMessage message = CanMessageFactory.requestConfigData(csUid, "mags");
    this.connection.sendCanMessage(message);
    String canFile = MessageInflator.inflateConfigDataStream(message, "magnetartikel");
    return AccessoryBeanParser.parseAccessoryFile(canFile, commandStationBean.getId(), commandStationBean.getShortName());
  }

  @Override
  public List<AccessoryBean> getAccessories() {
    List<AccessoryBean> accessories;
    if (System.getProperty("accessory.list.via", "http").equalsIgnoreCase("http")) {
      accessories = getAccessoriesViaHttp();
    } else {
      accessories = getAccessoriesViaCan();
    }
    return accessories;
  }

  @Override
  public Image getLocomotiveImage(String icon) {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection(virtual);
    Image locIcon = httpCon.getLocomotiveImage(icon);
    return locIcon;
  }

  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    CSHTTPConnection httpCon = CSConnectionFactory.getHTTPConnection(virtual);
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
  public void fireSensorEventListeners(final SensorEvent sensorEvent) {
    for (SensorEventListener listener : sensorEventListeners) {
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
    if (functionEvent.isValid()) {
      for (LocomotiveFunctionEventListener listener : this.locomotiveFunctionEventListeners) {
        listener.onFunctionChange(functionEvent);
      }
    }
  }

  private void notifyLocomotiveDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    if (directionEvent.isValid()) {
      for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
        listener.onDirectionChange(directionEvent);
      }
    }
  }

  private void notifyLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent speedEvent) {
    if (speedEvent.isValid()) {
      for (LocomotiveSpeedEventListener listener : this.locomotiveSpeedEventListeners) {
        listener.onSpeedChange(speedEvent);
      }
    }
  }

  //TODO measurements als a kind of data table...
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
                //Logger.trace("Ping Response RX: " + eventMessage);

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
                SensorEvent sme = new SensorEvent(sb);
                if (sme.getSensorBean() != null) {
                  fireSensorEventListeners(sme);
                }
              }
            }
            case CanMessage.SX1_EVENT -> {
              if (CanMessage.DLC_8 == dlc) {
                SensorBean sb = FeedbackEventMessage.parse(eventMessage, new Date());
                SensorEvent sme = new SensorEvent(sb);
                if (sme.getSensorBean() != null) {
                  fireSensorEventListeners(sme);
                }
              }
            }
            case CanMessage.SYSTEM_COMMAND -> {
              //Logger.trace("SystemConfigCommand RX: " + eventMessage);
            }
            case CanMessage.SYSTEM_COMMAND_RESP -> {
              switch (subcmd) {
                case CanMessage.STOP_SUB_CMD -> {
                  PowerEvent spe = PowerEventParser.parseMessage(eventMessage);
                  notifyPowerEventListeners(spe);
                }
                case CanMessage.GO_SUB_CMD -> {
                  PowerEvent gpe = PowerEventParser.parseMessage(eventMessage);
                  notifyPowerEventListeners(gpe);
                }
                case CanMessage.HALT_SUB_CMD -> {
                  PowerEvent gpe = PowerEventParser.parseMessage(eventMessage);
                  notifyPowerEventListeners(gpe);
                }
                case CanMessage.LOC_STOP_SUB_CMD -> {
                  //stop specific loc
                  LocomotiveSpeedEvent lse = LocomotiveEmergencyStopMessage.parse(eventMessage);
                  notifyLocomotiveSpeedEventListeners(lse);
                }
                case CanMessage.OVERLOAD_SUB_CMD -> {
                  PowerEvent gpe = PowerEventParser.parseMessage(eventMessage);
                  notifyPowerEventListeners(gpe);
                }
              }
            }
            case CanMessage.ACCESSORY_SWITCHING -> {
              Logger.trace("AccessorySwitching RX: " + eventMessage);
            }
            case CanMessage.ACCESSORY_SWITCHING_RESP -> {
              AccessoryEvent ae = AccessoryMessage.parse(eventMessage);
              if (ae.isValid()) {
                notifyAccessoryEventListeners(ae);
              }
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
              notifyLocomotiveDirectionEventListeners(LocomotiveDirectionEventParser.parseMessage(eventMessage));
            }
            case CanMessage.LOC_FUNCTION -> {

            }
            case CanMessage.LOC_FUNCTION_RESP -> {
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
      }

      Logger.debug("Stop Event handling");
    }
  }

  private void startWatchdog() {
    long checkInterval = Long.parseLong(System.getProperty("connection.watchdog.interval", "10"));
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
      checkInterval = Long.parseLong(System.getProperty("connection.watchdog.interval", "10")) * 1000;
    }

    @Override
    public void run() {
      if (commandStation.isConnected() && !virtual) {
        Long now = System.currentTimeMillis();
        long diff = now - commandStation.canBootLoaderLastCallMillis;
        boolean connectionLost = checkInterval < diff;

        if (connectionLost) {
          Logger.trace("The last CANBootLoader request is received more than " + (checkInterval / 1000) + "s ago!");
          DisconnectionEvent de = new DisconnectionEvent("Marklin Central Station");
          commandStation.onDisconnect(de);
        }
      } else {
        //Try to reconnect
        if (!virtual && "true".equalsIgnoreCase(System.getProperty("controller.autoconnect", "true"))) {
          commandStation.connect();
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

    Logger.debug((cs.connect() ? "Connected" : "NOT Connected"));

    if (cs.isConnected()) {
//      Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));
//      cs.power(false);
//      Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));
//      cs.power(true);
//
//      Logger.debug("Switch Accessory 2 to Red");
//      //cs.switchAccessory(2, AccessoryValue.RED, 250);
//
//      Logger.debug("Switch Accessory 2 to Green");
      //cs.switchAccessory(2, AccessoryValue.GREEN, 250);

      //cs.getLocomotivesViaCAN();
      //cs.getAccessoriesViaCan();
      //cs.pause(2000);
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
//      Map<Integer, ChannelBean> measurements = cs.getTrackMeasurements();
//      for (ChannelBean ch : measurements.values()) {
//        if (ch != null) {
//          Logger.trace("Channel " + ch.getNumber() + ": " + ch.getHumanValue() + " " + ch.getUnit());
//        }
//      }
//      Logger.debug("Channel 1: " + cs.channelData1.getChannel().getHumanValue() + " " + cs.channelData1.getChannel().getUnit());
//      Logger.debug("Channel 2: " + cs.channelData2.getChannel().getHumanValue() + " " + cs.channelData2.getChannel().getUnit());
//      Logger.debug("Channel 3: " + cs.channelData3.getChannel().getHumanValue() + " " + cs.channelData3.getChannel().getUnit());
//      Logger.debug("Channel 4: " + cs.channelData4.getChannel().getHumanValue() + " " + cs.channelData4.getChannel().getUnit());
      //cs.getSystemStatus(1);
      cs.performMeasurements();
//
//            Logger.debug("Channel 4....");
//            cs.getSystemStatus(4);
//Now get the systemstatus for all devices
//First the status data config must be called to get the channels
      //cs3.getSystemStatus()
      //            SystemStatus ss = cs.getSystemStatus();
      //            Logger.debug("1: "+ss);
      //
      //
      //            ss = cs.power(true);
      //            Logger.debug("3: "+ss);
      //
      //            cs.pause(1000);
      //            ss = cs.power(false);
      //            Logger.debug("4: "+ss);
      //            List<SensorMessageEvent> sml = cs.querySensors(48);
      //            for (SensorEvent sme : sml) {
      //                Sensor s = new Sensor(sme.getContactId(), sme.isNewValue() ? 1 : 0, sme.isOldValue() ? 1 : 0, sme.getDeviceIdBytes(), sme.getMillis(), new Date());
      //                Logger.debug(s.toLogString());
      //            }
      //List<AccessoryBean> asl = cs.getAccessoryStatuses();
      //for (AccessoryStatus as : asl) {
      //    Logger.debug(as.toString());
      //}
      //            for (int i = 0; i < 30; i++) {
      //                cs.sendIdle();
      //                pause(500);
      //            }
      //            Logger.debug("Sending  member ping\n");
      //            List<PingResponse> prl = cs.membersPing();
      //            //Logger.info("Query direction of loc 12");
      //            //DirectionInfo info = cs.getDirectionMarkin(12, DecoderType.MM);
      //            Logger.debug("got " + prl.size() + " responses");
      //            for (PingResponseParser device : prl) {
      //                Logger.debug(device);
      //            }
      //            List<SensorMessageEvent> sel = cs.querySensors(48);
      //
      //            for (SensorEvent se : sel) {
      //                Logger.debug(se.toString());
      //            }
      //            FeedbackModule fm2 = new FeedbackModule(2);
      //            cs.queryAllPorts(fm2);
      //            Logger.debug(fm2.toLogString());
      //cs2.querySensor(1);
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
    cs.pause(20000);
    cs.disconnect();
//    cs.pause(100L);
    Logger.debug("DONE");
    System.exit(0);
  }
  //for (int i = 0; i < 16; i++) {
  //    cs.requestFeedbackEvents(i + 1);
  //}

}
