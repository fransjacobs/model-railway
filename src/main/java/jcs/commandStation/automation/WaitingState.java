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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import static jcs.commandStation.automation.AbstractState.State.WAIT;
import static jcs.commandStation.automation.RailController.TAG;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import org.tinylog.Logger;

/**
 * The Waiting State is used when a Train is in a block and must wait, or after a route is searched and nothing is found.
 */
class WaitingState
        extends AbstractState {

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
      Logger.tag(TAG).warn("##@@! " + dispatcher.getName() + " Velocity " + locomotiveBean.getVelocity() + " is > 0 !");
      dispatcher.changeLocomotiveVelocity(0);
    }

    Logger.tag(TAG).debug("Locomotive " + locomotiveBean.getName() + " Direction: " + locomotiveBean.getDirection().getDirection() + " start waiting in block " + blockBean.getId() + " logicalDir: " + blockBean.getLogicalDirection() + " Arrived at " + blockBean.getArrivalSuffix() + " side.");

    remainingTime = calculateWaitTime(blockBean);
    long waitTime = remainingTime;
    Logger.tag(TAG).trace("Waiting for " + remainingTime + " s. Block Random " + blockBean.isRandomWait() + " Block max: " + blockBean.getMaxWaitTime());

    if (remainingTime > 0) {
      // Initialize scheduler
      ThreadGroup threadGroup = dispatcher.getThreadGroup();
      scheduler = Executors.newSingleThreadScheduledExecutor(runnable
              -> new Thread(threadGroup, runnable, "STM-WAIT->" + dispatcher.getName().toUpperCase()));

      // Schedule countdown task - runs every second
      countdownTask = scheduler.scheduleAtFixedRate(() -> {

        if (!dispatcher.isLocomotiveStarted()) {
          // Automode disabled - cancel waiting
          Logger.tag(TAG).debug("Automode is disabled for " + dispatcher.getName() + " Exit " + dispatcher.getStateName());
          cancelled = true;
          dispatcher.fireStateListeners(getName(), getName(), " (-)");

          stopScheduler();
          dispatcher.wakeup();
          return;
        }

        dispatcher.fireStateListeners(getName(), getName(), " (" + Math.max(remainingTime, 0) + ")");
        //Logger.trace("Remaining time for " + dispatcher.getName() + " " + remainingTime + " s...");

        remainingTime--;
        if (remainingTime <= 0) {
          waitCompleted = true;
          Logger.tag(TAG).trace(dispatcher.getName() + " waittime of " + waitTime + " s " + (waitCompleted ? "completed" : "waiting") + "...");
          stopScheduler();
          dispatcher.wakeup();
        }
      }, 1, 1, TimeUnit.SECONDS); // Initial delay 0, period 1 second

    } else {
      waitCompleted = true;
      Logger.tag(TAG).trace(dispatcher.getName() + " wakeup (2)...");
      dispatcher.wakeup();
    }
  }

  @Override
  AbstractState execute() {
    // Just check the status - actual waiting happens in scheduler
    if (waitCompleted) {
      Logger.tag(TAG).trace("Wait completed for " + dispatcher.getName() + "...");

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
    ScheduledFuture<?> task = countdownTask;
    if (task != null && !task.isCancelled()) {
      task.cancel(false);
    }

    ScheduledExecutorService executor = scheduler;
    if (executor != null && !executor.isShutdown()) {
      executor.shutdown();
    }
  }

  private long calculateWaitTime(BlockBean blockBean) {

    Integer configuredMin = blockBean.getMinWaitTime();
    Integer configuredMax = blockBean.getMaxWaitTime();

    int defaultMax = Integer.getInteger("default.max.waittime", 20);

    int minWait = configuredMin != null ? configuredMin : 0;
    int maxWait = configuredMax != null ? configuredMax : defaultMax;

    long waitTime;

    if (blockBean.isRandomWait()) {
      if (maxWait > minWait) {
        waitTime = ThreadLocalRandom.current().nextInt(minWait, maxWait + 1);
      } else {
        waitTime = minWait;
      }

    } else if (configuredMin != null) {
      waitTime = configuredMin;

    } else if (configuredMax != null) {
      waitTime = configuredMax;

    } else {
      waitTime = defaultMax;
    }

    if (!blockBean.isAlwaysStop()) {
      waitTime /= 2;
    }

    return waitTime;
  }
}
