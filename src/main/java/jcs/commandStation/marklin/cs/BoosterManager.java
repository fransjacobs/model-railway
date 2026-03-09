/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.commandStation.marklin.cs;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListMap;
import jcs.commandStation.entities.MeasurementBean;
import jcs.commandStation.events.MeasurementEvent;
import jcs.commandStation.events.MeasurementEventListener;
import jcs.commandStation.marklin.cs.can.CanMessage;
import jcs.commandStation.marklin.cs.can.CanMessageFactory;
import jcs.commandStation.marklin.cs.can.device.CanDevice;
import jcs.commandStation.marklin.cs.can.device.MeasuringChannel;
import jcs.commandStation.marklin.cs.can.parser.SystemStatusMessage;
import org.tinylog.Logger;

/**
 * Booster Manager for Marklin CS. <br>
 * This Manager performs the measurement on the GFP (Gleiss Format Prozessor).<br>
 * The Marklin CS self has 4 analog channels:<br>
 * - Track voltage<br>
 * - Track current<br>
 * - Programming Track current<br>
 * - CS Temperature<br>
 *
 * Channel initialization is done in the connect phase by querying the CS for devices.<br>
 * It has been observed that the initialization sometimes fails, hence this class has a worker thread to re-query<br>
 * the channel configuration.<br>
 *
 * Once the channel configuration is known a thread is started to query the measured values at regular intervals.
 */
class BoosterManager {

  private final MarklinCentralStationImpl marklinCentralStationImpl;

  private ChannelConfigurationQueryThread channelConfigurationQuery;

  private MeasurementTask measurementTask;
  private Timer measurementTimer;

  //The Marklin CS has 4 analog Channels
  private MeasuringChannel main;
  private MeasuringChannel prog;
  private MeasuringChannel volt;
  private MeasuringChannel temp;

  private final SortedMap<Long, Map<String, MeasurementBean>> measurements;

  private final List<MeasurementEventListener> measurementEventListeners;

  private final int maxMeasurementEntries;

  BoosterManager(MarklinCentralStationImpl marklinCentralStationImpl) {
    this.marklinCentralStationImpl = marklinCentralStationImpl;
    measurementEventListeners = new LinkedList<>();

    measurements = new ConcurrentSkipListMap<>();

    maxMeasurementEntries = Integer.getInteger("max.measurment.rows", 100);
  }

  void initChannels() {
    channelConfigurationQuery = new ChannelConfigurationQueryThread(this);

    channelConfigurationQuery.start();
  }

  void notifyLastMeasurement() {
    if (measurementTimer != null) {
      measurementTimer.cancel();
    }
    //Let all listener know the the values are 0 as we have disconnected
    if (!measurements.isEmpty()) {
      //get the last measurement as template
      long lastKey = measurements.lastKey();
      Map<String, MeasurementBean> lastMeasurement = measurements.get(lastKey);
      //hier gebleven

      //    //Signal listeners that there are no measurements
//    MeasuredChannels measuredChannels = new MeasuredChannels(System.currentTimeMillis());
//    MeasurementEvent me = new MeasurementEvent(measuredChannels);
//    for (MeasurementEventListener listener : measurementEventListeners) {
//      listener.onMeasurement(me);
//    }
    }

  }

  private void performMeasurements() {
    //The measurable channels are in the GFP. 
    //CanDevice gfp = this.marklinCentralStationImpl.getCanDevice(marklinCentralStationImpl.csUid);

    if (marklinCentralStationImpl.isConnected()) {
      long now = System.currentTimeMillis();
      Map<String, MeasurementBean> measurementRow = new HashMap<>();

      int channelNumber = main.getNumber();
      CanMessage message = marklinCentralStationImpl.sendMessage(CanMessageFactory.systemStatus(this.marklinCentralStationImpl.csUid, channelNumber));
      MeasurementBean mainMeasurement = SystemStatusMessage.parse(main, message, now);
      measurementRow.put(CanMessage.MAIN, mainMeasurement);

      channelNumber = prog.getNumber();
      message = marklinCentralStationImpl.sendMessage(CanMessageFactory.systemStatus(this.marklinCentralStationImpl.csUid, channelNumber));
      MeasurementBean progMeasurement = SystemStatusMessage.parse(prog, message, now);
      measurementRow.put(CanMessage.PROG, progMeasurement);

      channelNumber = volt.getNumber();
      message = marklinCentralStationImpl.sendMessage(CanMessageFactory.systemStatus(this.marklinCentralStationImpl.csUid, channelNumber));
      MeasurementBean voltMeasurement = SystemStatusMessage.parse(volt, message, now);
      measurementRow.put(CanMessage.VOLT, voltMeasurement);

      channelNumber = temp.getNumber();
      message = marklinCentralStationImpl.sendMessage(CanMessageFactory.systemStatus(this.marklinCentralStationImpl.csUid, channelNumber));
      MeasurementBean tempMeasurement = SystemStatusMessage.parse(temp, message, now);
      measurementRow.put(CanMessage.TEMP, tempMeasurement);

      measurements.put(now, measurementRow);

      if (measurements.size() > maxMeasurementEntries) {
        Map.Entry<Long, Map<String, MeasurementBean>> oldestRow = measurements.pollFirstEntry();
        Logger.trace("Removing measuement of " + new Date(oldestRow.getKey()));
      }

      MeasurementEvent me = new MeasurementEvent(measurementRow);
      for (MeasurementEventListener listener : measurementEventListeners) {
        listener.onMeasurement(me);
      }

    } else {
      Logger.warn("No measurement channels available");
    }
  }

  void addMeasurementEventListener(MeasurementEventListener listener) {
    this.measurementEventListeners.add(listener);
  }

  void removeMeasurementEventListener(MeasurementEventListener listener) {
    this.measurementEventListeners.remove(listener);
  }

  SortedMap<Long, Map<String, MeasurementBean>> getMeasurements() {
    return this.measurements;
  }

  Map<String, MeasurementBean> getLastMeasurement() {
    return measurements.lastEntry().getValue();
  }

  boolean isInitialized() {
    return channelConfigurationQuery != null && channelConfigurationQuery.configured;
  }

  private class ChannelConfigurationQueryThread extends Thread {

    private final BoosterManager boosterManager;

    @SuppressWarnings("FieldMayBeFinal")
    private boolean stop = false;
    private boolean quit = true;

    private boolean configured = false;

    public ChannelConfigurationQueryThread(BoosterManager boosterManager) {
      this.boosterManager = boosterManager;
    }

    @SuppressWarnings("unused")
    void quit() {
      this.quit = true;
    }

    @SuppressWarnings("unused")
    boolean isRunning() {
      return !this.quit;
    }

    @SuppressWarnings("unused")
    boolean isFinished() {
      return this.stop;
    }

    @Override
    public void run() {
      quit = false;

      Thread.currentThread().setName("BM-CHANNEL-CONFIG-QUERY-THREAD");
      Logger.debug("Obtaining channel configuration...");

      while (!configured) {

        CanDevice gfp = this.boosterManager.marklinCentralStationImpl.getCanDevice(this.boosterManager.marklinCentralStationImpl.getCsUid());
        if (gfp != null) {
          List<MeasuringChannel> measuringChannels = gfp.getMeasuringChannels();

          Logger.trace("There are " + measuringChannels.size() + " Measurement Channels...");

          for (MeasuringChannel mc : measuringChannels) {
            String name = mc.getName();
            switch (name) {
              case CanMessage.MAIN -> {
                this.boosterManager.main = mc;
                Logger.trace("Main: " + mc);
              }
              case CanMessage.PROG -> {
                this.boosterManager.prog = mc;
                Logger.trace("Prog: " + mc);
              }
              case CanMessage.VOLT -> {
                this.boosterManager.volt = mc;
                Logger.trace("Volt: " + mc);
              }
              case CanMessage.TEMP -> {
                this.boosterManager.temp = mc;
                Logger.trace("Temp: " + mc);
              }
            }
          }

          configured = boosterManager.main != null && boosterManager.prog != null && boosterManager.volt != null && boosterManager.volt != null;
          if (configured) {
            Logger.trace("Start measurements...");

            long measureInterval = Long.parseLong(System.getProperty("measurement.interval", "1"));
            measureInterval = measureInterval * 1000;

            if (measureInterval > 0 && !boosterManager.marklinCentralStationImpl.isVirtual()) {
              boosterManager.measurementTask = new MeasurementTask(boosterManager);

              boosterManager.measurementTimer = new Timer("MeasurementsTimer");
              boosterManager.measurementTimer.schedule(measurementTask, 10, measureInterval);

              Logger.debug("Started Measurements Timer with an interval of " + measureInterval + "ms");

            }

          } else {
            Logger.debug("Re-query the Measurement Channels...");
            synchronized (this) {
              try {
                this.wait(1000);
              } catch (InterruptedException ex) {
                Logger.debug("Interupted");
              }
            }
          }
        }

        Logger.debug("Channel config finished");
      }
    }
  }

  private class MeasurementTask extends TimerTask {

    private final BoosterManager boosterManager;

    MeasurementTask(BoosterManager boosterManager) {
      this.boosterManager = boosterManager;
    }

    @Override
    public void run() {
      if (boosterManager.marklinCentralStationImpl.isConnected() && !boosterManager.marklinCentralStationImpl.isVirtual()) {
        try {
          if (boosterManager.marklinCentralStationImpl.isSupportTrackMeasurements()) {
            boosterManager.performMeasurements();
          } else {
            Logger.debug("Track Measurements are not supported. Cancelling the Measurements schedule...");
            measurementTimer.cancel();
          }
        } catch (Exception e) {
          Logger.error(e);
        }
      }
    }
  }
}
