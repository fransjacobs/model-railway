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

import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 * Departing state of the Auto Drive State machine.<br>
 * This state is entered when a valid route is found and reserved.<br>
 * This state will start the locomotive by sending the direction and start velocity commands to the command station.<br>
 * Then it will automatically transition to the running state
 */
class DepartingState extends AbstractState {

  DepartingState() {
    super("Departing");
  }

  @Override
  AbstractState execute() {
    LocomotiveBean locomotive = PersistenceFactory.getService().getLocomotive(dispatcher.getLocomotiveId());
    BlockBean departureBlock = dispatcher.getDepartureBlock();
    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    departureBlock.setBlockState(BlockBean.BlockState.OUTBOUND);
    destinationBlock.setBlockState(BlockBean.BlockState.LOCKED);

    Logger.trace("Starting " + locomotive.getName() + " Direction " + locomotive.getDirection());

    //TODO: Is it always neccessary to set the locomotive direction?
    //First time starting as current velocity is zero, ensure the direction is right
    if (locomotive.getVelocity() == 0) {
      dispatcher.changeLocomotiveDirection(locomotive.getDirection());
    }

    //Speed to ~75% or speed 3
    Integer speed3 = locomotive.getSpeedThree();
    if (speed3 == null || speed3 == 0) {
      speed3 = 75;
    }

    int fullscale = locomotive.getTachoMax();
    double velocity = (speed3 / (double) fullscale) * 1000;
    dispatcher.changeLocomotiveVelocity(velocity);

    locomotive.setVelocity((int) velocity);
    departureBlock.setLocomotive(locomotive);
    //departureBlock.setLogicalDirection(locomotive.getDirection().getDirection());
    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showBlockState(destinationBlock);

//    StationBean station = dispatcher.getStation(departureBlock);
//    if (station != null) {
//      StationBlockBean sbb = station.getStationBlockBean(departureBlock);
//      //Set the depareset arrival time
//      sbb.setLastUpdated(new Date());
//      PersistenceFactory.getService().persist(station);
//    }
    return new RunningState();
  }

  @Override
  void onExit() {

  }

  @Override
  boolean canStopLocomotive() {
    return false;
  }

}
