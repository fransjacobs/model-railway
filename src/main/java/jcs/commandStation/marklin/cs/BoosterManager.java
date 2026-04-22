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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListMap;
import jcs.commandStation.entities.MeasurementBean;
import jcs.commandStation.events.ConnectionEvent;
import jcs.commandStation.events.ConnectionEventListener;
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
class BoosterManager implements ConnectionEventListener {

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
    //Let all listeners know the the values are 0 as we have disconnected
    if (!measurements.isEmpty()) {
      long now = System.currentTimeMillis();
      Map<String, MeasurementBean> measurementRow = new HashMap<>();

      MeasurementBean mainMeasurement = new MeasurementBean(1, "MAIN", true, now, 0, "A", 0.0);
      measurementRow.put(CanMessage.MAIN, mainMeasurement);
      MeasurementBean progMeasurement = new MeasurementBean(1, "PROG", true, now, 0, "A", 0.0);
      measurementRow.put(CanMessage.PROG, progMeasurement);
      MeasurementBean voltMeasurement = new MeasurementBean(1, "VOLT", true, now, 0, "V", 0.0);
      measurementRow.put(CanMessage.VOLT, voltMeasurement);
      MeasurementBean tempMeasurement = new MeasurementBean(1, "TEMP", true, now, 0, "C", 0.0);
      measurementRow.put(CanMessage.TEMP, tempMeasurement);

      measurements.put(now, measurementRow);
      MeasurementEvent me = new MeasurementEvent(measurementRow, false);
      //Signal listeners that there are no measurements
      for (MeasurementEventListener listener : measurementEventListeners) {
        listener.onMeasurement(me);
      }

      if (measurementTimer != null) {
        measurementTimer.cancel();
      }
      if (channelConfigurationQuery != null) {
        channelConfigurationQuery.quit();

        try {
          channelConfigurationQuery.join(2000);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private void performMeasurements() {
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
        @SuppressWarnings("unused")
        Map.Entry<Long, Map<String, MeasurementBean>> oldestRow = measurements.pollFirstEntry();
        //Logger.trace("Removing measuement of " + new Date(oldestRow.getKey()));
      }

      MeasurementEvent me = new MeasurementEvent(measurementRow, true);
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

  @SuppressWarnings("unused")
  SortedMap<Long, Map<String, MeasurementBean>> getMeasurements() {
    return this.measurements;
  }

  @SuppressWarnings("unused")
  Map<String, MeasurementBean> getLastMeasurement() {
    return measurements.lastEntry().getValue();
  }

  @SuppressWarnings("unused")
  boolean isInitialized() {
    return channelConfigurationQuery != null && channelConfigurationQuery.configured;
  }

  @Override
  public void onConnectionChange(ConnectionEvent event) {
    if (!event.isConnected()) {
      if (channelConfigurationQuery != null && channelConfigurationQuery.isRunning()) {
        channelConfigurationQuery.quit();
      }

      if (measurementTimer != null) {
        measurementTimer.cancel();
      }

    }
  }

  private class ChannelConfigurationQueryThread extends Thread {

    private final BoosterManager boosterManager;

    private volatile boolean running = false;
    private volatile boolean configured = false;

    public ChannelConfigurationQueryThread(BoosterManager boosterManager) {
      super("BM-CHANNEL-CONFIG-QUERY-THREAD");
      this.boosterManager = boosterManager;
    }

    void quit() {
      this.running = false;
    }

    boolean isRunning() {
      return running;
    }

    private void zleep(long millis) {
      try {
        sleep(millis);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }

    @Override
    public void run() {
      running = true;

      //Wait for 5 seconds before querying the channels
      zleep(5000);

      Logger.debug("Obtaining channel configuration...");
      while (!configured && running) {
        CanDevice gfp = boosterManager.marklinCentralStationImpl.getCanDevice(this.boosterManager.marklinCentralStationImpl.getCsUid());
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

            long measureInterval = Long.parseLong(System.getProperty("measurement.interval", "5"));
            measureInterval = measureInterval * 1000;

            if (measureInterval > 0 && !boosterManager.marklinCentralStationImpl.isVirtual()) {
              boosterManager.measurementTask = new MeasurementTask(boosterManager);

              boosterManager.measurementTimer = new Timer("MeasurementsTimer");
              boosterManager.measurementTimer.schedule(measurementTask, 10, measureInterval);

              Logger.debug("Started Measurements Timer with an interval of " + measureInterval + " ms");
            }
          } else {
            zleep(10000);
            boosterManager.marklinCentralStationImpl.obtainDevices();
            zleep(2000);
            
            Logger.debug("Re-query the Measurement Channels...");
          }
        }
        Logger.debug("ChannelConfig finished");
      }
    }
  }
  
  
//  TRACE	2026-04-22 19:28:17.057 [BM-CHANNEL-CONFIG-QUERY-THREAD] BoosterManager$ChannelConfigurationQueryThread.run(): There are 4 Measurement Channels...
//TRACE	2026-04-22 19:28:17.057 [BM-CHANNEL-CONFIG-QUERY-THREAD] BoosterManager$ChannelConfigurationQueryThread.run(): Main: MeasuringChannel{number=1, name=MAIN, scale=-3, colorGreen=48, colorYellow=240, colorRed=224, colorMax=192, zero=0, rangeGreen=312, rangeYellow=336, rangeRed=336, rangeMax=396, startValue=0.0, endValue=3.3, unit=A}
//TRACE	2026-04-22 19:28:17.057 [BM-CHANNEL-CONFIG-QUERY-THREAD] BoosterManager$ChannelConfigurationQueryThread.run(): Prog: MeasuringChannel{number=2, name=PROG, scale=-3, colorGreen=48, colorYellow=240, colorRed=224, colorMax=192, zero=0, rangeGreen=330, rangeYellow=363, rangeRed=363, rangeMax=759, startValue=0.0, endValue=2.3, unit=A}
//TRACE	2026-04-22 19:28:17.057 [BM-CHANNEL-CONFIG-QUERY-THREAD] BoosterManager$ChannelConfigurationQueryThread.run(): Volt: MeasuringChannel{number=3, name=VOLT, scale=-3, colorGreen=192, colorYellow=12, colorRed=48, colorMax=192, zero=0, rangeGreen=194, rangeYellow=252, rangeRed=252, rangeMax=659, startValue=10.0, endValue=27.0, unit=V}
//TRACE	2026-04-22 19:28:17.057 [BM-CHANNEL-CONFIG-QUERY-THREAD] BoosterManager$ChannelConfigurationQueryThread.run(): Temp: MeasuringChannel{number=4, name=TEMP, scale=0, colorGreen=12, colorYellow=8, colorRed=240, colorMax=192, zero=0, rangeGreen=121, rangeYellow=145, rangeRed=145, rangeMax=193, startValue=0.0, endValue=80.0, unit=C}
//
  
  

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
