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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.JCSCommandStation;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.CommandStationBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.util.RunUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.tinylog.Logger;

public class StateMachineThreadTest {

  private static final Long NS_DHG_6505 = 7L;
  private static final Long BR_101_003_2 = 23L;
  private static final Long NS_1631 = 39L;

  private final PersistenceTestHelper testHelper;
  private final JCSCommandStation cs;
  private final PersistenceService ps;
  private LocomotiveBean dhg;
  private LocomotiveBean ns1631;
  private Dispatcher dispatcher;

  private final ExecutorService executor;

  public StateMachineThreadTest() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");
    System.setProperty("do.not.simulate.virtual.drive", "true");
    System.setProperty("state.machine.stepTest", "false");

    executor = Executors.newCachedThreadPool();

    testHelper = PersistenceTestHelper.getInstance();
    testHelper.runTestDataInsertScript("autopilot_test_layout.sql");

    ps = PersistenceFactory.getService();
    cs = JCS.getJcsCommandStation();
    cs.disconnect();
    //When running in a batch the default command station could be different..
    CommandStationBean virt = ps.getCommandStation("virtual");
    ps.changeDefaultCommandStation(virt);

    cs.connect();
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

    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;
    JCS.getJcsCommandStation().switchPower(true);
    //Wait for power on..
    boolean powerOn = JCS.getJcsCommandStation().isPowerOn();
    while (!powerOn && timeout > now) {
      pause(1);
      powerOn = JCS.getJcsCommandStation().isPowerOn();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    Logger.trace("Power on in " + (now - start) + "ms.");

    AutoPilot.startAutoMode();

    Logger.info("=========================== setUp done..............");
  }

  @AfterEach
  public void tearDown() {
    Logger.info("=========================== Teardown..............");
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
    Logger.trace("Prepared layout");
  }

  private void toggleSensorInDirect(String sensorId) {
    this.executor.execute(() -> toggleSensorDirect(sensorId));
  }

  private void toggleSensorDirect(String sensorId) {
    SensorBean sensor = ps.getSensor(sensorId);
    toggleSensorDirect(sensor);
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

      synchronized (this) {
        notifyAll();
      }
    }
  }

  private void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }

  //Disabled for now as in Github this test fails for yet unkown reasons
  //@Test
  public void testBk1ToBk4() {
    //StateMachine Threaded functionality test.
    //The Sate machine runs in its own Thread.
    //Each execution step are automatically.
    //Lets drive with the DHD loc from bk-1 to bk-4
    Logger.info("Bk1ToBk4");
    setupbk1bkNsDHG();

    Dispatcher dhgDisp = AutoPilot.getLocomotiveDispatcher(dhg);

    //Capture the State Events by adding a Statelistener. 
    List<String> passedStates = new ArrayList<>();
    StatesListener statesListener = new StatesListener(passedStates);
    dhgDisp.addStateEventListener(statesListener);

    assertEquals(0, statesListener.getEventCount());

    //Check block statuses
    BlockBean block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    BlockBean block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.FREE, block4.getBlockState());

    //Start from bk-1
    assertEquals(NS_DHG_6505, block1.getLocomotiveId());
    //Destination bk-4
    assertNull(block4.getLocomotiveId());
    assertNull(dhgDisp.getRouteBean());

    //Thread should NOT run!
    assertFalse(dhgDisp.isRunning());
    assertFalse(dhgDisp.isLocomotiveAutomodeOn());
    assertEquals("IdleState", dhgDisp.getStateName());

    //Automode is off should stay Idle
    assertEquals("IdleState", dhgDisp.getStateName());
    assertTrue(AutoPilot.isOnTrack(dhg));

    assertTrue(AutoPilot.isAutoModeActive());
    AutoPilot.startAutoMode();

    long now = System.currentTimeMillis();
    long timeout = now + 10000;
    boolean started = dhgDisp.startLocomotiveAutomode();
    assertTrue(started);

    boolean running = dhgDisp.isRunning();
    while (!running && timeout > now) {
      pause(1);
      running = dhgDisp.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(dhgDisp.isRunning());

    assertTrue(dhgDisp.isLocomotiveAutomodeOn());
    assertTrue(dhgDisp.isRunning());

    assertEquals(0, statesListener.getEventCount());

    //Departure
    //State should advance to PrepareRoute 
    now = System.currentTimeMillis();
    timeout = now + 100000;

    int executedStates = statesListener.getEventCount();
    while (executedStates < 1 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertEquals(1, statesListener.getEventCount());
    String dispatcherState = dhgDisp.getStateName();
    assertEquals("PrepareRouteState", dispatcherState);

    //A route should be found and the state should jump to the next state
    now = System.currentTimeMillis();
    timeout = now + 10000;
    executedStates = statesListener.getEventCount();
    while (executedStates < 2 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();
    assertEquals("StartState", dispatcherState);
    assertEquals(2, passedStates.size());

    String routeId = dhgDisp.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dhgDisp.getRouteBean().isLocked());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OCCUPIED, block1.getBlockState());

    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.LOCKED, block4.getBlockState());

    //Block 4, destination block should be reserved for DHG to come
    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    String occupancySensorId = dhgDisp.getOccupationSensorId();
    String exitSensorId = dhgDisp.getExitSensorId();
    String enterSensorId = dhgDisp.getEnterSensorId();
    String inSensorId = dhgDisp.getInSensorId();

    assertNotNull(occupancySensorId);
    assertNotNull(exitSensorId);
    assertNotNull(enterSensorId);
    assertNotNull(inSensorId);

    assertEquals("0-0001", occupancySensorId);
    assertEquals("0-0002", exitSensorId);
    assertEquals("0-0013", enterSensorId);
    assertEquals("0-0012", inSensorId);

    assertTrue(JCS.getJcsCommandStation().isPowerOn());

    now = System.currentTimeMillis();
    timeout = now + 10000;
    boolean locStarted = dhgDisp.getLocomotiveBean().getVelocity() > 0;

    while (!locStarted && timeout > now) {
      pause(1);
      locStarted = dhgDisp.getLocomotiveBean().getVelocity() > 0;
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();
    assertEquals("StartState", dispatcherState);

    //Loc should be started
    assertEquals(700, dhgDisp.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dhgDisp.getLocomotiveBean().getDirection());

    //Should be waiting for the enter sensor
    String waitForId = dhgDisp.getWaitingForSensorId();
    assertEquals(enterSensorId, waitForId);

    //Now lets Toggle the enter sensor
    //Check if the enterSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(enterSensorId));

    now = System.currentTimeMillis();
    timeout = now + 10000;

    toggleSensorInDirect(enterSensorId);

    executedStates = statesListener.getEventCount();
    while (executedStates < 3 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();
    assertEquals("EnterBlockState", dispatcherState);
    assertEquals(3, passedStates.size());

    now = System.currentTimeMillis();
    timeout = now + 10000;
    boolean locBreaking = dhgDisp.getLocomotiveBean().getVelocity() < 600;

    while (!locBreaking && timeout > now) {
      pause(1);
      locBreaking = dhgDisp.getLocomotiveBean().getVelocity() < 600;
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();
    assertEquals("EnterBlockState", dispatcherState);

    //Loc should be slowing down
    assertEquals(100, dhgDisp.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dhgDisp.getLocomotiveBean().getDirection());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.OUTBOUND, block1.getBlockState());
    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.INBOUND, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());

    //Should be waiting for the in sensor
    waitForId = dhgDisp.getWaitingForSensorId();
    assertEquals(inSensorId, waitForId);

    //Now lets Toggle the in sensor
    //Check if the inSensor is registered a a "knownEvent" else we get a Ghost!
    assertTrue(AutoPilot.isSensorHandlerRegistered(inSensorId));

    now = System.currentTimeMillis();
    timeout = now + 10000;

    //Toggle the IN sensor
    toggleSensorInDirect(inSensorId);

    executedStates = statesListener.getEventCount();
    while (executedStates < 4 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();
    assertEquals("InBlockState", dispatcherState);
    assertEquals(4, passedStates.size());

    now = System.currentTimeMillis();
    timeout = now + 10000;
    boolean locStopped = dhgDisp.getLocomotiveBean().getVelocity() == 0;

    while (!locStopped && timeout > now) {
      pause(1);
      locStopped = dhgDisp.getLocomotiveBean().getVelocity() < 600;
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();
    assertEquals("InBlockState", dispatcherState);

    //Loc should be slowing down
    assertEquals(0, dhgDisp.getLocomotiveBean().getVelocity());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dhgDisp.getLocomotiveBean().getDirection());

    now = System.currentTimeMillis();
    timeout = now + 10000;

    executedStates = statesListener.getEventCount();
    while (executedStates < 5 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();
    assertEquals("WaitState", dispatcherState);
    assertEquals(5, passedStates.size());

    //Departure block state
    block1 = ps.getBlockByTileId("bk-1");
    assertEquals(BlockBean.BlockState.FREE, block1.getBlockState());

    //Destination block state
    block4 = ps.getBlockByTileId("bk-4");
    assertEquals(BlockBean.BlockState.OCCUPIED, block4.getBlockState());

    assertEquals(NS_DHG_6505, block4.getLocomotiveId());
    assertEquals(LocomotiveBean.Direction.FORWARDS, dhgDisp.getLocomotiveBean().getDirection());

    assertNull(dhgDisp.getRouteBean());
    assertNull(dhgDisp.getDestinationBlock());

    assertEquals("bk-4", dhgDisp.getDepartureBlock().getId());

    now = System.currentTimeMillis();
    timeout = now + 10000;

    executedStates = statesListener.getEventCount();
    while (executedStates < 6 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();

    assertEquals("WaitState", dispatcherState);
    assertEquals(6, passedStates.size());

    now = System.currentTimeMillis();
    timeout = now + 10000;
    //Disable automode which should jump to Idle state
    dhgDisp.stopLocomotiveAutomode();

    executedStates = statesListener.getEventCount();
    while (executedStates < 7 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dhgDisp.getStateName();

    assertEquals("IdleState", dispatcherState);
    assertEquals(7, passedStates.size());
  }

  //@Test
  public void testStartStopLocomotiveAutomode() {
//    if (RunUtil.isWindows()) {
//      //For some unknown reason in Windows this does not work....
//      Logger.info("Skipping startStopThreadRunning");
//      return;
//    }

    if (!JCS.getJcsCommandStation().isPowerOn()) {
      Logger.warn("Skipping StartStopLocomotiveAutomode due to power OFF!");
      return;
    }

    Logger.info("StartStopLocomotiveAutomode");
    setupbk1bkNsDHG();

    BlockBean block2 = PersistenceFactory.getService().getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = PersistenceFactory.getService().getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    assertFalse(AutoPilot.isAutoModeActive());

    //Capture the State Events by adding a Statelistener. 
    List<String> passedStates = new ArrayList<>();
    StatesListener statesListener = new StatesListener(passedStates);
    dispatcher.addStateEventListener(statesListener);

    assertEquals(0, statesListener.getEventCount());
    assertFalse(dispatcher.isLocomotiveAutomodeOn());
    assertFalse(dispatcher.isRunning());

    assertTrue(AutoPilot.isOnTrack(dhg));

    boolean started = dispatcher.startLocomotiveAutomode();
    //Should NOT start as the Autopilot is not in automode
    assertFalse(started);

    long now = System.currentTimeMillis();
    long timeout = now + 10000;
    //Start Automode
    AutoPilot.startAutoMode();

    boolean autoPilotRunning = AutoPilot.isAutoModeActive();
    while (!autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(AutoPilot.isAutoModeActive());

    assertFalse(dispatcher.isLocomotiveAutomodeOn());
    assertFalse(dispatcher.isRunning());

    assertEquals(0, statesListener.getEventCount());

    //AutoPilot is ON
    now = System.currentTimeMillis();
    timeout = now + 10000;

    started = dispatcher.startLocomotiveAutomode();
    assertTrue(started);
    String dispatcherState = dispatcher.getStateName();
    assertEquals("IdleState", dispatcherState);

    boolean dispatcherThreadRunning = dispatcher.isRunning();
    while (!dispatcherThreadRunning && timeout > now) {
      pause(10);
      dispatcherThreadRunning = dispatcher.isRunning();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertTrue(dispatcher.isRunning());
    assertTrue(dispatcher.isLocomotiveAutomodeOn());

    //Dispatcher is ON
    //It should jump the the next state    
    now = System.currentTimeMillis();
    timeout = now + 100000;

    int executedStates = statesListener.getEventCount();
    while (executedStates < 1 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertEquals(1, statesListener.getEventCount());
    dispatcherState = dispatcher.getStateName();
    assertEquals("PrepareRouteState", dispatcherState);

    //A route should be found and the state should jump to the next state
    now = System.currentTimeMillis();
    timeout = now + 10000;
    executedStates = statesListener.getEventCount();
    while (executedStates < 2 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("StartState", dispatcherState);
    assertEquals(2, passedStates.size());

    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    String occupancySensorId = dispatcher.getOccupationSensorId();
    String exitSensorId = dispatcher.getExitSensorId();
    String enterSensorId = dispatcher.getEnterSensorId();
    String inSensorId = dispatcher.getInSensorId();

    assertNotNull(occupancySensorId);
    assertNotNull(exitSensorId);
    assertNotNull(enterSensorId);
    assertNotNull(inSensorId);

    assertEquals("0-0001", occupancySensorId);
    assertEquals("0-0002", exitSensorId);
    assertEquals("0-0013", enterSensorId);
    assertEquals("0-0012", inSensorId);

    //Stop the automode. The thread and state machine should finish the whole cycle and then jump to Idle
    //Thread should stay alive.
    dispatcher.stopLocomotiveAutomode();

    //As the state is Start the loco is running we are waiting for the enter sensor
    //State machine should remain in StartState until the enter sensor is triggered
    now = System.currentTimeMillis();
    timeout = now + 100000;
    //Must be sure the the enter sensor is registered
    String waitingForSensorId = dispatcher.getWaitingForSensorId();
    while (!enterSensorId.equals(waitingForSensorId) && timeout > now) {
      pause(1);
      waitingForSensorId = dispatcher.getWaitingForSensorId();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("StartState", dispatcherState);

    //Now we can trigger the sensor   
    now = System.currentTimeMillis();
    timeout = now + 10000;

    //Trigger the enter sensor
    toggleSensorInDirect(enterSensorId);

    //Wait for the state switch
    executedStates = statesListener.getEventCount();
    while (executedStates < 3 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("EnterBlockState", dispatcherState);
    assertEquals(3, passedStates.size());

    //State machine will stay in Enterstate unil the IN sensor is hit
    now = System.currentTimeMillis();
    timeout = now + 10000;
    //Must be sure the the enter sensor is registered
    waitingForSensorId = dispatcher.getWaitingForSensorId();
    while (!inSensorId.equals(waitingForSensorId) && timeout > now) {
      pause(1);
      waitingForSensorId = dispatcher.getWaitingForSensorId();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("EnterBlockState", dispatcherState);

    //Let it figure out the IN sensor
    now = System.currentTimeMillis();
    timeout = now + 10000;

    //Trigger the In Sensor
    toggleSensorInDirect(inSensorId);

    //Wait for the state switch
    executedStates = statesListener.getEventCount();
    while (executedStates < 4 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("InBlockState", dispatcherState);

    assertEquals(4, passedStates.size());

    toggleSensorInDirect(inSensorId);

    executedStates = statesListener.getEventCount();
    while (executedStates < 4 && timeout > now) {
      pause(100);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertEquals(4, passedStates.size());
    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("InBlockState", dispatcherState);

    //As the locomotive is stopped the next state should be idle...
    now = System.currentTimeMillis();
    timeout = now + 10000;

    executedStates = statesListener.getEventCount();
    while (executedStates < 5 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertEquals(5, passedStates.size());
    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("IdleState", dispatcherState);

    assertTrue(dispatcher.isRunning());
    assertFalse(dispatcher.isLocomotiveAutomodeOn());

    now = System.currentTimeMillis();
    timeout = now + 10000;

    //Enable the locomotive again
    started = dispatcher.startLocomotiveAutomode();
    assertTrue(started);

    executedStates = statesListener.getEventCount();
    while (executedStates < 6 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertEquals(6, passedStates.size());
    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("PrepareRouteState", dispatcherState);

    assertTrue(dispatcher.isRunning());
    assertTrue(dispatcher.isLocomotiveAutomodeOn());

    now = System.currentTimeMillis();
    timeout = now + 10000;

    //(force) stop the state machine
    dispatcher.stopRunning();

    executedStates = statesListener.getEventCount();
    while (executedStates < 7 && timeout > now) {
      pause(1);
      executedStates = statesListener.getEventCount();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertEquals(7, passedStates.size());

    //Let wait for the idle state
    now = System.currentTimeMillis();
    timeout = now + 10000;
    dispatcherState = dispatcher.getStateName();
    while (!"IdleState".equals(dispatcherState) && timeout > now) {
      pause(1);
      dispatcherState = dispatcher.getStateName();
      now = System.currentTimeMillis();
    }

    assertEquals("IdleState", dispatcherState);
    assertTrue(timeout > now);

    assertFalse(dispatcher.isRunning());
    assertFalse(dispatcher.isLocomotiveAutomodeOn());
  }

  //@Test
  public void testStartStopThreadRunning() {
    if (RunUtil.isWindows()) {
      //For some unknown reason in Windows this does not work....
      Logger.info("Skipping startStopThreadRunning");
      return;
    }

    Logger.info("startStopThreadRunning");
    setupbk1bkNsDHG();
    BlockBean block2 = PersistenceFactory.getService().getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = PersistenceFactory.getService().getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    StateMachineThread instance = dispatcher.getStateMachineThread();

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isAlive());

    assertEquals("DT->NS DHG 6505", instance.getName());
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.start();
    pause(250);

    if (!JCS.getJcsCommandStation().isPowerOn()) {
      Logger.warn("Skipping startStopThreadRunning due to power OFF!");
      return;
    }

    Logger.debug("Dispatcher Thread Started");
    assertTrue(instance.isThreadRunning());
    assertTrue(instance.isAlive());
    assertFalse(instance.isEnableAutomode());

    instance.stopRunningThread();
    Logger.debug("Dispatcher Thread Stopped");
    assertFalse(instance.isThreadRunning());
    assertTrue(instance.isAlive());
  }

  private class StatesListener implements StateEventListener {

    private final List<String> stateNameList;
    private int eventCount;

    StatesListener(List<String> stateNameList) {
      this.stateNameList = stateNameList;
    }

    @Override
    public void onStateChange(Dispatcher dispatcher) {
      eventCount++;
      String currentDispatcherState = dispatcher.getStateName();
      stateNameList.add(currentDispatcherState);
    }

    public int getEventCount() {
      return this.eventCount;
    }

    public void resetEventCount() {
      this.eventCount = 0;
    }
  }

  @BeforeAll
  public static void beforeAll() {
    Logger.trace("####################### State Machine Thread Test ##############################");

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
    Logger.trace("^^^^^^^^^^^^^^^^^^^^^^^^ State Machine Thread Test ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
  }

}
