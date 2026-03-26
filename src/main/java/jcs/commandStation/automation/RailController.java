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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jcs.JCS;
import static jcs.commandStation.automation.RailControllerCommand.CMD_ADD_LOC;
import static jcs.commandStation.automation.RailControllerCommand.CMD_FIRE_STATUS_LST;
import static jcs.commandStation.automation.RailControllerCommand.CMD_REMOVE_LOC;
import static jcs.commandStation.automation.RailControllerCommand.CMD_RESET;
import static jcs.commandStation.automation.RailControllerCommand.CMD_RESTORE_FUNC;
import static jcs.commandStation.automation.RailControllerCommand.CMD_START;
import static jcs.commandStation.automation.RailControllerCommand.CMD_START_ALL_LOC;
import static jcs.commandStation.automation.RailControllerCommand.CMD_START_LOC;
import static jcs.commandStation.automation.RailControllerCommand.CMD_STOP;
import static jcs.commandStation.automation.RailControllerCommand.CMD_STOP_LOC;
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
 * The RailController is the core engine for automatic driving.<br>
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
The RailController has it own Monitor Thread: RailwayControllerMonitorThread.<br>
 * This moditorThread takes car of watching for ghost during automatic driving.
 *
 */
public final class RailController {

  private static RailController instance;

  private final ThreadGroup threadGroup;
  private CommandStationBean commandStationBean;
  private SensorMonitor sensorMonitor = null;

  private final BlockingQueue<RailControllerCommand> actionCommandQueue;

  private final CommandExecuter commandExecuter;

  private final Map<String, Dispatcher> dispatchers;

  private static final Semaphore semaphore = new Semaphore(1);

  private final List<RailControllerStatusListener> railwayStatusListeners;

  final static long THREADSTART_TIMEOUT = 2000L;

  final static long ALL_DISPATCHER_STOPPING_TIMEOUT = 600000L;

  private boolean automodeOn = false;

  private String status;

  private boolean functionsRestored;

  public static final String PENDING = "automode.pending";
  public static final String STOPPING = "automode.stopping";
  public static final String STOPPED = "automode.stopped";
  public static final String STARTED = "automode.started";

  private RailController() {
    threadGroup = new ThreadGroup("RAILWAY-CONTROLLER");
    dispatchers = new ConcurrentHashMap<>();
    railwayStatusListeners = new ArrayList<>();
    actionCommandQueue = new LinkedBlockingQueue();
    commandExecuter = new CommandExecuter(actionCommandQueue);
    status = PENDING;
  }

  /**
   * There can only be one Railway Controller.<br>
   *
   * @return the instance of the RailController
   */
  public synchronized static RailController getInstance() {
    if (instance == null) {
      instance = new RailController();

      //Start the command queue
      if (!instance.commandExecuter.isRunning()) {
        instance.commandExecuter.start();
      }
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
  private void enqueCommand(RailControllerCommand command) {
    actionCommandQueue.offer(command);
  }

  void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

  boolean startAutoMode() {
    if (JCS.getJcsCommandStation().isPowerOn()) {
      if (sensorMonitor != null && sensorMonitor.isRunning()) {
        Logger.trace("Already running");
        return true;
      } else {
        commandStationBean = JCS.getJcsCommandStation().getCommandStationBean();
        sensorMonitor = new SensorMonitor(threadGroup);

        long now = System.currentTimeMillis();
        long start = now;
        long timeout = now + THREADSTART_TIMEOUT;

        sensorMonitor.start();

        //On the first session restore the save locomotive functions
        boolean restoreFunctionsEverySession = Boolean.parseBoolean("restore.functions.every.session");
        if (!functionsRestored || restoreFunctionsEverySession) {
          restoreLocomotiveFunctions();
          functionsRestored = true;
        }

        boolean monitorStarted = sensorMonitor.isRunning();
        while (!monitorStarted && now < timeout) {
          monitorStarted = sensorMonitor.isRunning();
          now = System.currentTimeMillis();
        }

        Logger.trace("SensorMonitor Initialized in " + (now - start) + " ms...");
        automodeOn = true;

        //TODO: Do this every time the Automode is started or just once?
        //restoreLocomotiveFunctions();
        prepareAllDispatchers();

        Logger.trace("RailwayController Automode initialized. There are " + dispatchers.size() + " Dispatchers...");
        fireStatusListeners(STARTED);
      }

      return automodeOn;
    } else {
      Logger.warn("Can't start Automode, Command Station Power is Off!");
      //enqueCommand(new RailControllerCommand(CMD_FIRE_STATUS_LST, "automode.stopped"));
      fireStatusListeners(STOPPED);
      return false;
    }
  }

  public boolean isAutoModeActive() {
    return this.automodeOn;
  }

  void setAutomodeOn(boolean automodeOn) {
    this.automodeOn = automodeOn;
  }

  void setSensorMonitor(SensorMonitor sensorMonitor) {
    this.sensorMonitor = sensorMonitor;
  }

  SensorMonitor getSensorMonitor() {
    return sensorMonitor;
  }

  public String getStatus() {
    return status;
  }

  void fireStatusListeners(String status) {
    this.status = status;
    List<RailControllerStatusListener> snapshot = new ArrayList<>(railwayStatusListeners);
    for (RailControllerStatusListener rcsl : snapshot) {
      rcsl.onControllerStatusChange(status);
    }
  }

  void stopAutoMode() {
    if (sensorMonitor != null) {
      //Notify all dispachers so that the ones which waiting and Idle will stop
      Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
      for (Dispatcher d : snapshot) {
        if (d.isLocomotiveStarted()) {
          d.stopLocomotive();
        }
      }

      fireStatusListeners(STOPPING);

      long now = System.currentTimeMillis();
      long start = now;
      long timeout = now + ALL_DISPATCHER_STOPPING_TIMEOUT;
      boolean dispatcherRunning = isAnyDispatcherRunning();

      automodeOn = false;

      while (dispatcherRunning && now < timeout) {
        dispatcherRunning = isAnyDispatcherRunning();
        now = System.currentTimeMillis();
        pause(100);
      }

      Logger.trace("All dispatchers are stopped in " + (now - start) + " ms...");
      sensorMonitor.stopMonitor();

      try {
        sensorMonitor.join();
      } catch (InterruptedException ex) {
        Logger.error("Interrupted during join " + ex);
      }
    }
    Logger.debug("ControllerMonitor Stopped");
    sensorMonitor = null;
    dispatchers.clear();

    fireStatusListeners(STOPPED);
  }

  void addDispatcher(LocomotiveBean locomotiveBean) {
    Dispatcher dispatcher = createDispatcher(locomotiveBean);
    dispatcher.enable();
    dispatchers.put(locomotiveBean.getName(), dispatcher);
  }

  Dispatcher createDispatcher(LocomotiveBean locomotiveBean) {
    Dispatcher dispatcher = null;
    //check if the locomotive is on track
    if (isOnTrack(locomotiveBean)) {
      if (dispatchers.containsKey(locomotiveBean.getName())) {
        dispatcher = dispatchers.get(locomotiveBean.getName());
        Logger.trace("Reuse dispatcher for " + locomotiveBean.getName() + "...");
      } else {
        dispatcher = new Dispatcher(this, locomotiveBean);
        Logger.trace("Created new dispatcher for " + locomotiveBean.getName() + "...");
      }
    }
    return dispatcher;
  }

  public void prepareAllDispatchers() {
    Logger.trace("Preparing Dispatchers for all on track locomotives...");

    if (commandStationBean == null) {
      commandStationBean = JCS.getJcsCommandStation().getCommandStationBean();
    }

    List<LocomotiveBean> locs = getOnTrackLocomotives();

    Map<String, Dispatcher> snapshot = new HashMap<>(dispatchers);
    dispatchers.clear();

    for (LocomotiveBean loc : locs) {
      Dispatcher dispatcher;
      if (snapshot.containsKey(loc.getName())) {
        dispatcher = snapshot.get(loc.getName());
        dispatcher.enable();
        dispatchers.put(loc.getName(), dispatcher);
        Logger.trace("Re use dispatcher " + loc.getName() + "...");
      } else {
        dispatcher = createDispatcher(loc);
        dispatcher.enable();
        dispatchers.put(loc.getName(), dispatcher);
      }
    }
  }

  public void enableAutomode(boolean flag) {
    if (flag) {
      enqueCommand(new RailControllerCommand(CMD_START));
    } else {
      enqueCommand(new RailControllerCommand(CMD_STOP));
    }
  }

  public void startLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new RailControllerCommand(CMD_START_LOC, locomotiveBean));
  }

  public void stopLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new RailControllerCommand(CMD_STOP_LOC, locomotiveBean));
  }

  public void removeLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new RailControllerCommand(CMD_REMOVE_LOC, locomotiveBean));
  }

  public void addLocomotive(LocomotiveBean locomotiveBean) {
    enqueCommand(new RailControllerCommand(CMD_ADD_LOC, locomotiveBean));
  }

  public void restoreFunctions() {
    enqueCommand(new RailControllerCommand(CMD_RESTORE_FUNC));
  }

  public synchronized void startAllLocomotives() {
    enqueCommand(new RailControllerCommand(CMD_START_ALL_LOC));
  }

  public void reset() {
    enqueCommand(new RailControllerCommand("reset"));
  }

  public boolean isSensorMonitorThreadStopped() {
    if (sensorMonitor != null) {
      return !sensorMonitor.isRunning();
    } else {
      return true;
    }
  }

  public boolean isRunning(LocomotiveBean locomotive) {
    if (isAutoModeActive() && dispatchers.containsKey(locomotive.getName())) {
      Dispatcher dispatcher = dispatchers.get(locomotive.getName());
      return dispatcher.isLocomotiveStarted();
    } else {
      return false;
    }
  }

  boolean isAnyDispatcherRunning() {
    boolean isRunning = false;
    Set<Dispatcher> snapshot = new HashSet<>(dispatchers.values());
    for (Dispatcher ld : snapshot) {
      isRunning = ld.isLocomotiveStarted();
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
      if (ld.isLocomotiveStarted()) {
        runningCount++;
      }
    }
    return runningCount;
  }

  void removeDispatcher(LocomotiveBean locomotiveBean) {
    if (dispatchers.containsKey(locomotiveBean.getName())) {
      Dispatcher dispatcher = dispatchers.remove(locomotiveBean.getName());
      Logger.trace("removing Dispatcher for locomotive " + locomotiveBean.getName());
      if (dispatcher.isLocomotiveStarted()) {
        Logger.trace("Stopping Automode for " + locomotiveBean.getName() + "...");
        dispatcher.stopLocomotive();
        //dispatcher.stopRunning();
      }

      dispatcher.removeAllStateEventListeners();
      //enqueCommand(new RailControllerCommand(CMD_FIRE_STATUS_LST, "dispatcher.removed." + locomotiveBean.getName()));
    }
  }

  public synchronized void removeDispatchers() {
    Logger.trace("Removing all Dispatchers...");

    for (Dispatcher dispatcher : dispatchers.values()) {
      dispatcher.stopLocomotive();
    }

    dispatchers.clear();

    enqueCommand(new RailControllerCommand(CMD_FIRE_STATUS_LST, "all.dispatchers.removed"));
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

    if (!dispatcher.isLocomotiveStarted()) {
      Logger.trace("Starting dispatcher thread " + key);
      dispatcher.startLocomotive();
    }

    Logger.trace("Started locomotive " + key + "...");
  }

  synchronized void stopDispatcher(LocomotiveBean locomotiveBean) {
    Logger.trace("Stopping locomotive " + locomotiveBean.getName());
    String key = locomotiveBean.getName();

    Dispatcher dispatcher = dispatchers.get(key);
    if (dispatcher != null && dispatcher.isLocomotiveStarted()) {
      dispatcher.stopLocomotive();
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
    }
    Logger.debug("Unlocked " + lockedCounter + " routes out of " + routes.size());

    // Reset route
    int occupiedBlockCounter = 0;
    int freeBlockCounter = 0;
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();

    for (BlockBean block : blocks) {
      Tile tile = TileCache.findTile(block.getTileId());
      if (block.getLocomotiveId() != null && block.getBlockState() != null) {
        if (BlockState.OCCUPIED == block.getBlockState()) {
          occupiedBlockCounter++;
        }
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
              //tile.setArrivalSuffix(null);
              //arrival suffix should be set to the default also...
              occupiedBlockCounter++;
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

  public List<Dispatcher> getDispatchers() {
    return new ArrayList<>(dispatchers.values());
  }

  public Dispatcher getDispatcher(LocomotiveBean locomotiveBean) {
    String key = locomotiveBean.getName();
    return dispatchers.get(key);
  }

  public Dispatcher getDispatcher(int locUid) {
    LocomotiveBean locomotiveBean = PersistenceFactory.getService().getLocomotive(locUid, commandStationBean.getId());
    return getDispatcher(locomotiveBean);
  }

  public Dispatcher getLocomotiveDispatcher(long locId) {
    if (commandStationBean == null) {
      commandStationBean = PersistenceFactory.getService().getDefaultCommandStation();
    }
    LocomotiveBean locomotiveBean = PersistenceFactory.getService().getLocomotive((int) locId, commandStationBean.getId());
    return getDispatcher(locomotiveBean);
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

  public synchronized void addStatusListener(RailControllerStatusListener listener) {
    railwayStatusListeners.add(listener);
    Logger.trace("Status listeners: " + railwayStatusListeners.size());
  }

  public synchronized void removeStatusListener(RailControllerStatusListener listener) {
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

  private void executeCommand(RailControllerCommand event) {
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
      default -> {
        Logger.warn("Unknown Command: " + command);
      }
    }
  }

  /**
   * An executer Thread to execute commands.
   */
  private class CommandExecuter extends Thread {

    private volatile boolean running;
    private final BlockingQueue<RailControllerCommand> eventQueue;

    public CommandExecuter(BlockingQueue eventQueue) {
      super(threadGroup, "RAILWAY-CONTROLLER-EXECUTER");
      this.eventQueue = eventQueue;
    }

    boolean isRunning() {
      return running;
    }

    @Override
    public void run() {
      running = true;
      Logger.trace("RailwayController Command executer Started...");
      fireStatusListeners(PENDING);

      while (isRunning()) {
        try {
          RailControllerCommand event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
          if (event != null) {
            executeCommand(event);
          }
        } catch (InterruptedException ex) {
          Logger.error(ex);
        }
      }

      Logger.trace("CommandExecuter Stopped...");
    }

  }
}
