/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.commandStation;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.LocomotiveDirectionEventListener;
import jcs.commandStation.events.LocomotiveFunctionEventListener;
import jcs.commandStation.events.LocomotiveSpeedEventListener;
import jcs.commandStation.events.PowerEventListener;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.CommandStationBean;
import org.tinylog.Logger;
import jcs.commandStation.events.ConnectionEventListener;
import jcs.commandStation.events.MeasurementEventListener;

public abstract class AbstractController implements GenericController {

  protected CommandStationBean commandStationBean;
  protected boolean connected = false;

  protected final List<PowerEventListener> powerEventListeners;
  protected final List<AccessoryEventListener> accessoryEventListeners;
  protected final List<SensorEventListener> sensorEventListeners;

  protected final List<LocomotiveFunctionEventListener> locomotiveFunctionEventListeners;
  protected final List<LocomotiveDirectionEventListener> locomotiveDirectionEventListeners;
  protected final List<LocomotiveSpeedEventListener> locomotiveSpeedEventListeners;

  protected final List<ConnectionEventListener> connectionEventListeners;

  protected final List<MeasurementEventListener> measurementEventListeners;

  protected ExecutorService executor;

  protected int defaultSwitchTime;

  protected boolean power;
  protected boolean debug = false;
  protected boolean virtual = false;

  public AbstractController(CommandStationBean commandStationBean) {
    this(System.getProperty("skip.commandStation.autoconnect", "true").equalsIgnoreCase("true"), commandStationBean);
  }

  public AbstractController(boolean autoConnect, CommandStationBean commandStation) {
    this.commandStationBean = commandStation;

    debug = System.getProperty("message.debug", "false").equalsIgnoreCase("true");
    defaultSwitchTime = Integer.getInteger("default.switchtime", 100);

    powerEventListeners = new LinkedList<>();
    sensorEventListeners = new LinkedList<>();
    accessoryEventListeners = new LinkedList<>();

    locomotiveFunctionEventListeners = new LinkedList<>();
    locomotiveDirectionEventListeners = new LinkedList<>();
    locomotiveSpeedEventListeners = new LinkedList<>();

    connectionEventListeners = new LinkedList<>();

    measurementEventListeners = new LinkedList<>();

    if (this.commandStationBean != null) {
      this.virtual = commandStationBean.isVirtual();
    }

    executor = Executors.newCachedThreadPool();
  }

  @Override
  public CommandStationBean getCommandStationBean() {
    return commandStationBean;
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  @Override
  public void setVirtual(boolean flag) {
    this.virtual = flag;
    Logger.debug("Switching Virtual Mode " + (flag ? "On" : "Off"));
  }

  @Override
  public boolean isVirtual() {
    return this.virtual;
  }

  @Override
  public void addConnectionEventListener(ConnectionEventListener listener) {
    this.connectionEventListeners.add(listener);
  }

  @Override
  public void removeConnectionEventListener(ConnectionEventListener listener) {
    this.connectionEventListeners.remove(listener);
  }

  public boolean isPower() {
    return this.power;
  }

  public void addPowerEventListener(PowerEventListener listener) {
    this.powerEventListeners.add(listener);
  }

  public void removePowerEventListener(PowerEventListener listener) {
    this.powerEventListeners.remove(listener);
  }

  public void addSensorEventListener(SensorEventListener listener) {
    this.sensorEventListeners.add(listener);
  }

  public void removeSensorEventListener(SensorEventListener listener) {
    this.sensorEventListeners.remove(listener);
  }

  public void addAccessoryEventListener(AccessoryEventListener listener) {
    this.accessoryEventListeners.add(listener);
  }

  public void removeAccessoryEventListener(AccessoryEventListener listener) {
    this.accessoryEventListeners.remove(listener);
  }

  public void addLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    this.locomotiveFunctionEventListeners.add(listener);
  }

  public void removeLocomotiveFunctionEventListener(LocomotiveFunctionEventListener listener) {
    this.locomotiveFunctionEventListeners.remove(listener);
  }

  public void addLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    this.locomotiveDirectionEventListeners.add(listener);
  }

  public void removeLocomotiveDirectionEventListener(LocomotiveDirectionEventListener listener) {
    this.locomotiveDirectionEventListeners.remove(listener);
  }

  public void addLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    this.locomotiveSpeedEventListeners.add(listener);
  }

  public void removeLocomotiveSpeedEventListener(LocomotiveSpeedEventListener listener) {
    this.locomotiveSpeedEventListeners.remove(listener);
  }

  public void addMeasurementEventListener(MeasurementEventListener listener) {
    this.measurementEventListeners.add(listener);
  }

  public void removeMeasurementEventListener(MeasurementEventListener listener) {
    this.measurementEventListeners.remove(listener);
  }

  protected void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

}
