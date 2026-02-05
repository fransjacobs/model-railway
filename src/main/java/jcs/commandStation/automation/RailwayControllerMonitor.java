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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilotStatusListener;
import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.FunctionBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 */
class RailwayControllerMonitor extends Thread {

  private final RailwayController railwayController;
  private final List<SensorListener> sensorListeners = new ArrayList<>();

  private boolean running = false;
  private boolean stopped = false;

  RailwayControllerMonitor(RailwayController railwayController, ThreadGroup parent) {
    super(parent, "RW-CONTROLLER-MONITOR");
    this.railwayController = railwayController;
  }

  void stopAutoMode() {
    Logger.trace("Stopping Automode...");
    this.running = false;
    synchronized (this) {
      this.notifyAll();
    }
  }

  boolean isRunning() {
    return this.running;
  }

  void restoreLocomotiveFunctions() {
    List<LocomotiveBean> onTrackLocomotives = this.railwayController.getOnTrackLocomotives();
    Logger.trace("Restoring functions for " + onTrackLocomotives.size() + " locomotives");

    for (LocomotiveBean locomotive : onTrackLocomotives) {
      List<FunctionBean> functions = new LinkedList<>(locomotive.getFunctions().values());
      for (FunctionBean function : functions) {
        JCS.getJcsCommandStation().changeLocomotiveFunction(function.isOn(), function.getNumber(), locomotive);
      }
    }
  }

  void refreshAllSensorValues() {
    List<SensorBean> sensors = PersistenceFactory.getService().getAssignedSensors();
    for (SensorBean sb : sensors) {
      if (this.railwayController.isAutoModeActive()) {
        JCS.getJcsCommandStation().getSensorStatus(sb);
      } else {
        break;
      }
    }
  }

  void registerAllSensors() {
    //Use only assigned sensors, ignore sensors which are not assigned to a Tile
    //First refresh the sensors...
    refreshAllSensorValues();

    List<SensorBean> sensors = PersistenceFactory.getService().getAssignedSensors();
    int cnt = 0;
    for (SensorBean sb : sensors) {
      Integer key = sb.getId();
      if (!railwayController.sensorHandlers.containsKey(key)) {
        SensorListener seh = new SensorListener(key);
        sensorListeners.add(seh);
        cnt++;
        //Register with a command station
        JCS.getJcsCommandStation().addSensorEventListener(key, seh);
        //Logger.trace("Added handler " + cnt + " for sensor " + key);
      }
    }
    Logger.trace("Registered " + sensors.size() + " sensor event handlers");
  }

  void unRegisterAllSensors() {
    for (SensorListener seh : this.sensorListeners) {
      JCS.getJcsCommandStation().removeSensorEventListener(seh.getSensorId(), seh);
    }
    Logger.trace("Unregistered " + sensorListeners.size() + " sensor event handlers");
    this.sensorListeners.clear();
  }

  @Override
  public void run() {
    running = true;

    long threadWaitMillis = Long.parseUnsignedLong(System.getProperty("autopilot.thread.wait.millis", "10000"));

    Logger.debug("Autopilot thread wait time: " + threadWaitMillis + " ms.");

    registerAllSensors();
    this.railwayController.prepareAllDispatchers();

    restoreLocomotiveFunctions();

    Logger.trace("Autopilot Started. There are " + this.railwayController.dispatchers.size() + " Dispatchers created...");

    for (AutoPilotStatusListener asl : this.railwayController.autoPilotStatusListeners) {
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
    boolean dispatchersRunning = this.railwayController.isADispatcherRunning();

    Logger.trace("Try to finish all dispatchers. There are " + this.railwayController.getRunningDispatcherCount() + " Dispatchers running...");

    if (dispatchersRunning) {
      //Signal the dispatchers
      Set<Dispatcher> snapshot = new HashSet<>(this.railwayController.dispatchers.values());
      for (Dispatcher ld : snapshot) {
        synchronized (ld) {
          ld.stopLocomotiveAutomode();
        }
      }
    }

    while (dispatchersRunning && now < timeout) {
      dispatchersRunning = this.railwayController.isADispatcherRunning();
      try {
        synchronized (this) {
          wait(10);
        }
      } catch (InterruptedException ex) {
        Logger.trace("Interrupted during dispatcher running check");
      }
      now = System.currentTimeMillis();
    }

    Logger.trace((dispatchersRunning ? "Not " : "") + "All dispatchers stopped in " + ((now - start) / 1000) + " s. There are " + this.railwayController.getRunningDispatcherCount() + " Still running...");

    if (dispatchersRunning) {
      for (Dispatcher ld : this.railwayController.dispatchers.values()) {
        if (ld.isRunning()) {
          Logger.trace("Dispatcher: " + ld.getName() + " in State: " + ld.getStateName() + " is still running...");

        }
      }
    }

    unRegisterAllSensors();
    this.railwayController.sensorHandlers.clear();

    for (AutoPilotStatusListener asl : this.railwayController.autoPilotStatusListeners) {
      asl.statusChanged(running);
    }

    Logger.trace("Autopilot Finished. Notified " + this.railwayController.autoPilotStatusListeners.size() + " Listeners. Power is " + (JCS.getJcsCommandStation().isPowerOn() ? "on" : "off"));
    stopped = true;
  }

  boolean isStopped() {
    return this.stopped;
  }

  private class SensorListener implements SensorEventListener {

    private final Integer sensorId;

    SensorListener(Integer sensorId) {
      this.sensorId = sensorId;
    }

    @Override
    public Integer getSensorId() {
      return sensorId;
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      if (sensorId.equals(event.getSensorId())) {
        railwayController.handleSensorEvent(event);
      }
    }
  }

}
