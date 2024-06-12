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

import jcs.commandStation.autopilot.state.LocomotiveDispatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import static jcs.entities.BlockBean.BlockState.ARRIVING;
import static jcs.entities.BlockBean.BlockState.DEPARTING;
import static jcs.entities.BlockBean.BlockState.LEAVING;
import static jcs.entities.BlockBean.BlockState.LOCKED;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.events.TileEvent;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class AutoPilot extends Thread {

  private static AutoPilot instance = null;
  private boolean running;

  //private final Map<String, SensorEventHandler> sensorHandlers = Collections.synchronizedMap(new HashMap<>());
  private final Map<String, SensorEventHandler> sensorHandlers = new HashMap<>();
  private final Map<String, LocomotiveDispatcher> dispatchers = Collections.synchronizedMap(new HashMap<>());

  //Need a list to be able to unregister
  private final List<SensorListener> sensorListeners = new ArrayList<>();
  private final List<AutoPilotStatusListener> autoPilotStatusListeners = Collections.synchronizedList(new ArrayList<>());

  private AutoPilot() {
  }

  public static AutoPilot getInstance() {
    if (instance == null) {
      instance = new AutoPilot();
    }
    return instance;
  }

  @Override
  public void run() {
    this.running = true;
    setName("AutoPilot");
    //setDaemon(running);

    registerAllSensors();
    prepareDispatchers();

    Logger.trace("Autopilot Started. Notify " + autoPilotStatusListeners.size() + " Listeners...");

    for (AutoPilotStatusListener asl : autoPilotStatusListeners) {
      asl.statusChanged(running);
    }

    while (running) {
      try {
        synchronized (this) {
          wait(1000);
        }
      } catch (InterruptedException ex) {
        Logger.trace("Interrupted");
      }
    }

    //Test
    stoppingAutomodeTestDialogs();

    unRegisterAllSensors();
    sensorHandlers.clear();

    for (AutoPilotStatusListener asl : autoPilotStatusListeners) {
      asl.statusChanged(running);
    }

    resetStates();
    
    Logger.trace("Autopilot Finished. Notify " + autoPilotStatusListeners.size() + " Listeners...");
  }

  public void prepareDispatchers() {
    Logger.trace("Preparing Dispatcher for all on track locomotives...");

    List<LocomotiveBean> locs = getOnTrackLocomotives();
    Map<String, LocomotiveDispatcher> snapshot = new HashMap<>(this.dispatchers);
    this.dispatchers.clear();

    for (LocomotiveBean loc : locs) {
      LocomotiveDispatcher dispatcher;
      if (snapshot.containsKey(loc.getName())) {
        dispatcher = snapshot.get(loc.getName());
      } else {
        dispatcher = new LocomotiveDispatcher(loc, this);
      }
      dispatchers.put(loc.getName(), dispatcher);
      Logger.trace("Added dispatcher for " + loc.getName() + "...");
    }
  }

  public void startAllLocomotives() {
    Logger.trace("Starting automode for all on track locomotives...");

    List<LocomotiveBean> locs = getOnTrackLocomotives();
    Map<String, LocomotiveDispatcher> snapshot = new HashMap<>(this.dispatchers);
    this.dispatchers.clear();

    for (LocomotiveBean loc : locs) {
      LocomotiveDispatcher dispatcher;
      if (snapshot.containsKey(loc.getName())) {
        dispatcher = snapshot.get(loc.getName());
      } else {
        dispatcher = new LocomotiveDispatcher(loc, this);
      }
      dispatchers.put(loc.getName(), dispatcher);
      Logger.trace("Starting " + loc.getName() + "...");

      //TODO: Combine with the start stop (single) locomotive)
      dispatcher.startRunning();
    }
  }

  public void stopAllLocomotives() {
    Logger.trace("Stopping automode for all locomotives...");
    for (LocomotiveDispatcher ld : this.dispatchers.values()) {
      ld.stopRunning();
    }
  }

  //For now when stopping also reset the states if needed
  private void resetStates() {
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes();
    int lockedCounter = 0;
    for (RouteBean route : routes) {
      if (route.isLocked()) {
        route.setLocked(false);
        PersistenceFactory.getService().persist(route);
        lockedCounter++;
      }
      resetRoute(route);
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
            case LOCKED, ARRIVING -> {
              //destinations block, reset!
              block.setLocomotive(null);
              block.setBlockState(BlockBean.BlockState.FREE);
              block.setArrivalSuffix(null);
              freeBlockCounter++;
            }
            case DEPARTING, LEAVING -> {
              block.setBlockState(BlockBean.BlockState.OCCUPIED);
              block.setArrivalSuffix(null);
              occupiedBlockCounter++;
            }
            default -> {
              if (BlockBean.BlockState.OCCUPIED == block.getBlockState()) {
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
      showBlockStatus(block);
    }

    JCS.getJcsCommandStation().switchPower(true);

    Logger.debug("Occupied blocks: " + occupiedBlockCounter + " Free blocks " + freeBlockCounter + " of total " + blocks.size() + " blocks");
  }

  private void resetRoute(RouteBean route) {
    List<RouteElementBean> routeElements = route.getRouteElements();
    for (RouteElementBean re : routeElements) {
      String tileId = re.getTileId();
      TileEvent tileEvent = new TileEvent(tileId, false);
      TileFactory.fireTileEventListener(tileEvent);
    }
  }

  public synchronized List<LocomotiveDispatcher> getLocomotiveDispatchers() {
    if (this.running) {
      return new ArrayList<>(dispatchers.values());
    } else {
      return Collections.EMPTY_LIST;
    }
  }

  public synchronized LocomotiveDispatcher getLocomotiveDispatcher(LocomotiveBean locomotiveBean) {
    String key = locomotiveBean.getName();
    return dispatchers.get(key);
  }

  public void startStopLocomotive(LocomotiveBean locomotiveBean, boolean start) {
    Logger.trace((start ? "Starting" : "Stopping") + " auto drive for " + locomotiveBean.getName());
    if (start) {
      LocomotiveDispatcher dispatcher;
      String key = locomotiveBean.getName();
      if (this.dispatchers.containsKey(key)) {
        dispatcher = dispatchers.get(key);
      } else {
        dispatcher = new LocomotiveDispatcher(locomotiveBean, this);
        dispatchers.put(key, dispatcher);
      }

      Logger.debug("Starting " + key);
      dispatcher.startRunning();
    } else {
      LocomotiveDispatcher dispatcher = this.dispatchers.get(locomotiveBean.getName());
      if (dispatcher != null) {
        dispatcher.stopRunning();
      }
    }
  }

  private List<LocomotiveBean> getOnTrackLocomotives() {
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

    if (Logger.isDebugEnabled()) {
      Logger.trace("There are " + activeLocomotives.size() + " Locomotives on the track: ");
      for (LocomotiveBean loc : activeLocomotives) {
        Logger.trace(loc);
      }
    }
    return new ArrayList<>(activeLocomotives);
  }

  private void handleGhost(SensorEvent event) {
    Logger.warn("Ghost Detected! @ Sensor " + event.getId());
    //Switch power OFF!
    JCS.getJcsCommandStation().switchPower(false);

    //Show the Ghost block
    String gostSensorId = event.getId();
    //to which block does the sensor belong?
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    for (BlockBean block : blocks) {
      if (block.getMinSensorId().equals(gostSensorId) || block.getPlusSensorId().equals(gostSensorId)) {
        if (event.getSensorBean().isActive()) {
          block.setBlockState(BlockBean.BlockState.GHOST);
        } else {
          block.setBlockState(BlockBean.BlockState.FREE);
        }
        showBlockStatus(block);
        break;
      }
    }
  }

  public void showBlockStatus(BlockBean blockBean) {
    Logger.trace("Show Block " + blockBean.toString());
    TileEvent tileEvent = new TileEvent(blockBean);
    TileFactory.fireTileEventListener(tileEvent);
  }

  private void handleSensorEvent(SensorEvent event) {
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
        handleGhost(event);
      }
    }
  }

  private void registerAllSensors() {
    List<SensorBean> sensors = PersistenceFactory.getService().getSensors();
    int cnt = 0;
    for (SensorBean sb : sensors) {
      String key = sb.getId();
      if (!sensorHandlers.containsKey(key)) {
        SensorListener seh = new SensorListener(key, this);
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

  private void stoppingAutomodeTestDialogs() {
    Logger.trace("Stopping automode testdialogs");
    for (LocomotiveDispatcher ld : this.dispatchers.values()) {
      //Test
      //ld.disposeDialog();
    }
  }

  public void addHandler(SensorEventHandler handler, String sensorId) {
    sensorHandlers.put(sensorId, handler);
  }

  public boolean isSensorRegistered(String sensorId) {
    return this.sensorHandlers.containsKey(sensorId);
  }

  public synchronized void removeHandler(String sensorId) {
    sensorHandlers.remove(sensorId);
  }

  public synchronized void addAutoPilotStatusListener(AutoPilotStatusListener listener) {
    this.autoPilotStatusListeners.add(listener);
    Logger.trace("Status listeners: " + autoPilotStatusListeners.size());
  }

  public synchronized void removeAutoPilotStatusListener(AutoPilotStatusListener listener) {
    this.autoPilotStatusListeners.remove(listener);
    Logger.trace("Status listeners: " + autoPilotStatusListeners.size());
  }

  private class SensorListener implements SensorEventListener {

    private final String sensorId;
    private final AutoPilot delegate;

    SensorListener(String sensorId, AutoPilot delegate) {
      this.sensorId = sensorId;
      this.delegate = delegate;
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      if (sensorId.equals(event.getId())) {
        delegate.handleSensorEvent(event);
      }
    }
  }

  public boolean isRunning() {
    return running;
  }

  public boolean isRunning(LocomotiveBean locomotive) {
    if (this.running && this.dispatchers.containsKey(locomotive.getName())) {
      LocomotiveDispatcher dispatcher = this.dispatchers.get(locomotive.getName());
      return dispatcher.isRunning();
    } else {
      return false;
    }
  }

  public void startAutoPilot() {
    if (!running) {
      dispatchers.clear();
      sensorHandlers.clear();
      //Keep the statuslisteners
      List<AutoPilotStatusListener> apsl = new ArrayList<>(instance.autoPilotStatusListeners);

      instance = null;
      instance = new AutoPilot();
      instance.autoPilotStatusListeners.addAll(apsl);

      instance.start();
    }
  }

  public synchronized void stopAutoPilot() {
    this.running = false;
    notifyAll();
  }

  public static void main(String[] a) {
    AutoPilot ap = new AutoPilot();
    JCS.getJcsCommandStation().connect();

    ap.registerAllSensors();
    ap.startAllLocomotives();

    //ap.startAllLocomotives();
  }

}
