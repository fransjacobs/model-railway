/*
 * Copyright 2023 frans.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Block;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class AutoPilot {

  private static AutoPilot instance = null;

  Map<Integer, LocomotiveBean> syncMap = Collections.synchronizedMap(new HashMap<>());

  private AutoPilot() {

  }

  public static AutoPilot getInstance() {
    if (instance == null) {
      instance = new AutoPilot();
    }
    return instance;
  }

  public void initialize() {
    getOnTrackLocomotives();
  }

  public void startAllLocomotives(boolean start) {
    Logger.trace((start ? "Starting" : "Stopping") + " auto drive for All locomotives on the track");

    getOnTrackLocomotives();
  }

  public void startLocomotive(LocomotiveBean locomotiveBean, boolean start) {
    Logger.trace((start ? "Starting" : "Stopping") + " auto drive for " + locomotiveBean.getName());
  }

  private List<LocomotiveBean> getOnTrackLocomotives() {
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    //filter..
    List<BlockBean> occupiedBlocks = blocks.stream().filter(t -> t.getLocomotive() != null && t.getLocomotive().getId() != null).collect(Collectors.toList());

    Logger.trace("There are " + occupiedBlocks.size() + " occupied blocks");

    List<LocomotiveBean> activeLocomotives = new ArrayList<>();
    for (BlockBean occupiedBlock : occupiedBlocks) {
      LocomotiveBean dbl = PersistenceFactory.getService().getLocomotive(occupiedBlock.getLocomotiveId());
      if (dbl != null) {
        activeLocomotives.add(dbl);
      }
    }

    if (Logger.isDebugEnabled()) {
      Logger.trace("There are " + activeLocomotives.size() + " Locomotives on the track: ");
      for (LocomotiveBean loc : activeLocomotives) {
        Logger.trace(loc);
      }
    }
    return activeLocomotives;
  }

  public RouteBean findRoute(BlockBean from, LocomotiveBean locomotive) {
    Block block = (Block) TileFactory.createTile(PersistenceFactory.getService().getTileBean(from.getTileId()), true, true);
    String suffix = block.getLocomotiveBlockSuffix();
    Logger.trace("Trying to find routes for: " + block.getId() + " suffix " + suffix + " Loc: " + locomotive.getName() + " Direction: " + locomotive.getDirection());
    List<RouteBean> routes = PersistenceFactory.getService().getRoutes(from.getTileId(), suffix);

    Logger.trace("Found " + routes.size() + " routes");

    if (routes.isEmpty()) {
      return null;
    } else {
      //Lets use the first; may be later randomize
      return routes.get(0);
    }
  }

  public static void main(String[] a) {
    AutoPilot ap = new AutoPilot();

    ap.getOnTrackLocomotives();
  }

}
