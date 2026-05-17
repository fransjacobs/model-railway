/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.commandStation.esu.ecos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import jcs.commandStation.entities.MeasurementBean;
import jcs.commandStation.esu.ecos.entities.EcosBooster;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.MeasurementEventListener;
import org.tinylog.Logger;

/**
 *
 * BoosterManager (id=27)
 */
public class BoosterManager { //implements ConnectionEventListener {

  public static final int ID = 27;

  private final EsuEcosCommandStationImpl ecosCommandStation;
  private final Map<String, EcosBooster> boosters;
  private Integer size;

  private final SortedMap<Long, Map<String, MeasurementBean>> measurements;

  private final List<MeasurementEventListener> measurementEventListeners;

  private final int maxMeasurementEntries;

  //private MeasurementTask measurementTask;
  //private Timer measurementTimer;

  private EcosBooster internal;

  BoosterManager(EsuEcosCommandStationImpl esuEcosCommandStationImpl, EcosMessage message) {
    this.ecosCommandStation = esuEcosCommandStationImpl;
    boosters = new HashMap<>();
    measurementEventListeners = new LinkedList<>();
    measurements = new ConcurrentSkipListMap<>();
    maxMeasurementEntries = Integer.getInteger("max.measurment.rows", 100);
    parse(message);
  }

  private void parse(EcosMessage message) {
    //Logger.trace(message.getMessage());
    //Logger.trace(message.getResponse());

    Map<String, Object> values = message.getValueMap();
    int objectId = message.getObjectId();

    if (ID == objectId) {
      //A Booster list
      for (Object o : values.values()) {
        EcosBooster ecosBooster = null;
        if (o != null && o instanceof Map) {
          Map<String, Object> vm = (Map<String, Object>) o;
          ecosBooster = parseValues(vm);
        } else if (o != null) {
          //Details
          ecosBooster = parseValues(values);
        }

        if (ecosBooster != null && ecosBooster.getId() != null) {
          boosters.put(ecosBooster.getId(), ecosBooster);
        }
      }

      if (values.containsKey(Ecos.SIZE)) {
        this.size = Integer.valueOf(values.get(Ecos.SIZE).toString());
      } else {
        this.size = boosters.size();
      }

    } else if (objectId >= 65000 && objectId < 65999) {
      //Details
      EcosBooster ecosBooster = parseValues(values);
      if (ecosBooster.getId() != null) {
        boosters.put(ecosBooster.getId(), ecosBooster);
      }
    } else {
      Logger.warn("Unkown object Id:" + objectId);
    }
  }

  private EcosBooster parseValues(Map<String, Object> values) {
    String id = null;
    if (values.containsKey(Ecos.ID) && values.get(Ecos.ID) != null) {
      id = values.get(Ecos.ID).toString();
    }
    EcosBooster ecosBooster;
    if (boosters.containsKey(id)) {
      ecosBooster = boosters.get(id);
    } else {
      ecosBooster = new EcosBooster();
      ecosBooster.setId(id);
    }

    if (values.containsKey(Ecos.NAME)) {
      String name = values.get(Ecos.NAME).toString();
      ecosBooster.setName(name);
    }

    if (values.containsKey(Ecos.STATUS)) {
      String status = values.get(Ecos.STATUS).toString();
      ecosBooster.setStatus(status);
    }

    if (values.containsKey(Ecos.LIMIT)) {
      String lim = values.get(Ecos.LIMIT).toString();
      int limit = Integer.parseInt(lim);
      ecosBooster.setLimit(limit);
    }

    if (values.containsKey(Ecos.VOLTAGE)) {
      String v = values.get(Ecos.VOLTAGE).toString();
      double voltage = Double.parseDouble(v);
      //Volts is in mV so 
      voltage = voltage / 1000;
      ecosBooster.setVoltage(voltage);
    }

    if (values.containsKey(Ecos.TEMPERATURE)) {
      String t = values.get(Ecos.TEMPERATURE).toString();
      double temperature = Double.parseDouble(t);
      ecosBooster.setTemperature(temperature);
    }

    if (values.containsKey(Ecos.CURRENT)) {
      String c = values.get(Ecos.CURRENT).toString();
      //split the string
      String[] currents = c.split(",");
      double avg = Double.parseDouble(currents[0]);
      double peak = Double.parseDouble(currents[1]);

      ecosBooster.setCurrent(avg);
      ecosBooster.setPeakCurrent(peak);
      ecosBooster.setCurrentUnit("mA");
    }

    return ecosBooster;
  }

  void update(EcosMessage message) {
    parse(message);

    if (internal == null) {
      internal = getBooster("65000");
    } else {
      long now = System.currentTimeMillis();
      Map<String, MeasurementBean> measurementRow = new HashMap<>();

      MeasurementBean peakMeasurement = new MeasurementBean(0, "PEAK", true, now, internal.getPeakCurrent().intValue(), internal.getCurrentUnit(), internal.getPeakCurrent());
      MeasurementBean mainMeasurement = new MeasurementBean(1, "MAIN", true, now, internal.getCurrent().intValue(), internal.getCurrentUnit(), internal.getCurrent());
      MeasurementBean voltMeasurement = new MeasurementBean(2, "VOLT", true, now, internal.getVoltage().intValue(), "V", internal.getVoltage());
      MeasurementBean tempMeasurement = new MeasurementBean(3, "TEMP", true, now, internal.getTemperature().intValue(), "C", internal.getTemperature());

      measurementRow.put("PEAK", peakMeasurement);
      measurementRow.put("MAIN", mainMeasurement);
      measurementRow.put("VOLT", voltMeasurement);
      measurementRow.put("TEMP", tempMeasurement);

      //Logger.trace(voltMeasurement.getDisplayValue() + " " + mainMeasurement.getDisplayValue() + " " + tempMeasurement.getDisplayValue());
      measurements.put(now, measurementRow);

      if (measurements.size() > maxMeasurementEntries) {
        @SuppressWarnings("unused")
        Map.Entry<Long, Map<String, MeasurementBean>> oldestRow = measurements.pollFirstEntry();
        //Logger.trace("Removing measuement of " + new Date(oldestRow.getKey()));
      }

      MeasurementEvent me = new MeasurementEvent(measurementRow, true);
      for (MeasurementEventListener listener : measurementEventListeners) {
        listener.onMeasurement(me);
      }

    }

  }

  int getSize() {
    return this.size;
  }

  List<EcosBooster> getBoosters() {
    return new ArrayList<>(this.boosters.values());
  }

  EcosBooster getBooster(String id) {
    return boosters.get(id);
  }

  boolean isSupportMeasurements() {
    //check for the internal booster
    return internal != null && internal.getCurrent() != null;
  }

//  void startMeasurements() {
//    if (isSupportMeasurements()) {
//      Logger.trace("Start measurements...");
//
//      long measureInterval = Long.parseLong(System.getProperty("measurement.interval", "5"));
//      measureInterval = measureInterval * 1000;
//
//      if (measureInterval > 0 && !ecosCommandStation.isVirtual()) {
//        measurementTask = new MeasurementTask(this);
//
//        measurementTimer = new Timer("MeasurementsTimer");
//        measurementTimer.schedule(measurementTask, 10, measureInterval);
//
//        Logger.debug("Started Measurements Timer with an interval of " + measureInterval + " ms");
//      }
//    }
//  }

//  private void performMeasurements() {
//    if (ecosCommandStation.isConnected()) {
//      long now = System.currentTimeMillis();
//      Map<String, MeasurementBean> measurementRow = new HashMap<>();
//
//      EcosMessage detailsReply = ecosCommandStation.connection.sendMessage(EcosMessageFactory.getBoosterDetails("65000"));
//      update(detailsReply);
//
//      EcosBooster internal = boosters.get("65000");
//
//      MeasurementBean peakMeasurement = new MeasurementBean(0, "PEAK", true, now, internal.getPeakCurrent().intValue(), internal.getCurrentUnit(), internal.getPeakCurrent());
//      MeasurementBean mainMeasurement = new MeasurementBean(1, "MAIN", true, now, internal.getCurrent().intValue(), internal.getCurrentUnit(), internal.getCurrent());
//      MeasurementBean voltMeasurement = new MeasurementBean(2, "VOLT", true, now, internal.getVoltage().intValue(), "V", internal.getVoltage());
//      MeasurementBean tempMeasurement = new MeasurementBean(3, "TEMP", true, now, internal.getTemperature().intValue(), "C", internal.getTemperature());
//
//      measurementRow.put("PEAK", peakMeasurement);
//      measurementRow.put("MAIN", mainMeasurement);
//      measurementRow.put("VOLT", voltMeasurement);
//      measurementRow.put("TEMP", tempMeasurement);
//
//      measurements.put(now, measurementRow);
//
//      if (measurements.size() > maxMeasurementEntries) {
//        @SuppressWarnings("unused")
//        Map.Entry<Long, Map<String, MeasurementBean>> oldestRow = measurements.pollFirstEntry();
//        //Logger.trace("Removing measuement of " + new Date(oldestRow.getKey()));
//      }
//
//      MeasurementEvent me = new MeasurementEvent(measurementRow, true);
//      for (MeasurementEventListener listener : measurementEventListeners) {
//        listener.onMeasurement(me);
//      }
//    } else {
//      Logger.warn("No measurement channels available");
//    }
//  }

  void addMeasurementEventListener(MeasurementEventListener listener) {
    this.measurementEventListeners.add(listener);
  }

  void removeMeasurementEventListener(MeasurementEventListener listener) {
    this.measurementEventListeners.remove(listener);
  }

  @SuppressWarnings("unused")
  SortedMap<Long, Map<String, MeasurementBean>> getMeasurements() {
    return this.measurements;
  }

  @SuppressWarnings("unused")
  Map<String, MeasurementBean> getLastMeasurement() {
    return measurements.lastEntry().getValue();
  }

//  @Override
//  public void onConnectionChange(ConnectionEvent event) {
//    if (!event.isConnected()) {
//      if (measurementTimer != null) {
//        measurementTimer.cancel();
//      }
//
//    }
//  }

//  private class MeasurementTask extends TimerTask {
//
//    private final BoosterManager boosterManager;
//
//    MeasurementTask(BoosterManager boosterManager) {
//      this.boosterManager = boosterManager;
//    }
//
//    @Override
//    public void run() {
//      if (boosterManager.ecosCommandStation.isConnected() && !boosterManager.ecosCommandStation.isVirtual()) {
//        try {
//          if (boosterManager.ecosCommandStation.isSupportTrackMeasurements()) {
//            boosterManager.performMeasurements();
//          } else {
//            Logger.debug("Track Measurements are not supported. Cancelling the Measurements schedule...");
//            measurementTimer.cancel();
//          }
//        } catch (Exception e) {
//          Logger.error(e);
//        }
//      }
//    }
//  }

}
