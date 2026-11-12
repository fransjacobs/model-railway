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
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jcs.JCS;
import jcs.commandStation.AbstractController;
import jcs.commandStation.AccessoryController;
import jcs.commandStation.DecoderController;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.esu.ecos.net.EcosConnection;
import jcs.commandStation.esu.ecos.net.EcosConnectionFactory;
import jcs.commandStation.events.PowerEvent;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.AccessoryBean;
import jcs.entities.CommandStationBean;
import jcs.commandStation.entities.FeedbackModule;
import jcs.commandStation.entities.InfoBean;
import jcs.commandStation.esu.ecos.net.EcosHTTPConnection;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.LocomotiveDirectionEvent;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEvent;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEvent;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.automation.DriveSimulator;
import jcs.commandStation.VirtualConnection;
import jcs.commandStation.automation.RailController;
import jcs.commandStation.entities.Device;
import jcs.commandStation.esu.ecos.entities.EcosBooster;
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.events.MeasurementEventListener;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import static jcs.entities.AccessoryBean.AccessoryValue.RED2;
import static jcs.entities.AccessoryBean.AccessoryValue.WHITE;
import static jcs.entities.AccessoryBean.AccessoryValue.YELLOW;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import org.tinylog.Logger;

public class EsuEcosCommandStationImpl extends AbstractController implements DecoderController, AccessoryController, FeedbackController, ConnectionEventListener {
  
  EcosConnectionFactory factory;
  private EventHandler eventMessageHandler;
  
  private volatile EcosManager ecosManager;
  private volatile LocomotiveManager locomotiveManager;
  private volatile AccessoryManager accessoryManager;
  private volatile FeedbackManager feedbackManager;
  private volatile BoosterManager boosterManager;
  
  private volatile DriveSimulator simulator;
  // Controlled by JCSCommandStation before an attempt begins.
  private volatile boolean recoveryMode;

  // STOP must cancel any delayed GO retry. Only the actual TCP exchange is
  // serialized; the retry delay never holds this lock.
  private final Object powerCommandLock = new Object();
  private final EcosPowerRetryPolicy powerRetryPolicy = new EcosPowerRetryPolicy();
  private static final long GO_RETRY_DELAY_MS = 650L;

  
  public EsuEcosCommandStationImpl(CommandStationBean commandStationBean) {
    super(commandStationBean);
    factory = EcosConnectionFactory.getInstance();
    
    factory.setIpAddress(commandStationBean.getIpAddress());
    factory.setVirtual(commandStationBean.isVirtual());
    // ConnectionLifecycleManager owns all connection attempts and backoff.
    factory.setConnectionLostListener(this::onTransportLost);
  }
  
  @Override
  public void setVirtual(boolean flag) {
    virtual = flag;
    factory.setVirtual(flag);
    Logger.info("Switching Virtual Mode " + (flag ? "On" : "Off"));
    disconnect();
    // For direct driver use, retain the legacy reconnect behavior.
    // JCSCommandStation's lifecycle-managed mode switch does not call here.
    connect();
  }
  
  /**
   * Recovery mode must be set before reconnect begins, never afterward.
   * The first command on the new TCP session will then be STOP.
   */
  public void setRecoveryMode(boolean recovery) {
    recoveryMode = recovery;
    if (recovery) {
      powerRetryPolicy.cancelPending();
    }
  }

  private void onTransportLost(EcosConnection lostConnection) {
    powerRetryPolicy.cancelPending();
    connected = false;
    recoveryMode = true;
    // Do not close sockets or join the event handler on the receiver thread.
    ConnectionEvent event = new ConnectionEvent(
            commandStationBean.getDescription(), false, factory.isVirtual());
    for (ConnectionEventListener listener : new ArrayList<>(connectionEventListeners)) {
      try {
        listener.onConnectionChange(event);
      } catch (RuntimeException ex) {
        Logger.warn("ECoS disconnection listener failed: {}", ex.getMessage());
      }
    }
  }

  @Override
  public final synchronized boolean connect() {
    if (connected && factory.isConnected() && ecosManager != null) {
      return true;
    }
    
    Logger.trace("Connecting to a " + (virtual ? "Virtual " : "") + "ESU ECoS Command Station...");
    
    if (executor == null || executor.isShutdown()) {
      executor = Executors.newCachedThreadPool();
    }
    
    if (commandStationBean == null) {
      Logger.error("ESU ECoS Command Station Configuration NOT set!");
      return false;
    }
    
    // Called by ConnectionLifecycleManager's worker: one synchronous attempt.
    // Retry timing and cancellation belong exclusively to that manager.
    EcosConnection connection = factory.acquireConnection();
    
    if (connection == null || !factory.isConnected()) {
      Logger.warn("Can't connect with a ESU ECoS Command Station within {} ms!", EcosConnection.DEFAULT_CONNECT_TIMEOUT_MS);
      JCS.logProgress("Can't connect with ESU ECoS Command Station!");
      return false;
    }
    
    if (connection.getControllerAddress() != null) {
      commandStationBean.setIpAddress(connection.getControllerAddress().getHostAddress());
    }

    if (recoveryMode) {
      // STOP must precede all roster/subscription queries on reconnect.
      EcosMessage stopped = connection.sendMessage(EcosMessageFactory.setPowerStatus(false));
      EcosMessage actual = connection.sendMessage(EcosMessageFactory.getPowerStatus());
      if (!isSuccessfulReply(stopped) || !isSuccessfulReply(actual)
              || !Ecos.STOP.equals(actual.getValueMap().get(Ecos.STATUS))) {
        Logger.warn("ECoS recovery STOP not confirmed; retrying safely");
        return false;
      }
      power = false;
    }

    //Start the EventHandler
    eventMessageHandler = new EventHandler(connection);
    eventMessageHandler.start();

    //Obtain some info about the ECoS
    initBaseObject();
    
    initLocomotiveManager();
    Logger.trace("There are " + locomotiveManager.getSize() + " locomotives");
    
    initAccessoryManager();
    Logger.trace("There are " + accessoryManager.getSize() + " accessories");
    
    initFeedbackManager();
    Logger.trace("There are " + feedbackManager.getSize() + " feedback modules");
    
    initBoosterManager();
    Logger.trace("There are " + boosterManager.getSize() + " boosters");
    
    if (factory.isVirtual()) {
      simulator = new DriveSimulator();
      Logger.info("ECoS Virtual Mode Enabled!");
    }
    
    Logger.trace("{}", (factory.isConnected() ? "Connected" : "NOT Connected"));
    
    connected = factory.isConnected();
    return connected;
  }

  @Override
  public boolean isConnected() {
    return connected && factory.isConnected() && ecosManager != null;
  }
  
  private void initBaseObject() {
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.getBaseObject());
    if (!isSuccessfulReply(reply)) {
      throw new IllegalStateException("ECoS base-object query failed: " + responseDescription(reply));
    }
    ecosManager = new EcosManager(this, reply);
    
    factory.getConnection().sendMessage(EcosMessageFactory.subscribeBaseObject());
    addPowerEventListener(ecosManager);
  }
  
  private void initLocomotiveManager() {
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.getLocomotives());
    if (reply == null || !reply.isValid()) {
      throw new IllegalStateException("ECoS locomotive-list query failed: "
              + (reply == null ? "no response" : reply.getResponseCode()));
    }
    locomotiveManager = new LocomotiveManager(this, reply);
    
    factory.getConnection().sendMessage(EcosMessageFactory.subscribeLokManager());
    
    for (LocomotiveBean loc : locomotiveManager.getLocomotives().values()) {
      EcosMessage detailsReply = factory.getConnection().sendMessage(EcosMessageFactory.getLocomotiveDetails(loc.getId()));
      locomotiveManager.update(detailsReply);

      //Subscribe
      factory.getConnection().sendMessage(EcosMessageFactory.subscribeLocomotive(loc.getId()));
    }
    
    addLocomotiveSpeedEventListener(locomotiveManager);
    addLocomotiveDirectionEventListener(locomotiveManager);
    addLocomotiveFunctionEventListener(locomotiveManager);
  }
  
  private void initAccessoryManager() {
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.getAccessories());
    if (reply != null) {
      Logger.info(reply.getResponse());
    }
    if (!isSuccessfulReply(reply)) {
      throw new IllegalStateException("ECoS accessory-list query failed: "
              + (reply == null ? "no response" : reply.getResponseCode()));
    }
    accessoryManager = new AccessoryManager(this, reply);
    
    for (AccessoryBean accessory : accessoryManager.getAccessories().values()) {
      EcosMessage detailsReply = factory.getConnection().sendMessage(EcosMessageFactory.getAccessoryDetails(accessory.getId()));
      accessoryManager.update(detailsReply);
      //Subscribe
      factory.getConnection().sendMessage(EcosMessageFactory.subscribeAccessory(accessory.getId()));
    }
  }
  
  private void initFeedbackManager() {
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.getNumberOfFeedbackModules());
    if (reply == null || !reply.isValid()) {
      throw new IllegalStateException("ECoS feedback-manager query failed: "
              + (reply == null ? "no response" : reply.getResponseCode()));
    }
    feedbackManager = new FeedbackManager(this, reply);
    Logger.info("ECoS feedback modules reported: {}", feedbackManager.getSize());
    
    for (int i = 0; i < feedbackManager.getSize(); i++) {
      int moduleId = i + FeedbackManager.S88_OFFSET;
      factory.getConnection().sendMessage(EcosMessageFactory.subscribeFeedbackModule(moduleId));
      //Logger.trace("r: "+reply.getResponse());
    }
  }
  
  private void initBoosterManager() {
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.getBoosters());
    if (!isSuccessfulReply(reply)) {
      throw new IllegalStateException("ECoS booster-list query failed: " + responseDescription(reply));
    }
    boosterManager = new BoosterManager(this, reply);
    factory.getConnection().sendMessage(EcosMessageFactory.subscribeBoosterManager());
    
    for (EcosBooster eb : boosterManager.getBoosters()) {
      EcosMessage detailsReply = factory.getConnection().sendMessage(EcosMessageFactory.getBoosterDetails(eb.getId()));
      boosterManager.update(detailsReply);
      factory.getConnection().sendMessage(EcosMessageFactory.subscribeBooster(eb.getId()));
    }
  }
  
  @Override
  public SensorBean getSensorStatus(SensorBean sensorBean) {
    Integer sensorId = sensorBean.getId();
    return feedbackManager.getSensor(sensorId);
  }
  
  @Override
  public synchronized void disconnect() {
    powerRetryPolicy.cancelPending();
    connected = false;
    EventHandler handler = eventMessageHandler;
    eventMessageHandler = null;
    if (handler != null) {
      handler.quit();
      handler.interrupt();
    }
    // No remote commands on an offline or partially initialized session.
    factory.disconnectAll();
    if (handler != null && Thread.currentThread() != handler) {
      try {
        handler.join(1500L);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
    if (ecosManager != null) {
      removePowerEventListener(ecosManager);
    }
    if (locomotiveManager != null) {
      removeLocomotiveSpeedEventListener(locomotiveManager);
      removeLocomotiveDirectionEventListener(locomotiveManager);
      removeLocomotiveFunctionEventListener(locomotiveManager);
    }
    ecosManager = null;
    accessoryManager = null;
    locomotiveManager = null;
    boosterManager = null;
    feedbackManager = null;
    simulator = null;
  }

  @Override
  public void onConnectionChange(ConnectionEvent event) {
    String s = event.getSource();
    boolean con = event.isConnected();
    if (con) {
      Logger.trace(s + " has re-connected");
    } else {
      powerRetryPolicy.cancelPending();
      connected = false;
    }

    for (ConnectionEventListener listener : connectionEventListeners) {
      listener.onConnectionChange(event);
    }
  }
  
  @Override
  public InfoBean getCommandStationInfo() {
    InfoBean ib = new InfoBean(commandStationBean);

    // UI callers (including JCSFrame.getTitleString()) must never initiate
    // TCP connection or session initialization on Swing's event thread.
    EcosManager current = ecosManager;
    if (current != null) {
      String name = current.getName();
      String stationType = current.getCommandStationType();
      if (name != null) {
        ib.setArticleNumber(stationType != null ? name.replace(stationType + "-", "") : name);
        ib.setDescription(name);
        ib.setProductName(name);
      }
      ib.setSerialNumber(current.getSerialNumber());
      ib.setHardwareVersion(current.getHardwareVersion());
      ib.setSoftwareVersion(current.getApplicationVersion());
      ib.setHostname(getIp());
      if (ib.getIpAddress() == null) {
        ib.setIpAddress(getIp());
      }
    } else {
      ib.setDescription("Not Connected");
      ib.setHostname("Not Connected");
      Logger.warn("NOT Connected!");
    }
    return ib;
  }
  
  @Override
  public List<Device> getDevices() {
    // A TCP transport can be connected while the ECoS object managers are
    // still being initialized. Never let a settings dialog crash on a
    // partially initialized session.
    List<Device> devices = new ArrayList<>();
    EcosManager base = ecosManager;
    if (base != null) {
      Device ecos = new Device();
      ecos.setId(EcosManager.ID + "");
      ecos.setName(base.getName());
      ecos.setSerialNumber(base.getSerialNumber());
      ecos.setHardwareVersion(base.getHardwareVersion());
      ecos.setSoftwareVersion(base.getApplicationVersion());
      devices.add(ecos);
    }
    LocomotiveManager locomotives = locomotiveManager;
    if (locomotives != null) {
      Device locs = new Device();
      locs.setId(LocomotiveManager.ID + "");
      locs.setName("LocomotiveManager");
      locs.setSize(locomotives.getSize());
      devices.add(locs);
    }
    AccessoryManager accessories = accessoryManager;
    if (accessories != null) {
      Device acm = new Device();
      acm.setId(AccessoryManager.ID + "");
      acm.setName("AccessoryManager");
      acm.setSize(accessories.getSize());
      devices.add(acm);
    }
    FeedbackManager feedback = feedbackManager;
    if (feedback != null) {
      Device fbm = new Device();
      fbm.setId(FeedbackManager.ID + "");
      fbm.setName("FeedbackManager");
      fbm.setSize(feedback.getSize());
      fbm.setChannels(feedback.getModules().size());
      fbm.setFeedback(true);
      devices.add(fbm);
    }
    BoosterManager boosters = boosterManager;
    if (boosters != null) {
      Device bm = new Device();
      bm.setId(BoosterManager.ID + "");
      bm.setName("BoosterManager");
      bm.setSize(boosters.getSize());
      devices.add(bm);
    }
    return devices;
  }
  
  @Override
  public String getIp() {
    return factory.getIpAddress();
  }

  /**
   * Query the System Status
   *
   * @return true the track power is on else off.
   */
  @Override
  public boolean isPower() {
    if (!factory.isConnected() || ecosManager == null) {
      power = false;
      return false;
    }

    EcosMessage status = factory.getConnection().sendMessage(EcosMessageFactory.getPowerStatus());
    if (!isSuccessfulReply(status)) {
      Logger.warn("Unable to refresh ECoS track power status: {}", responseDescription(status));
      // Unknown is not the same as OFF. Keep the last confirmed value.
      return power;
    }

    ecosManager.update(status);
    power = Ecos.GO.equals(ecosManager.getStatus());
    return power;
  }

  /**
   * Sends STOP immediately, subject only to an in-flight TCP transaction.
   * A GO rejected with error 32 ("no go after stop") is retried once after
   * a short, cancellable wait. Any newer power request, especially STOP,
   * cancels that GO retry.
   *
   * The returned boolean is the last confirmed track-power state, not a
   * command-success flag. In particular, false is the expected STOP result.
   */
  @Override
  public boolean power(boolean on) {
    long requestId = powerRetryPolicy.beginRequest();

    if (!factory.isConnected() || ecosManager == null) {
      Logger.warn("Cannot switch ECoS track power {}: not initialized/connected", on ? "ON" : "OFF");
      return false;
    }

    // Never replay a GO against a different TCP session after reconnect.
    final EcosConnection session = factory.getConnection();
    final int attempts = on ? 2 : 1;
    for (int attempt = 1; attempt <= attempts; attempt++) {
      int errorCode = -1;
      boolean confirmed = false;

      synchronized (powerCommandLock) {
        if (!powerRetryPolicy.isCurrent(requestId) || !factory.isConnected()
                || factory.getConnection() != session) {
          Logger.debug("ECoS power {} request superseded before send", on ? "GO" : "STOP");
          return power;
        }

        EcosMessage command = EcosMessageFactory.setPowerStatus(on);
        EcosMessage reply = session.sendMessage(command);
        if (reply != null && reply.isResponseComplete()) {
          errorCode = reply.getErrorCode();
        }
        Logger.trace("ECoS power {} attempt {} reply: {}",
                on ? "GO" : "STOP", attempt, responseDescription(reply));

        // Always query the actual power status. A successful SET acknowledgement
        // has no status body; a rejected GO must not be mistaken for success.
        EcosMessage status = factory.isConnected() && factory.getConnection() == session
                ? session.sendMessage(EcosMessageFactory.getPowerStatus())
                : null;
        if (isSuccessfulReply(status)) {
          ecosManager.update(status);
          power = Ecos.GO.equals(ecosManager.getStatus());
          confirmed = true;
        } else {
          Logger.warn("ECoS power status verification failed after {}: {}",
                  on ? "GO" : "STOP", responseDescription(status));
        }

        if (!powerRetryPolicy.isCurrent(requestId)) {
          Logger.debug("ECoS power request superseded while awaiting acknowledgement");
          return power;
        }

        if (confirmed && power == on && errorCode == 0) {
          return power;
        }

        if (!on || errorCode != EcosPowerRetryPolicy.NO_GO_AFTER_STOP) {
          Logger.warn("ECoS {} not accepted or not confirmed: reply={}, actualPower={}",
                  on ? "GO" : "STOP", responseDescription(reply), confirmed ? power : "unknown");
          // If status did not change, EcosManager will not emit an event.
          // Restore the JCSFrame toggle to the confirmed state after rejection.
          if (confirmed) {
            firePowerEventListeners(new PowerEvent(power));
          }
          return power;
        }
      }

      if (powerRetryPolicy.shouldRetryGo(requestId, errorCode, attempt, attempts)) {
        Logger.info("ECoS refused GO immediately after STOP (code 32); retrying once after {} ms",
                GO_RETRY_DELAY_MS);
        try {
          if (!powerRetryPolicy.awaitRetry(requestId, GO_RETRY_DELAY_MS)) {
            Logger.debug("Cancelled delayed ECoS GO: newer power request received");
            return power;
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          Logger.warn("Interrupted while waiting to retry ECoS GO");
          return power;
        }
      } else {
        Logger.warn("ECoS GO still rejected after {} attempt(s); remaining {}",
                attempt, power ? "ON" : "OFF");
        if (confirmed && powerRetryPolicy.isCurrent(requestId)) {
          firePowerEventListeners(new PowerEvent(power));
        }
        return power;
      }
    }
    return power;
  }

  /**
   * Synchronous shutdown STOP with an explicit fresh protocol readback.
   * power(false) cancels any pending GO retry; a failed status query must
   * never be misinterpreted as confirmation that track power is OFF.
   */
  public boolean stopTrackPowerForShutdown() {
    power(false);
    if (!factory.isConnected()) {
      return false;
    }
    EcosMessage status = factory.getConnection().sendMessage(EcosMessageFactory.getPowerStatus());
    if (!isSuccessfulReply(status)) {
      Logger.warn("Shutdown STOP readback failed: {}", responseDescription(status));
      return false;
    }
    ecosManager.update(status);
    power = Ecos.GO.equals(ecosManager.getStatus());
    return Ecos.STOP.equals(status.getValueMap().get(Ecos.STATUS));
  }

  /**
   * ECoS recovery: verify STOP and zero velocity for every known locomotive.
   * Never send GO here. The operator must explicitly re-enable track power.
   */
  public boolean stopLocomotivesForRecovery(Collection<Integer> ids) {
    powerRetryPolicy.cancelPending();
    if (!isConnected() || !recoveryMode) {
      Logger.warn("ECoS recovery requested without a ready recovery session");
      return false;
    }
    final EcosConnection session = factory.getConnection();
    synchronized (powerCommandLock) {
      EcosMessage stop = session.sendMessage(EcosMessageFactory.setPowerStatus(false));
      EcosMessage status = session.sendMessage(EcosMessageFactory.getPowerStatus());
      if (!isSuccessfulReply(stop) || !isSuccessfulReply(status)
              || !Ecos.STOP.equals(status.getValueMap().get(Ecos.STATUS))) {
        Logger.warn("Cannot confirm ECoS STOP during recovery");
        return false;
      }
      ecosManager.update(status);
      power = false;

      for (Integer id : ids) {
        if (id == null) {
          continue;
        }
        boolean ok = false;
        try {
          EcosMessage control = session.sendMessage(EcosMessageFactory.getRequestLocomotiveControl(id));
          if (!isSuccessfulReply(control)) {
            Logger.warn("Unable to obtain ECoS control of locomotive {} during recovery", id);
            return false;
          }
          EcosMessage zero = session.sendMessage(EcosMessageFactory.setLocomotiveSpeed(id, 0));
          if (!isSuccessfulReply(zero)) {
            Logger.warn("Unable to STOP ECoS locomotive {} during recovery", id);
            return false;
          }
          EcosMessage readback = session.sendMessage(EcosMessageFactory.getLocomotiveDetails(id));
          if (!isSuccessfulReply(readback)
                  || !"0".equals(readback.getValueMap().get(Ecos.SPEED))) {
            Logger.warn("ECoS locomotive {} zero-speed verification failed", id);
            return false;
          }
          ok = true;
        } finally {
          EcosMessage release = session.sendMessage(EcosMessageFactory.getReleaseLocomotiveControl(id));
          if (!isSuccessfulReply(release)) {
            Logger.warn("Unable to release ECoS control of locomotive {} after recovery stop", id);
            ok = false;
          }
        }
        if (!ok) {
          return false;
        }
      }
      EcosMessage finalStatus = session.sendMessage(EcosMessageFactory.getPowerStatus());
      if (!isSuccessfulReply(finalStatus)
              || !Ecos.STOP.equals(finalStatus.getValueMap().get(Ecos.STATUS))) {
        Logger.warn("Track power not confirmed OFF after ECoS locomotive reset");
        return false;
      }
      recoveryMode = false;
      Logger.warn("ECoS recovery complete: {} locomotive(s) confirmed at speed zero; track power remains OFF",
              ids.size());
      return true;
    }
  }

  private static boolean isSuccessfulReply(EcosMessage reply) {
    return reply != null && reply.isResponseComplete() && reply.isValid();
  }

  private static String responseDescription(EcosMessage reply) {
    return reply == null ? "<no reply>"
            : reply.isResponseComplete() ? reply.getResponse().strip() : "<incomplete reply>";
  }

  @Override
  public void changeDirection(int locUid, LocomotiveBean.Direction direction) {
    Logger.trace("Changing Direction for {} to {}", locUid, direction);
    
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.getRequestLocomotiveControl(locUid));
    Logger.trace("{} ->\n{}", reply.getMessage(), reply.getResponse());
    
    reply = factory.getConnection().sendMessage(EcosMessageFactory.setLocomotiveSpeed(locUid, 0));
    Logger.trace("{} ->\n{}", reply.getMessage(), reply.getResponse());
    
    reply = factory.getConnection().sendMessage(EcosMessageFactory.setLocomotiveDirection(locUid, direction.getEcosValue()));
    Logger.trace("{} ->\n{}", reply.getMessage(), reply.getResponse());
    
    reply = factory.getConnection().sendMessage(EcosMessageFactory.getReleaseLocomotiveControl(locUid));
    Logger.trace("{} ->\n{}", reply.getMessage(), reply.getResponse());
    
    LocomotiveSpeedEvent vme = new LocomotiveSpeedEvent(locUid, commandStationBean.getId(), 0);
    LocomotiveDirectionEvent dce = new LocomotiveDirectionEvent(locUid, commandStationBean.getId(), direction);
    fireDirectionEventListeners(dce);
    fireLocomotiveSpeedEventListeners(vme);
  }
  
  @Override
  public void changeVelocity(int locUid, int speed, LocomotiveBean.Direction direction) {
    Logger.trace("Changing speed for " + locUid + " to " + speed + " Direction " + direction);
    //Scale the speedstep
    int speedstep = speed / 8;
    
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.getRequestLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
    
    reply = factory.getConnection().sendMessage(EcosMessageFactory.setLocomotiveSpeed(locUid, speedstep));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
    
    reply = factory.getConnection().sendMessage(EcosMessageFactory.getReleaseLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
    
    LocomotiveSpeedEvent vme = new LocomotiveSpeedEvent(locUid, commandStationBean.getId(), speed);

    //TODO: think about threading....
    Logger.trace("Speed changed to: " + speed + " speedstep: " + speedstep + " for loco ID: " + locUid);
    fireLocomotiveSpeedEventListeners(vme);
    
    if (isVirtual()) {
      //When a locomotive has a speed change (>0) check if Auto mode is on.
      //When in Auto mode try to simulate the first sensor the locomotive is suppose to hit.
      //if (AutoPilot.isAutoModeActive() && speed > 0) {
      if (RailController.getInstance().isAutoModeActive() && speed > 0 && simulator != null) {
        simulator.simulateDriving(locUid, speed, direction);
      }
    }
  }
  
  @Override
  public void changeFunctionValue(int locUid, int functionNumber, boolean flag) {
    Logger.trace("Changing Function " + functionNumber + " for " + locUid + " to " + flag);
    
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.getRequestLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
    
    reply = factory.getConnection().sendMessage(EcosMessageFactory.setLocomotiveFunction(locUid, functionNumber, flag));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
    
    reply = factory.getConnection().sendMessage(EcosMessageFactory.getReleaseLocomotiveControl(locUid));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());

    //TODO: think about threading....
    Logger.trace("Function " + functionNumber + " changed to: " + (flag ? "On" : "Off") + " for loco ID: " + locUid);
    
    LocomotiveFunctionEvent lfe = new LocomotiveFunctionEvent(locUid, commandStationBean.getId(), functionNumber, flag);
    fireFunctionEventListeners(lfe);
  }
  
  @Override
  public List<LocomotiveBean> getLocomotives() {
    LocomotiveManager manager = locomotiveManager;
    return manager == null ? new ArrayList<>() : new ArrayList<>(manager.getLocomotives().values());
  }
  
  @Override
  public Image getLocomotiveImage(String icon) {
    if (icon.startsWith("LOCO_TYPE")) {
      String[] urlParts = icon.split(","); //locodesc[LOCO_TYPE_E,IMAGE_TYPE_USER,2]
      if (urlParts.length == 3) {
        EcosHTTPConnection httpCon = factory.getHttpConnection();
        String type;
        if ("IMAGE_TYPE_INT".equals(urlParts[1])) {
          type = "internal";
        } else {
          type = "external";
        }
        String index = urlParts[2].toLowerCase();
        Image locImage = httpCon.getLocomotiveImage(type, index);
        return locImage;
      } else {
        Logger.trace("Unknown Ecos path: " + icon);
        return null;
      }
    } else {
      Logger.trace("Image already processed: " + icon);
      return null;
    }
  }
  
  @Override
  public Image getLocomotiveFunctionImage(String icon) {
    return null;
  }
  
  @Override
  public boolean isSupportTrackMeasurements() {
    BoosterManager manager = boosterManager;
    return manager != null && manager.isSupportMeasurements();
  }
  
  @Override
  public void addMeasurementEventListener(MeasurementEventListener listener) {
    this.boosterManager.addMeasurementEventListener(listener);
  }
  
  @Override
  public void removeMeasurementEventListener(MeasurementEventListener listener) {
    this.boosterManager.removeMeasurementEventListener(listener);
  }
  
  @Override
  public void switchAccessory(Integer address, String protocol, AccessoryBean.AccessoryValue value, Integer switchTime) {
    Logger.trace("Using Address: {} and Protocol: {} to find the AccessoryId...", address, protocol);
    String id = accessoryManager.findId(address, protocol);
    if (id != null) {
      switchAccessory(id, value);
    } else {
      Logger.warn("Accessory with address " + address + " does not exist for the Ecos");
    }
  }
  
  void switchAccessory(String id, AccessoryBean.AccessoryValue value) {
    Logger.trace("Changing Accessory " + id + " to " + value);
    
    AccessoryBean accessory;
    Integer switchTime = null;
    if (accessoryManager.getAccessories().containsKey(id)) {
      accessory = accessoryManager.getAccessories().get(id);
      switchTime = accessory.getSwitchTime();
    } else {
      accessory = new AccessoryBean();
      accessory.setId(id);
      accessory.setCommandStationId(commandStationBean.getId());
      accessory.setSwitchTime(defaultSwitchTime);
    }
    if (switchTime == null) {
      switchTime = this.defaultSwitchTime;
    }

    //State depend on type of accessory
    int state;
    switch (value) {
      case GREEN ->
        state = 0;
      case RED -> {
        if (accessory.is3WaySwitch()) {
          state = 2;
        } else {
          state = 1;
        }
      }
      case RED2 -> {
        if (accessory.is3WaySwitch()) {
          state = 1;
        } else {
          state = 2;
        }
      }
      case WHITE ->
        state = 2;
      case YELLOW ->
        state = 3;
      default ->
        state = 0;
    }
    
    EcosMessage reply = factory.getConnection().sendMessage(EcosMessageFactory.setAccessory(id, state, switchTime));
    Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
    
    accessory.setAccessoryValue(value);
    AccessoryEvent ae = new AccessoryEvent(accessory);
    fireAccessoryEventListeners(ae);
  }
  
  @Override
  public List<AccessoryBean> getAccessories() {
    AccessoryManager manager = accessoryManager;
    return manager == null ? new ArrayList<>() : new ArrayList<>(manager.getAccessories().values());
  }
  
  @Override
  public List<FeedbackModule> getFeedbackModules() {
    FeedbackManager manager = feedbackManager;
    return manager == null ? new ArrayList<>() : new ArrayList<>(manager.getModules().values());
  }
  
  @Override
  public void fireAllSensorEventsListeners(SensorEvent sensorEvent) {
    if (allSensorEventsListeners != null && !allSensorEventsListeners.isEmpty()) {
      Logger.trace("SensorEvent: " + sensorEvent + " Firing " + allSensorEventsListeners.size() + " listeners");
      List<AllSensorEventsListener> snapshot = new ArrayList<>(allSensorEventsListeners);
      for (AllSensorEventsListener listener : snapshot) {
        listener.onSensorChange(sensorEvent);
      }
    }
  }
  
  @Override
  public void simulateSensor(SensorEvent sensorEvent) {
    if (factory.getConnection() instanceof VirtualConnection virtualConnection) {
      virtualConnection.sendEvent(sensorEvent);
    }
  }
  
  void fireDirectionEventListeners(final LocomotiveDirectionEvent directionEvent) {
    for (LocomotiveDirectionEventListener listener : this.locomotiveDirectionEventListeners) {
      listener.onDirectionChange(directionEvent);
    }
  }
  
  void fireLocomotiveSpeedEventListeners(final LocomotiveSpeedEvent speedEvent) {
    for (LocomotiveSpeedEventListener listener : this.locomotiveSpeedEventListeners) {
      listener.onSpeedChange(speedEvent);
    }
  }
  
  void fireFunctionEventListeners(final LocomotiveFunctionEvent functionEvent) {
    for (LocomotiveFunctionEventListener listener : this.locomotiveFunctionEventListeners) {
      listener.onFunctionChange(functionEvent);
    }
  }
  
  void fireAccessoryEventListeners(final AccessoryEvent accessoryEvent) {
    if (accessoryEventListeners != null && !accessoryEventListeners.isEmpty()) {
      for (AccessoryEventListener listener : accessoryEventListeners) {
        listener.onAccessoryChange(accessoryEvent);
      }
    }
  }
  
  void firePowerEventListeners(final PowerEvent powerEvent) {
    Logger.trace("Notify " + powerEventListeners.size() + " Power is " + (powerEvent.isPower() ? "On" : "Off"));
    for (PowerEventListener listener : powerEventListeners) {
      listener.onPowerChange(powerEvent);
    }
  }

  //Communication from Ecos reply messages to JCS
  private class EventHandler extends Thread {
    
    private volatile boolean running = true;
    private final BlockingQueue<EcosMessage> eventQueue;
    
    public EventHandler(EcosConnection connection) {
      super("ECoS-EVENT-HANDLER");
      setDaemon(true);
      //eventQueue = connection.getEventQueue();
      eventQueue = connection.getEventQueue();
    }
    
    void quit() {
      running = false;
    }
    
    @Override
    public void run() {
      
      Logger.trace("Event Handler Started...");
      
      while (running) {
        try {
          EcosMessage eventMessage = eventQueue.poll(100, TimeUnit.MILLISECONDS);
          if (eventMessage != null) {
            //Logger.trace("# " + (eventMessage.isEvent() ? "-> " + eventMessage.getResponse() : eventMessage.getMessage() + " -> " + eventMessage.getResponse()));

            int id = eventMessage.getObjectId();
            switch (id) {
              case 1 -> {
                ecosManager.update(eventMessage);
              }
              case 10 -> {
                //Locomotive list changed
                locomotiveManager.update(eventMessage);
              }
              case 11 -> {
                //Accessory Manager change
                accessoryManager.updateManager(eventMessage);
              }
              case 27 -> {
                //Booster manager change
                boosterManager.update(eventMessage);
              }
              default -> {
                //Events for individual devices like accessory, locomotive, sensor etc.
                if (id >= 100 && id <= 1000) {
                  //Feedback event
                  feedbackManager.update(eventMessage);
                } else if (id >= 1000 && id <= 9999) {
                  locomotiveManager.update(eventMessage);
                } else if (id >= 20000 && id <= 29999) {
                  accessoryManager.update(eventMessage);
                } else if (id >= 65000 && id <= 65999) {
                  boosterManager.update(eventMessage);
                } else {
                  Logger.trace(eventMessage.getMessage() + " " + eventMessage.getResponse());
                }
              }
            }
          }
        } catch (InterruptedException ex) {
          // Normal shutdown; do not busy-spin on an interrupted poll.
          break;
        }
      }
      
      Logger.debug("Stop receiving");
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////////////
  /// @param a 
  // For testing only
  public static void main(String[] a) {
    
    System.setProperty("message.debug", "true");
    //Discover the ECoS using mdns
    //InetAddress ecosAddr = EcosConnectionFactory.discoverEcos();
    //String ip = ecosAddr.getHostAddress();

    if (1 == 1) {
      CommandStationBean csb = new CommandStationBean();
      csb.setId("esu-ecos");
      csb.setDescription("ESU-ECOS");
      csb.setClassName("jcs.commandStation.esu.ecos.EsuEcosCommandStationImpl");
      csb.setConnectVia("NETWORK");
      //csb.setIpAddress("192.168.1.110");
      //csb.setIpAddress(ip);
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

//        Logger.trace(cs.ecosManager);
//        boolean power = cs.isPower();
//        Logger.trace("1 Power is " + (power ? "On" : "Off"));
//
//        cs.pause(1000);
//
//        power = cs.power(true);
//        Logger.trace("2 Power is " + (power ? "On" : "Off"));
//
//        cs.pause(1000);
//
//        power = cs.power(false);
//        Logger.trace("3 Power is " + (power ? "On" : "Off"));
//
//        cs.pause(1000);
//
//        List<FeedbackModule> feedbackModules = cs.getFeedbackModules();
//        Logger.trace("There are " + feedbackModules + " Feedback Modules");
//        for (FeedbackModule fm : feedbackModules) {
//          Logger.trace("Module id: " + fm.getId() + " Module nr: " + fm.getModuleNumber() + " ports: " + fm.getPortCount() + " NodeId: " + fm.getIdentifier() + " BusNr: " + fm.getBusNumber());
//          Logger.trace("FBModule id: " + fm.getId() + " S 1 id:" + fm.getSensor(0).getId() + " contactId: " + fm.getSensor(0).getContactId() + " ModuleNr: " + fm.getSensor(0).getDeviceId() + " Name " + fm.getSensor(0).getName());
//          Logger.trace("FBModule id: " + fm.getId() + " S 15 id:" + fm.getSensor(15).getId() + " contactId: " + fm.getSensor(15).getContactId() + " ModuleNr: " + fm.getSensor(15).getDeviceId() + " Name " + fm.getSensor(15).getName());
//        }
//        power = cs.power(true);
//        Logger.trace("4 Power is " + (power ? "On" : "Off"));
        //EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(26)"));
        //Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Map<String, Object> values = reply.getValueMap();
//        for (String key : values.keySet()) {
//          Logger.trace("Key: " + key+ " Value: "+values.get(key));
//        }
//        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(10, name, addr, protocol)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Map<String, Object> values = reply.getValueMap();
//        for (String key : values.keySet()) {
//        Logger.trace("Key: " + key + " Value: " + values.get(key));
//        }
//        reply = cs.connection.sendMessage(new EcosMessage("queryObjects(10, name)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        
//        values = reply.getValueMap();
//        for (String key : values.keySet()) {
//          Logger.trace("Key: " + key+ " Value: "+values.get(key));
//        }
//        reply = cs.connection.sendMessage(new EcosMessage("queryObjects(10)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        values = reply.getValueMap();
//        for (String key : values.keySet()) {
//          Logger.trace("ID:" + key);
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(10, "+ key+")"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        }
//        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("request(1005, control, force)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("set(1005, speed[100])"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        cs.pause(2000);
//
//        reply = cs.connection.sendMessage(new EcosMessage("set(1005, speed[10])"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("release(1005, control)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(11, name1,name2,name3, addr, protocol,mode,symbol)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Map<String, Object> values = reply.getValueMap();
//        for (String key : values.keySet()) {
//        Logger.trace("Key: " + key + " Value: " + values.get(key));
//        }
//        reply = cs.connection.sendMessage(new EcosMessage("get(20000, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20001, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20002, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20003, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20004, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(20005, name1,name2,name3, addr, protocol,mode,symbol,state,addrext,duration,gates,variant,position,switching)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        EcosMessage reply = cs.connection.sendMessage(new EcosMessage("queryObjects(27,size)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//        reply = cs.connection.sendMessage(new EcosMessage("get(65000, maxlimit,limit)"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//
//         //reply = cs.connection.sendMessage(new EcosMessage("help(65000,attribute)"));
//        //Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        
//        
//        reply = cs.connection.sendMessage(new EcosMessage("request(65000,volt"));
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
//        Logger.trace(reply.getMessage() + " ->\n" + reply.getResponse());
        //reply = cs.connection.sendMessage(new EcosMessage("get(1002, name, addr, protocol, locodesc, dir,speed, speedstep, speedindicator,func)"));      
        //Logger.trace(reply.getMessage()+" ->\n"+reply.getResponse());
        //reply = cs.connection.sendMessage(new EcosMessage("request(10, view)"));      
        //Logger.trace(reply.getMessage()+" ->\n"+reply.getResponse());
        //reply = cs.connection.sendMessage(new EcosMessage("request(1002, view)"));      
        //Logger.trace(reply.getMessage()+" ->\n"+reply.getResponse());
        //reply = cs.connection.sendMessage(new EcosMessage("queryObjects(11, name1,name2,name3, addr, protocol, objectclass)"));       
        //Logger.trace(reply.getMessage()+" ->\n"+reply.getResponse());
        cs.pause(100000);
        //cs.connection.sendMessage(EcosMessageFactory.unSubscribeBaseObject());
        //cs.connection.sendMessage(EcosMessageFactory.unSubscribeFeedbackManager());
        cs.disconnect();
      }
      System.exit(0);
      
    }
    
  }
  
}
