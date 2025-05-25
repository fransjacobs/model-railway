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
package jcs.commandStation.esu.ecos.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import jcs.commandStation.esu.ecos.Ecos;
import jcs.commandStation.esu.ecos.EcosMessage;
import jcs.commandStation.esu.ecos.EcosMessageFactory;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.VirtualConnection;
import jcs.entities.AccessoryBean;
import jcs.entities.FeedbackModuleBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.util.NetworkUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
class EcosVirtualConnection implements EcosConnection, VirtualConnection {

  private boolean connected;

  private final TransferQueue<String> transferQueue;
  private final TransferQueue<EcosMessage> eventQueue;

  private EcosMessageListener messageListener;
  private boolean debug = false;
  
  private static String ESU_ECOS_ID = "esu-ecos";

  EcosVirtualConnection(InetAddress address) {
    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    transferQueue = new LinkedTransferQueue<>();
    eventQueue = new LinkedTransferQueue<>();
    this.connected = true;
  }

  private void disconnect() {
    this.connected = false;
  }

  @Override
  public void setMessageListener(EcosMessageListener messageListener) {
    this.messageListener = messageListener;
  }

  @Override
  public synchronized EcosMessage sendMessage(EcosMessage message) {
    String msg = message.getMessage();
    String reply;

    StringBuilder replyBuilder = new StringBuilder();
    replyBuilder.append("<REPLY ");
    replyBuilder.append(message.getMessage());
    replyBuilder.append(">");

    switch (msg) {
      case EcosMessageFactory.BASE_OBJECT -> {
        replyBuilder.append("1 objectclass[model]");
        replyBuilder.append("1 view[none]");
        replyBuilder.append("1 listview[none]");
        replyBuilder.append("1 control[none]");
        replyBuilder.append("1 list1 size[10]");
        replyBuilder.append("1 minarguments[64]");
        replyBuilder.append("1 protocolversion[0.5]");
        replyBuilder.append("1 commandstationtype[\"ECoS\"]");
        replyBuilder.append("1 name[\"ECoS-Virtual\"]");
        replyBuilder.append("1 serialnumber[\"0x00000000\"]");
        replyBuilder.append("1 hardwareversion[1.3]");
        replyBuilder.append("1 applicationversion[4.2.13]");
        replyBuilder.append("1 applicationversionsuffix[\"\"]");
        replyBuilder.append("1 updateonerror[0]");
        replyBuilder.append("1 status[GO]");
        replyBuilder.append("1 status2[ALL]");
        replyBuilder.append("1 prog-status[0]");
        replyBuilder.append("1 m4-status[none]");
        replyBuilder.append("1 railcomplus-status[none]");
        replyBuilder.append("1 watchdog[0,0]");
        replyBuilder.append("1 railcom[1]");
        replyBuilder.append("1 railcomplus[1]");
        replyBuilder.append("1 railcomplus-range[1000]");
        replyBuilder.append("1 railcomplus-mode[manual]");
        replyBuilder.append("1 allowlocotakeover[1]");
        replyBuilder.append("1 stoponlastdisconnect[0]");
      }
      case EcosMessageFactory.BASE_OBJECT_SUBSCRIBE_VIEW -> {

      }
      case EcosMessageFactory.BASE_OBJECT_RELEASE_VIEW -> {

      }
      case EcosMessageFactory.QUERY_LOCOMOTIVES -> {
        //Query the locomotives from the database
        List<LocomotiveBean> locos = PersistenceFactory.getService().getLocomotivesByCommandStationId(ESU_ECOS_ID);

        for (LocomotiveBean loco : locos) {
          //name,addr,protocol
          replyBuilder.append(loco.getId());
          replyBuilder.append(" name[");
          replyBuilder.append(loco.getName());
          replyBuilder.append("]");

          replyBuilder.append(loco.getId());
          replyBuilder.append(" addr[");
          replyBuilder.append(loco.getAddress());
          replyBuilder.append("]");

          replyBuilder.append(loco.getId());
          replyBuilder.append(" protocol[");
          replyBuilder.append(loco.getDecoderType());
          replyBuilder.append("]");
        }

      }
      case EcosMessageFactory.LOCO_MANAGER_SUBSCRIBE_VIEW -> {

      }
      case EcosMessageFactory.LOCO_MANAGER_RELEASE_VIEW -> {

      }
      case EcosMessageFactory.QUERY_ACCESSORIES -> {
        List<AccessoryBean> accessories = PersistenceFactory.getService().getAccessories();
        for (AccessoryBean accessory : accessories) {
          replyBuilder.append(accessory.getId());
          replyBuilder.append(" name1[");
          replyBuilder.append(accessory.getName());
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" addr[");
          replyBuilder.append(accessory.getAddress());
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" protocol[");
          String protocol = accessory.getProtocol().getValue();
          switch (protocol) {
            case "mm" ->
              replyBuilder.append("MOT");
            default ->
              replyBuilder.append("DCC");
          }
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" symbol[");
          replyBuilder.append(getSymbol(accessory.getType()));
          replyBuilder.append("]");
        }
      }
      case EcosMessageFactory.FEEDBACK_MODULES_SIZE -> {
        List<SensorBean> sensors = PersistenceFactory.getService().getSensors();
        int size = sensors.size() / 16;
        replyBuilder.append(" size[");
        replyBuilder.append(size);
        replyBuilder.append("]");
      }
      default -> {
        //Interpret the message
        //Logger.trace(msg);
        //Logger.trace(message.getIdString() + ": " + message.getCommand());
        String cmd = message.getCommand();
        String id = message.getId();
        int objId = message.getObjectId();

        if (objId < 100) {
          if (objId == Ecos.BASEOBJECT_ID && Ecos.CMD_SET.equals(cmd)) {
            replyBuilder.append(" status[");
            if (msg.contains(Ecos.GO)) {
              replyBuilder.append(Ecos.GO);
            } else {
              replyBuilder.append(Ecos.STOP);
            }
            replyBuilder.append("]");
          }
        } else if (objId >= 100 && objId < 999) {
          if (Ecos.CMD_GET.equals(cmd)) {
            FeedbackModuleBean module = getFeedbackModule(objId);
            replyBuilder.append(module.getAddressOffset() + module.getModuleNumber());
            replyBuilder.append(" state[0x");
            replyBuilder.append(module.getAccumulatedPortsValue());
            replyBuilder.append("]");
          }
        } else if (objId >= 1000 && objId < 9999) {
          switch (cmd) {
            case Ecos.CMD_GET -> {
              //Locomotive details
              LocomotiveBean loco = PersistenceFactory.getService().getLocomotive(Long.parseLong(message.getId()));
              replyBuilder.append(loco.getId());
              replyBuilder.append(" name[\"");
              replyBuilder.append(loco.getName());
              replyBuilder.append("\"]");

              replyBuilder.append(loco.getId());
              replyBuilder.append(" addr[");
              replyBuilder.append(loco.getAddress());
              replyBuilder.append("]");

              replyBuilder.append(loco.getId());
              replyBuilder.append(" protocol[");
              replyBuilder.append(loco.getDecoderType());
              replyBuilder.append("]");

              replyBuilder.append(loco.getId());
              replyBuilder.append(" dir[");
              replyBuilder.append(loco.getDirection().getEcosValue());
              replyBuilder.append("]");

              replyBuilder.append(loco.getId());
              replyBuilder.append(" speed[");
              replyBuilder.append(loco.getVelocity() / 8);
              replyBuilder.append("]");

              replyBuilder.append(loco.getId());
              replyBuilder.append(" speedstep[");
              replyBuilder.append(loco.getVelocity() / 8);
              replyBuilder.append("]");

              replyBuilder.append(loco.getId());
              replyBuilder.append(" active[");
              replyBuilder.append(0);
              replyBuilder.append("]");

              replyBuilder.append(loco.getId());
              replyBuilder.append(" locodesc[\"");
              replyBuilder.append(loco.getIcon());
              replyBuilder.append("\"]");

              //Locomotive functions
              List<FunctionBean> locoFunctions = new ArrayList<>(loco.getFunctions().values());
              for (FunctionBean fb : locoFunctions) {
                replyBuilder.append(loco.getId());
                replyBuilder.append(" func[");
                replyBuilder.append(fb.getNumber());
                replyBuilder.append(",");
                replyBuilder.append((fb.isOn() ? "1" : "0"));
                replyBuilder.append("]");
              }

              for (FunctionBean fb : locoFunctions) {
                replyBuilder.append(loco.getId());
                replyBuilder.append(" funcdesc[");
                replyBuilder.append(fb.getNumber());
                replyBuilder.append(",");
                replyBuilder.append(fb.getFunctionType());
                if (fb.isMomentary()) {
                  replyBuilder.append(",");
                  replyBuilder.append((fb.isMomentary() ? "momentary" : ""));
                }
                replyBuilder.append("]");
              }
            }
            case Ecos.CMD_REQUEST -> {
            }
            case Ecos.CMD_RELEASE -> {
            }

          }
        } else if (objId >= 20000 && objId < 20999) {
          AccessoryBean accessory = PersistenceFactory.getService().getAccessory(id);
          replyBuilder.append(accessory.getId());
          replyBuilder.append(" name1[");
          replyBuilder.append(accessory.getName());
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" addr[");
          replyBuilder.append(accessory.getAddress());
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" protocol[");
          String protocol = accessory.getProtocol().getValue();
          switch (protocol) {
            case "mm" ->
              replyBuilder.append("MOT");
            default ->
              replyBuilder.append("DCC");
          }
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" symbol[");
          replyBuilder.append(getSymbol(accessory.getType()));
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" state[");
          AccessoryBean.AccessoryValue value = accessory.getAccessoryValue();
          switch (value) {
            case AccessoryBean.AccessoryValue.GREEN ->
              replyBuilder.append("0");
            case AccessoryBean.AccessoryValue.RED ->
              replyBuilder.append("1");
            case AccessoryBean.AccessoryValue.WHITE ->
              replyBuilder.append("2");
            case AccessoryBean.AccessoryValue.YELLOW ->
              replyBuilder.append("3");
            default ->
              replyBuilder.append("0");
          }
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" duration[");
          replyBuilder.append(accessory.getSwitchTime());
          replyBuilder.append("]");

          replyBuilder.append(accessory.getId());
          replyBuilder.append(" gates[");
          replyBuilder.append(accessory.getStates());
          replyBuilder.append("]");
        }
      }
    }

    replyBuilder.append("<END 0 (OK)>");
    reply = replyBuilder.toString();
    message.addResponse(reply);

    if (debug) {
      Logger.trace("TX:" + message.getMessage() + " :->\n" + message.getResponse());
    }

    return message;
  }

  @Override
  public void close() {
    disconnect();
  }

  @Override
  public boolean isConnected() {
    return this.connected;
  }

  @Override
  public TransferQueue<EcosMessage> getEventQueue() {
    return this.eventQueue;
  }

  @Override
  public InetAddress getControllerAddress() {
    return NetworkUtil.getIPv4HostAddress();
  }

  static String getSymbol(String type) {
    return switch (type) {
      case "linksweiche" ->
        "0";
      case "rechtsweiche" ->
        "1";
      case "dreiwegweiche" ->
        "2";
      case "y_weiche" ->
        "3";
      case "formsignal_HP01" ->
        "5";
      case "formsignal_HP02" ->
        "6";
      case "formsignal_HP012" ->
        "7";
      case "formsignal_SH01" ->
        "8";
      case "lichtsignal_HP01" ->
        "9";
      case "lichtsignal_HP02" ->
        "10";
      case "lichtsignal_HP012" ->
        "11";
      case "lichtsignal_HP012_SH01" ->
        "12";
      case "lichtsignal_SH01" ->
        "13";
      case "entkupplungsgleis" ->
        "17";
      default ->
        "0";
    };
  }

  FeedbackModuleBean getFeedbackModule(int moduleId) {
    List<SensorBean> sensors = PersistenceFactory.getService().getSensors();
    int id = moduleId;
    int moduleNr = id - 100;
    FeedbackModuleBean module = new FeedbackModuleBean();
    module.setId(id);
    module.setModuleNumber(moduleNr);
    module.setPortCount(16);
    module.setAddressOffset(100);

    for (SensorBean sb : sensors) {
      if (sb.getDeviceId() == moduleNr) {
        int port = sb.getContactId() - 1;
        boolean value = sb.isActive();
        module.setPortValue(port, value);
      }
    }

    return module;
  }

  @Override
  public void sendEvent(SensorEvent sensorEvent) {
    Logger.trace("Device: " + sensorEvent.getDeviceId() + " contact: " + sensorEvent.getContactId() + " -> " + sensorEvent.isActive());
    FeedbackModuleBean fbm = getFeedbackModule(100 + sensorEvent.getDeviceId());
    //Logger.trace(fbm.getIdString()+" nr: "+fbm.getModuleNumber() + " Current ports: " + fbm.portToString());
    int port = sensorEvent.getContactId() - 1;
    fbm.setPortValue(port, sensorEvent.isActive());
    //Logger.trace(100 + fbm.getModuleNumber() + " changed ports: " + fbm.portToString());

    StringBuilder sb = new StringBuilder();
    int id = sensorEvent.getDeviceId() + 100;
    sb.append("<EVENT ");
    sb.append(id);
    sb.append(">");
    sb.append(id);
    sb.append(" state[0x");
    String state = Integer.toHexString(fbm.getAccumulatedPortsValue());
    sb.append(state);
    sb.append("]<END 0 (OK)>");

    EcosMessage eventMessage = new EcosMessage(sb.toString());

    //Logger.trace("Sensor " + eventMessage);
    try {
      this.eventQueue.put(eventMessage);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }
}
