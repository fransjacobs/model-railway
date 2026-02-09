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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import jcs.JCS;
import jcs.commandStation.events.AllSensorEventsListener;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
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

  private final Map<Integer, SensorBean> sensorBeans;
  //private final Map<Integer, SensorBean> assignedSensorBeans;
  private final Map<Integer, SensorEventCallback> sensorEventCallbacks;

  private boolean running = false;
  private boolean stopped = false;
  private long threadWaitMillis;

  private boolean allSensorsRegistered = false;

  SensorMonitor() {
    this(null);
  }

  SensorMonitor(RailwayController railwayController) {
    super((railwayController != null ? railwayController.getControllerRunners() : null), "RC-SENSOR-MONITOR");
    this.railwayController = railwayController;

    sensorEventQueue = new ConcurrentLinkedQueue();
    sensorBeans = new HashMap<>();
    //assignedSensorBeans = new HashMap<>();
    sensorEventCallbacks = new HashMap<>();

    threadWaitMillis = Long.parseUnsignedLong(System.getProperty("autopilot.thread.wait.millis", "1000"));
  }

  void stopMonitor() {
    Logger.trace("Stopping Monitor...");
    this.running = false;

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

  synchronized void registerSensorEventCallback(SensorEventCallback callback) {
    sensorEventCallbacks.put(callback.getSensorId(), callback);
  }

  synchronized void unRegisterSensorEventCallback(SensorEventCallback callback) {
    sensorEventCallbacks.remove(callback.getSensorId());
  }

  synchronized void unRegisterSensorEventCallback(Integer sensorId) {
    sensorEventCallbacks.remove(sensorId);
  }

  synchronized boolean isSensorCallbackRegistered(Integer sensorId) {
    return sensorEventCallbacks.containsKey(sensorId);
  }

  void registerAllSensors() {
    sensorBeans.clear();
    //Use only assigned sensors, ignore sensors which are not assigned to a Tile
    //First refresh the sensors...
    refreshAllSensorValues();

    List<SensorBean> sensors = PersistenceFactory.getService().getAssignedSensors();
    for (SensorBean sb : sensors) {
      sensorBeans.put(sb.getId(), sb);
    }
    Logger.trace("Registered " + sensorBeans.size() + " sensors");
  }

  void handleGhost(SensorEvent event) {
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
      if (sensorEventCallbacks.containsKey(event.getSensorId())) {
        SensorEventCallback sec = sensorEventCallbacks.get(event.getSensorId());
        Logger.trace("Registered " + event.getSensorId() + " has changed " + event.isChanged());

        if (!sec.isIgnoreEvent()) {
          sec.onSensorChange(event);

          synchronized (sec) {
            sec.notifyAll();
          }
        }

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
    sensorEventQueue.offer(sensorEvent);
    synchronized (this) {
      notifyAll();
    }
  }

  Map<Integer, SensorEventCallback> getSensorEventCallbacks() {
    return sensorEventCallbacks;
  }

  //The sensorMonitor thread is started with each new session of the RailWayController.
  //When the Sensor Monitor initialized first register all (bound) sensors on the layout.
  @Override
  public void run() {
    running = true;
    Logger.debug("SensorMonitor thread wait time: " + threadWaitMillis + " ms.");

    refreshAllSensorValues();

    JCS.getJcsCommandStation().addAllSensorEventsListener(this);
    //Register the assigned sensors

    allSensorsRegistered = true;

    while (running) {
      try {
        SensorEvent event = sensorEventQueue.poll();
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

    sensorEventCallbacks.clear();
    sensorBeans.clear();

    JCS.getJcsCommandStation().removeAllSensorEventsListener(this);

    Logger.trace("SensorMonitor Finished.");
    stopped = true;
  }

  boolean isStopped() {
    return this.stopped;
  }
}
