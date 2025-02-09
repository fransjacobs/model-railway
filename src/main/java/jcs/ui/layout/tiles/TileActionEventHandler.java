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
import java.util.concurrent.ConcurrentLinkedQueue;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.JCSActionEvent;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.AccessoryBean;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
class TileActionEventHandler extends Thread {

  private boolean stop = false;
  private boolean quit = true;

  private final ConcurrentLinkedQueue<JCSActionEvent> eventQueue;

  TileActionEventHandler(ConcurrentLinkedQueue eventQueue) {
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
    this.quit = false;
    this.setName("TILE-ACTION-EVENT-HANDLER");

    Logger.trace("Tile ActionEventHandler Started...");

    while (isRunning()) {
      try {
        JCSActionEvent event = eventQueue.poll();
        if (event != null) {
          if (event instanceof SensorEvent sensorEvent) {
            fireSensorEvent(sensorEvent);
          }
          if (event instanceof AccessoryEvent accessoryEvent) {
            switchChanged(accessoryEvent);
          }

        } else {
          //lets sleep for a while
          synchronized (this) {
            wait(10000);
          }
        }

      } catch (InterruptedException ex) {
        Logger.error(ex);
      }
    }

    stop = true;
    Logger.trace("Tile ActionEventHandler Stopped...");
  }

  private void fireSensorEvent(SensorEvent sensorEvent) {
    //Logger.trace("Firing Sensor Action " + sensorEvent.getId() + " -> " + sensorEvent.isActive());
    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
    for (FeedbackController fbc : acl) {
      fbc.fireSensorEventListeners(sensorEvent);
    }
  }

  private void switchChanged(AccessoryEvent accessoryEvent) {
    AccessoryBean ab = accessoryEvent.getAccessoryBean();
    JCS.getJcsCommandStation().switchAccessory(ab, ab.getAccessoryValue());
  }

}
