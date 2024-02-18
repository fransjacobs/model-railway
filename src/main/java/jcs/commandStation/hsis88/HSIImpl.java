/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.commandStation.hsis88;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.entities.DeviceBean;
import jcs.entities.FeedbackModuleBean;
import jcs.entities.InfoBean;
import jcs.entities.SensorBean;
import jcs.util.RunUtil;
import org.tinylog.Logger;

/**
 *
 * @author Frans Jacobs
 */
public class HSIImpl extends AbstractController implements FeedbackController {

  private HSIConnection connection;

  private InfoBean infoBean;
  private final Map<Integer, DeviceBean> devices;
  private DeviceBean mainDevice;
  private DeviceBean feedbackDevice;
  
  private final Map<Integer,SensorBean> sensors;

  public HSIImpl(CommandStationBean commandStationBean) {
    this(false, commandStationBean);
  }

  public HSIImpl(boolean autoConnect, CommandStationBean commandStationBean) {
    super(autoConnect, commandStationBean);
    devices = new HashMap<>();
    sensors = new HashMap<>();
    if (commandStationBean != null) {
      if (autoConnect) {
        Logger.trace("Perform auto connect");
        connect();
      }
    } else {
      Logger.error("Command Station NOT SET!");
    }
  }

  @Override
  public String getIp() {
    return "";
  }

  @Override
  public final synchronized boolean connect() {
    if (!connected) {
      Logger.trace("Connecting to HSI Interface " + (commandStationBean != null ? commandStationBean.getDescription() : "Unknown"));
      if (executor == null || executor.isShutdown()) {
        executor = Executors.newCachedThreadPool();
      }

      if (commandStationBean.getSerialPort() != null) {
        HSIConnectionFactory.writeLastUsedSerialPortProperty(commandStationBean.getSerialPort());
        HSIConnection hsiConnection = HSIConnectionFactory.getConnection(ConnectionType.SERIAL);
        connection = hsiConnection;
      } else {
        Logger.error("Can't connect; ComPort not set!");
      }

      if (connection != null) {
        long now = System.currentTimeMillis();
        long timeout = now + 1000L;

        while (!connected && now < timeout) {
          connected = connection.isConnected();
          now = System.currentTimeMillis();
        }
        if (!connected && now > timeout) {
          Logger.error("Could not establish a connection");
        }
        String info = "";
        if (connected) {
          //Add a listener to listen to HSI Interface responses
          HSIMessageListener messageListener = new HSIFeedbackListener(this);
          connection.addMessageListener(messageListener);

          JCS.logProgress("Obtaining Device information...");
          connection.sendMessage("v\r");
          info = connection.sendMessage("v\r");

          //Create Info
          this.infoBean = new InfoBean();
          this.infoBean.setProductName(commandStationBean.getDescription());
          this.infoBean.setArticleNumber(commandStationBean.getShortName());
          this.infoBean.setHostname(this.commandStationBean.getSerialPort());
          this.infoBean.setProductName(info);

          DeviceBean d = new DeviceBean();
          String[] hsiinfo = info.split("/");
          d.setName(info);
          d.setUid("0");
          for (int i = 0; i < hsiinfo.length; i++) {
            switch (i) {
              case 0 ->
                d.setVersion(hsiinfo[i]);
              case 1 ->
                d.setSerial(hsiinfo[i]);
              case 2 ->
                d.setName(hsiinfo[i]);
              case 3 ->
                d.setTypeName(hsiinfo[i]);
            }
          }
          this.mainDevice = d;
          this.devices.put(0, d);
          
          //Query the S88 Modules ?
          //connection.sendMessage("m\r");          
          //this.connection.setFeedbackListener(feedbackListener);  
        }
        Logger.trace(connected ? "Connected to: " + info : " Not connected");
      } else {
        Logger.warn("Can't connect with HSI S88 Interface!");
        JCS.logProgress("Can't connect with HSI S88 Interface!");
      }
    }

    return connected;
  }

  @Override
  public DeviceBean getDevice() {
    return this.mainDevice;
  }

  @Override
  public List<DeviceBean> getDevices() {
    return null;//this.devices.values().stream().collect(Collectors.toList());
  }

  @Override
  public InfoBean getCommandStationInfo() {
    return infoBean;
  }

  @Override
  public DeviceBean getFeedbackDevice() {
    return this.feedbackDevice;
  }

  @Override
  public List<FeedbackModuleBean> getFeedbackModules() {
    return null;
  }

  @Override
  public void disconnect() {
    try {
      if (connection != null) {
        connection.close();
        connected = false;
      }

      if (executor != null) {
        executor.shutdown();
      }
      executor = null;
      connection = null;

      HSIConnectionFactory.disconnectAll();
    } catch (Exception ex) {
      Logger.error(ex);
    }
    Logger.trace("Disconnected");
  }

  @Override
  public void fireSensorEventListeners(final SensorEvent sensorEvent) {
    for (SensorEventListener listener : sensorEventListeners) {
      listener.onSensorChange(sensorEvent);
    }
  }

  private void notifySensorEventListeners(final SensorEvent sensorEvent) {
    executor.execute(() -> fireSensorEventListeners(sensorEvent));
  }

  private class HSIFeedbackListener implements HSIMessageListener {

    private final HSIImpl hsiImpl;

    HSIFeedbackListener(HSIImpl hsiImpl) {
      this.hsiImpl = hsiImpl;
    }

    @Override
    public void onMessage(final HSIMessage message) {
      Logger.trace(message);
    }
  }

  private List<SensorEvent> createSensorEvents(HSIMessage message) {
    List<SensorEvent> events = new LinkedList<>();
    
    List<HSIMessage.S88Module> changedModules = message.getModules();
    
    for(HSIMessage.S88Module module : changedModules) {
      //Each moduel has 16 contacts
      int moduleNr = module.getModuleNumber();
      int low = module.getLowByte();
      int high = module.getHighByte();
        
      //To be continued      
      
    }
    
    
    
    return events;
  }
  
  
// private class CanFeedbackMessageListener implements FeedbackListener {
//
//    private final MarklinCentralStationImpl controller;
//
//    CanFeedbackMessageListener(MarklinCentralStationImpl controller) {
//      this.controller = controller;
//    }
//
//    @Override
//    public void onFeedbackMessage(final CanMessage message) {
//      int cmd = message.getCommand();
//      switch (cmd) {
//        case CanMessage.S88_EVENT_RESPONSE -> {
//          if (CanMessage.DLC_8 == message.getDlc()) {
//            SensorEvent sme = new SensorEvent(message, new Date());
//            if (sme.getSensorBean() != null) {
//              controller.notifySensorEventListeners(sme);
//            }
//          }
//        }
//      }
//    }
//  }
  
  
  
  
  
  
  
//  
  public static void main(String[] a) {
    RunUtil.loadExternalProperties();

    CommandStationBean csb = new CommandStationBean();
    csb.setId("hsi-s88");
    csb.setDescription("HSI S88");
    csb.setShortName("HSI");
    csb.setClassName("jcs.commandStation.hsis88.HSIImpl");
    csb.setConnectVia("SERIAL");
    csb.setSerialPort("cu.usbmodem14201");  //cu.usbmodem14201
    csb.setDecoderControlSupport(false);
    csb.setAccessorySynchronizationSupport(false);
    csb.setFeedbackSupport(true);
    csb.setLocomotiveFunctionSynchronizationSupport(false);
    csb.setLocomotiveImageSynchronizationSupport(false);
    csb.setLocomotiveSynchronizationSupport(false);
    //csb.setNetworkPort(15731);
    //csb.setProtocols();
    csb.setDefault(false);
    csb.setEnabled(true);

    //HSIConnectionFactory.logComports();
    HSIImpl cs = new HSIImpl(false, csb);
    Logger.debug((cs.connect() ? "Connected" : "NOT Connected"));

    if (cs.isConnected()) {

    }

    cs.pause(40000);
    cs.disconnect();
    cs.pause(100L);
    Logger.debug("DONE");
    //System.exit(0);
  }
  //for (int i = 0; i < 16; i++) {
  //    cs.requestFeedbackEvents(i + 1);
  //}

}
