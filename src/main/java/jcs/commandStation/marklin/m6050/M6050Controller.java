/*
 * Copyright 2018 Frans Jacobs.
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
package jcs.commandStation.marklin.m6050;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.entities.LocomotiveBean.Direction;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import org.tinylog.Logger;

/**
 * Implementation of the (old) serial P50 Interface
 *
 * @author frans
 */
public class M6050Controller { //implements ControllerService {

  static final int SOLENOID_MAX_ACTIVE_TIME = 2000;
  static final int PAUSE_BETWEEN_CMD = SerialPortImpl.PAUSE_BETWEEN_CMD;
  static final int PAUSE_BETWEEN_BYTES = SerialPortImpl.PAUSE_BETWEEN_BYTES;

  static final int SWITCH_OFF = 32;
  static final int SWITCH_RED = 34;
  static final int SWITCH_GREEN = 33;
  static final int SWITCH_CURVED = 34;
  static final int SWITCH_STRAIGHT = 33;

  static final int REVERSE_COMMAND = 15;
  static final int FUNCTION_OFFSET = 16;
  static final int SPECIAL_FUNCTION_OFFSET = 64;
  private static final int MAX_SPEED_VAL = 1023;
  static final int MAX_SPEED = 14;
  static final int MIN_SPEED = 0;

  static final int FEEDBACK_MULTIPLE_FIRST = 129;
  static final int FEEDBACK_MULTIPLE_LAST = 159;
  static final int FEEDBACK_OFFSET = 192;
  static final int FEEDBACK_FIRST = 193;
  static final int FEEDBACK_LAST = 223;

  private final String portname;
  private final SerialPortImpl p50;
  private M6050CommandHandler commandHandler;

  //private final List<ControllerEventListener> controllerEventListeners;
  private final ExecutorService executor;

  public M6050Controller() {
    portname = ""; //RunUtil.getDefaultPortname();
    p50 = new SerialPortImpl(portname);
    //commandHandler = new M6050CommandHandler(p50);
    //controllerEventListeners = new ArrayList<>();
    executor = Executors.newCachedThreadPool();

    init();
  }

  private void init() {
    connect();
    powerOn();
  }

  //@Override
  public void powerOff() {
    if (commandHandler != null) {
      commandHandler.purge();
    }
    p50.sendSingleCommand(SerialPortImpl.STOP_COMMAND);

    //executor.execute(() -> broadcastControllerEvent(new ControllerEvent(p50.isPowerOn(), isConnected())));
  }

  //@Override
  public void powerOn() {
    p50.sendSingleCommand(SerialPortImpl.GO_COMMAND);
    //executor.execute(() -> broadcastControllerEvent(new ControllerEvent(p50.isPowerOn(), isConnected())));
  }

  //@Override
  public boolean isPowerOn() {
    return p50.isPowerOn();
  }

  //@Override
  public boolean connect() {
    boolean result = true;
    if (commandHandler != null && commandHandler.isRunning()) {
      Logger.trace("Connected and running...");
    } else {
      result = p50.connectSerialPort();
      if (result) {
        commandHandler = new M6050CommandHandler(p50);
        commandHandler.start();
      }
    }

    boolean conRes = result && commandHandler.isRunning();

    //executor.execute(() -> broadcastControllerEvent(new ControllerEvent(p50.isPowerOn(), conRes)));
    return conRes;
  }

  //@Override
  public boolean isConnected() {
    return false; //RunUtil.hasSerialPort() && commandHandler != null && commandHandler.isRunning();
  }

  //@Override
  public void disconnect() {
    commandHandler.quit();
    commandHandler = null;
    p50.disconnectSerialPort();
    //executor.execute(() -> broadcastControllerEvent(new ControllerEvent(p50.isPowerOn(), false)));
  }

  //@Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  //@Override
  public void toggleDirection(int address, DecoderType protocol, boolean function) {
    int offset = function ? FUNCTION_OFFSET : 0;
    //REVERSE command       
    CommandAddressPair reverse = new CommandAddressPair(offset + REVERSE_COMMAND, address);
    //Speed and function
    CommandAddressPair speedFunction = new CommandAddressPair(offset + MIN_SPEED, address);

    commandHandler.enqueue(reverse);
    commandHandler.enqueue(speedFunction);
  }

  //@Override
  public void toggleDirection(int address, DecoderType protocol) {
    throw new UnsupportedOperationException("Not supported.");
  }

  // @Override
  public void setDirection(int address, DecoderType protocol, Direction direction) {
    throw new UnsupportedOperationException("Not supported.");
  }

  //@Override
  public void setSpeedAndFunction(int address, DecoderType protocol, boolean function, int speed) {
    //The speed must be calculated for MM. Max value from UI is 1023 which equals to 14
    int step = MAX_SPEED_VAL / MAX_SPEED;  //73
    int ls = speed / step;
    CommandAddressPair speedFunction = new CommandAddressPair((function ? FUNCTION_OFFSET : 0) + ls, address);
    commandHandler.enqueue(speedFunction);
  }

  //@Override
  public void setSpeed(int address, DecoderType protocol, int speed) {
    throw new UnsupportedOperationException("Not supported.");
  }

  //@Override
  public void setFunction(int address, DecoderType protocol, int functionNumber, boolean value) {
    throw new UnsupportedOperationException("Not supported.");
  }

  //@Override
  public void setFunctions(int address, DecoderType protocol, boolean f1, boolean f2, boolean f3, boolean f4) {
    int cmd = (f1 ? 1 : 0) + (f2 ? 2 : 0) + (f3 ? 4 : 0) + (f4 ? 8 : 0) + SPECIAL_FUNCTION_OFFSET;
    CommandAddressPair functions = new CommandAddressPair(cmd, address);
    commandHandler.enqueue(functions);
  }

  //@Override
  public void switchAccessoiry(int address, AccessoryValue value) {
    Logger.trace(address + " -> " + value);

    int cmd;
    if (AccessoryValue.GREEN.equals(value)) {
      cmd = SWITCH_GREEN;
    } else {
      cmd = SWITCH_RED;
    }
    CommandAddressPair accessoiry = new CommandAddressPair(cmd, address);
    commandHandler.enqueue(accessoiry);
  }

  //@Override
  public int[] getFeedback(int moduleNumber) {
    CommandAddressPair feedback = new CommandAddressPair(FEEDBACK_OFFSET + moduleNumber);
    commandHandler.enqueue(feedback);

    return feedback.getFeedback();
  }

  //@Override
  //public ServiceInfo getServiceInfo() {
  //  return ServiceInfoBuilder.createServiceInfo(ControllerService.SERVICE_TYPE, "M 6050", M6050Controller.class, "lan.wervel.jcs", "jcscommon", 0);
  //}
//  private void broadcastControllerEvent(ControllerEvent event) {
//    Set<ControllerEventListener> snapshot;
//    synchronized (controllerEventListeners) {
//      if (controllerEventListeners.isEmpty()) {
//        snapshot = new HashSet<>();
//      } else {
//        snapshot = new HashSet<>(controllerEventListeners);
//      }
//    }
//
//    for (ControllerEventListener listener : snapshot) {
//      listener.notify(event);
//    }
//  }
  //@Override
//  public void addControllerEventListener(ControllerEventListener listener) {
//    //this.controllerEventListeners.add(listener);
//  }
  //@Override
//  public void removeControllerEventListener(ControllerEventListener listener) {
//    //this.controllerEventListeners.remove(listener);
//  }
//  @Override
//  public void notifyAllControllerEventListeners() {
//    Logger.info("Current Controller Power Status: " + (p50.isPowerOn() ? "On" : "Off") + "...");
//    executor.execute(() -> broadcastControllerEvent(new ControllerEvent(p50.isPowerOn(), isConnected())));
//  }
//  @Override
//  public ControllerInfo getControllerInfo() {
//    return new ControllerInfo("N/A", "6021", "6021 via 6050", 5, true, false, false, false);
//  }
}
