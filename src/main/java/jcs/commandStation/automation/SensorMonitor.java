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
import java.util.concurrent.ConcurrentLinkedQueue;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilotStatusListener;
import jcs.commandStation.autopilot.SensorEventHandler;
import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * The Sensor monitor monitors the state of all sensors during automatic driving.
 */
class SensorMonitor extends Thread implements AllSensorEventsListener {

  private final RailwayController railwayController;

  private final ConcurrentLinkedQueue<SensorEvent> sensorEventQueue;

  private final Map<Integer, SensorEventHandler> sensorHandlers;
  //private final List<SensorListener> sensorListeners = new ArrayList<>();

  private boolean running = false;
  private boolean stopped = false;

  private long threadWaitMillis;

  private boolean allSensorsRegistered = false;

  SensorMonitor(RailwayController railwayController) {
    super(railwayController.getControllerRunners(), "RC-SENSOR-MONITOR");
    this.railwayController = railwayController;

    sensorEventQueue = new ConcurrentLinkedQueue();

    sensorHandlers = new HashMap<>();

    threadWaitMillis = Long.parseUnsignedLong(System.getProperty("autopilot.thread.wait.millis", "1000"));

    //JCS.getJcsCommandStation().addAllSensorEventsListener(this);
  }

  void stopMonitor() {
    Logger.trace("Stopping Monitor...");
    this.running = false;

    //JCS.getJcsCommandStation().removeAllSensorEventsListener(this);
    synchronized (this) {
      this.notifyAll();
    }
  }

  boolean isRunning() {
    return this.running;
  }

  //Obtain the current Sensor values
  void refreshAllSensorValues() {
    List<SensorBean> sensors = PersistenceFactory.getService().getAssignedSensors();
    for (SensorBean sb : sensors) {
      JCS.getJcsCommandStation().getSensorStatus(sb);
    }
    Logger.trace("Refreshed " + sensors.size() + " sensor values");
  }

//  void registerAllSensors() {
//    //Use only assigned sensors, ignore sensors which are not assigned to a Tile
//    sensorHandlers.clear();
//
//    //First refresh the sensors...
//    refreshAllSensorValues();
//
//    List<SensorBean> sensors = PersistenceFactory.getService().getAssignedSensors();
//    for (SensorBean sb : sensors) {
//      Integer key = sb.getId();
//
//      if (!sensorHandlers.containsKey(key)) {
//        SensorListener seh = new SensorListener(key);
//        sensorListeners.add(seh);
//        //Register with a command station
//        JCS.getJcsCommandStation().addSensorEventListener(key, seh);
//        //Logger.trace("Added handler " + cnt + " for sensor " + key);
//      }
//    }
//    Logger.trace("Registered " + sensors.size() + " sensor event handlers");
//  }
//  void unRegisterAllSensors() {
//    for (SensorListener seh : this.sensorListeners) {
//      JCS.getJcsCommandStation().removeSensorEventListener(seh.getSensorId(), seh);
//    }
//    Logger.trace("Unregistered " + sensorListeners.size() + " sensor event handlers");
//    this.sensorListeners.clear();
//  }
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

  public boolean isAllSensorsRegistered() {
    return allSensorsRegistered;
  }

  @Override
  public void onSensorChange(SensorEvent sensorEvent) {
    this.sensorEventQueue.offer(sensorEvent);
    synchronized (this) {
      notifyAll();
    }
  }

  //The sensorMonitor thread is started with each new session of the RailWayController.
  //When the Sensor Monitor initialized first register all (bound) sensors on the layout.
  @Override
  public void run() {
    running = true;
    Logger.debug("SensorMonitor thread wait time: " + threadWaitMillis + " ms.");

    //registerAllSensors();
    allSensorsRegistered = true;

    refreshAllSensorValues();

    JCS.getJcsCommandStation().addAllSensorEventsListener(this);

    while (running) {
      try {
        SensorEvent event = this.sensorEventQueue.poll();
        if (event != null) {
          handleSensorEvent(event);
        }

        synchronized (this) {
          wait(threadWaitMillis);
        }
      } catch (InterruptedException ex) {
        Logger.trace("Interrupted");
      }
    }

    //unRegisterAllSensors();
    sensorHandlers.clear();

    JCS.getJcsCommandStation().removeAllSensorEventsListener(this);

    Logger.trace("SensorMonitor Finished.");
    stopped = true;
  }

  boolean isStopped() {
    return this.stopped;
  }

//  private class SensorListener implements SensorEventListener {
//
//    private final Integer sensorId;
//
//    SensorListener(Integer sensorId) {
//      this.sensorId = sensorId;
//    }
//
//    @Override
//    public Integer getSensorId() {
//      return sensorId;
//    }
//
//    @Override
//    public void onSensorChange(SensorEvent event) {
//      if (sensorId.equals(event.getSensorId())) {
//        handleSensorEvent(event);
//      }
//    }
//  }
}
