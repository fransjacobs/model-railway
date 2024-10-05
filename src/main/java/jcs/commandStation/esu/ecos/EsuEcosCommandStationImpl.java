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

import java.util.List;
import java.util.concurrent.Executors;
import jcs.commandStation.AbstractController;
import jcs.entities.CommandStationBean;
import jcs.entities.DeviceBean;
import jcs.entities.InfoBean;
import jcs.util.SerialPortUtil;
import org.tinylog.Logger;

public class EsuEcosCommandStationImpl extends AbstractController {

  private int defaultSwitchTime;

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
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

  @Override
  public void disconnect() {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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

    SerialPortUtil.logComports();

    if (1 == 1) {
      CommandStationBean csb = new CommandStationBean();
      csb.setId("esu-ecos");
      csb.setDescription("ESU-ECOS");
      csb.setClassName("jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl");
      csb.setConnectVia("NETWORK");
      csb.setIpAddress("192.168.1.110");
      csb.setNetworkPort(2560);

      csb.setDefault(true);
      csb.setIpAutoConfiguration(false);
      csb.setEnabled(true);
      csb.setShortName("Ecos");
      csb.setDecoderControlSupport(true);
      csb.setAccessorySynchronizationSupport(false);
      csb.setFeedbackSupport(false);
      csb.setLocomotiveFunctionSynchronizationSupport(false);
      csb.setLocomotiveImageSynchronizationSupport(false);
      csb.setLocomotiveSynchronizationSupport(false);
      csb.setProtocols("DCC,MM,MFX");

      //CommandStation cs = CommandStationFactory.getCommandStation(csb);
      EsuEcosCommandStationImpl cs = new EsuEcosCommandStationImpl(csb);

      //cs.connect();
      //cs.power(true);
      Logger.trace("Power is: " + (cs.isPower() ? "On" : "Off"));

    }

  }

}
