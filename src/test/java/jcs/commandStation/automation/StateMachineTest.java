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
package jcs.commandStation.automation;

import java.util.List;
import jcs.JCS;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.tinylog.Logger;

/**
 * Test the State machine
 */
public class StateMachineTest {

  protected final PersistenceTestHelper testHelper;
  protected final PersistenceService ps;
  @SuppressWarnings("unused")
  private List<Tile> tiles;
  //private int eventCallbackCount = 0;

  SensorMonitor sensorMonitor;
  private final RailwayController railwayController;

  protected static final long BR_101_003_2 = 23;
  protected static final long NS_1631 = 39;

  @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
  public StateMachineTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    //Switch the Virtual Simulator OFF as it will interfere with this step test
    System.setProperty("do.not.simulate.virtual.drive", "true");
    //Don't let the thread run the execution
    System.setProperty("state.machine.stepTest", "true");

    testHelper = PersistenceTestHelper.getInstance();
    testHelper.runTestDataInsertScript("autopilot_queue_test.sql");

    ps = PersistenceFactory.getService();

    if (JCS.getJcsCommandStation().connect()) {

      JCS.getJcsCommandStation().switchPower(true);
      tiles = TileCache.loadTiles(true);
    } else {
      Logger.error("###### Can't connect to command station! ########");
    }

    railwayController = RailwayController.getInstance();
    railwayController.setAutomodeOn(true);

    sensorMonitor = new SensorMonitor();
    railwayController.setSensorMonitor(sensorMonitor);
    //Register the sensors
    sensorMonitor.registerAllSensors();
    //sensorMonitor.start();

    railwayController.prepareAllDispatchers();

  }

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    testHelper.runTestDataInsertScript("autopilot_queue_test.sql");

    if (JCS.getJcsCommandStation().connect()) {

      JCS.getJcsCommandStation().switchPower(true);
      tiles = TileCache.loadTiles(true);
    }
  }

  @AfterEach
  public void tearDown() {
    TileCache.flush();
//    Dispatcher ns1631 = railwayController.getDispatcher((int) NS_1631);
//    ns1631.stopLocomotive();
//    ns1631.disable();
//    ns1631.getRailwayController().getSensorMonitor().stopMonitor();
  }

  void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void toggleSensor(Integer sensorId) {
    SensorBean sensorBean = ps.getSensor(sensorId);
    //Create a sensorEvent with change
    sensorBean.setActive(true);
    sensorBean.setPreviousActive(false);
    SensorEvent sensorEvent = new SensorEvent(sensorBean);

    sensorMonitor.handleSensorEvent(sensorEvent);
  }

  @Order(1)
  @Test
  public void testIdle() {
    System.out.println("Idle");
    Dispatcher ns1631 = railwayController.getDispatcher((int) NS_1631);
    assertNotNull(ns1631);

    //The enablement of the dispatcher will create a statemachine
    ns1631.enable();
    StateMachine stateMachine = ns1631.getStateMachine();

    assertEquals("Idle", stateMachine.getCurrentStateName());

    stateMachine.executeState();
    assertEquals("Idle", stateMachine.getCurrentStateName());
    assertFalse(ns1631.isLocomotiveStarted());
  }

  @Order(2)
  @Test
  public void testIdleToWait() {
    System.out.println("IdleToWait");
    Dispatcher ns1631 = railwayController.getDispatcher((int) NS_1631);
    assertNotNull(ns1631);

    //Make sure the locomotive does not have a destination
    //by setting the target blocks out of order
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
    block1.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.FREE, block2.getBlockState());
    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block1);
    ps.persist(block2);

    ns1631.enable();
    StateMachine stateMachine = ns1631.getStateMachine();

    assertEquals("Idle", stateMachine.getCurrentStateName());

    stateMachine.executeState();
    assertEquals("Idle", stateMachine.getCurrentStateName());

    //For testing
    ns1631.setLocomotiveStarted(true);

    stateMachine.executeState();
    assertEquals("PrepareRoute", stateMachine.getCurrentStateName());

    assertTrue(ns1631.isLocomotiveStarted());
    assertNull(ns1631.getDestinationBlock());

    stateMachine.executeState();
    assertEquals("Waiting", stateMachine.getCurrentStateName());

    //Stop the dispatcher!
    ns1631.stopLocomotive();
    assertFalse(ns1631.isLocomotiveStarted());
  }

  @Order(3)
  @Test
  public void testIdleToStarting() {
    System.out.println("IdleToStarting");
    Dispatcher ns1631 = railwayController.getDispatcher((int) NS_1631);
    assertNotNull(ns1631);

    //Make sure the locomotive does have 1 destination
    //by setting 1 of the target blocks out of order
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.FREE, block2.getBlockState());
    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block2);

    ns1631.enable();
    StateMachine stateMachine = ns1631.getStateMachine();

    stateMachine.getCurrentState().onEnter(ns1631);

    assertEquals("Idle", stateMachine.getCurrentStateName());
    assertFalse(ns1631.isLocomotiveStarted());

    stateMachine.executeState();
    assertEquals("Idle", stateMachine.getCurrentStateName());

    //For testing
    ns1631.setLocomotiveStarted(true);
    assertTrue(ns1631.isLocomotiveStarted());

    stateMachine.executeState();
    assertEquals("PrepareRoute", stateMachine.getCurrentStateName());

    stateMachine.executeState();

    //check the route
    assertNotNull(ns1631.getDestinationBlock());
    assertNotNull(ns1631.getRouteBean());
    assertEquals("[bk-4+]->[bk-1-]", ns1631.getRouteBean().getId());

    assertEquals("Starting", stateMachine.getCurrentStateName());

    //Disabling the dispatcher and stopping the locomotive should not be possible
    ns1631.stopLocomotive();
    assertTrue(ns1631.isLocomotiveStarted());

    //To build a clean next test the state machine should be reset.
    stateMachine.reset();
    assertEquals("Idle", stateMachine.getCurrentStateName());
    assertFalse(ns1631.isLocomotiveStarted());
  }

  @Order(4)
  @Test
  public void testOneFullDrivewayStop() {
    System.out.println("OneFullDrivewayStop");
    Dispatcher ns1631 = railwayController.getDispatcher((int) NS_1631);
    assertNotNull(ns1631);

    //Make sure the locomotive does have 1 destination
    //by setting 1 of the target blocks out of order
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
    block1.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block1);

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.FREE, block2.getBlockState());
    block2.setAlwaysStop(true);
    ps.persist(block2);

    ns1631.enable();
    StateMachine stateMachine = ns1631.getStateMachine();

    assertEquals("Idle", stateMachine.getCurrentStateName());
    stateMachine.executeState();

    ns1631.setLocomotiveStarted(true);
    assertTrue(ns1631.isLocomotiveStarted());

    stateMachine.executeState();
    assertEquals("PrepareRoute", stateMachine.getCurrentStateName());

    stateMachine.executeState();
    assertEquals("Starting", stateMachine.getCurrentStateName());

    //check the route
    assertNotNull(ns1631.getDestinationBlock());
    assertNotNull(ns1631.getRouteBean());
    assertEquals("[bk-4+]->[bk-2-]", ns1631.getRouteBean().getId());

    //check route
    assertTrue(ns1631.getRouteBean().isLocked());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.LOCKED, block2.getBlockState());

    stateMachine.executeState();
    assertEquals("Running", stateMachine.getCurrentStateName());

    assertEquals(NS_1631, block2.getLocomotiveId());
    assertEquals(625, ns1631.getLocomotiveBean().getVelocity());

    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(ns1631.getDepartureBlock().getMinSensorId());
    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(ns1631.getDepartureBlock().getPlusSensorId());

    assertEquals(2, ns1631.getEnterSensorId());
    //assertEquals(3, ns1631.getInSensorId());
    assertEquals(2, ns1631.getInSensorId());
    ns1631.getSensorMonitor().isSensorRegisteredWithCallback(3);

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.LOCKED, block2.getBlockState());

    stateMachine.executeState();
    assertEquals("Running", stateMachine.getCurrentStateName());

    //Trigger the enter sensor
    toggleSensor(ns1631.getEnterSensorId());
    //Give the background sensor monitor thread a little time to process
    pause(50);

    stateMachine.executeState();
    assertEquals("Approaching", stateMachine.getCurrentStateName());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.INBOUND, block2.getBlockState());

    stateMachine.executeState();
    assertEquals("Braking", stateMachine.getCurrentStateName());

    assertEquals(NS_1631, block2.getLocomotiveId());
    assertEquals(83, ns1631.getLocomotiveBean().getVelocity());

    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(ns1631.getDepartureBlock().getMinSensorId());
    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(ns1631.getDepartureBlock().getPlusSensorId());

    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(2);
    ns1631.getSensorMonitor().isSensorRegisteredWithCallback(3);

    stateMachine.executeState();
    assertEquals("Braking", stateMachine.getCurrentStateName());

    //Trigger the in sensor
    toggleSensor(ns1631.getInSensorId());
    //Give the background sensor monitor thread a little time to process
    pause(50);

    stateMachine.executeState();
    assertEquals("Arrived", stateMachine.getCurrentStateName());

    //Check sensors
    assertNull(ns1631.getOccupationSensorId());
    assertNull(ns1631.getExitSensorId());

    block4 = ps.getBlockByTileId("bk-4");
    assertFalse(ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(block4.getMinSensorId()));
    assertFalse(ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(block4.getPlusSensorId()));

    assertEquals("bk-2", ns1631.getDepartureBlock().getId());
    assertTrue(ns1631.getSensorMonitor().getSubscribers().isEmpty());
    assertTrue(ns1631.getSensorMonitor().getSubscribersWithoutCallback().isEmpty());

    assertNull(ns1631.getDestinationBlock());

    assertEquals(0, ns1631.getLocomotiveBean().getVelocity());

    stateMachine.executeState();
    assertEquals("Waiting", stateMachine.getCurrentStateName());

    stateMachine.reset();
  }

  @Order(5)
  @Test
  public void testOneFullDrivewayContinue() {
    System.out.println("OneFullDrivewayContinue");
    Dispatcher ns1631 = railwayController.getDispatcher((int) NS_1631);
    assertNotNull(ns1631);

    //Make sure the locomotive does have 1 destination
    //by setting 1 of the target blocks out of order
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
    block1.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block1);

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.FREE, block2.getBlockState());
    block2.setAlwaysStop(false);
    ps.persist(block2);

    ns1631.enable();
    StateMachine stateMachine = ns1631.getStateMachine();

    assertEquals("Idle", stateMachine.getCurrentStateName());
    stateMachine.executeState();

    ns1631.setLocomotiveStarted(true);
    assertTrue(ns1631.isLocomotiveStarted());

    stateMachine.executeState();
    assertEquals("PrepareRoute", stateMachine.getCurrentStateName());

    stateMachine.executeState();
    assertEquals("Starting", stateMachine.getCurrentStateName());

    //check the route
    assertNotNull(ns1631.getDestinationBlock());
    assertNotNull(ns1631.getRouteBean());
    assertEquals("[bk-4+]->[bk-2-]", ns1631.getRouteBean().getId());

    //check route
    assertTrue(ns1631.getRouteBean().isLocked());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.LOCKED, block2.getBlockState());

    stateMachine.executeState();
    assertEquals("Running", stateMachine.getCurrentStateName());

    assertEquals(NS_1631, block2.getLocomotiveId());
    assertEquals(625, ns1631.getLocomotiveBean().getVelocity());

    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(ns1631.getDepartureBlock().getMinSensorId());
    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(ns1631.getDepartureBlock().getPlusSensorId());

    assertEquals(2, ns1631.getEnterSensorId());    
    //assertEquals(3, ns1631.getInSensorId());
    assertEquals(2, ns1631.getInSensorId());
    ns1631.getSensorMonitor().isSensorRegisteredWithCallback(3);

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.LOCKED, block2.getBlockState());

    stateMachine.executeState();
    assertEquals("Running", stateMachine.getCurrentStateName());

    //Trigger the enter sensor
    toggleSensor(ns1631.getEnterSensorId());
    //Give the background sensor monitor thread a little time to process
    pause(50);

    stateMachine.executeState();
    assertEquals("Approaching", stateMachine.getCurrentStateName());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.INBOUND, block2.getBlockState());

    stateMachine.executeState();
    assertEquals("PrepareNextRoute", stateMachine.getCurrentStateName());

    assertEquals(NS_1631, block2.getLocomotiveId());
    assertEquals(625, ns1631.getLocomotiveBean().getVelocity());

    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(ns1631.getDepartureBlock().getMinSensorId());
    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(ns1631.getDepartureBlock().getPlusSensorId());

    ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(2);
    ns1631.getSensorMonitor().isSensorRegisteredWithCallback(3);

//    stateMachine.executeState();
//    assertEquals("Braking", stateMachine.getCurrentStateName());
//
//    //Trigger the in sensor
//    toggleSensor(ns1631.getInSensorId());
//    //Give the background sensor monitor thread a little time to process
//    ns1631.pause(50);
//
//    stateMachine.executeState();
//    assertEquals("Arrived", stateMachine.getCurrentStateName());
//
//    //Check sensors
//    assertNull(ns1631.getOccupationSensorId());
//    assertNull(ns1631.getExitSensorId());
//
//    block4 = ps.getBlockByTileId("bk-4");
//    assertFalse(ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(block4.getMinSensorId()));
//    assertFalse(ns1631.getSensorMonitor().isSensorRegisteredWithoutCallback(block4.getPlusSensorId()));
//
//    assertEquals("bk-2", ns1631.getDepartureBlock().getId());
//    assertTrue(ns1631.getSensorMonitor().getSubscribers().isEmpty());
//    assertTrue(ns1631.getSensorMonitor().getSubscribersWithoutCallback().isEmpty());
//
//    assertNull(ns1631.getDestinationBlock());
//
//    assertEquals(0, ns1631.getLocomotiveBean().getVelocity());
//
//    stateMachine.executeState();
//    assertEquals("Waiting", stateMachine.getCurrentStateName());
    stateMachine.reset();
  }

  //@Order(5)
  //@Test
  public void testWait() {
    System.out.println("Wait");
    Dispatcher ns1631 = railwayController.getDispatcher((int) NS_1631);
    assertNotNull(ns1631);

    //Make sure the locomotive does not have a destination
    //by setting the target blocks out of order
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
    block1.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.FREE, block2.getBlockState());
    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block1);
    ps.persist(block2);

    //Must be enabled and starting    
    ns1631.enable();
    ns1631.startLocomotive();

    ns1631.enable();
    StateMachine stateMachine = ns1631.getStateMachine();

    stateMachine.getCurrentState().onEnter(ns1631);

    assertEquals("Waiting", stateMachine.getCurrentStateName());

    stateMachine.executeState();
    assertEquals("Waiting", stateMachine.getCurrentStateName());

    //Stop the dispatcher!
    ns1631.stopLocomotive();
    assertFalse(ns1631.isLocomotiveStarted());

  }

}
