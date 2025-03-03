/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.commandStation.marklin.cs.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import jcs.commandStation.VirtualConnection;
import jcs.commandStation.events.DisconnectionEvent;
import jcs.commandStation.events.DisconnectionEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.parser.SystemStatus;
import org.tinylog.Logger;

/**
 * Virtual Marklin CS Connection
 */
class CSVirtualConnection implements CSConnection, VirtualConnection {

  private boolean connected;

  private final InetAddress centralStationAddress;

  private PeriodicCSMessageSender periodicMessageSender;
  private final List<DisconnectionEventListener> disconnectionEventListeners;

  private final TransferQueue<CanMessage> eventQueue;

  CSVirtualConnection(InetAddress csAddress) {
    centralStationAddress = csAddress;
    eventQueue = new LinkedTransferQueue<>();
    //disconnectionEventListeners = new ArrayList<>();
    disconnectionEventListeners = Collections.synchronizedList(new ArrayList<>());
    this.connected = true;

    //initMessageSender();
  }

  @Override
  public void sendEvent(SensorEvent sensorEvent) {
    //Virtual
  }

  private void initMessageSender() {
    periodicMessageSender = new PeriodicCSMessageSender();
    periodicMessageSender.start();
    //periodicMessageSender.setDaemon(true);
  }

  @Override
  public TransferQueue<CanMessage> getEventQueue() {
    return this.eventQueue;
  }

  @Override
  public void addDisconnectionEventListener(DisconnectionEventListener listener) {
    this.disconnectionEventListeners.add(listener);
  }

  private void disconnect() {
    this.connected = false;
    if (periodicMessageSender != null) {
      periodicMessageSender.quit();
    }
    disconnectionEventListeners.clear();
  }

  @Override
  public synchronized CanMessage sendCanMessage(CanMessage message) {
    if (message == null) {
      Logger.warn("Message is NULL?");
      return null;
    }

    int command = message.getCommand();
    int dlc = message.getDlc();
    int uid = message.getDeviceUidNumberFromMessage();
    int subcmd = message.getSubCommand();

    Logger.trace("TX: " + message);

    switch (command) {
      case CanMessage.SYSTEM_COMMAND -> {
        switch (subcmd) {
          case CanMessage.STOP_SUB_CMD -> {
            if (dlc == 4) {
              //Power status Query, reply with On
              message.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x05 0x63 0x73 0x45 0x8c 0x01 0x00 0x00 0x00"));
              message.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x05 0x00 0x00 0x00 0x00 0x01 0x00 0x00 0x00"));
            } else if (dlc == 5) {
              if (SystemStatus.parseSystemPowerMessage(message)) {
                message.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x05 0x63 0x73 0x45 0x8c 0x01 0x00 0x00 0x00"));
                message.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x05 0x00 0x00 0x00 0x00 0x01 0x00 0x00 0x00"));
              } else {
                //Switch Power Off
                message.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x05 0x63 0x73 0x45 0x8c 0x00 0x00 0x00 0x00"));
                message.addResponse(CanMessage.parse("0x00 0x01 0x03 0x26 0x05 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00"));
              }
            }
          }
          case CanMessage.GO_SUB_CMD -> {
          }
          case CanMessage.HALT_SUB_CMD -> {
          }
          case CanMessage.LOC_STOP_SUB_CMD -> {
          }
          case CanMessage.OVERLOAD_SUB_CMD -> {
          }
        }

      }
      case CanMessage.PING_REQ -> {
        //Lets do this the when we know all of the CS...
//              if (mainDevice != null) {
//                if (CanMessage.DLC_0 == dlc) {
//                  Logger.trace("Ping RQ: " + eventMessage);
//                  sendJCSUIDMessage();
//                }
//              }
      }
      case CanMessage.PING_RESP -> {
//              if (CanMessage.DLC_8 == dlc) {
//                Logger.trace("Ping Response RX: " + eventMessage);
//
//                updateDevice(eventMessage);
//              }
      }
      case CanMessage.STATUS_CONFIG -> {
//              if (CanMessage.JCS_UID == uid && CanMessage.DLC_5 == dlc) {
//                Logger.trace("StatusConfig RQ: " + eventMessage);
//                sentJCSInformationMessage();
//              }
      }
      case CanMessage.STATUS_CONFIG_RESP -> {
      }
      case CanMessage.S88_EVENT_RESPONSE -> {
//              if (CanMessage.DLC_8 == dlc) {
//                SensorBean sb = SensorMessageParser.parseMessage(eventMessage, new Date());
//                SensorEvent sme = new SensorEvent(sb);
//                if (sme.getSensorBean() != null) {
//                  fireSensorEventListeners(sme);
//                }
//              }

//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x01 0x00 0x01 0xe5 0x10
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x01 0x01 0x00 0x00 0x32
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x02 0x00 0x01 0xab 0x68
//
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x02 0x00 0x01 0xab 0x68 0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x0f 0x00 0x01 0xff 0xff
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x0f 0x01 0x00 0x01 0x90
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x10 0x00 0x01 0xe3 0x3a
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x00 0x10 0x01 0x00 0x00 0x8c
//
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x07 0xd1 0x01 0x00 0x00 0xb4
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x07 0xe0 0x00 0x01 0xdc 0xf0
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x07 0xe0 0x01 0x00 0x00 0x50


//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xee 0x00 0x01 0x40 0xf6
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xee 0x01 0x00 0x00 0x0a
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xf0 0x00 0x01 0x42 0x5d
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xf0 0x01 0x00 0x00 0x0a
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xf0 0x00 0x01 0x00 0x82
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xf0 0x01 0x00 0x00 0x0a
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xf0 0x00 0x01 0x00 0x46
//0x00 0x23 0x7b 0x79 0x08 0x00 0x41 0x03 0xf0 0x01 0x00 0x00 0x0a




      }
      case CanMessage.SYSTEM_COMMAND_RESP -> {
      }
      case CanMessage.ACCESSORY_SWITCHING -> {
        message.addResponse(CanMessage.parse(message.toString(), true));
      }
      case CanMessage.ACCESSORY_SWITCHING_RESP -> {
      }
      case CanMessage.LOC_VELOCITY -> {
        message.addResponse(CanMessage.parse(message.toString(), true));
      }
      case CanMessage.LOC_VELOCITY_RESP -> {
      }
      case CanMessage.LOC_DIRECTION -> {
        message.addResponse(CanMessage.parse(message.toString(), true));
      }
      case CanMessage.LOC_DIRECTION_RESP -> {
      }
      case CanMessage.LOC_FUNCTION -> {
        message.addResponse(CanMessage.parse(message.toString(), true));
      }
      case CanMessage.LOC_FUNCTION_RESP -> {
      }
      default -> {
      }
    }
    return message;
  }

  @Override
  public void close() throws Exception {
    disconnect();
  }

  @Override
  public InetAddress getControllerAddress() {
    return centralStationAddress;
  }

  @Override
  public boolean isConnected() {
    return this.connected; 
  }

  private class PeriodicCSMessageSender extends Thread {

    private boolean quit = true;

    public PeriodicCSMessageSender() {
    }

    synchronized void quit() {
      quit = true;
    }

    synchronized boolean isRunning() {
      return !quit;
    }

    @Override
    public void run() {
      Thread.currentThread().setName("CS-VIRT-CAN-TX");

      quit = false;
      Logger.trace("Started sending periodic messages...");
      while (isRunning()) {
        //Send a ping requests once and a while
        CanMessage ping = CanMessage.parse("0x00 0x30 0x37 0x7e 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00");
        eventQueue.offer(ping);
        Logger.trace("Enqueued: " + ping + " QueueSize: " + eventQueue.size());

        synchronized (this) {
          try {
            wait(5000);
          } catch (InterruptedException ex) {
            Logger.trace(ex);
          }
        }
      }

      String msg = "Host " + centralStationAddress.getHostName();
      DisconnectionEvent de = new DisconnectionEvent(msg);
      for (DisconnectionEventListener listener : disconnectionEventListeners) {
        listener.onDisconnect(de);
      }

      Logger.trace("Stop sending");

    }
  }
}

