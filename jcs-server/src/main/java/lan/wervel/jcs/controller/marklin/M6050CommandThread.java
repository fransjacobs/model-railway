/*
 * Copyright (C) 2018 Frans Jacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package lan.wervel.jcs.controller.marklin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import lan.wervel.jcs.controller.marklin.serial.CommandPair;
import lan.wervel.jcs.controller.marklin.serial.Serial6050;
import lan.wervel.jcs.repository.model.AttributeChangedEvent;
import static lan.wervel.jcs.repository.model.AttributeChangedEvent.Item.S88;
import lan.wervel.jcs.repository.model.ControllableItem;
import lan.wervel.jcs.repository.model.FeedbackModule;
import lan.wervel.jcs.repository.model.Locomotive;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry;
import lan.wervel.jcs.repository.model.SolenoidAccessoiry.StatusType;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

/**
 *
 * @author frans
 */
public class M6050CommandThread extends Thread {

  private final LinkedBlockingQueue<AttributeChangedEvent> changedEventQueue;
  private boolean running = true;
  private final Serial6050 serial6050;
  private Long lastSolenoidCommand;

  static final int SOLENOID_MAX_ACTIVE_TIME = 2000;
  static final int PAUSE_BETWEEN_CMD = 100;
  static final int PAUSE_BETWEEN_BYTES = 5;

  public static final Integer SWITCH_OFF = 32;
  public static final Integer SWITCH_RED = 34;
  public static final Integer SWITCH_GREEN = 33;
  static final Integer SWITCH_CURVED = 34;
  static final Integer SWITCH_STRAIGHT = 33;

  static final Integer REVERSE_COMMAND = 15;
  static final Integer FUNCTION_OFFSET = 16;
  static final Integer SPECIAL_FUNCTION_OFFSET = 64;
  static final Integer MAX_SPEED = 14;
  static final Integer MIN_SPEED = 0;

  public static final Integer FEEDBACK_MULTIPLE_FIRST = 129;
  public static final Integer FEEDBACK_MULTIPLE_LAST = 159;
  public static final Integer FEEDBACK_OFFSET = 192;
  public static final Integer FEEDBACK_FIRST = 193;
  public static final Integer FEEDBACK_LAST = 223;

  public M6050CommandThread(Serial6050 serialImpl) {
    changedEventQueue = new LinkedBlockingQueue<>();
    serial6050 = serialImpl;
  }

  void enqueue(AttributeChangedEvent evt) {
    if (serial6050.isPowerOn()) {
      evt.getSource().setEnableAttributeChangeHandling(false);
      changedEventQueue.offer(evt);
      Logger.trace("Enqueued: " + evt.toString());
    }
  }

  private List<CommandPair> handleSolenoidAccessoiry(AttributeChangedEvent evt) {
    SolenoidAccessoiry si = (SolenoidAccessoiry) evt.getSource();
    List<CommandPair> cl = new ArrayList<>();
    StatusType newVal = (StatusType) evt.getNewValue();
    int cmd;
    if (StatusType.GREEN.equals(newVal)) {
      cmd = SWITCH_GREEN;
    } else {
      cmd = SWITCH_RED;
    }

    CommandPair cp = new CommandPair(cmd, si.getAddress());
    cl.add(cp);
    return cl;
  }

  private int getFunctionsCommand(boolean f1, boolean f2, boolean f3, boolean f4) {
    int cmd = (f1 ? 1 : 0) + (f2 ? 2 : 0) + (f3 ? 4 : 0) + (f4 ? 8 : 0) + SPECIAL_FUNCTION_OFFSET;
    return cmd;
  }

  private List<CommandPair> handleLocomotive(AttributeChangedEvent evt) {
    String attr = evt.getAttribute();
    switch (attr) {
      case "stop":
        return handleSpeedFunctionChange(evt);
      case "changeDirection":
        return handleDirectionChange(evt);
      case "setDirection":
        return handleDirectionChange(evt);
      case "setSpeed":
        return handleSpeedFunctionChange(evt);
      case "setThrottle":
        //Future use
        return new ArrayList<>();
      case "setF0":
        return handleSpeedFunctionChange(evt);
      case "setF1":
        return handleSpecialFunctionsChange(evt);
      case "setF2":
        return handleSpecialFunctionsChange(evt);
      case "setF3":
        return handleSpecialFunctionsChange(evt);
      case "setF4":
        return handleSpecialFunctionsChange(evt);
      case "setSelected":
        return new ArrayList<>();
      default:
        return new ArrayList<>();
    }
  }

  private List<CommandPair> handleDirectionChange(AttributeChangedEvent evt) {
    Locomotive loco = (Locomotive) evt.getSource();
    Integer address = loco.getAddress();
    List<CommandPair> cl = new ArrayList<>();
    Logger.debug("Event: " + evt.toString() + "...");

    //Send REVERSE command       
    int cmd = (loco.isF0() ? FUNCTION_OFFSET : 0) + REVERSE_COMMAND;
    CommandPair cp = new CommandPair(cmd, address);
    cl.add(cp);

    //Send Speed and function
    loco.setSpeed(0);
    cmd = (loco.isF0() ? FUNCTION_OFFSET : 0) + loco.getSpeed();
    cp = new CommandPair(cmd, address);
    cl.add(cp);

    //Send the special functions when applicable
    if (loco.isSpecialFuctions()) {
      cmd = getFunctionsCommand(loco.isF1(), loco.isF2(), loco.isF3(), loco.isF4());
      cp = new CommandPair(cmd, address);
      cl.add(cp);
    }
    return cl;
  }

  private List<CommandPair> handleSpeedFunctionChange(AttributeChangedEvent evt) {
    Locomotive loco = (Locomotive) evt.getSource();
    Integer address = loco.getAddress();
    List<CommandPair> cl = new ArrayList<>();

    //Send Speed and function
    int cmd = (loco.isF0() ? FUNCTION_OFFSET : 0) + loco.getSpeed();
    CommandPair cp = new CommandPair(cmd, address);
    cl.add(cp);

    //Send the special functions when applicable
    if (loco.isSpecialFuctions()) {
      cmd = getFunctionsCommand(loco.isF1(), loco.isF2(), loco.isF3(), loco.isF4());
      cp = new CommandPair(cmd, address);
      cl.add(cp);
    }
    return cl;
  }

  private List<CommandPair> handleSpecialFunctionsChange(AttributeChangedEvent evt) {
    Locomotive loco = (Locomotive) evt.getSource();
    Integer address = loco.getAddress();
    List<CommandPair> cl = new ArrayList<>();
    Logger.trace("Event: " + evt.toString() + "...");

    int cmd = getFunctionsCommand(loco.isF1(), loco.isF2(), loco.isF3(), loco.isF4());
    CommandPair cp = new CommandPair(cmd, address);
    cl.add(cp);

    return cl;
  }

  private List<CommandPair> handleS88(AttributeChangedEvent evt) {
    FeedbackModule fbm = (FeedbackModule) evt.getSource();
    List<CommandPair> cl = new ArrayList<>();
    int modnr = fbm.getModuleNumber();

    int cmd = FEEDBACK_OFFSET + modnr;
    CommandPair cp = new CommandPair(cmd);
    cl.add(cp);
    return cl;
  }

  private List<CommandPair> getCommandSequence(AttributeChangedEvent evt) {
    ControllableItem item = evt.getSource();
    switch (evt.getItemType()) {
      case LOCOMOTIVE:
        return handleLocomotive(evt);
      case SOLENOIDACCESSOIRY:
        return handleSolenoidAccessoiry(evt);
      case S88:
        return handleS88(evt);
      default:
        Logger.warn("Can't handle item: " + item.getClass().getSimpleName() + "...");
        return new ArrayList<>();
    }
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

  private synchronized boolean sendCommandSequence(AttributeChangedEvent evt) {
    boolean result = false;
    List<CommandPair> csl = getCommandSequence(evt);

    StringBuilder sb = new StringBuilder();
    for (CommandPair cp : csl) {
      sb.append("{ C: ");
      sb.append(cp.getCommand());
      if (cp.isAddress()) {
        sb.append(" [");
        sb.append(cp.getAddress());
        sb.append("]");
      }
      sb.append(" } ");
    }
    Logger.trace("Sending command sequence: " + sb);

    //send command to serial port...
    for (CommandPair cp : csl) {
      result = serial6050.sendCommand(cp);

      if (cp.isSolenoidCommand()) {
        lastSolenoidCommand = System.currentTimeMillis();
        serial6050.setSolenoidActive(true);
        Logger.trace("Last SolenoidCommand @: " + lastSolenoidCommand + " Solenoid Active: " + serial6050.isSolenoidActive());
      }

      pause(PAUSE_BETWEEN_CMD);
    }

    return result;
  }

  @Override
  public void run() {
    Logger.info("Starting M 6050/6051 Handle thread...");
    while (running) {
      while (!changedEventQueue.isEmpty() && this.serial6050.isPowerOn()) {
        AttributeChangedEvent evt = changedEventQueue.poll();
        Logger.trace("Polled: " + evt);

        if (evt != null) {
          boolean result = sendCommandSequence(evt);
          Logger.trace("Command for: " + evt + " send. Result: " + result);

          //Ensure the command thread is not stopping unexpectedly hence the try catch
          try {
            if (S88.equals(evt.getItemType())) {
              //when it is a feedback event get the feedback from the result        
              FeedbackModule s88 = (FeedbackModule) evt.getSource();
              Integer[] response = serial6050.getFeedback(s88.getModuleNumber());
              if (response != null && response.length == 2) {
                s88.setResponse(response);

                if (Logger.getLevel() == Level.TRACE) {
                  StringBuilder sb = new StringBuilder();
                  for (Integer r : response) {
                    sb.append("[");
                    sb.append(r);
                    sb.append("]");
                  }
                  Logger.trace("Response for Module: " + s88.getModuleNumber() + " -> " + sb);
                }
              } else {
                Logger.warn("No Response for Module: " + s88.getModuleNumber() + "...");
              }
            }
          } catch (Exception e) {
            Logger.error("Error during feedback processing! " + e.getMessage());
          }

          if (evt.getRepository() != null) {
            //Update the repository
            evt.getRepository().updateControllableItem(evt);
          }
          //we are done...
        }
      }
      pause(PAUSE_BETWEEN_CMD);
      if (serial6050 != null && serial6050.isSolenoidActive() && (lastSolenoidCommand + SOLENOID_MAX_ACTIVE_TIME) < System.currentTimeMillis()) {
        Logger.trace("Deactivating Solenoids...");
        serial6050.sendSingleCommand(SWITCH_OFF);
      }
    }
  }

  public boolean isRunning() {
    return running;
  }

  public synchronized void quit() {
    if (serial6050.isSolenoidActive()) {
      Logger.debug("Solenoid active switch off before quitting...");
      serial6050.sendSingleCommand(SWITCH_OFF);
    }
    changedEventQueue.clear();
    pause(20);
    running = false;
  }
}
