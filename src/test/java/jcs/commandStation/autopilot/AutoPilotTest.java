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
import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileFactory;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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
  private Dispatcher dispatcher;
  
  private List<SensorEvent> sensorHandlerEvents;
  
  public AutoPilotTest() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
    testHelper.runTestDataInsertScript("autopilot_test_layout.sql");
    ps = PersistenceFactory.getService();
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
    JCS.getJcsCommandStation().switchPower(true);
  }
  
  @AfterEach
  public void tearDown() {
    AutoPilot.getInstance().stopAutoMode();
    //let the autopilot finish...
    pause(1000);
    AutoPilot.getInstance().clearDispatchers();
  }
  
  @Test
  public void testStartStopAutoMode() {
    System.out.println("startStopAutoMode");
    AutoPilot instance = AutoPilot.getInstance();
    assertFalse(instance.isAutoModeActive());
    
    instance.startAutoMode();
    pause(50);
    Logger.debug("Started automode");
    assertTrue(instance.isAutoModeActive());
    
    instance.stopAutoMode();
    //let the autopilot finish...
    pause(1000);
    assertFalse(instance.isAutoModeActive());
  }
  
  @Test
  public void testNoDispatchersRunning() {
    System.out.println("NoDispatchersRunning");
    AutoPilot instance = AutoPilot.getInstance();
    assertFalse(instance.isAutoModeActive());
    instance.startAutoMode();
    pause(50);
    assertTrue(instance.isAutoModeActive());
    assertFalse(instance.areDispatchersRunning());
    
    instance.stopAutoMode();
    assertFalse(instance.isAutoModeActive());
    //let the autopilot finish...
    pause(1000);
  }
  
  @Test
  public void testIsSensorRegistered() {
    System.out.println("isSensorRegistered");
    String sensorId = "0-0001";
    AutoPilot instance = AutoPilot.getInstance();
    assertFalse(instance.isAutoModeActive());
    
    assertFalse(instance.isSensorHandlerRegistered(sensorId));
    instance.startAutoMode();
    //let the autopilot start...
    pause(50);
    assertTrue(instance.isAutoModeActive());
    assertFalse(instance.isSensorHandlerRegistered(sensorId));
    TestSensorHandler testSensorHandler = new TestSensorHandler(sensorId, this);
    instance.addHandler(testSensorHandler, sensorId);
    
    assertTrue(instance.isSensorHandlerRegistered(sensorId));
    
    instance.stopAutoMode();
    assertFalse(instance.isAutoModeActive());
    //let the autopilot finish...
    pause(1000);
    assertFalse(instance.isSensorHandlerRegistered(sensorId));
  }
  
  @Test
  public void testGetOnTrackLocomotives() {
    System.out.println("getOnTrackLocomotives");
    //Check is the test script has run well
    List<Tile> tiles = TileFactory.toTiles(ps.getTileBeans(), false, false);
    assertEquals(29, tiles.size());
    List<BlockBean> blocks = ps.getBlocks();
    assertEquals(4, blocks.size());
    
    AutoPilot instance = AutoPilot.getInstance();
    instance.startAutoMode();
    pause(50);
    assertTrue(instance.isAutoModeActive());
    
    List<LocomotiveBean> onTraclLocos = instance.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());

    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    ps.persist(block1);
    
    List<LocomotiveBean> expected = new ArrayList<>();
    expected.add(ns1631);
    
    onTraclLocos = instance.getOnTrackLocomotives();
    assertEquals(expected, onTraclLocos);
    
    instance.stopAutoMode();
    assertFalse(instance.isAutoModeActive());
    //let the autopilot finish...
    pause(1000);
  }
  
  @Test
  public void testGhostDetection() {
    System.out.println("getGhostDetection");
    
    AutoPilot instance = AutoPilot.getInstance();
    instance.startAutoMode();
    pause(50);
    assertTrue(instance.isAutoModeActive());
    
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
  
  @Test
  public void testGetLocomotiveDispatchers() {
    System.out.println("getLocomotiveDispatchers");
    AutoPilot instance = AutoPilot.getInstance();
    instance.startAutoMode();
    pause(50);
    assertTrue(instance.isAutoModeActive());
    
    List<LocomotiveBean> onTraclLocos = instance.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());

    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    block1.setBlockState(BlockBean.BlockState.OCCUPIED);
    ps.persist(block1);
    
    List<LocomotiveBean> expected = new ArrayList<>();
    expected.add(ns1631);
    
    onTraclLocos = instance.getOnTrackLocomotives();
    assertEquals(expected, onTraclLocos);
    
    instance.prepareAllDispatchers();
    List<Dispatcher> dispList = instance.getLocomotiveDispatchers();
    assertEquals(1, dispList.size());
    
    assertFalse(instance.areDispatchersRunning());
    
    instance.stopAutoMode();
    //let the autopilot finish...
    pause(1000);
    assertFalse(instance.isAutoModeActive());
    
    dispList = instance.getLocomotiveDispatchers();
    assertEquals(1, dispList.size());
    
    instance.clearDispatchers();
    
    dispList = instance.getLocomotiveDispatchers();
    assertEquals(0, dispList.size());
  }
  
  @Test
  public void testGetLocomotiveDispatcher() {
    System.out.println("getLocomotiveDispatcher");
    AutoPilot instance = AutoPilot.getInstance();
    
    List<LocomotiveBean> onTraclLocos = instance.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());
    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    ps.persist(block1);
    
    instance.startAutoMode();
    pause(50);
    instance.prepareAllDispatchers();
    
    Dispatcher disp = instance.getLocomotiveDispatcher(ns1631);
    assertNotNull(disp);
    assertEquals("NS 1631", disp.getName());
  }

  //@Test
  public void testResetDispatcher() {
    System.out.println("resetDispatcher");
    LocomotiveBean locomotiveBean = null;
    AutoPilot instance = null;
    instance.resetDispatcher(locomotiveBean);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testStartAllLocomotives() {
    System.out.println("startAllLocomotives");
    AutoPilot instance = null;
    instance.startAllLocomotives();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testStopAllLocomotives() {
    System.out.println("stopAllLocomotives");
    AutoPilot instance = null;
    instance.stopAllLocomotives();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testResetStates() {
    System.out.println("resetStates");
    AutoPilot instance = null;
    instance.resetStates();
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
  
}
