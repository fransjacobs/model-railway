/*
 * Copyright 2026 frans.
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
package jcs.commandStation.automation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.automation.state.SensorEventCallback;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

/**
 *
 */
public class SensorMonitorTest {

  protected final PersistenceTestHelper testHelper;
  protected final PersistenceService ps;
  private final ExecutorService executor;
  private List<Tile> tiles;

  public SensorMonitorTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    //Switch the Virtual Simulator OFF as it will interfeare with this step test
    System.setProperty("do.not.simulate.virtual.drive", "true");

    testHelper = PersistenceTestHelper.getInstance();
    testHelper.runTestDataInsertScript("autopilot_queue_test.sql");

    ps = PersistenceFactory.getService();
    executor = Executors.newCachedThreadPool();
  }

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    if (JCS.getJcsCommandStation().connect()) {

      JCS.getJcsCommandStation().switchPower(true);
      tiles = TileCache.loadTiles(true);

      Logger.info("=========================== setUp done..............");
    } else {
      Logger.error("###### Can't connect to command station! ########");
    }
  }

  @AfterEach
  public void tearDown() {
    //Logger.info("Test finished");
    //pause(1000);
  }

  private void toggleSensor(SensorBean sensorBean) {
    sensorBean.toggle();
    sensorBean.setActive((sensorBean.getStatus() == 1));
    SensorEvent sensorEvent = new SensorEvent(sensorBean);

    //JCS.getJcsCommandStation().getFeedbackControllers().getFirst().simulateSensor(sensorEvent);
    //executor.execute(() -> {
    JCS.getJcsCommandStation().getFeedbackControllers().getFirst().simulateSensor(sensorEvent);
    //});

  }

  @Test
  public void testCheckFeedbackController() {
    System.out.println("checkFeedbackController");

    assertNotNull(JCS.getJcsCommandStation());
    assertEquals(1, JCS.getJcsCommandStation().getFeedbackControllers().size());
  }

  /**
   * Test of stopMonitor method, of class SensorMonitor.
   */
  @Test
  public void testStopMonitor() {
    System.out.println("stopMonitor");
    SensorMonitor instance = new SensorMonitor();

    boolean result = instance.isRunning();
    assertFalse(result);
    instance.start();

    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;

    boolean running = instance.isRunning();
    while (!running && now < timeout) {
      running = instance.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(instance.isRunning());
    Logger.trace("Sensors prepared in " + (now - start) + " ms...");

    instance.stopMonitor();

    now = System.currentTimeMillis();
    start = now;
    timeout = now + 10000;

    boolean stopped = instance.isRunning();
    while (stopped && now < timeout) {
      stopped = instance.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertFalse(instance.isRunning());
    Logger.trace("Monitor <stopped in " + (now - start) + " ms...");
  }

  private void pause(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Logger.error(ex);
    }
  }

  /**
   * Test of isRunning method, of class SensorMonitor.
   */
  @Test
  public void testIsRunning() {
    System.out.println("isRunning");
    SensorMonitor instance = new SensorMonitor();

    boolean result = instance.isRunning();
    assertFalse(result);
    //Start the monitor
    instance.start();

    //Wait for the initialization of the Sensors
    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 1000000;

    boolean sensorsReady = instance.isRunning();
    while (!sensorsReady && now < timeout) {
      sensorsReady = instance.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(instance.isRunning());
    Logger.trace("Sensors prepared in " + (now - start) + " ms...");

    instance.stopMonitor();
  }

  /**
   * Test of isAllSensorsRegistered method, of class SensorMonitor.
   */
  @Test
  public void testIsAllSensorsRegistered() {
    System.out.println("isAllSensorsRegistered");

    SensorMonitor instance = new SensorMonitor();
    boolean result = instance.isRunning();
    assertFalse(result);

    Map<Integer, SensorBean> sensors = instance.getSensorBeans();

    assertEquals(0, sensors.size());

    //Start the monitor
    instance.start();

    //Wait for the initialization of the Sensors
    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;

    boolean sensorsReady = instance.isRunning();
    while (!sensorsReady && now < timeout) {
      sensorsReady = instance.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(instance.isRunning());
    Logger.trace("Sensors prepared in " + (now - start) + " ms...");

    sensors = instance.getSensorBeans();

    assertEquals(8, sensors.size());

    instance.stopMonitor();
  }

  private int eventCallbackCount = 0;

  /**
   * Test of onSensorChange method, of class SensorMonitor.
   */
  @Test
  public void testOnSensorChange() {
    System.out.println("onSensorChange");
    SensorMonitor instance = new SensorMonitor();
    instance.start();
    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;

    boolean sensorsReady = instance.isRunning();
    while (!sensorsReady && now < timeout) {
      sensorsReady = instance.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(instance.isRunning());
    Logger.trace("Sensors prepared in " + (now - start) + " ms...");

    Map<Integer, SensorBean> sensors = instance.getSensorBeans();
    assertEquals(8, sensors.size());
    //Check the sensor 1 is avalable
    SensorBean sensor0 = sensors.get(0);
    assertEquals("M01-C01", sensor0.getName());

    //Resister a callback
    TestSensorEventCallback callback = new TestSensorEventCallback(this);
    instance.subscribe(sensor0.getId(), callback);
    assertEquals(1, instance.getSubscribers().size());
    eventCallbackCount = 0;

    assertTrue(instance.isSensorCallbackRegistered(sensor0.getId()));

    toggleSensor(sensor0);

    now = System.currentTimeMillis();
    start = now;
    timeout = now + 1000000;

    boolean callbackCalled = eventCallbackCount > 0;
    while (!callbackCalled && now < timeout) {
      callbackCalled = eventCallbackCount > 0;
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    Logger.trace("Callback in " + (now - start) + " ms...");

    assertEquals(1, eventCallbackCount);

    instance.stopMonitor();
  }

  @Test
  public void testOnSensorChangeGhost() {
    System.out.println("onSensorChangeGhost");
    SensorMonitor instance = new SensorMonitor();
    instance.start();
    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;

    boolean sensorsReady = instance.isRunning();
    while (!sensorsReady && now < timeout) {
      sensorsReady = instance.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(instance.isRunning());
    Logger.trace("Sensors prepared in " + (now - start) + " ms...");

    assertTrue(JCS.getJcsCommandStation().isPowerOn());
    assertFalse(instance.isGhostDetected());

    Map<Integer, SensorBean> sensors = instance.getSensorBeans();
    assertEquals(8, sensors.size());
    //Check the sensor 1 is avalable
    SensorBean sensor0 = sensors.get(0);
    assertEquals("M01-C01", sensor0.getName());

    assertFalse(instance.isSensorCallbackRegistered(sensor0.getId()));

    toggleSensor(sensor0);

    //Wait a litlle to give the thread the change to process
    pause(30);

    assertTrue(instance.isGhostDetected());

    assertFalse(JCS.getJcsCommandStation().isPowerOn());
    instance.stopMonitor();
  }

  /**
   * Test of handleSensorEvent method, of class SensorMonitor.
   */
  //@Test
  public void testHandleSensorEvent() {
    System.out.println("handleSensorEvent");
    SensorEvent event = null;
    SensorMonitor instance = null;
    instance.handleSensorEvent(event);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  class TestSensorEventCallback implements SensorEventCallback {

    private SensorMonitorTest sensorMonitorTest;

    TestSensorEventCallback(SensorMonitorTest sensorMonitorTest) {
      this.sensorMonitorTest = sensorMonitorTest;
    }

    @Override
    public void onEvent(SensorEvent event) {
      if (event.getSensorId() == 0) {
        sensorMonitorTest.eventCallbackCount++;
      }

    }

  }

}
