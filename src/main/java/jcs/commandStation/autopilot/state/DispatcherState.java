/*
 * Copyright 2024 frans.
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
package jcs.commandStation.autopilot.state;

import java.util.List;
import jcs.entities.BlockBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public abstract class DispatcherState {

  protected final TrainDispatcher dispatcher;

  protected boolean running;

  protected int waitTime;

  protected boolean canAdvanceToNextState;

  protected DispatcherState(TrainDispatcher trainDispatcher) {
    this.dispatcher = trainDispatcher;
  }

  public abstract void next(TrainDispatcher dispatcher);

  public abstract void execute();

  public int getWaitTime() {
    return waitTime;
  }

  public boolean canAdvanceToNextState() {
    return canAdvanceToNextState;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  public void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

//  protected void refreshBlockTiles() {
//    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
//
//    Logger.trace("Refreshing " + blocks.size() + " block tiles...");
//    for (BlockBean b : blocks) {
//      BlockEvent be = new BlockEvent(b);
//      //JCS.getJcsCommandStation().fireBlockEventListeners(be);
//    }
//  }

}
