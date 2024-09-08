/*
 * Copyright 2024 Frans Jacobs.
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
import java.util.List;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.JCSCommandStation;
import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileFactory;
import jcs.util.RunUtil;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class AutoPilotTest {

  private static final Long NS_DHG_6505 = 7L;
  private static final Long BR_101_003_2 = 23L;
  private static final Long NS_1631 = 39L;

  private final PersistenceTestHelper testHelper;
  private final PersistenceService ps;
  private final JCSCommandStation cs;
  private boolean skipTest = false;

  private List<SensorEvent> sensorHandlerEvents;

  public AutoPilotTest() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");
    System.setProperty("do.not.simulate.virtual.drive", "true");
    System.setProperty("state.machine.stepTest", "false");
    testHelper = PersistenceTestHelper.getInstance();
    testHelper.runTestDataInsertScript("autopilot_test_layout.sql");
    ps = PersistenceFactory.getService();
    cs = JCS.getJcsCommandStation();
    cs.disconnect();
    //When running in a batch the default command station could be different..
    CommandStationBean virt = ps.getCommandStation("virtual");
    ps.changeDefaultCommandStation(virt);
    
    if (RunUtil.isWindows()) {
      Logger.info("Skipping tests on Windows!");
      skipTest = true;
    }
  }

  @BeforeEach
  public void setUp() {
    //Reset the layout...
    for (BlockBean block : ps.getBlocks()) {
      block.setLocomotive(null);
      block.setBlockState(BlockBean.BlockState.FREE);
      block.setArrivalSuffix(null);
      block.setReverseArrival(false);
      ps.persist(block);
    }

    for (RouteBean route : ps.getRoutes()) {
      route.setLocked(false);
      ps.persist(route);
    }

    sensorHandlerEvents = new ArrayList<>();
    if (!cs.isConnected()) {
      cs.connect();
    }
    cs.switchPower(true);

    assertTrue(cs.isPowerOn());
    Logger.trace("====================== Setup done ========================");
  }

  @AfterEach
  public void tearDown() {
    Logger.trace("====================== Teardown start ========================");
    AutoPilot.stopAutoMode();

    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;
    boolean autoPilotRunning = AutoPilot.isAutoModeActive();
    while (autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertFalse(AutoPilot.isAutoModeActive());

    Logger.debug("Autopilot Automode stopped in " + (now - start) + " ms.");

    AutoPilot.clearDispatchers();
  }

  @Test
  public void testStartStopAutoModeNoDispatchers() {
    if (this.skipTest) {
      return;
    }
    Logger.info("startStopAutoModeNoDispatchers");
    assertFalse(AutoPilot.isAutoModeActive());

    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;

    AutoPilot.startAutoMode();

    boolean autoPilotRunning = AutoPilot.isAutoModeActive();
    while (!autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(AutoPilot.isAutoModeActive());

    Logger.debug("Autopilot automode started in " + (now - start) + " ms.");

    now = System.currentTimeMillis();
    start = now;
    timeout = now + 10000;

    AutoPilot.stopAutoMode();

    autoPilotRunning = AutoPilot.isAutoModeActive();
    while (autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertFalse(AutoPilot.isAutoModeActive());

    Logger.debug("Autopilot automode stopped in " + (now - start) + " ms.");

  }

  @Test
  public void testStartStopAutoModeDispatcherstNotRunning() {
    if (this.skipTest) {
      return;
    }
    Logger.info("StartStopAutoModeDispatcherstNotRunning");
    assertFalse(AutoPilot.isAutoModeActive());

    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;

    AutoPilot.startAutoMode();

    boolean autoPilotRunning = AutoPilot.isAutoModeActive();
    while (!autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(AutoPilot.isAutoModeActive());

    Logger.debug("Autopilot automode started in " + (now - start) + " ms.");

    assertFalse(AutoPilot.areDispatchersRunning());

    //Lets create a dispatcher. A Loc must be on the track
    LocomotiveBean dhg = ps.getLocomotive(NS_DHG_6505);
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    block1.setLocomotive(dhg);
    block1.setBlockState(BlockBean.BlockState.OCCUPIED);
    ps.persist(block1);

    AutoPilot.prepareAllDispatchers();

    Dispatcher dhgDisp = AutoPilot.getLocomotiveDispatcher(dhg);
    assertNotNull(dhgDisp);

    assertFalse(dhgDisp.isLocomotiveAutomodeOn());
    assertFalse(dhgDisp.isRunning());

    now = System.currentTimeMillis();
    start = now;
    timeout = now + 10000;

    AutoPilot.stopAutoMode();

    autoPilotRunning = AutoPilot.isAutoModeActive();
    while (autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertFalse(AutoPilot.isAutoModeActive());

    Logger.debug("Autopilot automode stopped in " + (now - start) + " ms.");
  }

  @Test
  public void testStartStopAutoModeDispatcherRunning() {
    if (this.skipTest) {
      return;
    }
    Logger.info("StartStopAutoModeDispatcherRunning");
    assertFalse(AutoPilot.isAutoModeActive());

    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;

    AutoPilot.startAutoMode();

    boolean autoPilotRunning = AutoPilot.isAutoModeActive();
    while (!autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(AutoPilot.isAutoModeActive());

    Logger.debug("Autopilot automode started in " + (now - start) + " ms.");

    assertFalse(AutoPilot.areDispatchersRunning());

    //Lets create a dispatcher. A Loc must be on the track
    LocomotiveBean dhg = ps.getLocomotive(NS_DHG_6505);
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    block1.setLocomotive(dhg);
    block1.setBlockState(BlockBean.BlockState.OCCUPIED);
    ps.persist(block1);

    AutoPilot.prepareAllDispatchers();

    Dispatcher dhgDisp = AutoPilot.getLocomotiveDispatcher(dhg);
    assertNotNull(dhgDisp);

    assertFalse(dhgDisp.isLocomotiveAutomodeOn());
    assertFalse(dhgDisp.isRunning());

    now = System.currentTimeMillis();
    start = now;
    timeout = now + 10000;

    boolean started = dhgDisp.startLocomotiveAutomode();
    assertTrue(started);
    String dispatcherState = dhgDisp.getStateName();
    assertEquals("IdleState", dispatcherState);

    boolean dispatcherThreadRunning = dhgDisp.isRunning();
    while (!dispatcherThreadRunning && timeout > now) {
      pause(1);
      dispatcherThreadRunning = dhgDisp.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(dhgDisp.isRunning());
    assertTrue(dhgDisp.isLocomotiveAutomodeOn());

    Logger.debug("Dispatcher " + dhgDisp.getName() + " started in " + (now - start) + " ms.");

    dispatcherState = dhgDisp.getStateName();
    assertEquals("IdleState", dispatcherState);

    now = System.currentTimeMillis();
    start = now;
    timeout = now + 10000;

    AutoPilot.stopAutoMode();

    autoPilotRunning = AutoPilot.isAutoModeActive();
    while (autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertFalse(AutoPilot.isAutoModeActive());

    Logger.debug("Autopilot automode stopped in " + (now - start) + " ms.");
  }

  //@Test
  public void testIsSensorRegistered() {
    if (this.skipTest) {
      return;
    }
    System.out.println("isSensorRegistered");
    String sensorId = "0-0001";
    //AutoPilot instance = AutoPilot.getInstance();
    assertFalse(AutoPilot.isAutoModeActive());

    assertFalse(AutoPilot.isSensorHandlerRegistered(sensorId));
    AutoPilot.startAutoMode();
    //let the autopilot start...
    pause(150);
    assertTrue(AutoPilot.isAutoModeActive());
    assertFalse(AutoPilot.isSensorHandlerRegistered(sensorId));
    TestSensorHandler testSensorHandler = new TestSensorHandler(sensorId, this);
    AutoPilot.addSensorEventHandler(testSensorHandler);

    assertTrue(AutoPilot.isSensorHandlerRegistered(sensorId));

    AutoPilot.stopAutoMode();
    assertFalse(AutoPilot.isAutoModeActive());
    //let the autopilot finish...
    pause(1000);
    assertFalse(AutoPilot.isSensorHandlerRegistered(sensorId));
  }

  //@Test
  public void testGetOnTrackLocomotives() {
    if (this.skipTest) {
      return;
    }
    System.out.println("getOnTrackLocomotives");
    //Check is the test script has run well
    List<Tile> tiles = TileFactory.toTiles(ps.getTileBeans(), false, false);
    assertEquals(29, tiles.size());
    List<BlockBean> blocks = ps.getBlocks();
    assertEquals(4, blocks.size());

    //AutoPilot instance = AutoPilot.getInstance();
    AutoPilot.startAutoMode();
    pause(50);
    assertTrue(AutoPilot.isAutoModeActive());

    List<LocomotiveBean> onTraclLocos = AutoPilot.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());

    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    ps.persist(block1);

    List<LocomotiveBean> expected = new ArrayList<>();
    expected.add(ns1631);

    onTraclLocos = AutoPilot.getOnTrackLocomotives();
    assertEquals(expected, onTraclLocos);

    AutoPilot.stopAutoMode();
    assertFalse(AutoPilot.isAutoModeActive());
    //let the autopilot finish...
    pause(1000);
  }

  //@Test
  public void testGhostDetection() {
    if (this.skipTest) {
      return;
    }
    System.out.println("GhostDetection test");

    //AutoPilot instance = AutoPilot.getInstance();
    JCS.getJcsCommandStation().switchPower(true);

    long now = System.currentTimeMillis();
    long timeout = now + 10000;

    boolean powerOn = JCS.getJcsCommandStation().isPowerOn();
    while (!powerOn && timeout > now) {
      //JCS.getJcsCommandStation().switchPower(true);
      pause(250);
      powerOn = JCS.getJcsCommandStation().isPowerOn();
      now = System.currentTimeMillis();
    }

    //When building on linux this test soemtime times out
    //do not fail
    if (!(timeout > now) && !powerOn) {
      Logger.warn("Skipping Ghost detection test due to time out on Power ON");
      return;
    }
    assertTrue(timeout > now);
    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    AutoPilot.startAutoMode();
    pause(150);

    assertTrue(AutoPilot.isAutoModeActive());

    BlockBean block1 = ps.getBlockByTileId("bk-1");
    block1.setBlockState(BlockBean.BlockState.FREE);

    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    //Toggle a sensor, as the automode is on a Ghost should appear
    //Now lets Toggle the enter sensor
    SensorBean s2 = ps.getSensor("0-0002");
    toggleSensorDirect(s2);

    assertFalse(JCS.getJcsCommandStation().isPowerOn());

    block1 = ps.getBlockByTileId("bk-1");
    block1.setBlockState(BlockBean.BlockState.GHOST);
  }

  //@Test
  public void testGetLocomotiveDispatchers() {
    if (this.skipTest) {
      return;
    }
    System.out.println("getLocomotiveDispatchers");
    //AutoPilot instance = AutoPilot.getInstance();
    AutoPilot.startAutoMode();
    pause(150);

    if (!JCS.getJcsCommandStation().isPowerOn()) {
      Logger.warn("Skipping getLocomotiveDispatchers due to power OFF!");
      return;
    }

    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    assertTrue(AutoPilot.isAutoModeActive());

    List<LocomotiveBean> onTraclLocos = AutoPilot.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());

    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    block1.setBlockState(BlockBean.BlockState.OCCUPIED);
    ps.persist(block1);

    List<LocomotiveBean> expected = new ArrayList<>();
    expected.add(ns1631);

    onTraclLocos = AutoPilot.getOnTrackLocomotives();
    assertEquals(expected, onTraclLocos);

    AutoPilot.prepareAllDispatchers();
    List<Dispatcher> dispList = AutoPilot.getLocomotiveDispatchers();
    assertEquals(1, dispList.size());

    assertFalse(AutoPilot.areDispatchersRunning());

    AutoPilot.stopAutoMode();
    //let the autopilot finish...
    pause(1000);
    assertFalse(AutoPilot.isAutoModeActive());

    dispList = AutoPilot.getLocomotiveDispatchers();
    assertEquals(1, dispList.size());

    AutoPilot.clearDispatchers();

    dispList = AutoPilot.getLocomotiveDispatchers();
    assertEquals(0, dispList.size());
  }

  //@Test
  public void testGetLocomotiveDispatcher() {
    if (this.skipTest) {
      return;
    }
    System.out.println("getLocomotiveDispatcher");
    //AutoPilot instance = AutoPilot.getInstance();

    List<LocomotiveBean> onTraclLocos = AutoPilot.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());
    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    ps.persist(block1);

    AutoPilot.startAutoMode();
    pause(50);
    AutoPilot.prepareAllDispatchers();

    Dispatcher disp = AutoPilot.getLocomotiveDispatcher(ns1631);
    assertNotNull(disp);
    assertEquals("NS 1631", disp.getName());
  }

  //@Test
  public void testResetDispatcher() {
    System.out.println("resetDispatcher");
    LocomotiveBean locomotiveBean = null;
    AutoPilot instance = null;
    AutoPilot.resetDispatcher(locomotiveBean);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testStartAllLocomotives() {
    System.out.println("startAllLocomotives");
    AutoPilot instance = null;
    AutoPilot.startAllLocomotives();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testStopAllLocomotives() {
    System.out.println("stopAllLocomotives");
    AutoPilot instance = null;
    AutoPilot.stopAllLocomotives();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testResetStates() {
    System.out.println("resetStates");
    AutoPilot instance = null;
    AutoPilot.resetStates();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testIsRunning_0args() {
    System.out.println("isRunning");
    AutoPilot instance = null;
    boolean expResult = false;
    boolean result = instance.isAutoModeActive();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testIsRunning_LocomotiveBean() {
    System.out.println("isRunning");
    LocomotiveBean locomotive = null;
    AutoPilot instance = null;
    boolean expResult = false;
    boolean result = instance.isRunning(locomotive);
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  private void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }

  private class TestSensorHandler implements SensorEventHandler {

    private final String sensorId;
    private final AutoPilotTest autoPilotTest;

    TestSensorHandler(String sensorId, AutoPilotTest autoPilotTest) {
      this.sensorId = sensorId;
      this.autoPilotTest = autoPilotTest;
    }

    @Override
    public void handleEvent(SensorEvent event) {
      if (this.sensorId.equals(event.getId())) {
        this.autoPilotTest.sensorHandlerEvents.add(event);
      }
    }

    @Override
    public String getSensorId() {
      return sensorId;
    }

  }

  private void toggleSensorDirect(SensorBean sensorBean) {
    sensorBean.toggle();
    sensorBean.setActive((sensorBean.getStatus() == 1));
    SensorEvent sensorEvent = new SensorEvent(sensorBean);
    fireFeedbackEvent(sensorEvent);
  }

  private void fireFeedbackEvent(SensorEvent sensorEvent) {
    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
    for (FeedbackController fbc : acl) {
      fbc.fireSensorEventListeners(sensorEvent);
    }
  }

  @BeforeAll
  public static void beforeAll() {
    Logger.trace("####################### AutoPilot Test ##############################");

  }

  @AfterAll
  public static void assertOutput() {
    AutoPilot.stopAutoMode();

    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;
    boolean autoPilotRunning = AutoPilot.isAutoModeActive();
    while (autoPilotRunning && timeout > now) {
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertFalse(AutoPilot.isAutoModeActive());
    AutoPilot.clearDispatchers();

    Logger.info("Autopilot Reset in " + (now - start) + " ms.");
    Logger.trace("^^^^^^^^^^^^^^^^^^^^^^^^ AutoPilot Test ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
  }

}
