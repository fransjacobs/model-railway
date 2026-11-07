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
  private volatile Timer measurementTimer;
  // Invalidates even a measurement task that is already executing.
  private volatile long measurementGeneration;

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
    stopWorkers();
    synchronized (this) {
      main = null;
      prog = null;
      volt = null;
      temp = null;
      channelConfigurationQuery = new ChannelConfigurationQueryThread(this);
      channelConfigurationQuery.start();
    }
  }

  /**
   * Always stop the query thread and timer, even when no measurement was recorded.
   * The query thread is interruptible and must not schedule a new timer after stop.
   */
  private void stopWorkers() {
    ChannelConfigurationQueryThread query;
    synchronized (this) {
      measurementGeneration++;
      if (measurementTimer != null) {
        measurementTimer.cancel();
        measurementTimer = null;
      }
      query = channelConfigurationQuery;
      channelConfigurationQuery = null;
      if (query != null) {
        query.quit();
      }
    }
    if (query != null && query != Thread.currentThread()) {
      try {
        query.join(1000);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }


  void notifyLastMeasurement() {
    stopWorkers();
    // Let all listeners know the values are 0 as we have disconnected.
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

    }
  }

  /**
   * A Timer.cancel() does not stop a task which is already executing.
   * Check the connection and the generation before each CAN request.
   */
  private boolean measurementActive(long generation) {
    return measurementGeneration == generation && marklinCentralStationImpl.isConnected()
            && !marklinCentralStationImpl.isVirtual();
  }

  private MeasurementBean queryMeasurement(MeasuringChannel channel, long now, long generation) {
    if (!measurementActive(generation) || channel == null) {
      return null;
    }
    CanMessage message = marklinCentralStationImpl.sendMessage(
            CanMessageFactory.systemStatus(marklinCentralStationImpl.csUid, channel.getNumber()));
    if (!measurementActive(generation)) {
      return null;
    }
    return SystemStatusMessage.parse(channel, message, now);
  }

  private void performMeasurements(long generation) {
    if (!measurementActive(generation)) {
      return;
    }

    long now = System.currentTimeMillis();
    Map<String, MeasurementBean> measurementRow = new HashMap<>();

    MeasurementBean mainMeasurement = queryMeasurement(main, now, generation);
    if (mainMeasurement == null || !measurementActive(generation)) {
      return;
    }
    measurementRow.put(CanMessage.MAIN, mainMeasurement);

    MeasurementBean progMeasurement = queryMeasurement(prog, now, generation);
    if (progMeasurement == null || !measurementActive(generation)) {
      return;
    }
    measurementRow.put(CanMessage.PROG, progMeasurement);

    MeasurementBean voltMeasurement = queryMeasurement(volt, now, generation);
    if (voltMeasurement == null || !measurementActive(generation)) {
      return;
    }
    measurementRow.put(CanMessage.VOLT, voltMeasurement);

    MeasurementBean tempMeasurement = queryMeasurement(temp, now, generation);
    if (tempMeasurement == null || !measurementActive(generation)) {
      return;
    }
    measurementRow.put(CanMessage.TEMP, tempMeasurement);

    // Never publish an incomplete cycle, including one invalidated by disconnect.
    if (!measurementActive(generation)) {
      return;
    }
    measurements.put(now, measurementRow);
    if (measurements.size() > maxMeasurementEntries) {
      measurements.pollFirstEntry();
    }

    MeasurementEvent me = new MeasurementEvent(measurementRow, true);
    for (MeasurementEventListener listener : measurementEventListeners) {
      if (!measurementActive(generation)) {
        break;
      }
      listener.onMeasurement(me);
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
      stopWorkers();
    }
  }

  private class ChannelConfigurationQueryThread extends Thread {

    private final BoosterManager boosterManager;

    private volatile boolean running = true;
    private volatile boolean configured = false;

    public ChannelConfigurationQueryThread(BoosterManager boosterManager) {
      super("BM-CHANNEL-CONFIG-QUERY-THREAD");
      setDaemon(true);
      this.boosterManager = boosterManager;
    }

    void quit() {
      this.running = false;
      interrupt();
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
      // Wait before querying, but exit promptly when the connection is stopped.
      zleep(5000);
      if (!running || !marklinCentralStationImpl.isConnected()) {
        running = false;
        return;
      }

      Logger.debug("Obtaining channel configuration...");
      while (!configured && running && marklinCentralStationImpl.isConnected()) {
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

          configured = boosterManager.main != null && boosterManager.prog != null
                  && boosterManager.volt != null && boosterManager.temp != null;
          if (configured) {
            Logger.trace("Start measurements...");

            long measureInterval = Long.parseLong(System.getProperty("measurement.interval", "5"));
            measureInterval = measureInterval * 1000;

            synchronized (boosterManager) {
              if (running && boosterManager.marklinCentralStationImpl.isConnected()
                      && measureInterval > 0 && !boosterManager.marklinCentralStationImpl.isVirtual()) {
                boosterManager.measurementTask = new MeasurementTask(boosterManager, boosterManager.measurementGeneration);
                boosterManager.measurementTimer = new Timer("MeasurementsTimer", true);
                boosterManager.measurementTimer.schedule(measurementTask, 10, measureInterval);
                Logger.debug("Started Measurements Timer with an interval of " + measureInterval + " ms");
              }
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
    private final long generation;

    MeasurementTask(BoosterManager boosterManager, long generation) {
      this.boosterManager = boosterManager;
      this.generation = generation;
    }

    @Override
    public void run() {
      if (!boosterManager.measurementActive(generation)) {
        return;
      }
      try {
        if (boosterManager.marklinCentralStationImpl.isSupportTrackMeasurements()) {
          boosterManager.performMeasurements(generation);
        } else {
          Logger.debug("Track Measurements are not supported. Cancelling the Measurements schedule...");
          boosterManager.stopWorkers();
        }
      } catch (Exception ex) {
        if (boosterManager.measurementActive(generation)) {
          Logger.error(ex);
        }
      }
    }
  }
}
