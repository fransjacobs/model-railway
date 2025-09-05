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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.SensorEvent;
import static jcs.commandStation.hsis88.HSIConnection.COMMAND_VERSION;
import jcs.entities.CommandStationBean;
import jcs.entities.CommandStationBean.ConnectionType;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.VirtualConnection;
import jcs.commandStation.entities.Device;
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.entities.SensorBean;
import jcs.util.RunUtil;
import org.tinylog.Logger;
import jcs.commandStation.events.ConnectionEventListener;

/**
 *
 * @author Frans Jacobs
 */
public class HSIImpl extends AbstractController implements FeedbackController {

  private HSIConnection connection;

  private InfoBean infoBean;
  //private final Map<Integer, DeviceBean> devices;
  //private DeviceBean mainDevice;
  //private DeviceBean feedbackDevice;

  private final Map<Integer, SensorBean> sensors;

  public HSIImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public HSIImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    //devices = new HashMap<>();
    sensors = new HashMap<>();
    //this.executor = Executors.newCachedThreadPool();

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
        long timeout = now + 5000L;

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

          //Wait a while until the interface is ready
          pause(3000);
          JCS.logProgress("Obtaining Device information...");
          info = connection.sendMessage(COMMAND_VERSION);

          //Create Info
          this.infoBean = new InfoBean();
          this.infoBean.setProductName(commandStationBean.getDescription());
          this.infoBean.setArticleNumber(commandStationBean.getShortName());
          this.infoBean.setHostname(this.commandStationBean.getSerialPort());
          this.infoBean.setProductName(info);

          //DeviceBean d = new DeviceBean();
          String[] hsiinfo = info.split("/");
          //d.setName(info);
          //d.setUid("0");
//          for (int i = 0; i < hsiinfo.length; i++) {
//            switch (i) {
//              case 0 ->
//                //d.setVersion(hsiinfo[i]);
//              case 1 ->
//                //d.setSerial(hsiinfo[i]);
//              case 2 ->
//                //d.setName(hsiinfo[i]);
//              case 3 ->
//                //d.setTypeName(hsiinfo[i]);
//            }
//          }
//          //this.mainDevice = d;
//          this.devices.put(0, d);

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
  public InfoBean getCommandStationInfo() {
    return infoBean;
  }

  public List<Device> getDevices() {
    List<Device> devices = new ArrayList<>();
    return devices;
  }

  @Override
  public List<FeedbackModule> getFeedbackModules() {
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

  private void fireAllDisconnectionEventListeners(final ConnectionEvent disconnectionEvent) {
    for (ConnectionEventListener listener : this.connectionEventListeners) {
      listener.onConnectionChange(disconnectionEvent);
    }
    disconnect();
  }

  @Override
  public void fireAllSensorEventsListeners(final SensorEvent sensorEvent) {
    List<AllSensorEventsListener> snapshot = new ArrayList<>(allSensorEventsListeners);
    for (AllSensorEventsListener listener : snapshot) {
      listener.onSensorChange(sensorEvent);
    }
  }

  @Override
  public void simulateSensor(SensorEvent sensorEvent) {
    if (this.connection instanceof VirtualConnection virtualConnection) {
      virtualConnection.sendEvent(sensorEvent);
    }
  }

  private class HSIFeedbackListener implements HSIMessageListener {

    private final HSIImpl hsiImpl;

    HSIFeedbackListener(HSIImpl hsiImpl) {
      this.hsiImpl = hsiImpl;
    }

    @Override
    public void onMessage(final HSIMessage message) {
      executor.execute(() -> hsiImpl.fireSensorEvents(message));
    }

    @Override
    public void onDisconnect(ConnectionEvent event) {
      this.hsiImpl.fireAllDisconnectionEventListeners(event);
    }
  }

  private void fireSensorEvents(HSIMessage message) {
    List<SensorEvent> events = new LinkedList<>();
    List<HSIMessage.S88Module> changedModules = message.getModules();

    for (HSIMessage.S88Module module : changedModules) {
      //Each module has 16 contacts
      int moduleNr = module.getModuleNumber();
      int low = module.getLowByte();
      int high = module.getHighByte();
      int[] contacts = new int[16];

      for (int i = 0; i < 8; i++) {
        int m = ((int) Math.pow(2, i));
        int lv = (low & m) > 0 ? 1 : 0;
        int hv = (high & m) > 0 ? 1 : 0;
        contacts[i] = lv;
        contacts[(i + 8)] = hv;
      }

      int contactOffset = (moduleNr - 1) * 16;
      for (int i = 0; i < contacts.length; i++) {
        Integer key = contactOffset + i + 1;

        SensorBean sb;
        if (this.sensors.containsKey(key)) {
          //Update 
          sb = sensors.get((i + 1));
          if (!sb.getStatus().equals(contacts[i])) {
            sb.setPreviousStatus(sb.getStatus());
            sb.setStatus(contacts[i]);

            SensorEvent se = new SensorEvent(sb);
            events.add(se);

            //Logger.trace("U: " + sb.toLogString());
          }
        } else {
          //TODO: !!!!!!!
          sb = null; //new SensorBean(0, key, contacts[i]);
          this.sensors.put(sb.getContactId(), sb);

          SensorEvent se = new SensorEvent(sb);
          events.add(se);

          //Logger.trace("A: " + sb.toLogString());
        }
      }

      //Inform the sensor listeners
      //Logger.trace("Informing " + allSensorEventsListeners.size() + " sensorListeners with " + events.size() + " events");
      for (SensorEvent sensorEvent : events) {
        fireAllSensorEventsListeners(sensorEvent);
      }
    }

  }

  public static void main(String[] a) {
    RunUtil.loadExternalProperties();

    CommandStationBean csb = new CommandStationBean();
    csb.setId("hsi-s88");
    csb.setDescription("HSI S88");
    csb.setShortName("HSI");
    csb.setClassName("jcs.commandStation.hsis88.HSIImpl");
    csb.setConnectVia("SERIAL");
    //csb.setSerialPort("cu.usbmodem14201");  //cu.usbmodem14201
    csb.setSerialPort("ttyACM1");  //cu.usbmodem14201
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
    HSIImpl cs = new HSIImpl(csb, false);
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
