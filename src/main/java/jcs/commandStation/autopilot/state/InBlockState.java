/*
 * Copyright 2024 Frans Jacobs.
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

import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
class InBlockState extends DispatcherState {

  @Override
  DispatcherState execute(Dispatcher dispatcher) {
    LocomotiveBean locomotive = dispatcher.getLocomotiveBean();
    dispatcher.changeLocomotiveVelocity(locomotive, 0);

    BlockBean destinationBlock = dispatcher.getDestinationBlock();
    BlockBean departureBlock = dispatcher.getDepartureBlock();

    Logger.trace("Locomotive " + locomotive.getName() + " has arrived in destination " + destinationBlock.getDescription() + ". Stopping....");

    destinationBlock.setLocomotive(locomotive);
    destinationBlock.setBlockState(BlockBean.BlockState.OCCUPIED);
    destinationBlock.setReverseArrival(departureBlock.isReverseArrival());
    destinationBlock.setArrivalSuffix(dispatcher.getRouteBean().getToSuffix());

    boolean alwaysStop = destinationBlock.isAlwaysStop();

    departureBlock.setBlockState(BlockBean.BlockState.FREE);
    departureBlock.setLocomotive(null);
    departureBlock.setReverseArrival(false);
    departureBlock.setArrivalSuffix("-");

    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);

    RouteBean route = dispatcher.getRouteBean();
    route.setLocked(false);
    PersistenceFactory.getService().persist(route);

    dispatcher.showBlockState(departureBlock);
    dispatcher.showBlockState(destinationBlock);

    Dispatcher.resetRoute(route);
    dispatcher.setRouteBean(null);

    //Wait a short while...
    pause(250);

    DispatcherState newState;
    if (dispatcher.isLocomotiveAutomodeOn()) {
      if (alwaysStop) {
        newState = new WaitState();
      } else {
        //Do not wait if posible
        newState = new PrepareRouteState();
      }
    } else {
      newState = new IdleState();
    }

    return newState;
  }

}
