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

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.esu.ecos.entities.BaseObject;
import jcs.commandStation.esu.ecos.net.EcosConnection;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import jcs.entities.CommandStationBean;
import jcs.entities.DeviceBean;
import jcs.entities.InfoBean;
import org.tinylog.Logger;

public class EsuEcosCommandStationImpl extends AbstractController {

  private int defaultSwitchTime;
  private EcosConnection connection;

  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    defaultSwitchTime = Integer.getInteger("default.switchtime", 300);
    this.executor = Executors.newCachedThreadPool();

    if (commandStationBean != null) {
      if (autoConnect) {
        Logger.trace("Perform auto connect");
//        connect();
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
            //DccExMessageListener systemEventListener = new DccExCommandStationImpl.MessageListener(this);
            //this.connection.setMessageListener(systemEventListener);

            now = System.currentTimeMillis();
            long start = now;
            timeout = now + 200L;

//            while (this.mainDevice == null && now < timeout) {
//              pause(100);
//              now = System.currentTimeMillis();
//            }
//            if (mainDevice != null) {
//              if (debug) {
//                Logger.trace("Main Device found in " + (now - start) + " ms");
//              }
//            } else {
//              if (debug && mainDevice == null) {
//                Logger.trace("No Main Device found in " + (now - start) + " ms");
//              }
//            }
//            //Create Info
//            this.infoBean = new InfoBean();
//            this.infoBean.setProductName(commandStationBean.getDescription());
//            this.infoBean.setArticleNumber(commandStationBean.getShortName());
//
//            if (conType == CommandStationBean.ConnectionType.NETWORK) {
//              this.infoBean.setHostname(this.commandStationBean.getIpAddress());
//            } else {
//              this.infoBean.setHostname(this.commandStationBean.getSerialPort());
//            }
//            //Wait for the power status to be set
//            now = System.currentTimeMillis();
//            start = now;
//            timeout = now + (conType == CommandStationBean.ConnectionType.NETWORK ? 200L : 5000L);
//
//            while (!this.powerStatusSet && now < timeout) {
//              pause(50);
//              now = System.currentTimeMillis();
//            }
//
//            if (powerStatusSet) {
//              if (debug) {
//                Logger.trace("Power Status set in " + (now - start) + " ms");
//              }
//            } else {
//              if (debug) {
//                Logger.trace("Power Status not set in " + (now - start) + " ms");
//              }
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

  @Override
  public void disconnect() {
  }

  @Override
  public InfoBean getCommandStationInfo() {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody

  }

  @Override
  public DeviceBean getDevice() {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public List<DeviceBean> getDevices() {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public String getIp() {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
        EcosMessage baseObjectMessage = EcosMessageFactory.getBaseObjectMessage();
        
        EcosMessage reply = cs.connection.sendMessage(baseObjectMessage);

        BaseObject baseObject = new BaseObject(reply);

        Logger.trace("BaseObject: " + baseObject);
        
        Logger.trace("\n"+reply.getResponse());

      }

    }

  }

}
