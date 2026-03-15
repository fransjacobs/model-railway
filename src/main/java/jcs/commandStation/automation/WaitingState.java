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
import jcs.entities.BlockBean;
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
    super("Waiting");
  }

  @Override
  void onEnter(Dispatcher dispatcher) {
    super.onEnter(dispatcher);

    // Calculate wait time
    BlockBean blockBean = dispatcher.getDepartureBlock();
    long waitTime = calculateWaitTime(blockBean);

    Logger.debug("Waiting for " + waitTime + " s. Block Random " + blockBean.isRandomWait()
            + " Block max: " + blockBean.getMaxWaitTime());

    if (waitTime > 0) {
      // Initialize scheduler
      ThreadGroup threadGroup = dispatcher.getThreadGroup();
      scheduler = Executors.newSingleThreadScheduledExecutor(runnable
              -> new Thread(threadGroup, runnable, "STM-WAIT->" + dispatcher.getName().toUpperCase()));

      remainingTime = waitTime;
      waitCompleted = false;
      cancelled = false;

      // Schedule countdown task - runs every second
      countdownTask = scheduler.scheduleAtFixedRate(() -> {

        if (!dispatcher.isLocomotiveStarted()) {
          // Automode disabled - cancel waiting
          Logger.debug("Automode is disabled for " + dispatcher.getName() + " Exit " + dispatcher.getStateName());
          cancelled = true;
          stopScheduler();
          return;
        }

        dispatcher.fireStateListeners(name, name, "(" + remainingTime + ")");
        //Logger.trace("Remaining time for " + dispatcher.getName() + " " + remainingTime + " s...");

        remainingTime--;
        if (remainingTime < 0) {
          waitCompleted = true;
          stopScheduler();
        }
      }, 0, 1, TimeUnit.SECONDS); // Initial delay 0, period 1 second
      
      dispatcher.wakeup();

    } else {
      waitCompleted = true;
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
      Logger.trace("Overriding waiting time of " + waitTime + "+s as block: " + blockBean.getId() + " does not have to stop...");
      waitTime = 0;
    }

    return waitTime;
  }
}
