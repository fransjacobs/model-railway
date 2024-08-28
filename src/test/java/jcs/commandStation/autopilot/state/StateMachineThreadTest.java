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
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.util.RunUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

public class StateMachineThreadTest {

  private static final Long NS_DHG_6505 = 7L;
  private static final Long BR_101_003_2 = 23L;
  private static final Long NS_1631 = 39L;

  private final PersistenceTestHelper testHelper;
  private final JCSCommandStation cs;
  private final PersistenceService ps;
  private final AutoPilot autoPilot;
  private LocomotiveBean dhg;
  private LocomotiveBean ns1631;
  private Dispatcher dispatcher;

  private final ExecutorService executor;

  public StateMachineThreadTest() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");

    executor = Executors.newCachedThreadPool();

    //Switch the Virtual Simulator OFF 
    System.setProperty("dispatcher.stepTest", "true");
    testHelper = PersistenceTestHelper.getInstance();
    testHelper.runTestDataInsertScript("autopilot_test_layout.sql");

    ps = PersistenceFactory.getService();
    cs = JCS.getJcsCommandStation();

    autoPilot = AutoPilot.getInstance();
    //autoPilot.startAutoMode();
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
    cs.switchPower(true);
  }

  @AfterEach
  public void tearDown() {
    //autoPilot.stopAutoMode();
    //let the autopilot finish...
    //pause(1000);
    //autoPilot.clearDispatchers();
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

    autoPilot.prepareAllDispatchers();

    dispatcher = autoPilot.getLocomotiveDispatcher(dhg);
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

    autoPilot.prepareAllDispatchers();
    dispatcher = autoPilot.getLocomotiveDispatcher(ns1631);
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
    }
  }

  private void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }

  /////////////////////////////////////////////////
  @Test
  public void testStartStopLocomotiveAutomode() {
//    if (RunUtil.isWindows()) {
//      //For some unknown reason in Windows this does not work....
//      Logger.info("Skipping startStopThreadRunning");
//      return;
//    }

    Logger.info("startStopThreadRunning");
    setupbk1bkNsDHG();

    BlockBean block2 = PersistenceFactory.getService().getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = PersistenceFactory.getService().getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    assertFalse(autoPilot.isAutoModeActive());

    assertFalse(dispatcher.isLocomotiveAutomodeOn());
    assertFalse(dispatcher.isRunning());

    assertTrue(autoPilot.isOnTrack(dhg));

    List<String> passedStates = new ArrayList<>();

    StatesListener statesListener = new StatesListener(passedStates);
    dispatcher.addStateEventListener(statesListener);

    boolean started = dispatcher.startLocomotiveAutomode();
    //Should NOT start as the Autopilot is not in automode
    assertFalse(started);

    //Start Automode
    autoPilot.startAutoMode();
    pause(100);
    assertTrue(autoPilot.isAutoModeActive());

    assertFalse(dispatcher.isLocomotiveAutomodeOn());
    assertFalse(dispatcher.isRunning());

    long now = System.currentTimeMillis();
    long timeout = now + 10000;

    started = dispatcher.startLocomotiveAutomode();
    //Should start as the Autopilot is in automode
    assertTrue(started);
    pause(100);
    assertTrue(dispatcher.isRunning());
    assertTrue(dispatcher.isLocomotiveAutomodeOn());

    //After a while the loc should start running as it is on track
    int executedStates = passedStates.size();
    while (executedStates < 2 && timeout > now) {
      pause(100);
      executedStates = passedStates.size();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertEquals(2, passedStates.size());

    String dispatcherState = dispatcher.getStateName();
    assertEquals("StartState", dispatcherState);

    String routeId = dispatcher.getRouteBean().getId();
    assertEquals("[bk-1-]->[bk-4+]", routeId);
    assertTrue(dispatcher.getRouteBean().isLocked());

    //When the LocomotiveAutomode is stopped,
    //but the Autopilot Automode is still on,
    //the dispatcher thread should continue until the IN state
    //and then jump to idle instead of wait. 
    //Thread should stay alive.
    //starLocomotiveAutomode should immediately start the locomotive
    //by jumping to prepareState
    dispatcher.stopLocomotiveAutomode();

    pause(300);
    dispatcherState = dispatcher.getStateName();
    assertEquals("StartState", dispatcherState);
    assertEquals(2, passedStates.size());

    now = System.currentTimeMillis();
    timeout = now + 10000;

    //Trigger the enter sensor
    String enterSensorId = dispatcher.getEnterSensorId();
    toggleSensorInDirect(enterSensorId);
    //Wait for the state transition

    executedStates = passedStates.size();
    while (executedStates < 3 && timeout > now) {
      pause(100);
      executedStates = passedStates.size();
      now = System.currentTimeMillis();
    }

    assertEquals(3, passedStates.size());
    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("EnterBlockState", dispatcherState);

    //Let the enterState execute a few times
    pause(300);
    dispatcherState = dispatcher.getStateName();
    assertEquals("EnterBlockState", dispatcherState);

    now = System.currentTimeMillis();
    timeout = now + 10000;

    //Trigger the In Sensor
    String inSensorId = dispatcher.getInSensorId();
    while (inSensorId == null && timeout > now) {
      pause(100);
      inSensorId = dispatcher.getInSensorId();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertNotNull(inSensorId);

    toggleSensorInDirect(inSensorId);

    executedStates = passedStates.size();
    while (executedStates < 4 && timeout > now) {
      pause(100);
      executedStates = passedStates.size();
      now = System.currentTimeMillis();
    }

    assertEquals(4, passedStates.size());
    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("InBlockState", dispatcherState);

    //As the locomotive is stopped the next stae shoud be idle...
    now = System.currentTimeMillis();
    timeout = now + 10000;

    executedStates = passedStates.size();
    while (executedStates < 5 && timeout > now) {
      pause(100);
      executedStates = passedStates.size();
      now = System.currentTimeMillis();
    }

    assertEquals(5, passedStates.size());
    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("IdleState", dispatcherState);

    assertTrue(dispatcher.isRunning());
    assertFalse(dispatcher.isLocomotiveAutomodeOn());

    //Enable the locomotive again
    started = dispatcher.startLocomotiveAutomode();
    assertTrue(started);
    //pause(100);
    assertTrue(dispatcher.isRunning());
    assertTrue(dispatcher.isLocomotiveAutomodeOn());

    now = System.currentTimeMillis();
    timeout = now + 10000;

    executedStates = passedStates.size();
    while (executedStates < 6 && timeout > now) {
      pause(100);
      executedStates = passedStates.size();
      now = System.currentTimeMillis();
    }

    assertEquals(6, passedStates.size());
    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("PrepareRouteState", dispatcherState);

    now = System.currentTimeMillis();
    timeout = now + 10000;

    executedStates = passedStates.size();
    while (executedStates < 7 && timeout > now) {
      pause(100);
      executedStates = passedStates.size();
      now = System.currentTimeMillis();
    }

    assertEquals(7, passedStates.size());
    assertTrue(timeout > now);
    dispatcherState = dispatcher.getStateName();
    assertEquals("StartState", dispatcherState);

    //(force) stop the state machine
    dispatcher.stopRunning();

    assertFalse(dispatcher.isRunning());
    assertFalse(dispatcher.isLocomotiveAutomodeOn());

    now = System.currentTimeMillis();
    timeout = now + 10000;

    executedStates = passedStates.size();
    while (executedStates < 8 && timeout > now) {
      pause(100);
      executedStates = passedStates.size();
      now = System.currentTimeMillis();
    }

    assertEquals(8, passedStates.size());
    assertTrue(timeout > now);

    dispatcherState = dispatcher.getStateName();
    assertEquals("IdleState", dispatcherState);

    String lastState = passedStates.getLast();
    assertEquals("StartState", lastState);

  }

  @Test
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
    pause(50);

    Logger.debug("Dispatcher Thread Started");
    assertTrue(instance.isThreadRunning());
    assertTrue(instance.isAlive());
    assertFalse(instance.isEnableAutomode());

    instance.stopRunningThread();
    Logger.debug("Dispatcher Thread Stopped");
    assertFalse(instance.isThreadRunning());
    assertTrue(instance.isAlive());
  }

  @Test
  public void testStartStopLocomotiveAutomode2() {
    if (RunUtil.isWindows()) {
      //For some unknown reason in Windows this does not work....
      Logger.info("Skipping startStopLocomotiveAutomode2");
      return;
    }
    Logger.info("startStopLocomotiveAutomode");
    setupbk1bkNsDHG();

    BlockBean block2 = ps.getBlockByTileId("bk-2");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block2.getBlockState());

    BlockBean block3 = ps.getBlockByTileId("bk-3");
    assertEquals(BlockBean.BlockState.OUT_OF_ORDER, block3.getBlockState());

    StateMachineThread instance = dispatcher.getStateMachineThread();

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isEnableAutomode());
    assertEquals("IdleState", instance.getDispatcherStateName());
    instance.handleState();
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.setEnableAutomode(true);
    assertTrue(instance.isEnableAutomode());
    instance.handleState();
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());
    instance.handleState();
    assertEquals("StartState", instance.getDispatcherStateName());
    instance.handleState();
    assertEquals("StartState", instance.getDispatcherStateName());

    SensorBean s13 = ps.getSensor("0-0013");
    toggleSensorDirect(s13);
    instance.handleState();
    assertEquals("EnterBlockState", instance.getDispatcherStateName());
    instance.handleState();
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    SensorBean s12 = ps.getSensor("0-0012");
    toggleSensorDirect(s12);
    instance.handleState();
    assertEquals("InBlockState", instance.getDispatcherStateName());
    instance.handleState();
    assertEquals("WaitState", instance.getDispatcherStateName());

    instance.setEnableAutomode(false);
    assertFalse(instance.isEnableAutomode());

    instance.handleState();
    assertEquals("IdleState", instance.getDispatcherStateName());
  }

  private class StatesListener implements StateEventListener {

    private final List<String> stateNameList;

    StatesListener(List<String> stateNameList) {
      this.stateNameList = stateNameList;
    }

    @Override
    public void onStateChange(Dispatcher dispatcher) {

      String currentDispatcherState = dispatcher.getStateName();
      stateNameList.add(currentDispatcherState);
    }
  }

}
