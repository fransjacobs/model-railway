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

import java.awt.Image;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
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
import jcs.commandStation.marklin.cs.events.AccessoryListener;
import jcs.commandStation.marklin.cs.events.CanPingListener;
import jcs.commandStation.marklin.cs.events.FeedbackListener;
import jcs.commandStation.marklin.cs.events.LocomotiveListener;
import jcs.commandStation.marklin.cs.events.SystemListener;
import jcs.commandStation.marklin.cs.net.CSConnection;
import jcs.commandStation.marklin.cs.net.CSConnectionFactory;
import jcs.commandStation.marklin.cs.net.HTTPConnection;
import jcs.commandStation.marklin.cs2.AccessoryBeanParser;
import jcs.commandStation.marklin.cs2.ChannelDataParser;
import jcs.commandStation.marklin.cs2.LocomotiveBeanParser;
import jcs.commandStation.marklin.cs3.DeviceJSONParser;
import jcs.commandStation.marklin.cs3.FunctionSvgToPngConverter;
import jcs.commandStation.marklin.cs3.LocomotiveBeanJSONParser;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.entities.DeviceBean;
import jcs.entities.FeedbackModuleBean;
import jcs.entities.InfoBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import static jcs.entities.LocomotiveBean.DecoderType.DCC;
import static jcs.entities.LocomotiveBean.DecoderType.MFX;
import static jcs.entities.LocomotiveBean.DecoderType.MFXP;
import static jcs.entities.LocomotiveBean.DecoderType.SX1;
import jcs.entities.LocomotiveBean.Direction;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class MarklinCentralStationImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController {

  private CSConnection connection;

  private InfoBean infoBean;
  private final Map<Integer, DeviceBean> devices;
  private DeviceBean mainDevice;
  private DeviceBean feedbackDevice;
  private int csUid;

  Map<Integer, ChannelBean> analogChannels;

  private int defaultSwitchTime;

  public MarklinCentralStationImpl(CommandStationBean commandStationBean) {
    this(false, commandStationBean);
  }

  public MarklinCentralStationImpl(boolean autoConnect, CommandStationBean commandStationBean) {
    super(autoConnect, commandStationBean);
    devices = new HashMap<>();
    analogChannels = new HashMap<>();
    defaultSwitchTime = Integer.getInteger("default.switchtime", 300);

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

  private boolean isCS3(String articleNumber) {
    return "60216".equals(articleNumber) || "60226".equals(articleNumber);
  }

  public boolean isCS3() {
    if (mainDevice != null) {
      String articleNumber = mainDevice.getArticleNumber();
      return "60216".equals(articleNumber) || "60226".equals(articleNumber);
    } else {
      return false;
    }
  }

  @Override
  public String getIp() {
    return CSConnectionFactory.getControllerIp();
  }

  @Override
  public final synchronized boolean connect() {
    if (!connected) {
      Logger.trace("Connecting to a Central Station " + (commandStationBean != null ? commandStationBean.getDescription() : "Unknown"));
      if (executor == null || executor.isShutdown()) {
        executor = Executors.newCachedThreadPool();
      }

      CSConnection csConnection = CSConnectionFactory.getConnection();
      connection = csConnection;

      if (connection != null) {
        //Wait, if needed until the receiver thread has started
        long now = System.currentTimeMillis();
        long timeout = now + 1000L;

        while (!connected && now < timeout) {
          connected = csConnection.isConnected();
          now = System.currentTimeMillis();
        }
        if (!connected && now > timeout) {
          Logger.error("Could not establish a connection");
        }

        if (connected) {
          //Prepare the observers (listeners) which need to react on message events from the Central Station
          CanPingMessageListener pingListener = new CanPingMessageListener(this);
          CanFeedbackMessageListener feedbackListener = new CanFeedbackMessageListener(this);
          CanSystemMessageListener systemEventListener = new CanSystemMessageListener(this);
          CanAccessoryMessageListener accessoryListener = new CanAccessoryMessageListener(this);
          CanLocomotiveMessageListener locomotiveListener = new CanLocomotiveMessageListener(this);

          this.connection.setCanPingListener(pingListener);
          this.connection.setFeedbackListener(feedbackListener);
          this.connection.setSystemListener(systemEventListener);
          this.connection.setAccessoryListener(accessoryListener);
          this.connection.setLocomotiveListener(locomotiveListener);

          JCS.logProgress("Obtaining Device information...");

          infoBean = getCSInfo();

          //request all members on the Marklin CAN bus to give a response
          long start = System.currentTimeMillis();
          now = System.currentTimeMillis();
          timeout = now + 30000L;

          if (isCS3(infoBean.getArticleNumber())) {
            Logger.trace("Connected to CS3");
            getAppDevicesCs3();
          } else {
            Logger.trace("Connected to CS2");
            getMembers();
            while (mainDevice == null && mainDevice.getName() == null && mainDevice.getArticleNumber() == null && now < timeout) {
              pause(100);
              now = System.currentTimeMillis();
            }
          }

          if (mainDevice != null) {
            Logger.trace("Found " + mainDevice.getName() + ", " + mainDevice.getArticleNumber() + " SerialNumber: " + mainDevice.getSerial() + " in " + (now - start) + " ms");
            JCS.logProgress("Connected with " + infoBean.getProductName());

            power = isPower();
            JCS.logProgress("Power is " + (power ? "On" : "Off"));
          } else {
            Logger.warn("No main Device found yet...");
          }
        }
        Logger.trace("Connected: " + connected + " Default Accessory SwitchTime: " + this.defaultSwitchTime);
      } else {
        Logger.warn("Can't connect with Central Station!");
        JCS.logProgress("Can't connect with Central Station!");
      }
    }

    if (isCS3()) {
      getMembers();
    }

    return connected;
  }

  //The Central station has a "geraet.vrs" files which can be retrieved via HTTP.
  //Based on the info in this file it is quicker to know whether the CS is a version 2 or 3.
  //In case of a 3 the data can ve retreived via JSON else use CAN
  private InfoBean getCSInfo() {
    HTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    String geraet = httpCon.getInfoFile();
    InfoBean ib = new InfoBean(geraet, true);

    if ("60126".equals(ib.getArticleNumber()) || "60226".equals(ib.getArticleNumber())) {
      //CS3
      String json = httpCon.getInfoJSON();
      ib = new InfoBean(json, false);
      httpCon.setCs3(true);
    }
    return ib;
  }

  /**
   * The CS3 has a Web App API which is used for the Web GUI.<br>
   * The Internal devices can be obtained calling this API which returns a JSON string.<br>
   * From this JSON all devices are found.<br>
   * Most important is the GFP which is the heart of the CS 3 most CAN Commands need the GFP UID.<br>
   * This data can also be obtained using the CAN Member PING command, but The JSON gives a little more detail.
   *
   * @return
   */
  private void getAppDevicesCs3() {
    HTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();

    String devJson = httpCon.getDevicesJSON();
    List<DeviceBean> devs = DeviceJSONParser.parse(devJson);
    //Update the devices
    for (DeviceBean d : devs) {
      if (devices.containsKey(d.getUidAsInt())) {
        //Logger.trace("Updating " + d.getUid() + ", " + d.getName());
        devices.put(d.getUidAsInt(), d);
      } else {
        //Logger.trace("Adding " + d.getUid() + ", " + d.getName());
        devices.put(d.getUidAsInt(), d);
      }
      String an = d.getArticleNumber();
      if ("60213".equals(an) || "60214".equals(an) || "60215".equals(an) || "60126".equals(an) || "60226".equals(an)) {
        this.csUid = d.getUidAsInt();
        this.mainDevice = d;
        Logger.trace("MainDevice: " + d);
      }

      if ("60883".equals(an)) {
        this.feedbackDevice = d;
        Logger.trace("FeedbackDevice: " + d);
      }

    }
    Logger.trace("Found " + devices.size() + " devices");
  }

  @Override
  public DeviceBean getDevice() {
    return this.mainDevice;
  }

  @Override
  public List<DeviceBean> getDevices() {
    return this.devices.values().stream().collect(Collectors.toList());
  }

  @Override
  public InfoBean getCommandStationInfo() {
    return infoBean;
  }

  @Override
  public DeviceBean getFeedbackDevice() {
    return this.feedbackDevice;
  }

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
    if (this.connected) {
      CanMessage m = sendMessage(CanMessageFactory.querySystem(this.csUid));
      if (debug) {
        Logger.trace("Received " + m.getResponses().size() + " responses. RX: " + m.getResponse());
      }
      SystemStatus ss = new SystemStatus(m);
      this.power = ss.isPower();
    } else {
      this.power = false;
    }
    return this.power;
  }

  /**
   * System Stop and GO When on = true then the GO command is issued: The track format processor activates the operation and supplies electrical energy. Any speed levels/functions that may still exist
   * or have been saved will be sent again. when false the Stop command is issued: Track format processor stops operation on main and programming track. Electrical energy is no longer supplied. All
   * speed levels/function values and settings are retained.
   *
   * @param on true Track power On else Off
   * @return true the Track power is On else Off
   */
  @Override
  public boolean power(boolean on) {
    if (this.connected) {
      SystemStatus ss = new SystemStatus(sendMessage(CanMessageFactory.systemStopGo(on, csUid)));
      this.power = ss.isPower();

      PowerEvent pe = new PowerEvent(this.power);
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

  void getMembers() {
    CanMessage msg = CanMessageFactory.getMembersPing();
    this.connection.sendCanMessage(msg);

    if (debug) {
      Logger.trace(msg);
      for (CanMessage r : msg.getResponses()) {
        Logger.trace(r);
      }
    }

    for (CanMessage r : msg.getResponses()) {
      DeviceBean d = new DeviceBean(r);

      if (!devices.containsKey(d.getUidAsInt())) {
        devices.put(d.getUidAsInt(), d);
      }
      if (debug) {
        Logger.trace("Found uid: " + d.getUid() + " deviceId: " + d.getIdentifier() + " Device Type: " + d.getDevice());
      }
    }
    if (debug) {
      Logger.trace("Found " + this.devices.size() + " devices");
    }
    while (mainDevice == null) {
      for (DeviceBean d : this.getDevices()) {
        if (!d.isDataComplete()) {
          if (debug) {
            Logger.trace("Requesting more info for uid: " + d.getUid());
          }
          CanMessage updateMessage = sendMessage(CanMessageFactory.statusDataConfig(d.getUidAsInt(), 0));

          if (debug) {
            Logger.trace(updateMessage);
            for (CanMessage r : updateMessage.getResponses()) {
              Logger.trace(r);
            }
          }
          d.updateFromMessage(updateMessage);
          if (debug) {
            if (d.isDataComplete()) {
              Logger.trace("Updated: " + d);
            } else {
              Logger.trace("No data received for Device uid: " + d.getUid());
            }
          }
        }
      }

      for (DeviceBean d : this.getDevices()) {
        if (d.isDataComplete() && ("60213".equals(d.getArticleNumber()) || "60214".equals(d.getArticleNumber()) || "60215".equals(d.getArticleNumber()) || "60126".equals(d.getArticleNumber()) || "60226".equals(d.getArticleNumber()))) {
          csUid = d.getUidAsInt();
          mainDevice = d;
          if (debug) {
            Logger.trace("Main Device: " + d);
          }
          if (System.getProperty("cs.article") == null) {
            System.setProperty("cs.article", this.mainDevice.getArticleNumber());
            System.setProperty("cs.serial", this.mainDevice.getSerial());
            System.setProperty("cs.name", this.mainDevice.getName());
            System.setProperty("cs.cs3", (isCS3() ? "true" : "false"));
          }
        } else {
          if (debug) {
            Logger.trace(d);
          }
        }
      }
    }
  }

  private void updateMember(CanMessage message) {
    executor.execute(() -> updateDevice(message));
  }

  private void updateDevice(final CanMessage message) {
    if (CanMessage.PING_RESP == message.getCommand()) {
      int uid = message.getDeviceUidNumberFromMessage();

      DeviceBean device;
      if (this.devices.containsKey(uid)) {
        device = this.devices.get(uid);
      } else {
        device = new DeviceBean(message);
        this.devices.put(device.getUidAsInt(), device);
      }

      if (!device.isDataComplete()) {
        CanMessage msg = sendMessage(CanMessageFactory.statusDataConfig(device.getUidAsInt(), 0));
        device.updateFromMessage(msg);
        if (debug) {
          Logger.trace("Updated: " + device);
        }
        //Can the main device be set from the avaliable data
        for (DeviceBean d : this.devices.values()) {
          if (d.isDataComplete() && ("60214".equals(d.getArticleNumber()) || "60226".equals(d.getArticleNumber()) || "60126".equals(d.getArticleNumber()))) {
            this.csUid = d.getUidAsInt();
            this.mainDevice = d;
            if (debug) {
              Logger.trace("Main Device: " + d);
            }
          }
        }
      }

      if (this.mainDevice == null) {
        //Lets send a ping again
        getMembers();
      } else {
        if (this.mainDevice != null && this.mainDevice.isDataComplete()) {
          if (System.getProperty("cs.article") == null) {
            System.setProperty("cs.article", this.mainDevice.getArticleNumber());
            System.setProperty("cs.serial", this.mainDevice.getSerial());
            System.setProperty("cs.name", this.mainDevice.getName());
            System.setProperty("cs.cs3", (isCS3() ? "true" : "false"));
            if (debug) {
              Logger.trace("CS " + (isCS3() ? "3" : "2") + " Device: " + device);
            }
          }
        }
      }
    }
  }

  @Override
  public synchronized Map<Integer, ChannelBean> getTrackMeasurements() {
    if (this.connected && this.mainDevice != null) {
      //main device
      int nrOfChannels = this.mainDevice.getAnalogChannels().size();

      ChannelDataParser parser = new ChannelDataParser();

      if (this.analogChannels.isEmpty()) {
        //No channels configured so let do this first
        for (int c = 1; c <= nrOfChannels; c++) {
          Logger.trace("Quering config for channel " + c);
          CanMessage message = sendMessage(CanMessageFactory.statusDataConfig(csUid, c));

          ChannelBean ch = parser.parseConfigMessage(message);
          analogChannels.put(c, ch);
        }
      }

      for (int c = 1; c <= nrOfChannels; c++) {
        ChannelBean ch = this.analogChannels.get(c);
        if (ch != null) {
          CanMessage message = sendMessage(CanMessageFactory.systemStatus(c, csUid));
          ch = parser.parseUpdateMessage(message, ch);
          analogChannels.put(c, ch);
        }
      }
    }
    return this.analogChannels;
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
    if (this.connection != null) {
      this.connection.sendCanMessage(canMessage);
    } else {
      Logger.warn("NOT connected!");
      Logger.trace("Message: " + canMessage + " NOT Send!");
    }
    return canMessage;
  }

  private int getLocoAddres(int address, DecoderType decoderType) {
    int locoAddress;
    locoAddress = switch (decoderType) {
      case MFX ->
        0x4000 + address;
      case MFXP ->
        0x4000 + address;
      case DCC ->
        0xC000 + address;
      case SX1 ->
        0x0800 + address;
      default ->
        address;
    };

    return locoAddress;
  }

  @Override
  public void changeDirection(int locUid, Direction direction) {
    if (this.power && this.connected) {
      CanMessage message = sendMessage(CanMessageFactory.setDirection(locUid, direction.getMarklinValue(), this.csUid));
      LocomotiveDirectionEvent dme = new LocomotiveDirectionEvent(message);
      this.notifyLocomotiveDirectionEventListeners(dme);
    }
  }

  @Override
  public void changeVelocity(int locUid, int speed, Direction direction) {
    if (this.power && this.connected) {
      CanMessage message = sendMessage(CanMessageFactory.setLocSpeed(locUid, speed, this.csUid));
      LocomotiveSpeedEvent vme = new LocomotiveSpeedEvent(message);
      this.notifyLocomotiveSpeedEventListeners(vme);
    }
  }

  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
    if (this.power && this.connected) {
      CanMessage message = sendMessage(CanMessageFactory.setFunction(locUid, functionNumber, flag, this.csUid));
      this.notifyLocomotiveFunctionEventListeners(new LocomotiveFunctionEvent(message));
    }
  }

  @Override
  public void switchAccessory(Integer address, AccessoryValue value) {
    switchAccessory(address, value, this.defaultSwitchTime);
  }

  @Override
  public void switchAccessory(Integer address, AccessoryValue value, Integer switchTime) {
    if (this.power && this.connected) {
      //make sure a time is set!
      int st;
      if (switchTime == null || switchTime == 0) {
        st = this.defaultSwitchTime;
      } else {
        st = switchTime;
      }
      //CS Switchtime is in 10 ms increments
      st = st / 10;
      CanMessage message = sendMessage(CanMessageFactory.switchAccessory(address, value, true, st, this.csUid));
      //Notify listeners
      AccessoryEvent ae = new AccessoryEvent(message);

      notifyAccessoryEventListeners(ae);
    } else {
      Logger.trace("Trackpower is OFF! Can't switch Accessory: " + address + " to: " + value + "!");
    }
  }

  private void sendJCSUID() {
    executor.execute(() -> sendJCSUIDMessage());
  }

  private void sendJCSUIDMessage() {
    sendMessage(CanMessageFactory.getMemberPingResponse(CanMessage.JCS_UID, 1, CanMessage.JCS_DEVICE_ID));
  }

  private void sendJCSInformation() {
    executor.execute(() -> sentJCSInformationMessage());
  }

  private void sentJCSInformationMessage() {
    List<CanMessage> messages = getStatusDataConfigResponse(CanMessage.JCS_SERIAL, 0, 0, "JCS", "Java Central Station", CanMessage.JCS_UID);
    for (CanMessage msg : messages) {
      sendMessage(msg);
    }
  }

  List<LocomotiveBean> getLocomotivesViaCAN() {
    CanMessage message = CanMessageFactory.requestConfigData(csUid, "loks");
    this.connection.sendCanMessage(message);
    String lokomotive = MessageInflator.inflateConfigDataStream(message, "locomotive");

    LocomotiveBeanParser lp = new LocomotiveBeanParser();
    return lp.parseLocomotivesFile(lokomotive);
  }

  List<LocomotiveBean> getLocomotivesViaHttp() {
    HTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    String csLocos = httpCon.getLocomotivesFile();
    LocomotiveBeanParser lp = new LocomotiveBeanParser();
    return lp.parseLocomotivesFile(csLocos);
  }

  List<LocomotiveBean> getLocomotivesViaJSON() {
    HTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
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
    HTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
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
    HTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
    Image locIcon = httpCon.getLocomotiveImage(icon);
    return locIcon;
  }

  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    HTTPConnection httpCon = CSConnectionFactory.getHTTPConnection();
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
    this.power = powerEvent.isPower();
    executor.execute(() -> fireAllPowerEventListeners(powerEvent));
  }

  private void fireAllPowerEventListeners(final PowerEvent powerEvent) {
    this.power = powerEvent.isPower();
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }

  private void fireAllSensorEventListeners(final SensorEvent sensorEvent) {
    for (SensorEventListener listener : sensorEventListeners) {
      listener.onSensorChange(sensorEvent);
    }
  }

  private void notifySensorEventListeners(final SensorEvent sensorEvent) {
    executor.execute(() -> fireAllSensorEventListeners(sensorEvent));
  }

  private void fireAllAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    for (AccessoryEventListener listener : this.accessoryEventListeners) {
      listener.onAccessoryChange(accessoryEvent);
    }
  }

  private void notifyAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    executor.execute(() -> fireAllAccessoryEventListeners(accessoryEvent));
  }

  private void fireAllFunctionEventListeners(final LocomotiveFunctionEvent functionEvent) {
    if (functionEvent.isValid()) {
      for (LocomotiveFunctionEventListener listener : this.locomotiveFunctionEventListeners) {
        listener.onFunctionChange(functionEvent);
      }
    }
  }

  private void notifyLocomotiveFunctionEventListeners(final LocomotiveFunctionEvent functionEvent) {
    executor.execute(() -> fireAllFunctionEventListeners(functionEvent));
  }

  private void fireAllDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    if (directionEvent.isValid()) {
      for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
        listener.onDirectionChange(directionEvent);
      }
    }
  }

  private void notifyLocomotiveDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    executor.execute(() -> fireAllDirectionEventListeners(directionEvent));
  }

  private void fireAllLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent speedEvent) {
    if (speedEvent.isValid()) {
      for (LocomotiveSpeedEventListener listener : this.locomotiveSpeedEventListeners) {
        listener.onSpeedChange(speedEvent);
      }
    }
  }

  private void notifyLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent locomotiveEvent) {
    executor.execute(() -> fireAllLocomotiveSpeedEventListeners(locomotiveEvent));
  }

  private class CanFeedbackMessageListener implements FeedbackListener {

    private final MarklinCentralStationImpl controller;

    CanFeedbackMessageListener(MarklinCentralStationImpl controller) {
      this.controller = controller;
    }

    @Override
    public void onFeedbackMessage(final CanMessage message) {
      int cmd = message.getCommand();
      switch (cmd) {
        case CanMessage.S88_EVENT_RESPONSE -> {
          if (CanMessage.DLC_8 == message.getDlc()) {
            SensorEvent sme = new SensorEvent(message, new Date());
            if (sme.getSensorBean() != null) {
              controller.notifySensorEventListeners(sme);
            }
          }
        }
      }
    }
  }

  private class CanPingMessageListener implements CanPingListener {

    private final MarklinCentralStationImpl controller;

    CanPingMessageListener(MarklinCentralStationImpl controller) {
      this.controller = controller;
    }

    @Override
    public void onCanPingRequestMessage(final CanMessage message) {
      int cmd = message.getCommand();
      int dlc = message.getDlc();
      //int uid = message.getDeviceUidNumberFromMessage();
      switch (cmd) {
        case CanMessage.PING_REQ -> {
          //Lets do this the when we know all of the CS...
          if (controller.mainDevice != null) {
            if (CanMessage.DLC_0 == dlc) {
              controller.sendJCSUID();
            }
          }
        }
      }
    }

    @Override
    public void onCanPingResponseMessage(final CanMessage message
    ) {
      int cmd = message.getCommand();
      int dlc = message.getDlc();
      switch (cmd) {
        case CanMessage.PING_RESP -> {
          if (CanMessage.DLC_8 == dlc) {
            controller.updateMember(message);
          }
        }
      }
    }

    @Override
    public void onCanStatusConfigRequestMessage(final CanMessage message) {
      int cmd = message.getCommand();
      int dlc = message.getDlc();
      int uid = message.getDeviceUidNumberFromMessage();
      switch (cmd) {
        case CanMessage.STATUS_CONFIG -> {
          if (CanMessage.JCS_UID == uid && CanMessage.DLC_5 == dlc) {
            controller.sendJCSInformation();
          }
        }
      }
    }
  }

  private class CanSystemMessageListener implements SystemListener {

    private final MarklinCentralStationImpl controller;

    CanSystemMessageListener(MarklinCentralStationImpl controller) {
      this.controller = controller;
    }

    @Override
    public void onSystemMessage(CanMessage message) {
      int cmd = message.getCommand();
      int subcmd = message.getSubCommand();

      switch (cmd) {
        case CanMessage.SYSTEM_COMMAND_RESP -> {
          switch (subcmd) {
            case CanMessage.STOP_SUB_CMD -> {
              PowerEvent spe = new PowerEvent(message);
              controller.notifyPowerEventListeners(spe);
            }
            case CanMessage.GO_SUB_CMD -> {
              PowerEvent gpe = new PowerEvent(message);
              controller.notifyPowerEventListeners(gpe);
            }
            case CanMessage.HALT_SUB_CMD -> {
              PowerEvent gpe = new PowerEvent(message);
              controller.notifyPowerEventListeners(gpe);
            }
            case CanMessage.LOC_STOP_SUB_CMD -> {
              PowerEvent gpe = new PowerEvent(message);
              controller.notifyPowerEventListeners(gpe);
            }
            case CanMessage.OVERLOAD_SUB_CMD -> {
              PowerEvent gpe = new PowerEvent(message);
              controller.notifyPowerEventListeners(gpe);
            }
            default -> {
            }
          }
        }
      }
    }
  }

  private class CanAccessoryMessageListener implements AccessoryListener {

    private final MarklinCentralStationImpl controller;

    CanAccessoryMessageListener(MarklinCentralStationImpl controller) {
      this.controller = controller;
    }

    @Override
    public void onAccessoryMessage(CanMessage message) {
      int cmd = message.getCommand();
      switch (cmd) {
        case CanMessage.ACCESSORY_SWITCHING_RESP -> {
          AccessoryEvent ae = new AccessoryEvent(message);
          if (ae.isKnownAccessory()) {
            controller.notifyAccessoryEventListeners(ae);
          }
        }
      }
    }
  }

  private class CanLocomotiveMessageListener implements LocomotiveListener {

    private final MarklinCentralStationImpl controller;

    CanLocomotiveMessageListener(MarklinCentralStationImpl controller) {
      this.controller = controller;
    }

    @Override
    public void onLocomotiveMessage(CanMessage message) {
      int cmd = message.getCommand();
      switch (cmd) {
        case CanMessage.LOC_FUNCTION_RESP ->
          controller.notifyLocomotiveFunctionEventListeners(new LocomotiveFunctionEvent(message));
        case CanMessage.LOC_DIRECTION_RESP ->
          controller.notifyLocomotiveDirectionEventListeners(new LocomotiveDirectionEvent(message));
        case CanMessage.LOC_VELOCITY_RESP ->
          controller.notifyLocomotiveSpeedEventListeners(new LocomotiveSpeedEvent(message));
      }
    }
  }

//  
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

    MarklinCentralStationImpl cs = new MarklinCentralStationImpl(false, csb);
    Logger.debug((cs.connect() ? "Connected" : "NOT Connected"));

    if (cs.isConnected()) {
      Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));

      //cs.getLocomotivesViaCAN();
      //cs.getAccessoriesViaCan();
      //cs3.pause(2000);
      //Logger.trace("getStatusDataConfig CS3");
      //cs3.getStatusDataConfigCS3();
      //cs3.pause(2000);
      //Logger.trace("getStatusDataConfig");
      //cs3.getStatusDataConfig();
      //cs3.pause(1000);
      //cs3.pause(4000);
      //cs3.power(false);
      //Logger.debug("Power is " + (cs.isPower() ? "ON" : "Off"));
      //cs3.power(true);
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
      Map<Integer, ChannelBean> measurements = cs.getTrackMeasurements();

      for (ChannelBean ch : measurements.values()) {
        Logger.trace("Channel " + ch.getNumber() + ": " + ch.getHumanValue() + " " + ch.getUnit());
      }

//      Logger.debug("Channel 1: " + cs.channelData1.getChannel().getHumanValue() + " " + cs.channelData1.getChannel().getUnit());
//      Logger.debug("Channel 2: " + cs.channelData2.getChannel().getHumanValue() + " " + cs.channelData2.getChannel().getUnit());
//      Logger.debug("Channel 3: " + cs.channelData3.getChannel().getHumanValue() + " " + cs.channelData3.getChannel().getUnit());
//      Logger.debug("Channel 4: " + cs.channelData4.getChannel().getHumanValue() + " " + cs.channelData4.getChannel().getUnit());
      //cs.getSystemStatus(1);
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
    cs.pause(40000);
    cs.disconnect();
    cs.pause(100L);
    Logger.debug("DONE");
    //System.exit(0);
  }
  //for (int i = 0; i < 16; i++) {
  //    cs.requestFeedbackEvents(i + 1);
  //}

}
