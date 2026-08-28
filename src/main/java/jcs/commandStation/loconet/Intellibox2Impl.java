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
  ThreadGroup threadGroup;
  private EventMessageHandler eventMessageHandler;
  
  private final AccessoryManager accessoryManager;
  private final LocomotiveManager locomotiveManager;
  
  static final String COMMAND_STATION_ID = "intellibox2";
  
  public Intellibox2Impl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }
  
  public Intellibox2Impl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    threadGroup = new ThreadGroup("INTELLIBOX2");
    
    this.executor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "INBX-LN-BG-QUERY");
      thread.setDaemon(true);
      return thread;
    });
    
    this.accessoryManager = new AccessoryManager(this);
    this.locomotiveManager = new LocomotiveManager(this);
  }
  
  @Override
  public boolean connect() {
    loconet = LoconetConnectionFactory.aquireConnection();
    this.connected = loconet != null && loconet.isConnected();
    
    if (connected) {
      eventMessageHandler = new EventMessageHandler(loconet);
      eventMessageHandler.start();
      
      accessoryManager.start();
      //refresh the accessories in the background
      executor.execute(() -> this.accessoryManager.refresh());
      //refresh the locomotives in the background
      executor.execute(() -> this.locomotiveManager.refresh());
      
    }
    
    return connected;
  }
  
  @Override
  public void disconnect() {
    if (eventMessageHandler != null) {
      eventMessageHandler.quit();
      try {
        eventMessageHandler.join(1000L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      eventMessageHandler = null;
    }
    
    accessoryManager.shutdown();
    
    LoconetConnectionFactory.closeConnection();
    connected = false;
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
    if (this.loconet != null) {
      LoconetMessage reply = null;
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
                  //Switch State Report
                  Logger.trace("Accessory State RX: {}", message);
                  AccessoryBean ab = LoconetMessageParser.parseSwitchStateEvent(message);
                  accessoryManager.update(ab);
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
      //Lets power Off
      //intellibox2.power(false);
      //intellibox2.pause(2000);

      //intellibox2.accessoryManager.queryAccessory(1);
//      intellibox2.switchAccessory(1, "dcc", AccessoryValue.GREEN, 100);
//      intellibox2.pause(2000);
//      intellibox2.switchAccessory(1, "dcc", AccessoryValue.RED, 100);
      intellibox2.locomotiveManager.requestAddress(72);
      
      
//TX: 0xbf 0x00 0x48 0x08
//RX: 0xbf 0x00 0x48 0x08
//RX echo consumed: 0xbf 0x00 0x48 0x08
//slotread
//RX: 0xe7 0x0e 0x08 0x32 0x48 0x00 0x30 0x07 0x00 0x00 0x00 0x00 0x00 0x53

//
//
//  OPC_SL_RD_DATA 0xE7 ;SLOT DATA return, 10 bytes NO
// <0xE7>  -> OPC_SL_RD_DATA
// ,<0E>   -> len totaal 14 bytes
//  <SLOT#> -> slot nr "8"  SLOT NUMBER: ;0-7FH, 0 is special SLOT, 070H-07FH DIGITRAX reserved.
//  ,<STAT> 1) SLOT STATUS1:
//          2) SLOT LOCO ADR: 
//          3) SLOT SPEED: 
//   D7-SL_SPURGE 
//   D6-SL_CONUP ;1=SLOT purge en, ALSO adrSEL (INTERNAL use only) //; (not seen on NET!)
//                   CONDN/CONUP: bit encoding-Control double linked Consist List
//                11=LOGICAL MID CONSIST , Linked up AND down
//                10=LOGICAL CONSIST TOP, Only linked downwards
//                01=LOGICAL CONSIST SUB-MEMBER, Only linked upwards
//                00=FREE locomotive, no CONSIST indirection/linking number is now SLOT adr of SPD/DIR and STATUS of consist. i.e. is an Indirect pointer.
//                           This Slot has same BUSY/ACTIVE bits as TOP of Consist.
//                           TOP is loco with SPD/DIR for whole consist. (top of list). BUSY/ACTIVE: bit encoding for SLOT activity
//   D5-SL_BUSY   ;11=IN_USE loco adr in SLOT -REFRESHED
//   D4-SL_ACTIVE ;10=IDLE loco adr in SLOT -NOT refreshed ;
//                 01=COMMON loco adr IN SLOT -refreshed
//                 00=FREE SLOT, no valid DATA -not refreshed
//   D3-SL_CONDN ; shows other SLOT Consist linked INTO this slot,see SL_CONUP
//   D2-SL_SPDEX ; 3 BITS for Decoder TYPE encoding for this SLOT
//   D1-SL_SPD14 ;011=send 128 speed mode packets
//   D0-SL_SPD28 ;010=14 step MODE
//               ;001=28 step. Generate Trinary packets for this Mobile ADR
//               ;000=28 step/ 3 BYTE PKT regular mode
//               ;111=128 Step decoder, Allow Advanced DCC consisting
//               ;100=28 Step decoder ,Allow Advanced DCC consisting
//
//                  7|6|5|4|3|2|1|0
//    waarde 0x32:  0 0 1 1 0 0 2 0
//    14 STEP MODE in use refreshed data valid
//
//  ,<ADR> address low 0x48 -> 72
//  ,<SPD> sprre = 0
//  ,<DIRF> 
//           D7-0 ;always 0
//           D6-SL_XCNT ; reserved , set 0
//           D5-SL_DIR ;1=loco direction FORWARD
//           D4-SL_F0 ;1=Directional lighting ON
//           D3-SL_F4 ;1=F4 ON
//           D2-SL_F3 ;1=F3 ON
//           D1-SL_F2 ;1=F2 ON
//           D0-SL_F1 ;1=F1 ON
//  waarde 0x30   7|6|5|4|3|2|1|0
//                0 0 1 1 0 0 0 0   -> forwards F0 on F1..4 off
//  ,<TRK>  (GLOBAL system /track status)
//         D7-D4 Reserved
//         D3 GTRK_PROG_BUSY 1=Programming TRACK in this Master is BUSY.
//         D2 GTRK_MLOK1 , 1=This Master IMPLEMENTS LocoNet 1.1 capability
//                       , 0=Master is DT200
//         D1 GTRK_IDLE ; 0=TRACK is PAUSED, B'cast EMERG STOP.
//         D0 GTRK_POWER ; 1=DCC packets are ON in MASTER, Global POWER up
// waarde 0x07 7|6|5|4|3|2|1|0
//             0 0 0 0 0 1 1 1 ->master LN 1.1, Power On DCC is on
//  ,<SS2> SLOT STATUS2:
//   D3- 1=expansion IN ID1/2, 0=ENCODED alias
//   D2- 1=Expansion ID1/2 is NOT ID usage
//   D0- 1=this slot has SUPPRESSED ADV consist-
// waarde 0x00 
//  ,<ADR2>  high address byte
//  ,<SND>  0x00 
//SLOT SOUND: Slot sound/ Accesory Function mode II packets. F5-F8 (byte also sent as ARG2 in SND opcode)
//     D7-D4 reserved
//     D3-SL_SND4/F8
//     D2-SL_SND3/F7
//     D1-SL_SND2/F6
//     D0-SL_SND1/F5 ;1= SLOT Sound 1 function 1active (accessory 2)


//  ,<ID1>,<ID2> uid 0x00 not in use
//  ,<CHK>  
//; SLOT DATA READ, 10 bytes data /14 byte MSG
//;NOTE; If STAT2.2=0 EX1/EX2 encodes an ID#,[if STAT2.2=1 the STAT.3=0 means EX1/EX2 are ALIAS]
//;ID1/ID2 are two 7 bit values encoding a 14 bit unique DEVICE usage ID. ID1/ID2#'s 00/00 -means NO ID being used
//; 01/00 to 7F/01 -ID shows PC usage.Lo nibble is TYP PC# (PC can use hi values)
//; 00/02 to 7F/03 -SYSTEM reserved
//; 00/04 to 7F/7E -NORMAL throttle RANGE
      
      
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
