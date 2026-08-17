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
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import static jcs.commandStation.automation.RailController.TAG;
import jcs.commandStation.entities.Device;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.loconet.connection.LoconetConnection;
import jcs.commandStation.loconet.connection.LoconetConnectionFactory;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
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
  private ThreadGroup threadGroup;
  private EventMessageHandler eventMessageHandler;

  private final AccessoryManager accessoryManager;

  public Intellibox2Impl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public Intellibox2Impl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    threadGroup = new ThreadGroup("INTELLIBOX2");
    this.executor = Executors.newCachedThreadPool();
    this.accessoryManager = new AccessoryManager(this);

  }

  @Override
  public boolean connect() {
    loconet = LoconetConnectionFactory.aquireConnection();
    this.connected = loconet != null && loconet.isConnected();

    if (connected) {
      eventMessageHandler = new EventMessageHandler(loconet);
      eventMessageHandler.start();
    }

    return connected;
  }

  @Override
  public void disconnect() {
    eventMessageHandler.quit();

    LoconetConnectionFactory.closeConnection();
  }

  @Override
  public InfoBean getCommandStationInfo() {
    return null;
  }

  @Override
  public List<Device> getDevices() {
    return null;
  }

  @Override
  public String getIp() {
    return null;
  }

  @Override
  public boolean power(boolean on) {
    power = on;
    if (power) {
      loconet.sendMessage(LoconetMessageFactory.powerOn());
    } else {
      loconet.sendMessage(LoconetMessageFactory.powerOff());
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
  public void changeDirection(int locUid, LocomotiveBean.Direction direction) {
  }

  @Override
  public void changeVelocity(int locUid, int speed, LocomotiveBean.Direction direction) {
  }

  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    return null;
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
    return null;
  }

  @Override
  public void fireAllSensorEventsListeners(final SensorEvent sensorEvent) {
    List<AllSensorEventsListener> snapshot = new ArrayList<>(allSensorEventsListeners);
    for (AllSensorEventsListener listener : snapshot) {
      listener.onSensorChange(sensorEvent);
    }
  }

  @Override
  public List<FeedbackModule> getFeedbackModules() {
    return null;
  }

  @Override
  public SensorBean getSensorStatus(SensorBean sensorBean) {
    return null;
  }

  @Override
  public void simulateSensor(SensorEvent sensorEvent) {
  }

  @Override
  public void onConnectionChange(ConnectionEvent event) {
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
                  AccessoryBean ab = LoconetMessageParser.parseSwitchEvent(message);
                  accessoryManager.update(ab);
                }
                case LoconetMessage.OPC_INPUT_REP -> {
                  Logger.trace("SensorEvent RX: {}", message);
                  SensorBean sb = LoconetMessageParser.parseSensorEvent(message);
                  if (sb != null) {
                    Logger.trace("Sensor: {} Value: {} ", sb.getId(), sb.getStatus());
                    SensorEvent sme = new SensorEvent(sb);
                    fireAllSensorEventsListeners(sme);
                  }
                }
                case LoconetMessage.OPC_SW_REP -> {
//                  0xB1 ;Turnout SENSOR state REPORT NO
//; <0xB1>,<SN1>,<SN2>,<CHK> SENSOR state REPORT
//<SN1> <SN2> =<0,A6,A5,A4- A3,A2,A1,A0>, 7 ls adr bits. A1,A0 select 1 of 4 input pairs in a DS54
//=<0,1,I,L- A10,A9,A8,A7> Report/status bits and 4 MS adr bits.
//this <B1> opcode encodes input levels for turnout feedback
//"I" =0 for "aux" inputs (normally not feedback), 1 for "switch" input used for turnout
//feedback for DS54 ouput/turnout # encoded by A0-A10
//"L" = 0 for this input 0V (LO), 1= this input > +6V (HI)
//alternately;
//<SN2> =<0,0,C,T- A10,A9,A8,A7> Report/status bits and 4 MS adr bits.
//this <B1> opcode encodes current OUTPUT levels
//"C"= 0 if "Closed" ouput line is OFF, 1="closed" output line is ON (sink current)
//"T"=0 if "Thrown" output line is OFF, 1="thrown" output line is ON (sink I)

                }

//                case CanMessage.LOC_VELOCITY -> {
//                  Logger.trace("VelocityChange# " + eventMessage);
//
//                }
//                case CanMessage.LOC_VELOCITY_RESP -> {
//                  Logger.trace("VelocityChange " + eventMessage);
//                  notifyLocomotiveSpeedEventListeners(LocomotiveVelocityMessage.parse(eventMessage));
//                }
//                case CanMessage.LOC_DIRECTION -> {
//                  Logger.trace("DirectionChange# " + eventMessage);
//
//                }
//                case CanMessage.LOC_DIRECTION_RESP -> {
//                  Logger.trace("DirectionChange " + eventMessage);
//                  notifyLocomotiveDirectionEventListeners(LocomotiveDirectionEventParser.parse(eventMessage));
//                }
//                case CanMessage.LOC_FUNCTION -> {
//
//                }
//                case CanMessage.LOC_FUNCTION_RESP -> {
//                  Logger.trace("FunctionChange " + eventMessage);
//                  notifyLocomotiveFunctionEventListeners(LocomotiveFunctionEventParser.parseMessage(eventMessage));
//                }
//                default -> {
//                }
//              }
                case LoconetMessage.OPC_LONG_ACK -> {
                  Logger.trace("Aknowlegde RX: {}", message);
                }
                case LoconetMessage.OPC_LOCO_SPD -> {
                  //for now ignore it
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
      intellibox2.power(true);

      intellibox2.pause(2000);

      intellibox2.switchAccessory(1, "dcc", AccessoryValue.GREEN, 100);
      intellibox2.pause(2000);
      intellibox2.switchAccessory(1, "dcc", AccessoryValue.RED, 100);

      intellibox2.pause(200000);
      //Lets power Off
      intellibox2.power(false);

      intellibox2.disconnect();

    }
  }

//TRACE	2026-07-16 19:42:24.819 [main] IntelliBoxSerialConnection.sendMessage(): RX 3: 02
//TRACE	2026-07-16 19:42:24.819 [main] IntelliBoxSerialConnection.sendMessage(): RX 4: 39
//TRACE	2026-07-16 19:42:24.820 [main] IntelliBoxSerialConnection.sendMessage(): RX 5: 64
//TRACE	2026-07-16 19:42:25.379 [main] IntelliBoxSerialConnection.sendMessage(): RX 6: a0
//TRACE	2026-07-16 19:42:25.380 [main] IntelliBoxSerialConnection.sendMessage(): RX 7: 01
//TRACE	2026-07-16 19:42:25.380 [main] IntelliBoxSerialConnection.sendMessage(): RX 8: 02
//TRACE	2026-07-16 19:42:25.381 [main] IntelliBoxSerialConnection.sendMessage(): RX 9: 5c  
}
