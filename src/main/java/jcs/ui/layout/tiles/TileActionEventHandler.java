/*
 * Copyright 2025 fransjacobs.
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
package jcs.ui.layout.tiles;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.AccessoryBean;
import org.tinylog.Logger;

/**
 *
 * Handler for actions started from clicking on a tile
 */
class TileActionEventHandler extends Thread {

  private volatile boolean running = true;

  private final BlockingQueue<JCSActionEvent> eventQueue;

  TileActionEventHandler(BlockingQueue<JCSActionEvent> eventQueue) {
    this.eventQueue = eventQueue;
  }

  @SuppressWarnings("unused")
  void quit() {
    this.running = false;
  }

  @SuppressWarnings("unused")
  boolean isRunning() {
    return this.running;
  }

  @Override
  public void run() {
    running = true;
    this.setName("TILE-ACTION-EVENT-HANDLER");

    Logger.trace("Tile ActionEventHandler Started...");

    while (running) {
      try {
        JCSActionEvent actionEvent = eventQueue.poll(100, TimeUnit.MILLISECONDS);
        if (actionEvent != null) {
          Object event = actionEvent.getEventObject();
          if (event instanceof SensorEvent sensorEvent) {
            fireSensorEvent(sensorEvent);
          }
          if (event instanceof AccessoryEvent accessoryEvent) {
            AccessoryBean ab = accessoryEvent.getAccessoryBean();
            JCS.getJcsCommandStation().switchAccessory(ab, ab.getAccessoryValue());
          }
        }
      } catch (InterruptedException ex) {
        Logger.error(ex);
        Thread.currentThread().interrupt();
        break;
      }
    }
    Logger.trace("Tile ActionEventHandler Stopped...");
  }

  private void fireSensorEvent(SensorEvent sensorEvent) {
    //Logger.trace("Firing Sensor Action " + sensorEvent.getIdString() + " -> " + sensorEvent.isActive());
    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
    for (FeedbackController fbc : acl) {
      fbc.fireAllSensorEventsListeners(sensorEvent);
    }
  }

}
