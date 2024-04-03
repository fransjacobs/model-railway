/*
 * Copyright 2023 fransjacobs.
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

import java.util.List;
import java.util.Random;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Block;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * @author fransjacobs
 */
public class LocomotiveStateMachine extends Thread {

  private final LocomotiveBean locomotiveBean;

  private BlockBean blockBean;
  private Block block;

  LocomotiveStateMachine(LocomotiveBean locomotiveBean) {
    this.locomotiveBean = locomotiveBean;
    this.setName("STM->" + locomotiveBean.getName());
  }

//what are the states
  // a loc can be in a blockBean just stayng there: IDLE
  // When The auto drive in initiated a loc in a blok can become active so INIT
  // A lok can set a route PREPARING
  // A loc can start when the route is found and locked PREPARED
  // A lok can start driveing DRIVING
  // A lok has hit the enter blockBean sensor BRAKING
  // A lok has hit the in or occupance sensor STOP
  // A lok has stopped in a blok and is waiting for a new adventure WAIT
  // A lok is decativated will not start again -> IDLE
  // A lok has wiated and can start again -> PREPARING
  // A lok is preparing but cant find a valid route -> WAIT
  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public void run() {
    super.start();
  }

  @Override
  public void start() {
    super.start();
  }

  public void startLocomotive() {
    Logger.trace("Starting " + locomotiveBean.getName() + "...");
    this.blockBean = PersistenceFactory.getService().getBlockByLocomotiveId(locomotiveBean.getId());
    //We need to have the "Real" BlockTile to be able to sortout the direction...
    TileBean tileBean = this.blockBean.getTileBean();
    this.block = (Block) TileFactory.createTile(tileBean);

    String suffix = this.block.getLocomotiveBlockSuffix();

    Logger.trace("Loc " + locomotiveBean.getName() + " is in block " + blockBean.getId() + ". Going " + locomotiveBean.getDirection() + " towards the " + suffix + " side of the block...");
    //Search for the possible routes
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(blockBean.getId(), suffix);
    Logger.trace("There " + (routes.size() == 1 ? "is" : "are") +" "+routes.size()+ " possible route(s)...");
    //Choose randomly the route
    for(int i=0;i<10;i++) {
      //Seed a bit....
      getRandomNumber(0, routes.size());
    }
    int rIdx = getRandomNumber(0, routes.size());
    RouteBean route = routes.get(rIdx);
    Logger.trace("Try to lock route "+route.toLogString());
    
 
  }

  public int getRandomNumber(int min, int max) {
    Random random = new Random();
    return random.ints(min, max).findFirst().getAsInt();
  }

  public void stopLocomotive() {
    Logger.trace("Stopping " + this.locomotiveBean.getName());
  }

}
