/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.automation;

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
import static jcs.commandStation.autopilot.AutoPilot.autoPilotRunners;
import jcs.commandStation.autopilot.AutoPilotActionEvent;
import jcs.commandStation.autopilot.AutoPilotStatusListener;
import jcs.commandStation.autopilot.SensorEventHandler;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import static jcs.entities.BlockBean.BlockState.LOCKED;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * The RailwayController is the core engine for automatic driving.<br>
 * Every Locomotive on the layout will get its own (dispatcher) Thread.<br>
 * The Dispatcher is run in this Thread.<br>
 * The RailwayController has it own Monitor Thread: RailwayControllerMonitorThread.<br>
 * This moditorThread takes car of watching for ghost during automatic driving.
 *
 */
public final class RailwayController {

  private static RailwayController instance;

  private final ThreadGroup controllerRunners;
  private CommandStationBean commandStationBean;

  private final ConcurrentLinkedQueue<AutoPilotActionEvent> actionCommandQueue;

  private final ActionCommandExecuter commandExecuter;

  private final Map<String, Dispatcher> dispatchers;
  private final Map<Integer, SensorEventHandler> sensorHandlers;
  private ControllerThread controllerMonitor = null;

  private static final Semaphore semaphore = new Semaphore(1);

  //Need a list to be able to unregister
  final List<AutoPilotStatusListener> autoPilotStatusListeners = Collections.synchronizedList(new ArrayList<>());

  //private final ActionCommandHandler actionCommandHandler = new ActionCommandHandler(actionCommandQueue);
  private boolean stepTest = false;

  private RailwayController() {
    controllerRunners = new ThreadGroup("RAILWAYCONTROLLER");
    dispatchers = new HashMap<>();
    sensorHandlers = new HashMap<>();
    actionCommandQueue = new ConcurrentLinkedQueue();

    commandExecuter = new ActionCommandExecuter(actionCommandQueue);

    stepTest = "true".equals(System.getProperty("state.machine.stepTest", "false"));

    init();
  }

  private void init() {
    if (!stepTest) {
      commandExecuter.start();
    }
  }

  /**
   * There can only be one Railway Controller.<br>
   *
   * @return the instance of the RailwayController
   */
  public synchronized static RailwayController getInstance() {
    if (instance == null) {
      instance = new RailwayController();
    }
    return instance;
  }

  /**
   * Commands are executed by the controllerThread.<br>
   * Commands are passed through via a queue.
   *
   * @param command the command to execute
   */
  private void enqueCommand(AutoPilotActionEvent command) {
    if (!stepTest) {
      actionCommandQueue.offer(command);
      synchronized (commandExecuter) {
        commandExecuter.notifyAll();
      }
    } else {
      Logger.trace("Excute command " + command.getActionCommand() + " inline");
      this.executeCommand(command);
    }
  }

  public boolean startAutoMode() {
    if (JCS.getJcsCommandStation().isPowerOn()) {
      if (controllerMonitor != null && controllerMonitor.isRunning()) {
        Logger.trace("Allready running");
        return true;
      } else {
        commandStationBean = JCS.getJcsCommandStation().getCommandStationBean();
        dispatchers.clear();
        sensorHandlers.clear();
        ///??????????
        controllerMonitor = new ControllerThread(this, this.controllerRunners);

        controllerMonitor.start();
        Logger.debug("Monitor Started");
        return true;
      }
    } else {
      Logger.warn("Can't start Automode, Command Station Power is Off!");
      return false;
    }
  }

  public void stopAutoMode() {
    if (controllerMonitor != null) {
      //Notify all dispachers so that the ones which waiting and Idle will stop
      Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
      for (Dispatcher d : snapshot) {
        d.stopLocomotiveAutomode();
        if (!d.isRunning()) {
          d.stopRunning();
        }
      }

      controllerMonitor.stopAutoMode();

      try {
        controllerMonitor.join();
      } catch (InterruptedException ex) {
        Logger.error("Interrupted during join " + ex);
      }
    }
    Logger.debug("ControllerMonitor Stopped");

  }

//  public void startMonitor() {
//    actionCommandHandler.start();
//  }
  public void enableAutomode(boolean flag) {
    if (flag) {
      enqueCommand(new AutoPilotActionEvent("start"));
    } else {
      enqueCommand(new AutoPilotActionEvent("stop"));
    }
  }

  public void startLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new AutoPilotActionEvent("startLocomotive", locomotiveBean));
  }

  public void stopLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new AutoPilotActionEvent("stopLocomotive", locomotiveBean));
  }

  public void removeLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new AutoPilotActionEvent("removeLocomotive", locomotiveBean));
  }

  public void addLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new AutoPilotActionEvent("addLocomotive", locomotiveBean));
  }

  public synchronized void startAllLocomotives() {
    enqueCommand(new AutoPilotActionEvent("startAllLocomotives"));
  }

  public void reset() {
    enqueCommand(new AutoPilotActionEvent("reset"));
  }

  public boolean isAutoModeActive() {
    if (controllerMonitor != null) {
      return controllerMonitor.isRunning();
    } else {
      return false;
    }
  }

  public boolean isAutoPilotThreadStopped() {
    if (controllerMonitor != null) {
      return controllerMonitor.isStopped();
    } else {
      return true;
    }
  }

  public boolean isRunning(LocomotiveBean locomotive) {
    if (isAutoModeActive() && dispatchers.containsKey(locomotive.getName())) {
      Dispatcher dispatcher = dispatchers.get(locomotive.getName());
      return dispatcher.isRunning();
    } else {
      return false;
    }
  }

  public boolean isADispatcherRunning() {
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

  public int getRunningDispatcherCount() {
    Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
    int runningDispatchers = 0;
    for (Dispatcher ld : snapshot) {
      if (ld.isRunning()) {
        runningDispatchers++;
      }
    }
    return runningDispatchers;
  }

  Dispatcher createDispatcher(LocomotiveBean locomotiveBean) {
    Dispatcher dispatcher = null;
    //check if the locomotive is on track
    if (isOnTrack(locomotiveBean)) {
      synchronized (dispatchers) {
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
    }
    return dispatcher;
  }

  void removeDispatcher(LocomotiveBean locomotiveBean) {
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
        asl.statusChanged(controllerMonitor.isRunning());
      }
    }
  }

  void addDispatcher(LocomotiveBean locomotiveBean) {
    if (controllerMonitor != null) {
      createDispatcher(locomotiveBean);

      for (AutoPilotStatusListener asl : autoPilotStatusListeners) {
        asl.statusChanged(controllerMonitor.isRunning());
      }
    } else {
      Logger.error("autoPilotThread is null!");
    }
  }

  public void prepareAllDispatchers() {
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

  public synchronized void clearDispatchers() {
    Logger.trace("Removing all Dispatchers...");

    for (Dispatcher dispatcher : dispatchers.values()) {
      dispatcher.stopLocomotiveAutomode();
    }

    dispatchers.clear();
  }

  synchronized void startDispatcher(LocomotiveBean locomotiveBean) {
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

  synchronized void stopDispatcher(LocomotiveBean locomotiveBean) {
    Logger.trace("Stopping locomotive " + locomotiveBean.getName());
    String key = locomotiveBean.getName();

    Dispatcher dispatcher = dispatchers.get(key);
    if (dispatcher != null && dispatcher.isRunning()) {
      dispatcher.stopLocomotiveAutomode();
      Logger.trace("Stopped locomotive " + key + "...");
    }
  }

  synchronized void startLocomotives() {
    List<LocomotiveBean> locos = getOnTrackLocomotives();
    for (LocomotiveBean loc : locos) {
      startDispatcher(loc);
    }
  }

  public void resetDispatcher(LocomotiveBean locomotiveBean) {
    Logger.trace("Resetting dispatcher for " + locomotiveBean.getName());
    Dispatcher dispatcher;
    String key = locomotiveBean.getName();
    if (dispatchers.containsKey(key)) {
      dispatcher = dispatchers.get(key);
      dispatcher.reset();
    } else {
      Logger.warn("Dispatcher for " + locomotiveBean.getName() + " not found!");
    }
  }

  synchronized void resetStates() {
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
          if (!blockState.equals(tile.getBlockState())) {
            tile.setBlockBean(block);
          }
        }
      }
      PersistenceFactory.getService().persist(tile.getBlockBean());
    }

    JCS.getJcsCommandStation().switchPower(true);
    Logger.debug("Occupied blocks: " + occupiedBlockCounter + " Free blocks " + freeBlockCounter + " of total " + blocks.size() + " blocks");
  }

  public List<Dispatcher> getLocomotiveDispatchers() {
    return new ArrayList<>(dispatchers.values());
  }

  public synchronized Dispatcher getLocomotiveDispatcher(LocomotiveBean locomotiveBean) {
    String key = locomotiveBean.getName();
    return dispatchers.get(key);
  }

  public Dispatcher getLocomotiveDispatcher(int locUid) {
    LocomotiveBean locomotiveBean = PersistenceFactory.getService().getLocomotive(locUid, commandStationBean.getId());
    return getLocomotiveDispatcher(locomotiveBean);
  }

  public Dispatcher getLocomotiveDispatcher(long locId) {
    if (commandStationBean == null) {
      commandStationBean = PersistenceFactory.getService().getDefaultCommandStation();
    }
    LocomotiveBean locomotiveBean = PersistenceFactory.getService().getLocomotive((int) locId, commandStationBean.getId());
    return getLocomotiveDispatcher(locomotiveBean);
  }

  public boolean isOnTrack(LocomotiveBean locomotiveBean) {
    List<LocomotiveBean> onTrackLocomotives = getOnTrackLocomotives();
    for (LocomotiveBean locomotive : onTrackLocomotives) {
      if (locomotiveBean.getId().equals(locomotive.getId())) {
        return true;
      }
    }
    return false;
  }

  public List<LocomotiveBean> getOnTrackLocomotives() {
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
    return activeLocomotives;
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

  private void handleGhost(SensorEvent event) {
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

  void handleSensorEvent(SensorEvent event) {
    Logger.trace("Event for Sensor " + event.getSensorId() + " " + (event.isActive() ? "On" : "Off") + " isChanged " + event.isChanged());

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

  public synchronized void addSensorEventHandler(SensorEventHandler handler) {
    sensorHandlers.put(handler.getSensorId(), handler);
  }

  public boolean isSensorHandlerRegistered(Integer sensorId) {
    return sensorHandlers.containsKey(sensorId);
  }

  public synchronized void removeHandler(Integer sensorId) {
    sensorHandlers.remove(sensorId);
  }

  public synchronized void addAutoPilotStatusListener(AutoPilotStatusListener listener) {
    autoPilotStatusListeners.add(listener);
    Logger.trace("Status listeners: " + autoPilotStatusListeners.size());
  }

  public synchronized void removeAutoPilotStatusListener(AutoPilotStatusListener listener) {
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

  private void executeCommand(AutoPilotActionEvent event) {
    String command = event.getActionCommand();
    switch (command) {
      case "start" -> {
        startAutoMode();
      }
      case "stop" -> {
        stopAutoMode();
      }
      case "startLocomotive" -> {
        startDispatcher(event.getLocomotiveBean());
      }
      case "stopLocomotive" -> {
        stopDispatcher(event.getLocomotiveBean());
      }
      case "startAllLocomotives" -> {
        startLocomotives();
      }
      case "removeLocomotive" -> {
        removeDispatcher(event.getLocomotiveBean());
      }
      case "addLocomotive" -> {
        addDispatcher(event.getLocomotiveBean());
      }
      case "reset" -> {
        resetStates();
      }
    }
  }

  /**
   * An executer Thread to execute commands.
   */
  private class ActionCommandExecuter extends Thread {

    private boolean stop = false;
    private boolean quit = true;

    private final ConcurrentLinkedQueue<AutoPilotActionEvent> eventQueue;

    public ActionCommandExecuter(ConcurrentLinkedQueue eventQueue) {
      this.eventQueue = eventQueue;
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
      quit = false;
      setName("RAILWAY-CONTROLLER-EXECUTER");
      Logger.trace("RailwayController Command executer Started...");

      while (isRunning()) {
        try {
          AutoPilotActionEvent event = eventQueue.poll();
          if (event != null) {

            executeCommand(event);
          } else {
            //lets sleep for a while
            synchronized (this) {
              wait(250);
            }
          }

        } catch (InterruptedException ex) {
          Logger.error(ex);
        }
      }

      stop = true;
      Logger.trace("Tile ActionEventHandler Stopped...");
    }

  }
}
