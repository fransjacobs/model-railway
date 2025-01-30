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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import static jcs.entities.BlockBean.BlockState.LOCKED;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.TileCache;
import jcs.ui.layout.events.TileEvent;
import org.tinylog.Logger;

/**
 *
 * @author frans
 *
 */
public final class AutoPilot {

  private static AutoPilotThread autoPilotThread = null;

  private static CommandStationBean commandStationBean;

  //private final Map<String, SensorEventHandler> sensorHandlers = Collections.synchronizedMap(new HashMap<>());
  private static final Map<String, SensorEventHandler> sensorHandlers = new HashMap<>();
  //private final Map<String, Dispatcher> dispatchers = Collections.synchronizedMap(new HashMap<>());
  private static final Map<String, Dispatcher> dispatchers = new HashMap<>();

  //Need a list to be able to unregister
  private static final List<AutoPilotStatusListener> autoPilotStatusListeners = Collections.synchronizedList(new ArrayList<>());

  private static final Semaphore semaphore = new Semaphore(1);
  private static final ExecutorService executor = Executors.newCachedThreadPool();

  private static final ThreadGroup autoPilotRunners = new ThreadGroup("AUTOPILOT");

  private AutoPilot() {
  }

  public static void runAutoPilot(boolean flag) {
    if (flag) {
      executor.execute(() -> startAutoMode());
    } else {
      executor.execute(() -> stopAutoMode());
    }
  }

  public synchronized static void startAutoMode() {
    if (JCS.getJcsCommandStation().isPowerOn()) {
      if (autoPilotThread != null && autoPilotThread.isRunning()) {
        Logger.trace("Allready running");
      } else {
        commandStationBean = JCS.getJcsCommandStation().getCommandStationBean();
        dispatchers.clear();
        sensorHandlers.clear();

        autoPilotThread = new AutoPilotThread(autoPilotRunners);
        autoPilotThread.start();
      }
    } else {
      Logger.warn("Can't start Automode is Power is Off!");
    }
  }

  public static void stopAutoMode() {
    if (autoPilotThread != null) {
      autoPilotThread.stopAutoMode();

      //Notify all dispachers so thath the ones which waiting and Idle will stop
      Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
      for (Dispatcher d : snapshot) {
        d.stopLocomotiveAutomode();
        if (!d.isRunning()) {
          d.stopRunning();
        }
      }

      try {
        autoPilotThread.join();
      } catch (InterruptedException ex) {
        Logger.error("Interruppted during join " + ex);
      }
    }
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

  public static boolean areDispatchersRunning() {
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

  public static synchronized Dispatcher createDispatcher(LocomotiveBean locomotiveBean) {
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
        Logger.trace("Reused dispatcher for " + loc.getName() + "...");
      } else {
        createDispatcher(loc);
      }
    }
  }

  public static synchronized void clearDispatchers() {
    Logger.trace("Remove all Dispatchers...");

    for (Dispatcher dispatcher : dispatchers.values()) {
      dispatcher.stopLocomotiveAutomode();
    }

    dispatchers.clear();
  }

  public static void startStopLocomotive(LocomotiveBean locomotiveBean, boolean start) {
    Logger.trace((start ? "Starting" : "Stopping") + " auto drive for " + locomotiveBean.getName());
    String key = locomotiveBean.getName();

    if (start) {
      Dispatcher dispatcher;
      if (dispatchers.containsKey(key)) {
        dispatcher = dispatchers.get(key);
        Logger.trace("Dispatcher " + key + " exists");
      } else {
        dispatcher = createDispatcher(locomotiveBean);
        Logger.trace("Dispatcher " + key + " created");
      }

      if (!dispatcher.isRunning()) {
        Logger.trace("Starting dispatcher thread" + key);
        dispatcher.startLocomotiveAutomode();
      }

      Logger.trace("Started dispatcher" + key + " automode...");
    } else {
      Dispatcher dispatcher = dispatchers.get(key);
      if (dispatcher != null && dispatcher.isRunning()) {
        dispatcher.stopLocomotiveAutomode();
        Logger.trace("Stopped dispatcher" + key + " automode...");
      }
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

  private static void startStopAllLocomotivesInBackground(boolean start) {
    List<LocomotiveBean> onTrackLocos = getOnTrackLocomotives();
    Logger.trace((start ? "Starting" : "Stopping") + " automode for " + onTrackLocos.size() + " ontrack locomotives...");

    for (LocomotiveBean locomotiveBean : onTrackLocos) {
      startStopLocomotive(locomotiveBean, start);
    }
  }

  public static void startAllLocomotives() {
    executor.execute(() -> startStopAllLocomotivesInBackground(true));
  }

  public static void stopAllLocomotives() {
    executor.execute(() -> startStopAllLocomotivesInBackground(false));
  }

  public static void resetStates() {
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
      if (block.getLocomotiveId() != null) {
        if (null == block.getBlockState()) {
          if (BlockBean.BlockState.OCCUPIED == block.getBlockState()) {
            occupiedBlockCounter++;
          }
        } else {
          switch (block.getBlockState()) {
            case LOCKED, INBOUND -> {
              //destinations block, reset!
              block.setLocomotive(null);
              block.setBlockState(BlockBean.BlockState.FREE);
              block.setArrivalSuffix(null);
              freeBlockCounter++;
            }
            case OUTBOUND -> {
              block.setBlockState(BlockBean.BlockState.OCCUPIED);
              block.setArrivalSuffix(null);
              occupiedBlockCounter++;
            }
            default -> {
              if (BlockBean.BlockState.OCCUPIED == block.getBlockState()) {
                block.setArrivalSuffix(null);
                occupiedBlockCounter++;
              }
            }
          }
        }
      } else {
        block.setBlockState(BlockBean.BlockState.FREE);
        freeBlockCounter++;
      }
      PersistenceFactory.getService().persist(block);
      TileEvent tileEvent = new TileEvent(block);
      //TileCache.fireTileEventListener(tileEvent);
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
    Set<LocomotiveBean> activeLocomotives = new HashSet<>();
    for (BlockBean occupiedBlock : occupiedBlocks) {
      LocomotiveBean dbl = PersistenceFactory.getService().getLocomotive(occupiedBlock.getLocomotiveId());
      if (dbl != null) {
        activeLocomotives.add(dbl);
      }
    }

    //if (Logger.isDebugEnabled()) {
    //  Logger.trace("There are " + activeLocomotives.size() + " Locomotives on the track: ");
    for (LocomotiveBean loc : activeLocomotives) {
      Logger.trace(loc);
    }
    //}
    return new ArrayList<>(activeLocomotives);
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
    Logger.trace("Check for possible Ghost! @ Sensor " + event.getId());
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    String sensorId = event.getId();
    for (BlockBean block : blocks) {
      if ((block.getMinSensorId().equals(sensorId) || block.getPlusSensorId().equals(sensorId)) && block.getLocomotiveId() == null) {
        if (event.getSensorBean().isActive()) {
          block.setBlockState(BlockBean.BlockState.GHOST);
          //Also persist
          PersistenceFactory.getService().persist(block);

          Logger.warn("Ghost Detected! @ Sensor " + sensorId + " in block " + block.getId());
          //Switch power OFF!
          JCS.getJcsCommandStation().switchPower(false);

          TileEvent tileEvent = new TileEvent(block);
          //TileCache.fireTileEventListener(tileEvent);
        } else {
          if (block.getLocomotiveId() != null) {
            //keep state as is
          } else {
            block.setBlockState(BlockBean.BlockState.FREE);
          }
        }
        break;
      }
    }

  }

  static void handleSensorEvent(SensorEvent event) {
    if (event.isChanged()) {
      SensorEventHandler sh = sensorHandlers.get(event.getId());
      Boolean registered = sh != null;  //sensorHandlers.containsKey(event.getId());
      Logger.trace((registered ? "Registered " : "") + event.getId() + " has changed " + event.isChanged());

      if (sh != null) {
        //there is a handler registered for this id, pass the event through
        //SensorEventHandler sh = sensorHandlers.get(event.getId());
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

  public static boolean isSensorHandlerRegistered(String sensorId) {
    return sensorHandlers.containsKey(sensorId);
  }

  public static synchronized void removeHandler(String sensorId) {
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

  private static class AutoPilotThread extends Thread {

    private final List<SensorListener> sensorListeners = new ArrayList<>();

    private boolean running = false;
    private boolean stopped = false;

    AutoPilotThread(ThreadGroup parent) {
      super(parent, "AUTO_MAIN");
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
        String key = sb.getId();
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

      Logger.trace("Autopilot Started. Notify " + autoPilotStatusListeners.size() + " Listeners...");

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
      boolean dispatchersRunning = areDispatchersRunning();

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
        dispatchersRunning = areDispatchersRunning();
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

    private final String sensorId;

    SensorListener(String sensorId) {
      this.sensorId = sensorId;
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      if (sensorId.equals(event.getId())) {
        AutoPilot.handleSensorEvent(event);
      }
    }
  }
}
