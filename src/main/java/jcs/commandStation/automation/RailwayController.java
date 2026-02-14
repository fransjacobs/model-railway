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

import jcs.commandStation.automation.state.SensorEventCallback;
import jcs.commandStation.autopilot.state.Dispatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import jcs.JCS;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_ADD_LOC;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_FIRE_STATUS_LST;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_REMOVE_LOC;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_RESET;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_RESTORE_FUNC;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_START;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_START_ALL_LOC;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_START_LOC;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_STOP;
import static jcs.commandStation.automation.RailwayControllerCommand.CMD_STOP_LOC;
import static jcs.commandStation.autopilot.AutoPilot.autoPilotRunners;
import jcs.entities.BlockBean;
import jcs.entities.BlockBean.BlockState;
import static jcs.entities.BlockBean.BlockState.LOCKED;
import jcs.entities.CommandStationBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * The RailwayController is the core engine for automatic driving.<br>
 * Before the first locomotive can run automatically a few preconditions are necessary.<br>
 * The Layout need to be drawn, all blocks should be defined, all sensors are linked with blocks and the layout is routed.<br>
 *
 * When the RailwayContraller is instantiated for the first time the following tasks are performed:<br>
 * - The CommandExecution Thread is started. All Commands are executed in a background thread.<br>
 * - On first instantiation the last function settings of all locomotives are restored.<br>
 *
 * When the automatic driving is enabled:<br>
 * - The SensorMonitor Thread is started. The SensorMonitor will check the status of the sensor during the Automatic driving.<br>
 * - The (Locomotive)Dispatchers are (re)created for every Locomotive on the track.<br>
 * - The Status listeners are notified.<br>
 *
 * When a/all Locomotives is/are started the following events should happen: - The dispatcher for the Locomotive of choice should be started.<br>
 *
 * The Dispatcher is run in this Thread.<br>
 * The RailwayController has it own Monitor Thread: RailwayControllerMonitorThread.<br>
 * This moditorThread takes car of watching for ghost during automatic driving.
 *
 */
public final class RailwayController {

  private static RailwayController instance;

  private final ThreadGroup threadGroup;
  private CommandStationBean commandStationBean;
  private SensorMonitor sensorMonitor = null;

  private final ConcurrentLinkedQueue<RailwayControllerCommand> actionCommandQueue;

  private final CommandExecuter commandExecuter;

  private final Map<String, Dispatcher> dispatchers;

  private static final Semaphore semaphore = new Semaphore(1);

  //Need a list to be able to unregister
  private final List<RailwayControllerStatusListener> railwayStatusListeners;

  private boolean stepTest = false;

  private RailwayController() {
    threadGroup = new ThreadGroup("RAILWAY-CONTROLLER");
    dispatchers = new HashMap<>();
    railwayStatusListeners = new ArrayList<>();
    //railwayStatusListeners = Collections.synchronizedList(new ArrayList<>());

    actionCommandQueue = new ConcurrentLinkedQueue();
    commandExecuter = new CommandExecuter(actionCommandQueue);
    stepTest = Boolean.valueOf(System.getProperty("state.machine.stepTest", "false"));

    init();
  }

  private void init() {
    //The steptest property is need to enable testing. In normal operation this property is false.
    if (!stepTest) {
      commandExecuter.start();
      enqueCommand(new RailwayControllerCommand(CMD_RESTORE_FUNC));
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

  void restoreLocomotiveFunctions() {
    List<LocomotiveBean> onTrackLocomotives = getOnTrackLocomotives();
    Logger.trace("Restoring functions for " + onTrackLocomotives.size() + " locomotives");

    for (LocomotiveBean locomotive : onTrackLocomotives) {
      List<FunctionBean> functions = new LinkedList<>(locomotive.getFunctions().values());
      for (FunctionBean function : functions) {
        JCS.getJcsCommandStation().changeLocomotiveFunction(function.isOn(), function.getNumber(), locomotive);
      }
    }
  }

  ThreadGroup getThreadGroup() {
    return threadGroup;
  }

  /**
   * Commands are executed by the controllerThread.<br>
   * Commands are passed through via a queue.
   *
   * @param command the command to execute
   */
  private void enqueCommand(RailwayControllerCommand command) {
    if (!stepTest) {
      actionCommandQueue.offer(command);
      synchronized (commandExecuter) {
        commandExecuter.notifyAll();
      }
    } else {
      Logger.trace("Excute command " + command.getCommand() + " inline");
      this.executeCommand(command);
    }
  }

  void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

  public boolean startAutoMode() {
    if (JCS.getJcsCommandStation().isPowerOn()) {
      if (sensorMonitor != null && sensorMonitor.isRunning()) {
        Logger.trace("Allready running");
        return true;
      } else {
        commandStationBean = JCS.getJcsCommandStation().getCommandStationBean();
        sensorMonitor = new SensorMonitor(threadGroup);
        if (!stepTest) {
          sensorMonitor.start();

          //Wait for the initialization of the Sensors
          long now = System.currentTimeMillis();
          long start = now;
          long timeout = now + 10000; //give it max 10 s

          boolean sensorsReady = sensorMonitor.isRunning();
          while (!sensorsReady && now < timeout) {
            sensorsReady = sensorMonitor.isRunning();
            now = System.currentTimeMillis();
            //pause(100);
          }

          Logger.trace("Sensors prepared in " + (now - start) + " ms...");
        }

        prepareAllDispatchers();

        enqueCommand(new RailwayControllerCommand(CMD_FIRE_STATUS_LST, "automode.started"));

        Logger.trace("RailwayController Automode initialized. There are " + dispatchers.size() + " Dispatchers...");
      }

      return true;
    } else {
      Logger.warn("Can't start Automode, Command Station Power is Off!");
      return false;
    }
  }

  public SensorMonitor getSensorMonitor() {
    return sensorMonitor;
  }

  
  
  void fireStatusListeners(String status) {
    List<RailwayControllerStatusListener> snapshot = new ArrayList<>(railwayStatusListeners);
    for (RailwayControllerStatusListener rcsl : snapshot) {
      rcsl.statusChanged(status);
    }
  }

  public void stopAutoMode() {
    if (sensorMonitor != null) {
      //Notify all dispachers so that the ones which waiting and Idle will stop
      Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
      for (Dispatcher d : snapshot) {
        d.stopLocomotiveAutomode();
        if (!d.isRunning()) {
          d.stopRunning();
        }
      }

      
      //TODO: Can only be stopped if las dispatcher are stopped..
      //add check
      sensorMonitor.stopMonitor();

      try {
        sensorMonitor.join();
      } catch (InterruptedException ex) {
        Logger.error("Interrupted during join " + ex);
      }
    }
    Logger.debug("ControllerMonitor Stopped");
    sensorMonitor = null;

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
        addDispatcher(loc);
      }
    }
  }

  public void enableAutomode(boolean flag) {
    if (flag) {
      enqueCommand(new RailwayControllerCommand(CMD_START));
    } else {
      enqueCommand(new RailwayControllerCommand(CMD_STOP));
    }
  }

  public void startLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new RailwayControllerCommand(CMD_START_LOC, locomotiveBean));
  }

  public void stopLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new RailwayControllerCommand(CMD_STOP_LOC, locomotiveBean));
  }

  public void removeLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new RailwayControllerCommand(CMD_REMOVE_LOC, locomotiveBean));
  }

  public void addLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new RailwayControllerCommand(CMD_ADD_LOC, locomotiveBean));
  }

  public void restoreFunctions() {
    enqueCommand(new RailwayControllerCommand(CMD_RESTORE_FUNC));
  }

  public synchronized void startAllLocomotives() {
    enqueCommand(new RailwayControllerCommand(CMD_START_ALL_LOC));
  }

  public void reset() {
    enqueCommand(new RailwayControllerCommand("reset"));
  }

  public boolean isAutoModeActive() {
    if (sensorMonitor != null) {
      return sensorMonitor.isRunning();
    } else {
      return false;
    }
  }

  public boolean isSensorMonitorThreadStopped() {
    if (sensorMonitor != null) {
      return sensorMonitor.isStopped();
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

  public boolean isAnyDispatcherRunning() {
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
    int runningCount = 0;
    for (Dispatcher ld : snapshot) {
      if (ld.isRunning()) {
        runningCount++;
      }
    }
    return runningCount;
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

          //add it to the dispatchers
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

      enqueCommand(new RailwayControllerCommand(CMD_FIRE_STATUS_LST, "dispatcher.removed." + locomotiveBean.getName()));
    }
  }

  void addDispatcher(LocomotiveBean locomotiveBean) {
    createDispatcher(locomotiveBean);

    enqueCommand(new RailwayControllerCommand(CMD_FIRE_STATUS_LST, "dispatcher.added." + locomotiveBean.getName()));
  }

  public synchronized void removeDispatchers() {
    Logger.trace("Removing all Dispatchers...");

    for (Dispatcher dispatcher : dispatchers.values()) {
      dispatcher.stopLocomotiveAutomode();
    }

    dispatchers.clear();

    enqueCommand(new RailwayControllerCommand(CMD_FIRE_STATUS_LST, "all.dispatchers.removed"));
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

  public synchronized void addStatusListener(RailwayControllerStatusListener listener) {
    railwayStatusListeners.add(listener);
    Logger.trace("Status listeners: " + railwayStatusListeners.size());
  }

  public synchronized void removeStatusListener(RailwayControllerStatusListener listener) {
    railwayStatusListeners.remove(listener);
    Logger.trace("Status listeners: " + railwayStatusListeners.size());
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

  private void executeCommand(RailwayControllerCommand event) {
    String command = event.getCommand();
    switch (command) {
      case CMD_RESTORE_FUNC -> {
        restoreLocomotiveFunctions();
      }
      case CMD_START -> {
        startAutoMode();
      }
      case CMD_STOP -> {
        stopAutoMode();
      }
      case CMD_START_LOC -> {
        startDispatcher(event.getLocomotiveBean());
      }
      case CMD_STOP_LOC -> {
        stopDispatcher(event.getLocomotiveBean());
      }
      case CMD_START_ALL_LOC -> {
        startLocomotives();
      }
      case CMD_REMOVE_LOC -> {
        removeDispatcher(event.getLocomotiveBean());
      }
      case CMD_ADD_LOC -> {
        addDispatcher(event.getLocomotiveBean());
      }
      case CMD_RESET -> {
        resetStates();
      }
      case CMD_FIRE_STATUS_LST -> {
        fireStatusListeners(event.getStatus());
      }
    }
  }

//  void registerSensorEventCallback(SensorEventCallback callback) {
//    if (sensorMonitor != null) {
//      this.sensorMonitor.registerSensorEventCallback(callback);
//    } else {
//      Logger.warn("Can't register Callback for Sensor " + callback.getSensorId());
//    }
//  }

//  void unRegisterSensorEventCallback(SensorEventCallback callback) {
//    this.sensorMonitor.unRegisterSensorEventCallback(callback);
//  }

//  void unRegisterSensorEventCallback(Integer sensorId) {
//    this.sensorMonitor.unRegisterSensorEventCallback(sensorId);
//  }

//  boolean isSensorCallbackRegistered(Integer sensorId) {
//    return sensorMonitor.isSensorCallbackRegistered(sensorId);
//  }

  /**
   * An executer Thread to execute commands.
   */
  private class CommandExecuter extends Thread {

    private boolean stop = false;
    private boolean quit = true;

    private final ConcurrentLinkedQueue<RailwayControllerCommand> eventQueue;

    public CommandExecuter(ConcurrentLinkedQueue eventQueue) {
      this.eventQueue = eventQueue;
    }

    @SuppressWarnings("unused")
    void quit() {
      this.quit = true;
    }

    boolean isRunning() {
      return !this.quit;
    }

    @SuppressWarnings("unused")
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
          RailwayControllerCommand event = eventQueue.poll();
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
