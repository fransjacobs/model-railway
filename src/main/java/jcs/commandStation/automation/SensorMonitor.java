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
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
public class SensorMonitor extends Thread implements AllSensorEventsListener {

  private final BlockingQueue<SensorEvent> eventQueue;

  private final Map<Integer, List<SensorEventCallback>> subscribers;
  private final Set<Integer> subscribersWithoutCallback;

  private volatile boolean running = false;

  private final Map<Integer, SensorBean> sensorBeans;

  public SensorMonitor() {
    this(null);
  }

  public SensorMonitor(ThreadGroup threadGroup) {
    super(threadGroup, "RC-SENSOR-MONITOR");

    eventQueue = new LinkedBlockingQueue<>();
    subscribers = new ConcurrentHashMap<>();
    subscribersWithoutCallback = ConcurrentHashMap.newKeySet();
    sensorBeans = new HashMap<>();
  }

  Map<Integer, SensorBean> getSensorBeans() {
    return sensorBeans;
  }

  /**
   * Subscribe to events from a specific sensor(Id)
   *
   * @param sensorId
   * @param callback
   */
  public void subscribe(Integer sensorId, SensorEventCallback callback) {
    subscribers.computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>()).add(callback);
  }

  public void subscribeWithoutCallback(Integer sensorId) {
    subscribersWithoutCallback.add(sensorId);
  }

  Map<Integer, List<SensorEventCallback>> getSubscribers() {
    return subscribers;
  }

  @SuppressWarnings("unused")
  Set<Integer> getSubscribersWithoutCallback() {
    return subscribersWithoutCallback;
  }

  /**
   * Unsubscribe from sensor events
   *
   * @param sensorId
   * @param callback
   */
  public void unsubscribe(Integer sensorId, SensorEventCallback callback) {
    List<SensorEventCallback> callbacks = subscribers.get(sensorId);
    if (callbacks != null) {
      callbacks.remove(callback);
      if (callbacks.isEmpty()) {
        subscribers.remove(sensorId);
      }
    } else {
      //remove all
      subscribers.remove(sensorId);
    }

    subscribersWithoutCallback.remove(sensorId);
  }

  public boolean isSensorRegisteredWithCallback(Integer sensorId) {
    if (subscribers.containsKey(sensorId)) {
      List<SensorEventCallback> callbacks = subscribers.get(sensorId);
      return !callbacks.isEmpty();
    } else {
      return false;
    }
  }

  public boolean isSensorRegisteredWithoutCallback(Integer sensorId) {
    return this.subscribersWithoutCallback.contains(sensorId);
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

  void registerAllSensors() {
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
    Logger.trace("Check for possible Ghost! @ Sensor: " + event.getSensorId() + " Active: " + event.isActive());

    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    Integer sensorId = event.getSensorId();
    //this can be faster... ignore in case a block is set to not used...
    for (BlockBean block : blocks) {

      if ((block.getMinSensorId().equals(sensorId) || block.getPlusSensorId().equals(sensorId)) && block.getLocomotiveId() == null) {
        Tile tile = TileCache.findTile(block.getTileId());
        if (event.getSensorBean().isActive()) {
          block.setBlockState(BlockBean.BlockState.GHOST);

          if (tile != null) {
            tile.setBlockState(BlockBean.BlockState.GHOST);
          } else {
            Logger.warn("Can't find Tile " + block.getTileId());
          }
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

  boolean isGhostDetected() {
    return PersistenceFactory.getService().getGhostBlockCount() > 0;
  }

  void handleSensorEvent(SensorEvent event) {
    Logger.trace("Event for Sensor " + event.getSensorId() + " " + (event.isActive() ? "On" : "Off") + " isChanged " + event.isChanged());

    if (event.isChanged()) {
      if (subscribersWithoutCallback.contains(event.getSensorId())) {
        Logger.trace(event.getSensorId() + " is subscribed in ignore list...");
      } else if (subscribers.containsKey(event.getSensorId())) {
        Logger.trace(event.getSensorId() + " is subscribed in callback list...");
        notifySubscribers(event);
      } else {
        Logger.trace(event.getSensorId() + " is NOT subscribed...");
        handleGhost(event);
      }
    }
    synchronized (this) {
      notifyAll();
    }

  }

  void notifySubscribers(SensorEvent event) {
    List<SensorEventCallback> callbacks = subscribers.get(event.getSensorId());
    if (callbacks != null && !callbacks.isEmpty()) {
      for (SensorEventCallback callback : callbacks) {
        try {
          callback.onEvent(event);
        } catch (Exception e) {
          Logger.error("Error in event callback: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Called by the Command station when a sensor value is changed
   *
   * @param sensorEvent
   */
  @Override
  public void onSensorChange(SensorEvent sensorEvent) {
    this.eventQueue.offer(sensorEvent);
  }

  //The sensorMonitor thread is started with each new session of the RailWayController.
  //When the Sensor Monitor initialized first register all (bound) sensors on the layout.
  @Override
  public void run() {
    //Make sure for a clean start
    subscribers.clear();
    eventQueue.clear();
    sensorBeans.clear();

    registerAllSensors();
    //Subsribe to the command station as SensorEventListener 
    JCS.getJcsCommandStation().addAllSensorEventsListener(this);

    Logger.trace("SensorMonitor is watching " + sensorBeans.size());

    running = true;
    while (running) {
      try {
        //SensorEvent event = this.eventQueue.take();
        SensorEvent event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
        if (event != null) {
          handleSensorEvent(event);
        }
      } catch (InterruptedException ex) {
        Logger.trace("Interrupted");
        Thread.currentThread().interrupt();
        break;
      }
    }

    JCS.getJcsCommandStation().removeAllSensorEventsListener(this);
    subscribers.clear();
    subscribersWithoutCallback.clear();
    sensorBeans.clear();

    Logger.trace("SensorMonitor Finished.");
  }

}
