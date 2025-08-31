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

@TestMethodOrder(OrderAnnotation.class)
public class StateMachineStepByStepGhostTest extends AbstractStateMachineStepByStepTest {

  public StateMachineStepByStepGhostTest() {
    super();
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

    BlockBean block5 = ps.getBlockByTileId("bk-5");
    block5.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block5);

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

    BlockBean block5 = ps.getBlockByTileId("bk-5");
    block5.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    ps.persist(block5);

    AutoPilot.prepareAllDispatchers();
    dispatcher = AutoPilot.getLocomotiveDispatcher(ns1631);
    Logger.trace("Prepared layout");
  }

  //@Test
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

    BlockBean block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block5.getBlockState());

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

    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block5.getBlockState());

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

    block5 = ps.getBlockByTileId("bk-5");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block5.getBlockState());

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

    assertEquals(583, dispatcher.getLocomotiveBean().getVelocity());
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

  //TODO !!!!!!
  //@Test
  @Order(1)
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

}
