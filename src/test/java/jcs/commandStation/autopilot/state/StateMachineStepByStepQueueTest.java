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
package jcs.commandStation.autopilot.state;

import jcs.JCS;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.entities.StationBean;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.tinylog.Logger;

/**
 * Test cases for the new Station (Queue) functionality.
 *
 */
@TestMethodOrder(OrderAnnotation.class)
public class StateMachineStepByStepQueueTest extends AbstractStateMachineStepByStepQueueTest {

  public StateMachineStepByStepQueueTest() {
    super();
  }

  @Order(1)
  @Test
  public void testCheckSetup() {
    Logger.info("Setup check");
    //There should be 4 blocks
    //2 blocks free 2 block occupied

    //Check block statuses
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.FREE, block2.getBlockState());

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OCCUPIED, block3.getBlockState());
    assertEquals(BR_101_003_2, block3.getLocomotiveId());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());
    assertEquals(NS_1631, block4.getLocomotiveId());
  }

  @Order(2)
  @Test
  public void testBk3ToBk1() {
    Logger.info("Bk3ToBk1");

    JCS.getJcsCommandStation().connect();
    JCS.getJcsCommandStation().switchPower(true);

    AutoPilot.prepareAllDispatchers();
    dispatcherBr101 = AutoPilot.getLocomotiveDispatcher(BR_101_003_2);
    dispatcherNS1631 = AutoPilot.getLocomotiveDispatcher(NS_1631);

    assertNull(dispatcherBr101.getNextRouteBean());
    assertNull(dispatcherBr101.getNextDestinationBlock());

    assertNull(dispatcherNS1631.getNextRouteBean());
    assertNull(dispatcherNS1631.getNextDestinationBlock());

    StateMachine br101StateMachine = dispatcherBr101.getStateMachine();
    //Start from bk-3
    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BR_101_003_2, block3.getLocomotiveId());

    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.FREE, block2.getBlockState());
    //Force the router to got to block 1
    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block2);

    //Thread should NOT run!
    assertFalse(br101StateMachine.isThreadRunning());
    assertFalse(br101StateMachine.isAutomodeEnabled());
    assertEquals("IdleState", br101StateMachine.getDispatcherStateName());

    //Execute IdleState
    br101StateMachine.handleState();
    //Automode is off should stay Idle
    assertEquals("IdleState", br101StateMachine.getDispatcherStateName());

    //Check the Queue properties
    StationBean station = ps.getStation(block1);

    assertEquals("station-1", station.getId());
    assertEquals(2, station.getMinLocomotives());
    assertEquals(0, station.getLocomotiveCount());

    station = ps.getStation(block2);

    assertEquals("station-1", station.getId());
    assertEquals(2, station.getMinLocomotives());
    assertEquals(0, station.getLocomotiveCount());

    //Departure
    //Automode should be enabled
    br101StateMachine.setEnableAutomode(true);
    assertTrue(br101StateMachine.isAutomodeEnabled());

    //Execute IdleState again
    br101StateMachine.handleState();

    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", br101StateMachine.getDispatcherStateName());

    //execute the PrepareRouteState
    br101StateMachine.handleState();

    //After executing the PrepareRouteState should be advanced to StartState
    assertEquals("StartingState", br101StateMachine.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    String routeId = dispatcherBr101.getRouteBean().getId();
    assertEquals("[bk-3+]->[bk-1-]", routeId);
    assertTrue(dispatcherBr101.getRouteBean().isLocked());

    //Departure block state
    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OCCUPIED, block3.getBlockState());

    //Destination block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.LOCKED, block1.getBlockState());

    //Block 1, destination block, should be reserved for BR_101 to come
    assertEquals(BR_101_003_2, block1.getLocomotiveId());

    //Speed should still be zero as the startState has not been executed
    assertEquals(0, dispatcherBr101.getLocomotiveBean().getVelocity());

    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    //Execute the RunningState
    br101StateMachine.handleState();
    assertEquals("RunningState", br101StateMachine.getDispatcherStateName());

    //Departure block state
    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUTBOUND, block3.getBlockState());
    //Destination block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.LOCKED, block1.getBlockState());
    assertEquals(BR_101_003_2, block1.getLocomotiveId());

    //Loc should start
    assertEquals(375, dispatcherBr101.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcherBr101.getLocomotiveBean().getDirection());

    //Now lets Toggle the enter sensor
    Integer enterSensorId = dispatcherBr101.getEnterSensorId();
    assertEquals(0, enterSensorId);

    //Execute the RunningState
    br101StateMachine.handleState();
    assertEquals("RunningState", br101StateMachine.getDispatcherStateName());
    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));

    SensorBean enterSensor = ps.getSensor(enterSensorId);
    toggleSensorDirect(enterSensor);

    assertNull(dispatcherBr101.getNextRouteBean());
    assertNull(dispatcherBr101.getNextDestinationBlock());

    //Execute the RunningState
    br101StateMachine.handleState();
    //State should be advanced to EnterBlock
    assertEquals("ApproachingState", br101StateMachine.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.LOCKED, block1.getBlockState());
    //Destination block state
    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUTBOUND, block3.getBlockState());
    assertEquals(BR_101_003_2, block3.getLocomotiveId());

    //Execute the EnterState
    br101StateMachine.handleState();
    assertEquals("BrakingState", br101StateMachine.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.INBOUND, block1.getBlockState());
    //Destination block state
    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUTBOUND, block3.getBlockState());

    br101StateMachine.handleState();
    assertEquals("BrakingState", br101StateMachine.getDispatcherStateName());

    //Loc should not slow down
    assertEquals(50, dispatcherBr101.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcherBr101.getLocomotiveBean().getDirection());

    //Execute the 50
    br101StateMachine.handleState();
    assertEquals("BrakingState", br101StateMachine.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.INBOUND, block1.getBlockState());
    //Destination block state
    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUTBOUND, block3.getBlockState());
    assertEquals(BR_101_003_2, block3.getLocomotiveId());

    //Next Destination block state
    assertNull(dispatcherBr101.getNextRouteBean());
    assertNull(dispatcherBr101.getNextDestinationBlock());

    //Execute the BrakingState
    br101StateMachine.handleState();
    assertEquals("BrakingState", br101StateMachine.getDispatcherStateName());

    //Now lets Toggle the in sensor
    Integer inSensorId = dispatcherBr101.getInSensorId();
    assertEquals(1, inSensorId);
    //Check if the inSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(inSensorId));

    //Toggle the IN sensor
    SensorBean inSensor = ps.getSensor(inSensorId);
    toggleSensorDirect(inSensor);

    //Execute the BrakingState
    br101StateMachine.handleState();
    assertEquals("InBlockState", br101StateMachine.getDispatcherStateName());

    br101StateMachine.handleState();
    assertEquals("WaitingState", br101StateMachine.getDispatcherStateName());

    block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.FREE, block3.getBlockState());
    assertNull(block3.getLocomotiveId());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());
    assertEquals(BR_101_003_2, block1.getLocomotiveId());
    //Loc should stop
    assertEquals(0, dispatcherBr101.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcherBr101.getLocomotiveBean().getDirection());

    //Check the Queue properties
    station = ps.getStation(block1);
    assertEquals("station-1", station.getId());
    assertEquals(2, station.getMinLocomotives());
    assertEquals(1, station.getLocomotiveCount());

    //Check whether the loc does not continue
    br101StateMachine.handleState();
    assertEquals("PrepareRouteState", br101StateMachine.getDispatcherStateName());
    br101StateMachine.handleState();
    assertEquals("WaitingState", br101StateMachine.getDispatcherStateName());

    br101StateMachine.handleState();
    assertEquals("PrepareRouteState", br101StateMachine.getDispatcherStateName());
    br101StateMachine.handleState();
    assertEquals("WaitingState", br101StateMachine.getDispatcherStateName());

    //Start the second locomotive
    block2 = ps.getBlockByTileId("bk-2");
    block2.setBlockState(BlockBean.BlockState.FREE);
    ps.persist(block2);

    //////
    
    StateMachine ns1631StateMachine = dispatcherNS1631.getStateMachine();
    //Start from bk-4
    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(NS_1631, block4.getLocomotiveId());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.FREE, block2.getBlockState());

    //Thread should NOT run!
    assertFalse(ns1631StateMachine.isThreadRunning());
    assertFalse(ns1631StateMachine.isAutomodeEnabled());
    assertEquals("IdleState", ns1631StateMachine.getDispatcherStateName());

    //Execute IdleState
    ns1631StateMachine.handleState();
    //Automode is off should stay Idle
    assertEquals("IdleState", ns1631StateMachine.getDispatcherStateName());

    //Check the Queue properties    
    station = ps.getStation(block2);

    assertEquals("station-1", station.getId());
    assertEquals(2, station.getMinLocomotives());
    assertEquals(1, station.getLocomotiveCount());

    //Departure
    //Automode should be enabled
    ns1631StateMachine.setEnableAutomode(true);
    assertTrue(br101StateMachine.isAutomodeEnabled());

    //Execute IdleState again
    ns1631StateMachine.handleState();

    //State should advance to PrepareRoute    
    assertEquals("PrepareRouteState", ns1631StateMachine.getDispatcherStateName());
    //execute the PrepareRouteState
    ns1631StateMachine.handleState();
    assertEquals("StartingState", ns1631StateMachine.getDispatcherStateName());

    //Check the results of the PrepareRouteState execution
    routeId = dispatcherNS1631.getRouteBean().getId();
    assertEquals("[bk-4+]->[bk-2-]", routeId);
    assertTrue(dispatcherNS1631.getRouteBean().isLocked());

    //Departure block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    //Destination block state
    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.LOCKED, block2.getBlockState());

    //Block 2, destination block, should be reserved for BR_101 to come
    assertEquals(NS_1631, block2.getLocomotiveId());

    //Speed should still be zero as the startState has not been executed
    assertEquals(0, dispatcherNS1631.getLocomotiveBean().getVelocity());
    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    //Execute the RunningState
    ns1631StateMachine.handleState();
    assertEquals("RunningState", ns1631StateMachine.getDispatcherStateName());

    //Departure block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());
    //Destination block state
    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.LOCKED, block2.getBlockState());
    assertEquals(NS_1631, block2.getLocomotiveId());

    //Loc should start
    assertEquals(625, dispatcherNS1631.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcherNS1631.getLocomotiveBean().getDirection());

    //Now lets Toggle the enter sensor
    enterSensorId = dispatcherNS1631.getEnterSensorId();
    assertEquals(2, enterSensorId);

    //Execute the RunningState
    ns1631StateMachine.handleState();
    assertEquals("RunningState", ns1631StateMachine.getDispatcherStateName());
    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));

    enterSensor = ps.getSensor(enterSensorId);
    toggleSensorDirect(enterSensor);

    assertNull(dispatcherNS1631.getNextRouteBean());
    assertNull(dispatcherNS1631.getNextDestinationBlock());

    //Execute the RunningState
    ns1631StateMachine.handleState();
    //State should be advanced to EnterBlock
    assertEquals("ApproachingState", ns1631StateMachine.getDispatcherStateName());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.LOCKED, block2.getBlockState());
    assertEquals(NS_1631, block2.getLocomotiveId());

    //Execute the EnterState
    ns1631StateMachine.handleState();
    assertEquals("BrakingState", ns1631StateMachine.getDispatcherStateName());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.INBOUND, block2.getBlockState());

    ns1631StateMachine.handleState();
    assertEquals("BrakingState", ns1631StateMachine.getDispatcherStateName());

    //Loc should not slow down
    assertEquals(83, dispatcherNS1631.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcherNS1631.getLocomotiveBean().getDirection());

    ns1631StateMachine.handleState();
    assertEquals("BrakingState", ns1631StateMachine.getDispatcherStateName());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUTBOUND, block4.getBlockState());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.INBOUND, block2.getBlockState());
    assertEquals(NS_1631, block2.getLocomotiveId());

    assertNull(dispatcherNS1631.getNextRouteBean());
    assertNull(dispatcherNS1631.getNextDestinationBlock());

    ns1631StateMachine.handleState();
    assertEquals("BrakingState", ns1631StateMachine.getDispatcherStateName());

    //Now lets Toggle the in sensor
    inSensorId = dispatcherNS1631.getInSensorId();
    assertEquals(3, inSensorId);
    //Check if the inSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(inSensorId));

    //Toggle the IN sensor
    inSensor = ps.getSensor(inSensorId);
    toggleSensorDirect(inSensor);

    //Execute the BrakingState
    ns1631StateMachine.handleState();
    assertEquals("InBlockState", ns1631StateMachine.getDispatcherStateName());

    ns1631StateMachine.handleState();
    assertEquals("WaitingState", ns1631StateMachine.getDispatcherStateName());

    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block3.getBlockState());
    assertNull(block4.getLocomotiveId());

    block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OCCUPIED, block2.getBlockState());
    assertEquals(NS_1631, block2.getLocomotiveId());
    //Loc should stop
    assertEquals(0, dispatcherNS1631.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dispatcherNS1631.getLocomotiveBean().getDirection());

    //Check the Queue properties
    station = ps.getStation(block1);
    assertEquals("station-1", station.getId());
    assertEquals(2, station.getMinLocomotives());
    assertEquals(2, station.getLocomotiveCount());

//
//    assertEquals("bk-5", dispatcher.getDepartureBlock().getId());
//    assertEquals("bk-6", dispatcher.getDestinationBlock().getId());
//
//    //Execute the ContinueState
//    stateMachine.handleState();
//
//    assertEquals("StartState", stateMachine.getDispatcherStateName());
//
//
//    //Departure block state
//    block1 = ps.getBlockByTileId("bk-5");
//    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());
//
//    //Destination block state
//    block6 = ps.getBlockByTileId("bk-6");
//    assertEquals(BlockBean.BlockState.LOCKED, block6.getBlockState());
//
//    assertEquals(NS_1631, block6.getLocomotiveId());
//    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());
//
//    assertNull(dispatcher.getNextRouteBean());
//    assertNull(dispatcher.getNextDestinationBlock());
//
//    assertEquals("StartState", stateMachine.getDispatcherStateName());
//
//    //Execute the StartState
//    stateMachine.handleState();
//    //State should be advanced to EnterBlock
//    assertEquals("StartState", stateMachine.getDispatcherStateName());
//
//    block1 = ps.getBlockByTileId("bk-1");
//    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
//    assertNull(block1.getLocomotiveId());
//
//    block5 = ps.getBlockByTileId("bk-5");
//    assertEquals(BlockBean.BlockState.OUTBOUND, block5.getBlockState());
//    assertEquals(NS_1631, block5.getLocomotiveId());
//
//    block6 = ps.getBlockByTileId("bk-6");
//    assertEquals(BlockBean.BlockState.LOCKED, block6.getBlockState());
//    assertEquals(NS_1631, block6.getLocomotiveId());
//
//    //Now lets Toggle the enter sensor
//    enterSensorId = dispatcher.getEnterSensorId();
//    assertEquals(7, enterSensorId);
//    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
//    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));
//
//    enterSensor = ps.getSensor(enterSensorId);
//    toggleSensorDirect(enterSensor);
//
//    //Execute the EnterBlock
//    stateMachine.handleState();
//
//    block1 = ps.getBlockByTileId("bk-1");
//    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
//
//    block5 = ps.getBlockByTileId("bk-5");
//    assertEquals(BlockBean.BlockState.OUTBOUND, block5.getBlockState());
//    assertEquals(NS_1631, block5.getLocomotiveId());
//
//    block6 = ps.getBlockByTileId("bk-6");
//    assertEquals(BlockBean.BlockState.LOCKED, block6.getBlockState());
//    assertEquals(NS_1631, block6.getLocomotiveId());
//
//    assertNull(dispatcher.getNextRouteBean());
//    assertNull(dispatcher.getNextDestinationBlock());
//
//    //Now lets Toggle the enter sensor
//    enterSensorId = dispatcher.getEnterSensorId();
//    assertEquals(7, enterSensorId);
//    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
//    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));
//
//    enterSensor = ps.getSensor(enterSensorId);
//    toggleSensorDirect(enterSensor);
//
//    //In this in this block the train should stop. State should be advanced to BrakeState.
//    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());
//    //Execute the EnterBlock
//    stateMachine.handleState();
//
//    //In this in this block the train should stop. State should be advanced to BrakeState.
//    assertEquals("BrakeState", stateMachine.getDispatcherStateName());
//
//    //Execute the BrakeState
//    stateMachine.handleState();
//    assertEquals("BrakeState", stateMachine.getDispatcherStateName());
//
//    //Loc should slow down
//    assertEquals(83, dispatcher.getLocomotiveBean().getVelocity());
//    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());
//
//    block1 = ps.getBlockByTileId("bk-1");
//    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
//
//    block5 = ps.getBlockByTileId("bk-5");
//    assertEquals(BlockBean.BlockState.OUTBOUND, block5.getBlockState());
//    assertEquals(NS_1631, block5.getLocomotiveId());
//
//    block6 = ps.getBlockByTileId("bk-6");
//    assertEquals(BlockBean.BlockState.INBOUND, block6.getBlockState());
//    assertEquals(NS_1631, block6.getLocomotiveId());
//
//    assertNull(dispatcher.getNextRouteBean());
//    assertNull(dispatcher.getNextDestinationBlock());
//
//    //Lets trigger the IN sensor
//    inSensorId = dispatcher.getInSensorId();
//    assertEquals(8, inSensorId);
//    //Check if the inSensor is registered a a "knownEvent" else we get a Ghost!
//    assertTrue(AutoPilot.isSensorHandlerRegistered(inSensorId));
//
//    //Toggle the IN sensor
//    inSensor = ps.getSensor(inSensorId);
//    toggleSensorDirect(inSensor);
//
//    //Execute the EnterState
//    stateMachine.handleState();
//    assertEquals("InBlockState", stateMachine.getDispatcherStateName());
//
//    block1 = ps.getBlockByTileId("bk-1");
//    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
//
//    block5 = ps.getBlockByTileId("bk-5");
//    assertEquals(BlockBean.BlockState.OUTBOUND, block5.getBlockState());
//    assertEquals(NS_1631, block5.getLocomotiveId());
//
//    block6 = ps.getBlockByTileId("bk-6");
//    assertEquals(BlockBean.BlockState.INBOUND, block6.getBlockState());
//    assertEquals(NS_1631, block6.getLocomotiveId());
//
//    assertNull(dispatcher.getNextRouteBean());
//    assertNull(dispatcher.getNextDestinationBlock());
//
//    //Execute the InBlockState
//    stateMachine.handleState();
//    assertEquals("WaitState", stateMachine.getDispatcherStateName());
//
//    block1 = ps.getBlockByTileId("bk-1");
//    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
//
//    block5 = ps.getBlockByTileId("bk-5");
//    assertEquals(BlockBean.BlockState.FREE, block5.getBlockState());
//    assertNull(block5.getLocomotiveId());
//
//    block6 = ps.getBlockByTileId("bk-6");
//    assertEquals(BlockBean.BlockState.OCCUPIED, block6.getBlockState());
//    assertEquals(NS_1631, block6.getLocomotiveId());
//
//    assertNull(dispatcher.getNextRouteBean());
//    assertNull(dispatcher.getNextDestinationBlock());
//
//    //Disable automode which should jump to Idle state
//    dispatcher.stopLocomotiveAutomode();
//
//    assertFalse(dispatcher.isLocomotiveAutomodeOn());
//
//    //Execute the WaitState, should jump to Idle
//    //Execute the InBlockState
//    stateMachine.handleState();
//
//    //Should switch to Idle
//    assertEquals("IdleState", stateMachine.getDispatcherStateName());
  }

//  private void setupbk1bkNs1631() {
//    ns1631 = ps.getLocomotive((long) NS_1631);
//
//    BlockBean block1 = ps.getBlockByTileId("bk-1");
//    block1.setLocomotive(ns1631);
//    block1.setBlockState(BlockBean.BlockState.OCCUPIED);
//    block1.setAlwaysStop(true);
//    ps.persist(block1);
//
//    BlockBean block2 = ps.getBlockByTileId("bk-2");
//    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
//    ps.persist(block2);
//
//    BlockBean block3 = ps.getBlockByTileId("bk-3");
//    block3.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
//    ps.persist(block3);
//
//    BlockBean block4 = ps.getBlockByTileId("bk-4");
//    block4.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
//    ps.persist(block4);
//
//    BlockBean block5 = ps.getBlockByTileId("bk-5");
//    block5.setBlockState(BlockBean.BlockState.FREE);
//    block5.setAlwaysStop(false);
//    ps.persist(block5);
//
//    BlockBean block6 = ps.getBlockByTileId("bk-6");
//    block6.setBlockState(BlockBean.BlockState.FREE);
//    block6.setAlwaysStop(true);
//    ps.persist(block6);
//
//    AutoPilot.prepareAllDispatchers();
//    //dispatcher = AutoPilot.getLocomotiveDispatcher(ns1631);
//    Logger.trace("Prepared layout");
//  }
}
