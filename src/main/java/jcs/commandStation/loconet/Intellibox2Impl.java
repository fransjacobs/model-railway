/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.loconet;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.automation.DriveSimulator;
import jcs.commandStation.automation.RailController;
import static jcs.commandStation.automation.RailController.TAG;
import jcs.commandStation.entities.Device;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.loconet.connection.LoconetConnection;
import jcs.commandStation.loconet.connection.LoconetConnectionFactory;
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 * Intellibox2Impl IntelliBox 2 implementation
 */
public class Intellibox2Impl extends AbstractController implements DecoderController, AccessoryController, FeedbackController, ConnectionEventListener {

  LoconetConnection loconet;
  ThreadGroup threadGroup;
  private EventMessageHandler eventMessageHandler;

  private final AccessoryManager accessoryManager;
  private final LocomotiveManager locomotiveManager;
  private final FeedbackManager feedbackManager;

  static final String COMMAND_STATION_ID = "intellibox2";

  private DriveSimulator simulator;

  private final List<Device> devices;

  private volatile boolean connectionListenerRegistered = false;

  public Intellibox2Impl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public Intellibox2Impl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    threadGroup = new ThreadGroup("INTELLIBOX2");
    devices = new ArrayList<>();

    this.executor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "INBX-LN-BG-QUERY");
      thread.setDaemon(true);
      return thread;
    });

    accessoryManager = new AccessoryManager(this);
    locomotiveManager = new LocomotiveManager(this);
    feedbackManager = new FeedbackManager(this);
  }

  @Override
  public synchronized boolean connect() {
    if (connected && loconet != null && loconet.isConnected()) {
      return true;
    }
    loconet = LoconetConnectionFactory.acquireConnection(2000);
    this.connected = loconet != null && loconet.isConnected();

    if (connected) {
      if (!isVirtual() && !connectionListenerRegistered) {
        LoconetConnectionFactory.getInstance().registerConnectionListener(this);
        connectionListenerRegistered = true;
      }

      eventMessageHandler = new EventMessageHandler(loconet);
      eventMessageHandler.start();

      feedbackManager.refresh();

      getDevices();

      if (isVirtual()) {
        simulator = new DriveSimulator();
        Logger.info("Intellibox 2 Virtual Mode Enabled!");
      }

      accessoryManager.start();

      //refresh the accessories in the background
      executor.execute(() -> accessoryManager.refresh());
      //refresh the locomotives in the background
      executor.execute(() -> locomotiveManager.refresh());
    }

    return connected;
  }

  @Override
  public void disconnect() {
    this.devices.clear();
    if (eventMessageHandler != null) {
      eventMessageHandler.quit();
      try {
        eventMessageHandler.join(1000);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      eventMessageHandler = null;
    }

    if (connectionListenerRegistered) {
      LoconetConnectionFactory.getInstance().unRegisterConnectionListener(this);
      connectionListenerRegistered = false;
    }

    locomotiveManager.shutdown();
    accessoryManager.shutdown();

    LoconetConnectionFactory.closeConnection();
    connected = false;
  }

  @Override
  public InfoBean getCommandStationInfo() {
    InfoBean ib = new InfoBean(commandStationBean);
    if (connected && !devices.isEmpty()) {
      Device intellibox2 = devices.get(0);
      //ib.setArticleNumber();
      ib.setDescription("Intellibox 2");
      ib.setSerialNumber(intellibox2.getSerialNumber());
      ib.setProductName("Intellibox 2");
      ib.setSoftwareVersion(intellibox2.getSoftwareVersion());
    } else {
      ib.setDescription("Not Connected");
      Logger.warn("NOT Connected!");
    }
    return ib;
  }

  String getSerialNumber() {
    LoconetMessage request = LoconetMessageFactory.requestSerialNumber();

    Predicate<LoconetMessage> serialNumberReplyMatcher = msg -> LoconetMessageParser.isIntelliboxPeerReply(msg, 0x09);
    LoconetMessage reply = loconet.sendMessageAwaitEchoAndReply(request, serialNumberReplyMatcher, 500);

    if (reply == null) {
      Logger.warn("No reply received for Serial Number Request");
      return null;
    }
    String serialNumber = LoconetMessageParser.parseSerialNumber(reply);

    Logger.trace("SerialNumber: {}", serialNumber);
    return serialNumber;
  }

  String getSoftwareVersion() {
    LoconetMessage request = LoconetMessageFactory.requestSoftwareVersion();
    Predicate<LoconetMessage> softwareVersionReplyMatcher = msg -> LoconetMessageParser.isIntelliboxPeerReply(msg, 0x08);

    LoconetMessage reply = loconet.sendMessageAwaitEchoAndReply(request, softwareVersionReplyMatcher, 500);
    if (reply == null) {
      Logger.warn("No reply received for Software Version Request");
      return null;
    }

    String softwareVersion = LoconetMessageParser.parseSoftwareVersion(reply);
    Logger.trace("Sofware Version: {}", softwareVersion);
    return softwareVersion;
  }

  @Override
  public List<Device> getDevices() {
    if (devices.isEmpty()) {
      Device ib = new Device();
      ib.setId("intellibox2");
      ib.setName("Intellibox 2");
      devices.add(ib);
    }
    Device ib = devices.get(0);

    if (ib.getSerialNumber() == null) {
      ib.setSerialNumber(getSerialNumber());
    }

    if (ib.getSoftwareVersion() == null) {
      ib.setSoftwareVersion(getSoftwareVersion());
    }

    return new ArrayList<>(devices);
  }

  @Override
  public String getIp() {
    return null;
  }

  @Override
  public boolean power(boolean on) {
    power = on;
    if (loconet != null) {
      LoconetMessage reply;
      if (power) {
        reply = loconet.sendMessage(LoconetMessageFactory.powerOn());
      } else {
        reply = loconet.sendMessage(LoconetMessageFactory.powerOff());
      }

      if (reply != null) {
        Logger.trace("Processing reply {}", reply.toString());
        PowerEvent spe = LoconetMessageParser.parsePowerEvent(reply);
        notifyPowerEventListeners(spe);
      }
    }
    Logger.tag(TAG).debug("CommandStation Track Power is {}", (power ? "On" : "Off"));
    return power;
  }

  void notifyPowerEventListeners(final PowerEvent powerEvent) {
    power = powerEvent.isPower();
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }

  @Override
  public void changeDirection(int address, LocomotiveBean.Direction direction) {
    locomotiveManager.changeDirection(address, direction);
  }

  @Override
  public void changeVelocity(int address, int speed, LocomotiveBean.Direction direction) {
    locomotiveManager.changeVelocity(address, speed, direction);

    if (isVirtual()) {
      //When a locomotive has a speed change (> 0) check if AutoMode is on.
      if (RailController.getInstance().isAutoModeActive() && speed > 0 && simulator != null) {
        simulator.simulateDriving(address, speed, direction);
      }
    }
  }

  @Override
  public void changeFunctionValue(int address, int functionNumber, boolean flag) {
    locomotiveManager.changeFunctionValue(address, functionNumber, flag);
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    return new ArrayList<>(locomotiveManager.getLocomotives().values());
  }

  @Override
  public void refreshLocomotives() {
    this.locomotiveManager.refresh();
  }

  @Override
  public Image getLocomotiveImage(String icon) {
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

  List<AccessoryEventListener> getAccessoryEventListeners() {
    return this.accessoryEventListeners;
  }

  @Override
  public void switchAccessory(Integer address, String protocol, AccessoryBean.AccessoryValue value, Integer switchTime) {
    if (power && connected) {
      accessoryManager.switchAccessory(address, protocol, value, switchTime);
    } else {
      Logger.warn("Trackpower is OFF! Can't switch Accessory: " + address + " to: " + value + "!");
    }
  }

  @Override
  public List<AccessoryBean> getAccessories() {
    return accessoryManager.getAccessories();
  }

  @Override
  public void fireAllSensorEventsListeners(final SensorEvent sensorEvent) {
    List<AllSensorEventsListener> snapshot = new ArrayList<>(allSensorEventsListeners);
    for (AllSensorEventsListener listener : snapshot) {
      listener.onSensorChange(sensorEvent);
    }
  }

  void notifyLocomotiveFunctionEventListeners(final LocomotiveFunctionEvent functionEvent) {
    for (LocomotiveFunctionEventListener listener : this.locomotiveFunctionEventListeners) {
      listener.onFunctionChange(functionEvent);
    }
  }

  void notifyLocomotiveDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
      listener.onDirectionChange(directionEvent);
    }
  }

  void notifyLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent speedEvent) {
    for (LocomotiveSpeedEventListener listener : this.locomotiveSpeedEventListeners) {
      listener.onSpeedChange(speedEvent);
    }
  }

  @Override
  public List<FeedbackModule> getFeedbackModules() {
    return this.feedbackManager.getFeedbackModules();
  }

  @Override
  public SensorBean getSensorStatus(SensorBean sensorBean) {
    return this.feedbackManager.getSensorStatus(sensorBean);
  }

  public void RequestSensorStatuses() {
    this.feedbackManager.requestCurrentSensorStates();
  }

  @Override
  public void simulateSensor(SensorEvent sensorEvent) {
//    if (isVirtual() virtualConnection) {
//      virtualConnection.sendEvent(sensorEvent);
//    }
  }

  @Override
  public synchronized void onConnectionChange(ConnectionEvent event) {
    if (event.isConnected()) {

      LoconetConnection newConnection = LoconetConnectionFactory.acquireConnection(1000);
      if (newConnection == null || !newConnection.isConnected()) {
        connected = false;
        return;
      }

      if (eventMessageHandler != null) {
        eventMessageHandler.quit();
        try {
          eventMessageHandler.join(1000);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }

      loconet = newConnection;
      eventMessageHandler = new EventMessageHandler(loconet);
      eventMessageHandler.start();

      connected = true;
      Logger.trace("Reconnected...");
    } else {
      connected = false;
      if (eventMessageHandler != null) {
        eventMessageHandler.quit();
        eventMessageHandler = null;
      }
      Logger.warn("Disconnected...");
    }
  }

  private class EventMessageHandler extends Thread {

    private volatile boolean running = false;
    private final BlockingQueue<LoconetMessage> messagesQueue;

    EventMessageHandler(LoconetConnection connection) {
      super(threadGroup, "IB-LN-MSG-HANDLER");
      messagesQueue = connection.getMessageQueue();
    }

    void quit() {
      this.running = false;
    }

    boolean isRunning() {
      return this.running;
    }

    @Override
    public void run() {
      this.running = true;
      Logger.trace("Event Handler Started...");

      while (isRunning()) {
        try {
          try {
            LoconetMessage message = messagesQueue.poll(10, TimeUnit.MILLISECONDS);
            //Logger.trace("# " + eventMessage);

            if (message != null) {
              int opcode = message.getOpcode();
              int length = message.getLength();

              switch (opcode) {
                case LoconetMessage.OPC_GPON -> {
                  Logger.trace("Power On Event RX: {}", message);
                  PowerEvent spe = LoconetMessageParser.parsePowerEvent(message);
                  notifyPowerEventListeners(spe);
                }
                case LoconetMessage.OPC_GPOFF -> {
                  Logger.trace("Power Off Event RX: {}", message);
                  PowerEvent spe = LoconetMessageParser.parsePowerEvent(message);
                  notifyPowerEventListeners(spe);
                }
                case LoconetMessage.OPC_IDLE -> {
                  Logger.trace("Idle (HALT) Event RX: {}", message);
                  PowerEvent spe = LoconetMessageParser.parsePowerEvent(message);
                  notifyPowerEventListeners(spe);
                }
                case LoconetMessage.OPC_BUSY -> {
                  Logger.trace("Master Busy Event RX: {}", message);
                }
                case LoconetMessage.OPC_SW_REQ -> {
                  //Switch has changed
                  Logger.trace("AccessoryEvent RX: {}", message);
                  accessoryManager.update(message);
//                  AccessoryBean ab = LoconetMessageParser.parseSwitchEvent(message);
//                  accessoryManager.update(ab);
                }
                case LoconetMessage.OPC_INPUT_REP -> {
                  Logger.trace("SensorEvent RX: {}", message);
                  feedbackManager.update(message);
//                  
//                  SensorBean sb = LoconetMessageParser.parseSensorEvent(message);
//                  if (sb != null) {
//                    Logger.trace("Sensor: {} Value: {} ", sb.getId(), sb.getStatus());
//                    SensorEvent sme = new SensorEvent(sb);
//                    fireAllSensorEventsListeners(sme);
//                  }
                }
                case LoconetMessage.OPC_SW_REP -> {
                  //Switch State Report
                  Logger.trace("Accessory State RX: {}", message);
                  AccessoryBean ab = LoconetMessageParser.parseSwitchStateEvent(message);
                  accessoryManager.update(ab);
                }
                case LoconetMessage.OPC_LOCO_DIRF -> {
                  //Locomotive direction and functions
                  Logger.trace("LocomotiveDIRF: {}", message);
                  locomotiveManager.updateLocomotiveDirf(message);
                }
                case LoconetMessage.OPC_LOCO_SND -> {
                  //Locomotive and functions
                  Logger.trace("LocomotiveSND: {}", message);
                  locomotiveManager.updateLocomotiveSnd(message);
                }
                case LoconetMessage.OPC_LOCO_SPD -> {
                  //Locomotive direction and functions
                  Logger.trace("LocomotiveSPD: {}", message);
                  locomotiveManager.updateLocomotiveSpeed(message);
                }
                case LoconetMessage.OPC_PEER_XFER -> {
                  Logger.trace("OPC_PEER_XFER: {}", message);

//                  try {
//                    int numberOfModules = feedbackManager.parseLNCVReadReply(message, FeedbackManager.DEFAULT_ARTICLE, FeedbackManager.LNCV_MODULE_COUNT);
//                    feedbackManager.setNumberOfFeedbackModules(numberOfModules);
//                    Logger.trace("Intellibox S88 number of modules: {}", numberOfModules);
//
//                  } catch (IllegalArgumentException ex) {
//                    Logger.trace("Ignoring non-LNCV-read peer transfer: {}", message);
//                  }
                }

//                default -> {
//                }
//              }
                case LoconetMessage.OPC_LONG_ACK -> {
                  Logger.trace("Aknowlegde RX: {}", message);
                }
                default -> {
                  Logger.trace("%RX: {} Opcode: {} Lenght: {}", message.toString(), message.getHexOpcode(), length);
                }
              }
            }
          } catch (InterruptedException ex) {
            Logger.error(ex);
            Thread.currentThread().interrupt();
          }
        } catch (Exception e) {
          Logger.error("Error in Handling Thread. Cause: " + e.getMessage());
        }
      }
      Logger.debug("Stop Event handling");

    }

  }

  ////////For first steps testing only ///
  public static void main(String[] a) {
    System.setProperty("tinylog.writer.level", "trace");
    RunUtil.loadExternalProperties();

    CommandStationBean csb = new CommandStationBean();
    csb.setId("intellibox2");
    csb.setDescription("Uhlenbrock Intellibox 2");
    csb.setShortName("Loconet");
    csb.setClassName("jcs.commandStation.loconet.Intellibox2Impl");
    csb.setConnectVia("SERIAL");
    csb.setSerialPort("AUTO");
    //csb.setIpAddress("0.0.0.0");
    csb.setDecoderControlSupport(true);
    csb.setAccessorySynchronizationSupport(false);
    csb.setFeedbackSupport(true);
    csb.setLocomotiveFunctionSynchronizationSupport(false);
    csb.setLocomotiveImageSynchronizationSupport(false);
    csb.setLocomotiveSynchronizationSupport(false);
    //csb.setNetworkPort(0);
    csb.setProtocols("DCC,MM");
    csb.setDefault(true);
    csb.setEnabled(true);
    csb.setVirtual(false);

    Intellibox2Impl intellibox2 = new Intellibox2Impl(csb);
    intellibox2.debug = true;

    Logger.debug((intellibox2.connect() ? "Connected" : "NOT Connected"));

    //MeasurementListener mel = new MeasurementListener();
    if (intellibox2.isConnected()) {

      //Lets power ON
      //intellibox2.power(true);
      //System.out.println("\n\n\n");
      intellibox2.pause(2000);

      //intellibox2.readConfigurations();
      //Lets power Off
      //intellibox2.power(false);
      //intellibox2.pause(2000);
      //intellibox2.accessoryManager.queryAccessory(1);
//      intellibox2.switchAccessory(1, "dcc", AccessoryValue.GREEN, 100);
//      intellibox2.pause(2000);
//      intellibox2.switchAccessory(1, "dcc", AccessoryValue.RED, 100);
      //intellibox2.locomotiveManager.registerSlots();
      //intellibox2.getDevices();
      //intellibox2.RequestSensorStatuses();
      intellibox2.pause(200000);
      //Lets power Off
      intellibox2.power(false);

      intellibox2.disconnect();

    }
  }

}
