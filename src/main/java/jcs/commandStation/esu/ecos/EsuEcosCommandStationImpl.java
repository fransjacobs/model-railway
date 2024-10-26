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

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TransferQueue;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.esu.ecos.entities.BaseObject;
import jcs.commandStation.esu.ecos.net.EcosConnection;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.AccessoryBean;
import jcs.entities.ChannelBean;
import jcs.entities.CommandStationBean;
import jcs.entities.DeviceBean;
import jcs.entities.FeedbackModuleBean;
import jcs.entities.InfoBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

public class EsuEcosCommandStationImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController {

  private int defaultSwitchTime;
  private EcosConnection connection;
  private EventHandler eventMessageHandler;

  private BaseObject baseObject;

  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean) {
    this(commandStationBean, false);
  }

  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean, boolean autoConnect) {
    super(autoConnect, commandStationBean);
    defaultSwitchTime = Integer.getInteger("default.switchtime", 300);
    autoConnect(autoConnect);
  }

  private void autoConnect(boolean autoConnect) {
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
            //Obtain some info about the ECoS
            EcosMessage reply = connection.sendMessage(EcosMessageFactory.getBaseObject());
            this.baseObject = new BaseObject(reply);

            //Start the EventHandler
            eventMessageHandler = new EventHandler(this.connection);
            eventMessageHandler.start();

            reply = connection.sendMessage(EcosMessageFactory.subscribeBaseObject());
            Logger.trace("BaseObjectSubscription reply: " + reply.getResponse());

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
    try {
      if (this.connected) {
        this.connection.sendMessage(EcosMessageFactory.unSubscribeBaseObject());

      }
      this.eventMessageHandler.quit();
      this.connection.close();
    } catch (Exception ex) {
      Logger.error(ex);
    }
  }

  @Override
  public InfoBean getCommandStationInfo() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public DeviceBean getDevice() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<DeviceBean> getDevices() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getIp() {
    if (this.connection != null && this.connection.isConnected()) {
      return this.connection.getControllerAddress().getHostAddress();
    } else {
      return null;
    }
  }

  /**
   * Query the System Status
   *
   * @return true the track power is on else off.
   */
  @Override
  public boolean isPower() {
    if (this.connected) {
      EcosMessage reply = this.connection.sendMessage(EcosMessageFactory.getPowerStatus());
      this.baseObject.update(reply);
      this.power = "GO".equals(this.baseObject.getStatus());
    } else {
      this.power = false;
    }
    return this.power;
  }

  /**
   * System Stop and GO When on = true then the GO command is issued; <br>
   * The command station activates the operation and supplies electrical energy.<br>
   * Any speed levels/functions that may still exist or have been saved will be sent again.<br>
   * When false the Stop command is issued;<br>
   * The command station stops operation on main and programming track.<br>
   * Electrical energy is no longer supplied.<br>
   * All speed levels/function values and settings are retained.
   *
   * @param on true Track power On else Off
   * @return true the Track power is On else Off
   */
  @Override
  public boolean power(boolean on) {
    if (this.connected) {
      EcosMessage reply = this.connection.sendMessage(EcosMessageFactory.setPowerStatus(on));
      this.baseObject.update(reply);
      this.power = "GO".equals(this.baseObject.getStatus());

      PowerEvent pe = new PowerEvent(this.power);
      notifyPowerEventListeners(pe);

      return power;
    } else {
      return false;
    }
  }

  @Override
  public void changeDirection(int locUid, LocomotiveBean.Direction direction) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changeVelocity(int locUid, int speed, LocomotiveBean.Direction direction) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<LocomotiveBean> getLocomotives() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Image getLocomotiveImage(String icon) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isSupportTrackMeasurements() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Map<Integer, ChannelBean> getTrackMeasurements() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchAccessory(Integer address, AccessoryBean.AccessoryValue value) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchAccessory(Integer address, AccessoryBean.AccessoryValue value, Integer switchTime) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<AccessoryBean> getAccessories() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public DeviceBean getFeedbackDevice() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<FeedbackModuleBean> getFeedbackModules() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void fireSensorEventListeners(SensorEvent sensorEvent) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private void notifyPowerEventListeners(final PowerEvent powerEvent) {
    executor.execute(() -> fireAllPowerEventListeners(powerEvent));
  }

  private void fireAllPowerEventListeners(final PowerEvent powerEvent) {
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }

  private class EventHandler extends Thread {

    private boolean stop = false;
    private boolean quit = true;
    private BufferedReader reader;

    private final TransferQueue<EcosMessage> eventQueue;

    public EventHandler(EcosConnection connection) {
      eventQueue = connection.getEventQueue();
    }

    void quit() {
      this.quit = true;
    }

    boolean isRunning() {
      return !this.quit;
    }

    boolean isFinished() {
      return this.stop;
    }

    @Override
    public void run() {
      this.quit = false;
      this.setName("ESU-ECOS-EVENT-HANDLER");

      Logger.trace("Event Handler Started...");

      while (isRunning()) {
        try {
          EcosMessage eventMessage = eventQueue.take();
          int id = eventMessage.getObjectId();

          switch (id) {
            case 1 -> {
              String prevStatus = baseObject.getStatus();
              baseObject.update(eventMessage);
              Logger.trace(baseObject);

              if (!baseObject.getStatus().equals(prevStatus)) {
                power = "GO".equals(baseObject.getStatus());
                Logger.trace("Power changed to: " + (power ? "On" : "Off"));
                power = "GO".equals(baseObject.getStatus());
                PowerEvent pe = new PowerEvent(power);
                fireAllPowerEventListeners(pe);
              }

            }
            default -> {
              Logger.trace(eventMessage.getMessage());
            }
          }

        } catch (InterruptedException ex) {
          Logger.error(ex);
        }
      }

      Logger.debug("Stop receiving");
      try {
        reader.close();
      } catch (IOException ex) {
        Logger.error(ex);
      }
      stop = true;
    }
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

        Logger.trace(cs.baseObject);

        //EcosMessage subscribePowerMessage = EcosMessageFactory.subscribeBaseObject();
        //EcosMessage reply2 = cs.connection.sendMessage(subscribePowerMessage);
        //Logger.trace("subscribe: " + reply2.getResponse());
        boolean power = cs.isPower();
        Logger.trace("1 Power is " + (power ? "On" : "Off"));

        cs.pause(1000);

        power = cs.power(true);
        Logger.trace("2 Power is " + (power ? "On" : "Off"));

        cs.pause(1000);

        power = cs.power(false);
        Logger.trace("3 Power is " + (power ? "On" : "Off"));

        cs.pause(100000);

        cs.connection.sendMessage(EcosMessageFactory.unSubscribeBaseObject());
      }

    }

  }

}
