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
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

public class EsuEcosCommandStationImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController {

  private int defaultSwitchTime;
  private EcosConnection connection;
  private EventHandler eventMessageHandler;

  private EcosManager baseObject;
  private FeedbackManager feedbackManager;
  private LocomotiveManager locomotiveManager;

  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    defaultSwitchTime = Integer.getInteger("default.switchtime", 300);
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
  public boolean connect() {
    if (!connected) {
      Logger.trace("Connecting to a ESU ECoS Command Station...");
      if (executor == null || executor.isShutdown()) {
        executor = Executors.newCachedThreadPool();
      }

      if (commandStationBean == null) {
        Logger.error("No ESU ECoS Command Station Configuration set!");
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
          //try to discover the ECoS
          InetAddress ecosAddr = EcosConnectionFactory.discoverEcos();
          String ip = ecosAddr.getHostAddress();
          commandStationBean.setIpAddress(ip);
          EcosConnectionFactory.writeLastUsedIpAddressProperty(commandStationBean.getIpAddress());
          canConnect = ip != null;
          if (!canConnect) {
            Logger.error("Can't connect; IP Address not set");
          }
        }
      }

      if (canConnect) {
        connection = EcosConnectionFactory.getConnection();

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
            //Obtain some info about the ECoS
            setupBaseObject();

            setupFeedbackManager();
            Logger.trace("There are " + this.feedbackManager.getSize() + " feedback modules");

            setupLocomotiveManager();
            Logger.trace("There are " + this.locomotiveManager.getSize() + " locomotives");

//            //Create Info
//            this.infoBean = new InfoBean();
//            this.infoBean.setProductName(commandStationBean.getDescription());
//            this.infoBean.setArticleNumber(commandStationBean.getShortName());
//
//              JCS.logProgress("Try to switch Power ON...");
//              //Switch one the power
//              String response = connection.sendMessage(DccExMessageFactory.changePowerRequest(true));
//              Logger.trace(response);
//            }
//            Logger.trace("Connected with: " + (this.mainDevice != null ? this.mainDevice.getName() : "Unknown"));
//            JCS.logProgress("Power is " + (this.power ? "On" : "Off"));
          } else {
            Logger.warn("Can't connect with a ESU ECoS Command Station!");
            JCS.logProgress("Can't connect with ESU ECoS Command Station!");
          }
        }
      }
    }
    return this.connected;

  }

  private void setupBaseObject() {
    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getBaseObject());
    baseObject = new EcosManager(this, reply);

    //Start the EventHandler
    eventMessageHandler = new EventHandler(this.connection);
    eventMessageHandler.start();

    connection.sendMessage(EcosMessageFactory.subscribeBaseObject());
  }

  private void setupFeedbackManager() {
    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getNumberOfFeedbackModules());
    feedbackManager = new FeedbackManager(reply);

    for (int i = 0; i < feedbackManager.getSize(); i++) {
      int moduleId = i + FeedbackManager.S88_OFFSET;
      reply = connection.sendMessage(EcosMessageFactory.getFeedbackModuleInfo(moduleId));

      String state = reply.getValueMap().get(Ecos.STATE).toString();
      String ports = reply.getValueMap().get(Ecos.PORTS).toString();
      //TODO: we now know the begin state so refect that in the feedback modules....

      //Logger.trace("state: "+state+" ports: "+ports);
      connection.sendMessage(EcosMessageFactory.subscribeFeedbackModule(moduleId));
      //Logger.trace("r: "+reply.getResponse());
    }
  }

  private void setupLocomotiveManager() {
    EcosMessage reply = connection.sendMessage(EcosMessageFactory.getLocomotives());
    locomotiveManager = new LocomotiveManager(this, reply);

    for (LocomotiveBean loc : this.locomotiveManager.getLocomotives().values()) {
      EcosMessage detailsReply = connection.sendMessage(EcosMessageFactory.getLocomotiveDetails(loc.getId()));
      locomotiveManager.update(detailsReply);

      //Subscribe
      connection.sendMessage(EcosMessageFactory.subscribeLocomotive(loc.getId()));

      //Also start listening for Event for this locomotive
    }
    addLocomotiveSpeedEventListener(locomotiveManager);
    addLocomotiveDirectionEventListener(locomotiveManager);
    addLocomotiveFunctionEventListener(locomotiveManager);
  }

  @Override
  public void disconnect() {
    try {
      if (this.connected) {
        this.connection.sendMessage(EcosMessageFactory.unSubscribeBaseObject());

      }
      this.eventMessageHandler.quit();
      this.connection.close();
    } catch (Exception ex) {
      Logger.error(ex);
    }
  }

  @Override
  public InfoBean getCommandStationInfo() {
    InfoBean ib = new InfoBean(this.commandStationBean);

    ib.setArticleNumber(this.baseObject.getName().replace(this.baseObject.getCommandStationType() + "-", ""));
    ib.setDescription(this.baseObject.getName());
    ib.setArticleNumber(this.baseObject.getName().replace(this.baseObject.getCommandStationType() + "-", ""));
    ib.setSerialNumber(this.baseObject.getSerialNumber());
    ib.setHardwareVersion(this.baseObject.getHardwareVersion());
    ib.setSoftwareVersion(this.baseObject.getApplicationVersion());
    ib.setHostname(this.getIp());
    return ib;
  }

  //TODO: is the device in the form it is now really necessary?
  @Override
  public DeviceBean getDevice() {
    DeviceBean d = new DeviceBean();
    d.setName(baseObject.getName());
    d.setVersion(baseObject.getHardwareVersion());
    d.setTypeName(baseObject.getCommandStationType());
    d.setSerial(baseObject.getSerialNumber());
    return d;
  }

  @Override
  public List<DeviceBean> getDevices() {
    throw new UnsupportedOperationException("Not supported yet.");
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
      this.baseObject.update(reply);
      this.power = "GO".equals(this.baseObject.getStatus());
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
      baseObject.update(reply);

      this.power = on; //Ecos.GO.equals(baseObject.getStatus());

      PowerEvent pe = new PowerEvent(this.power);
      //notifyPowerEventListeners(pe);
      fireAllPowerEventListeners(pe);

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
     //locodesc[LOCO_TYPE_E,IMAGE_TYPE_USER,2]
    
    ////http://192.168.1.110/loco/image?type=internal&index=0
    return null;
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchAccessory(Integer address, AccessoryBean.AccessoryValue value, Integer switchTime) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<AccessoryBean> getAccessories() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public DeviceBean getFeedbackDevice() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<FeedbackModuleBean> getFeedbackModules() {
    List<FeedbackModuleBean> feedbackModules = new ArrayList<>();
    //this.feedbackManager.getModules()

    return feedbackModules;
  }

  @Override
  public void fireSensorEventListeners(SensorEvent sensorEvent) {
    Logger.trace("SensorEvent: " + sensorEvent);
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

//  private void notifyPowerEventListeners(final PowerEvent powerEvent) {
//    executor.execute(() -> fireAllPowerEventListeners(powerEvent));
//  }
  private void fireAllPowerEventListeners(final PowerEvent powerEvent) {
    Logger.trace("Notify " + powerEventListeners.size() + " Power is " + (powerEvent.isPower() ? "On" : "Off"));
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }

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

          Logger.trace("###-> " + eventMessage.getMessage() + " " + eventMessage.getResponse());

          int id = eventMessage.getObjectId();

          switch (id) {
            case 1 -> {
              String prevStatus = baseObject.getStatus();
              baseObject.update(eventMessage);
              Logger.trace(baseObject);

              if (!baseObject.getStatus().equals(prevStatus)) {
                power = "GO".equals(baseObject.getStatus());
                Logger.trace("Power changed to: " + (power ? "On" : "Off"));
                power = "GO".equals(baseObject.getStatus());
                PowerEvent pe = new PowerEvent(power);
                fireAllPowerEventListeners(pe);
              }

            }
            default -> {
              //Events
              if (id >= 100 && id < 1000) {
                //Feedback event
                feedbackManager.update(eventMessage);
              } else if (id >= 1000 && id < 9999) {
                locomotiveManager.update(eventMessage);
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

//        Logger.trace(cs.baseObject);
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


        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)"));
        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Map<String, Object> values = reply.getValueMap();
//        for (String key : values.keySet()) {
//        Logger.trace("Key: " + key + " Value: " + values.get(key));
//        }
//        reply = cs.connection.sendMessage(new EcosMessage("queryObjects(10, name)"));
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
//      queryObjects(10, name, addr, protocol, dir, speed,  locodesc, speedstep, func, funcset, favorite) ->
//        queryObjects(10, objectclass, addr, name, protocol, locodesc, favorite, control) ->
//<REPLY queryObjects(10, objectclass, addr, name, protocol, locodesc, favorite, control)>
//  1002 name["FS236-002"] addr[14] protocol[DCC28] objectclass[loco] locodesc[LOCO_TYPE_DIESEL,IMAGE_TYPE_INT,14] favorite[1] control[other]
//  1003 name["NS 6505"] addr[8] protocol[DCC28] objectclass[loco] locodesc[LOCO_TYPE_DIESEL,IMAGE_TYPE_INT,21] favorite[1] control[other]
//<END 0 (OK)>
//<REPLY get(31, link[0])>
//31 link[0,0,20000,R0]
//31 link[0,1,20001,R0]
//31 link[0,2,0]
//31 link[0,3,0]
//31 link[0,4,0]
//31 link[0,5,0]
//31 link[0,6,0]
//31 link[0,7,0]
//31 link[0,8,0]
//31 link[0,9,0]
//31 link[0,10,0]
//31 link[0,11,0]
//31 link[0,12,0]
//31 link[0,13,0]
//31 link[0,14,0]
//31 link[0,15,0]
//<END 0 (OK, but no newline after packet)>
//<REPLY queryObjects(27)>
//65000
//<END 0 (OK, but no newline after packet)>
//<REPLY get(100, ports, state, railcom, control)>
//100 ports[16]
//100 state[0x0]
//100 control[none]
//<END 0 (OK, but no newline after packet)>
//
//<REPLY get(100, railcom[0])>
//100 railcom[0,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//
//<REPLY get(100, railcom[1])>
//100 railcom[1,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//
//<REPLY get(100, railcom[2])>
//100 railcom[2,0,0]
//
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[3])>
//100 railcom[3,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//
//<REPLY get(100, railcom[4])>
//100 railcom[4,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[5])>
//100 railcom[5,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[6])>
//100 railcom[6,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[7])>
//100 railcom[7,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[8])>
//100 railcom[8,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[9])>
//100 railcom[9,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[10])>
//100 railcom[10,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[11])>
//100 railcom[11,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[12])>
//100 railcom[12,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[13])>
//100 railcom[13,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[14])>
//100 railcom[14,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//<REPLY get(100, railcom[15])>
//100 railcom[15,0,0]
//<END 0 (OK, but no RailCom port at 18)>
//
//<EVENT 100>
//100 state[0x10]
//<END 0 (OK)>
//      
//<REPLY get(1, status2)>
//1 status2[ALL]
//<END 0 (OK, but no newline after packet)>
//
//<REPLY request(1002, view)>
//<END 0 (OK, but no newline after packet)>
//
//<REPLY request(1002, view, attribute[realspeed])>
//<END 0 (OK, but no newline after packet)>
//
//<REPLY get(1002, multi, addressconflict, speedindicator, speedstep, speed, dir, sniffer, realspeed, autoregister, active)>
//1002 multi[0]
//1002 addressconflict[0]
//1002 speedindicator[0]
//1002 speedstep[0]
//1002 speed[0]
//1002 dir[1]
//1002 sniffer[0]
//1002 realspeed[0]
//1002 autoregister[railcomplus]
//1002 active[0]
//<END 0 (OK, but no newline after packet)>
//<REPLY request(1003, view)>
//<END 0 (OK, but no newline after packet)>
//<REPLY request(1003, view, attribute[realspeed])>
//<END 0 (OK, but no newline after packet)>
//
//<REPLY get(1003, multi, addressconflict, speedindicator, speedstep, speed, dir, sniffer, realspeed, autoregister, active)>
//1003 multi[0]
//1003 addressconflict[0]
//1003 speedindicator[0]
//1003 speedstep[0]
//1003 speed[0]
//1003 dir[0]
//1003 sniffer[0]
//1003 realspeed[0]
//1003 autoregister[railcomplus]
//1003 active[0]
//<END 0 (OK, but no newline after packet)>
//<REPLY request(20000, view)>
//<END 0 (OK, but no newline after packet)>
//<REPLY get(20000, addr, addrext, protocol, symbol, mode, duration, gates, state, control)>
//20000 addr[1]
//20000 addrext[1g,1r]
//20000 protocol[DCC]
//20000 symbol[1]
//20000 mode[SWITCH]
//20000 duration[250]
//20000 gates[2]
//20000 state[0]
//20000 control[none]
//<END 0 (OK, but no newline after packet)>
//
//<REPLY request(20001, view)>
//<END 0 (OK, but no newline after packet)>
//<REPLY get(20001, addr, addrext, protocol, symbol, mode, duration, gates, state, control)>
//20001 addr[2]
//20001 addrext[2g,2r]
//20001 protocol[DCC]
//20001 symbol[0]
//20001 mode[SWITCH]
//20001 duration[250]
//20001 gates[2]
//20001 state[0]
//20001 control[none]
//<END 0 (OK, but no newline after packet)>
//
//
//        cs.pause(100000);
        //cs.connection.sendMessage(EcosMessageFactory.unSubscribeBaseObject());
        //cs.connection.sendMessage(EcosMessageFactory.unSubscribeFeedbackManager());
        cs.disconnect();
      }
      System.exit(0);

    }

  }

}
