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

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static jcs.commandStation.automation.AbstractState.State.WAIT;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 * The Waiting State is used when a Train is in a block and must wait, or after a route is searched and nothing is found.
 */
class WaitingState extends AbstractState {

  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> countdownTask;
  private volatile long remainingTime;
  private volatile boolean waitCompleted = false;
  private volatile boolean cancelled = false;

  WaitingState() {
    super(WAIT);
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    waitCompleted = false;
    cancelled = false;

    // Calculate wait time
    BlockBean blockBean = dispatcher.getDepartureBlock();
    LocomotiveBean locomotiveBean = dispatcher.getLocomotiveBean();

    //This check should not be necessary....
    if (locomotiveBean.getVelocity() > 0) {
      Logger.warn("##@@! " + dispatcher.getName() + " Velocity " + locomotiveBean.getVelocity() + " is > 0 !");
      dispatcher.changeLocomotiveVelocity(0);
    }

    Logger.debug("Locomotive " + locomotiveBean.getName() + " Direction: " + locomotiveBean.getDirection().getDirection() + " start waiting in block " + blockBean.getId() + " logicalDir: " + blockBean.getLogicalDirection() + " Arrived at " + blockBean.getArrivalSuffix() + " side.");

    remainingTime = calculateWaitTime(blockBean);
    long waitTime = remainingTime;
    Logger.trace("Waiting for " + remainingTime + " s. Block Random " + blockBean.isRandomWait() + " Block max: " + blockBean.getMaxWaitTime());

    if (remainingTime > 0) {
      // Initialize scheduler
      ThreadGroup threadGroup = dispatcher.getThreadGroup();
      scheduler = Executors.newSingleThreadScheduledExecutor(runnable
              -> new Thread(threadGroup, runnable, "STM-WAIT->" + dispatcher.getName().toUpperCase()));

      // Schedule countdown task - runs every second
      countdownTask = scheduler.scheduleAtFixedRate(() -> {

        if (!dispatcher.isLocomotiveStarted()) {
          // Automode disabled - cancel waiting
          Logger.debug("Automode is disabled for " + dispatcher.getName() + " Exit " + dispatcher.getStateName());
          cancelled = true;
          stopScheduler();

          dispatcher.fireStateListeners(getName(), getName(), " (-)");

          return;
        }

        dispatcher.fireStateListeners(getName(), getName(), " (" + remainingTime + ")");
        //Logger.trace("Remaining time for " + dispatcher.getName() + " " + remainingTime + " s...");

        remainingTime--;
        if (remainingTime < 0) {
          waitCompleted = true;
          stopScheduler();
          Logger.trace(dispatcher.getName() + " waittime of " + waitTime + " s " + (waitCompleted ? "completed" : "waiting") + "...");
          dispatcher.wakeup();
        }
      }, 0, 1, TimeUnit.SECONDS); // Initial delay 0, period 1 second

    } else {
      waitCompleted = true;
      Logger.trace(dispatcher.getName() + " wakeup (2)...");
      dispatcher.wakeup();
    }
  }

  @Override
  AbstractState execute() {
    // Just check the status - actual waiting happens in scheduler
    if (waitCompleted) {
      Logger.trace("Wait completed for " + dispatcher.getName() + "...");

      return new PrepareRouteState();
    } else if (cancelled || !dispatcher.isLocomotiveStarted()) {
      return new IdleState();
    }

    // Stay in current state while waiting
    return this;
  }

  @Override
  void onExit() {
    stopScheduler();
  }

  @Override
  boolean canStopLocomotive() {
    return true;
  }

  private void stopScheduler() {
    if (countdownTask != null && !countdownTask.isCancelled()) {
      countdownTask.cancel(false);
    }
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  private long calculateWaitTime(BlockBean blockBean) {

    int minWait = blockBean.getMinWaitTime();
    int maxWait;

    if (blockBean.getMaxWaitTime() != null) {
      maxWait = blockBean.getMaxWaitTime();
    } else {
      maxWait = Integer.getInteger("default.max.waittime", 20);
    }

    long waitTime;

    if (blockBean.isRandomWait()) {
      Random random = new Random();
      // Seed a bit....
      for (int i = 0; i < 10; i++) {
        random.ints(minWait, maxWait).findFirst();
      }
      waitTime = random.ints(minWait, maxWait).findFirst().getAsInt();
    } else {
      if (blockBean.getMaxWaitTime() != null && blockBean.getMinWaitTime() == null) {
        waitTime = blockBean.getMaxWaitTime();
      } else {
        if (blockBean.getMinWaitTime() != null) {
          waitTime = minWait;
        } else {
          waitTime = maxWait;
        }
      }
    }

    //In case the train had to stop because the next route could not yet be found wait shorter
    //then the block waittime
    boolean alwayStop = blockBean.isAlwaysStop();
    if (!alwayStop) {
      //waitTime = Integer.getInteger("default.no.stop.waittime", 1);
      //Use a shorter time...
      waitTime = waitTime / 2;
    }

    return waitTime;
  }
}
