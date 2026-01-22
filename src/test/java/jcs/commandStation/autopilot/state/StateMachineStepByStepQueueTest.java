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

  private void setupbk1bkNs1631() {
    ns1631 = ps.getLocomotive(NS_1631);

    BlockBean block1 = ps.getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    block1.setBlockState(BlockBean.BlockState.OCCUPIED);
    block1.setAlwaysStop(true);
    ps.persist(block1);

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block2);

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    block3.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block3);

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    block4.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block4);

    BlockBean block5 = ps.getBlockByTileId("bk-5");
    block5.setBlockState(BlockBean.BlockState.FREE);
    block5.setAlwaysStop(false);
    ps.persist(block5);

    BlockBean block6 = ps.getBlockByTileId("bk-6");
    block6.setBlockState(BlockBean.BlockState.FREE);
    block6.setAlwaysStop(true);
    ps.persist(block6);

    AutoPilot.prepareAllDispatchers();
    dispatcher = AutoPilot.getLocomotiveDispatcher(ns1631);
    Logger.trace("Prepared layout");
  }

  @Order(1)
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
  
  
  
  
  
  //@Test
  @Order(1)
  public void testBk1ToBk6() {
    //StateMachine functionality test, runs in 1 single thread.
    //Each execution step should be manually performed.
    //Lets drive with the DHD loc from bk-1 to bk-4
    Logger.info("Bk1ToBk6");
    setupbk1bkNs1631();

    //Check block statuses
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block4.getBlockState());

    BlockBean block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.FREE, block5.getBlockState());

    BlockBean block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.FREE, block5.getBlockState());

    assertNull(dispatcher.getNextRouteBean());
    assertNull(dispatcher.getNextDestinationBlock());

    StateMachine stateMachine = dispatcher.getStateMachine();

    //Start from bk-1
    assertEquals(NS_1631, block1.getLocomotiveId());

    //Destination bk-6 via bk-5
    assertNull(block5.getLocomotiveId());
    assertNull(block6.getLocomotiveId());
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
    assertEquals("[bk-1-]->[bk-5-]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.LOCKED, block5.getBlockState());
    assertNull(block6.getLocomotiveId());

    //Block 5, (via) destination block should be reserved for NS_1631 to come
    assertEquals(NS_1631, block5.getLocomotiveId());
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
    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.LOCKED, block5.getBlockState());
    assertEquals(NS_1631, block5.getLocomotiveId());
    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.FREE, block6.getBlockState());
    assertNull(block6.getLocomotiveId());

    //Loc should start
    assertEquals(583, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //State should stay the same as the enter sensor of the destination is not hit.
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Now lets Toggle the enter sensor
    Integer enterSensorId = dispatcher.getEnterSensorId();
    assertEquals(5, enterSensorId);
    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));

    SensorBean enterSensor = ps.getSensor(enterSensorId);
    toggleSensorDirect(enterSensor);

    assertNull(dispatcher.getNextRouteBean());
    assertNull(dispatcher.getNextDestinationBlock());

    //Execute the StartState
    stateMachine.handleState();
    //State should be advanced to EnterBlock
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.LOCKED, block5.getBlockState());
    assertEquals(NS_1631, block5.getLocomotiveId());

    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.FREE, block6.getBlockState());
    assertNull(block6.getLocomotiveId());

    //Execute the EnterState
    stateMachine.handleState();

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.INBOUND, block5.getBlockState());
    assertEquals(NS_1631, block5.getLocomotiveId());

    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.FREE, block6.getBlockState());
    assertNull(block6.getLocomotiveId());

    assertNull(dispatcher.getNextRouteBean());
    assertNull(dispatcher.getNextDestinationBlock());

    //In the enterstate a decision should be made whether the train can continue to the next blok or stop.
    //In this testcase the train should stop. State should be advanced to BrakeState.
    assertEquals("PrepareNextRouteState", stateMachine.getDispatcherStateName());

    //Execute the PrepareNextRouteState
    stateMachine.handleState();

    //It should find the next route
    String nextRouteId = dispatcher.getNextRouteBean().getId();
    assertEquals("[bk-5+]->[bk-6-]", nextRouteId);
    assertTrue(dispatcher.getNextRouteBean().isLocked());

    assertEquals("ContinueState", stateMachine.getDispatcherStateName());

    //Loc should not slow down
    assertEquals(583, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.INBOUND, block5.getBlockState());
    assertEquals(NS_1631, block5.getLocomotiveId());

    //Next Destination block state
    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.LOCKED, block6.getBlockState());
    assertEquals(NS_1631, block6.getLocomotiveId());

    assertNotNull(dispatcher.getNextRouteBean());
    assertNotNull(dispatcher.getNextDestinationBlock());

    //Execute the ContinueState
    stateMachine.handleState();
    assertEquals("ContinueState", stateMachine.getDispatcherStateName());

    //Now lets Toggle the in sensor
    Integer inSensorId = dispatcher.getInSensorId();
    assertEquals(6, inSensorId);
    //Check if the inSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(inSensorId));

    //Toggle the IN sensor
    SensorBean inSensor = ps.getSensor(inSensorId);
    toggleSensorDirect(inSensor);

    //Execute the EnterState
    stateMachine.handleState();
    assertEquals("InBlockState", stateMachine.getDispatcherStateName());

    //Execute the ContinueState
    stateMachine.handleState();

    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Loc should continue
    assertEquals(583, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertEquals("bk-5", dispatcher.getDepartureBlock().getId());
    assertEquals("bk-6", dispatcher.getDestinationBlock().getId());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.LOCKED, block6.getBlockState());

    assertEquals(NS_1631, block6.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    assertNull(dispatcher.getNextRouteBean());
    assertNull(dispatcher.getNextDestinationBlock());

    assertEquals("StartState", stateMachine.getDispatcherStateName());

    //Execute the StartState
    stateMachine.handleState();
    //State should be advanced to EnterBlock
    assertEquals("StartState", stateMachine.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());
    assertNull(block1.getLocomotiveId());

    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.OUTBOUND, block5.getBlockState());
    assertEquals(NS_1631, block5.getLocomotiveId());

    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.LOCKED, block6.getBlockState());
    assertEquals(NS_1631, block6.getLocomotiveId());

    //Now lets Toggle the enter sensor
    enterSensorId = dispatcher.getEnterSensorId();
    assertEquals(7, enterSensorId);
    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));

    enterSensor = ps.getSensor(enterSensorId);
    toggleSensorDirect(enterSensor);

    //Execute the EnterBlock
    stateMachine.handleState();

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.OUTBOUND, block5.getBlockState());
    assertEquals(NS_1631, block5.getLocomotiveId());

    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.LOCKED, block6.getBlockState());
    assertEquals(NS_1631, block6.getLocomotiveId());

    assertNull(dispatcher.getNextRouteBean());
    assertNull(dispatcher.getNextDestinationBlock());

    //Now lets Toggle the enter sensor
    enterSensorId = dispatcher.getEnterSensorId();
    assertEquals(7, enterSensorId);
    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));

    enterSensor = ps.getSensor(enterSensorId);
    toggleSensorDirect(enterSensor);

    //In this in this block the train should stop. State should be advanced to BrakeState.
    assertEquals("EnterBlockState", stateMachine.getDispatcherStateName());
    //Execute the EnterBlock
    stateMachine.handleState();

    //In this in this block the train should stop. State should be advanced to BrakeState.
    assertEquals("BrakeState", stateMachine.getDispatcherStateName());

    //Execute the BrakeState
    stateMachine.handleState();
    assertEquals("BrakeState", stateMachine.getDispatcherStateName());

    //Loc should slow down
    assertEquals(83, dispatcher.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.BACKWARDS, dispatcher.getLocomotiveBean().getDirection());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.OUTBOUND, block5.getBlockState());
    assertEquals(NS_1631, block5.getLocomotiveId());

    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.INBOUND, block6.getBlockState());
    assertEquals(NS_1631, block6.getLocomotiveId());

    assertNull(dispatcher.getNextRouteBean());
    assertNull(dispatcher.getNextDestinationBlock());

    //Lets trigger the IN sensor
    inSensorId = dispatcher.getInSensorId();
    assertEquals(8, inSensorId);
    //Check if the inSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(inSensorId));

    //Toggle the IN sensor
    inSensor = ps.getSensor(inSensorId);
    toggleSensorDirect(inSensor);

    //Execute the EnterState
    stateMachine.handleState();
    assertEquals("InBlockState", stateMachine.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.OUTBOUND, block5.getBlockState());
    assertEquals(NS_1631, block5.getLocomotiveId());

    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.INBOUND, block6.getBlockState());
    assertEquals(NS_1631, block6.getLocomotiveId());

    assertNull(dispatcher.getNextRouteBean());
    assertNull(dispatcher.getNextDestinationBlock());

    //Execute the InBlockState
    stateMachine.handleState();
    assertEquals("WaitState", stateMachine.getDispatcherStateName());

    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.FREE, block5.getBlockState());
    assertNull(block5.getLocomotiveId());

    block6 = ps.getBlockByTileId("bk-6");
    assertEquals(BlockBean.BlockState.OCCUPIED, block6.getBlockState());
    assertEquals(NS_1631, block6.getLocomotiveId());

    assertNull(dispatcher.getNextRouteBean());
    assertNull(dispatcher.getNextDestinationBlock());

    //Disable automode which should jump to Idle state
    dispatcher.stopLocomotiveAutomode();

    assertFalse(dispatcher.isLocomotiveAutomodeOn());

    //Execute the WaitState, should jump to Idle
    //Execute the InBlockState
    stateMachine.handleState();

    //Should switch to Idle
    assertEquals("IdleState", stateMachine.getDispatcherStateName());
  }



}
