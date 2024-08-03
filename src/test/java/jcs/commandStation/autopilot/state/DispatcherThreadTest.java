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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.util.PersistenceTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

public class DispatcherThreadTest {

  private static final Long NS_DHG_6505 = 7L;
  private static final Long BR_101_003_2 = 23L;
  private static final Long NS_1631 = 39L;

  private final PersistenceTestHelper testHelper;
  private final AutoPilot autoPilot;
  private LocomotiveBean dhg;
  private Dispatcher dispatcher;

  public DispatcherThreadTest() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();

    PersistenceFactory.getService();
    autoPilot = AutoPilot.getInstance();
    autoPilot.startAutoMode();
  }

  @BeforeEach
  public void setUp() {
    testHelper.runTestDataInsertScript("autopilot_test_layout.sql");
  }

  private void setupbk1bk4() {
    dhg = PersistenceFactory.getService().getLocomotive(NS_DHG_6505);

    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(dhg);
    block1.setBlockState(BlockBean.BlockState.OCCUPIED);
    PersistenceFactory.getService().persist(block1);

    //force routes from bk-1 to bk-4 and bk-4 to bk-1
    BlockBean block2 = PersistenceFactory.getService().getBlockByTileId("bk-2");
    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    PersistenceFactory.getService().persist(block2);

    BlockBean block3 = PersistenceFactory.getService().getBlockByTileId("bk-3");
    block3.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    PersistenceFactory.getService().persist(block3);

    autoPilot.prepareDispatchers();
    dispatcher = autoPilot.getLocomotiveDispatcher(dhg);
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testStartStopThreadRunning() {
    System.out.println("startStopThreadRunning");
    setupbk1bk4();
    DispatcherThread instance = dispatcher.getDispatcherThread();

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isAlive());

    assertEquals("DT->NS DHG 6505", instance.getName());
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.start();
    pause(10);

    Logger.debug("Dispatcher Thread Started");
    assertTrue(instance.isThreadRunning());
    assertTrue(instance.isAlive());
    assertFalse(instance.isLocomotiveAutomodeOn());

    instance.stopRunningThread();
    Logger.debug("Dispatcher Thread Stopped");
    assertFalse(instance.isThreadRunning());
    assertTrue(instance.isAlive());
  }

  @Test
  public void testStartStopLocomotiveAutomode() {
    System.out.println("startStopLocomotiveAutomode");
    setupbk1bk4();
    DispatcherThread instance = dispatcher.getDispatcherThread();

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertEquals("IdleState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.setLocomotiveAutomode(true);
    assertTrue(instance.isLocomotiveAutomodeOn());
    instance.handleStates(false);
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());

    SensorBean s13 = PersistenceFactory.getService().getSensor("0-0013");
    toggleSensorDirect(s13);
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    SensorBean s12 = PersistenceFactory.getService().getSensor("0-0012");
    toggleSensorDirect(s12);
    instance.handleStates(false);
    assertEquals("InBlockState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("WaitState", instance.getDispatcherStateName());

    instance.setLocomotiveAutomode(false);
    assertFalse(instance.isLocomotiveAutomodeOn());

    instance.handleStates(false);
    assertEquals("IdleState", instance.getDispatcherStateName());
  }

  @Test
  public void testStartStopLocomotiveAutomodeToAndFrom() {
    System.out.println("startStopLocomotiveAutomodeToAndFrom");
    setupbk1bk4();

    DispatcherThread instance = dispatcher.getDispatcherThread();

    //Start from bk-1
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(NS_DHG_6505, block1.getLocomotiveId());

    //Destination bk-4
    BlockBean block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertNull(block4.getLocomotiveId());
    assertNull(dispatcher.getRouteBean());

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Execute IdleState
    instance.handleStates(false);
    //Automode is off should stay Idle
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Departure 
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());
    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    //Automode ON!
    instance.setLocomotiveAutomode(true);
    assertTrue(instance.isLocomotiveAutomodeOn());

    //Execute IdleState
    instance.handleStates(false);
    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());
    //execute the PrepareRouteState
    instance.handleStates(false);

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    //After executing the status should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    //Execute the StartState
    instance.handleStates(false);

    //Check the result of the StartState execution
    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    assertEquals(750, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //State should stay the same as the enter sensor of the destination is not het.
    assertEquals("StartState", instance.getDispatcherStateName());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());

    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Execute the StartState
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());

    //Now lets Toggle the enter sensor
    SensorBean s13 = PersistenceFactory.getService().getSensor("0-0013");
    toggleSensorDirect(s13);

    //Execute the StartState
    instance.handleStates(false);
    //State shoul be advanced to EnterBlock
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    //Execute the EnterState
    instance.handleStates(false);

    //Loc should be slowing down
    assertEquals(100, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.INBOUND, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Execute the EnterState
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    //Toggle the IN sensor
    SensorBean s12 = PersistenceFactory.getService().getSensor("0-0012");
    toggleSensorDirect(s12);

    //Execute the EnterState
    instance.handleStates(false);
    assertEquals("InBlockState", instance.getDispatcherStateName());

    //Execute the InBlockState
    instance.handleStates(false);

    //Loc should be stopped
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertNull(dispatcher.getRouteBean());
    assertNull(dispatcher.getDestinationBlock());

    assertEquals("bk-4", dispatcher.getDepartureBlock().getId());

    //Result of the InBlockState execution should be Wait
    assertEquals("WaitState", instance.getDispatcherStateName());

    //Execute Wait
    instance.handleStates(false);

    assertEquals("PrepareRouteState", instance.getDispatcherStateName());

    //Departure block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    //Destination block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Execute the 2nd PrepareRoute
    instance.handleStates(false);

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-4+]->[bk-1-]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    assertEquals("StartState", instance.getDispatcherStateName());

    //Execute the StartState
    instance.handleStates(false);
    //Check the result of the StartState execution
    //Departure block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    //Destination block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.LOCKED, block1.getBlockState());

    assertEquals(750, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //State should stay the same as the enter sensor of the destination is not het.
    assertEquals("StartState", instance.getDispatcherStateName());

    //Departure block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    //Destination block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.LOCKED, block1.getBlockState());

    assertEquals(NS_DHG_6505, block1.getLocomotiveId());

    //Execute the StartState
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());

    //Now lets Toggle the enter sensor
    SensorBean s2 = PersistenceFactory.getService().getSensor("0-0002");
    toggleSensorDirect(s2);

    //Execute the StartState
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    //Execute the EnterBlockState
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    //Execute the EnterState
    instance.handleStates(false);

    //Loc should be slowing down
    assertEquals(100, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.INBOUND, block1.getBlockState());
    //Destination block state

    assertEquals(NS_DHG_6505, block1.getLocomotiveId());

    //Execute the EnterState
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    //Togggle the IN sensor
    SensorBean s1 = PersistenceFactory.getService().getSensor("0-0001");
    toggleSensorDirect(s1);

    //Execute the EnterState
    instance.handleStates(false);
    assertEquals("InBlockState", instance.getDispatcherStateName());

    //Execute the InBlockState
    instance.handleStates(false);

    //Loc should be stopped
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    //Destination block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    assertEquals(NS_DHG_6505, block1.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertNull(dispatcher.getRouteBean());
    assertNull(dispatcher.getDestinationBlock());

    assertEquals("bk-1", dispatcher.getDepartureBlock().getId());

    //Result of the InBlockState execution should be Wait
    assertEquals("WaitState", instance.getDispatcherStateName());//    instance.setLocomotiveAutomode(false);

    //Automode OFF!
    instance.setLocomotiveAutomode(false);
    assertFalse(instance.isLocomotiveAutomodeOn());

    //Execute the WaitState
    instance.handleStates(false);
    //Should switch to Idle
    assertEquals("IdleState", instance.getDispatcherStateName());
  }

  @Test
  public void testStartStopLocomotiveAutomodeToOnly() {
    System.out.println("startStopLocomotiveAutomodeToOnly");
    setupbk1bk4();

    DispatcherThread instance = dispatcher.getDispatcherThread();

    //Start from bk-1
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(NS_DHG_6505, block1.getLocomotiveId());

    //Destination bk-4
    BlockBean block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertNull(block4.getLocomotiveId());
    assertNull(dispatcher.getRouteBean());

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Execute IdleState
    instance.handleStates(false);
    //Automode is off should stay Idle
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Departure 
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());
    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    //Automode ON!
    instance.setLocomotiveAutomode(true);
    assertTrue(instance.isLocomotiveAutomodeOn());

    //Execute IdleState
    instance.handleStates(false);
    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());
    //execute the PrepareRouteState
    instance.handleStates(false);

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    //After executing the status should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    //Automode OFF!
    instance.setLocomotiveAutomode(false);
    assertFalse(instance.isLocomotiveAutomodeOn());

    //Execute the StartState
    instance.handleStates(false);

    //Check the result of the StartState execution
    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    assertEquals(750, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //State should stay the same as the enter sensor of the destination is not het.
    assertEquals("StartState", instance.getDispatcherStateName());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());

    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Execute the StartState
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());

    //Now lets Toggle the enter sensor
    SensorBean s13 = PersistenceFactory.getService().getSensor("0-0013");
    toggleSensorDirect(s13);

    //Execute the StartState
    instance.handleStates(false);
    //State shoul be advanced to EnterBlock
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    //Execute the EnterState
    instance.handleStates(false);

    //Loc should be slowing down
    assertEquals(100, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.INBOUND, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Execute the EnterState
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    //Toggle the IN sensor
    SensorBean s12 = PersistenceFactory.getService().getSensor("0-0012");
    toggleSensorDirect(s12);

    //Execute the EnterState
    instance.handleStates(false);
    assertEquals("InBlockState", instance.getDispatcherStateName());

    //Execute the InBlockState
    instance.handleStates(false);

    //Loc should be stopped
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertNull(dispatcher.getRouteBean());
    assertNull(dispatcher.getDestinationBlock());

    assertEquals("bk-4", dispatcher.getDepartureBlock().getId());

    //Should switch to Idle
    assertEquals("IdleState", instance.getDispatcherStateName());
  }

  @Test
  public void testReset() {
    System.out.println("reset");
    setupbk1bk4();

    DispatcherThread instance = dispatcher.getDispatcherThread();

    //Start from bk-1
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(NS_DHG_6505, block1.getLocomotiveId());

    //Destination bk-4
    BlockBean block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertNull(block4.getLocomotiveId());
    assertNull(dispatcher.getRouteBean());

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Execute IdleState
    instance.handleStates(false);
    //Automode is off should stay Idle
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Departure 
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());
    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    //Automode ON!
    instance.setLocomotiveAutomode(true);
    assertTrue(instance.isLocomotiveAutomodeOn());

    //Execute IdleState
    instance.handleStates(false);
    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());
    //execute the PrepareRouteState
    instance.handleStates(false);

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    //After executing the status should be advanced to StartState
    assertEquals("StartState", instance.getDispatcherStateName());

    //Execute the StartState
    instance.handleStates(false);

    //Check the result of the StartState execution
    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    assertEquals(750, dispatcher.getLocomotiveBean().getVelocity());
    //assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //State should stay the same as the enter sensor of the destination is not het.
    assertEquals("StartState", instance.getDispatcherStateName());

    instance.resetStateMachine();

    //State should be reset to Idle.
    assertEquals("IdleState", instance.getDispatcherStateName());

    //Departure block state
    block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block4 = PersistenceFactory.getService().getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    assertNull(block4.getLocomotiveId());

    //Loc should be stopped
    assertEquals(0, dispatcher.getLocomotiveBean().getVelocity());

    assertNull(dispatcher.getRouteBean());
    assertNull(dispatcher.getDestinationBlock());

    assertFalse(instance.isLocomotiveAutomodeOn());

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
}
