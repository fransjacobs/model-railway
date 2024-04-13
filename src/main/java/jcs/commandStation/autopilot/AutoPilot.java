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
import jcs.commandStation.autopilot.state.TrainDispatcher;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.RouteDisplayCallBack;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class AutoPilot {

  private static AutoPilot instance = null;

  private RouteDisplayCallBack callback;

  Map<String, TrainDispatcher> locomotives = Collections.synchronizedMap(new HashMap<>());

  private AutoPilot(RouteDisplayCallBack callback) {
    this.callback = callback;
  }

  public static AutoPilot getInstance(RouteDisplayCallBack callback) {
    if (instance == null) {
      instance = new AutoPilot(callback);
    }
    return instance;
  }

  public void initialize() {
    getOnTrackLocomotives();
  }

  public void startAllLocomotives() {
    List<LocomotiveBean> locs = getOnTrackLocomotives();
    for (LocomotiveBean loc : locs) {
      TrainDispatcher dispatcher = new TrainDispatcher(loc, callback);
      locomotives.put(dispatcher.getName(), dispatcher);
      Logger.debug("Added " + dispatcher.getName());

      DispatcherTestDialog.showDialog(dispatcher);
      //lsm.startLocomotive();
    }

  }

  public void stopAllLocomotives() {
    for (TrainDispatcher lsm : this.locomotives.values()) {
      //lsm.stopLocomotive();
    }

  }

  public void startStopLocomotive(LocomotiveBean locomotiveBean, boolean start) {
    Logger.trace((start ? "Starting" : "Stopping") + " auto drive for " + locomotiveBean.getName());

    if (start) {
      TrainDispatcher dispatcher = new TrainDispatcher(locomotiveBean, callback);

      //LocomotiveStateMachine lsm = new LocomotiveStateMachine(locomotiveBean);
      locomotives.put(dispatcher.getName(), dispatcher);
      Logger.debug("Added " + dispatcher.getName());

      DispatcherTestDialog.showDialog(dispatcher);

      //lsm.startLocomotive();
    } else {
      TrainDispatcher lsm = this.locomotives.get("DP->" + locomotiveBean.getName());
      //lsm.stopLocomotive();
    }

  }

  private List<LocomotiveBean> getOnTrackLocomotives() {
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    //filter..
    List<BlockBean> occupiedBlocks = blocks.stream().filter(t -> t.getLocomotive() != null && t.getLocomotive().getId() != null).collect(Collectors.toList());

    Logger.trace("There " + (occupiedBlocks.size() == 1 ? "is" : "are") + " " + occupiedBlocks.size() + " occupied block(s)");

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

  public static void main(String[] a) {
    AutoPilot ap = new AutoPilot(null);

    ap.startAllLocomotives();

    //ap.startAllLocomotives();
  }

}
