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

import java.awt.Color;
import jcs.JCS;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class ArrivalState extends DispatcherState {

  private boolean locomotiveBraking = false;

  public ArrivalState(TrainDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    Logger.trace("canAdvanceState: " + canAdvanceToNextState);
    if (canAdvanceToNextState) {
      DispatcherState newState = new ArrivedState(this.dispatcher);
      newState.setRunning(running);
      locRunner.setDispatcherState(newState);
    } else {
      locRunner.setDispatcherState(this);
    }
  }

  @Override
  public void execute() {
//    while (!dispatcher.isEnterDestinationBlock()) {
//      try {
//        //wait
//        //this.wait();
//        Thread.sleep(1000L);
//      } catch (InterruptedException ex) {
//        Logger.trace(ex);
//      }
//    }

    //When the arrival event goes of this is executed
    if (!locomotiveBraking) {
      LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
      BlockBean destinationBlock = dispatcher.getDestinationBlock();
      Logger.debug("Locomotive " + locomotive.getName() + " has entered the destination " + destinationBlock.getDescription() + ". Slowing down....");

      JCS.getJcsCommandStation().changeLocomotiveSpeed(100, locomotive);

      BlockBean departureBlock = this.dispatcher.getDepartureBlock();
      departureBlock.setBlockState(BlockBean.BlockState.LEAVING);
      destinationBlock.setBlockState(BlockBean.BlockState.ARRIVING);

      PersistenceFactory.getService().persist(departureBlock);
      PersistenceFactory.getService().persist(destinationBlock);

      dispatcher.showBlockState(departureBlock);
      dispatcher.showBlockState(destinationBlock);

      RouteBean route = dispatcher.getRouteBean();
      dispatcher.showRoute(route, Color.magenta);

      locomotiveBraking = true;
    } else {
      Logger.trace("Waiting for the arrived(in) event from SensorId: " + dispatcher.getInSensorId() + " Is In Sensor triggered: " + dispatcher.isInDestinationBlock());
    }
    this.canAdvanceToNextState = this.dispatcher.isInDestinationBlock();
    Logger.trace("Can advance to next state: " + canAdvanceToNextState);
  }

}
