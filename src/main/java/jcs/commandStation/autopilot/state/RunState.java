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

import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class RunState extends DispatcherState {

  private SensorListener enterListener;
  private SensorListener arrivalListener;
  
  private BlockBean departureBlock;
  private BlockBean destinationBlock;
  
  boolean arrived = false;

  RunState(LocomotiveBean locomotive, RouteBean route) {
    super(locomotive, route);
  }

  @Override
  public void next(TrainDispatcher locRunner) {
    if (canAdvanceState) {
      locRunner.setState(new WaitState(locomotive));
    } else {
      locRunner.setState(this);
    }
  }

  @Override
  public void prev(TrainDispatcher locRunner) {
    locRunner.setState(new ReserveRouteState(locomotive, route));
  }

  @Override
  void onHalt(TrainDispatcher dispatcher) {
    Logger.debug("HALT!");
  }

  @Override
  public boolean performAction() {
    if(arrived) {
      return canAdvanceState;
    }
    //Which sensors do we need to watch
    String departureTileId = route.getFromTileId();
    String destinationTileId = route.getToTileId();
    //From which side on the block is the train expected to arrive?
    String arrivalSuffix = route.getToSuffix();

    Logger.trace("Destination tile: " + destinationTileId + " Arrival on the " + arrivalSuffix + " side of the block");
    departureBlock = PersistenceFactory.getService().getBlockByTileId(departureTileId);
    destinationBlock = PersistenceFactory.getService().getBlockByTileId(destinationTileId);

    SensorBean enterSensor;
    if ("+".equals(arrivalSuffix)) {
      enterSensor = destinationBlock.getPlusSensorBean();
    } else {
      enterSensor = destinationBlock.getMinSensorBean();
    }

    enterListener = new SensorListener(enterSensor, true, this);

    SensorBean inSensor;
    if ("-".equals(arrivalSuffix)) {
      inSensor = destinationBlock.getPlusSensorBean();
    } else {
      inSensor = destinationBlock.getMinSensorBean();
    }

    arrivalListener = new SensorListener(inSensor, false, this);
    
    Logger.trace("Register the enter sensor listener ");
    JCS.getJcsCommandStation().addSensorEventListener(enterListener);
    JCS.getJcsCommandStation().addSensorEventListener(arrivalListener);
  
    Logger.debug("Enter Sensor: " + enterSensor.getName() + " [" + enterSensor.getId() + "] In Sensor: " + inSensor.getName() + " [" + inSensor.getId() + "]");
    
    //Start The locomotive runner thread and start a loco

    return canAdvanceState;
  }

  private void onEnter() {
    Logger.debug("Train has entered the destination block. Slow down");
    
    
  }

  private void onArrival() {
    Logger.debug("Train has arrived in the destination block");
    
    //Stop the loco
    //set the loco in the destination block
    //remove the loco from the source block
    
    departureBlock.setLocomotive(null);
    destinationBlock.setLocomotive(locomotive);
    
    PersistenceFactory.getService().persist(departureBlock);
    PersistenceFactory.getService().persist(destinationBlock);
     
    JCS.getJcsCommandStation().removeSensorEventListener(enterListener);
    JCS.getJcsCommandStation().removeSensorEventListener(arrivalListener);
    
    route.setLocked(false);
    PersistenceFactory.getService().persist(route);
    this.route = null;
    canAdvanceState = true;
    arrived = true;
  }

  private class SensorListener implements SensorEventListener {

    private final Integer deviceId;
    private final Integer contactId;
    private final boolean enter;
    private final RunState runState;

    SensorListener(SensorBean sensor, boolean enter, RunState runState) {
      this.deviceId = sensor.getDeviceId();
      this.contactId = sensor.getContactId();
      this.enter = enter;
      this.runState = runState;
      Logger.trace("deviceId: "+deviceId+" contactId: "+contactId+" enter: "+enter);
    }

    @Override
    public void onSensorChange(SensorEvent event) {
      SensorBean sensor = event.getSensorBean();
      
      if (deviceId.equals(sensor.getDeviceId()) && contactId.equals(sensor.getContactId())) {
        Logger.trace("-> deviceId: "+deviceId+" contactId: "+contactId+" enter: "+enter);
        if (sensor.isActive()) {
          if (enter) {
            runState.onEnter();
          } else {
            runState.onArrival();
          }
        }
      }
    }
  }

}
