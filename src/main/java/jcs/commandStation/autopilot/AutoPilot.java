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
import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class AutoPilot {

  private static AutoPilot instance = null;

  private final Map<String, SensorEventHandler> sensorHandlers = Collections.synchronizedMap(new HashMap<>());

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
    //registerAllSensors();
  }

  public void startAllLocomotives() {
    List<LocomotiveBean> locs = getOnTrackLocomotives();
    for (LocomotiveBean loc : locs) {
      TrainDispatcher dispatcher = new TrainDispatcher(loc);
      locomotives.put(dispatcher.getName(), dispatcher);
      Logger.debug("Added " + dispatcher.getName());
      //dispatcher.start();

      DispatcherTestDialog.showDialog(dispatcher);
    }
  }

  public void stopAllLocomotives() {
    for (TrainDispatcher lsm : this.locomotives.values()) {
      lsm.stopRunning();
    }
  }

  public void startStopLocomotive(LocomotiveBean locomotiveBean, boolean start) {
    Logger.trace((start ? "Starting" : "Stopping") + " auto drive for " + locomotiveBean.getName());

    if (start) {
      TrainDispatcher dispatcher = new TrainDispatcher(locomotiveBean);

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

  private void ghostDetected(SensorEvent event) {
    Logger.debug("Sensor " + event.getSensorBean().getId() + " active: " + event.getSensorBean().isActive());
  }

  public SensorEventHandler getSensorEventHandler(String sensorId) {
    return this.sensorHandlers.get(sensorId);
  }

  private void registerAllSensors() {
    List<SensorBean> sensors = PersistenceFactory.getService().getSensors();

    int cnt =0;
    for (SensorBean sb : sensors) {
      String key = sb.getId();
      GostHandler gh = new GostHandler(this, key);
      cnt++;

      SensorEventHandlerImpl sensorListener = new SensorEventHandlerImpl(gh, key);
      JCS.getJcsCommandStation().addSensorEventListener(sensorListener);
      sensorHandlers.put(key, gh);
      Logger.trace("Added handler "+cnt+" for sensor "+key);
    }
    Logger.trace("Registered " + sensors.size() + " sensor event handlers");
  }

  private class GostHandler implements SensorEventHandler {

    private final AutoPilot autoPilot;
    private final String sensorId;

    GostHandler(AutoPilot autoPilot, String sensorId) {
      this.autoPilot = autoPilot;
      this.sensorId = sensorId;
    }

    @Override
    public void handleSensorEvent(SensorEvent event) {
      if (sensorId.equals(event.getSensorBean().getId())) {
        autoPilot.ghostDetected(event);
      }
    }

    @Override
    public String getSensorId() {
      return sensorId;
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
