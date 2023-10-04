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
package jcs.controller.autopilot;

import java.util.List;
import java.util.stream.Collectors;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class AutoPilot {

  //Fort started a list of locomotives is needed which are on the track ie assigned to a Tile
  private List<LocomotiveBean> getOnTrackLocomotives() {
    //direct query call or derive?
    //Loc can only be in a block, so strart there
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    Logger.trace("There are " + blocks.size() + " blocks");

    //filter..
    List<BlockBean> occupiedBlocks = blocks.stream().filter(t -> t.getLocomotive() != null && t.getLocomotive().getId() != null).collect(Collectors.toList());

    Logger.trace("There are " + occupiedBlocks.size() + " occupied blocks");

    return null;
  }

  public static void main(String[] a) {
    AutoPilot ap = new AutoPilot();

    ap.getOnTrackLocomotives();
  }

}
