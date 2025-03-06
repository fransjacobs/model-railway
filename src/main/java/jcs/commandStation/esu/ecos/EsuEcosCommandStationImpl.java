/*
 * Copyright 2024 Frans Jacobs.
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
package jcs.commandStation.esu.ecos;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TransferQueue;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.esu.ecos.net.EcosConnection;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.AccessoryBean;
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.commandStation.entities.DeviceBean;
import jcs.entities.FeedbackModuleBean;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.esu.ecos.net.EcosHTTPConnection;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.SensorEventListener;
import jcs.commandStation.autopilot.DriveSimulator;
import jcs.commandStation.VirtualConnection;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import jcs.entities.LocomotiveBean;
import jcs.util.NetworkUtil;
import org.tinylog.Logger;

public class EsuEcosCommandStationImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController {

  private EcosConnection connection;
  private EventHandler eventMessageHandler;

  private EcosManager ecosManager;
  private LocomotiveManager locomotiveManager;
  private AccessoryManager accessoryManager;
  private FeedbackManager feedbackManager;

  private DriveSimulator simulator;

  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    autoConnect(autoConnect);
  }

  private void autoConnect(boolean autoConnect) {
    if (commandStationBean != null) {
      if (autoConnect) {
        Logger.trace("Perform auto connect");
        connect();
      }
    } else {
      Logger.error("Command Station NOT SET!");
    }
  }

  @Override
  public void setVirtual(boolean flag) {
    this.virtual = flag;
    Logger.info("Switching Virtual Mode " + (flag ? "On" : "Off"));
    disconnect();
    connect();
  }

  @Override
  public boolean connect() {
    if (!connected) {
      Logger.trace("Connecting to a " + (this.virtual ? "Virtual " : "") + "ESU ECoS Command Station...");
      if (executor == null || executor.isShutdown()) {
        executor = Executors.newCachedThreadPool();
      }

      if (commandStationBean == null) {
        Logger.error("ESU ECoS Command Station Configuration NOT set!");
        return false;
      } else {
        Logger.trace("Connect using " + commandStationBean.getConnectionType());
      }

      CommandStationBean.ConnectionType conType = commandStationBean.getConnectionType();

      boolean canConnect = true;
      if (conType == CommandStationBean.ConnectionType.NETWORK) {
        if (commandStationBean.getIpAddress() != null) {
          EcosConnectionFactory.writeLastUsedIpAddressProperty(commandStationBean.getIpAddress());
        } else {

          String ip;
          if (!virtual) {
            //try to discover the ECoS
            InetAddress ecosAddr = EcosConnectionFactory.discoverEcos();
            ip = ecosAddr.getHostAddress();
          } else {
            ip = NetworkUtil.getIPv4HostAddress().getHostAddress();
          }
          commandStationBean.setIpAddress(ip);

          EcosConnectionFactory.writeLastUsedIpAddressProperty(commandStationBean.getIpAddress());
          canConnect = ip != null;
          if (!canConnect) {
            Logger.error("Can't connect; IP Address not set");
          }
        }
      }

      if (canConnect) {
        connection = EcosConnectionFactory.getConnection(commandStationBean.isVirtual());

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
            //Start the EventHandler
            eventMessageHandler = new EventHandler(this.connection);
            eventMessageHandler.start();

            //Obtain some info about the ECoS
            initBaseObject();

            initLocomotiveManager();
            Logger.trace("There are " + this.locomotiveManager.getSize() + " locomotives");

            initAccessoryManager();
            Logger.trace("There are " + this.accessoryManager.getSize() + " accessories");

            initFeedbackManager();
            Logger.trace("There are " + this.feedbackManager.getSize() + " feedback modules");

            if (isVirtual()) {
              simulator = new DriveSimulator();
              Logger.info("ECoS Virtual Mode Enabled!");

            }

          } else {
            Logger.warn("Can't connect with a ESU ECoS Command Station!");
            JCS.logProgress("Can't connect with ESU ECoS Command Station!");
          }
        }
      }
    }
//            Logger.trace("Connected with: " + (this.mainDevice != null ? this.mainDevice.getName() : "Unknown"));
//            JCS.logProgress("Power is " + (this.power ? "On" : "Off"));
    return this.connected;

  }

  private void initBaseObject() {
    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getBaseObject());
    ecosManager = new EcosManager(this, reply);

    connection.sendMessage(EcosMessageFactory.subscribeBaseObject());
    addPowerEventListener(ecosManager);
  }

  private void initLocomotiveManager() {
    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getLocomotives());
    locomotiveManager = new LocomotiveManager(this, reply);

    connection.sendMessage(EcosMessageFactory.subscribeLokManager());

    for (LocomotiveBean loc : this.locomotiveManager.getLocomotives().values()) {
      EcosMessage detailsReply = connection.sendMessage(EcosMessageFactory.getLocomotiveDetails(loc.getId()));
      locomotiveManager.update(detailsReply);

      //Subscribe
      connection.sendMessage(EcosMessageFactory.subscribeLocomotive(loc.getId()));
    }

    addLocomotiveSpeedEventListener(locomotiveManager);
    addLocomotiveDirectionEventListener(locomotiveManager);
    addLocomotiveFunctionEventListener(locomotiveManager);
  }

  private void initAccessoryManager() {
    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getAccessories());
    accessoryManager = new AccessoryManager(this, reply);

    for (AccessoryBean accessory : this.accessoryManager.getAccessories().values()) {
      EcosMessage detailsReply = connection.sendMessage(EcosMessageFactory.getAccessoryDetails(accessory.getId()));
      accessoryManager.update(detailsReply);
      //Subscribe
      connection.sendMessage(EcosMessageFactory.subscribeAccessory(accessory.getId()));
    }
  }

  private void initFeedbackManager() {
    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getNumberOfFeedbackModules());
    feedbackManager = new FeedbackManager(this, reply);

    for (int i = 0; i < feedbackManager.getSize(); i++) {
      int moduleId = i + FeedbackManager.S88_OFFSET;
      //reply = 
      connection.sendMessage(EcosMessageFactory.getFeedbackModuleInfo(moduleId));

      //TODO: Start of day...
      //feedbackManager.update(reply);
      connection.sendMessage(EcosMessageFactory.subscribeFeedbackModule(moduleId));
      //Logger.trace("r: "+reply.getResponse());
    }
  }

  @Override
  public void disconnect() {
    try {
      if (this.connected) {
        this.connection.sendMessage(EcosMessageFactory.unSubscribeBaseObject());
        //TODO unsubscribe from all locomotives, accessories and sensors

      }
      if (this.eventMessageHandler != null) {
        this.eventMessageHandler.quit();
      }
      if (this.connected) {
        this.connection.close();
        this.connected = false;
      }
    } catch (Exception ex) {
      Logger.error(ex);
    }
  }

  @Override
  public InfoBean getCommandStationInfo() {
    InfoBean ib = new InfoBean(this.commandStationBean);
    if (this.ecosManager != null) {

      ib.setArticleNumber(ecosManager.getName().replace(this.ecosManager.getCommandStationType() + "-", ""));
      ib.setDescription(ecosManager.getName());
      ib.setArticleNumber(ecosManager.getName().replace(this.ecosManager.getCommandStationType() + "-", ""));
      ib.setSerialNumber(ecosManager.getSerialNumber());
      ib.setHardwareVersion(ecosManager.getHardwareVersion());
      ib.setSoftwareVersion(ecosManager.getApplicationVersion());
      ib.setHostname(getIp());
      if (ib.getIpAddress() == null) {
        ib.setIpAddress(getIp());
      }
    } else {
      ib.setDescription("Not Connected");
      ib.setHostname("Not Connected");
    }
    return ib;
  }

  //TODO: is the device in this form it is now really necessary?
  @Override
  public DeviceBean getDevice() {
    DeviceBean d = new DeviceBean();
    if (ecosManager != null) {
      d.setName(ecosManager.getName());
      d.setVersion(ecosManager.getHardwareVersion());
      d.setTypeName(ecosManager.getCommandStationType());
      d.setSerial(ecosManager.getSerialNumber());
    } else {
      d.setName("Not Connected");
    }
    return d;
  }

  //TODO: is the device in this form it is now really necessary?
  @Override
  public List<DeviceBean> getDevices() {
    List<DeviceBean> devices = new ArrayList<>();
    devices.add(getDevice());
    return devices;
  }

  @Override
  public String getIp() {
    if (this.connection != null && this.connection.isConnected()) {
      return this.connection.getControllerAddress().getHostAddress();
    } else {
      return null;
    }
  }

  /**
   * Query the System Status
   *
   * @return true the track power is on else off.
   */
  @Override
  public boolean isPower() {
    if (this.connected) {
      EcosMessage reply = this.connection.sendMessage(EcosMessageFactory.getPowerStatus());
      this.ecosManager.update(reply);
      this.power = "GO".equals(this.ecosManager.getStatus());
    } else {
      this.power = false;
    }
    return this.power;
  }

  /**
   * System Stop and GO When on = true then the GO command is issued; <br>
   * The command station activates the operation and supplies electrical energy.<br>
   * Any speed levels/functions that may still exist or have been saved will be sent again.<br>
   * When false the Stop command is issued;<br>
   * The command station stops operation on main and programming track.<br>
   * Electrical energy is no longer supplied.<br>
   * All speed levels/function values and settings are retained.
   *
   * @param on true Track power On else Off
   * @return true the Track power is On else Off
   */
  @Override
  public boolean power(boolean on) {
    if (this.connected) {
      EcosMessage reply = this.connection.sendMessage(EcosMessageFactory.setPowerStatus(on));
      ecosManager.update(reply);
      this.power = Ecos.GO.equals(ecosManager.getStatus());
      return power;
    } else {
      return false;
    }
  }

  @Override
  public void changeDirection(int locUid, LocomotiveBean.Direction direction) {
    Logger.trace("Changing Direction for " + locUid + " to " + direction);

    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getRequestLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    reply = connection.sendMessage(EcosMessageFactory.setLocomotiveSpeed(locUid, 0));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    reply = connection.sendMessage(EcosMessageFactory.setLocomotiveDirection(locUid, direction.getEcosValue()));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    reply = connection.sendMessage(EcosMessageFactory.getReleaseLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    LocomotiveSpeedEvent vme = new LocomotiveSpeedEvent(locUid, 0, this.commandStationBean.getId());
    LocomotiveDirectionEvent dce = new LocomotiveDirectionEvent(locUid, direction, this.commandStationBean.getId());

    //TODO: think about threading....
    fireDirectionEventListeners(dce);
    fireLocomotiveSpeedEventListeners(vme);
  }

  @Override
  public void changeVelocity(int locUid, int speed, LocomotiveBean.Direction direction) {
    Logger.trace("Changing speed for " + locUid + " to " + speed + " Direction " + direction);
    //Scale the speedstep
    int speedstep = speed / 8;

    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getRequestLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    reply = connection.sendMessage(EcosMessageFactory.setLocomotiveSpeed(locUid, speedstep));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    reply = connection.sendMessage(EcosMessageFactory.getReleaseLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    LocomotiveSpeedEvent vme = new LocomotiveSpeedEvent(locUid, speed, this.commandStationBean.getId());

    //TODO: think about threading....
    Logger.trace("Speed changed to: " + speed + " speedstep: " + speedstep + " for loco ID: " + locUid);
    fireLocomotiveSpeedEventListeners(vme);

    if (isVirtual()) {
      //When a locomotive has a speed change (>0) check if Auto mode is on.
      //When in Auto mode try to simulate the first sensor the locomotive is suppose to hit.
      if (AutoPilot.isAutoModeActive() && speed > 0) {
        simulator.simulateDriving(locUid, speed, direction);
      }
    }
  }

  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
    Logger.trace("Changing Function " + functionNumber + " for " + locUid + " to " + flag);

    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getRequestLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    reply = connection.sendMessage(EcosMessageFactory.setLocomotiveFunction(locUid, functionNumber, flag));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    reply = connection.sendMessage(EcosMessageFactory.getReleaseLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    //TODO: think about threading....
    Logger.trace("Function " + functionNumber + " changed to: " + (flag ? "On" : "Off") + " for loco ID: " + locUid);

    LocomotiveFunctionEvent lfe = new LocomotiveFunctionEvent(locUid, functionNumber, flag);
    fireFunctionEventListeners(lfe);
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    List<LocomotiveBean> locomotives = new ArrayList<>(this.locomotiveManager.getLocomotives().values());
    return locomotives;
  }

  @Override
  public Image getLocomotiveImage(String icon) {
    if (icon.startsWith("LOCO_TYPE")) {
      String[] urlParts = icon.split(","); //locodesc[LOCO_TYPE_E,IMAGE_TYPE_USER,2]
      if (urlParts.length == 3) {
        EcosHTTPConnection httpCon = EcosConnectionFactory.getHttpConnection();
        String type;
        if ("IMAGE_TYPE_INT".equals(urlParts[1])) {
          type = "internal";
        } else {
          type = "external";
        }
        String index = urlParts[2].toLowerCase();
        Image locImage = httpCon.getLocomotiveImage(type, index);
        return locImage;
      } else {
        Logger.trace("Unknown Ecos path: " + icon);
        return null;
      }
    } else {
      Logger.trace("Image already processed: " + icon);
      return null;
    }
  }

  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    return null;
  }

  @Override
  public boolean isSupportTrackMeasurements() {
    return false;
  }

  @Override
  public Map<Integer, ChannelBean> getTrackMeasurements() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchAccessory(Integer address, AccessoryBean.AccessoryValue value) {
    switchAccessory(address, value, this.defaultSwitchTime);
  }

  @Override
  public void switchAccessory(Integer address, AccessoryBean.AccessoryValue value, Integer switchTime) {
    //for now try to find the object id based on the address.
    //The protocol is not known so "accidents" can happen...
    Logger.trace("Using Address " + address + " to find the AccessoryId...");
    String id = this.accessoryManager.findId(address);
    if (id != null) {
      switchAccessory(id, value);
    } else {
      Logger.warn("Accessory with address " + address + " does not exist for the Ecos");
    }
  }

  @Override
  public void switchAccessory(String id, AccessoryBean.AccessoryValue value) {
    //if (this.power && this.connected) {
    Logger.trace("Changing Accessory " + id + " to " + value);
    int state;
    switch (value) {
      case GREEN ->
        state = 0;
      case RED ->
        state = 1;
      case WHITE ->
        state = 2;
      case YELLOW ->
        state = 3;
      default ->
        state = 0;
    }

    AccessoryBean accessory;
    Integer switchTime = null;
    if (this.accessoryManager.getAccessories().containsKey(id)) {
      accessory = this.accessoryManager.getAccessories().get(id);
      switchTime = accessory.getSwitchTime();
    } else {
      accessory = new AccessoryBean();
      accessory.setId(id);
      accessory.setCommandStationId(this.commandStationBean.getId());
      accessory.setSwitchTime(defaultSwitchTime);
    }
    if (switchTime == null) {
      switchTime = this.defaultSwitchTime;
    }

    EcosMessage reply = connection.sendMessage(EcosMessageFactory.setAccessory(id, state, switchTime));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    accessory.setAccessoryValue(value);
    AccessoryEvent ae = new AccessoryEvent(accessory);
    fireAccessoryEventListeners(ae);
  }

  @Override
  public List<AccessoryBean> getAccessories() {
    List<AccessoryBean> accessories = new ArrayList<>(this.accessoryManager.getAccessories().values());
    return accessories;
  }

  EcosConnection getConnection() {
    return connection;
  }

  @Override
  public DeviceBean getFeedbackDevice() {
    DeviceBean db = new DeviceBean();
    db.setArticleNumber(this.ecosManager.getName());
    db.setIdentifier("0x0");
    db.getBusLength(this.feedbackManager.getSize());
    db.setVersion(this.ecosManager.getApplicationVersion());
    db.setSerial(this.ecosManager.getSerialNumber());
    db.setTypeName("Link S88");

    ChannelBean cb = new ChannelBean();
    cb.setName(DeviceBean.BUS0);
    cb.setNumber(0);

    db.addSensorBus(0, cb);

    return db;
  }

  @Override
  public List<FeedbackModuleBean> getFeedbackModules() {
    List<FeedbackModuleBean> feedbackModules = new ArrayList<>(this.feedbackManager.getModules().values());
    return feedbackModules;
  }

  @Override
  public void fireSensorEventListeners(SensorEvent sensorEvent) {
    Logger.trace("SensorEvent: " + sensorEvent);
    if (sensorEventListeners != null && !sensorEventListeners.isEmpty()) {
      for (SensorEventListener listener : sensorEventListeners) {
        listener.onSensorChange(sensorEvent);
      }
    }
  }

  @Override
  public void simulateSensor(SensorEvent sensorEvent) {
    if (connection instanceof VirtualConnection virtualConnection) {
      virtualConnection.sendEvent(sensorEvent);
    }
  }

  void fireDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    if (directionEvent.isValid()) {
      for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
        listener.onDirectionChange(directionEvent);
      }
    }
  }

  void fireLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent speedEvent) {
    if (speedEvent.isValid()) {
      for (LocomotiveSpeedEventListener listener : this.locomotiveSpeedEventListeners) {
        listener.onSpeedChange(speedEvent);
      }
    }
  }

  void fireFunctionEventListeners(final LocomotiveFunctionEvent functionEvent) {
    if (functionEvent.isValid()) {
      for (LocomotiveFunctionEventListener listener : this.locomotiveFunctionEventListeners) {
        listener.onFunctionChange(functionEvent);
      }
    }
  }

  void fireAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    if (accessoryEventListeners != null && !accessoryEventListeners.isEmpty()) {
      for (AccessoryEventListener listener : accessoryEventListeners) {
        listener.onAccessoryChange(accessoryEvent);
      }
    }
  }

  void firePowerEventListeners(final PowerEvent powerEvent) {
    Logger.trace("Notify " + powerEventListeners.size() + " Power is " + (powerEvent.isPower() ? "On" : "Off"));
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }

  //Communication from Ecos reply messages to JCS
  private class EventHandler extends Thread {

    private boolean stop = false;
    private boolean quit = true;
    private BufferedReader reader;

    private final TransferQueue<EcosMessage> eventQueue;

    public EventHandler(EcosConnection connection) {
      eventQueue = connection.getEventQueue();
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
      this.quit = false;
      this.setName("ESU-ECOS-EVENT-HANDLER");

      Logger.trace("Event Handler Started...");

      while (isRunning()) {
        try {
          EcosMessage eventMessage = eventQueue.take();
          Logger.trace("# " + (eventMessage.isEvent() ? "-> " + eventMessage.getResponse() : eventMessage.getMessage() + " -> " + eventMessage.getResponse()));

          int id = eventMessage.getObjectId();
          switch (id) {
            case 1 -> {
              ecosManager.update(eventMessage);
            }
            case 10 -> {
              //Locomotive list changed
            }
            case 11 -> {
              //Accessory Manager change
              accessoryManager.updateManager(eventMessage);

            }
            default -> {
              //Events
              if (id >= 100 && id < 1000) {
                //Feedback event
                feedbackManager.update(eventMessage);
              } else if (id >= 1000 && id < 9999) {
                locomotiveManager.update(eventMessage);
              } else if (id >= 20000 && id < 29999) {
                accessoryManager.update(eventMessage);
              } else {
                Logger.trace(eventMessage.getMessage() + " " + eventMessage.getResponse());
              }
            }
          }
        } catch (InterruptedException ex) {
          Logger.error(ex);
        }
      }

      Logger.debug("Stop receiving");
      try {
        reader.close();
      } catch (IOException ex) {
        Logger.error(ex);
      }
      stop = true;
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////
  /// @param a 
  // For testing only
  public static void main(String[] a) {

    System.setProperty("message.debug", "true");
    //Discover the ECoS using mdns
    InetAddress ecosAddr = EcosConnectionFactory.discoverEcos();
    String ip = ecosAddr.getHostAddress();

    if (1 == 1) {
      CommandStationBean csb = new CommandStationBean();
      csb.setId("esu-ecos");
      csb.setDescription("ESU-ECOS");
      csb.setClassName("jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl");
      csb.setConnectVia("NETWORK");
      //csb.setIpAddress("192.168.1.110");
      csb.setIpAddress(ip);
      csb.setNetworkPort(EcosConnection.DEFAULT_NETWORK_PORT);

      csb.setDefault(true);
      csb.setIpAutoConfiguration(false);
      csb.setEnabled(true);
      csb.setShortName("ECoS");
      csb.setDecoderControlSupport(true);
      csb.setAccessorySynchronizationSupport(true);
      csb.setFeedbackSupport(true);
      csb.setLocomotiveFunctionSynchronizationSupport(true);
      csb.setLocomotiveImageSynchronizationSupport(true);
      csb.setLocomotiveSynchronizationSupport(true);
      csb.setProtocols("DCC,MM,MFX");

      EsuEcosCommandStationImpl cs = new EsuEcosCommandStationImpl(csb);

      boolean connected = cs.connect();
      //cs.power(true);
      //Logger.trace("Power is: " + (cs.isPower() ? "On" : "Off"));

      if (connected) {

//        Logger.trace(cs.ecosManager);
//        boolean power = cs.isPower();
//        Logger.trace("1 Power is " + (power ? "On" : "Off"));
//
//        cs.pause(1000);
//
//        power = cs.power(true);
//        Logger.trace("2 Power is " + (power ? "On" : "Off"));
//
//        cs.pause(1000);
//
//        power = cs.power(false);
//        Logger.trace("3 Power is " + (power ? "On" : "Off"));
//
//        cs.pause(1000);
//
//        power = cs.power(true);
//        Logger.trace("4 Power is " + (power ? "On" : "Off"));
        //EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(26)"));
        //Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Map<String, Object> values = reply.getValueMap();
//        for (String key : values.keySet()) {
//          Logger.trace("Key: " + key+ " Value: "+values.get(key));
//        }
//        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(10, name, addr, protocol)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Map<String, Object> values = reply.getValueMap();
//        for (String key : values.keySet()) {
//        Logger.trace("Key: " + key + " Value: " + values.get(key));
//        }
//        reply = cs.connection.sendMessage(new EcosMessage("queryObjects(10, name)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        
//        values = reply.getValueMap();
//        for (String key : values.keySet()) {
//          Logger.trace("Key: " + key+ " Value: "+values.get(key));
//        }
//        reply = cs.connection.sendMessage(new EcosMessage("queryObjects(10)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        values = reply.getValueMap();
//        for (String key : values.keySet()) {
//          Logger.trace("ID:" + key);
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(10, "+ key+")"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        }
//        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("request(1005, control, force)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("set(1005, speed[100])"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        cs.pause(2000);
//
//        reply = cs.connection.sendMessage(new EcosMessage("set(1005, speed[10])"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("release(1005, control)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Map<String, Object> values = reply.getValueMap();
//        for (String key : values.keySet()) {
//        Logger.trace("Key: " + key + " Value: " + values.get(key));
//        }
//        reply = cs.connection.sendMessage(new EcosMessage("get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20002, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20003, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20004, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20005, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(27,size)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(65000, maxlimit,limit)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//         //reply = cs.connection.sendMessage(new EcosMessage("help(65000,attribute)"));
//        //Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        
//        
//        reply = cs.connection.sendMessage(new EcosMessage("request(65000,volt"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
        //reply = cs.connection.sendMessage(new EcosMessage("get(1002, name, addr, protocol, locodesc, dir,speed, speedstep, speedindicator,func)"));      
        //Logger.trace(reply.getMessage()+" ->\n"+reply.getResponse());
        //reply = cs.connection.sendMessage(new EcosMessage("request(10, view)"));      
        //Logger.trace(reply.getMessage()+" ->\n"+reply.getResponse());
        //reply = cs.connection.sendMessage(new EcosMessage("request(1002, view)"));      
        //Logger.trace(reply.getMessage()+" ->\n"+reply.getResponse());
        //reply = cs.connection.sendMessage(new EcosMessage("queryObjects(11, name1,name2,name3, addr, protocol, objectclass)"));       
        //Logger.trace(reply.getMessage()+" ->\n"+reply.getResponse());
        cs.pause(100000);
        //cs.connection.sendMessage(EcosMessageFactory.unSubscribeBaseObject());
        //cs.connection.sendMessage(EcosMessageFactory.unSubscribeFeedbackManager());
        cs.disconnect();
      }
      System.exit(0);

    }

  }

}
