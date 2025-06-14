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
package jcs.commandStation.autopilot.state;

import java.util.List;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.tinylog.Logger;

@TestMethodOrder(OrderAnnotation.class)
public class StateMachineStepByStepTest {

  private static final Long NS_DHG_6505 = 7L;
  private static final Long BR_101_003_2 = 23L;
  private static final Long NS_1631 = 39L;

  private final PersistenceTestHelper testHelper;
  private final PersistenceService ps;
  private LocomotiveBean dhg;
  private LocomotiveBean ns1631;
  private Dispatcher dispatcher;

  public StateMachineStepByStepTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    //Switch the Virtual Simulator OFF as it will interfeare with this step test
    System.setProperty("do.not.simulate.virtual.drive", "true");
    System.setProperty("state.machine.stepTest", "true");

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
    if (JCS.getJcsCommandStation().connect()) {

      JCS.getJcsCommandStation().switchPower(true);
      AutoPilot.runAutoPilot(true);
      Logger.info("=========================== setUp done..............");
    } else {
      Logger.error("###### Can't connect to command station! ########");
    }
  }

  @AfterEach
  public void tearDown() {
    Logger.info("=========================== Teardown..............");
    AutoPilot.runAutoPilot(false);
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

  private void setupbk1bkNsDHG() {
    dhg = ps.getLocomotive(NS_DHG_6505);

    BlockBean block1 = ps.getBlockByTileId("bk-1");
    block1.setLocomotive(dhg);
    block1.setBlockState(BlockBean.BlockState.OCCUPIED);
    block1.setAlwaysStop(true);
    ps.persist(block1);

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    block4.setAlwaysStop(true);
    ps.persist(block4);

    //force routes from bk-1 to bk-4 and bk-4 to bk-1
    BlockBean block2 = ps.getBlockByTileId("bk-2");
    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block2);

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    block3.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block3);

    AutoPilot.prepareAllDispatchers();

    dispatcher = AutoPilot.getLocomotiveDispatcher(dhg);
    Logger.trace("Prepared layout");
  }

  private void setupbk2bkNs1631() {
    ns1631 = ps.getLocomotive(NS_1631);

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    block2.setLocomotive(ns1631);
    block2.setBlockState(BlockBean.BlockState.OCCUPIED);
    block2.setAlwaysStop(true);
    ps.persist(block2);

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    block3.setAlwaysStop(true);
    ps.persist(block3);

    //force routes from bk-2 to bk-3 and bk-3 to bk-2
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    block1.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block1);

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    block4.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block4);

    AutoPilot.prepareAllDispatchers();
    dispatcher = AutoPilot.getLocomotiveDispatcher(ns1631);
    Logger.trace("Prepared layout");
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

  private void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }

  @Test
  @Order(1)
  public void testBk1ToBk4() {
    //StateMachine functionality test, runs in 1 single thread.
    //Each execution step should be manually performed.
    //Lets drive with the DHD loc from bk-1 to bk-4
    Logger.info("Bk1ToBk4");
    setupbk1bkNsDHG();

    //Check block statuses
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    StateMachine stateMachine = dispatcher.getStateMachine();

    //Start from bk-1
    assertEquals(NS_DHG_6505, block1.getLocomotiveId());
    //Destination bk-4
    assertNull(block4.getLocomotiveId());
    assertNull(dispatcher.getRouteBean());

    //Thread should NOT run!
    assertFalse(stateMachine.isThreadRunning());
    assertFalse(stateMachine.isAutomodeEnabled());
    assertEquals("IdleState", stateMachine.getDispatcherStateName());

    //Execute IdleState
    stateMachine.handleState();
    //Automode is off should stay Idle
    assertEquals("IdleState", stateMachine.getDispatcherStateName());

    //Departure
    //Automode should be enabled
    stateMachine.setEnableAutomode(true);
    assertTrue(stateMachine.isAutomodeEnabled());

    //Execute IdleState again
    stateMachine.handleState();

    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", stateMachine.getDispatcherStateName());

    //execute the PrepareRouteState
    stateMachine.handleState();

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    //Block 4, destination block should be reserved for DHG to come
    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    //Speed shoul still be zero as the startState has not been executed
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    //After executing the status should be still be StartState
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    //Execute the StartState
    stateMachine.handleState();
    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());
    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Loc should start
    assertEquals(700, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //State should stay the same as the enter sensor of the destination is not hit.
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Now lets Toggle the enter sensor
    Integer enterSensorId = dispatcher.getEnterSensorId();
    assertEquals(13, enterSensorId);
    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));

    SensorBean enterSensor = ps.getSensor(enterSensorId);
    toggleSensorDirect(enterSensor);

    //Execute the StartState
    stateMachine.handleState();
    //State should be advanced to EnterBlock
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());

    //Execute the EnterState
    stateMachine.handleState();

    //Loc should be slowing down
    assertEquals(100, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.INBOUND, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Execute the EnterState
    stateMachine.handleState();
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());

    //Now lets Toggle the in sensor
    Integer inSensorId = dispatcher.getInSensorId();
    assertEquals(12, inSensorId);
    //Check if the inSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(inSensorId));

    //Toggle the IN sensor
    SensorBean inSensor = ps.getSensor(inSensorId);
    toggleSensorDirect(inSensor);

    //Execute the EnterState
    stateMachine.handleState();
    assertEquals("InBlockState", stateMachine.getDispatcherStateName());

    //Execute the InBlockState
    stateMachine.handleState();

    //Loc should be stopped
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertNull(dispatcher.getRouteBean());
    assertNull(dispatcher.getDestinationBlock());

    assertEquals("bk-4", dispatcher.getDepartureBlock().getId());

    assertEquals("WaitState", stateMachine.getDispatcherStateName());

    //Disable automode which should jump to Idle state
    dispatcher.stopLocomotiveAutomode();

    assertFalse(dispatcher.isLocomotiveAutomodeOn());

    //Execute the WaitState, should jump to Idle
    //Execute the InBlockState
    stateMachine.handleState();

    //Should switch to Idle
    assertEquals("IdleState", stateMachine.getDispatcherStateName());
  }

  @Test
  @Order(2)
  public void testFromBk1ToBk4andViceVersa() {
    Logger.info("fromBk1ToBk4andViceVersa");
    setupbk1bkNsDHG();

    //Check block statuses
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());
    //Set a waiting time
    block4.setMaxWaitTime(3);
    block4.setMinWaitTime(3);
    ps.persist(block4);

    StateMachine stateMachine = dispatcher.getStateMachine();

    //Start from bk-1
    assertEquals(NS_DHG_6505, block1.getLocomotiveId());
    //Destination bk-4
    assertNull(block4.getLocomotiveId());
    assertNull(dispatcher.getRouteBean());

    //Thread should NOT run!
    assertFalse(stateMachine.isThreadRunning());
    assertFalse(stateMachine.isAutomodeEnabled());
    assertEquals("IdleState", stateMachine.getDispatcherStateName());

    //Execute IdleState
    stateMachine.handleState();
    //Automode is off should stay Idle
    assertEquals("IdleState", stateMachine.getDispatcherStateName());

    //Departure
    //Automode should be enabled
    stateMachine.setEnableAutomode(true);
    assertTrue(stateMachine.isAutomodeEnabled());

    //Execute IdleState again
    stateMachine.handleState();

    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", stateMachine.getDispatcherStateName());

    //execute the PrepareRouteState
    stateMachine.handleState();

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    //Block 4, destination block should be reserved for DHG to come
    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    //Speed shoul still be zero as the startState has not been executed
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    //After executing the status should be still be StartState
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    Integer occupancySensorId = dispatcher.getOccupationSensorId();
    Integer exitSensorId = dispatcher.getExitSensorId();
    Integer enterSensorId = dispatcher.getEnterSensorId();
    Integer inSensorId = dispatcher.getInSensorId();

    assertNotNull(occupancySensorId);
    assertNotNull(exitSensorId);
    assertNotNull(enterSensorId);
    assertNotNull(inSensorId);

    assertEquals(1, occupancySensorId);
    assertEquals(2, exitSensorId);
    assertEquals(13, enterSensorId);
    assertEquals(12, inSensorId);

    //Execute the StartState
    stateMachine.handleState();
    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());
    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Loc should start
    assertEquals(700, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //State should stay the same as the enter sensor of the destination is not hit.
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Now lets Toggle the enter sensor
    assertEquals(13, enterSensorId);
    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));

    SensorBean enterSensor = ps.getSensor(enterSensorId);
    toggleSensorDirect(enterSensor);

    //Execute the StartState
    stateMachine.handleState();
    //State should be advanced to EnterBlock
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());

    //Execute the EnterState
    stateMachine.handleState();

    //Loc should be slowing down
    assertEquals(100, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.INBOUND, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Execute the EnterState
    stateMachine.handleState();
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());

    //Now lets Toggle the in sensor
    assertEquals(12, inSensorId);
    //Check if the inSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(inSensorId));

    //Toggle the IN sensor
    SensorBean inSensor = ps.getSensor(inSensorId);
    toggleSensorDirect(inSensor);

    //Execute the EnterState
    stateMachine.handleState();
    assertEquals("InBlockState", stateMachine.getDispatcherStateName());

    //Execute the InBlockState
    stateMachine.handleState();

    //Loc should be stopped
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertTrue(block4.isAlwaysStop());

    assertNull(dispatcher.getRouteBean());
    assertNull(dispatcher.getDestinationBlock());

    assertEquals("bk-4", dispatcher.getDepartureBlock().getId());

    assertEquals("WaitState", stateMachine.getDispatcherStateName());

    //Execute Wait
    stateMachine.handleState();

    //Check block statuses
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    //Start cycle again
    assertEquals("PrepareRouteState", stateMachine.getDispatcherStateName());

    //Departure block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    //Destination block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Execute the 2nd PrepareRoute the way back...
    stateMachine.handleState();

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-4+]->[bk-1-]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    occupancySensorId = dispatcher.getOccupationSensorId();
    exitSensorId = dispatcher.getExitSensorId();
    enterSensorId = dispatcher.getEnterSensorId();
    inSensorId = dispatcher.getInSensorId();

    assertNotNull(occupancySensorId);
    assertNotNull(exitSensorId);
    assertNotNull(enterSensorId);
    assertNotNull(inSensorId);

    assertEquals(1, inSensorId);
    assertEquals(2, enterSensorId);
    assertEquals(13, exitSensorId);
    assertEquals(12, occupancySensorId);

    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Execute the StartState
    stateMachine.handleState();

    //Departure block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    //Destination block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.LOCKED, block1.getBlockState());

    assertEquals(700, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertEquals(NS_DHG_6505, block1.getLocomotiveId());

    //State should stay the same as the enter sensor of the destination is not het.
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Execute the StartState
    stateMachine.handleState();
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Now lets Toggle the enter sensor
    enterSensor = ps.getSensor(enterSensorId);
    toggleSensorDirect(enterSensor);

    //Execute the StartState
    stateMachine.handleState();
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());

    //Execute the EnterBlockState
    stateMachine.handleState();
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());

    //Loc should be slowing down
    assertEquals(100, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.INBOUND, block1.getBlockState());
    //Destination block state

    assertEquals(NS_DHG_6505, block1.getLocomotiveId());

    //Execute the EnterState
    stateMachine.handleState();
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());

    //Togggle the IN sensor
    inSensor = ps.getSensor(inSensorId);
    toggleSensorDirect(inSensor);

    //Execute the EnterState
    stateMachine.handleState();
    assertEquals("InBlockState", stateMachine.getDispatcherStateName());

    //Execute the InBlockState
    stateMachine.handleState();

    //Loc should be stopped
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    //Destination block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    assertEquals(NS_DHG_6505, block1.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertNull(dispatcher.getRouteBean());
    assertNull(dispatcher.getDestinationBlock());

    assertEquals("bk-1", dispatcher.getDepartureBlock().getId());

    //Result of the InBlockState execution should be Wait
    assertEquals("WaitState", stateMachine.getDispatcherStateName());//    stateMachine.setEnableAutomode(false);

    //Automode OFF!
    stateMachine.setEnableAutomode(false);
    assertFalse(stateMachine.isAutomodeEnabled());

    //Execute the WaitState
    stateMachine.handleState();
    //Should switch to Idle
    assertEquals("IdleState", stateMachine.getDispatcherStateName());
  }

  //TODO !!!!!!
  //@Test
  @Order(3)
  public void testFromBk1ToBk4Gost() {
    Logger.info("fromBk1ToBk4Gost");
    setupbk1bkNsDHG();

    //Check block statuses
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    StateMachine stateMachine = dispatcher.getStateMachine();

    //Start from bk-1
    assertEquals(NS_DHG_6505, block1.getLocomotiveId());
    //Destination bk-4
    assertNull(block4.getLocomotiveId());
    assertNull(dispatcher.getRouteBean());

    //Thread should NOT run!
    assertFalse(stateMachine.isThreadRunning());
    assertFalse(stateMachine.isAutomodeEnabled());
    assertEquals("IdleState", stateMachine.getDispatcherStateName());

    //Execute IdleState
    stateMachine.handleState();
    //Automode is off should stay Idle
    assertEquals("IdleState", stateMachine.getDispatcherStateName());

    //Departure
    //Automode should be enabled
    stateMachine.setEnableAutomode(true);
    assertTrue(stateMachine.isAutomodeEnabled());

    //Execute IdleState again
    stateMachine.handleState();

    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", stateMachine.getDispatcherStateName());

    //execute the PrepareRouteState
    stateMachine.handleState();

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    //Block 4, destination block should be reserved for DHG to come
    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    //Speed shoul still be zero as the startState has not been executed
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    //After executing the status should be still be StartState
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    //Execute the StartState
    stateMachine.handleState();
    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());
    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Loc should start
    assertEquals(700, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //State should stay the same as the enter sensor of the destination is not hit.
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Now lets Toggle and 'unexpected' sensor, which should cause a Ghost!
    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    Integer inSensorId = dispatcher.getInSensorId();
    assertNotEquals(13, inSensorId);

    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertFalse(AutoPilot.isSensorHandlerRegistered(inSensorId));

    SensorBean inSensor = ps.getSensor(inSensorId);
    toggleSensorDirect(inSensor);

    assertTrue(AutoPilot.isGostDetected());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.GHOST, block4.getBlockState());

    assertFalse(JCS.getJcsCommandStation().isPowerOn());

    //Execute the StartState
    stateMachine.handleState();
    assertEquals("StartState", stateMachine.getDispatcherStateName());
  }

  @Test
  @Order(3)
  public void testReset() {
    Logger.info("reset");
    setupbk2bkNs1631();

    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OCCUPIED, block2.getBlockState());

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.FREE, block3.getBlockState());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block4.getBlockState());

    StateMachine instance = dispatcher.getStateMachine();

    //Start from bk-2
    assertEquals(NS_1631, block2.getLocomotiveId());

    //Destination bk-3
    assertNull(block3.getLocomotiveId());
    assertNull(dispatcher.getRouteBean());

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isAutomodeEnabled());
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Execute IdleState
    instance.handleState();
    //Automode is off should stay Idle
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Departure 
    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OCCUPIED, block2.getBlockState());
    //Destination block state
    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.FREE, block3.getBlockState());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block1.getBlockState());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block4.getBlockState());

    //Automode ON!
    instance.setEnableAutomode(true);
    assertTrue(instance.isAutomodeEnabled());
    assertFalse(instance.isThreadRunning());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block1.getBlockState());

    //Execute IdleState
    instance.handleState();
    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block1.getBlockState());

    //execute the PrepareRouteState
    instance.handleState();

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block1.getBlockState());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block4.getBlockState());

    //Check the results of the PrepareRouteState execution
    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-2+]->[bk-3-]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OCCUPIED, block2.getBlockState());

    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.LOCKED, block3.getBlockState());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block1.getBlockState());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block4.getBlockState());

    assertEquals(NS_1631, block3.getLocomotiveId());
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    //After executing the status should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    //Execute the StartState
    instance.handleState();

    //Check the result of the StartState execution
    //Departure block state
    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUTBOUND, block2.getBlockState());
    //Destination block state
    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.LOCKED, block3.getBlockState());

    assertEquals(700, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block1.getBlockState());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block4.getBlockState());

    //State should stay the same as the enter sensor of the destination is not het.
    assertEquals("StartState", instance.getDispatcherStateName());

    instance.resetStateMachine();

    //State should be reset to Idle.
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Departure block state
    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OCCUPIED, block2.getBlockState());

    //Destination block state
    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.FREE, block3.getBlockState());

    assertNull(block3.getLocomotiveId());

    //Loc should be stopped
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    assertNull(dispatcher.getRouteBean());
    assertNull(dispatcher.getDestinationBlock());

    assertFalse(instance.isAutomodeEnabled());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block1.getBlockState());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block4.getBlockState());
  }

}
