/*
 * Copyright 2023 frans.
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
package jcs.commandStation.autopilot;

import jcs.commandStation.autopilot.state.Dispatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import static jcs.entities.BlockBean.BlockState.LOCKED;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * The AutoPilot is the "automatic driving engine".<br>
 * Every Locomotive on the track will start it own Thread.<br>
 * The Dispatcher is run in this Thread.<br>
 * The AutoPilot has it own Monitor Thread: AutoPilotMonitorThread.
 *
 */
public final class AutoPilot {

  private static AutoPilotMonitorThread autoPilotThread = null;

  private static CommandStationBean commandStationBean;

  private static final Map<Integer, SensorEventHandler> sensorHandlers = new HashMap<>();
  private static final Map<String, Dispatcher> dispatchers = new HashMap<>();

  //Need a list to be able to unregister
  private static final List<AutoPilotStatusListener> autoPilotStatusListeners = Collections.synchronizedList(new ArrayList<>());

  private static final Semaphore semaphore = new Semaphore(1);

  private static final ThreadGroup autoPilotRunners = new ThreadGroup("AUTOPILOT");

  private static final ConcurrentLinkedQueue<AutoPilotActionEvent> actionCommandQueue = new ConcurrentLinkedQueue();

  private static final ActionCommandHandler actionCommandHandler = new ActionCommandHandler(actionCommandQueue);

  static {
    actionCommandHandler.start();
  }

  private AutoPilot() {
  }

  public static void runAutoPilot(boolean flag) {
    if (flag) {
      enqueCommand(new AutoPilotActionEvent("start"));
    } else {
      enqueCommand(new AutoPilotActionEvent("stop"));
    }
  }

  public static void startLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new AutoPilotActionEvent("startLocomotive", locomotiveBean));
  }

  public static void stopLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new AutoPilotActionEvent("stopLocomotive", locomotiveBean));
  }

  public static void removeLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new AutoPilotActionEvent("removeLocomotive", locomotiveBean));
  }

  public static void addLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new AutoPilotActionEvent("addLocomotive", locomotiveBean));
  }

  public static void startAllLocomotives() {
    enqueCommand(new AutoPilotActionEvent("startAllLocomotives"));
  }

  public static void reset() {
    enqueCommand(new AutoPilotActionEvent("reset"));
  }

  private static void enqueCommand(AutoPilotActionEvent command) {
    actionCommandQueue.offer(command);
    synchronized (AutoPilot.actionCommandHandler) {
      actionCommandHandler.notifyAll();
    }
  }

  static boolean startAutoMode() {
    if (JCS.getJcsCommandStation().isPowerOn()) {
      if (autoPilotThread != null && autoPilotThread.isRunning()) {
        Logger.trace("Allready running");
        return true;
      } else {
        commandStationBean = JCS.getJcsCommandStation().getCommandStationBean();
        dispatchers.clear();
        sensorHandlers.clear();

        autoPilotThread = new AutoPilotMonitorThread(autoPilotRunners);
        autoPilotThread.start();
        Logger.debug("AutoMode Started");
        return true;
      }
    } else {
      Logger.warn("Can't start Automode, Command Station Power is Off!");
      return false;
    }
  }

  static void stopAutoMode() {
    if (autoPilotThread != null) {
      //Notify all dispachers so that the ones which waiting and Idle will stop
      Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
      for (Dispatcher d : snapshot) {
        d.stopLocomotiveAutomode();
        if (!d.isRunning()) {
          d.stopRunning();
        }
      }

      autoPilotThread.stopAutoMode();

      try {
        autoPilotThread.join();
      } catch (InterruptedException ex) {
        Logger.error("Interruppted during join " + ex);
      }
    }
    Logger.debug("AutoMode Stopped");

  }

  public static boolean isAutoModeActive() {
    if (autoPilotThread != null) {
      return autoPilotThread.isRunning();
    } else {
      return false;
    }
  }

  public static boolean isAutoPilotThreadStopped() {
    if (autoPilotThread != null) {
      return autoPilotThread.isStopped();
    } else {
      return true;
    }
  }

  public static boolean isRunning(LocomotiveBean locomotive) {
    if (isAutoModeActive() && dispatchers.containsKey(locomotive.getName())) {
      Dispatcher dispatcher = dispatchers.get(locomotive.getName());
      return dispatcher.isRunning();
    } else {
      return false;
    }
  }

  public static boolean isADispatcherRunning() {
    boolean isRunning = false;
    Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
    for (Dispatcher ld : snapshot) {
      isRunning = ld.isRunning();
      if (isRunning) {
        return isRunning;
      }
    }
    return isRunning;
  }

  public static int getRunningDispatcherCount() {
    Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
    int runningDispatchers = 0;
    for (Dispatcher ld : snapshot) {
      if (ld.isRunning()) {
        runningDispatchers++;
      }
    }
    return runningDispatchers;
  }

  static Dispatcher createDispatcher(LocomotiveBean locomotiveBean) {
    Dispatcher dispatcher = null;
    //check if the locomotive is on track
    if (isOnTrack(locomotiveBean)) {
      if (dispatchers.containsKey(locomotiveBean.getName())) {
        dispatcher = dispatchers.get(locomotiveBean.getName());
        Logger.trace("Reuse dispatcher for " + locomotiveBean.getName() + "...");
      } else {
        dispatcher = new Dispatcher(autoPilotRunners, locomotiveBean);

        //Also add it to the dispatchers
        dispatchers.put(locomotiveBean.getName(), dispatcher);
        Logger.trace("Added new dispatcher for " + locomotiveBean.getName() + "...");
      }
    }
    return dispatcher;
  }

  static void removeDispatcher(LocomotiveBean locomotiveBean) {
    if (dispatchers.containsKey(locomotiveBean.getName())) {
      Dispatcher dispatcher = dispatchers.remove(locomotiveBean.getName());
      Logger.trace("removing Dispatcher for locomotive " + locomotiveBean.getName());
      if (dispatcher.isRunning()) {
        Logger.trace("Stopping Automode for " + locomotiveBean.getName() + "...");
        dispatcher.stopLocomotiveAutomode();
        dispatcher.stopRunning();
      }

      dispatcher.removeAllStateEventListeners();

      for (AutoPilotStatusListener asl : autoPilotStatusListeners) {
        asl.statusChanged(autoPilotThread.running);
      }
    }
  }

  static void addDispatcher(LocomotiveBean locomotiveBean) {
    createDispatcher(locomotiveBean);

    for (AutoPilotStatusListener asl : autoPilotStatusListeners) {
      asl.statusChanged(autoPilotThread.running);
    }
  }

  public static void prepareAllDispatchers() {
    Logger.trace("Preparing Dispatchers for all on track locomotives...");
    List<LocomotiveBean> locs = getOnTrackLocomotives();

    Map<String, Dispatcher> snapshot = new HashMap<>(dispatchers);
    dispatchers.clear();

    for (LocomotiveBean loc : locs) {
      Dispatcher dispatcher;
      if (snapshot.containsKey(loc.getName())) {
        dispatcher = snapshot.get(loc.getName());
        dispatchers.put(loc.getName(), dispatcher);
        Logger.trace("Re use dispatcher " + loc.getName() + "...");
      } else {
        createDispatcher(loc);
      }
    }
  }

  public static synchronized void clearDispatchers() {
    Logger.trace("Removing all Dispatchers...");

    for (Dispatcher dispatcher : dispatchers.values()) {
      dispatcher.stopLocomotiveAutomode();
    }

    dispatchers.clear();
  }

  static void startDispatcher(LocomotiveBean locomotiveBean) {
    Logger.trace("Starting locomotive for " + locomotiveBean.getName());
    String key = locomotiveBean.getName();

    Dispatcher dispatcher;
    if (dispatchers.containsKey(key)) {
      dispatcher = dispatchers.get(key);
      //Logger.trace("Dispatcher " + key + " exists");
    } else {
      dispatcher = createDispatcher(locomotiveBean);
      Logger.trace("Dispatcher " + key + " created");
    }

    if (!dispatcher.isRunning()) {
      Logger.trace("Starting dispatcher thread " + key);
      dispatcher.startLocomotiveAutomode();
    }

    Logger.trace("Started locomotive " + key + "...");
  }

  static void stopDispatcher(LocomotiveBean locomotiveBean) {
    Logger.trace("Stopping locomotive " + locomotiveBean.getName());
    String key = locomotiveBean.getName();

    Dispatcher dispatcher = dispatchers.get(key);
    if (dispatcher != null && dispatcher.isRunning()) {
      dispatcher.stopLocomotiveAutomode();
      Logger.trace("Stopped locomotive " + key + "...");
    }
  }

  static void startLocomotives() {
    List<LocomotiveBean> locos = getOnTrackLocomotives();
    for (LocomotiveBean loc : locos) {
      startDispatcher(loc);
    }
  }

  public static synchronized void resetDispatcher(LocomotiveBean locomotiveBean) {
    Logger.trace("Resetting dispatcher for " + locomotiveBean.getName());
    Dispatcher dispatcher;
    String key = locomotiveBean.getName();
    if (dispatchers.containsKey(key)) {
      dispatcher = dispatchers.get(key);
    } else {
      //TODO is this really needed !
      dispatcher = new Dispatcher(autoPilotRunners, locomotiveBean);
      dispatchers.put(key, dispatcher);
      Logger.trace("Created a new dispatcher " + key + "...");
    }

    dispatcher.reset();
  }

  static void resetStates() {
    Logger.trace("Resetting AutoPilot...");

    stopAutoMode();

    List<RouteBean> routes = PersistenceFactory.getService().getRoutes();
    int lockedCounter = 0;
    for (RouteBean route : routes) {
      if (route.isLocked()) {
        route.setLocked(false);
        PersistenceFactory.getService().persist(route);
        lockedCounter++;
      }
      Dispatcher.resetRoute(route);
    }
    Logger.debug("Unlocked " + lockedCounter + " routes out of " + routes.size());

    // Reset route
    int occupiedBlockCounter = 0;
    int freeBlockCounter = 0;
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    for (BlockBean block : blocks) {
      Tile tile = TileCache.findTile(block.getTileId());
      if (block.getLocomotiveId() != null) {
        if (block.getBlockState() != null) {
          if (BlockState.OCCUPIED == block.getBlockState()) {
            occupiedBlockCounter++;
          }
        } else {
          switch (block.getBlockState()) {
            case LOCKED, INBOUND -> {
              //reserved block, reset!
              tile.setLocomotive(null);
              tile.setBlockState(BlockState.FREE);
              tile.setArrivalSuffix(null);
              freeBlockCounter++;
            }
            case OUTBOUND -> {
              //
              tile.setBlockState(BlockState.OCCUPIED);
              tile.setArrivalSuffix(null);
              occupiedBlockCounter++;
            }
            case OUT_OF_ORDER -> {
              //Keep as is
            }
            default -> {
              if (BlockState.OCCUPIED == block.getBlockState()) {
                //TODO...
                tile.setArrivalSuffix(null);
                //arrival suffix should be set to the default also...

                occupiedBlockCounter++;
              }
            }
          }
        }
      } else {
        BlockState blockState = block.getBlockState();
        if (!BlockState.OUT_OF_ORDER.equals(blockState)) {
          tile.setBlockState(BlockBean.BlockState.FREE);
          tile.setArrivalSuffix(null);
          freeBlockCounter++;
        } else {
          if(!blockState.equals(tile.getBlockState())) {
            tile.setBlockBean(block);
          }
        }
      }
      PersistenceFactory.getService().persist(tile.getBlockBean());
    }

    JCS.getJcsCommandStation().switchPower(true);
    Logger.debug("Occupied blocks: " + occupiedBlockCounter + " Free blocks " + freeBlockCounter + " of total " + blocks.size() + " blocks");
  }

  public static synchronized List<Dispatcher> getLocomotiveDispatchers() {
    return new ArrayList<>(dispatchers.values());
  }

  public static synchronized Dispatcher getLocomotiveDispatcher(LocomotiveBean locomotiveBean) {
    String key = locomotiveBean.getName();
    return dispatchers.get(key);
  }

  public static Dispatcher getLocomotiveDispatcher(int locUid) {
    LocomotiveBean locomotiveBean = PersistenceFactory.getService().getLocomotive(locUid, commandStationBean.getId());
    return getLocomotiveDispatcher(locomotiveBean);
  }

  public static boolean isOnTrack(LocomotiveBean locomotiveBean) {
    List<LocomotiveBean> onTrackLocomotives = getOnTrackLocomotives();
    for (LocomotiveBean locomotive : onTrackLocomotives) {
      if (locomotiveBean.getId().equals(locomotive.getId())) {
        return true;
      }
    }
    return false;
  }

  public static List<LocomotiveBean> getOnTrackLocomotives() {
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    //filter..
    List<BlockBean> occupiedBlocks = blocks.stream().filter(t -> t.getLocomotive() != null && t.getLocomotive().getId() != null).collect(Collectors.toList());

    //Logger.trace("There " + (occupiedBlocks.size() == 1 ? "is" : "are") + " " + occupiedBlocks.size() + " occupied block(s)");
    //Set<LocomotiveBean> activeLocomotives = new HashSet<>();
    ArrayList<LocomotiveBean> activeLocomotives = new ArrayList<>();
    for (BlockBean occupiedBlock : occupiedBlocks) {
      LocomotiveBean dbl = PersistenceFactory.getService().getLocomotive(occupiedBlock.getLocomotiveId());
      if (dbl != null) {
        if (activeLocomotives.contains(dbl)) {
          Logger.warn("Loc " + dbl.getName() + " Is allready in the list! ");
        } else {
          activeLocomotives.add(dbl);
        }
      }
    }

    //if (Logger.isTraceEnabled()) {
    //Logger.trace("There are " + activeLocomotives.size() + " Locomotives on the track: ");
    //for (LocomotiveBean loc : activeLocomotives) {
    //  Logger.trace(loc);
    //}
    //}
    return activeLocomotives; //new ArrayList<>(activeLocomotives);
  }

  public static boolean isGostDetected() {
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    for (BlockBean bb : blocks) {
      if (BlockBean.BlockState.GHOST == bb.getBlockState()) {
        return true;
      }
    }
    return false;
  }

  private static void handleGhost(SensorEvent event) {
    Logger.trace("Check for possible Ghost! @ Sensor " + event.getSensorId());
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    Integer sensorId = event.getSensorId();
    for (BlockBean block : blocks) {
      Tile tile = TileCache.findTile(block.getTileId());

      if ((block.getMinSensorId().equals(sensorId) || block.getPlusSensorId().equals(sensorId)) && block.getLocomotiveId() == null) {
        if (event.getSensorBean().isActive()) {
          block.setBlockState(BlockBean.BlockState.GHOST);
          tile.setBlockState(BlockBean.BlockState.GHOST);
          //Also persist
          PersistenceFactory.getService().persist(block);
          Logger.warn("Ghost Detected! @ Sensor " + sensorId + " in block " + block.getId());
          //Switch power OFF!
          JCS.getJcsCommandStation().switchPower(false);
        } else {
          if (block.getLocomotiveId() != null) {
            //keep state as is
          } else {
            block.setBlockState(BlockBean.BlockState.FREE);
            tile.setBlockState(BlockBean.BlockState.FREE);
          }
        }
        break;
      }
    }
  }

  static void handleSensorEvent(SensorEvent event) {
    if (event.isChanged()) {
      SensorEventHandler sh = sensorHandlers.get(event.getSensorId());
      Boolean registered = sh != null;

      Logger.trace((registered ? "Registered " : "") + event.getSensorId() + " has changed " + event.isChanged());

      if (sh != null) {
        //there is a handler registered for this id, pass the event through
        sh.handleEvent(event);
      } else {
        //sensor is not registered and thus not expected!
        if (event.isActive()) {
          handleGhost(event);
        }
      }
    }
  }

  public static synchronized void addSensorEventHandler(SensorEventHandler handler) {
    sensorHandlers.put(handler.getSensorId(), handler);
  }

  public static boolean isSensorHandlerRegistered(Integer sensorId) {
    return sensorHandlers.containsKey(sensorId);
  }

  public static synchronized void removeHandler(Integer sensorId) {
    sensorHandlers.remove(sensorId);
  }

  public static synchronized void addAutoPilotStatusListener(AutoPilotStatusListener listener) {
    autoPilotStatusListeners.add(listener);
    Logger.trace("Status listeners: " + autoPilotStatusListeners.size());
  }

  public static synchronized void removeAutoPilotStatusListener(AutoPilotStatusListener listener) {
    autoPilotStatusListeners.remove(listener);
    Logger.trace("Status listeners: " + autoPilotStatusListeners.size());
  }

  public static boolean tryAquireLock() {
    return semaphore.tryAcquire();
  }

  public static void releaseLock() {
    semaphore.release();
  }

  public static int avialablePermits() {
    return semaphore.availablePermits();
  }

  private static class AutoPilotMonitorThread extends Thread {

    private final List<SensorListener> sensorListeners = new ArrayList<>();

    private boolean running = false;
    private boolean stopped = false;

    AutoPilotMonitorThread(ThreadGroup parent) {
      super(parent, "AUTOPILOT-MONITOR");
    }

    void stopAutoMode() {
      Logger.trace("Stopping Automode...");
      this.running = false;
      synchronized (this) {
        this.notifyAll();
        //this.interrupt();
      }
    }

    boolean isRunning() {
      return this.running;
    }

    private void registerAllSensors() {
      //Use only assigned sensors, ignore sensors which are not assigned to a Tile
      List<SensorBean> sensors = PersistenceFactory.getService().getAssignedSensors();
      int cnt = 0;
      for (SensorBean sb : sensors) {
        Integer key = sb.getId();
        if (!sensorHandlers.containsKey(key)) {
          SensorListener seh = new SensorListener(key);
          sensorListeners.add(seh);
          cnt++;
          //Register with a command station
          JCS.getJcsCommandStation().addSensorEventListener(seh);
          //Logger.trace("Added handler " + cnt + " for sensor " + key);
        }
      }
      Logger.trace("Registered " + sensors.size() + " sensor event handlers");
    }

    private void unRegisterAllSensors() {
      for (SensorListener seh : this.sensorListeners) {
        JCS.getJcsCommandStation().removeSensorEventListener(seh);
      }
      Logger.trace("Unregistered " + sensorListeners.size() + " sensor event handlers");
      this.sensorListeners.clear();
    }

    @Override
    public void run() {
      running = true;

      registerAllSensors();
      prepareAllDispatchers();

      Logger.trace("Autopilot Started. There are " + dispatchers.size() + " Dispatchers created...");

      for (AutoPilotStatusListener asl : autoPilotStatusListeners) {
        asl.statusChanged(running);
      }

      while (running) {
        try {
          synchronized (this) {
            wait(10000);
          }
        } catch (InterruptedException ex) {
          Logger.trace("Interrupted");
        }
      }

      Logger.trace("Try to finish all dispatchers...");

      long now = System.currentTimeMillis();
      long start = now;
      long timeout = now + 30000;
      //Check if all dispachers are stopped
      boolean dispatchersRunning = isADispatcherRunning();

      Logger.trace("Try to finish all dispatchers. There are " + getRunningDispatcherCount() + " Dispatchers running...");

      if (dispatchersRunning) {
        //Signal the dispatchers
        Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
        for (Dispatcher ld : snapshot) {
          synchronized (ld) {
            ld.stopLocomotiveAutomode();
          }
        }
      }

      while (dispatchersRunning && now < timeout) {
        dispatchersRunning = isADispatcherRunning();
        try {
          synchronized (this) {
            wait(10);
          }
        } catch (InterruptedException ex) {
          Logger.trace("Interrupted during dispatcher running check");
        }
        now = System.currentTimeMillis();
      }

      Logger.trace((dispatchersRunning ? "Not " : "") + "All dispatchers stopped in " + ((now - start) / 1000) + " s. There are " + getRunningDispatcherCount() + " Still running...");

      if (dispatchersRunning) {
        for (Dispatcher ld : dispatchers.values()) {
          if (ld.isRunning()) {
            Logger.trace("Dispatcher: " + ld.getName() + " in State: " + ld.getStateName() + " is still running...");

          }
        }
      }

      unRegisterAllSensors();
      sensorHandlers.clear();

      for (AutoPilotStatusListener asl : autoPilotStatusListeners) {
        asl.statusChanged(running);
      }

      Logger.trace("Autopilot Finished. Notified " + autoPilotStatusListeners.size() + " Listeners. Power is " + (JCS.getJcsCommandStation().isPowerOn() ? "on" : "off"));
      stopped = true;
    }

    boolean isStopped() {
      return this.stopped;
    }
  }

  private static class SensorListener implements SensorEventListener {

    private final Integer sensorId;

    SensorListener(Integer sensorId) {
      this.sensorId = sensorId;
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      if (sensorId.equals(event.getSensorId())) {
        AutoPilot.handleSensorEvent(event);
      }
    }
  }
}
