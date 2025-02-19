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
package jcs.commandStation.autopilot;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.tinylog.Logger;

/**
 *
 * A Handler to Monitor the AutoPilot engine.
 *
 */
public class ActionCommandHandler extends Thread {

  private boolean stop = false;
  private boolean quit = true;

  private final ConcurrentLinkedQueue<AutoPilotActionEvent> eventQueue;

  ActionCommandHandler(ConcurrentLinkedQueue eventQueue) {
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
    setName("AUTOPILOT-COMMAND-HANDLER");

    Logger.trace("AutoPilot ActionCommandHandler Started...");

    while (isRunning()) {
      try {
        AutoPilotActionEvent event = eventQueue.poll();
        if (event != null) {
          switch (event.getActionCommand()) {
            case "start" -> {
              AutoPilot.startAutoMode();
            }
            case "stop" -> {
              AutoPilot.stopAutoMode();
            }
            case "startLocomotive" -> {
              AutoPilot.startDispatcher(event.getLocomotiveBean());
            }
            case "stopLocomotive" -> {
              AutoPilot.stopDispatcher(event.getLocomotiveBean());
            }
            case "startAllLocomotives" -> {
              AutoPilot.startLocomotives();
            }
            case "removeLocomotive" -> {
              AutoPilot.removeDispatcher(event.getLocomotiveBean());
            }
            case "addLocomotive" -> {
              AutoPilot.addDispatcher(event.getLocomotiveBean());
            }
            case "reset" -> {
              AutoPilot.resetStates();
            }
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

}
