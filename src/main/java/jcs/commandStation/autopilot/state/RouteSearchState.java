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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Block;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * Search a (free) Route and choose one
 */
public class RouteSearchState implements DispatcherState {

  private final LocomotiveBean locomotive;

  RouteSearchState(LocomotiveBean locomotive) {
    this.locomotive = locomotive;

  }

  @Override
  public void next(TrainDispatcher locRunner) {
    //Only advance when there is a route
    RouteBean route = searchRoute();
    if (route != null) {
      locRunner.setState(new ReserveRouteState(locomotive, route));
    } else {
      Logger.debug("Can't find a route for " + this.locomotive.getName() + " ...");
      locRunner.setState(this);
    }
  }

  @Override
  public void prev(TrainDispatcher locRunner) {
    locRunner.setState(new IdleState(locomotive));
  }

  @Override
  public void logState() {
    Logger.trace(this.getClass().getSimpleName() + "Searching for a Destination block and route");
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  RouteBean searchRoute() {
    Logger.trace("Search a free route for " + locomotive.getName() + "...");
    BlockBean blockBean = PersistenceFactory.getService().getBlockByLocomotiveId(locomotive.getId());
    //We need to have the "Real" BlockTile to be able to sortout the direction...
    TileBean tileBean = blockBean.getTileBean();
    Block blockTile = (Block) TileFactory.createTile(tileBean);

    String suffix = blockTile.getLocomotiveBlockSuffix();

    Logger.trace("Loc " + locomotive.getName() + " is in block " + blockBean.getId() + ". Going " + locomotive.getDirection() + " towards the " + suffix + " side of the block...");
    //Search for the possible routes
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(blockBean.getId(), suffix);
    Logger.trace("There " + (routes.size() == 1 ? "is" : "are") + " " + routes.size() + " possible route(s)...");
    //Choose randomly the route
    for (int i = 0; i < 10; i++) {
      //Seed a bit....
      getRandomNumber(0, routes.size());
    }
    int rIdx = getRandomNumber(0, routes.size());
    RouteBean route = routes.get(rIdx);
    Logger.trace("Choosen route " + route.toLogString());
    return route;

  }

  public int getRandomNumber(int min, int max) {
    Random random = new Random();
    return random.ints(min, max).findFirst().getAsInt();
  }

}
