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

import jcs.commandStation.autopilot.state.DispatcherTestDialog;
import jcs.commandStation.autopilot.state.TrainDispatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.events.TileEvent;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class AutoPilot {

  private static AutoPilot instance = null;

  private final Map<String, SensorEventHandler> sensorHandlers = Collections.synchronizedMap(new HashMap<>());

  //private final Map<String,SwitchableSensorEventHandler> sensorHandlers = Collections.synchronizedMap(new HashMap<>());
  private final Map<String, TrainDispatcher> locomotives = Collections.synchronizedMap(new HashMap<>());

  private AutoPilot() {
  }

  public static AutoPilot getInstance() {
    if (instance == null) {
      instance = new AutoPilot();
    }
    return instance;
  }

  public void initialize() {
    //getOnTrackLocomotives();
    registerAllSensors();
  }

  public void startAllLocomotives() {
    Logger.trace("Starting automode for all locomotives...");
    List<LocomotiveBean> locs = getOnTrackLocomotives();
    for (LocomotiveBean loc : locs) {
      TrainDispatcher dispatcher = new TrainDispatcher(loc, this);
      locomotives.put(dispatcher.getName(), dispatcher);
      Logger.debug("Added " + dispatcher.getName());
      //dispatcher.start();

      DispatcherTestDialog.showDialog(dispatcher);
    }
  }

  public void stopAllLocomotives() {
    Logger.trace("Stopping automode for all locomotives...");
    for (TrainDispatcher lsm : this.locomotives.values()) {
      lsm.stopRunning();
    }
  }

  public void startStopLocomotive(LocomotiveBean locomotiveBean, boolean start) {
    Logger.trace((start ? "Starting" : "Stopping") + " auto drive for " + locomotiveBean.getName());

    if (start) {
      TrainDispatcher dispatcher = new TrainDispatcher(locomotiveBean, this);
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

  private void handleGhost(SensorEvent event) {
    Logger.warn("Ghost Detected! @ Sensor " + event.getId());
    //Switch power OFF!
    JCS.getJcsCommandStation().switchPower(false);

    //Show the Ghost block
    String gostSensorId = event.getId();
    //to which block does the sensor belong?
    List<BlockBean> blocks = PersistenceFactory.getService().getBlocks();
    for (BlockBean block : blocks) {
      if (block.getMinSensorId().equals(gostSensorId) || block.getPlusSensorId().equals(gostSensorId)) {
        if (event.getSensorBean().isActive()) {
          block.setBlockState(BlockBean.BlockState.GHOST);
        } else {
          block.setBlockState(BlockBean.BlockState.FREE);
        }
        showBlockStatus(block);
        break;
      }
    }
  }

  public void showBlockStatus(BlockBean blockBean) {
    Logger.trace("Show Block " + blockBean.toString());
    TileEvent tileEvent = new TileEvent(blockBean);
    TileFactory.fireTileEventListener(tileEvent);
  }

  private void handleSensorEvent(SensorEvent event) {
    if (event.isChanged()) {
      Logger.trace(event.getId() + " has changed " + event.isChanged());

      if (this.sensorHandlers.containsKey(event.getId())) {
        //there is a handler registered for this id, pass the event through
        SensorEventHandler sh = this.sensorHandlers.get(event.getId());
        sh.handleEvent(event);
      } else {
        //sensor is not registered and thus not expected!
        handleGhost(event);
      }
    }
  }

  private void registerAllSensors() {
    List<SensorBean> sensors = PersistenceFactory.getService().getSensors();
    int cnt = 0;
    for (SensorBean sb : sensors) {
      String key = sb.getId();
      if (!sensorHandlers.containsKey(key)) {
        SensorListener seh = new SensorListener(key, this);
        cnt++;
        //Register with a command station
        JCS.getJcsCommandStation().addSensorEventListener(seh);
        Logger.trace("Added handler " + cnt + " for sensor " + key);
      }
    }
    Logger.trace("Registered " + sensors.size() + " sensor event handlers");
  }

  public synchronized void addHandler(SensorEventHandler handler, String sensoreId) {
    sensorHandlers.put(sensoreId, handler);
  }

  public synchronized void removeHandler(String sensorId) {
    sensorHandlers.remove(sensorId);
  }

  private class SensorListener implements SensorEventListener {

    private final String sensorId;
    private final AutoPilot delegate;

    SensorListener(String sensorId, AutoPilot delegate) {
      this.sensorId = sensorId;
      this.delegate = delegate;
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      if (sensorId.equals(event.getId())) {
        delegate.handleSensorEvent(event);
      }
    }
  }

  public static void main(String[] a) {
    AutoPilot ap = new AutoPilot();
    JCS.getJcsCommandStation().connect();

    ap.registerAllSensors();

    ap.startAllLocomotives();

    //ap.startAllLocomotives();
  }

}
