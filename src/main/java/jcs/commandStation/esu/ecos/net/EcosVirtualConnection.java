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
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import jcs.util.NetworkUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
class EcosVirtualConnection implements EcosConnection {

  private boolean connected;

  private final TransferQueue<String> transferQueue;
  private final TransferQueue<EcosMessage> eventQueue;

  private EcosMessageListener messageListener;
  private boolean debug = false;

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
        List<LocomotiveBean> locos = PersistenceFactory.getService().getLocomotives();

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
      default -> {
        //Interpret the message
        Logger.trace(msg);
        Logger.trace(message.getId() + ": " + message.getCommand());
        String cmd = message.getCommand();
        int objId = message.getObjectId();

        if (objId >= 1000 && objId < 9999) {
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
        }
      }
    }

    replyBuilder.append("<END 0 (OK)>");
    reply = replyBuilder.toString();
    message.addResponse(reply);

    if (debug) {
      Logger.trace("TX:" + message.getMessage() + " :->\n" + message.getMessage());
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

}
